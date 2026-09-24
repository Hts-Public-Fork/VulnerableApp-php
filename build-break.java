package lab;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Base64;

@WebServlet("/vulnerable")
public class VulnerableServlet extends HttpServlet {

    // 1. Hard-coded credentials
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "admin123";
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/testdb";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // 2. Reflected XSS
        if ("hello".equals(action)) {
            String name = request.getParameter("name");

            response.setContentType("text/html");
            response.getWriter().println(
                "<html><body>Hello " + name + "</body></html>"
            );
            return;
        }

        // 3. Path traversal
        if ("read".equals(action)) {
            String filename = request.getParameter("file");

            Path path = Paths.get("/tmp/uploads/" + filename);

            if (Files.exists(path)) {
                response.getWriter().println(
                    new String(Files.readAllBytes(path))
                );
            }
            return;
        }

        // 4. SQL injection
        if ("user".equals(action)) {
            String username = request.getParameter("username");

            try {
                Connection conn = DriverManager.getConnection(
                    DB_URL,
                    DB_USER,
                    DB_PASSWORD
                );

                Statement stmt = conn.createStatement();

                String sql =
                    "SELECT * FROM users WHERE username = '"
                    + username + "'";

                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    response.getWriter().println(
                        "User: " + rs.getString("username")
                    );
                }

                rs.close();
                stmt.close();
                conn.close();

            } catch (SQLException e) {
                // 5. Information disclosure through verbose errors
                response.getWriter().println(
                    "Database error: " + e.getMessage()
                );
            }

            return;
        }

        // 6. OS command injection
        if ("ping".equals(action)) {
            String host = request.getParameter("host");

            Process process = Runtime.getRuntime().exec(
                "ping -c 1 " + host
            );

            BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );

            String line;
            while ((line = reader.readLine()) != null) {
                response.getWriter().println(line);
            }

            return;
        }

        // 7. Weak authentication / authorization
        if ("admin".equals(action)) {
            String role = request.getParameter("role");

            if ("admin".equals(role)) {
                response.getWriter().println(
                    "Administrative operation performed."
                );
            } else {
                response.getWriter().println("Access denied.");
            }

            return;
        }

        // 8. Sensitive information exposed in response
        if ("debug".equals(action)) {
            response.setContentType("text/plain");

            response.getWriter().println(
                "DB URL: " + DB_URL
            );
            response.getWriter().println(
                "DB User: " + DB_USER
            );
            response.getWriter().println(
                "DB Password: " + DB_PASSWORD
            );
        }
    }

    // 9. Insecure file upload
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String filename = request.getParameter("filename");
        String content = request.getParameter("content");

        if (filename != null && content != null) {
            File output =
                new File("/tmp/uploads/" + filename);

            try (FileWriter writer = new FileWriter(output)) {
                writer.write(content);
            }

            response.getWriter().println(
                "File uploaded: " + output.getAbsolutePath()
            );
        }
    }
}
