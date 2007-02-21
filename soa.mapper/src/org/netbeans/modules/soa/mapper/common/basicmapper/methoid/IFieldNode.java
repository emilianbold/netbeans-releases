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

import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.IMapperNode;

/**
 * <p>
 *
 * Title: A Methoid Field Node </p> <p>
 *
 * Description: Generic interface describe a methoid field node.
 * FuncotidFieldNode is the base interface for all methoid field node to be
 * added to the IMethoidNode. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IFieldNode
     extends IMapperNode {
    /**
     * The property name of the field node name changed.
     */
    public static final String NAME_CHANGED = "FieldNode.Name";

    /**
     * The property name of the field node type changed.
     */
    public static final String TYPE_CHANGED = "FieldNode.Type";

    /**
     * The property name of the field node tooltip changed.
     */
    public static final String TOOLTIP_CHANGED = "FieldNode.ToolTip";

    public static final String STYLE_CHANGED = "FieldNode.Style";
    
    public static final String FIELD_OBJECT_CHANGED = "FieldNode.ObjectChanged";

    public static final String STYLE_TYPE_NORMAL  = "normal";
    public static final String STYLE_TYPE_LITERAL = "literal";
    
    
    /**
     * Return the name of this field.
     *
     * @return   the name of this field.
     */
    public String getName();

    /**
     * Set the name of this methoid field node.
     *
     * @param name  the name of this methoid field node.
     */
    public void setName(String name);
    
    /**
     * Return the type name of this field.
     *
     * @return   the type name of this field.
     */
    public String getTypeName();

    /**
     * Set the type of this methoid field node.
     *
     * @param type  the type of this methoid field node.
     */
    public void setTypeName(String type);

    /**
     * Return the popup tooltip text of this methoid field node.
     *
     * @return   the popup tooltip text of this methoid field node.
     */
    public String getToolTipText();

    /**
     * Set the tooptip text of this methoid field node.
     *
     * @param tooltip  the tooptip text of this methoid field node.
     */
    public void setToolTipText(String tooltip);

    /**
     * Return the field data in another object repersentation.
     *
     * @return   the field data in another object repersentation.
     */
    public Object getFieldObject();
    
    public void setFieldObject(Object fieldObj);

    /**
     * Return true if this field is an input field, false otherwise.
     *
     * @return   true if this field is an input field, false otherwise.
     */
    public boolean isInput();

    /**
     * Return true if this field is an output field, false otherwise.
     *
     * @return   true if this field is an output field, false otherwise.
     */
    public boolean isOutput();

    /**
     * Return the literal name of this methoid field node.
     * The literal name is the display name of the methoid
     * field node while the field node has an in-place literal.
     */
    public String getLiteralName();
    
    /**
     * Returns whether this field node has an in-place literal.
     * This represents an unlinked field node that is 
     * tied to literal expression. 
     */
    public boolean hasInPlaceLiteral();
    
    /**
     * Set the literal name of this methoid field node.
     */
    public void setLiteralName(String name);
        
    /**
     * Set the literal name of this methoid field node.
     * Override the innate literal updater with the specified one.
     */
    public void setLiteralName(String name, ILiteralUpdater literalUpdater);
}
