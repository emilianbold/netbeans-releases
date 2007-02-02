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

package org.netbeans.api.lexer;

/**
 * Token hierarchy event type determines the reason
 * why token hierarchy modification described by {@link TokenHierarchyEvent}
 * happened.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public enum TokenHierarchyEventType {

    /**
     * The token change was caused by modification (insert/remove) of the characters
     * in the underlying character sequence.
     */
    MODIFICATION,

    /**
     * The token change was caused by relexing of a part of the token hierarchy
     * without any text modification.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    RELEX,

    /**
     * The token change was caused by a complete rebuild
     * of the token hierarchy.
     * <br/>
     * That may be necessary because of changes
     * in input attributes that influence the lexing.
     * <br/>
     * When the whole hierarchy is rebuilt only the removed tokens
     * will be notified. There will be no added tokens
     * because they will be created lazily when asked.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    REBUILD,

    /**
     * The token change was caused by change in activity
     * of the token hierarchy.
     * <br/>
     * The current activity state can be determined by {@link TokenHierarchy#isActive()}.
     * <br/>
     * Firing an event with this type may happen because the input source
     * (for which the token hierarchy was created) has not been used for a long time
     * and its token hierarchy is being deactivated. Or the token hierarchy is just going
     * to be activated again.
     * <br/>
     * The hierarchy will only notify the tokens being removed (for the case when
     * the hierarchy is going to be deactivated). There will be no added tokens
     * because they will be created lazily when asked.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    ACTIVITY,
        
    /**
     * Custom language embedding was created by
     * {@link TokenSequence#createEmbedding(Language,int,int)}.
     * <br/>
     * The {@link TokenHierarchyEvent#tokenChange()} contains the token
     * where the embedding was created and the embedded change
     * {@link TokenChange#embeddedChange(int)} that describes the added
     * embedded language.
     */
    EMBEDDING,
    
    /**
     * Notification that result of
     * {@link TokenHierarchy#languagePaths()} has changed.
     */
    LANGUAGE_PATHS;

}