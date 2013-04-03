/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.platforms.ios;

import com.dd.plist.Base64;
import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.BinaryPropertyListWriter;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Properties;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.modules.cordova.platforms.MobileDebugTransport;
import org.netbeans.modules.netserver.api.WebSocketClient;
import org.netbeans.modules.netserver.api.WebSocketReadHandler;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jan Becicka
 */
public class IOSDebugTransport extends MobileDebugTransport implements TransportImplementation {

    private Socket socket;
    private ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private final static String LOCALHOST_IPV6 = "::1";
    private final static int port = 27753;
    private RequestProcessor.Task socketListener;
    private volatile boolean keepGoing = true;
    private static final String SET_CONNECTION_KEY = getCommand("setConnectionKey");
    private static final String CONNECT_TO_APP = getCommand("connectToApp");
    private static final String SET_SENDER_KEY = getCommand("setSenderKey");
    private static final String SEND_JSON_COMMAND = getCommand("sendJSONCommand");
    private ResponseCallback callBack;
    private static XPathExpression JSON_DATA_PATH;

    public IOSDebugTransport() {
        try {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            JSON_DATA_PATH = xpath.compile("/plist/dict/dict/data");
        } catch (XPathExpressionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public boolean attach() {
        try {
            init();

            socketListener = RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    while (keepGoing) {
                        try {
                            Thread.sleep(200);
                            process();
                        } catch (Exception exception) {
                            Exceptions.printStackTrace(exception);
                        }
                    }
                }
            });

            return true;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    public static void runWhenReady(Runnable run, long timeout) {
        long time;
        long started = System.currentTimeMillis();
        do {
            try {
                Socket socket = new Socket(LOCALHOST_IPV6, port);
                run.run();
            } catch (UnknownHostException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            time = System.currentTimeMillis() - started;
        } while (time < timeout);
        
    }
    @Override
    public boolean detach() {
        stop();
        return true;
    }

    @Override
    public void sendCommand(Command command) {
        try {
            sendCommand(command.getCommand());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void registerResponseCallback(ResponseCallback callback) {
        this.callBack = callback;
    }

    private static String getCommand(String name) {
        try {
            Properties props = new Properties();
            props.load(IOSDebugTransport.class.getResourceAsStream("Command.properties"));
            return props.getProperty(name).replace("$bundleId", "com.apple.mobilesafari");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String createJSONCommand(JSONObject command) throws IOException {
        String json = command.toString();
        String s = Base64.encodeBytes(json.getBytes());
        String res = SEND_JSON_COMMAND.replace("$json_encoded", s);
        //System.out.println("sending " + res);
        return res;
    }

    public byte[] plistXmlToBinary(String msg) throws Exception {
        NSObject object = XMLPropertyListParser.parse(msg.getBytes());
        return BinaryPropertyListWriter.writeToArray(object);

    }

    public String plistBinaryToXml(byte[] binary) throws Exception {
        try {
            NSObject object = BinaryPropertyListParser.parse(binary);
            return object.toXMLPropertyList();
        } catch (Exception e) {
            return null;
        }
    }

    public void init() throws Exception {
        if (socket != null && (socket.isConnected() || !socket.isClosed())) {
            socket.close();
        }
        socket = new Socket(LOCALHOST_IPV6, port);
        sendCommand(SET_CONNECTION_KEY);
        sendCommand(CONNECT_TO_APP);
        sendCommand(SET_SENDER_KEY);
    }

    public void sendCommand(JSONObject command) throws Exception {
        sendBinaryMessage(plistXmlToBinary(createJSONCommand(command)));
    }

    private void sendCommand(String command) throws Exception {
        //System.out.println("sending " + command);
        sendBinaryMessage(plistXmlToBinary(command));
    }

    private void sendBinaryMessage(byte[] bytes) throws IOException {
        OutputStream os = socket.getOutputStream();
        byte[] lenght = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(bytes.length).array();
        os.write(lenght);
        os.write(bytes);
    }

    private void pushInput(byte[] inputBytes) throws Exception {
        buf.write(inputBytes);
        while (buf.size() >= 4) {
            byte[] bytes = buf.toByteArray();
            int size = ByteBuffer.wrap(bytes, 0, 4).getInt();
            if (bytes.length >= 4 + size) {
                String message = plistBinaryToXml(Arrays.copyOfRange(bytes, 4, size + 4));
                //System.out.println("receiving " + message );
                JSONObject jmessage = extractResponse(message);
                if (jmessage != null) {
                    callBack.handleResponse(new Response(jmessage));
                }
                buf = new ByteArrayOutputStream();
                buf.write(bytes, 4 + size, bytes.length - size - 4);
            } else {
                //throw new IllegalStateException();
            }
        }
    }

    private void process() throws Exception {
        InputStream is = socket.getInputStream();
        while (is.available() > 0) {
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            // System.err.println("Received " + bytes.length + " bytes.");
            pushInput(bytes);
        }
    }

    private JSONObject extractResponse(String message) throws Exception {
        Document doc = XMLUtil.parse(new InputSource(fromString(message)), false, false, null, new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new ByteArrayInputStream(new byte[0]));
            }
        });

        String encoded = (String) JSON_DATA_PATH.evaluate(doc, XPathConstants.STRING);
        if (encoded != null && !encoded.isEmpty()) {
            byte[] bytes = Base64.decode(encoded);
            String s = new String(bytes);
            JSONObject o = (JSONObject) JSONValue.parseWithException(s);
            return o;
        }
        return null;
    
//        final JSONObject[] o = new JSONObject[1];
//
//        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
//        parser.parse(new ByteArrayInputStream(message.getBytes("UTF-8")), new DefaultHandler() {
//            private String currentPath = "";
//
//            @Override
//            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//                addPath(qName);
//            }
//
//            @Override
//            public void characters(char[] ch, int start, int length) throws SAXException {
//                if (currentPath.equals("/plist/dict/dict/data")) {
//                    String encoded = new String(ch, start, length);
//                    if (encoded != null && !encoded.trim().isEmpty()) {
//                        String s = null;
//                        try {
//                            byte[] bytes = Base64.decode(encoded.trim());
//                            s = new String(bytes);
//                            o[0] = (JSONObject) JSONValue.parseWithException(s);
//                        } catch (ParseException ex) {
//                            throw new RuntimeException(s, ex);
//                        } catch (IOException ex) {
//                            Exceptions.printStackTrace(ex);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void endElement(String uri, String localName, String qName) throws SAXException {
//                removePath(qName);
//            }
//
//            private void addPath(String qName) {
//                currentPath += "/" + qName;
//            }
//
//            private void removePath(String qName) {
//                currentPath = currentPath.substring(0, currentPath.lastIndexOf("/" + qName));
//            }
//        });
//
//        return o[0];
}

    public static InputStream fromString(String str) {
        try {
            byte[] bytes = str.getBytes("UTF-8");
            return new ByteArrayInputStream(bytes);
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public void stop() {
        try {
            socket.close();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        keepGoing = false;
        if (socketListener != null) {
            socketListener.cancel();
        }
        keepGoing = true;

    }

    @Override
    public String getConnectionName() {
        return "iOS";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public WebSocketClient createWebSocket(WebSocketReadHandler handler) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
