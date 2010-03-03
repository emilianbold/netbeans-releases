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
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.java.GoToSupport;
import org.netbeans.modules.editor.java.GoToSupport.Context;
import org.netbeans.modules.editor.java.JavaKit;
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
        goToImplementation(c);
    }

    public static void goToImplementation(final JTextComponent c) {
        try {
            JavaSource.forDocument(c.getDocument()).runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    
                    Context context = GoToSupport.resolveContext(parameter, c.getDocument(), c.getCaretPosition(), false);

                    if (context == null || !SUPPORTED_ELEMENTS.contains(context.resolved.getKind())) {
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GoToImplementation.class, "LBL_NoMethod"));
                        return ;
                    }

                    Element el = context.resolved;

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
    
}
