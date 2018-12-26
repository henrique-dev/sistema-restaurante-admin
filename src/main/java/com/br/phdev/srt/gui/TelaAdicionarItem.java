/*
 * Copyright (C) Paulo Henrique Goncalves Bacelar, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Paulo Henrique Gonacalves Bacelar <henrique.phgb@gmail.com>, Dezembro 2018
 */
package com.br.phdev.srt.gui;

import com.br.phdev.srt.dao.DataDAO;
import com.br.phdev.srt.exception.DAOException;
import com.br.phdev.srt.http.HttpConnection;
import com.br.phdev.srt.models.Complemento;
import com.br.phdev.srt.models.Foto;
import com.br.phdev.srt.models.Genero;
import com.br.phdev.srt.models.Item;
import com.br.phdev.srt.models.Tipo;
import com.br.phdev.srt.utils.ImagemParaMostrar;
import com.br.phdev.srt.utils.Mensagem;
import com.br.phdev.srt.utils.PegarArquivo;
import com.br.phdev.srt.utils.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar <henrique.phgb@gmail.com>
 */
public class TelaAdicionarItem extends javax.swing.JFrame {

    enum CarregarDados {
        TUDO, TIPO, GENERO, COMPLEMENTO, ITEM
    };

    private TableModelComplemento modeloTabelaComplementosExistentes;

    private TableModelComplemento modeloTabelaComplementosParaAdicionar;
    private Set<Complemento> complementosParaAdicionar;

    private TableModelTipo modeloTabelaTiposExistentes;

    private TableModelTipo modeloTabelaTiposParaAdicionar;
    private Set<Tipo> tiposParaSeremAdicionados;

    private TableModelItem modeloTabelaItemExistentes;
    private Set<Item> itensExistentes;

    private Set<Genero> generosExistentes;

    private ImagemParaMostrar imagemTipo1;
    private ImagemParaMostrar imagemTipo2;
    private ImagemParaMostrar imagemTipo3;
    private ImagemParaMostrar imagemTipo4;
    private ImagemParaMostrar imagemTipo5;

    private PegarArquivo pegarArquivo;

    public TelaAdicionarItem() {
        initComponents();
        Dimension tamanhoTela = Toolkit.getDefaultToolkit().getScreenSize();
        double largura = tamanhoTela.width;
        double altura = tamanhoTela.height;
        System.out.println("Largura: " + largura);
        System.out.println("Altura: " + altura);

        this.setSize(tamanhoTela);
        this.painel_rolante_principal.setSize(500, (int)altura);
        this.painel_divisor_principal.setSize(tamanhoTela);
        this.painel_divisor_principal.setDividerLocation(0.2);
        this.painel_divisor_secundario.setDividerLocation(0.7);

        this.modeloTabelaComplementosExistentes = new TableModelComplemento();
        this.tabela_complementos_existentes.setModel(this.modeloTabelaComplementosExistentes);

        this.complementosParaAdicionar = new HashSet<>();
        this.modeloTabelaComplementosParaAdicionar = new TableModelComplemento();
        this.tabela_complementos_para_serem_adicionados.setModel(this.modeloTabelaComplementosParaAdicionar);

        this.modeloTabelaTiposExistentes = new TableModelTipo();
        this.tabela_tipos_existentes.setModel(this.modeloTabelaTiposExistentes);

        this.tiposParaSeremAdicionados = new HashSet<>();
        this.modeloTabelaTiposParaAdicionar = new TableModelTipo();
        this.tabela_tipos_para_serem_adicionados.setModel(this.modeloTabelaTiposParaAdicionar);

        this.itensExistentes = new HashSet<>();
        this.modeloTabelaItemExistentes = new TableModelItem();
        this.tabela_item.setModel(this.modeloTabelaItemExistentes);

        this.generosExistentes = new HashSet<>();

        setEdicaoItem(false);
        this.botao_atualizar_item.setEnabled(false);
        this.botao_remover_item.setEnabled(false);
        this.botao_cadastrar_item.setEnabled(false);

        retrieveData(CarregarDados.TUDO);
    }

    public void retrieveData(CarregarDados opcao) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                retrieveDataList(opcao);
            }
        }).start();
    }

    private void retrieveDataList(CarregarDados opcao) {
        try {
            if (opcao == CarregarDados.GENERO || opcao == CarregarDados.TUDO) {
                carregarGeneros();
            }
            if (opcao == CarregarDados.TIPO || opcao == CarregarDados.TUDO) {
                carregarTipos();
            }
            if (opcao == CarregarDados.COMPLEMENTO || opcao == CarregarDados.TUDO) {
                carregarComplementos();
            }
            if (opcao == CarregarDados.ITEM || opcao == CarregarDados.TUDO) {
                carregarItens();
            }
            if (opcao == CarregarDados.TUDO) {
                this.botao_cadastrar_item.setEnabled(true);
            }
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(null, "Falha ao obter os dados, certifique-se de que você tem conexão com o sistema");
            super.dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Falha ao converter objeto");
            super.dispose();
        }
    }

    private void carregarGeneros() throws IOException, DAOException {
        HttpURLConnection con = new HttpConnection().getConnection("gerenciador/listar-generos");
        Session.get(con);
        String resposta = new DataDAO(con).retrieveString();
        ObjectMapper mapeador = new ObjectMapper();
        List<Genero> generos = mapeador.readValue(resposta, new TypeReference<List<Genero>>() {
        });
        con.disconnect();
        DefaultComboBoxModel<Genero> modeloCaixaOpcoesGenero = new DefaultComboBoxModel<>();
        for (Genero genero : generos) {
            this.generosExistentes.add(genero);
            modeloCaixaOpcoesGenero.addElement(genero);
        }
        this.caixa_opcoes_genero_item.setModel(modeloCaixaOpcoesGenero);
    }

    private void carregarTipos() throws IOException, DAOException {
        HttpURLConnection con = new HttpConnection().getConnection("gerenciador/listar-tipos");
        Session.get(con);
        String resposta = new DataDAO(con).retrieveString();
        ObjectMapper mapeador = new ObjectMapper();
        List<Tipo> tipos = mapeador.readValue(resposta, new TypeReference<List<Tipo>>() {
        });
        con.disconnect();
        this.modeloTabelaTiposExistentes.getDataVector().clear();
        this.tabela_tipos_existentes.updateUI();
        for (Tipo tipo : tipos) {
            this.modeloTabelaTiposExistentes.addRow(new Tipo[]{tipo});
        }
    }

    private void carregarComplementos() throws IOException, DAOException {
        HttpURLConnection con = new HttpConnection().getConnection("gerenciador/listar-complementos");
        Session.get(con);
        String resposta = new DataDAO(con).retrieveString();
        ObjectMapper mapeador = new ObjectMapper();
        List<Complemento> complementos = mapeador.readValue(resposta, new TypeReference<List<Complemento>>() {
        });
        con.disconnect();
        this.modeloTabelaComplementosExistentes.getDataVector().clear();
        this.tabela_complementos_existentes.updateUI();
        for (Complemento complemento : complementos) {
            this.modeloTabelaComplementosExistentes.addRow(new Complemento[]{complemento, complemento});
        }
    }

    private void carregarItens() throws IOException, DAOException {
        HttpURLConnection con = new HttpConnection().getConnection("gerenciador/listar-itens");
        Session.get(con);
        String resposta = new DataDAO(con).retrieveString();
        ObjectMapper mapeador = new ObjectMapper();
        List<Item> itens = mapeador.readValue(resposta, new TypeReference<List<Item>>() {
        });
        con.disconnect();
        this.modeloTabelaItemExistentes.getDataVector().clear();
        this.tabela_item.updateUI();
        for (Item item : itens) {
            this.modeloTabelaItemExistentes.addRow(new Item[]{item, item});
        }
    }

    private void setEdicaoItem(boolean estado) {
        this.campo_texto_nome_item.setEnabled(estado);
        this.campo_texto_preco_item.setEnabled(estado);
        this.area_texto_descricao_item.setEnabled(estado);
        this.caixa_opcoes_genero_item.setEnabled(estado);
        this.botao_adicionar_complemento.setEnabled(estado);
        this.botao_remover_complemento.setEnabled(estado);
        this.botao_adicionar_tipo.setEnabled(estado);
        this.botao_remover_tipo.setEnabled(estado);
        this.tabela_complementos_existentes.setEnabled(estado);
        this.tabela_tipos_existentes.setEnabled(estado);
        this.botao_salvar_item.setEnabled(estado);
        this.botao_adicionar_imagem1.setEnabled(estado);
        this.botao_adicionar_imagem2.setEnabled(estado);
        this.botao_adicionar_imagem3.setEnabled(estado);
        this.botao_adicionar_imagem4.setEnabled(estado);
        this.botao_adicionar_imagem5.setEnabled(estado);
    }

    private ImagemParaMostrar adicionarImagem(JButton botao) {
        ImagemParaMostrar imagemParaMostrar = null;
        if (this.pegarArquivo == null) {
            this.pegarArquivo = new PegarArquivo();
        }
        int opc = this.pegarArquivo.showOpenDialog(this);
        if (opc == JFileChooser.APPROVE_OPTION) {
            File arquivo = this.pegarArquivo.getSelectedFile();
            ImageIcon tmpIcon = new ImageIcon(arquivo.getPath());
            if (tmpIcon != null) {
                if (tmpIcon.getIconWidth() > 250) {
                    imagemParaMostrar = new ImagemParaMostrar(new ImageIcon(tmpIcon.getImage().getScaledInstance(250, -1, Image.SCALE_FAST)),
                            arquivo.getPath());
                } else {
                    imagemParaMostrar = new ImagemParaMostrar(tmpIcon, arquivo.getPath());
                }
                botao.setText("");
                botao.setIcon(imagemParaMostrar.getIcon());
            }
        }
        return imagemParaMostrar;
    }

    private void salvarItens() {
        List<File> files = new ArrayList<>();
        Set<Foto> fotos = new HashSet<>();
        if (imagemTipo1 != null) {
            files.add(new File(imagemTipo1.getCaminho()));
        }
        if (imagemTipo2 != null) {
            files.add(new File(imagemTipo2.getCaminho()));
        }
        if (imagemTipo3 != null) {
            files.add(new File(imagemTipo3.getCaminho()));
        }
        if (imagemTipo4 != null) {
            files.add(new File(imagemTipo4.getCaminho()));
        }
        if (imagemTipo5 != null) {
            files.add(new File(imagemTipo5.getCaminho()));
        }
        System.out.println(files.size());
        if (files.isEmpty()) {
            JOptionPane.showMessageDialog(null, "É necessário inserir pelo menos uma imagem", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nomeItem = this.campo_texto_nome_item.getText();
        String precoItemString = this.campo_texto_preco_item.getText();
        String descricaoItem = this.area_texto_descricao_item.getText();
        Genero genero = ((Genero) this.caixa_opcoes_genero_item.getSelectedItem());
        double precoItem;
        if (nomeItem.equals("") || precoItemString.equals("") || descricaoItem.equals("") || genero.equals("")) {
            JOptionPane.showMessageDialog(null, "É necessário preencher todos os campos", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            precoItem = Double.parseDouble(this.campo_texto_preco_item.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "O valor do preço informado é inválido", "Falha", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (this.tiposParaSeremAdicionados.isEmpty()) {
            JOptionPane.showMessageDialog(null, "É necessário inserir pelo menos um tipo", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Item item = new Item();
        item.setNome(nomeItem);
        item.setPreco(precoItem);
        item.setDescricao(descricaoItem);
        item.setGenero(genero);
        item.setTipos(this.tiposParaSeremAdicionados);
        if (!this.complementosParaAdicionar.isEmpty()) {
            item.setModificavel(true);
            item.setComplementos(this.complementosParaAdicionar);
        }
        Mensagem mensagem;
        HttpURLConnection conexao = null;
        try {
            for (File file : files) {
                conexao = new HttpConnection().getConnection("gerenciador/salvar-imagem");
                Session.get(conexao);
                DataDAO dataDAO = new DataDAO(conexao);
                dataDAO.sendMultiPartFile(file, null);
                String resposta = dataDAO.retrieveString();
                ObjectMapper mapeador = new ObjectMapper();
                mensagem = mapeador.readValue(resposta, new TypeReference<Mensagem>() {
                });
                fotos.add(new Foto(Integer.parseInt(mensagem.getDescricao()), null));
                conexao.disconnect();
            }
            item.setFotos(fotos);
            conexao = new HttpConnection().getConnection("gerenciador/cadastrar-item");
            Session.get(conexao);
            DataDAO dataDAO = new DataDAO(conexao);
            ObjectMapper mapeador = new ObjectMapper();
            String json = mapeador.writeValueAsString(item);
            System.out.println(json);
            dataDAO.sendJSON(json);
            String resposta = dataDAO.retrieveString();
            mensagem = mapeador.readValue(resposta, new TypeReference<Mensagem>() {
            });
            switch (mensagem.getCodigo()) {
                case -1:
                    JOptionPane.showMessageDialog(null, mensagem.getDescricao(), "Falha", JOptionPane.ERROR_MESSAGE);
                    break;
                case 0:
                    JOptionPane.showMessageDialog(null, mensagem.getDescricao(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    break;
            }
        } catch (IOException | DAOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Falha na comunicação com o servidor", "Falha", JOptionPane.ERROR_MESSAGE);
        } finally {
            conexao.disconnect();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painel_rolante_principal = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        painel_divisor_principal = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        botao_cadastrar_item = new javax.swing.JButton();
        botao_atualizar_item = new javax.swing.JButton();
        botao_remover_item = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabela_item = new javax.swing.JTable();
        painel_item = new javax.swing.JPanel();
        painel_divisor_secundario = new javax.swing.JSplitPane();
        jPanel6 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        campo_texto_nome_item = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        campo_texto_preco_item = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        area_texto_descricao_item = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        caixa_opcoes_genero_item = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tabela_tipos_existentes = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        botao_adicionar_tipo = new javax.swing.JButton();
        botao_remover_tipo = new javax.swing.JButton();
        jScrollPane9 = new javax.swing.JScrollPane();
        tabela_tipos_para_serem_adicionados = new javax.swing.JTable();
        botao_salvar_item = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        tabela_complementos_existentes = new javax.swing.JTable();
        jPanel12 = new javax.swing.JPanel();
        botao_adicionar_complemento = new javax.swing.JButton();
        botao_remover_complemento = new javax.swing.JButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        tabela_complementos_para_serem_adicionados = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        botao_adicionar_imagem1 = new javax.swing.JButton();
        botao_adicionar_imagem2 = new javax.swing.JButton();
        botao_adicionar_imagem3 = new javax.swing.JButton();
        botao_adicionar_imagem4 = new javax.swing.JButton();
        botao_adicionar_imagem5 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        painel_divisor_principal.setDividerLocation(250);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Itens existntes"));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel13.setLayout(new javax.swing.BoxLayout(jPanel13, javax.swing.BoxLayout.LINE_AXIS));

        jPanel3.setLayout(new java.awt.BorderLayout());

        botao_cadastrar_item.setText("Cadastrar item");
        botao_cadastrar_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_cadastrar_itemActionPerformed(evt);
            }
        });
        jPanel3.add(botao_cadastrar_item, java.awt.BorderLayout.PAGE_END);

        botao_atualizar_item.setText("Atualizar item");
        botao_atualizar_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_atualizar_itemActionPerformed(evt);
            }
        });
        jPanel3.add(botao_atualizar_item, java.awt.BorderLayout.PAGE_START);

        botao_remover_item.setText("Remover item");
        jPanel3.add(botao_remover_item, java.awt.BorderLayout.CENTER);

        jPanel13.add(jPanel3);

        jPanel2.add(jPanel13, java.awt.BorderLayout.PAGE_END);

        tabela_item.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tabela_item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabela_itemMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tabela_item);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        painel_divisor_principal.setLeftComponent(jPanel2);

        painel_item.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cadastrar item", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        painel_divisor_secundario.setDividerLocation(900);

        jLabel2.setText("Nome:");

        campo_texto_nome_item.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N

        jLabel3.setText("Preço:");

        campo_texto_preco_item.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N

        jLabel4.setText("Descrição:");

        area_texto_descricao_item.setColumns(20);
        area_texto_descricao_item.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        area_texto_descricao_item.setRows(3);
        jScrollPane3.setViewportView(area_texto_descricao_item);

        jLabel5.setText("Gênero:");

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipo"));

        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.LINE_AXIS));

        tabela_tipos_existentes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane5.setViewportView(tabela_tipos_existentes);

        jPanel9.add(jScrollPane5);

        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.Y_AXIS));

        botao_adicionar_tipo.setText("Adicionar >>");
        botao_adicionar_tipo.setMaximumSize(new java.awt.Dimension(150, 30));
        botao_adicionar_tipo.setMinimumSize(new java.awt.Dimension(90, 30));
        botao_adicionar_tipo.setPreferredSize(new java.awt.Dimension(90, 30));
        botao_adicionar_tipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_adicionar_tipoActionPerformed(evt);
            }
        });
        jPanel10.add(botao_adicionar_tipo);

        botao_remover_tipo.setText("<< Remover");
        botao_remover_tipo.setMaximumSize(new java.awt.Dimension(150, 30));
        botao_remover_tipo.setMinimumSize(new java.awt.Dimension(90, 30));
        botao_remover_tipo.setPreferredSize(new java.awt.Dimension(150, 30));
        botao_remover_tipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_remover_tipoActionPerformed(evt);
            }
        });
        jPanel10.add(botao_remover_tipo);

        jPanel9.add(jPanel10);

        tabela_tipos_para_serem_adicionados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane9.setViewportView(tabela_tipos_para_serem_adicionados);

        jPanel9.add(jScrollPane9);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addContainerGap())
        );

        botao_salvar_item.setText("Salvar");
        botao_salvar_item.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_salvar_itemActionPerformed(evt);
            }
        });

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Complemento"));
        jPanel8.setPreferredSize(new java.awt.Dimension(34, 184));

        jPanel11.setLayout(new javax.swing.BoxLayout(jPanel11, javax.swing.BoxLayout.LINE_AXIS));

        tabela_complementos_existentes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane10.setViewportView(tabela_complementos_existentes);

        jPanel11.add(jScrollPane10);

        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.PAGE_AXIS));

        botao_adicionar_complemento.setText("Adicionar >>");
        botao_adicionar_complemento.setMaximumSize(new java.awt.Dimension(150, 30));
        botao_adicionar_complemento.setMinimumSize(new java.awt.Dimension(90, 30));
        botao_adicionar_complemento.setPreferredSize(new java.awt.Dimension(150, 30));
        botao_adicionar_complemento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_adicionar_complementoActionPerformed(evt);
            }
        });
        jPanel12.add(botao_adicionar_complemento);

        botao_remover_complemento.setText("<< Remover");
        botao_remover_complemento.setMaximumSize(new java.awt.Dimension(150, 30));
        botao_remover_complemento.setMinimumSize(new java.awt.Dimension(90, 30));
        botao_remover_complemento.setPreferredSize(new java.awt.Dimension(150, 30));
        botao_remover_complemento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_remover_complementoActionPerformed(evt);
            }
        });
        jPanel12.add(botao_remover_complemento);

        jPanel11.add(jPanel12);

        tabela_complementos_para_serem_adicionados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane11.setViewportView(tabela_complementos_para_serem_adicionados);

        jPanel11.add(jScrollPane11);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(caixa_opcoes_genero_item, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(campo_texto_nome_item))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(campo_texto_preco_item, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(botao_salvar_item, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 876, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campo_texto_nome_item, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campo_texto_preco_item, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(caixa_opcoes_genero_item, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botao_salvar_item, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        painel_divisor_secundario.setLeftComponent(jPanel6);

        jPanel5.setMaximumSize(new java.awt.Dimension(100, 150));
        jPanel5.setMinimumSize(new java.awt.Dimension(50, 150));
        jPanel5.setPreferredSize(new java.awt.Dimension(100, 150));
        jPanel5.setLayout(new java.awt.GridLayout(5, 1));

        botao_adicionar_imagem1.setBackground(new java.awt.Color(255, 255, 255));
        botao_adicionar_imagem1.setText("Adicionar imagem");
        botao_adicionar_imagem1.setMaximumSize(new java.awt.Dimension(100, 30));
        botao_adicionar_imagem1.setMinimumSize(new java.awt.Dimension(100, 30));
        botao_adicionar_imagem1.setPreferredSize(new java.awt.Dimension(100, 30));
        botao_adicionar_imagem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_adicionar_imagem1ActionPerformed(evt);
            }
        });
        jPanel5.add(botao_adicionar_imagem1);

        botao_adicionar_imagem2.setBackground(new java.awt.Color(255, 255, 255));
        botao_adicionar_imagem2.setText("Adicionar imagem");
        botao_adicionar_imagem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_adicionar_imagem2ActionPerformed(evt);
            }
        });
        jPanel5.add(botao_adicionar_imagem2);

        botao_adicionar_imagem3.setBackground(new java.awt.Color(255, 255, 255));
        botao_adicionar_imagem3.setText("Adicionar imagem");
        botao_adicionar_imagem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_adicionar_imagem3ActionPerformed(evt);
            }
        });
        jPanel5.add(botao_adicionar_imagem3);

        botao_adicionar_imagem4.setBackground(new java.awt.Color(255, 255, 255));
        botao_adicionar_imagem4.setText("Adicionar imagem");
        botao_adicionar_imagem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_adicionar_imagem4ActionPerformed(evt);
            }
        });
        jPanel5.add(botao_adicionar_imagem4);

        botao_adicionar_imagem5.setBackground(new java.awt.Color(255, 255, 255));
        botao_adicionar_imagem5.setText("Adicionar imagem");
        botao_adicionar_imagem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botao_adicionar_imagem5ActionPerformed(evt);
            }
        });
        jPanel5.add(botao_adicionar_imagem5);

        painel_divisor_secundario.setRightComponent(jPanel5);

        javax.swing.GroupLayout painel_itemLayout = new javax.swing.GroupLayout(painel_item);
        painel_item.setLayout(painel_itemLayout);
        painel_itemLayout.setHorizontalGroup(
            painel_itemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painel_itemLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painel_divisor_secundario)
                .addContainerGap())
        );
        painel_itemLayout.setVerticalGroup(
            painel_itemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painel_itemLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painel_divisor_secundario))
        );

        painel_divisor_principal.setRightComponent(painel_item);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painel_divisor_principal)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painel_divisor_principal)
                .addContainerGap())
        );

        painel_rolante_principal.setViewportView(jPanel1);

        jMenu1.setText("Arquivo");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Editar");

        jMenuItem1.setText("Adicionar complemento");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem2.setText("Adicionar gênero");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuItem3.setText("Adicionar tipo");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painel_rolante_principal)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painel_rolante_principal)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        TelaAdicionarComplemento telaAdicionarComplemento = new TelaAdicionarComplemento(this);
        telaAdicionarComplemento.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        TelaAdicionarGenero telaAdicionarGenero = new TelaAdicionarGenero(this);
        telaAdicionarGenero.setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        TelaAdicionarTipo telaAdicionarTipo = new TelaAdicionarTipo(this);
        telaAdicionarTipo.setVisible(true);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void botao_adicionar_tipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_adicionar_tipoActionPerformed
        while (this.tabela_tipos_existentes.getSelectedRowCount() > 0) {
            int linhaSelecionada = this.tabela_tipos_existentes.getSelectedRow();
            Tipo tipo = (Tipo) this.modeloTabelaTiposExistentes.getValueAt(linhaSelecionada, 0);
            this.modeloTabelaTiposExistentes.removeRow(linhaSelecionada);
            this.modeloTabelaTiposParaAdicionar.addRow(new Tipo[]{tipo});
            this.tiposParaSeremAdicionados.add(tipo);
        }
        System.out.println("Tamanho da lista de tipos: " + tiposParaSeremAdicionados.size());
    }//GEN-LAST:event_botao_adicionar_tipoActionPerformed

    private void botao_adicionar_complementoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_adicionar_complementoActionPerformed
        while (this.tabela_complementos_existentes.getSelectedRowCount() > 0) {
            int linhaSelecionada = this.tabela_complementos_existentes.getSelectedRow();
            Complemento complemento = (Complemento) this.modeloTabelaComplementosExistentes.getValueAt(linhaSelecionada, 0);
            this.modeloTabelaComplementosExistentes.removeRow(linhaSelecionada);
            this.modeloTabelaComplementosParaAdicionar.addRow(new Complemento[]{complemento, complemento});
            this.complementosParaAdicionar.add(complemento);
        }
        System.out.println("Tamanho da lista de complementos: " + complementosParaAdicionar.size());
    }//GEN-LAST:event_botao_adicionar_complementoActionPerformed

    private void botao_adicionar_imagem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_adicionar_imagem1ActionPerformed
        this.imagemTipo1 = this.adicionarImagem(this.botao_adicionar_imagem1);
    }//GEN-LAST:event_botao_adicionar_imagem1ActionPerformed

    private void botao_adicionar_imagem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_adicionar_imagem2ActionPerformed
        this.imagemTipo2 = this.adicionarImagem(this.botao_adicionar_imagem2);
    }//GEN-LAST:event_botao_adicionar_imagem2ActionPerformed

    private void botao_adicionar_imagem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_adicionar_imagem3ActionPerformed
        this.imagemTipo3 = this.adicionarImagem(this.botao_adicionar_imagem3);
    }//GEN-LAST:event_botao_adicionar_imagem3ActionPerformed

    private void botao_adicionar_imagem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_adicionar_imagem4ActionPerformed
        this.imagemTipo4 = this.adicionarImagem(this.botao_adicionar_imagem4);
    }//GEN-LAST:event_botao_adicionar_imagem4ActionPerformed

    private void botao_adicionar_imagem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_adicionar_imagem5ActionPerformed
        this.imagemTipo5 = this.adicionarImagem(this.botao_adicionar_imagem5);
    }//GEN-LAST:event_botao_adicionar_imagem5ActionPerformed

    private void botao_salvar_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_salvar_itemActionPerformed
        this.salvarItens();
    }//GEN-LAST:event_botao_salvar_itemActionPerformed

    private void botao_remover_tipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_remover_tipoActionPerformed
        while (this.tabela_tipos_para_serem_adicionados.getSelectedRowCount() > 0) {
            int linhaSelecionada = this.tabela_tipos_para_serem_adicionados.getSelectedRow();
            Tipo tipo = (Tipo) this.modeloTabelaTiposParaAdicionar.getValueAt(linhaSelecionada, 0);
            this.modeloTabelaTiposParaAdicionar.removeRow(linhaSelecionada);
            this.modeloTabelaTiposExistentes.addRow(new Tipo[]{tipo});
            this.tiposParaSeremAdicionados.remove(tipo);
        }
        System.out.println("Tamanho da lista de tipos: " + tiposParaSeremAdicionados.size());
    }//GEN-LAST:event_botao_remover_tipoActionPerformed

    private void botao_remover_complementoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_remover_complementoActionPerformed
        while (this.tabela_complementos_para_serem_adicionados.getSelectedRowCount() > 0) {
            int linhaSelecionada = this.tabela_complementos_para_serem_adicionados.getSelectedRow();
            Complemento complemento = (Complemento) this.modeloTabelaComplementosParaAdicionar.getValueAt(linhaSelecionada, 0);
            this.modeloTabelaComplementosParaAdicionar.removeRow(linhaSelecionada);
            this.modeloTabelaComplementosExistentes.addRow(new Complemento[]{complemento, complemento});
            this.complementosParaAdicionar.remove(complemento);
        }
        System.out.println("Tamanho da lista de complementos: " + complementosParaAdicionar.size());
    }//GEN-LAST:event_botao_remover_complementoActionPerformed

    private void tabela_itemMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabela_itemMouseClicked
        if (this.tabela_item.getSelectedRowCount() > 0) {
            this.botao_remover_item.setEnabled(true);
            this.botao_atualizar_item.setEnabled(true);
        }
    }//GEN-LAST:event_tabela_itemMouseClicked

    private void botao_cadastrar_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_cadastrar_itemActionPerformed
        ((TitledBorder) this.painel_item.getBorder()).setTitle("Cadastrar item");
        super.repaint();
        this.tabela_item.clearSelection();
        this.botao_atualizar_item.setEnabled(false);
        this.botao_remover_item.setEnabled(false);
        this.setEdicaoItem(true);
    }//GEN-LAST:event_botao_cadastrar_itemActionPerformed

    private void botao_atualizar_itemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botao_atualizar_itemActionPerformed
        if (this.tabela_item.getSelectedRowCount() > 0) {
            if (this.tabela_item.getSelectedRowCount() < 2) {
                ((TitledBorder) this.painel_item.getBorder()).setTitle("Atualizar item");
                this.setEdicaoItem(true);
                super.repaint();
                Item item = (Item) this.modeloTabelaItemExistentes.getValueAt(this.tabela_item.getSelectedRow(), 0);
                this.campo_texto_nome_item.setText(item.getNome());
                this.campo_texto_preco_item.setText(String.valueOf(item.getPreco()));
                this.area_texto_descricao_item.setText(item.getDescricao());
                this.caixa_opcoes_genero_item.setSelectedItem(item.getGenero());
                for (Tipo tipo : item.getTipos()) {
                    
                }
            }
        }
    }//GEN-LAST:event_botao_atualizar_itemActionPerformed

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
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarItem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarItem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarItem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarItem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaAdicionarItem().setVisible(true);
            }
        });
    }

    private class TableModelComplemento extends DefaultTableModel {

        public TableModelComplemento() {
            super(new String[]{"Nome", "Preço"}, 0);
        }

        @Override
        public Object getValueAt(int i, int i1) {
            if (i1 == 0) {
                return ((Complemento) super.getValueAt(i, i1));
            } else {
                return ((Complemento) super.getValueAt(i, i1)).getPreco();
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

    }

    private class TableModelTipo extends DefaultTableModel {

        public TableModelTipo() {
            super(new String[]{"Nome"}, 0);
        }

        @Override
        public Object getValueAt(int i, int i1) {
            return super.getValueAt(i, i1);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Class getColumnClass(int column) {
            return super.getColumnClass(column);
        }
    }

    private class TableModelItem extends DefaultTableModel {

        public TableModelItem() {
            super(new String[]{"Nome", "Preco"}, 0);
        }

        @Override
        public Object getValueAt(int i, int i1) {
            if (i1 == 0) {
                return ((Item) super.getValueAt(i, i1));
            } else {
                return ((Item) super.getValueAt(i, i1)).getPreco();
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Class getColumnClass(int column) {
            return super.getColumnClass(column);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea area_texto_descricao_item;
    private javax.swing.JButton botao_adicionar_complemento;
    private javax.swing.JButton botao_adicionar_imagem1;
    private javax.swing.JButton botao_adicionar_imagem2;
    private javax.swing.JButton botao_adicionar_imagem3;
    private javax.swing.JButton botao_adicionar_imagem4;
    private javax.swing.JButton botao_adicionar_imagem5;
    private javax.swing.JButton botao_adicionar_tipo;
    private javax.swing.JButton botao_atualizar_item;
    private javax.swing.JButton botao_cadastrar_item;
    private javax.swing.JButton botao_remover_complemento;
    private javax.swing.JButton botao_remover_item;
    private javax.swing.JButton botao_remover_tipo;
    private javax.swing.JButton botao_salvar_item;
    private javax.swing.JComboBox<Genero> caixa_opcoes_genero_item;
    private javax.swing.JTextField campo_texto_nome_item;
    private javax.swing.JTextField campo_texto_preco_item;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSplitPane painel_divisor_principal;
    private javax.swing.JSplitPane painel_divisor_secundario;
    private javax.swing.JPanel painel_item;
    private javax.swing.JScrollPane painel_rolante_principal;
    private javax.swing.JTable tabela_complementos_existentes;
    private javax.swing.JTable tabela_complementos_para_serem_adicionados;
    private javax.swing.JTable tabela_item;
    private javax.swing.JTable tabela_tipos_existentes;
    private javax.swing.JTable tabela_tipos_para_serem_adicionados;
    // End of variables declaration//GEN-END:variables
}
