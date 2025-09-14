package iscm.manageruser.log;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.IOException;
import java.io.InputStream;

public class DelegatingServletInputStream extends ServletInputStream {

    private final InputStream sourceStream;

    public DelegatingServletInputStream(InputStream sourceStream) {
        this.sourceStream = sourceStream;
    }

    @Override
    public boolean isFinished() {
        try {
            return sourceStream.available() == 0;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to check stream availability", ex);
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        // Not implemented
    }

    @Override
    public int read() throws IOException {
        return sourceStream.read();
    }
}
