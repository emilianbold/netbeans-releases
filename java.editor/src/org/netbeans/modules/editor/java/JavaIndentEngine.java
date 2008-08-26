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

package org.netbeans.modules.editor.java;

import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.java.JavaFormatter;
import org.netbeans.modules.editor.FormatterIndentEngine;

/**
* Java indentation engine that delegates to java formatter
*
* @author Miloslav Metelka
*/

public class JavaIndentEngine extends FormatterIndentEngine {

//    public static final String JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP
//        = "javaFormatNewlineBeforeBrace"; // NOI18N
//
//    public static final String JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP
//        = "javaFormatSpaceBeforeParenthesis"; // NOI18N
//
//    public static final String JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP
//        = "javaFormatLeadingStarInComment"; // NOI18N
//
//    public static final String JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP
//        = "javaFormatStatementContinuationIndent"; // NOI18N

    static final long serialVersionUID = -7936605291288152329L;

    public JavaIndentEngine() {
        setAcceptedMimeTypes(new String[] { JavaKit.JAVA_MIME_TYPE });
    }

    protected ExtFormatter createFormatter() {
        return new JavaFormatter(JavaKit.class);
    }


//    public boolean getJavaFormatSpaceBeforeParenthesis() {
//        return CodeStyle.getDefault(null).spaceBeforeMethodDeclParen();
//    }
//    public void setJavaFormatSpaceBeforeParenthesis(boolean b) {
//        assert false : "Java formatting options can only be customized from Tools-Options -> Java Code"; //NOI18N
//    }
//
//    public boolean getJavaFormatNewlineBeforeBrace() {
//        return CodeStyle.getDefault(null).getMethodDeclBracePlacement() == CodeStyle.BracePlacement.NEW_LINE;
//    }
//    public void setJavaFormatNewlineBeforeBrace(boolean b) {
//        assert false : "Java formatting options can only be customized from Tools-Options -> Java Code"; //NOI18N
//    }
//
//    public boolean getJavaFormatLeadingStarInComment() {
//        // XXX: add support for this to FmtOptions
//        return true;
//    }
//    public void setJavaFormatLeadingStarInComment(boolean b) {
//        assert false : "Java formatting options can only be customized from Tools-Options -> Java Code"; //NOI18N
//    }
//
//    public int getJavaFormatStatementContinuationIndent() {
//        return CodeStyle.getDefault(null).getContinuationIndentSize();
//    }
//
//    public void setJavaFormatStatementContinuationIndent(int javaFormatStatementContinuationIndent) {
//        assert false : "Java formatting options can only be customized from Tools-Options -> Java Code"; //NOI18N
//    }

// XXX: remove
//    // Serialization ------------------------------------------------------------
//
//    private static final ObjectStreamField[] serialPersistentFields = {
//        new ObjectStreamField(JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP, Boolean.TYPE),
//        new ObjectStreamField(JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP, Boolean.TYPE),
//        new ObjectStreamField(JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP, Boolean.TYPE),
//        new ObjectStreamField(JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP, Integer.TYPE)
//    };
//    
//    private void readObject(java.io.ObjectInputStream ois)
//    throws IOException, ClassNotFoundException {
//        ObjectInputStream.GetField fields = ois.readFields();
//        setJavaFormatNewlineBeforeBrace(fields.get(JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP,
//            getJavaFormatNewlineBeforeBrace()));
//        setJavaFormatSpaceBeforeParenthesis(fields.get(JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
//            getJavaFormatSpaceBeforeParenthesis()));
//        setJavaFormatLeadingStarInComment(fields.get(JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP,
//            getJavaFormatLeadingStarInComment()));
//        setJavaFormatStatementContinuationIndent(fields.get(JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP,
//            getJavaFormatStatementContinuationIndent()));
//    }
//
//    private void writeObject(java.io.ObjectOutputStream oos)
//    throws IOException, ClassNotFoundException {
//        ObjectOutputStream.PutField fields = oos.putFields();
//        fields.put(JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP, getJavaFormatNewlineBeforeBrace());
//        fields.put(JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP, getJavaFormatSpaceBeforeParenthesis());
//        fields.put(JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP, getJavaFormatLeadingStarInComment());
//        fields.put(JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP, getJavaFormatStatementContinuationIndent());
//        oos.writeFields();
//    }

}

