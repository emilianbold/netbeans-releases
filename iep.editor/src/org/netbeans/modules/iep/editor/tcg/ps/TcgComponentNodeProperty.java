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


package org.netbeans.modules.iep.editor.tcg.ps;

import org.netbeans.modules.iep.editor.tcg.exception.I18nException;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import java.util.*;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;

import org.openide.nodes.Node;

import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import org.netbeans.modules.iep.editor.tcg.model.TcgPropertyType;
import org.netbeans.modules.iep.editor.tcg.model.TcgType;
import java.util.logging.Level;
import org.openide.util.NbBundle;


public class TcgComponentNodeProperty extends Node.Property {
    private static final Logger mLog = Logger.getLogger(TcgComponentNodeProperty.class.getName());

    protected TcgProperty mProperty;
    protected TcgPropertyType mPropertyType;
    protected TcgComponentNode mNode;
    
    public static TcgComponentNodeProperty newInstance(String propName, TcgComponentNode node) throws I18nException {
        TcgProperty property = node.getComponent().getProperty(propName);
        return newInstance(property, node);
    }

    public static TcgComponentNodeProperty newInstance(TcgProperty property, TcgComponentNode node) {
        TcgPropertyType pt = property.getType();
        TcgType type = pt.getType();
        if (type == TcgType.BOOLEAN) {
            return new TcgComponentNodeProperty(property, Boolean.class, node);
        }
        if (type == TcgType.INTEGER) {
            return new TcgComponentNodeProperty(property, Integer.class, node);
        }
        if (type == TcgType.LONG) {
            return new TcgComponentNodeProperty(property, Long.class, node);
        }
        if (type == TcgType.DOUBLE) {
            return new TcgComponentNodeProperty(property, Double.class, node);
        }
        if (type == TcgType.STRING) {
            return new TcgComponentNodeProperty(property, String.class, node);
        }
        if (type == TcgType.DATE) {
            return new TcgComponentNodeProperty(property, Date.class, node);
        }
        if (type == TcgType.OBJECT && !pt.isWritable()) {
            return new TcgComponentNodeProperty(property, String.class, node);
        }
        if (type == TcgType.BOOLEAN_LIST ||
            type == TcgType.INTEGER_LIST ||
            type == TcgType.LONG_LIST ||
            type == TcgType.DOUBLE_LIST ||
            type == TcgType.STRING_LIST) 
        {
            TcgComponentNodeProperty p = new TcgComponentNodeProperty(property, ListModel.class, node);
            // Disable inplace editing.
            p.setValue("canEditAsText", Boolean.FALSE);
            return p;
        }
        return new TcgComponentNodeProperty(property, Object.class, node);
    }

    private TcgComponentNodeProperty(TcgProperty property, Class valueType, TcgComponentNode node) {
        super (valueType);
        mProperty = property;
        mPropertyType = mProperty.getType();
        mNode = node;
        setName(mProperty.getName());
        setDisplayName(TcgPsI18n.getDisplayName(mPropertyType));
        setShortDescription(TcgPsI18n.getToolTip(mPropertyType));
    }


    /* Can read the value of the property.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canRead () {
        return mPropertyType.isReadable();
    }

    /* Getter for the value.
    * @return the value of the property
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public Object getValue () 
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        // Note that the return of this method is not used by 
        // TcgComponentNodePropertyEditor and its children at all.
        // It is used by Netbean's default property editors
        TcgType type = mPropertyType.getType();
        if (type == TcgType.OBJECT && !mPropertyType.isWritable()) {
            return mProperty.getValue().toString();
        }
        if (type == TcgType.BOOLEAN_LIST ||
            type == TcgType.INTEGER_LIST ||
            type == TcgType.LONG_LIST ||
            type == TcgType.DOUBLE_LIST ||
            type == TcgType.STRING_LIST) 
        {
            DefaultListModel listModel = new DefaultListModel();
            List list = mProperty.getListValue();
            for (int i = 0, I = list.size(); i < I; i++) {
                listModel.addElement(list.get(i));
            }
            return listModel;
        }
        return mProperty.getValue();
    }

    /* Can write the value of the property.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canWrite () {
        return mPropertyType.isWritable();
    }

    /** 
     * Setter for the value. Called when PropertyEditor updates this property's value
     * @param val the value of the property
     * @exception IllegalAccessException cannot access the called method
     * @exception IllegalArgumentException wrong argument
     * @exception InvocationTargetException an exception during invocation
     */
    public void setValue (Object val) 
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        if (!mPropertyType.isWritable()) {
            throw new IllegalAccessException ();
        }
        // Note that TcgComponentNodePropertyEditor and its children will only
        // pass back following types of value:
        // Boolean, Integer, Double, String, and 
        // Lists whose elements are of above type.
        if (val instanceof ListModel) { 
            ArrayList list = new ArrayList();
            ListModel listModel = (ListModel)val;
            for (int i = 0, I = listModel.getSize(); i < I; i++) {
                list.add(listModel.getElementAt(i));
            }
            mProperty.setValue(list);
            return;
        }
        if (val instanceof String) {
            mProperty.setStringValue((String)val);
            return;
        }
        mProperty.setValue(val);
    }

    /**
     * Returns property editor for this property.
     * @return the property editor or <CODE>null</CODE> if there should not be any editor.
     */
    public PropertyEditor getPropertyEditor () {
        String editorName = mPropertyType.getEditorName();
        if (editorName.equals("default")) {
            // Use Netbeans' default property editors.
            return super.getPropertyEditor();
        }
        try {
            TcgComponentNodePropertyEditor editor = (TcgComponentNodePropertyEditor)Class.forName(editorName).newInstance();
            editor.setProperty(this);
            return editor;
        } catch (Exception e) {
            mLog.log(Level.SEVERE, 
                     NbBundle.getMessage(TcgComponentNodeProperty.class, "TcgComponentNodeProperty.Editor_not_found", editorName),
                     e);
        }
        return null;
    }

    public TcgComponentNode getNode() {
        return mNode;
    }
    
    public TcgProperty getProperty() {
        return mProperty;
    }
    
    public TcgPropertyType getPropertyType() {
        return mPropertyType;
    }
}

