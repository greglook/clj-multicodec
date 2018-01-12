(ns multistream.codec.bin
  "Binary codec which simply encodes and decodes raw byte sequences."
  (:require
    [clojure.java.io :as io]
    [multistream.codec :as codec :refer [defcodec defdecoder defencoder]])
  (:import
    (java.io
      InputStream
      OutputStream)))


(def ^:const header "/bin/")



;; ## Encoding Protocol

(defprotocol BinaryData
  "Protocol for values which can be encoded directly as a binary sequence."

  (write-bytes!
    [data output]
    "Writes the binary data to the given output stream. Returns the number of
    bytes written."))


(extend-protocol BinaryData

  (class (byte-array 0))

  (write-bytes!
    [array ^OutputStream output]
    (.write output ^bytes array)
    (count array)))



;; ## Binary Codec

(defencoder BinaryEncoderStream
  [^OutputStream output]

  (write!
    [this value]
    (write-bytes! value output)))


(defdecoder BinaryDecoderStream
  [^InputStream input ^bytes buffer eof]

  (read!
    [this]
    (let [len (.read input buffer)]
      (cond
        (neg? len)
          (if (some? eof)
            eof
            (throw (ex-info "End of stream reached"
                            {:type ::eof})))

        (pos? len)
          (let [data (byte-array len)]
            (System/arraycopy buffer 0 data 0 len)
            data)

        :else nil))))


(defcodec BinaryCodec
  [header buffer-size eof]

  (encode-byte-stream
    [this _ output-stream]
    (codec/write-header! output-stream header)
    (->BinaryEncoderStream output-stream))

  (decode-byte-stream
    [this _ input-stream]
    (->BinaryDecoderStream
      input-stream
      (byte-array buffer-size)
      eof)))


(defn bin-codec
  "Creates a new binary codec. A number may be given to specify the buffer size
  in bytes for decoding operations."
  [& {:as opts}]
  (map->BinaryCodec (merge {:buffer-size 1024} opts)))