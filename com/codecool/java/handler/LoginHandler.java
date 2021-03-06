package codecool.java.handler;

import codecool.java.controller.LoginController;
import codecool.java.dao.DbAuthorizationDAO;
import codecool.java.dao.NotInDatabaseException;
import codecool.java.helper.HttpResponse;
import codecool.java.model.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URI;
import java.sql.SQLException;
import java.text.ParseException;

public class LoginHandler implements HttpHandler {
    CookieHelper cookieHelper = new CookieHelper();
    HttpResponse httpResponse = new HttpResponse();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method  = httpExchange.getRequestMethod();
        URI uri = httpExchange.getRequestURI();
        if(method.equals("POST") && (uri.toString().equals("/login"))){
            try {
                User user = getUserData(httpExchange.getRequestBody());
                cookieHelper.createNewCookie(httpExchange, user);
                httpResponse.sendResponse200(httpExchange, user.getClass().getSimpleName());
            } catch (SQLException | ClassNotFoundException | ParseException e) {
                httpResponse.sendResponse500(httpExchange);
                e.printStackTrace();
            } catch (NotInDatabaseException e) {
                httpResponse.sendResponse404(httpExchange);
            }
        }
        if (method.equals("POST") && (uri.toString().equals("/login/expired_cookie"))){
            String sessionId = getSessionIdFromCookie(httpExchange.getRequestBody());
            cookieHelper.refreshCookie(httpExchange);
            httpResponse.sendResponse200(httpExchange, "");
        }
    }

    private User getUserData(InputStream requestBody) throws IOException, SQLException, ClassNotFoundException, NotInDatabaseException {
        LoginController loginController = new LoginController();
        InputStreamReader isr = new InputStreamReader(requestBody, "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String loginData = br.readLine();
        loginData = loginData.substring(1, loginData.length()-1);
        String[] loginPassword = loginData.split(",");
        loginPassword[0] = loginPassword[0].replaceAll("\"", "");
        loginPassword[1] = loginPassword[1].replaceAll("\"", "");
        User user = loginController.authenticate(loginPassword[0], loginPassword[1]);
        System.out.println(user.toString());
        return user;

    }

    private String getSessionIdFromCookie(InputStream requestBody) throws IOException {
        InputStreamReader isr = new InputStreamReader(requestBody, "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String cookie = br.readLine();
        cookie = cookie.split("=")[1].replaceAll("\"", "");
        return cookie;
    }
}
