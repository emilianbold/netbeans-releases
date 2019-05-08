/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelui.trace;

import java.awt.Dialog;
import org.netbeans.modules.cnd.modelimpl.trace.TraceUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 */
public class ParserOptions extends javax.swing.JPanel {
    private static final String CND_MODELIMPL_EXCL_COMPOUND = "cnd.modelimpl.excl.compound"; // NOI18N
    private static final String CND_MODELIMPL_CPP_PARSER_ACTION = "cnd.modelimpl.cpp.parser.action"; // NOI18N
    private static final String CND_MODELIMPL_CPP_PARSER_ACTION_TRACE = "cnd.modelimpl.cpp.parser.action.trace"; // NOI18N
    private static final String CND_MODELIMPL_CPP_PARSER_NEW_GRAMMAR = "cnd.modelimpl.cpp.parser.new.grammar"; // NOI18N
    private static final String CND_MODELIMPL_PARSE_HEADERS_WITH_SOURCES = "cnd.modelimpl.parse.headers.with.sources"; // NOI18N
    private static final String CND_MODELIMPL_CPP_PARSER_RULES_TRACE = "cnd.modelimpl.cpp.parser.rules.trace"; // NOI18N
    private static final String CND_MODELIMPL_CPP_PARSER_SHOW_AST = "cnd.modelimpl.cpp.parser.show.AST"; // NOI18N

    /**
     * Creates new form ParserOptions
     */
    public ParserOptions() {
        initComponents();
        cppParserAction.setSelected(Boolean.getBoolean(CND_MODELIMPL_CPP_PARSER_ACTION));
        excludeCompound.setSelected(Boolean.getBoolean(CND_MODELIMPL_EXCL_COMPOUND));
        newGrammar.setSelected(Boolean.getBoolean(CND_MODELIMPL_CPP_PARSER_NEW_GRAMMAR));
        parseHeadersWithSources.setSelected(Boolean.getBoolean(CND_MODELIMPL_PARSE_HEADERS_WITH_SOURCES));
        traceParserAction.setSelected(Boolean.getBoolean(CND_MODELIMPL_CPP_PARSER_ACTION_TRACE));
        traceParserRule.setSelected(Boolean.getBoolean(CND_MODELIMPL_CPP_PARSER_RULES_TRACE));
        showAST.setSelected(Boolean.getBoolean(CND_MODELIMPL_CPP_PARSER_SHOW_AST));
    }

    static void showDialog() {
        ParserOptions panel = new ParserOptions();
        DialogDescriptor dd = new DialogDescriptor(panel,
                NbBundle.getMessage(ParserOptions.class, "ParserOptionsTitle"), true, // NOI18N
                DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setMinimumSize(dialog.getPreferredSize());
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            TraceUtils.updateTraceFlag(CND_MODELIMPL_CPP_PARSER_ACTION, panel.cppParserAction.isSelected());
            TraceUtils.updateTraceFlag(CND_MODELIMPL_EXCL_COMPOUND, panel.excludeCompound.isSelected());
            TraceUtils.updateTraceFlag(CND_MODELIMPL_CPP_PARSER_NEW_GRAMMAR, panel.newGrammar.isSelected());
            TraceUtils.updateTraceFlag(CND_MODELIMPL_PARSE_HEADERS_WITH_SOURCES, panel.parseHeadersWithSources.isSelected());
            TraceUtils.updateTraceFlag(CND_MODELIMPL_CPP_PARSER_ACTION_TRACE, panel.traceParserAction.isSelected());
            TraceUtils.updateTraceFlag(CND_MODELIMPL_CPP_PARSER_RULES_TRACE, panel.traceParserRule.isSelected());
            TraceUtils.updateTraceFlag(CND_MODELIMPL_CPP_PARSER_SHOW_AST, panel.showAST.isSelected());
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newGrammar = new javax.swing.JCheckBox();
        parseHeadersWithSources = new javax.swing.JCheckBox();
        cppParserAction = new javax.swing.JCheckBox();
        excludeCompound = new javax.swing.JCheckBox();
        traceParserRule = new javax.swing.JCheckBox();
        traceParserAction = new javax.swing.JCheckBox();
        showAST = new javax.swing.JCheckBox();
        oldOnly = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(newGrammar, org.openide.util.NbBundle.getMessage(ParserOptions.class, "ParserOptions.newGrammar.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(parseHeadersWithSources, org.openide.util.NbBundle.getMessage(ParserOptions.class, "ParserOptions.parseHeadersWithSources.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cppParserAction, org.openide.util.NbBundle.getMessage(ParserOptions.class, "ParserOptions.cppParserAction.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(excludeCompound, org.openide.util.NbBundle.getMessage(ParserOptions.class, "ParserOptions.excludeCompound.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(traceParserRule, org.openide.util.NbBundle.getMessage(ParserOptions.class, "ParserOptions.traceParserRule.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(traceParserAction, org.openide.util.NbBundle.getMessage(ParserOptions.class, "ParserOptions.traceParserAction.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showAST, org.openide.util.NbBundle.getMessage(ParserOptions.class, "ParserOptions.showAST.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(oldOnly, org.openide.util.NbBundle.getMessage(ParserOptions.class, "ParserOptions.oldOnly.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cppParserAction)
                    .addComponent(newGrammar)
                    .addComponent(excludeCompound)
                    .addComponent(traceParserRule)
                    .addComponent(traceParserAction)
                    .addComponent(showAST)
                    .addComponent(oldOnly)
                    .addComponent(parseHeadersWithSources))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(newGrammar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excludeCompound)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(traceParserRule)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(traceParserAction)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showAST)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(oldOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parseHeadersWithSources)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cppParserAction)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cppParserAction;
    private javax.swing.JCheckBox excludeCompound;
    private javax.swing.JCheckBox newGrammar;
    private javax.swing.JLabel oldOnly;
    private javax.swing.JCheckBox parseHeadersWithSources;
    private javax.swing.JCheckBox showAST;
    private javax.swing.JCheckBox traceParserAction;
    private javax.swing.JCheckBox traceParserRule;
    // End of variables declaration//GEN-END:variables
}
