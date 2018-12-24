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
public class Complemento {
    
    private long id;
    private String nome;
    private double preco;
    private Foto foto;

    public Complemento() {
    }        

    public Complemento(long id, String nome, double preco, Foto foto) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.foto = foto;
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

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public Foto getFoto() {
        return foto;
    }

    public void setFoto(Foto foto) {
        this.foto = foto;
    }      
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Complemento)) return false;
        if (obj == this) return true;
        return this.id == ((Complemento)obj).id && this.nome.toLowerCase().equals(((Complemento)obj).getNome().toLowerCase());
    }
    
    
    @Override
    public int hashCode() {
        return (int)this.id;
    }
    
}
