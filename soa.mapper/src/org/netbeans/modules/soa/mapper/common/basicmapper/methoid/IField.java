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

/**
 * <p>
 *
 * Title: IField </p> <p>
 *
 * Description: Generic interface describes the basic functionality of a methoid
 * field. IField is the base interface holding meta data of methoid field, to
 * allow IFieldNode to be constructed and added to IMethoidNode. Subclass should
 * fire property change on the name, type and tooltip properties once changed.
 * </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IField {

    /**
     * Return the name of this methoid field.
     *
     * @return   the name of this methoid field.
     */
    public String getName();

    /**
     * Return the type of this methoid field.
     *
     * @return   the type of this methoid field.
     */
    public String getType();

    /**
     * Return the tooltip text of this methoid field.
     *
     * @return   the tooltip text of this methoid field.
     */
    public String getToolTipText();

    /**
     * Return the methoid field in another object repersentation.
     *
     * @return   the methoid field in another object repersentation.
     */
    public Object getData();

    /**
     * Return true if this methoid field is a input field, false otherwise.
     *
     * @return   true if this methoid field is a input field, false otherwise.
     */
    public boolean isInput();

    /**
     * Return true if this methoid field is a output field, false otherwise.
     *
     * @return   true if this methoid field is a output field, false otherwise.
     */
    public boolean isOutput();
    
    /**
     * Return any optional literal updater for the field.
     * If no literal updater exists for the field (null literal updater), then
     * the field does not support being connected-to/represented-by literals.
     * Otherwise, the field will support literals.
     * 
     * @return the literal updater, or null if none exists
     */
    public ILiteralUpdater getLiteralUpdater();
    
    /**
     * Sets the optional literal updater for the field.
     * 
     * @param literalUpdater the literal updater
     *
     */ 
    public void setLiteralUpdater(ILiteralUpdater literalUpdater);
    
    /**
     * Sets the name of this methoid field.
     *
     * @param name the field name
     */
    public void setName(String name);

    /**
     * Sets the type of this methoid field.
     *
     * @param type the field type
     */
    public void setType(String type);

    /**
     * Sets the tooltip text of this methoid field.
     *
     * @param tooltip the field tooltip
     */
    public void setToolTipText(String tooltip);

    /**
     * Sets the methoid field in another object repersentation.
     *
     * @param data field data
     */
    public void setData(Object data);

    /**
     * Sets whether this methoid field is a input field.
     *
     * @param value true if the field is an input field
     */
    public void setInput(boolean value);

    /**
     * Sets whether this methoid field is a output field.
     *
     * @param value true if the field is an output field
     */
    public void setOutput(boolean value);
}
