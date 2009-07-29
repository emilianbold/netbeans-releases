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

    // facelets configuratin
    private boolean enableFacelets;
    private boolean debugFacelets;
    private boolean skipComments;
    private boolean createExamples;
    //jsf configuration
    private String facesSuffix;
    private String facesMapping;
    private boolean validateXml;
    private boolean verifyObjects;

    /** Creates a new instance of JSFConfigurationPanel */
    public JSFConfigurationPanel(JSFFrameworkProvider framework, ExtenderController controller, boolean customizer) {
        this.framework = framework;
        this.controller = controller;
        this.customizer = customizer;

        enableFacelets = false;
        debugFacelets = true;
        skipComments = true;
        createExamples = true;
        facesSuffix = ".xhtml"; //NOI18N
        validateXml = true;
        verifyObjects = false;
        facesMapping = "/faces/*";
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

    public String getFacesSuffix(){
        return facesSuffix;
    }

    public String getFacesMapping(){
        return facesMapping;
    }

    private void setFacesMapping(String facesMapping) {
        this.facesMapping = facesMapping;
    }

    public void update() {
        component.update();
    }
    
    public boolean isValid() {
        getComponent();
        if (component.valid()) {
            setFacesMapping(component.getURLPattern());
            return true;
        }
        return false;
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

    @Deprecated
    /*
     * Use getFacesMapping() instead
     */
    public String getURLPattern(){
        return component.getURLPattern();
    }

    public void setURLPattern(String pattern){
        if (component !=null)
            component.setURLPattern(pattern);
    }
    
    public boolean validateXML(){
        return validateXml;
    }
    
    public void setValidateXML(boolean ver){
        validateXml = ver;
    }
    
    public boolean verifyObjects(){
        return verifyObjects;
    }
    
    public void setVerifyObjects(boolean val){
        verifyObjects = val;
    }
    
    public boolean packageJars(){
        return component.packageJars();
    }
    
    public String getNewLibraryName(){
        return newLibraryName;
    }
    
    public void setNewLibraryName(String version){
        this.newLibraryName = version;
        fireChangeEvent();
    }

    public File getInstallFolder(){
        return installedFolder;
    }
    
    public void setInstallFolder(File folder){
        installedFolder = folder;
        fireChangeEvent();
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

    public boolean isEnableFacelets() {
        return enableFacelets;
    }

    public void setEnableFacelets(boolean enableFacelets) {
        if (this.enableFacelets != enableFacelets) {
            this.enableFacelets = enableFacelets;
        }
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
        fireChangeEvent();
    }

}
