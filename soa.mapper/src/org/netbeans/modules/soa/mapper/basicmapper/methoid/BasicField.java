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

package org.netbeans.modules.soa.mapper.basicmapper.methoid;

import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;

/**
 * <p>
 *
 * Title: BasicMethoidField </p> <p>
 *
 * Description: BasicMethoidField implements IMethoidField providing the
 * infomoration of a methoid field. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 26, 2002
 * @version   1.0
 */
public class BasicField
     implements IField {

    /**
     * field data object
     */
    private Object mData;

    /**
     * flag indicates input field
     */
    private boolean mIsInput;

    /**
     * flag indicates output field
     */
    private boolean mIsOutput;

    /**
     * the field name
     */
    private String mName;

    /**
     * the tool tip text
     */
    private String mToolTip;

    /**
     * the field type
     */
    private String mType;
    
    /**
     * whether the field can represent/connect to a literal
     */
    private ILiteralUpdater mLiteralUpdater;
    
    

    /**
     * Constructor a methoid field with all the specified basic field
     * information.
     *
     * @param name      the name of this field.
     * @param type      the tyoe of this field.
     * @param tooltip   the tooltip text of this field
     * @param data      the data object of this field
     * @param isInput   flag indicates if this is an input field
     * @param isOutput  flag indicates if this is an output field
     * @param literalFieldInfo optional literal info
     */
    public BasicField(String name, 
                      String type,
                      String tooltip, 
                      Object data,
                      boolean isInput, 
                      boolean isOutput, 
                      ILiteralUpdater literalUpdater) {
        mName = name;
        mType = type;
        mToolTip = tooltip;
        mData = data;
        mIsInput = isInput;
        mIsOutput = isOutput;
        mLiteralUpdater = literalUpdater;
    }

    /**
     * Return the methoid field in another object repersentation.
     *
     * @return   the methoid field in another object repersentation.
     */
    public Object getData() {
        return mData;
    }

    /**
     * Return the name of this methoid field.
     *
     * @return   the name of this methoid field.
     */
    public String getName() {
        return mName;
    }

    /**
     * Return the tooltip text of this methoid field.
     *
     * @return   the tooltip text of this methoid field.
     */
    public String getToolTipText() {
        return mToolTip;
    }

    /**
     * Return the type of this methoid field.
     *
     * @return   the type of this methoid field.
     */
    public String getType() {
        return mType;
    }

    /**
     * Return true if this methoid field is a input field, false otherwise.
     *
     * @return   true if this methoid field is a input field, false otherwise.
     */
    public boolean isInput() {
        return mIsInput;
    }

    /**
     * Return true if this methoid field is a output field, false otherwise.
     *
     * @return   true if this methoid field is a output field, false otherwise.
     */
    public boolean isOutput() {
        return mIsOutput;
    }

    /**
     * Set the name of this methoid field.
     *
     * @param name  the name of this methoid field.
     */
    public void setName(String name) {
        String oldName = mName;
        mName = name;
    }

    /**
     * Set the tooptip text of this methoid field.
     *
     * @param tooltip  the tooptip text of this methoid field.
     */
    public void setToolTipText(String tooltip) {
        String oldToolTip = mToolTip;
        mToolTip = tooltip;
    }

    /**
     * Set the type of this methoid field.
     *
     * @param type  the type of this methoid field.
     */
    public void setType(String type) {
        String oldType = mType;
        mType = type;
    }

    public void setInput(boolean value) {
        mIsInput = value;
    }

    public void setOutput(boolean value) {
        mIsOutput = value;
    }

    public void setData(Object data) {
         mData = data;
    }

    public ILiteralUpdater getLiteralUpdater() {
        return mLiteralUpdater;
    }
    
    public void setLiteralUpdater(ILiteralUpdater literalUpdater) {
        mLiteralUpdater = literalUpdater;
    }
}
