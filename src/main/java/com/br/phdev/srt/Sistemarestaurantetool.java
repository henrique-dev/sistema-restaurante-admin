/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.br.phdev.srt;

import com.br.phdev.srt.dao.DataDAO;
import com.br.phdev.srt.exception.DAOException;
import com.br.phdev.srt.http.HttpConnection;
import com.br.phdev.srt.models.Item;
import com.br.phdev.srt.models.ListaItens;
import com.br.phdev.srt.utils.UrlAttribute;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar <henrique.phgb@gmail.com>
 */
public class Sistemarestaurantetool {

    /**
     * @param args the command line arguments
     */
    public static void main_(String[] args) {
        try {
            HttpURLConnection conexao = new HttpConnection().getConnection("InfoItem");
            DataDAO dataDAO = new DataDAO(conexao);
            dataDAO.sendJSON("{\"id\":1}");
            String resposta = dataDAO.retrieveString();
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(resposta);            
            /*
            ListaItens listaItens = objectMapper.readValue(resposta, new TypeReference<ListaItens>(){});            
            for (Item item : listaItens.getItens()) {
                System.out.println(item);
            }*/
            Item item = objectMapper.readValue(resposta, new TypeReference<Item>(){});
            System.out.println(item);
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        /*
        try {
            HttpURLConnection conexao = new HttpConnection().getConnection("Autenticar");
            DataDAO dataDAO = new DataDAO(conexao);            
            List<UrlAttribute> attributes = new ArrayList<>();
            attributes.add(new UrlAttribute("usuario", "paulohenrique"));
            attributes.add(new UrlAttribute("senha", "12345"));
            
            String senha = "PAULO HENRIQUE";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            System.out.println(hash.length);
            
            StringBuilder hexString = new StringBuilder();
            for (int i=0; i<hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            System.out.println(hexString.toString());
            
            dataDAO.sendAttributes(attributes);
            System.out.println(dataDAO.retrieveString());
        } catch (DAOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }*/
    }
    
}
