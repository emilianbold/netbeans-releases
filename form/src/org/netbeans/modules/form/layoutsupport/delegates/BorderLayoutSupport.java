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

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * @author Tran Duc Trung, Tomas Pavek
 */

public class BorderLayoutSupport extends AbstractLayoutSupport
{
    public Class getSupportedClass() {
        return BorderLayout.class;
    }

    public LayoutConstraints getNewConstraints(Container container,
                                               Container containerDelegate,
                                               Component component,
                                               int index,
                                               Point posInCont,
                                               Point posInComp)
    {
        if (component != null && component.getParent() != containerDelegate)
            component = null;

        String primary = BorderLayout.CENTER;
        String alternateX = null;
        String alternateY = null;

        int w = containerDelegate.getSize().width;
        int h = containerDelegate.getSize().height;

        Insets contInsets = containerDelegate.getInsets();
        int marginW = getMargin(w - contInsets.left - contInsets.right);
        int marginH = getMargin(h - contInsets.top - contInsets.bottom);

        int xC = 1; // center by default (0 - left, 1 - center, 2 - right)
        int yC = 1; // center by default (0 - top, 1 - center, 2 - bottom)

        if (w > 25) {
            if (posInCont.x < contInsets.left+marginW) xC = 0; // left
            else if (posInCont.x >= w-marginW-contInsets.right) xC = 2; // right
        }
        if (h > 25) {
            if (posInCont.y < contInsets.top+marginH) yC = 0; // top
            else if (posInCont.y >= h-marginH-contInsets.bottom) yC = 2; // bottom
        }

        if (xC == 0) primary = BorderLayout.WEST;
        else if (xC == 2) primary = BorderLayout.EAST;
        else alternateX = posInCont.x - contInsets.left <
                            (w - contInsets.left - contInsets.right)/2 ?
                BorderLayout.WEST : BorderLayout.EAST;

        if (yC == 0) { // top
            alternateX = primary;
            primary = BorderLayout.NORTH;
        }
        else if (yC == 2) { // bottom
            alternateX = primary;
            primary = BorderLayout.SOUTH;
        }
        else alternateY = posInCont.y - contInsets.top <
                            (h - contInsets.top - contInsets.bottom)/2 ?
                BorderLayout.NORTH : BorderLayout.SOUTH;

        String[] suggested = new String[] { primary, alternateY, alternateX };
        String[] free = findFreePositions();

        for (int i=0; i < suggested.length; i++) {
            String str = suggested[i];
            if (str == null) continue;

            for (int j=0; j  < free.length; j++)
                if (free[j].equals(str))
                    return new BorderConstraints(str);

            if (component != null) {
                int idx = getComponentOnPosition(str);
                if (containerDelegate.getComponent(idx) == component)
                    return new BorderConstraints(str);
            }
        }
        return new BorderConstraints(free[0]);
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        String position = (String) newConstraints.getConstraintsObject();
        Component[] comps = containerDelegate.getComponents();
        int index;

        Dimension contSize = containerDelegate.getSize();
        Insets contInsets = containerDelegate.getInsets();
        Dimension compPrefSize =
            component != null ? component.getPreferredSize() : new Dimension(0,0);

        int x1, y1, x2, y2;
        int marginW = getMargin(contSize.width - contInsets.left - contInsets.right);
        int marginH = getMargin(contSize.height - contInsets.top - contInsets.bottom);

        if (BorderLayout.NORTH.equals(position)) {
            x1 = contInsets.left;
            x2 = contSize.width - contInsets.right;
            y1 = contInsets.top;
            y2 = contInsets.top + (compPrefSize.height > 0 ?
                                   compPrefSize.height : marginH);
        }
        else if (BorderLayout.SOUTH.equals(position)) {
            x1 = contInsets.left;
            x2 = contSize.width - contInsets.right;
            y1 = contSize.height - contInsets.bottom
                   - (compPrefSize.height > 0 ? compPrefSize.height : marginH);
            y2 = contSize.height - contInsets.bottom;
        }
        else { // EAST, WEST or CENTER
            if (BorderLayout.WEST.equals(position)) {
                x1 = contInsets.left;
                x2 = contInsets.left + (compPrefSize.width > 0 ?
                                        compPrefSize.width : marginW);
            }
            else if (BorderLayout.EAST.equals(position)) {
                x1 = contSize.width - contInsets.right
                       - (compPrefSize.width > 0 ? compPrefSize.width : marginW);
                x2 = contSize.width - contInsets.right;
            }
            else { // CENTER
                index = getComponentOnPosition(BorderLayout.WEST);
                x1 = contInsets.left;
                if (index >= 0)
                    x1 += comps[index].getSize().width;

                index = getComponentOnPosition(BorderLayout.EAST);
                x2 = contSize.width - contInsets.right;
                if (index >= 0)
                    x2 -= comps[index].getSize().width;
            }

            // y1 and y2 are the same for EAST, WEST and CENTER
            index = getComponentOnPosition(BorderLayout.NORTH);
            y1 = contInsets.top;
            if (index >= 0)
                y1 += comps[index].getSize().height;

            index = getComponentOnPosition(BorderLayout.SOUTH);
            y2 = contSize.height - contInsets.bottom;
            if (index >= 0)
                y2 -= comps[index].getSize().height;
        }

        if (x1 >= x2) {
            x1 = contInsets.left;
            x2 = contSize.width - contInsets.right;
            if (x1 >= x2) return true; // container is too small
        }
        if (y1 >= y2) {
            y1 = contInsets.top;
            x2 = contSize.height - contInsets.bottom;
            if (y1 >= y2) return true; // container is too small
        }

        g.drawRect(x1, y1, x2-x1-1, y2-y1-1);

        return true;
    }

/*    public LayoutConstraints fixConstraints(LayoutConstraints constraints) {
        if (!(constraints instanceof BorderConstraints))
            return new BorderConstraints(findFreePositions()[0]);

        String direction = (String) constraints.getConstraintsObject();
        String[] freePositions = findFreePositions();
        for (int i = 0; i < freePositions.length; i++) {
            if (direction.equals(freePositions[i]))
                return constraints;
        }
        return new BorderConstraints(BorderLayout.CENTER);
    } */

    // ----------

    protected LayoutConstraints readConstraintsCode(CodeExpression constrExp,
                                                    CodeGroup constrCode,
                                                    CodeExpression compExp)
    {
        BorderConstraints constr = new BorderConstraints(BorderLayout.CENTER);
        FormCodeSupport.readPropertyExpression(constrExp,
                                               constr.getProperties()[0],
                                               false);
        return constr;
    }

    protected CodeExpression createConstraintsCode(CodeGroup constrCode,
                                                   LayoutConstraints constr,
                                                   CodeExpression compExp,
                                                   int index)
    {
        if (!(constr instanceof BorderConstraints))
            return null; // should not happen

        return getCodeStructure().createExpression(
                   FormCodeSupport.createOrigin(constr.getProperties()[0]));
    }

    protected LayoutConstraints createDefaultConstraints() {
        return new BorderConstraints(findFreePositions()[0]);
    }

    // ----------------

    private String[] findFreePositions() {
        ArrayList positions = new ArrayList(6);

        if (getComponentOnPosition(BorderLayout.CENTER) == -1)
            positions.add(BorderLayout.CENTER);
        if (getComponentOnPosition(BorderLayout.NORTH) == -1)
            positions.add(BorderLayout.NORTH);
        if (getComponentOnPosition(BorderLayout.SOUTH) == -1)
            positions.add(BorderLayout.SOUTH);
        if (getComponentOnPosition(BorderLayout.EAST) == -1)
            positions.add(BorderLayout.EAST);
        if (getComponentOnPosition(BorderLayout.WEST) == -1)
            positions.add(BorderLayout.WEST);
        if (positions.size() == 0)
            positions.add(BorderLayout.CENTER);

        String[] free = new String[positions.size()];
        positions.toArray(free);
        return free;
    }

    private int getComponentOnPosition(String position) {
        java.util.List constraints = getConstraintsList();
        if (constraints == null)
            return -1;

        for (int i=0, n=constraints.size(); i < n; i++) {
            LayoutConstraints constr = (LayoutConstraints) constraints.get(i);
            if (constr != null && position.equals(constr.getConstraintsObject()))
                return i;
        }

        return -1;
    }

    private int getMargin(int size) {
        int margin = size/8;
        if (margin < 10) margin = 10;
        if (margin > 50) margin = 50;
        return margin;
    }

    // ----------------

    public static class BorderConstraints implements LayoutConstraints {
        private String direction;

        private Node.Property[] properties;

        public BorderConstraints(String direction) {
            this.direction = direction;
        }

        public Node.Property[] getProperties() {
            if (properties == null)
                properties = new FormProperty[] {
                    new FormProperty(
                            "direction", // NOI18N
                            String.class,
                            getBundle().getString("PROP_direction"), // NOI18N
                            getBundle().getString("HINT_direction")) // NOI18N
                    {
                        public Object getTargetValue() {
                            return direction;
                        }

                        public void setTargetValue(Object value) {
                            direction = (String)value;
                        }

                        public PropertyEditor getExpliciteEditor() {
                            return new BorderDirectionEditor();
                        }
                    }
                };

            return properties;
        }

        public Object getConstraintsObject() {
            return direction;
        }

        public LayoutConstraints cloneConstraints() {
            return new BorderConstraints(direction);
        }
    }

    // ---------

    static class BorderDirectionEditor extends PropertyEditorSupport {
        private final String[] values = {
            BorderLayout.CENTER,
            BorderLayout.WEST,
            BorderLayout.EAST,
            BorderLayout.NORTH,
            BorderLayout.SOUTH
        };
        private final String[] javaInitStrings = {
            "java.awt.BorderLayout.CENTER", // NOI18N
            "java.awt.BorderLayout.WEST", // NOI18N
            "java.awt.BorderLayout.EAST", // NOI18N
            "java.awt.BorderLayout.NORTH", // NOI18N
            "java.awt.BorderLayout.SOUTH" // NOI18N
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

        public String getJavaInitializationString() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return javaInitStrings[i];
            return null;
        }
    }
}
