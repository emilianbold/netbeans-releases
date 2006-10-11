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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.api.lexer.TokenId;

/**
 * Description of a particular language embedding including
 * starting and ending skipped regions of a branch token
 * containing this embedding
 * and a definition of an embedded language hierarchy.
 *
 * <p>
 * Depending on the language the embedding may be made flyweight
 * and service more than one branch token.
 * <br/>
 * Or it may be constructed to service just a single branch token.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class LanguageEmbedding {
    
    /**
     * Get the embedded language hierarchy by providing its description.
     * <br/>
     * This method is only evaluated once the list of embedded tokens
     * is being created.
     *
     * @return non-null embedded language description.
     */
    public abstract LanguageDescription<? extends TokenId> language();

    /**
     * Get length of the initial part of the branch token that should be skipped
     * so it will be excluded from lexing and no tokens will be created for it.
     *
     * @return &gt;=0 number of characters in an initial part of the branch token
     *  (for which the language embedding is defined) that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     */
    public abstract int startSkipLength();
    
    /**
     * Get length of the ending part of the branch token that should be skipped
     * so it will be excluded from lexing and no tokens will be created for it.
     *
     * @return &gt;=0 number of characters at the end of the branch token
     *  (for which the language embedding is defined) that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     */
    public abstract int endSkipLength();
    
}
