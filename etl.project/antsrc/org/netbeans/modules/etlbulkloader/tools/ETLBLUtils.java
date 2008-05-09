/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.etlbulkloader.tools;

import org.netbeans.modules.etlbulkloader.packager.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.project.Localizer;

/**
 *
 * @author Manish
 */
public class ETLBLUtils {

    private static transient final Logger mLogger = Logger.getLogger(ETLBLUtils.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public static boolean checkIfPackageExists(String absPackagePath, boolean createIfNotExists) {
        File testpackage = new File(absPackagePath);
        if (testpackage.exists()) {
            if (testpackage.isDirectory()) {
                return true;
            } else {
                mLogger.infoNoloc(mLoc.t("Package : " + absPackagePath + " is not a dir"));
                return false;
            }
        } else {
            // Create this package
            if (createIfNotExists) {
                mLogger.infoNoloc(mLoc.t("Package : " + absPackagePath + " does not exist. Creating ...."));
                boolean status = testpackage.mkdir();
                if (status) {
                    return true;
                } else {
                    mLogger.infoNoloc(mLoc.t("Unable to create dir : " + absPackagePath));
                    return false;
                }
            } else {
                mLogger.infoNoloc(mLoc.t("Package : " + absPackagePath + " does not exist."));
                return false;
            }
        }
    }

    public static void copyFile(String srcFileAbsPath, String trgtDir) {        
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {

            File input = new File(srcFileAbsPath);
            String srcfilename = input.getName();
            File output = new File(trgtDir + ETLBLPkgConstants.fs + srcfilename);

            //in = new FileReader(input);
            //out = new FileWriter(output);
            fis = new FileInputStream(input);
            fos = new FileOutputStream(output);
            byte[] buf = new byte[1024];
            /*
            int c;
            while ((c = in.read()) != -1) {
            out.write(c);
            }
             */
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }


        } catch (IOException ex) {
            mLogger.severe(mLoc.x("PRJS031: Exception4 :{0}",ex.getMessage()),ex);
        } finally {
            try {
                //in.close();
                //out.close();
                fis.close();
                fos.close();
            } catch (IOException ex) {
                mLogger.severe(mLoc.x("PRJS032: Exception :{0}",ex.getMessage()),ex);
            }
        }
    }
}
