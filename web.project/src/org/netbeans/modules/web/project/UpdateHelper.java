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

package org.netbeans.modules.web.project;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.modules.web.project.classpath.ClassPathSupport;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Mutex;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;


/**
 * Proxy for the AntProjectHelper which defers the update of the project metadata
 * to explicit user action. Currently it is hard coded for update from
 * "http://www.netbeans.org/ns/web-project/1" to "http://www.netbeans.org/ns/web-project/2".
 * In future it should define plugable SPI.
 */
public class UpdateHelper {

    private static final boolean TRANSPARENT_UPDATE = Boolean.getBoolean("webproject.transparentUpdate"); //NOI18N
    private static final String BUILD_NUMBER = System.getProperty("netbeans.buildnumber"); // NOI18N

    private final Project project;
    private final AntProjectHelper helper;
    private final AuxiliaryConfiguration cfg;
    private final Notifier notifier;
    private boolean alreadyAskedInWriteAccess;
    private Boolean isCurrent;
    private EditableProperties cachedProperties;
    private Element cachedElement;
    private static final String TAG_MINIMUM_ANT_VERSION = "minimum-ant-version"; // NOI18N
    private static final String TAG_FILE = "file"; //NOI18N
    private static final String TAG_LIBRARY = "library"; //NOI18N
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N

    /**
     * Creates new UpdateHelper
     * @param project
     * @param helper AntProjectHelper
     * @param cfg AuxiliaryConfiguration
     * @param notifier used to ask user about project update
     */
    UpdateHelper (Project project, AntProjectHelper helper, AuxiliaryConfiguration cfg, Notifier notifier) {
        assert project != null && helper != null && cfg != null && notifier != null;
        this.project = project;
        this.helper = helper;
        this.cfg = cfg;
        this.notifier = notifier;
    }
   
    /**
     * Returns the AntProjectHelper.getProperties(), {@link AntProjectHelper#getProperties(String)}
     * @param path a relative URI in the project directory.
     * @return a set of properties
     */
    public EditableProperties getProperties (final String path) {
        //Properties are the same in both webproject/1 and webproject/2
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
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                if (isCurrent() || !AntProjectHelper.PROJECT_PROPERTIES_PATH.equals(path)) {  //Only project props should cause update
                    helper.putProperties(path,props);
                } else if (canUpdate()) {
                    try {
                        saveUpdate(props);
                        helper.putProperties(path,props);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
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
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                if (!shared || isCurrent()) {
                    helper.putPrimaryConfigurationData(element, shared);
                } else if (canUpdate()) {
                    try {
                        saveUpdate(null);
                        helper.putPrimaryConfigurationData(element, shared);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
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
        saveUpdate (null);
        return true;
    }
    
    /**
     * Returns true if the project is of current version.
     * @return true if the project is of current version, otherwise false.
     */
    public synchronized boolean isCurrent () {
        if (this.isCurrent == null) {
            this.isCurrent = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/web-project/1",true) == null //NOI18N
                    && this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/web-project/2",true) == null? //NOI18N
                Boolean.TRUE : Boolean.FALSE;
        }
        return isCurrent.booleanValue();
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

    private void saveUpdate (EditableProperties props) throws IOException {
        this.helper.putPrimaryConfigurationData(getUpdatedSharedConfigurationData(),true);
        if (this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/web-project/1",true) != null) { //NOI18N
            this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/web-project/1",true); //NOI18N
        } else {
            this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/web-project/2",true); //NOI18N
        }
        
        boolean putProps = false;
        
        // AB: fix for #55597: should not update the project without adding the properties
        // update is only done once, so if we don't add the properties now, we don't get another chance to do so
        if (props == null) {
            props = getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            putProps = true;
        }

        //add properties needed by 4.1 project
        if(props != null) {
            props.put("test.src.dir", "test"); //NOI18N
            props.put("build.test.classes.dir", "${build.dir}/test/classes"); //NOI18N
            props.put("build.test.results.dir", "${build.dir}/test/results"); //NOI18N
            props.put("conf.dir","${source.root}/conf"); //NOI18N
            props.put("jspcompilation.classpath", "${jspc.classpath}:${javac.classpath}");
            
            props.setProperty(WebProjectProperties.JAVAC_TEST_CLASSPATH, new String[] {
                "${javac.classpath}:", // NOI18N
                "${build.classes.dir}:", // NOI18N
                "${libs.junit.classpath}:", // NOI18N
                "${libs.junit_4.classpath}", // NOI18N
            });
            props.setProperty(WebProjectProperties.RUN_TEST_CLASSPATH, new String[] {
                "${javac.test.classpath}:", // NOI18N
                "${build.test.classes.dir}", // NOI18N
            });
            props.setProperty(WebProjectProperties.DEBUG_TEST_CLASSPATH, new String[] {
                "${run.test.classpath}", // NOI18N
            });
            
            props.put(WebProjectProperties.WAR_EAR_NAME, props.getProperty(WebProjectProperties.WAR_NAME));
            props.put(WebProjectProperties.DIST_WAR_EAR, "${dist.dir}/${war.ear.name}");
            
            if (props.getProperty(WebProjectProperties.LIBRARIES_DIR) == null) {
                props.setProperty(WebProjectProperties.LIBRARIES_DIR, "${" + WebProjectProperties.WEB_DOCBASE_DIR + "}/WEB-INF/lib"); //NOI18N
            }
        }
        
        if(props != null) {
            //remove jsp20 and servlet24 libraries
            ReferenceHelper refHelper = new ReferenceHelper(helper, cfg, helper.getStandardPropertyEvaluator());
            ClassPathSupport cs = new ClassPathSupport( helper.getStandardPropertyEvaluator(), refHelper, helper, this,
                                        WebProjectProperties.WELL_KNOWN_PATHS, 
                                        WebProjectProperties.ANT_ARTIFACT_PREFIX );        
            Iterator items = cs.itemsIterator((String)props.get( WebProjectProperties.JAVAC_CLASSPATH ), ClassPathSupport.TAG_WEB_MODULE_LIBRARIES);
            ArrayList cpItems = new ArrayList();
            while(items.hasNext()) {
                ClassPathSupport.Item cpti = (ClassPathSupport.Item)items.next();
                String propertyName = cpti.getReference();
                if(propertyName != null) {
                    String libname = propertyName.substring("${libs.".length());
                    if(libname.indexOf(".classpath}") != -1) libname = libname.substring(0, libname.indexOf(".classpath}"));
                    
                    if(!("servlet24".equals(libname) || "jsp20".equals(libname))) { //NOI18N
                        cpItems.add(cpti);
                    }
                }
            }
            String[] javac_cp = cs.encodeToStrings(cpItems.iterator(), ClassPathSupport.TAG_WEB_MODULE_LIBRARIES );
            props.setProperty( WebProjectProperties.JAVAC_CLASSPATH, javac_cp );
        }
        
        if (putProps) {
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        }

        ProjectManager.getDefault().saveProject (this.project);
        synchronized(this) {
            this.isCurrent = Boolean.TRUE;
        } 
        
        //fire project updated
        if(projectUpdateListener != null) projectUpdateListener.projectUpdated();
        
        //create conf dir if doesn't exist and copy default manifest inside
        try {
            //I cannot use ${conf.dir} since the PE doesn't know about it
            //String confDir = helper.getStandardPropertyEvaluator().evaluate("${source.root}/conf"); //NOI18N 
            FileObject prjFO = project.getProjectDirectory();
            // folder creation will throw IOE if already exists
            // use the hard coded string due to issue #54882 - since the 4.0 supports creation of only jakarta structure projects the conf dir is always in project root
            FileObject confDirFO = prjFO.createFolder("conf");//NOI18N 
            // copyfile will throw IOE if the file already exists
            FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-project/MANIFEST.MF"), confDirFO, "MANIFEST"); //NOI18N
        }catch(IOException e) {
            //just ignore
        }
            
    }

    private synchronized Element getUpdatedSharedConfigurationData () {
        if (cachedElement == null) {
            int version = 1;
            Element  oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/web-project/1",true);    //NOI18N
            if (oldRoot == null) {
                version = 2;
                oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/web-project/2",true);    //NOI18N
            }
            final String ns = version == 1 ? "http://www.netbeans.org/ns/web-project/1" : "http://www.netbeans.org/ns/web-project/2"; //NOI18N
            if (oldRoot != null) {
                Document doc = oldRoot.getOwnerDocument();
                Element newRoot = doc.createElementNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"data"); //NOI18N
                copyDocument (doc, oldRoot, newRoot);
                if (version == 1) {
                    //1->2 upgrade
                    Element sourceRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
                    Element root = doc.createElementNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                    root.setAttribute ("id","src.dir");   //NOI18N
                    sourceRoots.appendChild(root);
                    newRoot.appendChild (sourceRoots);
                    Element testRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                    root = doc.createElementNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                    root.setAttribute ("id","test.src.dir");   //NOI18N
                    testRoots.appendChild (root);
                    newRoot.appendChild (testRoots);
                }
                if (version == 1 || version == 2) {
                    //2->3 upgrade
                    NodeList libList = newRoot.getElementsByTagNameNS(ns, TAG_LIBRARY);
                    for (int i = 0; i < libList.getLength(); i++) {
                        if (libList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element library = (Element) libList.item(i);
                            Node webFile = library.getElementsByTagNameNS(ns, TAG_FILE).item(0);
                            //remove ${ and } from the beginning and end
                            String webFileText = findText(webFile);
                            webFileText = webFileText.substring(2, webFileText.length() - 1);
//                            warIncludesMap.put(webFileText, pathInWarElements.getLength() > 0 ? findText((Element) pathInWarElements.item(0)) : Item.PATH_IN_WAR_NONE);
                            if (webFileText.startsWith ("lib.")) {
                                String libName = webFileText.substring(6, webFileText.indexOf(".classpath")); //NOI18N
                                List<URL> roots = LibraryManager.getDefault().getLibrary(libName).getContent("classpath"); //NOI18N
                                ArrayList<FileObject> files = new ArrayList<FileObject>();
                                ArrayList<FileObject> dirs = new ArrayList<FileObject>();
                                for (URL rootUrl : roots) {
                                    FileObject root = URLMapper.findFileObject (rootUrl);
                                    if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                                        root = FileUtil.getArchiveFile (root);
                                    }
                                    if (root != null) {
                                        if (root.isData()) {
                                            files.add(root);
                                        } else {
                                            dirs.add(root);
                                        }
                                    }
                                }
                                if (files.size() > 0) {
                                    library.setAttribute(ATTR_FILES, "" + files.size());
                                }
                                if (dirs.size() > 0) {
                                    library.setAttribute(ATTR_DIRS, "" + dirs.size());
                                }
                            }
                        }
                    }
                }
                cachedElement = updateMinAntVersion(newRoot, doc);
            }
        }
        return cachedElement;
    }

    private synchronized EditableProperties getUpdatedProjectProperties () {
        if (cachedProperties == null) {
            cachedProperties = this.helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            //The javadoc.additionalparam was not in NB 4.0
            if (cachedProperties.get (WebProjectProperties.JAVADOC_ADDITIONALPARAM)==null) {
                cachedProperties.put (WebProjectProperties.JAVADOC_ADDITIONALPARAM,"");    //NOI18N
            }
        }
        return this.cachedProperties;
    }
    
    private static void copyDocument (Document doc, Element from, Element to) {
        NodeList nl = from.getChildNodes();
        int length = nl.getLength();
        for (int i=0; i< length; i++) {
            Node node = nl.item (i);
            Node newNode = null;
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element oldElement = (Element) node;
                    newNode = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,oldElement.getTagName());
                    NamedNodeMap m = oldElement.getAttributes();
                    Element newElement = (Element) newNode;
                    for (int index = 0; index < m.getLength(); index++) {
                        Node attr = m.item(index);
                        newElement.setAttribute(attr.getNodeName(), attr.getNodeValue());
                    }
                    copyDocument(doc,oldElement,(Element)newNode);
                    break;
                case Node.TEXT_NODE:
                    Text oldText = (Text) node;
                    newNode = doc.createTextNode(oldText.getData());
                    break;
                case Node.COMMENT_NODE:
                    Comment oldComment = (Comment) node;
                    newNode = doc.createComment(oldComment.getData());
                    break;
            }
            if (newNode != null) {
                to.appendChild (newNode);
            }
        }
    }
    
    private static Element updateMinAntVersion (final Element root, final Document doc) {
        NodeList list = root.getElementsByTagNameNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,TAG_MINIMUM_ANT_VERSION);
        if (list.getLength() == 1) {
            Element me = (Element) list.item(0);
            list = me.getChildNodes();
            if (list.getLength() == 1) {
                me.replaceChild (doc.createTextNode(WebProjectUtilities.MINIMUM_ANT_VERSION), list.item(0));
                return root;
            }
        }
        assert false : "Invalid project file"; //NOI18N
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
     * Extract nested text from a node.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent node
     * @return the nested text, or null if none was found
     */
    private static String findText(Node parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
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
    
    private ProjectUpdateListener projectUpdateListener = null;
    
    public void setProjectUpdateListener(ProjectUpdateListener l) {
        this.projectUpdateListener = l;
    }
    
    /** Used to notify someone that the project needs to be updated. 
     * A workaround for #54077 - Import 4.0 project - remove Servlet/JSP APIs */
    public static interface ProjectUpdateListener {
        public void projectUpdated();
    }
    
}
