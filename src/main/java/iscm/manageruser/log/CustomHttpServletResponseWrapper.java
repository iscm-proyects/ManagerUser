package iscm.manageruser.log;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class CustomHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintWriter writer = new PrintWriter(outputStream);

    public CustomHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new iscm.manageruser.log.DelegatingServletOutputStream(outputStream);
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public void flushBuffer() {
        try {
            writer.flush();
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to flush response buffer", e);
        }
    }

    public String getBody() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
