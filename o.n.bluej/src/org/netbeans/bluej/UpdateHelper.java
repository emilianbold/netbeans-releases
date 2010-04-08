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

package org.netbeans.bluej;

import java.io.IOException;
import javax.swing.JButton;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Mutex;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.xml.XMLUtil;


/**
 * Proxy for the AntProjectHelper which defers the update of the project metadata
 * to explicit user action. Currently it is hard coded for update from
 * "http://www.netbeans.org/ns/j2se-project/1" to "http://www.netbeans.org/ns/j2se-project/2".
 * In future it should define plugable SPI.
 *
 * TODO not sure this is necessaryf or bluej projects
 */
public class UpdateHelper {

    private static final boolean TRANSPARENT_UPDATE = Boolean.getBoolean("j2seproject.transparentUpdate");   // NOI18N
    private static final String BUILD_NUMBER = System.getProperty("netbeans.buildnumber"); // NOI18N
    private static final String MINIMUM_ANT_VERSION_ELEMENT = "minimum-ant-version";  // NOI18N

    private final Project project;
    private final AntProjectHelper helper;
    private final AuxiliaryConfiguration cfg;
    private final GeneratedFilesHelper genFileHelper;
    private final Notifier notifier;
    private boolean alreadyAskedInWriteAccess;
    private Boolean isCurrent;
    private Element cachedElement;

    /**
     * Creates new UpdateHelper
     * @param project
     * @param helper AntProjectHelper
     * @param cfg AuxiliaryConfiguration
     * @param genFileHelper GeneratedFilesHelper
     * @param notifier used to ask user about project update
     */
    UpdateHelper (Project project, AntProjectHelper helper, AuxiliaryConfiguration cfg, GeneratedFilesHelper genFileHelper, Notifier notifier) {
        assert project != null && helper != null && cfg != null && genFileHelper != null && notifier != null;
        this.project = project;
        this.helper = helper;
        this.cfg = cfg;
        this.genFileHelper = genFileHelper;
        this.notifier = notifier;
    }

    /**
     * Returns the AntProjectHelper.getProperties(), {@link AntProjectHelper#getProperties(String)}
     * @param path a relative URI in the project directory.
     * @return a set of properties
     */
    public EditableProperties getProperties (final String path) {
        //Properties are the same in both j2seproject/1 and j2seproject/2
        return (EditableProperties) ProjectManager.mutex().readAccess(new Mutex.Action (){
            public Object run() {
                if (!isCurrent() && AntProjectHelper.PROJECT_PROPERTIES_PATH.equals(path)) { //Only project properties were changed
                    return getUpdatedProjectProperties ();
                }
                else {
                    return helper.getProperties(path);                    
                }
            }
        });
    }

    /**
     * In the case that the project is of current version or the properties are not {@link AntProjectHelper#PROJECT_PROPERTIES_PATH}
     * it calls AntProjectHelper.putProperties(), {@link AntProjectHelper#putProperties(String, EditableProperties)}
     * otherwise it asks user to updata project. If the user agrees with the project update, it does the update and calls
     * AntProjectHelper.putProperties().
     * @param path a relative URI in the project directory.
     * @param props a set of properties
     */
    public void putProperties (final String path, final EditableProperties props) {
        ProjectManager.mutex().writeAccess(
            new Runnable () {
                public void run() {
                    if (isCurrent() || !AntProjectHelper.PROJECT_PROPERTIES_PATH.equals(path)) {  //Only project props should cause update
                        helper.putProperties(path,props);
                    }
                    else if (canUpdate()) {
                        try {
                            saveUpdate ();
                            helper.putProperties(path,props);
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify (ioe);
                        }
                    }
                }
            });
    }

    /**
     * In the case that the project is of current version or shared is false it delegates to
     * AntProjectHelper.getPrimaryConfigurationData(), {@link AntProjectHelper#getPrimaryConfigurationData(boolean)}.
     * Otherwise it creates an in memory update of shared configuration data and returns it.
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @return the configuration data that is available
     */
    public Element getPrimaryConfigurationData (final boolean shared) {
        return (Element) ProjectManager.mutex().readAccess(new Mutex.Action (){
            public Object run() {
                if (!shared || isCurrent()) { //Only shared props should cause update
                    return helper.getPrimaryConfigurationData(shared);
                }
                else {
                    return getUpdatedSharedConfigurationData ();
                }
            }
        });
    }

    /**
     * In the case that the project is of current version or shared is false it calls AntProjectHelper.putPrimaryConfigurationData(),
     * {@link AntProjectHelper#putPrimaryConfigurationData(Element, boolean)}.
     * Otherwise it asks user to update the project. If the user agrees with the project update, it does the update and calls
     * AntProjectHelper.PrimaryConfigurationData().
     * @param element the configuration data
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     * <code>private.xml</code>
     */
    public void putPrimaryConfigurationData (final Element element, final boolean shared) {
        ProjectManager.mutex().writeAccess(new Runnable () {
            public void run () {
                if (!shared || isCurrent()) {
                    helper.putPrimaryConfigurationData(element, shared);
                } else if (canUpdate()) {
                    try {
                        saveUpdate ();
                        helper.putPrimaryConfigurationData(element, shared);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    }
                }
            }
        });
    }

    /**
     * Returns an AntProjectHelper. The helper may not be used for accessing/storing project metadata.
     * For project metadata manipulation the UpdateHelper must be used.
     * @return AntProjectHelper
     */
    public AntProjectHelper getAntProjectHelper () {
        return this.helper;
    }

    /**
     * Request an saving of update. If the project is not of current version the user will be asked to update the project.
     * If the user agrees with an update the project is updated.
     * @return true if the metadata are of current version or updated
     */
    public boolean requestSave () throws IOException{
        if (isCurrent()) {
            return true;
        }
        if (!canUpdate()) {
            return false;
        }
        saveUpdate ();
        return true;
    }

    /**
     * Returns true if the project is of current version.
     * @return true if the project is of current version, otherwise false.
     */
    public synchronized boolean isCurrent () {
        return true;
    }

    private boolean canUpdate () {
        if (TRANSPARENT_UPDATE) {
            return true;
        }
        //Ask just once under a single write access
        if (alreadyAskedInWriteAccess) {
            return false;
        }
        else {
            boolean canUpdate = this.notifier.canUpdate();
            if (!canUpdate) {
                alreadyAskedInWriteAccess = true;
                ProjectManager.mutex().postReadRequest(new Runnable() {
                    public void run() {
                        alreadyAskedInWriteAccess = false;
                    }
                });
            }
            return canUpdate;
        }
    }

    private void saveUpdate () throws IOException {
        this.helper.putPrimaryConfigurationData(getUpdatedSharedConfigurationData(),true);
////        this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/1",true); //NOI18N
////        this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/2",true); //NOI18N
        ProjectManager.getDefault().saveProject (this.project);
        synchronized(this) {
            this.isCurrent = Boolean.TRUE;
        }
    }

    private synchronized Element getUpdatedSharedConfigurationData () {
        if (cachedElement == null) {
            Element  oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/1",true);    // NOI18N
            if (oldRoot != null) {
                Document doc = oldRoot.getOwnerDocument();
                Element newRoot = doc.createElementNS (BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE,"data"); // NOI18N
                XMLUtil.copyDocument (oldRoot, newRoot, BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE);
                Element sourceRoots = doc.createElementNS(BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  // NOI18N
                Element root = doc.createElementNS (BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   // NOI18N
                root.setAttribute ("id","src.dir");   // NOI18N
                sourceRoots.appendChild(root);
                newRoot.appendChild (sourceRoots);
                Element testRoots = doc.createElementNS(BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  // NOI18N
                root = doc.createElementNS (BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   // NOI18N
                root.setAttribute ("id","test.src.dir");   // NOI18N
                testRoots.appendChild (root);
                newRoot.appendChild (testRoots);                
                cachedElement = updateMinAntVersion (newRoot, doc);
            } else {
                oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/j2se-project/2",true);    // NOI18N
                if (oldRoot != null) {
                    Document doc = oldRoot.getOwnerDocument();
                    Element newRoot = doc.createElementNS (BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE,"data"); // NOI18N
                    XMLUtil.copyDocument (oldRoot, newRoot, BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE);
                    cachedElement = updateMinAntVersion (newRoot, doc);
                }
            }
        }
        return cachedElement;
    }
    
    private synchronized EditableProperties getUpdatedProjectProperties () {
        EditableProperties cachedProperties = this.helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        //The javadoc.additionalparam was not in NB 4.0
        if (cachedProperties.get (BluejProject.JAVADOC_ADDITIONALPARAM)==null) {
            cachedProperties.put (BluejProject.JAVADOC_ADDITIONALPARAM,"");    // NOI18N
        }
        if (cachedProperties.get ("build.generated.dir")==null) { // NOI18N
            cachedProperties.put ("build.generated.dir","${build.dir}/generated"); // NOI18N
        }
         if (cachedProperties.get ("meta.inf.dir")==null) { // NOI18N
            cachedProperties.put ("meta.inf.dir","${src.dir}/META-INF"); // NOI18N
        }
        return cachedProperties;
    }

    static final String MINIMUM_ANT_VERSION = "1.6.5";  // NOI18N
    
    private static Element updateMinAntVersion (final Element root, final Document doc) {
        NodeList list = root.getElementsByTagNameNS (BluejProjectType.PROJECT_CONFIGURATION_NAMESPACE,MINIMUM_ANT_VERSION_ELEMENT);
        if (list.getLength() == 1) {
            Element me = (Element) list.item(0);
            list = me.getChildNodes();
            if (list.getLength() == 1) {
                me.replaceChild (doc.createTextNode(MINIMUM_ANT_VERSION), list.item(0));
                return root;
            }
        }
        assert false : "Invalid project file"; // NOI18N
        return root;
    }

    /**
     * Creates an default Notifier. The default notifier displays a dialog warning user about project update.
     * @return notifier
     */
    public static Notifier createDefaultNotifier () {
        return new Notifier() {
            public boolean canUpdate() {
                JButton updateOption = new JButton (NbBundle.getMessage(UpdateHelper.class, "CTL_UpdateOption"));
                updateOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(UpdateHelper.class, "AD_UpdateOption"));
                return DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor (NbBundle.getMessage(UpdateHelper.class,"TXT_ProjectUpdate", BUILD_NUMBER),
                        NbBundle.getMessage(UpdateHelper.class,"TXT_ProjectUpdateTitle"),
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE,
                        new Object[] {
                            updateOption,
                            NotifyDescriptor.CANCEL_OPTION
                        },
                        updateOption)) == updateOption;
            }
        };
    }

    /**
     * Interface used by the UpdateHelper to ask user about
     * the project update.
     */
    public static interface Notifier {
        /**
         * Asks user to update the project
         * @return true if the project should be updated
         */
        public boolean canUpdate ();
    }
}
