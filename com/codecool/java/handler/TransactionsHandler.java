package codecool.java.handler;

import codecool.java.dao.DbTransactionsDAO;
import codecool.java.dao.DbstudentDAO;
import codecool.java.helper.HttpResponse;
import codecool.java.model.Student;
import codecool.java.model.Transaction;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TransactionsHandler implements HttpHandler {
    CookieHelper cookieHelper = new CookieHelper();
    HttpResponse httpResponse = new HttpResponse();
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        String response = "";
        if(method.equals("GET")){
            if(!cookieHelper.isCookiePresent(httpExchange)){
                httpResponse.redirectToLoginPage(httpExchange);
            }else {
                cookieHelper.refreshCookie(httpExchange);
                try {
                    Gson gson = new Gson();
                    response = gson.toJson(getStudentTransactions(httpExchange));
                    httpResponse.sendResponse200(httpExchange, response);
                } catch (SQLException | ClassNotFoundException e) {
                    httpResponse.sendResponse500(httpExchange);
                    e.printStackTrace();
                }
           }
        }
    }

    private Map<String,List<Transaction>> getStudentTransactions(HttpExchange httpExchange) throws SQLException, ClassNotFoundException {
        DbstudentDAO studentDAO = new DbstudentDAO();
        DbTransactionsDAO transactionsDAO = new DbTransactionsDAO();
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        Student student = studentDAO.findStudentBySessionId(getSessionIdFromCookieString(cookieStr));
        return transactionsDAO.displayAllTransactionsByStudent(student);
    }

    private String getSessionIdFromCookieString(String cookieStr) {
        return cookieStr.split("=")[1].replace("\"","");
    }
}
