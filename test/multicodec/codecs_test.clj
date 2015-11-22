(ns multicodec.codecs-test
  (:require
    [clojure.test :refer :all]
    [multicodec.core :as mc]
    [multicodec.header :as mh]
    [multicodec.codecs :as codecs])
  (:import
    (java.io
      ByteArrayInputStream
      ByteArrayOutputStream)))


(defn mock-codec
  [tag header]
  (reify
    clojure.lang.ILookup
    (valAt [_ k]
      (when (= :header k) header))
    mc/Encoder
    (encode! [_ output value]
      (let [content (.getBytes (pr-str value))]
        (.write output content)
        (count content)))
    mc/Decoder
    (decode! [_ input]
      [tag (slurp input)])))


(deftest header-codec
  (let [foo (mock-codec :foo "/foo")
        wrapped (codecs/wrap-headers foo)]
    (testing "codec construction"
      (is (= "/foo" (:header wrapped))
          "header should inherit wrapped codec by default") 
      (let [wrapped (codecs/wrap-headers foo "/bar")]
        (is (= "/bar" (:header wrapped))
            "header should be settable with second arg")))
    (testing "encoding roundtrip"
      (let [encoded (mc/encode wrapped 1234)]
        (is (= "/foo" (mh/read-header! (ByteArrayInputStream. encoded)))
            "should write codec header to content")
        (is (= [:foo "1234"] (mc/decode wrapped encoded))
            "should read header and delegate to codec")))
    (testing "bad header"
      (let [baos (ByteArrayOutputStream.)]
        (codecs/write-header-encoded! "/bar" foo baos :abc)
        (is (thrown? RuntimeException
                     (mc/decode wrapped (.toByteArray baos))))))))


(deftest multiplex-codec
  (testing "codec construction"
    (is (thrown? IllegalArgumentException (codecs/mux-codec))
        "construction with no codecs should throw exception")
    (is (thrown? IllegalArgumentException (codecs/mux-codec (mock-codec :foo nil)))
        "construction with no-header arg should throw exception"))
  (testing "encoding with no selected codec"
    (let [mux (assoc (codecs/mux-codec (mock-codec :foo "/bar"))
                     :select-encoder (constantly nil))]
      (is (thrown? RuntimeException (mc/encode! mux nil nil)))))
  (let [mux (codecs/mux-codec
              (mock-codec :foo "/foo")
              (mock-codec :bar "/bar"))]
    (testing "encoding roundtrip"
      (let [encoded (mc/encode mux 1234)]
        (is (= 10 (count encoded)) "should write the correct number of bytes")
        (is (= "/foo" (mh/read-header! (ByteArrayInputStream. encoded))))
        (is (= [:foo "1234"] (mc/decode mux encoded)))))
    (testing "no-matching decoder"
      (let [baos (ByteArrayOutputStream.)]
        (mh/write-header! baos "/baz/qux")
        (.write baos (.getBytes "abcd"))
        (is (thrown? RuntimeException
                     (mc/decode mux (.toByteArray baos))))))))


(deftest bin-codec
  (let [bin (codecs/bin-codec)
        content (byte-array 10)]
    (.nextBytes (java.security.SecureRandom.) content)
    (let [encoded (mc/encode bin content)]
      (testing "encoding"
        (is (= (count encoded) (count content))
            "should encode bytes one-to-one")
        (is (every? true? (map = content encoded))
            "should return same bytes as encoded"))
      (testing "decoding"
        (let [decoded (mc/decode bin encoded)]
          (is (= (count decoded) (count content))
              "should decode bytes one-to-one")
          (is (every? true? (map = content decoded))
              "should decode same bytes as content"))))))
