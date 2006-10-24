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

package org.netbeans.modules.java.source.parsing;

import javax.swing.text.StyledDocument;

/**XXX: maybe not needed anymore
 *
 * @author Tomas Zezula
 */
public interface DocumentProvider {

    /**
     *  Returns the document which was used to get source
     *  code.
     *  @return {@link StyledDocument} or null
     */
    StyledDocument getDocument ();

    /**
     * Performs an atomic action on the document.
     * May be called only if {@link DocumentProvider#getDocument}
     * returns non null
     * @param {@link Runnable} the atomic action
     * @throws {@link IllegalStateException} is thrown if the {@link JavaFileObject} has no {@link Document}
     */
    void runAtomic (Runnable r);
}
