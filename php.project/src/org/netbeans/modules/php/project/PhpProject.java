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
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.api.classpath.GlobalPathRegistry;
import org.netbeans.modules.php.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.php.project.customizer.PhpCustomizerProvider;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.rt.utils.PhpProjectSharedConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * @author ads
 *
 */
public class PhpProject implements Project, AntProjectListener {
    
    protected static String SRC_              = "src.";               // NOI18N
    
    protected static String _DIR              = "dir";                // NOI18N
    
    public static String SRC                = SRC_ + _DIR;
    
    public static String SRC_DIR            = "${" + SRC + "}";     // NOI18N
    
    public static String TMP_FILE_POSTFIX   = "~";     // NOI18N
    
    public static final String PROVIDER_ID  = "provider.id";        // NOI18N
    
    public static final String VERSION      = "version";            // NOI18N
    
    public static final String COMMAND_PATH = "command.path";       // NOI18N
    
    private static final String NAME        
            = PhpProjectSharedConstants.PHP_PROJECT_NAME; // NOI18N
    
    public static final String SOURCE_ENCODING = "source.encoding"; // NOI18N
    
    public static final String SOURCE_LBL  = "LBL_Node_Sources";   // NOI18N

    public static final String SOURCES_TYPE_PHP 
            = PhpProjectSharedConstants.SOURCES_TYPE_PHP;
    
    public static final String COPY_SRC_FILES = "copy.src.files"; // NOI18N
    public static final String COPY_SRC_TARGET = "copy.src.target"; // NOI18N
    public static final String URL = "url"; // NOI18N

    private static final Icon PROJECT_ICON = 
        new ImageIcon(Utilities.loadImage( 
                ResourceMarker.getLocation()+ResourceMarker.PROJECT_ICON ));

    PhpProject( AntProjectHelper helper ) {
        myHelper = helper;
        AuxiliaryConfiguration configuration = 
            helper.createAuxiliaryConfiguration();
        myRefHelper = new ReferenceHelper(helper, configuration, getEvaluator());
        myGenFilesHelper = new GeneratedFilesHelper( helper );
        helper.addAntProjectListener(this);
        initLookup( configuration );
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.project.Project#getLookup()
     */
    public Lookup getLookup() {
        return myLookup;
    }

    /* (non-Javadoc)
     * @see org.netbeans.api.project.Project#getProjectDirectory()
     */
    public FileObject getProjectDirectory() {
        return getHelper().getProjectDirectory();
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.project.support.ant.AntProjectListener#configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
     */
    public void configurationXmlChanged( AntProjectEvent event ) {
        /*
         *  The code below is standart and copied f.e. from MakeProject
         */
        if (event.getPath().equals( AntProjectHelper.PROJECT_XML_PATH ) ) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info)getLookup().lookup( ProjectInformation.class );
            info.firePropertyChange( ProjectInformation.PROP_NAME );
            info.firePropertyChange( ProjectInformation.PROP_DISPLAY_NAME );
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.project.support.ant.AntProjectListener#propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
     */
    public void propertiesChanged( AntProjectEvent arg0 ) {
        // We are interested only to listen to changes in sources.
        // PhpSources will do it itself
        /*
         * Also copied from  MakeProject
         */
        //  currently ignored (probably better to listen to evaluator() if you need to)
    }
    
    /*
     * Copied from MakeProject.
     */
    public String getName() {
        return ProjectManager.mutex().readAccess(
                new Mutex.Action<String>() 
                {

                    public String run() {
                        Element data = getHelper().getPrimaryConfigurationData(true);
                        NodeList nl = data.getElementsByTagNameNS(
                                PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, NAME);
                        if (nl.getLength() == 1) {
                            nl = nl.item(0).getChildNodes();
                            if (nl.getLength() == 1
                                    && nl.item(0).getNodeType() == Node.TEXT_NODE)
                            {
                                return ((Text) nl.item(0)).getNodeValue();
                            }
                        }
                        return "???";                                           // NOI18N
                    }
                });
    }
    
    /*
     * Copied from MakeProject.
     */
    public void setName( final String name ) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {

            public Object run() {
                Element data = getHelper().getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(
                        PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, NAME ); 
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                }
                else {
                    nameEl = data.getOwnerDocument().createElementNS(
                            PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                            NAME ); 
                    data.insertBefore(nameEl, /* OK if null */data
                            .getChildNodes().item(0));
                }
                nameEl
                        .appendChild(data.getOwnerDocument().createTextNode(
                                name));
                getHelper().putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
    
    public AntProjectHelper getHelper() {
        return myHelper;
    }
    
    PropertyEvaluator getEvaluator() {
        if ( myEvaluator == null ) {
            myEvaluator = getHelper().getStandardPropertyEvaluator();
        }
        return myEvaluator;
    }

    private void initLookup( AuxiliaryConfiguration configuration ) {

        SubprojectProvider provider = getRefHelper().createSubprojectProvider();
        PhpSources phpSources = new PhpSources(getHelper(), getEvaluator());
        myLookup = Lookups.fixed(new Object[] {
                new Info(),
                configuration,
                new PhpXmlSavedHook(),
                new PhpOpenedHook(),
                provider,
                new PhpActionProvider( this ),
                getHelper().createCacheDirectoryProvider(),
                new ClassPathProviderImpl(getHelper(), getEvaluator(), phpSources),
                new PhpLogicalViewProvider( this , provider ),
                new CustomizerProviderImpl(this, myHelper),
                getHelper().createSharabilityQuery( getEvaluator(), 
                    new String[] { SRC_DIR } , new String[] {} ),
                new PhpProjectOperations(this) ,
                new PhpProjectEncodingQueryImpl(getEvaluator()),
                new PhpTemplates(),
                phpSources,
                getHelper(),
                getEvaluator()
                // ?? getRefHelper()
        });
    }

    private ReferenceHelper getRefHelper() {
        return myRefHelper;
    }
    
    private final class Info implements ProjectInformation {

        /* (non-Javadoc)
         * @see org.netbeans.api.project.ProjectInformation#addPropertyChangeListener(java.beans.PropertyChangeListener)
         */
        public void addPropertyChangeListener( PropertyChangeListener  listener  ) {
            mySupport.addPropertyChangeListener( listener );
        }

        /* (non-Javadoc)
         * @see org.netbeans.api.project.ProjectInformation#getDisplayName()
         */
        public String getDisplayName() {
            return PropertyUtils.getUsablePropertyName(getName());
        }

        /* (non-Javadoc)
         * @see org.netbeans.api.project.ProjectInformation#getIcon()
         */
        public Icon getIcon() {
            return PROJECT_ICON;
        }

        /* (non-Javadoc)
         * @see org.netbeans.api.project.ProjectInformation#getName()
         */
        public String getName() {
            return PhpProject.this.getName();
        }

        /* (non-Javadoc)
         * @see org.netbeans.api.project.ProjectInformation#getProject()
         */
        public Project getProject() {
            return PhpProject.this;
        }

        /* (non-Javadoc)
         * @see org.netbeans.api.project.ProjectInformation#removePropertyChangeListener(java.beans.PropertyChangeListener)
         */
        public void removePropertyChangeListener( PropertyChangeListener listener ){
            mySupport.removePropertyChangeListener(listener);
        }
        
        void firePropertyChange(String prop) {
            mySupport.firePropertyChange( prop , null, null );
        }
        
        private final PropertyChangeSupport mySupport = 
            new PropertyChangeSupport(this);
 
    }
    
    private final class PhpXmlSavedHook extends ProjectXmlSavedHook {
        
        protected void projectXmlSaved() {
        /*
            It seems we don't have "build" scripts here in this project.
            So I commented out this code at least for now. 
            
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                MakeProject.class.getResource("resources/build-impl.xsl"),
                false);
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_XML_PATH,
                MakeProject.class.getResource("resources/build.xsl"),
                false);
        */
        }
    }
    
    private final class PhpOpenedHook extends ProjectOpenedHook {
        
        protected void projectOpened() {
            ClassPathProviderImpl cpProvider = myLookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
        }
        
        protected void projectClosed() {
            try {
                ProjectManager.getDefault().saveProject( PhpProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    
    private final AntProjectHelper myHelper;
    
    private PropertyEvaluator myEvaluator;
    
    private final ReferenceHelper myRefHelper;
    
    private GeneratedFilesHelper myGenFilesHelper;

    private Lookup myLookup;

}
