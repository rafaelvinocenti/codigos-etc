package com.hm.br.common.rfc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.resource.ResourceException;
import javax.servlet.http.HttpServletRequest;

import com.hm.br.common.jca.JCAConnector;
import com.hm.br.common.ume.UmeUtil;
import com.hm.br.common.util.CommonProperties;
import com.hm.br.common.util.DateUtil;
import com.hm.br.common.util.LogWriter;
import com.hm.br.common.util.StringUtil;

import com.sapportals.connector.ConnectorException;
import com.sapportals.portal.prt.component.IPortalComponentRequest;
import com.sapportals.portal.prt.event.IPortalRequestEvent;

import java.net.URLDecoder;

/**
 * RFC를 실행 후 값을 가져오기 위한 작업을 단순화하기 위한 클래스.
 * 
 * @author  BPNR
 * @version v1.0
 * @since   JDK1.4.2
 */

public class RfcHelper {

    private JCAConnector jca = null;
    private boolean returnType = false;
    private String returnMessage =  "";
    
    private IPortalComponentRequest _request =  null;
    private String _SYSTEM_LANDSCAPE_R3 =  "";
    private String _epUserId =  "";
    private String _sapUserId =  "";
    private String _sapPassword =  "";

    
    /**
     * default Landscape 이름으로 rfc 호출 시
     * 
     * @param request IPortalComponentRequest
     * @param functionName RFC Function Name
     * @param inputParam HashMap is Input Scalar type input parameter
     */
    public RfcHelper(IPortalComponentRequest request) {
        jca = new JCAConnector();
        _request = request;
    }

    /**
     * 주어진 Landscape 이름으로 rfc 호출 시
     * 
     * @param request IPortalComponentRequest
     * @param functionName RFC Function Name
     * @param inputParam HashMap is Input Scalar type input parameter
     */
    public RfcHelper(IPortalComponentRequest request, String SYSTEM_LANDSCAPE_R3) {
    	if(!SYSTEM_LANDSCAPE_R3.equals("DEFAULT")){
    		jca = new JCAConnector();
    	}
        
        _request = request;
        _SYSTEM_LANDSCAPE_R3 =  SYSTEM_LANDSCAPE_R3;
    }
    
    /**
     * 로그인 전에 rfc 호출 시
     * 
     * @param request IPortalComponentRequest
     * @param functionName RFC Function Name
     * @param inputParam HashMap is Input Scalar type input parameter
     */
    public RfcHelper(
                    String epUserId,
                    String sapUserId,
                    String sapPassword,
                    String SYSTEM_LANDSCAPE_R3) {
        
        jca = new JCAConnector();

        _SYSTEM_LANDSCAPE_R3 =  SYSTEM_LANDSCAPE_R3;
        _epUserId =  epUserId;
        _sapUserId =  sapUserId;
        _sapPassword =  sapPassword;
    }
    
    /**
     * 커넥션 생성
     * 
     * @param request IPortalComponentRequest
     * @param functionName RFC Function Name
     * @param inputParam HashMap is Input Scalar type input parameter
     */
    private void createConnector() {
        
        try {

            if ( "".equals(_SYSTEM_LANDSCAPE_R3) || null == _SYSTEM_LANDSCAPE_R3)
                jca.createConnection(_request);
            else if ( !"".equals(_SYSTEM_LANDSCAPE_R3) && null != _SYSTEM_LANDSCAPE_R3)
                jca.createConnection(_request, _SYSTEM_LANDSCAPE_R3);
            else if ( !"".equals(_epUserId) && !"".equals(_sapUserId))
                jca.createConnection(_epUserId, _sapUserId, _sapPassword, _SYSTEM_LANDSCAPE_R3);
            
        } catch (Exception e) {
            LogWriter.errorT(this, "createConnection Exception = " + e);
        }
    }

    
    /**
     * jca connection close
     */
    private void closeConnector() {
        try {
            jca.closeConnection();
        } catch (Exception e) {
            LogWriter.errorT(this, "closeConnector Exception = " + e);
        }
    }


    /**
     * AllocatedS tock Enquiry 검색<br>
     * AllocatedStock 컴포넌트에서 호출.
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getAllocatedStock(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
        inputMap.put("I_MODEL", StringUtil.checkNull(request.getParameter("vehicle_model"))); // Model
        inputMap.put("I_USAGE", StringUtil.checkNull(request.getParameter("vehicle_usage"))); // Vehicle Usage
        inputMap.put("I_EXTC", StringUtil.checkNull(request.getParameter("exterior_color"))); // Extetior Color
        inputMap.put("I_LOCTN", StringUtil.checkNull(request.getParameter("logistic_status"))); // VMS-Vehicle Location(Logistics Status)
        inputMap.put("I_TMCOD", StringUtil.checkNull(request.getParameter("transmission"))); // Transmission Type
        inputMap.put("I_SO_NO", StringUtil.checkNull(request.getParameter("order_no"))); // Sales Document
        inputMap.put("I_VHVIN", StringUtil.checkNull(request.getParameter("vin"))); // Vehicle Identification Number (Vehicle Identification No.)
        inputMap.put("I_TRANSFER_FLAG", StringUtil.checkNull(request.getParameter("transfer"))); // Vehicle transferred


        String strRfcName = "ZHAU_DP_LIST_ALLOCATED_STK";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
     
    
    
    /**
     * Body Type 조건값을 가져온다.<br>
     * model 조건이 선택된 이후에 호출된다.<br>
     * vehicle availability화면, ConditionInfo 컴포넌트에서 호출.
     * 
     * @param String I_MODEL model code
     * @param String I_CODE flag  Body Type : "BTY", Eng Capacity : "EC", TM Type : "TMT" 
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getBodyTypeCondition(IPortalComponentRequest request, String I_MODEL)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
          
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_CODE" , CommonProperties.TYPE_BODY_CD);
        inputMap.put("I_MODEL", I_MODEL);

        String strRfcName = "ZHBR_SD_DP_DESC_CODE";  // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명

        try {
            //connection 생성
            if(jca.getConnection() == null) createConnector();
            
            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }


	/**
	 * Body Type 조건값을 가져온다.<br>
	 * model 조건이 선택된 이후에 호출된다.<br>
	 * vehicle availability화면, ConditionInfo 컴포넌트에서 호출.
	 * 
	 * @param String I_MODEL model code
	 * @param String I_CODE flag  Body Type : "BTY", Eng Capacity : "EC", TM Type : "TMT" 
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getVfactsRelation(String i_rdaType)
		throws ConnectorException, ResourceException {

		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_ZZRDA_TYPE", i_rdaType);

		String strRfcName = "ZHAU_DP_GET_VFACTS_REL";  // rfc 함수명
		String strListTable = "T_ZZVFACTS";           // 리턴 테이블명

		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
            
			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
			
			for(int i = 0 ; i<returnList.size() ; i++)
			{
				inputMap = (HashMap)returnList.get(i);
				//LogWriter.errorT(this, "value: " + inputMap.get("DOMVALUE_L"));
				//LogWriter.errorT(this, "text : " + inputMap.get("DDTEXT"));
			}
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	
	
    /**
     * Dealer  조회<br>
     * Dealer Stock화면, DealerStock 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getDealerView(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
//        inputMap.put("I_KUNNR", "B05AD00001");
        
        String strRfcName = "ZHBR_SD_DP_VIEW_DEALER";  // rfc 함수명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }


    /**
     * Dealer Stock 조회<br>
     * Dealer Stock화면, DealerStock 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getDealerStock(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        String strOrderNo = StringUtil.checkNull(request.getParameter("orderNo"),"");
        String strVin = StringUtil.checkNull(request.getParameter("vin"),"");
        

		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        
        if(!"".equals(strOrderNo) || !"".equals(strVin)){
			inputMap.put("I_VBELN", strOrderNo);
			inputMap.put("I_ZCVIN",   strVin);
        }else{
        	if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
				inputMap.put("I_ZCCAR", StringUtil.checkNull(request.getParameter("vehicle_model")));
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
				inputMap.put("I_BDTYP", StringUtil.checkNull(request.getParameter("body_type")));
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
				inputMap.put("I_TMCOD", StringUtil.checkNull(request.getParameter("transmission")));
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("exterior_color"),"")))
				inputMap.put("I_ZCEXTC", StringUtil.checkNull(request.getParameter("exterior_color")));
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("order_type"),"")))
				inputMap.put("I_AUART", StringUtil.checkNull(request.getParameter("order_type")));
        }
		
        String strRfcName = "ZHBR_SD_DP_LIST_DEALER_STK";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {
            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }
        return returnList;
    }
    
    
    
    /**
     * DeliveryConfirm 조회<br>
     * DeliveryConfirm화면, DeliveryConfirm 컴포넌트에서 호출.
     * 
     * @param request 		Parameter
     * @param dealerCode 	String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getDeliveryConfirm(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
        
    	if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
			inputMap.put("I_MODEL", StringUtil.checkNull(request.getParameter("vehicle_model")));
			
		if(!"".equals(StringUtil.checkNull(request.getParameter("vin"),"")))
		{
			if(StringUtil.checkNull(request.getParameter("vin"),"").length()>5)
			{
				inputMap.put("I_ZCVIN", StringUtil.checkNull(request.getParameter("vin")));
			}
			else
			{
				inputMap.put("I_SERIAL", StringUtil.checkNull(request.getParameter("vin")));
			}
		}
        String strRfcName = "ZHBR_SD_DP_DELIVERY_CNF";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {
            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }
        return returnList;
    }
    
    /**
     * DeliveryConfirm 처리<br>
     * DeliveryConfirm Save 화면, DeliveryConfirmTrans 컴포넌트에서 호출.
     * 
     * @param request 		Parameter
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getDeliveryConfirmSave(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_ZCVIN", StringUtil.checkNull(request.getParameter("vin")));
		inputMap.put("I_ZDDLST", DateUtil.getChangeDate(request.getParameter("delivery_date"),"/"));
        
        String strRfcName = "ZHBR_SD_DP_PODPR";  // rfc 함수명
        String strListTable = "T_RETURN";              // 리턴 테이블명
        
        try {
            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }
        return returnList;
    }
    
    /**
     * 오더 생성 시 Delivery To 조건값을 가져온다.<br>
     * order craete 화면, OrderCreate 컴포넌트에서 호출.
     * 
     * @param String dealerCode dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getDeliveryToCondition(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);

        String strRfcName = "ZHBR_SD_DP_LIST_SHIP_TO";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();
            
            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    } 

    
    /**
     * 오더 생성 시 Bill To 조건값을 가져온다.<br>
     * order craete 화면, OrderCreate 컴포넌트에서 호출.
     * 
     * @param String dealerCode dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getBillToCondition(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);

        String strRfcName = "ZHBR_SD_DP_LIST_BILL_TO";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();
            
            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    } 
    
    
    /**
     * end customer 조건값을 가져온다.<br>
     * 오더 생성을 비롯해서 end customer정보를 입력해야하는 모든 화면에서 호출된다.<br>
     * CustomerInfo 컴포넌트에서 호출.
     * 
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getEndCustomerCondition(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {

        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
//        inputMap.put("I_TITLE" , "X");
        inputMap.put("I_REGION" , "X");
        inputMap.put("I_OCCUPATION" , "X");
        inputMap.put("I_GENDER" , "X");
        inputMap.put("I_MARITAL" , "X");
        inputMap.put("I_PAYMETHOD" , "X");
        
        String strRfcName = "ZHBR_SD_DP_ENDCUSTOM_VALUE";  // rfc 함수명

        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    
    /**
     * State 값을 가져온다.<br>
     * Profile 정보 수정시  호출된다.<br>
     * CustomerInfo 컴포넌트에서 호출.
     * 
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getStateCondition(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {

        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_REGION" , "X");
        
        String strRfcName = "ZHBR_SD_DP_ENDCUSTOM_VALUE";  // rfc 함수명

        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    
    /**
     * Engine Capacity 조건값을 가져온다.<br>
     * model 조건이 선택된 이후에 호출된다.<br>
     * vehicle availability화면, ConditionInfo 컴포넌트에서 호출.
     * 
     * @param String I_MODEL model code
     * @param String I_CODE flag  Body Type : "BTY", Eng Capacity : "EC", TM Type : "TMT" 
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getEngineCapacityCondition(IPortalComponentRequest request, String I_MODEL)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_CODE" , CommonProperties.TYPE_ENG_CAPA);
        inputMap.put("I_MODEL", I_MODEL);

        String strRfcName = "ZHBR_SD_DP_DESC_CODE";  // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명

        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
    
    /**
     * Exterror Color 조건값을 가져온다.<br>
     * model 조건이 선택된 이후에 호출된다.<br>
     * vehicle availability화면, ConditionInfo 컴포넌트에서 호출.
     * 
     * @param String I_MODEL model code
     * @param String I_BDTYP body type
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getExteriorColorCondition(IPortalComponentRequest request, String I_MODEL, String I_BDTYP)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_MODEL", I_MODEL);
        inputMap.put("I_BDTYP", I_BDTYP);

        String strRfcName = "ZHBR_SD_DP_GET_EXT";     // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명

        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    } 

    
    /**
     * invoice list 검색<br>
     * 조회화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     * final invoice화면, FinalInvoice 컴포넌트에서 호출.
     * 
     * @param String I_MODEL model code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getFinalInvoiceList(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        String strVin = StringUtil.checkNull(request.getParameter("vin"),"");
        String strOrderNo = StringUtil.checkNull(request.getParameter("order_no"),"");
        String strInvoiceNo = StringUtil.checkNull(request.getParameter("iv_no"),"");
        
        // import parameters setting
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);

        if( !"".equals(strVin) || !"".equals(strOrderNo) || !"".equals(strInvoiceNo)){
        	if(!"".equals(StringUtil.checkNull(request.getParameter("vin"),"")))
        	{
        		if(StringUtil.checkNull(request.getParameter("vin"),"").length()>5)
    			{
    				inputMap.put("I_ZCVIN", StringUtil.checkNull(request.getParameter("vin")));
    			}
    			else
    			{
    				inputMap.put("I_SERIAL", StringUtil.checkNull(request.getParameter("vin")));
    			}
        	}
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("order_no"),"")))
    			inputMap.put("I_SO_NO",      StringUtil.checkNull(request.getParameter("order_no")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("iv_no"),"")))
    			inputMap.put("I_BILL_NO",    StringUtil.checkNull(request.getParameter("iv_no")));
        	
   			inputMap.put("I_FDATE",   "20120101");
    		
        }else{
        	if(!"".equals(StringUtil.checkNull(request.getParameter("order_type"),"")))
    			inputMap.put("I_PSTYV",    StringUtil.checkNull(request.getParameter("order_type")));
            
            if(!"".equals(StringUtil.checkNull(request.getParameter("iv_type"),"")))
    			inputMap.put("I_IVTYP",    StringUtil.checkNull(request.getParameter("iv_type")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
    			inputMap.put("I_ZCCAR",      StringUtil.checkNull(request.getParameter("vehicle_model")));
    		
    		if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
    			inputMap.put("I_BDTYP",      StringUtil.checkNull(request.getParameter("body_type")));
    		
    		if(!"".equals(StringUtil.checkNull(request.getParameter("so_date_from"),"")))
    			inputMap.put("I_FDATE_O",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("so_date_from")),"/"));
    		
    		if(!"".equals(StringUtil.checkNull(request.getParameter("so_date_to"),"")))
    			inputMap.put("I_TDATE_O",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("so_date_to")),"/"));
    		
   			inputMap.put("I_FDATE",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("iv_date_from")),"/"));
   			inputMap.put("I_TDATE",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("iv_date_to")),"/"));
    		
        }
        
        
        String strRfcName = "ZHBR_SD_DP_LIST_INVOICE";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }



    
	/**
	 * Customer Contract Details list 검색<br>
	 * 조회화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
	 * Customer Contract Details화면, CustomerContractDetails 컴포넌트에서 호출.
	 * 
	 * @param String I_MODEL model code
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getContractDetail(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		String strMonth = StringUtil.checkNull(request.getParameter("month"));
		String strYear = StringUtil.checkNull(request.getParameter("year"));
		String strDsn = StringUtil.checkNull(request.getParameter("dsn"));
		String strPartner = StringUtil.checkNull(request.getParameter("partner"));
		
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
		inputMap.put("I_ZMONTH", strYear + strMonth);
		
		if(!"".equals(strDsn))
			inputMap.put("I_VHCLE", strDsn);
		
		if(!"".equals(strPartner))
			inputMap.put("I_ZZPARNR", strPartner);
		
		String strRfcName = "ZHAU_DP_EC_CONTRACT_DETAILS";  // rfc 함수명
		String strListTable = "T_2050";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}



	/**
	 * Dealer Enquiry 검색<br>
	 * 조회화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
	 * Dealer Enquiry 화면, PCDealerEnquiry 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @param String dealerCode dealer code
	 * @return  ArrayList object data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getDealerList(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
		
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("dealer_id"),""))){
			String strDealerID = StringUtil.checkNull(request.getParameter("dealer_id"),"");
			if(strDealerID.length() > 5){
				inputMap.put("I_DEALER",      strDealerID.toUpperCase());
			}else{
				inputMap.put("I_DEALER",      CommonProperties.DEALER_PREFIX + strDealerID.toUpperCase());
			}
		}
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("dealer_name"),"")))
			inputMap.put("I_NAME",      StringUtil.checkNull(request.getParameter("dealer_name")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("order_by"),"")))
			inputMap.put("I_ORDERBY",      StringUtil.checkNull(request.getParameter("order_by")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("post_code"),"")))
			inputMap.put("I_REGIOC",      StringUtil.checkNull(request.getParameter("post_code")));
		
		String strRfcName = "ZHBR_SD_DP_LIST_DEALER";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			jca.createConnection(request, CommonProperties.R3_DP);

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
			
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	

	/**
	 * Account Statement list 검색<br>
	 * 조회화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
	 * Account Statement화면, AccountStatement 컴포넌트에서 호출.
	 * 
	 * @param request parameter
 	 * @param String dealerCode 	Dealer Code
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getAccountStatementList(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		String strVin = StringUtil.checkNull(request.getParameter("vin"),"");
		String strOrderNo = StringUtil.checkNull(request.getParameter("order_no"),"");
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        
		if(!"".equals(StringUtil.checkNull(request.getParameter("type"),"")))
			inputMap.put("I_PTYPE",    StringUtil.checkNull(request.getParameter("type")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("so_date_from"),"")))
			inputMap.put("I_IDATE",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("so_date_from")),"/"));
		
		String strRfcName = "ZHAU_DP_LIST_AR_STMT";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}


	/**
	 * Account Statement list PDF 출력<br>
	 * 조회화면에서 Print 버튼 클릭 시 실행된다.<br>
	 * Account Statement화면, AccountStatementPrint 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @param String dealerCode 	Dealer Code
	 * @return  Object return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public Object getAccountStatementPrint(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		HashMap returnMap = new HashMap();
		HashMap inputMap = new HashMap();
        
		String strVin = StringUtil.checkNull(request.getParameter("vin"),"");
		String strOrderNo = StringUtil.checkNull(request.getParameter("order_no"),"");
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_type"),"")))
			inputMap.put("I_PTYPE",    StringUtil.checkNull(request.getParameter("p_type")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_so_date_from"),"")))
			inputMap.put("I_IDATE",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("p_so_date_from")),"/"));
		
		String strRfcName = "ZHAU_DP_VIEW_AR_STMT";  // rfc 함수명
		
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnMap = jca.getRfcDataScalar(jca.executeFunction(strRfcName, inputMap));
            
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap.get("E_PDF");
	}

	/**
	 * Remittance & RCTI 검색<br>
	 * 조회화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
	 * Remittance & RCTI 화면, EFTRemittance 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getRemittanceList(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
        
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("so_date_from"),"")))
			inputMap.put("I_BILL_FDATE",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("so_date_from")),"/"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("so_date_to"),"")))
			inputMap.put("I_BILL_TDATE",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("so_date_to")),"/"));
			
		if(!"".equals(StringUtil.checkNull(request.getParameter("type"),"")))
			inputMap.put("I_LTYP", StringUtil.checkNull(request.getParameter("type")));
			
		String strRfcName = "ZHAU_DP_LIST_REMIT_ADVI";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
  		
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	
	/**
	 * Holdback 검색<br>
	 * 조회화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
	 * Holdback 화면, Holdback 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getHoldbackList(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
        
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
    
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
    
	
		if(!"".equals(StringUtil.checkNull(request.getParameter("so_date_from"),"")))
			inputMap.put("I_SIVDT",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("so_date_from")),"/"));
	
		if(!"".equals(StringUtil.checkNull(request.getParameter("so_date_to"),"")))
			inputMap.put("I_EIVDT",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("so_date_to")),"/"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("dsn"),"")))
			inputMap.put("I_VHCLE", StringUtil.checkNull(request.getParameter("dsn")));
		
		String strRfcName = "ZHAU_DP_LIST_HB_ENQUIRY";  // rfc 함수명
		String strListTable = "ET_LIST";              // 리턴 테이블명
	
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
        
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

			} catch (Exception e) {
				LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	
	/**
	 * Holdback Detail<br>
	 * 조회화면에서 Dsn 클릭 시 실행된다.<br>
	 * Holdback 화면, Holdback 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getHoldbackDetail(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
    
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();

		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);


		if(!"".equals(StringUtil.checkNull(request.getParameter("v_vin"),"")))
			inputMap.put("I_VHVIN", StringUtil.checkNull(request.getParameter("v_vin")));

		if(!"".equals(StringUtil.checkNull(request.getParameter("v_dsn"),"")))
			inputMap.put("I_VHCLE", StringUtil.checkNull(request.getParameter("v_dsn")));

		if(!"".equals(StringUtil.checkNull(request.getParameter("v_hb_doc"),"")))
			inputMap.put("I_HBDOCNO", StringUtil.checkNull(request.getParameter("v_hb_doc")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("v_hbb"),"")))
			inputMap.put("I_HBBUZEI", StringUtil.checkNull(request.getParameter("v_hbb")));
	
		if(!"".equals(StringUtil.checkNull(request.getParameter("v_ap_doc"),"")))
					inputMap.put("I_APDOCNO", StringUtil.checkNull(request.getParameter("v_ap_doc")));
	
		String strHbDate = StringUtil.checkNull(request.getParameter("v_hb_date"),"");
		if(!"".equals(strHbDate))
			inputMap.put("I_HBBUDAT",    DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("v_hb_date")),"/"));
	
		String strApDate = StringUtil.checkNull(request.getParameter("v_ap_date"),"");
		if(!"".equals(strApDate))
			inputMap.put("I_APBUDAT",   DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("v_ap_date")),"/"));
	
	
		String strDraDate = StringUtil.checkNull(request.getParameter("v_dra_date"),"");
		if(!"".equals(strDraDate))
			inputMap.put("I_DRAWDT",    DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("v_dra_date")),"/"));

			
		String strRfcName = "ZHAU_DP_VIEW_HB_ENQUIRY";  // rfc 함수명
		String strListTable = "ET_VIEW";              // 리턴 테이블명

		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
    
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	
	
    /**
     * Invoice 출력<br>
     * 조회화면에서 Print 버튼 클릭 시 실행된다.<br>
     * final invoice화면, FinalInvoice 컴포넌트에서 호출.
     * 
     * @param String invoiceNo invoice No
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public Object getFinalInvoicePrint(String invoiceNo)
        throws ConnectorException, ResourceException {
            
            HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_VBELN", invoiceNo);

        //System.err.println("invoiceNo: " + invoiceNo);

        //String strRfcName = "ZHAU_DP_VIEW_INVOICE";  // rfc 함수명
		String strRfcName = "ZHAU_DP_VIEW_TAXINVOICE";  // rfc 함수명
		
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            LogWriter.errorT(this, "returnMap: start~ ");
            
            // rfc excute
            returnMap = jca.getRfcDataScalar(jca.executeFunction(strRfcName, inputMap));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap.get("E_PDF");
    }



	/**
	 * Remittance & RCTI 출력<br>
	 * 조회화면에서 Print 버튼 클릭 시 실행된다.<br>
	 * Remittance & RCTI 화면, EFTRemittance 컴포넌트에서 호출.
	 * 
	 * @param String invoiceNo invoice No
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public Object getRemittancePrint(IPortalComponentRequest request, String strDealerCode)
		throws ConnectorException, ResourceException {
            
		HashMap returnMap = new HashMap();
		HashMap inputMap = new HashMap();

		//String strDealerCode = this.getLoginUserDealerCode(request);
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + strDealerCode);
		
		inputMap.put("I_PAYDT", DateUtil.getChangeDate(request.getParameter("p_payDate"),"/"));
		inputMap.put("I_LTYP", request.getParameter("p_listType"));
		inputMap.put("I_PAY_REF", request.getParameter("p_payRefNum"));
		inputMap.put("I_RCTI_REF", request.getParameter("p_RCTIRefNum"));

		//System.err.println("invoiceNo: " + invoiceNo);

		String strRfcName = "ZHAU_DP_VIEW_REMIT_ADVI";  // rfc 함수명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			LogWriter.errorT(this, "returnMap: start~ ");
            
			// rfc excute
			returnMap = jca.getRfcDataScalar(jca.executeFunction(strRfcName, inputMap));

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap.get("E_PDF");
	}



    /**
     * Fuel Type 조건값을 가져온다.<br>
     * model 조건이 선택된 이후에 호출된다.<br>
     * vehicle specification화면, VehicleSpecification 컴포넌트에서 호출.
     * 
     * @param String I_MODEL model code
     * @param String I_CODE flag  Body Type : "BTY", Eng Capacity : "EC", TM Type : "TMT" , Fuel : "FT"
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getFuelTypeCondition(IPortalComponentRequest request, String I_MODEL)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_CODE" , CommonProperties.TYPE_FUEL_TYPE);
        inputMap.put("I_MODEL", I_MODEL);

        String strRfcName = "ZHBR_SD_DP_DESC_CODE";  // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명

        try {
            //connection 생성
            if(jca.getConnection() == null) createConnector();
            
            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    /**
     * Grade 조건값을 가져온다.<br>
     * model 조건이 선택된 이후에 호출된다.<br>
     * vehicle availability화면, ConditionInfo 컴포넌트에서 호출.
     * 
     * @param String I_MODEL model code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getGradeCondition(IPortalComponentRequest request, String I_MODEL)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_CODE" , CommonProperties.TYPE_GRADE);
        inputMap.put("I_MODEL", I_MODEL);

        String strRfcName = "ZHBR_SD_DP_DESC_CLASS";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명

        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    } 


    /**
     * Interior Color 조건값을 가져온다.<br>
     * model 조건이 선택된 이후에 호출된다.<br>
     * Dealer Stock화면, DealerStock 컴포넌트에서 호출.
     * 
     * @param String I_MODEL model code
     * @param String I_BDTYP body type
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getInteriorColorCondition(IPortalComponentRequest request, String I_MODEL, String I_BDTYP)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_MODEL", I_MODEL);
        inputMap.put("I_BDTYP", I_BDTYP);
        
        String strRfcName = "ZHAU_DP_GET_INT";     // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명

        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }



    /**
     * Other Dealer Stock 조회화면에서 보여준다.<br>
     * OtherDealerStock 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getLocnCondition(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();

        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
		inputMap.put("I_MATNR", StringUtil.checkNull(request.getParameter("p_fsc")));
		inputMap.put("I_EXTCOL", StringUtil.checkNull(request.getParameter("p_extColorCode")));
		inputMap.put("I_INTCOL", StringUtil.checkNull(request.getParameter("p_intColorCode")));
        
        String strRfcName = "ZHAU_DP_LOCN_LIST";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    /**
     * order enquiry 조회 시 조건값을 가져온다.<br>
     * OrderEnquiry , OtherDealerStock컴포넌트에서 호출.
     * 
     * @param flag String 1 or 2 , 1: On Pro + Alloc, 2: Alloc, 3: Alloc + Dlr
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getLogisticsStatus(IPortalComponentRequest request, String flag)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_FLAG", flag);

        String strRfcName = "ZHBR_SD_DP_LIST_LOCATION"; // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명

        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();
            
            // rfc 실행
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }

    /**
     * model 조건값을 가져온다.<br>
     * VehicleAvailability, OrderEnquiry 컴포넌트에서 호출.
     * 
     * @param old 주문가능 모델 조회와 전체 모델 조회 구분("":Billing 이전, "Y":Billing 이후)
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getModelCondition(IPortalComponentRequest request, String old)
        throws ConnectorException, ResourceException {
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        if(!"".equals(old))	inputMap.put("I_OLD", old);

        String strRfcName = "ZHBR_SD_DP_DESC_MODEL"; // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명

        try {
            //connection 생성 
        	if(jca.getConnection() == null) createConnector();
            
            // rfc 실행
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }

    
    /**
     * Order Status값을 가져온다.<br>
     * OrderEnquiry 컴포넌트에서 호출.
     * 
     * @param IPortalComponentRequest request
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getOrderStatus(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting

        String strRfcName = "ZHBR_SD_DP_ORDER_STATUS"; // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명

        try {
            //connection 생성 
        	if(jca.getConnection() == null) createConnector();
            
            // rfc 실행
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
    /**
     * Order Detail 조회<br>
     * OrderDetail 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getOrderDetail(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_VBELN", StringUtil.checkNull(request.getParameter("p_order_no")));

        String strRfcName = "ZHBR_SD_DP_ORDER_VIEW";  // rfc 함수명
        
        try {
            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    /**
     * Order Enquiry 검색<br>
     * Order Enqiry화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     * Order Enqiry화면, OrderEnqiry 컴포넌트에서 호출.
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getOrderEnquirySearch(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
		inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code


    	if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),""))) 
			inputMap.put("I_MODEL", StringUtil.checkNull(request.getParameter("vehicle_model"))); // Model
		
    	if(!"".equals(StringUtil.checkNull(request.getParameter("order_type"),""))) 
			inputMap.put("I_AUART", StringUtil.checkNull(request.getParameter("order_type"))); // Model
		
    	if(!"".equals(StringUtil.checkNull(request.getParameter("order_age"),""))) 
			inputMap.put("I_AGE", StringUtil.checkNull(request.getParameter("order_age"))); // Model
		
    	if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),""))) 
			inputMap.put("I_ZCBYTE", StringUtil.checkNull(request.getParameter("body_type"))); // Body Type
    	
		if(!"".equals(StringUtil.checkNull(request.getParameter("exterior_color"),"")))
			inputMap.put("I_ZCEXTC", StringUtil.checkNull(request.getParameter("exterior_color"))); // Extetior Color
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("logistic_status"),"")))
			inputMap.put("I_LOCTN", StringUtil.checkNull(request.getParameter("logistic_status"))); // VMS-Vehicle Location(Logistics Status)
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
			inputMap.put("I_TMCOD", StringUtil.checkNull(request.getParameter("transmission"))); // Transmission Type
		
		
		if("C".equals(request.getParameter("date_type"))){
			if(!"".equals(StringUtil.checkNull(request.getParameter("date_from"),"")))
				inputMap.put("I_FBSTDK", DateUtil.getChangeDate(request.getParameter("date_from"),"/")); // Document Date (Date Received/Sent)
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("date_to"),"")))
				inputMap.put("I_TBSTDK", DateUtil.getChangeDate(request.getParameter("date_to"),"/")); // Document Date (Date Received/Sent)
			
		}else{
			if(!"".equals(StringUtil.checkNull(request.getParameter("date_from"),"")))
				inputMap.put("I_FDATE", DateUtil.getChangeDate(request.getParameter("date_from"),"/")); // Document Date (Date Received/Sent)
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("date_to"),"")))
				inputMap.put("I_TDATE", DateUtil.getChangeDate(request.getParameter("date_to"),"/")); // Document Date (Date Received/Sent)
			
		}
		
		
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("vin"),""))) 
			inputMap.put("I_ZCVIN", StringUtil.checkNull(request.getParameter("vin"))); // Model
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("order_status"),""))) 
			inputMap.put("I_ZCORDSTAT", StringUtil.checkNull(request.getParameter("order_status"))); // Order_status
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("post_code"),"")))
//			inputMap.put("I_POSTCODE", StringUtil.checkNull(request.getParameter("post_code"))); // Serial NO ( VIN Last 6 Digit)
        
        // import parameters table setting
        ArrayList inputList = new ArrayList();
        String importTableName = "T_VBELN";
        StringTokenizer tokens_orderno = new StringTokenizer( StringUtil.checkNull(request.getParameter("order_no"),""), "|" );
        
        String[] t_so_no = StringUtil.checkNull(request.getParameter("order_no"),"").split("|");
        //String[] t_so_no = request.getServletRequest().getParameterValues("order_no");
         
        if( !"".equals(StringUtil.checkNull(request.getParameter("order_no"))) ){
            for(int j=0 ; tokens_orderno.hasMoreElements() ; j++){
                String tmpNo = tokens_orderno.nextToken();
                HashMap tmpTableMap = new HashMap();
                
                tmpTableMap.put("VBELN", tmpNo);
                inputList.add(tmpTableMap);
            }
        }        
        String strRfcName = "ZHBR_SD_DP_ORDER_LIST";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            if(inputList.size() == 0){
                returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            }else{
                returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap, importTableName, inputList), strListTable);
            }
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }


    /**
     * OTD Report Summary By Sales Order 검색<br>
     * OTD Report Summary 화면에서 Total 수량 클릭 시 실행된다.<br>
     * OTD Report Summary By Sales Order화면, OrderToDeliveryGrid 컴포넌트에서 호출.
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getOtdEnquiry(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        //inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
       // inputMap.put("I_DPFLAG", "X"); // dp에선 꼭 x를 넣으라 함.
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
        
//      inputMap.put("I_MATNR", ""); // 이건 왜 넣나 몰겟네..화면 조건에는 없음.
//		inputMap.put("I_LGORT", ""); // 이건 왜 넣나 몰겟네..화면 조건에는 없음.
//		inputMap.put("I_SOPTION", ""); // 이건 왜 넣나 몰겟네..화면 조건에는 없음.
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
			inputMap.put("I_ZCCAR", StringUtil.checkNull(request.getParameter("vehicle_model"))); // Model
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
			inputMap.put("I_ZCBYTE", StringUtil.checkNull(request.getParameter("body_type"))); // body type
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("engine_capacity"),"")))
			inputMap.put("I_ZCECAPA", StringUtil.checkNull(request.getParameter("engine_capacity"))); // engine capacity
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
			inputMap.put("I_ZCTRMS", StringUtil.checkNull(request.getParameter("transmission"))); // Transmission Type
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("grade"),"")))
//			inputMap.put("I_ZCGRADE", StringUtil.checkNull(request.getParameter("grade"))); // grade
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("exterior_color"),"")))
			inputMap.put("I_ZCEXTC", StringUtil.checkNull(request.getParameter("exterior_color"))); // Extetior Color
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_usage"),"")))
//			inputMap.put("I_USAGE", StringUtil.checkNull(request.getParameter("vehicle_usage"))); // Vehicle Usage
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("allocation"),"")))
//			inputMap.put("I_VFOS_FLAG", StringUtil.checkNull(request.getParameter("allocation"))); // Allocation Type (All:"", VFOS:1, Non VFOS:2)
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("date_from"),"")))
			inputMap.put("I_FRDAT", DateUtil.getChangeDate(request.getParameter("date_from"),"/")); // Document Date (Date Received/Sent)
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("date_to"),"")))
			inputMap.put("I_TODAT", DateUtil.getChangeDate(request.getParameter("date_to"),"/")); // Document Date (Date Received/Sent)
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("order_no"),"")))
			inputMap.put("I_VBELN", StringUtil.checkNull(request.getParameter("order_no"))); // Sales Document number
		
        String strRfcName = "ZHBR_SD_DP_OTD_SUMMARY";  // rfc 함수명
//        String strListTable = "T_DATA";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap));
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    /**
     * OTD Report Summary By Sales Order 검색<br>
     * OTD Report Summary 화면에서 Total 수량 클릭 시 실행된다.<br>
     * OTD Report Summary By Sales Order화면, OrderToDeliveryGrid 컴포넌트에서 호출.
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getOtdbySO(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        //inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
       // inputMap.put("I_DPFLAG", "X"); // dp에선 꼭 x를 넣으라 함.
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
        
        String strFsc = StringUtil.checkNull(request.getParameter("fsc"),"");
        	
       
        if(!"".equals(strFsc)){
        	strFsc = StringUtil.replaceAll(strFsc,"&", " ");
        	inputMap.put("I_ZCSFSC", strFsc); // FSC
        }
					
        if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
			inputMap.put("I_ZCCAR", StringUtil.checkNull(request.getParameter("vehicle_model"))); // Model
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
			inputMap.put("I_ZCBYTE", StringUtil.checkNull(request.getParameter("body_type"))); // body type
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("engine_capacity"),"")))
			inputMap.put("I_ZCECAPA", StringUtil.checkNull(request.getParameter("engine_capacity"))); // engine capacity
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
			inputMap.put("I_ZCTRMS", StringUtil.checkNull(request.getParameter("transmission"))); // Transmission Type
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("grade"),"")))
//			inputMap.put("I_ZCGRADE", StringUtil.checkNull(request.getParameter("grade"))); // grade
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("exterior_color"),"")))
			inputMap.put("I_ZCEXTC", StringUtil.checkNull(request.getParameter("exterior_color"))); // Extetior Color
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_usage"),"")))
//			inputMap.put("I_USAGE", StringUtil.checkNull(request.getParameter("vehicle_usage"))); // Vehicle Usage
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("allocation"),"")))
//			inputMap.put("I_VFOS_FLAG", StringUtil.checkNull(request.getParameter("allocation"))); // Allocation Type (All:"", VFOS:1, Non VFOS:2)
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("date_from"),"")))
			inputMap.put("I_FRDAT", DateUtil.getChangeDate(request.getParameter("date_from"),"/")); // Document Date (Date Received/Sent)
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("date_to"),"")))
			inputMap.put("I_TODAT", DateUtil.getChangeDate(request.getParameter("date_to"),"/")); // Document Date (Date Received/Sent)
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("order_no"),"")))
//			inputMap.put("I_VBELN", StringUtil.checkNull(request.getParameter("order_no"))); // Sales Document number
		
        String strRfcName = "ZHBR_SD_DP_OTD_SUM_SORD";  // rfc 함수명
//        String strListTable = "T_DATA";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap));
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    /**
     * OTD Report Summary By Sales Order 검색<br>
     * OTD Report Summary 화면에서 Total 수량 클릭 시 실행된다.<br>
     * OTD Report Summary By Sales Order화면, OrderToDeliveryGrid 컴포넌트에서 호출.
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getOtdbyDetail(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        //inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
       // inputMap.put("I_DPFLAG", "X"); // dp에선 꼭 x를 넣으라 함.
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
               	
		if(!"".equals(StringUtil.checkNull(request.getParameter("vbeln"),"")))
			inputMap.put("I_VBELN", StringUtil.checkNull(request.getParameter("vbeln"))); // Sales Document number
		
        String strRfcName = "ZHBR_SD_DP_OTD_SORD_DETAIL";  // rfc 함수명
//        String strListTable = "T_DATA";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap));
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    /**
     * OTD Report Summary By Sales Order 검색<br>
     * OTD Report Summary 화면에서 Total 수량 클릭 시 실행된다.<br>
     * OTD Report Summary By Sales Order화면, OrderToDeliveryGrid 컴포넌트에서 호출.
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getOtdWeek(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        //inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
       // inputMap.put("I_DPFLAG", "X"); // dp에선 꼭 x를 넣으라 함.
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code

        String strFsc = StringUtil.checkNull(request.getParameter("fsc"),"");
        	
       
        if(!"".equals(strFsc)){
        	strFsc = StringUtil.replaceAll(strFsc,"&", " ");
        	inputMap.put("I_ZCSFSC", strFsc); // FSC
        }
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("weekFrom"),"")))
			inputMap.put("I_WEEK", StringUtil.checkNull(request.getParameter("weekFrom"))); // Model
		
					
        if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
			inputMap.put("I_ZCCAR", StringUtil.checkNull(request.getParameter("vehicle_model"))); // Model
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
			inputMap.put("I_ZCBYTE", StringUtil.checkNull(request.getParameter("body_type"))); // body type
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("engine_capacity"),"")))
			inputMap.put("I_ZCECAPA", StringUtil.checkNull(request.getParameter("engine_capacity"))); // engine capacity
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
			inputMap.put("I_ZCTRMS", StringUtil.checkNull(request.getParameter("transmission"))); // Transmission Type
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("grade"),"")))
//			inputMap.put("I_ZCGRADE", StringUtil.checkNull(request.getParameter("grade"))); // grade
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("exterior_color"),"")))
			inputMap.put("I_ZCEXTC", StringUtil.checkNull(request.getParameter("exterior_color"))); // Extetior Color
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_usage"),"")))
//			inputMap.put("I_USAGE", StringUtil.checkNull(request.getParameter("vehicle_usage"))); // Vehicle Usage
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("allocation"),"")))
//			inputMap.put("I_VFOS_FLAG", StringUtil.checkNull(request.getParameter("allocation"))); // Allocation Type (All:"", VFOS:1, Non VFOS:2)
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("date_from"),"")))
			inputMap.put("I_FRDAT", DateUtil.getChangeDate(request.getParameter("date_from"),"/")); // Document Date (Date Received/Sent)
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("date_to"),"")))
			inputMap.put("I_TODAT", DateUtil.getChangeDate(request.getParameter("date_to"),"/")); // Document Date (Date Received/Sent)
		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("order_no"),"")))
//			inputMap.put("I_VBELN", StringUtil.checkNull(request.getParameter("order_no"))); // Sales Document number
		
        String strRfcName = "ZHBR_SD_DP_OTD_DET_WEEK";  // rfc 함수명
//        String strListTable = "T_DATA";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap));
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    /**
     * Transfer Request 조회화면에서 보여준다.<br>
     * TransferRequest 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getTransferRequest(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();

		String strDsn = StringUtil.checkNull(request.getParameter("dsn"),"");
		
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
		inputMap.put("I_RT_FLAG", "N");
		inputMap.put("I_REQ",   "X");
		
		if(!"".equals(strDsn)){
			inputMap.put("I_CHARG", strDsn);
		}else{
			if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
				inputMap.put("I_CLINE", StringUtil.checkNull(request.getParameter("vehicle_model")));
		}
		
//		inputMap.put("I_BDTYP", StringUtil.checkNull(request.getParameter("body_type")));
//		inputMap.put("I_TMTYP", StringUtil.checkNull(request.getParameter("transmission")));
//		inputMap.put("I_EXCOC", StringUtil.checkNull(request.getParameter("exterior_color")));
//		inputMap.put("I_RT_FLAG", StringUtil.checkNull(request.getParameter("retail")));
//      inputMap.put("I_LOCTN", StringUtil.checkNull(request.getParameter("logistic_status")));
//      inputMap.put("I_VIN",   StringUtil.checkNull(request.getParameter("vin")));
//		inputMap.put("I_MATNR",   StringUtil.checkNull(request.getParameter("fsc")));
//      inputMap.put("I_REQ",   StringUtil.checkNull(request.getParameter("transfer")));
//		inputMap.put("I_GRADE",   StringUtil.checkNull(request.getParameter("grade")));
//		inputMap.put("I_REGION",   StringUtil.checkNull(request.getParameter("statefor_dealer")));
		
		
        String strRfcName = "ZHAU_DP_LIST_OTR_DEALER_STK";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }



    /**
     * Rebate Document 화면에서 호출.<br>
     * RebateDocument 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getRebateDocument(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();

        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("vin"),"")))
        	inputMap.put("I_VHVIN", request.getParameter("vin"));
        
		inputMap.put("I_VHCLE", request.getParameter("dsn"));
        inputMap.put("I_AUTHC", "F".equals(UmeUtil.getAuthCode(request))?"IC":"AL");
		
        String strRfcName = "ZHAU_DP_LIST_REBATE";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }


    /**
     * Registration Type 조건값을 가져온다.<br>
     * Sales 컴포넌트에서 호출.
     * 
     * @param String I_MODEL model code
     * @param String I_BDTYP body type
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getRegistrationTypeCondition(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        String strRfcName = "ZHBR_SD_DP_RETAIL_TYPE";     // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명

        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
    /**
     * Dealer Name 을 가져온다.<br>
     * Sales 컴포넌트에서 호출.
     * 
     * @param IPortalComponentRequest request
     * @param String dealerCode
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public String getDealerName(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
    	
    	String strDealerName = "";
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode.toUpperCase());
        inputMap.put("I_ORDERBY", "02");
        
        String strRfcName = "ZHBR_SD_DP_LIST_DEALER";     // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명

        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

            if(returnList.size() > 0){
            	HashMap element = (HashMap)returnList.get(0);
            	strDealerName = (String)element.get("NAME1");
            }
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return strDealerName;
    }
    
    /**
     * Dealer Name 을 가져온다.<br>
     * Sales 컴포넌트에서 호출.
     * 
     * @param IPortalComponentRequest request
     * @param String dealerCode
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public String getDealerName2(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
    	
    	String strDealerName = "";
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode.toUpperCase());
        inputMap.put("I_ORDERBY", "02");
        
        String strRfcName = "ZHBR_SD_DP_LIST_DEALER";     // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명

        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

            if(returnList.size() > 0){
            	HashMap element = (HashMap)returnList.get(0);
            	if("".equals(StringUtil.checkNull((String)element.get("NAME3")))){
            		strDealerName = (String)element.get("NAME1");
            	}else{
            		strDealerName = (String)element.get("NAME3");
            	}
            	
            }
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return strDealerName;
    }
    
    /**
     * Other Dealer Stock 조회화면에서 request 이력을 팝업조회한다.<br>
     * RequestHistory 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getRequestHistory(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
		
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_WHO",     StringUtil.checkNull(request.getParameter("i_who"))); // RQ or RE
        inputMap.put("I_HISTORY", StringUtil.checkNull(request.getParameter("i_history")));
        inputMap.put("I_VIN",     StringUtil.checkNull(request.getParameter("i_vin")));
        
        String strRfcName = "ZHAU_DP_TRANSFER_REQUEST_LIST";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
    /**
     * sales에서 search 결과 데이터를 받아온다<br>
     * Sales 컴포넌트에서 호출.
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getRetailedCar(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        String strVin = StringUtil.checkNull(request.getParameter("vin"),"");
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code

		if(!"".equals(strVin)){
			inputMap.put("I_ZCVIN", strVin); // VIN
		}else{
			if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
				inputMap.put("I_ZCCAR", StringUtil.checkNull(request.getParameter("vehicle_model"))); // Car-Line code
				
			if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))	
				inputMap.put("I_ZCBYTE", StringUtil.checkNull(request.getParameter("body_type"))); // Body-Type code
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("engine_capacity"),"")))
				inputMap.put("I_ZCECAPA", StringUtil.checkNull(request.getParameter("engine_capacity"))); // Engine-Capacity code
				
			if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
				inputMap.put("I_ZCTRMS", StringUtil.checkNull(request.getParameter("transmission"))); // Transmission-Type code
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("exterior_color"),"")))
				inputMap.put("I_ZCEXTC", StringUtil.checkNull(request.getParameter("exterior_color"))); // Extetior Color
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("registrationType"),"")))
				inputMap.put("I_ZCRETYTP", StringUtil.checkNull(request.getParameter("registrationType"))); //registration Type
			
			inputMap.put("I_FDATE", DateUtil.getChangeDate(request.getParameter("rt_from_date"),"/")); // from-date of retail
			inputMap.put("I_TDATE", DateUtil.getChangeDate(request.getParameter("rt_to_date"),"/")); // to-date of retail
		}
		

        String strRfcName = "ZHBR_SD_DP_SALES_ENQUIRY";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }

    /**
     * dealer stock에서 rda 하기전에 화면에 기본적으로 뿌려줘야할 데이터를 받아온다<br>
     * Demo, TBA, RDA 컴포넌트에서 호출.
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getRetailPrepare(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
        inputMap.put("I_RDA_TYPE", StringUtil.checkNull(request.getParameter("retailFlag"))); // 1:demo, 2:tba, 3:rda
        inputMap.put("I_VHVIN",    StringUtil.checkNull(request.getParameter("p_vin"))); // vin
          
//        inputMap.put("I_DEALER", "A02VE82045");
//        inputMap.put("I_RDA_TYPE", "3"); // 1:demo, 2:tba, 3:rda
//        inputMap.put("I_VHVIN",    "KNADH511LB6761060"); // vin
        
        String strRfcName = "ZHAU_DP_RETAIL_PREPARE";  // rfc 함수명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }


    /**
     * SecondOwner Owner add/change 화면에서 호출.<br>
     * CustomerInfo 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getSecondOwnerEC(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();

        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
		inputMap.put("I_ENDCU", StringUtil.checkNull(request.getParameter("customerNo")));
		
        String strRfcName = "ZHBR_SD_DP_GET_EC";  // rfc 함수명
        
        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    /**
     * SecondOwner Owner manintenance 화면에서 호출.<br>
     * SecondOwner 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getSecondOwnerHistory(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();

        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("vin"),"")))
        	inputMap.put("I_VHVIN", StringUtil.checkNull(request.getParameter("vin")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("dsn"),"")))
			inputMap.put("I_VHCLE", StringUtil.checkNull(request.getParameter("dsn")));
		
        String strRfcName = "ZHAU_DP_FIND_EC_HISTORY";  // rfc 함수명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }



	/**
	 * Roadside 화면에서 호출.<br>
	 * Roadside 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @param dealerCode 딜러코드
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public HashMap getRoadside(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		HashMap returnMap = new HashMap();
		HashMap inputMap = new HashMap();

		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        
		if(!"".equals(StringUtil.checkNull(request.getParameter("vin"),"")))
			inputMap.put("I_VHVIN", StringUtil.checkNull(request.getParameter("vin")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("dsn"),"")))
			inputMap.put("I_VHCLE", StringUtil.checkNull(request.getParameter("dsn")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("rego"),"")))
			inputMap.put("I_ZZREGON", StringUtil.checkNull(request.getParameter("rego")).toUpperCase());
			
		String strRfcName = "ZHAU_DP_ROADSIDE_SEARCH";  // rfc 함수명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap;
	}
	

	/**
	 * Roadside Assist Renewal<br>
	 * RoadsideTrans 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @param dealerCode 딜러코드
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList setRoadsideRenewal(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
		ArrayList inputStructureList = new ArrayList();
        
		// import parameters setting
		inputMap.put("I_BUKRS",    CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR",   CommonProperties.DEALER_PREFIX + dealerCode);
		
		inputMap.put("I_VHCLE",    request.getParameter("dsn"));
		inputMap.put("I_ZZREGON",    request.getParameter("rego"));
		inputMap.put("I_ZSRV_DATE",    DateUtil.getChangeDate(request.getParameter("service_date"),"/"));
		inputMap.put("I_ZSRV_TYPE",    request.getParameter("type"));
		inputMap.put("I_ZODOMETER",    request.getParameter("odometer"));
		
		
		// structure setting
		inputStructureList.add(paramSettingPrimary(request));
//		inputStructureList.add(paramSettingSecond(request));

		if( !"".equals(StringUtil.checkNull(request.getParameter("company_name"))) ){
			inputStructureList.add(paramSettingCompany(request));
		}

		String strRfcName = "ZHAU_DP_ROADSIDE_RENEWAL";  // rfc 함수명
		String strListTable = "T_RETURN";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap, inputStructureList, new ArrayList()),strListTable);

		} catch (Exception e) {
			System.err.println(e.toString());
			LogWriter.errorT(this, "Exception: " + e.getMessage());
			e.printStackTrace();
		} finally {
			closeConnector();
		}

		return returnList;
	}	
	
    /**
     * SecondOwner Owner search 화면에서 호출.<br>
     * SelectOwner 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getSecondOwnerSearch(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();

        // import parameters setting
        
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        if("P".equals(StringUtil.checkNull(request.getParameter("p_customerType"),""))){
        	inputMap.put("I_TYPE", "1");
        	inputMap.put("I_NAME_FIRST", request.getParameter("customerName"));
        }else if("B".equals(StringUtil.checkNull(request.getParameter("p_customerType"),""))){
        	inputMap.put("I_TYPE", "2");
        	inputMap.put("I_NAME_FIRST", request.getParameter("companyName"));
        }
		
        String strRfcName = "ZHBR_SD_DP_SEARCH_ENDCUSTOMER";  // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명
        
        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    /**
     * Transmission 조건값을 가져온다.<br>
     * model 조건이 선택된 이후에 호출된다.<br>
     * vehicle availability화면, ConditionInfo 컴포넌트에서 호출.
     * 
     * @param String I_MODEL model code
     * @param String I_CODE flag  Body Type : "BTY", Eng Capacity : "EC", TM Type : "TMT" 
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getTransmissionCondition(IPortalComponentRequest request, String I_MODEL)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_CODE" , CommonProperties.TYPE_TM_TYPE);
        inputMap.put("I_MODEL", I_MODEL);

        String strRfcName = "ZHBR_SD_DP_DESC_CODE";  // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명

        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
     
    /**
     * Vehicle Availability 검색<br>
     * 조회화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     * vehicle availability화면, VehicleAvailability 컴포넌트에서 호출.
     * 
     * @param request 
     * @param String dealerCode dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getVehicleAvailabilitySearch(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
			 
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
            
        // import parameters setting
        inputMap.put("I_DEALER",CommonProperties.DEALER_PREFIX + dealerCode);
        
        inputMap.put("I_MODEL", request.getParameter("vehicle_model"));
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
        	inputMap.put("I_BDTYP", request.getParameter("body_type"));
        
		if(!"".equals(StringUtil.checkNull(request.getParameter("engine_capacity"),"")))
			inputMap.put("I_ENGCP", request.getParameter("engine_capacity"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
			inputMap.put("I_TMCOD", request.getParameter("transmission"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("fuel_type"),"")))
			inputMap.put("I_FUELC", request.getParameter("fuel_type"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("exterior_color"),"")))
			inputMap.put("I_ZCEXTC",  request.getParameter("exterior_color"));
		
		
        String strRfcName = "ZHBR_SD_DP_LIST_AVAILABLE";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
    
    /**
     * Vehicle Availability 검색<br>
     * 조회화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     * vehicle availability화면, VehicleAvailability 컴포넌트에서 호출.
     * 
     * @param request 
     * @param String dealerCode dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
//    public HashMap getVehicleAvailabilitySearch(IPortalComponentRequest request, String dealerCode)
//        throws ConnectorException, ResourceException {
//			 
//    	HashMap returnMap = new HashMap();
//        HashMap inputMap = new HashMap();
//            
//        // import parameters setting
//        inputMap.put("I_DEALER",CommonProperties.DEALER_PREFIX + dealerCode);
//        
//        inputMap.put("I_MODEL", request.getParameter("vehicle_model"));
//        
//        if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
//        	inputMap.put("I_BDTYP", request.getParameter("body_type"));
//        
//		if(!"".equals(StringUtil.checkNull(request.getParameter("engine_capacity"),"")))
//			inputMap.put("I_ENGCP", request.getParameter("engine_capacity"));
//		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
//			inputMap.put("I_TMCOD", request.getParameter("transmission"));
//		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("fuel_type"),"")))
//			inputMap.put("I_FUELC", request.getParameter("fuel_type"));
//		
//		if(!"".equals(StringUtil.checkNull(request.getParameter("exterior_color"),"")))
//			inputMap.put("I_ZCEXTC",  request.getParameter("exterior_color"));
//		
//		
//        String strRfcName = "ZHBR_SD_DP_LIST_AVAILABLE";  // rfc 함수명
//        String strListTable = "T_LIST";              // 리턴 테이블명
//        
//        try {
//
//            //connection 생성
//        	if(jca.getConnection() == null) createConnector();
//
//            // rfc excute
//        	returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap, new ArrayList(), strListTable, new ArrayList()));
//
//        } catch (Exception e) {
//            LogWriter.errorT(this, "Exception: " + e.getMessage());
//        } finally {
//            closeConnector();
//        }
//
//        return returnMap;
//    }
    
    
    
    
    /**
     * Other Dealer Stock 조회화면에서 보여준다.<br>
     * OtherDealerStock 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getVehicleTransfer(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
		String strVin = StringUtil.checkNull(request.getParameter("vin"),"");
		String strDsn = StringUtil.checkNull(request.getParameter("dsn"),""); 
		
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        
        if(!"".equals(strVin) || !"".equals(strDsn)){
			inputMap.put("I_VIN",   strVin);
			inputMap.put("I_VHCLE",   strDsn);
        }else{
			//inputMap.put("I_RE_OK", "Q".equals(StringUtil.checkNull(request.getParameter("requested"),"X"))?"":"X");
			inputMap.put("I_RE_OK", "X");	
        }
        
        String strRfcName = "ZHAU_DP_TRANSFER_REQSTK_LIST";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }


    /**
     * Vehicle Specification 검색<br>
     * master vehicle Specification화면, VehicleSpecification 컴포넌트에서 호출.
     * 
     * @param request 
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getVehicleSpecificationSearch(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        String strFsc = StringUtil.checkNull(request.getParameter("fsc"), "");
        strFsc = strFsc.replace(" ", "   ");
        
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_MODEL", request.getParameter("vehicle_model"));
        inputMap.put("I_BODYC", request.getParameter("body_type"));
        inputMap.put("I_ENGCP", request.getParameter("engine_capacity"));
        inputMap.put("I_FUELC", request.getParameter("fuel_type"));
        inputMap.put("I_TMCOD", request.getParameter("transmission"));
        inputMap.put("I_GRADE", request.getParameter("grade"));
		inputMap.put("I_MATNR", strFsc);
        //inputMap.put("I_EXTC",  request.getParameter("exterior_color"));

        String strRfcName = "ZHBR_SD_DP_LIST_MASTER";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

//            LogWriter.errorT(this, "returnList: " + returnList.size());
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }


	/**
	 * Any Port 검색<br>
	 * 조회화면에서 Anyport 이미지 클릭 시 실행된다.<br>
	 * vehicle availability화면, AnyPort 컴포넌트에서 호출.
	 * 
	 * @param request 
	 * @param String dealerCode dealer code
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getAnyPortSearch(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
			 
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();

		// import parameters setting
        
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
		
		inputMap.put("I_FSC", request.getParameter("p_fsc"));
		inputMap.put("I_EXTC", request.getParameter("p_extColorCode"));
		inputMap.put("I_INTC", request.getParameter("p_intColorCode"));
		
		String strRfcName = "ZHAU_DP_OTHER_PORT";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}
		return returnList;
	}
	
	
	/**
	 * Any Port 검색<br>
	 * 조회화면에서 Anyport 이미지 클릭 시 실행된다.<br>
	 * vehicle availability화면, AnyPort 컴포넌트에서 호출.
	 * 
	 * @param request 
	 * @param String dealerCode dealer code
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getAnyPortDetailSearch(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
			 
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();

		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_model"),"")))
			inputMap.put("I_MODEL", request.getParameter("p_model"));
			
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_bodyType"),"")))
			inputMap.put("I_BODYC", request.getParameter("p_bodyType"));
	
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_engine_capacity"),"")))
			inputMap.put("I_ENGCP", request.getParameter("p_engine_capacity"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_tmCode"),"")))
			inputMap.put("I_TMCOD", request.getParameter("p_tmCode"));
			
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_grade"),"")))
			inputMap.put("I_GRADE", request.getParameter("p_grade"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_fsc"),"")))
			inputMap.put("I_FSC", request.getParameter("p_fsc"));
			
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_extColorCode"),"")))
			inputMap.put("I_EXTC", request.getParameter("p_extColorCode"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_intColorCode"),"")))
			inputMap.put("I_INTC", request.getParameter("p_intColorCode"));
		
		inputMap.put("I_LGORT", request.getParameter("p_whs"));

		String strRfcName = "ZHAU_DP_LIST_CAR_SUM";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}
		return returnList;
	}
	    
    /**
     * order enquiry 조회 시 조건값을 가져온다.<br>
     * OrderCreate, OrderEnquiry 컴포넌트에서 호출.
     * 
     * @param flag String 조회플래그 (1:정상오더, 2:오더변경, 3:vfos, 4:조회)
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getVehicleUsage(IPortalComponentRequest request, String flag)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_FLAG", flag);

        String strRfcName = "ZHBR_SD_DP_LIST_USAGE"; // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명

        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();
            
            // rfc 실행
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }    

    
    /**
     * order enquiry 조회 시 조건값을 가져온다.<br>
     * OrderCreate, OrderEnquiry 컴포넌트에서 호출.
     * 
     * @param flag String 조회플래그 (1:정상오더, 2:오더변경, 3:vfos, 4:조회)
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getOrderType(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        String strRfcName = "ZHBR_SD_DP_GET_ORDER_TYPE"; // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명

        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();
            
            // rfc 실행
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
    /**
     * Vfos Enquiry 검색<br>
     * Vfos Enqiry화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     * Vfos Enqiry화면, VfosEnqiry 컴포넌트에서 호출.
     * 
     * @param request 전달 변수를 담은 객체
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getVfosCondition(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
//        inputMap.put("I_KUNNR", "C02VA06759"); // Dealer code
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + request.getParameter("dealerCode"));
        inputMap.put("I_MONTH", StringUtil.checkNull(request.getParameter("month"))); // month
        inputMap.put("I_CLINE", StringUtil.checkNull(request.getParameter("vehicle_model"))); // Model
        inputMap.put("I_BDTYP", StringUtil.checkNull(request.getParameter("body_type"))); // body type

        String strRfcName = "ZHAU_DP_QUOTA_FILTER";  // rfc 함수명
        String strListTable = "";             // 리턴 테이블명
        
        if( "".equals(StringUtil.checkNull(request.getParameter("month"))) ){
            strListTable = "T_MONTH";
        }else if( "".equals(StringUtil.checkNull(request.getParameter("vehicle_model"))) ){
            strListTable = "T_CLINE";
        }else if( "".equals(StringUtil.checkNull(request.getParameter("body_type")))
                && "T".equals(StringUtil.checkNull(request.getParameter("tmFlag"))) ){
            strListTable = "T_TMTYP";
        }else if( "".equals(StringUtil.checkNull(request.getParameter("body_type"))) ){
            strListTable = "T_BDTYP";
        }else{
            strListTable = "T_TMTYP";
        }
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }  
    
    /**
     * Vfos Enquiry 검색<br>
     * Vfos Enqiry화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     * Vfos Enqiry화면, VfosEnqiry 컴포넌트에서 호출.
     * 
     * @param request 전달 변수를 담은 객체
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getVfosEnquirySearch(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_MONTH", StringUtil.checkNull(request.getParameter("month"))); // month
        inputMap.put("I_CLINE", StringUtil.checkNull(request.getParameter("vehicle_model"))); // Model
        inputMap.put("I_BDTYP", StringUtil.checkNull(StringUtil.checkNull(request.getParameter("body_type")))); // body type
        inputMap.put("I_TMTYP", StringUtil.checkNull(StringUtil.checkNull(request.getParameter("transmission")))); // T/M type
            
        String strRfcName = "ZHAU_DP_QUOTA_LIST";  // rfc 함수명
        String strListTable = "T_LIST";             // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }    
    

    /**
     * Vfos Order by spec 조회<br>
     * Vfos Order by spec화면이 호출될 때 실행된다.<br>
     * Vfos Order by spec화면, VfosOrderBySpec 컴포넌트에서 호출.
     * 
     * @param String I_MODEL model code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getVfosOrderBySpec(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_MONTH", StringUtil.checkNull(request.getParameter("month"))); // month
        inputMap.put("I_CLINE", StringUtil.checkNull(request.getParameter("vehicle_model"))); // Model
        inputMap.put("I_MATNR", StringUtil.checkNull(request.getParameter("fsc"))); // fsc
//        inputMap.put("I_KUNNR", "A02VE82142");
//        inputMap.put("I_MONTH", "201009"); // month
//        inputMap.put("I_CLINE", "BN"); // Model
//        inputMap.put("I_MATNR", "BNS6A5615GGF62"); // fsc

        String strRfcName = "ZHAU_DP_QUOTA_SERCHCOND";  // rfc 함수명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    } 
    
    /**
     * back order 정보 조회<br>
     * ViewBackOrderDetail 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getViewBackOrder(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_MATNR", StringUtil.checkNull(request.getParameter("p_fsc")));
//        inputMap.put("I_MYGRP", StringUtil.checkNull(request.getParameter("p_mygrp")));
        inputMap.put("I_EXCOD", StringUtil.checkNull(request.getParameter("p_extColorCode")));
        inputMap.put("I_INCOD", StringUtil.checkNull(request.getParameter("p_intColorCode")));

        String strRfcName = "ZHAU_DP_BO_LIST";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명

        try {
            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }

    /**
     * color 정보 조회<br>
     * ViewColor 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getViewColor(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_FSC", StringUtil.checkNull(request.getParameter("p_fsc")));

        String strRfcName = "ZHBR_SD_DP_LIST_COLOR";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명

        try {
            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }


    /**
     * Logistics 정보 조회<br>
     * ViewLogisticsDetail 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getViewLogistics(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_vin"),"")))
			inputMap.put("I_VHVIN", StringUtil.checkNull(request.getParameter("p_vin")));
        
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_dsn"),"")))
			inputMap.put("I_VHCLE", StringUtil.checkNull(request.getParameter("p_dsn")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_rego_no"),"")))
			inputMap.put("I_REGO_NO", StringUtil.checkNull(request.getParameter("p_rego_no")));
		
        String strRfcName = "ZHAU_DP_VIEW_LOGISTICS";  // rfc 함수명

        try {
            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    

    /**
     * vin 정보 조회<br>
     * ViewVinDetail 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getViewVin(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("p_vin"),"")))
        	inputMap.put("I_ZCVIN", StringUtil.checkNull(request.getParameter("p_vin")));
        
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_rego_no"),"")))
			inputMap.put("I_ZCREGON", StringUtil.checkNull(request.getParameter("p_rego_no")).toUpperCase());
		
        String strRfcName = "ZHBR_SD_DP_VIEW_VIN";  // rfc 함수명

        try {
            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    /**
     * vin 정보 조회<br>
     * ViewVinDetail 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getViewFsc(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        String strFsc = StringUtil.checkNull(request.getParameter("p_fsc"));
        strFsc = strFsc.replace(" ", "   ");
        
        // import parameters setting
        inputMap.put("I_FSC", strFsc);

        String strRfcName = "ZHBR_SD_DP_GET_FSC";  // rfc 함수명

        try {
            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    

    /**
     * My Accocated Stock Status 조회<br>
     * ViewMyAllocatedStock 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getViewMyAllocatedStock(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_MATNR", StringUtil.checkNull(request.getParameter("p_fsc")));

        inputMap.put("I_EXCOD", StringUtil.checkNull(request.getParameter("p_extColorCode")));
        inputMap.put("I_INCOD", StringUtil.checkNull(request.getParameter("p_intColorCode")));
        inputMap.put("I_LOCTN", StringUtil.checkNull(request.getParameter("p_loctn")));//130:sea,140:port,150:comp

        String strRfcName = "ZHAU_DP_ALLOC_LIST";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }

    /**
     * Stock Status 조회<br>
     * Vehicle Available 조회화면에서 팝업으로 보여준다.<br>
     * ViewMyStockDetail 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getViewMyStock(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_MATNR", StringUtil.checkNull(request.getParameter("p_fsc")));
//        inputMap.put("I_MYGRP", StringUtil.checkNull(request.getParameter("p_mygrp")));
        inputMap.put("I_EXCOD", StringUtil.checkNull(request.getParameter("p_extColorCode")));
        inputMap.put("I_INCOD", StringUtil.checkNull(request.getParameter("p_intColorCode")));

//        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
//        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
//        inputMap.put("I_FSC",   request.getParameter("fsc"));
//        inputMap.put("I_EXTC",  StringUtil.checkNull(request.getParameter("extc")));
//        inputMap.put("I_INTC",  StringUtil.checkNull(request.getParameter("intc")));
//        inputMap.put("I_LOCTN", StringUtil.checkNull(request.getParameter("loctn"))); //vehicle location
//        inputMap.put("I_LGORT", StringUtil.checkNull(request.getParameter("locrt"))); //whs(storage location)

//        String strRfcName = "ZHAU_DP_LIST_CAR";  // rfc 함수명
        String strRfcName = "ZHAU_DP_MYSTK_LIST";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    

    /**
     * other Stock Status 조회<br>
     * Vehicle Available 조회화면에서 팝업으로 보여준다.<br>
     * ViewOtherStockDetail 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getViewOtherStock(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_MATNR", StringUtil.checkNull(request.getParameter("p_fsc")));
//        inputMap.put("I_MYGRP", StringUtil.checkNull(request.getParameter("p_mygrp")));
        inputMap.put("I_EXCOD", StringUtil.checkNull(request.getParameter("p_extColorCode")));
        inputMap.put("I_INCOD", StringUtil.checkNull(request.getParameter("p_intColorCode")));
        inputMap.put("I_LGORT", StringUtil.checkNull(request.getParameter("p_whs")));

        String strRfcName = "ZHAU_DP_OTRSTK_LIST";  // rfc 함수명
        String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    /**
     * Order 생성 .<br>
     * OrderCreate 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap setOrderCreate(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        ArrayList inputStructureList = new ArrayList();
		ArrayList inputList = new ArrayList();
		
		String usage = request.getParameter("sel_orderType");
		
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_EPUSER", request.getUser().getLogonUid());
		
        HashMap I_ORDER = new HashMap();
        I_ORDER.put("STRUCTURE_NAME", "I_ORDER");
        I_ORDER.put("AUDAT",   	DateUtil.getChangeDate(request.getParameter("AUDAT"),"/"));	// Order Date
        I_ORDER.put("KUNWE",   	request.getParameter("sel_deliveryto"));					// Ship to
        I_ORDER.put("ZCKUNRE",  request.getParameter("sel_billto"));						// Bill to
        
		I_ORDER.put("VDATU",   	DateUtil.getChangeDate(request.getParameter("AUDAT"),"/"));	
        I_ORDER.put("WMENG",   	request.getParameter("WMENG"));														// Order Qty
        I_ORDER.put("MATNR",   	request.getParameter("MATNR"));								// FSC
        I_ORDER.put("AUART",   	request.getParameter("sel_orderType"));						// Order Type							// Usage
        I_ORDER.put("ZCEXTC",  	request.getParameter("ZZEXTCL"));							// Exterior
        I_ORDER.put("ZCINTC",  	request.getParameter("ZZINTCL"));							// Interior
        I_ORDER.put("ZCREMARK1",request.getParameter("order_remark"));						// Order Remark
        I_ORDER.put("ZCMYEAR"	,request.getParameter("ZCMYEAR"));							// Model Year Code
        // structure setting
        inputStructureList.add(I_ORDER);
        
        String strRfcName = "ZHBR_SD_DP_SAVE_ORDER";  // rfc 함수명
		String importTable  = "T_LIST";  
		
//        LogWriter.debugT(this, "Function : setOrderCreate Start---------");
//		LogWriter.debugT(this, "RFC Name : " + strRfcName);
//		LogWriter.debugT(this, "AUDAT	 : " + DateUtil.getChangeDate(request.getParameter("AUDAT"),"/"));
//		LogWriter.debugT(this, "KUNWE	 : " + request.getParameter("sel_deliveryto"));
//		LogWriter.debugT(this, "ZCKUNRE	 : " + request.getParameter("sel_billto"));
//		LogWriter.debugT(this, "WMENG	 : " + request.getParameter("WMENG"));
//		LogWriter.debugT(this, "MATNR	 : " + request.getParameter("MATNR"));
//		LogWriter.debugT(this, "PSTYV	 : " + request.getParameter("sel_orderType"));
//		LogWriter.debugT(this, "ZCEXTC	 : " + request.getParameter("ZZEXTCL"));
//		LogWriter.debugT(this, "ZCINTC	 : " + request.getParameter("ZZINTCL"));
//		LogWriter.debugT(this, "ZCREMARK1	 : " + request.getParameter("order_remark"));
//		
//		LogWriter.debugT(this, "I_BUKRS	 : " + CommonProperties.COMPANY_CODE);
//		LogWriter.debugT(this, "I_DEALER : " + CommonProperties.DEALER_PREFIX + dealerCode);
//		LogWriter.debugT(this, "I_EPUSER : " + request.getUser().getLogonUid());
		
		
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap, inputStructureList, importTable, inputList));
			

        } catch (Exception e) {
            System.err.println(e.toString());
            LogWriter.errorT(this, "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnector();
        }

        return returnMap;
    }


    /**
     * Order 변경 .<br>
     * OrderCreate 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap setOrderChange(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        ArrayList inputStructureList = new ArrayList();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
        
		if("F".equals(StringUtil.checkNull(request.getParameter("area_gubun"),""))){
			inputMap.put("I_FLEET", "X");
		}else if("B".equals(StringUtil.checkNull(request.getParameter("area_gubun"),""))){
			inputMap.put("I_MIG",  StringUtil.checkNull(request.getParameter("company_migration_flag")));
		}
			

        HashMap I_ORDER = new HashMap();
        I_ORDER.put("STRUCTURE_NAME", "I_ORDER");
        //I_ORDER.put("AUDAT",   DateUtil.getChangeDate(request.getParameter("AUDAT"),"/"));
        //I_ORDER.put("VDATU",   DateUtil.getChangeDate(request.getParameter("VDATU"),"/"));
        I_ORDER.put("KUNWE",   request.getParameter("sel_deliveryto"));
        I_ORDER.put("VBELN",   request.getParameter("VBELN"));
        I_ORDER.put("MVGR1",   request.getParameter("sel_vehicleUsage"));
        I_ORDER.put("REMARK", request.getParameter("order_remark"));
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("finance_release_no"),"")))
			I_ORDER.put("XBLNR",   request.getParameter("finance_release_no"));
			
//        I_ORDER.put("PARTNER",  StringUtil.checkNull(request.getParameter("PARTNER")));
        
        // structure setting
        inputStructureList.add(I_ORDER);
        inputStructureList.add(paramSettingPrimary(request));
//        inputStructureList.add(paramSettingSecond(request));

//        if( !"".equals(StringUtil.checkNull(request.getParameter("company_name"))) ){
        inputStructureList.add(paramSettingCompany(request));
//        }
 
        String strRfcName = "ZHAU_DP_CHANGE_ORDER";  // rfc 함수명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap, inputStructureList, new ArrayList()));

        } catch (Exception e) {
            System.err.println(e.toString());
            LogWriter.errorT(this, "Exception: " + e.getMessage());
            e.printStackTrace();
            returnMap.put("error", e.toString());
        } finally {
            closeConnector();
        }

        return returnMap;
    }


    /**
     * second owner 생성 및 수정.<br>
     * ChangeOwnerTrans 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList setOwner(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        ArrayList inputStructureList = new ArrayList();
        String strDrvUsg = StringUtil.checkNull(request.getParameter("area_gubun"));
		if("".equals(strDrvUsg))
			strDrvUsg = request.getParameter("p_drvusg");
        
        // import parameters setting
        inputMap.put("I_BUKRS",    CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER",   CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_VHVIN",    request.getParameter("p_vin"));
        inputMap.put("I_SOLD_TO_DATE",  DateUtil.getChangeDate(request.getParameter("soldToDate"),"/"));
        inputMap.put("I_ZZREGON", StringUtil.checkNull(request.getParameter("p_reg").toUpperCase()));
		
		//LogWriter.debugT(this, "salesMan: " + request.getParameter("salesMan"));
		if(!"".equals(StringUtil.checkNull(request.getParameter("salesMan"))))
			inputMap.put("I_ZZPARNR",  request.getParameter("salesMan"));
        
		if("F".equals(strDrvUsg) ){
			inputMap.put("I_FLEET",  "X");
		}else if("B".equals(strDrvUsg)){
			inputMap.put("I_MIG",  StringUtil.checkNull(request.getParameter("company_migration_flag")));
		}
        
        // structure setting
        inputStructureList.add(paramSettingPrimary(request));
//        inputStructureList.add(paramSettingSecond(request));

        if( !"".equals(StringUtil.checkNull(request.getParameter("company_name"))) ){
            inputStructureList.add(paramSettingCompany(request));
        }

        String strRfcName = "ZHAU_DP_ADD_UPDATE_EC";  // rfc 함수명
        String strListTable = "T_RETURN";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap, inputStructureList, new ArrayList()),strListTable);

        } catch (Exception e) {
            System.err.println(e.toString());
            LogWriter.errorT(this, "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    /**
     * second owner의 엔드 커스터머 삭제.<br>
     * SecondOwner 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList setOwnerCustomerDelete(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS",    CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER",   CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_VHVIN",    request.getParameter("p_vin"));
        inputMap.put("I_ENDCU",    StringUtil.checkNull(request.getParameter("p_customer")));

        String strRfcName = "ZHAU_DP_DEL_EC";  // rfc 함수명
        String strListTable = "T_RETURN";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            System.err.println(e.toString());
            LogWriter.errorT(this, "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnector();
        }

        return returnList;
    }

    /**
     * rda 생성 .<br>
     * Demo,Rda,Tba 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList setRda(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        ArrayList inputStructureList = new ArrayList();
        
        // import parameters setting
//        inputMap.put("I_BUKRS",    CommonProperties.COMPANY_CODE);
//        inputMap.put("I_DEALER",   CommonProperties.DEALER_PREFIX + dealerCode);
//        inputMap.put("I_RDA_TYPE", request.getParameter("rda_type"));
//        inputMap.put("I_VHVIN",    request.getParameter("vin"));
//        inputMap.put("I_ZZREGDT",  DateUtil.getChangeDate(request.getParameter("warrantyStartDate"),"/"));
//        inputMap.put("I_ZZREGTY",  StringUtil.checkNull(request.getParameter("registrationType")));
//        inputMap.put("I_ZZREGON",  request.getParameter("plateNo").toUpperCase());
//        inputMap.put("I_ZZVFACTS", StringUtil.checkNull(request.getParameter("purposeCode")));
//        //inputMap.put("I_ZZRTLDT",  DateUtil.getChangeDate(request.getParameter("retailDate"),"/"));
//        inputMap.put("I_ZZRTLDT",  DateUtil.getChangeDate(request.getParameter("ZZRTLDT"),"/"));//retail date
		
        inputStructureList.add(paramSettingRdaInfo(request, dealerCode));
		
        // structure setting
        // 마지막인 경우에만 사용됨.
        if("P".equals(request.getParameter("customerType"))){
            inputStructureList.add(paramSettingPrimary(request));
        }else if("B".equals(request.getParameter("customerType"))){
       	 	inputStructureList.add(paramSettingCompany(request));
        }

        String strRfcName = "ZHBR_SD_DP_RETAIL_INFO";  // rfc 함수명
        String strListTable = "T_RETURN";              // 리턴 테이블명
        
        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap, inputStructureList, new ArrayList()),strListTable);

        } catch (Exception e) {
            System.err.println(e.toString());
            LogWriter.errorT(this, "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
    
    
    /**
     * Yearly Forecast 조회값을 가져온다.<br>
     * yearlyForecast화면, YearlyForecast 컴포넌트에서 호출.
     * 
     * @param StrIPortalComponentRequest request		parameter
     * @param String strDealerCode 
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getYearlyForecast(IPortalComponentRequest request, String strDealerCode)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
          
        // import parameters setting
        inputMap.put("I_YDMD", "Y");
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + strDealerCode);
        
        if("excel".equals(StringUtil.checkNull(request.getParameter("excel"),""))){
        	inputMap.put("I_FLAG",	"");
        }else{
        	inputMap.put("I_FLAG",	"X");
        }
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("year"),"")))
			inputMap.put("I_ZCYYYY",	StringUtil.checkNull(request.getParameter("year")));
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
			inputMap.put("I_ZCCAR",	StringUtil.checkNull(request.getParameter("vehicle_model")));		// Model Code
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
			inputMap.put("I_ZCBYTE",	StringUtil.checkNull(request.getParameter("body_type")));		// Body Type
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("engine_capacity"),"")))
			inputMap.put("I_ZCECAPA",	StringUtil.checkNull(request.getParameter("engine_capacity")));	// Engine Capacity
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("fuel_type"),"")))
			inputMap.put("I_ZCFTYPE",	StringUtil.checkNull(request.getParameter("fuel_type")));		// Fuel Type
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
			inputMap.put("I_ZCTRMS",	StringUtil.checkNull(request.getParameter("transmission")));	// Transmission
		
        String strRfcName = "ZHBR_SD_DP_APS_FORECAST_LIST";  // rfc 함수명
        String strListTable = "T_YEARLY";           // 리턴 테이블명

        try {
            //connection 생성
        	if(jca.getConnection() == null) createConnector();
            
            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }
        return returnList;
    }
    
    
    /**
     * Monthly Forecast Button Control Flag.
     * 
     * @param request parameter
     * @param String  parameter
     * @return  HashMap data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getControlFlag(IPortalComponentRequest request, String strMonth)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
		
        
        if("".equals(strMonth)){
        	inputMap.put("I_ZYYYMM", DateUtil.getAddMonth(DateUtil.getSysDate(2), 2));
        }else{
        	inputMap.put("I_ZYYYMM", request.getParameter("year")  + strMonth);
        }
        
        String strRfcName = "ZHBR_SD_DP_PLANNING_PERIOD";  // rfc 함수명
        //String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
        	if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    
    /**
     * Yearly Forecast Save<br>
     * yearlyForecast화면, YearlyForecast 컴포넌트에서 호출.
     * 
     * @param StrIPortalComponentRequest request		parameter
     * @param String strDealerCode 
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public Hashtable setYearlyForecast(IPortalComponentRequest request, ArrayList dataList, String strDealerCode)
        throws ConnectorException, ResourceException {
    	
    	
    	LogWriter.debugT(this, "multipartRequest start");
    	
    	Hashtable msgInfo = new Hashtable();
        ArrayList returnList = new ArrayList();
        Hashtable input = new Hashtable();
        ArrayList inputTableName = new ArrayList();
        ArrayList inputTable = new ArrayList();
		ArrayList innerTable = new ArrayList();
		
        // import parameters setting
        input.put("I_YDMD", "Y");
        input.put("I_KUNNR", CommonProperties.DEALER_PREFIX + strDealerCode);
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("year"),"")))
			input.put("I_ZCYYYY",	StringUtil.checkNull(request.getParameter("year")));
		
        
        String strRfcName = "ZHBR_SD_DP_APS_FORECAST_SAVE";  // rfc 함수명
        String strListTable = "T_YEARLY";           // 리턴 테이블명
        inputTableName.add(strListTable);
        
        
        for(int i=0; i<dataList.size(); i++) {
			HashMap hm = (HashMap)dataList.get(i);
			Hashtable tableRow = new Hashtable();

			LogWriter.debugT(this, "ZCYYYY = " + hm.get("ZCYYYY"));
			LogWriter.debugT(this, "ZCMC = " + hm.get("ZCMC"));
			LogWriter.debugT(this, "ZCCAR = " + hm.get("ZCCAR"));
			LogWriter.debugT(this, "ZCCARX = " + hm.get("ZCCARX"));
			LogWriter.debugT(this, "ZCBYTE = " + hm.get("ZCBYTE"));
			LogWriter.debugT(this, "ZCBYTEX = " + hm.get("ZCBYTEX"));
			LogWriter.debugT(this, "ZCECAPA = " + hm.get("ZCECAPA"));
			LogWriter.debugT(this, "ZCECAPAX = " + hm.get("ZCECAPAX"));
			LogWriter.debugT(this, "ZCETYPE = " + hm.get("ZCETYPE"));
			LogWriter.debugT(this, "ZCETYPEX = " + hm.get("ZCETYPEX"));
			LogWriter.debugT(this, "ZCFTYPE = " + hm.get("ZCFTYPE"));
			LogWriter.debugT(this, "ZCFTYPEX = " + hm.get("ZCFTYPEX"));
			LogWriter.debugT(this, "ZCTRMS = " + hm.get("ZCTRMS"));
			LogWriter.debugT(this, "ZCTRMSX = " + hm.get("ZCTRMSX"));
			LogWriter.debugT(this, "ZQJAN = " + hm.get("ZQJAN"));
			LogWriter.debugT(this, "ZQFEB = " + hm.get("ZQFEB"));
			LogWriter.debugT(this, "ZQMAR = " + hm.get("ZQMAR"));
			LogWriter.debugT(this, "ZQAPR = " + hm.get("ZQAPR"));
			LogWriter.debugT(this, "ZQMAY = " + hm.get("ZQMAY"));
			LogWriter.debugT(this, "ZQJUN = " + hm.get("ZQJUN"));
			LogWriter.debugT(this, "ZQJUL = " + hm.get("ZQJUL"));
			LogWriter.debugT(this, "ZQAUG = " + hm.get("ZQAUG"));
			LogWriter.debugT(this, "ZQSEP = " + hm.get("ZQSEP"));
			LogWriter.debugT(this, "ZQOCT = " + hm.get("ZQOCT"));
			LogWriter.debugT(this, "ZQNOV = " + hm.get("ZQNOV"));
			LogWriter.debugT(this, "ZQDEC = " + hm.get("ZQDEC"));
			
			
			
			
			
			tableRow.put("ZCYYYY"	, hm.get("ZCYYYY"));	// Year
			tableRow.put("KUNNR"	, CommonProperties.DEALER_PREFIX + strDealerCode);	// Customer Number
			// Dealer Name 은 저장항목에서 제외. 
			tableRow.put("ZCMC"		, hm.get("ZCMC"));		// MC
			tableRow.put("ZCCAR"	, hm.get("ZCCAR"));		// Model Code	
			tableRow.put("ZCCARX"	, hm.get("ZCCARX"));	// Description(Eng.)
			tableRow.put("ZCBYTE"	, hm.get("ZCBYTE"));	// Body Type
			tableRow.put("ZCBYTEX"	, hm.get("ZCBYTEX"));	// Description(Eng.)
			tableRow.put("ZCECAPA"	, hm.get("ZCECAPA"));	// Engine Capacity
			tableRow.put("ZCECAPAX"	, hm.get("ZCECAPAX"));	// Description(Eng.)
			tableRow.put("ZCETYPE"	, hm.get("ZCETYPE"));	// Engine Type
			tableRow.put("ZCETYPEX"	, hm.get("ZCETYPEX"));	// Description(Eng.)
			tableRow.put("ZCFTYPE"	, hm.get("ZCFTYPE"));	// Fuel Type
			tableRow.put("ZCFTYPEX"	, hm.get("ZCFTYPEX"));	// Description(Eng.)
			tableRow.put("ZCTRMS"	, hm.get("ZCTRMS"));	// Transmission
			tableRow.put("ZCTRMSX"	, hm.get("ZCTRMSX"));	// Description(Eng.)
			tableRow.put("ZQJAN"	, hm.get("ZQJAN"));		// January Quantity
			tableRow.put("ZQFEB"	, hm.get("ZQFEB"));		// February Quantity
			tableRow.put("ZQMAR"	, hm.get("ZQMAR"));		// March Quantity
			tableRow.put("ZQAPR"	, hm.get("ZQAPR"));		// April Quantity
			tableRow.put("ZQMAY"	, hm.get("ZQMAY"));		// May Quantity
			tableRow.put("ZQJUN"	, hm.get("ZQJUN"));		// June Quantity
			tableRow.put("ZQJUL"	, hm.get("ZQJUL"));		// July Quantity
			tableRow.put("ZQAUG"	, hm.get("ZQAUG"));		// August Quantity
			tableRow.put("ZQSEP"	, hm.get("ZQSEP"));		// September Quantity
			tableRow.put("ZQOCT"	, hm.get("ZQOCT"));		// October Quantity
			tableRow.put("ZQNOV"	, hm.get("ZQNOV"));		// November Quantity
			tableRow.put("ZQDEC"	, hm.get("ZQDEC"));		// December Quantity
			
			innerTable.add(tableRow);
		}

		inputTable.add(innerTable);
		
		
        try {
        	LogWriter.debugT(this, "multipartRequest start222");
        	
            //connection 생성
        	if(jca.getConnection() == null) createConnector();
            
            
			ArrayList outputTableNames = new ArrayList();
			outputTableNames.add(strListTable);

			Hashtable outputTableColumnInfo = new Hashtable();
			List tableColumns = new ArrayList();
			tableColumns.add("ZCYYYY");		// Year
			tableColumns.add("KUNNR");		// Dealer Code
			tableColumns.add("ZCMC");		// MC
			tableColumns.add("ZCCAR");		// Model Code
			tableColumns.add("ZCCARX");		// Description(Eng.)
			tableColumns.add("ZCBYTE");		// Body Type
			tableColumns.add("ZCBYTEX");	// Description(Eng.)
			tableColumns.add("ZCECAPA");	// Engine Capacity	
			tableColumns.add("ZCECAPAX");	// Description(Eng.)
			tableColumns.add("ZCETYPE");	// Engine Type
			tableColumns.add("ZCETYPEX");	// Description(Eng.)
			tableColumns.add("ZCFTYPE");	// Fuel Type
			tableColumns.add("ZCFTYPEX");	// Description(Eng.)
			tableColumns.add("ZCTRMS");		// Transmission
			tableColumns.add("ZCTRMSX");	// Description(Eng.)
			tableColumns.add("ZQJAN");		// January Quantity
			tableColumns.add("ZQFEB");		// February Quantity
			tableColumns.add("ZQMAR");		// March Quantity
			tableColumns.add("ZQAPR");		// April Quantity
			tableColumns.add("ZQMAY");		// May Quantity
			tableColumns.add("ZQJUN");		// June Quantity
			tableColumns.add("ZQJUL");		// July Quantity
			tableColumns.add("ZQAUG");		// August Quantity
			tableColumns.add("ZQSEP");		// September Quantity
			tableColumns.add("ZQOCT");		// October Quantity
			tableColumns.add("ZQNOV");		// November Quantity
			tableColumns.add("ZQDEC");		// December Quantity

			tableColumns.add("TYPE");		// Message Type
			tableColumns.add("MESSAGE");	// Message Text

			outputTableColumnInfo.put(strListTable, tableColumns);
            
            // rfc excute
            Hashtable result = jca.getRfcTableResult(request
            										,strRfcName
            										,input
            										,new Hashtable()
            										,inputTable
            										,inputTableName
            										,new ArrayList()
            										,new ArrayList()
            										,outputTableColumnInfo
            										,outputTableNames);
            
//            List resultList = (List)result.get(strListTable);

			List msgTable = (List)result.get("RETURN_TABLE");
			msgInfo = msgTable!=null && !msgTable.isEmpty() ? (Hashtable)msgTable.get(0) : new Hashtable();
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }
        
        return msgInfo;
    }
    
    
    
    /**
     * Monthly Forecast 조회값을 가져온다.<br>
     * monthlyForecast화면, YearlyForecast 컴포넌트에서 호출.
     * 
     * @param StrIPortalComponentRequest request		parameter
     * @param String strDealerCode 
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getMonthlyForecast(IPortalComponentRequest request, String strDealerCode)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
          
        // import parameters setting
        inputMap.put("I_YDMD", "M");
        inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + strDealerCode);
        
        if("excel".equals(StringUtil.checkNull(request.getParameter("excel"),""))){
        	inputMap.put("I_FLAG",	"");
        }else{
        	inputMap.put("I_FLAG",	"X");
        }
        
        String strMonth = StringUtil.checkNull(request.getParameter("month"), "");
        if(!"".equals(strMonth)){
        	if(Integer.parseInt(strMonth) < 10)
        		strMonth = "0" + strMonth;
        }
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("month"),"")))
			inputMap.put("I_ZYYYMM",    StringUtil.checkNull(request.getParameter("year")) 
									  + strMonth);
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
			inputMap.put("I_ZCCAR",	StringUtil.checkNull(request.getParameter("vehicle_model")));		// Model Code
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
			inputMap.put("I_ZCBYTE",	StringUtil.checkNull(request.getParameter("body_type")));		// Body Type
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("engine_capacity"),"")))
			inputMap.put("I_ZCECAPA",	StringUtil.checkNull(request.getParameter("engine_capacity")));	// Engine Capacity
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("engine_type"),"")))
			inputMap.put("I_ZCETYPE",	StringUtil.checkNull(request.getParameter("engine_type")));		// Engine Type
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("fuel_type"),"")))
			inputMap.put("I_ZCFTYPE",	StringUtil.checkNull(request.getParameter("fuel_type")));		// Fuel Type
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
			inputMap.put("I_ZCTRMS",	StringUtil.checkNull(request.getParameter("transmission")));	// Transmission
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
			inputMap.put("I_ZCTRMS",	StringUtil.checkNull(request.getParameter("transmission")));	// Transmission
		
        
        String strRfcName = "ZHBR_SD_DP_APS_FORECAST_LIST";  // rfc 함수명
        String strListTable = "T_MONTHLY";           // 리턴 테이블명

        try {
            //connection 생성
        	if(jca.getConnection() == null) createConnector();
            
            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }    
    
    
    
    /**
     * Monthly Forecast Save<br>
     * Monthly Forecast화면, MonthlyForecast 컴포넌트에서 호출.
     * 
     * @param StrIPortalComponentRequest request		parameter
     * @param String strDealerCode 
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public Hashtable setMonthlyForecast(IPortalComponentRequest request, ArrayList dataList, String strDealerCode)
        throws ConnectorException, ResourceException {
    	
    	Hashtable msgInfo = new Hashtable();
        ArrayList returnList = new ArrayList();
        Hashtable input = new Hashtable();
        ArrayList inputTableName = new ArrayList();
        ArrayList inputTable = new ArrayList();
		ArrayList innerTable = new ArrayList();
		String strMonth = StringUtil.checkNull(request.getParameter("month"), "");
        if(!"".equals(strMonth)){
        	if(Integer.parseInt(strMonth) < 10)
        		strMonth = "0" + strMonth;
        }
        
        // import parameters setting
        input.put("I_YDMD", "M");
        input.put("I_KUNNR", CommonProperties.DEALER_PREFIX + strDealerCode);
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("year"),""))
           && !"".equals(StringUtil.checkNull(request.getParameter("year"),"")))
			input.put("I_ZYYYMM",	StringUtil.checkNull(request.getParameter("year"))
								  + strMonth);
		
        
        String strRfcName = "ZHBR_SD_DP_APS_FORECAST_SAVE";  // rfc 함수명
        String strListTable = "T_MONTHLY";           // 리턴 테이블명
        inputTableName.add(strListTable);
        
        
        for(int i=0; i<dataList.size(); i++) {
			HashMap hm = (HashMap)dataList.get(i);
			Hashtable tableRow = new Hashtable();
			
			tableRow.put("ZYYYMM"	, hm.get("ZYYYMM"));	// Year
			tableRow.put("KUNNR"	, CommonProperties.DEALER_PREFIX + strDealerCode);	// Customer Number
			// Dealer Name 은 저장항목에서 제외. 
			tableRow.put("ZCFSC"	, hm.get("ZCFSC"));		// SALES_FSC
			tableRow.put("MAKTX"	, hm.get("MAKTX"));		// FSC Desc
			tableRow.put("ZCOCN"	, hm.get("ZCOCN"));		// OCN
			tableRow.put("ZCCAR"	, hm.get("ZCCAR"));		// Model Code	
			tableRow.put("ZCCARX"	, hm.get("ZCCARX"));	// Description(Eng.)
			tableRow.put("ZCBYTE"	, hm.get("ZCBYTE"));	// Body Type
			tableRow.put("ZCBYTEX"	, hm.get("ZCBYTEX"));	// Description(Eng.)
			tableRow.put("ZCECAPA"	, hm.get("ZCECAPA"));	// Engine Capacity
			tableRow.put("ZCECAPAX"	, hm.get("ZCECAPAX"));	// Description(Eng.)
			tableRow.put("ZCETYPE"	, hm.get("ZCETYPE"));	// Engine Type
			tableRow.put("ZCETYPEX"	, hm.get("ZCETYPEX"));	// Description(Eng.)
			tableRow.put("ZCFTYPE"	, hm.get("ZCFTYPE"));	// Fuel Type
			tableRow.put("ZCFTYPEX"	, hm.get("ZCFTYPEX"));	// Description(Eng.)
			tableRow.put("ZCTRMS"	, hm.get("ZCTRMS"));	// Transmission
			tableRow.put("ZCTRMSX"	, hm.get("ZCTRMSX"));	// Description(Eng.)
			tableRow.put("ZCEXTC"	, hm.get("ZCEXTC"));	// External Color
			tableRow.put("ZCINTC"	, hm.get("ZCINTC"));	// Internal Color
			tableRow.put("ZQDEQTY"	, hm.get("ZQDEQTY"));	// Dealer Quantity
			tableRow.put("MATNR"	, hm.get("MATNR"));		// Material Number
			
			innerTable.add(tableRow);
		}

		inputTable.add(innerTable);
		
		
        try {
        	LogWriter.debugT(this, "multipartRequest start222");
        	
            //connection 생성
        	if(jca.getConnection() == null) createConnector();
            
            
			ArrayList outputTableNames = new ArrayList();
			outputTableNames.add(strListTable);

			Hashtable outputTableColumnInfo = new Hashtable();
			List tableColumns = new ArrayList();
			tableColumns.add("ZYYYMM");		// Year
			tableColumns.add("KUNNR");		// Dealer Code
			tableColumns.add("ZCFSC");		// Sales FSC
			tableColumns.add("MAKTX");		// FSC Desc
			tableColumns.add("ZCOCN");		// OCN
			tableColumns.add("ZCCAR");		// Model Code
			tableColumns.add("ZCCARX");		// Description(Eng.)
			tableColumns.add("ZCBYTE");		// Body Type
			tableColumns.add("ZCBYTEX");	// Description(Eng.)
			tableColumns.add("ZCECAPA");	// Engine Capacity	
			tableColumns.add("ZCECAPAX");	// Description(Eng.)
			tableColumns.add("ZCETYPE");	// Engine Type
			tableColumns.add("ZCETYPEX");	// Description(Eng.)
			tableColumns.add("ZCFTYPE");	// Fuel Type
			tableColumns.add("ZCFTYPEX");	// Description(Eng.)
			tableColumns.add("ZCTRMS");		// Transmission
			tableColumns.add("ZCTRMSX");	// Description(Eng.)
			tableColumns.add("ZCEXTC");		// External Color
			tableColumns.add("ZCINTC");		// Internal Color
			tableColumns.add("ZQDEQTY");	// Dealer Quantity
			tableColumns.add("MATNR");		// Material Number
			
			tableColumns.add("TYPE");		// Message Type
			tableColumns.add("MESSAGE");	// Message Text

			outputTableColumnInfo.put(strListTable, tableColumns);
            
            // rfc excute
            Hashtable result = jca.getRfcTableResult(request
            										,strRfcName
            										,input
            										,new Hashtable()
            										,inputTable
            										,inputTableName
            										,new ArrayList()
            										,new ArrayList()
            										,outputTableColumnInfo
            										,outputTableNames);
            
//            List resultList = (List)result.get(strListTable);

			List msgTable = (List)result.get("RETURN_TABLE");
			msgInfo = msgTable!=null && !msgTable.isEmpty() ? (Hashtable)msgTable.get(0) : new Hashtable();
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }
        
        return msgInfo;
    }
    
    
    
	/**
	 * Dealer Stock 조회화면에서 보여준다.<br>
	 * KeepVehicleTransfer 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @param dealerCode 딜러코드
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList setKeepVehicleTransfer(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();

		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
		inputMap.put("I_CHARG", StringUtil.checkNull(request.getParameter("p_dsn")));
		inputMap.put("I_VHVIN", StringUtil.checkNull(request.getParameter("p_vin")));
		inputMap.put("I_ZKPRL", StringUtil.checkNull(request.getParameter("p_status")));
        
		String strRfcName = "ZHAU_DP_DEALER_KEEP_VEH";  // rfc 함수명
		String strListTable = "T_RETURN";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
    
    
    
            
    /**
     * Other Dealer Stock 조회화면에서 보여준다.<br>
     * OtherDealerStock 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList setRequestForTransfer(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();

        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_RQ_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_RQ_DPUSER", request.getUser().getLogonUid());
        inputMap.put("I_RE_KUNNR", StringUtil.checkNull(request.getParameter("p_dealerCode")));
        inputMap.put("I_RE_VIN", StringUtil.checkNull(request.getParameter("p_vin")));
        inputMap.put("I_SHIP_TO", StringUtil.checkNull(request.getParameter("p_shipto")));
        
        String strRfcName = "ZHAU_DP_TRANSFER_REQUEST";  // rfc 함수명
        String strListTable = "T_RETURN";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    /**
     * Normal order 생성 전 예약.<br>
     * Reservation 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap setReservation(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
		String strWmeng = "";
		
		if("N1".equals((String)request.getParameter("sel_vehicleUsage"))){
			strWmeng = request.getParameter("WMENG");
		}else{
			strWmeng = request.getParameter("WMENG2");
		}
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_MATNR", StringUtil.checkNull(request.getParameter("p_fsc")));
        inputMap.put("I_LGORT", StringUtil.checkNull(request.getParameter("p_whs")));
        inputMap.put("I_ZZEXC", StringUtil.checkNull(request.getParameter("p_extColorCode")));
        inputMap.put("I_ZZINC", StringUtil.checkNull(request.getParameter("p_intColorCode")));
		inputMap.put("I_WMENG", strWmeng);
//        inputMap.put("I_ZZMDYEAR", StringUtil.checkNull(request.getParameter("p_mygrp")));
        
        String strRfcName = "ZHAU_DP_CREATE_RESERVATION";  // rfc 함수명
        //String strListTable = "T_LIST";              // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }

    /**
     * 요청받은 transfer에 대한 승인/거절 <br>
     * VehicleTransferAcceptTrans 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList setTransferAccept(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {

        HttpServletRequest servletRequest = request.getServletRequest();

        ArrayList returnList = new ArrayList();
        ArrayList inputList = new ArrayList();
        HashMap inputMap = new HashMap();

        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_RE_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_RE_DPUSER", request.getUser().getLogonUid());
        inputMap.put("I_RE_VIN", StringUtil.checkNull(request.getParameter("vin")));
        inputMap.put("I_RE_OK", StringUtil.checkNull(request.getParameter("RE_OK")));
        inputMap.put("I_REASON", StringUtil.checkNull(request.getParameter("p_reason")));
        
        // import parameters table setting
        String[] VHBIN = servletRequest.getParameterValues("VHVIN");
        String[] RQ_KUNNR = servletRequest.getParameterValues("RQ_KUNNR");
        String[] RE_KUNNR = servletRequest.getParameterValues("RE_KUNNR");
        String[] RQ_VER = servletRequest.getParameterValues("RQ_VER");
        String[] REQSTS = servletRequest.getParameterValues("REQSTS");
        String[] RQ_KUNWE = servletRequest.getParameterValues("RQ_KUNWE");

        for( int i=0 ; i<VHBIN.length ; i++){
            HashMap tmpMap = new HashMap();
            tmpMap.put("VHVIN",    VHBIN[i]);
            tmpMap.put("RQ_KUNNR", RQ_KUNNR[i]);
            tmpMap.put("RE_KUNNR", RE_KUNNR[i]);
            tmpMap.put("RQ_VER",   RQ_VER[i]);
            tmpMap.put("REQSTS",   REQSTS[i]);
            tmpMap.put("RQ_KUNWE", RQ_KUNWE[i]);
            
            inputList.add(tmpMap);
        }
        
        
        String strRfcName = "ZHAU_DP_TRANSFER_ACCEPT";  // rfc 함수명
        String strListTable = "T_RETURN";               // 리턴 테이블명
        String importTable  = "T_LIST";                 // 입력 테이블명
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap, importTable, inputList), strListTable);

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    

    /**
     * vfos 오더 생성
     * VfosOrderBySpecTrans 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap setVfosOrder(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {

        HttpServletRequest servletRequest = request.getServletRequest();
        HashMap returnMap = new HashMap();
        ArrayList inputList = new ArrayList();
        HashMap inputMap = new HashMap();

        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        //inputMap.put("I_DEALER", "A02VE82142");
        inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
        inputMap.put("I_EPUSER", request.getUser().getLogonUid());
        inputMap.put("I_SHIPTO", StringUtil.checkNull(servletRequest.getParameter("shipto")));
        inputMap.put("I_MATNR", StringUtil.checkNull(servletRequest.getParameter("fsc")));
        inputMap.put("I_MONTH", StringUtil.checkNull(servletRequest.getParameter("month")));
		inputMap.put("I_LGORT", StringUtil.checkNull(servletRequest.getParameter("lgort")));
        
        // import parameters table setting
        String[] p_extcolor = servletRequest.getParameterValues("p_extcolor");
        String[] p_intcolor = servletRequest.getParameterValues("p_intcolor");
        String[] p_quantity = servletRequest.getParameterValues("p_quantity");

        for( int i=0 ; i<p_quantity.length ; i++){
            HashMap tmpMap = new HashMap();
            tmpMap.put("WMENG",    p_quantity[i]);
            tmpMap.put("MATNR", servletRequest.getParameter("fsc"));
            tmpMap.put("EXT_COLOR_IN", p_extcolor[i]);
            tmpMap.put("INT_COLOR_IN", p_intcolor[i]);
            
            inputList.add(tmpMap);
        }
         
        
        String strRfcName = "ZHAU_DP_QUOTA_SAVE";  // rfc 함수명
        String importTable  = "T_LIST";                 // 입력 테이블명
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap, importTable, inputList));

        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    private HashMap paramSettingRdaInfo(IPortalComponentRequest request, String strDealerCode){
    	
    	//int nfe_num = Integer.parseInt(request.getParameter("nfe_num"));
    	//int nfe_serial = Integer.parseInt(request.getParameter("nfe_serial"));
    	
        HashMap I_SECONDARY_BP = new HashMap();
        
        
        I_SECONDARY_BP.put("STRUCTURE_NAME", "I_RETAIL");
        
        I_SECONDARY_BP.put("ZCMODL", request.getParameter("fsc").substring(0, 2));	// to do list
//        I_SECONDARY_BP.put("ZSSERIAL", "123456");	// to do list
        if(!"".equals(StringUtil.checkNull(request.getParameter("personal_partner"))))
        	I_SECONDARY_BP.put("ZCEQCUST", request.getParameter("personal_partner"));
        
        I_SECONDARY_BP.put("KUNWE", CommonProperties.DEALER_PREFIX + strDealerCode);
        I_SECONDARY_BP.put("KUNNR", CommonProperties.DEALER_PREFIX + strDealerCode);
        
        I_SECONDARY_BP.put("ZCRETYNO", StringUtil.checkNull(request.getParameter("salesMan")));	//Salesman
        I_SECONDARY_BP.put("ZCCUNO", StringUtil.checkNull(request.getParameter("plate_no")));		//Plate No
        I_SECONDARY_BP.put("ZCVIN", StringUtil.checkNull(request.getParameter("vin")));
        I_SECONDARY_BP.put("ZCRETYTP", StringUtil.checkNull(request.getParameter("registration_type")));
        I_SECONDARY_BP.put("ZCLEADNO", StringUtil.checkNull(request.getParameter("lead_no")));		//Lead No
        I_SECONDARY_BP.put("ZDRSALE", DateUtil.getSysDate(1));
        I_SECONDARY_BP.put("IMMATDATE", DateUtil.getSysDate(1));
        
        I_SECONDARY_BP.put("ZCTRDCAR", StringUtil.checkNull(request.getParameter("carName")));		//Car Name
        I_SECONDARY_BP.put("ZCTRDPRI", StringUtil.checkNull(request.getParameter("carValue")));		//Car Value
        
        /*I_RETAIL NEW FIELDS 02/08/2013 TO THE NF-E IMPLEMENTATION*/
        I_SECONDARY_BP.put("ZCNFNUM", StringUtil.checkNull(request.getParameter("nfe_num"))); //Numero da Nota.
        I_SECONDARY_BP.put("ZCSERIE", StringUtil.checkNull(request.getParameter("nfe_serial"))); //Numero da Série.
        
        LogWriter.debugT(this, "ZCRETYNO (Sales Man)====> " +  StringUtil.checkNull(request.getParameter("salesMan")));
        LogWriter.debugT(this, "ZCCUNO (Plate No)====> " + StringUtil.checkNull(request.getParameter("plate_no")));
        
//        I_SECONDARY_BP.put("ZCCUNO", StringUtil.checkNull(request.getParameter("rego_no")));
        
        return I_SECONDARY_BP;
    }
    
    private HashMap paramSettingPrimary(IPortalComponentRequest request){
        HashMap I_PRIMARY_BP = new HashMap();

        I_PRIMARY_BP.put("STRUCTURE_NAME", "I_PARTNER");
        I_PRIMARY_BP.put("TYPE", StringUtil.checkNull(request.getParameter("customerType")));
        I_PRIMARY_BP.put("COUNTRY", "BR");
        
//        LogWriter.debugT(this, "personal_partner = " + StringUtil.checkNull(request.getParameter("personal_partner")));
        I_PRIMARY_BP.put("PARTNER", StringUtil.checkNull(request.getParameter("personal_partner")));
        I_PRIMARY_BP.put("TAXNUM", StringUtil.checkNull(request.getParameter("cpf")));
        I_PRIMARY_BP.put("XSEXM", StringUtil.checkNull(request.getParameter("personal_gender")));
        I_PRIMARY_BP.put("POST_CODE1", StringUtil.checkNull(request.getParameter("address_post")));
        I_PRIMARY_BP.put("PARTNER", StringUtil.checkNull(request.getParameter("personal_partner")));
//        I_PRIMARY_BP.put("TITLE", StringUtil.checkNull(request.getParameter("personal_title")));
        I_PRIMARY_BP.put("NAME_FIRST", StringUtil.checkNull(URLDecoder.decode(StringUtil.checkNull(request.getParameter("personal_firstName")))));
        I_PRIMARY_BP.put("NAME_LAST", StringUtil.checkNull(URLDecoder.decode(StringUtil.checkNull(request.getParameter("personal_lastName")))));
        I_PRIMARY_BP.put("MARST", StringUtil.checkNull(request.getParameter("personal_marital")));
        I_PRIMARY_BP.put("BIRTHDATE", DateUtil.getChangeDate(request.getParameter("personal_birthDate"),"/"));
        I_PRIMARY_BP.put("JOBGR", StringUtil.checkNull(request.getParameter("personal_occupation")));
        I_PRIMARY_BP.put("SMTP_ADDR", StringUtil.checkNull(request.getParameter("personal_email")));
        
        I_PRIMARY_BP.put("STREET", StringUtil.checkNull(request.getParameter("address_street1")));
        I_PRIMARY_BP.put("CITY1", StringUtil.checkNull(request.getParameter("address_city")));
        I_PRIMARY_BP.put("CHILDREN", StringUtil.checkNull(request.getParameter("address_children")));
        I_PRIMARY_BP.put("STR_SUPPL3", StringUtil.checkNull(request.getParameter("address_suburb")));
        I_PRIMARY_BP.put("REGION", StringUtil.checkNull(request.getParameter("address_state")));
        //I_PRIMARY_BP.put("TEL_NUMBER", StringUtil.checkNull(request.getParameter("address_homePhone")));
        I_PRIMARY_BP.put("TEL_NUMBER", StringUtil.checkNull(request.getParameter("dp_ddd1")+request.getParameter("dp_phone1")));
        //I_PRIMARY_BP.put("OFF_NUMBER", StringUtil.checkNull(request.getParameter("address_officePhone")));
        I_PRIMARY_BP.put("OFF_NUMBER", StringUtil.checkNull(request.getParameter("dp_ddd2")+request.getParameter("dp_phone2")));
        //I_PRIMARY_BP.put("MOB_NUMBER", StringUtil.checkNull(request.getParameter("address_mobilePhone")));
        if("011".equals(request.getParameter("dp_ddd3"))){
        I_PRIMARY_BP.put("MOB_NUMBER", StringUtil.checkNull(request.getParameter("dp_ddd3")+"9"+request.getParameter("dp_phone3")));
        }
        else
        {
        I_PRIMARY_BP.put("MOB_NUMBER", StringUtil.checkNull(request.getParameter("dp_ddd3")+request.getParameter("dp_phone3")));
        }
        I_PRIMARY_BP.put("AUGRP", StringUtil.checkNull(request.getParameter("address_paymentType")));
        I_PRIMARY_BP.put("PAYMAIL", StringUtil.checkNull(request.getParameter("address_promotion")));
        I_PRIMARY_BP.put("PAYMONTH", StringUtil.checkNull(request.getParameter("address_finance")));
        
        return I_PRIMARY_BP;
    }
    
//    private HashMap paramSettingSecond(IPortalComponentRequest request){
//        HashMap I_SECONDARY_BP = new HashMap();
//        
//        I_SECONDARY_BP.put("STRUCTURE_NAME", "I_SECONDARY_BP");
//        I_SECONDARY_BP.put("PARTNER", StringUtil.checkNull(request.getParameter("add_partner")));
//        I_SECONDARY_BP.put("RELTYP", StringUtil.checkNull(request.getParameter("add_relation")));
//        I_SECONDARY_BP.put("TITLE", StringUtil.checkNull(request.getParameter("add_title")));
//        I_SECONDARY_BP.put("NAME_FIRST", StringUtil.checkNull(URLDecoder.decode(StringUtil.checkNull(request.getParameter("add_firstName")))));
//        I_SECONDARY_BP.put("NAME_LAST", StringUtil.checkNull(URLDecoder.decode(StringUtil.checkNull(request.getParameter("add_lastName")))));
//
//		//LogWriter.debugT(this,StringUtil.checkNull(	URLDecoder.decode(request.getParameter("add_firstName"))));
//		//LogWriter.debugT(this,StringUtil.checkNull(	URLDecoder.decode(request.getParameter("add_lastName"))));
//		
//        return I_SECONDARY_BP;
//    }


    private HashMap paramSettingCompany(IPortalComponentRequest request){
        HashMap I_COMPANY_BP = new HashMap();
        
        I_COMPANY_BP.put("STRUCTURE_NAME", "I_PARTNER");
//        I_COMPANY_BP.put("TITLE", "Z100");
        I_COMPANY_BP.put("COUNTRY", "BR");
//        LogWriter.debugT(this, "company_partner = " + StringUtil.checkNull(request.getParameter("company_partner")));
        I_COMPANY_BP.put("PARTNER", StringUtil.checkNull(request.getParameter("company_partner")));
        if(StringUtil.checkNull(request.getParameter("company_abn")).length() == 13)
        {
        	I_COMPANY_BP.put("TAXNUM", "0"+StringUtil.checkNull(request.getParameter("company_abn")));
        }
        else
        {
        	I_COMPANY_BP.put("TAXNUM", StringUtil.checkNull(request.getParameter("company_abn")));
        }
        I_COMPANY_BP.put("TYPE", StringUtil.checkNull(request.getParameter("customerType")));
        I_COMPANY_BP.put("PARTNER", StringUtil.checkNull(request.getParameter("company_partner")));
        I_COMPANY_BP.put("NAME_FIRST", StringUtil.checkNull(URLDecoder.decode(StringUtil.checkNull(request.getParameter("company_name")))));
        I_COMPANY_BP.put("STREET", StringUtil.checkNull(request.getParameter("company_street1")));
        I_COMPANY_BP.put("CITY1", StringUtil.checkNull(request.getParameter("company_city")));
//        I_COMPANY_BP.put("BU_SORT1", StringUtil.checkNull(request.getParameter("company_abn")));
        I_COMPANY_BP.put("NAME_LAST", StringUtil.checkNull(request.getParameter("company_represent")));
        I_COMPANY_BP.put("STR_SUPPL3", StringUtil.checkNull(request.getParameter("company_suburb")));
        I_COMPANY_BP.put("REGION", StringUtil.checkNull(request.getParameter("company_state")));
        I_COMPANY_BP.put("SMTP_ADDR", StringUtil.checkNull(request.getParameter("company_email")));
        I_COMPANY_BP.put("TEL_NUMBER", StringUtil.checkNull(request.getParameter("company_telephone")));
        I_COMPANY_BP.put("POST_CODE1", StringUtil.checkNull(request.getParameter("company_post")));
        I_COMPANY_BP.put("FAX_NUMBER", StringUtil.checkNull(request.getParameter("company_fax")));
        
        return I_COMPANY_BP;
    }


	public ArrayList getMiscellaneousCondition(String I_TYPE1, String I_TYPE2)
		throws ConnectorException, ResourceException {

		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_TYPE1", I_TYPE1);
		inputMap.put("I_TYPE2", I_TYPE2);

		String strRfcName = "ZHAU_DP_DESC_COMMON";     // rfc 함수명
		String strListTable = "T_LIST";            // 리턴 테이블명

		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");            
 

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	} 


	public ArrayList getUsedcarList(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
			inputMap.put("I_CLINE", StringUtil.checkNull(request.getParameter("vehicle_model")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
			inputMap.put("I_BDTYP", StringUtil.checkNull(request.getParameter("body_type")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("eng_capa"),"")))
			inputMap.put("I_ENGCP", StringUtil.checkNull(request.getParameter("eng_capa")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("tm_type"),"")))
			inputMap.put("I_TMTYP", StringUtil.checkNull(request.getParameter("tm_type")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("ex_color"),"")))
			inputMap.put("I_EXCOC", StringUtil.checkNull(request.getParameter("ex_color")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("region"),"")))	
			inputMap.put("I_BZIRK", StringUtil.checkNull(request.getParameter("region")));
		
		String strRfcName = "ZHAU_DP_UCAR_LIST";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
            
 

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}

	/*   
	 * Screen : Sales > Sales > Sales & Stock Report
	 * RFC name : ZHAU_DP_LIST_REPORT1
	 */
	public ArrayList getSSReport(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting (결과값을 받기 위해 Search parameter를 넘겨주는 부분)
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		inputMap.put("I_DATE_FROM", DateUtil.getChangeDate(request.getParameter("ws_from_date"),"/")); // from-date of retail
		inputMap.put("I_DATE_TO", DateUtil.getChangeDate(request.getParameter("ws_to_date"),"/")); // to-date of retail
//		inputMap.put("I_RT_FDATE", DateUtil.getChangeDate(request.getParameter("rt_from_date"),"/")); // from-date of retail
//		inputMap.put("I_RT_TDATE", DateUtil.getChangeDate(request.getParameter("rt_to_date"),"/")); // to-date of retail
		
		//from here new filters 02/04/2013
		if(!"".equals(StringUtil.checkNull(request.getParameter("cpf"),"")))
			inputMap.put("I_CPF", StringUtil.checkNull(request.getParameter("cpf")));

		if(!"".equals(StringUtil.checkNull(request.getParameter("vin"),"")))
			inputMap.put("I_ZCVIN", StringUtil.checkNull(request.getParameter("vin")));

		if(!"".equals(StringUtil.checkNull(request.getParameter("status"),"")))
			inputMap.put("I_VLC_STATU", StringUtil.checkNull(request.getParameter("status")));

		if(!"".equals(StringUtil.checkNull(request.getParameter("nfe_nro"),"")))
			inputMap.put("I_NFENUM", StringUtil.checkNull(request.getParameter("nfe_nro")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("retail_date"),"")))
			inputMap.put("I_IMMATDATE", DateUtil.getChangeDate(request.getParameter("retail_date"),"/"));
		//new filters ends here.
		
		String strRfcName = "ZHBR_SD_DP_GET_STKNRETAIL";  // rfc 함수명
		String strListTable = "T_LIST";            // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	
	/*   
	 * Screen : Sales > Sales > Retail Sales Report
	 * RFC name : ZHAU_DP_LIST_RETAIL
	 * Author : SOOYEON
	 */
	public ArrayList getRSReport(IPortalComponentRequest request, String dealerCode)
			throws ConnectorException, ResourceException {
            
			ArrayList returnList = new ArrayList();
			HashMap inputMap = new HashMap();
        	
//        	String strPlateNo = StringUtil.checkNull(request.getParameter("rego_no"),"");
        	
			// import parameters setting (결과값을 받기 위해 Search parameter를 넘겨주는 부분)
			inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("RDAType"),"")))
				inputMap.put("I_ZCRETYTP", StringUtil.checkNull(request.getParameter("RDAType")));
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("order_type"),"")))
				inputMap.put("I_AUART", StringUtil.checkNull(request.getParameter("order_type")));
			//I_PSTYV modificado para I_AUART
			inputMap.put("I_FDATE", DateUtil.getChangeDate(request.getParameter("rt_from_date"),"/")); // from-date of retail
			inputMap.put("I_TDATE", DateUtil.getChangeDate(request.getParameter("rt_to_date"),"/")); // to-date of retail
			
			
//			}
			
			LogWriter.debugT(this, "I_KUNNR :" + inputMap.get("I_KUNNR"));
			

			String strRfcName = "ZHBR_SD_DP_LIST_RETAIL";  // rfc 함수명
			String strListTable = "T_LIST";            // 리턴 테이블명

			try {

				//connection 생성
				if(jca.getConnection() == null) createConnector();

				// rfc excute
				returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
				
				/*
				for(int i=0;i<returnList.size();i++){
								   HashMap element = (HashMap)returnList.get(i);
								   LogWriter.debugT(this, "ZZVFACTS_T : " + element.get("ZZVFACTS_T"));
					               //LogWriter.debugT(this, "ZZVFACTS : " + element.get("ZZVFACTS"));
				}
            */
				//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
			} catch (Exception e) {
				LogWriter.errorT(this, "Exception: " + e.getMessage());
			} finally {
				closeConnector();
			}

			return returnList;
   }
   

       /*   
		* Screen : Sales > Sales > Normal Order
		* RFC name : ZHAU_DP_GET_ETA
		* Author : SOOYEON
		*/
	   public ArrayList getETADate(IPortalComponentRequest request, String dealerCode)
			   throws ConnectorException, ResourceException {
            
			   ArrayList returnList = new ArrayList();
			   HashMap inputMap = new HashMap();
        
			   // import parameters setting (결과값을 받기 위해 Search parameter를 넘겨주는 부분)
			   inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
			   inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code	   
			   inputMap.put("I_MATNR", StringUtil.checkNull(request.getParameter("p_fsc"))); //FSC
			   inputMap.put("I_LGORT", StringUtil.checkNull(request.getParameter("p_whfCode"))); //Storage Loc
			   inputMap.put("I_ZZEXC", StringUtil.checkNull(request.getParameter("p_extColorCode"))); //Ext. Color
			   inputMap.put("I_ZZINC", StringUtil.checkNull(request.getParameter("p_intColorCode"))); //Int. Color
			   inputMap.put("I_ZZMDYEAR", DateUtil.getChangeDate(request.getParameter("p_mygrp"),"/")); // model year group-optional

			   String strRfcName = "ZHAU_DP_GET_ETA";           // rfc 함수명
			   String strListTable = "T_SO_DET_TAB";            // 리턴 테이블명
        
			   try {

				   //connection 생성
				   if(jca.getConnection() == null) createConnector();

				   // rfc excute
				   returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
				   //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
			   } catch (Exception e) {
				   LogWriter.errorT(this, "Exception: " + e.getMessage());
			   } finally {
				   closeConnector();
			   }

			   return returnList;
	  }
	  
	  
	/*  
	  * Screen : Sals > Stock > Transfer Request 의 Search condition "State for dealer"
	  * RFC name : ZHAU_DP_LIST_REGION
	  * Author : SOOYEON
	  */
	 public ArrayList getListOfRegion()
			 throws ConnectorException, ResourceException {
            
			 ArrayList returnList = new ArrayList();
			 HashMap inputMap = new HashMap();
        
			 // import parameters setting (결과값을 받기 위해 Search parameter를 넘겨주는 부분)
			 inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code

			 String strRfcName = "ZHAU_DP_LIST_REGION";           // rfc 함수명
			 String strListTable = "T_LIST";                      // 리턴 테이블명
        
			 try {

				 //connection 생성
				 if(jca.getConnection() == null) createConnector();

				 // rfc excute
				 returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
				 //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
			 } catch (Exception e) {
				 LogWriter.errorT(this, "Exception: " + e.getMessage());
			 } finally {
				 closeConnector();
			 }

			 return returnList;
	}
	
	/*  
	  * Screen : Sals > Order > Normal Order에서 back order 시에 호출
	  * RFC name : ZHAU_DP_CHECK_BO
	  * Author : SOOYEON
	  */	
	public Object getOldFSCCheck(IPortalComponentRequest request)
		  throws ConnectorException, ResourceException {
            
		  HashMap returnMap = new HashMap();
		  HashMap inputMap = new HashMap();
        
		  // import parameters setting
		  inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE );//CommonProperties.COMPANY_CODE
		  inputMap.put("I_MATNR", StringUtil.checkNull(request.getParameter("p_fsc")));

		  String strRfcName = "ZHAU_DP_CHECK_BO";  // rfc 함수명

		  try {

			 //connection 생성
			 if(jca.getConnection() == null) createConnector();

             /*
			 LogWriter.debugT(this, "returnMap: start~ ");
			 LogWriter.debugT(this, "I_BURKS : " + inputMap.get("I_BURKS"));
			 LogWriter.debugT(this, "I_MATNR : " + inputMap.get("I_MATNR"));
             */
			 // rfc excute
			 returnMap = jca.getRfcDataScalar(jca.executeFunction(strRfcName, inputMap));
			 
			 //LogWriter.debugT(this, "size of returnMap : " + returnMap.size());
			 LogWriter.debugT(this, "E_MATNR Return Value : " + (String)returnMap.get("E_MATNR"));
			 //LogWriter.debugT(this, "E_MATNR yes? : " + returnMap.containsKey("E_MATNR")) ;
			 //LogWriter.debugT(this, "returnMap value size : " + returnMap.values().size()) ;
			 //LogWriter.debugT(this, "class of E_MATNR : " + returnMap.get("E_MATNR").getClass());


		 } catch (Exception e) {
			 LogWriter.errorT(this, "Exception: " + e.getMessage());
		 } finally {
			 closeConnector();
		 }

		 return returnMap.get("E_MATNR");

	  }
	  
	  
	/*  
	  * Screen : For Test
	  * RFC name : ZHAU_DP_TEST
	  * Author : SOOYEON
	  */	
	public HashMap getOldFSCCheck2(IPortalComponentRequest request)
		  throws ConnectorException, ResourceException {
            
			HashMap returnMap = new HashMap();
  			HashMap inputMap = new HashMap();
        
  			// import parameters setting
  			inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
  			inputMap.put("I_MATNR", StringUtil.checkNull(request.getParameter("p_fsc")));
  			
  			String strRfcName = "ZHAU_DP_TEST";  // rfc 함수명

  			try {
	 			 //connection 생성
	  			if(jca.getConnection() == null) createConnector();

	 		 	// rfc excute
	 			 returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
 			 } catch (Exception e) {
				  LogWriter.errorT(this, "Exception: " + e.getMessage());
  			} finally {
	 			  closeConnector();
  			}

  			return returnMap;
	  }


	/**
	 * Sales By Model 검색<br>
	 * Sales By Model 화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
	 * Sales By Model 화면, SalesByModel 컴포넌트에서 호출.
	 * 
	 * @param request 전달 변수를 담은 객체
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getSalesByModel(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		inputMap.put("I_ZCCAR"		, StringUtil.checkNull(request.getParameter("vehicle_model")));
        inputMap.put("I_ZCYYYY"		, StringUtil.checkNull(request.getParameter("year")));
		inputMap.put("I_STYPE"		, StringUtil.checkNull(request.getParameter("sales_type")));
		inputMap.put("I_ACTIVITY"	, "M");
		
		String strRfcName = "ZHBR_SD_DP_RESULT_SALES";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	

	/**
	 * Sales Info Model 검색<br>
	 * Sales By Model 화면에서 데이터 클릭 시 실행된다.<br>
	 * Sales Info Model 화면, SalesInfoModel 컴포넌트에서 호출.
	 * 
	 * @param request 전달 변수를 담은 객체
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getSalesInfoModel(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_model"),"")))
			inputMap.put("I_ZCCAR"		, StringUtil.checkNull(request.getParameter("p_model")));
			
		inputMap.put("I_ZYYYMM"		, StringUtil.checkNull(request.getParameter("p_year")) + StringUtil.checkNull(request.getParameter("p_month")));
		inputMap.put("I_STYPE"		, StringUtil.checkNull(request.getParameter("p_sales_type")));
		inputMap.put("I_ACTIVITY"	, "M");
		
		String strRfcName = "ZHBR_SD_DP_DETAIL_VIN";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}


	/**
	 * salesMan 조건값을 가져온다.<br>
	 * SalesBySalesman 컴포넌트에서 호출.
	 * 
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getSalesManCondition(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {

		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + dealerCode);
		
		String strRfcName = "ZHBR_SD_DP_SEARCH_SALESMAN"; // rfc 함수명
		String strListTable = "T_LIST";           // 리턴 테이블명

		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
            
			// rfc 실행
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	

	/**
	 * Sales By Salesman 검색<br>
	 * Sales By Salesman 화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
	 * Sales By Salesman 화면, SalesBySalesman 컴포넌트에서 호출.
	 * 
	 * @param request 전달 변수를 담은 객체
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getSalesBySalesman(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		inputMap.put("I_ZCYYYY"		, StringUtil.checkNull(request.getParameter("year")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("salesMan"),"")))
			inputMap.put("I_PARNR"		, StringUtil.checkNull(request.getParameter("salesMan")));
		
		inputMap.put("I_STYPE"		, "R");
		inputMap.put("I_ACTIVITY"	, "S");
		
		String strRfcName = "ZHBR_SD_DP_RESULT_SALES";  // rfc 함수명
		String strListTable = "T_LIST";              	// 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}


	/**
	 * Sales Info Salesman 검색<br>
	 * Sales By Salesman 화면에서 데이터 클릭 시 실행된다.<br>
	 * Sales Info Salesman 화면, SalesInfoSalesman 컴포넌트에서 호출.
	 * 
	 * @param request 전달 변수를 담은 객체
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getSalesInfoSalesman(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("p_model"),"")))
			inputMap.put("I_PARNR"		, StringUtil.checkNull(request.getParameter("p_model").toUpperCase()));
			
		inputMap.put("I_ZYYYMM"		, StringUtil.checkNull(request.getParameter("p_year")) + StringUtil.checkNull(request.getParameter("p_month")));
		inputMap.put("I_STYPE"		, "R");
		inputMap.put("I_ACTIVITY"	, "S");
		
		String strRfcName = "ZHBR_SD_DP_DETAIL_VIN";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
		
////////////////////////////////////////////////////////////////////////////////////////////////////////
//						시승차 관리 RFC START	
////////////////////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Holiday 검색<br>
	 * Holiday Designation 화면에서 조회 시 실행된다.<br>
	 * Holiday Designation 화면, HolidayDesignation 컴포넌트에서 호출.
	 * 
	 * @param request 전달 변수를 담은 객체
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public HashMap getHolidayList(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		HashMap returnMap = new HashMap();
		HashMap inputMap = new HashMap();
        String strMonth = "";
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		
			inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		

		
		if(!"".equals(StringUtil.checkNull(request.getParameter("year"))))
			inputMap.put("I_ZHYEAR"		, StringUtil.checkNull(request.getParameter("year")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("month")))){
			strMonth = StringUtil.checkNull(request.getParameter("month"));
			if( Integer.parseInt(strMonth) < 10)
				strMonth = "0" + strMonth;
			inputMap.put("I_ZHMON"		, strMonth);
		}
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("date"))))
			inputMap.put("I_ZDATE"		, DateUtil.getChangeDate(request.getParameter("date"),"/"));
		
		inputMap.put("I_LANGU"		, request.getLocale().toString().toUpperCase());
		
		
		String strRfcName = "ZHBR_SD_DP_LIST_HOLIDAY_DAY";  // rfc 함수명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
			
            
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap;
	}
	
    /**
     * Holiday Save .<br>
     * HolidayTrans 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  HashMap returnMap return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap setSaveHoliday(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
        
    	HttpServletRequest servletRequest = request.getServletRequest();
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        ArrayList inputStructureList = new ArrayList();
		ArrayList inputList = new ArrayList();
		String strMonth = "";
		
        // import parameters setting
        inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);

       
        			inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
        	

        
        if(!"".equals(StringUtil.checkNull(request.getParameter("p_year"))))
			inputMap.put("I_ZHYEAR"		, StringUtil.checkNull(request.getParameter("p_year")));
		
        if(!"".equals(StringUtil.checkNull(request.getParameter("p_month")))){
			strMonth = StringUtil.checkNull(request.getParameter("p_month"));
			if( Integer.parseInt(strMonth) < 10)
				strMonth = "0" + strMonth;
			inputMap.put("I_ZHMON"		, strMonth);
		}
        
        // import parameters table setting
        String[] DATE = servletRequest.getParameterValues("day_date");

        for( int i=0 ; i<DATE.length ; i++){
            HashMap tmpMap = new HashMap();
            String strName = "holidayChk" + (i+1);
            tmpMap.put("ZHDAT", DateUtil.getChangeDate(DATE[i],"/"));
            tmpMap.put("ZHLOX", "on".equals(StringUtil.checkNull(request.getParameter(strName)))?"X":"");
            
            inputList.add(tmpMap);
        }
		
        String strRfcName = "ZHBR_SD_DP_SAVE_HOLIDAY_DAY";  // rfc 함수명
		String importTable  = "T_LIST";  
		
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();
            
            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap, inputStructureList, importTable, inputList));

            
        } catch (Exception e) {
            System.err.println(e.toString());
            LogWriter.errorT(this, "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnector();
        }

        return returnMap;
    }
	
    
	/**
	 * Time Zone 검색<br>
	 * Time Zone Management 화면에서 조회 시 실행된다.<br>
	 * Time Zone Management 화면, TimeZoneList 컴포넌트에서 호출.
	 * 
	 * @param request 전달 변수를 담은 객체
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public HashMap getTimeZoneList(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		HashMap returnMap = new HashMap();
		HashMap inputMap = new HashMap();
        String strMonth = "";
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		
			inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		
		inputMap.put("I_ZDATE"		, DateUtil.getChangeDate(request.getParameter("p_dayDate"),"/"));
		
		String strRfcName = "ZHBR_SD_DP_LIST_TIM_SCHEDULE";  // rfc 함수명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
			
            
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap;
	}
    
    /**
     * Time Zone Save .<br>
     * TimeZoneTrans 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  HashMap returnMap return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap setSaveTimeZone(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
        
    	HttpServletRequest servletRequest = request.getServletRequest();
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        HashMap I_TIMES = new HashMap();
        ArrayList inputStructureList = new ArrayList();
		ArrayList inputList = new ArrayList();
		
		
        // import parameters setting
        inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
        
			inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		
        
        // structure setting    
        I_TIMES.put("STRUCTURE_NAME", "I_TIMEZ");
        I_TIMES.put("ZDATE",   	DateUtil.getChangeDate(request.getParameter("date"),"/"));			 // Time Zone Date
        I_TIMES.put("ZTIME1",   "on".equals(StringUtil.checkNull(request.getParameter("time1")))?"X":"");// 1st Time Zone
        I_TIMES.put("ZTIME2",  	"on".equals(StringUtil.checkNull(request.getParameter("time2")))?"X":"");// 2st Time Zone
        I_TIMES.put("ZTIME3",   "on".equals(StringUtil.checkNull(request.getParameter("time3")))?"X":"");// 3st Time Zone
        I_TIMES.put("ZTIME4",   "on".equals(StringUtil.checkNull(request.getParameter("time4")))?"X":"");// 4st Time Zone
        I_TIMES.put("ZTIME5",  	"on".equals(StringUtil.checkNull(request.getParameter("time5")))?"X":"");// 5st Time Zone
            
        inputStructureList.add(I_TIMES);
		
        String strRfcName = "ZHBR_SD_DP_SAVE_HOLIDAY_TIME";  // rfc 함수명
		String importTable  = "T_RETURN"; 
		
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();
            
            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap, inputStructureList, importTable, inputList));
            
        } catch (Exception e) {
            System.err.println(e.toString());
            LogWriter.errorT(this, "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    
    
	
	/**
	 * Test Drive 검색<br>
	 * Test Drive Master 화면에서 조회 클릭 시 실행된다.<br>
	 * Test Drive Master 화면, DriveMasterList 컴포넌트에서 호출.
	 * 
	 * @param request 		전달 변수를 담은 객체
	 * @param dealerCode 	Dealer Code
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getTestDriveList(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		
		
			inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		
		
		inputMap.put("I_ZCCAR"		, request.getParameter("model")); 		// Model Code
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("transmission"),"")))
			inputMap.put("I_ZCTRMS"		, request.getParameter("transmission"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("body_type"),"")))
			inputMap.put("I_ZCBYTE"		, request.getParameter("body_type"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("exterior_color"),"")))
			inputMap.put("I_ZCEXTC"		, request.getParameter("exterior_color"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("engine_capacity"),"")))
			inputMap.put("I_ZCECAPA"		, request.getParameter("engine_capacity"));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_status"),"")))
			inputMap.put("I_STATUS"		, request.getParameter("vehicle_status"));
		
		
		String strRfcName = "ZHBR_SD_DP_LIST_TD_MASTER";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	
	
    /**
     * Test Drive 상세 조회<br>
     * DriveMasterDetail 컴포넌트에서 호출.
     * 
     * @param request 		parameter
     * @param dealerCode  	Dealer Code
     * @return  HashMap structure data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getTestDriveDetail(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		
       
        inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
       
				
		inputMap.put("I_ZCVIN"		, request.getParameter("vin"));
		
		inputMap.put("I_LANGU"		, request.getLocale().toString().toUpperCase());
        
        String strRfcName = "ZHBR_SD_DP_VIEW_TD_MASTER";  // rfc 함수명
//        String strImportTable = "T_RETURN";

        try {
            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            //returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap, strImportTable, new ArrayList()));
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    
    /**
     * Test Drive 상세 조회<br>
     * DriveMasterDetail 컴포넌트에서 호출.
     * 
     * @param request 		parameter
     * @param dealerCode  	Dealer Code
     * @return  HashMap structure data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getSearchVin(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		inputMap.put("I_ZCVIN"		, request.getParameter("vin"));
		
        
        String strRfcName = "ZHBR_SD_DP_SEARCH_TD_VIN";  // rfc 함수명
//        String strImportTable = "T_RETURN";

        try {
            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    
    
    /**
     * Drive Master 생성 .<br>
     * DriveMasterTrans 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @param dealerCode 딜러코드
     * @return  HashMap returnMap return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap setDriveMaster(IPortalComponentRequest request, String dealerCode)
        throws ConnectorException, ResourceException {
            
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        ArrayList inputStructureList = new ArrayList();
		ArrayList inputList = new ArrayList();
		
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
       
			inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + dealerCode); // Dealer code
		
        inputMap.put("I_STATUS", request.getParameter("status"));
		
        HashMap I_TDMAT = new HashMap();
        I_TDMAT.put("STRUCTURE_NAME", "I_TDMAT");
        I_TDMAT.put("ZCVIN",   	request.getParameter("vin"));			// vin
        I_TDMAT.put("ZCCUNO",   request.getParameter("car_no"));		// car no
        I_TDMAT.put("STREET",  	request.getParameter("street"));		// Street
        I_TDMAT.put("CITYCD",   request.getParameter("city"));			// city code
//        I_TDMAT.put("CITYNM",   request.getParameter(""));			// city desc
        I_TDMAT.put("REGIO",   	request.getParameter("state"));			// Region (State, Province, County)
//        I_TDMAT.put("BEZEI",   	request.getParameter(""));			// state desc
        I_TDMAT.put("MNGER",  	request.getParameter("car_maneger"));	// Vehicle Manager
        I_TDMAT.put("INSCH",  	request.getParameter("insurance"));		// nsurance check(Y/N)
        I_TDMAT.put("STATUS",	request.getParameter("carStatus"));		// Satus - I:impossible, P:possible
        I_TDMAT.put("ULRSN"	,	request.getParameter("reason"));		// Reason for the Useless
        // structure setting
        inputStructureList.add(I_TDMAT);
        
        String strRfcName = "ZHBR_SD_DP_SAVE_TD_MASTER";  // rfc 함수명
		String importTable  = "T_RETURN";  
		
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap, inputStructureList, importTable, inputList));
			

        } catch (Exception e) {
            System.err.println(e.toString());
            LogWriter.errorT(this, "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnector();
        }

        return returnMap;
    }
	
    
	/**
	 * 시승 결과 검색<br>
	 * 시승 결과 리스트 화면에서 조회 클릭 시 실행된다.<br>
	 * 시승 결과 리스트 화면, DriveResultList 컴포넌트에서 호출.
	 * 
	 * @param request 		전달 변수를 담은 객체
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getDriveResultList(IPortalComponentRequest request)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		inputMap.put("I_STATUS"		, "F");
		
		if(UmeUtil.checkUserGroupExist(request.getUser().getLogonUid(), "HMB_DP_DEALER")){
			inputMap.put("I_ZCONPO", "D");
			
				inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + UmeUtil.getUserDealerCode(request)); // DealerCode
			
		}else{
			inputMap.put("I_ZCONPO", "C");
		}

		String strRsvNo = StringUtil.checkNull(request.getParameter("sel_rsvno"));
		
		if( "".equals(strRsvNo)){
			if(!"".equals(StringUtil.checkNull(request.getParameter("car_no"),"")))
				inputMap.put("I_ZCCUNO"		, request.getParameter("car_no"));
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("date_from"),"")))
				inputMap.put("I_STARDT"		, DateUtil.getChangeDate(request.getParameter("date_from"),"/"));
			
			if(!"".equals(StringUtil.checkNull(request.getParameter("date_to"),"")))
				inputMap.put("I_ENDDAT"		, DateUtil.getChangeDate(request.getParameter("date_to"),"/"));
		}else{
			inputMap.put("I_RSVNO"		, strRsvNo);
		}

		
		String strRfcName = "ZHBR_SD_DP_LIST_TEST_DRIVE";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
    
	
	/**
	 * 시승 결과 입력<br>
	 * 예약 변경 화면에서 시승결과입력 버튼 클릭 시 실행된다.<br>
	 * 시승 결과 입력 화면, DriveResultCreation 컴포넌트에서 호출.
	 * 
	 * @param request 		전달 변수를 담은 객체
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getSurveyItem(IPortalComponentRequest request)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		
		inputMap.put("I_LANGU"		, request.getLocale().toString().toUpperCase());
		
		String strRfcName = "ZHBR_SD_DP_LIST_SURVEY_ITEM";  // rfc 함수명
		String strListTable = "T_LIST";              // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
    
    
    /**
     * Drive Master 생성 .<br>
     * DriveMasterTrans 컴포넌트에서 호출.
     * 
     * @param request parameter
     * @return  HashMap returnMap return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap setSaveSurvey(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
         
    	HttpServletRequest servletRequest = request.getServletRequest();
        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        ArrayList inputStructureList = new ArrayList();
		ArrayList inputList = new ArrayList();
		
        // import parameters setting
        inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
        inputMap.put("I_RSVNO"		, StringUtil.checkNull(request.getParameter("sel_rsvno")));
        inputMap.put("I_DDATE"		, DateUtil.getChangeDate(request.getParameter("p_rsv_date"),"/"));
        inputMap.put("I_DTIME"		, StringUtil.checkNull(request.getParameter("p_rsv_time")));
        inputMap.put("I_ZCRETYNO"	, StringUtil.checkNull(request.getParameter("p_salesMan")));
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("wash"))))
        	inputMap.put("I_WASH"	, request.getParameter("wash"));
        
        if(!"".equals(StringUtil.checkNull(request.getParameter("commentValue"),"")))
			inputMap.put("I_COMMNT"		, request.getParameter("commentValue"));
        
        // import parameters table setting
        String[] surveyCode = servletRequest.getParameterValues("surveyCode");
        
        
        for( int i=0 ; i<surveyCode.length ; i++){
            HashMap tmpMap = new HashMap();
            String strName = "surveyValue" + i;
            tmpMap.put("ZPRBNO", StringUtil.checkNull(surveyCode[i]));
            tmpMap.put("ZPOINT", StringUtil.checkNull(request.getParameter(strName),"0"));
            
            inputList.add(tmpMap);
        }
        
        String strRfcName = "ZHBR_SD_DP_SAVE_SURVEY";  // rfc 함수명
		String importTable  = "T_LIST";  
		
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap, inputStructureList, importTable, inputList));

        } catch (Exception e) {
            System.err.println(e.toString());
            LogWriter.errorT(this, "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnector();
        }

        return returnMap;
    }
    
    
    /**
     * Dealer Select 검색<br>
     * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     *
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getCustomerList(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
    	HashMap returnList = new HashMap();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); 							// Company Code
		inputMap.put("I_PARTNER", StringUtil.checkNull(request.getParameter("sel_partno"))); // customer number
      
        String strRfcName = "ZHBR_SD_DP_VIEW_TD_ENDCUST";  // rfc 함수명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            returnList = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
            if(returnList.size()>0 || returnList != null)
            {
            	LogWriter.debugT(this, "inCustInfo return==========================" + returnList.toString());
            }
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
	/**
	 * region 조건값을 가져온다.<br>
	 * PCBoardContent  컴포넌트에서 호출.
	 * 
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public HashMap getBrand(IPortalComponentRequest request)
		throws ConnectorException, ResourceException {

		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		inputMap.put("I_BRAND"		, "X");
		inputMap.put("I_YEAR"		, "X");
		inputMap.put("I_OCCUPATION"	, "X");
		inputMap.put("I_REGION"		, "X");
		
		inputMap.put("I_LANGU"		, request.getLocale().toString().toUpperCase());

		String strRfcName = "ZHBR_SD_DP_LIST_CUSTOMER_INFO"; // rfc 함수명
		String strListTable = "T_LIST";           // 리턴 테이블명
		
		HashMap returnMap = new HashMap();

		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
            
			// rfc 실행
			returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap));
			LogWriter.debugT("", "returnMap======" + returnMap.toString());
			

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap;
	}
	
	
	/**
	* doSearch
	* 
	* Search 결과를 가져온다.
	* 
	* @see          none
	* @param     	request			IPortalComponentRequest
	* @param        event			IPortalRequestEvent
	* @exception    none
	*/
    public HashMap doSaveCustomerInfo(IPortalComponentRequest request, IPortalRequestEvent event){
    	
        //변수정의를 위한 클래스 선언
        HttpServletRequest servletRequest = request.getServletRequest();

        // rfc helper 클래스 선언
        RfcHelper rfcHelper = new RfcHelper(request, CommonProperties.getDPRFCAlias(request));

        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        ArrayList inputStructureList = new ArrayList();
		ArrayList inputList = new ArrayList();

		LogWriter.debugT(this, "doSaveCustomerInfo before Rfc : " + DateUtil.getCurrentTime());
		
        try{       	

        	// import parameters setting
    		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
        	
        	HashMap I_CUST = new HashMap();
        	
        	I_CUST.put("STRUCTURE_NAME", "I_PARTNER");        	
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("cpf"))))
        		I_CUST.put("TAXNUM", StringUtil.checkNull(request.getParameter("cpf")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("mankind"))))
        		I_CUST.put("XSEXM", StringUtil.checkNull(request.getParameter("mankind")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("fName"))))
        		I_CUST.put("NAME_FIRST", StringUtil.checkNull(request.getParameter("fName")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("lstName"))))
        		I_CUST.put("NAME_LAST", StringUtil.checkNull(request.getParameter("lstName")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("birth"))))
        		I_CUST.put("BIRTHDATE", DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("birth")),"/"));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("occur"))))
        		I_CUST.put("JOBGR", StringUtil.checkNull(request.getParameter("occur")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("post"))))
        		I_CUST.put("POST_CODE1", StringUtil.checkNull(request.getParameter("post")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("street1"))))
        		I_CUST.put("STREET", StringUtil.checkNull(request.getParameter("street1")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("cust_state"))))
        		I_CUST.put("REGION", StringUtil.checkNull(request.getParameter("cust_state")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("city"))))
        		I_CUST.put("CITY1", StringUtil.checkNull(request.getParameter("city")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("htel"))))
        		I_CUST.put("TEL_NUMBER", StringUtil.checkNull(request.getParameter("htel")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("offTel"))))
        		I_CUST.put("OFF_NUMBER", StringUtil.checkNull(request.getParameter("offTel")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("mtel"))))
        		I_CUST.put("MOB_NUMBER", StringUtil.checkNull(request.getParameter("mtel")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("email"))))
        		I_CUST.put("SMTP_ADDR", StringUtil.checkNull(request.getParameter("email")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("prmCarBr"))))
        		I_CUST.put("OLDCARBR1", StringUtil.checkNull(request.getParameter("prmCarBr")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("prmModel"))))        		
        		I_CUST.put("OLDCARMD1", StringUtil.checkNull(request.getParameter("prmModel")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("prmOwnYear"))))
        		I_CUST.put("OLDCARYR1", StringUtil.checkNull(request.getParameter("prmOwnYear")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("secCarBr"))))
        		I_CUST.put("OLDCARBR2", StringUtil.checkNull(request.getParameter("secCarBr")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("secModel"))))
        		I_CUST.put("OLDCARMD2", StringUtil.checkNull(request.getParameter("secModel")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("secOwnYear"))))
        		I_CUST.put("OLDCARYR2", StringUtil.checkNull(request.getParameter("secOwnYear")));
        	
        	if(!"".equals(StringUtil.checkNull(request.getParameter("sel_partno"))))
        		I_CUST.put("PARTNER", StringUtil.checkNull(request.getParameter("sel_partno")));
        	
        	
            inputStructureList.add(I_CUST);
            
            String strRfcName = "ZHBR_SD_DP_SAVE_TD_ENDCUST";  // rfc 함수명
    		String importTable  = "T_RETURN";
    		
            //connection 생성
            if(jca.getConnection() == null) createConnector();

    		// rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap, inputStructureList, importTable, inputList));  
            
            LogWriter.debugT(this, "doSaveCustomerInfo End Rfc : " + DateUtil.getCurrentTime());

        } catch(Exception e) {
        	LogWriter.debugT(this, "returnMap : " + e.getMessage());
            
            e.printStackTrace();
        }
        finally {//TALVEZ O PROBLEMA ESTEJA RESOLVIDO AQUI
			closeConnector();
		}
        
        return returnMap;
    }
    
    
    
	/**
	 * region 조건값을 가져온다.<br>
	 * PCBoardContent  컴포넌트에서 호출.
	 * 
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getRegion(IPortalComponentRequest request)
		throws ConnectorException, ResourceException {

		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
//		inputMap.put("I_SPRAS"		, CommonProperties.getWebLocale(request).toUpperCase());
		
		inputMap.put("I_LANGU"		, request.getLocale().toString().toUpperCase());
		
		String strRfcName = "ZHBR_SD_DP_LIST_STATE"; // rfc 함수명
		String strListTable = "T_LIST";           // 리턴 테이블명

		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
            
			// rfc 실행
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	
	
    /**
     * City 값을 가져온다.<br>
     * state 조건이 선택된 이후에 호출된다.<br>
     * TestDriveMaster화면, ConditionInfo 컴포넌트에서 호출.
     * 
     * @param String I_STATE sate code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getCityCondition(IPortalComponentRequest request, String I_STATE, String I_ADDRE)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
          
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        inputMap.put("I_LAND1" , "BR");
        inputMap.put("I_BLAND", I_STATE);
        inputMap.put("I_ADDRE", I_ADDRE);

        String strRfcName = "ZHBR_SD_DP_LIST_CITY";  	// rfc 함수명
        String strListTable = "T_LIST";           		// 리턴 테이블명

        try {
            //connection 생성
            if(jca.getConnection() == null) createConnector();
            
            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
    /**
     * Dealer Select 검색<br>
     * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     *
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getDealerEnquirySearch(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
		inputMap.put("I_REGIO", StringUtil.checkNull(request.getParameter("sel_state"))); // state
		inputMap.put("I_CITYCD", StringUtil.checkNull(request.getParameter("sel_city"))); // city
    	
		inputMap.put("I_LANGU"		, request.getLocale().toString().toUpperCase());
		
    	LogWriter.debugT("", "dealer Input============" +inputMap.toString() );
        String strRfcName = "ZHBR_SD_DP_LIST_TD_DEALER";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            LogWriter.debugT("", "rfc Dealer List=-========================" + returnList.size());
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
	/**
	* doSearch
	* 
	* Search 결과를 가져온다.
	* 
	* @see          none
	* @param     	request			IPortalComponentRequest
	* @param        event			IPortalRequestEvent
	* @exception    none
	*/
    public ArrayList getReserveList(IPortalComponentRequest request){
    	
        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
		
		// Dealer Group 일경우만 DealerCode Setting..
		if(UmeUtil.checkUserGroupExist(request.getUser().getLogonUid(), "HMB_DP_DEALER")){
			inputMap.put("I_ZCONPO", "D");
			
				inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + UmeUtil.getUserDealerCode(request)); // DealerCode
			
		}else{
			inputMap.put("I_ZCONPO", "C");
		}
		
		
		String strCPF = StringUtil.checkNull(request.getParameter("cpf"));
		String strCustomerName = StringUtil.checkNull(request.getParameter("sel_custNm"));
		String strRsvNo = StringUtil.checkNull(request.getParameter("sel_rsvno"));
		String strTime = StringUtil.checkNull(request.getParameter("sel_time"));
		
		if( "".equals(strCPF) && "".equals(strCustomerName) && "".equals(strRsvNo)){
			if(!"".equals(StringUtil.checkNull(request.getParameter("sel_car")))) 
	    		inputMap.put("I_ZCCUNO", StringUtil.checkNull(request.getParameter("sel_car"))); // plate number. 
	    	
	    	if(!"".equals(StringUtil.checkNull(request.getParameter("sel_status")))) 
	    		inputMap.put("I_STATUS", StringUtil.checkNull(request.getParameter("sel_status"))); // status
	    	
	    	if(!"".equals(StringUtil.checkNull(request.getParameter("fdate"))))
	    		inputMap.put("I_STARDT", DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("fdate")),"/")); // start_date
	    	
	    	if(!"".equals(StringUtil.checkNull(request.getParameter("tdate"))))
	    		inputMap.put("I_ENDDAT", DateUtil.getChangeDate(StringUtil.checkNull(request.getParameter("tdate")),"/")); // 
	  	
	    	if(!"".equals(StringUtil.checkNull(request.getParameter("sel_car")))) 
	    		inputMap.put("I_ZCCUNO", StringUtil.checkNull(request.getParameter("sel_car"))); // plate number. 
	    	
	    	// Report 에서 넘어오는 Time 값에 대한 import setting..
	    	if(!"".equals(strTime)){
	    		if("1".equals(strTime)){
	    			inputMap.put("I_RTIME1", "X");
	    		}else if("2".equals(strTime)){
	    			inputMap.put("I_RTIME2", "X");
	    		}else if("3".equals(strTime)){
	    			inputMap.put("I_RTIME3", "X");
	    		}else if("4".equals(strTime)){
	    			inputMap.put("I_RTIME4", "X");
	    		}else if("5".equals(strTime)){
	    			inputMap.put("I_RTIME5", "X");
	    		}
	    	}
	    	
		}else{
			if(!"".equals(strCPF))
				inputMap.put("I_TAXNUM", strCPF); 					// CPF
			
			if(!"".equals(strCustomerName))
				inputMap.put("I_NAME_FIRST", strCustomerName); 		// Customer Name
			
			if(!"".equals(strRsvNo)){
				inputMap.put("I_RSVNO", strRsvNo); 					// Reservation No
				inputMap.put("I_STATUS", StringUtil.checkNull(request.getParameter("sel_status"))); // status
			}
		}
    	
    	LogWriter.debugT("", "dealer Input============" +inputMap.toString() );
        String strRfcName = "ZHBR_SD_DP_LIST_TEST_DRIVE";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
            LogWriter.debugT("", "rfc Dealer List=-========================" + returnList.size());
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
    /**
     * Car Number List 를  가져온다.<br>
     * 시승 결과 리스트 화면, DriveResultList 컴포넌트에서 호출.
     * 
     * @param String strDealerCode 		Dealer Code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public ArrayList getCarNoCondition(IPortalComponentRequest request, String strDealerCode)
        throws ConnectorException, ResourceException {

        ArrayList returnList = new ArrayList();
        HashMap inputMap = new HashMap();
        
        // import parameters setting
        inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
        
        	inputMap.put("I_KUNNR" , CommonProperties.DEALER_PREFIX + strDealerCode);
        
        inputMap.put("I_LANGU"		, request.getLocale().toString().toUpperCase());
        
        String strRfcName = "ZHBR_SD_DP_LIST_CAR_PLATE";  // rfc 함수명
        String strListTable = "T_LIST";           // 리턴 테이블명

        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
    /**
     * Dealer Select 검색<br>
     * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     *
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getReserveInfo(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
    	HashMap returnList = new HashMap();
        HashMap inputMap = new HashMap();
        String wk_status = StringUtil.checkNull(request.getParameter("wk_status"));
        
        LogWriter.debugT(this, "sel_vin=========" + StringUtil.checkNull(request.getParameter("sel_vin")));
        LogWriter.debugT(this, "sel_dealer=========" + StringUtil.checkNull(request.getParameter("sel_dealer")));
        LogWriter.debugT(this, "sel_partno=========" + StringUtil.checkNull(request.getParameter("sel_partno")));
        
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("sel_vin"))))
			inputMap.put("I_ZCVIN", StringUtil.checkNull(request.getParameter("sel_vin")));
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("sel_dealer"))))
			
				inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX  + StringUtil.checkNull(request.getParameter("sel_dealer")));
			
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("sel_partno"))))
			inputMap.put("I_PARTNER", StringUtil.checkNull(request.getParameter("sel_partno"))); // Company Code	        
		
    	if(!"".equals(StringUtil.checkNull(request.getParameter("sel_rsvno"),""))) 
			inputMap.put("I_RSVNO", StringUtil.checkNull(request.getParameter("sel_rsvno"))); // rsvno

    	if("N".equals(wk_status)){			// N : 신규생성
    		inputMap.put("I_STATUS", "1");
    		
    	}else if("M".equals(wk_status)){	// M : 수정
    		inputMap.put("I_STATUS", "2");
    		
    	} else {							// V : 조회
    		inputMap.put("I_STATUS", "3");
    	}
    	
    	inputMap.put("I_LANGU"		, request.getLocale().toString().toUpperCase());
    	
    	String strRfcName = "ZHBR_SD_DP_VIEW_RESERVATION";  // rfc 함수명
        
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            returnList = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
            LogWriter.debugT("", "reserveInfo return List=============================" + returnList.toString());
            
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
	/**
	* doSearch
	* 
	* Search 결과를 가져온다.
	* 
	* @see          none
	* @param     	request			IPortalComponentRequest
	* @param        event			IPortalRequestEvent
	* @exception    none
	*/
    public HashMap doSaveReserve(IPortalComponentRequest request, IPortalRequestEvent event){
    	LogWriter.debugT(this, "doSaveReserve Start Time : " + DateUtil.getCurrentTime());
        //변수정의를 위한 클래스 선언
        HttpServletRequest servletRequest = request.getServletRequest();

        // rfc helper 클래스 선언
        RfcHelper rfcHelper = new RfcHelper(request, CommonProperties.getDPRFCAlias(request));

        HashMap returnMap = new HashMap();
        HashMap inputMap = new HashMap();
        ArrayList inputStructureList = new ArrayList();
		ArrayList inputList = new ArrayList();

		LogWriter.debugT(this, "doSaveReserve before Rfc : " + DateUtil.getCurrentTime());
		
        try{       	

        	// import parameters setting
        	
        	String wk_status = StringUtil.checkNull(request.getParameter("wk_status"));
        	String strRsvNo = "";
        	
        	String sapStatus = "";
        	
        	if("N".equals(wk_status)){			// N : 신규 생성
        		sapStatus = "1";
        		
        	}else if("M".equals(wk_status)){	// M : 수정
        		sapStatus = "2";
        		strRsvNo =  StringUtil.checkNull(request.getParameter("sel_rsvno"));
        		
        	} else if("C".equals(wk_status)){	// C : 취소
        		sapStatus = "3";
        		strRsvNo =  StringUtil.checkNull(request.getParameter("sel_rsvno"));
        	}
        		
        	
    		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
    		inputMap.put("I_STATUS"		, sapStatus);
    		
    	
    			inputMap.put("I_KUNNR"		, CommonProperties.DEALER_PREFIX + StringUtil.checkNull(request.getParameter("sel_dealer")));
    		
    		if(!"".equals(strRsvNo))
    			inputMap.put("I_RSVNO"		, strRsvNo);
    		
        	HashMap I_CUST = new HashMap();
        	
        	String selTime = StringUtil.checkNull(request.getParameter("sel_time"));
        	
        	LogWriter.debugT(this, "reservInfo trans input===========" + inputMap.toString());
        	LogWriter.debugT(this, "reservInfo trans input rsvno ===========" + strRsvNo);
        	
        	I_CUST.put("STRUCTURE_NAME", "I_RESERV");
        	I_CUST.put("ZCVIN", StringUtil.checkNull(request.getParameter("sel_vin")));
        	I_CUST.put("PARTNER",StringUtil.checkNull(request.getParameter("sel_partno")));
        	
        	if(UmeUtil.checkUserGroupExist(request.getUser().getLogonUid(), "HMB_DP_CALL_CENTER_ASSISTANCE")){
        		I_CUST.put("ZCONPO", "C");
        	}else{
        		I_CUST.put("ZCONPO", "D");
        	}
        	
        	I_CUST.put("RDATE", StringUtil.checkNull(request.getParameter("sel_date")));
        	if( !"".equals(StringUtil.checkNull(request.getParameter("comment"))))
        		I_CUST.put("ZCOMMT_R", request.getParameter("comment"));
        	
        	
        	if("1".equals(selTime))
        		I_CUST.put("RTIME1", "X");
        	
        	if("2".equals(selTime))
        		I_CUST.put("RTIME2", "X");

        	if("3".equals(selTime))
        		I_CUST.put("RTIME3", "X");

        	if("4".equals(selTime))
        		I_CUST.put("RTIME4", "X");

        	if("5".equals(selTime))
        		I_CUST.put("RTIME5", "X");
        	
            inputStructureList.add(I_CUST);
            
            
            LogWriter.debugT(this, "reservInfo trans I_CUST===========" + I_CUST.toString());
            
            String strRfcName = "ZHBR_SD_DP_SAVE_RESERVATION";  // rfc 함수명
    		String importTable  = "T_RETURN";  
    		
            //connection 생성
            if(jca.getConnection() == null) createConnector();

            // rfc excute
            returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap, inputStructureList, importTable, inputList));  
            
            LogWriter.debugT(this, "doSaveReserve End Rfc : " + DateUtil.getCurrentTime());

        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {//TALVEZ PROBLEMA AQUI
			closeConnector();
		}
        
        return returnMap;
    }
    
    
    /**
     * Dealer Select 검색<br>
     * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     *
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getTestCarSelect(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        HashMap returnList = new HashMap();
        HashMap inputMap = new HashMap();
        String strUserId = request.getUser().getLogonUid();		// User ID
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
		
		
			inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + StringUtil.checkNull(request.getParameter("sel_dealer"))); // dealer_code
		
		
    	if(!"".equals(StringUtil.checkNull(request.getParameter("p_state")))) 
			inputMap.put("I_REGIO", request.getParameter("p_state")); // state
		
    	if(!"".equals(StringUtil.checkNull(request.getParameter("p_city")))) 
			inputMap.put("I_CITYCD", request.getParameter("p_city")); // city
    	
    	
    	
    	// Dealer Check..
    	if(UmeUtil.checkUserGroupExist(strUserId, "HMB_DP_CALL_CENTER_ASSISTANCE")){
    		inputMap.put("I_ZCONPO", "C");	// Call Center
    	}else{
    		inputMap.put("I_ZCONPO", "D");	// Dealer
    	}
    	
        String strRfcName = "ZHBR_SD_DP_LIST_CAR";  // rfc 함수명
        String strListTable = "T_LIST";            // 리턴 테이블명
        
        
        LogWriter.debugT("", "testCar input=================" + inputMap.toString());
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap));
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
	/**
	 * region 조건값을 가져온다.<br>
	 * PCBoardContent  컴포넌트에서 호출.
	 * 
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public ArrayList getModel(IPortalComponentRequest request)
		throws ConnectorException, ResourceException {

		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS"		, CommonProperties.COMPANY_CODE);
		inputMap.put("I_BRAND"		, request.getParameter("brand_code"));

		String strRfcName = "ZHBR_SD_DP_LIST_OWNED_MODEL"; // rfc 함수명
		String strListTable = "T_LIST";           // 리턴 테이블명
		
		LogWriter.debugT("", "model input======" + inputMap.toString());

		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
            
			// rfc 실행
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
			LogWriter.debugT("", "city returnList======" + returnList.toString());
			

		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}


	//달력 관련 추가
	/**
	 * Date Schedule 검색<br>
	 * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
	 *
	 * 
	 * @param request
	 * @param dealerCode String is dealer code
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	 public ArrayList getDateScheduleList(IPortalComponentRequest request)
	     throws ConnectorException, ResourceException {
	         
		 ArrayList returnList = new ArrayList();
		 HashMap inputMap = new HashMap();
	     String strUserId = request.getUser().getLogonUid();		// User ID
	     String strYear = StringUtil.checkNull(request.getParameter("sch_year"),DateUtil.getYear());
	     String strMonth = StringUtil.checkNull(request.getParameter("sch_mon"),Integer.parseInt(DateUtil.getMonth()) + "");
	     
	     // import parameters setting
	     inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
	     	
				inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + StringUtil.checkNull(request.getParameter("sel_dealer"))); // dealer_code
			
	     inputMap.put("I_ZCVIN", request.getParameter("sel_vin")); // state
	     inputMap.put("I_ZYEAR", strYear); 							// Year
	     inputMap.put("I_ZMNTH", strMonth); 						// Month
	   
	     // Dealer Check..
	     if(UmeUtil.checkUserGroupExist(strUserId, "HMB_DP_DEALER")){
	    	 inputMap.put("I_ZCONPO", "D");
	     }else{
	    	 inputMap.put("I_ZCONPO", "C");
	     }
	     
	     LogWriter.debugT("", "schedule Input============" +inputMap.toString() );
	     String strRfcName = "ZHBR_SD_DP_LIST_DAT_SCHEDULE";  // rfc 함수명
	     String strListTable = "T_LIST";            // 리턴 테이블명
	     
	     try {
	
	         //connection 생성
	         if(jca.getConnection() == null) createConnector();
	
	         returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
	         
	         LogWriter.debugT("", "rfc getDateScheduleList List=-========================" + returnList.toString());
	         
	         //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
	     } catch (Exception e) {
	         LogWriter.errorT(this, "Exception: " + e.getMessage());
	     } finally {
	         closeConnector();
	     }
	
	     return returnList;
	 }
 	
	 /**
	  * Date Schedule 검색<br>
	  * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
	  * 
	  * @param request
	  * @param dealerCode String is dealer code
	  * @return  ArrayList table data return.
	  * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	  * @throws ResourceException    import parameter 생성 실패
	  */
	 public HashMap getTimeScheduleList(IPortalComponentRequest request)
	     throws ConnectorException, ResourceException {
	         
	     HashMap returnList = new HashMap();
	     HashMap inputMap = new HashMap();
	     String strUserId = request.getUser().getLogonUid();		// User ID
	     
	     // import parameters setting
	     inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
	     inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + request.getParameter("sel_dealer"));
	     inputMap.put("I_ZCVIN", request.getParameter("sel_vin")); // state
		 inputMap.put("I_ZDATE", request.getParameter("sel_date")); // state
			
	     // Dealer Check..
	     if(UmeUtil.checkUserGroupExist(strUserId, "HMB_DP_DEALER")){
	    	 inputMap.put("I_ZCONPO", "D");
	     }else{
	    	 inputMap.put("I_ZCONPO", "C");
	     }
	     
	     LogWriter.debugT("", "schedule dt Input============" +inputMap.toString() );
	     String strRfcName = "ZHBR_SD_DP_LIST_TIM_SCHEDULE";  // rfc 함수명
	     
	     try {
	
	         //connection 생성
	         if(jca.getConnection() == null) createConnector();
	
	         returnList = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
	         
	         LogWriter.debugT("", "rfc getTimeScheduleList List=-========================" + returnList.toString());
	         
	         //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
	     } catch (Exception e) {
	         LogWriter.errorT(this, "Exception: " + e.getMessage());
	     } finally {
	         closeConnector();
	     }
	
	     return returnList;
	 }
	 
	////////////////////////////////// Test Drive Report Start
	 /**
     * ReservationStatusList 검색<br>
     * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     *
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getImplementationList(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        HashMap returnList = new HashMap();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
		
			inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + request.getParameter("dealer_code"));
		
		
		inputMap.put("I_ZCCUNO", request.getParameter("car_no")); // Company Code
		inputMap.put("I_ZYEAR", request.getParameter("sel_year")); // Year
		inputMap.put("I_ZMNTH", request.getParameter("sel_month")); // Year
      
    	LogWriter.debugT("", "report implementation Input============" +inputMap.toString() );
        String strRfcName = "ZHBR_SD_DP_REPORT_IMPLEMENT";  // rfc 함수명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            returnList = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
            LogWriter.debugT("", "rfc ZHBR_SD_DP_REPORT_IMPLEMENT =====" + returnList.toString());
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
	    
	    
    /**
     * ReservationStatusList 검색<br>
     * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     *
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getReservationStatusList(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        HashMap returnList = new HashMap();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
		
			inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + request.getParameter("dealer_code"));
		

		inputMap.put("I_ZCCUNO", request.getParameter("car_no")); // Company Code
		
      
    	LogWriter.debugT("", "report reservation Input============" +inputMap.toString() );
        String strRfcName = "ZHBR_SD_DP_REPORT_RESERVATION";  // rfc 함수명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            returnList = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
            LogWriter.debugT("", "rfc ZHBR_SD_DP_REPORT_RESERVATION =====" + returnList.toString());
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }

    
	/**
     * ReservationStatusList 검색<br>
     * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     *
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getDriveSatisfactionLevelList(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        HashMap returnList = new HashMap();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
		
			inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + request.getParameter("dealer_code"));
		
		inputMap.put("I_ZCCUNO", (String)request.getParameter("car_no")); // Company Code
		inputMap.put("I_STADAT", DateUtil.getChangeDate((String)request.getParameter("fdate"), "/")); // Year
		inputMap.put("I_ENDDAT", DateUtil.getChangeDate((String)request.getParameter("tdate"), "/")); // Year
		inputMap.put("I_LANGU", request.getLocale().toString().toUpperCase());
      
    	LogWriter.debugT("", "report getDriveSatisfactionLevelList============" +inputMap.toString() );
        String strRfcName = "ZHBR_SD_DP_REPORT_SATISFACTION";  // rfc 함수명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            returnList = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
            LogWriter.debugT("", "rfc ZHBR_SD_DP_REPORT_SATISFACTION =====" + returnList.toString());
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    
    
	/**
     * ReservationStatusList 검색<br>
     * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getTestDrivePurchaseList(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        HashMap returnList = new HashMap();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
		
			inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + request.getParameter("dealer_code"));
		
		inputMap.put("I_ZCCUNO", (String)request.getParameter("car_no")); // Company Code
		inputMap.put("I_LANGU", request.getLocale().toString().toUpperCase());
		inputMap.put("I_ZYEAR", request.getParameter("sel_year")); // Year
		inputMap.put("I_ZMNTH", request.getParameter("sel_month")); // Year
		inputMap.put("I_ZCRETYNO", request.getParameter("sel_salesman")); // Year		
      
    	LogWriter.debugT("", "report getTestDrivePurchaseList============" +inputMap.toString() );
        String strRfcName = "ZHBR_SD_DP_REPORT_RETAIL_TD";  // rfc 함수명
        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            returnList = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
            LogWriter.debugT("", "rfc ZHBR_SD_DP_REPORT_RETAIL_TD =====" + returnList.toString());
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }    
    
    
	/**
     * ReservationStatusList 검색<br>
     * Dealer Select화면에서 Enquiry 버튼 클릭 시 실행된다.<br>
     *
     * 
     * @param request
     * @param dealerCode String is dealer code
     * @return  ArrayList table data return.
     * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
     * @throws ResourceException    import parameter 생성 실패
     */
    public HashMap getTestDriveRetailPurchaseList(IPortalComponentRequest request)
        throws ConnectorException, ResourceException {
            
        HashMap returnList = new HashMap();
        HashMap inputMap = new HashMap();
        
		// import parameters setting
		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE); // Company Code
		
			inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + request.getParameter("dealer_code"));
				
		inputMap.put("I_LANGU", request.getLocale().toString().toUpperCase());
		inputMap.put("I_ZYEAR", request.getParameter("sel_year")); // Year
		inputMap.put("I_ZMNTH", request.getParameter("sel_month")); // Year
		inputMap.put("I_ZCRETYNO", request.getParameter("sel_salesman")); // Year		
      
    	LogWriter.debugT("", "report getTestDriveRetailPurchaseList============" +inputMap.toString() );
        
    	String strRfcName = "ZHBR_SD_DP_REPORT_RETAIL_PUR";  // rfc 함수명        
        try {

            //connection 생성
            if(jca.getConnection() == null) createConnector();

            returnList = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
            
            LogWriter.debugT("", "rfc ZHBR_SD_DP_REPORT_RETAIL_PUR =====" + returnList.toString());
            
            //resultTable = (ArrayList)returnTableMap.get("T_RETURN");
        } catch (Exception e) {
            LogWriter.errorT(this, "Exception: " + e.getMessage());
        } finally {
            closeConnector();
        }

        return returnList;
    }
    ////////////////////////////////// Test Drive Report End
////////////////////////////////////////////////////////////////////////////////////////////////////////
//						시승차 관리 RFC END	
////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
	
	
	/**
	 * Showroom Floor Log 생성
	 * ShowroomFloorLogTrans 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @param dealerCode 딜러코드
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public HashMap setShowRoom(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {

		HttpServletRequest servletRequest = request.getServletRequest();

		HashMap returnMap = new HashMap();
		ArrayList inputList = new ArrayList();
		HashMap inputMap = new HashMap();

		// import parameters setting
//		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
//		//inputMap.put("I_DEALER", "A02VE82142");
//		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
//		inputMap.put("I_EPUSER", request.getUser().getLogonUid());
//		inputMap.put("I_SHIPTO", StringUtil.checkNull(servletRequest.getParameter("shipto")));
//		inputMap.put("I_MATNR", StringUtil.checkNull(servletRequest.getParameter("fsc")));
//		inputMap.put("I_MONTH", StringUtil.checkNull(servletRequest.getParameter("month")));
//		inputMap.put("I_LGORT", StringUtil.checkNull(servletRequest.getParameter("lgort")));
        
		// import parameters table setting
		String[] p_model = servletRequest.getParameterValues("model");
		String[] p_vtext = servletRequest.getParameterValues("model_desc");
		String[] p_walk_in = servletRequest.getParameterValues("walk_in");
		String[] p_phone = servletRequest.getParameterValues("phone");
		String[] p_internet = servletRequest.getParameterValues("internet");
		String[] p_return = servletRequest.getParameterValues("return");
		String[] p_test_drive = servletRequest.getParameterValues("test_drive");
		String[] p_non_fleet = servletRequest.getParameterValues("non_fleet");
		String[] p_fleet = servletRequest.getParameterValues("fleet");
		String[] p_on_sold = servletRequest.getParameterValues("on_sold");
		String input_date = DateUtil.getChangeDate(request.getParameter("input_date"),"/");
//		LogWriter.debugT(this, "ZHOLIDAY : " + StringUtil.checkNull(servletRequest.getParameter("chk_day")));
//		LogWriter.debugT(this, "CommonProperties.DEALER_PREFIX + dealerCode : " + CommonProperties.DEALER_PREFIX + dealerCode);
					
		for( int i=0 ; i<p_model.length ; i++){
			HashMap tmpMap = new HashMap();
			tmpMap.put("DATUM"			, input_date);
			tmpMap.put("ZHOLIDAY"		, StringUtil.checkNull(servletRequest.getParameter("chk_day")));
			tmpMap.put("KUNNR"			, CommonProperties.DEALER_PREFIX + dealerCode);
			tmpMap.put("MODEL"			, p_model[i]);
			tmpMap.put("VTEXT"			, p_vtext[i]);
			tmpMap.put("WALK_IN_QTY"	, p_walk_in[i]);
			tmpMap.put("PHONE_QTY"		, p_phone[i]);
			tmpMap.put("INTERNET_QTY"	, p_internet[i]);
			tmpMap.put("RETURN_QTY"		, p_return[i]);
			tmpMap.put("TEST_DRIVE_QTY"	, p_test_drive[i]);
			tmpMap.put("NON_FLEET_QTY"	, p_non_fleet[i]);
			tmpMap.put("FLEET_QTY"		, p_fleet[i]);
			tmpMap.put("ON_SOLD_QTY"	, p_on_sold[i]);
			
			inputList.add(tmpMap);
		}
        
		String strRfcName = "ZHAU_DP_SHOWROOM_FLOOW_SAVE";  // rfc 함수명
		String importTable  = "T_DATA";                 // 입력 테이블명
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			// rfc excute
			returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap, importTable, inputList));
			
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap;
	}
	
		
	/*   
	 * Screen : Sales > Sales > ShowRoom Floor Log
	 * RFC name : ZHAU_DP_SHOWROOM_FLOOW_DISPLAY
	 */
	public ArrayList getShowRoom(IPortalComponentRequest request, String strDealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
        
		// import parameters setting (결과값을 받기 위해 Search parameter를 넘겨주는 부분)
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + strDealerCode);
//		inputMap.put("I_MODEL", ); 		// Model Code
//		inputMap.put("I_SPMON", "");	// Year/Month(Ex. 201112)				
		inputMap.put("I_DAILY", DateUtil.getChangeDate(request.getParameter("input_date"),"/"));	// Date
//		inputMap.put("I_TYPE", "");		// Report Type
		inputMap.put("I_GUBUN", "D");	// Daily Type
		
		String strRfcName = "ZHAU_DP_SHOWROOM_FLOOW_DISPLAY";  // rfc 함수명
		String strListTable = "T_DAILY";            // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
            
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	


	/*   
	 * Screen : Sales > Sales > ShowRoom Floor Log Report
	 * RFC name : ZHAU_DP_SHOWROOM_FLOOW_DISPLAY
	 */
	public ArrayList getShowRoomReport(IPortalComponentRequest request, String strDealerCode)
		throws ConnectorException, ResourceException {
            
		ArrayList returnList = new ArrayList();
		HashMap inputMap = new HashMap();
		
		
        
		// import parameters setting (결과값을 받기 위해 Search parameter를 넘겨주는 부분)
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + strDealerCode);
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("vehicle_model"),"")))
			inputMap.put("I_MODEL", StringUtil.checkNull(request.getParameter("vehicle_model"))); 		// Model Code
			
		inputMap.put("I_SPMON", request.getParameter("month") + request.getParameter("year"));		// Year/Month(Ex. 201112)
		
		if(!"".equals(StringUtil.checkNull(request.getParameter("type"),"")))				
			inputMap.put("I_TYPE", request.getParameter("type"));		// Report Type
		inputMap.put("I_GUBUN", "M");	// Monthly Type
		
		String strRfcName = "ZHAU_DP_SHOWROOM_FLOOW_DISPLAY";  // rfc 함수명
		String strListTable = "T_DATA";            // 리턴 테이블명
        
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();

			// rfc excute
			returnList = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap), strListTable);
			//resultTable = (ArrayList)returnTableMap.get("T_RETURN");
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnList;
	}
	
	
	
	/**
	 * Showroom Floor Log 생성
	 * ShowroomFloorLogTrans 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @param dealerCode 딜러코드
	 * @return  ArrayList table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public HashMap getApprovalDocument_org(IPortalComponentRequest request, String dealerCode)
		throws ConnectorException, ResourceException {

		HttpServletRequest servletRequest = request.getServletRequest();

		HashMap returnMap = new HashMap();
		ArrayList inputList = new ArrayList();
		HashMap inputMap = new HashMap();

		// import parameters setting
//		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
//		//inputMap.put("I_DEALER", "A02VE82142");
//		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + dealerCode);
//		inputMap.put("I_EPUSER", request.getUser().getLogonUid());
//		inputMap.put("I_SHIPTO", StringUtil.checkNull(servletRequest.getParameter("shipto")));
//		inputMap.put("I_MATNR", StringUtil.checkNull(servletRequest.getParameter("fsc")));
//		inputMap.put("I_MONTH", StringUtil.checkNull(servletRequest.getParameter("month")));
//		inputMap.put("I_LGORT", StringUtil.checkNull(servletRequest.getParameter("lgort")));
        
		// import parameters table setting
		String[] p_model = servletRequest.getParameterValues("model");
		String[] p_vtext = servletRequest.getParameterValues("model_desc");
		String[] p_walk_in = servletRequest.getParameterValues("walk_in");
		String[] p_phone = servletRequest.getParameterValues("phone");
		String[] p_internet = servletRequest.getParameterValues("internet");
		String[] p_return = servletRequest.getParameterValues("return");
		String[] p_test_drive = servletRequest.getParameterValues("test_drive");
		String[] p_non_fleet = servletRequest.getParameterValues("non_fleet");
		String[] p_fleet = servletRequest.getParameterValues("fleet");
		String[] p_on_sold = servletRequest.getParameterValues("on_sold");
		String input_date = DateUtil.getChangeDate(request.getParameter("input_date"),"/");
//		LogWriter.debugT(this, "ZHOLIDAY : " + StringUtil.checkNull(servletRequest.getParameter("chk_day")));
//		LogWriter.debugT(this, "CommonProperties.DEALER_PREFIX + dealerCode : " + CommonProperties.DEALER_PREFIX + dealerCode);
					
		for( int i=0 ; i<p_model.length ; i++){
			HashMap tmpMap = new HashMap();
			tmpMap.put("DATUM"			, input_date);
			tmpMap.put("ZHOLIDAY"		, StringUtil.checkNull(servletRequest.getParameter("chk_day")));
			tmpMap.put("KUNNR"			, CommonProperties.DEALER_PREFIX + dealerCode);
			tmpMap.put("MODEL"			, p_model[i]);
			tmpMap.put("VTEXT"			, p_vtext[i]);
			tmpMap.put("WALK_IN_QTY"	, p_walk_in[i]);
			tmpMap.put("PHONE_QTY"		, p_phone[i]);
			tmpMap.put("INTERNET_QTY"	, p_internet[i]);
			tmpMap.put("RETURN_QTY"		, p_return[i]);
			tmpMap.put("TEST_DRIVE_QTY"	, p_test_drive[i]);
			tmpMap.put("NON_FLEET_QTY"	, p_non_fleet[i]);
			tmpMap.put("FLEET_QTY"		, p_fleet[i]);
			tmpMap.put("ON_SOLD_QTY"	, p_on_sold[i]);
			
			inputList.add(tmpMap);
		}
        
		String strRfcName = "ZHIN_HR_IF_APPROVAL_DOC_CNT";  // rfc 함수명
		String importTable  = "T_DATA";                 // 입력 테이블명
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			// rfc excute
			returnMap = jca.getRfcDataTable(jca.executeFunction(strRfcName, inputMap, importTable, inputList));
			
		} catch (Exception e) {
			LogWriter.errorT(this, "Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap;
	}	
	

	/**
	 * Portal에서 결제하는 Process(PP)
	 * SPMainLeft 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @return  HashMap table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public HashMap getPortalProcess(IPortalComponentRequest request)
		throws ConnectorException, ResourceException {

		HashMap returnMap = new HashMap();
		HashMap inputMap = new HashMap();
        
		String strRfcName = "ZHBR_PP_PORTAL_PROCESS";  // rfc 함수명
		
		try {

			//connection 생성
//			if(jca.getConnection() == null) jca.createConnection(request, CommonProperties.R3_SP_LT);
			if(jca.getConnection() == null) jca.createConnection(request, "SAP_R3_SP_LT_QA");
			
			// rfc excute
			returnMap = jca.getRfcDataScalar(jca.executeFunction(strRfcName, inputMap));
			
		} catch (Exception e) {
			LogWriter.errorT(this, "getPortalProcess() Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap;
	}	
	

	/**
	 * Portal에서 결제하는 Process(MM)
	 * SPMainLeft 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @return  HashMap table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public HashMap getChkAuthForPortal(IPortalComponentRequest request)
		throws ConnectorException, ResourceException {

		HashMap returnMap = new HashMap();
		HashMap inputMap = new HashMap();
        
		String strRfcName = "ZHBR_MM_CHK_PR_AUTH_FOR_PORTAL";  // rfc 함수명
		
		try {
			//connection 생성
			if(jca.getConnection() == null) jca.createConnection(request, CommonProperties.R3_SP_LT);

			// rfc excute
			returnMap = jca.getRfcDataScalar(jca.executeFunction(strRfcName, inputMap));
            
		} catch (Exception e) {
			LogWriter.errorT(this, "getPortalProcess() Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap;
	}
	
	/**
	 * Portal에서 결제하는 Process(PP)
	 * DPMainLeft 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @return  HashMap table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public HashMap getOrderStockQuantity(IPortalComponentRequest request, String strDealerCode)
		throws ConnectorException, ResourceException {

		HashMap returnMap = new HashMap();
		HashMap inputMap = new HashMap();
        
		inputMap.put("I_KUNNR", CommonProperties.DEALER_PREFIX + strDealerCode);
		
		String strRfcName = "ZHBR_SD_DP_DEALERSHIP_STATUS";  // rfc 함수명
		
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			// rfc excute
			returnMap = jca.getRfcDataScalar(jca.executeFunction(strRfcName, inputMap));
			
		} catch (Exception e) {
			LogWriter.errorT(this, "getPortalProcess() Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap;
	}
	
	
	/**
	 * CRM Process
	 * DPMainLeft 컴포넌트에서 호출.
	 * 
	 * @param request parameter
	 * @return  HashMap table data return.
	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
	 * @throws ResourceException    import parameter 생성 실패
	 */
	public HashMap getLeadCRM(IPortalComponentRequest request, String strDealerCode)
		throws ConnectorException, ResourceException {

		HashMap returnMap = new HashMap();
		HashMap inputMap = new HashMap();
        
		inputMap.put("I_DEALER", CommonProperties.DEALER_PREFIX + strDealerCode);
		
		String strRfcName = "ZHCM_CRM_GET_URGENT_FLOW_HMES";  // rfc 함수명
		
		try {

			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			// rfc excute
			returnMap = jca.getRfcDataScalar(jca.executeFunction(strRfcName, inputMap));
			
		} catch (Exception e) {
			LogWriter.errorT(this, "getPortalProcess() Exception: " + e.getMessage());
		} finally {
			closeConnector();
		}

		return returnMap;
	}
	
	public ArrayList getNoticeList(IPortalComponentRequest request){
		ArrayList resultList = null;
		HashMap inputMap = new HashMap();
		
		String strRfcName = "ZHIN_HR_NOTICE_LIST";
		 
		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			// rfc excute
			resultList = jca.getRfcData(jca.executeFunction(strRfcName, inputMap));
			
			if(resultList!=null && resultList.size()>0){
				resultList = (ArrayList)((ArrayList)resultList.get(1)).get(0);
			}
		}
		catch (Exception e) {
			LogWriter.errorT(this, "getNoticeList() Exception: " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			closeConnector();
		}
		return resultList;
	}	
	
	
	public ArrayList getNoticeDetail(IPortalComponentRequest request, String docType, String docNumber){
		ArrayList resultList = null;
		HashMap inputMap = new HashMap();
		
		String rfcName = "ZHIN_HR_IF_NOTICE_DETAIL";
		
		inputMap.put("IV_ZCDOCTP",docType);
		inputMap.put("IV_ZNDOCUNR",docNumber);
		
		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			resultList = jca.getRfcData(jca.executeFunction(rfcName, inputMap));
			
			if(resultList!=null && resultList.size()>0){
				resultList = (ArrayList)resultList.get(2);
				LogWriter.debugT(this, "resultList.size() = " + resultList.size());
			}
		}
		catch (Exception e) {
			LogWriter.errorT(this, "getNoticeDetail() Exception: " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			closeConnector();
		}
		return resultList;
	}	
	
	
	public HashMap getNoticeDetailFile(IPortalComponentRequest request,String docType,String docNumber){
		HashMap resultMap = null;
		HashMap inputMap = new HashMap();
		
		String rfcName = "ZHIN_HR_DOWNLOAD_FROM_SERVER";
		
		inputMap.put("IV_KEY",docType+docNumber+"01");
	
		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			ArrayList resultList = jca.getRfcData(jca.executeFunction(rfcName,inputMap));
			
			if(resultList!=null && resultList.size()>0){
				resultMap = (HashMap)resultList.get(0);
			}
		}
		catch (Exception e) {
			LogWriter.errorT(this, "getNoticeDetailFile() Exception: " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			closeConnector();
		}
		return resultMap;
	}	
	
	
	public ArrayList getQnaList(IPortalComponentRequest request){
		ArrayList resultList = null;
		HashMap hashMap = new HashMap();
		
		String rfcName = "ZHIN_HR_QNA_LIST";
		
		hashMap.put("IV_LOGIN",request.getUser().getLogonUid());
		
		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			resultList = jca.getRfcData(jca.executeFunction(rfcName,hashMap));
			
			if(resultList!=null && resultList.size()>0){
				resultList = (ArrayList)((ArrayList)resultList.get(1)).get(0);
			}
		}
		catch (Exception e) {
			LogWriter.errorT(this, "getQnaList() Exception: " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			closeConnector();
		}
		return resultList;
	}
	
	public ArrayList getQnaDetail(IPortalComponentRequest request,String docType,String docNumber){
		ArrayList resultList = null;
		HashMap inputMap = new HashMap();
		
		String rfcName = "ZHIN_HR_IF_QNA_DETAIL";
		
		inputMap.put("IV_ZCDOCTP",docType);
		inputMap.put("IV_ZNDOCUNR",docNumber);
			
		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			resultList = jca.getRfcData(jca.executeFunction(rfcName,inputMap));
			
			if(resultList!=null && resultList.size()>0){
				resultList = (ArrayList)resultList.get(2);
			}
		}
		catch (Exception e) {
			LogWriter.errorT(this, "getQnaDetail() Exception: " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			closeConnector();
		}
		return resultList;
	}	
	
	
	public String getBirthdayCount(IPortalComponentRequest request){
		String resultString = null;
		HashMap inputMap = new HashMap();
		
		String rfcName = "ZHIN_HR_IF_BIRTHDAY_CNT";	
					
		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			ArrayList resultList = jca.getRfcData(jca.executeFunction(rfcName, inputMap));
			if(resultList!=null && resultList.size()>0){
				HashMap resultMap = (HashMap)resultList.get(0);
				resultString = String.valueOf(resultMap.get("EV_COUNT"));
			}
		}
		catch (Exception e) {
			LogWriter.errorT(this, "getBirthdayCount() Exception: " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			closeConnector();
		}
		
		return resultString;
	}
	
	public ArrayList getBirthdayList(IPortalComponentRequest request){
		ArrayList resultList = null;
		HashMap inputMap = new HashMap();
		
		String rfcName = "ZHIN_HR_BIRTHDAY_DETAIL";
		
		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
			
			resultList = jca.getRfcData(jca.executeFunction(rfcName, inputMap));
			if(resultList!=null && resultList.size()>0){
				resultList = (ArrayList)((ArrayList)resultList.get(1)).get(0);
			}
		}
		catch (Exception e) {
			LogWriter.errorT(this, "getBirthdayList() Exception: " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			closeConnector();
		}
		return resultList;
	}
	
	
	public HashMap getApprovalDocument(IPortalComponentRequest request){
		HashMap returnMap = new HashMap();
		//String resultString = null;
		HashMap inputMap = new HashMap();
		
		String strRfcName = "ZHIN_HR_IF_APPROVAL_DOC_CNT";
		
		inputMap.put("IV_LOGIN",String.valueOf(request.getUser().getLogonUid()));
		
					
		try {
			//connection 생성
			if(jca.getConnection() == null) createConnector();
			// rfc excute
			//returnMap = jca.getRfcData(jca.executeFunction(rfcName, inputMap));
			returnMap = jca.getRfcDataScalar(jca.executeFunction(strRfcName, inputMap));
//			ArrayList resultList = jca.getRfcData(jca.executeFunction(rfcName,inputMap));
//			if(resultList!=null && resultList.size()>0){
//				returnMap = new HashMap();
//				HashMap resultMap = (HashMap)resultList.get(0);
//
//				resultString = String.valueOf(resultMap.get("EV_OVERTIME"));				
//				returnMap.put("EV_OVERTIME"	, StringUtil.checkNull(resultString, "0"));
//			}
		}
		catch (Exception e) {
			LogWriter.errorT(this, "getApprovalDocument() Exception: " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			closeConnector();
		}
		return returnMap;
	}
	
	
	
//	/**
//	 * vin 정보 조회<br>
//	 * VisitorEnquiry 컴포넌트에서 호출.
//	 * 
//	 * @param request parameter
//	 * @return  HashMap table data return.
//	 * @throws ConnectorException   interaction spec 가져오기 실패, record factory 가져오기 실패, RFC 실행 실패
//	 * @throws ResourceException    import parameter 생성 실패
//	 */
//	public HashMap getVisitorEnquiry(IPortalComponentRequest request)
//		throws ConnectorException, ResourceException {
//            
//		HashMap returnMap = new HashMap();
//		HashMap inputMap = new HashMap();
//        
//		// import parameters setting
//		inputMap.put("I_BUKRS", CommonProperties.COMPANY_CODE);
//		inputMap.put("I_VHVIN", StringUtil.checkNull(request.getParameter("vin")));
//		inputMap.put("I_VHCLE", StringUtil.checkNull(request.getParameter("dsn")));
////		inputMap.put("I_REGO", StringUtil.checkNull(request.getParameter("rego_no")));
//
//		String strRfcName = "ZHAU_DP_VIEW_VIN";  // rfc 함수명
//
//		try {
//			//connection 생성
//			if(jca.getConnection() == null) createConnector();
//
//			// rfc excute
//			returnMap = jca.getRfcDataTotal(jca.executeFunction(strRfcName, inputMap));
//            
//		} catch (Exception e) {
//			LogWriter.errorT(this, "Exception: " + e.getMessage());
//		} finally {
//			closeConnector();
//		}
//
//		return returnMap;
//	}	
	
}
