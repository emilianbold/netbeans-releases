/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
