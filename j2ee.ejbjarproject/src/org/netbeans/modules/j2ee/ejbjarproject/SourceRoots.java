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
package org.netbeans.modules.j2ee.ejbjarproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.Mutex;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.openide.util.Exceptions;

/**
 * This class represents a project source roots. It is used to obtain roots as Ant properties, FileObject's
 * or URLs.
 * @author Tomas Zezula
 */
public final class SourceRoots {

    public static final String PROP_ROOT_PROPERTIES = "rootProperties";    //NOI18N
    public static final String PROP_ROOTS = "roots";   //NOI18N
    private static final String PROP_BUILD_DIR = "build.dir";   //NOI18N

    public static final String DEFAULT_SOURCE_LABEL = NbBundle.getMessage(SourceRoots.class, "NAME_src.dir"); //NOI18N
    public static final String DEFAULT_TEST_LABEL = NbBundle.getMessage(SourceRoots.class, "NAME_test.src.dir"); //NOI18N

    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    private final String elementName;
    private final String newRootNameTemplate;
    private List<String> sourceRootProperties;
    private List<String> sourceRootNames;
    private List<FileObject> sourceRoots;
    private List<URL> sourceRootURLs;
    private final PropertyChangeSupport support;
    private final ProjectMetadataListener listener;
    private final boolean isTest;
    private final File projectDir;

    /**
     * Creates new SourceRoots
     * @param helper
     * @param evaluator
     * @param elementName the name of XML element under which are declared the roots
     * @param newRootNameTemplate template for new property name of source root
     */
    SourceRoots (UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper refHelper, String elementName, boolean isTest, String newRootNameTemplate) {
        assert helper != null && evaluator != null && refHelper != null && elementName != null && newRootNameTemplate != null;
        this.helper = helper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.elementName = elementName;
        this.isTest = isTest;
        this.newRootNameTemplate = newRootNameTemplate;        
        this.projectDir = FileUtil.toFile(this.helper.getAntProjectHelper().getProjectDirectory());
        this.support = new PropertyChangeSupport(this);
        this.listener = new ProjectMetadataListener();
        this.evaluator.addPropertyChangeListener (WeakListeners.propertyChange(this.listener,this.evaluator));
        this.helper.getAntProjectHelper().addAntProjectListener(WeakListeners.create(AntProjectListener.class, this.listener, this.helper));
    }


    /**
     * Returns the display names of soruce roots
     * The returned array has the same length as an array returned by the getRootProperties.
     * It may contain empty strings but not null.
     * @return an array of String
     */
    public   String[] getRootNames () {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String[]>() {
            public String[] run() {
                synchronized (SourceRoots.this) {
                    if (sourceRootNames == null) {
                        readProjectMetadata();
                    }
                }
                return sourceRootNames.toArray(new String[sourceRootNames.size()]);
            }
        });
    }

    /**
     * Returns names of Ant properties in the project.properties file holding the source roots.
     * @return an array of String
     */
    public String[] getRootProperties () {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String[]>() {
            public String[] run() {
                synchronized (SourceRoots.this) {
                    if (sourceRootProperties == null) {
                        readProjectMetadata();
                    }
                }
                return sourceRootProperties.toArray (new String[sourceRootProperties.size()]);
            }
        });
    }

    /**
     * Returns the source roots
     * @return an array of FileObject
     */
    public FileObject[] getRoots () {
        return ProjectManager.mutex().readAccess(new Mutex.Action<FileObject[]>() {
                public FileObject[] run () {
                    synchronized (this) {
                        //Local caching
                        if (sourceRoots == null) {
                            String[] srcProps = getRootProperties();
                            List<FileObject> result = new ArrayList<FileObject>();
                            for (int i = 0; i<srcProps.length; i++) {
                                String prop = evaluator.getProperty(srcProps[i]);
                                if (prop != null) {
                                    FileObject f = helper.getAntProjectHelper().resolveFileObject(prop);
                                    if (f == null) {
                                        continue;
                                    }
                                    if (FileUtil.isArchiveFile(f)) {
                                        f = FileUtil.getArchiveRoot(f);
                                    }
                                    result.add(f);
                                }
                            }
                            
                            String buildDir = evaluator.getProperty(PROP_BUILD_DIR);
                            if (buildDir != null) {
                                try {
                                    // generated/wsimport/client
                                    File f =  new File (helper.getAntProjectHelper().resolveFile (buildDir),"generated/wsimport/client"); //NOI18N
                                    URL url = f.toURI().toURL();
                                    if (!f.exists()) {  //NOI18N
                                        assert !url.toExternalForm().endsWith("/");  //NOI18N
                                        url = new URL (url.toExternalForm()+'/');   //NOI18N
                                    }
                                    FileObject root = URLMapper.findFileObject(url);
                                    if (root != null) {
                                        result.add(root);
                                    }
                                    // generated/wsimport/service
                                    f = new File (helper.getAntProjectHelper().resolveFile(buildDir),"generated/wsimport/service"); //NOI18N
                                    url = f.toURI().toURL();
                                    if (!f.exists()) {  //NOI18N
                                        assert !url.toExternalForm().endsWith("/");  //NOI18N
                                        url = new URL (url.toExternalForm()+'/');   //NOI18N
                                    }
                                    root = URLMapper.findFileObject(url);
                                    if (root != null) {
                                        result.add(root);
                                    }
                                } catch (MalformedURLException ex) {
                                     Logger.getLogger("global").log(Level.INFO, null, ex);
                                }
                            }
                            sourceRoots = Collections.unmodifiableList(result);
                        }
                    }
                    return sourceRoots.toArray(new FileObject[sourceRoots.size()]);
                }
        });                
    }

    /**
     * Returns the source roots as URLs.
     * @return an array of URL
     */
    public URL[] getRootURLs() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<URL[]>() {
            public URL[] run () {
                synchronized (this) {
                    //Local caching
                    if (sourceRootURLs == null) {
                        String[] srcProps = getRootProperties();
                        List<URL> result = new ArrayList<URL>();
                        for (int i = 0; i<srcProps.length; i++) {
                            String prop = evaluator.getProperty(srcProps[i]);
                            if (prop != null) {
                                File f = helper.getAntProjectHelper().resolveFile(prop);
                                try {
                                    result.add(EjbJarProjectUtil.getRootURL(f,null));
                                } catch (MalformedURLException e) {
                                    Exceptions.printStackTrace(e);
                                }
                            }
                        }
                        sourceRootURLs = Collections.<URL>unmodifiableList(result);
                    }
                }
                return sourceRootURLs.toArray(new URL[sourceRootURLs.size()]);
            }
        });
    }

    /**
     * Adds PropertyChangeListener
     * @param listener
     */
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }


    /**
     * Replaces the current roots by the new ones
     * @param roots the URLs of new roots
     * @param labels the names of roots
     */
    public void putRoots (final URL[] roots, final String[] labels) {
        ProjectManager.mutex().writeAccess(
                new Runnable() {
                    public void run() {
                        String[] originalProps = getRootProperties();
                        URL[] originalRoots = getRootURLs();
                        Map<URL, String> oldRoots2props = new HashMap<URL, String>();
                        for (int i=0; i<originalProps.length;i++) {
                            oldRoots2props.put (originalRoots[i],originalProps[i]);
                        }
                        Map<URL, String> newRoots2lab = new HashMap<URL, String>();
                        for (int i=0; i<roots.length;i++) {
                            newRoots2lab.put (roots[i],labels[i]);
                        }
                        Element cfgEl = helper.getPrimaryConfigurationData(true);
                        NodeList nl = cfgEl.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, elementName);
                        assert nl.getLength() == 1 : "Illegal project.xml"; //NOI18N
                        Element ownerElement = (Element) nl.item(0);
                        //Remove all old roots
                        NodeList rootsNodes = ownerElement.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");    //NOI18N
                        while (rootsNodes.getLength()>0) {
                            Element root = (Element) rootsNodes.item(0);
                            ownerElement.removeChild(root);
                        }
                        //Remove all unused root properties
                        List newRoots = Arrays.asList(roots);
                        Map<URL, String> propsToRemove = new HashMap<URL, String>(oldRoots2props);
                        propsToRemove.keySet().removeAll(newRoots);
                        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        for (Iterator it = propsToRemove.values().iterator(); it.hasNext();) {
                            String propName = (String) it.next ();
                            props.remove(propName);
                        }
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
                        //Add the new roots
                        Document doc = ownerElement.getOwnerDocument();
                        oldRoots2props.keySet().retainAll(newRoots);
                        for (Iterator it = newRoots.iterator(); it.hasNext();) {
                            URL newRoot = (URL) it.next ();
                            String rootName = oldRoots2props.get(newRoot);
                            if (rootName == null) {
                                //Root is new generate property for it
                                props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                String[] names = newRoot.getPath().split("/");  //NOI18N
                                rootName = MessageFormat.format(newRootNameTemplate,new Object[]{names[names.length-1],""});    //NOI18N
                                int rootIndex = 1;
                                while (props.containsKey(rootName)) {
                                    rootIndex++;
                                    rootName = MessageFormat.format(newRootNameTemplate,new Object[]{names[names.length-1],new Integer(rootIndex)});
                                }
                                File f = FileUtil.normalizeFile(new File(URI.create(newRoot.toExternalForm())));
                                File projDir = FileUtil.toFile(helper.getAntProjectHelper().getProjectDirectory());
                                String path = f.getAbsolutePath();
                                String prjPath = projDir.getAbsolutePath()+File.separatorChar;
                                if (path.startsWith(prjPath)) {
                                    path = path.substring(prjPath.length());
                                }
                                else {
                                    path = refHelper.createForeignFileReference(f, JavaProjectConstants.SOURCES_TYPE_JAVA);
                                    props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                }
                                props.put(rootName,path);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
                            }
                            Element newRootNode = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); //NOI18N
                            newRootNode.setAttribute("id",rootName);    //NOI18N
                            String label = newRoots2lab.get(newRoot);
                            if (label != null && label.length()>0 && !label.equals (getRootDisplayName(null,rootName))) { //NOI18N
                                newRootNode.setAttribute("name",label); //NOI18N
                            }
                            ownerElement.appendChild (newRootNode);
                        }
                        helper.putPrimaryConfigurationData(cfgEl,true);
                    }
                }
        );
    }

    /**
     * Translates root name into display name of source/test root
     * @param rootName the name of root got from {@link SourceRoots#getRootNames}
     * @param propName the name of property the root is stored in
     * @return the label to be displayed
     */
    public String getRootDisplayName (String rootName, String propName) {
        if (rootName == null || rootName.length() ==0) {
            //If the prop is src.dir use the default name
            if (isTest && "test.src.dir".equals(propName)) {    //NOI18N
                rootName = DEFAULT_TEST_LABEL;
            }
            else if (!isTest && "src.dir".equals(propName)) {   //NOI18N
                rootName = DEFAULT_SOURCE_LABEL;
            }
            else {
                //If the name is not given, it should be either a relative path in the project dir
                //or absolute path when the root is not under the project dir
                String propValue = evaluator.getProperty(propName);
                File sourceRoot = propValue == null ? null : helper.getAntProjectHelper().resolveFile(propValue);
                rootName = createInitialDisplayName(sourceRoot);                
            }
        }
        return rootName;
    }
    
    /**
     * Creates initial display name of source/test root
     * @param sourceRoot the source root
     * @return the label to be displayed
     */
    public String createInitialDisplayName (File sourceRoot) {
        String rootName;
        if (sourceRoot != null) {
            String srPath = sourceRoot.getAbsolutePath();
            String pdPath = projectDir.getAbsolutePath() + File.separatorChar;
            if (srPath.startsWith(pdPath)) {
                rootName = srPath.substring(pdPath.length());
            }
            else {
                rootName = sourceRoot.getAbsolutePath();
            }
        }
        else {
            rootName = isTest ? DEFAULT_TEST_LABEL : DEFAULT_SOURCE_LABEL;
        }
        return rootName;
    }

    private void resetCache (boolean isXMLChange, String propName) {
        boolean fire = false;
        synchronized (this) {
            //In case of change reset local cache
            if (isXMLChange) {
                this.sourceRootProperties = null;
                this.sourceRootNames = null;
                this.sourceRoots = null;
                this.sourceRootURLs = null;
                fire = true;
            } else if (propName == null || (sourceRootProperties != null && sourceRootProperties.contains(propName))) {
                this.sourceRoots = null;
                this.sourceRootURLs = null;
                fire = true;
            }
        }
        if (fire) {
            if (isXMLChange) {
                this.support.firePropertyChange (PROP_ROOT_PROPERTIES,null,null);
            }
            this.support.firePropertyChange (PROP_ROOTS,null,null);
        }
    }

    private void readProjectMetadata () {
        Element cfgEl = helper.getPrimaryConfigurationData(true);
        NodeList nl = cfgEl.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, elementName);
        assert nl.getLength() == 0 || nl.getLength() == 1 : "Illegal project.xml"; //NOI18N
        List<String> rootProps = new ArrayList<String>();
        List<String> rootNames = new ArrayList<String>();
        // It can be 0 in the case when the project is created by EjbJarProjectGenerator and not yet customized
        if (nl.getLength()==1) {
            NodeList roots = ((Element)nl.item(0)).getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");    //NOI18N
            for (int i=0; i<roots.getLength(); i++) {
                Element root = (Element) roots.item(i);
                String value = root.getAttribute("id");  //NOI18N
                assert value.length() > 0 : "Illegal project.xml";
                rootProps.add(value);
                value = root.getAttribute("name");  //NOI18N
                rootNames.add (value);
            }
        }
        this.sourceRootProperties = Collections.<String>unmodifiableList(rootProps);
        this.sourceRootNames = Collections.<String>unmodifiableList(rootNames);
    }

    private class ProjectMetadataListener implements PropertyChangeListener,AntProjectListener {

        public void propertyChange(PropertyChangeEvent evt) {
            resetCache (false,evt.getPropertyName());
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            resetCache (true,null);
        }

        public void propertiesChanged(AntProjectEvent ev) {
            //Handled by propertyChange
        }
    }

    public boolean isTest() {
        return isTest;
    }
}
