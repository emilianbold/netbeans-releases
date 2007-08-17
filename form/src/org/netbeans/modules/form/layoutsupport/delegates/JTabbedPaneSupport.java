/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * Dedicated layout support class for JTabbedPane.
 *
 * @author Tomas Pavek
 */

public class JTabbedPaneSupport extends AbstractLayoutSupport {

    private int selectedTab = -1;

    private static Method addTabMethod1;
    private static Method addTabMethod2;
    private static Method addTabMethod3;

    /** Gets the supported layout manager class - JTabbedPane.
     * @return the class supported by this delegate
     */
    public Class getSupportedClass() {
        return JTabbedPane.class;
    }

    /** Removes one component from the layout (at metadata level).
     * The code structures describing the layout is updated immediately.
     * @param index index of the component in the layout
     */
    public void removeComponent(int index) {
        super.removeComponent(index);
        if (selectedTab >= getComponentCount())
            selectedTab = getComponentCount() - 1;
    }

    /** This method is called when user clicks on the container in form
     * designer. For JTabbedPane, we it switch the selected TAB.
     * @param p Point of click in the container
     * @param real instance of the container when the click occurred
     * @param containerDelegate effective container delegate of the container
     */
    public void processMouseClick(Point p,
                                  Container container,
                                  Container containerDelegate)
    {
        if (!(container instanceof JTabbedPane))
            return;

        JTabbedPane tabbedPane = (JTabbedPane)container;
        int n = tabbedPane.getTabCount();
        for (int i=0; i < n; i++) {
            if (tabbedPane.getBoundsAt(i).contains(p)) {
                selectedTab = i;
                tabbedPane.setSelectedIndex(i);
                break;
            }
        }
    }

    /** This method is called when a component is selected in Component
     * Inspector.
     * @param index position (index) of the selected component in container
     */
    public void selectComponent(int index) {
        selectedTab = index; // remember as selected tab
    }

    /** In this method, the layout delegate has a chance to "arrange" real
     * container instance additionally - some other way that cannot be
     * done through layout properties and added components.
     * @param container instance of a real container to be arranged
     * @param containerDelegate effective container delegate of the container
     */
    public void arrangeContainer(Container container,
                                 Container containerDelegate)
    {
        if (!(container instanceof JTabbedPane))
            return;

        JTabbedPane tabbedPane = (JTabbedPane) container;
        if (selectedTab >= 0) {
            if (tabbedPane.getTabCount() > selectedTab) {
                // select the tab
                tabbedPane.setSelectedIndex(selectedTab);

                // workaround for JTabbedPane bug 4190719
                Component comp = tabbedPane.getSelectedComponent();
                if (comp != null)
                    comp.setVisible(true);
                tabbedPane.repaint();
            }
        }
        else if (tabbedPane.getTabCount() > 0) {
            // workaround for JTabbedPane bug 4190719
            tabbedPane.getComponentAt(0).setVisible(true);
        }
    }

    /** This method should calculate position (index) for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     * @param component the real component being dragged; not needed here
     * @param index position (index) of the component in its current container;
     *        not needed here
     * @param posInCont position of mouse in the container delegate; not needed
     * @param posInComp position of mouse in the dragged component; not needed
     * @return index corresponding to the position of the component in the
     *         container
     */
    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(container instanceof JTabbedPane))
            return -1;
        return ((JTabbedPane)container).getTabCount();
    }

    public String getAssistantContext() {
        return "tabbedPaneLayout"; // NOI18N
    }

    /** This method paints a dragging feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     * @param component the real component being dragged; not needed here
     * @param newConstraints component layout constraints to be presented;
     *        not used for JTabbedPane
     * @param newIndex component's index position to be presented; not needed
     * @param g Graphics object for painting (with color and line style set)
     * @return whether any feedback was painted (true in this case)
     */
    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(container instanceof JTabbedPane))
            return false;

        JTabbedPane tabbedPane = (JTabbedPane) container;
        if ((tabbedPane.getTabCount() == 0) || (component == tabbedPane.getComponentAt(0))) {
            Dimension sz = container.getSize();
            Insets insets = container.getInsets();
            sz.width -= insets.left + insets.right;
            sz.height -= insets.top + insets.bottom;
            g.drawRect(0, 0, sz.width, sz.height);
        }
        else {
            Rectangle rect = tabbedPane.getComponentAt(0).getBounds();
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
        }
        return true;
    }
    
    /** Adds real components to given container (according to layout
     * constraints stored for the components).
     * @param container instance of a real container to be added to
     * @param containerDelegate effective container delegate of the container
     * @param components components to be added
     * @param index position at which to add the components to container
     */
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
                    Object title =
                        ((FormProperty)constraints.getProperties()[0])
                            .getRealValue();
                    Object icon =
                        ((FormProperty)constraints.getProperties()[1])
                            .getRealValue();
                    Object tooltip =
                        ((FormProperty)constraints.getProperties()[2])
                            .getRealValue();

                    tabbedPane.addTab(
                        title instanceof String ? (String) title : null,
                        icon instanceof Icon ? (Icon) icon : null,
                        components[i],
                        tooltip instanceof String ? (String) tooltip : null);
                }
                catch (Exception ex) {
                    org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }

    // ---------

    /** This method is used for scanning code structures and recognizing
     * components added to containers and their constraints. It's called from
     * initialize method. When a relevant code statement is found, then the
     * CodeExpression of component is get and added to component, and also the
     * layout constraints information is read.
     * @param statement CodeStatement to be tested if it contains relevant code
     * @param componentCode CodeGroup to be filled with all component code
     * @return CodeExpression representing found component; null if the
     *         statement is not relevant
     */
    protected CodeExpression readComponentCode(CodeStatement statement,
                                               CodeGroup componentCode)
    {
        CodeExpression compExp;
        int[] constrPropsIndices;
        CodeExpression[] params = statement.getStatementParameters();

        Object connectingObject = statement.getMetaObject();
        if (getAddTabMethod1().equals(connectingObject)) {
            compExp = params[2];
            constrPropsIndices = new int[] { 0, 1, -1, 2 }; // tab, icon, tooltip
        }
        else if (getAddTabMethod2().equals(connectingObject)) {
            compExp = params[2];
            constrPropsIndices = new int[] { 0, 1, -1 }; // tab, icon
        }
        else if (getAddTabMethod3().equals(connectingObject)) {
            compExp = params[1];
            constrPropsIndices = new int[] { 0, -1 }; // tab
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

    /** Creates code for a component added to the layout (opposite to
     * readComponentCode method).
     * @param componentCode CodeGroup to be filled with complete component code
     *        (code for initializing the layout constraints and adding the
     *        component to the layout)
     * @param compExp CodeExpression object representing component
     * @param index position of the component in the layout
     */
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

    /** This method is called to get a default component layout constraints
     * metaobject in case it is not provided (e.g. in addComponents method).
     * @return the default LayoutConstraints object for the supported layout;
     *         null if no component constraints are used
     */
    protected LayoutConstraints createDefaultConstraints() {
        return new TabConstraints("tab"+(getComponentCount())); // NOI18N
    }

    // ----------

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

    /** LayoutConstraints implementation for managing JTabbedPane tab
     * parameters.
     */
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
                    new FormProperty("TabConstraints.tabTitle", // NOI18N
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

                        protected void propertyValueChanged(Object old, Object current) {
                            if (isChangeFiring())
                                updateCode();
                            super.propertyValueChanged(old, current);
                        }
                    },

                    new FormProperty("TabConstraints.tabIcon", // NOI18N
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

                        protected void propertyValueChanged(Object old, Object current) {
                            if (isChangeFiring())
                                updateCode();
                            super.propertyValueChanged(old, current);
                        }
                    },

                    new FormProperty("TabConstraints.tabToolTip", // NOI18N
                                     String.class,
                                 getBundle().getString("PROP_tabToolTip"), // NOI18N
                                 getBundle().getString("HINT_tabToolTip")) { // NOI18N

                        public Object getTargetValue() {
                            return toolTip;
                        }

                        public void setTargetValue(Object value) {
                            toolTip = (String)value;
                        }

                        protected Object getRealValue(Object value) {
                            Object realValue = super.getRealValue(value);
                            if (realValue == FormDesignValue.IGNORED_VALUE)
                                realValue = ((FormDesignValue)value).getDescription();
                            return realValue;
                        }

                        public boolean supportsDefaultValue() {
                            return true;
                        }

                        public Object getDefaultValue() {
                            return null;
                        }

                        protected void propertyValueChanged(Object old, Object current) {
                            if (isChangeFiring())
                                updateCode();
                            super.propertyValueChanged(old, current);
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
                getProperties(),
                constr.getProperties(),
                FormUtils.CHANGED_ONLY | FormUtils.DISABLE_CHANGE_FIRING);
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
