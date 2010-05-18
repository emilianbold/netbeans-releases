/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.project.jsf.framework;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.util.HelpCtx;

/**
 *
 * @author Po-Ting Wu
 */
public class JSFConfigurationPanel extends WebModuleExtender {

    private final JSFFrameworkProvider framework;
    private final Project project;
    private final ExtenderController controller;
    private JSFConfigurationPanelVisual component;

    public enum LibraryType {USED, NEW, NONE};
    private LibraryType libraryType;
    private Library jsfCoreLibrary;
    private String newLibraryVersion;
    private File installedFolder;

    /** Creates a new instance of JSFConfigurationPanel */
    public JSFConfigurationPanel(JSFFrameworkProvider framework, Project project, ExtenderController controller, boolean customizer) {
        this.project = project;
        this.framework = framework;
        this.controller = controller;
        this.customizer = customizer;
        getComponent();
    }
    
    private boolean customizer;

    public JSFConfigurationPanelVisual getComponent() {
        if (component == null)
            component = new JSFConfigurationPanelVisual(this, project, customizer);

        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(JSFConfigurationPanel.class);
    }
    
    public void update() {
        component.update();
        
        // <RAVE> Default Bean Package
        String name = (String)controller.getProperties().getProperty("name"); // NOI18N
        if (name != null && name.length() > 0) {
            setBeanPackage(name);
        }
        // </RAVE>
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

    // <RAVE> Default Bean Package
    public String getBeanPackage(){
        return component.getBeanPackage();
    }

    public void setBeanPackage(String pkg_name){
        component.setBeanPackage(JsfProjectUtils.deriveSafeName(pkg_name));
    }
    // </RAVE>

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
    
    public String getNewLibraryVersion(){
        return newLibraryVersion;
    }
    
    public void setNewLibraryVersion(String version){
        this.newLibraryVersion = version;
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
