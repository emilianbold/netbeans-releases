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
import java.lang.reflect.Constructor;

import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.SharedClassObject;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

import org.netbeans.lib.awtextra.AbsoluteLayout;
import org.netbeans.lib.awtextra.AbsoluteConstraints;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.FormLoaderSettings;

/**
 * @author Tomas Pavek
 */

public class AbsoluteLayoutSupport extends AbstractLayoutSupport {

    private static Image layoutIcon;
    private static Image layoutIcon32;

    private static Constructor constrConstructor;

    private static FormLoaderSettings formSettings = (FormLoaderSettings)
                   SharedClassObject.findObject(FormLoaderSettings.class, true);


    public Class getSupportedClass() {
        return AbsoluteLayout.class;
    }

    public Image getIcon(int type) {
        switch (type) {
            case BeanInfo.ICON_COLOR_16x16:
            case BeanInfo.ICON_MONO_16x16:
                if (layoutIcon == null)
                    layoutIcon = Utilities.loadImage(
                        "org/netbeans/modules/form/layoutsupport/resources/AbsoluteLayout.gif"); // NOI18N
                return layoutIcon;

            default:
                if (layoutIcon32 == null)
                    layoutIcon32 = Utilities.loadImage(
                        "org/netbeans/modules/form/layoutsupport/resources/AbsoluteLayout32.gif"); // NOI18N
                return layoutIcon32;
        }
    }

    public LayoutConstraints getNewConstraints(Container container,
                                               Container containerDelegate,
                                               Component component,
                                               int index,
                                               Point posInCont,
                                               Point posInComp)
    {
        int x = posInCont.x;
        int y = posInCont.y;
        int w = -1;
        int h = -1;

        LayoutConstraints constr = getConstraints(index);

        if (component != null) {
            int currentW;
            int currentH;

            if (constr instanceof AbsoluteLayoutConstraints) {
                currentW = ((AbsoluteLayoutConstraints)constr).w;
                currentH = ((AbsoluteLayoutConstraints)constr).h;
            }
            else {
                currentW = -1;
                currentH = -1;
            }

            Dimension size = component.getSize();
            Dimension prefSize = component.getPreferredSize();

            w = computeConstraintSize(size.width, currentW, prefSize.width);
            h = computeConstraintSize(size.height, currentH, prefSize.height);
        }

        if (posInComp != null) {
            x -= posInComp.x;
            y -= posInComp.y;
        }

        if (formSettings.getApplyGridToPosition()) {
            x = computeGridSize(x, formSettings.getGridX());
            y = computeGridSize(y, formSettings.getGridY());
        }

        return createNewConstraints(constr, x, y, w, h);
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        Rectangle r = ((AbsoluteLayoutConstraints)newConstraints).getBounds();
        int w = r.width;
        int h = r.height;

        if (w == -1 || h == -1) {
            // JInternalFrame.getPreferredSize() behaves suspiciously
            Dimension pref = component instanceof javax.swing.JInternalFrame ?
                             component.getSize() : component.getPreferredSize();
            if (w == -1) w = pref.width;
            if (h == -1) h = pref.height;
        }

        if (w < 4) w = 4;
        if (h < 4) h = 4;

        g.drawRect(r.x, r.y, w, h);

        return true;
    }

    public int getResizableDirections(Component component, int index) {
        return LayoutSupportContext.RESIZE_UP
               | LayoutSupportContext.RESIZE_DOWN
               | LayoutSupportContext.RESIZE_LEFT
               | LayoutSupportContext.RESIZE_RIGHT;
    }

    public LayoutConstraints getResizedConstraints(Component component,
                                                   int index,
                                                   Insets sizeChanges)
    {
        int x, y, w, h;
        Rectangle bounds = component.getBounds();
        x = bounds.x;
        y = bounds.y;
        w = bounds.width;
        h = bounds.height;

        Dimension prefSize = component.getPreferredSize();
        int currentW, currentH;

        LayoutConstraints constr = getConstraints(index);
        if (constr instanceof AbsoluteLayoutConstraints) {
            Rectangle r = ((AbsoluteLayoutConstraints)constr).getBounds();
            currentW = r.width;
            currentH = r.height;
        }
        else {
            currentW = computeConstraintSize(w, -1, prefSize.width);
            currentH = computeConstraintSize(h, -1, prefSize.height);
        }

        int x2 = x + w;
        int y2 = y + h;

        if (sizeChanges.left + sizeChanges.right == 0)
            w = currentW; // no change
        else { // compute resized width and x coordinate
            w += sizeChanges.left + sizeChanges.right;
            w = w <= 0 ? -1 : computeConstraintSize(w, currentW, prefSize.width);

            if (w > 0) {
                if (formSettings.getApplyGridToSize()) {
                    int gridW = computeGridSize(w, formSettings.getGridX());
                    x -= sizeChanges.left +
                         (gridW - w) * sizeChanges.left
                         / (sizeChanges.left + sizeChanges.right);
                    w = gridW;
                }
            }
            else if (sizeChanges.left != 0)
                x = x2 - prefSize.width;
        }

        if (sizeChanges.top + sizeChanges.bottom == 0)
            h = currentH; // no change
        else { // compute resized height and y coordinate
            h += sizeChanges.top + sizeChanges.bottom;
            h = h <= 0 ? -1 : computeConstraintSize(h, currentH, prefSize.height);

            if (h > 0) {
                if (formSettings.getApplyGridToSize()) {
                    int gridH = computeGridSize(h, formSettings.getGridY());
                    y -= sizeChanges.top +
                         (gridH - h) * sizeChanges.top
                         / (sizeChanges.top + sizeChanges.bottom);
                    h = gridH;
                }
            }
            else if (sizeChanges.top != 0)
                y = y2 - prefSize.height;
        }

        return createNewConstraints(constr, x, y, w, h);
    }

    public void convertConstraints(LayoutConstraints[] previousConstraints,
                                   LayoutConstraints[] currentConstraints,
                                   Component[] components)
    {
        if (currentConstraints == null || components == null)
            return;

        for (int i=0; i < currentConstraints.length; i++)
            if (currentConstraints[i] == null) {
                Rectangle bounds = components[i].getBounds();
                Dimension prefSize = components[i].getPreferredSize();
                int x = bounds.x;
                int y = bounds.y;
                int w = computeConstraintSize(bounds.width, -1, prefSize.width);
                int h = computeConstraintSize(bounds.height, -1, prefSize.height);

                currentConstraints[i] = new AbsoluteLayoutConstraints(x, y, w, h);
            }
    }

    // -------

    protected LayoutConstraints readConstraintsCode(
                                    CodeElement constrElement,
                                    CodeConnectionGroup constrCode,
                                    CodeElement compElement)
    {
        AbsoluteLayoutConstraints constr =
            new AbsoluteLayoutConstraints(0, 0, -1, -1);

        CodeElement[] params = constrElement.getOrigin().getCreationParameters();
        if (params.length == 4) {
            constr.readPropertyElements(params, 0);
        }

        return constr;
    }

    protected CodeElement createConstraintsCode(CodeConnectionGroup constrCode,
                                                LayoutConstraints constr,
                                                CodeElement compElement,
                                                int index)
    {
        if (!(constr instanceof AbsoluteLayoutConstraints))
            return null;

        CodeStructure codeStructure = getCodeStructure();
        AbsoluteLayoutConstraints absConstr = (AbsoluteLayoutConstraints) constr;
        return codeStructure.createElement(
                   getConstraintsConstructor(),
                   absConstr.createPropertyElements(codeStructure, 0));
    }

    protected LayoutConstraints createDefaultConstraints() {
        return new AbsoluteLayoutConstraints(0, 0, -1, -1);
    }

    // --------

    protected LayoutConstraints createNewConstraints(
                                    LayoutConstraints currentConstr,
                                    int x, int y, int w, int h)
    {
        return new AbsoluteLayoutConstraints(x, y, w, h);
    }

    private static int computeConstraintSize(int newSize,
                                             int currSize,
                                             int prefSize) {
        return newSize != -1 && (newSize != prefSize
                                 || (currSize != -1 && currSize == prefSize)) ?
               newSize : -1;
    }

    private static int computeGridSize(int size, int step) {
        if (step <= 0) return size;
        int mod = size % step;
        return mod >= step/2 ? size + step - mod : size - mod;
    }

    private static Constructor getConstraintsConstructor() {
        if (constrConstructor == null) {
            try {
                constrConstructor = AbsoluteConstraints.class.getConstructor(
                                    new Class[] { Integer.TYPE, Integer.TYPE,
                                                  Integer.TYPE, Integer.TYPE });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return constrConstructor;
    }

    // -------------

    public static class AbsoluteLayoutConstraints implements LayoutConstraints {
        int x, y, w, h; // position and size

        private Node.Property[] properties;
        boolean nullMode;
        Component refComponent;

        public AbsoluteLayoutConstraints(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public Node.Property[] getProperties() {
            if (properties == null) {
                properties = createProperties();
                reinstateProperties();
            }
            return properties;
        }

        public Object getConstraintsObject() {
            return new AbsoluteConstraints(x, y, w, h);
        }

        public LayoutConstraints cloneConstraints() {
            return new AbsoluteLayoutConstraints(x, y, w, h);
        }

        // --------

        public Rectangle getBounds() {
            return new Rectangle(x, y, w, h);
        }

        protected Node.Property[] createProperties() {
            return new Node.Property[] {
                new FormProperty("posx", // NOI18N
                                 Integer.TYPE,
                             getBundle().getString("PROP_posx"), // NOI18N
                             getBundle().getString("HINT_posx")) { // NOI18N

                    public Object getTargetValue() {
                        return new Integer(x);
                    }
                    public void setTargetValue(Object value) {
                        x = ((Integer)value).intValue();
                    }
                },

                new FormProperty("posy", // NOI18N
                                 Integer.TYPE,
                             getBundle().getString("PROP_posy"), // NOI18N
                             getBundle().getString("HINT_posy")) { // NOI18N

                    public Object getTargetValue() {
                        return new Integer(y);
                    }
                    public void setTargetValue(Object value) {
                        y = ((Integer)value).intValue();
                    }
                },

                new FormProperty("width", // NOI18N
                                 Integer.TYPE,
                             getBundle().getString("PROP_width"), // NOI18N
                             getBundle().getString("HINT_width")) { // NOI18N

                    public Object getTargetValue() {
                        return new Integer(w);
                    }
                    public void setTargetValue(Object value) {
                        w = ((Integer)value).intValue();
                    }
                    public boolean supportsDefaultValue () {
                        return true;
                    }
                    public Object getDefaultValue() {
                        return new Integer(-1);
                    }
                    public PropertyEditor getExpliciteEditor() {
                        return new SizeEditor();
                    }
                    public String getJavaInitializationString() {
                        if (nullMode && refComponent != null && !isChanged())
                            return Integer.toString(
                                     refComponent.getPreferredSize().width);
                        return super.getJavaInitializationString();
                    }
                },

                new FormProperty("height", // NOI18N
                                 Integer.TYPE,
                             getBundle().getString("PROP_height"), // NOI18N
                             getBundle().getString("HINT_height")) { // NOI18N

                    public Object getTargetValue() {
                        return new Integer(h);
                    }
                    public void setTargetValue(Object value) {
                        h = ((Integer)value).intValue();
                    }
                    public boolean supportsDefaultValue () {
                        return true;
                    }
                    public Object getDefaultValue() {
                        return new Integer(-1);
                    }
                    public PropertyEditor getExpliciteEditor() {
                        return new SizeEditor();
                    }
                    public String getJavaInitializationString() {
                        if (nullMode && refComponent != null && !isChanged())
                            return Integer.toString(
                                     refComponent.getPreferredSize().height);
                        return super.getJavaInitializationString();
                    }
                }
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
            catch(java.lang.reflect.InvocationTargetException e2) {} // should not happen
        }

        protected final CodeElement[] createPropertyElements(
                                          CodeStructure codeStructure,
                                          int shift)
        {
            getProperties();
            CodeElement xEl = codeStructure.createElement(
                        FormCodeSupport.createOrigin(properties[shift++]));
            CodeElement yEl = codeStructure.createElement(
                        FormCodeSupport.createOrigin(properties[shift++]));
            CodeElement wEl = codeStructure.createElement(
                        FormCodeSupport.createOrigin(properties[shift++]));
            CodeElement hEl = codeStructure.createElement(
                        FormCodeSupport.createOrigin(properties[shift++]));
            return new CodeElement[] { xEl, yEl, wEl, hEl };
        }

        protected final void readPropertyElements(CodeElement[] elements,
                                                  int shift)
        {
            getProperties();
            for (int i=0; i < elements.length; i++)
                FormCodeSupport.readPropertyElement(elements[i],
                                                    properties[i+shift],
                                                    false);
        }
    }

    // -----------

    public static final class SizeEditor extends PropertyEditorSupport
                                         implements EnhancedPropertyEditor {
        final Integer prefValue = new Integer(-1);
        final String prefTag = getBundle().getString("VALUE_preferred"); // NOI18N

        public String[] getTags() {
            return new String[] { prefTag };
        }

        public String getAsText() {
            Object value = getValue();
            return prefValue.equals(value) ?
                     prefTag : value.toString();
        }

        public void setAsText(String str) {
            if (prefTag.equals(str))
                setValue(prefValue);
            else
                try {
                    setValue(new Integer(Integer.parseInt(str)));
                } 
                catch (NumberFormatException e) {} // ignore
        }

        public String getJavaInitializationString() {
            Object value = getValue();
            return value != null ? value.toString() : null;
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
