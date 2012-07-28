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

package org.netbeans.modules.j2ee.persistence.editor.hyperlink;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedQuery;
import org.netbeans.modules.j2ee.persistence.editor.completion.CCParser;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class NamedQueryHyperlinkProvider implements HyperlinkProviderExt {

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return getIdentifierSpan(doc, offset, null);
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        Line ln = getLine(doc, offset);
        if (ln != null) {
            ln.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);
        
        if (ts == null)
            return null;
        
        ts.move(offset);
        if (!ts.moveNext())
            return null;
        
        Token<JavaTokenId> t = ts.token();
        FileObject fo = getFileObject(doc);
        String name = t.toString();
        name = name.substring(name.startsWith("\"") ? 1 : 0, name.endsWith("\"") ? name.length()-1 : name.length());
        String query = findNq(fo, name);
        if (query != null) {
            return query;
        }
        return null;
    }
    
    private Line getLine(Document doc, int offset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);
        
        if (ts == null)
            return null;
        
        ts.move(offset);
        if (!ts.moveNext())
            return null;
        
        Token<JavaTokenId> t = ts.token();
        FileObject fo = getFileObject(doc);
        String name = t.toString();
        name = name.substring(name.startsWith("\"") ? 1 : 0, name.endsWith("\"") ? name.length()-1 : name.length());
        FileObject ent  = findEntity(fo, name);
        if (ent != null) {
            try {
                DataObject dobj = DataObject.find(ent);
                EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                try {
                    ec.openDocument();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                LineCookie lc = dobj.getLookup().lookup(LineCookie.class);
                if (lc != null) {
                    Line.Set ls = lc.getLineSet();
                    for (Line line : ls.getLines()) {
                        if (line.getText().contains("\""+name+"\"")) {
                            return line;
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
    private FileObject findEntity(FileObject javaFile, String nqName) {
        Project prj = FileOwnerQuery.getOwner(javaFile);
        if (prj == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(javaFile, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }       
        EntityClassScopeProvider provider = (EntityClassScopeProvider) prj.getLookup().lookup(EntityClassScopeProvider.class);
        EntityClassScope ecs = null;
        Entity[] entities = null;
        if (provider != null) {
            ecs = provider.findEntityClassScope(javaFile);
        }
        if (ecs != null) {
            try {
                entities = ecs.getEntityMappingsModel(false).runReadAction(new MetadataModelAction<EntityMappingsMetadata, Entity[]>() {
                   @Override
                    public Entity[] run(EntityMappingsMetadata metadata) throws Exception {
                        return metadata.getRoot().getEntity();
                    }
                });
            } catch (MetadataModelException ex) {
            } catch (IOException ex) {
            }
        }
        if(entities != null)
        for (Entity entity : entities) {
            for(NamedQuery nq:entity.getNamedQuery()){
                if(nqName.equals(nq.getName())){
                    return cp.findResource(entity.getClass2().replace('.', '/')+".java");
                }
            }
        }
        return null;
    }
    private String findNq(FileObject javaFile, String nqName) {
        Project prj = FileOwnerQuery.getOwner(javaFile);
        if (prj == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(javaFile, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }       
        EntityClassScopeProvider provider = (EntityClassScopeProvider) prj.getLookup().lookup(EntityClassScopeProvider.class);
        EntityClassScope ecs = null;
        Entity[] entities = null;
        if (provider != null) {
            ecs = provider.findEntityClassScope(javaFile);
        }
        if (ecs != null) {
            try {
                entities = ecs.getEntityMappingsModel(false).runReadAction(new MetadataModelAction<EntityMappingsMetadata, Entity[]>() {
                   @Override
                    public Entity[] run(EntityMappingsMetadata metadata) throws Exception {
                        return metadata.getRoot().getEntity();
                    }
                });
            } catch (MetadataModelException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if(entities != null)
        for (Entity entity : entities) {
            for(NamedQuery nq:entity.getNamedQuery()){
                if(nqName.equals(nq.getName())){
                    return nq.getQuery();
                }
            }
        }
        return null;
    }
    
    private static final Set<JavaTokenId> USABLE_TOKEN_IDS = EnumSet.of(JavaTokenId.STRING_LITERAL);
    
    public static int[] getIdentifierSpan(final Document doc, final int offset, Token<JavaTokenId>[] token) {
        FileObject fo = getFileObject(doc);
        if (fo == null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }
        Project prj = FileOwnerQuery.getOwner(fo);
        if (prj == null) {
            return null;
        }
     
        EntityClassScopeProvider eCS = prj.getLookup().lookup(EntityClassScopeProvider.class);
        if(eCS == null){
            return null;//no jpa support
        }

        final int[] ret = new int[] { -1, -1 };
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);

                if (ts == null) {
                    return;
                }

                ts.move(offset);
                if (!ts.moveNext()) {
                    return;
                }

                Token<JavaTokenId> t = ts.token();
                boolean hasMessage = false;
                if (USABLE_TOKEN_IDS.contains(t.id())) {
                    for (int i = 0; i < 5; i++) {
                        if (!ts.movePrevious()) {
                            break;
                        }
                        Token<JavaTokenId> tk = ts.token();
                        if (TokenUtilities.equals(CCParser.CREATE_NAMEDQUERY, tk.text())) {//NOI18N
                            hasMessage = true;
                        }
                    }
                    if (hasMessage) {
                        ts.move(offset);
                        ts.moveNext();
                        ret[0] = ts.offset();
                        ret[1] = ts.offset() + t.length();
                        return;
                    }
                }
            }
        });
        return (ret[0] == -1) ? null : ret;
        
    }
    
    private static FileObject getFileObject(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        return od != null ? od.getPrimaryFile() : null;
    }
    

}
