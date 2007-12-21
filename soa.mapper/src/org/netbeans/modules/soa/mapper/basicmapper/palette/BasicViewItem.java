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

package org.netbeans.modules.soa.mapper.basicmapper.palette;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteItem;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicField;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicMethoid;
import org.netbeans.modules.soa.mapper.basicmapper.util.DragableLabel;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdaterFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteViewItem;

/**
 * <p>
 *
 * Title: </p> BasicViewItem <p>
 *
 * Description: </p> BasicViewItem provide a dragable JLabel for the mapper
 * canvas to be able to receive a drag operation of a IMethoid. <p>
 *
 * @author    Un Seng Leong
 * @created   January 6, 2003
 */

public class BasicViewItem
     extends DragableLabel
     implements IPaletteViewItem {

    /**
     * The methoid of this palette item
     */
    private IMethoid mMethoid;

    /**
     * the palette item from PaletteManager
     */
    private IPaletteItem mItem;

    /**
     * Description of the Field
     */
    private Object mTransferable;

    /**
     * the log instance
     */
    private static final Logger LOGGER = Logger.getLogger(BasicMapperPalette.class.getName());

    
    /**
     * Initialize a palette label with the specified palette item data model.
     * All the properties, such as name, tooltip, icon, as well as the methoid
     * data are reterived from the model.
     *
     * @param item    the palette item model of this palette.
     * @param bundle  DOCUMENT ME!
     */
    public BasicViewItem(IPaletteItem item, 
                         ResourceBundle bundle, 
                         ILiteralUpdaterFactory literalUpdaterFactory) {
        super();
        this.setIcon(new ImageIcon(item.getIcon()));
        this.setText(item.getName());
        this.setHorizontalAlignment(JButton.LEFT);
        mItem = item;
        mMethoid = generateMethoid(bundle, literalUpdaterFactory);
        setTransferableObject(mMethoid);
        
        MapperUtilities.activateInlineMnemonics((Component) this);
        String a11yName = (String) item.getItemAttribute("A11yName");   // NOI18N
        String a11yDesc = (String) item.getItemAttribute("A11yDesc");   // NOI18N
        if (!MapperUtilities.isEmpty(a11yName)) {
            a11yName = bundle.getString(a11yName);
        }
        if (MapperUtilities.isEmpty(a11yName)) {
            a11yName = this.getText();
        }
        if (!MapperUtilities.isEmpty(a11yDesc)) {
            a11yDesc = bundle.getString(a11yDesc);
        }
        if (MapperUtilities.isEmpty(a11yDesc)) {
            a11yDesc = this.getText();
        }
        this.getAccessibleContext().setAccessibleName(a11yName);
        this.getAccessibleContext().setAccessibleDescription(a11yDesc);
    }

    /**
     * Return the palette item object from palette manager.
     *
     * @return   the palette item object from palette manager.
     */
    public Object getItemObject() {
        return mItem;
    }

    /**
     * Return the transferable object for drag and drop opertaion.
     *
     * @return   the transferable object for drag and drop opertaion.
     */
    public Object getTransferableObject() {
        return mTransferable;
    }

    /**
     * Return the transferable object for drag and drop opertaion.
     *
     * @param obj  the transferable object for drag and drop opertaion.
     */
    public void setTransferableObject(Object obj) {
        mTransferable = obj;
        setDefaultTransferData(obj);
    }


    /**
     * Return the Java AWT component as the viewiable object of this palette
     * view item.
     *
     * @return   the Java AWT component as the viewiable object of this palette
     *      view item.
     */
    public Component getViewComponent() {
        return this;
    }

    /**
     * Create a field object by providing all its properties.
     *
     * @param item        the palette item
     * @param attrName    the attribute name to reterive the name of the field.
     * @param attrType    the attribute name to reterive the type of the field.
     * @param attrTooTip  the attribute name to reterive the tooltip
     * @param isInput     true if this field is input enable.
     * @param isOutput    true if this field is output enable.
     * @param bundle      the bundle to loads the acturally messages.
     * @return            the field object repersentation of specified
     *      properties.
     */
    protected IField generateField(IPaletteItem item, 
                                   String attrName,
                                   String attrType, 
                                   String attrTooTip, 
                                   boolean isInput,
                                   boolean isOutput, 
                                   ResourceBundle bundle,
                                   boolean isLiteral,
                                   ILiteralUpdaterFactory literalUpdaterFactory) {
        String fieldName = "";
        String fieldTooltip = "";
        String fieldType = "";
        String tooltipKey = "";

        if ((fieldName = (String) mItem.getItemAttribute(attrName)) != null) {
            if (
                (fieldName == null) ||
                (fieldName.length() == 0) ||
                // java expression, no "this"
                (attrName.equals("Class") && fieldName.equals("javaExp"))) {
                return null;
            }

            fieldType = (String) mItem.getItemAttribute(attrType);
            tooltipKey = (String) mItem.getItemAttribute(attrTooTip);
            
            if (tooltipKey == null) {
                LOGGER.warning("Unable to find tooltip name:[" + attrTooTip + "]");
            } else {
                try {
                    fieldTooltip = bundle.getString(tooltipKey);
                } catch (Exception e) {
                }
            }

            ILiteralUpdater literalUpdater = null;
            if (
                    (isInput || isLiteral) && 
                    literalUpdaterFactory != null) {
                // literal updater will be non-null if field type supports literals
                literalUpdater = literalUpdaterFactory.createLiteralUpdater(fieldType);
            }
            
            IField field = new BasicField(
                    fieldName, 
                    fieldType, 
                    fieldTooltip, 
                    null, 
                    isInput,
                    isOutput, 
                    literalUpdater);

            return field;
        }

        return null;
    }

    /**
     * Create and return a newly create Methoid by the specified Palette item
     * and resource bundle.
     *
     * @param item    the palette item of this methoid
     * @param bundle  the resource bundle
     * @return        a newly create Methoid by the specified Palette item and
     *      resource bundle.
     */
    protected IMethoid generateMethoid(ResourceBundle bundle, 
                                       ILiteralUpdaterFactory literalUpdaterFactory) {
        
        boolean isEditableLiteral = false;
        Object literalObj = mItem.getItemAttribute("EditableLiteral");
        if (literalObj != null && literalObj instanceof Boolean) {
            isEditableLiteral = ((Boolean) literalObj).booleanValue();
        }
        
        // creating funtoid namespace (this) field
        String fieldName = null;
        String fieldTooltip = null;
        String fieldType = null;
        IField thisField = generateField(
            mItem,
            "InputThis",
            "Class",
            "InputThisTooltip",
            true,
            false,
            bundle,
            isEditableLiteral,
            literalUpdaterFactory);

        // creating input fields
        List fieldList = new ArrayList();
        IField field = null;
        int i = 1;
        int inputNum = 0;

        try {
            inputNum =
                Integer.parseInt((String) mItem.getItemAttribute("InputNum"));
        } catch (java.lang.Throwable t) {
//            t.printStackTrace(System.err);
        }

        for (; i <= inputNum; i++) {
            field = generateField(
                mItem,
                "InputParam" + i,
                "InputType" + i,
                "InputTooltip" + i,
                true,
                false,
                bundle,
                isEditableLiteral,
                literalUpdaterFactory);

            if (field != null) {
                fieldList.add(field);
            }
        }

        List input = new ArrayList(fieldList);
        fieldList.clear();

        // creating output fields
        i = 1;

        int outputNum = 0;

        try {
            outputNum =
                Integer.parseInt((String) mItem.getItemAttribute("OutputNum"));
        } catch (java.lang.Throwable t) {
//            t.printStackTrace(System.err);
        }

        for (; i <= outputNum; i++) {
            field = generateField(
                mItem,
                "OutputParam" + i,
                "OutputType" + i,
                "OutputTooltip" + i,
                false,
                true,
                bundle,
                isEditableLiteral,
                literalUpdaterFactory);

            if (field != null) {
                fieldList.add(field);
            }
        }

        List output = new ArrayList(fieldList);
        
        boolean isAccumulative = false;
        Object accumObj = mItem.getItemAttribute("Accumulative");
        if (accumObj != null && accumObj instanceof Boolean) {
            isAccumulative = ((Boolean) accumObj).booleanValue();
        }
        
        return new BasicMethoid(this.getIcon(),
                MapperUtilities.cutAmpersand(mItem.getName()),
                mItem.getToolTip(), mItem, thisField, input, output,
                isAccumulative, isEditableLiteral);
    }
}
