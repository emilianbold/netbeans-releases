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
package org.netbeans.modules.sql.framework.ui.graph;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Icon;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public interface IOperatorXmlInfo {

    /** Event descriptor indicating operator is marked for display in toolbar */
    public static final String OPERATOR_CHECKED = "operator_checked";

    /**
     * Event descriptor indicating new instance operator should be dropped onto current
     * collaboration canvas.
     */
    public static final String OPERATOR_DROPPED = "operator_dropped";

    /**
     * Gets the (non-localized) name of this operator
     * 
     * @return name
     */
    public String getName();

    /**
     * Gets the (localized) display name of this operator
     * 
     * @return display name
     */
    public String getDisplayName();

    /**
     * Gets class name of SQLBuilder object associated with this operator
     * 
     * @return object type
     */
    public String getObjectClassName();

    /**
     * Gets tool tip for the operator
     * 
     * @return tool tip
     */
    public String getToolTip();

    /**
     * Get the icon for this operator
     */
    public Icon getIcon();

    /**
     * Gets number of inputs for thie operator
     * 
     * @return number of inputs
     */
    public int getInputCount();

    /**
     * Gets number of outputs for thie operator
     * 
     * @return number of outputs
     */
    public int getOutputCount();

    /**
     * Gets the output list of IOperatorFields
     * 
     * @return list of output list
     */
    public List getOutputFields();

    /**
     * Gets the input list of IOperatorFields
     * 
     * @return list of input list
     */
    public List getInputFields();

    /**
     * Gets the value of the attribute with the given name
     * 
     * @param name name of attribute whose value is to be retrieved
     * @return value of the attribute
     */
    public Object getAttributeValue(String name);

    /**
     * Sets an attribute's value
     * 
     * @param attrName attribute name
     * @param val attribute value
     */
    public void setAttributeValue(String attrName, Object val);

    /**
     * Indicates whether to display this operator in the toolbar
     * 
     * @return true if displayable, false otherwise
     */
    public boolean isChecked();

    /**
     * Sets whether to display this operator in the toolbar
     * 
     * @param checked true if this operator should be displayed
     */
    public void setChecked(boolean checked);

    /**
     * Adds the given property change listener.
     * 
     * @param l new PropertyChangeListener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Removes the given property change listener.
     * 
     * @param l PropertyChangeListener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Gets the transferable object
     * 
     * @return transferable
     */
    public Transferable getTransferable();

    /**
     * Signals that an instance of this operator should be 'dropped' onto the current
     * collaboration canvas.
     * 
     * @param dropped true if a new instance should be dropped.
     */
    public void setDropInstance(boolean dropped);

    /**
     * check if open and close paranthesis should be used
     * 
     * @return bool
     */
    public boolean isShowParenthesis();

    public IOperatorField getInputField(String name);

    /**
     * Is this a java operator.
     * 
     * @return true if this operator is implemented and should be invoked in java.
     */
    public boolean isJavaOperator();

    /**
     * Returns Toolbar type/category for this operator Node. See IOperatorXmlInfoModel for
     * Toolbar types.
     * 
     * @return toobar type this operator node can belong.
     */
    public int getToolbarType();

}

