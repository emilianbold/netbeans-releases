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
import javax.swing.*;
import java.lang.reflect.Constructor;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * Support class for BoxLayout. As BoxLayout is not a bean, it must be
 * handled differently from other layout managers.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class BoxLayoutSupport extends AbstractLayoutSupport
{
    private int axis = BoxLayout.X_AXIS;

    private Node.PropertySet propertySet;
    private FormProperty[] properties;

    private static Constructor boxLayoutConstructor;

    public Class getSupportedClass() {
        return BoxLayout.class;
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(containerDelegate.getLayout() instanceof BoxLayout))
            return -1;
        
        Component[] components = containerDelegate.getComponents();
        for (int i = 0; i < components.length; i++) {
            Rectangle b = components[i].getBounds();
            if (axis == BoxLayout.X_AXIS) {
                if (posInCont.x < b.x + b.width / 2)
                    return i;
            }
            else {
                if (posInCont.y < b.y + b.height / 2)
                    return i;
            }
        }
        
        return components.length;
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(containerDelegate.getLayout() instanceof BoxLayout))
            return false;

        Component[] components = containerDelegate.getComponents();
        Rectangle rect;

        if (components.length == 0) {
            Insets ins = containerDelegate.getInsets();
            rect = axis == BoxLayout.X_AXIS ?
                   new Rectangle(ins.left,
                                 ins.top + (containerDelegate.getHeight()
                                     - ins.top - ins.bottom - 20) / 2,
                                 30, 20) :
                   new Rectangle(ins.left + (containerDelegate.getWidth()
                                     - ins.left - ins.right - 30) / 2,
                                 ins.top,
                                 30, 20);
        }
        else if (newIndex < 0 || newIndex >= components.length) {
            Rectangle b = components[components.length - 1].getBounds();
            rect = axis == BoxLayout.X_AXIS ?
                   new Rectangle(b.x + b.width - 10, b.y, 20, b.height) :
                   new Rectangle(b.x, b.y + b.height - 10, b.width, 20);
        }
        else {
            Rectangle b = components[newIndex].getBounds();
            rect = axis == BoxLayout.X_AXIS ?
                   new Rectangle(b.x - 10, b.y, 20, b.height) :
                   new Rectangle(b.x, b.y - 10, b.width, 20);
        }

        g.drawRect(rect.x, rect.y, rect.width, rect.height);

        return true;
    }

    // ------------

    protected LayoutManager createDefaultLayoutInstance(
                                Container container,
                                Container containerDelegate)
    {
        return new BoxLayout(containerDelegate, BoxLayout.X_AXIS);
    }

    protected LayoutManager cloneLayoutInstance(Container container,
                                                Container containerDelegate)
    {
        return new BoxLayout(containerDelegate, axis);
    }

    // we must override this because BoxLayout is not a bean
    protected void readInitLayoutCode(CodeElement layoutElement,
                                      CodeConnectionGroup layoutCode)
    {
        CodeElement[] params = layoutElement.getOrigin().getCreationParameters();
        if (params.length == 2) {
            FormCodeSupport.readPropertyElement(
                                params[1], getProperties()[0], false);
            updateLayoutInstance();
        }
    }

    // we must override this because BoxLayout is not a bean
    protected CodeElement createInitLayoutCode(CodeConnectionGroup layoutCode) {
        CodeStructure codeStructure = getCodeStructure();

        CodeElement[] params = new CodeElement[2];
        params[0] = getLayoutContext().getContainerDelegateCodeElement();
        params[1] = codeStructure.createElement(
                        FormCodeSupport.createOrigin(getProperties()[0]));

        return codeStructure.createElement(getBoxLayoutConstructor(), params);
    }

    protected void layoutChanged() {
        super.layoutChanged();
        updateLayoutInstance();
    }

    // we must override this because BoxLayout is not a bean
    protected FormProperty[] getProperties() {
        if (properties == null) {
            // we cannot use RADProperty because "axis" is not a real
            // bean property - we must create a special FormProperty
            properties = new FormProperty[1];

            properties[0] = new FormProperty(
                                "axis", // NOI18N
                                Integer.TYPE,
                                getBundle().getString("PROP_axis"), // NOI18N
                                getBundle().getString("HINT_axis")) // NOI18N
            {
                public Object getTargetValue() {
                    return new Integer(axis);
                }

                public void setTargetValue(Object value) {
                    int ax = ((Integer)value).intValue();
                    if (ax == BoxLayout.X_AXIS || ax == BoxLayout.Y_AXIS) {
                        axis = ax;
                    }
                }

                public boolean supportsDefaultValue() {
                    return true;
                }

                public Object getDefaultValue() {
                    return new Integer(BoxLayout.X_AXIS);
                }

                public PropertyEditor getExpliciteEditor() {
                    return new BoxAxisEditor();
                }
            };
            // [!!]
//            properties[0].setPropertyContext(
//                new FormPropertyContext.DefaultImpl(getContainer().getFormModel()));
        }

        return properties;
    }

    protected Node.Property getProperty(String propName) {
        return "axis".equals(propName) ? getProperties()[0] : null; // NOI18N
    }

    private static Constructor getBoxLayoutConstructor() {
        if (boxLayoutConstructor == null) {
            try {
                boxLayoutConstructor = BoxLayout.class.getConstructor(
                                new Class[] { Container.class, Integer.TYPE });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return boxLayoutConstructor;
    }

    // --------------

    public static final class BoxAxisEditor extends PropertyEditorSupport {
        private final String[] tags = {
            getBundle().getString("VALUE_axis_x"), // NOI18N
            getBundle().getString("VALUE_axis_y")  // NOI18N
        };
        private final Integer[] values = {
            new Integer(BoxLayout.X_AXIS),
            new Integer(BoxLayout.Y_AXIS)
        };
        private final String[] javaInitStrings = {
            "javax.swing.BoxLayout.X_AXIS", // NOI18N
            "javax.swing.BoxLayout.Y_AXIS"  // NOI18N
        };

        public String[] getTags() {
            return tags;
        }

        public String getAsText() {
            Object value = getValue();
            if (values[0].equals(value)) return tags[0];
            if (values[1].equals(value)) return tags[1];
            return null;
        }

        public void setAsText(String str) {
            if (tags[0].equals(str)) setValue(values[0]);
            else if (tags[1].equals(str)) setValue(values[1]);
        }

        public String getJavaInitializationString() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return javaInitStrings[i];
            return null;
        }
    }
}
