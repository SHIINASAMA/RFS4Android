package pers.kaoru.rfsclient.core;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WebUtils {

    public static void WriteRequest(Socket socket, Request request) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write((request.getMethod().toString() + "\r\n").getBytes(StandardCharsets.UTF_8));
        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            out.write((header.getKey() + ": " + header.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    public static Request ReadRequest(Socket socket) throws IOException {
        Request request = new Request();
        InputStream in = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String str = reader.readLine();
        switch (str) {
            case "LIST_SHOW":
                request.setMethod(RequestMethod.LIST_SHOW);
                break;
            case "REMOVE":
                request.setMethod(RequestMethod.REMOVE);
                break;
            case "COPY":
                request.setMethod(RequestMethod.COPY);
                break;
            case "MOVE":
                request.setMethod(RequestMethod.MOVE);
                break;
            case "MAKE_DIRECTORY":
                request.setMethod(RequestMethod.MAKE_DIRECTORY);
                break;
            case "DOWNLOAD":
                request.setMethod(RequestMethod.DOWNLOAD);
                break;
            case "UPLOAD":
                request.setMethod(RequestMethod.UPLOAD);
                break;
            case "VERIFY":
                request.setMethod(RequestMethod.VERIFY);
                break;
            default:
                request.setMethod(RequestMethod.ERROR);
                break;
        }
        while (!(str = reader.readLine()).equals("")) {
            String[] substr = str.split(": ");
            request.setHeader(substr[0], substr[1]);
        }

        return request;
    }

    public static void WriteResponse(Socket socket, Response response) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write((response.getCode() + "\r\n").getBytes(StandardCharsets.UTF_8));
        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            out.write((header.getKey() + ": " + header.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    public static Response ReadResponse(Socket socket) throws IOException {
        Response response = new Response();
        InputStream in = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String str = reader.readLine();
        switch (str) {
            case "OK":
                response.setCode(ResponseCode.OK);
                break;
            case "FAIL":
                response.setCode(ResponseCode.FAIL);
                break;
            default:
                response.setCode(ResponseCode.ERROR);
                break;
        }

        while (!(str = reader.readLine()).equals("")) {
            String[] substr = str.split(": ");
            response.setHeader(substr[0], substr[1]);
        }

        return response;
    }
}
