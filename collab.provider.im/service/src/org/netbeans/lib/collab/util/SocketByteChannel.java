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

import java.io.*;
import java.net.*;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;

import java.nio.channels.*;
import java.nio.*;

/**
 * ByteChannel implementation on top of a socket.
 *
 * @author Vijayakumar Palaniappan
 *
 */
public class SocketByteChannel implements ByteChannel {

    private Socket _socket = null;

    public SocketByteChannel(Socket s)
    {
	_socket = s;
    }

    public int write(ByteBuffer src) throws IOException
    {
	int len = src.remaining();
	_socket.getOutputStream().write(src.array(), src.position(), len);
	try {
	    src.position(src.position()+len);
	} catch (IllegalArgumentException iae) {
	    // never happens
	}
	return len;
    }
 
    public int read(ByteBuffer dst) throws IOException
    {
        if(_socket instanceof SSLSocket) {
            SSLSession session = (SSLSession)((SSLSocket)_socket).getSession();
            //for jsse Bug - 4836493
            session.putValue("BUG_4836493_READ", Boolean.TRUE);
        }
            
        int len = 0;
        try {
            len = _socket.getInputStream().read(dst.array(), dst.position(), dst.remaining());
	
            dst.position(dst.position()+len);
	} catch (IllegalArgumentException iae) {
	    // never happens
	} catch(SocketTimeoutException e) {
    }
	return len;
    }

    public void close() throws IOException
    {
	_socket.close();
    }

    public boolean isOpen() {
	return _socket.isConnected();
    }

}
