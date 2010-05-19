/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.etlbulkloader.packager;

import java.io.File;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.project.Localizer;

/**
 *
 * @author Manish
 */
public class ETLBLPkgConstants {

    // File System Constants
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String fs = File.separator;
    //eTL Bulk Loader Source Constants 
    //Change the cluster names 
    public static final String soaCluster = getClusters(new File(getNetbeansHome()), "soa");
    public static final String sourcePath = getNetbeansHome() + fs + soaCluster + fs + "modules" + fs + "ext";
    public static final String axionDBPath = sourcePath + fs + "dm" + fs + "virtual" + fs + "db";
    public static final String etlprojpath = sourcePath + fs + "etlpro";
    public static final String srclibs = sourcePath + fs + "bulkloader";
    public static final String etlEnginePath = sourcePath + fs + "etl";    //eTL Bulk Loader Package Construction Constants
    public static final String toplevelrt = "ETLProcess";  //shal  "ETLProcess"
    public static final String toplevelpkg = "ETLLoader";   //shal  
    public static final String pkglibs = toplevelpkg + fs + "lib";
    //eTL Project System Constants
    public static final String collabFolderName = "Collaborations";
    //public static final String cliproperties = "etlcli";
    public static final String buildFolderName = "build";
    public static final String modelFileSuffix = ".etl";
    public static final String engineFileSuffix = "_engine.xml";
    private static transient final Logger mLogger = Logger.getLogger(ETLBLPkgConstants.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    private static String getNetbeansHome() {
        String netbeansHome = System.getProperty("netbeans.home");
        if (!netbeansHome.endsWith("netbeans")) {
            File f = new File(netbeansHome);
            netbeansHome = f.getParentFile().getAbsolutePath();
        }
        return netbeansHome;
    }

    public static String getClusters(File dir, String str) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                if (children[i].trim().toLowerCase().startsWith(str)) {
                    return children[i];
                }
            }
        }
        return null;
    }
}
