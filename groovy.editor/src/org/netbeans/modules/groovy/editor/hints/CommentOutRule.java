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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.editor.hints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.groovy.editor.hints.spi.Description;
import org.netbeans.modules.groovy.editor.hints.spi.HintSeverity;
import org.netbeans.modules.groovy.editor.hints.spi.RuleContext;
import org.netbeans.modules.groovy.editor.hints.spi.SelectionRule;
import org.netbeans.modules.gsf.api.CompilationInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.Formatter;
import org.netbeans.modules.groovy.editor.hints.spi.EditList;
import org.netbeans.modules.groovy.editor.hints.spi.Fix;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class CommentOutRule implements SelectionRule {
    
    public static final Logger LOG = Logger.getLogger(CommentOutRule.class.getName()); // NOI18N
    String bulbDesc = NbBundle.getMessage(CommentOutRule.class, "CommentOutRuleDescription");
    
    enum OPERATION { COMMENT_OUT, ADD_IF };

    public void run(RuleContext context, List<Description> result) {
        CompilationInfo info = context.compilationInfo;
        int start = context.selectionStart;
        int end = context.selectionEnd;
        
        assert start < end;
        
        BaseDocument baseDoc;
        try {
            baseDoc = (BaseDocument) info.getDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        
        if (end > baseDoc.getLength()) {
            return;
        }

        if (end-start > 1000) {
            // Avoid doing tons of work when the user does a Ctrl-A to select all in a really
            // large buffer.
            return;
        }
        
        ASTNode root = AstUtilities.getRoot(info);
        
        if (root == null) {
            return;
        }
        
        OffsetRange range = new OffsetRange(start, end);

        result.add(getDescriptor(OPERATION.COMMENT_OUT, "CommentOutRuleHintDescription", context, baseDoc, range));
        result.add(getDescriptor(OPERATION.ADD_IF, "AddIfAroundBlockHintDescription", context, baseDoc, range));
        
        return;
    }
    
    Description getDescriptor(OPERATION operation, String bulbDescriptionMsgBundle, RuleContext context,
            BaseDocument baseDoc, OffsetRange range) {

        int DEFAULT_PRIORITY = 292;
        String descriptionString = NbBundle.getMessage(CommentOutRule.class, bulbDescriptionMsgBundle);
        Fix fixToApply = new SimpleFix(operation, descriptionString, baseDoc, context);

        List<Fix> fixList = new ArrayList<Fix>(1);
        fixList.add(fixToApply);
        Description descriptor = new Description(this, fixToApply.getDescription(), context.compilationInfo.getFileObject(), range,
                fixList, DEFAULT_PRIORITY);

        return descriptor;
    }
    

    public boolean appliesTo(CompilationInfo compilationInfo) {
        return true;
    }

    public String getDisplayName() {
        return bulbDesc;
    }

    public boolean showInTasklist() {
        return false;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
    
    
    private class SimpleFix implements Fix {
        BaseDocument baseDoc;
        String desc;
        RuleContext context;
        OPERATION operation;

        public SimpleFix(OPERATION operation ,String desc, BaseDocument baseDoc, RuleContext context) {
            this.desc = desc;
            this.baseDoc = baseDoc;
            this.context = context;
            this.operation = operation;
        }

        public String getDescription() {
            return desc;
        }

        public void implement() throws Exception {
            EditList edits = new EditList(baseDoc);
            
            int start = context.selectionStart;
            int end = context.selectionEnd;
            
            JTextComponent component = Utilities.getFocusedComponent();
            
            switch(operation){
                case COMMENT_OUT:
                    edits.replace(end, 0, "*/", false, 0);
                    edits.replace(start, 0, "/*", false, 1);
                    edits.apply();

                    // Clear selection 
                    component.setCaretPosition(start);
                    
                    break;
                case ADD_IF:
                    String START_INSERT = "if (true) {\n";
                    String END_INSERT = "\n}";
                    
                    edits.replace(end, 0, END_INSERT, false, 0);
                    
                    int startOfRow = Utilities.getRowStart(baseDoc, start);
                    
                    edits.replace(startOfRow, 0, START_INSERT, false, 1);
                    edits.format();
                    edits.apply();
                    
                    component.setCaretPosition(start + 4);
                    component.moveCaretPosition(start + 8);
                    
                    break;
            }
            
            
            return;
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return false;
        }
        
    }

}
