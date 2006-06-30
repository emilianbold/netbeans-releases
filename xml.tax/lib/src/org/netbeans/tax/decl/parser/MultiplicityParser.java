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
package org.netbeans.tax.decl.parser;

import java.io.*;

import org.netbeans.tax.decl.*;

/** Utility parser parsing multiplisity marks. */
public class MultiplicityParser {

    /** Parse model content.
     * @param model string without starting delimiter.
     */
    public String parseMultiplicity (ParserReader model) {

        int ch = model.peek ();
        switch (ch) {
            case '?': case '+': case '*':

                try {
                    model.read (); //use peeked character
                } catch (IOException ex) {
                    ex.printStackTrace ();
                }
                
                return new String ( new char[] {(char) ch});
                
            default:
                return ""; // NOI18N
        }
    }
    
}
