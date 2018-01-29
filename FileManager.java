/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.jcnsistemas.api.inregdb.controller;

import br.com.jcnsistemas.api.inregdb.model.FileBean;
import br.com.jcnsistemas.api.inregdb.model.dao.ConnectionDAO;
import br.com.jcnsistemas.api.inregdb.model.dao.QueryManagerDAO;
import br.com.jcnsistemas.api.inregdb.model.RegBean;
import br.com.jcnsistemas.api.inregdb.model.UserObj;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author rafael.inocenti
 */
public class FileManager {

    /*public void readFileTxt(String source){
        String[] info;
         ArrayList<String> phoneList = new ArrayList<String>();
         ArrayList<String> phoneListProcess = new ArrayList<String>();
        try{
        BufferedReader br = new BufferedReader(new FileReader(source));
			while(br.ready()){
				String linha = br.readLine();
                                
                                info = linha.split(";");
                                
                                phoneListProcess.add(info[0]);
                              
			}
			br.close();
        }catch(IOException ioe){
			ioe.printStackTrace();
		} 
        System.out.println(phoneListProcess.size());
    
        int totLines = (phoneListProcess.size());         
        int x = 0;
        

        for (int c = 0; c < phoneListProcess.size(); c++) {

            int calc = (phoneListProcess.size() - c);
            x++;
            
            //if(x <= 100){
                String phoneProcessed = phoneListProcess.get(c);
           
                phoneList.add(phoneProcessed);
                
                if(phoneListProcess.size() >= 100){
                    if(x == 100){
                        
                        //System.out.println(phoneList.toString());


                        String phoneStr = phoneList.toString();

                        //System.out.println(phoneStr);

                        String aux = phoneStr.replace(",", ",\n");
                        aux = aux.replace("[","");
                        aux = aux.replace("]","");

                        FileBean fbean = new FileBean();

                        fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                        //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX

                        QueryManagerDAO result = new QueryManagerDAO();

                        //System.out.println(result.retrieveAccession());

                        result.doAccession(result.retrieveAccession()); //CORRETO
                          //result.doAccession(); //TESTE

                        //System.out.println("\n----------\n");

                        phoneList.clear();
                        x=0;
                    }
                    else if(calc < 100){
                        String phoneStr = phoneList.toString();

                        //System.out.println(phoneStr);

                        String aux = phoneStr.replace(",", ",\n");
                        aux = aux.replace("[","");
                        aux = aux.replace("]","");

                        //System.out.println(aux);

                        FileBean fbean = new FileBean();

                        fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                        //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX

                        QueryManagerDAO result = new QueryManagerDAO();

                        //System.out.println(result.retrieveAccession());

                        result.doAccession(result.retrieveAccession()); //CORRETO
                          //result.doAccession(); //TESTE

                        phoneList.clear();
                    }
                }
                
               

    }
    }*/
    public void readFile(String fileName) {
        
        int contador = 0;

        InputStream xlsx = null;

        Boolean b = false;

        ArrayList<String> phoneList = new ArrayList<>();
        ArrayList<String> phoneListProcess = new ArrayList<>();
        ArrayList<RegBean> regList = new ArrayList<>();// GRAVA REGISTROS NUMERO,ID,VALOR
        ArrayList<String> difIdList = new ArrayList<>();// QUANTOS IDS DIFERENTES O ARQUIVO POSSUI
        ArrayList<String> list = new ArrayList<>();
        String currId = "";
        
        ArrayList<String> difValList = new ArrayList<>();
        String currVal = "";
        
        FileBean fb = new FileBean();
        ConnectionDAO conn = new ConnectionDAO();

        File dir = new File("TEMP");
        if (!dir.exists()) {
            b = dir.mkdir();
        }

        dir.setWritable(true);
        String path = dir.getPath();

        //csvformated.csv
        //String source = path + "/jacksonCSVDIA04-2017CaminhaoA.csv";//MUDAR O ARQUIVO AQUI
        String source = path + "/"+fileName;//MUDAR O ARQUIVO AQUI

        System.out.println(">>>> Have to create a directory: " + b);

        System.out.println("file: " + source);

        //INICIO DE LEITURA DE CSV
        try {
            Scanner scanner = new Scanner(new File(source));

            //scanner.useDelimiter(",");
            while (scanner.hasNext()) {

                //String phone = scanner.next();
                String splitPhone[] = scanner.nextLine().split(";");

                String splitLine[] = splitPhone[0].split(",");

                String phone = splitPhone[0];//AQUI

                if (phone.startsWith("ï»¿")) {
                    phone = phone.substring(3);
                }

                //List all regs
                regList.add(new RegBean(phone, splitPhone[1], splitPhone[2]));

                //IDENTIFICANDO IDS DIFERENTES
                if (currId.equals("")) {
                    currId = splitPhone[1];
                    difIdList.add(splitPhone[1]);
                }

                if (!difIdList.contains(splitPhone[1])) {
                    difIdList.add(splitPhone[1]);
                    currId = splitPhone[1];
                }
                
                //IDENTIFICANDO VALORES DIFERENTES
                if (currVal.equals("")) {
                    currVal = splitPhone[2];
                    difValList.add(splitPhone[2]);
                }

                if (!difValList.contains(splitPhone[2])) {
                    difValList.add(splitPhone[2]);
                    currVal = splitPhone[2];
                }
                
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        //System.out.println(phoneListProcess);
        System.out.println("fb - " + fb.getRegId());
        System.out.println("DF ID LIST -> " + difIdList);
        System.out.println("DF VAL LIST -> "+ difValList);

        /*for (int v = 0; v < difValList.size(); v++) {
            //List all regs 
            for (RegBean auxV : regList) {
               // System.out.println("-> " + aux.toString());
                String[] regPosition = auxV.toString().split(";");
                //System.out.println("pos-> "+regPosition[1]);
                //System.out.println("pos dif-> "+difIdList.get(i).toString());
                if (difValList.get(v).equals(regPosition[2]) && ) {
                    phoneListProcess.add(regPosition[0]);
                    fb.setRegVal(regPosition[2]);
                }
                
            }
        */
            
        for (int v = 0; v < difValList.size(); v++) {   
            
             for (int i = 0; i < difIdList.size(); i++) {
            
                for (RegBean aux : regList) {
                   // System.out.println("-> " + aux.toString());
                    String[] regPosition = aux.toString().split(";");
                    //System.out.println("pos-> "+regPosition[1]);
                    //System.out.println("pos dif-> "+difIdList.get(i).toString());

                    if (difValList.get(v).equals(regPosition[2]) && difIdList.get(i).equals(regPosition[1])) {
                        phoneListProcess.add(regPosition[0]);
                        fb.setRegVal(regPosition[2]);
                        fb.setRegId(regPosition[1]);
                    }

                }
        
           // for (int i = 0; i < difIdList.size(); i++) {
            //List all regs 
            /*for (RegBean aux : regList) {
               // System.out.println("-> " + aux.toString());
                String[] regPosition = aux.toString().split(";");
                //System.out.println("pos-> "+regPosition[1]);
                //System.out.println("pos dif-> "+difIdList.get(i).toString());
                if (difIdList.get(i).equals(regPosition[1])) {
                    phoneListProcess.add(regPosition[0]);
                    fb.setRegId(regPosition[1]);
                }
                
            }*/
            int totLines = (regList.size());
            int x = 0;

//Aqui tem que ser o numero de registros já filtrado
            for (int c = 0; c < phoneListProcess.size(); c++) {

                int calc = (phoneListProcess.size() - c);
                x++;

                //if(x <= 100){
                String phoneProcessed = phoneListProcess.get(c);

                phoneList.add(phoneProcessed);

                if (phoneListProcess.size() >= 100) {
                    if (x == 100) {

                        //System.out.println(phoneList.toString());
                        String phoneStr = phoneList.toString();

                        //System.out.println(phoneStr);
                        String aux = phoneStr.replace(",", ",\n");
                        aux = aux.replace("[", "");
                        aux = aux.replace("]", "");

                        FileBean fbean = new FileBean();

                        fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                        //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX
                        QueryManagerDAO result = new QueryManagerDAO();

                        //System.out.println(result.retrieveAccession());
                        result.doAccession(result.retrieveAccession()); //CORRETO
                        contador++;
                        //result.doAccession(); //TESTE

                        //System.out.println("\n----------\n");
                        phoneList.clear();
                        x = 0;
                    } else if (calc < 100) {
                        String phoneStr = phoneList.toString();

                        //System.out.println(phoneStr);
                        String aux = phoneStr.replace(",", ",\n");
                        aux = aux.replace("[", "");
                        aux = aux.replace("]", "");

                        //System.out.println(aux);
                        FileBean fbean = new FileBean();

                        fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                        //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX
                        QueryManagerDAO result = new QueryManagerDAO();

                        //System.out.println(result.retrieveAccession());
                        result.doAccession(result.retrieveAccession()); //CORRETO
                        contador++;
                        //result.doAccession(); //TESTE

                        phoneList.clear();
                    }
                } 
                else {
                    String phoneStr = phoneList.toString();

                    //System.out.println(phoneStr);
                    String aux = phoneStr.replace(",", ",\n");
                    aux = aux.replace("[", "");
                    aux = aux.replace("]", "");

                    //System.out.println(aux);
                    FileBean fbean = new FileBean();

                    fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                    //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX
                    QueryManagerDAO result = new QueryManagerDAO();

                    //System.out.println(result.retrieveAccession());
                    result.doAccession(result.retrieveAccession()); //CORRETO
                    contador++;
                    //result.doAccession(); //TESTE

                    phoneList.clear();
                }

                //TERMINO DE LEITURA DO CSV
            }
            phoneListProcess.clear();
            System.out.println("------------------------------\n------------------------------");
        }
             System.out.println("------------------------------\n------------------------------");
        }
        //int tot = contador*3;
        //System.out.println("CONTADOR: "+contador);
        //System.out.println("TOTAL DE INSERTS: "+tot);
    }
    
    public void readFile(ArrayList<String> listaProc) {
        
        int contador = 0;

        InputStream xlsx = null;

        Boolean b = false;

        ArrayList<String> phoneList = new ArrayList<>();
        ArrayList<String> phoneListProcess = new ArrayList<>();
        ArrayList<RegBean> regList = new ArrayList<>();// GRAVA REGISTROS NUMERO,ID,VALOR
        ArrayList<String> difIdList = new ArrayList<>();// QUANTOS IDS DIFERENTES O ARQUIVO POSSUI
        ArrayList<String> list = new ArrayList<>();
        String currId = "";
        
        ArrayList<String> difValList = new ArrayList<>();
        String currVal = "";
        
        FileBean fb = new FileBean();
        ConnectionDAO conn = new ConnectionDAO();

        File dir = new File("TEMP");
        if (!dir.exists()) {
            b = dir.mkdir();
        }

        dir.setWritable(true);
        String path = dir.getPath();

        //csvformated.csv
        //String source = path + "/jacksonCSVDIA04-2017CaminhaoA.csv";//MUDAR O ARQUIVO AQUI
        //String source = path + "/"+fileName;//MUDAR O ARQUIVO AQUI

       // System.out.println(">>>> Have to create a directory: " + b);

        //System.out.println("file: " + source);

        //INICIO DE LEITURA DE CSV
        try {
            //Scanner scanner = new Scanner(new File(source));

            //scanner.useDelimiter(",");
            //while (listaProc.hasNext()) {
                
            for(String listRegistros : listaProc){

                //String phone = scanner.next();
                String splitPhone[] = listRegistros.split(";");

                String reg = splitPhone[0];

                String phone = splitPhone[0];//AQUI

                if (phone.startsWith("ï»¿")) {
                    phone = phone.substring(3);
                }

                //List all regs
                regList.add(new RegBean(phone, splitPhone[1], splitPhone[2]));

                //IDENTIFICANDO IDS DIFERENTES
                if (currId.equals("")) {
                    currId = splitPhone[1];
                    difIdList.add(splitPhone[1]);
                }

                if (!difIdList.contains(splitPhone[1])) {
                    difIdList.add(splitPhone[1]);
                    currId = splitPhone[1];
                }
                
                //IDENTIFICANDO VALORES DIFERENTES
                if (currVal.equals("")) {
                    currVal = splitPhone[2];
                    difValList.add(splitPhone[2]);
                }

                if (!difValList.contains(splitPhone[2])) {
                    difValList.add(splitPhone[2]);
                    currVal = splitPhone[2];
                }
                
            }
        } catch (Exception ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        //System.out.println(phoneListProcess);
        System.out.println("fb - " + fb.getRegId());
        System.out.println("DF ID LIST -> " + difIdList);
        System.out.println("DF VAL LIST -> "+ difValList);

        /*for (int v = 0; v < difValList.size(); v++) {
            //List all regs 
            for (RegBean auxV : regList) {
               // System.out.println("-> " + aux.toString());
                String[] regPosition = auxV.toString().split(";");
                //System.out.println("pos-> "+regPosition[1]);
                //System.out.println("pos dif-> "+difIdList.get(i).toString());
                if (difValList.get(v).equals(regPosition[2]) && ) {
                    phoneListProcess.add(regPosition[0]);
                    fb.setRegVal(regPosition[2]);
                }
                
            }
        */
            
       for (int v = 0; v < difValList.size(); v++) {   
            
             for (int i = 0; i < difIdList.size(); i++) {
            
                for (RegBean aux : regList) {
                   // System.out.println("-> " + aux.toString());
                    String[] regPosition = aux.toString().split(";");
                    //System.out.println("pos-> "+regPosition[1]);
                    //System.out.println("pos dif-> "+difIdList.get(i).toString());

                    if (difValList.get(v).equals(regPosition[2]) && difIdList.get(i).equals(regPosition[1])) {
                        phoneListProcess.add(regPosition[0]);
                        fb.setRegVal(regPosition[2]);
                        fb.setRegId(regPosition[1]);
                    }

                }
        
           // for (int i = 0; i < difIdList.size(); i++) {
            //List all regs 
            /*for (RegBean aux : regList) {
               // System.out.println("-> " + aux.toString());
                String[] regPosition = aux.toString().split(";");
                //System.out.println("pos-> "+regPosition[1]);
                //System.out.println("pos dif-> "+difIdList.get(i).toString());
                if (difIdList.get(i).equals(regPosition[1])) {
                    phoneListProcess.add(regPosition[0]);
                    fb.setRegId(regPosition[1]);
                }
                
            }*/
            int totLines = (regList.size());
            int x = 0;

//Aqui tem que ser o numero de registros já filtrado
            for (int c = 0; c < phoneListProcess.size(); c++) {

                int calc = (phoneListProcess.size() - c);
                x++;

                //if(x <= 100){
                String phoneProcessed = phoneListProcess.get(c);

                phoneList.add(phoneProcessed);

                if (phoneListProcess.size() >= 100) {
                    if (x == 100) {

                        //System.out.println(phoneList.toString());
                        String phoneStr = phoneList.toString();

                        //System.out.println(phoneStr);
                        String aux = phoneStr.replace(",", ",\n");
                        aux = aux.replace("[", "");
                        aux = aux.replace("]", "");

                        FileBean fbean = new FileBean();

                        fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                        //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX
                        QueryManagerDAO result = new QueryManagerDAO();

                        //System.out.println(result.retrieveAccession());
                        result.doAccession(result.retrieveAccession()); //CORRETO
                        contador++;
                        //result.doAccession(); //TESTE

                        //System.out.println("\n----------\n");
                        phoneList.clear();
                        x = 0;
                    } else if (calc < 100) {
                        String phoneStr = phoneList.toString();

                        //System.out.println(phoneStr);
                        String aux = phoneStr.replace(",", ",\n");
                        aux = aux.replace("[", "");
                        aux = aux.replace("]", "");

                        //System.out.println(aux);
                        FileBean fbean = new FileBean();

                        fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                        //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX
                        QueryManagerDAO result = new QueryManagerDAO();

                        //System.out.println(result.retrieveAccession());
                        result.doAccession(result.retrieveAccession()); //CORRETO
                        contador++;
                        //result.doAccession(); //TESTE

                        phoneList.clear();
                    }
                } 
                else {
                    String phoneStr = phoneList.toString();

                    //System.out.println(phoneStr);
                    String aux = phoneStr.replace(",", ",\n");
                    aux = aux.replace("[", "");
                    aux = aux.replace("]", "");

                    //System.out.println(aux);
                    FileBean fbean = new FileBean();

                    fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                    //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX
                    QueryManagerDAO result = new QueryManagerDAO();

                    //System.out.println(result.retrieveAccession());
                    result.doAccession(result.retrieveAccession()); //CORRETO
                    contador++;
                    //result.doAccession(); //TESTE

                    phoneList.clear();
                }

                //TERMINO DE LEITURA DO CSV
            }
            phoneListProcess.clear();
            System.out.println("------------------------------\n------------------------------");
        }
             System.out.println("------------------------------\n------------------------------");
        }
        //int tot = contador*3;
        //System.out.println("CONTADOR: "+contador);
        //System.out.println("TOTAL DE INSERTS: "+tot);
    }
    

     public void readFile(String fileName, String id, String valor) {
         int contador = 0;
        InputStream xlsx = null;

        Boolean b = false;
             
        ArrayList<String> phoneList = new ArrayList<>();
        ArrayList<String> phoneListProcess = new ArrayList<>();
        
        ArrayList<RegBean> regList = new ArrayList<>();
        
        ConnectionDAO conn = new ConnectionDAO();

        File dir = new File("TEMP");
        if (!dir.exists()) {
            b = dir.mkdir();
        }

        dir.setWritable(true);
        String path = dir.getPath();
        
        //csvformated.csv
        //String source = path + "/jacksonCSVDIA04-2017CaminhaoA.csv";//MUDAR O ARQUIVO AQUI
        
        String source = path + "/"+fileName;//MUDAR O ARQUIVO AQUI
        
        FileBean fb = new FileBean();
        
        fb.setRegId(id);
        
        fb.setRegVal(valor);

        System.out.println(">>>> Have to create a directory: " + b);

        System.out.println("file: " + source);
        
        //conn.oraConn();
        //conn.postgreConn();
        //conn.
        //INICIO DE LEITURA DE CSV
        try {
            Scanner scanner = new Scanner(new File(source));
            
            //scanner.useDelimiter(",");
            while(scanner.hasNext()){
               
                //String phone = scanner.next();
                
                String splitPhone[] = scanner.nextLine().split(";");
                
                String splitLine[] = splitPhone[0].split(",");
                                
                // System.out.println(splitPhone[0]);AQUI
                /*String phone = splitLine[0];
                
                if(phone.startsWith("ï»¿")){
                    phone = phone.substring(3);
                }*/
                
                String phone = splitPhone[0];//AQUI
                
                if(phone.startsWith("ï»¿")){
                    phone = phone.substring(3);
                }
                
               /* 
                System.out.println("fone "+splitLine[0]+"\n");//AQUI
                System.out.println("produto "+splitLine[1]+"\n");
                System.out.println("valor "+splitLine[2]+"\n");*/
               // System.out.println(phone);
                //String phone = splitPhone[1]+splitPhone[2];
                //String produto = splitPhone[1];
                //String valor = splitPhone[2];
                
                //System.out.println(phone);
                
               // regList.add(new RegBean(phone, splitPhone[1], splitPhone[2]));
                
                phoneListProcess.add(phone);
                //System.out.println(phone+"***");
               
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //System.out.println(phoneListProcess);
        /*for (RegBean aux : regList){
        System.out.println("-> "+aux.toString());
        }*/
        int totLines = (phoneListProcess.size());         
        int x = 0;
        

        for (int c = 0; c < phoneListProcess.size(); c++) {

            int calc = (phoneListProcess.size() - c);
            x++;
            
            //if(x <= 100){
                String phoneProcessed = phoneListProcess.get(c);
           
                phoneList.add(phoneProcessed);
                
                if(phoneListProcess.size() >= 100){
                    if(x == 100){
                        
                        //System.out.println(phoneList.toString());


                        String phoneStr = phoneList.toString();

                        //System.out.println(phoneStr);

                        String aux = phoneStr.replace(",", ",\n");
                        aux = aux.replace("[","");
                        aux = aux.replace("]","");

                        FileBean fbean = new FileBean();

                        fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                        //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX

                        QueryManagerDAO result = new QueryManagerDAO();

                        //System.out.println(result.retrieveAccession());

                        result.doAccession(result.retrieveAccession()); //CORRETO
                        contador++;
                          //result.doAccession(); //TESTE

                        //System.out.println("\n----------\n");

                        phoneList.clear();
                        x=0;
                    }
                    else if(calc < 100){
                        String phoneStr = phoneList.toString();

                        //System.out.println(phoneStr);

                        String aux = phoneStr.replace(",", ",\n");
                        aux = aux.replace("[","");
                        aux = aux.replace("]","");

                        //System.out.println(aux);

                        FileBean fbean = new FileBean();

                        fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                        //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX

                        QueryManagerDAO result = new QueryManagerDAO();

                        //System.out.println(result.retrieveAccession());

                        result.doAccession(result.retrieveAccession()); //CORRETO
                        contador++;
                          //result.doAccession(); //TESTE

                        phoneList.clear();
                    }
                }
                
               /* if(phoneListProcess.size() >= 100){
                    if(x == 100){
                        //System.out.println(phoneList.toString());


                        String phoneStr = phoneList.toString();

                        //System.out.println(phoneStr);

                        String aux = phoneStr.replace(",", ",\n");
                        aux = aux.replace("[","");
                        aux = aux.replace("]","");

                        FileBean fbean = new FileBean();

                        fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO

                        //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX

                        QueryManagerDAO result = new QueryManagerDAO();

                        //System.out.println(result.retrieveAccession());

                          result.doAccession(result.retrieveAccession()); //CORRETO
                          //result.doAccession(); //TESTE

                        //System.out.println("\n----------\n");

                        phoneList.clear();
                        x=0;
                    }
                }*/
                else{
                    String phoneStr = phoneList.toString();
                    
                    //System.out.println(phoneStr);
                
                    String aux = phoneStr.replace(",", ",\n");
                    aux = aux.replace("[","");
                    aux = aux.replace("]","");
                    
                    //System.out.println(aux);

                    FileBean fbean = new FileBean();

                    fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO
                    
                    //System.out.println(fbean.getPhoneNumbers()); //PEGA TODOS OS NUMEROS DO ARQUIVO XLSX

                    QueryManagerDAO result = new QueryManagerDAO();

                    //System.out.println(result.retrieveAccession());
                    
                      result.doAccession(result.retrieveAccession()); //CORRETO
                      contador++;
                      //result.doAccession(); //TESTE

                    phoneList.clear();
                }
        
        //TERMINO DE LEITURA DO CSV
        
        
       //INICIO DE LEITURA DE ARQUIVO XLSX
        /*
        try {
            xlsx = new FileInputStream(source);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        //HSSFWorkbook & HSSFSheet
        //XSSFSheet & XSSFSheet
        XSSFWorkbook workbook = null;

        try {
            workbook = new XSSFWorkbook(xlsx);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        //System.out.println("workbook "+ workbook.getSheetName(0));
        Sheet sheet = workbook.getSheetAt(0);
        System.out.println("LINHAS NO XLS " + (sheet.getLastRowNum() + 1));// MAYBE +1
        
        int totLines = (sheet.getLastRowNum() + 1);         
        int x = 0;

        for (int c = 0; c <= sheet.getLastRowNum(); c++) {

            x++;
            
            //if(x <= 100){
                Row row = sheet.getRow(c);
                String phoneProcessed = (int) row.getCell(1).getNumericCellValue() + "" + (int) row.getCell(2).getNumericCellValue();
           
                phoneList.add(phoneProcessed);
                if(x == 100){
                    //System.out.println(phoneList.toString());
                    
                    
                    String phoneStr = phoneList.toString();
        
                    String aux = phoneStr.replace(",", ",\n");
                    aux = aux.replace("[","");
                    aux = aux.replace("]","");

                    FileBean fbean = new FileBean();

                    fbean.setPhoneNumbers(aux); //SETA TODOS OS TELEFONES ENCONTRADOS NO ARQUIVO
                    
                    //System.out.println(fbean.getPhoneNumbers()); PEGA TODOS OS NUMEROS DO ARQUIVO XLSX

                    QueryManagerDAO result = new QueryManagerDAO();

                    //System.out.println(result.retrieveAccession());
                    
                    result.doAccession(result.retrieveAccession());
                    
                    //System.out.println("\n----------\n");
                    
                    phoneList.clear();
                    x=0;
                }
           // }   
            //System.out.println("PHONE -> " + phoneProcessed);*/
        }//COLOCAR FINAL AQUI
       /* String phoneStr = phoneList.toString();
        
        String aux = phoneStr.replace(",", ",\n");
        aux = aux.replace("[","");
        aux = aux.replace("]","");
        
        FileBean fbean = new FileBean();
        
        fbean.setPhoneNumbers(aux);
        
        QueryManagerDAO result = new QueryManagerDAO();
        
        System.out.println(result.retrieveAccession());*/
        
        //System.out.println(fbean.getPhoneNumbers());
    }
     
    public void enterAccession(ArrayList<UserObj> listaProc) {
        
        for(UserObj user : listaProc){
            
        }
        
    }
     
     public void cancelAccession(ArrayList<String> phones){
         QueryManagerDAO doit = new QueryManagerDAO();
         
         doit.cancel(phones);
     }
}
