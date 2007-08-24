/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
	    retval[1] = "<anon-count>";
	    anon_count = 0;
	} else {
	    retval[0] = null;
	}
    }
    
    private void parseNextField(String field) {
        boolean modified = true;
        
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
    private final class MyStringTokenizer {
        
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
