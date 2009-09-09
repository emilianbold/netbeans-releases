/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.jsf.wizards;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.jpa.dao.EjbFacadeWizardIterator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerIterator;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsf.palette.items.FromEntityBase;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileSystem;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Pavel Buzek
 */
public class PersistenceClientIterator implements TemplateWizard.Iterator {

    private int index;
    private transient WizardDescriptor.Panel[] panels;

    static final String[] UTIL_CLASS_NAMES = {"JsfCrudELResolver", "JsfUtil", "PagingInfo"}; //NOI18N
    static final String[] UTIL_CLASS_NAMES2 = {"JsfUtil", "PaginationHelper"}; //NOI18N
    static final String UTIL_FOLDER_NAME = "util"; //NOI18N
    private static final String FACADE_SUFFIX = "Facade"; //NOI18N
    private static final String CONTROLLER_SUFFIX = "Controller";  //NOI18N
    private static final String CONVERTER_SUFFIX = "Converter";  //NOI18N
    private static final String JAVA_EXT = "java"; //NOI18N
    public static final String JSF2_GENERATOR_PROPERTY = "jsf2Generator"; // "true" if set otherwise undefined
  
    private transient WebModuleExtender wme;
    
    public Set instantiate(TemplateWizard wizard) throws IOException
    {
        final List<String> entities = (List<String>) wizard.getProperty(WizardProperties.ENTITY_CLASS);
        final String jsfFolder = (String) wizard.getProperty(WizardProperties.JSF_FOLDER);
        final Project project = Templates.getProject(wizard);
        final FileObject javaPackageRoot = (FileObject)wizard.getProperty(WizardProperties.JAVA_PACKAGE_ROOT_FILE_OBJECT);
        final String jpaControllerPkg = (String) wizard.getProperty(WizardProperties.JPA_CLASSES_PACKAGE);
        final String controllerPkg = (String) wizard.getProperty(WizardProperties.JSF_CLASSES_PACKAGE);
        Boolean ajaxifyBoolean = (Boolean) wizard.getProperty(WizardProperties.AJAXIFY_JSF_CRUD);
        final boolean ajaxify = ajaxifyBoolean == null ? false : ajaxifyBoolean.booleanValue();
        final boolean jsf2Generator = "true".equals(wizard.getProperty(JSF2_GENERATOR_PROPERTY));
        
        PersistenceUnit persistenceUnit = 
                (PersistenceUnit) wizard.getProperty(org.netbeans.modules.j2ee.persistence.wizard.WizardProperties.PERSISTENCE_UNIT);

        if (persistenceUnit != null){
            try {
                ProviderUtil.addPersistenceUnit(persistenceUnit, Templates.getProject(wizard));
            }
            catch (InvalidPersistenceXmlException e) {
                throw new IOException(e.toString());
            }
        }
        
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        JSFFrameworkProvider fp = new JSFFrameworkProvider();
        if (!fp.isInWebModule(wm)) {    //add jsf if not already present
            updateWebModuleExtender(project, wm, fp);
            wme.extend(wm);
        }
        
        final JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport = new JpaControllerUtil.EmbeddedPkSupport();
        
        final String title = NbBundle.getMessage(PersistenceClientIterator.class, "TITLE_Progress_Jsf_Pages"); //NOI18N
        final ProgressContributor progressContributor = AggregateProgressFactory.createProgressContributor(title);
        final AggregateProgressHandle handle = 
                AggregateProgressFactory.createHandle(title, new ProgressContributor[]{progressContributor}, null, null);
        final ProgressPanel progressPanel = new ProgressPanel();
        final JComponent progressComponent = AggregateProgressFactory.createProgressComponent(handle);
        
        final Runnable r = new Runnable() {

            public void run() {
                final boolean genSessionBean=J2eeProjectCapabilities.forProject(project).isEjb31LiteSupported();
                try {
                    javaPackageRoot.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            handle.start();
                            int jpaProgressStepCount = genSessionBean ? EjbFacadeWizardIterator.getProgressStepCount(entities.size()) :  JpaControllerIterator.getProgressStepCount(entities.size());
                            int progressStepCount = jpaProgressStepCount + getProgressStepCount(ajaxify, jsf2Generator);
                            progressStepCount += ((jsf2Generator ? 5 : JSFClientGenerator.PROGRESS_STEP_COUNT) * entities.size());
                            progressContributor.start(progressStepCount);
                            FileObject jpaControllerPackageFileObject = FileUtil.createFolder(javaPackageRoot, jpaControllerPkg.replace('.', '/'));
                            if(genSessionBean)
                            {
                                EjbFacadeWizardIterator.generateSessionBeans(progressContributor, progressPanel, entities, project, jpaControllerPkg, jpaControllerPackageFileObject, false, false, true);
                            }
                            else
                            {
                                assert !jsf2Generator : "jsf2 generator works only with EJBs";
                                JpaControllerIterator.generateJpaControllers(progressContributor, progressPanel, entities, project, jpaControllerPkg, jpaControllerPackageFileObject, embeddedPkSupport, false);
                            }
                            FileObject jsfControllerPackageFileObject = FileUtil.createFolder(javaPackageRoot, controllerPkg.replace('.', '/'));
                            if (jsf2Generator) {
                                Sources srcs = ProjectUtils.getSources(project);
                                SourceGroup sgWeb[] = srcs.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
                                FileObject webRoot = sgWeb[0].getRootFolder();
                                generateJsfControllers2(progressContributor, progressPanel, jsfControllerPackageFileObject, controllerPkg, jpaControllerPkg, entities, ajaxify, project, jsfFolder, jpaControllerPackageFileObject, embeddedPkSupport, genSessionBean, jpaProgressStepCount, webRoot);
                            } else {
                                generateJsfControllers(progressContributor, progressPanel, jsfControllerPackageFileObject, controllerPkg, jpaControllerPkg, entities, ajaxify, project, jsfFolder, jpaControllerPackageFileObject, embeddedPkSupport, genSessionBean, jpaProgressStepCount);
                            }
                            progressContributor.progress(progressStepCount);
                        }
                    });
                } catch (IOException ioe) {
                    Logger.getLogger(PersistenceClientIterator.class.getName()).log(Level.INFO, null, ioe);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ioe.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                } finally {
                    progressContributor.finish();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressPanel.close();
                        }
                    });
                    handle.finish();
                }
            }
        };
        
        // Ugly hack ensuring the progress dialog opens after the wizard closes. Needed because:
        // 1) the wizard is not closed in the AWT event in which instantiate() is called.
        //    Instead it is closed in an event scheduled by SwingUtilities.invokeLater().
        // 2) when a modal dialog is created its owner is set to the foremost modal
        //    dialog already displayed (if any). Because of #1 the wizard will be
        //    closed when the progress dialog is already open, and since the wizard
        //    is the owner of the progress dialog, the progress dialog is closed too.
        // The order of the events in the event queue:
        // -  this event
        // -  the first invocation event of our runnable
        // -  the invocation event which closes the wizard
        // -  the second invocation event of our runnable
        
        SwingUtilities.invokeLater(new Runnable() {
            private boolean first = true;
            public void run() {
                if (!first) {
                    RequestProcessor.getDefault().post(r);
                    progressPanel.open(progressComponent, title);
                } else {
                    first = false;
                    SwingUtilities.invokeLater(this);
                }
            }
        });
        
        return Collections.singleton(DataFolder.findFolder(javaPackageRoot));
    }
    
    private static int getProgressStepCount(boolean ajaxify, boolean jsf2Generator) {
        int count = jsf2Generator ? UTIL_CLASS_NAMES2.length+1 : UTIL_CLASS_NAMES.length+2;    //2 "pre" messages (see generateJsfControllers) before generating util classes and controller/converter classes
        if (ajaxify) {
            count++;
        }
        return count;
    }
    
    private static void generateJsfControllers(ProgressContributor progressContributor, final ProgressPanel progressPanel, FileObject targetFolder, String controllerPkg, String jpaControllerPkg, List<String> entities, boolean ajaxify, Project project, String jsfFolder, FileObject jpaControllerPackageFileObject, JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport, boolean genSessionBean, int progressIndex) throws IOException {
        String progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Jsf_Util_Pre"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);     
        
        //copy util classes
        FileObject utilFolder = targetFolder.getFileObject(UTIL_FOLDER_NAME);
        if (utilFolder == null) {
            utilFolder = FileUtil.createFolder(targetFolder, UTIL_FOLDER_NAME);
        }
        String utilPackage = controllerPkg == null || controllerPkg.length() == 0 ? UTIL_FOLDER_NAME : controllerPkg + "." + UTIL_FOLDER_NAME;
        for (int i = 0; i < UTIL_CLASS_NAMES.length; i++){
            if (utilFolder.getFileObject(UTIL_CLASS_NAMES[i], JAVA_EXT) == null) {
                progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", UTIL_CLASS_NAMES[i] + "."+JAVA_EXT); //NOI18N
                progressContributor.progress(progressMsg, progressIndex++);
                progressPanel.setText(progressMsg);
                String content = JpaControllerUtil.readResource(PersistenceClientIterator.class.getClassLoader().getResourceAsStream(JSFClientGenerator.RESOURCE_FOLDER + UTIL_CLASS_NAMES[i] + ".java.txt"), "UTF-8"); //NOI18N
                content = content.replaceAll("__PACKAGE__", utilPackage);
                FileObject target = FileUtil.createData(utilFolder, UTIL_CLASS_NAMES[i] + "."+JAVA_EXT);//NOI18N
                String projectEncoding = JpaControllerUtil.getProjectEncodingAsString(project, target);
                JpaControllerUtil.createFile(target, content, projectEncoding);  //NOI18N
            }
            else {
                progressContributor.progress(progressIndex++);
            }
        }
        
        progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Jsf_Controller_Converter_Pre"); //NOI18N"Preparing to generate JSF controllers and converters";
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);  
        
        int[] nameAttemptIndices = new int[entities.size()];
        FileObject[] controllerFileObjects = new FileObject[entities.size()];
        FileObject[] converterFileObjects = new FileObject[entities.size()];
        for (int i = 0; i < controllerFileObjects.length; i++) {
            String entityClass = entities.get(i);
            String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
            String simpleControllerNameBase = simpleClassName + CONTROLLER_SUFFIX;
            String simpleControllerName = simpleControllerNameBase;
            while (targetFolder.getFileObject(simpleControllerName, JAVA_EXT) != null && nameAttemptIndices[i] < 1000) {
                simpleControllerName = simpleControllerNameBase + ++nameAttemptIndices[i];
            }
            String simpleConverterName = simpleClassName + CONVERTER_SUFFIX + (nameAttemptIndices[i] == 0 ? "" : nameAttemptIndices[i]);
            int converterNameAttemptIndex = 1;
            while (targetFolder.getFileObject(simpleConverterName, JAVA_EXT) != null && converterNameAttemptIndex < 1000) {
                simpleConverterName += "_" + converterNameAttemptIndex++;
            }
            controllerFileObjects[i] = GenerationUtils.createClass(targetFolder, simpleControllerName, null);
            converterFileObjects[i] = GenerationUtils.createClass(targetFolder, simpleConverterName, null);
        }
        
        if (ajaxify) {
            progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Add_Ajax_Lib"); //NOI18N
            progressContributor.progress(progressMsg, progressIndex++);
            progressPanel.setText(progressMsg);
            Library jsfExtensionsLib = LibraryManager.getDefault().getLibrary("jsf-extensions"); //NOI18N
            if (jsfExtensionsLib != null) {
                ProjectClassPathModifier.addLibraries(new Library[] {jsfExtensionsLib},
                        getSourceRoot(project), ClassPath.COMPILE);
            }
        }
        
        for (int i = 0; i < controllerFileObjects.length; i++) {
            String entityClass = entities.get(i);
            String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
            String firstLower = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
            if (nameAttemptIndices[i] > 0) {
                firstLower += nameAttemptIndices[i];
            }
            if (jsfFolder.endsWith("/")) {
                jsfFolder = jsfFolder.substring(0, jsfFolder.length() - 1);
            }
            if (jsfFolder.startsWith("/")) {
                jsfFolder = jsfFolder.substring(1);
            }
            String controller = ((controllerPkg == null || controllerPkg.length() == 0) ? "" : controllerPkg + ".") + controllerFileObjects[i].getName();
            String simpleJpaControllerName = simpleClassName + (genSessionBean ? FACADE_SUFFIX : "JpaController"); //NOI18N
            FileObject jpaControllerFileObject = jpaControllerPackageFileObject.getFileObject(simpleJpaControllerName, JAVA_EXT);
            JSFClientGenerator.generateJSFPages(progressContributor, progressPanel, project, entityClass, jsfFolder, firstLower, controllerPkg, controller, targetFolder, controllerFileObjects[i], embeddedPkSupport, entities, ajaxify, jpaControllerPkg, jpaControllerFileObject, converterFileObjects[i], genSessionBean, progressIndex);
            progressIndex += JSFClientGenerator.PROGRESS_STEP_COUNT;
        }
    }

    public static boolean doesSomeFileExistAlready(FileObject javaPackageRoot, FileObject webRoot,
            String jpaControllerPkg, String jsfControllerPkg, String jsfFolder, List<String> entities) {
        for (String entity : entities) {
            String simpleControllerName = getFacadeFileName(entity);
            String pkg = jpaControllerPkg;
            if (pkg.length() > 0) {
                pkg += ".";
            }
            if (javaPackageRoot.getFileObject((pkg+simpleControllerName).replace('.', '/')+".java") != null) {
                return true;
            }
            simpleControllerName = getControllerFileName(entity);
            pkg = jsfControllerPkg;
            if (pkg.length() > 0) {
                pkg += ".";
            }
            if (javaPackageRoot.getFileObject((pkg+simpleControllerName).replace('.', '/')+".java") != null) {
                return true;
            }
            String fileName = getJsfFileName(entity, jsfFolder, "");
            if (webRoot.getFileObject(fileName+"View.xhtml") != null ||
                webRoot.getFileObject(fileName+"Edit.xhtml") != null ||
                webRoot.getFileObject(fileName+"List.xhtml") != null ||
                webRoot.getFileObject(fileName+"Create.xhtml") != null) {
                return true;
            }
        }
        return false;
    }
    
    private static void generateJsfControllers2(
            ProgressContributor progressContributor,
            final ProgressPanel progressPanel,
            FileObject targetFolder,
            String controllerPkg,
            String jpaControllerPkg,
            List<String> entities,
            boolean ajaxify,
            Project project,
            String jsfFolder,
            FileObject jpaControllerPackageFileObject,
            JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport,
            boolean genSessionBean,
            int progressIndex,
            FileObject webRoot) throws IOException {
        String progressMsg;

        //copy util classes
        FileObject utilFolder = targetFolder.getFileObject(UTIL_FOLDER_NAME);
        if (utilFolder == null) {
            utilFolder = FileUtil.createFolder(targetFolder, UTIL_FOLDER_NAME);
        }
        String utilPackage = controllerPkg == null || controllerPkg.length() == 0 ? UTIL_FOLDER_NAME : controllerPkg + "." + UTIL_FOLDER_NAME;
        for (int i = 0; i < UTIL_CLASS_NAMES2.length; i++){
            if (utilFolder.getFileObject(UTIL_CLASS_NAMES2[i], JAVA_EXT) == null) {
                progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", UTIL_CLASS_NAMES2[i] + "."+JAVA_EXT); //NOI18N
                progressContributor.progress(progressMsg, progressIndex++);
                progressPanel.setText(progressMsg);
                FileObject tableTemplate = FileUtil.getConfigRoot().getFileObject("/Templates/JSF/JSF_From_Entity_Wizard/"+UTIL_CLASS_NAMES2[i] + ".ftl");
                FileObject target = FileUtil.createData(utilFolder, UTIL_CLASS_NAMES2[i] + "."+JAVA_EXT);//NOI18N
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("packageName", utilPackage);
                params.put("comment", Boolean.FALSE); // NOI18N
                JSFPaletteUtilities.expandJSFTemplate(tableTemplate, params, target);
            } else {
                progressContributor.progress(progressIndex++);
            }
        }

        //int[] nameAttemptIndices = new int[entities.size()];
        FileObject[] controllerFileObjects = new FileObject[entities.size()];
        for (int i = 0; i < controllerFileObjects.length; i++) {
            String simpleControllerName = getControllerFileName(entities.get(i));
            controllerFileObjects[i] = targetFolder.getFileObject(simpleControllerName, JAVA_EXT);
            if (controllerFileObjects[i] == null) {
                controllerFileObjects[i] = targetFolder.createData(simpleControllerName, JAVA_EXT);
            }
        }

        Charset encoding = FileEncodingQuery.getEncoding(project.getProjectDirectory());
        if (webRoot.getFileObject("resources/css/"+JSFClientGenerator.JSFCRUD_STYLESHEET) == null) {
            String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(JSFClientGenerator.RESOURCE_FOLDER + JSFClientGenerator.JSFCRUD_STYLESHEET), "UTF-8"); //NOI18N
            FileObject target = FileUtil.createData(webRoot, "resources/css/"+JSFClientGenerator.JSFCRUD_STYLESHEET);
            JSFFrameworkProvider.createFile(target, content, encoding.name());
            progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", target.getNameExt()); //NOI18N
            progressContributor.progress(progressMsg, progressIndex++);
            progressPanel.setText(progressMsg);
        }

        for (int i = 0; i < controllerFileObjects.length; i++) {
            String entityClass = entities.get(i);
            String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
            String simpleJpaControllerName = simpleClassName + (genSessionBean ? FACADE_SUFFIX : "JpaController"); //NOI18N

            progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", simpleClassName + "."+JAVA_EXT); //NOI18N
            progressContributor.progress(progressMsg, progressIndex++);
            progressPanel.setText(progressMsg);

            FileObject template = FileUtil.getConfigRoot().getFileObject("/Templates/JSF/JSF_From_Entity_Wizard/controller.ftl");
            Map<String, Object> params = new HashMap<String, Object>();
            String controllerClassName = controllerFileObjects[i].getName();
            String managedBean = controllerClassName.substring(0, 1).toLowerCase() + controllerClassName.substring(1);
            params.put("managedBeanName", managedBean);
            params.put("controllerPackageName", controllerPkg);
            params.put("controllerClassName", controllerClassName);
            params.put("entityFullClassName", entityClass);
            params.put("ejbFullClassName", jpaControllerPkg+"."+simpleJpaControllerName);
            params.put("ejbClassName", simpleJpaControllerName);
            params.put("entityClassName", simpleClassName);
            params.put("comment", Boolean.FALSE); // NOI18N
            FromEntityBase.createParamsForConverterTemplate(params, targetFolder, entityClass);

            JSFPaletteUtilities.expandJSFTemplate(template, params, controllerFileObjects[i]);

            params = FromEntityBase.createFieldParameters(webRoot, entityClass, managedBean, managedBean+".selected", false, true);
            expandSingleJSFTemplate("create.ftl", entityClass, jsfFolder, webRoot, "Create", params, progressContributor, progressPanel, progressIndex++);
            expandSingleJSFTemplate("edit.ftl", entityClass, jsfFolder, webRoot, "Edit", params, progressContributor, progressPanel, progressIndex++);
            expandSingleJSFTemplate("view.ftl", entityClass, jsfFolder, webRoot, "View", params, progressContributor, progressPanel, progressIndex++);
            params = FromEntityBase.createFieldParameters(webRoot, entityClass, managedBean, managedBean+".items", true, true);
            expandSingleJSFTemplate("list.ftl", entityClass, jsfFolder, webRoot, "List", params, progressContributor, progressPanel, progressIndex++);

            String styleAndScriptTags = "<h:outputStylesheet name=\"css/jsfcrud.css\"/>";
            JSFClientGenerator.addLinkToListJspIntoIndexJsp(WebModule.getWebModule(project.getProjectDirectory()),
                    simpleClassName, styleAndScriptTags, "UTF-8", "/"+getJsfFileName(entityClass, jsfFolder, "List"));

        }

    }

    private static String getControllerFileName(String entityClass) {
        String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
        return simpleClassName + CONTROLLER_SUFFIX;
    }

    private static String getFacadeFileName(String entityClass) {
        String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
        return simpleClassName + FACADE_SUFFIX;
    }

    private static String getJsfFileName(String entityClass, String jsfFolder, String name) {
        String simpleClassName = JpaControllerUtil.simpleClassName(entityClass);
        String firstLower = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
        if (jsfFolder.endsWith("/")) {
            jsfFolder = jsfFolder.substring(0, jsfFolder.length() - 1);
        }
        if (jsfFolder.startsWith("/")) {
            jsfFolder = jsfFolder.substring(1);
        }
        if (jsfFolder.length() > 0) {
            return jsfFolder+"/"+firstLower+"/"+name;
        } else {
            return firstLower+"/"+name;
        }
    }

    private static void expandSingleJSFTemplate(String templateName, String entityClass,
            String jsfFolder, FileObject webRoot, String name, Map<String, Object> params,
            ProgressContributor progressContributor, ProgressPanel progressPanel, int progressIndex) throws IOException {
        FileObject template = FileUtil.getConfigRoot().getFileObject("/Templates/JSF/JSF_From_Entity_Wizard/"+templateName);
        String fileName = getJsfFileName(entityClass, jsfFolder, name);
        String  progressMsg = NbBundle.getMessage(PersistenceClientIterator.class, "MSG_Progress_Jsf_Now_Generating", fileName); //NOI18N
        progressContributor.progress(progressMsg, progressIndex);
        progressPanel.setText(progressMsg);

        FileObject jsfFile = webRoot.getFileObject(fileName+".xhtml");
        if (jsfFile == null) {
            jsfFile = FileUtil.createData(webRoot, fileName+".xhtml");
        }
        JSFPaletteUtilities.expandJSFTemplate(template, params, jsfFile);
    }

    /**
     * Convenience method to obtain the source root folder.
     * @param project the Project object
     * @return the FileObject of the source root folder
     */
    private static FileObject getSourceRoot(Project project) {
        if (project == null) {
            return null;
        }

        // Search the ${src.dir} Source Package Folder first, use the first source group if failed.
        Sources src = ProjectUtils.getSources(project);
        SourceGroup[] grp = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grp.length; i++) {
            if ("${src.dir}".equals(grp[i].getName())) { // NOI18N
                return grp[i].getRootFolder();
            }
        }
        if (grp.length != 0) {
            return grp[0].getRootFolder();
        }

        return null;
    }

    public void initialize(TemplateWizard wizard) {
        index = 0;
        wme = null;
        // obtaining target folder
        Project project = Templates.getProject( wizard );
        DataFolder targetFolder=null;
        try {
            targetFolder = wizard.getTargetFolder();
        } catch (IOException ex) {
            targetFolder = DataFolder.findFolder(project.getProjectDirectory());
        }

        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        
        WizardDescriptor.Panel secondPanel = new ValidationPanel(
                new PersistenceClientEntitySelection(NbBundle.getMessage(PersistenceClientIterator.class, "LBL_EntityClasses"),
                        new HelpCtx("framework_jsf_fromentity"), wizard)); // NOI18N
        PersistenceClientSetupPanel thirdPanel = new PersistenceClientSetupPanel(project, wizard);
         
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());

        if (wm.getJ2eeProfile().equals(Profile.JAVA_EE_6_WEB) || wm.getJ2eeProfile().equals(Profile.JAVA_EE_6_FULL)) {
            wizard.putProperty(JSF2_GENERATOR_PROPERTY, "true");
        }

        JSFFrameworkProvider fp = new JSFFrameworkProvider();
        String[] names;
        if (fp.isInWebModule(wm)) {
            panels = new WizardDescriptor.Panel[] { secondPanel, thirdPanel };
            names = new String[] {
                NbBundle.getMessage(PersistenceClientIterator.class, "LBL_EntityClasses"),
                NbBundle.getMessage(PersistenceClientIterator.class, "LBL_JSFPagesAndClasses")
            };
        }
        else {
            updateWebModuleExtender(project, wm, fp);
            
            JSFConfigurationWizardPanel jsfWizPanel = new JSFConfigurationWizardPanel(wme);
            
            thirdPanel.setFinishPanel(false);
            panels = new WizardDescriptor.Panel[] { secondPanel, thirdPanel, jsfWizPanel };
            names = new String[] {
                NbBundle.getMessage(PersistenceClientIterator.class, "LBL_EntityClasses"),
                NbBundle.getMessage(PersistenceClientIterator.class, "LBL_JSFPagesAndClasses"),
                NbBundle.getMessage(PersistenceClientIterator.class, "LBL_JSF_Config_CRUD"),
            };
        }
        wizard.putProperty("NewFileWizard_Title", 
            NbBundle.getMessage(PersistenceClientIterator.class, "Templates/Persistence/JsfFromDB"));
        Wizards.mergeSteps(wizard, panels, names);
    }
    
    private void updateWebModuleExtender(Project project, WebModule wm, JSFFrameworkProvider fp) {
        if (wme == null) {
            ExtenderController ec = ExtenderController.create();
            String j2eeLevel = wm.getJ2eePlatformVersion();
            ec.getProperties().setProperty("j2eeLevel", j2eeLevel);
            J2eeModuleProvider moduleProvider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            if (moduleProvider != null) {
                String serverInstanceID = moduleProvider.getServerInstanceID();
                ec.getProperties().setProperty("serverInstanceID", serverInstanceID);
            }
            wme = fp.createWebModuleExtender(wm, ec);
        }
        wme.update();
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }
    
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
        wme = null;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return NbBundle.getMessage (PersistenceClientIterator.class, "LBL_WizardTitle_FromEntity");
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }

    public void previousPanel() {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
    
    /** 
     * A panel which checks that the target project has a valid server set
     * otherwise it delegates to the real panel.
     */
    private class ValidationPanel extends DelegatingWizardDescriptorPanel {

        private ValidationPanel(WizardDescriptor.Panel delegate) {
            super(delegate);
        }
        
        public boolean isValid() {
            Project project = getProject();
            WizardDescriptor wizardDescriptor = getWizardDescriptor();
            
            // check that this project has a valid target server
            if (!org.netbeans.modules.j2ee.common.Util.isValidServerInstance(project)) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(PersistenceClientIterator.class, "ERR_MissingServer")); // NOI18N
                return false;
            }

            return super.isValid();
        }
    }
}
