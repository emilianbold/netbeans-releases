/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
