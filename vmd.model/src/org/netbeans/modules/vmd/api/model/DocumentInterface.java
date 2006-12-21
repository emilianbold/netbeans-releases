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
package org.netbeans.modules.vmd.api.model;

import javax.swing.undo.UndoableEdit;

/**
 * This interface defines the project and allows the model API to be independant on any project/loaders infrastructure.
 *
 * @author David Kaspar
 */
public interface DocumentInterface {

    /**
     * Returns unique id of the project.
     * @return the unique project id
     */
    public String getProjectID ();

    /**
     * Returns a project type.
     * @return the project type
     */
    public String getProjectType ();

    /**
     * Called by the model when a document structure is modified.
     */
    public void notifyModified ();

    /**
     * Called by the model when an undoable edit happened.
     * @param edit the edit
     */
    public void undoableEditHappened (UndoableEdit edit);

    /**
     * Called by the model when a model is changed the way that cannot be undone.
     */
    public void discardAllEdits ();

}
