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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import org.netbeans.modules.sql.framework.ui.graph.IGraphFieldNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;

import com.nwoods.jgo.JGoText;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OperatorGraphFieldNode extends BasicCellArea.Highlightable implements IGraphFieldNode {

    /* RCS id */
    static final String RCS_ID = "$Id$";

    private String name;

    /**
     * Creates a new instance of OperatorGraphFieldNode with the given type and operator
     * field information, using default text alignment.
     * 
     * @param type field type
     * @param field contains field information
     */
    public OperatorGraphFieldNode(int type, IOperatorField field) {
        this(type, field, JGoText.ALIGN_LEFT);
    }

    public OperatorGraphFieldNode(int type, String text) {
        super(type, text);
        
        this.drawBoundingRect(true);
    }

    /**
     * Creates a new instance of OperatorGraphFieldNode with the given type and operator
     * field information, using the given text alignment.
     * 
     * @param type field type
     * @param field contains field information
     * @param textAlignment desired text alignment, one of JGoText.ALIGN_LEFT,
     *        JGoText.ALIGN_RIGHT, or JGoText.ALIGN_CENTER
     */
    public OperatorGraphFieldNode(int type, IOperatorField field, int textAlignment) {
        this(type, field.getDisplayName());

        this.name = field.getName();
        this.setToolTipText(field.getToolTip());

        this.setTextEditable(field.isEditable());
        this.setTextAlignment(textAlignment);
    }

    /**
     * Gets the data object stored in this field node
     * 
     * @return data object stored in the field node
     */
    public Object getDataObject() {
        return null;
    }

    /**
     * Sets the data object in this field node
     * 
     * @param obj data object
     */
    public void setObject(Object obj) {

    }

    /**
     * Gets the graph node. Normally this is parent which contain this field node.
     * 
     * @return graph node
     */
    public IGraphNode getGraphNode() {
        return null;
    }

    /**
     * Gets name of this field
     * 
     * @return field name
     */
    public String getName() {
        return this.name;
    }

    /**
     * sets name of this field
     * 
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }
}

