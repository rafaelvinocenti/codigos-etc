/*
Module:	contact us - board(DP)
Date: 	2012.09.06.
Author:	Rafael Garcia Lopes Inocenti
---------------------------------------------------------------
Modification Log
Date:
Author:
Modified:
---------------------------------------------------------------
*/
package com.hm.br.dp.community.contactus.board;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.hm.br.common.HKMPortalComponent;
import com.hm.br.common.dp.DpUtil;
import com.hm.br.common.ume.UmeUtil;
import com.hm.br.common.util.CommonLocalization;
import com.hm.br.common.util.CommonProperties;
import com.hm.br.common.util.StringUtil;
import com.hm.br.dp.community.contactus.bean.ContactUsBean;
import com.hm.br.dp.community.contactus.dao.ContactUsDAO;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserFactory;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;
import com.sapportals.portal.prt.resource.IResource;

public class ContactUsContent extends HKMPortalComponent
{
    public void doContent(IPortalComponentRequest request, IPortalComponentResponse response)
    {
    	HttpServletRequest servletRequest = request.getServletRequest();
    	
    	DpUtil DpUtil = new DpUtil(); 
    	//String strDealerCode 	= UmeUtil.getCookieValue(request,UmeUtil.COOKIE_NAME);
    	String strDealerCode 	= this.getLoginUserDealerCode(request);  
    	ContactUsDAO dbAction = new ContactUsDAO(request, response);
    	ContactUsBean data = new ContactUsBean();
    	
    	String page = StringUtil.checkNull(request.getParameter("pg"), "1");
    	
    	int pg = Integer.parseInt(page);
    	
    	//response.write("<BR><BR>THIS IS PG WITHOUT DAO - > "+pg+"<BR><BR>");
    	
    	//String pgTot = request.getParameter("pgTot");
    	
    	//response.write(pgTot);
    	
    	String dep = request.getParameter("dep");
    	String subj = request.getParameter("subj");
    	String iss = request.getParameter("iss");
    	String yesno = request.getParameter("yesno");
    	
    	String currPage = request.getParameter("currPage");
    	
    	String req_name = request.getParameter("req_nm");
		String reqFromDt = request.getParameter("rt_from_date");
		String reqToDt = request.getParameter("rt_to_date");
    	
    	
    	

    	
    	
    
    	
    	

////////////////////////////////
//ENQUIRY FIELDS FUNCTIONALITY//
////////////////////////////////
    	
 		String mcode = "";
  		String scode = "";
  		String tcode = "";
  		
  		String department = request.getParameter("department");
  		
  		//response.write(department);
		String subject = request.getParameter("dep_subject");
		//response.write(subject);
		
		String issue = request.getParameter("issue");
		//response.write(issue);
		
		String answered = request.getParameter("answered");
		
		
		
		Cookie d = new Cookie("d", department);
    	response.addCookie(d);
    	Cookie s = new Cookie("s", subject);
    	response.addCookie(s);
    	Cookie i = new Cookie("i", issue);
    	response.addCookie(i);
    	Cookie yn = new Cookie("yn", answered);
    	response.addCookie(yn);
    	Cookie cpg = new Cookie("cpg", currPage);
    	response.addCookie(cpg);
    	Cookie rnm = new Cookie("rnm", req_name);
    	response.addCookie(rnm);
    	Cookie fDT = new Cookie("fDT", reqFromDt);
    	response.addCookie(fDT);
    	Cookie tDT = new Cookie("tDT", reqToDt);
    	response.addCookie(tDT);
    	Cookie d_name = new Cookie("d_name", dep);
    	response.addCookie(d_name);
    	Cookie s_name = new Cookie("s_name", subj);
    	response.addCookie(s_name);
    	Cookie i_name = new Cookie("i_name", iss);
    	response.addCookie(i_name);
    	
    	
    	String nm = "";
    	String from = "";
    	String to = "";
    	
    	if(rnm.getValue() == null)
    	{
    		nm = "";
    	}
    	else
    	{
    		nm = rnm.getValue();
    	}
    	if(fDT.getValue() == null)
    	{
    		from = "";
    	}
    	else
    	{
    		from = fDT.getValue();
    	}
    	if(tDT.getValue() == null)
    	{
    		to = "";
    	}
    	else
    	{
    		to = tDT.getValue();
    	}
    	
    	
    	servletRequest.setAttribute("nm", nm);
    	servletRequest.setAttribute("from", from);
    	servletRequest.setAttribute("to", to);
    	//response.write("<br>reqName = "+nm);
    	//Cookie[] c = request.getCookies(); 
    	//servletRequest.setAttribute("c_requesterNM", rnm.getValue());
    	/*response.write("dept = "+d.getValue());
    	response.write("<br>subj = "+s.getValue());
    	response.write("<br>iss = "+i.getValue());
    	response.write("<br>yes/no = "+yn.getValue());
    	response.write("<br>currPage = "+cpg.getValue());
    	
    	response.write("<br>fromDT = "+fDT.getValue());
    	response.write("<br>toDT = "+tDT.getValue());*/
		
    	IUserFactory userFactory = UMFactory.getUserFactory();
    	try {
			IUser specifiedUser = userFactory.getUserByLogonID(request.getUser().getLogonUid());
		} catch (UMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
    	String reqName = request.getUser().getLastName() + " " +request.getUser().getFirstName();
		
		//response.write("department = "+department+"<br>subject = "+subject+"<br>issue = "+issue+"<br>answered = "+answered+"<br>req_name = "+req_name+"<br>reqFromDT = "+reqFromDt+"<br>reqToDt = "+reqToDt);
		
		if(reqFromDt == null || reqFromDt.trim().length() == 0)
		{
			reqFromDt = "";
		}
		else
		{
			data.setReq_from_date(reqFromDt);
		}
		
		if(reqToDt == null || reqToDt.trim().length() == 0)
		{
			reqToDt = "";
		}
		else
		{
			data.setReq_to_date(reqToDt);
		}
		
		if(answered == null || answered.trim().length() == 0)
		{	 
			answered = "";
		}
		else
		{
			data.setAnswered(answered);
		}
		/*if(!answered.equals("all"))
		{
			data.setAnswered(answered);	
		}
		else if(answered.equals("all"))
		{
			data.setAnswered(answered);	
		}
		*/
		if(req_name == null || req_name.trim().length() == 0)
		{	 
			req_name = "";
		}
		if(!req_name.equals(""))
		{
			data.setReq_nm(req_name);	
		}
		else if(answered.equals(""))
		{
			data.setReq_nm(req_name);
		}
		
			
		
  	//IF mcode, scode, tcode ARE EMPTY	
		if(department == null || department.trim().length() == 0)
	{	 
			 department = "";
	}
		if(subject == null || subject.trim().length() == 0)
	{	 
	  		 subject = "";
	}
		if(issue == null || issue.trim().length() == 0)
	{	 
	  		 issue = "";
	}
		
		
		//IF ISSUE NOT EMPTY
		else if(issue != null || issue.trim().length() != 0) //has one else
		{
	 		String t1 = issue; //take issue requested value
	 		String t2 = subject; //take subject requested value
	 		
	 		if(department.equals("SAL") && !subject.equals("all") && !issue.equals("all"))
	 		{
	 			String[] z = t1.split("-");
	     		mcode = z[0];
	     		scode = z[1];
	     		tcode = z[2];
	     		
	     		data.setMcode(mcode);										//CATEGORY CODE
	        	data.setScode(Integer.parseInt(scode));						//SUB CATAGORY
	        	data.setTcode(Integer.parseInt(tcode));						//SUB SUB CATEGORY
	 		}
	 		if(department.equals("MKT") && !subject.equals("all") && !issue.equals("all"))
	 		{
	 			String[] z = t1.split("-");
	     		mcode = z[0];
	     		scode = z[1];
	     		tcode = z[2];
	     		
	     		data.setMcode(mcode);										//CATEGORY CODE
	        	data.setScode(Integer.parseInt(scode));						//SUB CATAGORY
	        	data.setTcode(Integer.parseInt(tcode));						//SUB SUB CATEGORY
	 		}
	 		if(department.equals("CCR") && !subject.equals("all") && !issue.equals("all"))
	 		{
	 			String[] z = t1.split("-");
	     		mcode = z[0];
	     		scode = z[1];
	     		tcode = z[2];
	     		
	     		data.setMcode(mcode);										//CATEGORY CODE
	        	data.setScode(Integer.parseInt(scode));						//SUB CATAGORY
	        	data.setTcode(Integer.parseInt(tcode));						//SUB SUB CATEGORY
	 		}
	 		if(department.equals("TRA") && !subject.equals("all") && !issue.equals("all"))
	 		{
	 			String[] z = t1.split("-");
	     		mcode = z[0];
	     		scode = z[1];
	     		tcode = z[2];
	     		
	     		data.setMcode(mcode);										//CATEGORY CODE
	        	data.setScode(Integer.parseInt(scode));						//SUB CATAGORY
	        	data.setTcode(Integer.parseInt(tcode));						//SUB SUB CATEGORY
	 		}
	 	
	 		
  	 	if(department.equals("SAL") && !subject.equals("all") && issue.equals("all"))//has one else
  	 	{
  	 		String[] z = t2.split("-");
  	 		mcode = z[0];
  	 		scode = z[1];
  	 		tcode = issue;
  	 		
  	 		data.setMcode(mcode);										//CATEGORY CODE
  	    	data.setScode(Integer.parseInt(scode));						//SUB CATAGORY
  	 	}
	  	if(department.equals("MKT") && !subject.equals("all") && issue.equals("all"))
	  	{
	  		String[] z = t2.split("-");
	  		mcode = z[0];
	  		scode = z[1];
	  		tcode = issue;
	  		
  	 		data.setMcode(mcode);										//CATEGORY CODE
  	    	data.setScode(Integer.parseInt(scode));						//SUB CATAGORY
	  	}
	  	if(department.equals("DRD") && !subject.equals("all") && issue.equals("all"))
	  	{
	  		String[] z = t2.split("-");
	  		mcode = z[0];
	  		scode = z[1];
	  		tcode = issue;
	  		
  	 		data.setMcode(mcode);										//CATEGORY CODE
  	    	data.setScode(Integer.parseInt(scode));						//SUB CATAGORY
	  	}
	  	if(department.equals("CCR") && !subject.equals("all") && issue.equals("all"))
	  	{
	  		String[] z = t2.split("-");
	  		mcode = z[0];
	  		scode = z[1];
	  		tcode = issue;
	  		
  	 		data.setMcode(mcode);										//CATEGORY CODE
  	    	data.setScode(Integer.parseInt(scode));						//SUB CATAGORY
	  	}
	  	if(department.equals("TRA") && !subject.equals("all") && issue.equals("all"))
	  	{
	  		String[] z = t2.split("-");
	  		mcode = z[0];
	  		scode = z[1];
	  		tcode = issue;
	  		
  	 		data.setMcode(mcode);										//CATEGORY CODE
  	    	data.setScode(Integer.parseInt(scode));						//SUB CATAGORY
	  	}
		}
		
		
		if(department.equals("SAL") && subject.equals("all") && issue.equals("all"))
		{
			mcode = department;
			scode = "all";
			tcode = "all";
			
			data.setMcode(mcode);										//CATEGORY CODE
		}
		if(department.equals("MKT") && subject.equals("all") && issue.equals("all"))
		{
			mcode = department;
			scode = "all";
			tcode = "all";
			
			data.setMcode(mcode);										//CATEGORY CODE

		}
		if(department.equals("DRD") && subject.equals("all") && issue.equals("all"))
		{
			mcode = department;
			scode = "all";
			tcode = "all";
			
			data.setMcode(mcode);										//CATEGORY CODE

		}
		if(department.equals("CCR") && subject.equals("all") && issue.equals("all"))
		{
			mcode = department;
			scode = "all";
			tcode = "all";
			
			data.setMcode(mcode);										//CATEGORY CODE

		}
		if(department.equals("TRA") && subject.equals("all") && issue.equals("all"))
		{
			mcode = department;
			scode = "all";
			tcode = "all";
			
			data.setMcode(mcode);										//CATEGORY CODE

		}
		if(department.equals("SAL") && subject.equals("all") && issue.equals("all"))
		{
			mcode = department;
			scode = "all";
			tcode = "all";
			
			data.setMcode(mcode);										//CATEGORY CODE

		}
		if(department.equals("MKT") && subject.equals("all") && issue.equals("all"))
		{
			mcode = department;
			scode = "all";
			tcode = "all";
			
			data.setMcode(mcode);										//CATEGORY CODE
		}
		if(department.equals("CCR") && subject.equals("all") && issue.equals("all"))
		{
			mcode = department;
			scode = "all";
			tcode = "all";
			
			data.setMcode(mcode);										//CATEGORY CODE
		}
		if(department.equals("TRA") && subject.equals("all") && issue.equals("all"))
		{
			mcode = department;
			scode = "all";
			tcode = "all";
			
			data.setMcode(mcode);										//CATEGORY CODE
		}
		if(department.equals(""))
		{
			mcode = "all";
			scode = "all";
			tcode = "all";
			
		}
		
///////
//END//
///////
		
		//Department Subject Issue VALUES
		servletRequest.setAttribute("department", department);
		servletRequest.setAttribute("subject", subject);
		servletRequest.setAttribute("issue", issue);
		
		servletRequest.setAttribute("YN", yesno);
		servletRequest.setAttribute("reqNm", req_name);
		servletRequest.setAttribute("fromDT", reqFromDt);
		servletRequest.setAttribute("toDT", reqFromDt);
		
		//ALREADY SETED
		servletRequest.setAttribute("departmentFill", dep);
		servletRequest.setAttribute("subjectFill", subj);
		servletRequest.setAttribute("issueFill", iss);
		servletRequest.setAttribute("yesno", yesno);
		//servletRequest.setAttribute("pgTot", pgTot);
		servletRequest.setAttribute("currPage", currPage);
		
    	try{
    		dbAction.connectDB();
    		//dbAction.tableDesc();
    		//dbAction.deleteSub();
    		//response.write(dbAction.listDepCCR());
    		
    		//Database Methods Settings to JSP 
    		//response.write(dbAction.listDepTraining());
    		
    		
    		//List of the Departments
    		 servletRequest.setAttribute("listDepartment", dbAction.listDepartment());
    		 servletRequest.setAttribute("departmentCondition", dbAction.getContactUsCategory());
    		 
    		//List of the Sub Departments
    		 servletRequest.setAttribute("listDepSales", dbAction.listDepSales());
    		 servletRequest.setAttribute("listDepMarketing", dbAction.listDepMarketing());
    		 servletRequest.setAttribute("listDepCCR", dbAction.listDepCCR());
    		 servletRequest.setAttribute("listDepTraining", dbAction.listDepTraining());//NEW
    		 
    		 servletRequest.setAttribute("listDepDRD", dbAction.listDepDRD());
    		 
    		 //List of the Issues
    		 servletRequest.setAttribute("listDepSalesField", dbAction.listDepSalesField());
    		 servletRequest.setAttribute("listDepSalesOrder", dbAction.listDepSalesOrder());
    		 servletRequest.setAttribute("listDepSalesDirectSal", dbAction.listDepSalesDirectSal());
    		 servletRequest.setAttribute("listDepMarketingProd", dbAction.listDepMarketingProd());
    		 servletRequest.setAttribute("listDepMarketingComm", dbAction.listDepMarketingComm());
    		 servletRequest.setAttribute("listDepCallCenterSis", dbAction.listDepCallCenterSis());
    		 servletRequest.setAttribute("listDepTrainingV", dbAction.listDepTrainingV());//NEW
    		 servletRequest.setAttribute("listDepTrainingP", dbAction.listDepTrainingP());//NEW
    		// servletRequest.setAttribute("listDepCallCenterProc", dbAction.listDepCallCenterProc());
    		 
    		 servletRequest.setAttribute("dbase", dbAction.listAll());
    		 
    		 //List of Master Table
    		 servletRequest.setAttribute("listMaster", dbAction.listContactUs());
    		 
    		 //List all the Enquiry
    		 
    		 
    		 //response.write("*"+department+" *"+answered+"<br>");
    		int p1 = dbAction.p1(pg,strDealerCode);
    		//response.write("tot p1 => "+p1);
    		servletRequest.setAttribute("allreg", p1);
    		int p2 = 0;
     		
    		if(department.length() == 0)
    		{
    			p2 = 0;
    			
    			servletRequest.setAttribute("allreg2", p2);
    			
    			//servletRequest.setAttribute("allreg2", p2);
    		}
    		else
    		{
    			p2 = dbAction.p2(data, pg, strDealerCode);
    			servletRequest.setAttribute("allreg2", p2);	
    		}
    		//response.write("reqName - "+reqName+"<br>pg - "+pg+"<br> p2 - "+p2);
     		//response.write("P1 = "+p1+"<br>P2 = "+p2+"<br>");
    		 if(department.equals("all") && answered.equals("all") && req_name.equals("") && reqFromDt.equals(""))
    		 {
    			 servletRequest.setAttribute("enquiryListAll", dbAction.enquiryListAll(pg,p1,strDealerCode));
    			 servletRequest.setAttribute("pagination", dbAction.paginationListDealerAll(pg, strDealerCode));
    			 servletRequest.setAttribute("paginationTot", dbAction.paginationTotAll(pg));
    			 
    		 }
    		 else
    		 {
    			 servletRequest.setAttribute("enquiryListAll", dbAction.enquiryList(data, pg, p2,strDealerCode));
    			 servletRequest.setAttribute("pagination", dbAction.paginationListDealer(data, pg, strDealerCode));
    			 servletRequest.setAttribute("paginationTot", dbAction.paginationTot(data, pg));
    			
    		 }
    		
    		 
    		 
    		     		
    	}catch (Exception e)
    	{
    		//response.write("no database connection");
    	}
    	finally
    	{
    		// disconnect from DB
			dbAction.disconnectDB();
    	}
    	
    	String strDealerName = DpUtil.getDealerName(request, strDealerCode);

    	request.getServletRequest().setAttribute("webrs", CommonProperties.getWebResource(request));
    	
    	servletRequest.setAttribute("label", new CommonLocalization(request.getLocale()));
    	servletRequest.setAttribute("dealerName", strDealerName);
    	servletRequest.setAttribute("c_page", pg);
    	 	
    	
    	
    	
    	servletRequest.setAttribute("REQUEST_URL", "/irj/servlet/prt/portal/prtroot/com.hm.br.dp.community.contactus.ContactUsRequest");
        servletRequest.setAttribute("ENQUIRY_URL", "/irj/servlet/prt/portal/prtroot/com.hm.br.dp.community.contactus.ContactUsContent");
        
        // contact us search condition..
        servletRequest.setAttribute("CONDITION_SUB_URL"		,"/irj/servlet/prt/portal/prteventname/getContactUsConditionSub/prtroot/com.hm.br.common.ConditionInfo");
        servletRequest.setAttribute("CONDITION_ISSUE_URL"	,"/irj/servlet/prt/portal/prteventname/getContactUsConditionIssue/prtroot/com.hm.br.common.ConditionInfo");
       //URL FUNCTIONAL IS JUST LIKE THIS -> http://testportal.hyundai-brasil.com:50000/irj/servlet/prt/portal/prtroot/com.hm.br.dp.community.contactus.ContactUsContent
    	 try{
//    		 IResource jspResource = request.getResource(IResource.JSP, "jsp/contactUsMain.jsp");
//				response.include(request, jspResource);
				
	    	IResource navTreeSource =
	    	request.getResource(IResource.JSP,"jsp/contactUsMain.jsp");
	    	response.include(request,navTreeSource);
    	 }
    	 catch (Exception e) {
				response.write("<br>Contact US have a fail... : "+e.getMessage());
			}
    	 
    	 	
    	//output mcode, scode, tcode DATA
 		//response.write(mcode+"<br>"+scode+"<br>"+tcode+"<br>");
 		//output answered
 		//response.write(answered);
    }
    
    
   
    
}