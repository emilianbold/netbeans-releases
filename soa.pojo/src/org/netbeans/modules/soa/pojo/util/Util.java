/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.pojo.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.soa.pojo.api.model.POJOsModel;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEventListener;
import org.netbeans.modules.soa.pojo.model.api.POJOsModelImpl;
import org.netbeans.modules.soa.pojo.schema.POJOConsumer;
import org.netbeans.modules.soa.pojo.schema.POJOConsumers;
import org.netbeans.modules.soa.pojo.schema.POJOProvider;
import org.netbeans.modules.soa.pojo.schema.POJOProviders;
import org.netbeans.modules.soa.pojo.schema.POJOs;
import org.netbeans.modules.soa.pojo.wizards.POJOHelper;
import org.openide.filesystems.FileChangeListener;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.execution.ExecutorTask;

/**
 * Utility class
 * @author Sreenivasan Genipudi
 */
public class Util {
    public static boolean isJavaIdentifier(String s) {
        if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isJavaIdentifierNonStart(String s) {
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean pojoConsumerExist(Project prj, POJOConsumer pjs) {
        POJOs pojos = getPOJOs(prj);
        POJOConsumers pjcs = pojos.getPOJOConsumers();
        if ( pjcs == null) {
            return false;
        }
        POJOConsumer[] pjcArry = pjcs.getPOJOConsumer();
        for (POJOConsumer pjc : pjcArry) {
            if ( pjc.equals(pjs)) {
                return true;
            }
        }
        return false;
    }

    public static String getSelectedPackageName(FileObject targetFolder) {
        Project project = FileOwnerQuery.getOwner(targetFolder);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups [i].getRootFolder(), targetFolder);
        }
        if (packageName != null) {
            packageName = packageName.replaceAll("/", ".");
        }
        return packageName+"";
    }

    public synchronized static POJOs getPOJOs(Project prj) {
        POJOs pjs = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            File projDir = FileUtil.toFile(fo);
            File cfgFile = null;
            try {
                cfgFile = new File(projDir, NBPOJOConstants.NBPROJECT_DIR + File.separator
                        + NBPOJOConstants.POJOS_CONFIG_FILE_NAME);
                if (cfgFile.exists()) {
                    pjs = POJOs.read(cfgFile);
                } else {
                    //check if old cfg file exists.
                    File oldCfgFile = new File(projDir, NBPOJOConstants.NBPROJECT_DIR 
                            + File.separator + NBPOJOConstants.POJOS_OLD_CONFIG_FILE_NAME);
                    if (oldCfgFile.exists()){
                        //rename to new name.
                        oldCfgFile.renameTo(cfgFile);
                        pjs = POJOs.read(cfgFile);

                        File oldFile = new File(projDir, NBPOJOConstants.NBPROJECT_DIR
                                + File.separator + NBPOJOConstants.POJOS_OLD_CONFIG_FILE_NAME);
                        if (oldFile.exists()){
                            oldFile.delete();
                        }
                    } else {
                        pjs = new POJOs();
                        pjs.setVersion(NBPOJOConstants.LATEST_CFG_VERSION);
                        pjs.setPOJOProviders(new POJOProviders());
                    }
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return pjs;
    }

    private static File getPOJOsConfigFile(Project prj) {
        File configFile = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            File projDir = FileUtil.toFile(fo);

            try {
                configFile = new File(projDir, NBPOJOConstants.NBPROJECT_DIR + File.separator
                        + NBPOJOConstants.POJOS_CONFIG_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return configFile;
    }

    public static void savePOJOs(Project prj, POJOs pojos) {
        try {
            File configFile = getPOJOsConfigFile(prj);
            if (configFile != null) {
                pojos.write(configFile);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static FileObject getFOForJavaClass(Project prj, String qClassName){
        FileObject ret = null;
        String path = qClassName.replaceAll("\\.", "/"); //NOI18N
        String srcDir = POJOHelper.getProjectProperty(prj, "src.dir") ; //NOI18N
        ret = prj.getProjectDirectory().getFileObject(srcDir + "/" + path + ".java"); //NOI18N
        return ret;
    }

    public static FileObject getFOForPOJOsCfgFile(Project prj) {
        FileObject configFile = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();

            try {
                fo.getFileObject(NBPOJOConstants.NBPROJECT_DIR
                        + NBPOJOConstants.FILE_OBJECT_SEPARATOR).refresh();
                configFile = fo.getFileObject(NBPOJOConstants.NBPROJECT_DIR
                        + NBPOJOConstants.FILE_OBJECT_SEPARATOR
                        + NBPOJOConstants.POJOS_CONFIG_FILE_NAME);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return configFile;
    }

    private static FileObject getFOForNBPrjDir(Project prj) {
        return prj.getProjectDirectory().getFileObject(NBPOJOConstants.NBPROJECT_DIR);
    }


    public static void addCfgFileChangeListener(Project prj,
            FileChangeListener l){

        FileObject fo = getFOForPOJOsCfgFile(prj);
        FileChangeListener fcl = null;
        if (fo != null) {
            fcl = FileUtil.weakFileChangeListener(l, fo);
            fo.addFileChangeListener(fcl);
        } else {
            fo = getFOForNBPrjDir(prj);
            if (fo != null) {
                fcl = FileUtil.weakFileChangeListener(l, fo);
                fo.addFileChangeListener(fcl);
            }
        }
    }

    public static void removeModelListner(Project prj, FileChangeListener l){
        // WeakChangeListener will return true for its proxy.
        FileObject fo = getFOForPOJOsCfgFile(prj);
        if (fo != null) {
            fo.removeFileChangeListener(l);
        }

        fo = getFOForNBPrjDir(prj);
        if (fo != null) {
            fo.removeFileChangeListener(l);
        }
    }

    private static Project getNormalizedProject(Project prj){
        Project ret = null;
        try {
            ret = ProjectManager.getDefault().findProject(FileUtil.toFileObject(FileUtil.normalizeFile(FileUtil.toFile(prj.getProjectDirectory()))));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ret;
    }

    public static void addModelListener(Project prj, POJOsEventListener l){
        if (prj != null){
            Project np = getNormalizedProject(prj);
            if (np != null){
                prj = np;
            }
            POJOsModel model = prj.getLookup().lookup(POJOsModel.class);
            if (model != null){
                POJOsEventListener weak = WeakListeners.create(
                        POJOsEventListener.class, l, model);
                model.addPOJOsEventListener(weak);
            }
        }
    }

    public static void removeModelListener(Project prj, POJOsEventListener l){
        if (prj != null){
            Project np = getNormalizedProject(prj);
            if (np != null){
                prj = np;
            }
            POJOsModel model = prj.getLookup().lookup(POJOsModel.class);
            if (model != null){
                // WeakChangeListener will return true for its proxy.
                model.removePOJOsEventListener(l);
            }
        }
    }

    public static void addPOJO2Model(Project prj, POJOProvider pojo){
        if (prj != null){
            Project np = getNormalizedProject(prj);
            if (np != null){
                prj = np;
            }
            POJOsModelImpl model = (POJOsModelImpl) prj.getLookup().lookup(POJOsModel.class);
            if (model != null){
                model.addPojo(pojo);
            }
        }
    }
    public static void addPOJOConsumer2Model(Project prj, POJOConsumer pojo){
        if (prj != null){
            Project np = getNormalizedProject(prj);
            if (np != null){
                prj = np;
            }
            POJOsModelImpl model = (POJOsModelImpl) prj.getLookup().lookup(POJOsModel.class);
            if (model != null){
                model.addConsumer(pojo);
            }
        }
    }


    public static void changePOJOInModel(Project prj, POJOProvider op, POJOProvider np){
        if (prj != null){
            Project nprj = getNormalizedProject(prj);
            if (nprj != null){
                prj = nprj;
            }
            POJOsModelImpl model = (POJOsModelImpl) prj.getLookup().lookup(POJOsModel.class);
            if (model != null){
                model.changePojo(op, np);
            }
        }
    }

    public static void changePOJOConsumerInModel(Project prj, POJOConsumer op, POJOConsumer np){
        if (prj != null){
            Project nprj = getNormalizedProject(prj);
            if (nprj != null){
                prj = nprj;
            }
            POJOsModelImpl model = (POJOsModelImpl) prj.getLookup().lookup(
                    POJOsModel.class);
            if (model != null){
                model.changePOJOConsumer(op, np);
            }
        }
    }

    public static void fireCfgFileChangedEvent(Project prj, POJOs pjs){
        if (prj != null){
            Project np = getNormalizedProject(prj);
            if (np != null){
                prj = np;
            }
            POJOsModelImpl model = (POJOsModelImpl) prj.getLookup().lookup(
                    POJOsModel.class);
            if (model != null){
                model.fireCfgFileEditedEvent(pjs);
            }
        }
    }

    /**
     * Should be only called from Ant Task.
     *
     * @param prjFile
     * @param pjs
     */

    public static void fireCfgFileChangedEvent(File prjFile, POJOs pjs){
        try {
            if (prjFile != null){
                FileObject fo = FileUtil.toFileObject(prjFile);
                Project prj = FileOwnerQuery.getOwner(fo);
                POJOsModelImpl model = (POJOsModelImpl) prj.getLookup().lookup(
                        POJOsModel.class);
                if (model != null){
                    model.fireCfgFileEditedEvent(pjs);
                }
            }
        } catch (Throwable t){
            //ignore, called from command line. As this is called from Ant task.
        }
    }

    public static void deletePOJOFromModel(Project prj, POJOProvider op){
        if (prj != null){
            Project np = getNormalizedProject(prj);
            if (np != null){
                prj = np;
            }
            POJOsModelImpl model = (POJOsModelImpl) prj.getLookup().lookup(
                    POJOsModel.class);
            if (model != null){
                model.deletePojo(op);
            }
        }
    }

    public static boolean isValidPOJOFile(FileObject file) {
        //Need a better way to look at the file.
        try {
            JavaSource src = JavaSource.forFileObject(file);
            MethodExistenceCheckUtil mu = new MethodExistenceCheckUtil(src,
                    GeneratorUtil.POJO_DEFAULT_RECEIVE_OPERATION, null, false);
            return mu.containsPOJO() && mu.containsOperation();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    public static void close(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception ex) {
            //ignore
        }
    }

    public static void cleanAndBuild(Project prj){
        executeAntTarget(prj, new String[] {"clean", "jar"});//NOI18N
    }
    
    private static FileObject getFOForProjectBuildFile(Project prj) {
        FileObject buildFileFo = null;
        if (prj != null) {
            FileObject fo = prj.getProjectDirectory();
            buildFileFo = fo.getFileObject("build.xml"); //NOI18N
        }
        return buildFileFo;
    }

    private static void executeAntTarget(final Project project, final String[] antTargets){

        final ProgressHandle progressHandle = ProgressHandleFactory
                .createHandle(NbBundle.getMessage(Util.class,
                "MSG_REFRESH_PROGRESS")); //NOI18N;
        progressHandle.start();

        Runnable run = new Runnable() {
            public void run() {
                try {
                    FileObject buildXml = getFOForProjectBuildFile(project);

                    if (buildXml != null) {
                        ExecutorTask task = ActionUtils.runTarget(buildXml,
                                antTargets, null);
                        task.waitFinished();
                        if (task.result() != 0) {
                            String mes = NbBundle.getMessage(
                                    Util.class, "MSG_ERROR_BUILDING"); //NOI18N
                            NotifyDescriptor desc = new NotifyDescriptor
                                    .Message(mes,
                                    NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                        }
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                } finally {
                    progressHandle.finish();
                }
            }
        };

        RequestProcessor.getDefault().post(run);
    }
    
}
