/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
