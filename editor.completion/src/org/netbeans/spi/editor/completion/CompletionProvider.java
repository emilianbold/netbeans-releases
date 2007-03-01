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

package org.netbeans.spi.editor.completion;

import javax.swing.text.JTextComponent;

/**
 * The basic interface of the code completion querying SPI. Various implementations can
 * be registered per a document mime-type.
 *
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.01
 */

public interface CompletionProvider {

    /**
     * The <code>int</code> value representing the query for a code completion.
     */
    public static final int COMPLETION_QUERY_TYPE = 1;

    /**
     * The <code>int</code> value representing the query for a documentation.
     */    
    public static final int DOCUMENTATION_QUERY_TYPE = 2;
    
    /**
     * The <code>int</code> value representing the query for a tooltip hint.
     */    
    public static final int TOOLTIP_QUERY_TYPE = 4;

    /**
     * The <code>int</code> value representing the query for an all code completion.
     */
    public static final int COMPLETION_ALL_QUERY_TYPE = 9;

    /**
     * Creates a task that performs a query of the given type on the given component.
     * <br>
     * This method is invoked in AWT thread only and the returned task
     * may either be synchronous (if it's not complex)
     * or it may be asynchonous
     * (see {@link org.netbeans.spi.editor.completion.support.AsyncCompletionTask}).
     * <br>
     * The task usually inspects the component's document, the
     * text up to the caret position and returns the appropriate result.
     * 
     * @param queryType a type ot the query. It can be one of the {@link #COMPLETION_QUERY_TYPE},
     *  {@link #COMPLETION_ALL_QUERY_TYPE}, {@link #DOCUMENTATION_QUERY_TYPE},
     *  or {@link #TOOLTIP_QUERY_TYPE} (but not their combination).          
     * @param component a component on which the query is performed
     *
     * @return a task performing the query.
     */
    public CompletionTask createTask(int queryType, JTextComponent component);

    /**
     * Called by the code completion infrastructure to check whether a text just typed
     * into a text component triggers an automatic query invocation.
     * <br>
     * If the particular query type is returned the infrastructure
     * will then call {@link #createTask(int, JTextComponent)}.
     *
     * @param component a component in which typing appeared
     * @param typedText a typed text 
     *
     * @return a combination of the {@link #COMPLETION_QUERY_TYPE}, {@link #COMPLETION_ALL_QUERY_TYPE},
     *         {@link #DOCUMENTATION_QUERY_TYPE}, and {@link #TOOLTIP_QUERY_TYPE}
     *         values, or zero if no query should be automatically invoked.
     */
    public int getAutoQueryTypes(JTextComponent component, String typedText);

}
