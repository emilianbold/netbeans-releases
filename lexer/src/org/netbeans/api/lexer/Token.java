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

package org.netbeans.api.lexer;

/**
 * Token interface encapsulates identification
 * of the token and the text of the token.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface Token {
    
    /** @return non-null identification of this token.
     */
    public TokenId getId();

    /** Get the text of the token (also called image)
     * as sequence of characters.
     */
    public CharSequence getText();

}

