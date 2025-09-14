package iscm.manageruser.log;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.IOException;
import java.io.OutputStream;

public class DelegatingServletOutputStream extends ServletOutputStream {

    private final OutputStream targetStream;

    public DelegatingServletOutputStream(OutputStream targetStream) {
        this.targetStream = targetStream;
    }

    @Override
    public void write(int b) throws IOException {
        targetStream.write(b);
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        // Not implemented
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
