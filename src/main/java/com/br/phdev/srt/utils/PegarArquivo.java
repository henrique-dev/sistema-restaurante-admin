/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.br.phdev.srt.utils;

import javax.swing.JFileChooser;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar <henrique.phgb@gmail.com>
 */
public class PegarArquivo extends JFileChooser {

    public PegarArquivo() {
        super.setFileSelectionMode(JFileChooser.FILES_ONLY);
        super.setMultiSelectionEnabled(false);
        super.addChoosableFileFilter(new ImageFilter());
        super.setAcceptAllFileFilterUsed(false);
        super.setFileView(new ImageFileView());
        super.setAccessory(new ImagePreview(this));
    }

}
