<%--
	Module: contactUsWrite.jsp
	Date  : 31/08/2012
	Author: Rafael Garcia Lopes Vaz Inocenti
	Description: Contact-Us first page 
	***************************************************************
	Modification Log
	Date:
	Author:
	Modified:
	***************************************************************
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="com.sapportals.portal.prt.component.*" %>
<%@ page import="com.hm.br.common.*" %>
<%@ page import="com.hm.br.common.util.CommonProperties" %>

<%@ page import="java.util.*" %>

<%@ page import="com.hm.br.common.util.StringUtil" %>
<%@ page import="com.hm.br.common.util.DateUtil" %>

<!-- Dealer Portal Source -->
<jsp:useBean id="webrs" 					scope="request" class="java.lang.String" />

<!-- Database parameters -->
<jsp:useBean id="listDepartment" 			scope="request" class="java.lang.String" />
<jsp:useBean id="listDepSales"				scope="request" class="java.lang.String" />
<jsp:useBean id="listDepMarketing"			scope="request" class="java.lang.String" />
<jsp:useBean id="listDepDRD"				scope="request" class="java.lang.String" />
<jsp:useBean id="listDepCCR"				scope="request" class="java.lang.String" />
<jsp:useBean id="listDepTraining"			scope="request" class="java.lang.String" />
<jsp:useBean id="listDepSalesField"			scope="request" class="java.lang.String" />
<jsp:useBean id="listDepSalesOrder"			scope="request" class="java.lang.String" />
<jsp:useBean id="listDepSalesDirectSal"		scope="request" class="java.lang.String" />
<jsp:useBean id="listDepMarketingProd"		scope="request" class="java.lang.String" />
<jsp:useBean id="listDepMarketingComm"		scope="request" class="java.lang.String" />

<jsp:useBean id="listDepCallCenterSis"		scope="request" class="java.lang.String" />
<jsp:useBean id="listDepTrainingV"			scope="request" class="java.lang.String" />
<jsp:useBean id="listDepTrainingP"			scope="request" class="java.lang.String" />

<jsp:useBean id="listDepartmentByGroup"		scope="request" class="java.lang.String" />

<!-- To consult ContactUs all data from Master Table -->
<jsp:useBean id="listMaster"				scope="request" class="java.lang.String" />

<!-- To Enquiry list table -->
<jsp:useBean id="enquiryListAll"			scope="request" class="java.lang.String" />
<jsp:useBean id="pagination"				scope="request" class="java.lang.String" />
<jsp:useBean id="paginationTot"				scope="request" class="java.lang.String" />


<jsp:useBean id="dealerName"				scope="request" class="java.lang.String" />

<jsp:useBean id="dbase"						scope="request" class="java.lang.String" />

<!-- Variable who make difference between Pt-Br and En -->
<jsp:useBean id="label" 					scope="request" class="com.hm.br.common.util.CommonLocalization" />

<jsp:useBean id="department"				scope="request" class="java.lang.String" />
<jsp:useBean id="subject"					scope="request" class="java.lang.String" />
<jsp:useBean id="issue"						scope="request" class="java.lang.String" />

<jsp:useBean id="YN"						scope="request" class="java.lang.String" />
<jsp:useBean id="reqNm"						scope="request" class="java.lang.String" />
<jsp:useBean id="fromDT"					scope="request" class="java.lang.String" />
<jsp:useBean id="toDT"						scope="request" class="java.lang.String" />
	
<jsp:useBean id="departmentFill"			scope="request" class="java.lang.String" />
<jsp:useBean id="subjectFill"				scope="request" class="java.lang.String" />
<jsp:useBean id="issueFill"					scope="request" class="java.lang.String" />
<jsp:useBean id="currPage"					scope="request" class="java.lang.String" />
<jsp:useBean id="yesno"						scope="request" class="java.lang.String" />
<jsp:useBean id="allreg"					scope="request" class="java.lang.Integer" />
<jsp:useBean id="allreg2"					scope="request" class="java.lang.Integer" />

<jsp:useBean id="deptGroup"					scope="request" class="java.lang.String" />

<jsp:useBean id="nm"						scope="request" class="java.lang.String" />
<jsp:useBean id="from"						scope="request" class="java.lang.String" />
<jsp:useBean id="to"						scope="request" class="java.lang.String" />




<jsp:useBean id="REQUEST_URL"				scope="request" class="java.lang.String" />

<!-- Search the Values in DB by submit form -->
<jsp:useBean id="ENQUIRY_URL"				scope="request" class="java.lang.String" />

<link rel="stylesheet" type="text/css" href="<%=webrs%>/css/dp_main.css"  />
<link rel="stylesheet" type="text/css" href="<%=webrs%>/css/calendar.css"  />
<script type="text/javascript" src="<%=webrs%>/scripts/jquery-1.3.2.min.js"></script>

<script type="text/javascript" src="<%=webrs%>/scripts/BPNR-ProgressBar.1.1.js"></script>
<script type="text/javascript" src="<%=webrs%>/scripts/hmb_calendar_0.4.js"></script>

<script type="text/javascript">
$(document).ready( function() {


		$("#requestBtn").click(goRequest);
		$("#enquiryBtn").click(goEnquiry);
		$("#pg").change(enquiryByChange);

	valor = $("#department option:selected").val();
	dep = "<%=departmentFill%>";
	//alert(valor);

	if(dep != "")
	{
		
		
		val = "<%=department%>";
		//alert(val);
			
		if(val == "SAL"){
			$("#dep_subject").append("<%=listDepSales%>");
			$("option").remove(".MKT");
			$("option").remove(".DRD");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-24");
			
			}
		if(val == "MKT" && val != "SAL" && val != "DRD" && val != "CCR" && val != "TRA")
		{
			$("#dep_subject").append("<%=listDepMarketing%>");
			$("option").remove(".SAL");
			$("option").remove(".DRD");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
			
		}
		if(val == "CCR" && val != "SAL" && val != "DRD" && val != "MKT" && val != "TRA")
		{
			$("#dep_subject").append("<%=listDepCCR%>");
			$("option").remove(".SAL");
			$("option").remove(".DRD");
			$("option").remove(".MKT");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
		}
		if(val == "TRA" && val != "SAL" && val != "DRD" && val != "MKT" && val != "CCR")
		{
			$("#dep_subject").append("<%=listDepTraining%>");
			$("option").remove(".SAL");
			$("option").remove(".DRD");
			$("option").remove(".MKT");
			$("option").remove(".CCR");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
		}
		if(val == "DRD" && val != "MKT" && val != "SAL" && val != "CCR" && val != "TRA")
		{
			$("#dep_subject").append("<%=listDepDRD%>");
			$("option").remove(".MKT");
			$("option").remove(".SAL");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
			
		}
		if(val == "all" && val != "MKT" && val != "SAL" && val != "CCR" && val != "TRA")
		{
			$("option").remove(".DRD");
			$("option").remove(".MKT");
			$("option").remove(".SAL");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
			
		}


	val2 = "<%=subject%>";
		
		if(val2 == "SAL-1"){
			$("#issue").append("<%=listDepSalesField%>");
			$("option").remove(".SAL-2");
			$("option").remove(".SAL-3");
			}
		if(val2 == "SAL-2")
		{
			$("#issue").append("<%=listDepSalesOrder%>");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-3");
		}
		if(val2 == "SAL-3")
		{
			$("#issue").append("<%=listDepSalesDirectSal%>");
			$("option").remove(".SAL-2");
			$("option").remove(".SAL-1");
		}
		if(val2 == "MKT-4")
		{
			$("#issue").append("<%=listDepMarketingProd%>");
			$("option").remove(".MKT-5");
		}
		if(val2 == "MKT-5")
		{
			$("#issue").append("<%=listDepMarketingComm%>");
			$("option").remove(".MKT-4");
		}
		if(val2 == "CCR-12")
		{
			$("#issue").append("<%=listDepCallCenterSis%>");
		}
		if(val2 == "TRA-13")
		{
			$("#issue").append("<%=listDepTrainingV%>");
			$("option").remove(".TRA-14");
		}
		if(val2 == "TRA-14")
		{
			$("#issue").append("<%=listDepTrainingP%>");
			$("option").remove(".TRA-13");
		}
		
		if(val2 == "all")
		{
			//$("option").remove(".DRD");
			$("option").remove(".MKT-1");
			$("option").remove(".MKT-2");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-1");
			$("option").remove(".TRA-1");
			$("option").remove(".TRA-2");
			
		}
			
	}
	else
	{
		val = $("#department option:selected").val();
		//alert(val);
			
		if(val == "SAL"){
			$("#dep_subject").append("<%=listDepSales%>");
			$("option").remove(".MKT");
			$("option").remove(".DRD");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-24");
			
			}
		if(val == "MKT" && val != "SAL" && val != "DRD" && val != "CCR" && val != "TRA")
		{
			$("#dep_subject").append("<%=listDepMarketing%>");
			$("option").remove(".SAL");
			$("option").remove(".DRD");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
			
		}
		if(val == "CCR" && val != "SAL" && val != "DRD" && val != "MKT" && val != "TRA")
		{
			$("#dep_subject").append("<%=listDepCCR%>");
			$("option").remove(".SAL");
			$("option").remove(".DRD");
			$("option").remove(".MKT");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
		}
		if(val == "TRA" && val != "SAL" && val != "DRD" && val != "MKT" && val != "CCR")
		{
			$("#dep_subject").append("<%=listDepTraining%>");
			$("option").remove(".SAL");
			$("option").remove(".DRD");
			$("option").remove(".MKT");
			$("option").remove(".CCR");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
		}
		if(val == "DRD" && val != "MKT" && val != "SAL" && val != "CCR" && val != "TRA")
		{
			$("#dep_subject").append("<%=listDepDRD%>");
			$("option").remove(".MKT");
			$("option").remove(".SAL");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
			
		}
		if(val == "all" && val != "MKT" && val != "SAL" && val != "CCR" && val != "TRA")
		{
			$("option").remove(".DRD");
			$("option").remove(".MKT");
			$("option").remove(".SAL");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
			
		}



	val2 = $("#dep_subject option:selected").val();
		
	if(val2 == "SAL-1"){
		$("#issue").append("<%=listDepSalesField%>");
		$("option").remove(".SAL-2");
		$("option").remove(".SAL-3");
		}
	if(val2 == "SAL-2")
	{
		$("#issue").append("<%=listDepSalesOrder%>");
		$("option").remove(".SAL-1");
		$("option").remove(".SAL-3");
	}
	if(val2 == "SAL-3")
	{
		$("#issue").append("<%=listDepSalesDirectSal%>");
		$("option").remove(".SAL-2");
		$("option").remove(".SAL-1");
	}
	if(val2 == "MKT-4")
	{
		$("#issue").append("<%=listDepMarketingProd%>");
		$("option").remove(".MKT-5");
	}
	if(val2 == "MKT-5")
	{
		$("#issue").append("<%=listDepMarketingComm%>");
		$("option").remove(".MKT-4");
	}
	if(val2 == "CCR-12")
	{
		$("#issue").append("<%=listDepCallCenterSis%>");
	}
	if(val2 == "TRA-13")
	{
		$("#issue").append("<%=listDepTrainingV%>");
		$("option").remove(".TRA-14");
	}
	if(val2 == "TRA-14")
	{
		$("#issue").append("<%=listDepTrainingP%>");
		$("option").remove(".TRA-13");
	}
	
	if(val2 == "all")
	{
		//$("option").remove(".DRD");
		$("option").remove(".MKT-1");
		$("option").remove(".MKT-2");
		$("option").remove(".SAL-3");
		$("option").remove(".SAL-1");
		$("option").remove(".SAL-2");
		$("option").remove(".CCR-1");
		$("option").remove(".TRA-1");
		$("option").remove(".TRA-2");
		
	}
	}

	$('#department').change(function() {
		val = $("#department option:selected").val();
		
		if(val == "SAL" && val != "MKT" && val != "DRD" && val != "CCR" && val != "TRA"){
			$("#dep_subject").append("<%=listDepSales%>");
			$("option").remove(".MKT");
			$("option").remove(".DRD");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-24");
			}
		if(val == "MKT" && val != "SAL" && val != "DRD" && val != "CCR" && val != "TRA")
		{
			$("#dep_subject").append("<%=listDepMarketing%>");
			$("option").remove(".SAL");
			$("option").remove(".DRD");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
			
		}
		if(val == "CCR" && val != "SAL" && val != "DRD" && val != "MKT" && val != "TRA")
		{
			$("#dep_subject").append("<%=listDepCCR%>");
			$("option").remove(".SAL");
			$("option").remove(".DRD");
			$("option").remove(".MKT");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
		}
		if(val == "TRA" && val != "SAL" && val != "DRD" && val != "MKT" && val != "CCR")
		{
			$("#dep_subject").append("<%=listDepTraining%>");
			$("option").remove(".SAL");
			$("option").remove(".DRD");
			$("option").remove(".MKT");
			$("option").remove(".CCR");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
		}
		if(val == "DRD" && val != "MKT" && val != "SAL" && val != "CCR" && val != "TRA")
		{
			$("#dep_subject").append("<%=listDepDRD%>");
			$("option").remove(".MKT");
			$("option").remove(".SAL");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
			
		}
		if(val == "all" && val != "MKT" && val != "SAL" && val != "CCR" && val != "TRA")
		{
			$("option").remove(".DRD");
			$("option").remove(".MKT");
			$("option").remove(".SAL");
			$("option").remove(".CCR");
			$("option").remove(".TRA");
			$("option").remove(".MKT-4");
			$("option").remove(".MKT-5");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-12");
			$("option").remove(".TRA-13");
			$("option").remove(".TRA-14");
			
		}


	}); 

	$('#dep_subject').change(function() { 
		val2 = $("#dep_subject option:selected").val();
		
		if(val2 == "SAL-1"){
			$("#issue").append("<%=listDepSalesField%>");
			$("option").remove(".SAL-2");
			$("option").remove(".SAL-3");
			}
		if(val2 == "SAL-2")
		{
			$("#issue").append("<%=listDepSalesOrder%>");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-3");
		}
		if(val2 == "SAL-3")
		{
			$("#issue").append("<%=listDepSalesDirectSal%>");
			$("option").remove(".SAL-2");
			$("option").remove(".SAL-1");
		}
		if(val2 == "MKT-4")
		{
			$("#issue").append("<%=listDepMarketingProd%>");
			$("option").remove(".MKT-5");
		}
		if(val2 == "MKT-5")
		{
			$("#issue").append("<%=listDepMarketingComm%>");
			$("option").remove(".MKT-4");
		}
		if(val2 == "CCR-12")
		{
			$("#issue").append("<%=listDepCallCenterSis%>");
		}
		if(val2 == "TRA-13")
		{
			$("#issue").append("<%=listDepTrainingV%>");
			$("option").remove(".TRA-14");
		}
		if(val2 == "TRA-14")
		{
			$("#issue").append("<%=listDepTrainingP%>");
			$("option").remove(".TRA-13");
		}
		
		if(val2 == "all")
		{
			//$("option").remove(".DRD");
			$("option").remove(".MKT-1");
			$("option").remove(".MKT-2");
			$("option").remove(".SAL-3");
			$("option").remove(".SAL-1");
			$("option").remove(".SAL-2");
			$("option").remove(".CCR-1");
			$("option").remove(".TRA-1");
			$("option").remove(".TRA-2");
			
		}

		});

	dep = "<%=departmentFill%>";
	txtdepartment = new Array($("#department option").text());
	subj = "<%=subjectFill%>";
	txtsubject = new Array($("#department option").text());
	iss = "<%=issueFill%>";
	txtissue = new Array($("#department option").text());
	yesno = "<%=yesno%>";
	txtyesno = new Array($("#answered option").text());

	pagination = "<%=pagination%>";
	if(pagination != null)
	{
		currPage = "<%=currPage%>";
		txtpage = new Array($("#pg option").text());
	}
	
	
	if(dep != null)
	{
		$("#department option[text="+dep+"]").attr("selected", true);
	}
	if(subj != null)
	{
		$("#dep_subject option[text="+subj+"]").attr("selected", true);
	}
	if(iss != null)
	{
		$("#issue option[text="+iss+"]").attr("selected", true);
	}
	if(yesno != null)
	{
		$("#answered option[text="+yesno+"]").attr("selected", true);
	}
	if(pagination != null)
	{
		if(currPage != null)
		{
			$("#pg option[text="+currPage+"]").attr("selected", true);
		}
	}
	 
});


function goRequest(){
	$(".loading_clock").show();
	$("#searchForm").attr("action","<%=REQUEST_URL%>").submit();
}
function goEnquiry(){
	$(".loading_clock").show();	

	var a=document.getElementById("department").selectedIndex;
	var b=document.getElementById("department").options;
	
	var c=document.getElementById("dep_subject").selectedIndex;
	var d=document.getElementById("dep_subject").options;

	var e=document.getElementById("issue").selectedIndex;
	var f=document.getElementById("issue").options;

	var g=document.getElementById("answered").selectedIndex;
	var h=document.getElementById("answered").options;

	

	document.getElementById("dep").value = b[a].text;
	document.getElementById("subj").value = d[c].text;
	document.getElementById("iss").value = f[e].text;
	
	document.getElementById("yesno").value = h[g].text;

	
	$("#searchForm").attr("action", "<%=ENQUIRY_URL%>").submit();
}

function enquiryByChange(){
var pagination = "<%=pagination%>";
	
	if(pagination != "")
	{
	var i=document.getElementById("pg").selectedIndex;
	var j=document.getElementById("pg").options;
	}
	if(pagination != "")
	{
	document.getElementById("currPage").value = j[i].text;
	} 
	$("#searchForm").attr("action", "<%=ENQUIRY_URL%>").submit();
}
</script>
<%
int y = 0;
if(!pagination.equals(""))
{
	
	int x = 0;
	
	if(allreg2 != 0){
		x = (int) Math.ceil((double)allreg2/10);
	}
	else
	{
		x = (int) Math.ceil((double)allreg/10);
	}
	//response.write(c+"<br>");
	//response.write(x+"<br>");
	for(int count=1;count<=x; count++)
	{
		y = count;
	}
}

int day = Integer.parseInt(new java.text.SimpleDateFormat("dd").format(new java.util.Date()));
int year = Integer.parseInt(new java.text.SimpleDateFormat("yyyy").format(new java.util.Date()));
int month = Integer.parseInt(new java.text.SimpleDateFormat("MM").format(new java.util.Date()));
%>
<!-- Title -->
<h1 class="hmb_board_tt"><p><%=label.CONTACT_US%> - List</p></h1>
<!-- /Title -->


<div class="hmb_board_write">
<table align="center" width="100%">
<form id="searchForm" name="searchForm" method="post">
<input type="hidden" name="dep" id="dep" value=""/>
<input type="hidden" name="subj" id="subj" value=""/>
<input type="hidden" name="iss" id="iss" value=""/>
<input type="hidden" name="yesno" id="yesno" value=""/>
<input type="hidden" name="currPage" id="currPage" value=""/>

<tbody>
<thead>
<th><%=label.C_DEPARTMENT%></th>
<td>
<select name="department" id="department" style="width:220">

<option value="all" selected="selected">--<%=label.ALL%>--</option>
<%=listDepartment%>

</select>
</td>
<th><%=label.C_ANSWER%></th>
<td>
<select name="answered" id="answered" style="width:220">
<option value="all" selected="selected">--<%=label.ALL%>--</option>
<option value="Y"><%=label.C_Y%></option>
<option value="N"><%=label.C_N%></option>
</select>
</td>
</tr>
<th><%=label.C_SUBJECT%></th>
<td>
<select name="dep_subject" id="dep_subject" style="width:220">

<option value="all" selected="selected">--<%=label.ALL%>--</option>

</select>
</td>
<th><%=label.C_REQ_NAME%></th>
<%if(nm.equals("") || nm.equals("null") ){%>
<td><input type="text" name="req_nm" value="" style="width:220"></td>
<%}else{%>
<td><input type="text" name="req_nm" value="<%=nm%>" style="width:220"></td>
<%}%>
</tr>
<th><%=label.C_ISSUE%></th>
<td>
<select name="issue" id="issue" style="width:220">
<option value="all" selected="selected">--<%=label.ALL%>--</option>

</select>
</td>
<th><%=label.C_REQ_DATE%></th>
<td colspan="3">
            	<%=label.C_FROM%> 
            	<%if(!from.equals("") && !from.equals("null")){%>
            	<input type="text" id="rt_from_date" name="rt_from_date" style="width:80px" readOnly value="<%=from%>">
            	<%}else{%>
            	<input type="text" id="rt_from_date" name="rt_from_date" style="width:80px" readOnly value="01/<%=month%>/<%=year%>">
            	<%}%>
	            	 <img onclick="javascript:Calendar('rt_from_date',event)" src='<%=webrs%>/images/dp/comm_icon_cal.gif' align='absmiddle' style='cursor:pointer;'>
	            	 <img onclick="javascript:document.getElementById('rt_from_date').value='';" src="<%=webrs%>/images/dp/comm_icon_del.gif" width="16" height="16" align="absmiddle"  style='cursor:pointer;'>
            	<%=label.C_TO%>
            	<%if(!to.equals("") && !to.equals("null")){%> 
            	<input type="text" id="rt_to_date" name="rt_to_date" style="width:80px" readOnly value="<%=to%>">
            	<%}else{%>
            	<input type="text" id="rt_to_date" name="rt_to_date" style="width:80px" readOnly value="<%=day%>/<%=month%>/<%=year%>">
            	<%}%>
	               <img onclick="javascript:Calendar('rt_to_date',event)" src='<%=webrs%>/images/dp/comm_icon_cal.gif' width='16' height='16' align='absmiddle' style='cursor:pointer;'>
	               <img onclick="javascript:document.getElementById('rt_to_date').value='';" src="<%=webrs%>/images/dp/comm_icon_del.gif" width="16" height="16" align="absmiddle"  style='cursor:pointer;'>
</td>
</thead>
</tbody>      

</table>
</div>

<!-- Btn -->
<div class="hmb_board_search">
	<table align="center">
		<tr>
			<td class="tr">
				<button type="button" class="btn" id="enquiryBtn"> <%=label.ENQUIRY%></button>
				<button type="button" class="btn" id="requestBtn"> <%=label.REQUEST%></button>
			</td>
		</tr>
	</table>
</div>
<!-- /Btn -->


<div class="hmb_board_list" id="contentsDiv" style="overflow-y:auto;">
	<table align="center" width="100%">
        
        <tbody>
       
						
			<thead>
			<th><%=label.C_DEPARTMENT%></th>
			<th><%=label.C_SUBJECT%></th>
			<th><%=label.C_ISSUE%></th>
			<th><%=label.C_REQ_NAME%></th>
			<th><%=label.C_REQ_DATE%></th>
			<th><%=label.C_FILE%></th>
			<th><%=label.C_ANSWERED%></th>
			</thead>
			
			</tr>
			<%if(enquiryListAll == null ||enquiryListAll.trim().length() == 0){%>
			<td colspan="7" class="msg">Enter all or part of enquiry condition in the top area and click the Enquiry button.</td></tr>
			<%}else{%>
			<%=enquiryListAll%>
			<%}%>
			
		</tbody>
	</table>
</div>
<div align="right" style="padding-right: 30px;">
<%if(!pagination.equals("")){%>
<select name='pg' id='pg' style='width:50'>
<%=pagination%>
</select> of <%=y%> 
<%if(allreg2 != 0){%>
[Total:<%=allreg2%>]
<%}else{%>
[Total:<%=allreg%>]
<%}%>
<%}%>
</div>
<%=paginationTot%>
</form>
</div>
<!-- loading image -->
<div class="loading_clock" style="display:none;"></div>