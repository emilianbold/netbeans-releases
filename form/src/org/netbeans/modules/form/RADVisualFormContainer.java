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

package org.netbeans.modules.form;

import java.awt.*;

import org.openide.nodes.*;

/**
 * RADVisualFormContainer represents the top-level container of the form and
 * the form itself during design time.
 *
 * @author Ian Formanek
 */

public class RADVisualFormContainer extends RADVisualContainer implements FormContainer
{
    public static final String PROP_FORM_SIZE_POLICY = "formSizePolicy"; // NOI18N
    public static final String PROP_FORM_SIZE = "formSize"; // NOI18N
    public static final String PROP_FORM_POSITION = "formPosition"; // NOI18N
    public static final String PROP_GENERATE_POSITION = "generatePosition"; // NOI18N
    public static final String PROP_GENERATE_SIZE = "generateSize"; // NOI18N
    public static final String PROP_GENERATE_CENTER = "generateCenter"; // NOI18N
    public static final String PROP_DESIGNER_SIZE = "designerSize"; // NOI18N

    public static final int GEN_BOUNDS = 0;
    public static final int GEN_PACK = 1;
    public static final int GEN_NOTHING = 2;

    // Synthetic properties of form
    private Dimension formSize;// = new Dimension(FormEditor.DEFAULT_FORM_WIDTH, FormEditor.DEFAULT_FORM_HEIGHT);
    private Point formPosition;
    private boolean generatePosition = true;
    private boolean generateSize = true;
    private boolean generateCenter = true;
    private int formSizePolicy = GEN_NOTHING;
    private Dimension designerSize;


    // ------------------------------------------------------------------------------
    // Form synthetic properties

    /**
     * Getter for the Name property of the component - overriden to provide
     * non-null value, as the top-level component does not have a variable
     * @return current value of the Name property
     */
    public String getName() {
        return FormEditor.getFormBundle().getString("CTL_FormTopContainerName"); // NOI18N
    }

    /**
     * Setter for the Name property of the component - usually maps to
     * variable declaration for holding the instance of the component
     * @param value new value of the Name property
     */
    public void setName(String value) {
        // noop in forms
    }

    public Point getFormPosition() {
        if (formPosition == null) {
            formPosition = new Point(0,0);//topContainer.getLocation();
        }
        return formPosition;
    }

    public void setFormPosition(Point value) {
        Object old = formPosition;
        formPosition = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_FORM_POSITION,
                                                    old, value);
    }

    public Dimension getFormSize() {
        if (formSize == null) {
            formSize = new Dimension(400, 300); //topContainer.getSize();
        }
        return formSize;
    }

    public void setFormSize(Dimension value) {
        Object old = formSize;
        formSize = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_FORM_SIZE, old, value);
        
        if (getFormSizePolicy() == GEN_BOUNDS && !getDesignerSize().equals(value)) {
            setDesignerSize(value);
        }
        
        if (getNodeReference() != null) { // propagate the change to node
            getNodeReference().firePropertyChangeHelper(PROP_FORM_SIZE, old, value);
        }
    }
    
    public Dimension getDesignerSize() {
        if (designerSize == null) {
            designerSize = new Dimension(400, 300);
        }
        return designerSize;
    }

    public void setDesignerSize(Dimension value) {
        Object old = designerSize;
        designerSize = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_DESIGNER_SIZE, old, value);

        if (getFormSizePolicy() == GEN_BOUNDS && !getFormSize().equals(value)) {
            setFormSize(value);
        }        
        
    }

    public boolean getGeneratePosition() {
        return generatePosition;
    }

    public void setGeneratePosition(boolean value) {
        // [PENDING - set as aux value]
        boolean old = generatePosition;
        generatePosition = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_GENERATE_POSITION,
                                        new Boolean(old), new Boolean(value));
    }

    public boolean getGenerateSize() {
        return generateSize;
    }

    public void setGenerateSize(boolean value) {
        // [PENDING - set as aux value]
        boolean old = generateSize;
        generateSize = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_GENERATE_SIZE,
                                        new Boolean(old), new Boolean(value));
    }

    public boolean getGenerateCenter() {
        return generateCenter;
    }

    public void setGenerateCenter(boolean value) {
        // [PENDING - set as aux value]
        boolean old = generateCenter;
        generateCenter = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_GENERATE_CENTER,
                                        new Boolean(old), new Boolean(value));
    }

    public int getFormSizePolicy() {
        return java.awt.Window.class.isAssignableFrom(getBeanClass())
                   || javax.swing.JInternalFrame.class.isAssignableFrom(getBeanClass())
               ? formSizePolicy : GEN_NOTHING;
    }

    public void setFormSizePolicy(int value) {
        // [PENDING - set as aux value]
        int old = formSizePolicy;
        formSizePolicy = value;
        if (value == GEN_BOUNDS)
            setFormSize(getDesignerSize());
        getFormModel().fireSyntheticPropertyChanged(this, PROP_FORM_SIZE_POLICY,
                                        new Integer(old), new Integer(value));
    }

    // ------------------------------------------------------------------------------
    // End of form synthetic properties

    protected Node.Property[] createSyntheticProperties() {
        Node.Property policyProperty = new PropertySupport.ReadWrite(
            PROP_FORM_SIZE_POLICY,
            Integer.TYPE,
            FormEditor.getFormBundle().getString("MSG_FormSizePolicy"), // NOI18N
            FormEditor.getFormBundle().getString("HINT_FormSizePolicy")) // NOI18N
        {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Integer(getFormSizePolicy());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Integer)) throw new IllegalArgumentException();
                setFormSizePolicy(((Integer)val).intValue());
                if (getNodeReference() != null)
                    getNodeReference().fireComponentPropertySetsChange();
            }

            public boolean canWrite() {
                return !isReadOnly();
            }

            /** Editor for alignment */
            public java.beans.PropertyEditor getPropertyEditor() {
                return new SizePolicyEditor();
            }

        };

        Node.Property sizeProperty = new PropertySupport.ReadWrite(
            PROP_FORM_SIZE,
            Dimension.class,
            FormEditor.getFormBundle().getString("MSG_FormSize"), // NOI18N
            FormEditor.getFormBundle().getString("HINT_FormSize")) // NOI18N
        {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return getFormSize();
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Dimension)) throw new IllegalArgumentException();
                setFormSize((Dimension)val);
            }

            public boolean canWrite() {
                return !isReadOnly()
                       && getFormSizePolicy() == GEN_BOUNDS
                       && getGenerateSize();
            }
        };

        Node.Property positionProperty = new PropertySupport.ReadWrite(
            PROP_FORM_POSITION,
            Point.class,
            FormEditor.getFormBundle().getString("MSG_FormPosition"), // NOI18N
            FormEditor.getFormBundle().getString("HINT_FormPosition")) // NOI18N
        {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return getFormPosition();
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Point)) throw new IllegalArgumentException();
                setFormPosition((Point)val);
            }

            public boolean canWrite() {
                return !isReadOnly()
                        && getFormSizePolicy() == GEN_BOUNDS
                        && getGeneratePosition()
                        && !getGenerateCenter();
            }
        };

        Node.Property genPositionProperty = new PropertySupport.ReadWrite(
            PROP_GENERATE_POSITION,
            Boolean.TYPE,
            FormEditor.getFormBundle().getString("MSG_GeneratePosition"), // NOI18N
            FormEditor.getFormBundle().getString("HINT_GeneratePosition")) // NOI18N
        {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Boolean(getGeneratePosition());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGeneratePosition(((Boolean)val).booleanValue());
                if (getNodeReference() != null)
                    getNodeReference().fireComponentPropertySetsChange();
            }

            public boolean canWrite() {
                return !isReadOnly()
                        && getFormSizePolicy() == GEN_BOUNDS
                        && !getGenerateCenter();
            }
        };

        Node.Property genSizeProperty = new PropertySupport.ReadWrite(
            PROP_GENERATE_SIZE,
            Boolean.TYPE,
            FormEditor.getFormBundle().getString("MSG_GenerateSize"), // NOI18N
            FormEditor.getFormBundle().getString("HINT_GenerateSize")) // NOI18N
        {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Boolean(getGenerateSize());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGenerateSize(((Boolean)val).booleanValue());
                if (getNodeReference() != null)
                    getNodeReference().fireComponentPropertySetsChange();
            }

            public boolean canWrite() {
                return !isReadOnly() && getFormSizePolicy() == GEN_BOUNDS;
            }
        };

        Node.Property genCenterProperty = new PropertySupport.ReadWrite(
            PROP_GENERATE_CENTER,
            Boolean.TYPE,
            FormEditor.getFormBundle().getString("MSG_GenerateCenter"), // NOI18N
            FormEditor.getFormBundle().getString("HINT_GenerateCenter")) // NOI18N
        {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Boolean(getGenerateCenter());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGenerateCenter(((Boolean)val).booleanValue());
                if (getNodeReference() != null)
                    getNodeReference().fireComponentPropertySetsChange();
            }

            public boolean canWrite() {
                return !isReadOnly() && getFormSizePolicy() == GEN_BOUNDS;
            }
        };

        Node.Property designerSizeProperty = new PropertySupport.ReadWrite(
            PROP_DESIGNER_SIZE,
            Dimension.class,
            FormEditor.getFormBundle().getString("MSG_DesignerSize"), // NOI18N
            FormEditor.getFormBundle().getString("HINT_DesignerSize")) // NOI18N
        {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return getDesignerSize();
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Dimension)) throw new IllegalArgumentException();
                setDesignerSize((Dimension)val);
            }

            public boolean canWrite() {
                return false;
            }
        };

        java.util.List propList = new java.util.ArrayList();

        if (java.awt.Window.class.isAssignableFrom(getBeanClass())
            || javax.swing.JInternalFrame.class.isAssignableFrom(getBeanClass()))
        {
            propList.add(sizeProperty);
            propList.add(positionProperty);
            propList.add(policyProperty);
            propList.add(genPositionProperty);
            propList.add(genSizeProperty);
            propList.add(genCenterProperty);
        }
        
        propList.add(designerSizeProperty);

        Node.Property[] props = new Node.Property[propList.size()];
        propList.toArray(props);
        return props;
    }

    // ------------------------------------------------------------------------------------------
    // Innerclasses

    final public static class SizePolicyEditor extends java.beans.PropertyEditorSupport {
        /** Display Names for alignment. */
        private static final String[] names = {
            FormEditor.getFormBundle().getString("VALUE_sizepolicy_full"),
            FormEditor.getFormBundle().getString("VALUE_sizepolicy_pack"),
            FormEditor.getFormBundle().getString("VALUE_sizepolicy_none"),
        };

        /** @return names of the possible directions */
        public String[] getTags() {
            return names;
        }

        /** @return text for the current value */
        public String getAsText() {
            int value =((Integer)getValue()).intValue();
            return names[value];
        }

        /** Setter.
         * @param str string equal to one value from directions array
         */
        public void setAsText(String str) {
            if (names[0].equals(str))
                setValue(new Integer(0));
            else if (names[1].equals(str))
                setValue(new Integer(1));
            else if (names[2].equals(str))
                setValue(new Integer(2));
        }
    }
}
