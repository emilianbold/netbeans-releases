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
import java.lang.reflect.Method;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.*;

/**
 * @author Tomas Pavek
 */

public class JTabbedPaneSupport extends AbstractLayoutSupport
                                implements LayoutSupportArranging
{
    private int selectedTab = -1;

    private static Method addTabMethod1;
    private static Method addTabMethod2;
    private static Method addTabMethod3;

    public Class getSupportedClass() {
        return JTabbedPane.class;
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(container instanceof JTabbedPane))
            return -1;
        return container.getComponentCount();
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(container instanceof JTabbedPane))
            return false;

        if (container.getComponentCount() == 0) {
            Dimension sz = container.getSize();
            Insets insets = container.getInsets();
            sz.width -= insets.left + insets.right;
            sz.height -= insets.top + insets.bottom;
            g.drawRect(0, 0, sz.width, sz.height);
        }
        else {
            Rectangle rect = container.getComponent(0).getBounds();
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
        }
        return true;
    }
    
    public void removeComponent(int index) {
        super.removeComponent(index);
        if (selectedTab >= getComponentCount())
            selectedTab = getComponentCount() - 1;
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        if (!(container instanceof JTabbedPane))
            return;

        for (int i=0; i < components.length; i++) {
            LayoutConstraints constraints = getConstraints(i + index);
            if (constraints instanceof TabConstraints) {
                JTabbedPane tabbedPane = (JTabbedPane) container;
                try {
                    FormProperty titleProperty = (FormProperty)
                                                 constraints.getProperties()[0];
                    FormProperty iconProperty = (FormProperty)
                                                constraints.getProperties()[1];
                    FormProperty tooltipProperty = (FormProperty)
                                                   constraints.getProperties()[2];

                    tabbedPane.addTab((String) titleProperty.getRealValue(),
                                      (Icon) iconProperty.getRealValue(),
                                      components[i],
                                      (String) tooltipProperty.getRealValue());
                }
                catch (Exception ex) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        ex.printStackTrace();
                }
            }
        }
    }

    protected CodeExpression readComponentCode(CodeStatement statement,
                                               CodeGroup componentCode)
    {
        CodeExpression compExp;
        int[] constrPropsIndices;
        CodeExpression[] params = statement.getStatementParameters();

        Object connectingObject = statement.getMetaObject();
        if (getAddTabMethod1().equals(connectingObject)) {
            compExp = params[2];
            constrPropsIndices = new int[] { 0, 1, 2 }; // tab, icon, tooltip
        }
        else if (getAddTabMethod2().equals(connectingObject)) {
            compExp = params[2];
            constrPropsIndices = new int[] { 0, 1 }; // tab, icon
        }
        else if (getAddTabMethod3().equals(connectingObject)) {
            compExp = params[1];
            constrPropsIndices = new int[] { 0 }; // tab
        }
        else return null;

        TabConstraints constr = new TabConstraints("tab"); // NOI18N
        Node.Property[] props = constr.getProperties();
        for (int i=0; i < params.length; i++) {
            if (params[i] != compExp)
                FormCodeSupport.readPropertyExpression(
                                    params[i],
                                    props[constrPropsIndices[i]],
                                    false);
        }
        getConstraintsList().add(constr);

        componentCode.addStatement(statement);

        return compExp;
    }

    protected void createComponentCode(CodeGroup componentCode,
                                       CodeExpression componentExpression,
                                       int index)
    {
        LayoutConstraints constr = getConstraints(index);
        if (!(constr instanceof TabConstraints))
            return; // should not happen

        ((TabConstraints)constr).createComponentCode(
                           componentCode,
                           getLayoutContext().getContainerCodeExpression(),
                           componentExpression);
    }

    protected LayoutConstraints createDefaultConstraints() {
        return new TabConstraints("tab"+(getComponentCount())); // NOI18N
    }

    // tab, icon, component, tooltip
    private static Method getAddTabMethod1() {
        if (addTabMethod1 == null) {
            try {
                addTabMethod1 = JTabbedPane.class.getMethod(
                                "addTab", // NOI18N
                                new Class[] { String.class, Icon.class,
                                      Component.class, String.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return addTabMethod1;
    }

    // tab, icon, component
    private static Method getAddTabMethod2() {
        if (addTabMethod2 == null) {
            try {
                addTabMethod2 = JTabbedPane.class.getMethod(
                                "addTab", // NOI18N
                                new Class[] { String.class, Icon.class,
                                              Component.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return addTabMethod2;
    }

    // tab, component
    private static Method getAddTabMethod3() {
        if (addTabMethod3 == null) {
            try {
                addTabMethod3 = JTabbedPane.class.getMethod(
                                "addTab", // NOI18N
                                new Class[] { String.class, Component.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return addTabMethod3;
    }

    // ----------
    // LayoutSupportArranging

    public void processMouseClick(Point p, Container cont) {
        if (!(cont instanceof JTabbedPane))
            return;

        JTabbedPane tabbedPane = (JTabbedPane)cont;
        int n = tabbedPane.getTabCount();
        for (int i=0; i < n; i++) {
            if (tabbedPane.getBoundsAt(i).contains(p)) {
                selectedTab = i;
                tabbedPane.setSelectedIndex(i);
                break;
            }
        }
    }

    public void selectComponent(int index) {
        selectedTab = index;
    }

    public void arrangeContainer(Container container,
                                 Container containerDelegate)
    {
        if (!(container instanceof JTabbedPane) || selectedTab < 0)
            return;

        ((JTabbedPane)container).setSelectedIndex(selectedTab);
    }

    // ----------

    public static class TabConstraints implements LayoutConstraints {
        private String title;
        private Icon icon;
        private String toolTip;

        private FormProperty[] properties;

        private CodeExpression containerExpression;
        private CodeExpression componentExpression;
        private CodeGroup componentCode;
        private CodeExpression[] propertyExpressions;

        public TabConstraints(String title) {
            this.title = title;
        }

        public TabConstraints(String title, Icon icon, String toolTip) {
            this.title = title;
            this.icon = icon;
            this.toolTip = toolTip;
        }

        public String getTitle() { 
            return title;
        }

        public Icon getIcon() {
            return icon;
        }

        public String getToolTip() {
            return toolTip;
        }

        // -----------

        public Node.Property[] getProperties() {
            if (properties == null) {
                properties = new FormProperty[] {
                    new FormProperty("tabTitle", // NOI18N
                                     String.class,
                                 getBundle().getString("PROP_tabTitle"), // NOI18N
                                 getBundle().getString("HINT_tabTitle")) { // NOI18N

                        public Object getTargetValue() {
                            return title;
                        }

                        public void setTargetValue(Object value) {
                            title = (String)value;
                        }

                        protected Object getRealValue(Object value) {
                            Object realValue = super.getRealValue(value);
                            if (realValue == FormDesignValue.IGNORED_VALUE)
                                realValue = ((FormDesignValue)value).getDescription();
                            return realValue;
                        }
                    },

                    new FormProperty("tabIcon", // NOI18N
                                     Icon.class,
                                 getBundle().getString("PROP_tabIcon"), // NOI18N
                                 getBundle().getString("HINT_tabIcon")) { // NOI18N

                        public Object getTargetValue() {
                            return icon;
                        }

                        public void setTargetValue(Object value) {
                            icon = (Icon)value;
                        }

                        public boolean supportsDefaultValue() {
                            return true;
                        }

                        public Object getDefaultValue() {
                            return null;
                        }
                    },

                    new FormProperty("tabToolTip", // NOI18N
                                     String.class,
                                 getBundle().getString("PROP_tabToolTip"), // NOI18N
                                 getBundle().getString("HINT_tabToolTip")) { // NOI18N

                        public Object getTargetValue() {
                            return toolTip;
                        }

                        public void setTargetValue(Object value) {
                            toolTip = (String)value;
                        }

                        public boolean supportsDefaultValue() {
                            return true;
                        }

                        public Object getDefaultValue() {
                            return null;
                        }
                    }
                };

                properties[0].setChanged(true);
            }

            return properties;
        }

        public Object getConstraintsObject() {
            return title;
        }

        public LayoutConstraints cloneConstraints() {
            LayoutConstraints constr = new TabConstraints(title);
            org.netbeans.modules.form.FormUtils.copyProperties(
                getProperties(), constr.getProperties(), true, false);
            return constr;
        }

        // --------

        private void createComponentCode(CodeGroup compCode,
                                         CodeExpression contExp,
                                         CodeExpression compExp)
        {
            this.componentCode = compCode;
            this.containerExpression = contExp;
            this.componentExpression = compExp;
            this.propertyExpressions = null;
            updateCode();
        }

        private void updateCode() {
            if (componentCode == null)
                return;

            CodeStructure.removeStatements(
                componentCode.getStatementsIterator());
            componentCode.removeAll();

            getProperties();

            Method addTabMethod;
            CodeExpression[] params;

            if (properties[2].isChanged()) {
                addTabMethod = getAddTabMethod1();
                params = new CodeExpression[] { getPropertyExpression(0), // tab
                                                getPropertyExpression(1), // icon
                                                componentExpression,
                                                getPropertyExpression(2) }; // tooltip
            }
            else if (properties[1].isChanged()) {
                addTabMethod = getAddTabMethod2();
                params = new CodeExpression[] { getPropertyExpression(0), // tab
                                                getPropertyExpression(1), // icon
                                                componentExpression };
            }
            else { // tab
                addTabMethod = getAddTabMethod3();
                params = new CodeExpression[] { getPropertyExpression(0), // tab
                                                componentExpression };
            }

            CodeStatement addTabStatement = CodeStructure.createStatement(
                                                          containerExpression,
                                                          addTabMethod,
                                                          params);
            componentCode.addStatement(addTabStatement);
        }

        private CodeExpression getPropertyExpression(int index) {
            if (propertyExpressions == null) {
                propertyExpressions = new CodeExpression[properties.length];
                for (int i=0; i < properties.length; i++) {
                    propertyExpressions[i] =
                        componentExpression.getCodeStructure().createExpression(
                            FormCodeSupport.createOrigin(properties[i]));
                }
            }
            return propertyExpressions[index];
        }
    }
}
