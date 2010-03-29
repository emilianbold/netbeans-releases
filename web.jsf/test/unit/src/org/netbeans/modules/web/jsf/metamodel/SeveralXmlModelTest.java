/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.web.jsf.metamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
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
public class SeveralXmlModelTest extends CommonTestCase {

    public SeveralXmlModelTest( String testName ) {
        super(testName);
    }
    
    public void testSeveralModelInSrc() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "WEB-INF/faces-config.xml",
                getFileContent("data/faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/one.faces-config.xml",
                getFileContent("data/one.faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/two.faces-config.xml",
                getFileContent("data/two.faces-config.xml"));
        
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                assertEquals( 3 , model.getModels().size());
                assertNotNull( model.getMainConfig() );
                assertEquals( 3 ,  model.getFacesConfigs().size());
                
                List<Application> applications = model.getElements( Application.class);
                assertEquals( 2 , applications.size());
                Application withSystemEventListener= null;
                for (Application application : applications) {
                    if ( application.getSystemEventListeners().size() == 1){
                        withSystemEventListener = application;
                    }
                }
                
                assertNotNull( "one application should have " +
                		"child system event listener", withSystemEventListener );
                Application empty = 
                    applications.get( 0 ).equals( withSystemEventListener) ? 
                            applications.get( 1 ) : applications.get(0);
                assertEquals( "one application should be empty", 0, empty.getChildren().size());
                return null;
            }
        });
    }
    
    public void testRemoveModelInSrc() throws IOException, InterruptedException{
        TestUtilities.copyStringToFileObject(srcFO, "WEB-INF/faces-config.xml",
                getFileContent("data/faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/one.faces-config.xml",
                getFileContent("data/one.faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/two.faces-config.xml",
                getFileContent("data/two.faces-config.xml"));
        
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                PropListener l = new PropListener();
                model.addPropertyChangeListener(l);
                srcFO.getFileObject("META-INF/two.faces-config.xml").delete();
                l.waitForModelUpdate();
                assertEquals( 2 ,  model.getModels().size());
                assertEquals( 2 , model.getFacesConfigs().size());
                
                List<Application> applications = model.getElements( Application.class);
                assertEquals( 1 , applications.size());
                return null;
            }
        });
    }

    public void testAddModelInSrc() throws IOException, InterruptedException{
        FileObject fileObject = srcFO.getFileObject("META-INF/one.faces-config.xml");
        if ( fileObject!= null ){
            fileObject.delete();
        }
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/one.faces-config.xml",
                getFileContent("data/one.faces-config.xml"));
        TestUtilities.copyStringToFileObject(srcFO, "META-INF/two.faces-config.xml",
                getFileContent("data/two.faces-config.xml"));
        
        createJsfModel().runReadAction(new MetadataModelAction<JsfModel,Void>(){

            public Void run( JsfModel model ) throws Exception {
                assertEquals( 2 ,  model.getModels().size());
                List<Application> applications = model.getElements( Application.class);
                assertEquals( 1 , applications.size());

                PropListener l = new PropListener();
                model.addPropertyChangeListener(l);
                TestUtilities.copyStringToFileObject(srcFO, "WEB-INF/faces-config.xml",
                        getFileContent("data/faces-config.xml"));
                l.waitForModelUpdate();
                
                assertEquals( 3 ,  model.getModels().size());
                assertEquals( 3 , model.getFacesConfigs().size());
                
                applications = model.getElements( Application.class);
                assertEquals( 2 , applications.size());
                return null;
            }
        });
    }

    /**
     * File change events (which cause reload of list of configuration files) are
     * fired in separate thread and to synchronize on delivery of these events
     * we wait on a property change event.
     */
    private static class PropListener implements PropertyChangeListener {

        private boolean modelUpdated = false;

        @Override
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            modelUpdated = true;
        }

        public void waitForModelUpdate() throws InterruptedException {
            while (!isModelUpdated()) {
                Thread.sleep(100);
            }
        }

        public synchronized boolean isModelUpdated() {
            return modelUpdated;
        }
        
    }
}
