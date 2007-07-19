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
import java.util.StringTokenizer;

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
    private StringTokenizer tok;
    
    /** Creates a new instance of FieldTokenizer */
    public FieldTokenizer(String fields) {
        if (fields == null) {
            throw new NullPointerException();
        }
        tok = new StringTokenizer(fields, ";"); // NOI18N
	retval[0] = null;
    }
    
    private void findNextField() {
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken().trim();
            if (!token.endsWith(")") && !token.endsWith(") const")) {
                parseNextField(token);
                return;
            }
        }
	retval[0] = null;
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
    
    public boolean hasMoreFields() {
	findNextField();
        return retval[0] != null;
    }
    
    public String[] nextField() {
	if (retval[0] == null) {
	    throw new NoSuchElementException();
	}
        return retval;
    }
}
