/*
 * TomcatInstallUtil.java
 *
 * Created on December 9, 2003, 11:14 AM
 */

package org.netbeans.modules.tomcat5.util;

import java.io.File;
import java.io.FileFilter;
import org.netbeans.modules.tomcat5.config.Engine;
import org.netbeans.modules.tomcat5.config.Host;
import org.netbeans.modules.tomcat5.config.Service;

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
    
    public static String getPort(Service service) {
        
        int defCon = -1;
        boolean[] connectors = service.getConnector();
        String port = "8080";   //NOI18N
                
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
        return port;
    }
    
    public static Host[] getHosts(Service service) {
        Host[] hosts = new Host[0];
        Engine engine = service.getEngine();
        if (engine!=null) {
            hosts = engine.getHost();
        }
        return hosts;
    }

    public static String getHostName(Host host) {
        return host.getAttributeValue("name");                                  //NOI18N
    }
    
}
