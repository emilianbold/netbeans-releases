/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.sql.framework.model.impl;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;

/**
 * 
 */
public class OperatorNodeX extends CommonNodeX implements org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo, Transferable {

    private static final String KEY_CHECKED = "Checked"; // NOI18N

    private static final String KEY_DISPLAYNAME = "DisplayName"; // NOI18N

    private static final String KEY_CLASSNAME = "className"; // NOI18N

    private static final String KEY_INPUTCOUNT = "InputNum"; // NOI18N

    private static final String KEY_OUTPUTCOUNT = "OutputNum"; // NOI18N

    private static final String KEY_TOOLTIP = "Tooltip"; // NOI18N

    private static final String KEY_TOOLBARCATEGORY = "ToolbarCategory"; // NOI18N

    private static final String KEY_SHOWPARENTHESIS = "ShowParenthesis"; // NOI18N

    private static final String KEY_JAVAOPERATOR = "JavaOperator"; // NOI18N

    private static final String KEY_OUTPUTPARAM = "OutputParam"; // NOI18N

    private static final String SUFFIX_DISPLAYNAME = KEY_DISPLAYNAME; // NOI18N

    private static final String SUFFIX_DISPLAYVALUES = "DisplayValues"; // NOI18N

    private static final String SUFFIX_CHOICES = "Choices"; // NOI18N

    private static final String SUFFIX_DEFAULT = "Default"; // NOI18N

    private static final String SUFFIX_EDITABLE = "Editable"; // NOI18N

    private static final String SUFFIX_STATIC = "Static"; // NOI18N

    private static final String KEY_OUTPUTPARAMEDITABLE = KEY_OUTPUTPARAM + SUFFIX_EDITABLE; // NOI18N

    private static final String KEY_OUTPUTTOOLTIP = "OutputTooltip"; // NOI18N

    private static final String KEY_INPUTTOOLTIP = "InputTooltip"; // NOI18N

    private static final String KEY_INPUTPARAM = "InputParam"; // NOI18N

    private static final String KEY_INPUTPARAMEDITABLE = KEY_INPUTPARAM + SUFFIX_EDITABLE; // NOI18N

    private static final String KEY_INPUTPARAMSTATIC = KEY_INPUTPARAM + SUFFIX_STATIC; // NOI18N

    private static final String KEY_INPUTPARAMSTATICCHOICES = KEY_INPUTPARAMSTATIC + SUFFIX_CHOICES; // NOI18N

    private static final String KEY_INPUTPARAMSTATICDEFAULT = KEY_INPUTPARAMSTATIC + SUFFIX_DEFAULT; // NOI18N

    private static final String KEY_LISTDELIMITER = "attrListDelimiter"; // NOI18N

    private static DataFlavor[] mDataFlavorArray = new DataFlavor[1];
    private ArrayList inputList = new ArrayList();
    private ArrayList outputList = new ArrayList();
    private List propChangeListners = new ArrayList();
    
    static {
        try {
            mDataFlavorArray[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void populateAttributes(Element opElem) {
        NodeList children = opElem.getChildNodes();
        Node node = null;
        Element element = null;
        String tName = null;
        Object value = null;
        for (int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if (node.getNodeName().equals(TAG_ATTRIBUTE)) {
                element = (Element) node;
                tName = element.getAttribute(ATTR_NAME);
                value = this.getAttributeValue(element);
                this.attributes.put(tName, value);
            }
        }
    }
 
    /**
     * Creates a new instance of OperatorNodeX using the given DataObject.
     * 
     * @param operatorObj DatabaObject used to create this operator node
     */
    public OperatorNodeX(Element opElement) throws BaseException {
        super(opElement);
        populateAttributes(opElement);
        
        String listDelimiter = (String) getAttributeValue(KEY_LISTDELIMITER);
        if (listDelimiter == null) {
            listDelimiter = ",";
        }

        // add input graph field
        for (int i = 0; i < getInputCount(); i++) {
            int cnt = i + 1;

            String fieldName = (String) getAttributeValue(KEY_INPUTPARAM + cnt);
            OperatorFieldNodeX fieldNode = new OperatorFieldNodeX(fieldName, getLocalizedAttributeValue(fieldName));

            String displayKey = (String) getAttributeValue(KEY_INPUTPARAM + cnt + "_" + SUFFIX_DISPLAYNAME);
            if (displayKey == null || displayKey.trim().length() == 0) {
                fieldNode.setDisplayName(fieldName);
            } else {
                fieldNode.setDisplayName(getLocalizedAttributeValue(displayKey));
            }

            String toolTip = (String) getAttributeValue(KEY_INPUTTOOLTIP + cnt);
            fieldNode.setToolTip(getLocalizedAttributeValue(toolTip));
            Boolean editFlag = (Boolean) getAttributeValue(KEY_INPUTPARAMEDITABLE + cnt);
            boolean editable = false;
            if (editFlag != null && editFlag.booleanValue()) {
                editable = true;
            }
            fieldNode.setEditable(editable);

            Boolean staticFlag = (Boolean) getAttributeValue(KEY_INPUTPARAMSTATIC + cnt);
            boolean isStatic = false;
            if (staticFlag != null && staticFlag.booleanValue()) {
                isStatic = true;
            }
            fieldNode.setStatic(isStatic);

            if (isStatic) {
                String acceptableValStr = (String) getAttributeValue(KEY_INPUTPARAMSTATICCHOICES + cnt);
                List acceptableValues = Collections.EMPTY_LIST;
                if (acceptableValStr != null && acceptableValStr.trim().length() != 0) {
                    acceptableValues = StringUtil.createStringListFrom(acceptableValStr, listDelimiter.charAt(0));
                    fieldNode.setAcceptableValues(acceptableValues);

                    String displayValKey = (String) getAttributeValue(KEY_INPUTPARAMSTATICCHOICES + cnt + "_" + SUFFIX_DISPLAYVALUES);
                    String displayValStr = getLocalizedAttributeValue(displayValKey);

                    if (displayValStr != null && displayValStr.trim().length() != 0) {
                        List displayValues = StringUtil.createStringListFrom(displayValStr, listDelimiter.charAt(0));
                        if (displayValues.size() == acceptableValues.size()) {
                            fieldNode.setAcceptableDisplayValues(displayValues);
                        }
                    }

                    String defaultValue = (String) getAttributeValue(KEY_INPUTPARAMSTATICDEFAULT + cnt);
                    if (StringUtil.isNullString(defaultValue)) {
                        throw new BaseException("Must have a valid default value for a static parameter");
                    }

                    fieldNode.setDefaultValue(defaultValue);
                }
            }

            inputList.add(fieldNode);
        }

        // add output graph field
        for (int i = 0; i < getOutputCount(); i++) {
            int cnt = i + 1;

            String fieldName = (String) getAttributeValue(KEY_OUTPUTPARAM + cnt);
            OperatorFieldNodeX fieldNode = new OperatorFieldNodeX(fieldName, getLocalizedAttributeValue(fieldName));

            String displayKey = (String) getAttributeValue(KEY_OUTPUTPARAM + cnt + "_" + SUFFIX_DISPLAYNAME);
            if (displayKey == null || displayKey.trim().length() == 0) {
                fieldNode.setDisplayName(fieldName);
            } else {
                fieldNode.setDisplayName(getLocalizedAttributeValue(displayKey));
            }

            String toolTip = (String) getAttributeValue(KEY_OUTPUTTOOLTIP + cnt);
            fieldNode.setToolTip(getLocalizedAttributeValue(toolTip));

            Boolean edit = (Boolean) getAttributeValue(KEY_OUTPUTPARAMEDITABLE + cnt);
            boolean editable = false;
            if (edit != null && edit.booleanValue()) {
                editable = true;
            }
            fieldNode.setEditable(editable);

            outputList.add(fieldNode);
        }
    }

    /**
     * Gets the value of the attribute with the given name
     * 
     * @param name name of attribute whose value is to be retrieved
     * @return value of the attribute
     */
    public Object getAttributeValue(String tName) {
        return this.attributes.get(tName);
    }

    private String getLocalizedAttributeValue(String attrVal) {
        return getLocalizedValue(attrVal);
    }

    /**
     * Gets number of inputs for thie operator
     * 
     * @return number of inputs
     */
    public int getInputCount() {
        Integer cnt = (Integer) this.attributes.get(KEY_INPUTCOUNT);
        return cnt.intValue();
    }

    /**
     * Gets the (non-localized) name of this operator
     * 
     * @return name
     */
    public String getName() {
        String tname = super.getName();
        
        if ( tname != null) {
            return tname;
        }
        return "op";
    }

    /**
     * Gets class name of SQLBuilder object associated with this operator
     * 
     * @return object type
     */
    public String getObjectClassName() {
        return (String) this.attributes.get(KEY_CLASSNAME);
    }

    /**
     * Gets number of outputs for this operator
     * 
     * @return number of outputs
     */
    public int getOutputCount() {
        Integer cnt = (Integer) this.attributes.get(KEY_OUTPUTCOUNT);
        if (cnt != null){
            return cnt.intValue();
        } else {
            return 0;
        }
    }

    /**
     * Gets tool tip for the operator
     * 
     * @return tool tip
     */
    public String getToolTip() {
        String toolTip = (String) this.attributes.get(KEY_TOOLTIP);
        return getLocalizedValue(toolTip);
    }

    /**
     * Indicates whether to display this operator in the toolbar
     * 
     * @return true if displayable, false otherwise
     */
    public boolean isChecked() {
        Boolean checked = (Boolean) this.attributes.get(KEY_CHECKED);
        if (checked != null) {
            return checked.booleanValue();
        }
        return false;
    }

    public int getToolbarType() {
        Integer toolbarType = (Integer) this.attributes.get(KEY_TOOLBARCATEGORY);
        int ret = org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel.CATEGORY_ALL;
        if (toolbarType != null) {
            ret = toolbarType.intValue();
        }

        return ret;
    }

    /**
     * Returns an object which represents the data to be transferred. The class of the
     * object returned is defined by the representation class of the flavor.
     * 
     * @param flavor the requested flavor for the data
     * @return data to be transferred
     * @throws IOException if the data is no longer available in the requested flavor.
     * @throws UnsupportedFlavorException if the requested data flavor is not supported.
     * @see DataFlavor#getRepresentationClass
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return (isDataFlavorSupported(flavor)) ? this : null;
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the data can be
     * provided in. The array should be ordered according to preference for providing the
     * data (from most richly descriptive to least descriptive).
     * 
     * @return an array of data flavors in which this data can be transferred
     */
    public DataFlavor[] getTransferDataFlavors() {
        return mDataFlavorArray;
    }

    /**
     * Returns whether or not the specified data flavor is supported for this object.
     * 
     * @param flavor the requested flavor for the data
     * @return boolean indicating whether or not the data flavor is supported
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (int i = 0; i < mDataFlavorArray.length; i++) {
            if (flavor.equals(mDataFlavorArray[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     * override canRename to return false
     * 
     * @return false
     */
    public boolean canRename() {
        return false;
    }

    /**
     * Indicates whether this instance can be copied.
     * 
     * @return true if copiable, false otherwise
     */
    public boolean canCopy() {
        return true;
    }

    /**
     * Called when a drag is started with this node
     * 
     * @return this
     */
    public Transferable drag() {
        return clipboardCopy();
    }

    /**
     * Gets Transferable instance of this object
     * 
     * @return Transferable reference to this
     */
    public Transferable clipboardCopy() {
        return this;
    }

    /**
     * Sets whether to display this operator in the toolbar
     * 
     * @param checked true if this operator should be displayed
     */
    public void setChecked(boolean checked) {
        Boolean oldSel = (Boolean) this.attributes.get(KEY_CHECKED);
        this.attributes.put(KEY_CHECKED, Boolean.valueOf(checked));
        firePropertyChange(OPERATOR_CHECKED, oldSel, Boolean.valueOf(checked));
    }

    /**
     * Gets the transferable object
     * 
     * @return transferable
     */
    public Transferable getTransferable() {
        return this;
    }

    /**
     * Sets an attribute's value
     * 
     * @param attrName attribute name
     * @param val attribute value
     */
    public void setAttributeValue(String attrName, Object val) {

    }

    /**
     * Gets the input list of IOperatorFields
     * 
     * @return list of input list
     */
    public List getInputFields() {
        return inputList;
    }

    /**
     * Gets the output list of IOperatorFields
     * 
     * @return list of output list
     */
    public List getOutputFields() {
        return outputList;
    }

    /**
     * Signals that an instance of this operator should be 'dropped' onto the current
     * collaboration canvas.
     * 
     * @param dropped true if a new instance should be dropped.
     */
    public void setDropInstance(boolean dropped) {
        firePropertyChange(OPERATOR_DROPPED, Boolean.FALSE, Boolean.valueOf(dropped));
    }

    /**
     * check if open and close parenthesis should be used
     * 
     * @return bool
     */
    public boolean isShowParenthesis() {
        Boolean showParenthesis = (Boolean) this.attributes.get(KEY_SHOWPARENTHESIS);
        if (showParenthesis != null) {
            return showParenthesis.booleanValue();
        }

        return false;
    }

    public org.netbeans.modules.sql.framework.ui.graph.IOperatorField getInputField(String tName) {
        Iterator it = this.inputList.iterator();
        while (it.hasNext()) {
            org.netbeans.modules.sql.framework.ui.graph.IOperatorField field = (org.netbeans.modules.sql.framework.ui.graph.IOperatorField) it.next();
            if (field.getName().equals(tName)) {
                return field;
            }
        }

        return null;
    }

    /**
     * Is this a java operator.
     * 
     * @return true if this operator is implemented and should be invoked in java.
     */
    public boolean isJavaOperator() {
        Boolean javaOperator = (Boolean) this.attributes.get(KEY_JAVAOPERATOR);
        if (javaOperator != null) {
            return javaOperator.booleanValue();
        }
        return false;
    }

    public void addPropertyChangeListener(PropertyChangeListener l){
        this.propChangeListners.add(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l){
        this.propChangeListners.remove(l);
    }
    
    private void firePropertyChange(String propName, Object oldValue, Object newValue) {
        PropertyChangeEvent pce = new PropertyChangeEvent(this, propName, oldValue, newValue);
        Iterator itr = this.propChangeListners.iterator();
        PropertyChangeListener pcl = null;
        while (itr.hasNext()){
            pcl = (PropertyChangeListener) itr.next();
            
            try {
                pcl.propertyChange(pce);
            } catch (Exception ex) {
                // Log
            }
        }        
    }
}
