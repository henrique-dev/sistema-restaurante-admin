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
import com.br.phdev.srt.models.Tipo;
import com.br.phdev.srt.utils.ModelToComboBox;
import com.br.phdev.srt.utils.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar
 */
public class TelaAdicionarItem extends javax.swing.JFrame {

    public enum Mode {
        ADICIONAR_ITEM, ATUALIZAR_ITEM
    }

    private final TelaPrincipal.Telas comingFrom;
    private static TelaAdicionarItem instance;

    public static TelaAdicionarItem getInstance() {
        return instance;
    }

    public static TelaAdicionarItem inflate(TelaPrincipal.Telas comingFrom, Mode currentMode) {
        if (instance == null) {
            instance = new TelaAdicionarItem(comingFrom, currentMode);
        }
        return instance;
    }

    private final Mode currentMode;
    private boolean medicFinded = false;

    private List<Tipo> tipos;

    /**
     * Creates new form JFrameAddMedic
     */
    private TelaAdicionarItem(TelaPrincipal.Telas comingFrom, Mode currentMode) {
        this.currentMode = currentMode;
        this.comingFrom = comingFrom;
        this.initComponents();
        this.tipos = new ArrayList<>();
        if (currentMode == Mode.ADICIONAR_ITEM) {
            retrieveData();
            super.setTitle("Adicionar item");
        } else {
            super.setTitle("Atualizar item");
        }
        this.setAllComponentsEnable(false);
    }

    public void retrieveData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (currentMode == Mode.ATUALIZAR_ITEM) {
                    retrieveDateMedic();
                }
                retrieveDataList();
            }
        }).start();
    }

    private void retrieveDataList() {
        HttpURLConnection con = null;
        try {
            DefaultComboBoxModel<ModelToComboBox> comboBoxModelGenero = new DefaultComboBoxModel<>();
            con = new HttpConnection().getConnection("Gerenciador/ListarGeneros");
            Session.get(con);
            String resposta = new DataDAO(con).retrieveString();
            ObjectMapper mapeador = new ObjectMapper();
            List<Genero> generos = mapeador.readValue(resposta, new TypeReference<List<Genero>>(){});            
            con.disconnect();
            for (Genero genero : generos) {
                comboBoxModelGenero.addElement(new ModelToComboBox(genero.getId(), genero.getNome()));
            }            
            comboBox_speciality.setModel(comboBoxModelGenero);
            DefaultComboBoxModel<ModelToComboBox> comboBoxModelTipo = new DefaultComboBoxModel<>();
            con = new HttpConnection().getConnection("Gerenciador/ListarTipos");
            Session.get(con);
            resposta = new DataDAO(con).retrieveString();
            mapeador = new ObjectMapper();
            List<Tipo> tipos = mapeador.readValue(resposta, new TypeReference<List<Tipo>>(){});
            con.disconnect();
            for (Tipo tipo : tipos) {
                comboBoxModelTipo.addElement(new ModelToComboBox(tipo.getId(), tipo.getNome()));
            }
            comboBox_clinic.setModel(comboBoxModelTipo);
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(null, "Falha ao obter os dados, certifique-se de que você tem conexão com o sistema");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Falha ao converter objetos");
        } finally {
            con.disconnect();
        }
        setAllComponentsEnable(true);
    }

    private void retrieveDateMedic() {

    }

    private void setMode() {
        switch (currentMode) {
            case ADICIONAR_ITEM:
                textFiled_medicName.setEnabled(false);                
                break;
            case ATUALIZAR_ITEM:
                break;
        }
    }

    public void setAllComponentsEnable(boolean enable) {        
        textFiled_medicCrm.setEnabled(currentMode == Mode.ATUALIZAR_ITEM ? !medicFinded : enable);
        textFiled_medicName.setEnabled(currentMode == Mode.ATUALIZAR_ITEM ? false : enable);
        comboBox_clinic.setEnabled(enable);
        comboBox_speciality.setEnabled(enable);
        button_removeMedicWorkProfile.setEnabled(enable);
        button_insertMediWorkProfile.setEnabled(enable);
        button_newClinic.setEnabled(enable);
        button_newSpeciality.setEnabled(enable);
        button_saveNcontinue.setEnabled(enable);
        button_saveNexit.setEnabled(enable);                
        table_medicWorkProfile.setEnabled(enable);
    }
/*
    private void clearFields() {
        DefaultTableModel defaultTableModel = TableModelMedicWorkProfile.getInstance();
        while (defaultTableModel.getRowCount() > 0) {
            defaultTableModel.removeRow(0);
        }
        medicWorkProfileList.clear();

        textField_consuntPerDay.setText("0");
        textField_desc.setText("");
        textField_medicUserName.setText("");
        textField_medicUserPassword.setText("");
        textFiled_info.setText("");
        textFiled_medicCrm.setText("");
        textFiled_medicName.setText("");
        radioButton_1.setSelected(false);
        radioButton_2.setSelected(false);
        radioButton_3.setSelected(false);
        radioButton_4.setSelected(false);
        radioButton_5.setSelected(false);
        radioButton_6.setSelected(false);
        radioButton_7.setSelected(false);
    }

    private void insertMedicSpeciality() {
        int clinicId = ((ModelToComboBox) comboBox_clinic.getSelectedItem()).getId();
        String clinicName = ((ModelToComboBox) comboBox_clinic.getSelectedItem()).getName();
        int specialityId = ((ModelToComboBox) comboBox_speciality.getSelectedItem()).getId();
        String specialityName = ((ModelToComboBox) comboBox_speciality.getSelectedItem()).getName();

        for (MedicWorkProfile mwp : medicWorkProfileList) {
            if (mwp.getMedicSpeciality().getSpeciality().getSpecialityId() == specialityId
                    && mwp.getMedicWorkAddress().getClinicProfile().getId() == clinicId) {
                throw new Error("A especialidade já se encontra cadastrada para este médico nesta unidade de saúde!");
            }
        }

        int consultPerDay = Integer.parseInt(textField_consuntPerDay.getText());
        String desc = textField_desc.getText();
        String info = textFiled_info.getText();

        StringBuilder daysOfWeek = new StringBuilder("");
        daysOfWeek.replace(0, 0, "" + (radioButton_1.isSelected() ? 1 : 0));
        daysOfWeek.replace(1, 1, "" + (radioButton_2.isSelected() ? 1 : 0));
        daysOfWeek.replace(2, 2, "" + (radioButton_3.isSelected() ? 1 : 0));
        daysOfWeek.replace(3, 3, "" + (radioButton_4.isSelected() ? 1 : 0));
        daysOfWeek.replace(4, 4, "" + (radioButton_5.isSelected() ? 1 : 0));
        daysOfWeek.replace(5, 5, "" + (radioButton_6.isSelected() ? 1 : 0));
        daysOfWeek.replace(6, 6, "" + (radioButton_7.isSelected() ? 1 : 0));

        MedicWorkProfile medicWorkProfile = new MedicWorkProfile();

        MedicSpeciality medicSpeciality = new MedicSpeciality();
        medicSpeciality.setSpeciality(new Speciality(specialityId, specialityName, false));

        MedicWorkAddress medicWorkAddress = new MedicWorkAddress();
        medicWorkAddress.setClinicProfile(new ClinicProfile(clinicId, clinicName, null));
        medicWorkAddress.setMedicWorkAddressComplement(info);

        MedicWorkScheduling medicWorkScheduling = new MedicWorkScheduling();
        medicWorkScheduling.setMedicWorkSchedulingInfo(desc);
        medicWorkScheduling.setMedicWorkSchedulingPerDay(consultPerDay);
        medicWorkScheduling.setMedicWorkSchedulingDaysOfWeek(daysOfWeek.toString());

        medicWorkProfile.setMedicSpeciality(medicSpeciality);
        medicWorkProfile.setMedicWorkAddress(medicWorkAddress);
        medicWorkProfile.setMedicWorkScheduling(medicWorkScheduling);

        medicWorkProfileList.add(medicWorkProfile);

        DefaultTableModel defaultTableModel = TableModelMedicWorkProfile.getInstance();
        while (defaultTableModel.getRowCount() > 0) {
            defaultTableModel.removeRow(0);
        }
        for (MedicWorkProfile m : medicWorkProfileList) {
            defaultTableModel.addRow(new Object[]{m.getMedicWorkAddress().getClinicProfile().getClinicName(),
                m.getMedicSpeciality().getSpeciality().getSpecialityName(),
                m.getMedicWorkScheduling().getMedicWorkSchedulingPerDay() + "",
                m.getMedicWorkScheduling().getMedicWorkSchedulingInfo(),
                m.getMedicWorkScheduling().getMedicWorkSchedulingDaysOfWeek()});
        }
        table_medicWorkProfile.setModel(defaultTableModel);
    }

    private void saveMedic() {
        String medicName = textFiled_medicName.getText();
        if (medicName.trim().length() == 0) {
            throw new Error("O nome do médico não pode estar vazio!");
        }
        String medicCrm = textFiled_medicCrm.getText();
        if (medicCrm.trim().length() == 0) {
            throw new Error("O número da CRM do médico não pode estar vazio!");
        }
        String medicUserName = textField_medicUserName.getText();
        if (medicUserName.trim().length() == 0) {
            throw new Error("O nome de usuário do médico não pode estar vazio!");
        }
        String medicUserPassword = textField_medicUserPassword.getText();
        if (medicUserPassword.trim().length() == 0) {
            throw new Error("A senha de usuário do medico não pode estar vazia!");
        }

        MedicProfile medicProfile = new MedicProfile();
        medicProfile.setMedicName(medicName);
        medicProfile.setMedicCrm(medicCrm);
        medicProfile.setUserName(medicUserName);
        medicProfile.setUserPassword(medicUserPassword);

        Result finalResult = new Result();
        finalResult.setAttrName("medicInfoList");
        finalResult.setResultType(Result.ResultType.LIST);
        List<Result> objList = new ArrayList<>();

        for (MedicWorkProfile m : medicWorkProfileList) {
            List<Result> attrList = new ArrayList<>();
            attrList.add(new Result(Result.ResultType.ATTRIBUTE, "specialityId", m.getMedicSpeciality().getSpeciality().getSpecialityId()));
            attrList.add(new Result(Result.ResultType.ATTRIBUTE, "clinicId", m.getMedicWorkAddress().getClinicProfile().getId()));
            attrList.add(new Result(Result.ResultType.ATTRIBUTE, "medicWorkInfo", m.getMedicWorkAddress().getMedicWorkAddressComplement()));
            attrList.add(new Result(Result.ResultType.ATTRIBUTE, "medicSchedPerDay", m.getMedicWorkScheduling().getMedicWorkSchedulingPerDay()));
            attrList.add(new Result(Result.ResultType.ATTRIBUTE, "medicSchedInfo", m.getMedicWorkScheduling().getMedicWorkSchedulingInfo()));
            attrList.add(new Result(Result.ResultType.ATTRIBUTE, "medicSchedDayOfWeek", m.getMedicWorkScheduling().getMedicWorkSchedulingDaysOfWeek()));
            Result objResult = new Result();
            objResult.setResultType(Result.ResultType.OBJECT);
            objResult.setAttrName("medicInfo");
            objResult.setAttrValue(attrList);
            objList.add(objResult);
        }
        finalResult.setAttrValue(objList);
        String medicWorkInfo = new ResultSet().insertResult(finalResult);
        
        System.out.println(medicWorkInfo);

        HttpURLConnection con = new HttpConnection().getConnection("Manager/CadastrarMedico");        
        Session.get(con);
        List<UrlAttribute> urlAttributeList = new ArrayList<>();
        urlAttributeList.add(new UrlAttribute("userName", medicUserName));
        urlAttributeList.add(new UrlAttribute("userPassword", medicUserPassword));
        urlAttributeList.add(new UrlAttribute("medicName", medicName));
        urlAttributeList.add(new UrlAttribute("medicCrm", medicCrm));
        urlAttributeList.add(new UrlAttribute("medicWorkInfo", medicWorkInfo));

        String msg = "";
        try {
            DataDAO dataDAO = new DataDAO(con);
            dataDAO.sendAttributes(urlAttributeList);
            msg = dataDAO.retrieveString();
            JOptionPane.showMessageDialog(null, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (DAOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, msg, "Falha", JOptionPane.ERROR_MESSAGE);
        } finally {
            con.disconnect();
        }
    }

    @Override
    protected void processWindowEvent(final WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            FrameAddUpdateClinic fc = FrameAddUpdateClinic.getInstance();
            if (fc != null) {
                fc.dispose();
            }
            FrameAddSpeciality fs = FrameAddSpeciality.getInstance();
            if (fs != null) {
                fs.dispose();
            }
            instance = null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        FrameAddUpdateClinic fc = FrameAddUpdateClinic.getInstance();
        if (fc != null) {
            fc.dispose();
        }
        FrameAddSpeciality fs = FrameAddSpeciality.getInstance();
        if (fs != null) {
            fs.dispose();
        }
        instance = null;
    }*/

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
        jLabel2 = new javax.swing.JLabel();
        textFiled_medicCrm = new javax.swing.JTextField();
        textFiled_medicName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        comboBox_clinic = new javax.swing.JComboBox<>();
        button_newClinic = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        comboBox_speciality = new javax.swing.JComboBox<>();
        button_newSpeciality = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table_medicWorkProfile = new javax.swing.JTable();
        button_insertMediWorkProfile = new javax.swing.JButton();
        button_removeMedicWorkProfile = new javax.swing.JButton();
        button_clearMedicWorkProfile = new javax.swing.JButton();
        button_saveNcontinue = new javax.swing.JButton();
        button_saveNexit = new javax.swing.JButton();
        button_saveNexit1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Adicionar item"));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Geral"));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Nome do médico:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Crm do médico:");

        textFiled_medicCrm.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        textFiled_medicCrm.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFiled_medicCrmKeyReleased(evt);
            }
        });

        textFiled_medicName.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        textFiled_medicName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFiled_medicNameActionPerformed(evt);
            }
        });
        textFiled_medicName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFiled_medicNameKeyReleased(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel5.setText("Gênero:");

        comboBox_clinic.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        button_newClinic.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        button_newClinic.setText("Cadastrar nova");
        button_newClinic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_newClinicActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textFiled_medicName))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(comboBox_clinic, javax.swing.GroupLayout.PREFERRED_SIZE, 562, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(button_newClinic, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                            .addComponent(textFiled_medicCrm, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textFiled_medicName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(textFiled_medicCrm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(comboBox_clinic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_newClinic))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Específico"));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel6.setText("Tipo:");

        comboBox_speciality.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        button_newSpeciality.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        button_newSpeciality.setText("Cadastrar nova");
        button_newSpeciality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_newSpecialityActionPerformed(evt);
            }
        });

        jScrollPane1.setEnabled(false);

        table_medicWorkProfile.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        table_medicWorkProfile.setModel(TableModelMedicWorkProfile.getInstance());
        table_medicWorkProfile.setEnabled(false);
        table_medicWorkProfile.setRowHeight(30);
        table_medicWorkProfile.setRowMargin(2);
        jScrollPane1.setViewportView(table_medicWorkProfile);

        button_insertMediWorkProfile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        button_insertMediWorkProfile.setText("Inserir");
        button_insertMediWorkProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_insertMediWorkProfileActionPerformed(evt);
            }
        });

        button_removeMedicWorkProfile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        button_removeMedicWorkProfile.setText("Remover selecionada");
        button_removeMedicWorkProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_removeMedicWorkProfileActionPerformed(evt);
            }
        });

        button_clearMedicWorkProfile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        button_clearMedicWorkProfile.setText("Limpar");
        button_clearMedicWorkProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_clearMedicWorkProfileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBox_speciality, javax.swing.GroupLayout.PREFERRED_SIZE, 561, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button_newSpeciality))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addComponent(button_insertMediWorkProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(button_removeMedicWorkProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(button_clearMedicWorkProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 214, Short.MAX_VALUE)))
                .addGap(14, 14, 14))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(comboBox_speciality, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_newSpeciality))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(button_insertMediWorkProfile)
                    .addComponent(button_removeMedicWorkProfile)
                    .addComponent(button_clearMedicWorkProfile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        button_saveNcontinue.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        button_saveNcontinue.setText("Salvar e continuar inserindo");
        button_saveNcontinue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_saveNcontinueActionPerformed(evt);
            }
        });

        button_saveNexit.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        button_saveNexit.setText("Salvar e sair");
        button_saveNexit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_saveNexitActionPerformed(evt);
            }
        });

        button_saveNexit1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        button_saveNexit1.setText("Voltar");
        button_saveNexit1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_saveNexit1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(button_saveNcontinue, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(button_saveNexit, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(button_saveNexit1, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(265, 265, 265)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(button_saveNcontinue)
                    .addComponent(button_saveNexit)
                    .addComponent(button_saveNexit1))
                .addGap(53, 53, 53))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(48, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 769, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void textFiled_medicNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFiled_medicNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textFiled_medicNameActionPerformed

    private void button_saveNcontinueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_saveNcontinueActionPerformed
        try {
            //saveMedic();
            //clearFields();
        } catch (Error e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_button_saveNcontinueActionPerformed

    private boolean firstNameOk = false;
    private String userName = "";

    private int module(int value) {
        if (value < 0) {
            value *= -1;
        }
        return value;
    }

    private void textFiled_medicCrmKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFiled_medicCrmKeyReleased
        
    }//GEN-LAST:event_textFiled_medicCrmKeyReleased

    private void button_saveNexitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_saveNexitActionPerformed
        try {
            //saveMedic();
            dispose();
        } catch (Error e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_button_saveNexitActionPerformed

    private void textFiled_medicNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFiled_medicNameKeyReleased
        
    }//GEN-LAST:event_textFiled_medicNameKeyReleased

    private void button_newSpecialityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_newSpecialityActionPerformed
        //FrameAddSpeciality frameAddSpeciality = FrameAddSpeciality.inflate(FrameMain.Telas.ADD_MEDIC);
        //frameAddSpeciality.setVisible(true);
    }//GEN-LAST:event_button_newSpecialityActionPerformed

    private void button_insertMediWorkProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_insertMediWorkProfileActionPerformed
        try {
            //insertMedicSpeciality();
        } catch (Error e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_button_insertMediWorkProfileActionPerformed

    private void button_saveNexit1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_saveNexit1ActionPerformed
        dispose();
    }//GEN-LAST:event_button_saveNexit1ActionPerformed

    private void button_newClinicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_newClinicActionPerformed
        //FrameAddUpdateClinic frameAddUpdateClinic = FrameAddUpdateClinic.inflate(FrameMain.Telas.ADD_MEDIC);
        //frameAddUpdateClinic.setVisible(true);
    }//GEN-LAST:event_button_newClinicActionPerformed

    private void button_clearMedicWorkProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_clearMedicWorkProfileActionPerformed
        /*DefaultTableModel defaultTableModel = TableModelMedicWorkProfile.getInstance();
        while (defaultTableModel.getRowCount() > 0) {
            defaultTableModel.removeRow(0);
        }
        medicWorkProfileList.clear();*/
    }//GEN-LAST:event_button_clearMedicWorkProfileActionPerformed

    private void button_removeMedicWorkProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_removeMedicWorkProfileActionPerformed
        /*int indexToRemove = table_medicWorkProfile.getSelectedRow();
        if (indexToRemove >= 0) {
            medicWorkProfileList.remove(indexToRemove);
            DefaultTableModel defaultTableModel = TableModelMedicWorkProfile.getInstance();
            defaultTableModel.removeRow(indexToRemove);
        }*/
    }//GEN-LAST:event_button_removeMedicWorkProfileActionPerformed

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
            java.util.logging.Logger.getLogger(TelaAdicionarItem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarItem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarItem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaAdicionarItem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaAdicionarItem(null, Mode.ADICIONAR_ITEM).setVisible(true);
            }
        });
    }

    private static class TableModelMedicWorkProfile extends DefaultTableModel {

        private static TableModelMedicWorkProfile instance;

        public static TableModelMedicWorkProfile getInstance() {
            if (instance == null) {
                instance = new TableModelMedicWorkProfile();
            }
            return instance;
        }

        private TableModelMedicWorkProfile() {
            super(new String[]{"Clinica", "Especialidade", "Consultas por dia", "Descrição", "Dias disponiveis"}, 0);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_clearMedicWorkProfile;
    private javax.swing.JButton button_insertMediWorkProfile;
    private javax.swing.JButton button_newClinic;
    private javax.swing.JButton button_newSpeciality;
    private javax.swing.JButton button_removeMedicWorkProfile;
    private javax.swing.JButton button_saveNcontinue;
    private javax.swing.JButton button_saveNexit;
    private javax.swing.JButton button_saveNexit1;
    private javax.swing.JComboBox<ModelToComboBox> comboBox_clinic;
    private javax.swing.JComboBox<ModelToComboBox> comboBox_speciality;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table_medicWorkProfile;
    private javax.swing.JTextField textFiled_medicCrm;
    private javax.swing.JTextField textFiled_medicName;
    // End of variables declaration//GEN-END:variables
}
