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

/*
 * AddProjectConfigurationAction.java
 *
 * Created on July 26, 2005, 6:59 PM
 *
 */
package org.netbeans.modules.mobility.editor.actions;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.mobility.antext.preprocessor.PPBlockInfo;
import org.netbeans.mobility.antext.preprocessor.PPLine;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class AddElifBlockAction extends PreprocessorEditorContextAction {
    
    public static final String NAME = "add-elif-block-action"; //NOI18N
    
    private WeakReference<JTextComponent> textComponent;
    private ArrayList<PPLine> preprocessorLineList;
    
    /** Creates a new instance of AddProjectConfigurationAction */
    public AddElifBlockAction() {
        super(NAME); //NOI18N
    }
    
    public boolean isEnabled(final ProjectConfigurationsHelper cfgProvider, final ArrayList<PPLine> preprocessorLineList, final JTextComponent target) {
        if (cfgProvider != null && preprocessorLineList != null && target != null) {
            this.textComponent = new WeakReference<JTextComponent>(target);
            this.preprocessorLineList = preprocessorLineList;
        } else this.textComponent = null;
        return cfgProvider != null && cfgProvider.isPreprocessorOn() && cfgProvider.getConfigurations().size() > 1 && isInsideIfChain(target, preprocessorLineList) && !overlapsBlockBorders(target, preprocessorLineList) && !overlapsGuardedBlocks(target);
    }
    
    public String getPopupMenuText(@SuppressWarnings("unused")
	final ProjectConfigurationsHelper cfgProvider, @SuppressWarnings("unused")
	final ArrayList preprocessorLineList, @SuppressWarnings("unused")
	final JTextComponent target) {
        return NbBundle.getMessage(AddElifBlockAction.class, "LBL_Add_Elif_Block");//NOI18N
    }
    
    private boolean isInsideIfChain(final JTextComponent target, final ArrayList<PPLine> preprocessorLineList) {
        final int x = getSelectionStartLine(target);
        if (x >= preprocessorLineList.size()) return false;
        final PPBlockInfo b = getBlock(target, preprocessorLineList);
        return b != null && (b.getType() == PPLine.IF || b.getType() == PPLine.IFDEF || b.getType() == PPLine.IFNDEF || b.getType() == PPLine.ELIF ||
                b.getType() == PPLine.ELIFDEF || b.getType() == PPLine.ELIFNDEF || b.getType() == PPLine.ELSE);
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final ActionEvent evt, final JTextComponent txt) {
        if (txt != null && this.textComponent != null && txt.equals(this.textComponent.get())) try {
            final PPBlockInfo b = getBlock(txt, this.preprocessorLineList);
            if (b != null) {
                int s, e;
                final BaseDocument doc = (BaseDocument)txt.getDocument();
                if (txt.getSelectionStart() == txt.getSelectionEnd()) {
                    //whole block copy
                    s = Utilities.getRowStartFromLineOffset(doc, b.getStartLine());
                    e = Utilities.getRowEnd(txt, Utilities.getRowStartFromLineOffset(doc, b.getEndLine() - (b.hasFooter() ? 2 : 1)));
                } else {
                    //selected rows copy
                    s = Utilities.getRowStartFromLineOffset(doc, getSelectionStartLine(txt) - 1);
                    e = Utilities.getRowEnd(txt, Utilities.getRowStartFromLineOffset(doc, getSelectionEndLine(txt) - 1));
                }
                final String text = txt.getText(s, e-s);
                NbDocument.runAtomic((StyledDocument)txt.getDocument(), new Runnable() {
                    public void run() {
                        try {
                            if (b.getType() == PPLine.ELSE) {
                                //insert new block before else
                                final int offs = Utilities.getRowStartFromLineOffset(doc, b.getStartLine() - 1);
                                doc.insertString(offs, "//#elif \n" + text + "\n", null); //NOI18N
                                txt.setSelectionStart(offs + 8);
                                txt.setSelectionEnd(offs + 8);
                            } else {
                                //insert new block after the current
                                final int offs = Utilities.getRowEnd(txt, Utilities.getRowStartFromLineOffset(doc, b.getEndLine() - (b.hasFooter() ? 2 : 1)));
                                doc.insertString(offs, "\n//#elif \n" + text, null); //NOI18N
                                txt.setSelectionStart(offs + 9);
                                txt.setSelectionEnd(offs + 9);
                            }
                        } catch (BadLocationException ble) {
                            ErrorManager.getDefault().notify(ble);
                        }
                        RecommentAction.actionPerformed(txt);
                    }
                });
                Completion.get().showCompletion();
            }
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        }
    }
}
