(ns multistream.codec.text
  "Text codec which uses UTF-8 to serialize strings."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [multistream.codec :as codec :refer [defcodec defdecoder defencoder]])
  (:import
    (java.io
      InputStream
      InputStreamReader
      OutputStream
      OutputStreamWriter
      StringWriter)
    java.nio.charset.Charset))


(def ^:const header-prefix
  "/text/")


(defn- header->charset
  ^Charset
  [header]
  (when (and (string? header) (not= header header-prefix))
    (Charset/forName (subs header (count header-prefix)))))


(defn- charset->header
  [charset]
  (str header-prefix charset))



;; ## Text Codec

(defencoder TextEncoderStream
  [^OutputStreamWriter writer
   ^Charset charset]

  (write!
    [this value]
    (let [content (str value)]
      (.write writer content)
      (.flush writer)
      (count (.getBytes content charset)))))


; TODO: make this read lines instead?
(defdecoder TextDecoderStream
  [^InputStreamReader reader
   ^chars buffer]

  (read!
    [this]
    (let [len (.read reader buffer)]
      (cond
        (neg? len)
          (codec/eof-error!)

        (pos? len)
          (String. buffer 0 len)

        :else nil))))


(defcodec TextCodec
  [^Charset default-charset buffer-size]

  (processable?
    [this header]
    (str/starts-with? header header-prefix))


  (select-header
    [this selector]
    (-> (header->charset selector)
        (or default-charset)
        (charset->header)))


  (encode-byte-stream
    [this selector output-stream]
    (let [charset (or (header->charset selector) default-charset)]
      (->TextEncoderStream
        (OutputStreamWriter. ^OutputStream output-stream charset)
        charset)))


  (decode-byte-stream
    [this header input-stream]
    (let [charset (or (header->charset header) default-charset)
          header (charset->header charset)]
      (->TextDecoderStream
        (InputStreamReader. ^InputStream input-stream charset)
        (char-array buffer-size)))))


(defn text-codec
  "Creates a new text codec. If a charset is not provided, it defaults to UTF-8."
  [& {:as opts}]
  (map->TextCodec
    (merge {:default-charset (Charset/forName "UTF-8")
            :buffer-size 512}
           opts)))
