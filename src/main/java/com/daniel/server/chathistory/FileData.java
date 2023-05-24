package com.daniel.server.chathistory;

import java.io.Serializable;
import java.util.ArrayList;

public class FileData implements Serializable {

    private byte[] data;
    private int messagesCount;

    public FileData(byte[] data) {
        this.data = data;
        String s = new String(data);
        String[] arrBuf = s.split("\n");
        messagesCount = arrBuf.length;
    }

    public ArrayList<Object> getDataWithOffset(int offset) {
        if (offset < 0) offset = 0;
        if (offset > messagesCount) offset = messagesCount;
        String s = new String(data);
        String[] buf = s.split("\n");
        ArrayList<Object> ret = new ArrayList<>();
        for (int i = offset; i < buf.length; i++) {
            ret.add(buf[i]);
        }
        return ret;
    }
}
