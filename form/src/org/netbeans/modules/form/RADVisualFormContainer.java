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


    public String getJavaContainerDelegateString() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            return delegateGetter + "()"; // NOI18N
        }
        else
            return "";          // NOI18N
    }
    
    /** Called to obtain a Java code to be used to generate code to access the
     * container for adding subcomponents.  It is expected that the returned
     * code is either ""(in which case the form is the container) or is a name
     * of variable or method call ending with
     * "."(e.g. "container.getContentPane().").  This implementation simply
     * delegates to FormInfo.getContainerGenName().
     * @return the prefix code for generating code to add subcomponents to this
     * container
     */
    public String getContainerGenName() {
        String delegateGetter = getContainerDelegateGetterName();
        if (delegateGetter != null) {
            return delegateGetter + "()."; // NOI18N
        }
        else
            return "";          // NOI18N
//        return formInfo.getContainerGenName();
    }

    // ------------------------------------------------------------------------------
    // Form synthetic properties

    /**
     * Getter for the Name property of the component - overriden to provide
     * non-null value, as the top-level component does not have a variable
     * @return current value of the Name property
     */
    public String getName() {
        return FormEditor.getFormBundle().getString("CTL_FormTopContainerName");
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
        getFormModel().fireSyntheticPropertyChanged(this, PROP_FORM_SIZE,
                                                    old, value);
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
        getFormModel().fireSyntheticPropertyChanged(this, PROP_FORM_SIZE_POLICY,
                                        new Integer(old), new Integer(value));
    }

    // ------------------------------------------------------------------------------
    // End of form synthetic properties

    protected Node.Property[] createSyntheticProperties() {
/*        if (!getFormModel().getFormEditorSupport().supportsAdvancedFeatures()) {
            return getFormMenuType() > 0 ?
                     new Node.Property[] { createMenuProperty() } :
                     new Node.Property[] { };
//            if ((formInfo instanceof JMenuBarContainer) ||(formInfo instanceof MenuBarContainer)) {
//                return new Node.Property[] { createMenuProperty() } ;
//            } else {
//                return new Node.Property[0];
//            }
        } */

        Node.Property policyProperty = new PropertySupport.ReadWrite(PROP_FORM_SIZE_POLICY, Integer.TYPE,
                                                                     FormEditor.getFormBundle().getString("MSG_FormSizePolicy"),
                                                                     FormEditor.getFormBundle().getString("MSG_FormSizePolicy")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Integer(getFormSizePolicy());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Integer)) throw new IllegalArgumentException();
                setFormSizePolicy(((Integer)val).intValue());
            }

            public boolean canWrite() {
                return !isReadOnly();
            }

            /** Editor for alignment */
            public java.beans.PropertyEditor getPropertyEditor() {
                return new SizePolicyEditor();
            }

        };


        Node.Property sizeProperty = new PropertySupport.ReadWrite(PROP_FORM_SIZE, Dimension.class,
                                                                   FormEditor.getFormBundle().getString("MSG_FormSize"),
                                                                   FormEditor.getFormBundle().getString("MSG_FormSize")) {
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
                return !isReadOnly();
            }
        };

        Node.Property positionProperty = new PropertySupport.ReadWrite(PROP_FORM_POSITION, Point.class,
                                                                       FormEditor.getFormBundle().getString("MSG_FormPosition"),
                                                                       FormEditor.getFormBundle().getString("MSG_FormPosition")) {
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
                return !isReadOnly();
            }
        };

        Node.Property genPositionProperty = new PropertySupport.ReadWrite(PROP_GENERATE_POSITION, Boolean.TYPE,
                                                                          FormEditor.getFormBundle().getString("MSG_GeneratePosition"),
                                                                          FormEditor.getFormBundle().getString("MSG_GeneratePosition")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Boolean(getGeneratePosition());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGeneratePosition(((Boolean)val).booleanValue());
            }

            public boolean canWrite() {
                return !isReadOnly();
            }
        };

        Node.Property genSizeProperty = new PropertySupport.ReadWrite(PROP_GENERATE_SIZE, Boolean.TYPE,
                                                                      FormEditor.getFormBundle().getString("MSG_GenerateSize"),
                                                                      FormEditor.getFormBundle().getString("MSG_GenerateSize")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Boolean(getGenerateSize());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGenerateSize(((Boolean)val).booleanValue());
            }

            public boolean canWrite() {
                return !isReadOnly();
            }
        };

        Node.Property genCenterProperty = new PropertySupport.ReadWrite(PROP_GENERATE_CENTER, Boolean.TYPE,
                                                                        FormEditor.getFormBundle().getString("MSG_GenerateCenter"),
                                                                        FormEditor.getFormBundle().getString("MSG_GenerateCenter")) {
            public Object getValue() throws
                IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                return new Boolean(getGenerateCenter());
            }

            public void setValue(Object val) throws IllegalAccessException,
                                                    IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                if (!(val instanceof Boolean)) throw new IllegalArgumentException();
                setGenerateCenter(((Boolean)val).booleanValue());
            }

            public boolean canWrite() {
                return !isReadOnly();
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
