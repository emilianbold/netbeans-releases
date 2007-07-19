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
 * Read the token information from a struct/union/class returned by gdb. This class is
 * slightly patterned after StringTokenizer but understands C and C++ syntax (up to a point).
 *
 * @author gordonp
 */
public class ValueTokenizer {
    
    private String[] retval = new String[2];
    private String buf;
    
    /** Creates a new instance of ValueTokenizer */
    public ValueTokenizer(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (value.startsWith("{") && value.endsWith("}")) { // NOI18N 
            buf = value.substring(1, value.length() - 1);
        } else {
            buf = value;
        }
	retval[0] = null;
    }
    
    private void findNextToken() {
        int pos1 = buf.indexOf('=');
        int pos2 = findTokenEnd(pos1);
        
        if (pos1 != -1 && pos2 != -1) {
            retval[0] = buf.substring(0, pos1 - 1).trim();
            retval[1] = buf.substring(pos1 + 1, pos2);
            buf = buf.substring(pos2 < buf.length() ? pos2 + 1 : pos2).trim();
        } else if (buf.equals("<No data fields>")) { // NOI18N - gdb message for empty classes
            retval[0] = buf;
            retval[1] = "";
            buf = "";
        } else {
            retval[0] = null;
            retval[1] = null;
        }
    }
    
    private int findTokenEnd(int idx) {
        while (idx != -1 && idx < buf.length()) {
            char c = buf.charAt(idx);
            switch (c) {
                case '<':
                    idx = GdbUtils.findMatchingLtGt(buf, idx);
                    break;
                case '{':
                    idx = GdbUtils.findMatchingCurly(buf, idx);
                    break;
                case ',':
                    return idx;
                default:
                    idx++;
                    break;
            }
        }
        return idx;
    }
    
    public boolean hasMoreTokens() {
	findNextToken();
        return retval[0] != null;
    }
    
    public String[] nextToken() {
	if (retval[0] == null) {
	    throw new NoSuchElementException();
	}
        return retval;
    }
}
