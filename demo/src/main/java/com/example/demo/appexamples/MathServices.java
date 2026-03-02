package com.example.demo.appexamples;
import com.example.demo.HttpServer;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.example.demo.HttpServer.get;
public class MathServices {
    public static void main(String[] args) throws Exception {

        HttpServer.staticfiles("/webroot");

        get("/hello",(req,res) -> "Hello " + req.getValue("name"));

        get("/pi",(req,res) -> String.valueOf(Math.PI));

        get("/e", (req,res) -> String.valueOf(Math.E));
        
        HttpServer.main(args);
    }

    private static String euler() {
        return "e= "+ Math.E;
    }

}
