/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.br.phdev.srt.dao;

import com.br.phdev.srt.exception.DAOException;
import com.br.phdev.srt.utils.UrlAttribute;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.List;

/**
 *
 * @author Paulo Henrique Gonçalves Bacelar <henrique.phgb@gmail.com>
 */
public class DataDAO {
    
    private final HttpURLConnection connection;

    public DataDAO(HttpURLConnection connection) {
        this.connection = connection;
        try {
            this.connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
        }
    }

    public DataDAO(HttpURLConnection connection, String requestMethod) {
        this.connection = connection;
        try {
            this.connection.setRequestMethod(requestMethod);
        } catch (ProtocolException e) {
        }
    }

    private byte[] getBytesFromFile(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return bytes;
    }
    
    public void sendMultiPartFile(File arquivo, List<UrlAttribute> urlAttributeList) throws DAOException {
        String boundary = "SwA " + Long.toString(System.currentTimeMillis()) + " SwA";
        String delimiter = "--";
        OutputStream os = null;        
        String paramName = "param1";
        String value = "value1";
        try {
            byte[] bytes = getBytesFromFile(arquivo);

            this.connection.setDoOutput(true);
            this.connection.setDoInput(true);           
            this.connection.setRequestProperty("Connection", "Keep-Alive");
            this.connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            this.connection.connect();
            os = this.connection.getOutputStream();

            os.write((delimiter + boundary + "\r\n").getBytes());
            os.write("Content-Type: text/plain\r\n".getBytes());
            os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());
            os.write(("\r\n" + value + "\r\n").getBytes());

            os.write((delimiter + boundary + "\r\n").getBytes());
            //os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
            os.write(("Content-Disposition: form-data; name=\"arquivo\"; filename=\"" + arquivo.getName() + "\"\r\n").getBytes());
            os.write(("Content-Type: application/octet-stream\r\n").getBytes());
            os.write(("Content-Transfer-Encoding: binary\r\n").getBytes());

            os.write("\r\n".getBytes());
            os.write(bytes);
            os.write("\r\n".getBytes());
            
            for (UrlAttribute urlAttribute : urlAttributeList) {
                os.write((delimiter + boundary + "\r\n").getBytes());
                os.write("Content-Type: text/plain\r\n".getBytes());
                os.write(("Content-Disposition: form-data; name=\"" + urlAttribute.getName() + "\"\r\n").getBytes());
                os.write(("\r\n" + urlAttribute.getValue() + "\r\n").getBytes());
            }

            os.write((delimiter + boundary + delimiter + "\r\n").getBytes());
            os.close();            
        } catch (IOException e) {
            throw new DAOException("Falha ao enviar o arquivo", e);
        }        
    }

    public void sendAttributes(List<UrlAttribute> urlAttributeList) throws DAOException {        
        if (!this.connection.getDoOutput()) {
            this.connection.setDoOutput(true);
        }
        if (!urlAttributeList.isEmpty()) {
            OutputStream os = null;
            BufferedWriter bfw = null;
            try {
                os = connection.getOutputStream();
                bfw = new BufferedWriter(new OutputStreamWriter(os, "ISO-8859-1"));
                StringBuilder parameters = new StringBuilder();
                for (UrlAttribute attr : urlAttributeList) {
                    parameters.append(attr.getName());
                    parameters.append("=");
                    parameters.append(attr.getValue());
                    parameters.append("&");
                }
                parameters.deleteCharAt(parameters.length() - 1);
                bfw.write(parameters.toString());
                bfw.flush();
                bfw.close();
                os.close();                
            } catch (IOException e) {
                throw new DAOException("Falha ao enviar os dados", e);
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                    if (bfw != null) {
                        bfw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }        
    }
    
    public void sendJSON(String json) throws DAOException {
        if (!this.connection.getDoOutput()) {
            this.connection.setDoOutput(true);
        }
        OutputStream os = null;
            BufferedWriter bfw = null;
            try {
                this.connection.setRequestProperty("Content-type", "application/json");
                os = this.connection.getOutputStream();
                bfw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));                                                
                bfw.write(json);
                bfw.flush();
                bfw.close();
                os.close();                
            } catch (IOException e) {
                throw new DAOException("Falha ao enviar os dados", e);
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                    if (bfw != null) {
                        bfw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    public String retrieveString() throws DAOException {
        StringBuilder content = null;
        BufferedReader bfr = null;
        try {
            bfr = new BufferedReader(new InputStreamReader(this.connection.getInputStream(), "UTF-8"));
            String inputLine;
            content = new StringBuilder();
            while ((inputLine = bfr.readLine()) != null) {
                content.append(inputLine);
            }
        } catch (IOException e) {
            throw new DAOException("Falha ao recuperar os dados", e);
        } finally {
            if (bfr != null) {
                try {
                    bfr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content != null ? content.toString() : null;
    }

    public byte[] retrieveByteArray(int lenght) throws DAOException {
        InputStream is = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            is = this.connection.getInputStream();
            byte[] bytes = new byte[lenght];
            while (is.read(bytes) != -1) {
                baos.write(bytes);
            }
            baos.flush();
        } catch (IOException e) {
            throw new DAOException("Falha ao recuperar os dados", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return baos.toByteArray();
    }

    public static int getResponseCode(HttpURLConnection con) {
        if (con == null) {
            return -1;
        }
        try {
            return con.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
}
