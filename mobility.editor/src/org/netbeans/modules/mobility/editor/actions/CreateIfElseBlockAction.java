/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * AddProjectConfigurationAction.java
 *
 * Created on July 26, 2005, 6:59 PM
 *
 */
package org.netbeans.modules.mobility.editor.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.mobility.antext.preprocessor.PPLine;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class CreateIfElseBlockAction extends PreprocessorEditorContextAction {
    
    public static final String NAME = "create-if-else-block-action"; //NOI18N
    
    /** Creates a new instance of AddProjectConfigurationAction */
    public CreateIfElseBlockAction() {
        super(NAME); //NOI18N
    }
    
    public boolean isEnabled(final ProjectConfigurationsHelper cfgProvider, final ArrayList<PPLine> preprocessorLineList, final JTextComponent target) {
        return cfgProvider != null && cfgProvider.isPreprocessorOn() && cfgProvider.getConfigurations().size() > 1 && !overlapsBlockBorders(target, preprocessorLineList) && !overlapsGuardedBlocks(target);
    }
    
    public String getPopupMenuText(@SuppressWarnings("unused")
	final ProjectConfigurationsHelper cfgProvider, final ArrayList<PPLine> preprocessorLineList, final JTextComponent target) {
        return getBlock(target, preprocessorLineList) != null ? NbBundle.getMessage(CreateIfElseBlockAction.class, "LBL_Create_Nested_If_Else_Block") : NbBundle.getMessage(CreateIfElseBlockAction.class, "LBL_Create_If_Else_Block");//NOI18N
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final ActionEvent evt, final JTextComponent txt) {
        final ProjectConfigurationProvider prov = J2MEProjectUtils.getConfigProviderForDoc(txt.getDocument());
        if (prov == null) return;
        NbDocument.runAtomic((StyledDocument)txt.getDocument(), new Runnable() {
            public void run() {
                try {
                    final BaseDocument doc = (BaseDocument)txt.getDocument();
                    final int s = Utilities.getRowStartFromLineOffset(doc, getSelectionStartLine(txt) - 1);
                    final int e = Utilities.getRowEnd(txt, Utilities.getRowStartFromLineOffset(doc, getSelectionEndLine(txt) - 1));
                    final String text = doc.getText(s, e-s);
                    doc.insertString(e,  "\n//#else\n" + text + "\n//#endif", null); //NOI18N
                    doc.insertString(s, "//#if \n", null); //NOI18N
                    txt.setSelectionStart(s + 6);
                    txt.setSelectionEnd(s + 6);
                } catch (BadLocationException ble) {
                    ErrorManager.getDefault().notify(ble);
                }
                RecommentAction.actionPerformed(txt);
            }
        });
        Completion.get().showCompletion();
    }
}
