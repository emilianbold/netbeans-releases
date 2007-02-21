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

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;

import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;

/**
 * <p>
 *
 * Title:BasicMethoid </p> <p>
 *
 * Description: BasicMethoid implements IMethoid to provide basic
 * functionalities as a methoid. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 26, 2002
 * @version   1.0
 */
public class BasicMethoid
     implements IMethoid {

    /**
     * the data object of this methoid.
     */
    private Object mData;

    /**
     * the icon of this methoid
     */
    private Icon mIcon;

    /**
     * storage of the input fields.
     */
    private List mInput;

    /**
     * the name of this methoid
     */
    private String mName;

    /**
     * the namespace field of this methoid.
     */
    private IField mNamespace;

    /**
     * storage of the output fields.
     */
    private List mOutput;

    /**
     * storge of PropertyChangeListener of this field
     */
    private List mPropertyListeners;

    /**
     * the tooltip of this methoid
     */
    private String mTooltip;
    
    /**
     * if the methoid input can grow
     */
    private boolean mIsAccumulative;
    
    /**
     * if the methoid is a literal
     */
    private boolean mIsLiteral;
    

    /**
     * Construct a methoid object with specified all properties.
     *
     * @param icon            the icon of this methoid.
     * @param name            the name of this funcotid.
     * @param tooltip         the tooltip of this methoid.
     * @param data            the data of this methoid.
     * @param namespace       the namespace of this methoid.
     * @param inputFields     the input fields of this methoid.
     * @param outputFields    the output fields of this methoid.
     * @param isAccumulative  whether the input fields can grow dynamically
     */
    public BasicMethoid(Icon icon, String name, String tooltip,
        Object data, IField namespace, List inputFields, List outputFields,
        boolean isAccumulative, boolean isLiteral) {
        this.mIcon = icon;
        this.mName = name;
        this.mTooltip = tooltip;
        this.mData = data;
        this.mNamespace = namespace;
        this.mIsAccumulative = isAccumulative;
        this.mIsLiteral = isLiteral;

        if (inputFields == null) {
            this.mInput = new Vector();
        } else {
            this.mInput = new Vector(inputFields);
        }

        if (outputFields == null) {
            this.mOutput = new Vector();
        } else {
            this.mOutput = new Vector(outputFields);
        }
    }

    /**
     * Return the data object of this methoid.
     *
     * @return   the data object of this methoid.
     */
    public Object getData() {
        return mData;
    }

    public void setData(Object data) {
        mData = data;
    }

    /**
     * Return the icon of this methoid.
     *
     * @return   the icon of this methoid
     */
    public Icon getIcon() {
        return mIcon;
    }
    
    public void setIcon(Icon icon) {
        mIcon = icon;
    }

    /**
     * Retrun the index of the specified field in this funcotid input fields.
     *
     * @param field  the field to be matched.
     * @return       the index of the specified field.
     */
    public int getIndexOfInputField(IField field) {
        return mInput.indexOf(field);
    }

    /**
     * Retrun the index of the specified field in this funcotid output fields.
     *
     * @param field  the field to be matched.
     * @return       the index of the specified field.
     */
    public int getIndexOfOutputField(IField field) {
        return mInput.indexOf(mOutput);
    }

    /**
     * Return the input fields of this methoid.
     *
     * @return   the input fields of this methoid.
     */
    public List getInput() {
        return Collections.unmodifiableList(mInput);
    }

    /**
     * Return the number of input fields in this methoid.
     *
     * @return   the number of input fields in this methoid.
     */
    public int getInputCount() {
        return mInput.size();
    }

    /**
     * Return the name of this methoid.
     *
     * @return   the name of this methoid.
     */
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    /**
     * Return the namespace field of this methoid.
     *
     * @return   the namespace field of this methoid.
     */
    public IField getNamespace() {
        return mNamespace;
    }

    /**
     * Return the output fields of this methoid.
     *
     * @return   the output fields of this methoid.
     */
    public List getOutput() {
        return Collections.unmodifiableList(mOutput);
    }

    /**
     * Return the number of output fields in this methoid.
     *
     * @return   the number of output fields in this methoid.
     */
    public int getOutputCount() {
        return mOutput.size();
    }

    /**
     * Return the tooltip text of this methoid.
     *
     * @return   the tooltip text of this methoid.
     */
    public String getToolTipText() {
        return mTooltip;
    }

    public void setToolTipText(String text) {
        mTooltip = text;
    }
    
    public boolean isAccumulative() {
        return mIsAccumulative;
    }
    
    public boolean isLiteral() {
        return mIsLiteral;
    }
}
