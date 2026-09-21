// vuln.js
// Intentionally vulnerable: DOM-based XSS demonstration.
// Run locally only.

const http = require("http");

const server = http.createServer((req, res) => {
  const url = new URL(req.url, "http://localhost:3000");
  const name = url.searchParams.get("name") || "Guest";

  res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });

  // VULNERABLE: untrusted input is inserted directly into HTML.
  res.end(`
    <!doctype html>
    <html>
      <body>
        <h1>Hello ${name}</h1>
        <p>This page intentionally contains an XSS vulnerability.</p>
      </body>
    </html>
  `);
});

server.listen(3000, "127.0.0.1", () => {
  console.log("Vulnerable lab running at http://127.0.0.1:3000");
});
