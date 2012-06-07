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
package org.netbeans.modules.web.client.rest.wizard;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.ChangeListener;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardDescriptor.ProgressInstantiatingIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.Node;



/**
 * @author ads
 *
 */
public class JSClientIterator implements ProgressInstantiatingIterator<WizardDescriptor>{
    
    private static final String PROPERTY_VERSION = "version"; // NOI18N
    
    private static final String PROPERTY_REAL_NAME = "name"; // NOI18N
    
    private static String VOL_REGULAR = "regular"; // NOI18N
    
    private static String VOL_MINIFIED = "minified"; // NOI18N
    
    private static String VOL_DOCUMENTED = "documented"; // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(JSClientIterator.class.getName());

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener( ChangeListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#current()
     */
    @Override
    public Panel current() {
        return myPanels[myIndex];
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasPrevious()
     */
    @Override
    public boolean hasPrevious() {
        return myIndex >0 ;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#name()
     */
    @Override
    public String name() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#nextPanel()
     */
    @Override
    public void nextPanel() {
        if (! hasNext()) {
            throw new NoSuchElementException();
        }
        myIndex++;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#previousPanel()
     */
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        myIndex--;        
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener( ChangeListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#initialize(org.openide.WizardDescriptor)
     */
    @Override
    public void initialize( WizardDescriptor descriptor ) {
        myWizard = descriptor;
        myRestPanel = new RestPanel( descriptor );
        Project project = Templates.getProject( descriptor );
        Sources sources = ProjectUtils.getSources(project);
        myPanels = new WizardDescriptor.Panel[]{
                Templates.buildSimpleTargetChooser(project, 
                sources.getSourceGroups(Sources.TYPE_GENERIC)).bottomPanel(myRestPanel).create() };
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#instantiate()
     */
    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        Project project = Templates.getProject(myWizard);
        
        Node restNode = myRestPanel.getRestNode();
        RestServiceDescription description = restNode.getLookup().lookup(
                RestServiceDescription.class);
        Boolean addBackbone = (Boolean)myWizard.getProperty(RestPanel.ADD_BACKBONE);
        Boolean existsBackbone = (Boolean)myWizard.getProperty(RestPanel.EXISTS_BACKBONE);
        boolean useLocalBackbone = false;
        
        FileObject libs = FileUtil.createFolder(project.
                getProjectDirectory(),"js/libs");  // NOI18N
        
        if ( existsBackbone == null || !existsBackbone){
            if ( addBackbone!=null && addBackbone ){
                useLocalBackbone = addLibrary( libs );
            }
        }
        else{
            useLocalBackbone = true;
        }
        
        FileObject targetFolder = Templates.getTargetFolder(myWizard);
        String targetName = Templates.getTargetName(myWizard);
        
        FileObject templateFO = FileUtil.getConfigFile("Templates/ClientSide/new.js");  //NOI18N
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        DataObject createdFile = templateDO.createFromTemplate(dataFolder, targetName);
        createdFile.rename(targetName);
        
        JSClientGenerator generator = JSClientGenerator.create( description );
        generator.generate( useLocalBackbone );
        return null;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#instantiate()
     */
    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#uninitialize(org.openide.WizardDescriptor)
     */
    @Override
    public void uninitialize( WizardDescriptor descriptor ) {
        myPanels = null;
    }
    
    private boolean addLibrary(FileObject libs) {
        // TODO : Client side project should give API for library addition
        Library[] libraries = LibraryManager.getDefault().getLibraries();
        Library backbone = null;
        SpecificationVersion lastVersion=null;
        for (Library library : libraries) {
            String libName = library.getName();
            if ( libName.startsWith("cdnjs-backbone.js")){
                Map<String, String> properties = library.getProperties();
                String version = properties.get(PROPERTY_VERSION);
                int index = version.indexOf(' ');
                if ( index !=-1) {
                    version = version.substring( 0, index);
                }
                try {
                    SpecificationVersion specVersion = new SpecificationVersion(version);
                    if ( lastVersion == null || specVersion.compareTo(lastVersion)>0){
                        lastVersion = specVersion;
                        backbone = library;
                    }
                }
                catch( NumberFormatException e ){
                    continue;
                }
            }
        }
        if ( backbone == null ){
            return false;
        }
        try {
            FileObject libFolder = libs.createFolder(
                    backbone.getProperties().get(PROPERTY_REAL_NAME).
                        replace(' ', '-')+"-"+ // NOI18N
                    backbone.getProperties().get(PROPERTY_VERSION));
            List<URL> urls = backbone.getContent(VOL_MINIFIED);
            if ( urls.isEmpty() ){
                urls = backbone.getContent(VOL_REGULAR);
            }
            if ( urls.isEmpty() ){
                urls = backbone.getContent(VOL_DOCUMENTED);
            }
            for (URL url : urls) {
                String name = url.getPath();
                name = name.substring(name.lastIndexOf("/")+1); // NOI18N
                copyFile(url, name, libFolder);
            }
        }
        catch(IOException e ){
            return false;
        }
        
        return true;
    }
    
    private void copyFile(URL u, String name, FileObject libTarget) throws IOException {
        FileObject fo = libTarget.createData(name);
        InputStream is;
        try {
            is = u.openStream();
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.INFO, "could not open stream for "+u, ex); // NOI18N
            return;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "could not open stream for "+u, ex); // NOI18N
            return;
        }
        OutputStream os = null;
        try {
            os = fo.getOutputStream();
            FileUtil.copy(is, os);
        } finally {
            is.close();
            if (os != null) {
                os.close();
            }
        }
    }

    private WizardDescriptor myWizard;
    private RestPanel myRestPanel;
    private WizardDescriptor.Panel[] myPanels;
    private int myIndex;

}
