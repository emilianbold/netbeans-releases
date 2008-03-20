/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.etlbulkloader.packager;

import org.netbeans.modules.etlbulkloader.tools.ETLBLUtils;
import org.netbeans.modules.etlbulkloader.tools.CopyFile;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.project.Localizer;


/**
 *
 * @author Manish
 */
public class ETLBLPackager {

    String projecthome = null;
    HashMap<String, String> modelAndEngineFilesMap = new HashMap();
    ArrayList triggerStrings = new ArrayList();
    private static transient final Logger mLogger = Logger.getLogger(ETLBLPackager.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public ETLBLPackager() {
        mLogger.infoNoloc(mLoc.t("Init ETL Bulk Loader Packaging ..."));
    }

    public ETLBLPackager(String projecthome) {
        this();
        this.projecthome = projecthome;
        createETLExecutableDir();
    }

    private void createETLExecutableDir() {
        String packageContainer = this.projecthome + ETLBLPkgConstants.fs + ETLBLPkgConstants.toplevelpkg;
        boolean statusL = ETLBLUtils.checkIfPackageExists(packageContainer, true);
        if (!statusL) {
            mLogger.infoNoloc(mLoc.t("ERROR: Unable To Create ETLLoader Package in the project home : " + packageContainer));
        //System.exit(0);
        }
        String eTLProcessContainer = packageContainer + ETLBLPkgConstants.fs + ETLBLPkgConstants.toplevelrt;
        boolean statusP = ETLBLUtils.checkIfPackageExists(eTLProcessContainer, true);
        if (!statusP) {
            mLogger.infoNoloc(mLoc.t("ERROR: Unable To Create ETLProcess Package in the project home : " + eTLProcessContainer));
        // System.exit(0);
        }
    }

    public void createExecutablePackage() {
        String pathToeTLDefinitionFiles = this.projecthome + ETLBLPkgConstants.fs + ETLBLPkgConstants.collabFolderName;
        String pathToeTLEngineFiles = this.projecthome + ETLBLPkgConstants.fs + ETLBLPkgConstants.buildFolderName;
        mLogger.infoNoloc(mLoc.t("Building package for artifacts under package :: \n" +
                "ETL Definition file :: " + pathToeTLDefinitionFiles + "\n" +
                "ETL Engine file :: " + pathToeTLEngineFiles));

        //Validate if each model file has an engine file generated, build a list
        correlateModelAndEngineFiles(pathToeTLDefinitionFiles, pathToeTLEngineFiles);

        //Copy Model and Engine files in respective packages
        if (this.modelAndEngineFilesMap != null) {
            Iterator i = this.modelAndEngineFilesMap.keySet().iterator();
            while (i.hasNext()) {
                String modelfilename = (String) i.next();
                String enginefilename = (String) this.modelAndEngineFilesMap.get(modelfilename);
                copyETLArtifactsToPackage(pathToeTLDefinitionFiles + ETLBLPkgConstants.fs + modelfilename, pathToeTLEngineFiles + ETLBLPkgConstants.fs + enginefilename);
            }

            //Copy Libs
            copyRequiredLibsToPackage();

            //Copy and Create Triggers
            copyAndAppendTriggers();
        }
    }

    private void correlateModelAndEngineFiles(String modelp, String enginep) {
        boolean statusModelp = ETLBLUtils.checkIfPackageExists(modelp, false);
        boolean statusEnginep = ETLBLUtils.checkIfPackageExists(enginep, false);
        if (!statusModelp) {
            mLogger.infoNoloc(mLoc.t("ERROR: Missing Source package from eTL Project : " + modelp));
        //System.exit(0);
        } else if (!statusEnginep) {
            mLogger.infoNoloc(mLoc.t("ERROR: Missing Source package from eTL Project : " + enginep));
        //System.exit(0);
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
                    mLogger.infoNoloc(mLoc.t("Predicted engine file name for model [ " + modelfiles[i] + " ] is : " + predictedEngineFileName));
                    // This model must have corresponding engine file built
                    for (int j = 0; j < enginefiles.length; j++) {
                        if (enginefiles[j].equals(predictedEngineFileName)) {
                            matchfound = true;
                            this.modelAndEngineFilesMap.put(modelfiles[i], predictedEngineFileName);
                            break;
                        }
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
        String outdir = this.projecthome + ETLBLPkgConstants.fs + ETLBLPkgConstants.toplevelpkg + ETLBLPkgConstants.fs + ETLBLPkgConstants.toplevelrt + ETLBLPkgConstants.fs + sourcepackage;
        boolean outpackageStatus = ETLBLUtils.checkIfPackageExists(outdir, true);

        if (outpackageStatus) {
            ETLBLUtils.copyFile(absmodelf, outdir);
            ETLBLUtils.copyFile(absenginef, outdir);

            //Build an entry for inserting into the trigger
            buildTriggerEntry(sourcepackage, new File(absenginef).getName());
        } else {
            mLogger.infoNoloc(mLoc.t("ERROR: Unable to create output package for eTL process : " + outdir));
        }
    }

    private void buildTriggerEntry(String srcfoldername, String enginefilename) {
        StringBuilder sb = new StringBuilder();
        sb.append("java -cp %CP% -Xms256M -Xmx1024M %JAVA_OPTS% ETLEngineInvoker .");
        sb.append(ETLBLPkgConstants.fs + "ETLProcess" + ETLBLPkgConstants.fs);
        sb.append(srcfoldername + ETLBLPkgConstants.fs);
        sb.append(enginefilename);
        this.triggerStrings.add(sb.toString());
    }

    private void copyRequiredLibsToPackage() {
        //Create a lib folder if not exists        
        String trgtLibdir = this.projecthome + ETLBLPkgConstants.fs + ETLBLPkgConstants.pkglibs;
        String srcLibPath = ETLBLPkgConstants.srclibs;
        String etlEnginePath = ETLBLPkgConstants.etlEnginePath;
        boolean status = ETLBLUtils.checkIfPackageExists(trgtLibdir, true);
        if (status) {
            //NOTE : ***** Check if some of these libs can be copied from project system directly *****
            File srclibs = new File(srcLibPath);
            if (!(srclibs.exists())) {
                srcLibPath = ETLBLPkgConstants.srclibs1;
                etlEnginePath = ETLBLPkgConstants.etlEnginePath1;
                srclibs = new File(srcLibPath);
            }
            String[] libnames = srclibs.list();
            try {
                CopyFile.copyFile(new File(etlEnginePath + ETLBLPkgConstants.fs + "etlengine.jar"), new File(trgtLibdir + ETLBLPkgConstants.fs + "etlengine.jar"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < libnames.length; i++) {
                if (libnames[i].endsWith(".jar")) {
                    mLogger.infoNoloc(mLoc.t("Copying Lib [ " + libnames[i] + " ] ..."));
                    try {
                        CopyFile.copyFile(new File(srcLibPath + ETLBLPkgConstants.fs + libnames[i]), new File(trgtLibdir + ETLBLPkgConstants.fs + libnames[i]));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            mLogger.infoNoloc(mLoc.t("ERROR: Unable to create lib package : " + trgtLibdir));
        }
    }

    private void copyAndAppendTriggers() {
        //Copy the triggers first to the root dir
        String path = ETLBLPkgConstants.srctriggertempl;
        File triggertempl = new File(path);
        if (!(triggertempl.exists())) {
            path = ETLBLPkgConstants.srctriggertempl1;
            triggertempl = new File(path);
        }

        String[] triggerstemp = triggertempl.list();
        for (int i = 0; i < triggerstemp.length; i++) {
            if (!(triggerstemp[i].endsWith(".jar"))) {
                mLogger.infoNoloc(mLoc.t("Copying Lib [ " + triggerstemp[i] + " ] ..."));
                ETLBLUtils.copyFile(path + ETLBLPkgConstants.fs + triggerstemp[i], this.projecthome + ETLBLPkgConstants.fs + ETLBLPkgConstants.toplevelpkg);
            }
        }
        //Modicy trigger with invocation calls
        String triggerpkg = this.projecthome + ETLBLPkgConstants.fs + ETLBLPkgConstants.toplevelpkg;
        File finaltrigfiles = new File(triggerpkg);
        String[] triggers = finaltrigfiles.list();
        for (int i = 0; i < triggers.length; i++) {
            if ((triggers[i].indexOf(".bat") != -1) || (triggers[i].indexOf(".sh") != -1)) {
                BufferedWriter out = null;
                try {
                    mLogger.infoNoloc(mLoc.t("Creating eTL Invocation trigger : " + triggers[i]));
                    out = new BufferedWriter(new FileWriter(triggerpkg + ETLBLPkgConstants.fs + triggers[i], true));
                    for (int j = 0; j < this.triggerStrings.size(); j++) {
                        out.write("\n" + this.triggerStrings.get(j).toString());
                    }
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
}
