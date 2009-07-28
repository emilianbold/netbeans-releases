/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;
import org.netbeans.spi.java.project.runner.JavaRunnerImplementation;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.windows.InputOutput;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import static org.netbeans.api.java.project.runner.JavaRunner.*;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.project.runner.JavaRunnerImplementation.class)
public class ProjectRunnerImpl implements JavaRunnerImplementation {

    private static final Logger LOG = Logger.getLogger(ProjectRunnerImpl.class.getName());
    
    public boolean isSupported(String command, Map<String, ?> properties) {
        return locateScript(command) != null;
    }

    public ExecutorTask execute(String command, Map<String, ?> properties) throws IOException {
        if (QUICK_CLEAN.equals(command)) {
            return clean(properties);
        }
        
        String[] projectName = new String[1];
        Properties antProps = computeProperties(command, properties, projectName);
        
        FileObject script = buildScript(command);
        AntProjectCookie apc = new FakeAntProjectCookie(AntScriptUtils.antProjectCookieFor(script), projectName[0]);
        AntTargetExecutor.Env execenv = new AntTargetExecutor.Env();
        Properties props = execenv.getProperties();
        props.putAll(antProps);
        execenv.setProperties(props);

        return AntTargetExecutor.createTargetExecutor(execenv).execute(apc, null);
    }

    static Properties computeProperties(String command, Map<String, ?> properties, String[] projectNameOut) {
        properties = new HashMap<String, Object>(properties);
        FileObject toRun = getValue(properties, PROP_EXECUTE_FILE, FileObject.class);
        String workDir = getValue(properties, PROP_WORK_DIR, String.class);
        String className = getValue(properties, PROP_CLASSNAME, String.class);
        ClassPath boot = getValue(properties, "boot.classpath", ClassPath.class);
        ClassPath exec = getValue(properties, PROP_EXECUTE_CLASSPATH, ClassPath.class);
        String javaTool = getValue(properties, PROP_PLATFORM_JAVA, String.class);
        String projectName = getValue(properties, PROP_PROJECT_NAME, String.class);
        Iterable<String> runJVMArgs = getMultiValue(properties, PROP_RUN_JVMARGS, String.class);
        Iterable<String> args = getMultiValue(properties, PROP_APPLICATION_ARGS, String.class);
        if (workDir == null) {
            Parameters.notNull("toRun", toRun);
            Project project = FileOwnerQuery.getOwner(toRun);
            if (project != null) {
                //NOI18N
                FileObject projDirectory = project.getProjectDirectory();
                assert projDirectory != null;
                File file = FileUtil.toFile(projDirectory);
                if (file != null) {
                    workDir = file.getAbsolutePath(); //NOI18N
                }
            }
        }
        if (className == null) {
            Parameters.notNull("toRun", toRun);
            ClassPath source = ClassPath.getClassPath(toRun, ClassPath.SOURCE);
            if (source == null) {
                throw new IllegalArgumentException("The source classpath for specified toRun parameter has is null. " +
                        "Report against caller module. [toRun = " + toRun + "]");
            }
            className = source.getResourceName(toRun, '.', false);
        }
        if (exec == null) {
            Parameters.notNull("toRun", toRun);
            exec = ClassPath.getClassPath(toRun, ClassPath.EXECUTE);
        }
        JavaPlatform p = getValue(properties, PROP_PLATFORM, JavaPlatform.class);

        if (p == null) {
            p = JavaPlatform.getDefault();
        }
        if (javaTool == null) {
            
            FileObject javaToolFO = p.findTool("java");

            if (javaToolFO == null) {
                IllegalArgumentException iae = new IllegalArgumentException("Cannot find java");

                Exceptions.attachLocalizedMessage(iae, NbBundle.getMessage(ProjectRunnerImpl.class, "ERR_CannotFindJava"));
                throw iae;
            }
            
            javaTool = FileUtil.toFile(javaToolFO).getAbsolutePath();
        }
        if (boot == null) {
            boot = p.getBootstrapLibraries();
        }
        if (projectName == null) {
            Project project = getValue(properties, "project", Project.class);
            if (project != null) {
                projectName = ProjectUtils.getInformation(project).getDisplayName();
            }
            if (projectName == null && toRun != null) {
                project = FileOwnerQuery.getOwner(toRun);
                if (project != null) {
                    //NOI18N
                    projectName = ProjectUtils.getInformation(project).getDisplayName();
                }
            }
            if (projectName == null) {
                projectName = "";
            }
        }

        LOG.log(Level.FINE, "execute classpath={0}", exec);
        String cp = exec.toString(ClassPath.PathConversionMode.FAIL);
        Properties antProps = new Properties();
        setProperty(antProps, "platform.bootcp", boot.toString(ClassPath.PathConversionMode.FAIL));
        setProperty(antProps, "classpath", cp);
        setProperty(antProps, "classname", className);
        setProperty(antProps, "platform.java", javaTool);
        setProperty(antProps, "work.dir", workDir);
        setProperty(antProps, "run.jvmargs", toOneLine(runJVMArgs));
        if (toRun == null) {
            // #152881 - pass arguments only if not run single
            setProperty(antProps, "application.args", toOneLine(args));
        }
        {
            FileObject source = toRun;
            if (source == null) {
                String binaryResource = className.replace('.', '/') + ".class";
                for (FileObject root : exec.getRoots()) {
                    if (root.getFileObject(binaryResource) != null) {
                        try {
                            String sourceResource = className.replace('.', '/') + ".java";
                            for (FileObject srcRoot : SourceForBinaryQuery.findSourceRoots(root.getURL()).getRoots()) {
                                FileObject srcFile = srcRoot.getFileObject(sourceResource);
                                if (srcFile != null) {
                                    source = srcFile;
                                    break;
                                }
                            }
                        } catch (FileStateInvalidException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    break;
                }
            }
            String encoding = "UTF-8";
            if (source != null) {
                Charset sourceEncoding = FileEncodingQuery.getEncoding(source);
                if (Charset.isSupported(sourceEncoding.name())) {
                    encoding = sourceEncoding.name();
                }
            }
            setProperty(antProps, "encoding", encoding);
        }

        for (Entry<String, ?> e : properties.entrySet()) {
            if (e.getValue() instanceof String) {
                antProps.setProperty(e.getKey(), (String) e.getValue());
            }
        }
        
        projectNameOut[0] = projectName;
        
        return antProps;
    }

    private static ExecutorTask clean(Map<String, ?> properties) {
        properties = new HashMap<String, Object>(properties);
        String projectName = getValue(properties, PROP_PROJECT_NAME, String.class);
        FileObject toRun = getValue(properties, PROP_EXECUTE_FILE, FileObject.class);
        ClassPath exec = getValue(properties, PROP_EXECUTE_CLASSPATH, ClassPath.class);

        if (exec == null) {
            Parameters.notNull("toRun", toRun);
            exec = ClassPath.getClassPath(toRun, ClassPath.EXECUTE);
        }

        if (projectName == null) {
            Project project = getValue(properties, "project", Project.class);
            if (project != null) {
                projectName = ProjectUtils.getInformation(project).getDisplayName();
            }
            if (projectName == null && toRun != null) {
                project = FileOwnerQuery.getOwner(toRun);
                if (project != null) {
                    //NOI18N
                    projectName = ProjectUtils.getInformation(project).getDisplayName();
                }
            }
            if (projectName == null) {
                projectName = "";
            }
        }
        
        LOG.log(Level.FINE, "execute classpath={0}", exec);

        final ClassPath execFin = exec;

        return ExecutionEngine.getDefault().execute(projectName, new Runnable() {
            public void run() {
                try {
                    doClean(execFin);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, InputOutput.NULL);
    }

    private static void setProperty(Properties antProps, String property, String value) {
        if (value != null) {
            antProps.setProperty(property, value);
        }
    }

    private static <T> T getValue(Map<String, ?> properties, String name, Class<T> type) {
        Object v = properties.remove(name);

        if (v instanceof FileObject && type == String.class) {
            FileObject f = (FileObject) v;
            File file = FileUtil.toFile(f);

            if (file == null) {
                return null;
            }
            
            v = file.getAbsolutePath();
        }

        if (v instanceof File && type == String.class) {
            v = ((File) v).getAbsolutePath();
        }

        return type.cast(v);
    }

    private static <T> Iterable<T> getMultiValue(Map<String, ?> properties, String name, Class<T> type) {
        Iterable v = (Iterable) properties.remove(name);
        List<T>  result = new LinkedList<T>();

        if (v == null) {
            return Collections.emptyList();
        }
        
        for (Object o : v) {
            result.add(type.cast(o));
        }

        return result;
    }

    private static String toOneLine(Iterable<String> it) {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (String s : it) {
            if (!first) {
                result.append(' ');
            }
            first = false;
            result.append(s);
        }

        return result.toString();
    }

    private static URL locateScript(String actionName) {
        return ProjectRunnerImpl.class.getResource("/org/netbeans/modules/java/source/ant/resources/" + actionName + "-snippet.xml");
    }

    private static FileObject buildScript(String actionName) throws IOException {
        URL script = locateScript(actionName);

        if (script == null) {
            return null;
        }

        URL thisClassSource = ProjectRunnerImpl.class.getProtectionDomain().getCodeSource().getLocation();
        File jarFile = FileUtil.archiveOrDirForURL(thisClassSource);
        File scriptFile = new File(getCacheFolder(), actionName + ".xml");
        
        if (!scriptFile.canRead() || (jarFile != null && jarFile.lastModified() > scriptFile.lastModified())) {
            try {
                scriptFile.delete();

                URLConnection connection = script.openConnection();
                FileObject target = FileUtil.createData(scriptFile);

                copyFile(connection, target);
                return target;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        return FileUtil.toFileObject(scriptFile);
    }

    private static final String NB_USER_DIR = "netbeans.user";   //NOI18N
    private static final String SNIPPETS_CACHE_DIR = "var"+File.separatorChar+"cache"+File.separatorChar+"executor-snippets";    //NOI18N


    private static String getNbUserDir () {
        final String nbUserProp = System.getProperty(NB_USER_DIR);
        return nbUserProp;
    }

    private static File cacheFolder;

    private static synchronized File getCacheFolder () {
        if (cacheFolder == null) {
            final String nbUserDirProp = getNbUserDir();
            assert nbUserDirProp != null;
            final File nbUserDir = new File (nbUserDirProp);
            cacheFolder = FileUtil.normalizeFile(new File (nbUserDir, SNIPPETS_CACHE_DIR));
            if (!cacheFolder.exists()) {
                boolean created = cacheFolder.mkdirs();
                assert created : "Cannot create cache folder";  //NOI18N
            }
            else {
                assert cacheFolder.isDirectory() && cacheFolder.canRead() && cacheFolder.canWrite();
            }
        }
        return cacheFolder;
    }

    private static void copyFile(URLConnection source, FileObject target) throws IOException {
        InputStream ins = null;
        OutputStream out = null;

        try {
            ins = source.getInputStream();
            out = target.getOutputStream();

            FileUtil.copy(ins, out);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private static void doClean(ClassPath exec) throws IOException {
        for (ClassPath.Entry entry : exec.entries()) {
            SourceForBinaryQuery.Result2 r = SourceForBinaryQuery.findSourceRoots2(entry.getURL());

            if (r.preferSources() && r.getRoots().length > 0) {
                for (FileObject source : r.getRoots()) {
                    File sourceFile = FileUtil.toFile(source);

                    if (sourceFile == null) {
                        LOG.log(Level.WARNING, "Source URL: {0} cannot be translated to file, skipped", source.getURL().toExternalForm());
                        continue;
                    }

                    BuildArtifactMapperImpl.clean(sourceFile.toURI().toURL());
                }
            }
        }
    }

    private static final class FakeAntProjectCookie implements AntProjectCookie, ChangeListener {

        private final AntProjectCookie apc;
        private final String projectName;
        private final ChangeSupport cs = new ChangeSupport(this);

        public FakeAntProjectCookie(AntProjectCookie apc, String projectName) {
            this.apc = apc;
            this.apc.addChangeListener(WeakListeners.change(this, this.apc));
            this.projectName = projectName;
        }

        public File getFile() {
            return this.apc.getFile();
        }

        public FileObject getFileObject() {
            return this.apc.getFileObject();
        }

        public Document getDocument() {
            return this.apc.getDocument();
        }

        public Element getProjectElement() {
            return new FakeElement(this.apc.getProjectElement(), projectName);
        }

        public Throwable getParseException() {
            return this.apc.getParseException();
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        public void stateChanged(ChangeEvent e) {
            cs.fireChange();
        }

    }

    private static final class FakeElement implements Element {

        private final Element delegate;
        private final String projectName;
        
        public FakeElement(Element delegate, String projectName) {
            this.delegate = delegate;
            this.projectName = projectName;
        }

        public Object setUserData(String key, Object data, UserDataHandler handler) {
            return delegate.setUserData(key, data, handler);
        }

        public void setTextContent(String textContent) throws DOMException {
            delegate.setTextContent(textContent);
        }

        public void setPrefix(String prefix) throws DOMException {
            delegate.setPrefix(prefix);
        }

        public void setNodeValue(String nodeValue) throws DOMException {
            delegate.setNodeValue(nodeValue);
        }

        public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
            return delegate.replaceChild(newChild, oldChild);
        }

        public Node removeChild(Node oldChild) throws DOMException {
            return delegate.removeChild(oldChild);
        }

        public void normalize() {
            delegate.normalize();
        }

        public String lookupPrefix(String namespaceURI) {
            return delegate.lookupPrefix(namespaceURI);
        }

        public String lookupNamespaceURI(String prefix) {
            return delegate.lookupNamespaceURI(prefix);
        }

        public boolean isSupported(String feature, String version) {
            return delegate.isSupported(feature, version);
        }

        public boolean isSameNode(Node other) {
            return delegate.isSameNode(other);
        }

        public boolean isEqualNode(Node arg) {
            return delegate.isEqualNode(arg);
        }

        public boolean isDefaultNamespace(String namespaceURI) {
            return delegate.isDefaultNamespace(namespaceURI);
        }

        public Node insertBefore(Node newChild, Node refChild) throws DOMException {
            return delegate.insertBefore(newChild, refChild);
        }

        public boolean hasChildNodes() {
            return delegate.hasChildNodes();
        }

        public boolean hasAttributes() {
            return delegate.hasAttributes();
        }

        public Object getUserData(String key) {
            return delegate.getUserData(key);
        }

        public String getTextContent() throws DOMException {
            return delegate.getTextContent();
        }

        public Node getPreviousSibling() {
            return delegate.getPreviousSibling();
        }

        public String getPrefix() {
            return delegate.getPrefix();
        }

        public Node getParentNode() {
            return delegate.getParentNode();
        }

        public Document getOwnerDocument() {
            return delegate.getOwnerDocument();
        }

        public String getNodeValue() throws DOMException {
            return delegate.getNodeValue();
        }

        public short getNodeType() {
            return delegate.getNodeType();
        }

        public String getNodeName() {
            return delegate.getNodeName();
        }

        public Node getNextSibling() {
            return delegate.getNextSibling();
        }

        public String getNamespaceURI() {
            return delegate.getNamespaceURI();
        }

        public String getLocalName() {
            return delegate.getLocalName();
        }

        public Node getLastChild() {
            return delegate.getLastChild();
        }

        public Node getFirstChild() {
            return delegate.getFirstChild();
        }

        public Object getFeature(String feature, String version) {
            return delegate.getFeature(feature, version);
        }

        public NodeList getChildNodes() {
            return delegate.getChildNodes();
        }

        public String getBaseURI() {
            return delegate.getBaseURI();
        }

        public NamedNodeMap getAttributes() {
            return delegate.getAttributes();
        }

        public short compareDocumentPosition(Node other) throws DOMException {
            return delegate.compareDocumentPosition(other);
        }

        public Node cloneNode(boolean deep) {
            return delegate.cloneNode(deep);
        }

        public Node appendChild(Node newChild) throws DOMException {
            return delegate.appendChild(newChild);
        }

        public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
            delegate.setIdAttributeNode(idAttr, isId);
        }

        public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
            delegate.setIdAttributeNS(namespaceURI, localName, isId);
        }

        public void setIdAttribute(String name, boolean isId) throws DOMException {
            delegate.setIdAttribute(name, isId);
        }

        public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
            return delegate.setAttributeNodeNS(newAttr);
        }

        public Attr setAttributeNode(Attr newAttr) throws DOMException {
            return delegate.setAttributeNode(newAttr);
        }

        public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
            delegate.setAttributeNS(namespaceURI, qualifiedName, value);
        }

        public void setAttribute(String name, String value) throws DOMException {
            delegate.setAttribute(name, value);
        }

        public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
            return delegate.removeAttributeNode(oldAttr);
        }

        public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
            delegate.removeAttributeNS(namespaceURI, localName);
        }

        public void removeAttribute(String name) throws DOMException {
            delegate.removeAttribute(name);
        }

        public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
            return delegate.hasAttributeNS(namespaceURI, localName);
        }

        public boolean hasAttribute(String name) {
            return delegate.hasAttribute(name);
        }

        public String getTagName() {
            return delegate.getTagName();
        }

        public TypeInfo getSchemaTypeInfo() {
            return delegate.getSchemaTypeInfo();
        }

        public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
            return delegate.getElementsByTagNameNS(namespaceURI, localName);
        }

        public NodeList getElementsByTagName(String name) {
            return delegate.getElementsByTagName(name);
        }

        public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
            return delegate.getAttributeNodeNS(namespaceURI, localName);
        }

        public Attr getAttributeNode(String name) {
            return delegate.getAttributeNode(name);
        }

        public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
            return delegate.getAttributeNS(namespaceURI, localName);
        }

        public String getAttribute(String name) {
            if ("name".equals(name)) {
                String pattern = delegate.getAttribute(name);
                
                return MessageFormat.format(pattern, projectName);
            }
            return delegate.getAttribute(name);
        }

    }
}
