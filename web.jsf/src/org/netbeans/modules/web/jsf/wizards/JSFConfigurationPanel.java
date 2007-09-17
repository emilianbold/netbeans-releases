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

package org.netbeans.modules.web.jsf.wizards;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.util.HelpCtx;

/**
 *
 * @author petr
 */
public class JSFConfigurationPanel extends WebModuleExtender {
    
    private final JSFFrameworkProvider framework;
    private final ExtenderController controller;
    private JSFConfigurationPanelVisual component;

    public enum LibraryType {USED, NEW, NONE};
    private LibraryType libraryType;
    private Library jsfCoreLibrary;
    private String newLibraryName;
    private File installedFolder;

    /** Creates a new instance of JSFConfigurationPanel */
    public JSFConfigurationPanel(JSFFrameworkProvider framework, ExtenderController controller, boolean customizer) {
        this.framework = framework;
        this.controller = controller;
        this.customizer = customizer;
        getComponent();
    }
    
    private boolean customizer;
    
    public JSFConfigurationPanelVisual getComponent() {
        if (component == null)
            component = new JSFConfigurationPanelVisual(this, customizer);

        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(JSFConfigurationPanel.class);
    }
    
    public void update() {
        component.update();
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid();
    }
    
    public Set extend(WebModule webModule) {
        return framework.extendImpl(webModule);
    }
    
    public ExtenderController getController() {
        return controller;
    }
    
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
    
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    public String getServletName(){
        return component.getServletName();
    }
    
    public void setServletName(String name){
        component.setServletName(name);
    }
    
    public String getURLPattern(){
        return component.getURLPattern();
    }
    
    public void setURLPattern(String pattern){
        component.setURLPattern(pattern);
    }
    
    public boolean validateXML(){
        return component.validateXML();
    }
    
    public void setValidateXML(boolean ver){
        component.setValidateXML(ver);
    }
    
    public boolean verifyObjects(){
        return component.verifyObjects();
    }
    
    public void setVerifyObjects(boolean val){
        component.setVerifyObjects(val);
    }
    
    public boolean packageJars(){
        return component.packageJars();
    }
    
    public String getNewLibraryName(){
        return newLibraryName;
    }
    
    public void setNewLibraryName(String version){
        this.newLibraryName = version;
    }

    public File getInstallFolder(){
        return installedFolder;
    }
    
    public void setInstallFolder(File folder){
        installedFolder = folder;
    }

    public LibraryType getLibraryType(){
        return libraryType;
    }
    
    public void setLibraryType(LibraryType value){
        libraryType = value;
    }

    public Library getLibrary(){
        return jsfCoreLibrary;
    }
    
    protected void setLibrary(Library library){
        this.jsfCoreLibrary = library;
    }

}
