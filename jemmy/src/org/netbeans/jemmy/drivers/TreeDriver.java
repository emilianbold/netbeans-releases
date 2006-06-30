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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.drivers;

import javax.swing.tree.TreePath;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Defines how to work with trees.
 */
public interface TreeDriver extends MultiSelListDriver {

    /**
     * Expandes a node.
     * @param oper Tree operator.
     * @param index Node index.
     */
    public void expandItem(ComponentOperator oper, int index);

    /**
     * Collapses a node.
     * @param oper Tree operator.
     * @param index Node index.
     */
    public void collapseItem(ComponentOperator oper, int index);

    /**
     * Edits a node.
     * @param oper Tree operator.
     * @param index Node index.
     * @param newValue New node value
     * @param waitEditorTime Time to wait node editor.
     */
    public void editItem(ComponentOperator oper, int index, Object newValue, Timeout waitEditorTime);

    /**
     * Starts node editing.
     * @param oper Tree operator.
     * @param index Node index.
     * @param waitEditorTime Time to wait node editor.
     */
    public void startEditing(ComponentOperator oper, int index, Timeout waitEditorTime);
}
