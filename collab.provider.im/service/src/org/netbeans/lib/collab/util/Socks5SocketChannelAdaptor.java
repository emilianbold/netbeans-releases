/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab.util;

import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.SocketException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.net.Inet6Address;

import java.nio.*;
import java.nio.channels.*;

/**
 * A standalone SOCKS V5 SocketChannel provider.
 *
 * This code is an adaptation of a standalone socket SOCKS 5 client
 * socket implementation written by Todd Fast and Matt Stevens.
 * This copying was necessary in order to use SOCKS on a per-socket
 * basis, as the JDK only allows SOCKS usage across the entire VM.
 * 
 * @author        Jacques Belissent
 * @author        Matt Stevens, matthew.stevens@sun.com
 * @author        Todd Fast, todd.fast@sun.com
 */
public class Socks5SocketChannelAdaptor {
        
    static final byte PROTO_VERS4               = 4;
    static final byte PROTO_VERS                = 5;
    static final int DEFAULT_PORT               = 1080;

    static final byte NO_AUTH                   = 0;
    static final byte GSSAPI                    = 1;
    static final byte USER_PASSW                = 2;
    static final short NO_METHODS               = 0xff;

    static final byte CONNECT                   = 1;
    static final byte BIND                      = 2;
    static final byte UDP_ASSOC                 = 3;

    static final byte IPV4                      = 1;
    static final byte DOMAIN_NAME               = 3;
    static final byte IPV6                      = 4;

    static final byte REQUEST_OK                = 0;
    static final byte GENERAL_FAILURE           = 1;
    static final byte NOT_ALLOWED               = 2;
    static final byte NET_UNREACHABLE           = 3;
    static final byte HOST_UNREACHABLE          = 4;
    static final byte CONN_REFUSED              = 5;
    static final byte TTL_EXPIRED               = 6;
    static final byte CMD_NOT_SUPPORTED         = 7;
    static final byte ADDR_TYPE_NOT_SUP         = 8;
        


    /**
     * open a connection to a remote host via a SOCKS V5 proxy
     * @param host remote host name
     * @param host remote port
     * @param socksHost SOCKS 5 proxy server hostname
     * @param socksPort SOCKS 5 proxy server port
     * @param username SOCKS authentication credential's identity
     * @param password SOCKS authentication credential's password
     */
    public static SocketChannel open(String host, int port,
                                     String socksHost, int socksPort,
                                     String username, String password)
        throws UnknownHostException, IOException 
    {
        InetSocketAddress isa = new InetSocketAddress(socksHost, socksPort <= 0 ? DEFAULT_PORT : socksPort);
        Socks5SocketChannelAdaptor ssca
            = new Socks5SocketChannelAdaptor(host, port, isa,
                                             username, password);
        return ssca.getChannel();
    }

    /**
     * open a connection to a remote host via a SOCKS5 proxy
     * @param host remote host name
     * @param host remote port
     * @param socksAddress SOCKS 5 proxy server IP address
     * @param socksPort SOCKS 5 proxy server port
     * @param username SOCKS authentication credential's identity
     * @param password SOCKS authentication credential's password
     */
    public static SocketChannel open(InetAddress address, int port,
                                     InetAddress socksAddress, int socksPort,
                                     String username, String password)
        throws IOException
    {
        InetSocketAddress isa = new InetSocketAddress(socksAddress, socksPort <= 0 ? DEFAULT_PORT : socksPort);
        Socks5SocketChannelAdaptor ssca
            = new Socks5SocketChannelAdaptor(address, port, isa,
                                             username, password);
        return ssca.getChannel();
    }

     /**
     * open a connection to a remote host via a SOCKS V5 proxy
     * @param host remote host name
     * @param host remote port
     * @param socksProxychannel preconfigured socks proxy channel
     * @param socksHost SOCKS 5 proxy server hostname
     * @param socksPort SOCKS 5 proxy server port
     * @param username SOCKS authentication credential's identity
     * @param password SOCKS authentication credential's password
     */
    public static SocketChannel open(String host, int port,
                                     SocketChannel socksProxyChannel, String socksHost, int socksPort,
                                     String username, String password)
        throws UnknownHostException, IOException 
    {
        InetSocketAddress isa = new InetSocketAddress(socksHost, socksPort <= 0 ? DEFAULT_PORT : socksPort);
        Socks5SocketChannelAdaptor ssca
            = new Socks5SocketChannelAdaptor(host, port, socksProxyChannel,isa,
                                             username, password);
        return ssca.getChannel();
    }
    

    //////////////////////////////////////////////////////////////
    //////// constructors modeled after java.net.Socket //////////
    //////////////////////////////////////////////////////////////

    private Socks5SocketChannelAdaptor() throws IOException {
        _channel = SocketChannel.open();
    }
        
    private Socks5SocketChannelAdaptor(SocketAddress sa,
                                       String username, String password)
        throws IOException 
{
        _channel = SocketChannel.open();
        // no connection at this point
                
        // remember SOCKS address for when we do "connect"
        setProxyAddress(sa);
        setUsername(username);
        setPassword(password);
    }
        
    private Socks5SocketChannelAdaptor(String host, int port,
                                       SocketAddress sa,
                                       String username, String password)
        throws IOException 
    {
        _channel = SocketChannel.open(sa);
        // already connected by now

        setDestinationAddress(host, port);
        setUsername(username);
        setPassword(password);
        finishConnect();
    }
    
      private Socks5SocketChannelAdaptor(String host, int port,
                                       SocketChannel socksProxyChannel, SocketAddress addr,
                                       String username, String password)
        throws IOException 
    {
        _channel = socksProxyChannel;
        _channel.connect(addr);
        // already connected by now

        setDestinationAddress(host, port);
        setUsername(username);
        setPassword(password);
        finishConnect();
    }

    private Socks5SocketChannelAdaptor(InetAddress address, int port,
                                       SocketAddress sa,
                                       String username, String password)
        throws IOException 
    {
        _channel = SocketChannel.open(sa);
        // already connected by now

        setDestinationAddress(address,port);
        setUsername(username);
        setPassword(password);
        finishConnect();
    }
   
    // not done
    private Socks5SocketChannelAdaptor(String host, int port,
                                       InetAddress localAddr, int localPort,
                                       SocketAddress sa,
                                       String username, String password)
        throws UnknownHostException, IOException 
    {
        _channel = SocketChannel.open(sa);
        // already connected by now

        setDestinationAddress(host,port);
        setUsername(username);
        setPassword(password);
        finishConnect();
    }
        
    private Socks5SocketChannelAdaptor(InetAddress address, int port, 
                                       InetAddress localAddr, int localPort,
                                       SocketAddress sa,
                                       String username, String password)
        throws IOException 
    {
        _channel = SocketChannel.open(sa);
        // already connected by now

        setDestinationAddress(address,port);
        setUsername(username);
        setPassword(password);
        finishConnect();
    }
        
    private Socks5SocketChannelAdaptor(String host, int port,
                                       boolean stream,
                                       SocketAddress sa,
                                       String username, String password)
        throws IOException 
    {
        _channel = SocketChannel.open(sa);
        // already connected by now

        setDestinationAddress(host,port);
        setUsername(username);
        setPassword(password);
        finishConnect();
    }
        
    private Socks5SocketChannelAdaptor(InetAddress address, int port,
                                       boolean stream, 
                                       SocketAddress sa,
                                       String username, String password)
        throws IOException 
    {
        _channel = SocketChannel.open(sa);
        // already connected by now

        setDestinationAddress(address,port);
        setUsername(username);
        setPassword(password);
        finishConnect();
    }
                        
    /////////////////////////////////////////////////////////////////
    ////////////////////// specializing /////////////////////////////
    /////////////////////////////////////////////////////////////////                
    private boolean isClosed() { return !_channel.isOpen(); }
        
    private InetAddress getInetAddress() {
        if (_channel.socket().getInetAddress() != null &&
            getDestinationAddress() != null)
            return getDestinationAddress().getAddress();
        return null;
    }
        
    private int getPort() {
        int result = _channel.socket().getPort();
        if (result > 0) {
            if (getDestinationAddress() == null) {
                // should not happen
                result = -1;
            } else {
                result = getDestinationAddress().getPort();
            }
        }
        return result;
    }

    public String toString() {
        if (_channel.isConnected()) {
            return _channel.socket().toString() +
                " SOCKS[socksAddr=" + null +
                ", socksPort=" + null + "]";
        }
        return "Socket[unconnected]";
    }

    private InetSocketAddress getProxyAddress() {
        return socksProxyAddress;
    }
        
    /**
     * @param port will resolve to default SOCKS server port if <=0
     */
    private void setProxyAddress(String host, int port)
        throws UnknownHostException, IOException {
            if (_channel.isConnected() || isClosed())
                throw new IllegalStateException(
                                                "Socket is currently connected or has been closed");
            if(null == host || host.trim().length()==0)
                throw new IllegalArgumentException(
                                                   "parameter 'host' may not be null or blank");
            setProxyAddress(InetAddress.getByName(host),
                            port <= 0 ? DEFAULT_PORT : port);
    }
        
    /**
     * @param port will resolve to default SOCKS server port if <=0
     */
    private void setProxyAddress(InetAddress address, int port)
        throws IOException {
            if(_channel.isConnected() || isClosed())
                throw new IllegalStateException(
                                                "Socket is currently connected or has been closed");
            if(null == address)
                throw new IllegalArgumentException(
                                                   "parameter 'address' may not be null");
            socksProxyAddress = new InetSocketAddress(address,
                                                      port <= 0 ? DEFAULT_PORT : port);
    }
        
    private void setProxyAddress(InetSocketAddress address)
        throws IOException {
            if(_channel.isConnected() || isClosed())
                throw new IllegalStateException(
                                                "Socket is currently connected or has been closed");
            if(null == address)
                throw new IllegalArgumentException(
                                                   "parameter 'address' may not be null");
            socksProxyAddress = address;
    }

    private void setProxyAddress(SocketAddress address)
        throws IOException
    {
        if (address instanceof InetSocketAddress) {
            setProxyAddress((InetSocketAddress)address);
        } else {
            throw new IllegalArgumentException("parameter 'address' must be an InetSocketAddress");
        }
    }
        
    private InetSocketAddress getDestinationAddress() {
        return destinationAddress;
    }

    private void setDestinationAddress(String host, int port)
        throws UnknownHostException, IOException {
            if(null == host || host.trim().length()==0)
                throw new IllegalArgumentException(
                                                   "parameter 'host' may not be null or blank");
            setDestinationAddress(
                                  new InetSocketAddress(host,port <= 0 ? DEFAULT_PORT : port));
    }
        
    private void setDestinationAddress(InetAddress address, int port)
        throws IOException {
            if(null == address)
                throw new IllegalArgumentException(
                                                   "parameter 'address' may not be null");
            destinationAddress = new InetSocketAddress(address,
                                                       port <= 0 ? DEFAULT_PORT : port);
    }
        
    private void setDestinationAddress(InetSocketAddress address) {
        if(null == address)
            throw new IllegalArgumentException("parameter 'address' may not be null");
        destinationAddress = address;
    }
        
    private String getUsername() {
        return username;
    }
        
    private void setUsername(String username) {
        this.username = username;
    }
        
    private String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public SocketChannel getChannel() { return _channel; }
        
    private InetSocketAddress socksProxyAddress;
    private InetSocketAddress destinationAddress;
    private String username;
    private String password;
    private SocketChannel _channel = null;
        
                
    private int readSocksReply(InputStream in, byte[] data)
        throws IOException 
    {
        int len = data.length;
        int received = 0;
        for (int attempts = 0; received < len && attempts < 3; attempts++) {
            int count = in.read(data, received, len - received);
            if (count < 0)
                throw new SocketException("Malformed reply from SOCKS server");
            received += count;
        }
        return received;
    }

    private boolean authenticate(byte method, InputStream in,
                                 DataOutputStream out) throws IOException
    {
                
        // No Authentication required. We're done then!
        if (method == NO_AUTH)
            return true;
                
        if (method == USER_PASSW) {
            if(getUsername() == null)
                return false;
                        
            out.write((byte)1);
            out.write((byte)getUsername().length());
            out.write(getUsername().getBytes());
                        
            if (getPassword() != null) {
                out.write((byte)getPassword().length());
                out.write(getPassword().getBytes());
            } else {
                out.write((byte)0);
            }        

            out.flush();
                        
            byte[] data = new byte[2];
            int i = readSocksReply(in, data);
            if (i != 2 || data[1] != 0) {
                /* RFC 1929 specifies that the connection MUST be closed if
                   authentication fails */
                out.close();
                //in.close();
                _channel.close();
                return false;
            }
            /* Authentication succeeded */
            return true;
        }
        return false;
    }        
        
    // method was called 'connect'
    private void finishConnect() throws IOException { 
        DataOutputStream out = new DataOutputStream(_channel.socket().getOutputStream());
        InputStream in = _channel.socket().getInputStream();

        // This is SOCKS V5
        out.write(PROTO_VERS);
        out.write((byte)2);
        out.write(NO_AUTH);
        out.write(USER_PASSW);
        out.flush();
        byte[] data = new byte[2];
        int i = readSocksReply(in, data);
        if (i != 2 || ((int)data[1]) == NO_METHODS)
            throw new SocketException("SOCKS : No acceptable methods");
        if (!authenticate(data[1], in, out))
            throw new SocketException("SOCKS : authentication failed");

        out.write(PROTO_VERS);
        out.write(CONNECT);
        out.write((byte)0);
                
        InetSocketAddress epoint = getDestinationAddress();
                
        /* Test for IPV4/IPV6/Unresolved */
        if (epoint.isUnresolved()) {
            out.write(DOMAIN_NAME);
            out.write(epoint.getHostName().length());
            out.write(epoint.getHostName().getBytes());
            out.write((byte)((epoint.getPort() >> 8) & 0xff));
            out.write((byte)((epoint.getPort() >> 0) & 0xff));
        } else if (epoint.getAddress() instanceof Inet6Address) {
            out.write(IPV6);
            out.write(epoint.getAddress().getAddress());
            out.write((byte)((epoint.getPort() >> 8) & 0xff));
            out.write((byte)((epoint.getPort() >> 0) & 0xff));
        } else {
            out.write(IPV4);
            out.write(epoint.getAddress().getAddress());
            out.write((byte)((epoint.getPort() >> 8) & 0xff));
            out.write((byte)((epoint.getPort() >> 0) & 0xff));
        }
        out.flush();
        data = new byte[4];
        i = readSocksReply(in, data);
        if (i != 4)
            throw new SocketException("Reply from SOCKS server has bad length");
        SocketException ex = null;
        int nport, len;
        byte[] addr;
        switch (data[1]) {
        case REQUEST_OK:
            // success!
            switch(data[3]) {
            case IPV4:
                addr = new byte[4];
                i = readSocksReply(in, addr);
                if (i != 4)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                nport = ((int)data[0] & 0xff) << 8;
                nport += ((int)data[1] & 0xff);
                break;
            case DOMAIN_NAME:
                len = data[1];
                byte[] host = new byte[len];
                i = readSocksReply(in, host);
                if (i != len)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                nport = ((int)data[0] & 0xff) << 8;
                nport += ((int)data[1] & 0xff);
                break;
            case IPV6:
                len = data[1];
                addr = new byte[len];
                i = readSocksReply(in, addr);
                if (i != len)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                data = new byte[2];
                i = readSocksReply(in, data);
                if (i != 2)
                    throw new SocketException("Reply from SOCKS server badly formatted");
                nport = ((int)data[0] & 0xff) << 8;
                nport += ((int)data[1] & 0xff);
                break;
            default:
                ex = new SocketException("Reply from SOCKS server contains wrong code");
                break;
            }
            break;
        case GENERAL_FAILURE:
            ex = new SocketException("SOCKS server general failure");
            break;
        case NOT_ALLOWED:
            ex = new SocketException("SOCKS: Connection not allowed by ruleset");
            break;
        case NET_UNREACHABLE:
            ex = new SocketException("SOCKS: Network unreachable");
            break;
        case HOST_UNREACHABLE:
            ex = new SocketException("SOCKS: Host unreachable");
            break;
        case CONN_REFUSED:
            ex = new SocketException("SOCKS: Connection refused");
            break;
        case TTL_EXPIRED:
            ex =  new SocketException("SOCKS: TTL expired");
            break;
        case CMD_NOT_SUPPORTED:
            ex = new SocketException("SOCKS: Command not supported");
            break;
        case ADDR_TYPE_NOT_SUP:
            ex = new SocketException("SOCKS: address type not supported");
            break;
        }
                
        if (ex != null) {
            //in.close();
            out.close();
            _channel.close();
            throw ex;
        }
    }
        
    /////////////////////////////////////////////////////////////////
    /////////////////////// test ////////////////////////////////////
    /////////////////////////////////////////////////////////////////
        
    public static void main(String[] args) 
    {
        final String HTTP_GET = "<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams'>\r\n";
        String sHostPort = "localhost";
        String sProxyHostPort = "localhost";
        String user="foo";
        String password="bar";
        if (args.length >= 1) sHostPort = args[0];
        if (args.length >= 2) sProxyHostPort = args[1];
        if (args.length == 4) {
            user = args[2];
            password = args[3];
        }

        try {
            HostPort server = new HostPort(sHostPort, 80);
            HostPort proxy = new HostPort(sProxyHostPort, 1080);

            SocketChannel sc =
                Socks5SocketChannelAdaptor.open(server.getHostName(),
                                                server.getPort(),
                                                proxy.getHostName(),
                                                proxy.getPort(),
                                                user, password);

            System.out.println("Sending -> " + HTTP_GET);
            ByteBuffer bb = ByteBuffer.wrap(HTTP_GET.getBytes("US-ASCII"));
            sc.write(bb);

            System.out.println("Receiving <-");
            int len;
            bb = ByteBuffer.allocate(1024); 
            byte[] b = new byte[1024];
            do {
                bb.rewind();
                len = sc.read(bb);                
                System.out.println("Receiving <- " + len);
                if (len > 0) {
                    bb.flip();
                    bb.get(b, 0, len);
                    System.out.print(new String(b, 0, len));
                }
            } while (len >= 0);

            sc.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
