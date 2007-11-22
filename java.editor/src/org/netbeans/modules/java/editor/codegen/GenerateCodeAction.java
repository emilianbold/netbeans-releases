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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.util.TreePath;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.MainMenuAction;
import org.netbeans.modules.java.editor.codegen.ui.GenerateCodePanel;
import org.netbeans.modules.java.editor.overridden.PopupUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 */
public class GenerateCodeAction extends BaseAction {

    public static final String generateCode = "generate-code"; //NOI18N
    
    public GenerateCodeAction(){
        super(generateCode);
        putValue(ExtKit.TRIMMED_TEXT, NbBundle.getBundle(GenerateCodeAction.class).getString("generate-code-trimmed")); //NOI18N
        putValue(SHORT_DESCRIPTION, NbBundle.getBundle(GenerateCodeAction.class).getString("desc-generate-code")); //NOI18N
        putValue(POPUP_MENU_TEXT, NbBundle.getBundle(GenerateCodeAction.class).getString("popup-generate-code")); //NOI18N
    }
    
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        try {
            JavaSource js = JavaSource.forDocument(target.getDocument());
            if (js != null) {
                final int caretOffset = target.getCaretPosition();
                final ArrayList<CodeGenerator> gens = new ArrayList<CodeGenerator>();
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController controller) throws Exception {
                        controller.toPhase(JavaSource.Phase.PARSED);
                        TreePath path = controller.getTreeUtilities().pathFor(caretOffset);
                        for (CodeGenerator.Factory factory : getCodeGeneratorFactories()) {
                            for (CodeGenerator gen : factory.create(controller, path))
                                gens.add(gen);
                        }
                    }
                }, true);
                if (gens.size() > 0) {
                    Rectangle carretRectangle = target.modelToView(target.getCaretPosition());
                    Point where = new Point( carretRectangle.x, carretRectangle.y + carretRectangle.height );
                    SwingUtilities.convertPointToScreen( where, target);
                    GenerateCodePanel panel = new GenerateCodePanel(target, gens);
                    PopupUtil.showPopup(panel, null, where.x, where.y, true, carretRectangle.height);
                } else {
                    target.getToolkit().beep();
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }
    
    private Iterable<? extends CodeGenerator.Factory> getCodeGeneratorFactories() {
        return MimeLookup.getLookup("text/x-java").lookupAll(CodeGenerator.Factory.class);
    }
    
    public static final class GlobalAction extends MainMenuAction {

        private final JMenuItem menuPresenter;
        
        public GlobalAction() {
            super();
            this.menuPresenter = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText() {
            return NbBundle.getBundle(GlobalAction.class).getString("generate-code-main-menu-source-item"); //NOI18N
        }

        protected String getActionName() {
            return generateCode;
        }

        public JMenuItem getMenuPresenter() {
            return menuPresenter;
        }
        
    } // End of GlobalAction class
}
