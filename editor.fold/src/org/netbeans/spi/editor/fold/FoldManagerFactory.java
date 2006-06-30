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

package org.netbeans.spi.editor.fold;

/**
 * This factory interface allows to produce {@link FoldManager}
 * instance for the given fold.
 * <br>
 * It is intended for xml layer registration
 * into the following folder in the system FS:
 * <pre>
 *     Editors/&lt;mime-type&gt;/FoldManager
 * </pre>
 * For example java fold manager factories should be registered in
 * <pre>
 *     Editors/text/x-java/FoldManager
 * </pre>
 *
 * <p>
 * The factories present in the folder can be sorted by using standard
 * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">
 * Layer Ordering</a>.
 * <br>
 * The fold manager of factory A registered prior factory B produces
 * folds with higher priority than those from fold manager of factory B.
 * <br>
 * If two folds would overlap the one with higher priority
 * will be visible - see {@link FoldManager} for more details.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface FoldManagerFactory {
    
    /**
     * Create fold manager instance.
     */
    public FoldManager createFoldManager();
    
}
