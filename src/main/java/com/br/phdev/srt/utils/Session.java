/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.br.phdev.srt.utils;

import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class Session {

    private static Session instance = new Session();
    private List<HttpCookie> cookies;

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    private Session() {}

    public static void newSession(HttpURLConnection con) {
        con.addRequestProperty("Cookie", "JSESSIONID=value");
    }

    public static void validate(HttpURLConnection con) {
        Map<String, List<String>> headers = con.getHeaderFields();
        String sessionId = headers.get("Set-Cookie").get(0);
        instance.cookies = HttpCookie.parse(sessionId);
    }

    public static void get(HttpURLConnection con) {
        if (instance.cookies != null)
            con.addRequestProperty("Cookie", instance.cookies.get(0).getName() + "=" + instance.cookies.get(0).getValue());
    }

}
