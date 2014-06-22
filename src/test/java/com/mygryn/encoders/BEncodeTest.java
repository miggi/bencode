package com.mygryn.encoders;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BEncodeTest {

    @Test
    public void testEncode() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[10];
        int idx = 0;
        for (int i = 48; i < 58; i++) { // 0..9 chars
            bytes[idx] = (byte) i;
            idx++;
        }

        BEncoder encoder = new BEncoder(outputStream);

        encoder.encode(bytes);
        assertEquals("10:0123456789", outputStream.toString());
        outputStream.reset();

        encoder.encode("Hello");
        assertEquals("5:Hello", outputStream.toString());
        outputStream.reset();

        encoder.encode(1000);
        assertEquals("i1000e", outputStream.toString());
        outputStream.reset();

        encoder.encode(808080L);
        assertEquals("i808080e", outputStream.toString());
        outputStream.reset();

        encoder.encode(-10000);
        assertEquals("i-10000e", outputStream.toString());
        outputStream.reset();

        encoder.encode(new BEntry(100));
        assertEquals("i100e", outputStream.toString());
        outputStream.reset();

        encoder.encode(new BEntry(9999999L));
        assertEquals("i9999999e", outputStream.toString());
        outputStream.reset();

        encoder.encode(new BEntry(bytes));
        assertEquals("10:0123456789", outputStream.toString());
        outputStream.reset();

        encoder.encode(new BEntry(bytes));
        assertEquals("10:0123456789", outputStream.toString());
        outputStream.reset();

        Map<String, BEntry> map = new HashMap<String, BEntry>();
        map.put("b", new BEntry("hello"));
        map.put("a", new BEntry("bencode"));
        map.put("z", new BEntry("entry"));

        encoder.encode(map);
        assertEquals("d1:a7:bencode1:b5:hello1:z5:entrye", outputStream.toString());
        outputStream.reset();

        List<BEntry> list = new ArrayList<BEntry>();
        list.add(new BEntry("hello"));
        list.add(new BEntry(1234L));
        list.add(new BEntry(bytes));

        encoder.encode(list);
        assertEquals("l5:helloi1234e10:0123456789e", outputStream.toString());
        outputStream.reset();


        Throwable throwable = null;
        try {
            new BEncoder("nonexisting", outputStream);
        } catch (Throwable ex) {
            throwable = ex;
        }
        assertTrue(throwable instanceof IllegalArgumentException);
    }

    @Test
    public void testDecode() throws IOException {
        //byte array
        BDecoder bDecoder = new BDecoder(toInputStream("10:0123456789"));
        BEntry decoded = bDecoder.decode();

        assertNotNull(decoded);
        assertTrue(decoded.get() instanceof byte[]);
        assertEquals(10, ((byte[]) decoded.get()).length);
        assertEquals('0', ((byte[]) decoded.get())[0]);
        assertEquals('1', ((byte[]) decoded.get())[1]);
        assertEquals('9', ((byte[]) decoded.get())[9]);

        //dictionary
        bDecoder = new BDecoder(toInputStream("d1:a7:bencode1:b5:hello1:z5:entrye"));
        decoded = bDecoder.decode();

        assertNotNull(decoded);
        assertTrue(decoded.get() instanceof Map);
        byte[] bytes = new byte[]{101, 110, 116, 114, 121};
        assertTrue(((Map) decoded.get()).get("b") instanceof BEntry);
        assertTrue(((Map) decoded.get()).get("a") instanceof BEntry);
        BEntry zEntry = (BEntry) ((Map) decoded.get()).get("z");

        assertEquals(bytes[0], zEntry.getBytes()[0]);
        assertEquals(bytes[1], zEntry.getBytes()[1]);
        assertEquals(bytes[2], zEntry.getBytes()[2]);

        //digit
        bDecoder = new BDecoder(toInputStream("i808080e"));
        decoded = bDecoder.decode();

        assertNotNull(decoded);
        assertEquals("808080", decoded.getString());

        //digit
        bDecoder = new BDecoder(toInputStream("i-10000e"));
        decoded = bDecoder.decode();

        assertNotNull(decoded);
        assertEquals("-10000", decoded.getString());
    }

    private InputStream toInputStream(String str) {
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }


}
