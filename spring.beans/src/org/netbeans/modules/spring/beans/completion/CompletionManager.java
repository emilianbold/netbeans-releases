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
package org.netbeans.modules.spring.beans.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class CompletionManager {

    private static final String BEAN_TAG = "bean"; // NOI18N
    private static final String ALIAS_TAG = "alias"; // NOI18N
    private static final String BEANS_TAG = "beans"; // NOI18N
    private static final String LIST_TAG = "list"; // NOI18N
    private static final String SET_TAG = "set"; // NOI18N
    private static final String MAP_TAG = "map"; // NOI18N
    private static final String PROPS_TAG = "props"; // NOI18N
    private static final String IMPORT_TAG = "import"; // NOI18N
    private static final String DEPENDS_ON_ATTRIB = "depends-on"; // NOI18N
    private static final String PARENT_ATTRIB = "parent"; // NOI18N
    private static final String FACTORY_BEAN_ATTRIB = "factory-bean"; // NOI18N
    private static final String NAME_ATTRIB = "name"; // NOI18N
    private static final String DEFAULT_LAZY_INIT_ATTRIB = "default-lazy-init"; // NOI18N
    private static final String AUTOWIRE_ATTRIB = "autowire"; // NOI18N
    private static final String DEFAULT_MERGE_ATTRIB = "default-merge"; // NOI18N
    private static final String DEFAULT_DEPENDENCY_CHECK_ATTRIB = "default-dependency-check"; // NOI18N
    private static final String DEFAULT_AUTOWIRE_ATTRIB = "default-autowire"; // NOI18N
    private static final String DEPENDENCY_CHECK_ATTRIB = "dependency-check"; // NOI18N
    private static final String LAZY_INIT_ATTRIB = "lazy-init"; // NOI18N
    private static final String ABSTRACT_ATTRIB = "abstract"; // NOI18N
    private static final String AUTOWIRE_CANDIDATE_ATTRIB = "autowire-candidate"; // NOI18N
    private static final String MERGE_ATTRIB = "merge"; // NOI18N
    private static final String RESOURCE_ATTRIB = "resource"; // NOI18N
    private static final String INIT_METHOD_ATTRIB = "init-method"; // NOI18N
    private static final String DESTROY_METHOD_ATTRIB = "destroy-method"; // NOI18N
    private static final String CLASS_ATTRIB = "class"; // NOI18N
    private static Map<String, Completor> completors = new HashMap<String, Completor>();

    private CompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {

        String[] autowireItems = new String[]{
            "no", NbBundle.getMessage(CompletionManager.class, "DESC_autowire_no"), // NOI18N
            "byName", NbBundle.getMessage(CompletionManager.class, "DESC_autowire_byName"), // NOI18N
            "byType", NbBundle.getMessage(CompletionManager.class, "DESC_autowire_byType"), // NOI18N
            "constructor", NbBundle.getMessage(CompletionManager.class, "DESC_autowire_constructor"), // NOI18N
            "autodetect", NbBundle.getMessage(CompletionManager.class, "DESC_autowire_autodetect") // NOI18N
        };
        AttributeValueCompletor completor = new AttributeValueCompletor(autowireItems);
        registerCompletor(BEAN_TAG, AUTOWIRE_ATTRIB, completor);
        registerCompletor(BEANS_TAG, DEFAULT_AUTOWIRE_ATTRIB, completor);

        String[] defaultLazyInitItems = new String[]{
            "true", null, //XXX: Documentation // NOI18N
            "false", null, //XXX: Documentation // NOI18N
        };
        completor = new AttributeValueCompletor(defaultLazyInitItems);
        registerCompletor(BEANS_TAG, DEFAULT_LAZY_INIT_ATTRIB, completor);
        registerCompletor(BEAN_TAG, LAZY_INIT_ATTRIB, completor);
        
        String[] defaultMergeItems = new String[] {
            "true", null, //XXX: Documentation // NOI18N
            "false", null, //XXX: Documentation // NOI18N
        };
        completor = new AttributeValueCompletor(defaultMergeItems);
        registerCompletor(BEANS_TAG, DEFAULT_MERGE_ATTRIB, completor);
        
        String[] defaultDepCheckItems = new String[] {
            "none", NbBundle.getMessage(CompletionManager.class, "DESC_def_dep_check_none"), // NOI18N
            "simple", NbBundle.getMessage(CompletionManager.class, "DESC_def_dep_check_simple"), // NOI18N
            "objects", NbBundle.getMessage(CompletionManager.class, "DESC_def_dep_check_objects"), // NOI18N
            "all", NbBundle.getMessage(CompletionManager.class, "DESC_def_dep_check_all"), // NOI18N
        };
        completor = new AttributeValueCompletor(defaultDepCheckItems);
        registerCompletor(BEANS_TAG, DEFAULT_DEPENDENCY_CHECK_ATTRIB, completor);
        registerCompletor(BEAN_TAG, DEPENDENCY_CHECK_ATTRIB, completor);
        
        String[] abstractItems = new String[] {
            "true", null, // XXX: documentation? // NOI18N
            "false", null, // XXX: documentation? // NOI18N
        };
        completor = new AttributeValueCompletor(abstractItems);
        registerCompletor(BEAN_TAG, ABSTRACT_ATTRIB, completor);
        
        String[] autowireCandidateItems = new String[] {
            "true", null, // XXX: documentation? // NOI18N
            "false", null, // XXX: documentation? // NOI18N
        };
        completor = new AttributeValueCompletor(autowireCandidateItems);
        registerCompletor(BEAN_TAG, AUTOWIRE_CANDIDATE_ATTRIB, completor);
        
        String[] mergeItems = new String[] {
            "true", null, // XXX: documentation? // NOI18N
            "false", null, // XXX: documentation? // NOI18N
        };
        completor = new AttributeValueCompletor(mergeItems);
        registerCompletor(LIST_TAG, MERGE_ATTRIB, completor);
        registerCompletor(SET_TAG, MERGE_ATTRIB, completor);
        registerCompletor(MAP_TAG, MERGE_ATTRIB, completor);
        registerCompletor(PROPS_TAG, MERGE_ATTRIB, completor);
        
        ResourceCompletor resourceCompletor = new ResourceCompletor(true);
        registerCompletor(IMPORT_TAG, RESOURCE_ATTRIB, resourceCompletor);
    }
    private static CompletionManager INSTANCE = new CompletionManager();

    public static CompletionManager getDefault() {
        return INSTANCE;
    }

    public List<SpringXMLConfigCompletionItem> completeAttributeValues(CompletionContext context) {
        String tagName = context.getTag().getNodeName();
        TokenItem attrib = ContextUtilities.getAttributeToken(context.getCurrentToken());
        String attribName = attrib != null ? attrib.getImage() : null;

        Completor completor = locateCompletor(tagName, attribName);
        if (completor != null) {
            return completor.doCompletion(context);
        }

        return Collections.emptyList();
    }

    public List<SpringXMLConfigCompletionItem> completeAttributes(CompletionContext context) {
        // TBD
        return Collections.emptyList();
    }

    public List<SpringXMLConfigCompletionItem> completeElements(CompletionContext context) {
        // TBD
        return Collections.emptyList();
    }

    private static abstract class Completor {

        public abstract List<SpringXMLConfigCompletionItem> doCompletion(CompletionContext context);

        public int getAnchorOffset() {
            return -1;
        }
    }

    private void registerCompletor(String tagName, String attribName,
            Completor completor) {
        completors.put(createRegisteredName(tagName, attribName), completor);
    }

    private static String createRegisteredName(String nodeName, String attributeName) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(nodeName)) {
            builder.append("/nodeName=");  // NOI18N
            builder.append(nodeName);
        } else {
            builder.append("/nodeName=");  // NOI18N
            builder.append("*");  // NOI18N
        }

        if (StringUtils.hasText(attributeName)) {
            builder.append("/attribute="); // NOI18N
            builder.append(attributeName);
        }

        return builder.toString();
    }

    private Completor locateCompletor(String nodeName, String attributeName) {
        String key = createRegisteredName(nodeName, attributeName);
        if (completors.containsKey(key)) {
            return completors.get(key);
        }

        key = createRegisteredName("*", attributeName); // NOI18N
        if (completors.containsKey(key)) {
            return completors.get(key);
        }

        return null;
    }

    /**
     * A simple completor for general attribute value items
     * 
     * Takes an array of strings, the even elements being the display text of the items
     * and the odd ones being the corresponding documentation of the items
     * 
     */
    private static class AttributeValueCompletor extends Completor {

        private String[] itemTextAndDocs;

        public AttributeValueCompletor(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }

        public List<SpringXMLConfigCompletionItem> doCompletion(CompletionContext context) {
            List<SpringXMLConfigCompletionItem> results = new ArrayList<SpringXMLConfigCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            for (int i = 0; i < itemTextAndDocs.length; i += 2) {
                if (itemTextAndDocs[i].startsWith(typedChars)) {
                    SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                    results.add(item);
                }
            }

            return results;
        }
    }

    private static class ResourceCompletor extends Completor {

        private boolean showDirectories;

        public ResourceCompletor(boolean showDirectories) {
            this.showDirectories = showDirectories;
        }

        public List<SpringXMLConfigCompletionItem> doCompletion(CompletionContext context) {
            List<SpringXMLConfigCompletionItem> results = new ArrayList<SpringXMLConfigCompletionItem>();
            Document doc = context.getDocument();
            FileObject fileObject = NbEditorUtilities.getFileObject(doc).getParent();
            String typedChars = context.getTypedPrefix();

            int lastSlashIndex = typedChars.lastIndexOf("/");
            String prefix = typedChars;

            if (lastSlashIndex != -1) {
                String pathStr = typedChars.substring(0, typedChars.lastIndexOf("/"));
                fileObject = fileObject.getFileObject(pathStr);
                if (lastSlashIndex != typedChars.length() - 1) {
                    prefix = typedChars.substring(Math.min(typedChars.lastIndexOf("/") + 1,
                            typedChars.length() - 1));
                } else {
                    prefix = "";
                }
            }

            if (fileObject == null) {
                return Collections.emptyList();
            }

            if (prefix == null) {
                prefix = "";
            }

            if (showDirectories) {
                Enumeration<? extends FileObject> folders = fileObject.getFolders(false);
                while (folders.hasMoreElements()) {
                    FileObject fo = folders.nextElement();
                    if (fo.getName().startsWith(prefix)) {
                        results.add(SpringXMLConfigCompletionItem.createFolderItem(context.getCaretOffset() - prefix.length(),
                                fo));
                    }
                }
            }

            Enumeration<? extends FileObject> files = fileObject.getData(false);
            while (files.hasMoreElements()) {
                FileObject fo = files.nextElement();
                if (fo.getName().startsWith(prefix) && fo.getMIMEType().equals("text/x-springconfig+xml")) {
                    results.add(SpringXMLConfigCompletionItem.createSpringXMLFileItem(context.getCaretOffset() - prefix.length(),
                            fo));
                }
            }

            return results;
        }
    }
}
