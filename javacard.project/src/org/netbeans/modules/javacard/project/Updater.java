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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.w3c.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.Dependency;
import org.netbeans.modules.javacard.project.deps.DependencyKind;
import org.netbeans.modules.javacard.project.deps.DeploymentStrategy;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tim Boudreau
 */
public class Updater implements UpdateImplementation {

    private final JCProject project;
    private final AntProjectHelper helper;
    private final AuxiliaryConfiguration config;
    public static final String NAMESPACE_V1 = "http://www.netbeans.org/ns/javacard-project/1"; //NOI18N
    public static final String NAMESPACE_V2 = "http://www.netbeans.org/ns/javacard-project/2"; //NOI18N
    public static final String NAMESPACE_V3 = "http://www.netbeans.org/ns/javacard-project/3"; //NOI18N
    private volatile int namespaceVersion;

    //do not make static, this value should not ever get inlined - at some point
    //it may be used by other classes, and should not be inlined as a constant
    private static int CURRENT_VERSION = 3; 

    public Updater(JCProject p, AntProjectHelper helper, AuxiliaryConfiguration config) {
        this.project = p;
        this.helper = helper;
        this.config = config;
    }

    public boolean isCurrent() {
        boolean result;
        if (namespaceVersion > 0) {
            result = namespaceVersion >= CURRENT_VERSION;
        } else {
            result = ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {

                public Boolean run() {
                    if (config.getConfigurationFragment("data", //NOI18N
                            NAMESPACE_V3, true) != null) {
                        namespaceVersion = 3;
                    } else if (config.getConfigurationFragment("data", //NOI18N
                            NAMESPACE_V2, true) != null) {
                        namespaceVersion = 2;
                    } else if (config.getConfigurationFragment("data", //NOI18N
                            NAMESPACE_V1, true) != null) {
                        namespaceVersion = 1;
                    }
                    return namespaceVersion < CURRENT_VERSION;
                }
            }).booleanValue();
        }
        return result;
    }

    public boolean canUpdate() {
        return !isCurrent();
    }

    public void save() throws IOException {
        saveUpdate(null);
        ProjectManager.getDefault().saveProject(project);
    }

    public void saveUpdate(EditableProperties props) throws IOException {
        if (!isCurrent()) {
            //The old sample projects in the RI have out of sync GenFiles.properties.
            //Brutally force a rewrite of the build-impl.xml by deleting the old one
            FileObject oldBuildImpl = project.getProjectDirectory().getFileObject("nbproject/build-impl.xml"); //NOI18N
            if (oldBuildImpl  != null) {
                oldBuildImpl.delete();
            }
            EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            //find user.build.properties
            File userBuildProperties = userBuildProperties();
            if (userBuildProperties != null) { //unit test
                String userBuildPropertiesPath = userBuildProperties.getPath();
                if (Utilities.isWindows()) {
                    userBuildPropertiesPath = userBuildPropertiesPath.replace('\\', '/'); //NOI18N
                }
                privateProps.put(ProjectPropertyNames.PROJECT_PROP_USER_PROPERTIES_FILE,
                        userBuildPropertiesPath);
            }
            this.helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
            this.helper.putPrimaryConfigurationData(getUpdatedSharedConfigurationData(), true);
            this.config.removeConfigurationFragment("data", NAMESPACE_V1, true); //NOI18N
            this.config.removeConfigurationFragment("data", NAMESPACE_V2, true); //NOI18N
            EditableProperties updatedProperties = getUpdatedProjectProperties();
            this.helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, updatedProperties);
            namespaceVersion = CURRENT_VERSION;
            ProjectManager.getDefault().saveProject(project);
        }
    }

    private static File userBuildProperties() {
        String nbuser = System.getProperty("netbeans.user"); // NOI18N
        if (nbuser != null) {
            return FileUtil.normalizeFile(new File(nbuser, "build.properties")); // NOI18N
        } else {
            return null;
        }
    }
    
    public synchronized Element getUpdatedSharedConfigurationData() {
        Element result = null;
        if (namespaceVersion == 0 || namespaceVersion == 1) {
            result = updateToV2();
        }
        if (namespaceVersion == 2) {
            result = result == null ? this.config.getConfigurationFragment("data", NAMESPACE_V2, true) : result;    //NOI18N
            result = updateToV3(result);
        }
        return result;
    }

    Element updateToV2() {
        Element oldRoot = this.config.getConfigurationFragment("data", NAMESPACE_V1, true);    //NOI18N
        if (oldRoot != null) {
            Document doc = oldRoot.getOwnerDocument();
            Element newRoot = doc.createElementNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, "data"); //NOI18N
            Element minAntVersion = doc.createElement("minimum-ant-version"); //NOI18N
            minAntVersion.setTextContent(JCProjectType.MINIMUM_ANT_VERSION);
            copyDocument(doc, oldRoot, newRoot);
            Element sourceRoots = doc.createElementNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots");  //NOI18N
            Element root = doc.createElementNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");   //NOI18N
            root.setAttribute("id", "src.dir");   //NOI18N
            sourceRoots.appendChild(root);
            newRoot.appendChild(sourceRoots);
            namespaceVersion = 2;
            return newRoot;
        }
        return null;
    }

    Element updateToV3(Element element) {
        if (element == null) {
            element = this.config.getConfigurationFragment("data", NAMESPACE_V2, true);    //NOI18N
        }
        Document doc = element.getOwnerDocument();
        Element newRoot = doc.createElementNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, "data"); //NOI18N
        copyDocument(doc, element, newRoot);
        Element dependencies = doc.createElementNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, "dependencies"); //NOI18N
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String cp = props.getProperty(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH);
        try {
            if (cp != null && !"".equals(cp.trim())) { //NOI18N
                ResolvedDependencies deps = project.createResolvedDependencies();
                String[] paths = cp.split(File.pathSeparator);
                for (int i = 0; i < paths.length; i++) {
                    String path = paths[i];
                    File f = FileUtil.normalizeFile(new File(path));
                    if (f != null && f.exists()) {
                        Map<ArtifactKind, String> m = new HashMap<ArtifactKind, String>();
                        m.put(ArtifactKind.ORIGIN, f.getAbsolutePath());
                        Dependency d = new Dependency("lib" + (i + 1), //NOI18N
                                DependencyKind.RAW_JAR, DeploymentStrategy.ALREADY_ON_CARD);
                        deps.add(d, m);
                    }
                }
                if (!deps.all().isEmpty()) {
                    deps.save();
                }
                props.remove(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH);
            }
            newRoot.appendChild(dependencies);
        } catch (Exception e) {
            throw new IllegalStateException("Project metadata corrupted", e); //NOI18N
        }
        element = newRoot;
        return element;
    }


    private static void copyDocument(Document doc, Element from, Element to) {
        NodeList nl = from.getChildNodes();
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nl.item(i);
            Node newNode = null;
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element oldElement = (Element) node;
                    if ("folders".equals(oldElement.getNodeName())) { //NOI18N
                        //We will rewrite the folders node later
                        continue;
                    }
                    if ("view".equals(oldElement.getNodeName())) { //NOI18N
                        //Discard this node, it is not useful
                        continue;
                    }
                    if ("property".equals(oldElement.getNodeName())) {
                        String name = oldElement.getAttribute("name"); //NOI18N
                        if ("ant.script".equals(name)) { //NOI18N
                            continue;
                        }
                    }
                    newNode = doc.createElementNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, oldElement.getTagName());
                    NamedNodeMap m = oldElement.getAttributes();
                    Element newElement = (Element) newNode;
                    for (int index = 0; index < m.getLength(); index++) {
                        Node attr = m.item(index);
                        newElement.setAttribute(attr.getNodeName(), attr.getNodeValue());
                    }
                    copyDocument(doc, oldElement, newElement);
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
                to.appendChild(newNode);
            }
        }
    }

    public EditableProperties getUpdatedProjectProperties() {
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties result = new EditableProperties(true);
        Replacement[] changes = replacements();
        String displayName = project.getLookup().lookup(ProjectInformation.class).getDisplayName();
        for (Map.Entry<String, String> entry : props.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            for (Replacement r : changes) {
                key = r.replaceKey(key);
                val = r.filterValue(val);
            }
            if (!"display.name".equals(key)) { //NOI18N
                val = val.replace(displayName, "${display.name}"); //NOI18N
            }
            if (!ProjectPropertyNames.PROJECT_PROP_DIST_JAR.equals(key) && val.startsWith("${basedir}/")) { //NOI18N
                val = val.substring ("${basedir}/".length()); //NOI18N
            }
            if ("dist.jar.name".equals(key)) { //NOI18N
                continue;
            }
            if ("dest.eeprom".equals(key)) { //NOI18N
                continue;
            }
            if ("dest.sig".equals(key)) { //NOI18N
                continue;
            }
            if ("dest.sig.name".equals(key)) { //NOI18N
                continue;
            }
            result.setProperty(key, val);
        }
        result.setProperty ("dist.bundle.name", "${display.name}." + project.kind().getBundleFileExtension());//NOI18N
        result.setProperty ("dist.bundle.sig.name", "${display.name}.signature");//NOI18N
        result.setProperty ("dist.bundle.sig", "${dist.dir}/${dist.bundle.sig.name}");//NOI18N
        result.setProperty ("dist.bundle", "${dist.dir}/${dist.bundle.name}");//NOI18N
        result.setProperty ("meta.inf.dir", "META-INF");//NOI18N
        if (project.kind().isApplet()) {
            result.setProperty ("applet.inf.dir", "APPLET-INF");//NOI18N
            result.setProperty ("scripts.dir", "scripts");
        }
        
        result.remove ("runtime.descriptor"); //NOI18N
        result.remove ("jcap.descriptor"); //NOI18N
        result.remove ("appletdescriptor"); //NOI18N
        result.remove ("web.inf.dir"); //NOI18N
        result.remove ("main.script"); //NOI18N
        result.remove ("mainscript"); //NOI18N
        result.remove ("dest.eeprom.name"); //NOI18N
        result.remove("application.free.form.name"); //NOI18N
        if (project.kind() != ProjectKind.WEB) {
            result.remove ("launch.external.browser");//NOI18N
        }

        // looks like the following properties are not in template files
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE, JCConstants.TEMPLATE_DEFAULT_DEVICE_NAME);
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM, JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME);
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_SRC_DIR, "src"); //NOI18N
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_JAVAC_SOURCE, "1.6"); //NOI18N
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_JAVAC_TARGET, "1.6"); //NOI18N
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_SOURCE_ENCODING, "UTF-8");
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_BUILD_DIR, "build"); //NOI18N
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_BUILD_SCRIPT, "build.xml"); //NOI18N
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_JAVAC_DEPRECATION, "true"); //NOI18N
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_JAVAC_ADDITIONAL_ARGS,""); //NOI18N
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_COMPILE_ON_SAVE, "false"); //NOI18N
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_JAVAC_DEBUG, "false"); //NOI18N
        result.setProperty ("compile.on.save", "false");
        result.setProperty ("javac.args", "");
        result.setProperty ("javac.debug", "true");
//        added to the template files
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_KEYSTORE_PATH,
                "${javacard.home}/samples/keystore/a.keystore"); //NOI18N
        result.setProperty(ProjectPropertyNames.PROJECT_PROP_SIGN_JAR, "true"); //NOI18N

        return result;
    }

    private static Replacement[] replacements() {
        Replacement[] result = new Replacement[]{
            new Replacement("dest.jar", "dist.jar"), //NOI18N
            new Replacement("main.script", "mainscript"), //NOI18N
            new Replacement("dest.war", "dist.jar"), //NOI18N
            new Replacement("dest.eap", "dist.jar"), //NOI18N
            new Replacement("dest.dir", "dist.dir"), //NOI18N
            new Replacement("dest.war.name", "dist.jar.name"), //NOI18N
            new Replacement("dest.cap", "dist.jar"), //NOI18N
            new Replacement("dest.cap.name", "dist.jar.name"), //NOI18N
            new Replacement("dest.eap.name", "dist.jar.name"), //NOI18N
        };
        return result;
    }

    private static final class Replacement {

        public final String oldKey;
        public final String newKey;

        public Replacement(String oldKey, String newKey) {
            this.oldKey = oldKey;
            this.newKey = newKey;
        }

        public String replaceKey(String val) {
            return oldKey.equals(val) ? newKey : val;
        }

        public String filterValue(String value) {
            String lookFor = "${" + oldKey + "}"; //NOI18N
            return value.replace(lookFor, "${" + newKey + "}"); //NOI18N
        }
    }
}
