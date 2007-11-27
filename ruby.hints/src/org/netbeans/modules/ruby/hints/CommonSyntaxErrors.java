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
package org.netbeans.modules.ruby.hints;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Error;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.ErrorRule;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Rule which identifies common syntax errors and offers to help
 * 
 * @author Tor Norbye
 */
public class CommonSyntaxErrors implements ErrorRule {

    public Set<String> getCodes() {
        // Add more as necessary
        return Collections.singleton("Syntax error, unexpected '=' "); // NOI18N
    }

    public void run(CompilationInfo info, Error error, List<Description> result) {
        // See if it's a "begin"
        try {
            // TODO - if we get many codes, switch on these here!
            BaseDocument doc = (BaseDocument) info.getDocument();
            int offset = error.getStartPosition().getOffset();
            if ((offset < doc.getLength()-"begin".length()) && // NOI18N
                    "begin".equals(doc.getText(offset, "begin".length()))) { // NOI18N
                OffsetRange range = new OffsetRange(offset-1, offset+"=begin".length());
                Fix fix = new FixDocIndent(info, offset-1);
                List<Fix> fixList = Collections.singletonList(fix);
                String displayName = NbBundle.getMessage(CommonSyntaxErrors.class, "DontIndentDocs");
                Description desc = new Description(this, displayName, info.getFileObject(), range, fixList, 500);
                result.add(desc);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public boolean appliesTo(CompilationInfo compilationInfo) {
        return true;
    }

    public String getDisplayName() {
        return "X";
    }

    private class FixDocIndent implements Fix {
        private CompilationInfo info;
        private int equalOffset;
        
        private FixDocIndent(CompilationInfo info, int equalOffset) {
            this.info = info;
            this.equalOffset = equalOffset;
        }

        public String getDescription() {
            return NbBundle.getMessage(CommonSyntaxErrors.class, "ReindentBegin");
        }

        public void implement() throws Exception {
            // Move code - but I've gotta make sure I create a new line if necessary
            try {
                BaseDocument doc = (BaseDocument) info.getDocument();
                
                if (equalOffset > doc.getLength()) {
                    return; // recent edits
                }

                try {
                    doc.atomicLock();
                    int rowStart = Utilities.getRowStart(doc, equalOffset);
                    if (Utilities.getRowFirstNonWhite(doc, equalOffset) < equalOffset) {
                        // There's something else on this line! Create a newline instead!
                        doc.insertString(equalOffset, "\n", null);
                    } else {
                        doc.remove(rowStart, equalOffset-rowStart);
                    }
                    int nextRow = Utilities.getRowEnd(doc, rowStart)+1;
                    if (nextRow < doc.getLength()) {
                        String text = doc.getText(nextRow, doc.getLength()-nextRow);
                        int index = text.indexOf("=end");
                        if (index != -1) {
                            int beginIndex = text.indexOf("=begin");
                            if (index < beginIndex || beginIndex == -1) {
                                int offset = nextRow+index;
                                rowStart = Utilities.getRowStart(doc, offset);
                                if (Utilities.getRowFirstNonWhite(doc, offset) < offset) {
                                    // There's something else on this line! Create a newline instead!
                                    doc.insertString(offset, "\n", null);
                                } else {
                                    doc.remove(rowStart, offset-rowStart);
                                }

                            }
                        }
                    }
                } finally {
                    doc.atomicUnlock();
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

}
