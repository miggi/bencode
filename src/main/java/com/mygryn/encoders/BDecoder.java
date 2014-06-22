package com.mygryn.encoders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mygryn.encoders.Constants.*;

/**
 * Created by @omygryn
 */
public class BDecoder {

    private int marker = 0;
    private final InputStream inputStream;

    public BDecoder(InputStream input) {
        this.inputStream = input;
    }

    private int readNext() throws IOException {
        if (this.marker == 0) {
            this.marker = inputStream.read();
        }
        return this.marker;
    }

    public BEntry decode() throws IOException	{
        if (readNext() == -1)
            return null;

        if (isDigit(this.marker))
            return this.decodeBytes();
        else if (this.marker == DIGIT)
            return this.decodeDigit();
        else if (this.marker == LIST)
            return decodeList();
        else if (this.marker == DICT)
            return decodeMap();
        else
            throw new IllegalStateException("Can't decode. Wrong input type: " + this.marker);
    }

    private boolean isDigit (int value) {
        return (value >= '0' && value <= '9');
    }

    /**
     * Returns the next b-encoded value on the stream and makes sure it is a
     * byte array.
     * Expects to input sequence like "4:spam".
     */
    public BEntry decodeBytes() throws IOException {
        int current = readNext();
        int length = current - DIGITS_ZERO_BYTE;
        if (!isDigit(current)) {
            throw new IllegalStateException("Wrong format. We expect leading digit, Got: " + (char) current);
        }
        this.marker = 0;
        current = read();
        //reading size (only digits)
        int idx = current - DIGITS_ZERO_BYTE;
        while (isDigit(current)) {
            current = read();
            length = length  * 10 + idx; // switch number to the next order
        }

        // expecting ':' after digits
        if (current != COL) {
            throw new IllegalStateException("Wrong format. ':' required. Got: " + (char) current);
        }
        return new BEntry(read(length));
    }

    private int read() throws IOException {
        int current = inputStream.read();
        if (current == -1)
             throw new IllegalStateException("Error. End of stream!");
        return current;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public BEntry decodeDigit() throws IOException {
        int current = readNext();
        if (current != DIGIT) {
            throw new IllegalStateException("Wrong format. We expect 'i',  but got: " + (char) current);
        }

        this.marker = 0;
        current = read();

        if (current == ZERO) {
            current = read();
            if (current == END)
                return new BEntry(0);
            else
                throw new IllegalStateException("Wrong format. We expect 'e', but got: " + (char) current);
        }

        char[] chars = new char[1024];
        int position = 0;

        if (current == MINUS) {
            chars[position] = (char) current;
            position++;
            current = read();
            if (current == ZERO)
                throw new IllegalStateException("Wrong format. '-0' now allowed. ");
        }

        if (!isDigit(current))
            throw new IllegalStateException("Invalid start format. Digit is required. Got: " + (char) current);

        chars[position] = (char) current;
        position++;

        current = this.read();
        while (isDigit(current)) {
            chars[position] = (char) current;
            position++;
            current = read();
        }

        if (current != END)
            throw new IllegalStateException("Digit should end with 'e'. Got: " + (char) current);

        return new BEntry(new String(chars, 0, position));
    }


    /**
     * Decode and return BEntry<List<BEntry>>
     * @throws Exception
     */
    public BEntry decodeList() throws IOException {
        int current = readNext();
        if (current != LIST) {
            throw new IllegalStateException("Wrong format. Leading 'l' required. Got: " + (char) current);
        }

        this.marker = 0;
        List<BEntry> result = new ArrayList<BEntry>();
        current = readNext();

        while (current != END) {
            result.add(decode());
            current = readNext();
        }
        this.marker = 0;
        return new BEntry(result);
    }

    /**
     * Decode and return BEntry<Map<BEntry>>
     * @throws Exception
     */
    public BEntry decodeMap() throws IOException {
        int current = this.readNext();
        if (current != DICT) {
            throw new IllegalStateException("Wrong format. Leading 'd' required. Got: " + (char) current);
        }
        this.marker = 0;

        Map<String, BEntry> result = new HashMap<String, BEntry>();
        current = readNext();
        while (current != END) {
            String key = decode().getString(); // always strings
            result.put(key, decode());
            current = readNext();
        }
        this.marker = 0;
        return new BEntry(result);
    }

    /**
     * Returns a byte[] containing length valid bytes from stream,
     * starting with zero offset.
     */
    private byte[] read(int length) throws IOException {
        byte[] bytes = new byte[length];
        int offset = 0;
        while (offset < length) {
            int chr = inputStream.read(bytes, offset, length - offset);
            if (chr == -1)
                throw new IllegalStateException("Error. End of stream");
            offset += chr;
        }
        return bytes;
    }

}
