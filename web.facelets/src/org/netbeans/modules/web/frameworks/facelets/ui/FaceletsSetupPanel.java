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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.frameworks.facelets.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.frameworks.facelets.FaceletsFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsSetupPanel extends WebModuleExtender {
    
    private WizardDescriptor wizardDescriptor;
    private FaceletsSetupPanelVisual component;
    
    private boolean customizer;
    
    private String error_message;
   
    // library configuration
    public enum LibraryType {USED, NEW, NONE};
    private LibraryType libraryType;
    private Library faceletsCoreLibrary;
    private String newLibraryVersion;
    private File installedFolder;
    
    // facelets configuratin
    private boolean debugFacelets;
    private boolean skipComments;
    private boolean createExamples;
    //jsf configuration
    private String facesSuffix;
    private String facesMapping;
    
    
    private final Set <ChangeListener> listeners = new HashSet(1);
    private final FaceletsFrameworkProvider framework;
    private final ExtenderController controller;

    public FaceletsSetupPanel(FaceletsFrameworkProvider framework, ExtenderController controller, boolean customizer){
        this.framework = framework;
        this.controller = controller;
        this.customizer = customizer;
        libraryType = LibraryType.NONE;
        faceletsCoreLibrary = null;
        newLibraryVersion = null;
        installedFolder = null;
        debugFacelets = false;
        skipComments = true;
        createExamples = true;
        facesMapping = "*.jsf";
        facesSuffix = ".xhtml";
        getComponent();
    }
       
    protected void setErrorMessage(String message){
        if (error_message != null && (message == null || "".equals(message))){
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", ""); // NOI18N
            error_message = null;
        } else
            this.error_message = message;
        fireChangeEvent();
    }
    
//    public void enableComponents(boolean enable) {
//        getComponent();
//        component.enableComponents(enable);
//    }
    
    public FaceletsSetupPanelVisual getComponent() {
        if (component == null)
            component = new FaceletsSetupPanelVisual(this, customizer);
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(FaceletsSetupPanel .class);
    }
    
//    public void readSettings(Object settings) {
//        wizardDescriptor = (WizardDescriptor) settings;
//        //component.read(wizardDescriptor);
//
//        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
//        // this name is used in NewProjectWizard to modify the title
//        Object substitute = ((JComponent) getComponent()).getClientProperty("NewProjectWizard_Title"); // NOI18N
//        if (substitute != null)
//            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
//    }
//
//    public void storeSettings(Object settings) {
//        WizardDescriptor d = (WizardDescriptor) settings;
//        //component.store(d);
//        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
//    }
    
    public boolean isValid() {
//        boolean valid = true;
//        if (error_message != null && !"".equals(error_message)){
//            wizardDescriptor.putProperty( "WizardPanel_errorMessage", error_message); // NOI18N
//            valid = false;
//        }
        getComponent();
        String serverInstanceID = (String) getController().getProperties().getProperty("serverInstanceID");
        try{
            if(serverInstanceID != null && serverInstanceID.length()>0){
                if(hasLib(serverInstanceID, "javax.faces.application.ProjectStage")){
                    controller.setErrorMessage("facelets included in JSF2.0");// NOI18N
                    return false;
                }
            }
        }catch(IOException ioe){
        }

        return component.valid(wizardDescriptor);
    }

        protected static boolean containsClass(Collection<File> classpath, String className) throws IOException {
        Parameters.notNull("classpath", classpath);
        Parameters.notNull("driverClassName", className);
        String classFilePath = className.replace('.', '/') + ".class";
        for (File file : classpath) {
            if (file.isFile()) {
                JarFile jf = new JarFile(file);
                try {
                    Enumeration entries = jf.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = (JarEntry) entries.nextElement();
                        if (classFilePath.equals(entry.getName())) {
                            return true;
                        }
                    }
                } finally {
                    jf.close();
                }
            } else {
                if (new File(file, classFilePath).exists()) {
                    return true;
                }
            }
        }
        return false;
    }

    //JSF 1.1 	javax.faces.FacesException
    //JSF 1.2 	javax.faces.application.StateManagerWrapper
    //JSF 2.0 	javax.faces.application.ProjectStage
    private boolean hasLib(String serverInstanceID, String className) throws IOException {
        boolean hasLib = false;
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        if (serverInstanceID != null) {
            File[] classpathEntries = j2eePlatform.getClasspathEntries();
            hasLib = containsClass(Arrays.asList(classpathEntries), className);
            return hasLib;
        }
        return hasLib;
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    public boolean isFinishPanel() {
        return true;
    }
    
//    public void validate() throws WizardValidationException {
//
//    }
    
    void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    public File getInstallFolder(){
        return installedFolder;
    }
    
    public void setInstallFolder(File folder){
        installedFolder = folder;
    }
    
    public boolean isDebugFacelets(){
        return debugFacelets;
    }
    
    public void setDebugFacelets(boolean value){
        debugFacelets = value;
    }
    
    public boolean isSkipComments(){
        return skipComments;
    }
    
    public void setSkipComments(boolean value){
        skipComments = value;
    }
    
    public boolean isCreateExamples(){
        return createExamples;
    }
     
    public void setCreateExamples(boolean value){
        createExamples = value;
    }
   
    public LibraryType getLibraryType(){
        return libraryType;
    }
    
    public void setLibraryType(LibraryType value){
        libraryType = value;
    }
    
    public Library getLibrary(){
        return faceletsCoreLibrary;
    }
    
    protected void setLibrary(Library library){
        this.faceletsCoreLibrary = library;
    }
    
    public String getNewLibraryVersion(){
        return newLibraryVersion;
    }
    
    public void setNewLibraryVersion(String version){
        this.newLibraryVersion = version;
    }
    
    public String getFacesSuffix(){
        return facesSuffix;
    }
    
    public String getFacesMapping(){
        return facesMapping;
    }
    
    public boolean isCustomizer(){
        return customizer;
    }

    public void update() {
        component.update();
    }

    public Set<FileObject> extend(WebModule webModule) {
        return framework.extendImpl(webModule);
    }

    public ExtenderController getController() {
        return controller;
    }
}
