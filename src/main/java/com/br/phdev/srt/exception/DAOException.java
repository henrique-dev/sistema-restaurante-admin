/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.br.phdev.srt.exception;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar <henrique.phgb@gmail.com>
 */
public class DAOException extends Exception {
    
    public DAOException(Exception e) {
        super(e);
    }
    
    public DAOException(String message) {
        super(message);
    }
    
    public DAOException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
}
