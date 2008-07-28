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
import java.text.MessageFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.project.runner.ProjectRunnerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

/**
 *
 * @author Jan Lahoda
 */
public class ProjectRunnerImpl implements ProjectRunnerImplementation{

    private static final Logger LOG = Logger.getLogger(ProjectRunnerImpl.class.getName());
    
    public boolean isSupported(String command, FileObject file) {
        return locateScript(command) != null && checkRunSupported(file);
    }

    public void execute(String command, Properties props, FileObject toRun) throws IOException {
        Project project = FileOwnerQuery.getOwner(toRun);
        ClassPath exec = ClassPath.getClassPath(toRun, ClassPath.EXECUTE);
        ClassPath source = ClassPath.getClassPath(toRun, ClassPath.SOURCE);

        LOG.log(Level.FINE, "execute classpath={0}", exec);

        String cp = exec.toString(ClassPath.PathConversionMode.FAIL);

        Properties antProps = (Properties) props.clone();

        antProps.setProperty("classpath", cp);
        antProps.setProperty("classname", source.getResourceName(toRun, '.', false));
        
        FileObject script = buildScript(command);
        String projectName = project != null ? ProjectUtils.getInformation(project).getDisplayName() : "";
        AntProjectCookie apc = new FakeAntProjectCookie(AntScriptUtils.antProjectCookieFor(script), projectName);
        AntTargetExecutor.Env execenv = new AntTargetExecutor.Env();
        Properties p = execenv.getProperties();
        p.putAll(antProps);
        execenv.setProperties(p);

        AntTargetExecutor.createTargetExecutor(execenv).execute(apc, null);
    }

    private static boolean checkRunSupported(FileObject file) {
        //XXX: finish
        return true;
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
