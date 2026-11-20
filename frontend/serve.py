#!/usr/bin/env python3
"""
Lightweight Static HTTP Server for Antique Store Frontend
Serves frontend assets strictly on port 5500 (matching Backend CORS configuration)
"""

import http.server
import os
import socketserver
import sys

PORT = 5500
DIRECTORY = os.path.dirname(os.path.abspath(__file__))

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def end_headers(self):
        # Disable caching for clean development/evaluation
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        self.send_header('Pragma', 'no-cache')
        self.send_header('Expires', '0')
        super().end_headers()

def run_server():
    socketserver.TCPServer.allow_reuse_address = True
    try:
        with socketserver.TCPServer(("0.0.0.0", PORT), Handler) as httpd:
            print(f"============================================================")
            print(f" Antique Store Frontend is running at:")
            print(f"   Local:            http://localhost:{PORT}")
            print(f"   Directory served: {DIRECTORY}")
            print(f"   Backend Target:   http://localhost:8080/api")
            print(f" Press Ctrl+C to stop.")
            print(f"============================================================")
            sys.stdout.flush()
            httpd.serve_forever()
    except OSError as e:
        if e.errno == 98 or e.errno == 10048:
            print(f"[ERROR] Port {PORT} is already in use. Please terminate existing process on port {PORT}.")
        else:
            print(f"[ERROR] Could not start server: {e}")
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n[INFO] Server stopped by user.")
        sys.exit(0)

if __name__ == '__main__':
    run_server()
