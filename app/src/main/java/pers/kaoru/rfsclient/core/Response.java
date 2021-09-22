package pers.kaoru.rfsclient.core;

import java.util.TreeMap;

public class Response {

    private ResponseCode code;

    private final TreeMap<String, String> headers;

    public Response() {
        headers = new TreeMap<>();
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public final TreeMap<String, String> getHeaders() {
        return headers;
    }
}
