package iscm.manageruser.log;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final String body;

    public CustomHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    public String getBody() {
        return body;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new DelegatingServletInputStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    }
}
