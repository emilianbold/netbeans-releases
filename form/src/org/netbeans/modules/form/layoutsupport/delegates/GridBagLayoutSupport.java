/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.lang.reflect.*;

import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * @author Tran Duc Trung, Tomas Pavek
 */

public class GridBagLayoutSupport extends AbstractLayoutSupport
{
    // [where is the icon ??]
    private static Constructor constrConstructor;

//    private String constraintsVariableName;
    public Class getSupportedClass() {
        return GridBagLayout.class;
    }

    public Class getCustomizerClass() {
        return GridBagCustomizer.class;
    }

    public void convertConstraints(LayoutConstraints[] previousConstraints,
                                   LayoutConstraints[] currentConstraints,
                                   Component[] components)
    {
        if (previousConstraints == null || currentConstraints == null
                || components == null)
            return;

        for (int i=0, n=currentConstraints.length; i < n; i++) {
            if (currentConstraints[i] == null
                && previousConstraints[i] instanceof
                    AbsoluteLayoutSupport.AbsoluteLayoutConstraints)
            {
                Rectangle iBounds = components[i].getBounds();
                Dimension prefSize = components[i].getPreferredSize();
                GridBagConstraints constr = new GridBagConstraints();

                int gx = 0, gy = 0, gw = 1, gh = 1;
                int ix1 = iBounds.x;
                int iy1 = iBounds.y;
                int ix2 = ix1 + iBounds.width;
                int iy2 = iy1 + iBounds.height;
                int fromX = 0, fromY = 0;

                for (int j=0; j < n; j++) {
                    Rectangle jBounds = components[j].getBounds();
                    int jx2 = jBounds.x + jBounds.width;
                    int jy2 = jBounds.y + jBounds.height;
                    if (jx2 <= ix1) {
                        gx++;
                        fromX = Math.max(fromX, jx2);
                    }
                    if (jy2 <= iy1) {
                        gy++;
                        fromY = Math.max(fromY, jy2);
                    }
                    if (jx2 > ix1 && jx2 < ix2) gw++;
                    if (jy2 > iy1 && jy2 < iy2) gh++;
                }

                constr.gridx = gx;
                constr.gridy = gy;
                constr.gridwidth = gw;
                constr.gridheight = gh;
                constr.insets = new Insets(iy1 - fromY, ix1 - fromX, 0, 0);
                constr.fill = GridBagConstraints.BOTH;
                constr.ipadx = iBounds.width - prefSize.width;
                constr.ipady = iBounds.height - prefSize.height;

                currentConstraints[i] = new GridBagLayoutConstraints(constr);
            }
        }
    }

    // --------

    protected LayoutConstraints readConstraintsCode(
                                    CodeElement constrElement,
                                    CodeConnectionGroup constrCode,
                                    CodeElement compElement)
    {
        GridBagLayoutConstraints constr = new GridBagLayoutConstraints();
        constr.readCodeElement(constrElement, constrCode);
        return constr;
    }

    protected CodeElement createConstraintsCode(CodeConnectionGroup constrCode,
                                                LayoutConstraints constr,
                                                CodeElement compElement,
                                                int index)
    {
        if (!(constr instanceof GridBagLayoutConstraints))
            return null;

        return ((GridBagLayoutConstraints)constr).createCodeElement(
                                            getCodeStructure(), constrCode);
    }

    protected LayoutConstraints createDefaultConstraints() {
        return new GridBagLayoutConstraints();
    }

    // -----------------

    public static class GridBagLayoutConstraints implements LayoutConstraints {
        private GridBagConstraints constraints;

        private GridBagConstraints defaultConstraints = new GridBagConstraints();

        private Property[] properties;

        private CodeElement constraintsElement;
        private CodeConnectionGroup constraintsCode;
        private CodeConnection[] propertyConnections;

        private static Constructor constrConstructor;

        public GridBagLayoutConstraints() {
            constraints = new GridBagConstraints();
        }

        public GridBagLayoutConstraints(GridBagConstraints constraints) {
            this.constraints = constraints;
        }

        public Node.Property[] getProperties() {
            if (properties == null) {
                createProperties();
                reinstateProperties();
            }
            return properties;
        }

        public Object getConstraintsObject() {
            return constraints;
        }

        public LayoutConstraints cloneConstraints() {
            return new GridBagLayoutConstraints(constraints);
        }

        // -------

        private CodeElement createCodeElement(CodeStructure codeStructure,
                                              CodeConnectionGroup constrCode)
        {
            this.constraintsCode = constrCode;
            propertyConnections = null;

            constraintsElement = codeStructure.createElement(
                                     getConstraintsConstructor(),
                                     CodeStructure.EMPTY_PARAMS);
            updateCodeElement();

            return constraintsElement;
        }

        private void readCodeElement(CodeElement constrElement,
                                     CodeConnectionGroup constrCode)
        {
            constraintsElement = constrElement;
            constraintsCode = constrCode;
            propertyConnections = null;

//            constrElement.setOrigin(CodeStructure.createOrigin(
//                                        getConstraintsConstructor(),
//                                        CodeStructure.EMPTY_PARAMS));

            getProperties(); // ensure properties are created

            boolean isAnyChanged = false;

            Iterator it = CodeStructure.getConnectionsIterator(constrElement);
            while (it.hasNext()) {
                CodeConnection connection = (CodeConnection) it.next();
                for (int j=0; j < properties.length; j++) {
                    Property prop = properties[j];
                    if (prop.field.equals(connection.getConnectingObject())) {
                        FormCodeSupport.readPropertyConnection(
                                            connection, prop, false);
                        setPropertyConnection(j, connection);
                        if (prop.isChanged()) {
                            constrCode.addConnection(connection);
                            isAnyChanged = true;
                        }
                        break;
                    }
                }
            }

            setupVariable(isAnyChanged);
        }

        private void updateCodeElement() {
            if (constraintsCode == null || constraintsElement == null)
                return;

            constraintsCode.removeAll();

            getProperties(); // ensure properties are created

            boolean isAnyChanged = false;
            for (int i=0; i < properties.length; i++)
                if (properties[i].isChanged()) {
                    constraintsCode.addConnection(getPropertyConnection(i));
                    isAnyChanged = true;
                }

            setupVariable(isAnyChanged);
        }

        private CodeConnection getPropertyConnection(int index) {
            if (propertyConnections == null)
                propertyConnections = new CodeConnection[properties.length];

            CodeConnection propConnection = propertyConnections[index];
            if (propConnection == null) {
                CodeElement propElement =
                    constraintsElement.getCodeStructure().createElement(
                        FormCodeSupport.createOrigin(properties[index]));

                propConnection = CodeStructure.createConnection(
                                     constraintsElement,
                                     properties[index].field,
                                     propElement);

                propertyConnections[index] = propConnection;
            }
            return propConnection;
        }

        private void setPropertyConnection(int index,
                                           CodeConnection propConnection)
        {
            if (propertyConnections == null)
                propertyConnections = new CodeConnection[properties.length];
            propertyConnections[index] = propConnection;
        }

        private void setupVariable(boolean anyChangedProperty) {
            CodeStructure codeStructure = constraintsElement.getCodeStructure();
            if (anyChangedProperty) {
                CodeElementVariable var = codeStructure.createVariable(
                        constraintsElement, CodeElementVariable.LOCAL, null);
                constraintsCode.addConnection(0, var.getAssignmentConnection());
            }
            else {
                codeStructure.releaseVariable(constraintsElement);
            }
        }

        private void createProperties() {
            properties = new Property[] {
                new Property("gridx", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridx"), // NOI18N
                             getBundle().getString("HINT_gridx"), // NOI18N
                             GridPosEditor.class),

                new Property("gridy", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridy"), // NOI18N
                             getBundle().getString("HINT_gridy"), // NOI18N
                             GridPosEditor.class),

                new Property("gridwidth", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridwidth"), // NOI18N
                             getBundle().getString("HINT_gridwidth"), // NOI18N
                             GridSizeEditor.class),

                new Property("gridheight", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridheight"), // NOI18N
                             getBundle().getString("HINT_gridheight"), // NOI18N
                             GridSizeEditor.class),

                new Property("fill", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_fill"), // NOI18N
                             getBundle().getString("HINT_fill"), // NOI18N
                             FillEditor.class),

                new Property("ipadx", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_ipadx"), // NOI18N
                             getBundle().getString("HINT_ipadx"), // NOI18N
                             null),

                new Property("ipady", // NOI18N
                              Integer.TYPE,
                              getBundle().getString("PROP_ipady"), // NOI18N
                              getBundle().getString("HINT_ipady"), // NOI18N
                              null),

                new Property("insets", // NOI18N
                             Insets.class,
                             getBundle().getString("PROP_insets"), // NOI18N
                             getBundle().getString("HINT_insets"), // NOI18N
                             null),

                new Property("anchor", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_anchor"), // NOI18N
                             getBundle().getString("HINT_anchor"), // NOI18N
                             AnchorEditor.class),

                new Property("weightx", // NOI18N
                             Double.TYPE,
                             getBundle().getString("PROP_weightx"), // NOI18N
                             getBundle().getString("HINT_weightx"), // NOI18N
                             null),

                new Property("weighty", // NOI18N
                             Double.TYPE,
                             getBundle().getString("PROP_weighty"), // NOI18N
                             getBundle().getString("HINT_weighty"), // NOI18N
                             null)
            };
        }

        private void reinstateProperties() {
            try {
                for (int i=0; i < properties.length; i++) {
                    FormProperty prop = (FormProperty) properties[i];
                    prop.reinstateProperty();
                }
            }
            catch(IllegalAccessException e1) {} // should not happen
            catch(InvocationTargetException e2) {} // should not happen
        }

        private static Constructor getConstraintsConstructor() {
            if (constrConstructor == null) {
                try {
                    constrConstructor =
                        GridBagConstraints.class.getConstructor(new Class[0]);
                }
                catch (NoSuchMethodException ex) { // should not happen
                    ex.printStackTrace();
                }
            }
            return constrConstructor;
        }

        // ---------

        private final class Property extends FormProperty {
            private Field field;
            private Class propertyEditorClass;

            Property(String name, Class type,
                     String displayName, String shortDescription,
                     Class propertyEditorClass)
            {
                super(name, type, displayName, shortDescription);
                this.propertyEditorClass = propertyEditorClass;
                try {
                    field = GridBagConstraints.class.getField(getName());
                }
                catch (NoSuchFieldException ex) { // should not happen
                    ex.printStackTrace();
                }
            }

            public Object getTargetValue() {
                try {
                    return field.get(constraints);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            public void setTargetValue(Object value) {
                try {
                    field.set(constraints, value);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                }
                    
            }

            public boolean supportsDefaultValue () {
                return true;
            }

            public Object getDefaultValue() {
                try {
                    return field.get(defaultConstraints);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            public PropertyEditor getExpliciteEditor() {
                if (propertyEditorClass == null)
                    return null;
                try {
                    return (PropertyEditor) propertyEditorClass.newInstance();
                }
                catch (Exception ex) { //should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            protected void propertyValueChanged(Object old, Object current) {
                if (isChangeFiring())
                    updateCodeElement();
                super.propertyValueChanged(old, current);
            }
        }
    }

    // ------------
    // property editors for properties of GridBagLayoutConstraints

    private abstract static class GridBagConstrEditor extends PropertyEditorSupport {
        String[] tags;
        Integer[] values;
        String[] javaInitStrings;
        boolean otherValuesAllowed;

        public String[] getTags() {
            return tags;
        }

        public String getAsText() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return tags[i];

            return otherValuesAllowed && value != null ?
                       value.toString() : null;
        }

        public void setAsText(String str) {
            for (int i=0; i < tags.length; i++)
                if (tags[i].equals(str)) {
                    setValue(values[i]);
                    return;
                }

            if (otherValuesAllowed)
                try {
                    setValue(new Integer(Integer.parseInt(str)));
                } 
                catch (NumberFormatException e) {} // ignore
        }

        public String getJavaInitializationString() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return javaInitStrings[i];

            if (!otherValuesAllowed)
                return javaInitStrings[0];
            return value != null ? value.toString() : null;
        }
    }

    public static final class GridPosEditor extends GridBagConstrEditor
                                        implements EnhancedPropertyEditor {
        public GridPosEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_relative") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.RELATIVE)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.RELATIVE" // NOI18N
            };
            otherValuesAllowed = true;
        }

        public Component getInPlaceCustomEditor() {
            return null;
        }
        public boolean hasInPlaceCustomEditor() {
            return false;
        }
        public boolean supportsEditingTaggedValues() {
            return true;
        }
    }

    public static final class GridSizeEditor extends GridBagConstrEditor
                                        implements EnhancedPropertyEditor {
        public GridSizeEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_relative"), // NOI18N
                getBundle().getString("VALUE_remainder") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.RELATIVE),
                new Integer(GridBagConstraints.REMAINDER)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.RELATIVE", // NOI18N
                "java.awt.GridBagConstraints.REMAINDER" // NOI18N
            };
            otherValuesAllowed = true;
        }

        public Component getInPlaceCustomEditor() {
            return null;
        }
        public boolean hasInPlaceCustomEditor() {
            return false;
        }
        public boolean supportsEditingTaggedValues() {
            return true;
        }
    }

    public static final class FillEditor extends GridBagConstrEditor {
        public FillEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_fill_none"), // NOI18N
                getBundle().getString("VALUE_fill_horizontal"), // NOI18N
                getBundle().getString("VALUE_fill_vertical"), // NOI18N
                getBundle().getString("VALUE_fill_both") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.NONE),
                new Integer(GridBagConstraints.HORIZONTAL),
                new Integer(GridBagConstraints.VERTICAL),
                new Integer(GridBagConstraints.BOTH)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.NONE", // NOI18N
                "java.awt.GridBagConstraints.HORIZONTAL", // NOI18N
                "java.awt.GridBagConstraints.VERTICAL", // NOI18N
                "java.awt.GridBagConstraints.BOTH" // NOI18N
            };
            otherValuesAllowed = false;
        }
    }

    public static final class AnchorEditor extends GridBagConstrEditor {
        public AnchorEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_anchor_center"), // NOI18N
                getBundle().getString("VALUE_anchor_north"), // NOI18N
                getBundle().getString("VALUE_anchor_northeast"), // NOI18N
                getBundle().getString("VALUE_anchor_east"), // NOI18N
                getBundle().getString("VALUE_anchor_southeast"), // NOI18N
                getBundle().getString("VALUE_anchor_south"), // NOI18N
                getBundle().getString("VALUE_anchor_southwest"), // NOI18N
                getBundle().getString("VALUE_anchor_west"), // NOI18N
                getBundle().getString("VALUE_anchor_northwest"), // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.CENTER),
                new Integer(GridBagConstraints.NORTH),
                new Integer(GridBagConstraints.NORTHEAST),
                new Integer(GridBagConstraints.EAST),
                new Integer(GridBagConstraints.SOUTHEAST),
                new Integer(GridBagConstraints.SOUTH),
                new Integer(GridBagConstraints.SOUTHWEST),
                new Integer(GridBagConstraints.WEST),
                new Integer(GridBagConstraints.NORTHWEST)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.CENTER", // NOI18N
                "java.awt.GridBagConstraints.NORTH", // NOI18N
                "java.awt.GridBagConstraints.NORTHEAST", // NOI18N
                "java.awt.GridBagConstraints.EAST", // NOI18N
                "java.awt.GridBagConstraints.SOUTHEAST", // NOI18N
                "java.awt.GridBagConstraints.SOUTH", // NOI18N
                "java.awt.GridBagConstraints.SOUTHWEST", // NOI18N
                "java.awt.GridBagConstraints.WEST", // NOI18N
                "java.awt.GridBagConstraints.NORTHWEST" // NOI18N
            };
            otherValuesAllowed = false;
        }
    }

    // ------
    // temporary hacks for GridBagCustomizer and GridBagControlCenter

    static ResourceBundle getBundleHack() {
        return AbstractLayoutSupport.getBundle();
    }

    LayoutSupportContext getLayoutSupportHack() {
        return super.getLayoutContext();
    }
}
