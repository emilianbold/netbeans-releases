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
package org.netbeans.lib.collab.xmpp.httpbind.providers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.xmpp.httpbind.ConnectionProvider;

/**
 *
 * @author Mridul Muralidharan
 */
public class ConnectionProviderImpl implements ConnectionProvider{

    private Map connParams;

    /**
     * Opens a new connection to the specified url and configures it with the
     * relevent properties.
     */
    public HttpURLConnection openConnection(URL destination) throws IOException{
        HttpURLConnection conn = (HttpURLConnection)
            destination.openConnection();
        setConnProperties(conn);
        return conn;
    }
    
    protected void setConnProperties(URLConnection conn){
        //conn.setConnectTimeout(getConnectionTimeout());
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setDefaultUseCaches(false);
        conn.setUseCaches(false);
        /*
         * 1.5 introduced setReadTimeout which makes life easy for us.
         * As of now , I am not handling the case of infinite/very long
         * blocks on the read , will need to have a seperate watcher thread
         * which will interrupt this 'read' in case of long delays so that
         * it acts like a timeout ...
         * TODO
         */
        //conn.setReadTimeout(getConnectionTimeout());
    }
    
    /**
     * Set the properties associated with this provider
     */
    public void setProperties(Map connParams){
        this.connParams = connParams;
    }
    /**
     * Get the properties associated with this provider
     */
    public Map getProperties(){
        return connParams;
    }
    
    /**
     * Creates and returns a new instance of itself.
     */
    public ConnectionProvider createInstance(Map connParams)
    throws CollaborationException{
        ConnectionProvider conn = new ConnectionProviderImpl();
        conn.setProperties(connParams);
        return conn;
    }

    public int getConnectionTimeout(){
        String str = (String)getProperties().
                get(ConnectionProvider.CONNECTION_TIMEOUT);
        
        int retval = ConnectionProvider.DEFAULT_CONNECTION_TIMEOUT;
        
        try{
            if (null != str){
                retval = Integer.parseInt(str);
            }
        }catch(NumberFormatException nfEx){}
        return retval;
    }
}
