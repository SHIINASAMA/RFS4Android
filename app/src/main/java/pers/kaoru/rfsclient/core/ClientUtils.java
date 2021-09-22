package pers.kaoru.rfsclient.core;

import java.io.IOException;
import java.net.Socket;

public class ClientUtils {

    public static Response ListShow(String host, int port, String source, String token) throws IOException {
        Request request = new Request();
        request.setMethod(RequestMethod.LIST_SHOW);
        request.setHeader("source", source);
        request.setHeader("token", token);

        Socket socket = new Socket(host, port);
        WebUtils.WriteRequest(socket, request);
        return WebUtils.ReadResponse(socket);
    }

    public static Response Remove(String host, int port, String source, String token) throws IOException {
        Request request = new Request();
        request.setMethod(RequestMethod.REMOVE);
        request.setHeader("source", source);
        request.setHeader("token", token);

        Socket socket = new Socket(host, port);
        WebUtils.WriteRequest(socket, request);
        return WebUtils.ReadResponse(socket);
    }

    public static Response Copy(String host, int port, String source, String destination, String token) throws IOException {
        Request request = new Request();
        request.setMethod(RequestMethod.COPY);
        request.setHeader("source", source);
        request.setHeader("destination", destination);
        request.setHeader("token", token);

        Socket socket = new Socket(host, port);
        WebUtils.WriteRequest(socket, request);
        return WebUtils.ReadResponse(socket);
    }

    public static Response Move(String host, int port, String source, String destination, String token) throws IOException {
        Request request = new Request();
        request.setMethod(RequestMethod.MOVE);
        request.setHeader("source", source);
        request.setHeader("destination", destination);
        request.setHeader("token", token);

        Socket socket = new Socket(host, port);
        WebUtils.WriteRequest(socket, request);
        return WebUtils.ReadResponse(socket);
    }

    public static Response MakeDirectory(String host, int port, String source, String token) throws IOException {
        Request request = new Request();
        request.setMethod(RequestMethod.MAKE_DIRECTORY);
        request.setHeader("source", source);
        request.setHeader("token", token);

        Socket socket = new Socket(host, port);
        WebUtils.WriteRequest(socket, request);
        return WebUtils.ReadResponse(socket);
    }

    public static Response Verify(String host, int port, String name, String pwd) throws IOException {
        String md5 = MD5Utils.GenerateMD5(pwd);
        Request request = new Request();
        request.setMethod(RequestMethod.VERIFY);
        request.setHeader("username", name);
        request.setHeader("password", md5);

        Socket socket = new Socket(host, port);
        WebUtils.WriteRequest(socket, request);
        return WebUtils.ReadResponse(socket);
    }
}
