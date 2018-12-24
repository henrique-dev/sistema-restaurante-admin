/*
 * Copyright (C) Paulo Henrique Goncalves Bacelar, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Paulo Henrique Gonacalves Bacelar <henrique.phgb@gmail.com>, Dezembro 2018
 */
package com.br.phdev.srt.models;

import java.io.File;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar <henrique.phgb@gmail.com>
 */
public class Foto {
    
    private long id;    
    private File arquivo;

    public Foto() {
    }        

    public Foto(long id, File arquivo) {
        this.id = id;
        this.arquivo = arquivo;
    }    

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public File getArquivo() {
        return arquivo;
    }

    public void setArquivo(File arquivo) {
        this.arquivo = arquivo;
    }    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Foto)) return false;
        if (obj == this) return true;
        return this.id == ((Foto)obj).id && this.arquivo.getAbsolutePath().equals(((Foto)obj).arquivo.getAbsolutePath());
    }
    
    
    @Override
    public int hashCode() {
        return (int)this.id;
    }
    
}
