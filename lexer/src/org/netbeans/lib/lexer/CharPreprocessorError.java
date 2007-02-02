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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer;

import org.netbeans.lib.lexer.CharPreprocessorOperation;
import org.netbeans.lib.lexer.UnicodeEscapesPreprocessor;


/**
 * Error that occurred during character preprocessing.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CharPreprocessorError {

        private final String message;

        private int index;

        public CharPreprocessorError(String message, int index) {
            if (message == null) {
                throw new IllegalArgumentException("message cannot be null"); // NOI18N
            }
            this.message = message;
            this.index = index;
        }
        
        /**
         * Get a message of what the error is.
         */
        public String message() {
            return message;
        }
        
        /**
         * Get index relative to token's begining where the error has occurred.
         */
        public int index() {
            return index;
        }
        
        public void updateIndex(int diff) {
            this.index += diff;
        }
        
        public String description() {
            return message + " at index=" + index;
        }
        
}
