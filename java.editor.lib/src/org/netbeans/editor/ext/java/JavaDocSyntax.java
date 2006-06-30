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

package org.netbeans.editor.ext.java;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.ext.MultiSyntax;

/**
* Extended java lexical anlyzer that combines JavaSyntax with the HTMLSyntax
* to form java with the javadoc tokens recognition.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaDocSyntax extends MultiSyntax {

    // Internal states
    private static final int HTML_ON = 0;
    private static final int ISI_ERROR = 1; // after carriage return

    public JavaDocSyntax() {
    }

    protected TokenID parseToken() {
        char actChar;

        while(offset < stopOffset) {
            actChar = buffer[offset];
            switch (state) {


            } // end of switch(state)

        } // end of while(offset...)

        /** At this stage there's no more text in the scanned buffer.
        * Scanner first checks whether this is completely the last
        * available buffer.
        */

        if (lastBuffer) {
            switch(state) {

            }
        }

        return null;

    }


}
