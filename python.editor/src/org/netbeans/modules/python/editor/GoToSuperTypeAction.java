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
package org.netbeans.modules.python.editor;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.GsfHtmlFormatter;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.gsf.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.netbeans.modules.gsfret.editor.hyperlink.DeclarationPopup;
import org.netbeans.modules.gsfret.editor.hyperlink.GoToSupport;
import org.netbeans.modules.gsfret.editor.hyperlink.PopupUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tor Norbye
 */
public class GoToSuperTypeAction extends BaseAction {
    @SuppressWarnings("deprecation")
    public GoToSuperTypeAction() {
        super("goto-super-implementation", SAVE_POSITION | ABBREV_RESET); // NOI18N

    }

    @Override
    public Class getShortDescriptionBundleClass() {
        return GoToSuperTypeAction.class;
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target.getCaret() == null) {
            return;
        }

        FileObject fo = GsfUtilities.findFileObject(target);
        BaseDocument doc = (BaseDocument)target.getDocument();

        if (fo != null) {
            // Cleanup import section: Remove newlines
            // Sort imports alphabetically
            // Split multi-imports into single splits
            // Look for missing imports: Take ALL calls,
            // and ensure we have imports for all of them.
            // (This means I need to have a complete index of all the builtins)
            // Combine multiple imports (from X import A,, from X import B,  etc. into single list)
            // Move imports that I think may be unused to the end - or just comment them out?

            // For imports: Gather imports from everywhere... move others into the same section
            CompilationInfo info = null;

            SourceModel model = SourceModelFactory.getInstance().getModel(fo);
            if (model != null) {
                final CompilationInfo[] infoHolder = new CompilationInfo[1];
                try {
                    model.runUserActionTask(new CancellableTask<CompilationInfo>() {
                        public void cancel() {
                        }

                        public void run(CompilationInfo info) throws Exception {
                            infoHolder[0] = info;
                        }
                    }, false);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                info = infoHolder[0];
            }
            if (info != null && PythonAstUtils.getRoot(info) != null) {
                // Figure out if we're on a method, and if so, locate the nearest
                // method it is overriding.
                // Otherwise, if we're on a class (anywhere, not just definition),
                // go to the super class.
                PythonDeclarationFinder finder = new PythonDeclarationFinder();
                int offset = target.getCaretPosition();
                DeclarationLocation location = finder.getSuperImplementations(info, offset);
                if (location == DeclarationLocation.NONE) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    if (location.getAlternativeLocations().size() > 0 &&
                            !PopupUtil.isPopupShowing()) {
                        // Many alternatives - pop up a dialog and make the user choose
                        if (chooseAlternatives(doc, offset, location.getAlternativeLocations())) {
                            return;
                        }
                    }

                    GsfUtilities.open(location.getFileObject(), location.getOffset(), null);
                }
            }
        }
    }

    // COPY FROM GSF's GOTOSUPPORT!!!
    private static boolean chooseAlternatives(Document doc, int offset, List<AlternativeLocation> alternatives) {
        Collections.sort(alternatives);

        // Prune results a bit
        int MAX_COUNT = 30; // Don't show more items than this
        String previous = "";
        GsfHtmlFormatter formatter = new GsfHtmlFormatter();
        int count = 0;
        List<AlternativeLocation> pruned = new ArrayList<AlternativeLocation>(alternatives.size());
        for (AlternativeLocation alt : alternatives) {
            String s = alt.getDisplayHtml(formatter);
            if (!s.equals(previous)) {
                pruned.add(alt);
                previous = s;
                count++;
                if (count == MAX_COUNT) {
                    break;
                }
            }
        }
        alternatives = pruned;
        if (alternatives.size() <= 1) {
            return false;
        }

        JTextComponent target = findEditor(doc);
        if (target != null) {
            try {
                Rectangle rectangle = target.modelToView(offset);
                Point point = new Point(rectangle.x, rectangle.y + rectangle.height);
                SwingUtilities.convertPointToScreen(point, target);

                String caption = NbBundle.getMessage(GoToSupport.class, "ChooseDecl");
                PopupUtil.showPopup(new DeclarationPopup(caption, alternatives), caption, point.x, point.y, true, 0);

                return true;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return false;
    }

    /** TODO - MOVE TO UTILITTY LIBRARY */
    private static JTextComponent findEditor(Document doc) {
        JTextComponent comp = EditorRegistry.lastFocusedComponent();
        if (comp.getDocument() == doc) {
            return comp;
        }
        List<? extends JTextComponent> componentList = EditorRegistry.componentList();
        for (JTextComponent component : componentList) {
            if (comp.getDocument() == doc) {
                return comp;
            }
        }

        return null;
    }
}
