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

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.lang.reflect.Method;

import org.openide.nodes.Node;
import org.openide.util.Utilities;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * @author Tomas Pavek
 */

public abstract class AbstractLayoutSupport implements LayoutSupportDelegate
{
    private static Image defaultLayoutIcon;
    private static Image defaultLayoutIcon32;

    private static ResourceBundle bundle = null;

    private static Method simpleAddMethod = null;
    private static Method addWithConstraintsMethod = null;
    private static Method setLayoutMethod = null;

    // ------

    private LayoutSupportContext layoutContext;

    private java.util.List componentCodeExpressions;
    private java.util.List componentCodeGroups;
    private java.util.List componentConstraints;

    private BeanCodeManager layoutBeanCode;
    private CodeGroup setLayoutCode;

    private MetaLayout metaLayout;
    private Node.PropertySet[] propertySets;
    private FormProperty[] allProperties;

    private PropertyChangeListener layoutListener;

    // -----------
    // LayoutSupportDelegate interface implementation

    public void initialize(LayoutSupportContext layoutContext,
                           boolean fromCode)
    {
        this.layoutContext = layoutContext;

        CodeStructure codeStructure = layoutContext.getCodeStructure();

        if (componentCodeExpressions != null)
            componentCodeExpressions.clear();
        else componentCodeExpressions = new ArrayList();

        if (componentCodeGroups != null)
            componentCodeGroups.clear();
        else componentCodeGroups = new ArrayList();

        if (componentConstraints != null)
            componentConstraints.clear();
        else componentConstraints = new ArrayList();

        if (setLayoutCode != null)
            setLayoutCode.removeAll();
        else setLayoutCode = codeStructure.createCodeGroup();

        Class cls = getSupportedClass();
        if (cls != null && LayoutManager.class.isAssignableFrom(cls)) {
            // create default layout instance and metacomponent for it
            LayoutManager lmInstance = null;
            try {
                lmInstance = createDefaultLayoutInstance(
                               layoutContext.getPrimaryContainer(),
                               layoutContext.getPrimaryContainerDelegate());
            }
            catch (Exception ex) { // cannot make default layout instance
                ex.printStackTrace(); // [just ignore??]
            }

            if (lmInstance != null)
                metaLayout = new MetaLayout(this, lmInstance);
        }
        else metaLayout = null;

        // read layout code
        readLayoutCode(setLayoutCode);

        if (fromCode) { // read components from code
            CodeGroup componentCode = null;
            Iterator it = CodeStructure.getStatementsIterator(
                                            getActiveContainerCodeExpression());
            while (it.hasNext()) {
                if (componentCode == null)
                    componentCode = codeStructure.createCodeGroup();

                CodeStatement statement = (CodeStatement) it.next();
                CodeExpression compExp = readComponentCode(statement,
                                                            componentCode);
                if (compExp != null) {
                    componentCodeExpressions.add(compExp);
                    componentCodeGroups.add(componentCode);
                    componentCode = null;

                    if (componentConstraints.size()
                            < componentCodeExpressions.size())
                        componentConstraints.add(null);
                }
            }
        }
    }

    public boolean isDedicated() {
        Class cls = getSupportedClass();
        return cls != null && !LayoutManager.class.isAssignableFrom(cls);
    }

    // node presentation
    public boolean shouldHaveNode() {
        Class cls = getSupportedClass();
        return cls != null && LayoutManager.class.isAssignableFrom(cls);
    }

    public String getDisplayName() {
        String name = getSupportedClass().getName();
        int lastdot = name.lastIndexOf('.');
        if (lastdot > 0)
            name = name.substring(lastdot + 1);
        return name;
    }

    public Image getIcon(int type) {
        if (metaLayout != null) {
            Image icon = metaLayout.getBeanInfo().getIcon(type);
            if (icon != null)
                return icon;
        }

        switch (type) {
            case BeanInfo.ICON_COLOR_16x16:
            case BeanInfo.ICON_MONO_16x16:
                if (defaultLayoutIcon == null)
                    defaultLayoutIcon = Utilities.loadImage(
                        "org/netbeans/modules/form/layoutsupport/resources/AbstractLayout.gif"); // NOI18N
                return defaultLayoutIcon;

            default:
                if (defaultLayoutIcon32 == null)
                    defaultLayoutIcon32 = Utilities.loadImage(
                        "org/netbeans/modules/form/layoutsupport/resources/AbstractLayout32.gif"); // NOI18N
                return defaultLayoutIcon32;
        }
    }

    public Node.PropertySet[] getPropertySets() {
        if (propertySets == null) {
            FormProperty[] properties = getProperties();
            if (properties == null) {
                propertySets = metaLayout != null ?
                                   metaLayout.getProperties() : null;
            }
            else { // a subclass provides special properties
                propertySets = new Node.PropertySet[1];
                propertySets[0] = new Node.PropertySet(
                    "properties", // NOI18N
                    FormEditor.getFormBundle().getString("CTL_PropertiesTab"), // NOI18N
                    FormEditor.getFormBundle().getString("CTL_PropertiesTabHint")) // NOI18N
                {
                    public Node.Property[] getProperties() {
                        return AbstractLayoutSupport.this.getProperties();
                    }
                };
            }

            if (propertySets != null) {
                ArrayList allPropsList = new ArrayList();
                for (int i=0; i < propertySets.length; i++) {
                    Node.Property[] props = propertySets[i].getProperties();
                    for (int j=0; j < props.length; j++) {
                        Node.Property prop = props[j];
                        if (prop instanceof FormProperty) {
                            allPropsList.add(prop);
                            ((FormProperty)prop).addPropertyChangeListener(
                                                   getLayoutPropertyListener());
                        }
                    }
                }
                allProperties = new FormProperty[allPropsList.size()];
                allPropsList.toArray(allProperties);
            }
            else allProperties = new FormProperty[0];
        }
        return propertySets;
    }

    public Class getCustomizerClass() {
        return null;
    }

    public CodeGroup getLayoutCode() {
        return setLayoutCode;
    }

    public CodeGroup getComponentCode(int index) {
        return (CodeGroup) componentCodeGroups.get(index);
    }

    public CodeExpression getComponentCodeExpression(int index) {
        return (CodeExpression) componentCodeExpressions.get(index);
    }

    public int getComponentCount() {
        return componentCodeExpressions.size();
    }

    // components adding/removing
    public void addComponents(CodeExpression[] newCompExps,
                              LayoutConstraints[] newConstraints)
    {
        int oldCount = componentCodeExpressions.size();
        CodeStructure codeStructure = layoutContext.getCodeStructure();

        for (int i=0; i < newCompExps.length; i++) {
            CodeExpression compExp = newCompExps[i];
            componentCodeExpressions.add(compExp);

            LayoutConstraints constr = newConstraints != null ?
                                       newConstraints[i] : null;
            if (constr == null)
                constr = createDefaultConstraints();

            componentConstraints.add(constr);

            CodeGroup componentCode =
                codeStructure.createCodeGroup();
            createComponentCode(componentCode, compExp, i + oldCount);
            componentCodeGroups.add(componentCode);
        }
    }

    public void removeComponent(int index) {
        componentCodeExpressions.remove(index);
        componentCodeGroups.remove(index);
        componentConstraints.remove(index);
    }

    public void removeAll() {
        componentCodeExpressions.clear();
        componentCodeGroups.clear();
        componentConstraints.clear();
    }

    public boolean isLayoutChanged(Container defaultContainer,
                                   Container defaultContainerDelegate)
    {
        if (isDedicated())
            return false;

        Class layoutClass = getSupportedClass();
        LayoutManager lm = defaultContainerDelegate.getLayout();

        if (layoutClass == null)
            return lm != null;
        if (lm == null)
            return true;
        if (!layoutClass.isAssignableFrom(lm.getClass()))
            return true;

        FormProperty[] props = getAllProperties();
        for (int i=0; i < props.length; i++)
            if (props[i].isChanged())
                return true;

        return false;
    }

    // managing constraints
    public LayoutConstraints getConstraints(int index) {
        return index < 0 || index >= componentConstraints.size() ? null :
               (LayoutConstraints) componentConstraints.get(index);
    }

    public void convertConstraints(LayoutConstraints[] previousConstraints,
                                   LayoutConstraints[] currentConstraints,
                                   Component[] components)
    {
    }

    // managing live components
    public void setLayoutToContainer(Container container,
                                     Container containerDelegate)
    {
        if (isDedicated())
            return;

        LayoutManager lm = null;
        try {
            if (containerDelegate == layoutContext.getPrimaryContainerDelegate()) {
                if (metaLayout != null) // use the instance of MetaLayout
                    lm = (LayoutManager) metaLayout.getBeanInstance();
            }
            else { // use cloned layout instance
                lm = cloneLayoutInstance(container, containerDelegate);
            }
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }

        if (lm != null)
            containerDelegate.setLayout(lm);
    }

    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        for (int i=0; i < components.length; i++) {
            LayoutConstraints constr = getConstraints(i+index);
            if (constr != null)
                containerDelegate.add(components[i],
                                      constr.getConstraintsObject(),
                                      i + index);
            else
                containerDelegate.add(components[i], i + index);
        }
    }

    public boolean removeComponentFromContainer(Container container,
                                                Container containerDelegate,
                                                Component component,
                                                int index)
    {
        containerDelegate.remove(component);
        return true;
    }

    public boolean clearContainer(Container container,
                                  Container containerDelegate)
    {
        containerDelegate.removeAll();
        return true;
    }

    // drag and drop support
    public LayoutConstraints getNewConstraints(Container container,
                                               Container containerDelegate,
                                               Component component,
                                               int index,
                                               Point posInCont, Point posInComp)
    {
        return null;
    }

    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont, Point posInComp)
    {
        return -1;
    }

    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        return false;
    }

    // resizing support
    public int getResizableDirections(Component component, int index) {
        return 0;
    }

    public LayoutConstraints getResizedConstraints(Component component,
                                                   int index,
                                                   Insets sizeChanges)
    {
        return null;
    }

    // copying
    public LayoutSupportDelegate cloneLayout(LayoutSupportContext targetContext,
                                             CodeExpression[] targetComponents)
    {
        AbstractLayoutSupport clone;
        try {
            clone = (AbstractLayoutSupport) getClass().newInstance();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        clone.initialize(targetContext, false);

        FormProperty[] sourceProperties = getAllProperties();
        FormProperty[] targetProperties = clone.getAllProperties();
        FormUtils.copyProperties(sourceProperties, targetProperties,
                                 true, false);
        clone.layoutChanged();

        int compCount = getComponentCount();
        LayoutConstraints[] constraints = new LayoutConstraints[compCount];
        for (int i=0; i < compCount; i++) {
            LayoutConstraints constr = getConstraints(i);
            constraints[i] = constr != null ? constr.cloneConstraints() : null;
        }

        clone.addComponents(targetComponents, constraints);

        return clone;
    }

    // ---------
    // extending API for subclasses

    // can be overriden
    protected LayoutManager createDefaultLayoutInstance(
                                Container container,
                                Container containerDelegate)
        throws Exception
    {
        return (LayoutManager)
               CreationFactory.createDefaultInstance(getSupportedClass());
    }

    // can be overriden
    protected LayoutManager cloneLayoutInstance(Container container,
                                                Container containerDelegate)
        throws Exception
    {
        return metaLayout == null ? null :
               (LayoutManager) metaLayout.cloneBeanInstance(null);
    }

    // can be overriden
    // This methods returns the code expression to be used for layout settings
    // and components - this can be either container, or container delegate
    // expression. In fact, it is container delegate in most cases (so this
    // method needs to be overriden very rarely). But there's e.g. JScrollPane
    // which has viewport as the container delegate, but we work with the
    // JScrollPane (whole container).
    protected CodeExpression getActiveContainerCodeExpression() {
        return layoutContext.getContainerDelegateCodeExpression();
    }

    // can be overriden
    protected void readLayoutCode(CodeGroup layoutCode) {
        if (isDedicated())
            return;

        CodeGroup initLayoutCode =
            getCodeStructure().createCodeGroup();
        CodeStatement setLayoutStatement = null;

        CodeStatement[] statements = CodeStructure.getStatements(
                                           getActiveContainerCodeExpression(),
                                           getSetLayoutMethod());
        if (statements.length > 0) { // read from code
            setLayoutStatement = statements[0];
            readInitLayoutCode(setLayoutStatement.getStatementParameters()[0],
                               initLayoutCode);
        }
        else { // create new
            CodeExpression layoutExp = createInitLayoutCode(initLayoutCode);
            if (layoutExp != null)
                setLayoutStatement = CodeStructure.createStatement(
                         getActiveContainerCodeExpression(),
                         getSetLayoutMethod(),
                         new CodeExpression[] { layoutExp });
        }

        if (setLayoutStatement != null) {
            layoutCode.addGroup(initLayoutCode);
            layoutCode.addStatement(setLayoutStatement);
        }
    }

    // can be overriden
    protected void readInitLayoutCode(CodeExpression layoutExp,
                                      CodeGroup initLayoutCode)
    {
        if (metaLayout == null)
            return;

        layoutBeanCode = new BeanCodeManager(
            getSupportedClass(),
            getAllProperties(),
            CreationDescriptor.PLACE_ALL | CreationDescriptor.CHANGED_ONLY,
            false, // don't force empty constructor
            false, // disable changes firing when properties are restored
            layoutExp,
            initLayoutCode);
    }

    // can be overriden
    protected CodeExpression createInitLayoutCode(CodeGroup initLayoutCode) {
        if (metaLayout == null)
            return null;

        layoutBeanCode = new BeanCodeManager(
            getSupportedClass(),
            getAllProperties(),
            CreationDescriptor.PLACE_ALL | CreationDescriptor.CHANGED_ONLY,
            false,
            layoutContext.getCodeStructure(),
            CodeVariable.LOCAL,
            initLayoutCode);

        return layoutBeanCode.getCodeExpression();
    }

    // can be overriden
    // called automatically when some property of layout has been changed
    protected void layoutChanged() {
        if (layoutBeanCode != null)
            layoutBeanCode.updateCode();
    }

    // can be overriden
    protected CodeExpression readComponentCode(CodeStatement statement,
                                               CodeGroup componentCode)
    {
        CodeExpression compExp;
        CodeGroup constrCode;
        LayoutConstraints constr;

        if (getSimpleAddMethod().equals(statement.getMetaObject())) {
            compExp = statement.getStatementParameters()[0];
            constrCode = null;
            constr = null;
        }
        else if (getAddWithConstraintsMethod().equals(
                                 statement.getMetaObject()))
        {
            CodeExpression[] params = statement.getStatementParameters();

            compExp = params[0];
            constrCode = getCodeStructure().createCodeGroup();
            constr = readConstraintsCode(params[1], constrCode, compExp);
        }
        else return null;

        componentConstraints.add(constr);
        if (constrCode != null)
            componentCode.addGroup(constrCode);
        componentCode.addStatement(statement);

        return compExp;
    }

    // can be overriden
    protected LayoutConstraints readConstraintsCode(CodeExpression constrExp,
                                                    CodeGroup constrCode,
                                                    CodeExpression compExp)
    {
        return null;
    }

    // can be overriden
    // creates code for one newly added component
    protected void createComponentCode(CodeGroup componentCode,
                                       CodeExpression compExp,
                                       int index)
    {
        CodeGroup constrCode = getCodeStructure().createCodeGroup();
        LayoutConstraints constr = getConstraints(index);

        // first create init code for the constraints object
        CodeExpression constrExp = createConstraintsCode(
                                       constrCode, constr, compExp, index);

        // create "add" code for the component
        CodeStatement compAddStatement;
        if (constrExp != null) { // add with constraints
            compAddStatement = CodeStructure.createStatement(
                    getActiveContainerCodeExpression(),
                    getAddWithConstraintsMethod(),
                    new CodeExpression[] { compExp, constrExp });
        }
        else { // add without constraints
            compAddStatement = CodeStructure.createStatement(
                    getActiveContainerCodeExpression(),
                    getSimpleAddMethod(),
                    new CodeExpression[] { compExp });
        }

        componentCode.addGroup(constrCode);
        componentCode.addStatement(compAddStatement);
    }

    // can be overriden
    protected CodeExpression createConstraintsCode(CodeGroup constrCode,
                                                   LayoutConstraints constr,
                                                   CodeExpression compExp,
                                                   int index)
    {
        return null;
    }

    // can be overriden
    protected LayoutConstraints createDefaultConstraints() {
        return null;
    }

    // can be overriden
    protected Node.Property getProperty(String propName) {
        return metaLayout == null ? null :
                                    metaLayout.getPropertyByName(propName);
    }

    /** Can be overriden to provide other properties than standard bean
     * properties (handled by MetaLayout). This method is called only by
     * getPropertySets() interface method to obtain default property set for
     * the layout. So it is also possible to override (more generally)
     * getPropertySets() instead. Overriding this method requires also dealing
     * with layout initialization code - createInitLayoutCode(...) method.
     */
    protected FormProperty[] getProperties() {
        return null;
    }

    // ---------
    // useful method for subclasses

    protected final LayoutSupportContext getLayoutContext() {
        return layoutContext;
    }

    protected final CodeStructure getCodeStructure() {
        return layoutContext.getCodeStructure();
    }

    protected final java.util.List getConstraintsList() {
        return componentConstraints;
    }

    protected final FormProperty[] getAllProperties() {
        if (allProperties == null)
            getPropertySets();

        return allProperties;
    }

    // to be used by subclasses if they need to re-create the layout instance
    // used by MetaLayout (see BoxLayoutSupport)
    protected final void updateLayoutInstance() {
        Container cont = layoutContext.getPrimaryContainer();
        Container contDel = layoutContext.getPrimaryContainerDelegate();
        Component comps[] = contDel.getComponents();

        LayoutManager lm = null;
        try {
            lm = cloneLayoutInstance(cont, contDel);
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }

        if (lm != null && metaLayout != null)
            metaLayout.updateInstance(lm);
    
        clearContainer(cont, contDel);
        setLayoutToContainer(cont, contDel);
        addComponentsToContainer(cont, contDel, comps, 0);
    }

    protected final CodeStatement getSetLayoutStatement() {
        CodeStatement[] found = CodeStructure.getStatements(
                                     getActiveContainerCodeExpression(),
                                     getSetLayoutMethod());
        return found != null && found.length > 0 ? found[0] : null;
    }

    // ---------
    // utility methods

    // [protected?? - subclasses]
    protected static ResourceBundle getBundle() {
        if (bundle == null)
            bundle = org.openide.util.NbBundle.getBundle(AbstractLayoutSupport.class);
        return bundle;
    }

    protected static Method getSimpleAddMethod() {
        if (simpleAddMethod == null) {
            try {
                simpleAddMethod = Container.class.getMethod(
                                      "add", // NOI18N
                                      new Class[] { Component.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return simpleAddMethod;
    }

    protected static Method getAddWithConstraintsMethod() {
        if (addWithConstraintsMethod == null) {
            try {
                addWithConstraintsMethod = Container.class.getMethod(
                                               "add", // NOI18N
                                               new Class[] { Component.class,
                                                             Object.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return addWithConstraintsMethod;
    }

    protected static Method getSetLayoutMethod() {
        if (setLayoutMethod == null) {
            try {
                setLayoutMethod = Container.class.getMethod(
                                    "setLayout", // NOI18N
                                    new Class[] { LayoutManager.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return setLayoutMethod;
    }

    // -------
    // private methods

    private PropertyChangeListener getLayoutPropertyListener() {
        if (layoutListener == null)
            layoutListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    Object source = ev.getSource();
                    if (!(source instanceof FormProperty))
                        return;

                    layoutChanged();

                    ev = FormProperty.PROP_VALUE.equals(ev.getPropertyName()) ?
                         new PropertyChangeEvent(AbstractLayoutSupport.this,
                                                 ((FormProperty)source).getName(),
                                                 ev.getOldValue(),
                                                 ev.getNewValue())
                         :
                         new PropertyChangeEvent(AbstractLayoutSupport.this,
                                                 null, null, null);

                    layoutContext.containerLayoutChanged(ev);
                }
            };

        return layoutListener;
    }
}
