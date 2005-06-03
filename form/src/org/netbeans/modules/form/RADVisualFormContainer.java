/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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

    // ------------------------------------------------------------------------------
    // Form synthetic properties

    /**
     * Getter for the Name property of the component - overriden to provide
     * non-null value, as the top-level component does not have a variable
     * @return current value of the Name property
     */
    public String getName() {
        return FormUtils.getBundleString("CTL_FormTopContainerName"); // NOI18N
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
            Dimension size = getDesignerSize();
            if (getBeanInstance() instanceof Dialog
                || getBeanInstance() instanceof Frame)
            {
                Dimension diffDim = getWindowContentDimensionDiff();
                size = new Dimension(size.width + diffDim.width,
                                     size.height + diffDim.height);
            }
            formSize = size; //new Dimension(400, 300); //topContainer.getSize();
        }
        return formSize;
    }

    public void setFormSize(Dimension value) {
        setFormSizeImpl(value);

        if (getFormSizePolicy() == GEN_BOUNDS) {
            if (getBeanInstance() instanceof Dialog
                || getBeanInstance() instanceof Frame)
            {
                Dimension diffDim = getWindowContentDimensionDiff();
                value = new Dimension(value.width - diffDim.width,
                                      value.height - diffDim.height);
            }
            setDesignerSizeImpl(value);
        }
    }

    private void setFormSizeImpl(Dimension value) {
        Object old = formSize;
        formSize = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_FORM_SIZE, old, value);

        if (getNodeReference() != null) // propagate the change to node
            getNodeReference().firePropertyChangeHelper(PROP_FORM_SIZE, old, value);
    }

    public Dimension getDesignerSize() {
        Dimension size = (Dimension) getAuxValue(FormDesigner.PROP_DESIGNER_SIZE);
        if (size == null)
            size = new Dimension(400, 300);
        return size;
    }

    public void setDesignerSize(Dimension value) {
        setDesignerSizeImpl(value);

        if (getFormSizePolicy() == GEN_BOUNDS) {
            if (getBeanInstance() instanceof Dialog
                || getBeanInstance() instanceof Frame)
            {
                Dimension diffDim = getWindowContentDimensionDiff();
                value = new Dimension(value.width + diffDim.width,
                                      value.height + diffDim.height);
            }
            setFormSizeImpl(value);
        }
    }

    private void setDesignerSizeImpl(Dimension value) {
        Object old = getDesignerSize();
        setAuxValue(FormDesigner.PROP_DESIGNER_SIZE, value);
        getFormModel().fireSyntheticPropertyChanged(
            this, FormDesigner.PROP_DESIGNER_SIZE, old, value);

        if (getNodeReference() != null) // propagate the change to node
            getNodeReference().firePropertyChangeHelper(FormDesigner.PROP_DESIGNER_SIZE, old, value);
    }

    public boolean getGeneratePosition() {
        return generatePosition;
    }

    public void setGeneratePosition(boolean value) {
        boolean old = generatePosition;
        generatePosition = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_GENERATE_POSITION,
                                        old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean getGenerateSize() {
        return generateSize;
    }

    public void setGenerateSize(boolean value) {
        boolean old = generateSize;
        generateSize = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_GENERATE_SIZE,
                                        old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean getGenerateCenter() {
        return generateCenter;
    }

    public void setGenerateCenter(boolean value) {
        boolean old = generateCenter;
        generateCenter = value;
        getFormModel().fireSyntheticPropertyChanged(this, PROP_GENERATE_CENTER,
                                        old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public int getFormSizePolicy() {
        return java.awt.Window.class.isAssignableFrom(getBeanClass())
                   || javax.swing.JInternalFrame.class.isAssignableFrom(getBeanClass())
               ? formSizePolicy : GEN_NOTHING;
    }

    public void setFormSizePolicy(int value) {
        int old = formSizePolicy;
        formSizePolicy = value;
        if (value == GEN_BOUNDS && formSize == null)
            setFormSize(getDesignerSize());
        getFormModel().fireSyntheticPropertyChanged(this, PROP_FORM_SIZE_POLICY,
                                        new Integer(old), new Integer(value));
    }

    // ------------------------------------------------------------------------------
    // End of form synthetic properties

    protected Node.Property[] createSyntheticProperties() {
        java.util.ResourceBundle bundle = FormUtils.getBundle();

        Node.Property policyProperty = new PropertySupport.ReadWrite(
            PROP_FORM_SIZE_POLICY,
            Integer.TYPE,
            bundle.getString("MSG_FormSizePolicy"), // NOI18N
            bundle.getString("HINT_FormSizePolicy")) // NOI18N
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
            bundle.getString("MSG_FormSize"), // NOI18N
            bundle.getString("HINT_FormSize")) // NOI18N
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
            bundle.getString("MSG_FormPosition"), // NOI18N
            bundle.getString("HINT_FormPosition")) // NOI18N
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
            bundle.getString("MSG_GeneratePosition"), // NOI18N
            bundle.getString("HINT_GeneratePosition")) // NOI18N
        {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return getGeneratePosition() ? Boolean.TRUE : Boolean.FALSE;
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
            bundle.getString("MSG_GenerateSize"), // NOI18N
            bundle.getString("HINT_GenerateSize")) // NOI18N
        {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return getGenerateSize() ? Boolean.TRUE : Boolean.FALSE;
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
            bundle.getString("MSG_GenerateCenter"), // NOI18N
            bundle.getString("HINT_GenerateCenter")) // NOI18N
        {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return getGenerateCenter() ? Boolean.TRUE : Boolean.FALSE;
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

        Node.Property designerSizeProperty = new PropertySupport.ReadOnly(
            FormDesigner.PROP_DESIGNER_SIZE,
            Dimension.class,
            bundle.getString("MSG_DesignerSize"), // NOI18N
            bundle.getString("HINT_DesignerSize")) // NOI18N
        {
            public Object getValue()
                throws IllegalAccessException, IllegalArgumentException,
                       java.lang.reflect.InvocationTargetException
            {
                return getDesignerSize();
            }

            public void setValue(Object val)
                throws IllegalAccessException, IllegalArgumentException,
                       java.lang.reflect.InvocationTargetException
            {
                if (!(val instanceof Dimension))
                    throw new IllegalArgumentException();
                setDesignerSize((Dimension)val);
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

    // ---------
    // providing the difference of the whole frame/dialog size and the size
    // of the content pane

    private static Dimension windowContentDimensionDiff;

    public static Dimension getWindowContentDimensionDiff() {
        if (windowContentDimensionDiff == null) {
            javax.swing.JFrame frame = new javax.swing.JFrame();
            frame.pack();
            Dimension d1 = frame.getSize();
            Dimension d2 = frame.getRootPane().getSize();
            windowContentDimensionDiff =
                new Dimension(d1.width - d2.width, d1.height - d2.height);
        }
        return windowContentDimensionDiff;
    }

    // ------------------------------------------------------------------------------------------
    // Innerclasses

    final public static class SizePolicyEditor extends java.beans.PropertyEditorSupport {
        /** Display Names for alignment. */
        private static final String[] names = {
            FormUtils.getBundleString("VALUE_sizepolicy_full"), // NOI18N
            FormUtils.getBundleString("VALUE_sizepolicy_pack"), // NOI18N
            FormUtils.getBundleString("VALUE_sizepolicy_none"), // NOI18N
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
