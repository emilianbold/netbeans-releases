/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.overridden;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@EditorActionRegistration(
        name = "goto-implementation",
        mimeType = JavaKit.JAVA_MIME_TYPE,
        popupText = "#CTL_GoToImplementation"
)
public final class GoToImplementation extends BaseAction {

    public GoToImplementation() {
        super(SAVE_POSITION | ABBREV_RESET);
//        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(GoToImplementation.class, "CTL_GoToImplementation"));
//        String name = NbBundle.getMessage(GoToImplementation.class, "CTL_GoToImplementation_trimmed");
//        putValue(ExtKit.TRIMMED_TEXT,name);
//        putValue(POPUP_MENU_TEXT, name);
    }

    public void actionPerformed(ActionEvent e, final JTextComponent c) {
        try {
            JavaSource.forDocument(c.getDocument()).runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    
                    Element el = resolveElement(parameter, c.getCaretPosition());

                    if (el == null) {
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GoToImplementation.class, "LBL_NoMethod"));
                        return ;
                    }

                    TypeElement type = el.getKind() == ElementKind.METHOD ? (TypeElement) el.getEnclosingElement() : (TypeElement) el;
                    ExecutableElement method = el.getKind() == ElementKind.METHOD ? (ExecutableElement) el : null;

                    Map<ElementHandle<? extends Element>, List<ElementDescription>> overriding = new ComputeOverriders(new AtomicBoolean()).process(parameter, type, method, true);

                    List<ElementDescription> overridingMethods = overriding != null ? overriding.get(ElementHandle.create(el)) : null;

                    if (overridingMethods == null || overridingMethods.isEmpty()) {
                        String key = el.getKind() == ElementKind.METHOD ? "LBL_NoOverridingMethod" : "LBL_NoOverridingType";

                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GoToImplementation.class, key));
                        return;
                    }
                    
                    Point p = new Point(c.modelToView(c.getCaretPosition()).getLocation());

                    SwingUtilities.convertPointToScreen(p, c);
                    
                    performGoToAction(overridingMethods, p, method != null);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    static void performGoToAction(List<ElementDescription> declarations, Point position, boolean method) {
        String caption = NbBundle.getMessage(GoToImplementation.class, method ? "LBL_ImplementorsOverridersMethod" : "LBL_ImplementorsOverridersClass");
        
        PopupUtil.showPopup(new IsOverriddenPopup(caption, declarations), caption, position.x, position.y, true, 0);
    }

    private static Set<ElementKind> SUPPORTED_ELEMENTS = EnumSet.of(ElementKind.METHOD, ElementKind.ANNOTATION_TYPE, ElementKind.CLASS, ElementKind.ENUM, ElementKind.INTERFACE);
    
    private static Element resolveElement(CompilationInfo info, int caret) {
        TreePath tp = info.getTreeUtilities().pathFor(caret);

        TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        ts.move(caret);

        boolean isIdentifier = ts.moveNext() && ts.token().id() == JavaTokenId.IDENTIFIER;

        if (!isIdentifier) {
            ts.move(caret);
            isIdentifier = ts.movePrevious() && ts.token().id() == JavaTokenId.IDENTIFIER;
        }
        
        if (isIdentifier) {
            Element  elementUnderCaret = info.getTrees().getElement(tp);

            if (elementUnderCaret != null && SUPPORTED_ELEMENTS.contains(elementUnderCaret.getKind())) {
                return (Element) elementUnderCaret;
            }
        }

        Element el = resolveHeader(tp, info, caret);

        if (el == null) {
            tp = info.getTreeUtilities().pathFor(caret + 1);
            el = resolveHeader(tp, info, caret);
        }

        return el;
    }
    
    private static Element resolveHeader(TreePath tp, CompilationInfo info, int caret) {
        while (tp.getLeaf().getKind() != Kind.METHOD && tp.getLeaf().getKind() != Kind.CLASS && tp.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            tp = tp.getParentPath();
        }

        if (tp.getLeaf().getKind() == Kind.COMPILATION_UNIT) {
            return null;
        }

        long bodyStart;

        if (tp.getLeaf().getKind() == Kind.METHOD) {
            MethodTree mt = (MethodTree) tp.getLeaf();
            SourcePositions sp = info.getTrees().getSourcePositions();
            BlockTree body = mt.getBody();
            bodyStart = body != null ? sp.getStartPosition(info.getCompilationUnit(), body) : Integer.MAX_VALUE;
        } else {
            assert tp.getLeaf().getKind() == Kind.CLASS;
            Document doc = info.getSnapshot().getSource().getDocument(false);

            if (doc == null) {
                return null;
            }

            bodyStart = Utilities.findBodyStart(tp.getLeaf(), info.getCompilationUnit(), info.getTrees().getSourcePositions(), doc);
        }

        if (caret >= bodyStart) {
            return null;
        }

        Element el = info.getTrees().getElement(tp);

        if (el == null || !SUPPORTED_ELEMENTS.contains(el.getKind())) {
            return null;
        }
        
        return (Element) el;
    }

}
