/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.editor.completion;

/**
 *
 * @author Marek Fukala
 */
public class Utils {
    
    public static  String unquote(String quoted) {
        if (quoted == null || quoted.length() <= 0) {
            return quoted;
        }
        int start = 0;
        int end = quoted.length();
        if (quoted.charAt(0) == '\"') {
            start++;
        }
        if (quoted.charAt(end - 1) == '\"') {
            end--;
        }
        if (start < end) {
            return quoted.substring(start, end);
        } else {
            return ""; // NOI18N
        }
    }
    
}
