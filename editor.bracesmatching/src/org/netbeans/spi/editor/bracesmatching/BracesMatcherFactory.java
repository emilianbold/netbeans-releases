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
package org.netbeans.spi.editor.bracesmatching;

/**
 * The factory for creating {@link BracesMatcher}s. Instances of this class
 * are supposed to be registered in MIME lookup under the mime type of documents
 * that they wish to provide matching services for.
 * 
 * @author Vita Stejskal
 */
public interface BracesMatcherFactory {

    /**
     * Creates a matcher for searching a document for matching areas.
     * 
     * <p class="nonnormative">An example of <code>BracesMatcher</code> could be
     * a matcher that detects braces, brackets or parenthesis next to a caret
     * and finds their matching counterparts.
     * 
     * @param context The context to use for searching. It contains
     *   the position of a caret in a document and allows to report results.
     * 
     * @return A new matcher.
     */
    public BracesMatcher createMatcher(MatcherContext context);
    
}
