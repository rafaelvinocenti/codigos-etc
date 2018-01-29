/*
Module:	contact us - board(DP)
Date: 	2012.09.06.
Author:	Rafael Garcia Lopes Vaz Inocenti
---------------------------------------------------------------
Modification Log
Date:
Author:
Modified:
---------------------------------------------------------------
*/
package com.hm.br.dp.community.contactus.board;

import java.util.Hashtable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.hm.br.common.HKMPortalComponent;
import com.hm.br.common.dp.DpUtil;
import com.hm.br.common.rfc.RfcHelper;
import com.hm.br.common.ume.UmeUtil;
import com.hm.br.common.util.CommonLocalization;
import com.hm.br.common.util.CommonProperties;
import com.hm.br.common.util.StringUtil;
import com.hm.br.dp.community.contactus.bean.ContactUsBean;
import com.hm.br.dp.community.contactus.dao.ContactUsDAO;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;
import com.sapportals.portal.prt.resource.IResource;

public class ContactUsAnswer extends HKMPortalComponent
{
    public void doContent(IPortalComponentRequest request, IPortalComponentResponse response)
    {
    	
    	HttpServletRequest servletRequest = request.getServletRequest();
    	
    	ContactUsDAO dbAction = new ContactUsDAO(request, response);
    	
    	ContactUsBean data = new ContactUsBean();

    	
    	String strAnswerID = request.getUser().getLogonUid();
		String strAnswerName = request.getUser().getLastName() + " " +StringUtil.checkNull(request.getUser().getFirstName(),"");
		
    	
    	String reqAction = request.getParameter("reqAction");
    	String aStatus = request.getParameter("aStatus");
    	String reqId = request.getParameter("reqId");   
    	
    	
    	String companyCode = UmeUtil.getCompanyCode(request);
    	
    	Hashtable groupInfo = UmeUtil.getContactUsAdminGroup(request,companyCode);
		String strContactUsAdmin = StringUtil.checkNull((String)groupInfo.get("CONTACTUS_ADMIN"),"");// Y : Admin

    	
    	servletRequest.setAttribute("adminGroup",strContactUsAdmin);
    	
    	
    	// NULL
    	/*if(reqAction != null || reqAction.trim().length() != 0)
    	{
        	reqAction = "";
    	}*/
    	
    	Cookie[] cookies = request.getCookies();
		
		String d = cookies[0].getValue();
		String s = cookies[1].getValue();
		String i = cookies[2].getValue();
		String yesno = cookies[3].getValue();
		String rnm = cookies[5].getValue();
		String dtFrom = cookies[6].getValue();
		String dtTo = cookies[7].getValue();
		String c_dName = cookies[8].getValue();
		String c_sName = cookies[9].getValue();
		String c_iName = cookies[10].getValue();
		
		servletRequest.setAttribute("c_department", d);
		servletRequest.setAttribute("c_subject", s);
		servletRequest.setAttribute("c_issue", i);
		servletRequest.setAttribute("c_yesno", yesno);
		servletRequest.setAttribute("c_dtFrom", dtFrom);
		servletRequest.setAttribute("c_reqNm", rnm);
		servletRequest.setAttribute("c_dtTo", dtTo);
		
		servletRequest.setAttribute("c_dName", c_dName);
		servletRequest.setAttribute("c_sName", c_sName);
		servletRequest.setAttribute("c_iName", c_iName);
    	
    	RfcHelper rfcHelper = new RfcHelper(request, CommonProperties.getDPRFCAlias(request));
    	
    	
    	String strDealerCode 	= this.getLoginUserDealerCode(request);
    	//BY now My code is 26015
    	
    	String strDealerName = DpUtil.getDealerName(request, strDealerCode);
    	    	    		
    	
    	request.getServletRequest().setAttribute("webrs", CommonProperties.getWebResource(request));
    	servletRequest.setAttribute("label", new CommonLocalization(request.getLocale()));
    	servletRequest.setAttribute("dealerName", strDealerName);
    	servletRequest.setAttribute("dealerCode", strDealerCode);
    	
    	request.getServletRequest().setAttribute("webrs", CommonProperties.getWebResource(request));
    	servletRequest.setAttribute("label", new CommonLocalization(request.getLocale()));
    	
    	//THE FORM ACTION
    	servletRequest.setAttribute("EXEC_URL", "/irj/servlet/prt/portal/prtroot/com.hm.br.dp.community.contactus.ContactUsAnswer?reqId="+reqId+"&aStatus="+aStatus+"");
    	servletRequest.setAttribute("LIST_URL", "/irj/servlet/prt/portal/prtroot/com.hm.br.dp.community.contactus.ContactUsAnswerList");
    	servletRequest.setAttribute("DOWN_URL", "/irj/servlet/prt/portal/prtroot/com.hm.br.dp.community.contactus.ContactUsDown");

    	
    	try{
    		dbAction.connectDB();
    		
    		

        	data = dbAction.getSubjectData(reqId);
        	
        	/*response.write(data.getReq_nm()+"<br>");
        	response.write(data.getReq_phone()+"<br>");
        	response.write(data.getMcode()+"<br>");
        	response.write(data.getScode()+"<br>");
        	response.write(data.getTcode()+"<br>");
        	response.write(data.getContents()+"<br>");
        	response.write(data.getDealer_code()+"<br>");
        	response.write(data.getStatus()+"<br>");
        	response.write(data.getReqDate()+"<br>");*/
        	
        	String setReqName = data.getReq_nm();
        	String setReqEmail = data.getReq_email();
        	String setReqPhone = data.getReq_phone();
        	String setReqCat = data.getMcode();
        	int setReqSubj = data.getScode();
        	int setReqIssue = data.getTcode();
        	String setReqCont = data.getContents();
        	String setReqDcode = data.getDealer_code();
        	String setReqDname = data.getDealer_nm();
        	String setReqStatus = data.getStatus();
        	String setReqDate = data.getReqDate();
        	
        	String setDepartment = data.getDepartment();
        	String setSubject = data.getSubject();
        	String setIssue = data.getIssue();
        	String setAnswer = data.getAnswer();
        	String setFileName = data.getFile_key();

        	
        	
        	servletRequest.setAttribute("setReqName", setReqName);
        	servletRequest.setAttribute("setReqEmail", setReqEmail);
        	servletRequest.setAttribute("setReqPhone", setReqPhone);
        	servletRequest.setAttribute("setReqCat", setReqCat);
        	servletRequest.setAttribute("setReqSubj", setReqSubj);
        	servletRequest.setAttribute("setReqIssue", setReqIssue);
        	servletRequest.setAttribute("setReqCont", setReqCont);
        	servletRequest.setAttribute("setReqDcode", setReqDcode);
        	servletRequest.setAttribute("setReqDname", setReqDname);
        	servletRequest.setAttribute("setReqStatus", setReqStatus);
        	servletRequest.setAttribute("setReqDate", setReqDate);
        	
        	servletRequest.setAttribute("setDepartment", setDepartment);
        	servletRequest.setAttribute("setSubject", setSubject);
        	servletRequest.setAttribute("setIssue", setIssue);
        	
        	servletRequest.setAttribute("setAnswer", setAnswer);
        	
        	servletRequest.setAttribute("setFileName", setFileName);
        	
        	

        	
        	
        	//servletRequest.setAttribute("setAnswerStatus", setAnswerStatus);*/
        	
        	
        	if(reqAction != null)
        	{	
        		data.setAns_nm(strAnswerName);
        		data.setAnswer(request.getParameter("content"));
        		//data.setDealer_nm(strDealerName);
        		data.setAns_id(strAnswerID);
        		data.setStatus(request.getParameter("reqAction"));
        		
        		dbAction.updateAnswerRequest(data, reqId);
        		response.write("<script>window.location=/irj/servlet/prt/portal/prtroot/com.hm.br.dp.community.contactus.ContactUsAnswer?reqId="+reqId+"&aStatus="+aStatus+";</script>");
        	}
        	
        	
        	
    	}catch (Exception e)
    	{
    		response.write("no database connection");
    	}
    	finally
    	{
    		// disconnect from DB
			dbAction.disconnectDB();
    	}
    	
    	 try{
    		 IResource jspResource = request.getResource(IResource.JSP, "jsp/contactUsAnswer.jsp");
				response.include(request, jspResource);
				
    	 }
    	 catch (Exception e) {
				response.write("<br>Contact US have a fail... : "+e.getMessage());
			}
    	    	    	
    }
}