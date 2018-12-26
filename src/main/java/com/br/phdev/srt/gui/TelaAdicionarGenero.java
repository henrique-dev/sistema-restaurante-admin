/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.br.phdev.srt.gui;

import com.br.phdev.srt.dao.DataDAO;
import com.br.phdev.srt.exception.DAOException;
import com.br.phdev.srt.http.HttpConnection;
import com.br.phdev.srt.models.Genero;
import com.br.phdev.srt.utils.Mensagem;
import com.br.phdev.srt.utils.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class TelaAdicionarGenero extends javax.swing.JFrame {    
    
    private TelaAdicionarItem telaAdicionarItem;

    private TableModelGenero modeloTabelaGenerosParaAdicionar;
    private Set<Genero> generosParaSeremAdicionados;

    private TableModelGenero modeloTabelaGenerosExistentes;
    private Set<Genero> generosExistentes;    

    /**
     * Creates new form FrameAddSpeciality
     */
    public TelaAdicionarGenero(TelaAdicionarItem telaAdicionarItem) {
        super("Adicionar gêneros");
        this.telaAdicionarItem = telaAdicionarItem;
        initComponents();
        setAllComponentsEnable(false);        
        this.generosParaSeremAdicionados = new HashSet<>();
        this.modeloTabelaGenerosParaAdicionar = new TableModelGenero();
        this.modeloTabelaGenerosParaAdicionar.setColumnIdentifiers(new String[]{"Nome"});
        this.tabela_genero_para_add.setModel(this.modeloTabelaGenerosParaAdicionar);
        this.generosExistentes = new HashSet<>();
        this.modeloTabelaGenerosExistentes = new TableModelGenero();
        this.modeloTabelaGenerosExistentes.setColumnIdentifiers(new String[]{"Nome"});
        this.tabela_generos_existentes.setModel(this.modeloTabelaGenerosExistentes);
        retrieveData();
    }

    public void retrieveData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                retrieveDataList();
            }
        }).start();
    }

    private void retrieveDataList() {
        try {
            HttpURLConnection con = new HttpConnection().getConnection("gerenciador/listar-generos");
            Session.get(con);
            String resposta = new DataDAO(con).retrieveString();
            ObjectMapper mapeador = new ObjectMapper();
            List<Genero> generos = mapeador.readValue(resposta, new TypeReference<List<Genero>>() {
            });
            con.disconnect();            
            this.modeloTabelaGenerosExistentes.getDataVector().clear();
            this.tabela_generos_existentes.updateUI();
            for (Genero genero : generos) {
                this.generosExistentes.add(genero);
                this.modeloTabelaGenerosExistentes.addRow(new Genero[]{genero});
            }
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(null, "Falha ao obter os dados, certifique-se de que você tem conexão com o sistema");
            super.dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Falha ao converter objeto");
            super.dispose();
        }
        setAllComponentsEnable(true);
    }

    public void setAllComponentsEnable(boolean enable) {
        caixatexto_nome_genero.setEnabled(enable);
        botao_salvar_e_continuar.setEnabled(enable);
        botao_salvar_e_sair.setEnabled(enable);
        botao_sair.setEnabled(enable);
        tabela_genero_para_add.setEnabled(enable);
    }

    private void adicionarGenero() {
        String nome = caixatexto_nome_genero.getText().trim();
        if (nome.equals("")) {
            return;
        }
        for (Genero gen : generosExistentes) {
            if (nome.toLowerCase().equals(gen.getNome().toLowerCase())) {
                JOptionPane.showMessageDialog(null, "Genero com este nome já exixte", "Atenção", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        Genero genero = new Genero();
        genero.setId(0);
        genero.setNome(nome);
        if (generosParaSeremAdicionados.add(genero)) {            
            modeloTabelaGenerosParaAdicionar.addRow(new Genero[]{genero});
        }
    }

    private void salvarGeneros() {
        if (generosParaSeremAdicionados.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Não há nada para salvar", "Atenção", JOptionPane.INFORMATION_MESSAGE);
        }
        HttpURLConnection con = new HttpConnection().getConnection("gerenciador/cadastrar-generos");
        Mensagem mensagem = new Mensagem();
        try {
            Session.get(con);
            ObjectMapper mapeador = new ObjectMapper();
            String json = mapeador.writeValueAsString(this.generosParaSeremAdicionados);
            DataDAO dataDAO = new DataDAO(con);
            dataDAO.sendJSON(json);
            String resposta = dataDAO.retrieveString();
            mensagem = mapeador.readValue(resposta, new TypeReference<Mensagem>() {});
            JOptionPane.showMessageDialog(null, mensagem.getDescricao(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | DAOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, mensagem.getDescricao(), "Falha", JOptionPane.ERROR_MESSAGE);
        } finally {
            con.disconnect();
        }
    }

    private boolean removerGeneros() {
        if (this.tabela_generos_existentes.getSelectedRowCount() == 0) {
            return false;
        }
        int opc = JOptionPane.showConfirmDialog(null, "Esses dados serão excluídos do sistema. Continuar?", "Atenção", JOptionPane.YES_NO_OPTION);
        if (opc == JOptionPane.YES_OPTION) {
            List<Genero> generosParaRemover = new ArrayList<>();
            int linhasSelecionadas[] = tabela_generos_existentes.getSelectedRows();
            for (int i = 0; i < linhasSelecionadas.length; i++) {
                generosParaRemover.add((Genero) modeloTabelaGenerosExistentes.getGeneroAt(linhasSelecionadas[i], 0));
            }
            if (!generosParaRemover.isEmpty()) {
                HttpURLConnection conexao = new HttpConnection().getConnection("gerenciador/remover-generos");
                Mensagem mensagem = new Mensagem();
                try {
                    Session.get(conexao);
                    ObjectMapper mapeador = new ObjectMapper();
                    String json = mapeador.writeValueAsString(generosParaRemover);
                    DataDAO dataDAO = new DataDAO(conexao);
                    dataDAO.sendJSON(json);
                    String resposta = dataDAO.retrieveString();
                    mensagem = mapeador.readValue(resposta, new TypeReference<Mensagem>() {});
                    switch(mensagem.getCodigo()) {
                        case -1:
                            JOptionPane.showMessageDialog(null, mensagem.getDescricao(), "Falha", JOptionPane.ERROR_MESSAGE);
                            return false;
                        case 0:
                            JOptionPane.showMessageDialog(null, mensagem.getDescricao(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                            break;
                    }                    
                } catch (IOException | DAOException e) {
                    e.printStackTrace();                    
                    JOptionPane.showMessageDialog(null, mensagem.getDescricao(), "Falha", JOptionPane.ERROR_MESSAGE);
                    return false;
                } finally {
                    conexao.disconnect();
                }
            }
        }
        return true;
    }

    

    private void clearFields() {
        modeloTabelaGenerosParaAdicionar.getDataVector().clear();
        generosParaSeremAdicionados.clear();
        tabela_genero_para_add.updateUI();
        caixatexto_nome_genero.setText("");
    }

    @Override
    protected void processWindowEvent(final WindowEvent e) {
        super.processWindowEvent(e);        
    }

    @Override
    public void dispose() {     
        this.telaAdicionarItem.retrieveData(TelaAdicionarItem.CarregarDados.GENERO);
        this.telaAdicionarItem.setVisible(true);
        super.dispose();        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        caixatexto_nome_genero = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela_genero_para_add = new javax.swing.JTable();
        botao_salvar_e_continuar = new javax.swing.JButton();
        botao_salvar_e_sair = new javax.swing.JButton();
        botao_sair = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabela_generos_existentes = new javax.swing.JTable();
        botao_remover_genero_existsnte = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Adicionar gênero"));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Novo gênero"));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Nome do gênero:");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        caixatexto_nome_genero.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        caixatexto_nome_genero.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                caixatexto_nome_generoActionPerformed1(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jButton1.setText("Adicionar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(caixatexto_nome_genero, javax.swing.GroupLayout.PREFERRED_SIZE, 461, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(caixatexto_nome_genero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Gêneros a serem adicionados"));

        tabela_genero_para_add.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tabela_genero_para_add.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Gênero"
            }
        ));
        jScrollPane1.setViewportView(tabela_genero_para_add);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        botao_salvar_e_continuar.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        botao_salvar_e_continuar.setText("Salvar e continuar inserindo");
        botao_salvar_e_continuar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_salvar_e_continuarActionPerformed(evt);
            }
        });

        botao_salvar_e_sair.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        botao_salvar_e_sair.setText("Salvar e sair");
        botao_salvar_e_sair.setPreferredSize(new java.awt.Dimension(201, 25));
        botao_salvar_e_sair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_salvar_e_sairActionPerformed(evt);
            }
        });

        botao_sair.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        botao_sair.setText("Voltar");
        botao_sair.setPreferredSize(new java.awt.Dimension(201, 25));
        botao_sair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_sairActionPerformed(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Gêneros existentes"));

        tabela_generos_existentes.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tabela_generos_existentes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Gênero"
            }
        ));
        jScrollPane2.setViewportView(tabela_generos_existentes);

        botao_remover_genero_existsnte.setText("Remover selecionada(s)");
        botao_remover_genero_existsnte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_remover_genero_existsnteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                    .addComponent(botao_remover_genero_existsnte, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botao_remover_genero_existsnte)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(botao_salvar_e_continuar, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(botao_salvar_e_sair, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botao_sair, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botao_salvar_e_continuar)
                    .addComponent(botao_salvar_e_sair, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botao_sair, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void caixatexto_nome_generoActionPerformed1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_caixatexto_nome_generoActionPerformed1
        // TODO add your handling code here:
    }//GEN-LAST:event_caixatexto_nome_generoActionPerformed1

    private void botao_salvar_e_continuarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_salvar_e_continuarActionPerformed
        this.salvarGeneros();
        this.clearFields();
        this.setAllComponentsEnable(false);
        this.retrieveData();
    }//GEN-LAST:event_botao_salvar_e_continuarActionPerformed

    private void botao_salvar_e_sairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_salvar_e_sairActionPerformed
        this.salvarGeneros();
        this.dispose();
    }//GEN-LAST:event_botao_salvar_e_sairActionPerformed

    private void botao_sairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_sairActionPerformed
        this.dispose();
    }//GEN-LAST:event_botao_sairActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.adicionarGenero();
        this.caixatexto_nome_genero.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void botao_remover_genero_existsnteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_remover_genero_existsnteActionPerformed
        if (this.removerGeneros()) {
            this.clearFields();
            this.setAllComponentsEnable(false);
            this.retrieveData();            
        }
    }//GEN-LAST:event_botao_remover_genero_existsnteActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarGenero.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarGenero.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarGenero.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarGenero.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaAdicionarGenero(null).setVisible(true);
            }
        });
    }

    private class TableModelGenero extends DefaultTableModel {

        public TableModelGenero() {
            super(new String[]{"Nome"}, 0);
        }

        public Genero getGeneroAt(int i, int i1) {
            return (Genero) super.getValueAt(i, i1);
        }

        @Override
        public Object getValueAt(int i, int i1) {
            return ((Genero) super.getValueAt(i, i1)).getNome();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botao_remover_genero_existsnte;
    private javax.swing.JButton botao_sair;
    private javax.swing.JButton botao_salvar_e_continuar;
    private javax.swing.JButton botao_salvar_e_sair;
    private javax.swing.JTextField caixatexto_nome_genero;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tabela_genero_para_add;
    private javax.swing.JTable tabela_generos_existentes;
    // End of variables declaration//GEN-END:variables
}
