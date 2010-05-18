/*****************************************************************************
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):

 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Gerrit Riessen.
 * Portions created by Gerrit Riessen are Copyright (C) 2000.
 * All Rights Reserved.
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

 * Contributor(s): Gerrit Riessen.
 *****************************************************************************/

package org.netbeans.lib.cvsclient.connection;

import org.netbeans.lib.cvsclient.CVSRoot;

/**
 Simple class for managing the mapping from CVSROOT specifications to
 Connection classes.
 @author <a href="mailto:gerrit.riessen@wiwi.hu-berlin.de">Gerrit Riessen</a>, OAR Development AG
 @author <a href="mailto:rami.ojares@elisa.fi">Rami Ojares</a>, Elisa Internet Oy
 */
public class ConnectionFactory {
    
    /**
     <b>Protected Constructor</b>
     */
    protected ConnectionFactory() {}
    
    /**
     * Returns a Connection object to handle the specific CVSRoot
     * specification. This returns null if not suitable connection
     * was found.
     * 
     * If the return value is an instance of the PServerConnection class,
     * then the encoded password needs to be set if not defined in the CVSRoot.
     * This is left up to the client to set.
     */
    public static Connection getConnection(String cvsRoot) throws IllegalArgumentException {
        
        CVSRoot root = CVSRoot.parse(cvsRoot);
        return getConnection(root);
        
    }
    
    /**
     * Returns a Connection object to handle the specific CVSRoot
     * specification. This returns null if not suitable connection
     * was found.
     * 
     * If the return value is an instance of the PServerConnection class,
     * then the encoded password needs to be set if not defined in the CVSRoot.
     * This is left up to the client to set.
     */
    public static Connection getConnection(CVSRoot root) throws IllegalArgumentException {
        
        // LOCAL CONNECTIONS (no-method, local & fork)
        if (root.isLocal()) {
            LocalConnection con = new LocalConnection();
            con.setRepository(root.getRepository());
            return con;
        }
        
        String method = root.getMethod();
        // SSH2Connection (server, ext)
        /* SSH2Connection is TBD
        if (
            method == null || CVSRoot.METHOD_SERVER == method || CVSRoot.METHOD_EXT == method
        ) {
            // NOTE: If you want to implement your own authenticator you have to construct SSH2Connection yourself
            SSH2Connection con = new SSH2Connection(
                root,
                new ConsoleAuthenticator()
            );
            return con;
        }
         */
        
        // PServerConnection (pserver)
        if (CVSRoot.METHOD_PSERVER == method) {
            PServerConnection con = new PServerConnection(root);
            return con;
        }
        
        throw new IllegalArgumentException("Unrecognized CVS Root: " + root);
        
    }

}
