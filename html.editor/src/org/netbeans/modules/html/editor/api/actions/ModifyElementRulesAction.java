/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.api.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.ui.ModifyElementRulesPanel;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "action.name_modify.rules=Modify Rules"
})
public class ModifyElementRulesAction extends AbstractSourceElementAction {

    public ModifyElementRulesAction(FileObject file, String elementPath) {
        super(file, elementPath);
        putValue(Action.NAME, Bundle.action_name_modify_rules());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final ModifyElementRulesPanel panel = new ModifyElementRulesPanel(this);

        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                Bundle.action_name_modify_rules(),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (e.getSource().equals(DialogDescriptor.OK_OPTION)) {
                            applyChanges(panel);
                        }
                    }
                });

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
    }

    private void applyChanges(final ModifyElementRulesPanel panel) {
        final BaseDocument doc = (BaseDocument) DataLoadersBridge.getDefault().getDocument(file);
        doc.runAtomicAsUser(new Runnable() {
            @Override
            public void run() {
                try {
                    pos = Integer.MAX_VALUE;
                    diff = -1;
                    updateAttribute(doc, panel.getOriginalClassAttribute(), panel.getNewClassAttributeValue(), "class");
                    updateAttribute(doc, panel.getOriginalIdAttribute(), panel.getNewIdAttributeValue(), "id");
                    
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        invalidate();
    }

    int pos;
    int diff;

    private void updateAttribute(Document doc, Attribute a, String value, String name) throws BadLocationException {
        OpenTag ot = getSourceElement();
        Snapshot snap = getSnapshot();
        if(a == null && value == null) {
            return ; //no change
        }
        
        if(a == null && value != null) {
            //insert whole new attribute 
            int insertPos = snap.getOriginalOffset(ot.from() + 1 + ot.name().length());
            
            StringBuilder sb = new StringBuilder();
            sb.append(' ');
            sb.append(name);
            sb.append('=');
            sb.append('"');
            sb.append(value);
            sb.append('"');
            
            doc.insertString(insertPos, sb.toString(), null);
            
            pos = insertPos;
            diff = sb.length();
        } else if(a != null && value == null) {
            //remove
            int removeFrom = a.from();
            int removeTo = a.to();
            
            int rfdoc = snap.getOriginalOffset(removeFrom);
            int rtdoc = snap.getOriginalOffset(removeTo);
            
            if(rfdoc >= pos) {
                rfdoc += diff;
                rtdoc += diff;
            }
            
            doc.remove(rfdoc, rtdoc - rfdoc);
            
            pos = removeFrom;
            diff = rfdoc - rtdoc;
            
        } else {
            //change
            int removeFrom = a.from();
            int removeTo = a.to();
            
            int rfdoc = snap.getOriginalOffset(removeFrom);
            int rtdoc = snap.getOriginalOffset(removeTo);
            
            if(rfdoc >= pos) {
                rfdoc += diff;
                rtdoc += diff;
            }
            
            doc.remove(rfdoc, rtdoc - rfdoc);
            
            int insertPos = rfdoc;
            
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append('=');
            sb.append('"');
            sb.append(value);
            sb.append('"');
            
            doc.insertString(insertPos, sb.toString(), null);
            
            pos = insertPos;
            diff = rfdoc - rtdoc + sb.length();
        }
    }
    
    
}
