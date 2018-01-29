/*
Module:	contact us - board(DP)
Date: 	2012.09.14.
Author:	Rafael Garcia Lopes Vaz Inocenti
---------------------------------------------------------------
Modification Log
Date:
Author:
Modified:
---------------------------------------------------------------
*/
package com.hm.br.dp.community.contactus.board;
 
import java.io.File;
import java.sql.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.hm.br.common.HKMPortalComponent;
import com.hm.br.common.dp.DpUtil;
import com.hm.br.common.util.StringUtil;
import com.hm.br.dp.community.contactus.bean.ContactUsBean;
import com.hm.br.dp.community.contactus.dao.ContactUsDAO;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserFactory;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;

public class ContactUsDataManager extends HKMPortalComponent
{
    public void doContent(IPortalComponentRequest request, IPortalComponentResponse response)
    {
    	
    	
    	HttpServletRequest servletRequest = request.getServletRequest();
    	
    	
    	IUserFactory userFactory = UMFactory.getUserFactory();
    	try {
			IUser specifiedUser = userFactory.getUserByLogonID(request.getUser().getLogonUid());
		} catch (UMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		String strDealerCode 	= this.getLoginUserDealerCode(request);  	
    	String strDealerName = DpUtil.getDealerName(request, strDealerCode);
    	String strRequesterName = request.getUser().getLastName() + " " +StringUtil.checkNull(request.getUser().getFirstName(),"");
    	String strRequestID = request.getUser().getLogonUid();
    	
    	/*
    	String strRequestID = request.getUser().getDisplayName();
		String strRequestName = request.getUser().getLastName() + " " +request.getUser().getFirstName();
		String strDealerCode 	= this.getLoginUserDealerCode(request);   	
    	String strDealerName = DpUtil.getDealerName(request, strDealerCode);
    	*/
    	
    	
    	ContactUsDAO dbAction = new ContactUsDAO(request, response);
    	
    	ContactUsBean data = new ContactUsBean();
    	
    	String subject = "";
    	String issue = "";
    	String reqId = request.getParameter("reqId");
    	//String cancel = request.getParameter("cancel");
    	
    	String fileName = "";
    	String req_name = "";
    	String req_email = "";
    	String req_phone = "";
    	String req_contents = "";
    	String dealer_code = "";
    	String status = "";
    	
    	
    	String FILE_PATH_CONTACTUS = "/sapmnt/" + System.getProperty("SAPSYSTEMNAME") + "/global/";
    	
    	
    	boolean isMultipart = ServletFileUpload.isMultipartContent(servletRequest);
		if(isMultipart) {
			try {
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				List<FileItem> items = (List<FileItem>)upload.parseRequest(servletRequest);
				for(FileItem item : items) {
					if(item.isFormField()) 
					{
						if(item.getFieldName().equals("dep_subject"))
						{
							subject = item.getString();
						}
						if(item.getFieldName().equals("issue"))
						{
							issue = item.getString();
							//response.write("this issue -> "+issue);
						}
						if(item.getFieldName().equals("req_name"))
						{
							req_name = item.getString();
						}
						if(item.getFieldName().equals("req_email"))
						{
							req_email = item.getString();
						}
						if(item.getFieldName().equals("req_phone"))
						{
							req_phone = item.getString();
						}
						if(item.getFieldName().equals("content"))
						{
							req_contents = item.getString();
						}
						if(item.getFieldName().equals("dealerCode"))
						{
							dealer_code = item.getString();
						}
						if(item.getFieldName().equals("reqAction"))
						{
							status = item.getString();
						}
					}
					else
					{
						
						//if file type
						/*response.write("NOT Form field<br>");
						response.write("<br>Name:"+item.getFieldName());
						response.write("<br>FileName:"+item.getName());
						response.write("<br>Size:"+item.getSize());
						response.write("<br>ContentType:"+item.getContentType());*/
						
						if(item.getName() != "")
						{
						fileName = dealer_code+"_"+item.getName();
						//response.write("file - > "+item.getName());
						
						File uploadedFile = new File(FILE_PATH_CONTACTUS+fileName);
		        		item.write(uploadedFile);
						}
						else
						{
							fileName = "";
						}
			    		
						
	
					}
				}
			} catch (Exception e) {
				response.write("problem to make a  upload: " + e.getMessage());
			}
		}

		//MCODE SCODE TCODE MODIFICATIONS
		String t1 = subject;
    	String t2 = issue;

    	
    	String mcode = "";
    	String scode = "";
    	String tcode = "";
    	
    	
    	
    	/*response.write(issue+"</br>");
    	response.write(subject+"</br>");
    	response.write(reqId+"</br>");*/
    	
    	//response.write(text2);
    
    	if(!issue.equals("") && !subject.equals(""))
    	{
    	if(t2.equals("all") && t1.equals("DRD-1") || t1.equals("DRD-2")|| t1.equals("DRD-3")|| t1.equals("DRD-4")|| t1.equals("DRD-5")|| t1.equals("DRD-6"))
    	{
    		String[] cut = t1.split("-");
    		mcode = cut[0];
    		scode = cut[1];
    		tcode = "99";//FIX IT
    	}
    	else
    	{
    		String[] cut = t2.split("-");
    		mcode = cut[0];
    		scode = cut[1];
    		tcode = cut[2];
    	}	
    	}
    	//////////////////////////////
    	
    	//response.write(fileName+"<br>"+FILE_PATH+"<br>");
    	//response.write("<br><br>"+subject+"<br>"+issue+"<br>"+req_name+"<br>"+req_email+"<br>"+req_phone+"<br>"+req_contents+"<br>"+dealer_code+"<br>"+status+"<br>"+fileName+"<br>");
    	
    	//TEST OUTPUT
    	 /* response.write(mcode+"</br>");
    	  response.write(scode+"</br>");
    	  response.write(tcode+"</br>");
    	  */
    	
    	/*TEST STATUS BUTTONS
    	 * String button = request.getParameter("btnStatus");
    	 * response.write(button);
    	 */
    	
    	/*TEST ALL DATA REQUEST*/
    	/*
    	response.write("REQUESTER (DEALER) NAME = "+request.getParameter("dealerName")+"</br>");
    	response.write("DEALER PHONE = "+request.getParameter("req_phone")+"</br>");
    	response.write("DEPARTMENT = "+mcode+"</br>");
    	response.write("SUBJECT = "+scode+"</br>");
    	response.write("ISSUE = "+tcode+"</br>");
    	response.write("REQUEST CONTENT :</br></br>"+request.getParameter("content")+"</br></br>");
    	response.write("DEALER CODE = "+request.getParameter("dealerCode")+"</br>");
    	response.write("OPERATION STATUS = "+request.getParameter("btnStatus")+"</br>");
    	*/
    	
    	//GET THE REQUEST FORM DATA
    	
    	//data.setReq_id(reqId); // DAO
    	//data.setAns_id(request.getParameter(""));//ID DA RESPOSTA - DAO
    
    	
    	
    	//ATTACHED FILE
    	//data.setFile_key(request.getParameter("req_file"));		//FILE ATTACHED
    	
    	//PAGE COUNT
    	//data.setRead_cnt(request.getParameter(""));//CONTADOR DE COMENTÁRIOS - HIDDEN

    	
    	//GET THE ANSWER FORM DATA

    	//data.setAns_nm(request.getParameter(""));					//ANSWER NAME
       	//data.setAnswer(request.getParameter(""));					//REQUEST ANSWER
       	//data.setDealer_nm(request.getParameter("dealerName"));		//DEALER NAME
    	
    	//PAGE COUNT
    	//data.setRead_cnt(request.getParameter(""));//CONTADOR DE COMENTÁRIOS - HIDDEN
    	
    	//TEST IF THE BEAN HAS BEEN SET TO BE GET IN THE DAO FUNCTION
    	//dbAction.test(data);
    	
     	try
     	{
     		dbAction.connectDB();
     		int id = dbAction.nextValMaster();
     		
     		//TABLE FIELDS DESCRIPTION
     		//dbAction.tableDesc();
     		
     		//LIST CONTACTUS MASTER TABLE
     		//dbAction.listContactUs();
     		
     		
     		
     		data.setReq_nm(strRequesterName);			//REQUESTER NAME 
     		data.setDealer_nm(strDealerName);
     		data.setReq_id(strRequestID); //Dealer ID NAME
        	data.setReq_email(req_email);
        	data.setReq_phone(req_phone);		//DEALER PHONE
        	
        	if(!issue.equals("") && !subject.equals(""))
        	{
        		data.setMcode(mcode);										//CATEGORY CODE
            	data.setScode(Integer.parseInt(scode));						//SUB CATAGORY
            	data.setTcode(Integer.parseInt(tcode));						//SUB SUB CATEGORY
        	}
        	else
        	{
        		dbAction.getMSTcodes(reqId);
        		mcode = dbAction.getMSTcodes(reqId).getMcode();
        		data.setMcode(mcode);
        		
            	data.setScode(dbAction.getMSTcodes(reqId).getScode());	
            	data.setTcode(dbAction.getMSTcodes(reqId).getTcode());
        	}
        	
        	data.setContents(req_contents);			//REQUEST CONTENT
        	data.setDealer_code(strDealerCode);	//DEALER CODE
        	
        	data.setStatus(status);			//REQUEST STATUS
        	
        	data.setFile_key(fileName);
        	
     		if(reqId.equals("null") && !status.equals("C"))
        	{
        		dbAction.insertContactRequest(data,id);
    
        	}
     		else if(!reqId.equals("null") && !status.equals("C"))
        	{
     			dbAction.updateRequest(data, reqId);
        	}
     		else if(status.equals("C"))
     		{
     			dbAction.cancel(reqId);
     		}
    		
    		
    		
    	}
     	catch (Exception e)
    	{
    		response.write("SQLException: " + e.getMessage());
    	}
    	finally
    	{
    		// disconnect from DB
			dbAction.disconnectDB();
    	}
    }
}