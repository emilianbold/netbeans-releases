/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.etlbulkloader.packager;

import java.io.BufferedInputStream;
import org.netbeans.modules.etlbulkloader.tools.ETLBLUtils;
import org.netbeans.modules.etlbulkloader.tools.CopyFile;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.sql.framework.model.SQLDBModel;

/**
 *
 * @author Manish
 */
public class ETLBLPackager {

    String projecthome = null;
    String projectName = "";
    ZipOutputStream zos;
    HashMap<String, String> modelAndEngineFilesMap = new HashMap();
    StringBuilder startLoadWin = new StringBuilder();
    StringBuilder startLoadUnix = new StringBuilder();
    private static transient final Logger mLogger = Logger.getLogger(ETLBLPackager.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    protected static final String fs = File.separator;
    
    public ETLBLPackager() {
        mLogger.infoNoloc(mLoc.t("Init ETL Bulk Loader Packaging ..."));
    }

    public ETLBLPackager(String projecthome) {
        this();
        this.projecthome = projecthome;
        createETLExecutableDir();
    }

    private void createETLExecutableDir() {
        String packageContainer = this.projecthome + fs + ETLBLPkgConstants.toplevelpkg;
        boolean statusL = ETLBLUtils.checkIfPackageExists(packageContainer, true);
        if (!statusL) {
            mLogger.infoNoloc(mLoc.t("ERROR: Unable To Create ETLLoader Package in the project home : " + packageContainer));
        }
        String eTLProcessContainer = packageContainer + fs + ETLBLPkgConstants.toplevelrt;
        boolean statusP = ETLBLUtils.checkIfPackageExists(eTLProcessContainer, true);
        if (!statusP) {
            mLogger.infoNoloc(mLoc.t("ERROR: Unable To Create ETLProcess Package in the project home : " + eTLProcessContainer));
        }
    }

    private boolean deleteDirectory(File path) {
        if (path != null) {
            if (path.exists()) {
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
            return (path.delete());
        }
        return false;
    }

    private void doCleanUp() {
        System.out.println("Deleting Dir : ETLLoader");
        File ldrdir = new File(this.projecthome + fs + "ETLLoader");
        if (ldrdir != null) {
            deleteDirectory(ldrdir);
            ldrdir.mkdir();
        }

        File zipfile = new File(this.projecthome + fs + getProjectName(this.projecthome) + "_ETL.zip");
        System.out.println("Deleting older zip : " + zipfile.getAbsolutePath());
        if (zipfile != null) {
            zipfile.delete();
        }
    }

    private String getProjectName(String projpath) {
        File f = new File(projpath);
        if (f != null) {
            return f.getName();
        }
        return "";
    }

    public void createExecutablePackage() {
        String pathToeTLDefinitionFiles = this.projecthome + fs + ETLBLPkgConstants.collabFolderName;
        String pathToeTLEngineFiles = this.projecthome + fs + ETLBLPkgConstants.buildFolderName;
        mLogger.infoNoloc(mLoc.t("Building package for artifacts under package :: \n" +
                "ETL Definition file :: " + pathToeTLDefinitionFiles + "\n" +
                "ETL Engine file :: " + pathToeTLEngineFiles));

        //Check if the project was built XXX - May need change later R6U2 when cli is integrated with build
        File f = new File(this.projecthome + fs + "build");
        boolean gencli = false;
        if ((f != null) && (f.exists())) {
            gencli = true; // Check t Project has been built
        }

        if (!gencli) {
            System.out.println("*********************************** WARNING ***********************************");
            System.out.println("***                     Project has not been built yet.                     ***");
            System.out.println("*** Pls build the project first and then attempt to build etl command line. ***");
            System.out.println("*********************************** WARNING ***********************************");
        }
        
            //Do Some Cleanup on previous builds
            doCleanUp();
            //Validate if each model file has an engine file generated, build a list
            correlateModelAndEngineFiles(pathToeTLDefinitionFiles, pathToeTLEngineFiles);

            //Copy Model and Engine files in respective packages
            if (this.modelAndEngineFilesMap != null) {
                Iterator i = this.modelAndEngineFilesMap.keySet().iterator();
                while (i.hasNext()) {
                    String modelfilename = (String) i.next();
                    String enginefilename = (String) this.modelAndEngineFilesMap.get(modelfilename);
                    copyETLArtifactsToPackage(pathToeTLDefinitionFiles + fs + modelfilename, pathToeTLEngineFiles + fs + enginefilename);
                }

                //Copy Libs
                copyRequiredLibsToPackage();
                //Copy and Create Triggers
                copyAndAppendTriggers();
                //Copy ETL CLI properties to trigger
                getEtlCliProperties();
            }
    }

    private void getEtlCliProperties() {
        String clipropspath = this.projecthome + fs + ETLBLPkgConstants.toplevelrt;
        //Copy this directory to zip location i.e. ETLLoader/ETLProcess
        File etlclizipdir = new File(this.projecthome + fs + ETLBLPkgConstants.toplevelpkg + fs + ETLBLPkgConstants.toplevelrt);
        etlclizipdir.mkdir();

        ETLBLUtils.copyFile(clipropspath + fs + "globalconnection.properties", etlclizipdir.getAbsolutePath());
        ETLBLUtils.copyFile(clipropspath + fs + "etlcli.properties", etlclizipdir.getAbsolutePath());
    }

    private void correlateModelAndEngineFiles(String modelp, String enginep) {
        boolean statusModelp = ETLBLUtils.checkIfPackageExists(modelp, false);
        boolean statusEnginep = ETLBLUtils.checkIfPackageExists(enginep, false);
        if (!statusModelp) {
            mLogger.infoNoloc(mLoc.t("ERROR: Missing Source package from eTL Project : " + modelp));
            return;
        } else if (!statusEnginep) {
            mLogger.infoNoloc(mLoc.t("ERROR: Missing Source package from eTL Project : " + enginep));
            return;
        } else {
            //Read All the Model Files available under model files path.
            File models = new File(modelp);
            File engines = new File(enginep);
            String[] modelfiles = models.list();
            String[] enginefiles = engines.list();

            for (int i = 0; i < modelfiles.length; i++) {
                boolean matchfound = false;
                if (modelfiles[i].indexOf(ETLBLPkgConstants.modelFileSuffix) != -1) {
                    String predictedEngineFileName = modelfiles[i].substring(0, modelfiles[i].indexOf(ETLBLPkgConstants.modelFileSuffix)) + ETLBLPkgConstants.engineFileSuffix;
                    // String predictedEngineFileName = modelfiles[i].substring(0, modelfiles[i].indexOf(ETLBLPkgConstants.modelFileSuffix)) + ETLBLPkgConstants.engineFileSuffix;
                    mLogger.infoNoloc(mLoc.t("Predicted engine file name for model [ " + modelfiles[i] + " ] is : " + predictedEngineFileName));
                    // This model must have corresponding engine file built
                    for (int j = 0; j < enginefiles.length; j++) {

                        String predictedEngineFileName1 = "";
                        if (enginefiles[j].endsWith("_engine.xml")) {
                            String fileName[] = enginefiles[j].split("_");
                            projectName = fileName[0];
                            predictedEngineFileName1 = projectName + "_" + predictedEngineFileName;
                            if (enginefiles[j].equals(predictedEngineFileName1)) { //equals(predictedEngineFileName)) {
                                matchfound = true;
                                this.modelAndEngineFilesMap.put(modelfiles[i], predictedEngineFileName1);
                                break;
                            }
                        }// if(enginefiles[j].endsWith("_engine.xml")
                    }
                    if (!matchfound) {
                        mLogger.warnNoloc(mLoc.t("WARNING: Engine file does not exist for model : " + modelfiles[i] + ".\n" +
                                "This will be excluded from the eTL Bulk Loader Package"));
                    }
                }
            }
        }
    }

    private void copyETLArtifactsToPackage(String absmodelf, String absenginef) {
        File etlmodel = new File(absmodelf);
        String sourcefilename = etlmodel.getName();
        String sourcepackage = null;
        if ((sourcefilename.indexOf(".")) != -1) {
            sourcepackage = sourcefilename.substring(0, sourcefilename.indexOf(".")).toUpperCase();
        } else {
            sourcepackage = sourcefilename.toUpperCase();
        }

        //Output Directory
        //String outdir = this.projecthome + fs + ETLBLPkgConstants.toplevelpkg + fs + ETLBLPkgConstants.toplevelrt + fs + sourcepackage;
        String outdir = this.projecthome + fs + ETLBLPkgConstants.toplevelpkg + fs + ETLBLPkgConstants.toplevelrt;
        boolean outpackageStatus = ETLBLUtils.checkIfPackageExists(outdir, true);

        if (outpackageStatus) {
            //ETLBLUtils.copyFile(absmodelf, outdir);
            ETLBLUtils.copyFile(absenginef, outdir);

            //Build an entry for inserting into the trigger
            buildTriggerEntry(sourcepackage, new File(absenginef).getName());
        } else {
            mLogger.infoNoloc(mLoc.t("ERROR: Unable to create output package for eTL process : " + outdir));
        }
    }

    private void buildTriggerEntry(String srcfoldername, String enginefilename) {
        this.startLoadWin.append("\n%JAVA_HOME%\\bin\\java -Xms256M -Xmx512M -cp %CLASSPATH% org.netbeans.modules.etlcli.executor.EngineExecutor .\\ETLProcess\\" + enginefilename + "\n");
        this.startLoadUnix.append("\n$JAVA_HOME/bin/java -Xms256M -Xmx512M -cp $CLASSPATH  org.netbeans.modules.etlcli.executor.EngineExecutor ./ETLProcess/" + enginefilename + "\n");        
    }

    private void copyRequiredLibsToPackage() {
        //Create a lib folder if not exists         
        String trgtLibdir = this.projecthome + fs + ETLBLPkgConstants.pkglibs;
        String srcLibPath = ETLBLPkgConstants.srclibs;
        String etlEnginePath = ETLBLPkgConstants.etlEnginePath;
        String etlInvokerPath = ETLBLPkgConstants.etlprojpath;
        boolean status = ETLBLUtils.checkIfPackageExists(trgtLibdir, true);

        if (status) {
            //NOTE : ***** Check if some of these libs can be copied from project system directly *****
            File srclibs = new File(srcLibPath);
            if (!(srclibs.exists())) {
                //srcLibPath = ETLBLPkgConstants.srclibs1;
                etlEnginePath = ETLBLPkgConstants.etlEnginePath;
                srclibs = new File(srcLibPath);
            }


            String[] libnames = srclibs.list();
            try {
                //Copy ETL Engine jar file to package                
                System.out.println("Copying engine [" + etlEnginePath + fs + "etlengine.jar" + "] to [" + trgtLibdir + fs + "etlengine.jar]");
                CopyFile.copyFile(new File(etlEnginePath + fs + "etlengine.jar"), new File(trgtLibdir + fs + "etlengine.jar"));
                //Copy axion db jar to package
                System.out.println("Copying Axion DB jar [" + ETLBLPkgConstants.axionDBPath + fs + "axiondb.jar" + "] to [" + trgtLibdir + fs + "axiondb.jar]");
                CopyFile.copyFile(new File(ETLBLPkgConstants.axionDBPath + fs + "axiondb.jar"), new File(trgtLibdir + fs + "axiondb.jar"));

                System.out.println("Copying engine invoker [" + ETLBLPkgConstants.etlprojpath + fs + "org-netbeans-modules-etl-project-etlcli.jar" + "] to [" + trgtLibdir + fs + "org-netbeans-modules-etl-project-etlcli.jar");
                CopyFile.copyFile(new File(ETLBLPkgConstants.etlprojpath + fs + "org-netbeans-modules-etl-project-etlcli.jar"), new File(trgtLibdir + fs + "org-netbeans-modules-etl-project-etlcli.jar"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        /*
        for (int i = 0; i < libnames.length; i++) {
        if (libnames[i].endsWith(".jar")) {
        mLogger.infoNoloc(mLoc.t("Copying Lib [ " + libnames[i] + " ] ..."));
        try {
        System.out.println("Copying Libs [" + srcLibPath + fs + libnames[i] + "] to [" + trgtLibdir + fs + libnames[i]+ "]");
        CopyFile.copyFile(new File(srcLibPath + fs + libnames[i]), new File(trgtLibdir + fs + libnames[i]));
        } catch (IOException e) {
        e.printStackTrace();
        }
        }
        }*/
        //zip
        //  getDriverLib();
        //zip
        } else {
            mLogger.infoNoloc(mLoc.t("ERROR: Unable to create lib package : " + trgtLibdir));
        }
    }

    private void copyAndAppendTriggers() {
        //Copy the triggers first to the root dir
        String path = ETLBLPkgConstants.srclibs;
        File triggertempl = new File(path);
        if (!(triggertempl.exists())) {
            path = ETLBLPkgConstants.srclibs;
            triggertempl = new File(path);
        }

        String[] triggerstemp = triggertempl.list();
        for (int i = 0; i < triggerstemp.length; i++) {
            if (!(triggerstemp[i].endsWith(".jar"))) {
                mLogger.infoNoloc(mLoc.t("Copying Lib [ " + triggerstemp[i] + " ] ..."));
                ETLBLUtils.copyFile(path + fs + triggerstemp[i], this.projecthome + fs + ETLBLPkgConstants.toplevelpkg);
            }
        }
        //Modicy trigger with invocation calls
        String triggerpkg = this.projecthome + fs + ETLBLPkgConstants.toplevelpkg;
        File finaltrigfiles = new File(triggerpkg);
        String[] triggers = finaltrigfiles.list();
        for (int i = 0; i < triggers.length; i++) {
            // Append for Windows
            if (triggers[i].indexOf(".bat") != -1){
                BufferedWriter out = null;
                try {
                    mLogger.infoNoloc(mLoc.t("Creating eTL Invocation trigger (Windows) : " + triggers[i]));
                    out = new BufferedWriter(new FileWriter(triggerpkg + fs + triggers[i], true));
                    //for (int j = 0; j < this.triggerStrings.size(); j++) {
                        out.write(this.startLoadWin.toString());
                    //}
                } catch (IOException ex) {
                    mLogger.errorNoloc(mLoc.t("PRJS023: Exception :{0}", ex.getMessage()), ex);
                } finally {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        mLogger.errorNoloc(mLoc.t("PRJS024: Exception :{0}", ex.getMessage()), ex);
                    }
                }
            }
            //Append for Unix
            else if ((triggers[i].indexOf(".sh") != -1)){
                BufferedWriter out = null;
                try {
                    mLogger.infoNoloc(mLoc.t("Creating eTL Invocation trigger (Unix) : " + triggers[i]));
                    out = new BufferedWriter(new FileWriter(triggerpkg + fs + triggers[i], true));
                    //for (int j = 0; j < this.triggerStrings.size(); j++) {
                        out.write(this.startLoadUnix.toString());
                    //}
                } catch (IOException ex) {
                    mLogger.errorNoloc(mLoc.t("PRJS023: Exception :{0}", ex.getMessage()), ex);
                } finally {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        mLogger.errorNoloc(mLoc.t("PRJS024: Exception :{0}", ex.getMessage()), ex);
                    }
                }                
            }
        }
    }

    /**
     * Creates a Zip archive. If the name of the file passed in is a
     * directory, the directory's contents will be made into a Zip file.
     */
    public void makeZip(String fileName1) //throws IOException, FileNotFoundException
    {

        try {
            String fileName = this.projecthome + fs + ETLBLPkgConstants.toplevelpkg;
            String output = this.projecthome + fs + projectName;
            File file1 = new File(this.projecthome, projectName);
            File file = new File(fileName);
            zos = new ZipOutputStream(new FileOutputStream(file1 + "_ETL.zip"));

            //Call recursion
            recurseFiles(file);

            zos.close();

        } catch (Exception e) {
            e.printStackTrace();


        }
    }

    /**
     * Recurses down a directory and its subdirectories to look for
     * files to add to the Zip. If the current file being looked at
     * is not a directory, the method adds it to the Zip file.
     */
    private void recurseFiles(File file)
            throws IOException, FileNotFoundException {

        if (file.isDirectory()) {
            String[] fileNames = file.list();
            if (fileNames != null) {
                for (int i = 0; i < fileNames.length; i++) {
                    recurseFiles(new File(file, fileNames[i]));
                }
            }
        } //Otherwise, a file so add it as an entry to the Zip file.
        else {
            byte[] buf = new byte[1024];
            int len;

            String actPath = getActualPath(file);
            ZipEntry zipEntry = new ZipEntry(actPath);

            //we're trying to add into the Zip archive.
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream in = new BufferedInputStream(fin);
            zos.putNextEntry(zipEntry);

            while ((len = in.read(buf)) >= 0) {
                zos.write(buf, 0, len);
            }

            in.close();
            zos.closeEntry();
        }
    }

    public String getActualPath(File file) {
        String fullPath = file.getPath();
        String filePkg = projectName;
        int pathLength = fullPath.length();
        int index = fullPath.indexOf(filePkg);
        String resulStr = fullPath.substring(index);
        return resulStr;
    }

    public void getDriverNames() {
        ETLDefinitionImpl def = new ETLDefinitionImpl();

        List dbList = def.getAllDatabases();
        if (dbList != null) {
            try {
                //System.out.println("size of the list ==="+db.size());
                for (int i = 0; i < dbList.size(); i++) {
                    SQLDBModel sqlModel = (SQLDBModel) dbList.get(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }//if
    }
}
