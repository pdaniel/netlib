/*
 * silvertunnel.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2009-2012 silvertunnel.org
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package org.silvertunnel.netlib.adapter.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

/**
 * 
 * See class SocketUtil.
 * 
 * @author hapke
 */
class InvalidSocketImpl extends SocketImpl {

    private void notImplemented() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void accept(SocketImpl s) throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
    }

    @Override
    protected int available() throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        return 0;
    }

    @Override
    protected void bind(InetAddress host, int port) throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        
    }

    @Override
    protected void close() throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        
    }

    @Override
    protected void connect(String host, int port) throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        
    }

    @Override
    protected void connect(InetAddress address, int port) throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        
    }

    @Override
    protected void connect(SocketAddress address, int timeout)
            throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        
    }

    @Override
    protected void create(boolean stream) throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        return null;
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        return null;
    }

    @Override
    protected void listen(int backlog) throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        
    }

    @Override
    protected void sendUrgentData(int data) throws IOException {
        // TODO Auto-generated method stub
        notImplemented();
        
    }

    public Object getOption(int arg0) throws SocketException {
        // TODO Auto-generated method stub
        notImplemented();
        return null;
    }

    public void setOption(int arg0, Object arg1) throws SocketException {
        // TODO Auto-generated method stub
        notImplemented();
        
    }
}
