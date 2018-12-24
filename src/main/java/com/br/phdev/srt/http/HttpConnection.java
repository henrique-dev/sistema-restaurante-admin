/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.br.phdev.srt.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class HttpConnection {
    
    public static final int SC_LOGIN_ERROR = 1000;
    
    private final String urlContext = "http://localhost:8084/sr/";
    
    public HttpURLConnection getConnection(String urlController) {
        try {
            String url = this.urlContext + urlController;
            URL urlToConnect = new URL(url);
            return (HttpURLConnection) urlToConnect.openConnection();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
