/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * TomcatInstallUtil.java
 *
 * Created on December 9, 2003, 11:14 AM
 */

package org.netbeans.modules.tomcat5.util;

import java.io.*;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;

import org.netbeans.modules.tomcat5.config.*;
import org.netbeans.modules.tomcat5.TomcatFactory;

import org.w3c.dom.Document;
import org.apache.xml.serialize.*;
import org.openide.modules.InstalledFileLocator;


/**
 *
 * @author Martin Grebac
 */
public class TomcatInstallUtil {
    
    /** default value of bundled tomcat server port */
    private static final Integer BUNDLED_DEFAULT_SERVER_PORT = new Integer(8084);
    /** default value of bundled tomcat admin port */
    private static final Integer BUNDLED_DEFAULT_ADMIN_PORT = new Integer(8025);
    /** default value of bundled tomcat' http connector uri encoding */
    private static final String BUNDLED_DEFAULT_URI_ENCODING = "utf-8"; // NOI18N
    /** default value of bundled tomcat's host autoDeploy attribute */
    private static final Boolean BUNDLED_DEFAULT_AUTO_DEPLOY = Boolean.FALSE;
    
    private static final String ATTR_URI_ENCODING = "URIEncoding"; // NOI18N
    private static final String ATTR_PORT = "port"; // NOI18N
    private static final String ATTR_PROTOCOL = "protocol"; // NOI18N
    private static final String ATTR_AUTO_DEPLOY = "autoDeploy";    // NOI18N
    private static final String ATTR_SCHEME = "scheme";             // NOI18N
    private static final String ATTR_SECURE = "secure";             // NOI18N
    
    private static final String PROP_CONNECTOR = "Connector"; // NOI18N
    
    private static final String HTTP    = "http";   // NOI18N
    private static final String HTTPS   = "https";  // NOI18N
    private static final String TRUE    = "true";   // NOI18N
    
    /** Creates a new instance of TomcatInstallUtil */
    private TomcatInstallUtil() {
    }
    
    public static String getShutdownPort(Server server) {
        String port;
        
        port = server.getAttributeValue("port");
                
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("T5Util.getAdminPort: " + port);             // NOI18N
        }
        return port;
    }
    
    public static String getPort(Server server) {

        Service service = server.getService(0);

        int defCon = -1;
        boolean[] connectors = service.getConnector();
        String port;
        for (int i=0; i<service.sizeConnector(); i++) {
            String protocol = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_PROTOCOL);
            String scheme = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_SCHEME);
            String secure = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_SECURE);
            if (isHttpConnector(protocol, scheme, secure)) {
                defCon = i;
                break;
            }
        }
        
        if (defCon==-1 && service.sizeConnector() > 0) {
            defCon=0;
        }
        
        port = service.getAttributeValue(PROP_CONNECTOR, defCon, ATTR_PORT);

        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("T5Util.getPort: " + port);             // NOI18N
        }
        return port;
    }
    
    public static String getHost(Server server) {
        String host = null;
        Service service = server.getService(0);
        if (service != null) {
            host = service.getAttributeValue("Engine",0,"defaultHost");
        }
       
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("T5Util.getHost: " + host);             // NOI18N
        }
        return host;
    }
    
    /**
     * Return the CATALINA_HOME directory of the bundled Tomcat
     *
     * @return the CATALINA_HOME directory of the bundled Tomcat, <code>null</code>
     *         if the CATALINA_HOME directory does not exist which should never happen.
     */
    public static File getBundledHome() {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = fs.findResource(TomcatProperties.BUNDLED_TOMCAT_SETTING);
        if (fo != null) {
            InstalledFileLocator ifl = InstalledFileLocator.getDefault();
            return ifl.locate(fo.getAttribute("bundled_home").toString(), null, false); // NOI18N
        }
        return null;
    }
    
    /** @return text (suitable for printing to XML file) for a given XML document.
     * this method uses org.apache.xml.serialize.XMLSerializer class for printing XML file
     */
    public static String getDocumentText(Document doc) {
        OutputFormat format = new OutputFormat ();
        format.setPreserveSpace (true);
        StringWriter sw = new StringWriter();
        org.w3c.dom.Element rootElement = doc.getDocumentElement();
        if (rootElement==null) return null;
        try {
            XMLSerializer ser = new XMLSerializer (sw, format);
            ser.serialize (rootElement);
            // Apache serializer also fails to include trailing newline, sigh.
            sw.write('\n');
            return sw.toString();
        }catch(IOException ex) {
            System.out.println("ex="+ex);
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return rootElement.toString();
        }
        finally {
            try {
                sw.close();
            } catch(IOException ex) {
                System.out.println("ex="+ex);
                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    
    public static void updateDocument(javax.swing.text.Document doc, String newDoc, String prefixMark) throws javax.swing.text.BadLocationException {
        int origLen = doc.getLength();
        String origDoc = doc.getText(0, origLen);
        int prefixInd=0;
        if (prefixMark!=null) {
            prefixInd = origDoc.indexOf(prefixMark);
            if (prefixInd>0) {
                origDoc=doc.getText(prefixInd,origLen-prefixInd);
            }
            else {
                prefixInd=0;
            }
            int prefixIndNewDoc=newDoc.indexOf(prefixMark);
            if (prefixIndNewDoc>0)
                newDoc=newDoc.substring(prefixIndNewDoc);
        }
        
        if (origDoc.equals(newDoc)) {
            // no change in document
            return;
        }
        
        doc.remove(prefixInd, origLen - prefixInd);
        doc.insertString(prefixInd, newDoc, null);
    }
    
    private static boolean isHttpConnector(String protocol, String scheme, String secure) {
        return (protocol == null || protocol.length() == 0 || protocol.toLowerCase().equals(HTTP))
                && (scheme == null || !scheme.toLowerCase().equals(HTTPS))
                && (secure == null || !secure.toLowerCase().equals(TRUE));
    }
    
    public static boolean setServerPort(int port, File tomcatConf) {
        FileObject fo = FileUtil.toFileObject(tomcatConf);
        if (fo != null) {
            try {
                XMLDataObject dobj = (XMLDataObject)DataObject.find(fo);
                org.w3c.dom.Document doc = dobj.getDocument();
                org.w3c.dom.Element root = doc.getDocumentElement();
                org.w3c.dom.NodeList list = root.getElementsByTagName("Service"); //NOI18N
                int size=list.getLength();
                if (size>0) {
                    org.w3c.dom.Element service=(org.w3c.dom.Element)list.item(0);
                    org.w3c.dom.NodeList cons = service.getElementsByTagName(PROP_CONNECTOR);
                    for (int i=0;i<cons.getLength();i++) {
                        org.w3c.dom.Element con=(org.w3c.dom.Element)cons.item(i);
                        String protocol = con.getAttribute(ATTR_PROTOCOL);
                        String scheme = con.getAttribute(ATTR_SCHEME);
                        String secure = con.getAttribute(ATTR_SECURE);
                        if (isHttpConnector(protocol, scheme, secure)) {
                            con.setAttribute(ATTR_PORT, String.valueOf(port));
                            updateDocument(dobj,doc);
                            return true;
                        }
                    }
                }
            } catch(org.xml.sax.SAXException ex){
                org.openide.ErrorManager.getDefault ().notify(ex);
            } catch(org.openide.loaders.DataObjectNotFoundException ex){
                org.openide.ErrorManager.getDefault ().notify(ex);
            } catch(javax.swing.text.BadLocationException ex){
                org.openide.ErrorManager.getDefault ().notify(ex);
            } catch(java.io.IOException ex){
                org.openide.ErrorManager.getDefault ().notify(ex);
            }
        }
        return false;
    }
    
    private static void setServerAttributeValue(Server server, String attribute, String value) {
        server.setAttributeValue(attribute, value);
    }
    
    private static void setHttpConnectorAttributeValue(Server server, String attribute, String value) {
        Service services[] = server.getService();
        if (services != null && services.length > 0) {
            Service service = services[0];
            int sizeConnector = service.sizeConnector();
            for(int i = 0; i < sizeConnector; i++) {
                String protocol = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_PROTOCOL);
                String scheme   = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_SCHEME);
                String secure   = service.getAttributeValue(PROP_CONNECTOR, i, ATTR_SECURE);
                if (isHttpConnector(protocol, scheme, secure)) {
                    service.setAttributeValue(PROP_CONNECTOR, i, attribute, value);
                    return;
                }
            }
        }
    }
    
    private static void setHostAttributeValue(Server server, String attribute, String value) {
        Service service[] = server.getService();
        if (service != null) {
            for(int i = 0; i < service.length; i++) {
                Engine engine = service[i].getEngine();
                if (engine != null) {
                    Host host[] = engine.getHost();
                    if (host != null && host.length > 0) {
                        host[0].setAttributeValue(attribute, value);
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Make some Bundled Tomcat specific changes in server.xml.
     */
    public static void patchBundledServerXml(File serverXml) {
        try {
            Server server = Server.createGraph(serverXml);
            setServerAttributeValue(server, ATTR_PORT, BUNDLED_DEFAULT_ADMIN_PORT.toString());
            setHttpConnectorAttributeValue(server, ATTR_PORT, BUNDLED_DEFAULT_SERVER_PORT.toString());
            setHttpConnectorAttributeValue(server, ATTR_URI_ENCODING, BUNDLED_DEFAULT_URI_ENCODING);
            setHostAttributeValue(server, ATTR_AUTO_DEPLOY, BUNDLED_DEFAULT_AUTO_DEPLOY.toString());
            server.write(serverXml);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public static boolean setShutdownPort(int port, File tomcatConf) {
        FileObject fo = FileUtil.toFileObject(tomcatConf);
        if (fo != null) {
            try {
                XMLDataObject dobj = (XMLDataObject)DataObject.find(fo);
                org.w3c.dom.Document doc = dobj.getDocument();
                org.w3c.dom.Element root = doc.getDocumentElement();
                root.setAttribute("port", String.valueOf(port)); //NOI18N
                updateDocument(dobj,doc);
                return true;
            } catch(org.xml.sax.SAXException ex){
                org.openide.ErrorManager.getDefault ().notify(ex);
            } catch(org.openide.loaders.DataObjectNotFoundException ex){
                org.openide.ErrorManager.getDefault ().notify(ex);
            } catch(javax.swing.text.BadLocationException ex){
                org.openide.ErrorManager.getDefault ().notify(ex);
            } catch(java.io.IOException ex){
                org.openide.ErrorManager.getDefault ().notify(ex);
            }
        }
        return false;
    }
    
    public static void updateDocument(DataObject dobj, org.w3c.dom.Document doc)
        throws javax.swing.text.BadLocationException, java.io.IOException {
        org.openide.cookies.EditorCookie editor = (EditorCookie)dobj.getCookie(EditorCookie.class);
        javax.swing.text.Document textDoc = editor.getDocument();
        if (textDoc==null) textDoc = editor.openDocument();
        TomcatInstallUtil.updateDocument(textDoc,TomcatInstallUtil.getDocumentText(doc),"<Server"); //NOI18N
        SaveCookie savec = (SaveCookie) dobj.getCookie(SaveCookie.class);
        if (savec!=null) savec.save();
    }
    
    public static String generatePassword(int length) {
	int ran2 = 0;
	String pwd = "";
	for (int i = 0; i < length; i++) {
            ran2 = (int)(Math.random()*61);
            if (ran2 < 10) {
                ran2 += 48;
            } else {
                if (ran2 < 35) {
                    ran2 += 55;
                } else {
                    ran2 += 62;
                }
            }
            char c = (char) ran2;
            pwd += c;
	}
        return pwd;
    }

}
