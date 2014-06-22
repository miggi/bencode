package com.mygryn.encoders;

import static com.mygryn.encoders.Constants.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;

public class BEncoder {

    private final String charset;
    private OutputStream out;

    public BEncoder(OutputStream outputStream) {
        this.out = outputStream;
        this.charset = DEFAULT_CHARSET;
    }

    public BEncoder(String charset, OutputStream outputStream) {
        this.out = outputStream;
        if (Charset.availableCharsets().containsKey(charset))
            this.charset = charset;
        else
            throw new IllegalArgumentException("Unsupported Charset type: " + charset);
    }

    /**
     * BEncode string.
     * @param str
     * @throws IOException
     */
    void encode(String str) throws IOException {
        encode(str.getBytes(charset));
    }

    /**
     * BEncode bytes array.
     * @param bytes
     * @throws IOException
     */
    public void encode(byte[] bytes) throws IOException {
        out.write(Integer.toString(bytes.length).getBytes(charset));
        out.write(COL);
        out.write(bytes);
    }

    /**
     * BEncode Number chars.
     * @param number
     * @throws IOException
     */
    void encode(Number number) throws IOException {
        out.write(DIGIT);
        out.write(number.toString().getBytes(charset));
        out.write(END);
    }

    /**
     * BEncode list of BEntry objects.
     * @param entry
     * @throws IOException
     */
    public void encode(List<BEntry> entry) throws IOException {
        out.write(LIST);
        for (BEntry value : entry) {
            encode(value);
        }
        out.write(END);
    }

    /**
     * BEncode map of BEntry objects.
     * @param map
     * @throws IOException
     */
    public void encode(Map<String, BEntry> map) throws IOException {
        out.write(DICT);
        ;
        //sort keys in lexicographical order
        for (Map.Entry<String, BEntry> entry : new TreeMap<String, BEntry>(map).entrySet()) {
            encode(entry.getKey());
            encode(entry.getValue());
        }
        out.write(END);
    }

    public void encode(Object obj) throws IOException, IllegalArgumentException {

        if (obj instanceof BEntry) {
            obj = ((BEntry) obj).get();
        }

        if (obj instanceof String) {
            encode((String) obj);
        } else if (obj instanceof byte[]) {
            encode((byte[]) obj);
        } else if (obj instanceof Number) {
            encode((Number) obj);
        } else if (obj instanceof List) {
            encode((List<BEntry>) obj);
        } else if (obj instanceof Map) {
            encode((Map<String, BEntry>) obj);
        } else {
            throw new IllegalArgumentException("Value not supported: " + obj.toString());
        }
    }

}
