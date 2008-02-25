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

package org.netbeans.modules.spring.beans.model.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.model.SpringBeanSource;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An implementation of {@link SpringBeanSource} delegating to
 * a file or a its document in the editor.
 *
 * @author Andrei Badea
 */
public class ConfigFileSpringBeanSource implements SpringBeanSource {

    private static final Logger LOGGER = Logger.getLogger(ConfigFileSpringBeanSource.class.getName());

    private final Map<String, ConfigFileSpringBean> id2Bean = new HashMap<String, ConfigFileSpringBean>();
    private final Map<String, ConfigFileSpringBean> name2Bean = new HashMap<String, ConfigFileSpringBean>();
    private final List<ConfigFileSpringBean> beans = new ArrayList<ConfigFileSpringBean>();

    /**
     * Parses the given document.
     * Currently the implementation expects it to be a {@link BaseDocument} or null.
     *
     * @param  document the document to parse.
     */
    public void parse(BaseDocument document) throws IOException {
        FileObject fo = NbEditorUtilities.getFileObject(document);
        if (fo == null) {
            LOGGER.log(Level.WARNING, "Could not get a FileObject for document {0}", document);
            return;
        }
        LOGGER.log(Level.FINE, "Parsing {0}", fo);
        File file = FileUtil.toFile(fo);
        if (file == null) {
            LOGGER.log(Level.WARNING, "{0} resolves to a null File, aborting", fo);
            return;
        }
        new DocumentParser(file, document).run();
        LOGGER.log(Level.FINE, "Parsed {0}", fo);
    }

    public List<SpringBean> getBeans() {
        return Collections.<SpringBean>unmodifiableList(beans);
    }

    public SpringBean findBeanByID(String id) {
        return id2Bean.get(id);
    }

    public SpringBean findBeanByIDOrName(String name) {
        SpringBean bean = findBeanByID(name);
        if (bean == null) {
            bean = name2Bean.get(name);
        }
        return bean;
    }

    /**
     * This is the actual document parser.
     */
    private final class DocumentParser implements Runnable {

        private final File file;
        private final Document document;

        public DocumentParser(File file, Document document) {
            this.file = file;
            this.document = document;
        }

        public void run() {
            id2Bean.clear();
            name2Bean.clear();
            beans.clear();
            Node rootNode = SpringXMLConfigEditorUtils.getDocumentRoot(document);
            NodeList childNodes = rootNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (!"bean".equals(node.getNodeName())) { // NOI18N
                    continue;
                }
                parseBean(node);
            }
        }

        private void parseBean(Node node) {
            String id = getTrimmedAttr(node, "id"); // NOI18N
            String name = getTrimmedAttr(node, "name"); // NOI18N
            List<String> names;
            if (name != null) {
                names = Collections.unmodifiableList(StringUtils.tokenize(name, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS));
            } else {
                names = Collections.<String>emptyList();
            }
            String clazz = getTrimmedAttr(node, "class"); // NOI18N
            String parent = getTrimmedAttr(node, "parent"); // NOI18N
            String factoryBean = getTrimmedAttr(node, "factory-bean"); // NOI18N
            String factoryMethod = getTrimmedAttr(node, "factory-method"); // NOI18N
            Tag tag = (Tag)node;
            Location location = new ConfigFileLocation(file, tag.getElementOffset());
            ConfigFileSpringBean bean = new ConfigFileSpringBean(id, names, clazz, parent, factoryBean, factoryMethod, location);
            if (id != null) {
                addBeanID(id, bean);
            }
            for (String each : names) {
                addBeanName(each, bean);
            }
            beans.add(bean);
        }

        private void addBeanID(String id, ConfigFileSpringBean bean) {
            if (id2Bean.get(id) == null) {
                id2Bean.put(id, bean);
            }
        }

        private void addBeanName(String name, ConfigFileSpringBean bean) {
            if (name2Bean.get(name) == null) {
                name2Bean.put(name, bean);
            }
        }

        private String getTrimmedAttr(Node node, String attrName) {
            String attrValue = SpringXMLConfigEditorUtils.getAttribute(node, attrName);
            if (attrValue != null) {
                attrValue = attrValue.trim();
            }
            return attrValue;
        }
    }
}
