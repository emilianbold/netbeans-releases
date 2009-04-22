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

package org.netbeans.modules.mobility.jsr172.wizard;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.text.MessageFormat;

import java.util.List;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openide.util.NbBundle;


/** !PW FIXME This thread runs without a monitor thread ensuring a proper
 *      timeout and shutdown of the HttpConnection.  It should use the timeout
 *      feature of JDK 1.5.0 and the timeout global properties in JDK 1.4.2.
 *
 *      As is, it is probably possible for this thread to hang if it opens a
 *      connection to an HTTP server, sends a request, and that server never
 *      responds.
 *
 * @author Peter Williams
 */
public class WsdlRetriever implements Runnable {
    
    public static final int STATUS_START = 0;
    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_DOWNLOADING = 2;
    public static final int STATUS_COMPLETE = 3;
    public static final int STATUS_FAILED = 4;
    public static final int STATUS_TERMINATED = 5;
    public static final int STATUS_BAD_WSDL = 6;
    
    public static final String [] STATUS_MESSAGE = {
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Ready"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Connecting"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Downloading"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Complete"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Exception"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_Terminated"), // NOI18N
        NbBundle.getMessage(WsdlRetriever.class, "LBL_UnknownFileType") // NOI18N
    };
    
    // Thread plumbing
    private volatile boolean shutdown;
    private volatile int status;
    
    // Wsdl collection information
    private String wsdlUrlName;
    private byte [] wsdlContent;
    private String wsdlFileName;
    
    // Parent
    private MessageReceiver receiver;
    
    public WsdlRetriever(MessageReceiver r, String url) {
        this.shutdown = false;
        this.status = STATUS_START;
        this.wsdlUrlName = url;
        this.wsdlContent = null;
        this.wsdlFileName = null;
        this.receiver = r;
    }
    
    // Properties
    public byte [] getWsdl() {
        return wsdlContent;
    }
    
    public int getState() {
        return status;
    }
    
    public String getWsdlFileName() {
        return wsdlFileName;
    }
    
    public String getWsdlUrl() {
        return wsdlUrlName;
    }
    
    // not sure this is necessary -- for controller to signal shutdown in case
    // interrupted() doesn't work.
    public synchronized void stopRetrieval() {
        shutdown = true;
    }
    
    private URL wsdlUrl;
    private URLConnection connection;
    private InputStream in;
    
    public void run() {
        // Set name of thread for easier debugging in case of deadlocks, etc.
        //Thread.currentThread().setName("WsdlRetrieval"); // NOI18N
        
        wsdlUrl = null;
        connection = null;
        in = null;
        
        SSLSocketFactory orig = null;
        try {
            // !PW FIXME if we wanted to add an option to turn beautification of
            // the URL off (because our algorithm conflicted with what the user
            // need to enter), this is the method that such option would need to
            // disable.
            wsdlUrlName = beautifyUrlName(wsdlUrlName);
            
            if (wsdlUrlName.startsWith("https")) {//NOI18N
                NotifyDescriptor nd;
                DialogDisplayer.getDefault().notify(
                        nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(WsdlRetriever.class, "MSG_SecureURL"),
                        NotifyDescriptor.YES_NO_OPTION));
                orig = HttpsURLConnection.getDefaultSSLSocketFactory();
                if (nd.getValue() == NotifyDescriptor.YES_OPTION) {
                    // Create a trust manager that does not validate certificate chains
                    final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                            public void checkClientTrusted(
                                    @SuppressWarnings("unused") java.security.cert.X509Certificate[] certs, @SuppressWarnings("unused") String authType) {
                            }
                            public void checkServerTrusted(
                                    @SuppressWarnings("unused") java.security.cert.X509Certificate[] certs, @SuppressWarnings("unused") String authType) {
                            }
                        }
                    };
                    
                    // Install the all-trusting trust manager
                    try {
                        final SSLContext sc = SSLContext.getInstance("SSL");
                        sc.init(null, trustAllCerts, new java.security.SecureRandom());
                        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    } catch (Exception e) {
                    }
                } else {
                    setState(STATUS_FAILED, NbBundle.getMessage(WsdlRetriever.class, "MSG_NoHttpsAccess"), //NOI18N
                            new IOException("")); // NOI18N
                    return;
                }
            }
            
            wsdlUrl = new URL(wsdlUrlName);
            // Fix for IZ#162522 - IllegalArgumentException: protocol = http host = null
            if ( "".equals(wsdlUrl.getHost())){
                throw new MalformedURLException();
            }
            setState(STATUS_CONNECTING);
            connection = wsdlUrl.openConnection();
            in = connection.getInputStream();
            
            setState(STATUS_DOWNLOADING);
            
            // Download the wsdl file
            wsdlContent = downloadWsdlFileEncoded(new BufferedInputStream(in));
            
            // extract the (first) service name to use as a suggested filename.
            if(!shutdown) {
                final List<String> serviceNames = getServiceNames();
                if(serviceNames != null && serviceNames.size() > 0) {
                    wsdlFileName = wsdlUrl.getPath();
                    final int slashIndex = wsdlFileName.lastIndexOf('/');
                    if(slashIndex != -1) {
                        wsdlFileName = wsdlFileName.substring(slashIndex+1);
                    }
                    
                    if(wsdlFileName.length() == 0) {
                        wsdlFileName = serviceNames.get(0) + ".wsdl"; // NOI18N
                    } else if(wsdlFileName.length() < 5 || !".wsdl".equals(wsdlFileName.substring(wsdlFileName.length()-5))) { // NOI18N
                        wsdlFileName += ".wsdl"; // NOI18N
                    }
                    setState(STATUS_COMPLETE);
                } else {
                    // !PW FIXME bad wsdl file -- can we save and return the parser error message?
                    setState(STATUS_BAD_WSDL);
                }
            } else {
                setState(STATUS_TERMINATED);
            }
        } catch(ConnectException ex) {
            setState(STATUS_FAILED, NbBundle.getMessage(WsdlRetriever.class, "ERR_Connection"), ex); // NOI18N
            log(ex.getMessage());
        } catch(MalformedURLException ex) {
            setState(STATUS_FAILED, NbBundle.getMessage(WsdlRetriever.class, "ERR_BadUrl"), ex); // NOI18N
            log(ex.getMessage());
        } catch(IOException ex) {
            setState(STATUS_FAILED, NbBundle.getMessage(WsdlRetriever.class, "ERR_IOException"), ex); // NOI18N
            log(ex.getMessage());
        } finally {
            try {
                //put back original factory
                if (orig != null){
                    HttpsURLConnection.setDefaultSSLSocketFactory(orig);
                }
            } catch (Exception e) {
            }
            
            if(in != null) {
                try {
                    in.close();
                } catch(IOException ex) {
                }
            }
        }
    }
    
    /** Retrieve the wsdl file from the specified inputstream.  We don't know how big
     *  the file might be, and while many WSDL files are less than 30-40K, eBay's
     *  WSDL is over 1MB, so there is extra logic here to be very flexible on buffer
     *  space with minimal copying.
     *
     *  This routine could possibly be cleaned up a bit, but might lose in readability.
     *  For example, 'chunksize' is probably redundant and could be replaced by 'i',
     *  but the java optimizer will do that for us anyway and the usage is more clear
     *  this way.
     *
     */
    private byte [] downloadWsdlFileEncoded(final InputStream in) throws IOException {
        final java.util.ArrayList<Chunk> chunks = new java.util.ArrayList<Chunk>();
        final int BUF = 65536;
        boolean eof = false;
        byte [] data = new byte [0];
        
        while(!shutdown && !eof) {
            final byte [] b = new byte[BUF]; // New buffer for this block
            int i = 0; // index within this block we're writing at
            int l = 0; // number of bytes read during last call to read().
            int limit = b.length; // maximum number of bytes to read during call to read()
            int chunksize = 0; // number of bytes read into this block.  Should be always be BUF, except for last block of file.
            
            while(!shutdown && (l = in.read(b, i, limit)) != -1) {
                limit -= l;
                i += l;
                chunksize += l;
                
                if(limit == 0) {
                    break;
                }
            }
            
            // if we downloaded any data, add a chunk containing the data to our list of chunks.
            if(chunksize > 0) {
                chunks.add(new Chunk(b, chunksize));
            }
            
            eof = (l == -1);
        }
        
        if(!shutdown) {
            // calculate length for single byte array that contains the entire WSDL
            int bufLen = 0;
            for ( final Chunk c : chunks ) {
                bufLen += c.getLength();
            }
            
            // Now fill the single byte array with all the chunks we downloaded.
            data = new byte[bufLen];
            int index = 0;
            for ( final Chunk c : chunks ) {
                System.arraycopy(c.getData(), 0, data, index, c.getLength());
                index += c.getLength();
            }
        }
        
        return data;
    }
    
    
    private String beautifyUrlName(String urlName) {
        // 1. verify protocol, use http if not specified.
        if(urlName.indexOf("://") == -1 && urlName.indexOf("file:/") == -1) { // NOI18N
            urlName = "http://" + urlName; // NOI18N
        }
        
        // 2. if this looks like a service, add a ?WSDL argument.
        try {
            final URL testUrl = new URL(urlName);
            String testName = testUrl.getPath();
            final boolean hasArguments = (testUrl.getFile().indexOf('?') != -1);
            final int slashIndex = testName.lastIndexOf('/');
            if(slashIndex != -1) {
                testName = testName.substring(slashIndex+1);
            }
            final int dotIndex = testName.lastIndexOf('.');
            if(dotIndex != -1) {
                final String extension = testName.substring(dotIndex+1);
                if(!"xml".equals(extension) && !"wsdl".equals(extension) && !hasArguments) { // NOI18N
                    urlName += "?WSDL"; // NOI18N
                }
            } else if(!hasArguments) {
                // no file extension and no http arguments -- probably needs extension
                urlName = urlName + "?WSDL"; // NOI18N
            }
        } catch(MalformedURLException ex) {
            // do nothing about this here.  This error will occur again for real
            // in the caller and be handled there.
        }
        
        return urlName;
    }
    
    private void setState(final int newState) {
        status = newState;
        log(STATUS_MESSAGE[newState]);
        SwingUtilities.invokeLater(new MessageSender(receiver, STATUS_MESSAGE[newState]));
    }
            
    private void setState(final int newState, final String msg, final Exception ex) {
        status = newState;
        final Object [] args = new Object [] { msg, ex.getMessage()};
        final String message = MessageFormat.format(STATUS_MESSAGE[newState], args);
        log(message);
        SwingUtilities.invokeLater(new MessageSender(receiver, message));
    }
    
    private void log(@SuppressWarnings("unused")
	final String message) {
        // This method for debugging only.
//        System.out.println(message);
    }
    
    // private class used to cache a message and post to UI component on AWT Thread.
    private static class MessageSender implements Runnable {
        final private MessageReceiver receiver;
        final private String message;
        
        public MessageSender(MessageReceiver r, String m) {
            receiver = r;
            message = m;
        }
        
        public void run() {
            receiver.setWsdlDownloadMessage(message);
        }
    }
    
    public interface MessageReceiver {
        public void setWsdlDownloadMessage(String m);
    }
    
    /** Private method to sanity check the overall format of the WSDL file and
     *  determine the names of the one or more services defined therein.
     */
    private List<String> getServiceNames() {
        List<String> result = null;
        
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            final SAXParser saxParser = factory.newSAXParser();
            final ServiceNameParser handler= new ServiceNameParser();
            saxParser.parse(new InputSource(new ByteArrayInputStream(wsdlContent)), handler);
            result = handler.getServiceNameList();
        } catch(ParserConfigurationException ex) {
            // Bogus WSDL, return null.
        } catch(SAXException ex) {
            // Bogus WSDL, return null.
        } catch(IOException ex) {
            // Bogus WSDL, return null.
        }
        
        return result;
    }
    
    private static final class ServiceNameParser extends DefaultHandler {
        
        private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl"; // NOI18N
        private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N
        
        final private ArrayList<String> serviceNameList;
        
        ServiceNameParser() {
            serviceNameList = new ArrayList<String>();
        }
        
        @SuppressWarnings("unused")
		public void startElement(final String uri, final String localname, final String qname, final Attributes attributes) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("service".equals(localname)) { // NOI18N
                    serviceNameList.add(attributes.getValue("name")); // NOI18N
                }
            }
        }
        
        public List<String> getServiceNameList() {
            return serviceNameList;
        }
    }
    
    /** Data chunk of downloaded WSDL.
     */
    private static class Chunk {
        final private int length;
        final private byte [] data;
        
        public Chunk(byte [] d, int l) {
            data = d;
            length = l;
        }
        
        public byte [] getData() {
            return data;
        }
        
        public int getLength() {
            return length;
        }
    }
    
}
