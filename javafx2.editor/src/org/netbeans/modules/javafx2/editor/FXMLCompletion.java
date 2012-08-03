/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing the
 * software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 */
package org.netbeans.modules.javafx2.editor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.NbBundle;

/**
 *
 * @author David Strupl
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=CompletionProvider.class)
public class FXMLCompletion implements CompletionProvider {
    
    private static final String MIME_TYPE = "text/x-java"; // NOI18N
    private static final String FX_BASE_CLASS = "javafx.scene.Node"; // NOI18N
    
    /**
     * Creates a new instance of WordCompletion
     */
    public FXMLCompletion() {
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        /*
        if (queryType == COMPLETION_QUERY_TYPE) {
            return new AsyncCompletionTask(new Query(), component);
        }
        */

        return null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    static class Query extends AsyncCompletionQuery {
        List<FXMLCompletionItem> results;

        @Override
        protected void query(final CompletionResultSet resultSet, final Document document, final int caretOffset) {

            results = new ArrayList<FXMLCompletionItem>();

            document.render(new Runnable() {

                @Override
                public void run() {
                    TokenHierarchy<Document> h = TokenHierarchy.get(document);
                    TokenSequence<?> ts = h.tokenSequence();
                    if (ts == null) {
                        return;
                    }
                    try {
                        ts.move(caretOffset);
                        if (ts.moveNext()) {
                            Token<?> t = ts.token();
                            List<DocumentElement> path = getPathToRoot(document, caretOffset);
                            String prefix = ""; // NOI18N
  //                        System.out.println("TOKEN ID= " + t.id() + " TEXT: " + t.text()); // TODO: remove me
                            if (t.id() == XMLTokenId.TAG) {
                                if (ts.offset() < caretOffset) {
                                   prefix = t.text().toString().substring(1, caretOffset - ts.offset());
                                }
                                if (t.text().toString().startsWith("<")) { // NOI18N
                                    if ( (path.size() == 2) || 
                                         ( (path.size() > 2) && (path.get(1).getName().equals("children")) ) // NOI18N
                                       ) {
                                        List<String> classes = getAvailableClasses(document, prefix, resultSet);
                                        for (String cls : classes) {
                                            results.add(new FXMLCompletionItem(ts.offset(), "<" + cls)); // NOI18N
                                        }
                                    }
                                    if ( (path.size() > 2) && (!path.get(1).getName().equals("children")) )  { // NOI18N
                                        String className = path.get(1).getName();
                                        List<String> properties = getAvailableProperties(document, prefix, className,resultSet);
                                        for (String prop : properties) {
                                            results.add(new FXMLCompletionItem(ts.offset(), "<" + prop)); // NOI18N
                                        }
                                    }
                                }
                            }
                            if (t.id() == XMLTokenId.ARGUMENT) {
                                if (ts.offset() < caretOffset) {
                                   prefix = t.text().toString().substring(0, caretOffset - ts.offset());
                                }
                                if (path.size() > 0) {
                                    String className = path.get(0).getName();
                                    List<String> props = getAvailableProperties(document, prefix, className, resultSet);
                                    for (String prop : props) {
                                        results.add(new FXMLCompletionItem(ts.offset(), prop)); // NOI18N
                                    }
                                }
                            }
                        }
                    } catch (ParseException e) {
                        Logger.getLogger(FXMLCompletion.class.getName()).log(Level.FINE, null, e);
                    } catch (InterruptedException e) {
                        Logger.getLogger(FXMLCompletion.class.getName()).log(Level.FINE, null, e);
                    } catch (ExecutionException e) {
                        Logger.getLogger(FXMLCompletion.class.getName()).log(Level.FINE, null, e);
                    }
                }
            });
            if (resultSet != null) {
                // resultSet can be null only in tests!
                resultSet.addAllItems(results);
                resultSet.finish();
            }
        }
    }

    /**
     * This method uses the Tags based Editors Library to compute the path in the DOM.
     * @param bdoc
     * @param caretOffset
     * @return the list contains first the element at current offset and the
     *    path all the way up to the root (root is the last in the list)
     */
    private static List<DocumentElement> getPathToRoot(Document bdoc, int caretOffset) {
        List<DocumentElement> result = new ArrayList<DocumentElement>();
        final DocumentModel model;
        try {
            model = DocumentModel.getDocumentModel(bdoc);
            if (model != null) {
                DocumentElement root = model.getRootElement();
                DocumentElement current = model.getLeafElementForOffset(caretOffset);
                while ((current != null) && (current != root)) {
                    result.add(current);
                    current = current.getParentElement();
                }
                result.add(root);
            }
        } catch (DocumentModelException ex) {
            Logger.getLogger(FXMLCompletion.class.getName()).log(Level.FINE, null, ex);
        }
        return result;
    }

    private static List<String> getAvailableProperties(Document doc, String prefix, String className, CompletionResultSet resultSet) throws ParseException, InterruptedException, ExecutionException {
        List<String> result = new ArrayList<String>();
        Future<Void> f = ParserManager.parseWhenScanFinished(MIME_TYPE, getAllPropertiesTask(doc, prefix, className, result));
        if (!f.isDone()) {
            if (resultSet != null) {
                resultSet.setWaitText(NbBundle.getMessage(FXMLCompletion.class, "scanning-in-progress")); //NOI18N
            }
            f.get();
        }
        return result;
    }

    
    private static List<String> getAvailableClasses(Document doc, String prefix, CompletionResultSet resultSet) throws ParseException, InterruptedException, ExecutionException {
        List<String> result = new ArrayList<String>();
        Future<Void> f = ParserManager.parseWhenScanFinished(MIME_TYPE, getAllClassesTask(doc, prefix, result));
        if (!f.isDone()) {
            if (resultSet != null) {
                resultSet.setWaitText(NbBundle.getMessage(FXMLCompletion.class, "scanning-in-progress")); //NOI18N
            }
            f.get();
        }
        return result;
    }
    private static UserTask getAllPropertiesTask(final Document doc, final String prefix, final String className, final List<String> res) {
        final ClasspathInfo cpInfo = ClasspathInfo.create(doc);
        class AllPropertiesTask extends UserTask implements ClasspathInfoProvider {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                ClassIndex index = cpInfo.getClassIndex();
                CompilationInfo info = CompilationInfo.get(result);
                Elements elems = info.getElements();
                TypeElement fxBaseElem = elems.getTypeElement(FX_BASE_CLASS);
                if (fxBaseElem == null) {
                    throw new IllegalStateException("Cannot find class " + FX_BASE_CLASS + " on classpath!"); // NOI18N
                }
                Types types = info.getTypes();
                DeclaredType fxBaseClassType = types.getDeclaredType(fxBaseElem);
                List<TypeElement> myTypes =new LinkedList<TypeElement>();
                for(ElementHandle<TypeElement> handle : index.getDeclaredTypes(className, 
                            ClassIndex.NameKind.SIMPLE_NAME, EnumSet.allOf(ClassIndex.SearchScope.class))) {
                    TypeElement te = handle.resolve(info);
                    if (te != null && types.isSubtype(types.getDeclaredType(te), fxBaseClassType)) {
                        myTypes.add(te);
                    }
                    

                }
                List<TypeElement> superTypes = new LinkedList<TypeElement>();
                for (TypeElement myType : myTypes) {
                    while (myType != null) {
                        TypeMirror tm = myType.getSuperclass();
                        if (tm != null) {
                            TypeElement superType = (TypeElement)types.asElement(tm);
                            if (superType != null) {
                                superTypes.add(superType);
                            }
                            myType = superType;
                        } else {
                            myType = null;
                        }
                    }
                }
                myTypes.addAll(superTypes);
                for (TypeElement myType : myTypes) {
                    List<? extends Element> children = myType.getEnclosedElements();
                    for (Element e : children) {
                        String name = e.getSimpleName().toString();
                        if (name.startsWith("set") && name.length() > 3) { // NOI18N
                            name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                            if (name.startsWith(prefix)) {                   
                                res.add(name);
                            }
                        }
                    }
                }
            }
                
            @Override
            public ClasspathInfo getClasspathInfo() {
                return cpInfo;
            }
        }
        return new AllPropertiesTask();
    }

    private static UserTask getAllClassesTask(final Document doc, final String prefix, final List<String> res) {
        final ClasspathInfo cpInfo = ClasspathInfo.create(doc);
        class AllClassesTask extends UserTask implements ClasspathInfoProvider {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                ClassIndex index = cpInfo.getClassIndex();
                CompilationInfo info = CompilationInfo.get(result);
                Elements elems = info.getElements();
                TypeElement fxBaseElem = elems.getTypeElement(FX_BASE_CLASS);
                if (fxBaseElem == null) {
                    throw new IllegalStateException("Cannot find class " + FX_BASE_CLASS + " on classpath!"); // NOI18N
                }
                Types types = info.getTypes();
                if (prefix != null && prefix.length()>2) {
                    DeclaredType fxBaseClassType = types.getDeclaredType(fxBaseElem);
                    for(ElementHandle<TypeElement> handle : index.getDeclaredTypes(prefix, 
                            ClassIndex.NameKind.PREFIX, EnumSet.allOf(ClassIndex.SearchScope.class))) {
                        TypeElement te = handle.resolve(info);
                        if (te != null && types.isSubtype(types.getDeclaredType(te), fxBaseClassType)) {
                            res.add(te.getSimpleName().toString());
                        }
                    }
                } else {
                    List<ElementHandle> toExplore = new LinkedList<ElementHandle>();
                    toExplore.add(ElementHandle.create(fxBaseElem));
                    while (!toExplore.isEmpty()) {
                        ElementHandle current = toExplore.remove(0);
                        for (ElementHandle<TypeElement> eh : index.getElements(current, 
                            EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.allOf(ClassIndex.SearchScope.class))) {
                            TypeElement te = eh.resolve(info);
                            String name = te.getSimpleName().toString();
                            if ((prefix == null) || (name.startsWith(prefix))) {
                                if (name.length() > 0) {
                                    res.add(name);
                                }
                            }
                            toExplore.add(eh);
                        }
                    }
                }
            }

            @Override
            public ClasspathInfo getClasspathInfo() {
                return cpInfo;
            }
        }
        return new AllClassesTask();
    }
}
