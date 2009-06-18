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
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.ModelUnit;
import org.netbeans.modules.web.jsf.impl.facesmodel.AbstractJsfModel;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;


/**
 * @author ads
 *
 */
public class JsfModelImpl extends AbstractJsfModel implements JsfModel {

    JsfModelImpl( ModelUnit unit ) {
        myUnit = unit;
        mySupport = new PropertyChangeSupport( this );
        myModels = new CopyOnWriteArrayList<JSFConfigModel>();
        myLastModifications = new WeakHashMap<JSFConfigModel, Long>();
        initModels();
        registerChangeListeners();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getElement(java.lang.Class)
     */
    public <T extends FacesConfigElement> List<T> getElement( Class<T> clazz ) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getFacesConfigs()
     */
    public List<FacesConfig> getFacesConfigs() {
        return Collections.unmodifiableList( myFacesConfig );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getMainConfig()
     */
    public FacesConfig getMainConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getModels()
     */
    public List<JSFConfigModel> getModels() {
        return Collections.unmodifiableList( myModels );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        getChangeSupport().addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener( PropertyChangeListener listener )
    {
        getChangeSupport().removePropertyChangeListener(listener);
    }
    
    private PropertyChangeSupport getChangeSupport(){
        return mySupport;
    }
    
    private void refreshModels(){
        
    }
    
    private void initModels() {
        // TODO Auto-generated method stub
        List<FacesConfig> list = new ArrayList<FacesConfig>( myModels.size() );
        for ( JSFConfigModel model : myModels ){
            list.add( model.getRootComponent() );
        }
        myFacesConfig = new CopyOnWriteArrayList<FacesConfig>( list );
        for ( JSFConfigModel model : myModels ){
            myLastModifications.put( model , -1L);
        }
    }
    
    private void registerChangeListeners() {
        
        ClassPath compile = myUnit.getCompilePath();
        compile.addPropertyChangeListener( new PropertyChangeListener(){

            public void propertyChange( PropertyChangeEvent event ) {
                // TODO Auto-generated method stub
                
            }
            
        });
        
        FileUtil.addFileChangeListener( new FileChangeListener(){

                public void fileAttributeChanged( FileAttributeEvent arg0 ) {
                }

                public void fileChanged( FileEvent event ) {
                    // TODO : check if it works . If so then we don't need
                    // to check file modification. 
                    FileObject file = event.getFile();
                    if ( !checkName(file)){
                        return;
                    }
                    ClassPath sources = myUnit.getSourcePath();
                    FileObject[] fileObjects = sources.getRoots();
                    for (FileObject fileObject : fileObjects) {
                        if ( FileUtil.isParentOf( fileObject, file)){
                            // TODO : find model by file object
                            JSFConfigModel model = null;
                            //model.sync();
                        }
                    }
                }

                public void fileDataCreated( FileEvent event ) {
                    FileObject file = event.getFile();
                    if ( !checkName(file)){
                        return;
                    }
                    ClassPath sources = myUnit.getSourcePath();
                    FileObject[] fileObjects = sources.getRoots();
                    for (FileObject fileObject : fileObjects) {
                        if ( FileUtil.isParentOf( fileObject, file)){
                            // TODO : create model based on newly discovered file
                            JSFConfigModel model = null;
                            myModels.add( model );
                            synchronized (myLastModifications) {
                                myLastModifications.put( model,  -1L);
                            }
                        }
                    }
                }

                public void fileDeleted( FileEvent event ) {
                    FileObject file = event.getFile();
                    if ( !checkName(file)){
                        return;
                    }
                    // TODO : find model based on file object.
                    JSFConfigModel model = null;
                    myModels.remove( model );
                    synchronized ( myLastModifications) {
                        myLastModifications.remove( model );
                    }
                }

                public void fileFolderCreated( FileEvent arg0 ) {
                }

                public void fileRenamed( FileRenameEvent arg0 ) {
                    // TODO Auto-generated method stub
                    
                }
                
                private boolean checkName( FileObject fileObject ){
                    // TODO
                    return false;
                }
                
            });
    }
    
    private final PropertyChangeSupport mySupport;
    private ModelUnit myUnit;
    private List<JSFConfigModel> myModels ;
    private List<FacesConfig> myFacesConfig;
    private Map<JSFConfigModel, Long> myLastModifications;
    private static final Logger LOG = Logger.getLogger(JsfModelImpl.class.getName());
}
