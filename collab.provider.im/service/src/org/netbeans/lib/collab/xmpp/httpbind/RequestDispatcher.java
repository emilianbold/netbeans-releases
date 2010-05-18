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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.xmpp.httpbind.providers.ConnectionProviderImpl;

/**
 *
 * @author Mridul Muralidharan
 */
public class RequestDispatcher implements Runnable, HTTPBindConstants{
    private static ConnectionProvider _connectionProvider = null;
    // default value - 2 mins
    private static final int connectionTimeout = 120 * 1000;
    
    static{
        
        try{
            _connectionProvider = createConnectionProvider(
                    System.getProperty(CONNECTION_PROVIDER) , 
                    new ConnectionProviderImpl() , new HashMap());
        }catch(Exception ex){}
    }

    public static final int RESPONSE_OK = 200;

    private ConnectionProvider connectionProvider = null;
    private String sid;
    private URL gatewayURL;
    private List gatewayResponseListeners = new LinkedList();

    private boolean dispatchInterest = false;
    private boolean deregistered = false;
    //private Proxy proxy
    private List dataList = new LinkedList();
    private boolean waitStart = false;
    private boolean abortThread = false;
    
    private Object lockObj = new Object();
    
    public RequestDispatcher(Map params) throws CollaborationException {
        ConnectionProvider connp = 
                createConnectionProvider(
                    (String)params.get(CONNECTION_PROVIDER) ,
                    _connectionProvider , params);
        setConnectionProvider(connp);
    }

    public void init(){
        Thread th = new Thread(this);
        th.start();
        while (!waitStart){
            try{
                Thread.currentThread().sleep(100);
            }catch(InterruptedException iEx){}
        }
    }
    
    public void run(){
        boolean done = false;
        // Once we encounter an error , we will try to abort and exit as soon as
        // possible - except when the final termination should happen _after_
        // this last request goes to server.
        boolean encounteredError = false;
        
        while (!done){
            synchronized(lockObj){
                waitStart = true;
                fireInstanceAvailableListener();
                while (0 == dataList.size()){
                    try{
                        assert (!encounteredError ||
                                isAbortThread());
                        if (isAbortThread()){
                            deregisterDispatcher();
                            return ;
                        }
                        lockObj.wait();
                        if (isAbortThread() && !isDispatchInterest() &&
                                0 == dataList.size()){
                            // If there is data to be sent , send it first before
                            // exiting thread.
                            deregisterDispatcher();
                            return ;
                        }
                    }catch(InterruptedException iEx){
                        //iEx.printStackTrace();
                        HTTPSessionController.debug(iEx.toString() , iEx);
                    }
                }
                waitStart = false;
            }
            
            byte[] data = null;
            String rid = null;
            boolean moreDone = false;
            
            while (!moreDone)
            {
                synchronized (lockObj){
                    assert (!encounteredError ||
                            isAbortThread());
                    if (0 == dataList.size() || encounteredError){
                        if (isAbortThread() && !isDispatchInterest()){
                            deregisterDispatcher();
                            return ;
                        }
                        break;
                    }
                    data = (byte[])dataList.remove(0);
                    rid = (String)dataList.remove(0);
                }
                
                try{
                    setDispatchInterest(false);
                    byte[] resp = sendAndWait(data , rid);
                    if (null != resp){
                        fireGatewayResponseListener(
                                GatewayResponseListener.DATA_ARRIVED, resp);
                    }
                    else{
                        encounteredError = true;
                    }
                }catch(MalformedURLException mduEx){
                    // Should never happen ...
                    //mduEx.printStackTrace();
                    HTTPSessionController.debug(mduEx.toString() , mduEx);
                }
            }
        }
        
        assert (isDeregistered());
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("[" + Thread.currentThread().getName() +  // NOI18N
                "] run : exiting"); // NOI18N
        }
    }
    
    // Should be invoked while holding a lock on dataList
    // This will prevent addition of data to this dispatcher
    // while it is being deregistered.
    protected void deregisterDispatcher(){
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("[" + Thread.currentThread().getName() +  // NOI18N
                "] deregisterDispatcher : " + deregistered); // NOI18N
        }
        if (!deregistered){
            deregistered = true;
            fireGatewayResponseListener(GatewayResponseListener.DISPATCHER_EXIT , 
                    this);
        }
        return ;
    }
    
    protected void abortThread(){
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("[" + Thread.currentThread().getName() +  // NOI18N
                "] RequestDispatcher.abortThread() : START"); // NOI18N
        }
        synchronized(lockObj){
            lockObj.notify();
            setAbortThread(true);
        }
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("[" + Thread.currentThread().getName() +  // NOI18N
                "] RequestDispatcher.abortThread() : END"); // NOI18N
        }
    }

    public boolean dispatch(byte[] data , long rid){
        
        if (null == data){
            throw new NullPointerException("data == null"); // NOI18N
        }

        synchronized(lockObj){
            if (isDeregistered()){
                return false;
            }
            dataList.add(data);
            dataList.add("" + rid); // NOI18N
            lockObj.notify();
        }
        return true;
    }

    // TODO : Hardcoded timeout of 10 retries ... make it customizable ?
    protected byte[] sendAndWait(byte data[] , String rid) throws MalformedURLException{
        
        //boolean resendGenerated = false;
        int retryCount = 10;
        boolean done = false;
        
        while (!done){
            URL url = getGatewayURL();
            HttpURLConnection conn = null;
            
            try{
                conn = getConnectionProvider().openConnection(url);
                writeHTTPRequest(conn , data);
            }catch(IOException ioEx){
                retryCount --;
                //ioEx.printStackTrace();
                HTTPSessionController.debug(ioEx.toString() , ioEx);
                if (null != conn){
                    closeConn(conn);
                }
                if (HTTPSessionController.isDebugOn()){
                    HTTPSessionController.debug("[" + Thread.currentThread().getName() +  // NOI18N
                    "] sendAndWait : " + retryCount , ioEx); // NOI18N
                }
                if (retryCount < 0){
                    break;
                }
                continue;
            }

            try{
                byte[] retdata = readHttpResponse(conn);
                closeConn(conn);
                return retdata;                
            }catch(IOException ioEx){
                retryCount --;
                //ioEx.printStackTrace();
                HTTPSessionController.debug(ioEx.toString() , ioEx);
                closeConn(conn);
                if (HTTPSessionController.isDebugOn()){
                    HTTPSessionController.debug("[" + Thread.currentThread().getName() +  // NOI18N
                        "] sendAndWait : " + retryCount , ioEx); // NOI18N
                }
                // i think I had got this wrong - we are expected to resend the 
                // whole request - not a new request.
                /*
                // Ask the server to resend data for this 'rid'
                if (!resendGenerated){
                    data = generateResendRequest(rid);
                    resendGenerated = true;
                }
                 */
                if (retryCount < 0){
                    break;
                }
            }
        }
        // Ok , the gateway maybe down !
        // flag an error.
        fireSessionInvalidated();
        return null;
    }
    
    public void addGatewayResponseListener(GatewayResponseListener listener){
        if (!gatewayResponseListeners.contains(listener)){
            gatewayResponseListeners.add(listener);
        }
    }

    public void fireGatewayResponseListener(int flag , Object payload){
        Iterator iter = gatewayResponseListeners.iterator();
        
        while (iter.hasNext()){
            GatewayResponseListener listener = (GatewayResponseListener)iter.next();
            listener.processGatewayResponse(
                    flag, payload);
        }
    }

    public void fireInstanceAvailableListener(){
        Iterator iter = gatewayResponseListeners.iterator();
        
        while (iter.hasNext()){
            GatewayResponseListener listener = (GatewayResponseListener)iter.next();
            listener.instanceAvailable(this);
        }
        setDispatchInterest(false);
    }
    
    public void fireSessionInvalidated(){
        fireGatewayResponseListener(
                GatewayResponseListener.CONNECTION_LOST, this);
    }

    protected void writeHTTPRequest(URLConnection conn , byte[] data) throws IOException{
        conn.setRequestProperty("content-type" , REQUEST_CONTENTTYPE);
        conn.connect();
        OutputStream out = conn.getOutputStream();
        //System.out.println("Writing request : \n" + data); // NOI18N
        
        out.write(data);
        out.flush();
        out.close();
    }

    protected byte[] readHttpResponse(HttpURLConnection conn) throws IOException{
        
        InputStream instrm = null;
        try{
            instrm = conn.getInputStream();
        }catch(FileNotFoundException fnfEx){
            fireSessionInvalidated();
            if (HTTPSessionController.isDebugOn()){
                HTTPSessionController.debug("[" + Thread.currentThread().getName() +  // NOI18N
                    "] readHttpResponse : " + fnfEx); // NOI18N
            }
            return null;
        }catch(IOException ioEx){
            throw ioEx;
        }
        int len = conn.getContentLength();
        
        if (RESPONSE_OK != conn.getResponseCode()){
            // Ok , gateway returned error !
            // Terminate this session.
            if (HTTPSessionController.isDebugOn()){
                HTTPSessionController.debug("[" + Thread.currentThread().getName() +  // NOI18N
                    "] readHttpResponse - responsecode : " +  // NOI18N
                    conn.getResponseCode());
            }
            fireSessionInvalidated();
            return null;
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // clumsy code
        byte[] arr = new byte[1024];
        
        while (len > arr.length){
            int size = instrm.read(arr);
            if (size < 0){
                throw new EOFException("End of file exception"); // NOI18N
            }
            len -= size;
            baos.write(arr , 0 , size);
        }
        
        while (len > 0){
            int size = instrm.read(arr , 0 , len);
            if (size < 0){
                throw new EOFException("End of file exception"); // NOI18N
            }
            len -= size;
            baos.write(arr , 0 , size);
        }

        return baos.toByteArray();
    }

    protected void closeConn(URLConnection conn){
        try{
            conn.getInputStream().close();
        }catch(IOException ioEx){}
        try{
            conn.getOutputStream().close();
        }catch(IOException ioEx){}
    }
    
    public String getSID(){
        return sid;
    }
    
    public void setSID(String sid){
        this.sid = sid;
    }

    public URL getGatewayURL(){
        return gatewayURL;
    }

    public void setGatewayURL(URL gatewayURL){
        this.gatewayURL = gatewayURL;
    }
    
    public void setConnectionTimeout(int connectionTimeout){
        connectionProvider.getProperties().put(
                ConnectionProvider.CONNECTION_TIMEOUT ,
                new Integer(connectionTimeout 
                    //+ 20 * 1000
                    )
                );
    }
    
    public void setAbortThread(boolean abortThread){
        this.abortThread = abortThread;
    }
    
    public boolean isAbortThread(){
        return abortThread;
    }

    public boolean isDeregistered(){
        return deregistered;
    }
    
    public void setDispatchInterest(boolean dispatchInterest){
        this.dispatchInterest = dispatchInterest;
    }
    
    public boolean isDispatchInterest(){
        return dispatchInterest;
    }

    public Object getLockObj(){
        return lockObj;
    }
    public void setConnectionProvider(ConnectionProvider connectionProvider){
        this.connectionProvider = connectionProvider;
    }

    public ConnectionProvider getConnectionProvider(){
        return connectionProvider;
    }
    
    private static ConnectionProvider createConnectionProvider(
            String className , 
            ConnectionProvider def , 
            Map connParams) throws CollaborationException{
        
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("createConnectionProvider - className : " + className + " def : " + def + "\nconnParams : " + connParams);
        }
        ConnectionProvider retval = null;
        
        if (null != className){
            try{
                retval = (ConnectionProvider)Class.forName(
                        className).newInstance();
                retval = retval.createInstance(connParams);
            }catch(Exception ex){
                if (HTTPSessionController.isDebugOn()){
                    HTTPSessionController.debug("createConnectionProvider : " + className, ex);
                }
            }
        }
        
        if (null == retval){
            retval = def.createInstance(connParams);
        }
        
        assert (null != retval);
        
        return retval;
    }
}
