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
import java.lang.reflect.Method;
import javax.swing.JSplitPane;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * @author Tomas Pavek
 */

public class JSplitPaneSupport extends AbstractLayoutSupport {

    private static Method setLeftComponentMethod;
    private static Method setRightComponentMethod;
    private static Method setTopComponentMethod;
    private static Method setBottomComponentMethod;

    public Class getSupportedClass() {
        return JSplitPane.class;
    }
    
    public LayoutConstraints getNewConstraints(Container container,
                                               Container containerDelegate,
                                               Component component,
                                               int index,
                                               Point posInCont,
                                               Point posInComp)
    {
        if (!(container instanceof JSplitPane))
            return null;

        JSplitPane splitPane = (JSplitPane) container;
        Dimension sz = splitPane.getSize();
        int orientation = splitPane.getOrientation();
        Component left = splitPane.getLeftComponent();
        Component right = splitPane.getRightComponent();

        String freePosition = findFreePosition();
        
        if (left == null || right == null) {
            if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
                if (posInCont.x <= sz.width / 2)
                    freePosition = JSplitPane.LEFT;
                else
                    freePosition = JSplitPane.RIGHT;
            }
            else {
                if (posInCont.x <= sz.height / 2)
                    freePosition = JSplitPane.TOP;
                else
                    freePosition = JSplitPane.BOTTOM;
            }
        }

        return new SplitConstraints(freePosition);
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(container instanceof JSplitPane))
            return false;

        String position = (String) newConstraints.getConstraintsObject();
        if (position == null)
            return false;
        
        JSplitPane splitPane = (JSplitPane) container;
        int orientation = splitPane.getOrientation();

        Dimension sz = splitPane.getSize();
        Insets insets = container.getInsets();
        sz.width -= insets.left + insets.right;
        sz.height -= insets.top + insets.bottom;

        Rectangle rect = new Rectangle(insets.left, insets.top, sz.width, sz.height);

        if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
            Component left = splitPane.getLeftComponent();
            Component right = splitPane.getRightComponent();
            
            if (position == JSplitPane.LEFT) {
                if (right == null) {
                    rect.width = sz.width / 2;
                }
                else {
                    rect.width = right.getBounds().x - rect.x;
                }
            }
            else {
                if (left == null) {
                    rect.x = insets.left + sz.width / 2;
                    rect.width = sz.width - rect.x;
                }
                else {
                    rect.x = left.getBounds().x + left.getBounds().width;
                    rect.width = sz.width - rect.x;
                }
            }
        }
        else {
            Component top = splitPane.getTopComponent();
            Component bottom = splitPane.getBottomComponent();
            
            if (position == JSplitPane.TOP) {
                if (bottom == null) {
                    rect.height /= 2;
                }
                else {
                    rect.height = bottom.getBounds().y - rect.y;
                }
            }
            else {
                if (top == null) {
                    rect.y = insets.top + sz.height / 2;
                    rect.height = sz.height - rect.y;
                }
                else {
                    rect.y = top.getBounds().y + top.getBounds().height;
                    rect.height = sz.height - rect.y;
                }
            }
        }
        g.drawRect(rect.x, rect.y, rect.width, rect.height);
        return true;
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        if (!(container instanceof JSplitPane))
            return;

        for (int i=0; i < components.length; i++) {
            int descPos = convertPosition(getConstraints(i + index));
            if (descPos == 0)
                ((JSplitPane)container).setLeftComponent(components[i]);
            else if (descPos == 1)
                ((JSplitPane)container).setRightComponent(components[i]);
        }
    }

    public boolean removeComponentFromContainer(Container container,
                                                Container containerDelegate,
                                                Component component,
                                                int index)
    {
        return false;
    }

    // ------

    protected CodeExpression readComponentCode(CodeStatement statement,
                                               CodeGroup componentCode)
    {
        CodeExpression[] params = statement.getStatementParameters();
        if (params.length != 1)
            return null;

        String position;

        Object connectingObject = statement.getMetaObject();
        if (getSetLeftComponentMethod().equals(connectingObject))
            position = JSplitPane.LEFT;
        else if (getSetRightComponentMethod().equals(connectingObject))
            position = JSplitPane.RIGHT;
        else if (getSetTopComponentMethod().equals(connectingObject))
            position = JSplitPane.TOP;
        else if (getSetBottomComponentMethod().equals(connectingObject))
            position = JSplitPane.BOTTOM;
        else return null;

        SplitConstraints constr = new SplitConstraints(position);
        getConstraintsList().add(constr);

        componentCode.addStatement(statement);

        return params[0];
    }

    protected void createComponentCode(CodeGroup componentCode,
                                       CodeExpression componentExpression,
                                       int index)
    {
        LayoutConstraints constr = getConstraints(index);
        if (!(constr instanceof SplitConstraints))
            return; // should not happen

        ((SplitConstraints)constr).createComponentCode(
                               componentCode,
                               getLayoutContext().getContainerCodeExpression(),
                               componentExpression);
    }

    protected LayoutConstraints createDefaultConstraints() {
        return new SplitConstraints(findFreePosition());
    }

    // ------------

    private int convertPosition(LayoutConstraints desc) {
        Object position = desc.getConstraintsObject();
        if (JSplitPane.LEFT.equals(position) || JSplitPane.TOP.equals(position))
            return 0;
        if (JSplitPane.RIGHT.equals(position) || JSplitPane.BOTTOM.equals(position))
            return 1;
        return -1;
    }

    private String findFreePosition() {
        int leftTop = 0, rightBottom = 0;
        int orientation = JSplitPane.HORIZONTAL_SPLIT;

        for (int i=0, n=getComponentCount(); i < n; i++) {
            LayoutConstraints constraints = getConstraints(i);
            if (!(constraints instanceof SplitConstraints))
                continue;

            int constrPos = convertPosition(constraints);
            if (constrPos == 0)
                leftTop++;
            else if (constrPos == 1)
                rightBottom++;
        }

        if (leftTop == 0 || leftTop < rightBottom)
            return orientation == JSplitPane.HORIZONTAL_SPLIT ?
                JSplitPane.LEFT : JSplitPane.TOP;
        else
            return orientation == JSplitPane.HORIZONTAL_SPLIT ?
                JSplitPane.RIGHT : JSplitPane.BOTTOM;
    }

    // --------

    private static Method getSetLeftComponentMethod() {
        if (setLeftComponentMethod == null)
            setLeftComponentMethod = getAddMethod("setLeftComponent"); // NOI18N
        return setLeftComponentMethod;
    }

    private static Method getSetRightComponentMethod() {
        if (setRightComponentMethod == null)
            setRightComponentMethod = getAddMethod("setRightComponent"); // NOI18N
        return setRightComponentMethod;
    }

    private static Method getSetTopComponentMethod() {
        if (setTopComponentMethod == null)
            setTopComponentMethod = getAddMethod("setTopComponent"); // NOI18N
        return setTopComponentMethod;
    }

    private static Method getSetBottomComponentMethod() {
        if (setBottomComponentMethod == null)
            setBottomComponentMethod = getAddMethod("setBottomComponent"); // NOI18N
        return setBottomComponentMethod;
    }

    private static Method getAddMethod(String name) {
        try {
            return JSplitPane.class.getMethod(name,
                                              new Class[] { Component.class });
        }
        catch (NoSuchMethodException ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    // -----------

    public static class SplitConstraints implements LayoutConstraints {
        private String position;

        private Node.Property[] properties;

        private CodeExpression containerExpression;
        private CodeExpression componentExpression;
        private CodeGroup componentCode;

        public SplitConstraints(String position) {
            this.position = position;
        }

        public Node.Property[] getProperties() {
            if (properties == null)
                properties = new Node.Property[] {
                    new FormProperty(
                            "splitPosition", // NOI18N
                            String.class,
                            getBundle().getString("PROP_splitPos"), // NOI18N
                            getBundle().getString("HINT_splitPos")) // NOI18N
                    {
                        public Object getTargetValue() {
                            return position;
                        }
                        public void setTargetValue(Object value) {
                            position = (String)value;
                        }
                        public PropertyEditor getExpliciteEditor() {
                            return new SplitPositionEditor();
                        }
                        protected void propertyValueChanged(Object old,
                                                            Object current) {
                            if (isChangeFiring())
                                updateCode();
                            super.propertyValueChanged(old, current);
                        }
                    }
                };

            return properties;
        }

        public Object getConstraintsObject() {
            return position;
        }

        public LayoutConstraints cloneConstraints() {
            return new SplitConstraints(position);
        }

        private void createComponentCode(CodeGroup compCode,
                                         CodeExpression contExp,
                                         CodeExpression compExp)
        {
            componentCode = compCode;
            containerExpression = contExp;
            componentExpression = compExp;
            updateCode();
        }

        private void updateCode() {
            if (componentCode == null)
                return;

            CodeStructure.removeStatements(
                componentCode.getStatementsIterator());
            componentCode.removeAll();

            Method addMethod;
            if (JSplitPane.LEFT.equals(position))
                addMethod = getSetLeftComponentMethod();
            else if (JSplitPane.RIGHT.equals(position))
                addMethod = getSetRightComponentMethod();
            else if (JSplitPane.TOP.equals(position))
                addMethod = getSetTopComponentMethod();
            else if (JSplitPane.BOTTOM.equals(position))
                addMethod = getSetBottomComponentMethod();
            else return;

            componentCode.addStatement(
                    CodeStructure.createStatement(
                           containerExpression,
                           addMethod,
                           new CodeExpression[] { componentExpression }));
        }
    }

    static class SplitPositionEditor extends PropertyEditorSupport {
        private final String[] values = {
            JSplitPane.LEFT,
            JSplitPane.RIGHT,
            JSplitPane.TOP,
            JSplitPane.BOTTOM
        };

        public String[] getTags() {
            return values;
        }

        public String getAsText() {
            return (String)getValue();
        }

        public void setAsText(String str) {
            for (int i = 0; i < values.length; i++)
                if (str.equals(values[i])) {
                    setValue(str);
                    break;
                }
        }
    }
}
