/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

