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

    private java.util.List componentCodeElements;
    private java.util.List componentCodeGroups;
    private java.util.List componentConstraints;

    private BeanCodeManager layoutBeanElement;
    private CodeConnectionGroup setLayoutCode;

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

        if (componentCodeElements != null)
            componentCodeElements.clear();
        else componentCodeElements = new ArrayList();

        if (componentCodeGroups != null)
            componentCodeGroups.clear();
        else componentCodeGroups = new ArrayList();

        if (componentConstraints != null)
            componentConstraints.clear();
        else componentConstraints = new ArrayList();

        if (setLayoutCode != null)
            setLayoutCode.removeAll();
        else setLayoutCode = codeStructure.createConnectionGroup();

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
            CodeConnectionGroup componentCode = null;
            Iterator it = CodeStructure.getConnectionsIterator(
                                            getActiveContainerCodeElement());
            while (it.hasNext()) {
                if (componentCode == null)
                    componentCode = codeStructure.createConnectionGroup();

                CodeConnection connection = (CodeConnection) it.next();
                CodeElement compElement = readComponentCode(connection,
                                                            componentCode);
                if (compElement != null) {
                    componentCodeElements.add(compElement);
                    componentCodeGroups.add(componentCode);
                    componentCode = null;

                    if (componentConstraints.size() < componentCodeElements.size())
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

    public CodeConnectionGroup getLayoutCode() {
        return setLayoutCode;
    }

    public CodeConnectionGroup getComponentCode(int index) {
        return (CodeConnectionGroup) componentCodeGroups.get(index);
    }

    public CodeElement getComponentCodeElement(int index) {
        return (CodeElement) componentCodeElements.get(index);
    }

    public int getComponentCount() {
        return componentCodeElements.size();
    }

    // components adding/removing
    public void addComponents(CodeElement[] newCompElements,
                              LayoutConstraints[] newConstraints)
    {
        int oldCount = componentCodeElements.size();
        CodeStructure codeStructure = layoutContext.getCodeStructure();

        for (int i=0; i < newCompElements.length; i++) {
            CodeElement compElement = newCompElements[i];
            componentCodeElements.add(compElement);

            LayoutConstraints constr = newConstraints != null ?
                                       newConstraints[i] : null;
            if (constr == null)
                constr = createDefaultConstraints();

            componentConstraints.add(constr);

            CodeConnectionGroup componentCode =
                codeStructure.createConnectionGroup();
            createComponentCode(componentCode, compElement, i + oldCount);
            componentCodeGroups.add(componentCode);
        }
    }

    public void removeComponent(int index) {
        componentCodeElements.remove(index);
        componentCodeGroups.remove(index);
        componentConstraints.remove(index);
    }

    public void removeAll() {
        componentCodeElements.clear();
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
                                             CodeElement[] targetComponents)
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
    // This methods returns the code element to be used for layout settings
    // and components - this can be either container, or container delegate
    // element. In fact, it is container delegate in most cases (so this method
    // needs to be overriden very rarely). But there's e.g. JScrollPane which
    // has viewport as the container delegate, but we work with the JScrollPane.
    protected CodeElement getActiveContainerCodeElement() {
        return layoutContext.getContainerDelegateCodeElement();
    }

    // can be overriden
    protected void readLayoutCode(CodeConnectionGroup layoutCode) {
        if (isDedicated())
            return;

        CodeConnectionGroup initLayoutCode =
            getCodeStructure().createConnectionGroup();
        CodeConnection setLayoutConnection = null;

        CodeConnection[] connections = CodeStructure.getConnections(
                                           getActiveContainerCodeElement(),
                                           getSetLayoutMethod());
        if (connections.length > 0) { // read from code
            setLayoutConnection = connections[0];
            readInitLayoutCode(setLayoutConnection.getConnectionParameters()[0],
                               initLayoutCode);
        }
        else { // create new
            CodeElement layoutElement = createInitLayoutCode(initLayoutCode);
            if (layoutElement != null)
                setLayoutConnection = CodeStructure.createConnection(
                         getActiveContainerCodeElement(),
                         getSetLayoutMethod(),
                         new CodeElement[] { layoutElement });
        }

        if (setLayoutConnection != null) {
            layoutCode.addGroup(initLayoutCode);
            layoutCode.addConnection(setLayoutConnection);
        }
    }

    // can be overriden
    protected void readInitLayoutCode(CodeElement layoutElement,
                                      CodeConnectionGroup initLayoutCode)
    {
        if (metaLayout == null)
            return;

        layoutBeanElement = new BeanCodeManager(
            getSupportedClass(),
            getAllProperties(),
            CreationDescriptor.PLACE_ALL | CreationDescriptor.CHANGED_ONLY,
            false, // don't force empty constructor
            false, // disable changes firing when properties are restored
            layoutElement,
            initLayoutCode);
    }

    // can be overriden
    protected CodeElement createInitLayoutCode(
                              CodeConnectionGroup initLayoutCode)
    {
        if (metaLayout == null)
            return null;

        layoutBeanElement = new BeanCodeManager(
            getSupportedClass(),
            getAllProperties(),
            CreationDescriptor.PLACE_ALL | CreationDescriptor.CHANGED_ONLY,
            false,
            layoutContext.getCodeStructure(),
            CodeElementVariable.LOCAL,
            initLayoutCode);

        return layoutBeanElement.getCodeElement();
    }

    // can be overriden
    // called automatically when some property of layout has been changed
    protected void layoutChanged() {
        if (layoutBeanElement != null)
            layoutBeanElement.updateCode();
    }

    // can be overriden
    protected CodeElement readComponentCode(CodeConnection connection,
                                            CodeConnectionGroup componentCode)
    {
        CodeElement compElement;
        CodeConnectionGroup constrCode;
        LayoutConstraints constr;

        if (getSimpleAddMethod().equals(connection.getConnectingObject())) {
            compElement = connection.getConnectionParameters()[0];
            constrCode = null;
            constr = null;
        }
        else if (getAddWithConstraintsMethod().equals(
                                 connection.getConnectingObject()))
        {
            CodeElement[] params = connection.getConnectionParameters();

            compElement = params[0];
            constrCode = getCodeStructure().createConnectionGroup();
            constr = readConstraintsCode(params[1], constrCode, compElement);
        }
        else return null;

        componentConstraints.add(constr);
        if (constrCode != null)
            componentCode.addGroup(constrCode);
        componentCode.addConnection(connection);

        return compElement;
    }

    // can be overriden
    protected LayoutConstraints readConstraintsCode(
                                    CodeElement constrElement,
                                    CodeConnectionGroup constrCode,
                                    CodeElement compElement)
    {
        return null;
    }

    // can be overriden
    // creates code for one newly added component
    protected void createComponentCode(CodeConnectionGroup componentCode,
                                       CodeElement compElement,
                                       int index)
    {
        CodeConnectionGroup constrCode =
            getCodeStructure().createConnectionGroup();
        LayoutConstraints constr = getConstraints(index);

        // first create init code for the constraints object
        CodeElement constrElement = createConstraintsCode(
                                      constrCode, constr, compElement, index);

        // create "add" code for the component
        CodeConnection compAddConnection;
        if (constrElement != null) { // add with constraints
            compAddConnection = CodeStructure.createConnection(
                    getActiveContainerCodeElement(),
                    getAddWithConstraintsMethod(),
                    new CodeElement[] { compElement, constrElement });
        }
        else { // add without constraints
            compAddConnection = CodeStructure.createConnection(
                    getActiveContainerCodeElement(),
                    getSimpleAddMethod(),
                    new CodeElement[] { compElement });
        }

        componentCode.addGroup(constrCode);
        componentCode.addConnection(compAddConnection);
    }

    // can be overriden
    protected CodeElement createConstraintsCode(CodeConnectionGroup constrCode,
                                                LayoutConstraints constr,
                                                CodeElement compElement,
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

    protected final CodeConnection getSetLayoutConnection() {
        CodeConnection[] found = CodeStructure.getConnections(
                                     getActiveContainerCodeElement(),
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
