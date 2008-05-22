/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.etlbulkloader.packager;

import java.io.File;
import java.io.IOException;
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
    public static final String sourcePath1 = getCWD() + fs + "netbeans" + fs + "soa2" + fs + "modules" + fs + "ext";
    public static final String sourcePath = getCWD() + fs + ".." + fs + "soa2" + fs + "modules" + fs + "ext";
    public static final String srclibs1 = sourcePath1 + fs + "bulkloader";
    public static final String srclibs = sourcePath + fs + "bulkloader";
    public static final String etlEnginePath1 = sourcePath1 + fs + "etl";
    public static final String etlEnginePath = sourcePath + fs + "etl";
    public static final String srctriggertempl = srclibs;// + fs + "bulkloader";
    public static final String srctriggertempl1 = srclibs1;// + fs + "bulkloader";
    //eTL Bulk Loader Package Construction Constants
    public static final String toplevelrt = "ETLProcess";
    public static final String toplevelpkg = "ETLLoader";
    public static final String pkglibs = toplevelpkg + fs + "lib";
    //eTL Project System Constants
    public static final String collabFolderName = "collaborations";
    public static final String buildFolderName = "build";
    public static final String modelFileSuffix = ".etl";
    public static final String engineFileSuffix = "_engine.xml";
    private static transient final Logger mLogger = Logger.getLogger(ETLBLPkgConstants.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    //Current Working dir
    public static String getCWD() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException ex) {
            mLogger.errorNoloc(mLoc.t("PRJS041: Exception :{0}", ex.getMessage()), ex);
        }
        return null;
    }
}
