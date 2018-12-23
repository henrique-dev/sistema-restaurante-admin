/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.br.phdev.srt.dao;

import java.net.HttpURLConnection;
import java.net.ProtocolException;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar <henrique.phgb@gmail.com>
 */
public class AdminDAO {
    
    private final HttpURLConnection conexao;

    public AdminDAO(HttpURLConnection conexao) {
        this.conexao = conexao;
        try {
            this.conexao.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
    }
    
    
    
}
