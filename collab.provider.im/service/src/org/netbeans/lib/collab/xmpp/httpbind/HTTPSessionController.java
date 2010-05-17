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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
//import java.net.Proxy;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.jabberstudio.jso.JSOImplementation;
import org.jabberstudio.jso.StreamError;
import org.jabberstudio.jso.util.Utilities;
import org.netbeans.lib.collab.CollaborationException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mridul Muralidharan
 */
public class HTTPSessionController implements Runnable , GatewayResponseListener ,
    HTTPBindConstants , NegotiationConstants{
    
    public static final int ABORT_THREADS = 1;

    private URL gatewayURL = null;
    //private Proxy proxy;
    
    private boolean abortThread = false;
    private int waitTime = -1;
    private String sid = null;
    private int maxRequests = -1;
    private String contentType = null;
    private String xmlLang = null;
    private int pollingInterval = 1;
    private int inactivityTimeout;

    private Map connParams = null;
    private Map negotiatedParams = null;

    // The JEP allows for multiple concurrent requests to the gateway
    // though this is limited to a upper limit - these lists hold the available
    // and complete instances of these dispatchers.
    private List availableRequestDispatcher = new LinkedList();
    private boolean availableRequestDispatcherWoken = false;
    private List completeRequestDispatchers = new LinkedList();
    
    private String authID;
    private long rid;
    private boolean initialised = false;
    private boolean initialising = false;
    
    private List outMessages = new LinkedList();
    private Object lockObj = new Object();
    
    // Since pipe's have a circular buffer restriction of PIPE_SIZE (1K) bytes
    // there is a possibility of deadlock.
    // So moving to a custom implementation.
    private QueuedStream serverResponses;
    private boolean pipesClosed = false;

    private List inputDataListeners = new LinkedList();
    
    private String domain = ""; // NOI18N
    private String routeServer = null;
    
    private String features = ""; // NOI18N
    
    
    private boolean terminateStreamInserted = false;
    
    public HTTPSessionController() {
        if (isDebugOn()){
            debug("XMPPToHTTPBridge"); // NOI18N
        }
    }
    
    public void initialise() throws IOException, CollaborationException {

        // First request is 'special'
        // Get the rid , etc.
        synchronized(lockObj){
            if (isInitialised()){
                throw new IllegalStateException
                        ("initialise() called after bridge is already initialised"); // NOI18N
            }
            if (isInitialising()){
                return ;
            }
            setInitialising(true);
            closePipes();
            serverResponses = new QueuedStream();
        }
        
        if (isDebugOn()){
            debug("initialise()"); // NOI18N
        }
        pipesClosed = false;
        executeFirstRequest();
        setInitialised(true);
        setInitialising(false);
    }
    
    public void run(){
        
        if (isDebugOn()){
            debug("run()"); // NOI18N
        }

        assert (isInitialised());
        boolean done = false;
        
        int minReqPingWorkers = (completeRequestDispatchers.size() + 1) / 2;
        int min = Math.min(getWaitTime() , getInactivityTimeout());
        
        // Usual cases , this will be waittime - (inactivity - waittime) / 4
        int maxUpdateDelay = min - 
                Math.min(min , Math.abs(getWaitTime() - getInactivityTimeout())) / 4;
        
        long lastUpdate = System.currentTimeMillis();

        while (!done){
            /*
             * Sleep for a while then poll server
             * While polling , if there are messages in  toServerQueue , send them too
             * If there are responses from the server , add them to the client queue
             */
            RequestDispatcher disp = null;
            synchronized (availableRequestDispatcher){
                try{
                    //Thread.currentThread().sleep(getPollingInterval());
                    availableRequestDispatcher.wait(getPollingInterval());
                    availableRequestDispatcherWoken = false;
                }catch(InterruptedException iEx){}

                // Even if we get an abort , wait until we have written off 
                // all the pending messages to the server.
                if (isAbortThread() && 0 == outMessages.size()){
                    break;
                }

                if (0 != outMessages.size()){
                    if (isDebugOn()){
                        debug("run() : data_size : " + outMessages.size() +  // NOI18N
                                " , availableRequestDispatcher.size()  : " + // NOI18N
                                availableRequestDispatcher.size() + " [" + // NOI18N
                                getMaxRequests() + "]"); // NOI18N
                    }
                }
                if (0 == outMessages.size() && 
                        getMaxRequests() != availableRequestDispatcher.size() &&
                        // We could have whole bunch of retransission requests such that the
                        // actual waittime - idletime relationship could go
                        // out of wack ... the following condition also makes sure that
                        // we ping gateway even when the previous requests (gateway response)
                        // were 'huge' requiring sizable packet size (and so transmission delay).
                        (lastUpdate + maxUpdateDelay >= System.currentTimeMillis() ||
                            availableRequestDispatcher.size() < minReqPingWorkers)){
                    // There are already requests pending at the server
                    // and there are no requests from the client.
                    // So dont bother polling the server.
                    continue;
                }
                
                disp = getAvailableRequestDispatcher();
                if (null == disp){
                    if (0 == completeRequestDispatchers.size()){
                        assert (isAbortThread());
                        break;
                    }
                    continue;
                }
            }
            
            // Check if this has been deregistered.
            synchronized(disp.getLockObj()){
                if (disp.isDeregistered()){
                    // *sigh*
                    
                    // Are there any valid ones left ?
                    // If no , then exit ...
                    if (0 == completeRequestDispatchers.size()){
                        assert (isAbortThread());
                        break;
                    }
                    continue;
                }
                disp.setDispatchInterest(true);
            }

            long thisRid = incrRid();
            
            lastUpdate = System.currentTimeMillis();
            
            boolean sent = disp.dispatch(constructRequest(thisRid) , thisRid);
            assert (sent);
        }
        closeRequestDispatcherThreads();
    }
    
    protected void enqueueServerResponse(byte[] data){
        if (isDebugOn()){
            debug("enqueueServerResponse() :\n" + new String(data)); // NOI18N
        }
        try{
            serverResponses.write(data);
        }catch(IllegalArgumentException iaEx){
            // Should not happen - log it ?
        }catch(IOException ioEx){
            // Should not happen - log it ?
        }
    }
            
    public int readServerResponse(byte[] buffer, int offset, int length)
        throws IllegalArgumentException , IOException{
        return serverResponses.read(buffer , offset , length);
    }

    protected void enqueueServerResponse(String data){
        try{
            enqueueServerResponse(data.getBytes(UTF_8));
        }catch(UnsupportedEncodingException useEx){
            enqueueServerResponse(data.getBytes());
        }
    }

    protected byte[] dequeueClientRequest(){
        if (isDebugOn()){
            debug("dequeueClientRequest"); // NOI18N
        }
        synchronized(outMessages){
            if (outMessages.isEmpty()){
                return null;
            }
            return (byte[]) outMessages.remove(0);
        }
    }
    
    private int outMessagesBufferedSize = 0;
    private int outMessagesBufferedPackets = 0;
    
    private int maxBufferedSize = 0;
    private int maxBufferedPackets = 0;
    
    protected int enqueueClientRequest(byte[] data , int offset , int length) 
    throws IOException{
        
        if (isDebugOn()){
            debug("enqueueClientRequest"); // NOI18N
        }
        if (pipesClosed){
            throw new IOException("Not connected"); // NOI18N
        }
        if (0 == length){
            return 0;
        }
        int retval = 0;
        synchronized(outMessages){
            /*
             * We enqueue a request if :
             * 1) No requests has been enqueued till now.
             * OR all of the folloing true
             * 2.a) maxBufferedSize is 0 or after addition of this request , 
             *      the enqueued size is less than or equal to maxBufferedSize
             * 2.b) maxBufferedPackets is 0 or after addition of this request , 
             *      the number of enqueued requests is less than or equal to maxBufferedPackets
             */
            if (0 == outMessagesBufferedSize ||
                    (
                        (0 == maxBufferedSize || 
                            outMessagesBufferedSize + length <= maxBufferedSize) &&
                        (0 == maxBufferedPackets || 
                            outMessagesBufferedPackets < maxBufferedPackets)
                    )
                ){
                
                byte[] array = new byte[length];
                System.arraycopy(data , offset , array , 0 , length);
                outMessages.add(array);
                retval = length;
                outMessagesBufferedSize += length;
                outMessagesBufferedPackets ++;
            }
            else{
                if (isDebugOn()){
                    debug("Did not enqueue : outMessagesBufferedSize  == " + outMessagesBufferedSize +
                            " , maxBufferedSize == " + maxBufferedSize + 
                            " , outMessagesBufferedPackets == " + outMessagesBufferedPackets + 
                            " , maxBufferedPackets == " + maxBufferedPackets); 
                }
            }
        }
        
        if (!availableRequestDispatcherWoken){
            synchronized (availableRequestDispatcher){
                if (availableRequestDispatcher.size() > 0) availableRequestDispatcher.notify();
                availableRequestDispatcherWoken = true;
            }
        }
        return retval;
    }
    
    private void updateMessagesStat(int removedBytes , int removedPackets){
        synchronized(outMessages){
            if (isDebugOn()){
                debug("updateMessagesStat : outMessagesBufferedSize  == " + outMessagesBufferedSize +
                        " , removedBytes == " + removedBytes + 
                        " , outMessagesBufferedPackets == " + outMessagesBufferedPackets + 
                        " , removedPackets == " + removedPackets); 
            }
            if (removedBytes > outMessagesBufferedSize || 
                    removedPackets > outMessagesBufferedPackets){
                if (isDebugOn()){
                    debug("\n\nError with updateMessageStat !! : outMessagesBufferedSize : " + outMessagesBufferedSize +
                            " , outMessagesBufferedPackets : " + outMessagesBufferedPackets + "\n\n");
                }
                outMessagesBufferedSize = 0;
                outMessagesBufferedPackets = 0;
            }
            else{
                outMessagesBufferedSize -= removedBytes;
                outMessagesBufferedPackets -= removedPackets;
            }
        }
    }

    protected void reinsertClientRequest(byte[] data){
        if (isDebugOn()){
            debug("reinsertClientRequest"); // NOI18N
        }
        synchronized(outMessages){
            outMessages.add(0 , data);
        }
    }

    // TODO : optimise this method - does a byte[] -> String conversion
    // for parsing , etc - can we improve this ?
    protected byte[] constructRequest(long thisRid){
        
        // Generate the xml to be sent to the server
        if (isDebugOn()){
            debug("constructRequest"); // NOI18N
        }
        StringBuffer header = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        boolean done = false;
        int stanzasProcessed = 0;
        int bytesProcessed = 0;
        
        addJEP124Header(header , getSID() , thisRid);

        try{
            while (!done){
                byte[] data = dequeueClientRequest();

                if (null == data){
                    break;
                }

                String request;
                try{
                    request = new String(data , UTF_8);
                }catch(UnsupportedEncodingException useEx){
                    request = new String(data);
                }
                String firstStr = request.trim();

                if (firstStr.length() > 0){
                    firstStr = firstStr.substring(1);
                }

                if (firstStr.trim().startsWith("stream")){ // NOI18N
                    // The first stream start request from client
                    String strmResp = constructInitialStreamResponse();
                    enqueueServerResponse(strmResp);
                    fireDataArrivedEvent();
                }
                else if (firstStr.trim().startsWith("/stream")){ // NOI18N
                    if (0 == stanzasProcessed){
                        // Stream close request from client
                        header.setLength(0);
                        header.append(generateCloseSessionRequest(getSID() , thisRid));
                        sb.append(generateUnavailablePresence());
                    }
                    else{
                        // Service no more requests.
                        // The next poll will service this /stream request and 
                        // terminate this connection.
                        reinsertClientRequest(data);
                    }
                    break;
                }
                else{
                    sb.append(request);
                }

                bytesProcessed += data.length;
                stanzasProcessed ++;
            }
        }finally{
            updateMessagesStat(bytesProcessed , stanzasProcessed);
        }
        
        StringBuffer request = new StringBuffer();
        request.append(header);
        request.append(sb);
        addJEP124Footer(request);

        if (isDebugOn()){
            debug("constructRequest REQUEST : \n" + request.toString()); // NOI18N
        }
        try{
            return request.toString().getBytes(UTF_8);
        }catch(UnsupportedEncodingException useEx){
            // ??!!
            return request.toString().getBytes();
        }
    }
    
    public String generateCloseSessionRequest(String thissid , long thisrid){
        if (isDebugOn()){
            debug("generateCloseSessionRequest"); // NOI18N
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<body rid='") // NOI18N
        .append(thisrid)
        .append("' sid='") // NOI18N
        .append(thissid)
        .append("' type='terminate' xmlns='http://jabber.org/protocol/httpbind'>"); // NOI18N
        return sb.toString();
    }

    // Maybe we can inline this instead of having it as a method ?
    public String generateUnavailablePresence(){
        return "<presence type='unavailable' xmlns='jabber:client'/>"; // NOI18N
    }
    
    public String constructInitialStreamResponse(){
        if (isDebugOn()){
            debug("constructInitialStreamResponse"); // NOI18N
        }
        
        StringBuffer sb = new StringBuffer();
        
        sb
        .append("<stream:stream xmlns:stream='http://etherx.jabber.org/streams'") // NOI18N
        .append(" xmlns='jabber:client' ") // NOI18N
        .append("from='") // NOI18N
        .append(getDomain())
        .append("' id='") // NOI18N
        .append(getAuthID())
        .append("'  version='1.0' >") // NOI18N
        .append(getFeatures());
        
        return sb.toString();
    }

    public String constructFinalStreamResponse(){
        if (isDebugOn()){
            debug("constructFinalStreamResponse"); // NOI18N
        }

        return "</stream:stream>"; // NOI18N
    }
    
    public void addJEP124Header(StringBuffer sb , String thissid , long thisrid){
        sb.append("<body rid='") // NOI18N
        .append(thisrid)
        .append("' sid='") // NOI18N
        .append(thissid)
        .append("' xmlns='http://jabber.org/protocol/httpbind'>"); // NOI18N
        return ;
    }
    
    public void addJEP124Footer(StringBuffer sb){
        sb.append("</body>"); // NOI18N
    }
    
    public void instanceAvailable(RequestDispatcher disp){
        if (isDebugOn()){
            debug("instanceAvailable"); // NOI18N
        }
        addAvailableRequestDispatcher(disp);
    }
    
    // Inefficient ?
    public void processGatewayResponse(int flag, Object payload){
        
        if (flag == CONNECTION_LOST){
            try{
                disconnect();

            }catch(Exception ioEx){
                // ignore for now - TODO : log it ?
            }
            return ;
        }
        
        if (DISPATCHER_EXIT == flag){
            // This would always be called while 
            RequestDispatcher disp = (RequestDispatcher)payload;
            removeCompleteRequestDispatchers(disp);
            synchronized (availableRequestDispatcher){
                removeAvailableRequestDispatcher(disp);
            }
            return ;
        }
        
        // We do not handle anything else here as of now ...
        assert (DATA_ARRIVED == flag);
        
        byte[] data = (byte[])payload;
        String response;

        try{
            response = new String(data , UTF_8);
        }catch(UnsupportedEncodingException useEx){
            response = new String(data);
        }
        
        if (isDebugOn()){
            debug("processGatewayResponse : \n" + response); // NOI18N
        }
        
        org.w3c.dom.Element element = XMLParserHelper.parseXMLInput(response);
        
        if (null == element){
            // What the hell ?
            return ;
        }
        
        assert element.getNodeName().equals(BODY_REQ_PARAM) : "Unknown node specified : " + element; // NOI18N
        
        String type = element.getAttribute(TYPE_REQ_PARAM); // NOI18N

        //boolean appendSlashError = false;
        boolean closeConnection = false;
        boolean errorOrTerminate = false;

        if (null != type && 
                (type.equals(TERMINATE_TYPE) || type.equals(ERROR_TYPE))){ // NOI18N
            closeConnection = true;
            errorOrTerminate = true;
	    /*
            String condition = element.getAttribute(ERROR_CONDITION); // NOI18N
            // TODO :: Handle more cases here as required.
            if (condition.equals(REMOTE_STREAM_ERROR_ERRORTYPE)){ // NOI18N
                enqueueServerResponse("<stream:error>"); // NOI18N
                appendSlashError = true;
            }
            else{
                forceCloseConnection();
                return;
            }
	    */
        }

        NodeList nodelist = element.getChildNodes();
        
        int length = nodelist.getLength();
        int count = 0;
        
        while (count < length){
            Node node = nodelist.item(count);
            String nodeValue = XMLParserHelper.nodeToString(node);
            if (null != nodeValue){
                enqueueServerResponse(nodeValue);
            }
            count ++;
        }
        
	/*
        if (appendSlashError){
            if (count <= 0){
                count ++;
            }
            enqueueServerResponse("</stream:error>"); // NOI18N
        }
	*/
        if (errorOrTerminate){
            String condition = element.getAttribute(ERROR_CONDITION);
            if (null != condition){
                try{
                    StreamError error = JSOImplementation.getInstance().createStream(Utilities.CLIENT_NAMESPACE).getDataFactory().createStreamError(condition);
                    enqueueServerResponse(error.toString());
                }catch(Exception ex){}
            }
        }
        
        if (closeConnection){
            forceCloseConnection();
            return;
        }
        else{
            if (0 != length){
                fireDataArrivedEvent();
            }
            // Could be a empty body tag !
            // Which is to be interpreted as '</stream>'
            else{
                org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
                if (null == attributes || 0 == attributes.getLength()){
                    forceCloseConnection();
                    return;
                }
            }
        }
        return ;
    }
    
    protected void forceCloseConnection(){
        boolean insert = false;
        synchronized(this){
            if (!terminateStreamInserted){
                terminateStreamInserted = true;
                insert = true;
            }
        }
        if (insert){
            enqueueServerResponse("</stream:stream>"); // NOI18N
        }
        fireDataArrivedEvent();
        try{
            disconnect();
        }catch(Exception ioEx){
            // ignore for now - TODO : log it ?
        }
        return ;
    }
    
    private int getParamInt(String param , int def){
        int retval = def;
        if (null != connParams){
            String val = (String)connParams.get(param);
            if (null != val){
                try{
                    retval = Integer.parseInt(val.trim());
                }catch(Exception ex){}
            }
        }
        return retval;
    }

    protected void executeFirstRequest() throws IOException , MalformedURLException ,
    CollaborationException {

        if (isDebugOn()){
            debug("executeFirstRequest"); // NOI18N
        }
        
        String generatedXml = generateFirstRequestXML();
        
        // Set the appropriate values from connParams
        maxBufferedSize = getParamInt(MAX_BUFFERED_BYTES , 0);
        maxBufferedPackets = getParamInt(MAX_BUFFERED_PACKETS , 0);
        
        RequestDispatcher disp = new RequestDispatcher(connParams);
        disp.setGatewayURL(getGatewayURL());
        disp.setSID(null);
        //disp.setProxy(getProxy());
        
        byte[] data = null;
        
        try{
            data = generatedXml.getBytes(UTF_8);
        }catch(UnsupportedEncodingException useEx){
            // ??!!
            data = generatedXml.getBytes();
        }
        
        byte[] dataResponse = disp.sendAndWait(data , "" + getRid()); // NOI18N
        
        if (null == dataResponse){
            if (isDebugOn()){
                debug("dataResponse == null"); // NOI18N
            }
            throw new IOException("Error connecting to gateway"); // NOI18N
        }

        String resp;
        try{
            resp = new String(dataResponse , UTF_8);
        }catch(UnsupportedEncodingException useEx){
            resp = new String(dataResponse);
        }

        if (isDebugOn()){
            debug("First request response : \n" + resp); // NOI18N
        }
        
        if (0 == resp.trim().length()){
            throw new IOException("Error connecting to gateway"); // NOI18N
        }
        
        org.w3c.dom.Element doc = XMLParserHelper.parseXMLInput(resp);
        
        String sid = XMLParserHelper.getStringAttribute(doc , SID_REQ_PARAM);
        setSID(sid); // NOI18N
        getNegotiatedParameters().put(SID_REQ_PARAM , sid);
        
        int wait = XMLParserHelper.getIntAttribute(doc , WAIT_REQ_PARAM);
        setWaitTime(wait * 1000); // NOI18N
        getNegotiatedParameters().put(WAIT_REQ_PARAM , "" + wait);

        String authid = XMLParserHelper.getStringAttribute(doc , AUTHID_REQ_PARAM);
        setAuthID(authid); // NOI18N
        getNegotiatedParameters().put(AUTHID_REQ_PARAM , authid);

        int maxreq = XMLParserHelper.getIntAttribute(doc , REQUESTS_REQ_PARAM);
        setMaxRequests(maxreq + 1); // NOI18N
        getNegotiatedParameters().put(REQUESTS_REQ_PARAM , "" + maxreq);

        int pollinterval = XMLParserHelper.getIntAttribute(doc , POLLING_REQ_PARAM);
        setPollingInterval(pollinterval * 1000); // NOI18N
        getNegotiatedParameters().put(POLLING_REQ_PARAM , "" + pollinterval);

        int inactivity = XMLParserHelper.getIntAttribute(doc , INACTIVITY_REQ_PARAM);
        setInactivityTimeout(inactivity * 1000);  // NOI18N
        getNegotiatedParameters().put(INACTIVITY_REQ_PARAM , "" + inactivity);
        
        // We will be already adding the used dispatcher , so how many more to add.

        int value = getMaxRequests();
        
        // Get the features.
        NodeList children = doc.getChildNodes();
        int length = children.getLength();
        int count = 0;
        
        try{
            while (count < length){
                Node node = children.item(count);
                String nodeValue = XMLParserHelper.nodeToString(node);
                appendToFeatures(nodeValue);
                count ++;
            }
        }catch(Exception ex){
            debug(ex.toString() , ex);
        }
        
        disp.setSID(getSID());
        disp.addGatewayResponseListener(this);
        disp.setConnectionTimeout(getWaitTime());
        disp.init();
        addCompleteRequestDispatchers(disp);
        value --;

        while (value > 0){
            disp = new RequestDispatcher(connParams);
            disp.setSID(getSID());
            disp.setGatewayURL(getGatewayURL());
            disp.addGatewayResponseListener(this);
            //disp.setProxy(getProxy());
            disp.init();
            addCompleteRequestDispatchers(disp);
            value --;
        }
        return ;
    }
    
    protected String generateFirstRequestXML(){
        StringBuffer sb = new StringBuffer();
        
        Random rand = new Random();
        
        // For backward compatibility with IFR release , we restrict the generated rid to
        // 30 bits.
        //long val = rand.nextLong();
        int val = rand.nextInt(30);
        String routeServer = getRouteServer();
        
        if (null == getContentType()){
            throw new NullPointerException("Invalid content type"); // NOI18N
        }
        if (-1 == getMaxRequests()){
            throw new NullPointerException("Invalid max requests"); // NOI18N
        }
        if (null == getDomain()){
            throw new NullPointerException("Invalid domain"); // NOI18N
        }
        if (-1 == getWaitTime()){
            throw new NullPointerException("Invalid wait time"); // NOI18N
        }
        
        if (null == getXmlLang()){
            throw new NullPointerException("Invalid xml:lang"); // NOI18N
        }
        
        setRid((long)val);
        
        sb.append("<body content='") // NOI18N
        .append(getContentType())
        .append("' hold='") // NOI18N
        .append(getMaxRequests())
        .append("' rid='") // NOI18N
        .append(getRid())
        .append("' to='") // NOI18N
        .append(getDomain());
        
        if (null != routeServer && 0 != routeServer.trim().length()){
            sb.append("' route='") // NOI18N
            .append(routeServer);
        }
        
        sb.append("' wait='") // NOI18N
        .append(getWaitTime() / 1000)
        .append("' xml:lang='") // NOI18N
        .append(getXmlLang())
        .append("' xmlns='http://jabber.org/protocol/httpbind'/>"); // NOI18N
        if (isDebugOn()){
            debug("generateFirstRequestXML : \n" + sb.toString()); // NOI18N
        }
        return sb.toString();
    }
    
    public void raiseEvent(int event){
        if (isDebugOn()){
            debug("raiseEvent : " + event); // NOI18N
        }
        if (ABORT_THREADS == event){
            setAbortThread(true);
        }
        else{
            if (isDebugOn()){
                debug("Unknown event in raiseEvent : " + event); // NOI18N
            }
        }
    }
    
    public void disconnect() throws IOException, Exception{
        raiseEvent(ABORT_THREADS);
    }
    
    public void addDataArrivedEventHandler(DataArrivedEventHandler handler){
        if (isDebugOn()){
            debug("addDataArrivedEventHandler"); // NOI18N
        }
        synchronized(inputDataListeners){
            if (!inputDataListeners.contains(handler)){
                inputDataListeners.add(handler);
            }
        }
    }

    protected void fireDataArrivedEvent(){
        if (isDebugOn()){
            debug("fireDataArrivedEvent"); // NOI18N
        }
        synchronized(inputDataListeners){
            Iterator iter = inputDataListeners.iterator();
            
            while (iter.hasNext()){
                DataArrivedEventHandler handler = (DataArrivedEventHandler)iter.next();
                handler.dataArrivedNotification();
            }
        }
    }

    // To be called within a synchronized block which holds a lock to
    // availableRequestDispatcher
    protected RequestDispatcher getAvailableRequestDispatcher(){
        synchronized (availableRequestDispatcher){
            return availableRequestDispatcher.size() > 0 ? 
                (RequestDispatcher)availableRequestDispatcher.remove(0) :
                null;
        }
    }
    
    // To be called within a synchronized block which holds a lock to
    // availableRequestDispatcher
    protected void removeAvailableRequestDispatcher(RequestDispatcher disp){
        synchronized (availableRequestDispatcher){
            availableRequestDispatcher.remove(disp);
        }
    }

    protected void addAvailableRequestDispatcher(RequestDispatcher disp){
        boolean added = false;
        boolean notified = false;
        synchronized (availableRequestDispatcher){
            if (!availableRequestDispatcher.contains(disp)){
                added = true;
                availableRequestDispatcher.add(disp);
                if (availableRequestDispatcher.size() == getMaxRequests()){
                    availableRequestDispatcher.notify();
                    notified = true;
                }
            }
        }
        if (isDebugOn()){
            debug("addAvailableRequestDispatcher : added " + added + 
                    ", notified : " + notified); // NOI18N
        }
    }
    
    protected void addCompleteRequestDispatchers(RequestDispatcher disp){
        synchronized(completeRequestDispatchers){
            if (!completeRequestDispatchers.contains(disp)){
                completeRequestDispatchers.add(disp);
            }
        }
    }
    
    protected void removeCompleteRequestDispatchers(RequestDispatcher disp){
        synchronized(completeRequestDispatchers){
            completeRequestDispatchers.remove(disp);
        }
    }

    protected void closeRequestDispatcherThreads(){
        if (isDebugOn()){
            debug("closeRequestDispatcherThreads"); // NOI18N
        }
        
        synchronized(completeRequestDispatchers){
            Iterator iter = completeRequestDispatchers.iterator();

            while (iter.hasNext()){
                RequestDispatcher disp = (RequestDispatcher)iter.next();
                disp.abortThread();
            }
        }
        closePipes();
        if (isDebugOn()){
            debug("closeRequestDispatcherThreads DONE"); // NOI18N
        }
    }
    
    protected void closePipes(){
        if (isDebugOn()){
            debug("closePipes"); // NOI18N
        }
        try{
            if (null != serverResponses &&
                    !serverResponses.isClosed()){
                boolean insert = false;
                synchronized(this){
                    if (!terminateStreamInserted){
                        terminateStreamInserted = true;
                        insert = true;
                    }
                }
                if (insert){
                    enqueueServerResponse("</stream:stream>"); // NOI18N
                }
                serverResponses.close();
                pipesClosed = true;
                fireDataArrivedEvent();
            }
        }catch(IOException ioEx){}
        //serverResponses = null;
    }
    
    protected void setAbortThread(boolean abortThread){
        this.abortThread = abortThread;
    }

    protected boolean isAbortThread(){
        return abortThread;
    }

    public synchronized long getRid(){
        return rid;
    }
    
    public synchronized void setRid(long rid){
        this.rid = rid;
    }
    
    protected synchronized long incrRid(){
        rid ++;
        if (isDebugOn()){
            debug("incrRid : "  + rid); // NOI18N
        }
        return rid;
    }
    
    public int getWaitTime(){
        return waitTime;
    }
    
    public void setWaitTime(int waitTime){
        this.waitTime = waitTime;
    }

    public URL getGatewayURL(){
        return gatewayURL;
    }
    
    protected void setGatewayURL(URL gatewayURL){
        this.gatewayURL = gatewayURL;
    }

    protected String getSID(){
        return sid;
    }
    
    protected void setSID(String sid){
        this.sid = sid;
    }
    
    protected void setAuthID(String authID){
        this.authID = authID;
    }

    public String getAuthID(){
        return authID;
    }
    
    public void appendToFeatures(String str){
        features += str;
    }
    
    public String getFeatures(){
        return features;
    }
    
    public String getDomain(){
        return domain;
    }
    
    public void setDomain(String domain){
        this.domain = domain;
    }
    
    public String getRouteServer(){
        return routeServer;
    }
    
    public void setRouteServer(String routeServer){
        this.routeServer = routeServer;
    }
    
    public void setMaxRequests(int maxRequests){
        this.maxRequests = maxRequests;
    }
    
    public int getMaxRequests(){
        return maxRequests;
    }
    
    /*
    public Proxy getProxy(){
        return proxy;
    }
    
    public void setProxy(Proxy proxy){
        this.proxy = proxy;
    }
     */
    
    public boolean isInitialised(){
        return initialised;
    }
    
    public void setInitialised(boolean initialised){
        this.initialised = initialised;
    }
    
    public boolean isInitialising(){
        return initialising;
    }
    
    public void setInitialising(boolean initialising){
        this.initialising = initialising;
    }
    
    public String getContentType(){
        return contentType;
    }

    public void setContentType(String contentType){
        this.contentType = contentType;
    }

    public String getXmlLang(){
        return xmlLang;
    }

    public void setXmlLang(String xmlLang){
        this.xmlLang = xmlLang;
    }
    
    public void setPollingInterval(int pollingInterval){
        // min of 0.5 seconds hardcoded ... someway to specify/change this ?
        this.pollingInterval = Math.max(pollingInterval , 500);
    }

    public int getPollingInterval(){
        return pollingInterval;
    }
    
    public int getInactivityTimeout(){
        return inactivityTimeout;
    }
    
    public void setInactivityTimeout(int inactivityTimeout){
        this.inactivityTimeout = inactivityTimeout;
    }
    
    public void setConnectionParameters(Map params){
        this.connParams = params;
    }

    public void setNegotiatedParameters(Map negotiatedParams){
        this.negotiatedParams = negotiatedParams;
    }

    public Map getNegotiatedParameters(){
        return negotiatedParams;
    }
    
    private static PrintStream fileStrm = null;
    // Change this to false as soon as debugging is over and kick out
    // all the corresponding HTTPSessionController.debug() code !
    // Where we really require debugging (should be minimal within the
    // httpbind package if ever requierd) introduce the logger code from 
    // XMPPSession.
    public static boolean debugOn = System.getProperty("org.netbeans.lib.collab.xmpp.httpbind.HTTPSessionController.debug" , "false").equalsIgnoreCase("true");
    //public static boolean debugOn = false;
    
    public static boolean isDebugOn(){
        return debugOn;
    }
    
    static{
        if (debugOn){
            try{
                File file = new File("/tmp/client.out"); // NOI18N
                fileStrm = new PrintStream(new FileOutputStream(file));
            }catch(Exception ex){}
            debugOn = true;
            if (null == fileStrm){
                debugOn = false;
            }
        }
    }

    public static void debug(String str){
        if (isDebugOn()){
            assert (null != fileStrm);
            fileStrm.print(Thread.currentThread().getName() + " : ");
            fileStrm.println(str);
            fileStrm.flush();
        }
    }
    public static void debug(String str , Throwable th){
        if (isDebugOn()){
            assert (null != fileStrm);
            fileStrm.print(Thread.currentThread().getName() + " : ");
            fileStrm.println(str);
            th.printStackTrace(fileStrm);
            fileStrm.flush();
        }
    }
}
