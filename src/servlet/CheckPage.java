package servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

@WebServlet("/checkPage")
public class CheckPage extends HttpServlet implements Servlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("text/xml");
		try {
			URL u = new URL(request.getParameter("url"));
			HttpURLConnection huc = (HttpURLConnection) u.openConnection();
			huc.setRequestMethod("GET");
			huc.connect();
			out.print(huc.getResponseCode());
		} catch (Exception e) {
			out.print(e);
		}
	}

}