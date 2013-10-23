/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.web.beans.completion;

import java.io.IOException;
import java.util.*;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;

/**
 * Various completor for code completing XML tags and attributes in Hibername
 * configuration and mapping file
 *
 * @author Dongmei Cao
 */
public abstract class BeansCompletor {


    
    private int anchorOffset = -1;

    public abstract List<BeansCompletionItem> doCompletion(CompletionContext context);

    protected void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    /**
     * A completor for completing class tag
     */
    public static class EntityClassCompletor extends BeansCompletor {

        @Override
        public List<BeansCompletionItem> doCompletion(final CompletionContext context) {
            final List<BeansCompletionItem> results = new ArrayList<BeansCompletionItem>();
            try {
                Document doc = context.getDocument();
                final String typedChars = context.getTypedPrefix();

                JavaSource js = Utils.getJavaSource(doc);
                if (js == null) {
                    return Collections.emptyList();
                }
                FileObject fo = NbEditorUtilities.getFileObject(context.getDocument());
                doJavaCompletion(fo, js, results, typedChars, context.getCurrentToken().getOffset());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return results;
        }

        private void doJavaCompletion(final FileObject fo, final JavaSource js, final List<BeansCompletionItem> results,
                final String typedPrefix, final int substitutionOffset) throws IOException {
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                    Project project = FileOwnerQuery.getOwner(fo);
//                    EntityClassScopeProvider provider = (EntityClassScopeProvider) project.getLookup().lookup(EntityClassScopeProvider.class);
//                    EntityClassScope ecs = null;
//                    Entity[] entities = null;
//                    if (provider != null) {
//                        ecs = provider.findEntityClassScope(fo);
//                    }
//                    if (ecs != null) {
//                        entities = ecs.getEntityMappingsModel(false).runReadAction(new MetadataModelAction<EntityMappingsMetadata, Entity[]>() {
//
//                            @Override
//                            public Entity[] run(EntityMappingsMetadata metadata) throws Exception {
//                                return metadata.getRoot().getEntity();
//                            }
//                        });
//                    }
//                    // add classes 
//                    if(entities != null) {
//                        for (Entity entity : entities) {
//                            if (typedPrefix.length() == 0 || entity.getClass2().toLowerCase().startsWith(typedPrefix.toLowerCase()) || entity.getName().toLowerCase().startsWith(typedPrefix.toLowerCase())) {
//                                BeansCompletionItem item = BeansCompletionItem.createAttribValueItem(substitutionOffset, entity.getClass2());
//                                results.add(item);
//                            }
//                        }
//                    }
                }
            }, true);

            setAnchorOffset(substitutionOffset);
        }
    }

    private static String getProviderClass(Node tag) {
        String name = null;
        while (tag != null && !"persistence-unit".equals(tag.getNodeName())) {
            tag = tag.getParentNode();//NOI18N
        }
        if (tag != null) {
            for (Node ch = tag.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
                if ("provider".equals(ch.getNodeName())) {//NOI18N
                    name = ch.getFirstChild().getNodeValue();
                }
            }
        }
        return name;
    }

    private static String getPropertyName(Node tag) {
        String name = null;
        while (tag != null && !"property".equals(tag.getNodeName())) {
            tag = tag.getParentNode();//NOI18N
        }
        if (tag != null) {
            Node nmN = tag.getAttributes().getNamedItem("name");//NOI18N
            if (nmN != null) {
                name = nmN.getNodeValue();
            }
        }
        return name;
    }
}
