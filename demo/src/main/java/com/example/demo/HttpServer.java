package com.example.demo;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    static Map<String,WebMethod> endPoints =new HashMap();
    static String staticFolder = "";
    static String appPrefix = "/App";

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(35000);
            System.out.println("Servidor iniciado en puerto 35000...");
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            return;
        }

        boolean running = true;

        while (running) {
            try {
                System.out.println("Listo para recibir ...");

                Socket clientSocket = serverSocket.accept();

                new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

            } catch (IOException e) {
                System.err.println("Accept failed.");
            }
        }

        serverSocket.close();
    }


    public static void get(String path,WebMethod wm){
        endPoints.put(path,wm);
    }

    public static void staticfiles(String folder){
        staticFolder = folder;
    }

    private static void serveStaticFile(String reqpath, OutputStream rawOut, PrintWriter out) throws IOException {

        InputStream is = HttpServer.class.getClassLoader()
                .getResourceAsStream(staticFolder.substring(1) + reqpath);

        if (is != null) {

            byte[] fileData = is.readAllBytes();
            is.close();

            String contentType = getContentType(reqpath);

            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: " + contentType + "\r\n");
            out.print("Content-Length: " + fileData.length + "\r\n");
            out.print("\r\n");
            out.flush();

            rawOut.write(fileData);
            rawOut.flush();

        } else {

            String body = "<h1>404 Not Found</h1>";
            String response = buildHttpResponse("404 Not Found", "text/html", body);
            out.print(response);
            out.flush();
        }
    }

    private static void handleClient(Socket clientSocket) {

        PrintWriter out = null;
        BufferedReader in = null;
        OutputStream rawOut = null;

        try {

            rawOut = clientSocket.getOutputStream();
            out = new PrintWriter(rawOut, false);
            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            boolean firstLine = true;
            String reqpath = "";
            URI requesturi = null;

            // Leer request
            while ((inputLine = in.readLine()) != null) {

                if (firstLine) {
                    String[] reqTokens = inputLine.split(" ");
                    String method = reqTokens[0];

                    if (!method.equals("GET")) {
                        String response = buildHttpResponse(
                                "405 Method Not Allowed",
                                "text/html",
                                "<h1>405 Method Not Allowed</h1>");
                        out.print(response);
                        out.flush();
                        return;
                    }

                    requesturi = new URI(reqTokens[1]);
                    reqpath = requesturi.getPath();
                    firstLine = false;
                }

                if (!in.ready()) break;
            }

            // Manejo de root
            if (reqpath.equals("/")) {
                reqpath = "/index.html";
            }

            // Manejo de prefijo App
            if (reqpath.startsWith(appPrefix)) {
                reqpath = reqpath.substring(appPrefix.length());
            }

            String query = requesturi != null ? requesturi.getQuery() : null;
            HttpRequest req = new HttpRequest(query);
            HttpResponse res = new HttpResponse();

            WebMethod wm = endPoints.get(reqpath);

            if (wm != null) {

                String body = wm.execute(req, res);
                String response = buildHttpResponse(
                        "200 OK",
                        "text/html",
                        body);

                out.print(response);
                out.flush();

            } else {

                serveStaticFile(reqpath, rawOut, out);
            }

        } catch (Exception e) {

            if (out != null) {
                String response = buildHttpResponse(
                        "500 Internal Server Error",
                        "text/html",
                        "<h1>500 Internal Server Error</h1>");
                out.print(response);
                out.flush();
            }

        } finally {

            try { if (out != null) out.close(); } catch (Exception ignored) {}
            try { if (in != null) in.close(); } catch (Exception ignored) {}
            try { if (clientSocket != null) clientSocket.close(); } catch (Exception ignored) {}
        }
    }



    private static String buildHttpResponse(String status, String contentType, String body) {

        return "HTTP/1.1 " + status + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.getBytes().length + "\r\n"
                + "\r\n"
                + body;
    }

    private static String getContentType(String path) {

        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg")) return "image/jpeg";

        return "text/plain";
    }

}