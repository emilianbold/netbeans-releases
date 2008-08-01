/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
     * or it may be asynchronous
     * (see {@link org.netbeans.spi.editor.completion.support.AsyncCompletionTask}).
     * <br>
     * The task usually inspects the component's document, the
     * text up to the caret position and returns the appropriate result.
     * 
     * @param queryType a type of the query. It can be one of the {@link #COMPLETION_QUERY_TYPE},
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
