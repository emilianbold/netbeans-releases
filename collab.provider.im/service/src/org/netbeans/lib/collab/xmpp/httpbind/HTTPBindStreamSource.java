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
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.jabberstudio.jso.Stream;
import org.jabberstudio.jso.io.StreamSource;

// Problem - java.net.Proxy is available only on 1.5
//import java.net.Proxy;


/**
 *
 * @author Mridul Muralidharan
 */
public class HTTPBindStreamSource implements
        StreamSource , HTTPBindConstants{

    //private URL gatewayURL;
    private String domain;
    //private Proxy proxy;
    private HTTPSessionController bridge;
    private boolean connected = false;
    
    /**
     * Creates a new instance of HTTPBindStreamSource 
     */
    public HTTPBindStreamSource(URL gatewayURL , Map connParams , Map negotiatedParams) throws IOException {
        
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("HTTPBindStreamSource conn params : " + connParams);
        }
        
        String to = getStringValue(connParams , TO_DOMAIN_PARAMETER , null);
        
        if (null == to){
            throw new IllegalArgumentException("Domain not specified"); // NOI18N
        }
        
        if (null == gatewayURL){
            throw new IllegalArgumentException("Invalid gateway URL"); // NOI18N
        }

        //setGatewayURL(gatewayURL);
        setDomain(to);
        
        //setProxy(proxy);
        HTTPSessionController bridge = new HTTPSessionController();

        // Manatory
        bridge.setDomain(to);
        bridge.setGatewayURL(gatewayURL);

        bridge.setMaxRequests(getIntValue(connParams , MAXREQUESTS_PARAMETER , 
                DEFAULT_MAX_REQUESTS));
        bridge.setWaitTime(getIntValue(connParams , WAIT_PARAMETER, 
                DEFAULT_WAIT_TIME) * 1000);
        bridge.setContentType(getStringValue(connParams , CONTENTTYPE_PARAMETER, 
                DEFAULT_CONTENTTYPE));
        bridge.setXmlLang(getStringValue(connParams , XML_LANG_PARAMETER, 
                DEFAULT_XML_LANG));
        bridge.setRouteServer(getStringValue(connParams , ROUTE_PARAMETER, 
                bridge.getRouteServer()));
        bridge.setConnectionParameters(connParams);
        bridge.setNegotiatedParameters(negotiatedParams);
        setBridge(bridge);
    }
    
    // Delegate this to the bridge ?
    public void addDataArrivedEventHandler(DataArrivedEventHandler handler){
        getBridge().addDataArrivedEventHandler(handler);
    }
    
    /**
     * <p>
     * Disconnects this <tt>StreamSource</tt> from input and output. Once this
     * method is called, <tt>{@link #read}</tt> and <tt>{@link #write}</tt>
     * will throw <tt>IOException</tt>.</p>
     *
     * <p>
     * This method takes the <tt>Stream</tt> performing the method call,
     * allowing this <tt>StreamSource</tt> to uninitialize itself.</p>
     *
     * <p>
     * The value of <tt>s</tt> must not be <tt>null</tt>, or a
     * <tt>NullPointerException</tt> is thrown.</p>
     *
     * <p>
     * If there are any I/O errors in closing this <tt>StreamSource</tt>,
     * this method throws an <tt>IOException</tt>.  All other errors
     * result in some form of <tt>RuntimeException</tt> being thrown.</p>
     *
     * @param s The <tt>Stream</tt> to disconnect from.
     * @throws IOException If any I/O errors occur trying to close this
     * <tt>StreamSource</tt>.
     * @throws Exception If any other errors occur trying to close this
     * <tt>StreamSource</tt>.
     * @since   JSO-0.10
     */    
    public void disconnect(Stream s) throws IOException, Exception{
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("JEP124StreamSource.disconnect()"); // NOI18N
        }
        if (!isConnected()){
            throw new IllegalStateException("Already disconnected"); // NOI18N
        }
        setConnected(false);
        getBridge().disconnect();
    }
    
    /**
     * <p>
     * Deterines if this <tt>StreamSource</tt> is ready to attempt a read.</p>
     *
     * <p>
     * This method may return <tt>true</tt> if there is no data immediately
     * available, but is not yet closed or has reached the end of file.</p>
     *
     * @return <tt>true</tt> if this <tt>StreamSource</tt> can still be used.
     * @since JSO-0.10
     */
    public boolean ready() throws IOException{
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("JEP124StreamSource.ready()"); // NOI18N
        }
        return getBridge().isInitialised();
    }
    /**
     * <p>
     * Reads bytes from this <tt>StreamSource</tt> into the specified byte
     * buffer array.</p>
     *
     * <p>
     * This method returns the number of bytes read, or -1 if the end of the
     * input stream is reached.</p>
     *
     * <p>
     * The value of <tt>buffer</tt> cannot be <tt>null</tt>, or an
     * <tt>IllegalArgumentException</tt> is thrown.</p>
     *
     * <p>
     * The value of <tt>offset</tt> and <tt>length</tt> must represent a valid
     * range within <tt>buffer</tt>, or an <tt>IllegalArgumentException</tt>
     * is thrown. For <tt>offset</tt> the valid ranges are:</p>
     *
     * <table border='0' cellpadding='0' cellspacing='0'>
     * <tr><th>offset</th><td><tt>0 <= offset < (buffer.length - length)</tt></td></tr>
     * <tr><th>length</th><td><tt>0 <= length < (buffer.length - offset)</tt></td></tr>
     * </table>
     *
     * @param buffer The buffer to read to.
     * @param offset The offset into <tt>buffer</tt> to start at.
     * @param length The length within <tt>buffer</tt> to use.
     * @throws IllegalArgumentException If any parameters are invalid.
     * @throws IOException If there are any I/O errors.
     * @since JSO-0.10
     */
    public int read(byte[] buffer, int offset, int length) 
        throws IllegalArgumentException, IOException{
        
        if (!isConnected() || !getBridge().isInitialised()){
            throw new IOException("Not connected"); // NOI18N
        }
        
        validateInputBounds(buffer , offset , length);
        
        // Delegate to HTTPSessionController
        return getBridge().readServerResponse(buffer , offset , length);
    }
    /**
     * <p>
     * Writes bytes to this <tt>StreamSource</tt> from the specified byte
     * buffer array.</p>
     *
     * <p>
     * This method returns the number of bytes written.</p>
     *
     * <p>
     * The value of <tt>buffer</tt> cannot be <tt>null</tt>, or an
     * <tt>IllegalArgumentException</tt> is thrown.</p>
     *
     * <p>
     * The value of <tt>offset</tt> and <tt>length</tt> must represent a valid
     * range within <tt>buffer</tt>, or an <tt>IllegalArgumentException</tt>
     * is thrown. For <tt>offset</tt> the valid ranges are:</p>
     *
     * <table border='0' cellpadding='0' cellspacing='0'>
     * <tr><th>offset</th><td><tt>0 <= offset < (buffer.length - length)</tt></td></tr>
     * <tr><th>length</th><td><tt>0 <= length < (buffer.length - offset)</tt></td></tr>
     * </table>
     *
     * @param buffer The buffer to write from.
     * @param offset The offset into <tt>buffer</tt> to start at.
     * @param length The length within <tt>buffer</tt> to use.
     * @throws IllegalArgumentException If any parameters are invalid.
     * @throws IOException If there are any I/O errors.
     * @since JSO-0.10
     */
    public int write(byte[] buffer, int offset, int length) 
        throws IllegalArgumentException, IOException{
        
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("JEP124StreamSource.write() : " + getBridge().isInitialised() + ":\n" + // NOI18N
                new String(buffer , offset , length));
        }
        if (!isConnected() || !getBridge().isInitialised()){
            throw new IOException("Not connected"); // NOI18N
        }

        validateInputBounds(buffer , offset , length);
        
        if (0 == length){
            return 0;
        }
        
        return getBridge().enqueueClientRequest(buffer , offset , length);
    }
    
    /**
     * <p>
     * Connects this <tt>StreamSource</tt> for input and output.
     * Until this method is called, <tt>{@link #read}</tt>  and <tt>{@link
     * #write}</tt> will throw <tt>IOException</tt>.</p>
     *
     * <p>
     * This method takes the <tt>Stream</tt> performing the method call,
     * allowing this <tt>StreamSource</tt> to initialize itself.</p>
     *
     * <p>
     * The value of <tt>s</tt> must not be <tt>null</tt>, or a
     * <tt>NullPointerException</tt> is thrown.</p>
     *
     * <p>
     * If there are any I/O errors in opening this <tt>StreamSource</tt>,
     * this method throws an <tt>IOException</tt>.  All other errors
     * result in some form of <tt>RuntimeException</tt> being thrown.</p>
     *
     * @param s The <tt>Stream</tt> to connect to.
     * @throws IOException If any I/O errors occur trying to open this
     * <tt>StreamSource</tt>.
     * @throws Exception If any other errors occur trying to open this
     * <tt>StreamSource</tt>.
     * @since   JSO-0.10
     */    
    public void connect(Stream s) throws IOException, Exception{
        
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("JEP124StreamSource.connect()"); // NOI18N
        }
        
        if (isConnected()){
            throw new IllegalStateException("Already connected"); // NOI18N
        }

        if (!getBridge().isInitialised()){
            getBridge().initialise();
            Thread th = new Thread(getBridge());
            th.start();
            setConnected(true);
        }
        else{
            throw new IllegalStateException("Already initialised"); // NOI18N
        }
    }
    
    protected void validateInputBounds(byte[] buffer , int offset , int length)
        throws IllegalArgumentException{

        if (null == buffer || offset < 0 || length < 0 || 
                offset >= buffer.length || 
                offset + length > buffer.length){
            throw new IllegalArgumentException("Invalid inputs : " + buffer +  // NOI18N
                    " , " + offset + " , " + length); // NOI18N
        }
    }

    // Part of the StreamSource interface definition.
    /**
     * Retrieves the 'expected' hostname for this connection. 
     * This value is the name of the 'remote' side of the connection, as a 
     * resolvable domain name or IP address. 
     * It is mostly provided as a fallback when a more suitable 'domain' 
     * cannot be provided.
     * The value returned by this method is never null or "".
     * @return
     * The 'remote' hostname.
     * @since 
     * JSO-0.2
     */
    public String getHostname(){
        // In our case , since we know the domain for sure , just return that !
        return getDomain();
    }
    
    public String getDomain(){
        return domain;
    }

    public void setDomain(String domain){
        this.domain = domain;
    }

    /*
     public Proxy getProxy(){
        return proxy;
    }

    public void setProxy(Proxy proxy){
        this.proxy = proxy;
    }
     */
    
    public HTTPSessionController getBridge(){
        return bridge;
    }

    protected void setBridge(HTTPSessionController bridge){
        this.bridge = bridge;
    }
    
   
    protected boolean isConnected(){
        return connected;
    }

    protected void setConnected(boolean connected){
        this.connected = connected;
    }
    
    // Move to a util class
    public static int getIntValue(Map params , String key , int def){
        int retval = def;
        String val = (String)params.get(key);
        
        if (null != val){
            try{
                val = val.trim();
                retval = Integer.parseInt(val);
            }catch(NumberFormatException nfEx){}
        }
        return retval;
    }
    
    // Move to a util class
    public static String getStringValue(Map params , String key , String def){
        String retval = def;
        String val = (String)params.get(key);
        return null == val ? def : val;
    }
}
