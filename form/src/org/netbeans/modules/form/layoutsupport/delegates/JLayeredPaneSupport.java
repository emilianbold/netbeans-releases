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
import javax.swing.*;
import java.beans.*;
import java.lang.reflect.Method;

import org.openide.nodes.*;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * @author Tomas Pavek
 */

public class JLayeredPaneSupport extends AbsoluteLayoutSupport {

    private static Method setBoundsMethod;

    public Class getSupportedClass() {
        return JLayeredPane.class;
    }

    public void convertConstraints(LayoutConstraints[] previousConstraints,
                                   LayoutConstraints[] currentConstraints,
                                   Component[] components)
    {
        return; // not needed here (contrary to AbsoluteLayoutSupport)
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        if (!(container instanceof JLayeredPane))
            return;

        for (int i=0; i < components.length; i++) {
            LayoutConstraints constraints = getConstraints(i + index);
            if (constraints instanceof LayeredConstraints) {
                Component comp = components[i];
                container.add(comp, constraints.getConstraintsObject(), i + index);

                Rectangle bounds = ((LayeredConstraints)constraints).getBounds();
                if (bounds.width == -1 || bounds.height == -1) {
                    Dimension pref = comp.getPreferredSize();
                    if (bounds.width == -1)
                        bounds.width = pref.width;
                    if (bounds.height == -1)
                        bounds.height = pref.height;
                }
                comp.setBounds(bounds);
            }
        }
    }

    protected LayoutConstraints readConstraintsCode(
                                    CodeElement constrElement,
                                    CodeConnectionGroup constrCode,
                                    CodeElement compElement)
    {
        LayeredConstraints constr = new LayeredConstraints(0, 0, 0, -1, -1);
//        constr.refComponent = getLayoutContext().getPrimaryComponent(index);

        CodeConnection[] connections = CodeStructure.getConnections(
                                           compElement, getSetBoundsMethod());
        if (connections.length > 0) {
            CodeConnection boundsConnection = connections[connections.length-1];
            constr.readPropertyElements(
                       boundsConnection.getConnectionParameters(), 1);
            constrCode.addConnection(boundsConnection);
        }

        FormCodeSupport.readPropertyElement(constrElement,
                                            constr.getProperties()[0],
                                            false);

        return constr;
    }

    protected CodeElement createConstraintsCode(CodeConnectionGroup constrCode,
                                                LayoutConstraints constr,
                                                CodeElement compElement,
                                                int index)
    {
        if (!(constr instanceof LayeredConstraints))
            return null;

        LayeredConstraints layerConstr = (LayeredConstraints) constr;
        layerConstr.refComponent = getLayoutContext().getPrimaryComponent(index);

        CodeStructure codeStructure = getCodeStructure();

        CodeConnection boundsConnection = CodeStructure.createConnection(
                           compElement,
                           getSetBoundsMethod(),
                           layerConstr.createPropertyElements(codeStructure, 1));
        constrCode.addConnection(boundsConnection);

        return codeStructure.createElement(
                 FormCodeSupport.createOrigin(layerConstr.getProperties()[0]));
    }

    protected LayoutConstraints createDefaultConstraints() {
        return new LayeredConstraints(0, 0, 0, -1, -1);
    }

    // ----------

    protected LayoutConstraints createNewConstraints(
                                    LayoutConstraints currentConstr,
                                    int x, int y, int w, int h)
    {
        int layer = currentConstr instanceof LayeredConstraints ?
                    ((LayeredConstraints)currentConstr).getLayer() : 0;

        return new LayeredConstraints(layer, x, y, w, h);
    }

    private static Method getSetBoundsMethod() {
        if (setBoundsMethod == null) {
            try {
                setBoundsMethod = Component.class.getMethod(
                                    "setBounds", // NOI18N
                                    new Class[] { Integer.TYPE, Integer.TYPE,
                                                  Integer.TYPE, Integer.TYPE });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return setBoundsMethod;
    }

    // ----------

    public static class LayeredConstraints extends AbsoluteLayoutConstraints {
        private int layer;

        public LayeredConstraints(int layer, int x, int y, int w, int h) {
            super(x, y, w, h);
            this.layer = layer;
            nullMode = true;
        }

        public int getLayer() {
            return layer;
        }

        // ------

        public Object getConstraintsObject() {
            return new Integer(layer);
        }

        public LayoutConstraints cloneConstraints() {
            return new LayeredConstraints(layer, x, y, w, h);
        }

        // -------

        protected Node.Property[] createProperties() {
            Node.Property[] props = super.createProperties();
            Node.Property[] layeredProps = new Node.Property[props.length + 1];

            layeredProps[0] =
                new FormProperty("layer", // NOI18N
                                 Integer.TYPE,
                             getBundle().getString("PROP_layer"), // NOI18N
                             getBundle().getString("HINT_layer")) { // NOI18N

                    public Object getTargetValue() {
                        return new Integer(layer);
                    }
                    public void setTargetValue(Object value) {
                        layer = ((Integer)value).intValue();
                    }
                    public boolean supportsDefaultValue () {
                        return true;
                    }
                    public Object getDefaultValue() {
                        return new Integer(0);
                    }
                    public PropertyEditor getExpliciteEditor() {
                        return new LayerEditor();
                    }
                };

            for (int i=0; i < props.length; i++)
                layeredProps[i+1] = props[i];

            return layeredProps;
        }
    }

    // ---------

    public static final class LayerEditor extends PropertyEditorSupport
                                          implements EnhancedPropertyEditor {
        final String[] tags = {
            "DEFAULT_LAYER", // NOI18N
            "PALETTE_LAYER", // NOI18N
            "MODAL_LAYER", // NOI18N
            "POPUP_LAYER", // NOI18N
            "DRAG_LAYER" // NOI18N
        };

        final Integer[] values = {
            JLayeredPane.DEFAULT_LAYER,
            JLayeredPane.PALETTE_LAYER,
            JLayeredPane.MODAL_LAYER,
            JLayeredPane.POPUP_LAYER,
            JLayeredPane.DRAG_LAYER
        };

        final String[] javaInitStrings = {
            "javax.swing.JLayeredPane.DEFAULT_LAYER", // NOI18N
            "javax.swing.JLayeredPane.PALETTE_LAYER", // NOI18N
            "javax.swing.JLayeredPane.MODAL_LAYER", // NOI18N
            "javax.swing.JLayeredPane.POPUP_LAYER", // NOI18N
            "javax.swing.JLayeredPane.DRAG_LAYER" // NOI18N
        };

        public String[] getTags() {
            return tags;
        }

        public String getAsText() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return tags[i];

            return value.toString();
        }

        public void setAsText(String str) {
            for (int i=0; i < tags.length; i++)
                if (tags[i].equals(str)) {
                    setValue(values[i]);
                    return;
                }

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

            return value != null ? 
                       "new Integer(" + value.toString() + ")" // NOI18N
                       : null;
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
}
