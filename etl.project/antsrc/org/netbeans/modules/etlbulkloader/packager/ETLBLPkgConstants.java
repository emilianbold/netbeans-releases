/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.etlbulkloader.packager;

import java.io.File;
import java.io.IOException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.logger.LogUtil;

/**
 *
 * @author Manish
 */
public class ETLBLPkgConstants {

    // File System Constants
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String fs = System.getProperty("file.separator"); //File Separator
    //eTL Bulk Loader Source Constants
    public static final String srclibs = getCWD() + fs + "../etl.editor" + fs + "external";//+fs+"modules"+fs+"ext"+fs+"etlpro";    
    public static final String srctriggertempl = srclibs + fs + "triggertemplates";
    //eTL Bulk Loader Package Construction Constants
    public static final String toplevelrt = "ETLProcess";
    public static final String toplevelpkg = "ETLLoader";
    public static final String pkglibs = toplevelpkg + fs + "lib";
    //eTL Project System Constants
    public static final String collabFolderName = "collaborations";
    public static final String buildFolderName = "build";
    public static final String modelFileSuffix = ".etl";
    public static final String engineFileSuffix = "_engine.xml";
    
    private static transient final Logger mLogger = LogUtil.getLogger(ETLBLPkgConstants.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    //Current Working dir
    public static String getCWD() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException ex) {
            mLogger.errorNoloc(mLoc.t("PRSR041: Exception :{0}",ex.getMessage()),ex);
        }
        return null;
    }
}
