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
