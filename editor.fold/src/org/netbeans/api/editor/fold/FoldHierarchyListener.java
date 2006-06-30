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

package org.netbeans.api.editor.fold;

/**
 * Listener for changes in the fold hierarchy.
 * <br>
 * It can be attached to fold hierarhcy
 * by {@link FoldHierarchy#addFoldHierarchyListener(FoldHierarchyListener)}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface FoldHierarchyListener extends java.util.EventListener {

    /**
     * Notify that the code hierarchy of folds has changed.
     *
     * @param evt event describing the changes in the fold hierarchy.
     */
    void foldHierarchyChanged(FoldHierarchyEvent evt);

}
