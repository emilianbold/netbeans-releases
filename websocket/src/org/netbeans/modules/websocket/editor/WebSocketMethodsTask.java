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
package org.netbeans.modules.websocket.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import javax.swing.text.Position;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;

/**
 * @author ads
 * 
 */
public class WebSocketMethodsTask implements CancellableTask<CompilationInfo> {

    @Override
    public void run(CompilationInfo compilationInfo) throws Exception {
        FileObject fileObject = compilationInfo.getFileObject();

        if (!isApplicable(fileObject)) {
            return;
        }

        WebSocketTask task = new WebSocketTask(compilationInfo);
        runTask.set(task);
        task.run();
        runTask.compareAndSet(task, null);
        HintsController.setErrors(fileObject, "WebSocket Methods Scanner", // NOI18N
                task.getDescriptions());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.api.java.source.CancellableTask#cancel()
     */
    @Override
    public void cancel() {
        WebSocketTask scanTask = runTask.getAndSet(null);
        if (scanTask != null) {
            scanTask.stop();
        }
    }

    private boolean isApplicable(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return false;
        }
        WebModule webModule = WebModule.getWebModule(project
                .getProjectDirectory());
        if (webModule == null) {
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        if (!Profile.JAVA_EE_7_WEB.equals(profile)
                && !Profile.JAVA_EE_7_FULL.equals(profile)) {
            return false;
        }
        return true;
    }

    private boolean hasAnnotation(Element element, String... annotationFqns) {
        List<? extends AnnotationMirror> annotations = element
                .getAnnotationMirrors();
        for (AnnotationMirror annotation : annotations) {
            Element annotationElement = annotation.getAnnotationType()
                    .asElement();
            if (annotationElement instanceof TypeElement) {
                String fqn = ((TypeElement) annotationElement)
                        .getQualifiedName().toString();
                for (String annotationFqn : annotationFqns) {
                    if (fqn.equals(annotationFqn)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Map<String, String> suggestWebSocketMethod() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("javax.websocket.OnMessage", "onMessage"); // NOI18N
        result.put("javax.websocket.OnOpen", "onOpen"); // NOI18N
        result.put("javax.websocket.OnClose", "onClose"); // NOI18N
        result.put("javax.websocket.OnError", "onError"); // NOI18N
        return result;
    }

    private Collection<String> getAnnotationsFqn(Element element) {
        List<? extends AnnotationMirror> annotations = element
                .getAnnotationMirrors();
        Collection<String> result = new HashSet<String>();
        for (AnnotationMirror annotation : annotations) {
            Element annotationElement = annotation.getAnnotationType()
                    .asElement();
            if (annotationElement instanceof TypeElement) {
                String fqn = ((TypeElement) annotationElement)
                        .getQualifiedName().toString();
                result.add(fqn);
            }
        }
        return result;
    }

    private List<Integer> getElementPosition(CompilationInfo info, Tree tree) {
        SourcePositions srcPos = info.getTrees().getSourcePositions();

        int startOffset = (int) srcPos.getStartPosition(
                info.getCompilationUnit(), tree);
        int endOffset = (int) srcPos.getEndPosition(info.getCompilationUnit(),
                tree);

        Tree startTree = null;

        if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())) {
            startTree = ((ClassTree) tree).getModifiers();

        } else if (tree.getKind() == Tree.Kind.METHOD) {
            startTree = ((MethodTree) tree).getReturnType();
        } else if (tree.getKind() == Tree.Kind.VARIABLE) {
            startTree = ((VariableTree) tree).getType();
        }

        if (startTree != null) {
            int searchStart = (int) srcPos.getEndPosition(
                    info.getCompilationUnit(), startTree);

            TokenSequence<?> tokenSequence = info.getTreeUtilities().tokensFor(
                    tree);

            if (tokenSequence != null) {
                boolean eob = false;
                tokenSequence.move(searchStart);

                do {
                    eob = !tokenSequence.moveNext();
                } while (!eob
                        && tokenSequence.token().id() != JavaTokenId.IDENTIFIER);

                if (!eob) {
                    Token<?> identifier = tokenSequence.token();
                    startOffset = identifier.offset(info.getTokenHierarchy());
                    endOffset = startOffset + identifier.length();
                }
            }
        }

        List<Integer> result = new ArrayList<Integer>(2);
        result.add(startOffset);
        result.add(endOffset);
        return result;
    }

    private class WebSocketTask {

        private WebSocketTask(CompilationInfo info) {
            myInfo = info;
            descriptions = new LinkedList<ErrorDescription>();
        }

        void run() {
            List<? extends TypeElement> classes = myInfo.getTopLevelElements();
            for (TypeElement clazz : classes) {
                if (stop) {
                    return;
                }
                List<ExecutableElement> methods = ElementFilter.methodsIn(clazz
                        .getEnclosedElements());
                if (!isEndpoint(clazz)) {
                    continue;
                }
                Map<String, String> webSocketMethod = suggestWebSocketMethod();
                Set<String> wsAnnotations = webSocketMethod.keySet();
                Set<String> existedMethods = new HashSet<String>();
                for (ExecutableElement method : methods) {
                    if (stop) {
                        return;
                    }
                    wsAnnotations.removeAll(getAnnotationsFqn(method));
                    existedMethods.add(method.getSimpleName().toString());
                }
                List<Fix> fixes = new LinkedList<Fix>();
                for (Entry<String, String> entry : webSocketMethod.entrySet()) {
                    Fix fix = new AddMethod(myInfo.getFileObject(),
                            ElementHandle.create(clazz), entry.getKey(),
                            entry.getValue(), existedMethods);
                    fixes.add(fix);
                }
                if (!fixes.isEmpty()) {
                    ClassTree classTree = myInfo.getTrees().getTree(clazz);
                    List<Integer> positions = getElementPosition(myInfo,
                            classTree);
                    ErrorDescription description = ErrorDescriptionFactory
                            .createErrorDescription(Severity.HINT, NbBundle
                                    .getMessage(WebSocketMethodsTask.class,
                                            "TXT_AddWebSocketMethods"), // NOI18N
                                    fixes, myInfo.getFileObject(), positions
                                            .get(0), positions.get(1));
                    getDescriptions().add(description);
                }
            }
        }

        private boolean isEndpoint(TypeElement clazz) {
            return hasAnnotation(clazz, "javax.websocket.server.ServerEndpoint"); // NOI18N
        }

        Collection<ErrorDescription> getDescriptions() {
            return descriptions;
        }

        void stop() {
            stop = true;
        }

        private final Collection<ErrorDescription> descriptions;
        private volatile boolean stop;
        private final CompilationInfo myInfo;
    }

    private class AddMethod implements Fix {

        AddMethod(FileObject fileObject,
                ElementHandle<TypeElement> endpointClass, String annotation,
                String methodName, Set<String> existedMethodNames) {
            myFileObject = fileObject;
            myHandle = endpointClass;
            myAnnotation = annotation;
            myMethodName = methodName;
            myExistedMethods = existedMethodNames;
        }

        @Override
        public ChangeInfo implement() throws Exception {
            JavaSource javaSource = JavaSource.forFileObject(myFileObject);
            if (javaSource == null) {
                return null;
            }
            ModificationResult modificationTask = javaSource
                    .runModificationTask(new Task<WorkingCopy>() {

                        @Override
                        public void run(WorkingCopy copy) throws Exception {
                            copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            TypeElement clazz = myHandle.resolve(copy);
                            TreeMaker maker = copy.getTreeMaker();

                            ClassTree classTree = copy.getTrees()
                                    .getTree(clazz);

                            AnnotationTree annotation = maker.Annotation(
                                    maker.QualIdent(myAnnotation),
                                    Collections.<ExpressionTree> emptyList());
                            ModifiersTree modifiers = maker.Modifiers(EnumSet
                                    .of(Modifier.PUBLIC), Collections
                                    .<AnnotationTree> singletonList(annotation));
                            ModifiersTree parMods = maker.Modifiers(
                                    Collections.<Modifier>emptySet(),
                                    Collections.<AnnotationTree>emptyList());
                            VariableTree par1 = maker.Variable(parMods, "t",
                                    maker.QualIdent("java.lang.Throwable"), null);
                            MethodTree method = maker.Method(
                                    modifiers,
                                    getMethodName(),
                                    maker.Type("void"), // NOI18N
                                    Collections.<TypeParameterTree> emptyList(),
                                    /*
                                     * XXX : optional session parameter could be
                                     * provided in the method signature :
                                     * javax.websocket.Session
                                     */
                                    Collections.<VariableTree> singletonList(par1),
                                    Collections.<ExpressionTree> emptyList(),
                                    "{}", null);
                            ClassTree newTree = maker.addClassMember(classTree,
                                    method);
                            copy.rewrite(classTree, newTree);
                        }
                    });
            List<? extends Difference> differences = modificationTask
                    .getDifferences(myFileObject);
            ChangeInfo changeInfo = new ChangeInfo();
            for (Difference difference : differences) {
                Position start = difference.getStartPosition();
                Position end = difference.getEndPosition();
                changeInfo.add(myFileObject, start, end);
            }
            modificationTask.commit();
            return changeInfo;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(WebSocketMethodsTask.class,
                    "TXT_AddWebScoketMethod", getDisplayName()); // NOI18N
        }

        private String getMethodName() {
            return getMethodName(myMethodName, 0);
        }

        private String getMethodName(String name, int i) {
            String suggestName = name;
            if (i == 0) {
                suggestName = name;
            } else {
                suggestName = name + i;
            }
            if (myExistedMethods.contains(suggestName)) {
                return getMethodName(name, i + 1);
            }
            return suggestName;
        }

        private String getDisplayName() {
            int index = myAnnotation.lastIndexOf('.');
            return "@" + myAnnotation.substring(index + 1); // NOI18N
        }

        private final FileObject myFileObject;
        private final ElementHandle<TypeElement> myHandle;
        private final String myAnnotation;
        private final String myMethodName;
        private final Set<String> myExistedMethods;
    }

    private final AtomicReference<WebSocketTask> runTask = new AtomicReference<WebSocketTask>();
}
