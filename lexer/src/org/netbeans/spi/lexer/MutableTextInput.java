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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 * Mutable attributed character sequence allowing to listen for changes in its text.
 *
 * <p>
 * The input can temporarily be made inactive which leads to dropping
 * of all the present tokens for the input until the input becomes
 * active again.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class MutableTextInput<I> {

    private TokenHierarchyControl<I> thc;

    /**
     * Get the language suitable for lexing of this input.
     *
     * @return language language by which the text of this input should be lexed.
     *  <br/>
     *  This method is only checked upon creation of token hierarchy.
     *  <br/>
     *  If this method returns null the token hierarchy cannot be created
     *  and will be returned as null upon asking until this method will return
     *  non-null value.
     */
    protected abstract Language<? extends TokenId> language();

    /**
     * Get the character sequence provided and maintained by this input.
     */
    protected abstract CharSequence text();
    
    /**
     * Get lexer-specific information about this input
     * or null if there is no specific information.
     * <br>
     * The attributes are typically retrieved by the lexer.
     */
    protected abstract InputAttributes inputAttributes();
    
    /**
     * Get object that logically provides the text
     * for this mutable text input.
     * <br/>
     * For example it may be a swing text document instance
     * {@link javax.swing.text.Document} in case the result of {@link #text()}
     * is the content of the document.
     *
     * @return non-null mutable input source.
     */
    protected abstract I inputSource();
    
    /**
     * Get token hierarchy control for this mutable text input.
     * <br>
     * Each mutable text input can hold it in a specific way
     * e.g. swing document can use
     * <code>getProperty(TokenHierarchyControl.class)</code>.
     */
    public final TokenHierarchyControl<I> tokenHierarchyControl() {
        synchronized (this) {
            if (thc == null) {
                thc = new TokenHierarchyControl<I>(this);
            }
            return thc;
        }
    }

}
