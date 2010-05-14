/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.debugger.api;


/**
 * Mechanism to store host and port number of a
 * BPEL service engine that we are attaching to.
 *
 * @author Sun Microsystems
 */
public final class AttachingCookie {

    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String ID = "netbeans-bpel-AttachingDICookie";

    private String mHost;
    private int mPort;


    private AttachingCookie(String host, String port) {
        mHost = host;
        mPort = Integer.parseInt(port);
    }
    
    
    /**
     * Creates a new instance of AttachingDICookie for given parameters.
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a potr number
     * @return a new instance of AttachingDICookie for given parameters
     */
    public static AttachingCookie create (String host, String port) {
        return new AttachingCookie(host, port);
        
        
    }

    /**
     * Returns port number.
     *
     * @return port number
     */
    public int getPort() {
        return mPort;
    }

    /**
     * Returns name of computer.
     *
     * @return name of computer
     */
    public String getHost() {
        return mHost;
    }


    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return mHost.hashCode()*37 +  mPort*7;
    }


    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return mHost + "/" + mPort;
    }


    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if (! (obj instanceof AttachingCookie)) 
            return false;
        return ((AttachingCookie)obj).mHost.equals(mHost) && (((AttachingCookie)obj).mPort == mPort);
    }
}
