import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ping")
public class PingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String host = request.getParameter("host");

        // VULNERABLE: request input is concatenated into a shell command.
        Process p = Runtime.getRuntime().exec(new String[] {"sh", "-c", "ping -c 1 " + host});

        response.setContentType("text/plain");
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                response.getWriter().println(line);
            }
        }
    }
}
