/*
 * Copyright (C) Paulo Henrique Goncalves Bacelar, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Paulo Henrique Gonacalves Bacelar <henrique.phgb@gmail.com>, Dezembro 2018
 */
package com.br.phdev.srt.models;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar <henrique.phgb@gmail.com>
 */
public class Genero {
    
    private long id;
    private String nome;

    public Genero() {
    }        

    public Genero(long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }        
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Genero)) return false;
        if (obj == this) return true;
        return this.id == ((Genero)obj).id && this.nome.toLowerCase().equals(((Genero)obj).getNome().toLowerCase());
    }
    
    
    @Override
    public int hashCode() {
        return (int)this.id;
    }

    @Override
    public String toString() {
        return this.nome;
    }        
    
}
