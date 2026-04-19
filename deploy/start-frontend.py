#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# 如果上面的shebang不工作，请尝试: #!/usr/bin/python3
# 或者直接运行: python3 start-frontend.py

"""
============================================================
Video Manager 前端启动脚本 (Python HTTP Server)
用途: 使用Python启动前端静态文件服务，支持SPA路由
特点: 无需nginx，轻量级，适合NAS环境
============================================================
"""

import http.server
import socketserver
import os
import sys
import signal
from urllib.parse import urlparse

# ------------------------------------------------------------
# 配置区域 - 请根据实际环境修改
# ------------------------------------------------------------

# 前端服务端口
FRONTEND_PORT = 3000

# 后端API端口 (用于代理)
BACKEND_PORT = 8080

# 静态文件目录 (前端打包后的dist目录)
STATIC_DIR = "dist"

# 是否启用API代理
# True: 前端请求/api/*会代理到后端
# False: 前端直接请求后端(需要后端配置CORS)
ENABLE_API_PROXY = True

# ------------------------------------------------------------
# HTTP请求处理器
# ------------------------------------------------------------

class SPAHandler(http.server.SimpleHTTPRequestHandler):
    """
    支持SPA路由的HTTP请求处理器
    
    功能:
    1. 支持Vue Router的history模式 (所有路由返回index.html)
    2. 支持API请求代理到后端
    3. 添加CORS头支持跨域
    """
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=STATIC_DIR, **kwargs)
    
    def log_message(self, format, *args):
        """自定义日志格式"""
        print(f"[{self.log_date_time_string()}] {args[0]}")
    
    def do_GET(self):
        """处理GET请求"""
        self.handle_request('GET')
    
    def do_POST(self):
        """处理POST请求"""
        self.handle_request('POST')
    
    def do_PUT(self):
        """处理PUT请求"""
        self.handle_request('PUT')
    
    def do_DELETE(self):
        """处理DELETE请求"""
        self.handle_request('DELETE')
    
    def do_OPTIONS(self):
        """处理OPTIONS请求 (CORS预检)"""
        self.send_response(200)
        self.send_cors_headers()
        self.end_headers()
    
    def handle_request(self, method):
        """
        统一请求处理
        
        逻辑:
        1. API请求 -> 代理到后端
        2. 静态资源 -> 直接返回文件
        3. SPA路由 -> 返回index.html
        """
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        
        # API请求代理
        if ENABLE_API_PROXY and path.startswith('/api/'):
            self.proxy_request(method)
            return
        
        # 静态资源直接返回
        # 包括: /assets/*, /favicon.ico, /robots.txt 等
        if self.is_static_resource(path):
            super().do_GET()
            return
        
        # 检查是否是实际文件
        file_path = os.path.join(STATIC_DIR, path.lstrip('/'))
        if os.path.isfile(file_path) and os.path.exists(file_path):
            super().do_GET()
            return
        
        # SPA路由: 返回index.html
        # Vue Router的history模式需要这个处理
        self.path = '/index.html'
        super().do_GET()
    
    def is_static_resource(self, path):
        """判断是否是静态资源"""
        static_prefixes = ['/assets/', '/favicon', '/robots', '/manifest']
        for prefix in static_prefixes:
            if path.startswith(prefix):
                return True
        return False
    
    def proxy_request(self, method):
        """
        代理请求到后端
        
        说明: 将/api/*的请求转发到后端服务
        """
        import urllib.request
        import urllib.error
        
        backend_url = f"http://localhost:{BACKEND_PORT}{self.path}"
        
        try:
            # 读取请求体
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length) if content_length > 0 else None
            
            # 创建代理请求
            req = urllib.request.Request(backend_url, data=body, method=method)
            
            # 复制请求头
            for header in self.headers:
                header_lower = header.lower()
                if header_lower not in ['host', 'connection', 'content-length']:
                    req.add_header(header, self.headers[header])
            
            # 发送请求到后端
            with urllib.request.urlopen(req, timeout=30) as response:
                # 返回响应
                self.send_response(response.status)
                
                # 复制响应头
                for header in response.headers:
                    if header.lower() not in ['transfer-encoding']:
                        self.send_header(header, response.headers[header])
                
                self.send_cors_headers()
                self.end_headers()
                self.wfile.write(response.read())
                
        except urllib.error.HTTPError as e:
            # 后端返回错误
            self.send_response(e.code)
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(e.read())
            
        except urllib.error.URLError as e:
            # 无法连接后端
            self.send_response(502)
            self.send_cors_headers()
            self.end_headers()
            error_msg = f"{{\"error\": \"Backend unavailable: {str(e.reason)}\"}}"
            self.wfile.write(error_msg.encode('utf-8'))
            
        except Exception as e:
            # 其他错误
            self.send_response(500)
            self.send_cors_headers()
            self.end_headers()
            error_msg = f"{{\"error\": \"Proxy error: {str(e)}\"}}"
            self.wfile.write(error_msg.encode('utf-8'))
    
    def send_cors_headers(self):
        """发送CORS头"""
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With')
        self.send_header('Access-Control-Max-Age', '86400')
    
    def end_headers(self):
        """结束响应头"""
        super().end_headers()


# ------------------------------------------------------------
# 主程序
# ------------------------------------------------------------

def main():
    """主函数"""
    
    # 切换到脚本所在目录
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)
    
    # 检查静态文件目录
    if not os.path.exists(STATIC_DIR):
        print("=" * 60)
        print("错误: 找不到静态文件目录")
        print("=" * 60)
        print(f"期望目录: {os.path.abspath(STATIC_DIR)}")
        print("")
        print("请确保:")
        print("1. 前端已打包: npm run build")
        print("2. dist目录已上传到NAS")
        print("3. dist目录与脚本在同一目录下")
        print("=" * 60)
        sys.exit(1)
    
    # 检查index.html
    if not os.path.exists(os.path.join(STATIC_DIR, 'index.html')):
        print("错误: dist目录中没有找到index.html")
        print("请确保前端打包正确")
        sys.exit(1)
    
    # 打印启动信息
    print("=" * 60)
    print("  Video Manager 前端服务")
    print("=" * 60)
    print(f"服务端口: {FRONTEND_PORT}")
    print(f"静态目录: {os.path.abspath(STATIC_DIR)}")
    print(f"API代理: {'启用 -> localhost:' + str(BACKEND_PORT) if ENABLE_API_PROXY else '禁用'}")
    print("=" * 60)
    print(f"访问地址: http://localhost:{FRONTEND_PORT}")
    print("=" * 60)
    print("按 Ctrl+C 停止服务")
    print("")
    
    # 创建HTTP服务器
    try:
        with socketserver.TCPServer(("", FRONTEND_PORT), SPAHandler) as httpd:
            # 设置socket复用，避免端口占用
            httpd.socket.setsockopt(socketserver.socket.SOL_SOCKET, 
                                   socketserver.socket.SO_REUSEADDR, 1)
            
            # 启动服务
            httpd.serve_forever()
            
    except KeyboardInterrupt:
        print("\n")
        print("=" * 60)
        print("服务已停止")
        print("=" * 60)
        
    except OSError as e:
        if e.errno == 98:  # 端口被占用
            print(f"错误: 端口 {FRONTEND_PORT} 已被占用")
            print("请检查是否有其他服务使用该端口")
            print("或修改脚本中的 FRONTEND_PORT 配置")
        else:
            print(f"启动失败: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
