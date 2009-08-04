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
package org.netbeans.modules.cnd.debugger.gdb.models;

import java.awt.AWTKeyStroke;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.cnd.debugger.gdb.ui.FilteredKeymap;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbContextBinding;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 * A GUI panel for customizing a Watch.
 *
 * @author Maros Sandor
 */
public class WatchPanel {

    private JPanel panel;
    private JEditorPane editorPane;
    private String expression;

    public WatchPanel(String expression) {
        this.expression = expression;
    }

    public JComponent getPanel() {
        if (panel != null) {
            return panel;
        }

        panel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);

        panel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_WatchPanel")); // NOI18N
        JLabel textLabel = new JLabel();
        Mnemonics.setLocalizedText(textLabel, bundle.getString("CTL_Watch_Name")); // NOI18N
        editorPane = new JEditorPane();//expression); // NOI18N
        editorPane.setText(expression);

        ActionListener editorPaneUpdated = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                editorPane.setText(expression);
                editorPane.selectAll();
            }
        };
        GdbContextBinding.getInstance().setupContext(editorPane, editorPaneUpdated);

        JScrollPane sp = createScrollableLineEditor(editorPane);
        FontMetrics fm = editorPane.getFontMetrics(editorPane.getFont());
        int size = 2 * fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent() + 2;
        Insets eInsets = editorPane.getInsets();
        Insets spInsets = sp.getInsets();
        sp.setPreferredSize(new Dimension(30 * size,
                size + 2 +
                eInsets.bottom + eInsets.top +
                spInsets.bottom + spInsets.top));

        textLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(11, 12, 1, 11));
        panel.add(BorderLayout.NORTH, textLabel);
        panel.add(BorderLayout.CENTER, sp);

        editorPane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Watch_Name")); // NOI18N
        editorPane.setText(expression);
        editorPane.selectAll();

        textLabel.setLabelFor(editorPane);
        HelpCtx.setHelpIDString(editorPane, "debug.customize.watch"); // NOI18N
        editorPane.requestFocus();

        return panel;
    }

    public String getExpression() {
        return editorPane.getText().trim();
    }

    public static JScrollPane createScrollableLineEditor(JEditorPane editorPane) {
        editorPane.setKeymap(new FilteredKeymap(editorPane));
        final JScrollPane sp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        editorPane.setBorder(
                new CompoundBorder(editorPane.getBorder(),
                new EmptyBorder(0, 0, 0, 0)));

        JTextField referenceTextField = new JTextField("M"); // NOI18N
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(referenceTextField.getBackground());
        sp.setBorder(referenceTextField.getBorder());
        sp.setBackground(referenceTextField.getBackground());

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        panel.add(editorPane, gridBagConstraints);
        sp.setViewportView(panel);

        int preferredHeight = referenceTextField.getPreferredSize().height;
        if (sp.getPreferredSize().height < preferredHeight) {
            sp.setPreferredSize(referenceTextField.getPreferredSize());
        }
        sp.setMinimumSize(sp.getPreferredSize());

        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
        return sp;
    }

    private static final class Context {

        public final FileObject fileObject;
        public final int line;
        private Context(FileObject fo, int contextLine) {
            this.fileObject = fo;
            this.line = contextLine <= 0 ? 0 : contextLine;
        }
    }
}
