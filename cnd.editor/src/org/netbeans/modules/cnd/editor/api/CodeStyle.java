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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.api;

import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.cnd.editor.cplusplus.CKit;
import org.netbeans.modules.cnd.editor.options.EditorOptions;

/**
 *
 * @author Alexander Simon
 */
public class CodeStyle {
    private static CodeStyle INSTANCE_C;
    private static CodeStyle INSTANCE_CPP;
    static {
        EditorOptions.codeStyleProducer = new Producer();
    }

    private Preferences preferences;
    
    private CodeStyle(Preferences preferences) {
        this.preferences = preferences;
    }
    
    public synchronized static CodeStyle getDefault(Language language) {
        switch(language) {
            case C:
                if (INSTANCE_C == null) {
                    INSTANCE_C = create(language);
                }
                return INSTANCE_C;
            case CPP:
            default:
                if (INSTANCE_CPP == null) {
                    INSTANCE_CPP = create(language);
                }
                return INSTANCE_CPP;
        }
    }

    public synchronized static CodeStyle getDefault(Document doc) {
        if (doc instanceof BaseDocument) {
            Formatter f = ((BaseDocument)doc).getFormatter();
            if (f instanceof ExtFormatter) {
                if (CKit.class.equals(f.getKitClass())) {
                    return getDefault(Language.C);
                }
            }
        }
        return getDefault(Language.CPP);
    }
    
    static CodeStyle create(Language language) {
        return new CodeStyle(EditorOptions.getPreferences(EditorOptions.getCurrentProfileId(language)));
    }
    
    // General tabs and indents ------------------------------------------------
    public boolean getFormatSpaceBeforeParenthesis() {
        return getOption(EditorOptions.CC_FORMAT_SPACE_BEFORE_PARENTHESIS,
                         EditorOptions.defaultCCFormatSpaceBeforeParenthesis);
    }

    public boolean getFormatSpaceAfterComma() {
        return getOption(EditorOptions.CC_FORMAT_SPACE_AFTER_COMMA,
                         EditorOptions.defaultCCFormatSpaceAfterComma);
    }

    public BracePlacement getFormatNewlineBeforeBrace() {
        return BracePlacement.valueOf(getOption(EditorOptions.CC_FORMAT_NEWLINE_BEFORE_BRACE,
                                      EditorOptions.defaultCCFormatNewlineBeforeBrace));
    }

    public BracePlacement getFormatNewlineBeforeBraceDeclaration() {
        return BracePlacement.valueOf(getOption(EditorOptions.CC_FORMAT_NEWLINE_BEFORE_BRACE_DECLARATION,
                                      EditorOptions.defaultCCFormatNewlineBeforeBraceDeclaration));
    }

    public BracePlacement getFormatNewlineBeforeBraceClass() {
        return BracePlacement.valueOf(getOption(EditorOptions.CC_FORMAT_NEWLINE_BEFORE_BRACE_CLASS,
                                      EditorOptions.defaultCCFormatNewlineBeforeBraceClass));
    }

    public BracePlacement getFormatNewlineBeforeBraceMethod() {
        return BracePlacement.valueOf(getOption(EditorOptions.CC_FORMAT_NEWLINE_BEFORE_BRACE_METHOD,
                                      EditorOptions.defaultCCFormatNewlineBeforeBraceMethod));
    }
    
    public boolean getFormatPreprocessorAtLineStart(){
        return getOption(EditorOptions.CC_FORMAT_PREPROCESSOR_AT_LINE_START,
                         EditorOptions.defaulCCtFormatPreprocessorAtLineStart);
    }
            
    public boolean getFormatLeadingSpaceInComment() {
        return getOption(EditorOptions.CC_FORMAT_LEADING_SPACE_IN_COMMENT,
                         EditorOptions.defaultCCFormatLeadingSpaceInComment);
    }

    public boolean getFormatLeadingStarInComment() {
        return getOption(EditorOptions.CC_FORMAT_LEADING_STAR_IN_COMMENT,
                         EditorOptions.defaultCCFormatLeadingStarInComment);
    }

    public int getFormatStatementContinuationIndent() {
        return getOption(EditorOptions.CC_FORMAT_STATEMENT_CONTINUATION_INDENT,
                         EditorOptions.defaultCCFormatStatementContinuationIndent);
    }

    public boolean spaceAroundUnaryOps() {
        return getOption(EditorOptions.spaceAroundUnaryOps,
                         EditorOptions.spaceAroundUnaryOpsDefault);
    }

    public boolean spaceAroundBinaryOps() {
        return getOption(EditorOptions.spaceAroundBinaryOps,
                         EditorOptions.spaceAroundBinaryOpsDefault);
    }

    public boolean spaceAroundAssignOps() {
        return getOption(EditorOptions.spaceAroundAssignOps,
                         EditorOptions.spaceAroundAssignOpsDefault);
    }
    
    private boolean getOption(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    private int getOption(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    private String getOption(String key, String defaultValue) {
        return preferences.get(key, defaultValue);
    }

    // for tests only
    public Preferences getPreferences(){
        return this.preferences;
    }
    
    // Nested classes ----------------------------------------------------------
    public enum Language {
        C,
        CPP,
    }

    public enum BracePlacement {
        SAME_LINE,
        NEW_LINE,
    }

    // Communication with non public packages ----------------------------------
    private static class Producer implements EditorOptions.CodeStyleProducer {
        public CodeStyle create(Preferences preferences) {
            return new CodeStyle(preferences);
        }
    } 
}
