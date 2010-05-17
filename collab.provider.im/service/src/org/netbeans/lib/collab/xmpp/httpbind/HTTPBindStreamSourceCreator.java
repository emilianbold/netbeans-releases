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
package org.netbeans.lib.collab.xmpp.httpbind;

import java.util.HashMap;
import org.netbeans.lib.collab.CollaborationSessionListener;
import org.netbeans.lib.collab.util.Worker;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jabberstudio.jso.io.StreamSource;
import java.util.Map;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.xmpp.StreamSourceCreator;

/**
 *
 * @author Mridul Muralidharan
 */
public class HTTPBindStreamSourceCreator 
        implements StreamSourceCreator , DataAvailableNotifier , 
        DataArrivedEventHandler , HTTPBindConstants{
    
    protected List inputDataListeners = new LinkedList();
    protected URL gatewayURL;
    protected Worker _worker;
    protected Map connParams;
    private CollaborationSessionListener _sessionListener;
    private Map negotiatedParams = new HashMap();
    
    // copied from XMPPSessionProvider
    static int concurrency = 10;
    
    {
        _worker = new Worker(1, concurrency, concurrency*20);
    }

    public HTTPBindStreamSourceCreator(URL url , Map parameters,
            CollaborationSessionListener listener) {
        _sessionListener = listener;
        parameters.put(SESSION_LISTENER , listener);
        setConnParams(parameters);
        setGatewayURL(url);
    }
    
    public Map getNegotiatedParameters(){
        return negotiatedParams;
    }
    
    public Map getConnParams(){
        return connParams;
    }

    protected void setConnParams(Map connParams){
        this.connParams = connParams;
    }
    
    protected void setGatewayURL(URL url){
        this.gatewayURL = url;
    }

    protected URL getGatewayURL(){
        return gatewayURL;
    }

    public void addInputDataListeners(Runnable runnable){
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("addInputDataListeners()"); // NOI18N
        }
        
        synchronized(inputDataListeners){
            if (!inputDataListeners.contains(runnable)){
                inputDataListeners.add(runnable);
            }
        }
    }    

    public StreamSource createStreamSource(String hostName, int port) throws Exception {
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("JEP124StreamSourceCreator.createStreamSource()"); // NOI18N
        }
        HTTPBindStreamSource src = new HTTPBindStreamSource(getGatewayURL() , 
                getConnParams() , getNegotiatedParameters());
        src.addDataArrivedEventHandler(this);
        return src;
    }
    
    public void setParameter(String key , String value){
        getConnParams().put(key , value);
    }
    
    public String getParameter(String key , String def){
        String retval = (String)getConnParams().get(key);
        return null == retval ? def : retval;
    }

    public void dataArrivedNotification(){
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("JEP124StreamSourceCreator.dataArrivedEventHandler()"); // NOI18N
            HTTPSessionController.debug("inputDataListeners : " + inputDataListeners.size() + 
                    " _worker : " + _worker.isFull() + " , backlog : " + _worker.backlog());
            HTTPSessionController.debug("inputDataListeners : " + inputDataListeners);
        }
        synchronized(inputDataListeners){
            Iterator iter = inputDataListeners.iterator();
            
            while (iter.hasNext()){
                Runnable runnable = (Runnable)iter.next();
                _worker.addRunnable(runnable);
            }
        }
    }

    public CollaborationSessionListener getSessionListener() {
        return _sessionListener;
    }

    public boolean isTLSSupported(){
        return false;
    }
    
    public void upgradeToTLS(StreamSource stream) throws IOException , 
            CollaborationException  , UnsupportedOperationException{
        throw new UnsupportedOperationException("Not supported");
    }
}
