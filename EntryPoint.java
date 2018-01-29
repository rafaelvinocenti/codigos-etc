package br.com.jcnsistemas.jcn.busconnector;

import br.com.jcnsistemas.jcn.busconnector.control.Facade;
import br.com.jcnsistemas.jcn.busconnector.control.TransactionControl;
import br.com.jcnsistemas.jcn.busconnector.to.jaxb.payment.TransactionWrapper;
import br.com.jcnsistemas.jcn.busconnector.control.UserControl;
import br.com.jcnsistemas.jcn.busconnector.control.Validator;
import br.com.jcnsistemas.jcn.busconnector.dao.FeaturesDAO;
import br.com.jcnsistemas.jcn.busconnector.dao.QueryUserDAO;
import br.com.jcnsistemas.jcn.busconnector.dao.TransactionDAO;
import br.com.jcnsistemas.jcn.busconnector.dao.UserDAO;
import br.com.jcnsistemas.jcn.busconnector.dao.VideoDAO;
import br.com.jcnsistemas.jcn.busconnector.responses.BusResponseCodes;
import br.com.jcnsistemas.jcn.busconnector.responses.MessageResponse;
import br.com.jcnsistemas.jcn.busconnector.responses.MessageResponseGateway;
import br.com.jcnsistemas.jcn.busconnector.responses.PrometheusResponse;
import br.com.jcnsistemas.jcn.busconnector.responses.RechargeValuesResponse;
import br.com.jcnsistemas.jcn.busconnector.security.Crypt;
import br.com.jcnsistemas.jcn.busconnector.staticcontent.CardBanners;
import br.com.jcnsistemas.jcn.busconnector.staticcontent.Factory;
import br.com.jcnsistemas.jcn.busconnector.staticcontent.Media;
import br.com.jcnsistemas.jcn.busconnector.staticcontent.Operators;
import br.com.jcnsistemas.jcn.busconnector.staticcontent.Origin;
import br.com.jcnsistemas.jcn.busconnector.staticcontent.RechargeValues;
import br.com.jcnsistemas.jcn.busconnector.staticcontent.Sources;
import br.com.jcnsistemas.jcn.busconnector.staticcontent.ThreadLoop;
import br.com.jcnsistemas.jcn.busconnector.staticcontent.Trigger;
import br.com.jcnsistemas.jcn.busconnector.to.ActivateEmail;
import br.com.jcnsistemas.jcn.busconnector.to.Campaign;
import br.com.jcnsistemas.jcn.busconnector.to.Channel;
import br.com.jcnsistemas.jcn.busconnector.to.IssuerCityWrapper;
import br.com.jcnsistemas.jcn.busconnector.to.Lead;
import br.com.jcnsistemas.jcn.busconnector.to.LuckyNumber;
import br.com.jcnsistemas.jcn.busconnector.to.QueryUser;
import br.com.jcnsistemas.jcn.busconnector.to.RechargeRequest;
import br.com.jcnsistemas.jcn.busconnector.to.RequestRecurrence;
import br.com.jcnsistemas.jcn.busconnector.to.ResetPassword;
import br.com.jcnsistemas.jcn.busconnector.to.SystemAuthenticated.OffRechargeRequest;
import br.com.jcnsistemas.jcn.busconnector.to.User;
import br.com.jcnsistemas.jcn.busconnector.to.UserETicket;
import br.com.jcnsistemas.jcn.busconnector.to.UserUpdate;
import br.com.jcnsistemas.jcn.busconnector.to.Video;
import br.com.jcnsistemas.jcn.busconnector.to.ProductOffer;
import br.com.jcnsistemas.jcn.busconnector.to.QueryUserWrapper;
import br.com.jcnsistemas.jcn.busconnector.to.UserTransdata;
import br.com.jcnsistemas.jcn.busconnector.to.TransactionTransData;
import br.com.jcnsistemas.jcn.busconnector.to.WinnerState;
import br.com.jcnsistemas.jcn.busconnector.to.auth.AuthClient;
import br.com.jcnsistemas.jcn.busconnector.to.gson.report.ReportOrderRequest;
import br.com.jcnsistemas.jcn.busconnector.utils.DateValidator;
import br.com.jcnsistemas.jcn.busconnector.utils.TokenControl;
import br.com.jcnsistemas.jcn.busconnector.utils.DateHour;
import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.beanio.BeanIOConfigurationException;
import org.beanio.BeanReader;
import org.beanio.BeanWriter;
import org.beanio.StreamFactory;
import org.w3c.dom.Document;
import br.com.jcnsistemas.jcn.busconnector.to.gson.report.ReportRequest;
import br.com.jcnsistemas.jcn.busconnector.to.jaxb.payment.Transaction;
import br.com.jcnsistemas.jcn.busconnector.to.mcafee.McAfee;
import br.com.jcnsistemas.jcn.busconnector.to.telefonedossonhos.TSRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import javax.json.JsonObject;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.xml.bind.JAXBException;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/")
@Produces(MediaType.APPLICATION_XML + ";charset=UTF-8")
public class EntryPoint {

    private static HashMap<String, Properties> propertiesMp = new HashMap<String, Properties>();
    private br.com.jcnsistemas.jcn.busconnector.to.Origin origin = null;
    private Channel channel = null;
    private br.com.jcnsistemas.jcn.busconnector.to.Product product = null;
    private br.com.jcnsistemas.jcn.busconnector.to.Service service = null;
    @Context 
    private HttpServletRequest request;

    public EntryPoint(@HeaderParam("X-JCNBusConnector-Source-Hash") String sourceHash,
            @HeaderParam("X-JCNBusConnector-Source-Channel") String channelId,
            @HeaderParam("X-JCNBusConnector-Source-Product") String productId,
            @HeaderParam("X-JCNBusConnector-Source-Service") String serviceId) {
        if (sourceHash != null) {
            origin = Factory.getInstance(Sources.class).getOrigin(sourceHash);
            if (origin != null && channelId != null) {
                for (Channel c : origin.getChannels()) {
                    if (c.getSnChannel().equals(channelId)) {
                        channel = c;
                    }
                }
                if (channel != null && productId != null) {
                    for (br.com.jcnsistemas.jcn.busconnector.to.Product p : channel.getProducts()) {
                        if (p.getSnProduct().equals(productId)) {
                            product = p;
                        }
                    }
                    if (product != null && serviceId != null) {
                        for (br.com.jcnsistemas.jcn.busconnector.to.Service s : product.getServices()) {
                            if (s.getSnService().equals(serviceId)) {
                                service = s;
                            }
                        }
                    }
                }
            }
        }
    }

    static {
        Trigger.execute();
        ThreadLoop tl = new ThreadLoop(new ScheduledServices());
        tl.start();
    }

    private static void loadProperty(String name, Properties properties) {
        InputStream is = EntryPoint.class.getResourceAsStream("/" + name + ".properties");
        try {
            properties.load(is);
            is.close();
        } catch (IOException ex) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Properties getProperties(String name) {
        if (!propertiesMp.containsKey(name)) {
            propertiesMp.put(name, new Properties());
            loadProperty(name, propertiesMp.get(name));
        }
        return propertiesMp.get(name);
    }

    private String busMessage = "[JCNBusConnector] ";
    private static short SYSTEM_STATUS = BusResponseCodes.SYSTEM_ACTIVE;

    public static boolean isSystemActive() {
        return (EntryPoint.SYSTEM_STATUS == BusResponseCodes.SYSTEM_ACTIVE);
    }

    public void getIpClient() { 
        System.out.println("Client IP = " + request.getRemoteAddr()); 
    }
    
    @GET
    @Path("/setStatus/{status}")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String setStatus(@PathParam("status") boolean status) {
        return "200 OK";
    }

    @GET
    @Path("/initializeAll")
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String initializeAll() {
        System.out.println(busMessage + "Initialize triggered...");
        Trigger.execute();
        return "200 OK";
    }

    @GET
    @Path("operatorStatus/{type}/{operator}")
    public Boolean operatorStatus(@PathParam("type") String type, @PathParam("operator") String operator) {
        return (getChannel() != null ? Factory.getInstance(Operators.class).getSourceDescription(operator, type).isActive() : null);
    }

    @POST
    @Path("registerUser/")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public MessageResponse registerUser(String user) {
        MessageResponse r = new MessageResponse();
        r.setStatus(false);
        System.out.println(busMessage + "Solicitação de registro de novo usuário");
        if (getChannel() != null) {
            try {
                User u = (User) (readBeanIO(user, "RegisterUser"));
                u.setSource(getChannel().getIdChannel());
                busMessage += u.getPhoneNumber() + " - ";
                if (u.getPhoneNumber().toString().length() < 11) {
                    System.out.println(busMessage + "Número de telefone inválido");
                    r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INVALID_DATA);
                    r.setStatusMessage("Número de telefone inválido");
                    return r;
                }
                System.out.println(busMessage + "Telefone de registro é " + u.getPhoneNumber());
                if (UserDAO.checkUser(u)) {
                    System.out.println(busMessage + "Usuário já registrado");
                    r.setStatusCode(BusResponseCodes.USER_RESPONSE_ALREADY_REGISTERED);
                    r.setStatusMessage("Usuário já registrado.");
                } else {
                    DateValidator dv = new DateValidator();

                    boolean validDate;
                    if (u.getCardValidity() == null) {
                        validDate = true;
                    } else {
                        validDate = dv.isDateValid(u.getCardValidity())
                                & dv.isDateGreaterThanNow(u.getCardValidity());
                    }

                    System.out.println(busMessage + "Validando cartão final " + u.getCreditCard4LastDigits() + ", v: " + u.getCardValidity());
                    if (validDate) {
                        MessageResponse mr = requestCreditCardQuery(Crypt.decrypt(u.getEncryptedCard()), u.getCardValidity());
                        if (mr.isStatus()) {
                            if (UserControl.validadeUserIntegrity(getChannel(), u)) {
                                Document d = new PrometheusConnector().parseXMLDocument(mr.getResponse().toString());
                                int cardBanner = Factory.getInstance(CardBanners.class).getJCNCode(
                                        Integer.parseInt(d.getElementsByTagName("bandeiraCartao").item(0).getTextContent()));

                                u.setCardBanner(cardBanner);
                                System.out.println(busMessage + "Bandeira do cartão é " + Factory.getInstance(CardBanners.class).getSourceDescription(cardBanner));
                                if (UserDAO.insert(u)) {
                                    System.out.println(busMessage + "Registro efetuado com sucesso");
                                    r.setStatus(true);
                                    r.setResponse("<needsSecurityCode>" + (d.getElementsByTagName("CSEG").item(0).getTextContent().equals("1")) + "</needsSecurityCode>");
                                    r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                                    r.setStatusMessage("Usuário registrado com sucesso.");
                                }
                            } else {
                                System.out.println(busMessage + "Atributos obrigatÃ³rios não presentes no objeto");
                                r.setStatusCode(BusResponseCodes.USER_RESPONSE_NECESSARY_DATA_NOT_SET);
                                r.setStatusMessage("Faltam campos obrigatÃ³rios");
                            }
                        } else {
                            System.out.println(busMessage + "Cartão informado é inválido");
                            r.setStatusCode(BusResponseCodes.USER_RESPONSE_INVALID_CREDIT_CARD);
                            r.setStatusMessage("Cartão de crédito inválido.");
                        }
                    } else {
                        System.out.println(busMessage + "Data de cartão de crédito inválida.");
                        r.setStatusCode(BusResponseCodes.USER_RESPONSE_INVALID_CREDIT_CARD_DATE);
                        r.setStatusMessage("Data de cartão de crédito inválida.");
                    }
                }
            } catch (SQLException e) {
                switch (e.getErrorCode()) {
                    case 20002:
                        System.out.println(busMessage + "Cartão ativo em outra conta");
                        r.setStatusCode(BusResponseCodes.USER_RESPONSE_ACTIVE_CREDIT_CARD);
                        r.setStatusMessage("Cartão de crédito ativo em outra conta.");
                        break;
                    case 20004:
                        System.out.println(busMessage + "Cartão informado é inválido");
                        r.setStatusCode(BusResponseCodes.USER_RESPONSE_INVALID_CREDIT_CARD);
                        r.setStatusMessage("Cartão de crédito inválido.");
                        break;
                    default:
                        Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                        r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                        r.setStatusMessage("Não foi possível inserir o usuário. Erro interno - OEC " + ((SQLException) e).getErrorCode());
                        break;
                }
            } catch (Exception e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatusMessage("Não foi possível registrar o usuário. Erro interno.");
            }
        }
        return r;
    }

    /**
     * This method is responsible for updating the database because of an
     * external transaction.
     *
     * @param mobilePhone mobilePhone to be processed
     * @param requestJson json to be processed as a transaction report
     * @return HTTP Code
     */
    @POST
    @Path("reportTransaction/{phoneNumber}")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public MessageResponse reportTransaction(@PathParam("phoneNumber") Long mobilePhone, String requestJson) {
        MessageResponse r = new MessageResponse();
        r.setStatusMessage("200 OK");
        r.setResponse("Transacao ok!");
        r.setStatus(true);
        System.out.println(requestJson);
        if (getService() != null) {
            TransactionControl transactionControl = new TransactionControl();
            Gson gson = new Gson();
            ReportRequest request = gson.fromJson(requestJson, ReportRequest.class);
            if (!transactionControl.reportTransaction(this, request)) {
                r.setStatusMessage("500 INTERNAL_SERVER_ERROR");
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatus(false);
            }
        } else {
            r.setStatusMessage("403 FORBIDDEN");
            r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_FORBIDDEN);
            r.setStatus(false);
        }
        return r;
    }

    /**
     * This method is responsible for inserting an order from a customer in the
     * database
     *
     * @param orderJson the json to be processed as an order.
     * @param phoneNumber the phoneNumber to be processed
     * @param agent
     * @return json response
     */
    @POST
    @Path("reportOrder/{update}/{phoneNumber}/{agent}")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public MessageResponse reportOrder(@PathParam("update") Boolean update,
            @PathParam("phoneNumber") Long phoneNumber, @PathParam("agent") Long agent, String orderJson) {
        MessageResponse response = new MessageResponse();
        response.setStatusMessage("200 OK");
        response.setStatus(true);
        response.setResponse("Transacao ok!");
        TSRequest r = new TSRequest();
        r.setAgent(agent);
        r.setJson(orderJson);
        r.setTelefone(phoneNumber);
        r.setId(r.getAgent() + "" + (new Date().getTime()));
        if (getService() != null) {
            TransactionControl transactionControl = new TransactionControl();
            if (update) {
                if (!transactionControl.updateOrder(this, r)) {
                    response.setStatusMessage("500 INTERNAL_SERVER_ERROR");
                    response.setStatus(false);
                }
            } else {
                if (!transactionControl.reportOrder(this, r)) {
                    response.setStatusMessage("500 INTERNAL_SERVER_ERROR");
                    response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                    response.setStatus(false);
                }
            }
        } else {
            response.setStatusMessage("403 FORBIDDEN");
            response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_FORBIDDEN);
            response.setStatus(false);
        }
        return response;
    }

    /**
     * This method is responsible for inserting an order from a customer in the
     * database
     *
     * @param orderJson the json to be processed as an order.
     * @return json response
     */
    @POST
    @Path("reportOrder/{update}/{phoneNumber}")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public MessageResponse reportOrder(@PathParam("update") Boolean update,
            @PathParam("phoneNumber") Long mobilePhone, String orderJson) {
        MessageResponse r = new MessageResponse();
        r.setStatusMessage("200 OK");
        r.setStatus(true);
        r.setResponse("Transacao ok!");
        if (getService() != null) {
            TransactionControl transactionControl = new TransactionControl();
            Gson gson = new Gson();
            ReportOrderRequest request = gson.fromJson(orderJson, ReportOrderRequest.class);
            if (update) {
                if (!transactionControl.updateOrder(this, request)) {
                    r.setStatusMessage("500 INTERNAL_SERVER_ERROR");
                    r.setStatus(false);
                }
            } else {
                if (!transactionControl.reportOrder(this, request)) {
                    r.setStatusMessage("500 INTERNAL_SERVER_ERROR");
                    r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                    r.setStatus(false);
                }
            }
        } else {
            r.setStatusMessage("403 FORBIDDEN");
            r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_FORBIDDEN);
            r.setStatus(false);
        }
        return r;
    }

    @POST
    @Path("manageETicket/{idUser}/{add}")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public MessageResponse manageETicket(String xml, @PathParam("idUser") Long idUser, @PathParam("add") Boolean add) {
        UserETicket eTicket = null;
        try {
            eTicket = (UserETicket) (readBeanIO(xml, "ElectronicTicketCardInsert"));
        } catch (IOException ex) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        MessageResponse response = new MessageResponse();
        System.out.println(busMessage + "Add ETicket");
        if (getChannel() != null && eTicket != null) {
            eTicket.setIdUser(idUser);
            try {
                if (add) {
                    if (UserDAO.insert(eTicket)) {
                        response.setStatusMessage("Cartão Adicionado Com Sucesso!");
                        response.setStatus(true);
                        response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                    } else {
                        response.setStatusMessage("Erro ao Adicionar Cartão");
                        response.setStatus(false);
                        response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INVALID_DATA);
                    }
                } else {
                    if (UserDAO.delete(eTicket)) {
                        response.setStatusMessage("Cartão Removido Com Sucesso!");
                        response.setStatus(true);
                        response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return response;
    }

    @GET
    @Path("getIssuers/{ddd}")
    //@Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public String getIssuers(@PathParam("ddd") int ddd) {
        String xml = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.out.println(busMessage + "Get Issuers");
        if (getChannel() != null) {
            IssuerCityWrapper wrapper = new FeaturesDAO().getIssuerCities(ddd);
            JAXBContext jaxbContext;
            try {
                jaxbContext = JAXBContext.newInstance(IssuerCityWrapper.class);
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                jaxbMarshaller.marshal(wrapper, baos);
                xml = new String(baos.toByteArray());
                baos.close();
            } catch (JAXBException ex) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return xml;
    }

    @POST
    @Path("updateUser/{user}")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public MessageResponse updateUser(String xmlUser, @PathParam("user") Long phoneNumber) {
        busMessage += phoneNumber + " - ";
        System.out.println(busMessage + "Solicitação de alteração de usuário");
        MessageResponse r = new MessageResponse();
        r.setStatus(false);
        if (getChannel() != null) {
            try {
                UserUpdate rawUser = (UserUpdate) (readBeanIO(xmlUser, "UpdateUser"));
                User realUser = UserDAO.getUser(rawUser.getIdUser());
                if (realUser != null) {
                    if (realUser.getStatus().equals(User.STATUS_BLOCKED)) {
                        System.out.println(busMessage + "Usuário bloqueado");
                        r.setStatusMessage("Usuário bloqueado.");
                        r.setStatusCode(BusResponseCodes.USER_UPDATE_RESPONSE_BLOCKED_USER);
                    } else {
                        System.out.println(busMessage + "ID do usuário é " + realUser.getIdUser());
                        MessageResponse mr = new MessageResponse();
                        mr.setStatus(true);
                        // Valida Data do Cartão
                        DateValidator dv = new DateValidator();
                        if (rawUser.getUserData().getCardValidity() != null
                                && !(dv.isDateValid(rawUser.getUserData().getCardValidity())
                                && dv.isDateGreaterThanNow(rawUser.getUserData().getCardValidity()))) {

                            System.out.println(busMessage + "Data de cartão de crédito inválida (" + rawUser.getUserData().getCardValidity() + ")");
                            r.setStatusCode(BusResponseCodes.USER_UPDATE_RESPONSE_INVALID_CREDIT_CARD_DATE);
                            r.setResponse("Data de cartão de crédito inválida.");
                            return r;
                        }

                        if (rawUser.getUserData().getEncryptedCard() != null) {
                            System.out.println(busMessage + "Valida cartão " + rawUser.getUserData().getCreditCard4LastDigits() + "; validade " + rawUser.getUserData().getCardValidity());
                            mr = requestCreditCardQuery(Crypt.decrypt(rawUser.getUserData().getEncryptedCard()), rawUser.getUserData().getCardValidity());

                            if (mr.isStatus()) {
                                Document d = new PrometheusConnector().parseXMLDocument(mr.getResponse().toString());
                                int cardBanner = Factory.getInstance(CardBanners.class).getJCNCode(
                                        Integer.parseInt(d.getElementsByTagName("bandeiraCartao").item(0).getTextContent()));

                                rawUser.getUserData().setCardBanner(cardBanner);
                                System.out.println(busMessage + "Bandeira do cartão é " + Factory.getInstance(CardBanners.class).getSourceDescription(cardBanner));
                                mr.setResponse("<needsSecurityCode>" + (d.getElementsByTagName("CSEG").item(0).getTextContent().equals("1")) + "</needsSecurityCode>");
                            }
                        }

                        if (mr.isStatus()) {
                            UserDAO.prepareUserObjectUpdate(realUser, rawUser.getUserData());
                            rawUser.getUserData().setIdUser(realUser.getIdUser());
                            if (UserDAO.update(rawUser.getUserData())) {
                                System.out.println(busMessage + "Usuário alterado com sucesso");
                                r.setStatus(true);
                                if (mr.getResponse() != null) {
                                    r.setResponse(mr.getResponse());
                                }
                                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                                r.setStatusMessage("Usuário alterado com sucesso.");
                            } else {
                                System.out.println(busMessage + "Usuário não alterado: UPDATE mal-sucedido");
                                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                                r.setStatusMessage("Não foi possível alterar o usuário. Erro interno: Update mal-sucedido.");
                            }
                        } else {
                            System.out.println(busMessage + "Cartão informado é inválido");
                            r.setStatusCode(BusResponseCodes.USER_UPDATE_RESPONSE_INVALID_CREDIT_CARD);
                            r.setStatusMessage("Cartão de crédito inválido.");
                        }
                    }
                } else {
                    System.out.println(busMessage + "Acesso negado ou usuário inexistente");
                    r.setStatusCode(BusResponseCodes.USER_UPDATE_RESPONSE_NONEXISTENT);
                    r.setStatusMessage("Acesso negado ou usuário não existe na base.");
                }
            } catch (SQLException e) {
                switch (e.getErrorCode()) {
                    case 20002:
                        System.out.println(busMessage + "Cartão ativo em outra conta");
                        r.setStatusCode(BusResponseCodes.USER_UPDATE_RESPONSE_ACTIVE_CREDIT_CARD);
                        r.setStatusMessage("Cartão de crédito ativo em outra conta.");
                        break;
                    case 20004:
                        System.out.println(busMessage + "Cartão informado é inválido");
                        r.setStatusCode(BusResponseCodes.USER_RESPONSE_INVALID_CREDIT_CARD);
                        r.setStatusMessage("Cartão de crédito inválido.");
                        break;  
                    default:
                        Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                        r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                        r.setStatusMessage("Não foi possível alterar o usuário. Erro interno - OEC " + ((SQLException) e).getErrorCode());
                        break;
                }
            } catch (Exception e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatusMessage("Não foi possível alterar o usuário. Erro interno.");
            }
        }
        return r;
    }

    /**
     * @since 1.1.0r7
     * @param phoneNumber
     * @param token
     * @param newPassword
     * @return
     */
    @POST
    @Path("resetPassword/{phoneNumber}")
    public MessageResponse resetPassword(String xml,
            @PathParam("phoneNumber") Long phoneNumber) {
        MessageResponse r = new MessageResponse();
        try {
            ResetPassword reset = (ResetPassword) (readBeanIO(xml, "ResetPasswordToken"));
            if (FeaturesDAO.checkToken(phoneNumber, reset.getToken())) {
                User u = UserDAO.getUserNoPwd(phoneNumber);
                u.setPassword(reset.getNewPassword());
                UserDAO.update(u);
                FeaturesDAO.flagTokenAsUsed(reset.getToken());
                r.setStatus(true);
                r.setStatusMessage("Senha redefinida com sucesso.");
            } else {
                r.setStatus(false);
                r.setStatusMessage("Sessão Expirada! Efetue nova solicitação de redefinição de senha.");
            }
        } catch (IOException ex) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        return r;
    }

    @GET()
    @Path("resetPassword/{tokenType}/{phoneNumber}")
    public MessageResponse requestPasswordReset(@PathParam("tokenType") Long tokenType,
            @PathParam("phoneNumber") Long phoneNumber) {
        return (new TokenControl()).requestReset(tokenType, phoneNumber);
    }

    @GET()
    @Path("requestEmailValidation/{phoneNumber}")
    public MessageResponse requestEmailValidation(
            @PathParam("phoneNumber") Long phoneNumber) {
        return (new TokenControl()).requestEmailValidation(phoneNumber);
    }

    @POST()
    @Path("activateEmail/{phoneNumber}")
    public MessageResponse activateEmail(String xml,
            @PathParam("phoneNumber") Long phoneNumber) {
        MessageResponse r = new MessageResponse();
        try {
            ActivateEmail actiavetEmail = (ActivateEmail) (readBeanIO(xml, "EmailActivation"));
            if (FeaturesDAO.checkToken(phoneNumber, actiavetEmail.getToken())) {
                User u = UserDAO.getUserNoPwd(phoneNumber);
                User rawUser = new User();
                rawUser.setIdUser(u.getIdUser());
                rawUser.setFgEmail(1);
                UserDAO.update(rawUser);
                FeaturesDAO.flagTokenAsUsed(actiavetEmail.getToken());
                r.setStatus(true);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                r.setStatusMessage("Email ativado com sucesso.");
            } else {
                r.setStatus(false);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INVALID_DATA);
                r.setStatusMessage("Sessão Expirada! Efetue novo link de ativação");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    @GET
    @Path("queryUser/{phoneNumber}")
    public MessageResponse queryUser(@PathParam("phoneNumber") Long phoneNumber) {
        MessageResponse r = new MessageResponse();
        if (getChannel() != null) {
            busMessage += phoneNumber + " - ";

            System.out.println(busMessage + "Consultando usuário " + phoneNumber);
            try {
                User u = new User();
                u.setPhoneNumber(phoneNumber);
                Integer status = UserDAO.getUserStatus(u);

                if (status == null) {
                    System.out.println(busMessage + "Usuário não registrado");
                    r.setStatus(false);
                    r.setStatusCode(BusResponseCodes.USER_QUERY_RESPONSE_INEXISTENT_USER);
                    r.setStatusMessage("Usuário não registrado na base.");
                } else if (status.equals(User.STATUS_ACTIVE)) {
                    System.out.println(busMessage + "Usuário registrado");
                    r.setStatus(true);
                    r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                    r.setStatusMessage("Usuário registrado.");
                } else if (status.equals(User.STATUS_BLOCKED)) {
                    System.out.println(busMessage + "Usuário bloqueado");
                    r.setStatus(false);
                    r.setStatusCode(BusResponseCodes.USER_QUERY_RESPONSE_BLOCKED_USER);
                    r.setStatusMessage("Usuário bloqueado.");
                }
            } catch (Exception e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                r.setStatus(false);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INVALID_DATA);
                r.setStatusMessage("Dados Inválidos.");
            }
        }
        return r;
    }

    @POST
    @Path("queryUser/{phoneNumber}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML + ";charset=UTF-8", MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    public MessageResponse queryUser(String user,
            @PathParam("phoneNumber") Long phoneNumber) {
        MessageResponse r = new MessageResponse();
        r.setStatus(false);
        if (getChannel() != null) {
            try {
                User up = new User();
                up.setPhoneNumber(phoneNumber);
                QueryUserWrapper wrapper = new QueryUserWrapper();
                QueryUser u = null;
                User authData = new User();
                authData.setPhoneNumber(phoneNumber);
                busMessage += up.getPhoneNumber() + " - ";
                System.out.println(busMessage + "Consultando usuário " + phoneNumber);
                if (user != null && !user.isEmpty()) {
                    authData = (User) (readBeanIO(user, "QueryUser"));
                    authData.setPhoneNumber(phoneNumber);
                }
                u = QueryUserDAO.getUser(getChannel(), getOrigin(), authData, true);
                //WebBlockingValidator wbv = new WebBlockingValidator();
                if (u != null) {
                    if (authData != null && authData.getNetworkOperator() != null) {
                        if (!authData.getNetworkOperator().equals(u.getNetworkOperator())) {
                            System.out.println(busMessage + "Houve mudança de operadora do usuário. Fazendo alteração: " + u.getNetworkOperator() + " > " + authData.getNetworkOperator());
                            UserDAO.updateNetworkOperator(u, authData.getNetworkOperator());
                        }
                    }
                    QueryUser qu = u;
                    wrapper.setUser(qu);
                    qu.setFormattedCEP(u.formatCEP());
                    qu.setFormattedCPF(u.formatCPF());
                    qu.setCardBannerDescription(Factory.getInstance(CardBanners.class).getSourceDescription(u.getCardBanner()));
                    qu.setNetworkOperatorDescription(Factory.getInstance(Operators.class).getSourceDescription(u.getNetworkOperator(), "B0").getOperatorName());
                    qu.setMediaDescription(Factory.getInstance(Media.class).getSourceDescription(u.getMedia()));
                    qu.setSourceDescription(Factory.getInstance(Origin.class).getSourceDescription(u.getSource()));
                    qu.setCardValid(u.isCardStillValid());
                    qu.setCreditCardLast4Digits(u.getCreditCard4LastDigits());
                    qu.setNeedsSecurityCode(u.isIndCVV());
                    qu.setclientHasTransactionInProgress(QueryUserDAO.checkTransactionsInProgress(qu.getPhoneNumber()));
                    qu.setFeeValue(u.getFeeValue());
                    qu.setMustChangePassword(u.isMustChangePassword());
                    
                    if (u.getStatus().equals(User.STATUS_CANCELLED)) {
                        System.out.println(busMessage + "Usuário cancelado");
                        r.setResponse(wrapper);
                        r.setStatusCode(BusResponseCodes.USER_QUERY_RESPONSE_CANCELLED);
                    } else if (u.getStatus().equals(User.STATUS_BLOCKED)) {
                        System.out.println(busMessage + "Usuário bloqueado");
                        r.setStatusMessage("Usuário bloqueado.");
                        r.setStatusCode(BusResponseCodes.USER_QUERY_RESPONSE_BLOCKED_USER);
                    } else if (!u.isPasswordValid()) {
                        QueryUser q = new QueryUser();
                        q.setIdUser(u.getIdUser());
                        q.setCreditCardLast4Digits(u.getCreditCardLast4Digits());
                        q.setFormattedCPF(u.formatCPF());
                        q.setCardBanner(u.getCardBanner());
                        q.setCardBannerDescription(Factory.getInstance(CardBanners.class).getSourceDescription(u.getCardBanner()));
                        q.setMustChangePassword(u.isMustChangePassword());
                        wrapper.setUser(q);
                        r.setResponse(wrapper);
                        r.setStatusCode(BusResponseCodes.USER_QUERY_RESPONSE_LOGIN_NOT_AUTHORIZED);
                        System.out.println(busMessage + "Usuário ou senha inválidos.");
                        r.setStatusMessage("Usuário ou senha inválidos.");
                    } else {
                        System.out.println(busMessage + "Buscando usuário " + u.getPhoneNumber());
                        //System.out.println(busMessage + "Consultando cartão com final " + u.getCreditCard4LastDigits());
                        // Prepares User Response. QueryUser is a formatted
                        // version of User
                        r.setStatus(true);
                        r.setResponse(wrapper);
                        r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                        System.out.println(busMessage + "Usuário autorizado");
                    }
                } else {
                    System.out.println(busMessage + "Usuário inexistente");
                    r.setStatusMessage("Usuário inexistente.");
                    r.setStatusCode(BusResponseCodes.USER_QUERY_RESPONSE_INEXISTENT_USER);
                }
            } catch (Exception e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatusMessage("Não foi possível consultar o usuário. Erro interno.");
            }
        }
        return r;
    }

    @POST
    @Path("transdataCharts")
    @Produces(MediaType.APPLICATION_JSON)
    public String transdataCharts() throws Exception{
        //preciso de 3 selects para retorno distintos:
        //1 para operações com sucesso
        //1 para opetações com erro
        //1 para operações pendentes
        //deve produzir um json com todas as informações acima.
        return "";
    }
 /*
    This method is the creator of new payment for Trasdata
    */
    @POST
    @Path("api/payment/create") //definido o caminho para chamar o serviço
    @Produces(MediaType.APPLICATION_JSON) //tipo de dado a ser produzido pelo serviço
    @Consumes(MediaType.APPLICATION_JSON) //tipo de dado a ser fornecido pelo serviço
	public String create(JsonObject inputJsonObj) throws Exception{ //assinatura do método que recebe um JsonObject via POST
        
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        //System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43

        System.out.println("objeto json recebido: "+ inputJsonObj);

        JsonObject order = (JsonObject) inputJsonObj.get("Order");//Nó de order
        JsonObject user = (JsonObject) inputJsonObj.get("User");//Nó de user
        Integer amountInCents;//valor da transação em centavos
        String strAmountInCents = inputJsonObj.get("AmountInCents").toString();
        amountInCents = Integer.parseInt(strAmountInCents);
        Integer feeInCents;
        String strFeeInCents = inputJsonObj.get("FeeInCents").toString();
        feeInCents = Integer.parseInt(strFeeInCents);
        JsonObject buyToken = (JsonObject) inputJsonObj.get("BuyToken");
        JsonObject creditCard = (JsonObject) inputJsonObj.get("CreditCard");//Nó de cartão de crédito
        JsonObject ticket = (JsonObject) inputJsonObj.get("Ticket");
        JsonObject history = (JsonObject) inputJsonObj.get("HistoryData");
        
        
        UserTransdata userTr = new UserTransdata();
        
        String banner = "";
        
        double valueAmount = (double) amountInCents ;
        
        float valueFee = (float) feeInCents;
        
        User u = new User();
        
        UserDAO uDAO = new UserDAO();

        TransactionWrapper transactionWrapper = new TransactionWrapper();

        List<Transaction> transactions = new ArrayList<Transaction>();
        
        Transaction transaction = new Transaction();
        
        
        if(creditCard != null){
            
            u.setName(creditCard.getString("HolderName"));
            System.out.println("NAME: "+u.getName());
            
            JsonObject billingAddress = (JsonObject) creditCard.get("BillingAddress");//Nó de endereço
            
            System.out.println("BILLING ADDRESS: "+billingAddress);

                        String mes1 = "";
            if(creditCard.get("ExpMonth") != null){
            switch(creditCard.get("ExpMonth").toString()){
                case "1":
                    mes1 = "01";
                break;
                case "2":
                    mes1 = "02";
                break;
                case "3":
                    mes1 = "03";
                break;
                case "4":
                    mes1 = "04";
                break;
                case "5":
                    mes1 = "05";
                break;
                case "6":
                    mes1 = "06";
                break;
                case "7":
                    mes1 = "07";
                break;
                case "8":
                    mes1 = "08";
                break;
                case "9":
                    mes1 = "09";
                break;
                case "10":
                    mes1 = "10";
                break;
                case "11":
                    mes1 = "11";
                break;
                case "12":
                    mes1 = "12";
                break;
            }
            
            u.setCardValidity(mes1+"/"+creditCard.get("ExpYear").toString());
            }else{
                u.setCardValidity("");
            }
            System.out.println(u.getCardValidity());
            if(!billingAddress.getString("ZipCode").equals("")){
                u.setCEP(Integer.parseInt(billingAddress.getString("ZipCode")));
                System.out.println("CEP: "+u.getCEP());
            }
            
            
            if(billingAddress.get("Number") != null){
                u.setNumber(billingAddress.get("Number").toString());
            }else{
                u.setNumber("0");
            }
            System.out.println(u.getNumber());
            u.setStreet(billingAddress.getString("Street"));
            System.out.println(u.getStreet());
            u.setDistrict(billingAddress.getString("District"));
            System.out.println(u.getDistrict());
            u.setCity(billingAddress.getString("City"));
            System.out.println(u.getCity());
            u.setState(billingAddress.getString("State"));
            System.out.println(u.getState());
            u.setComplement(billingAddress.getString("Complement"));
            System.out.println(u.getComplement());
            if(!"".equals(creditCard.getString("CreditCardNumber"))){
                u.setCreditCard(creditCard.getString("CreditCardNumber"));
            }
            else{
                u.setCreditCard(null);
            }
            System.out.println(u.getCreditCard());
            
            MessageDigest md = null;
            String bToken = "";
            
            md = MessageDigest.getInstance("MD5");
            //abef81b91547618983dfb9e8a5480f02
            
            BigInteger hash = new BigInteger(1, md.digest(creditCard.getString("CreditCardNumber").getBytes()));
            bToken = hash.toString(16);
            
            u.setBuyToken(bToken);
            System.out.println("BTOKEN: "+bToken);//setar Btoken para gravar na tuser_lead
            
            //Fazendo a validação da bandeira do cartão de crédito. E setando a numeração de acordo com a String de bandeira.
            //VISA\”,”MASTERCARD\”, ”AMEX\”, “DINERS\”, “DISCOVER nao tem”, “ELO”, “HIPERCARD”, “AURA\”

            if(creditCard.getString("CreditCardBrand").equals("MASTERCARD")){
                u.setCardBanner(1);
                System.out.println(u.getCardBanner());
            }
            else if(creditCard.getString("CreditCardBrand").equals("DINERS")){
                u.setCardBanner(2);
                System.out.println(u.getCardBanner());
            }
            else if(creditCard.getString("CreditCardBrand").equals("VISA")){
                u.setCardBanner(3);
                System.out.println(u.getCardBanner());
            }
            else if(creditCard.getString("CreditCardBrand").equals("AURA")){
                u.setCardBanner(4);
                System.out.println(u.getCardBanner());
            }
            else if(creditCard.getString("CreditCardBrand").equals("HIPERCARD")){
                u.setCardBanner(5);
                System.out.println(u.getCardBanner());
            }
            else if(creditCard.getString("CreditCardBrand").equals("AMEX")){
                u.setCardBanner(6);
                System.out.println(u.getCardBanner());
            }
            else if(creditCard.getString("CreditCardBrand").equals("ELO")){
                u.setCardBanner(10);
                System.out.println(u.getCardBanner());
            }
            
            transaction.setCvv(creditCard.getString("SecurityCode"));
            System.out.println("CVV: "+ transaction.getCvv());
            
            ////VISA\”,”MASTERCARD\”, ”AMEX\”, “DINERS\”, “DISCOVER nao tem”, “ELO”, “HIPERCARD”, “AURA\”
       if(u.getCardBanner() != null){
        switch(u.getCardBanner()){
            case 1:
                banner = "MASTERCARD";
            break;
            case 2:
                banner = "DINERS";
            break;
            case 3:
                banner = "VISA";
            break;
            case 4:
                banner = "AURA";
            break;
            case 5:
                banner = "HIPERCARD";
            break;
            case 6:
                banner = "AMEX";
            break;
            case 10:
                banner = "ELO";
            break;
            default:
                banner = "";
        }
       }
        }
        else{
            uDAO.userLeadInfos(buyToken.getString("Token"));
            
            u.setEncryptedCard(userTr.getCdCardEncrypted());
            System.out.println("ENCRIPTED CARD: "+u.getEncryptedCard());
            transaction.setCvv(buyToken.getString("SecurityCode"));
            System.out.println("CVV: "+transaction.getCvv());
                     
            String mes = "";
            
            switch(userTr.getNrCardValidityMonth()){
                case "1":
                    mes = "01";
                break;
                case "2":
                    mes = "02";
                break;
                case "3":
                    mes = "03";
                break;
                case "4":
                    mes = "04";
                break;
                case "5":
                    mes = "05";
                break;
                case "6":
                    mes = "06";
                break;
                case "7":
                    mes = "07";
                break;
                case "8":
                    mes = "08";
                break;
                case "9":
                    mes = "09";
                break;
                case "10":
                    mes = "10";
                break;
                case "11":
                    mes = "11";
                break;
                case "12":
                    mes = "12";
                break;
            }
            
            u.setCardValidity(mes+"/"+userTr.getNrCardValidityYear());
            System.out.println(u.getCardValidity());
            u.setCEP(Integer.parseInt(userTr.getIdCep()));
            System.out.println(u.getCEP());
            u.setNumber(userTr.getCdAddress());
            System.out.println(u.getNumber());
            u.setStreet(userTr.getNmStreet());
            System.out.println(u.getStreet());
            u.setDistrict(userTr.getNmDistrict());
            System.out.println(u.getDistrict());
            u.setCity(userTr.getDsCity());
            System.out.println(u.getCity());
            u.setState(userTr.getUfState());
            System.out.println(u.getState());
            u.setComplement(userTr.getDsComplement());
            System.out.println(u.getComplement());
            u.setCreditCard(Crypt.decrypt(userTr.getCdCardEncrypted()));
            System.out.println(u.getCreditCard());
            u.setCardBanner(Integer.parseInt(userTr.getIdCardBanner()));
            u.setBuyToken(userTr.getBuyToken());
            u.setName(userTr.getName());
            System.out.println("NAME: "+u.getName());
            
            switch(Integer.parseInt(userTr.getIdCardBanner())){
            case 1:
                banner = "MASTERCARD";
            break;
            case 2:
                banner = "DINERS";
            break;
            case 3:
                banner = "VISA";
            break;
            case 4:
                banner = "AURA";
            break;
            case 5:
                banner = "HIPERCARD";
            break;
            case 6:
                banner = "AMEX";
            break;
            case 10:
                banner = "ELO";
            break;
            default:
                banner = "";
        }

        }
        System.out.println("SERVICO: "+getService());
        System.out.println("ORIGEM: "+getOrigin());
        System.out.println("CANAL: "+getChannel());
        System.out.println("PRODUTO: "+getProduct());
        System.out.println("ID SOURCE: "+getService().getIdSource());
        System.out.println("USER ID: "+user.getString("Id"));
        
                
        //Setando as informações do usuário da transação
        //u.setFeeValue(uDAO.transactionFeeValue(Integer.parseInt(getService().getIdSource().toString()))); 
        //System.out.println("FEE VALUE: "+u.getFeeValue());
        u.setFeeValue(valueFee/100); 
        System.out.println("FEE VALUE: "+u.getFeeValue());
        
        u.setCdUserLead(user.getString("Id"));
        System.out.println("ID USER: "+u.getIdUser());
        //u.setIdUser(Long.parseLong(user.getString("Id")));
        u.setCPF(Long.parseLong(user.getString("DocumentNumber")));
        System.out.println("CPF: "+u.getCPF());
        u.setSource(getService().getIdSource());
        System.out.println("SOURCE: "+ u.getSource());
        
        String completePhone = "";
        if(!"".equals(inputJsonObj.getString("CellphoneNumber"))){
       /* String ddd = inputJsonObj.getString("CellphoneNumber").substring(0, 2);
        System.out.println("ddd -> "+ddd);
        String phone = inputJsonObj.getString("CellphoneNumber").substring(2, 11);
        System.out.println("phone -> "+phone);*/
        u.setPhoneNumber(Long.parseLong(inputJsonObj.getString("CellphoneNumber")));
            //completePhone = ddd+phone;
        
        //u.setPhoneNumber(Long.parseLong(completePhone));
        }
        else{
            completePhone = "";
            //u.setPhoneNumber();
        }
        //System.out.println(completePhone);
        
                  
        System.out.println("PHONE NUMBER: "+u.getPhoneNumber());
        
        //199876543
        //55 19 998765432
        
        u.setTicketNumber(ticket.getString("Number"));
        System.out.println("TICKET NUMBER: "+ u.getTicketNumber());
        u.setTicketOwner(ticket.getString("OwnerName"));
        System.out.println("TICKET OWNER: "+ u.getTicketOwner());
        u.setTicketType(ticket.getString("Type"));
        System.out.println("TICKET TYPE: "+ u.getTicketType());
        
        u.setHistoryFlag(history.getString("HistoryFlag"));
        System.out.println("HISTORY FLAG: "+ u.getHistoryFlag());

        
        //System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
        
        //u.setRegistration(dateFormat.format(date));
        u.setRegistration(order.getString("Date"));
        //AQUI
        
        transaction.setAmount(valueAmount/100);
        System.out.println("AMOUNT: "+transaction.getAmount());
        
        
        transactionWrapper.setUser(u);
        
        System.out.println("TRANSACTION WRAPPER: "+transactionWrapper);
        

        
        System.out.println("TRANSACTION "+transaction);
        
        transactions.add(transaction);
        
        System.out.println("TRANSACTIONS: "+transactions);
        
        transactionWrapper.setTransactions(transactions);
        
        transactionWrapper.setWaitReply(Boolean.FALSE);
        //seta todo ttransaction wrapper e chama a facade depois 
        
        //AQUI
        
         System.out.println("Transação cartão  " + transactionWrapper.getUser().getMaskedCard() 
                        + "\n Valor: "+ transactionWrapper.getTransactions().get(0).getAmount()
                        + "\n Cliente " + getOrigin().getDsOrigin()
                        + "\n Serviço " + getService().getDsService());
                
                //NECESSÁRIO PARA DEFINIR MULTI BANDEIRA   
        if(transactionWrapper.getUser().getNetworkOperator() == null){
                    transactionWrapper.getUser().setNetworkOperator("001");//VIVO
                }
                
        new Facade(origin, channel, product, service).process(transactionWrapper);
        
        String retorno = transactionWrapper.toString();
        /*
        tenho que pegar o retorno de transactionWrapper e montar a resposta de acordo com o json esperado
        tambem tenho que tratar o json conforme a informação completa de cartão ou não;
        */
        
        //Validar erro da transaction.
        //Verificar retorno desse método no wildfly.
        String response = "";
        System.out.println("CONFIRM: "+transaction.getConfirm());
        //if(transaction.getConfirm() == true || transaction.getConfirm() != null){
        if(creditCard != null){
            if(!history.getString("HistoryFlag").equals("YES")){
            uDAO.insertUserLead(u);
            }
        }
            response = "{\n" +
                        "	\"Error\": {\n" +
                        "		\"Code\": "+transaction.getStatusCode()+",\n" +
                        "		\"Message\": "+transaction.getResponse()+"\n" +
                        "	},\n" + 
                        "	\"PaymentId\": \""+transaction.getId()+"\",\n" +
                        "	\"PaymentDate\": \""+dateFormat.format(date)+"\",\n" +//dateFormat.format(date)
                        "	\"BuyToken\": \""+u.getBuyToken()+"\",\n" +
                        "	\"MaskedCC\": \""+u.getMaskedCard()+"\",\n" +
                        "	\"CreditCardBrand\": \""+banner+"\"\n" +
                        "}";
       // }
        //else if(transaction.getConfirm() == false || transaction.getConfirm() == null){
       /*     response = "\n" +
"	\"Error\": {\n" +
"		\"Code\": "+transaction.getStatusCode()+",\n" +
"		\"Message\": "+transaction.getResponse()+"\"\"\n" +
"	}\n"; 
        }*/
        //System.out.println("DATA: "+transactionWrapper.getTransactions().get(0).getDate());
        
        return response; //retorno do método para o client **transactionWrapper.toString() para validar primeiro o retorno do transactionWrapper 
    }
    
    @GET
    @Path("updateRecurrence")
    public String updateRecurrence() {
        TransactionDAO tDao = new TransactionDAO();
        tDao.updateRecurrences();
        new ScheduledServices().recoverEDIStatus();
        return "200 OK";
    }

    @POST
    @Path("cancelUser/{phoneNumber}")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public MessageResponse cancelUser(String user, @PathParam("phoneNumber") Long phoneNumber) {
        MessageResponse r = new MessageResponse();
        r.setStatus(false);

        busMessage += phoneNumber + " - ";
        System.out.println(busMessage + "Cancelando usuário");
        if (getChannel() != null) {
            try {
                User u = null;
                if (getChannel().isPasswordRequired()) {
                    User authData = (User) (readBeanIO(user, "CancelUser"));
                    u = (User) UserDAO.getUser(phoneNumber, authData.getPassword());
                } else {
                    u = (User) UserDAO.getUserNoPwd(phoneNumber);
                }

                if (u != null) {
                    UserDAO.cancel(u);
                    r.setStatus(true);
                    r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                    System.out.println(busMessage + "Registro cancelado com sucesso");
                } else {
                    System.out.println(busMessage + "Acesso não autorizado ou usuário não existe");
                    r.setStatusMessage("Acesso não autorizado ou usuário não existe.");
                    r.setStatusCode(BusResponseCodes.USER_CANCEL_RESPONSE_NONEXISTENT);
                }
            } catch (Exception e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatusMessage("Não foi possível cancelar o usuário. Erro interno.");
            }
        }
        return r;
    }

    @POST
    @Path("blockUser/{phoneNumber}")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public MessageResponse blockUser(String user, @PathParam("phoneNumber") Long phoneNumber) {
        MessageResponse r = new MessageResponse();
        r.setStatus(false);

        busMessage += phoneNumber + " - ";
        System.out.println(busMessage + "Bloqueando usuário " + phoneNumber);
        if (getChannel() != null) {
            try {
                User u = (User) (readBeanIO(user, "BlockUser"));

                UserDAO.block(u.getIdUser());
                r.setStatus(true);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                r.setStatusMessage("Usuário bloqueado com sucesso.");
                System.out.println(busMessage + "Registro bloqueado com sucesso");

            } catch (Exception e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatusMessage("Não foi possível bloquear o usuário. Erro interno.");
            }
        }
        return r;
    }

    @POST
    @Path("unblockUser/{phoneNumber}")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public MessageResponse unblockUser(String user, @PathParam("phoneNumber") Long phoneNumber) {
        MessageResponse r = new MessageResponse();
        r.setStatus(false);
        busMessage += phoneNumber + " - ";
        System.out.println(busMessage + "Desbloqueando usuário " + phoneNumber);
        if (getChannel() != null) {
            try {
                User u = (User) (readBeanIO(user, "BlockUser"));

                UserDAO.unblock(u.getIdUser());
                r.setStatus(true);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                r.setStatusMessage("Usuário desbloqueado com sucesso.");
                System.out.println(busMessage + "Registro desbloqueado com sucesso");

            } catch (Exception e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatusMessage("Não foi possível desbloquear o usuário. Erro interno.");
            }
        }
        return r;
    }

    @POST
    @Path("requestRecharge/{idUser}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public MessageResponse requestRecurrence(String request, @PathParam("idUser") Long idUser) {
        TransactionControl transactionControl = new TransactionControl();
        MessageResponse r = new MessageResponse();
        r.setStatus(false);
        RequestRecurrence recurrence = null;
        if (getService() != null) {
            try {
                recurrence = (RequestRecurrence) (readBeanIO(request, "RequestRecurrence"));
                User u = QueryUserDAO.getUser(getChannel(), getOrigin(), idUser, false);
                if (transactionControl.processTransaction(this, recurrence, u)) {
                    r.setStatus(true);
                    r.setStatusMessage("Transação efetuada com sucesso!");
                } else {
                    r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                    r.setStatusMessage("Erro Interno");
                }
            } catch (IOException ex) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return r;
    }

    @POST
    @Path("requestRecharge/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public MessageResponse requestRecharge(String request) {
        System.out.println("Request -" + request);
        RechargeRequest rechargeRequest = null;
        MessageResponse r = new MessageResponse();
        boolean stepStatus = false;
        r.setStatus(false);
        if (getService() != null) {
            try {
                rechargeRequest = (RechargeRequest) (readBeanIO(request, "RequestTransaction"));
                rechargeRequest.setSource(getService().getIdSource());
                stepStatus = true;
            } catch (Exception e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatusMessage("Não foi possível executar a transação. Erro interno: Validação de requisição.");
            }

            /*
             * Gets Client Information and tries login
             */
            QueryUser u = null;
            // Data de Solicitação
            long requestTime = System.currentTimeMillis();
            if (stepStatus) {
                stepStatus = false;
                try {
                    busMessage += rechargeRequest.getPhoneNumber() + " - ";
                    System.out.println(busMessage + "Iniciando solicitação de recarga. Valor: " + rechargeRequest.getValue());
                    User auth = new User();
                    auth.setPhoneNumber(rechargeRequest.getPhoneNumber());
                    auth.setPassword(rechargeRequest.getPassword());
                    if (rechargeRequest.getToken() != null) {
                        auth.setAuthClient(new AuthClient());
                        auth.getAuthClient().setToken(rechargeRequest.getToken());
                    }
                    u = QueryUserDAO.getUser(getChannel(), getOrigin(), auth, false);

                    if (u != null && u.isPasswordValid()) {
                        rechargeRequest.setFee(u.getFeeValue());
                        if (u.getStatus().equals(User.STATUS_BLOCKED)) {
                            System.out.println(busMessage + "Usuário bloqueado");
                            r.setStatusCode(BusResponseCodes.REQUEST_RECHARGE_RESPONSE_BLOCKED_USER);
                            r.setStatusMessage("Usuário está bloqueado.");
                        } else {
                            // User Authorized to request transaction
                            stepStatus = true;
                        }
                    } else {
                        System.out.println(busMessage + "Acesso negado");
                        r.setStatusCode(BusResponseCodes.REQUEST_RECHARGE_RESPONSE_LOGIN_NOT_AUTHORIZED);
                        r.setStatusMessage("Acesso não autorizado.");
                    }
                } catch (Exception e) {
                    Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                    r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                    r.setStatusMessage("Não foi possível executar a transação. Erro interno: Busca de registro de usuário.");
                }
            }

            /*
             * Prepares XML and sends Transaction Request to Prometheus
             */
            if (stepStatus) {
                if (Factory.getInstance(Operators.class).isOperatorUp(u.getNetworkOperator(), "B0")) {
                    try {
                        System.out.println(busMessage + "Preparando XML para Prometheus");

                        DefaultRequests.PreparedRequest req = DefaultRequests.standardTransaction(rechargeRequest.getLifeCycle(), rechargeRequest, u);
                        req.getOperatorsList().entrySet().stream().forEach((s) -> {
                            System.out.println(busMessage + "\tMultioperador " + s.getKey() + ": " + s.getValue());
                        });

                        PrometheusConnector.Options pOptions = new PrometheusConnector.Options();
                        pOptions.enable(PrometheusConnector.Options.CONCILIABLE);
                        PrometheusResponse pr = (new PrometheusConnector()).makeUnicodeRequest(req.getPreparedXML(), pOptions);

                        // Registers data on TRECHARGE
                        if (pr.isDone()) {
                            rechargeRequest.setNetworkOperator(u.getNetworkOperator());
                            System.out.println(busMessage + "Registrando transação " + pr.getTID());
                            (new TransactionDAO()).insert(rechargeRequest.getLifeCycle(), pr.getTID(), rechargeRequest, requestTime, u, req);

                            r.setStatus(true);
                            r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                            r.setResponse(pr.getXmlResponse());
                        } else {
                            r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                            r.setStatusMessage("Transação não efetuada");
                            System.out.println(busMessage + "Transação não efetuada");
                        }
                    } catch (Exception e) {
                        Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                        r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                        r.setStatusMessage("Não foi possível executar a transação. Erro interno: Início de requisição.");
                    }
                } else {
                    r.setStatusMessage("Serviço indisponível!\nPor favor, tente novamente mais tarde.");
                    r.setStatusCode(BusResponseCodes.SYSTEM_INACTIVE);
                }
            }

        }
        return r;
    }

    @POST
    @Path("/{a:requestOffRecharge|SA/requestOffRecharge}")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public MessageResponse requestOffRecharge(String request) {
        OffRechargeRequest rechargeRequest = null;

        MessageResponse r = new MessageResponse();
        r.setStatus(false);

        try {
            rechargeRequest = (OffRechargeRequest) (readBeanIO(request, "RequestOffRecharge"));
        } catch (Exception e) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
            r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
            r.setStatusMessage("Não foi possível executar a transação. Erro interno: Validação de requisição.");
            return r;
        }

        /*
         * Authenticate using a password
         */
        if (!rechargeRequest.auth()) {
            r.setStatusMessage("Autenticação mal-sucedida");
            r.setStatusCode(BusResponseCodes.REQUEST_RECHARGE_RESPONSE_LOGIN_NOT_AUTHORIZED);
            return r;
        }

        /*
         * Prepares XML and sends Transaction Request to Prometheus
         */
        long requestTime = System.currentTimeMillis();
        try {
            System.out.println(busMessage + "Preparando XML para Prometheus - Recarga Off");
            PrometheusConnector.Options pOptions = new PrometheusConnector.Options();
            pOptions.enable(PrometheusConnector.Options.CONCILIABLE);

            DefaultRequests.PreparedRequest req = DefaultRequests.id_a8550afcd88e24a7ab8e6464e91a12d1(rechargeRequest);

            req.getOperatorsList().entrySet().stream().forEach((s) -> {
                System.out.println(busMessage + "\tMultioperador " + s.getKey() + ": " + s.getValue());
            });
            PrometheusResponse pr = (new PrometheusConnector()).makeUnicodeRequest(req.getPreparedXML(), pOptions);

            // Registers data on TRECHARGE
            if (pr.isDone()) {
                System.out.println(busMessage + "Registrando transação (Recarga Off) " + pr.getTID());

                (new TransactionDAO()).insert("a8550afcd88e24a7ab8e6464e91a12d1", pr.getTID(), rechargeRequest, requestTime, null, req);

                r.setStatus(true);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                r.setResponse(pr.getXmlResponse());
            } else {
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatusMessage("Transação não efetuada - Recarga Off");
                System.out.println(busMessage + "Transação não efetuada - Recarga Off");
            }
        } catch (Exception e) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
            r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
            r.setStatusMessage("Não foi possível executar a transação. Erro interno: Início de requisição.");
        }
        return r;
    }

    @POST
    @Path("requestTransaction/{phoneNumber}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML + ";charset=UTF-8", MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    public TransactionWrapper requestTransaction(@PathParam("phoneNumber") Long phoneNumber, TransactionWrapper transactionWrapper) {
        System.out.println("RequestTransaction to " + phoneNumber);
        if (getService() != null) {
            try {
                User u = UserDAO.getUserNoPwd(phoneNumber);
                System.out.println("User object is: " + u);
                try {
                    if (transactionWrapper != null) {
                        transactionWrapper.setUser(u);
                        new Facade(origin, channel, product, service).process(transactionWrapper);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(TransactionWrapper.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (Exception ex) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return transactionWrapper;
    }

   
    
    @POST
    @Path("requestTransaction")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML + ";charset=UTF-8", MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    public MessageResponseGateway requestTransaction(TransactionWrapper transactionWrapper) {
        MessageResponseGateway response = new MessageResponseGateway();
        response.setStatus(false);
        
        if (getService() != null) {

            if((transactionWrapper != null)
                    &&(transactionWrapper.getTransactions().size()>0)
                         &&(transactionWrapper.getUser() != null)){
                
                //transactionWrapper.getUser().setCreditCard(transactionWrapper.getUser().getCreditCard());
                System.out.println("Transação cartão  " + transactionWrapper.getUser().getMaskedCard() 
                        + "\n Valor: "+ transactionWrapper.getTransactions().get(0).getAmount()
                        + "\n Cliente " + getOrigin().getDsOrigin()
                        + "\n Serviço " + getService().getDsService());
                
                //NECESSÁRIO PARA DEFINIR MULTI BANDEIRA
                if(transactionWrapper.getUser().getNetworkOperator() == null){
                    transactionWrapper.getUser().setNetworkOperator("001");//VIVO
                }
                
                try {
                    new Facade(origin, channel, product, service).process(transactionWrapper);
                    
                    if(transactionWrapper.getTransactions().size() > 0){
                     
                        response.setTid(transactionWrapper.getTransactions().get(0).getId());
                        if(transactionWrapper.getTransactions().get(0).getResponse().size() > 1){
                            for(br.com.jcnsistemas.jcn.busconnector.to.jaxb.Response resp : transactionWrapper.getTransactions().get(0).getResponse()){
                                //FORMATAÇÃO PARA NEGATIVA DE ANALISE DE RISCO
                                if(resp.getIdentifier().equals("ANALISE_CREDITO_BOAVISTA") &&
                                        resp.getCode().equals("-1")){
                                    
                                    resp.setStatus(false);
                                    resp.setMessage("Credit Analysis Failed");
                                    resp.setIdentifier("PAGAMENTO");
                                    response.setResponse(resp);
                                }
                                else if(resp.getIdentifier().equals("ANALISE_CREDITO_BOAVISTA") &&
                                        resp.getCode().equals("5")){
                                    
                                    resp.setStatus(false);
                                    resp.setMessage("Negada Análise");
                                    resp.setIdentifier("PAGAMENTO");
                                    resp.setCode("-1");
                                    response.setResponse(resp);
                            }
                                 else if(resp.getIdentifier().equals("ANALISE_CREDITO_BOAVISTA") &&
                                        resp.getCode().equals("4")){
                                    response.setResponse(transactionWrapper.getTransactions().get(0).getResponse().get(0));
                                    
                        }else{
                                    response.setResponse(transactionWrapper.getTransactions().get(0).getResponse().get(0));
                                }
                            }
                        }else{
                            
                            response.setResponse(transactionWrapper.getTransactions().get(0).getResponse().get(0));
                        }
                        System.out.println("Response TID " + response.getTid() + " - " + transactionWrapper.getTransactions().get(0).getResponse().get(0).getMessage());
                        response.setStatus(true);
                        response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                        response.setStatusMessage("Transação recebida com sucesso!");
                        
                    }else{
                        
                        response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                        response.setStatusMessage("Não foi possível executar a transação. Erro interno: Início de requisição.");
                    }               
                } catch (Exception e) {
                    Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                    response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                    response.setStatusMessage("Não foi possível executar a transação. Erro interno: Início de requisição.");
                }
            }else{
                response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INVALID_DATA);
                response.setStatusMessage("Erro no envio de dados");
            }
        }
        
        return response;
    }
    
    @POST
    @Path("SA/requestPayment")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    public MessageResponse requestPayment(String request) {
        br.com.jcnsistemas.jcn.busconnector.to.PaymentRequest paymentRequest = null;
        br.com.jcnsistemas.jcn.busconnector.to.TransactionRequest transactionRequest = new br.com.jcnsistemas.jcn.busconnector.to.TransactionRequest();
        MessageResponse r = new MessageResponse();
        r.setStatus(false);
        if (getService() != null) {
            try {
                paymentRequest = (br.com.jcnsistemas.jcn.busconnector.to.PaymentRequest) (readBeanIO(request, "RequestPayment"));
                paymentRequest.setSource(getService().getIdSource());
            } catch (Exception e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatusMessage("Não foi possível executar a transação. Erro interno: Validação de requisição.");
                return r;
            }

            /*
             * Authenticate using a password
             */
            /*
             * Prepares XML and sends Transaction Request to Prometheus
             */
            try {
                User u = UserDAO.getUserNoPwd(paymentRequest.getPhoneNumber());

                if (u != null) {
                    System.out.println(busMessage + "Preparando XML para Prometheus - Pagamento");
                    PrometheusConnector.Options pOptions = new PrometheusConnector.Options();
                    pOptions.enable(PrometheusConnector.Options.WAIT_REPLY);
                    transactionRequest.getPaymentRequest().add(paymentRequest);
                    PrometheusResponse pr = (new PrometheusConnector()).makeUnicodeRequest(DefaultRequests.transaction(getService(), transactionRequest, u), pOptions);
                    if (pr.isDone()) {
                        r.setStatus(true);
                        r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                        r.setResponse(pr.getXmlResponse());
                    } else {
                        r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                        r.setStatusMessage("Não foi possível executar a transação. Erro interno: Início de requisição - Pagamento SysAuth.");
                    }
                } else {
                    System.out.println(busMessage + "Autenticação negada - Pagamento SysAuth");
                    r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                    r.setStatusMessage("Autenticação Negada - Pagamento SysAuth");
                }
            } catch (Exception e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
                r.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_INTERNAL_ERROR);
                r.setStatusMessage("Não foi possível executar a transação. Erro interno: Início de requisição - Pagamento SysAuth.");
            }
        }
        return r;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("rechargeValues/B0/{networkOperator}")
    public synchronized MessageResponse requestRechargeValues(@PathParam("networkOperator") String networkOperator) {
        HashMap<Integer, ArrayList<Float>> request = Factory.getInstance(RechargeValues.class).getValuesByOperator(networkOperator);

        MessageResponse response = new MessageResponse();
        response.setStatus(true);
        response.setResponse(request.toString());

        return response;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("rechargeValues/B0/{networkOperator}/{ddd}")
    public synchronized MessageResponse requestRechargeValues(@PathParam("networkOperator") String networkOperator, @PathParam("ddd") Integer ddd) {
        ArrayList<Float> request = Factory.getInstance(RechargeValues.class).getValuesByOperatorAndRegion(networkOperator, ddd);

        MessageResponse response = new MessageResponse();
        response.setStatus(true);
        response.setResponse(request.toString().replaceAll("[\\[\\]]", "").replaceAll(", ", "|"));
        return response;
    }

    @GET
    @Path("rechargeValues/json/B0/{networkOperator}/{ddd}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public synchronized ArrayList<Float> requestRechargeValuesJSON(@PathParam("networkOperator") String networkOperator, @PathParam("ddd") Integer ddd) {
        ArrayList<Float> r = Factory.getInstance(RechargeValues.class).getValuesByOperatorAndRegion(networkOperator, ddd);
        if (r == null || r.isEmpty()) {
            return null;
        } else {
            return r;
        }
    }

    @GET
    @Path("rechargeValuesAndFee/B0/{networkOperator}/{ddd}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public synchronized RechargeValuesResponse requestRechargeValuesAndFeeJSON(@PathParam("networkOperator") String networkOperator, @PathParam("ddd") Integer ddd) {
        return Factory.getInstance(RechargeValues.class).getValuesAndFeeByOperatorAndRegion(networkOperator, ddd);
    }

    @POST
    @Path("creditCardQuery/")
    public MessageResponse requestCreditCardQuery(@FormParam("cartao") String cartao, @FormParam("validade") String validade) {
        MessageResponse response = new MessageResponse();
        response.setStatus(false);
        if (cartao == null || cartao.length() == 0) {
            response.setStatusCode(BusResponseCodes.CREDIT_CARD_QUERY_RESPONSE_INVALID_CARD);
            response.setStatusMessage("Cartão de crédito inválido.");
            return response;
        }

        if (validade == null) {
            validade = "1299";
        }
        /*
         * Verifica se cartão é Cartão Mais. Em caso positivo, testa com
         * algorítmo exclusivo
         */
        Validator validator = new Validator();
        if (cartao.substring(0, 6).equals("628028")) {

            //System.out.println("e: " + expectedLast + "; l: " + last);
            if (validator.credSystemCCValidator(cartao)) {
                response.setStatusMessage("Consulta realizada com sucesso - Cartão Mais");
                response.setStatus(true);
                response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                response.setStatusMessage("Consulta realizada com sucesso");
                response.setResponse("<response><bandeiraCartao>0</bandeiraCartao><CSEG>1</CSEG></response>");
            } else {
                System.out.println(busMessage + "Dígito verificador do cartão é inválido - Cartão Mais");
                response.setStatusCode(BusResponseCodes.CREDIT_CARD_QUERY_RESPONSE_INVALID_CARD);
                response.setStatusMessage("Cartão de crédito inválido.");
            }

            return response;
        } else {
            /* -----------------------------------------------------------------
             * Implementação do algoritmo de Luhn para verificação do 
             * número de cartão de crédito
             */

            if (!validator.luhn(cartao)) {
                System.out.println(busMessage + "Dígito verificador do cartão é inválido.");
                response.setStatusCode(BusResponseCodes.CREDIT_CARD_QUERY_RESPONSE_INVALID_CARD);
                response.setStatusMessage("Cartão de crédito inválido.");
                return response;
            }

            /* Fim do algoritmo verificador 
             * ---------------------------------------------------------------------
             */
        }

        PrometheusResponse r;
        PrometheusConnector prometheus = new PrometheusConnector();
        try {
            System.out.println(busMessage + "Tentando identificar cartão de crédito (networkOperator 000)...");
            PrometheusConnector.Options pOptions = new PrometheusConnector.Options();
            pOptions.enable(PrometheusConnector.Options.WAIT_REPLY);
            r = prometheus.makeUnicodeRequest(DefaultRequests.E0(cartao, validade, "000"), pOptions);

            // Throws a NullPointerException in case of a bad request. In this
            // case, will try again
            // System.out.println("CreditCardQuery Response (000): " + r.getXmlResponse());
            if (prometheus.parseXMLDocument(r.getXmlResponse()).getElementsByTagName("bandeiraCartao").item(0).getTextContent().equals("")) {
                System.out.println(busMessage + "Consulta mal-sucedida. Retry.");
                throw new Exception("");
            } else {
                System.out.println(busMessage + "Consulta realizada com sucesso.");
                response.setStatus(true);
                response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                response.setStatusMessage("Consulta realizada com sucesso");
                response.setResponse(r.getXmlResponse());
            }
        } catch (Exception e) {
            //Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
            try {
                // Retries with a new networkOperator
                System.out.println(busMessage + "Tentando identificar cartão de crédito (networkOperator 001)...");
                PrometheusConnector.Options pOptions = new PrometheusConnector.Options();
                pOptions.enable(PrometheusConnector.Options.WAIT_REPLY);
                r = prometheus.makeUnicodeRequest(DefaultRequests.E0(cartao, validade, "001"), pOptions);

                //System.out.println("CreditCardQuery Response (001): " + r.getXmlResponse());
				/* If a second Exception is thrown, the response is set to ERROR */
                if (prometheus.parseXMLDocument(r.getXmlResponse()).getElementsByTagName("bandeiraCartao").item(0).getTextContent().equals("")) {
                    System.out.println(busMessage + "Consulta mal-sucedida. Cartão é inválido.");
                    throw new Exception("");
                } else {
                    response.setStatusMessage("Consulta realizada com sucesso");
                    response.setStatus(true);
                    response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
                    response.setStatusMessage("Consulta realizada com sucesso");
                    response.setResponse(r.getXmlResponse());
                }
            } catch (Exception ex) {
                //Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
                response.setStatus(false);
                response.setStatusCode(BusResponseCodes.CREDIT_CARD_QUERY_RESPONSE_INVALID_CARD);
                response.setResponse("");
                response.setStatusMessage("Cartão de Crédito Inválido");
            }
        }

        return response;
    }

    public static void testeQuery(String[] args) throws Exception {
        String xml = "<QueryUser><Auth><password>5912</password></Auth></QueryUser>";

        xml = "<RechargeRequest>\n"
                + "	<lifeCycle>a1d0c6e83f027327d8461063f4ac58a6</lifeCycle>\n"
                + "	<source>1</source>\n"
                + "	<User>\n"
                + "		<phoneNumber>11974075547</phoneNumber>\n"
                + "		<password>5912</password>\n"
                + "	</User>\n"
                + "	<CreditAnalysis>\n"
                + "		<sessionID>kírkpe</sessionID>\n"
                + "	</CreditAnalysis>\n"
                + "	<Recharge>\n"
                + "		<networkOperator>001</networkOperator>"
                + "		<phoneNumber>11900011234</phoneNumber>"
                + "		<value>1000</value>\n"
                + "	</Recharge>\n<Payment />"
                + "</RechargeRequest>";

        xml = "<Auth>\n"
                + "	<password>5911</password>\n"
                + "	<networkOperator>001</networkOperator>\n"
                + "</Auth>";

        //xml = "<UserDataUpdate><currentPassword>5912</currentPassword><User><cardNumber>4108637623680108</cardNumber><password>5912</password></User></UserDataUpdate>";
        //System.out.println(new EntryPoint().requestRechargeValues("001", 11));
        //System.out.println(new EntryPoint().queryUser(xml, 11974075547L));
        //System.out.println(new EntryPoint().queryUser(11988238411L));
        //System.out.println(new EntryPoint().requestRechargeValuesJSON("001", 2));
        //System.out.println(new EntryPoint().updateUser(xml, 11974075547L));
        //System.out.println(new EntryPoint().requestRecharge(xml));
        //EntryPoint ep = new EntryPoint();
        //System.out.println(ep.requestCreditCardQuery("5096029320079482", "0916"));
        //	Trigger.execute();
    }

    /* --- Private Members --- */
    private Object readBeanIO(String payload, String mapper) throws IOException {
        Object o;
        StreamFactory factory = StreamFactory.newInstance();
        try (InputStream is = new URL("http://" + EntryPoint.getProperties("SysConfig").getProperty("PROMETHEUS_CONFIG.HOST") + ":88/jcn/bus/mappings/Mappings.xml").openStream()) {
            factory.load(is);
            BeanReader br = factory.createReader(mapper, new StringReader(payload));
            o = br.read();
            br.close();
        }

        return o;
    }

    private String __writeBeanio(Object obj, String layout) {
        StreamFactory factory = StreamFactory.newInstance();
        String url = "http://" + EntryPoint.getProperties("SysConfig").getProperty("PROMETHEUS_CONFIG.HOST") + ":88/jcn/bus/mappings/Mappings.xml";
        InputStream stream = null;

        String ret = null;

        try {
            stream = new URL(url).openStream();
        } catch (Exception ex) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (stream != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(out);
            BeanWriter writer = null;

            try {
                factory.load(stream);
                writer = factory.createWriter(layout, osw);
                writer.write(obj);
                writer.close();

                ret = out.toString();
            } catch (BeanIOConfigurationException | IOException e) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                try {
                    osw.close();
                    out.close();
                    stream.close();
                } catch (IOException e) {
                }
            }
        }

        return ret;
    }

    public static void main__(String[] args) throws Exception {
        try {
            //String xml = "<UserDataUpdate><idUser>3510669</idUser><User><cardNumber>4024007180833336</cardNumber><cardValidity>0622</cardValidity><networkOperator>001</networkOperator></User></UserDataUpdate>";
            /*String xml = "<PaymentRequest>\n"
             + " <lifeCycle>5b6393803717c63782bab2d012c0a17f</lifeCycle>\n"
             + " <source>6</source>\n"
             + "<sessionID>Teste234_234</sessionID>"
             + " <moduleOperator>005</moduleOperator>\n"
             + " <Auth>\n"
             + "  <password>JCNfhoas9013</password>\n"
             + " </Auth>\n"
             + " <PaymentData>\n"
             + "  <value>1000</value>\n"
             + " </PaymentData>\n"
             + " <UserData>\n"
             + "  <phoneNumber>11974075547</phoneNumber>\n"
             + " </UserData>\n"
             + "</PaymentRequest>";*/
            String xml = "<transactions partner_id= \"teste\">"
                    + "	<transaction>\n"
                    + "<cvv>163</cvv>"
                    + "<amount>90.00</amount>"
                    + "<times>2</times>"
                    + "	</transaction>\n"
                    + "	<transaction>\n"
                    + "<cvv>163</cvv>"
                    + "<amount>90.00</amount>"
                    + "<times>1</times>"
                    + "	</transaction>\n"
                    + "</transactions>";
            String xml1 = "<transactions partner_id= \"A1234\">\n"
                    + "	<transaction>\n"
                    + "		<date>2016-04-23</date>\n"
                    + "		<amount>127.80</amount>\n"
                    + "	</transaction>\n"
                    + "	<transaction>\n"
                    + "		<date>2016-05-23</date>\n"
                    + "		<amount>127.80</amount>\n"
                    + "	</transaction>\n"
                    + "		\n"
                    + "\n"
                    + "	<transaction>\n"
                    + "		<date>2016-06-23</date>\n"
                    + "		<amount>127.80</amount>\n"
                    + "	</transaction>\n"
                    + "	<transaction>\n"
                    + "		<date>2016-07-23</date>\n"
                    + "		<amount>127.80</amount>\n"
                    + "	</transaction>\n"
                    + "</transactions>";
            EntryPoint ep = new EntryPoint("BC4FE2DC08E61EBC9E0CADF0A1D1A30B", "WEB", "PAYMENT", "OFFLINE");
            BufferedWriter esc = new BufferedWriter(new FileWriter("trans.log", true));
            //String response = ep.requestTransaction(3582544l, xml).toString();
            //String response = ep.operatorStatus("B0", "004").toString();
            String json = "{  \n"
                    + "   \"TokenAutenticacao\":\"76B4B358D8292D0568AFFD6ECABF48BFC7F3AA215B774D430D88586304734BB3\",\n"
                    + "   \"TokenTransacao\":\"47f788b4-7f92-494c-aaca-0606adb33114\",\n"
                    + "   \"Pedido\":3,\n"
                    + "   \"Contrato\":\"0000000000\",\n"
                    + "   \"LinkContrato\":\"http://www.vaivoando.com.br/{LINK}\",\n"
                    + "   \"DataPedido\":\"2016-05-09T18:44:46.0951465-03:00\",\n"
                    + "   \"Status\":\"Pedido Efetuado\",\n"
                    + "   \"Cia\":\"TAM\",\n"
                    + "   \"Logo\":\"http://www.vaivoando.com.br/{LOGO}”,\",\n"
                    + "   \"TrechoIda\":\"GRU X FOR\",\n"
                    + "   \"TrechoVolta\":\"FOR X GRU\",\n"
                    + "   \"VooIda\":\"1234\",\n"
                    + "   \"VooVolta\":\"1234\",\n"
                    + "   \"Localizador\":\"Z9Z999Z\",\n"
                    + "   \"PartidaIda\":\"2016-05-09T18:44:46.0961466-03:00\",\n"
                    + "   \"ChegadaIda\":\"2016-05-09T18:44:46.0961466-03:00\",\n"
                    + "   \"PartidaVolta\":\"2016-05-09T18:44:46.0961466-03:00\",\n"
                    + "   \"ChegadaVolta\":\"2016-05-09T18:44:46.0961466-03:00\",\n"
                    + "   \"ValorTotal\":1900.99,\n"
                    + "   \"FormaPagamento\":[  \n"
                    + "      {  \n"
                    + "         \"Forma\":\"BOLETO/ONLINE/OFFLINE\",\n"
                    + "         \"NumeroCartao\":\"9999************9999\",\n"
                    + "         \"Titular\":\"JOÃO SILVA\",\n"
                    + "         \"Validade\":\"MM/AAAA\",\n"
                    + "         \"Bandeira\":\"VISA\",\n"
                    + "         \"CVV\":\"123\",\n"
                    + "         \"Parcelas\":[  \n"
                    + "            {  \n"
                    + "               \"Parcela\":1,\n"
                    + "               \"NSU\":000000000,\n"
                    + "               \"NoDoc\":0000000000000,\n"
                    + "               \"Link\":\"http://www.vaivoando.com.br/{BOLETO-LINK}\",\n"
                    + "               \"Vencimento\":\"2016-05-09T00:00:00.00\",\n"
                    + "               \"Pagamento\":\"2016-05-09T00:00:00.00\",\n"
                    + "               \"ValorParcela\":6.99,\n"
                    + "               \"ValorPago\":0,\n"
                    + "               \"Pago\":false,\n"
                    + "               \"Status\":\"PAGAMENTO EFETUADO\",\n"
                    + "               \"Sucesso\":true\n"
                    + "            },\n"
                    + "            {  \n"
                    + "               \"Parcela\":2,\n"
                    + "               \"NSU\":000000000,\n"
                    + "               \"NoDoc\":0000000000000,\n"
                    + "               \"Link\":\"http://www.vaivoando.com.br/{BOLETO-LINK}\",\n"
                    + "               \"Vencimento\":\"2016-06-09T00:00:00.00-03:00\",\n"
                    + "               \"Pagamento\":\"\",\n"
                    + "               \"ValorParcela\":6.99,\n"
                    + "               \"ValorPago\":0,\n"
                    + "               \"Pago\":false,\n"
                    + "               \"Status\":\"PAGAMENTO EFETUADO\",\n"
                    + "               \"Sucesso\":true\n"
                    + "            }            \n"
                    + "         ]\n"
                    + "      }\n"
                    + "   ],\n"
                    + "   \"Passageiros\":[  \n"
                    + "      {  \n"
                    + "         \"Nome\":\"JOÃO SILVA\",\n"
                    + "         \"DataNascimento\":\"2016-05-09T18:44:46.0991469-03:00\",\n"
                    + "         \"FaixaEtaria\":\"ADULTO/BEBE/CRIANCA\"\n"
                    + "      },\n"
                    + "      {  \n"
                    + "         \"Nome\":\"MARIA SILVA\",\n"
                    + "         \"DataNascimento\":\"2016-05-09T18:44:46.0991469-03:00\",\n"
                    + "         \"FaixaEtaria\":\"ADULTO/BEBE/CRIANCA\"\n"
                    + "      }\n"
                    + "   ],\n"
                    + "   \"Cliente\":{  \n"
                    + "      \"IdUser\":\"string\",\n"
                    + "      \"Nome\":\"string\",\n"
                    + "      \"Email\":\"string\",\n"
                    + "      \"Telefone\":\"(99) 9999-9999\",\n"
                    + "      \"Celular\":\"(19) 9999-9999\",\n"
                    + "      \"Fax\":\"(19) 9999-9996\",\n"
                    + "      \"PJ\":false,\n"
                    + "      \"CPFCNPJ\":\"999.999.999-99\",\n"
                    + "      \"Sexo\":\"F/M\",\n"
                    + "      \"Nascimento\":\"2016-06-09T00:00:00.00-03:00\",\n"
                    + "      \"EstadoCivil\":\"string\",\n"
                    + "      \"RG\":\"string\",\n"
                    + "      \"Endereco\":{  \n"
                    + "         \"CEP\":\"99999-999\",\n"
                    + "         \"Rua\":\"string\",\n"
                    + "         \"Numero\":\"string\",\n"
                    + "         \"Complemento\":\"string\",\n"
                    + "         \"Bairro\":\"string\",\n"
                    + "         \"Cidade\":\"string\",\n"
                    + "         \"Estado\":\"AC/AL/AP/AM/BA/CE/DF/ES/GO/MA/MT/MS/MG/PA/PB/PR/PE/PI/RJ/RN/RS/RO/RR/SC/SP/SE/TO\"\n"
                    + "      }\n"
                    + "   }\n"
                    + "}";

            String json1 = "{  \n"
                    + "   \"TokenAutenticacao\":\"76B4B358D8292D0568AFFD6ECABF48BFC7F3AA215B774D430D88586304734BB3\",\n"
                    + "   \"TokenTransacao\":\"47f788b4-7f92-494c-aaca-0606adb33114\",\n"
                    + "   \"FormaPagamento\":[  \n"
                    + "      {  \n"
                    + "         \"Forma\":\"BOLETO/ONLINE/OFFLINE\",\n"
                    + "         \"NumeroCartao\":\"9999************9999\",\n"
                    + "         \"Titular\":\"JOÃO SILVA\",\n"
                    + "         \"Validade\":\"MM/AAAA\",\n"
                    + "         \"Bandeira\":\"VISA\",\n"
                    + "         \"Parcelas\":[  \n"
                    + "            {  \n"
                    + "               \"Parcela\":1,\n"
                    + "               \"NSU\":000000000,\n"
                    + "               \"NoDoc\":0000000000000,\n"
                    + "               \"Link\":\"http://www.vaivoando.com.br/{BOLETO-LINK}\",\n"
                    + "               \"Vencimento\":\"2016-05-09T00:00:00.00\",\n"
                    + "               \"Pagamento\":\"2016-05-09T00:00:00.00\",\n"
                    + "               \"ValorParcela\":6.99,\n"
                    + "               \"ValorPago\":6.99,\n"
                    + "               \"Pago\":true,\n"
                    + "               \"Status\":\"PAGAMENTO EFETUADO\",\n"
                    + "               \"Sucesso\":true\n"
                    + "            }\n"
                    + "         ]\n"
                    + "      }\n"
                    + "   ]\n"
                    + "}";

            //System.out.println(ep.requestPasswordReset(1l, 15996300145l));
            //System.out.println(Crypt.encrypt("6278921008821166"));
        } catch (Exception ex) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main_(String[] args) {
        String cartao = "6280281030829846";

        int[] iCartao = new int[cartao.length() - 1];
        if (cartao.substring(0, 6).equals("628028")) {
            for (int i = 0; i < cartao.length() - 1; i++) {
                iCartao[i] = Integer.parseInt(String.valueOf(cartao.charAt(i)));
            }

            int multiplier = 18;
            int sum = 0;
            for (int i = 0; i < iCartao.length; i++) {
                int factor = (multiplier == 11) ? 19 : multiplier;
                sum += factor * iCartao[i];
                multiplier--;
            }

            int remainder = (sum % 11);
            int expectedLast = 11 - remainder;
            if (expectedLast >= 10) {
                expectedLast -= 10;
            }

            int last = Integer.parseInt(String.valueOf(cartao.charAt(cartao.length() - 1)));

            //System.out.println("e: " + expectedLast + "; l: " + last);
            if (expectedLast != last) {
                System.out.println("Dígito verificador do cartão é inválido - Cartão Mais");

            } else {
                System.out.println("ok");
            }
        }
    }

    public static void main(String[] args) {
        EntryPoint ep = new EntryPoint("E1702F2D40BAC8996CFFB5837E53CCF2", "APP", "", "");
        MessageResponse mr = ep.queryUser("<Auth><TokenControl><token>B82B4623945FF4C0B64790B2E299CF62</token></TokenControl></Auth>", 11943008550l);
        System.out.println(mr.getStatusMessage());
        System.out.println(mr.getResponse());
    }

    public static void main___(String[] args) throws Exception {
        //String xml = "<transactions><transaction><ETicketCard>4201841</ETicketCard><cityID>20160422</cityID><issuerID>1</issuerID><feeValue>3.80</feeValue><ticketType>1</ticketType><productCode>691</productCode><cvv>221</cvv><ETicketMode>amount</ETicketMode><amount>10</amount></transaction></transactions>";
        String xml = "<transactions>\n"
                + "                 <transaction>\n"
                + "                  <ETicketCard>546224857</ETicketCard>\n"
                + "                  <waitReply>false</waitReply>\n"
                + "                  <cityID>20160422</cityID>\n"
                + "                  <issuerID>1</issuerID>\n"
                + "                  <amount>7.60</amount>\n"
                + "                  <feeValue>3.80</feeValue>\n"
                + "                  <cvv>163</cvv>\n"
                + "                  <ETicketMode>amount</ETicketMode>\n"
                //+ "                  <quantity>1</quantity>\n"
                + "                  <productCode>691</productCode>\n"
                + "                  <ticketType>1</ticketType>\n"
                + "                 </transaction>\n"
                + "</transactions>";
        xml = "<transactions>\n"
                + " <transaction>\n"
                + "  <ETicketCard>467749409</ETicketCard>\n"
                + "  <ddd>11</ddd>\n"
                + "  <issuerID>1</issuerID>\n"
                + " </transaction>\n"
                + "</transactions>";
        EntryPoint ep = new EntryPoint("E1702F2D40BAC8996CFFB5837E53CCF2", "URA", "E_TICKET", "QUERY");
        //EntryPoint ep = new EntryPoint("E1702F2D40BAC8996CFFB5837E53CCF2", "SYS", "RECHARGE", "ONLINE");
        McAfee mcafee = new McAfee();
        mcafee.setDoc("41904886826");
        mcafee.setEmail("11943008550@jcnsistemas.com.br");
        mcafee.setFirstName("USUARIO");
        mcafee.setLastName("JCN");
        mcafee.setMode("NEW");
        mcafee.setPassword("1234");
        mcafee.setPhoneNumber(11943008550l);
        new BufferedReader(new InputStreamReader(System.in)).lines();
        mcafee.setQuantity(1);
        mcafee.setRef(mcafee.getDoc() + "" + mcafee.getPhoneNumber());
        mcafee.setValue(9.90f);
        //System.out.println(ep.requestTransaction(11943008550l, xml));
        /*BufferedReader leitor = new BufferedReader(new FileReader("C:\\Users\\bruno.oliveira\\Documents\\bu_validacao.csv"));
         String file = leitor.readLine();
         String line = null;
         while ((line = leitor.readLine()) != null) {
         String split[] = line.split(";");

         xml = "<transactions>\n"
         + "   <transaction>\n"
         + "      <ddd>11</ddd>\n"
         + "      <ETicketCard>" + split[7] + "</ETicketCard>\n"
         + "      <issuerID>1</issuerID>\n"
         + "   </transaction>\n"
         + "</transactions>";
         //EntryPoint ep = new EntryPoint("826210A38A4F787FDBC6CBEF70F6F1E8", "SYS", "PAYMENT", "ONLINE");
         //while(true)
         //while (true) 
         //String xml = "<ElectronicTicketCardInsert><electronicTicketCard><eTicket>123</eTicket><idIssuerCity>1</idIssuerCity></electronicTicketCard></ElectronicTicketCardInsert>";
         //System.out.println(ep.manageETicket(xml, 3582544l, true));
         String response = ep.requestTransaction(Long.parseLong(split[6]), xml);
         String registered = response.substring(response.indexOf("<registeredCard>") + 16, response.indexOf("</registeredCard>")).equals("false") ? "NÃO" : "SIM";
         split[8] = registered;
         for (String split1 : split) {
         file += split1 + ";";
         }
         file += System.lineSeparator();
         }
         leitor.close();
         BufferedWriter escritor = new BufferedWriter(new FileWriter("report.csv"));
         escritor.write(file);
         escritor.close();*/
        //System.out.println(ep.getIssuers(11));
    }

    public static void testRecharge() {
        String xml = "<RechargeRequest>\n"
                + " <lifeCycle>a1d0c6e83f027327d8461063f4ac58a6</lifeCycle>\n"
                + " <source>1</source>\n"
                + " <User>\n"
                + "  <phoneNumber>11974075547</phoneNumber>\n"
                + " </User>\n"
                + " <CreditAnalysis>\n"
                + "  <sessionID>JCNTeste_$sid</sessionID>\n"
                + " </CreditAnalysis>\n"
                + " <Recharge>\n"
                + "  <phoneNumber>11974075547</phoneNumber>\n"
                + "  <value>1000</value>\n"
                + " </Recharge>\n"
                + " <Payment>\n"
                + "  <cvv>012</cvv>\n"
                + " </Payment>\n"
                + "</RechargeRequest>";
        EntryPoint ep = new EntryPoint("E1702F2D40BAC8996CFFB5837E53CCF2", "URA", "RECHARGE", "ONLINE");

        //String response = ep.requestTransaction(553232l, xml).toString();operatorStatus
        // String response = ep.operatorStatus("B0", "004").toString();
        try {
            BufferedWriter esc = new BufferedWriter(new FileWriter("trans.log", true));
            String response = ep.requestRecharge(xml).toString();
            esc.append(response + "\n");
            esc.close();
            System.out.println(response);
        } catch (IOException ex) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @return the origin
     */
    public br.com.jcnsistemas.jcn.busconnector.to.Origin getOrigin() {
        return origin;
    }

    /**
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * @return the product
     */
    public br.com.jcnsistemas.jcn.busconnector.to.Product getProduct() {
        return product;
    }

    /**
     * @return the service
     */
    public br.com.jcnsistemas.jcn.busconnector.to.Service getService() {
        return service;
    }

    @POST
    @Path("updateVideo/")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_XML)
    public MessageResponse updateVideo(Video video) {
        System.out.println("Updating Video " + video.getId());
        MessageResponse response = new MessageResponse();
        response.setStatusMessage("200 OK");
        VideoDAO dao = new VideoDAO();
        response.setStatus(dao.updateVideo(video));
        System.out.println("Video " + video.getId() + " was updated!");
        return response;
    }

    @POST
    @Path("insertVideo/{telephone}/")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_XML)
    public MessageResponse insertVideo(Video video, @PathParam("telephone") Long telephone) {
        MessageResponse response = new MessageResponse();
        VideoDAO dao = new VideoDAO();
        video.setTelephone(telephone);
        video.setUploadDate(new Date());
        response.setStatus(dao.insertVideo(video));
        return response;
    }

    @POST
    @Path("getVideoUsers/")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_XML)
    public ArrayList<Video> getVideoUsers(Video video) {
        ArrayList<Video> videos = null;
        VideoDAO dao = new VideoDAO();
        videos = dao.listVideo(video);
        return videos;
    }

    @POST
    @Path("scheduleRecharge/{idCampaign}")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_XML)
    public MessageResponse scheduleRecharge(TransactionWrapper transaction,
            @PathParam("idCampaign") Long idCampaign) {
        MessageResponse response = new MessageResponse();
        if (getChannel() != null) {
            TransactionDAO dao = new TransactionDAO();
            if (dao.scheduleTransaction(transaction, idCampaign)) {
                response.setStatus(true);
                response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_SUCCESS);
            }
        } else {
            response.setStatusMessage("403 FORBIDDEN");
            response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_FORBIDDEN);
        }
        return response;
    }

    @PUT
    @Path("queryCampaigns/{phoneNumber}")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_XML)
    public ArrayList<Campaign> queryCampaigns(@PathParam("phoneNumber") Long phoneNumber) {
        UserDAO dao = new UserDAO();
        ArrayList<Campaign> list = null;
        try {
            list = dao.getUserCampaigns(phoneNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @PUT
    @Path("queryLuckyNumbersByCampaign/{idCampaign}/{phoneNumber}")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_XML)
    public ArrayList<LuckyNumber> queryLNumbers(@PathParam("idCampaign") Long idCampaign, @PathParam("phoneNumber") Long phoneNumber) {
        UserDAO dao = new UserDAO();
        ArrayList<LuckyNumber> list = null;
        try {
            list = dao.getLuckyNumbersByCampaign(idCampaign, phoneNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @PUT
    @Path("queryCampaignWinner/{idCampaign}/{phoneNumber}")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_XML)
    public ArrayList<Video> queryCampaignWinner(@PathParam("idCampaign") Long idCampaign, @PathParam("phoneNumber") Long phoneNumber) {
        UserDAO dao = new UserDAO();
        ArrayList<Video> videos = null;
        videos = dao.getWinners(phoneNumber);
        return videos;
    }
    
    @PUT
    @Path("queryCampWinState/{idCampaign}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ArrayList<WinnerState> queryCampWinState(@PathParam("idCampaign") Long idCampaign) {
        UserDAO dao = new UserDAO();
        ArrayList<WinnerState> winnerStateList = null;
        winnerStateList = dao.getWinnersByState(idCampaign);
        return winnerStateList;
    }
    

    @POST
    @Path("manageMcafee/")
    @Consumes(MediaType.APPLICATION_XML + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_XML)
    public MessageResponse manageMcafee(McAfee mcafee) {
        MessageResponse response = new MessageResponse();
        if (getChannel() != null) {
            System.out.println(mcafee.getId());
            TransactionControl transactionControl = new TransactionControl();
            String mcAfeeResponse = transactionControl.processMcAfeeTransaction(mcafee, getChannel(), UserDAO.getUserNoPwd(mcafee.getPhoneNumber()));
            response.setResponse(mcAfeeResponse);
        } else {
            response.setStatusMessage("403 FORBIDDEN");
            response.setStatusCode(BusResponseCodes.DEFAULT_RESPONSE_FORBIDDEN);
        }
        return response;
    }

    public static void _main(String[] args) throws ParseException {
        ArrayList<Campaign> vl = (new EntryPoint(null, null, null, null).queryCampaigns(15988270980l));
        System.out.println("here");
        for (Campaign vl1 : vl) {
            System.out.println(vl1.getName());
        }
    }

    @PUT
    @Path("productOffer/{phoneNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<ProductOffer> productOffer(@PathParam("phoneNumber") String phoneNumber) {
        ArrayList<ProductOffer> products = null;
        if (getChannel() != null) {
            products = FeaturesDAO.getProductOffer(getChannel(), phoneNumber);
        }
        return products;
    }

    /**
     *
     * @param idCampaign The Campaign ID which is desired to get the next Lead
     * @return the next Lead with the following pattern:<br>
     * @code{<Lead>
     *          <phoneNumber>99999999999</phoneNumber>
     *      </Lead>}
     */
    @PUT
    @Path("nextLead/{IDCampaign}")
    @Produces(MediaType.APPLICATION_XML)
    public Lead nextLead(@PathParam("IDCampaign") Long idCampaign) {
        return null;
    }

    /**
     *
     * @param idCampaign The Campaign ID the Lead is member of
     * @param lead the Lead as a xml. e.g.:<br>
     * @code{<Lead>
     *          <phoneNumber>99999999999</phoneNumber>
     *          <description></description>
     *      </Lead>}
     *
     * @return 200 for ok and 500 for error
     */
    @PUT
    @Path("updateLead/{IDCampaign}")
    @Produces(MediaType.APPLICATION_XML)
    public Lead updateLead(@PathParam("IDCampaign") Long idCampaign, Lead lead) {
        return null;
    }

    @POST
    @Path("mock/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Lead mock(Lead l) {
        l.setDescription("MARCAO É GAY");
        l.setObj(UserDAO.getUserNoPwd(11943008550l));
        return l;
    }
    
    @POST
    @Path("statusTransaction")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String statusTransaction(String requestJson) {
        System.out.println("Consultando transação");
        MessageResponse r = new MessageResponse();
        String jsonReturn = null;
        String vErrorMessage = null;
        String vStatusDate = null;
        DateHour dateHour = new DateHour();
        System.out.println(dateHour.getDateTime());
        System.out.println("Recebemos:");
        System.out.println(requestJson);
        if (getChannel() != null) {
            try {
                JSONObject json = new JSONObject(requestJson);
                System.out.println(json.getString("PaymentId"));
                TransactionTransData transactionTransData = null;
                TransactionDAO dao = new TransactionDAO();
                Channel channel = new Channel();
                channel = getChannel();
                if (channel.getSnChannel().equals("GAME")){
                    transactionTransData = dao.getStatusTransactionRecharge(json.getString("PaymentId"));
                }
                else{
                    transactionTransData = dao.getStatusTransactionPayment(json.getString("PaymentId"));
                }
                
                if (transactionTransData.getErrorMessage() == null){vErrorMessage = "null";}
                else{vErrorMessage = "\""+transactionTransData.getErrorMessage()+"\"";}
                
                if (transactionTransData.getStatusDate() == null){vStatusDate = "null";}
                else{vStatusDate = "\""+transactionTransData.getStatusDate()+"\"";}
                
                jsonReturn = "{"+
                                    "\"PaymentId\": \""+transactionTransData.getPaymentId()+"\","+
                                    "\"Order\": {"+
                                                    "\"Reference\": "+transactionTransData.getOrderReference()+
                                    "},"+
                                    "\"Status\": {"+
                                                    "\"Code\": "+transactionTransData.getStatusCode()+","+
                                                    "\"Date\": "+vStatusDate+
                                    "},"+
                                    "\"Error\": {"+
                                            "\"Code\": "+transactionTransData.getErrorCode()+",";
                
                if (channel.getSnChannel().equals("GAME")){
                    jsonReturn = jsonReturn + "\"Message\": "+vErrorMessage; // retorna uma string para o Game
                }
                    else{
                    jsonReturn = jsonReturn + "\"Message\": ["+vErrorMessage+"]"; // retorna um Array para a TransData
                }
                
                jsonReturn = jsonReturn + "}"+
                              "}";
                r.setStatusMessage("200 OK");
                r.setResponse("Transacao ok!");
                r.setStatus(true);
                System.out.println("Enviamos:");
                System.out.println(jsonReturn);
                System.out.println(dateHour.getDateTime());
                return jsonReturn;
            } catch (JSONException ex) {
                Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jsonReturn;
    }
}