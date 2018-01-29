/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.jcnsistemas.api.inregdb.model.dao;

import br.com.jcnsistemas.api.inregdb.model.FileBean;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Date;


/**
 *
 * @author rafael.inocenti
 */
public class QueryManagerDAO {
    
    //protected Connection connOra = new ConnectionDAO().oraConn();
    
    //protected Connection connPostgre = new ConnectionDAO().postgreConn();
    
    public String retrieveAccession(){
        ArrayList<String> queryList = new ArrayList<String>();
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        //System.out.println(dateFormat.format(date));
        
        FileBean phones = new FileBean();
        ResultSet rs = null;
        String query = "SELECT 'SELECT * FROM recorrencia_jcn.firstcharge(''"+dateFormat.format(date)+"'', "+phones.getRegVal()+", "+phones.getRegId()+", '''||NR_CPF||''', ''ADESAORECORRENCIA'', '''||NR_TELEPHONE||''', '''||TOP.DS_OPERATOR||''', 0, ''CALLCENTER'', '''');' FROM TUSER TU INNER JOIN TOPERATOR TOP ON (TU.CD_OPERATOR = TOP.CD_OPERATOR AND TOP.CD_TYPE_TRANSACTION = 'B0') WHERE TU.TS_LAST_CHANGE = (SELECT MAX(TS_LAST_CHANGE) FROM TUSER WHERE NR_TELEPHONE = TU.NR_TELEPHONE) AND TU.NR_TELEPHONE IN("+phones.getPhoneNumbers()+")";
          // System.out.println("Query " + query);
//String query = "select * from tfacil_show";
        
        String result = null;
        Connection connOra = new ConnectionDAO().oraConn();
        try {
            
            PreparedStatement ps = connOra.prepareStatement(query);
            
            rs = ps.executeQuery();
                      
            while(rs.next()){
                result = rs.getString(1);
                queryList.add(result);
            }
           
        } catch (SQLException ignore) {
            
        }finally{
           if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
           if (connOra != null) try { connOra.close(); } catch (SQLException ignore) {}
        }
        
        String groupOfQuerys = queryList.toString();
        groupOfQuerys = groupOfQuerys.replace("[", "");
        groupOfQuerys = groupOfQuerys.replace("]", "");
        groupOfQuerys = groupOfQuerys.replace(";,", ";\n");
                
        return groupOfQuerys;
    }
    
    public void cancel(ArrayList<String> phones){
        //START
        ArrayList<String> idList = new ArrayList<String>();
        
        for(String phone : phones){
        
            try {
                String retrieveUserIds = "select distinct u.id as quantidade from recorrencia_jcn.cobranca c " +
                        "inner join recorrencia_jcn.usuarios u on c.id_usuario = u.id " +
                        "where u.telefone = '"+phone+"' " +
                        "group by u.id";

                ResultSet rs = null;

                Connection connPostgre = new ConnectionDAO().postgreConn();

                PreparedStatement pstm = connPostgre.prepareStatement(retrieveUserIds);
                rs = pstm.executeQuery();

                while(rs.next()){
                    idList.add(rs.getString(1));
                }

                //SELECT * FROM recorrencia_jcn.stoprecurrence(id_user, 2);
            } catch (SQLException ex) {
                Logger.getLogger(QueryManagerDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(idList);
        //END
    }
    
    public void doAccession(String selects){
                
        System.out.println(selects);
        
        try {
            Connection conn = new ConnectionDAO().oraConn();
            
            if(!conn.isClosed()){
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(QueryManagerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
      
        //select * from recorrencia_jcn.operadoras
       
        
        //VERIFICAR CONDIÇÕES A PARTIR DAQUI
        ArrayList<String> queryList = new ArrayList<String>();
        ResultSet rs = null;
        String query = selects;
        /*String query = "select * from recorrencia_jcn.tipo_produtos;"
                + "select * from recorrencia_jcn.tipo_produtos;"
                + "select * from recorrencia_jcn.tipo_produtos;";*/
        /*
        String query = "insert into recorrencia_jcn.testeTB (id, testea, testeb, testec, tested) values(0,'A','B','C','D');" 
                      +"insert into recorrencia_jcn.testeTB (id, testea, testeb, testec, tested) values(1,'E','F','G','H');"
                      +"insert into recorrencia_jcn.testeTB (id, testea, testeb, testec, tested) values(2,'I','J','K','L');";
          */      
        String[] querySplit = query.split(";");
        
        String result;
        
        ArrayList<String> teste = new ArrayList<String>();
        
        Connection connPostgre = new ConnectionDAO().postgreConn();
        
        try {
            
            
            
            //CÓDIGO PARA TESTES COMEÇA AQUI.
            /*for(int i = 0; i < querySplit.length; i++){
                teste.add("insert into recorrencia_jcn.testeTB (id, testea, testeb, testec, tested) values(0,'A','B','C','D');");
            }
                String a = teste.toString();
                 a = a.replace("[", "");
                 a = a.replace("]", "");
                 a = a.replace(";,", ";\n");
                
                 CallableStatement st = connPostgre.prepareCall(a);*/
            //TERMINA AQUI.     
                 
                 CallableStatement st = connPostgre.prepareCall(query); // ATIVAR ESSA LINHA PARA CÓDIGO DE PRODUÇÃO
                
                rs = st.executeQuery();
            

                while(rs.next()){
                    result = rs.getString(1)+rs.getString(2);
                    result = rs.getString(2);
                   // System.out.println(i+"> "+querySplit[i]);
                    queryList.add(result);
                }
            //}
            
           System.out.println("registers -> "+querySplit.length);
            System.out.println(queryList);
            queryList.clear();
           
           
        } catch (SQLException ignore) {
            
        }finally{
           if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
           if (connPostgre != null) try { connPostgre.close(); } catch (SQLException ignore) {}
        }
        
       //System.out.println(">> "+queryList);
        
        //System.out.println("\n");
        
        //return "aaa";//terminei aqui*/
    }
}