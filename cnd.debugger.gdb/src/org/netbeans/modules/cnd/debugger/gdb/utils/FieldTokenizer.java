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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.gdb.utils;

import java.util.NoSuchElementException;

/**
 * Read the field information from a struct/union/class returned by gdb. This class is
 * slightly patterned after StringTokenizer but understands C and C++ syntax (up to a point).
 *
 * Format and assumptions:
 *      Each field is terminated by a ';'
 *      Each field has a type and variable name - the rest can be ignored
 *      We don't care about C vs C++ because we assume gdb provides valid field information
 *      We don't care about method declarations, only data fields
 *
 * @author gordonp
 */
public class FieldTokenizer {
    
    private String[] retval = new String[2];
    private MyStringTokenizer tok;
    private int anon_count;
    
    /** Creates a new instance of FieldTokenizer */
    public FieldTokenizer(String fields) {
        if (fields == null) {
            throw new NullPointerException();
        }
        tok = new MyStringTokenizer(fields);
        anon_count = 0;
	retval[0] = null;
    }
    
    private void findNextField() {
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken().trim();
            if (!token.endsWith(")") && !token.endsWith(") const")) { // NOI18N
                parseNextField(token);
                return;
            }
        }
	if (anon_count > 0) {
	    retval[0] = Integer.toString(anon_count);
	    retval[1] = "<anon-count>"; // NOI18N
	    anon_count = 0;
	} else {
	    retval[0] = null;
	}
    }
    
    private void parseNextField(String field) {
        boolean modified = true;
        field = field.replace("\\n", "").trim(); // NOI18N
        
        while (modified) {
            modified = false;
            if (field.startsWith("static ")) { // NOI18N
                field = field.substring(7).trim();
                modified = true;
            } else if (field.startsWith("public: ")) { // NOI18N
                field = field.substring(8).trim();
                modified = true;
            } else if (field.startsWith("private: ")) { // NOI18N
                field = field.substring(9).trim();
                modified = true;
            } else if (field.startsWith("protected: ")) { // NOI18N
                field = field.substring(11).trim();
                modified = true;
            } else if (field.startsWith("virtual ")) { // NOI18N
                field = field.substring(8).trim();
                modified = true;
            } else if (field.startsWith("const ")) { // NOI18N
                field = field.substring(6).trim();
                modified = true;
            } else if (field.endsWith(" const")) { // NOI18N
                field = field.substring(0, field.length() - 6).trim();
                modified = true;
            } else if (field.startsWith("\\n ")) { // NOI18N
                field = field.substring(2).trim();
                modified = true;
            }
        }
        
        if (field.endsWith("}")) { // NOI18N
            // This happens for anonymous structs/unions/classes...
            retval[0] = field;
            retval[1] = "<anonymous" + ++anon_count + ">"; // NOI18N
        } else {
            int pos = field.lastIndexOf(' ');
            if (pos != -1) {
                int pos2 = field.indexOf('*', pos);
                if (pos2 != -1) {
                    while (field.charAt(pos2 + 1) == '*') {
                        pos2++;
                    }
                    retval[0] = field.substring(0, pos2 + 1).trim();
                    retval[1] = field.substring(pos2 + 1).trim();
                } else {
                    retval[0] = field.substring(0, pos).trim();
                    retval[1] = field.substring(pos).trim();
                }
            } else {
                retval[0] = "int"; // NOI18N - missing type (GNU's basic_string does this)
                retval[1] = field;
            }
        }
    }
    
    public boolean hasMoreFields() {
	findNextField();
        return retval[0] != null || anon_count != 0;
    }
    
    public String[] nextField() {
	if (retval[0] == null) {
	    throw new NoSuchElementException();
	}
        return retval;
    }
    
    /**
     * This private string tokenizer differs from StringTokenizer by ignoring the delimeter
     * if its in quotes or betwwn curly braces. StringTokenizer fails in cases similar to
     * the following:
     *
     *      class A { class B { int x; } i; };
     *
     * See IZ 112664 for a full test file.
     */
    private static final class MyStringTokenizer {
        
        private String info;
        private int start, end;
        
        public MyStringTokenizer(String info) {
            this.info = info;
            start = 0;
        }
        
        public boolean hasMoreTokens() {
            return start < info.length();
        }
        
        public String nextToken() {
            if (start >= info.length()) {
                throw new NoSuchElementException();
            }
            String t;
            end = GdbUtils.findNextSemi(info, start);
            if (end == -1) {
                t = info.substring(start);
                start = info.length(); // this will make the next hasMoreTokens() call return false
            } else {
                t = info.substring(start, end);
                start = end + 1;
            }
            return t;
        }
    }
}
