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

package org.netbeans.modules.soa.mapper.common.basicmapper.methoid;

import java.util.List;

import javax.swing.Icon;

import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;

/**
 * <p>
 *
 * Title: IMethoidNode </p> <p>
 *
 * Description: Generic interfaces describes the functionalities of a methoid
 * node. IMethoidNode is the base interfaces for all methoid node to be added as
 * a methoid to the mapper model. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IMethoidNode
     extends IMapperGroupNode {
    /**
     * The property name of the field node name changed.
     */
    public static final String NAME_CHANGED = "MethoidNode.Name";

    /**
     * The property name of the field node type changed.
     */
    public static final String ICON_CHANGED = "MethoidNode.Icon";

    /**
     * The property name of the field node tooltip changed.
     */
    public static final String TOOLTIP_CHANGED = "MethoidNode.ToolTip";
    
    public static final String METHOID_OBJECT_CHANGED = "MethoidNode.MethoidObject";
    /**
     * Return the icon repersents this methoid node.
     *
     * @return   the icon repersents this methoid node.
     */
    public Icon getIcon();

    /**
     * Set the icon repersents this methoid node.
     *
     * @param icon  icon
     */
    public void setIcon(Icon icon);

    /**
     * Return the name of this methoid node.
     *
     * @return   the name of this methoid node.
     */
    public String getMethoidName();

    /**
     * Set the name of this methoid node.
     *
     * @param name  methoid name
     */
    public void setMethoidName(String name);

    /**
     * Return the text of this tooltip.
     *
     * @return   the text of this tooltip.
     */
    public String getToolTipText();

    /**
     * Set the tooptip text of this methoid node.
     *
     * @param tooltip  the tooptip text of this methoid node.
     */
    public void setToolTipText(String tooltip);

    /**
     * Return the data object if this methoid node.
     *
     * @return   the data object if this methoid node.
     */
    public Object getMethoidObject();

    /**
     * Find and return the methoid field node that repersents the specified
     * field, or null if the node cannot be found.
     *
     * @param field  the specifed field to be matched.
     * @return       the methoid field node that repersents the specified field.
     */
    public IFieldNode findNode(IField field);

    /**
     * Return true if the specified field node is the name space field node of
     * this methoid, false otherwise.
     *
     * @param fieldNode  the field node to be matched.
     * @return           true if the specified field node is the name space
     *      field node of this methoid, false otherwise.
     */
    public boolean isNamespaceField(IFieldNode fieldNode);

    /**
     * Retrun the name space field node of this methoid.
     *
     * @return   the name space field node of this methoid.
     */
    public IFieldNode getNamespaceFieldNode();

    /**
     * Returns the input field nodes of this methoid node
     *
     * @return   The input field nodes of this methoid node.
     */
    public List getInputFieldNodes();

    /**
     * Returns the output field nodes of this methoid node
     *
     * @return   the output field nodes of this methoid node
     */
    public List getOutputFieldNodes();
    
    /**
     * Add and return the new input child of this methoid.
     * This only applies to methoid nodes of accumulating methoids.
     *
     * @return   the new input child of this methoid.
     */
    public IFieldNode getNextNewNode();
}
