/*
Module:	bbs - board(DP)
Date: 	
Author:	
---------------------------------------------------------------
Modification Log
Date:
Author:
Modified:
---------------------------------------------------------------
*/

package com.hm.br.dp.community.contactus.board;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.hm.br.common.HKMPortalComponent;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.component.IPortalComponentResponse;

public class ContactUsDown extends HKMPortalComponent
{
	/**
	* PCBoardFileDown.java
	*
	* board 게시물 상세 페이지의 파일다운로드를 한다. 
	*
	* @author	Rafael Vaz Inocenti
	* @since	jdk 1.6
	*/
	public static final String DEFAULT_CHARSET 	= "UTF-8";	//MS949
	public static final String KOR_CHARSET 		= "KSC5601"; //EUC-KR
	public static final String ENG_CHARSET 		= "ISO-8859-1";	
	public void doContent(IPortalComponentRequest request, IPortalComponentResponse response)
	{
		//PCBoardWriteDAO dao = new PCBoardWriteDAO(request, response);
		ContactUsDown down = new ContactUsDown();
		
		String fileName = request.getParameter("file_down");
		
		String strFileOriName	= fileName;
		String strFileChnName	= fileName;
		String strFilePath     	= "/sapmnt/" + System.getProperty("SAPSYSTEMNAME") + "/global/";
		///sapmnt/PDB/global/uploadtest.txt
		File downFile = new File(strFilePath + strFileChnName);
		
		try{	
			down.download(request.getServletRequest(), request.getServletResponse(true), downFile, strFileOriName, strFileChnName); 
			response.write("<script>window.location = history.back(-1);</script>");//NEW LINE HERE
		}
		catch(Exception e){
			e.printStackTrace();
			response.write("<div>Unpredicted exceptions are occured.<p>Please, inform system administrator of them.");
			response.write("<p>Exception Message : " + e.toString() + "</div>");
		}
	}
	
	
    public void download(HttpServletRequest request, HttpServletResponse response,  File downFile, String strFileOrgName, String strFileChnName) throws IOException 
    {		
		try{
			/* 파일 다운로드 시작*/ 
			request.getHeader("user-agent");
			String mime = "application/msword";

			response.setContentType(mime);

			response.setHeader("Content-Disposition","attachment; filename="+URLEncoder.encode(strFileOrgName, "utf-8")+";");
			response.setHeader("Content-Length", ""+downFile.length() );
			
			transport(new FileInputStream(downFile), response.getOutputStream());
			
		}catch(Exception ee){
			
		}finally{
			response.sendRedirect("<script>self.close();</script>");
			response.flushBuffer();
		}
    }
    
    
    
	/**
	 * Method transport.
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private void transport(InputStream in, OutputStream out) throws IOException {
            
		BufferedInputStream bin = null;
		BufferedOutputStream bos = null;
        
		try {
			bin = new BufferedInputStream( in );
			bos = new BufferedOutputStream( out );
        
			byte[] buf = new byte[2048]; //buffer size 2K.
			int read = 0;
			while ((read = bin.read(buf)) != -1) {
				bos.write(buf,0,read);
			}
		} finally {
			bos.close();
			bin.close();
		}        
	}


	public static String KSC(String s_uni_code_string) throws IOException {
		if(s_uni_code_string == null || s_uni_code_string.equals("")){
			return "";
		}
		String s_ksc_code_string = null;
		//s_ksc_code_string = new String(s_uni_code_string.getBytes("8859_1"),"KSC5601");
		s_ksc_code_string = new String(s_uni_code_string.getBytes("EUC-KR"),"KSC5601");
		return s_ksc_code_string;
	}

	/**
	* 영문을 한글로 Conversion해주는 프로그램.
	* @param english 한글로 바꾸어질 영문 String
	* @return 한글로 바꾸어진 String
	*/
	public static String E2K(String english) {
		String korean = null;
		if (english == null ) {
			return null;
		}
		try { 
			korean = new String(english.getBytes(ENG_CHARSET), KOR_CHARSET);
		} catch (UnsupportedEncodingException e) {
			korean = new String(english);
		}

		return korean;
	}

	/**
	* 한글을 영문으로 Conversion해주는 프로그램.
	* @param korean 영문으로 바꾸어질 한글 String
	* @return 영문로 바꾸어진 String
	*/
	public static String K2E(String korean) {
		String english = null;
        
		if (korean == null ) {
			return null;
		}
        
		try { 
			english = new String(korean.getBytes(KOR_CHARSET), ENG_CHARSET);
		} catch (UnsupportedEncodingException e) {
			english = new String(korean);
		}

		return english;
	}	
	
	
	/**
	 * Method encode.
	 * @param s
	 * @return String
	 */
	public static String encode(String s) {
		try {
			return URLEncoder.encode(s, DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}	
}
