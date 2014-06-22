package com.mygryn.encoders;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BEntry {

    private final Object _obj;

     /* Constructors */

    public BEntry(int value) {this._obj = new Integer(value);}
    public BEntry(long value) {this._obj = new Long(value);}
    public BEntry(Number value) {this._obj = value;}
    public BEntry(List<BEntry> value) {this._obj = value;}
    public BEntry(Map<String, BEntry> value) {this._obj = value;}
    public BEntry(byte[] value) {this._obj = value;}
    public BEntry(String value) throws UnsupportedEncodingException {
        this._obj = value.getBytes(Constants.DEFAULT_CHARSET);
    }

    public Object get() {
        return this._obj;
    }

    public String getString() throws UnsupportedEncodingException {
        return new String(this.getBytes(), Constants.DEFAULT_CHARSET);
    }

    /**
     * Returns bytes[] value.
     */
    public byte[] getBytes() {
        return (byte[]) this._obj;
    }

    /**
     * Returns Number value.
     */
    public Number getNumber() {
        return (Number) this._obj;
    }

    /**
     * Returns int value.
     */
    public int getInt() {
        return this.getNumber().intValue();
    }

    /**
     * Returns long value.
     */
    public long getLong() {
        return this.getNumber().longValue();
    }

    /**
     * Returns List value.
     */
    public List<BEntry> getList() {
        if (this._obj instanceof ArrayList) {
            return (ArrayList<BEntry>) this._obj;
        }
        return new ArrayList<BEntry>();
    }

    /**
     * Returns Map value.
     */
    public Map<String, BEntry> getMap() {
        if (this._obj instanceof Map) {
            return (Map<String, BEntry>) this._obj;
        }
        return new HashMap<String, BEntry>();
    }
}
