/*
 * TomcatInstallUtil.java
 *
 * Created on December 9, 2003, 11:14 AM
 */

package org.netbeans.modules.tomcat5.util;

import java.io.*;

import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.tomcat5.config.*;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.TomcatManager;

import org.w3c.dom.Document;
import org.apache.xml.serialize.*;

/**
 *
 * @author  snajper
 */
public class TomcatInstallUtil {
    
    static private final String TOMCAT_TEMP_DIR = "temp";                       //NOI18N
    static private final String SERVER_XML_NAME_EXT = "server.xml";             //NOI18N
    static private final String TOMCAT_CONF_DIR = "conf";//NOI18N

    /** Creates a new instance of TomcatInstallUtil */
    public TomcatInstallUtil() {
    }    
    
    public static boolean noTempDir(File homeDir) {
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().equals(TOMCAT_TEMP_DIR); 
            }
        };
        File[] subFolders = homeDir.listFiles( filter );
        if ( subFolders == null || subFolders.length == 0 )
            return true;

        return false;        
    }
    
    public static boolean noServerXML(File homeDir, File baseDir) {
        File testDir = ( baseDir != null ) ? baseDir : homeDir;
        
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().equals(TOMCAT_CONF_DIR);
            }
        };
        File[] subFolders = testDir.listFiles( filter );
        if ( subFolders == null || subFolders.length == 0 )
            return true;
        
        File[] serverFiles = subFolders[0].listFiles( new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().equals( SERVER_XML_NAME_EXT );
            }
        } );
        if ( serverFiles == null || serverFiles.length == 0 )
            return true;

        return false;
    }
    
    public static boolean noBootStrapJar(File homeDir, File baseDir) {
        File[] subFolders = homeDir.listFiles();
        if (subFolders==null) return true;
        for (int i=0; i<subFolders.length; i++) {
            if (subFolders[i].getName().equals("bin")&&subFolders[i].isDirectory()) {//NOI18N
                File[] subBinFolders = subFolders[i].listFiles();
                if (subBinFolders==null) return true;
                for (int ii=0; ii<subBinFolders.length; ii++) {
                    if (subBinFolders[ii].getName().equals("bootstrap.jar")) {  //NOI18N
                        return false;
                   }
                }
            }
        }
        return true;
    }    

    public static String getAdminPort(Server server) {
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
            String protocol = service.getAttributeValue("Connector",i,"protocol"); // NOI18N
            if ((protocol == null) || (protocol.toLowerCase().indexOf("http") > -1)) { // NOI18N
                defCon = i;
            }
        }
        
        if (defCon==-1 && service.sizeConnector() > 0) {
            defCon=0;
        }
        
        port = service.getAttributeValue("Connector",defCon,"port");            //NOI18N

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
    /** The method is useful to notify the user that Tomcat must be restarted 
     *
    */
    public static void notifyToRestart(final TomcatManager mng) {
        org.openide.util.RequestProcessor.getDefault().post( new Runnable() {
            public void run() {
                if (mng.getStartTomcat().isRunning()) {
                    DialogDisplayer disp = DialogDisplayer.getDefault();
                    disp.notify(new NotifyDescriptor.Message(
                     org.openide.util.NbBundle.getMessage(TomcatInstallUtil.class,"MSG_TomcatRestart")));
                }
            }
        });
    }
    
    /** The method is useful to notify the user that changes cannot be performed 
     *
    */
    public static void notifyThatRunning(final TomcatManager mng) {
        DialogDisplayer disp = DialogDisplayer.getDefault();
        disp.notify(new NotifyDescriptor.Message(
         org.openide.util.NbBundle.getMessage(TomcatInstallUtil.class,"MSG_TomcatIsRunning")));
    }
    
}
