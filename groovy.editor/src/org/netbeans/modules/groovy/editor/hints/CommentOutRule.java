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
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.hints.spi.Fix;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.openide.util.Exceptions;

public class CommentOutRule implements SelectionRule {
    
    public static final Logger LOG = Logger.getLogger(CommentOutRule.class.getName()); // NOI18N
    
    String desc = "Comment out selected text";

    public void run(RuleContext context, List<Description> result) {
        CompilationInfo info = context.compilationInfo;
        int start = context.selectionStart;
        int end = context.selectionEnd;
        
        assert start < end;
        
        BaseDocument doc;
        try {
            doc = (BaseDocument) info.getDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        
        if (end > doc.getLength()) {
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
        
        // this is just a dummy fix to see the lightbulb ...
        Fix fix = new SimpleFix(desc);
        OffsetRange range = new OffsetRange(start, end);
        
        List<Fix> fixList = new ArrayList<Fix>(1);
        fixList.add(fix);
        Description desc = new Description(this, fix.getDescription(), info.getFileObject(), range,
                fixList, 292);
        result.add(desc);
        
        return;
    }

    public boolean appliesTo(CompilationInfo compilationInfo) {
        return true;
    }

    public String getDisplayName() {
        return desc;
    }

    public boolean showInTasklist() {
        return false;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }
    
    private class SimpleFix implements Fix {
        
        String desc;

        public SimpleFix(String desc) {
            this.desc = desc;
        }

        public String getDescription() {
            return desc;
        }

        public void implement() throws Exception {
            return;
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
        
    }
    
    

}
