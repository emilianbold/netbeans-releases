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

package org.netbeans.modules.form;

import java.beans.*;
import java.util.*;
import java.lang.reflect.Method;
import org.netbeans.modules.form.RADProperty.FakePropertyDescriptor;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

import org.netbeans.modules.form.codestructure.*;

/**
 *
 * @author Ian Formanek
 */

public class RADComponent {

    // -----------------------------------------------------------------------------
    // Static variables

    public static final String PROP_NAME = "variableName"; // NOI18N

    static final NewType[] NO_NEW_TYPES = {};
    static final RADProperty[] NO_PROPERTIES = {};
    static final BindingProperty[] NO_BINDINGS = {};

    // -----------------------------------------------------------------------------
    // Private variables

    private static int idCounter;

    private String id = Integer.toString(++idCounter);

    private Class<? extends Object> beanClass;
    private Object beanInstance;
    private BeanInfo beanInfo;
    private BeanInfo fakeBeanInfo;
    private String missingClassName;

    protected Node.PropertySet[] propertySets;
    private Node.Property[] syntheticProperties;
    private RADProperty[] beanProperties1;
    private RADProperty[] beanProperties2;
    private BindingProperty[][] bindingProperties;
    private EventProperty[] eventProperties;
    private Map<Object,FormProperty[]> otherProperties;
    private List actionProperties;

    private RADProperty[] knownBeanProperties;
    private Event[] knownEvents; // must be grouped by EventSetDescriptor

    private PropertyChangeListener propertyListener;

    private Map<String,Object> auxValues;
    protected Map<String,Node.Property> nameToProperty;

    private RADComponent parentComponent;

    private FormModel formModel;
    private boolean inModel;

    private RADComponentNode componentNode;

    private CodeExpression componentCodeExpression;

    private String storedName; // component name preserved e.g. for remove undo

    private boolean valid = true;
    // -----------------------------------------------------------------------------
    // Constructors & Initialization

    /** Called to initialize the component with specified FormModel.
     * @param formModel the FormModel of the form into which this component
     * will be added 
     */
    public boolean initialize(FormModel formModel) {
        if (this.formModel == null) {
            this.formModel = formModel;

            // properties and events will be created on first request
            clearProperties();

//            if (beanClass != null)
//                createCodeExpression();

            return true;
        }
        else if (this.formModel != formModel)
            throw new IllegalStateException(
                "Cannot initialize metacomponent with another form model"); // NOI18N
        return false;
    }

    public void setParentComponent(RADComponent parentComp) {
        parentComponent = parentComp;
    }

    /** Initializes the bean instance represented by this meta component.
     * A default instance is created for the given bean class.
     * The meta component is fully initialized after this method returns.
     * @param beanClass the bean class to be represented by this meta component
     */
    public Object initInstance(Class<? extends Object> beanClass) throws Exception {
        if (beanClass == null)
            throw new NullPointerException();

        if (this.beanClass != beanClass && this.beanClass != null) {
            beanInfo = null;
            fakeBeanInfo = null;
            clearProperties();
        }

        this.beanClass = beanClass;

        Object bean = createBeanInstance();
        getBeanInfo(); // force BeanInfo creation here - will be needed, may fail
        setBeanInstance(bean);

        return beanInstance;
    }

    /** Sets the bean instance represented by this meta component.
     * The meta component is fully initialized after this method returns.
     * @param beanInstance the bean to be represented by this meta component
     */
    public void setInstance(Object beanInstance) {
        if (this.beanClass != beanInstance.getClass()){
            beanInfo = null;                
            fakeBeanInfo = null;
        }
            
        clearProperties();

        this.beanClass = beanInstance.getClass();

        getBeanInfo(); // force BeanInfo creation here - will be needed, may fail
        setBeanInstance(beanInstance);

        getAllBeanProperties();
        for (int i=0; i < knownBeanProperties.length; i++) {
            try {
                knownBeanProperties[i].reinstateProperty();
            }
            catch (Exception ex) {
                ErrorManager.getDefault()
                    .notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    /** Updates the bean instance - e.g. when setting a property requires
     * to create new instance of the bean.
     */
    public void updateInstance(Object beanInstance) {
        if (this.beanInstance != null && this.beanClass == beanInstance.getClass())
            setBeanInstance(beanInstance);
            // should properties also be reinstated?
            // formModel.fireFormChanged() ?
        else
            setInstance(beanInstance);
    }

    /**
     * Called to create the instance of the bean. This method is called if the
     * initInstance method is used; using the setInstance method, the bean
     * instance is set directly.
     * @return the instance of the bean that will be used during design time 
     */
    protected Object createBeanInstance() throws Exception {
        return CreationFactory.createDefaultInstance(beanClass);
    }

    /** Sets directly the bean instance. Can be overriden.
     */
    protected void setBeanInstance(Object beanInstance) {
        if (beanClass == null) { // bean class not set yet
            beanClass = beanInstance.getClass();
//            createCodeExpression();
        }
        this.beanInstance = beanInstance;
    }

    void setInModel(boolean in) {
        if (inModel != in) {
            inModel = in;
            formModel.updateMapping(this, in);
            if (in) {
                createCodeExpression();
            } else {
                releaseCodeExpression();
                setNodeReference(null);
            }
        }
    }

    void setNodeReference(RADComponentNode node) {
        this.componentNode = node;
    }

    protected void createCodeExpression() {
        // create expression object
        if (componentCodeExpression == null) {
            CodeStructure codeStructure = formModel.getCodeStructure();
            componentCodeExpression = codeStructure.createExpression(
                                   FormCodeSupport.createOrigin(this));
            codeStructure.registerExpression(componentCodeExpression);
        }
        // make sure a variable is attached
        if (formModel.getTopRADComponent() != this && componentCodeExpression.getVariable() == null) {
            formModel.getCodeStructure().createVariableForExpression(
                                           componentCodeExpression,
                                           0x30DF, // default type
                                           (String)getAuxValue("JavaCodeGenerator_TypeParameters"), //NOI18N
                                           storedName);
        }
    }

/*    final void removeCodeExpression() {
        if (componentCodeExpression != null) {
            CodeVariable var = componentCodeExpression.getVariable();
            if (var != null)
                storedName = var.getName();
            CodeStructure.removeExpression(componentCodeExpression);
        }
    } */

    final void releaseCodeExpression() {
        if (componentCodeExpression != null) {
            CodeVariable var = componentCodeExpression.getVariable();
            if (var != null) {
                storedName = var.getName();
                formModel.getCodeStructure()
                    .removeExpressionFromVariable(componentCodeExpression);
            }
        }
    }

    // -----------------------------------------------------------------------------
    // Public interface

    private boolean testMode = Boolean.getBoolean("netbeans.form.layout_test"); // NOI18N

    public final String getId() {
        return testMode ? getName() : id;
    }

    public final boolean isReadOnly() {
        return formModel.isReadOnly(); //readOnly;
    }

    /** Provides access to the Class of the bean represented by this RADComponent
     * @return the Class of the bean represented by this RADComponent
     */
    public final Class<? extends Object> getBeanClass() {
        return beanClass;
    }

    public final String getMissingClassName() {
        return missingClassName;
    }

    public final void setMissingClassName(String className) {
        missingClassName = className;
    }
    
    /** Provides access to the real instance of the bean represented by this RADComponent
     * @return the instance of the bean represented by this RADComponent
     */
    public final Object getBeanInstance() {
        return beanInstance;
    }

    public final RADComponent getParentComponent() {
        return parentComponent;
    }

    public final boolean isParentComponent(RADComponent comp) {
        if (comp == null)
            return false;

        do {
            comp = comp.getParentComponent();
            if (comp == this)
                return true;
        }
        while (comp != null);

        return false;
    }

    public Object cloneBeanInstance(Collection relativeProperties) {
        Object clone;
        try {
            clone = createBeanInstance();
        }
        catch (Exception ex) { // ignore, this should not fail
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            return null;
        }

        FormUtils.copyPropertiesToBean(getKnownBeanProperties(),
                                       clone,
                                       relativeProperties);
        return clone;
    }

    /** Provides access to BeanInfo of the bean represented by this RADComponent
     * @return the BeanInfo of the bean represented by this RADComponent
     */
    public BeanInfo getBeanInfo() {
        if (beanInfo == null) {
            try {
                beanInfo = FormUtils.getBeanInfo(beanClass);        
            } catch (Error err) { // Issue 74002
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, err);
                beanInfo = new FakeBeanInfo();
            } catch (IntrospectionException ex) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                beanInfo = new FakeBeanInfo();
            }
        }
        if(isValid()) {            
            return beanInfo;
        } else {
            if (fakeBeanInfo == null) {
		fakeBeanInfo  = new FakeBeanInfo();
	    }            
            return fakeBeanInfo ;            
        }        
    }            
    
    /** This method can be used to check whether the bean represented by this
     * RADComponent has hidden-state.
     * @return true if the component has hidden state, false otherwise
     */
    public boolean hasHiddenState() {
        String name = beanClass.getName();
        if (name.startsWith("javax.") // NOI18N
              || name.startsWith("java.") // NOI18N
              || name.startsWith("org.openide.")) // NOI18N
            return false;

        return getBeanInfo().getBeanDescriptor()
                                .getValue("hidden-state") != null; // NOI18N
    }

    public CodeExpression getCodeExpression() {
        return componentCodeExpression;
    }

    /** Getter for the name of the metacomponent - it maps to variable name
     * declared for the instance of the component in the generated java code.
     * It is a unique identification of the component within a form, but it may
     * change (currently editable as "Variable Name" in code gen. properties).
     * @return current value of the Name property
     */
    public String getName() {
        if (componentCodeExpression != null) {
            CodeVariable var = componentCodeExpression.getVariable();
            if (var != null)
                return var.getName();
        }
        return storedName;
    }

    /** Setter for the name of the component - it is the name of the
     * component's node and the name of the variable declared for the component
     * in the generated code.
     * @param value new name of the component
     */
    public void setName(String name) {
        if (!needsVariableRename(name)) {
            return;
        }

        String oldName = componentCodeExpression.getVariable().getName();

        formModel.getCodeStructure().renameVariable(oldName, name);

        resourceComponentRename(oldName, name);

        renameDefaultEventHandlers(oldName, name);
        // [possibility of renaming default event handlers should be probably
        // configurable in options]

        formModel.fireSyntheticPropertyChanged(this, PROP_NAME,
                                               oldName, name);

        if (getNodeReference() != null)
            getNodeReference().updateName();
    }
    
    /**
     * Method used to push setName through refactoring instead of just setting the variable name and
     * not changing it in users custom code.
     * @param name the new name for the variable
     */
    public void rename(String name) {
        if (!needsVariableRename(name)) {
            return;
        }

        if (!org.openide.util.Utilities.isJavaIdentifier(name)) {
            IllegalArgumentException iae =
                new IllegalArgumentException("Invalid component name"); // NOI18N
            ErrorManager.getDefault().annotate(
                iae, ErrorManager.USER, null, 
                FormUtils.getBundleString("ERR_INVALID_COMPONENT_NAME"), // NOI18N
                null, null);
            throw iae;
        }

        if (formModel.getCodeStructure().isVariableNameReserved(name)) {
            IllegalArgumentException iae =
                new IllegalArgumentException("Component name already in use: "+name); // NOI18N
            ErrorManager.getDefault().annotate(
                iae, ErrorManager.USER, null,
                FormUtils.getBundleString("ERR_COMPONENT_NAME_ALREADY_IN_USE"), // NOI18N
                null, null);
            throw iae;
        }

        try {
            RADComponentRenameRefactoringSupport rf = new RADComponentRenameRefactoringSupport(name);
            rf.setComponent(this);
            rf.doRenameRefactoring();
        } finally { // hack for robustness - if refactoring fails for whatever reason
            if (!getName().equals(name)) {
                setName(name);
            }
        }
    }

    private boolean needsVariableRename(String name) {
        if (componentCodeExpression != null) {
            CodeVariable var = componentCodeExpression.getVariable();
            return var != null && name != null && !name.equals(var.getName());
        }
        return false;
    }

    public void setStoredName(String name) {
        storedName = name;
    }

    private void renameDefaultEventHandlers(String oldComponentName,
                                            String newComponentName)
    {
        boolean renamed = false; // whether any handler was renamed
        FormEvents formEvents = null;

        Event[] events = getKnownEvents();
        for (int i=0; i < events.length; i++) {
            String[] handlers = events[i].getEventHandlers();
            for (int j=0; j < handlers.length; j++) {
                String handlerName = handlers[j];
                int idx = handlerName.indexOf(oldComponentName);
                if (idx >= 0) {
                    if (formEvents == null)
                         formEvents = getFormModel().getFormEvents();
                    String newHandlerName = formEvents.findFreeHandlerName(
                        handlerName.substring(0, idx)
                        + newComponentName
                        + handlerName.substring(idx + oldComponentName.length())
                    );
                    formEvents.renameEventHandler(handlerName, newHandlerName);
                }
            }
        }

        if (renamed && getNodeReference() != null)
            getNodeReference().fireComponentPropertySetsChange();
    }

    /** Allows to add an auxiliary <name, value> pair, which is persistent
     * in Gandalf. The current value can be obtained using
     * getAuxValue(aux_property_name) method. To remove aux value for specified
     * property name, use setAuxValue(name, null).
     * @param key name of the aux property
     * @param value new value of the aux property or null to remove it
     */
    public void setAuxValue(String key, Object value) {
        if (auxValues == null) {
            if (value == null)
                return;
            auxValues = new TreeMap<String,Object>();
        }
        auxValues.put(key, value);
    }

    /** Allows to obtain an auxiliary value for specified aux property name.
     * @param key name of the aux property
     * @return null if the aux value for specified name is not set
     */
    public Object getAuxValue(String key) {
        return auxValues != null ? auxValues.get(key) : null;
    }

    /** Provides access to the FormModel class which manages the form in which
     * this component has been added.
     * @return the FormModel which manages the form into which this component
     *         has been added
     */
    public final FormModel getFormModel() {
        return formModel;
    }

    public final boolean isInModel() {
        return inModel;
    }

    /** @return the map of all component's aux value-pairs of <String, Object>
     */
    public Map getAuxValues() {
        return auxValues;
    }

    /** Support for new types that can be created in this node.
     * @return array of new type operations that are allowed
     */
    public NewType[] getNewTypes() {
        return NO_NEW_TYPES;
    }

    public Node.PropertySet[] getProperties() {
        if (propertySets == null) {
            List<Node.PropertySet> propSets = new ArrayList<Node.PropertySet>(5);
            createPropertySets(propSets);
            propertySets = new Node.PropertySet[propSets.size()];
            propSets.toArray(propertySets);
        }
        return propertySets;
    }

    public RADProperty[] getAllBeanProperties() {
        if (knownBeanProperties == null) {
            createBeanProperties();
        }

        return knownBeanProperties;
    }

    public RADProperty[] getKnownBeanProperties() {
        return knownBeanProperties != null ? knownBeanProperties : NO_PROPERTIES;
    }

    public Iterator getBeanPropertiesIterator(FormProperty.Filter filter,
                                              boolean fromAll)
    {
        return new PropertyIterator(
                   fromAll ? getAllBeanProperties() : getKnownBeanProperties(),
                   filter);
    }

    public BindingProperty[] getAllBindingProperties() {
        BindingProperty[][] bprop = getBindingProperties();
        BindingProperty[] prop = new BindingProperty[bprop[0].length + bprop[1].length + bprop[2].length];
        System.arraycopy(bprop[0], 0, prop, 0, bprop[0].length);
        System.arraycopy(bprop[1], 0, prop, bprop[0].length, bprop[1].length);
        System.arraycopy(bprop[2], 0, prop, bprop[0].length+bprop[1].length, bprop[2].length);
        return prop;
    }
    
    public BindingProperty[][] getBindingProperties() {
        if (bindingProperties == null) {
            createBindingProperties();
        }
        return bindingProperties;
    }

    public final BindingProperty getBindingProperty(String name) {
        for (BindingProperty prop : getAllBindingProperties()) {
            if (prop.getName().equals(name))
                return prop;
        }
        return null;
    }

    BindingProperty[] getKnownBindingProperties() {
        return bindingProperties != null ? getAllBindingProperties() : NO_BINDINGS;
    }

    boolean hasBindings() {
        if (bindingProperties != null) {
            for (BindingProperty p : getAllBindingProperties()) {
                if (p.getValue() != null)
                    return true;
            }
        }
        return false;
    }

    /** Provides access to the Node which represents this RADComponent
     * @return the RADComponentNode which represents this RADComponent
     */
    public RADComponentNode getNodeReference() {
        return componentNode;
    }

    // -----------------------------------------------------------------------------
    // Access to component Properties

    Node.Property[] getSyntheticProperties() {
        if (syntheticProperties == null)
            syntheticProperties = createSyntheticProperties();
        return syntheticProperties;
    }

    RADProperty[] getBeanProperties1() {
        if (beanProperties1 == null)
            createBeanProperties();
        return beanProperties1;
    }

    RADProperty[] getBeanProperties2() {
        if (beanProperties2 == null)
            createBeanProperties();
        return beanProperties2;
    }

    EventProperty[] getEventProperties() {
        if (eventProperties == null)
            createEventProperties();
        return eventProperties;
    }
    
    List getActionProperties() {
        if (actionProperties == null) {
            createBeanProperties();
        }
        return actionProperties;
    }

    protected Node.Property getPropertyByName(String name,
                                              Class<? extends Object> propertyType,
                                              boolean fromAll)
    {
        Node.Property prop = nameToProperty.get(name);
        if (prop == null && fromAll) {
            if (beanProperties1 == null && !name.startsWith("$")) // NOI18N
                createBeanProperties();
            if (eventProperties == null && name.startsWith("$")) // NOI18N
                createEventProperties();

            prop = nameToProperty.get(name);
        }
        return prop != null
                 && (propertyType == null
                     || propertyType.isAssignableFrom(prop.getClass())) ?
               prop : null;
    }

    /**
     * Returns property of given name corresponding to a property or event.
     * Forces creation of all property objects.
     * @return bean or event property
     */
    public Node.Property getPropertyByName(String name) {
        return getPropertyByName(name, null, true);
    }

    public final RADProperty getBeanProperty(String name) {
        return (RADProperty) getPropertyByName(name, RADProperty.class, true);
    }

    public final Node.Property getSyntheticProperty(String name) {
        for (Node.Property prop : getSyntheticProperties()) {
            if (prop.getName().equals(name))
                return prop;
        }
        return null;
    }

    public RADProperty[] getFakeBeanProperties(String[] propNames, Class[] propertyTypes) {
        FakeBeanInfo fbi = (FakeBeanInfo) getBeanInfo();        
        fbi.removePropertyDescriptors();
        for (int i = 0; i < propNames.length; i++) {
            fbi.addPropertyDescriptor(propNames[i], propertyTypes[i]);
        }
        return getBeanProperties(propNames);
    }   

    /**
     * Returns bean properties of given names. Creates the properties if not
     * created yet, but does not force creation of all bean properties.
     * @return array of properties corresponding to the names; may contain
     *         null if there is no property of given name
     */
    public RADProperty[] getBeanProperties(String[] propNames) {
        RADProperty[] properties = new RADProperty[propNames.length];        
        PropertyDescriptor[] descriptors = null;
        
        boolean empty = knownBeanProperties == null;
        int validCount = 0;
        List<RADProperty> newProps = null;
        Object[] propAccessClsf = null;
        Object[] propParentChildDepClsf = null;

        int descIndex = 0;
        for (int i=0; i < propNames.length; i++) {
            String name = propNames[i];
            if (name == null)
                continue;

            RADProperty prop;
            if (!empty) {
                Object obj = nameToProperty.get(name);
                prop = obj instanceof RADProperty ? (RADProperty) obj : null;
            }
            else prop = null;

            if (prop == null) {
                if (descriptors == null) {
                    descriptors = getBeanInfo().getPropertyDescriptors();
                    if (descriptors.length == 0)
                        break;
                }
                int j = descIndex;
                do {
                    if (descriptors[j].getName().equals(name)) {
                        if (propAccessClsf == null) {
                            propAccessClsf = FormUtils.getPropertiesAccessClsf(beanClass);
                            propParentChildDepClsf = FormUtils.getPropertiesParentChildDepsClsf(beanClass);
                        }

                        prop = createBeanProperty(descriptors[j], propAccessClsf, propParentChildDepClsf);

                        if (!empty) {
                            if (newProps == null)
                                newProps = new ArrayList<RADProperty>();
                            newProps.add(prop);
                        }
                        descIndex = j + 1;
                        if (descIndex == descriptors.length
                                         && i+1 < propNames.length)
                            descIndex = 0; // go again from beginning
                        break;
                    }
                    j++;
                    if (j == descriptors.length)
                        j = 0;
                }
                while (j != descIndex);
            }
            if (prop != null) {
                properties[i] = prop;
                validCount++;
            }
            else { // force all properties creation, there might be more
                   // properties than from BeanInfo [currently just ButtonGroup]
                properties[i] = (RADProperty)
                                getPropertyByName(name, RADProperty.class, true);
                empty = false;
                newProps = null;
            }
        }

        if (empty) {
            if (validCount == properties.length)
                knownBeanProperties = properties;
            else if (validCount > 0) {
                knownBeanProperties = new RADProperty[validCount];
                for (int i=0,j=0; i < properties.length; i++)
                    if (properties[i] != null)
                        knownBeanProperties[j++] = properties[i];
            }
        }
        else if (newProps != null) { // just for consistency, should not occur
            RADProperty[] knownProps =
                new RADProperty[knownBeanProperties.length + newProps.size()];
            System.arraycopy(knownBeanProperties, 0,
                             knownProps, 0,
                             knownBeanProperties.length);
            for (int i=0; i < newProps.size(); i++)
                knownProps[i + knownBeanProperties.length] = newProps.get(i);

            knownBeanProperties = knownProps;
        }

        return properties;
    }

    public Event getEvent(String name) {
        Object prop = nameToProperty.get(name);
        if (prop == null && eventProperties == null) {
            createEventProperties();
            prop = nameToProperty.get(name);
        }
        return prop instanceof EventProperty ?
               ((EventProperty)prop).getEvent() : null;
    }

    public Event[] getEvents(String[] eventNames) {
        Event[] events = new Event[eventNames.length];
        EventSetDescriptor[] eventSets = null;

        boolean empty = knownEvents == null;
        int validCount = 0;
        List<Event> newEvents = null;

        int setIndex = 0;
        int methodIndex = 0;

        for (int i=0; i < eventNames.length; i++) {
            String name = eventNames[i];
            if (name == null)
                continue;

            boolean fullName = name.startsWith("$"); // NOI18N

            Event event;
            if (!empty) {
                Object obj = nameToProperty.get(name);
                event = obj instanceof EventProperty ?
                        ((EventProperty)obj).getEvent() : null;
            }
            else event = null;

            if (event == null) {
                if (eventSets == null) {
                    eventSets = getBeanInfo().getEventSetDescriptors();
                    if (eventSets.length == 0)
                        break;
                }
                int j = setIndex;
                do {
                    Method[] methods = eventSets[j].getListenerMethods();
                    if (methods.length > 0) {
                        int k = methodIndex;
                        do {
                            String eventFullId =
                                FormEvents.getEventIdName(methods[k]);
                            String eventId = fullName ?
                                eventFullId : methods[k].getName();
                            if (eventId.equals(name)) {
                                event = createEventProperty(eventFullId,
                                                            eventSets[j],
                                                            methods[k])
                                                .getEvent();
                                if (!empty) {
                                    if (newEvents == null)
                                        newEvents = new ArrayList<Event>();
                                    newEvents.add(event);
                                }
                                methodIndex = k + 1;
                                break;
                            }
                            k++;
                            if (k == methods.length)
                                k = 0;
                        }
                        while (k != methodIndex);
                    }

                    if (event != null) {
                        if (methodIndex == methods.length) {
                            methodIndex = 0;
                            setIndex = j + 1; // will continue in new set
                            if (setIndex == eventSets.length
                                            && i+1 < eventNames.length)
                                setIndex = 0; // go again from beginning
                        }
                        else setIndex = j; // will continue in the same set
                        break;
                    }

                    j++;
                    if (j == eventSets.length)
                        j = 0;
                    methodIndex = 0;
                }
                while (j != setIndex);
            }
            if (event != null) {
                events[i] = event;
                validCount++;
            }
        }

        if (empty) {
            if (validCount == events.length)
                knownEvents = events;
            else if (validCount > 0) {
                knownEvents = new Event[validCount];
                for (int i=0,j=0; i < events.length; i++)
                    if (events[i] != null)
                        knownEvents[j++] = events[i];
            }
        }
        else if (newEvents != null) { // just for consistency, should not occur
            Event[] known = new Event[knownEvents.length + newEvents.size()];
            System.arraycopy(knownEvents, 0, known, 0, knownEvents.length);
            for (int i=0; i < newEvents.size(); i++)
                known[i + knownEvents.length] = newEvents.get(i);

            knownEvents = known;
        }

        return events;
    }

    /** @return all events of the component grouped by EventSetDescriptor
     */
    public Event[] getAllEvents() {
        if (knownEvents == null || eventProperties == null) {
            if (eventProperties == null)
                createEventProperties();
            else {
                knownEvents = new Event[eventProperties.length];
                for (int i=0; i < eventProperties.length; i++)
                    knownEvents[i] = eventProperties[i].getEvent();
            }
        }

        return knownEvents;
    }

    // Note: events must be grouped by EventSetDescriptor
    public Event[] getKnownEvents() {
        return knownEvents != null ? knownEvents : FormEvents.NO_EVENTS;
    }

    // ---------
    // events

    void attachDefaultEvent() {
        Event event = null;

        int eventIndex = getBeanInfo().getDefaultEventIndex();
        if (eventIndex < getEventProperties().length && eventIndex >= 0)
            event = eventProperties[eventIndex].getEvent();
        else
            for (int i=0; i < eventProperties.length; i++) {
                Event e = eventProperties[i].getEvent();
                if ("actionPerformed".equals(e.getListenerMethod().getName())) { // NOI18N
                    event = e;
                    break;
                }
            }

        if (event != null)
            getFormModel().getFormEvents().attachEvent(event, null, null);
    }

    // -----------------------------------------------------------------------------
    // Properties

    protected void clearProperties() {
        if (nameToProperty != null)
            nameToProperty.clear();
        else nameToProperty = new HashMap<String,Node.Property>();

        propertySets = null;
        syntheticProperties = null;
        beanProperties1 = null;
        beanProperties2 = null;
        knownBeanProperties = null;
        eventProperties = null;
        knownEvents = null;
    }

    protected void createPropertySets(List<Node.PropertySet> propSets) {
        if (beanProperties1 == null)
            createBeanProperties();

        ResourceBundle bundle = FormUtils.getBundle();

        Node.PropertySet ps;        
        propSets.add(new Node.PropertySet(
                "properties", // NOI18N
                bundle.getString("CTL_PropertiesTab"), // NOI18N
                bundle.getString("CTL_PropertiesTabHint")) // NOI18N
        {
            public Node.Property[] getProperties() {
                return getBeanProperties1();
            }
        });

        if(isValid()) {
            Iterator entries = otherProperties.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry)entries.next();
                final String category = (String)entry.getKey();
                ps = new Node.PropertySet(category, category, category) {        
                    public Node.Property[] getProperties() {
                        if (otherProperties == null) {
                            createBeanProperties();
                        }
                        return (Node.Property[])otherProperties.get(category);
                    }
                };
                //ps.setValue("tabName", category); // NOI18N
                propSets.add(ps);
            }
        
            if (beanProperties2.length > 0) {
                propSets.add(new Node.PropertySet(
                        "properties2", // NOI18N
                        bundle.getString("CTL_Properties2Tab"), // NOI18N
                        bundle.getString("CTL_Properties2TabHint")) // NOI18N
                {
                    public Node.Property[] getProperties() {
                        return getBeanProperties2();
                    }
                });
            }

            if (getAllBindingProperties().length > 0) {
                BindingProperty[][] bprop = getBindingProperties();
                for (int i=0; i<bprop.length; i++) {
                    final int index = i;
                    ps = new Node.PropertySet(
                            "binding" + i, // NOI18N
                            bundle.getString("CTL_BindingTab" + i), // NOI18N
                            bundle.getString("CTL_BindingTabHint" + i)) // NOI18N
                    {
                        public Node.Property[] getProperties() {
                            return getBindingProperties()[index];
                        }
                    };
                    ps.setValue("tabName", bundle.getString("CTL_BindingTab")); // NOI18N
                    propSets.add(ps);
                }
            }

            ps = new Node.PropertySet(
                    "events", // NOI18N
                    bundle.getString("CTL_EventsTab"), // NOI18N
                    bundle.getString("CTL_EventsTabHint")) // NOI18N
            {
                public Node.Property[] getProperties() {
                    return getEventProperties();
                }
            };

            ps.setValue("tabName", bundle.getString("CTL_EventsTab")); // NOI18N
            propSets.add(ps);
        
        }        
        
        ps = new Node.PropertySet(
                "synthetic", // NOI18N
                bundle.getString("CTL_SyntheticTab"), // NOI18N
                bundle.getString("CTL_SyntheticTabHint")) // NOI18N
        {
            public Node.Property[] getProperties() {
                return getSyntheticProperties();
            }
        };
        ps.setValue("tabName", bundle.getString("CTL_SyntheticTab_Short")); // NOI18N
        propSets.add(ps);
    }

    protected Node.Property[] createSyntheticProperties() {
        CodeGenerator codeGen = FormEditor.getCodeGenerator(formModel);
        return codeGen != null ? codeGen.getSyntheticProperties(this) : new Node.Property[0];
    }

    private void createBeanProperties() {
        List<FormProperty> prefProps = new ArrayList<FormProperty>();
        List<FormProperty> normalProps = new ArrayList<FormProperty>();
        List<FormProperty> expertProps = new ArrayList<FormProperty>();
        Map<Object,List<FormProperty>> otherProps = new TreeMap<Object,List<FormProperty>>();
        List<FormProperty> actionProps = new LinkedList<FormProperty>();

        Object[] propsCats = FormUtils.getPropertiesCategoryClsf(
                                 beanClass, getBeanInfo().getBeanDescriptor());
        Object[] propsAccess = FormUtils.getPropertiesAccessClsf(beanClass);
        Object[] propParentChildDepClsf = FormUtils.getPropertiesParentChildDepsClsf(beanClass);

        PropertyDescriptor[] props = getBeanInfo().getPropertyDescriptors();
        for (int i = 0; i < props.length; i++) {
            PropertyDescriptor pd = props[i];
            boolean action = (pd.getValue("action") != null); // NOI18N
            Object category = pd.getValue("category"); // NOI18N
            List<FormProperty> listToAdd;
            
            if ((category == null) || (!(category instanceof String))) {
                Object propCat = FormUtils.getPropertyCategory(pd, propsCats);
                if (propCat == FormUtils.PROP_PREFERRED)
                    listToAdd = prefProps;
                else if (propCat == FormUtils.PROP_NORMAL)
                    listToAdd = normalProps;
                else if (propCat == FormUtils.PROP_EXPERT)
                    listToAdd = expertProps;
                else continue; // PROP_HIDDEN

            } else {
                listToAdd = otherProps.get(category);
                if (listToAdd == null) {
                    listToAdd = new ArrayList<FormProperty>();
                    otherProps.put(category, listToAdd);
                }
            }
            
            FormProperty prop = (FormProperty) nameToProperty.get(pd.getName());
            if (prop == null)
                prop = createBeanProperty(pd, propsAccess, propParentChildDepClsf);

            if (prop != null) {
                listToAdd.add(prop);
                if ("action".equals(pd.getName()) && (listToAdd == prefProps) && javax.swing.Action.class.isAssignableFrom(pd.getPropertyType())) { // NOI18N
                    action = true;
                    prop.setValue("actionName", FormUtils.getBundleString("CTL_SetAction")); // NOI18N
                }
                if (action) {
                    Object actionName = pd.getValue("actionName"); // NOI18N
                    if (actionName instanceof String) {
                        prop.setValue("actionName", actionName); // NOI18N
                    }
                    actionProps.add(prop);
                }
            }
        }

        changePropertiesExplicitly(prefProps, normalProps, expertProps);

        int prefCount = prefProps.size();
        int normalCount = normalProps.size();
        int expertCount = expertProps.size();
        int otherCount = 0;

        if (prefCount > 0) {
            beanProperties1 = new RADProperty[prefCount];
            prefProps.toArray(beanProperties1);
            if (normalCount + expertCount > 0) {
                normalProps.addAll(expertProps);
                beanProperties2 = new RADProperty[normalCount + expertCount];
                normalProps.toArray(beanProperties2);
            }
            else beanProperties2 = new RADProperty[0];
        }
        else {
            beanProperties1 = new RADProperty[normalCount];
            normalProps.toArray(beanProperties1);
            if (expertCount > 0) {
                beanProperties2 = new RADProperty[expertCount];
                expertProps.toArray(beanProperties2);
            }
            else beanProperties2 = new RADProperty[0];
        }
        
        Map<Object,FormProperty[]> otherProps2 = new TreeMap<Object,FormProperty[]>();
        FormProperty[] array = new FormProperty[0];
        for (Map.Entry<Object,List<FormProperty>> entry : otherProps.entrySet()) {
            List<FormProperty> catProps = entry.getValue();
            otherCount += catProps.size();
            otherProps2.put(entry.getKey(), catProps.toArray(array));
        }
        otherProperties = otherProps2;
        
        FormUtils.reorderProperties(beanClass, beanProperties1);
        FormUtils.reorderProperties(beanClass, beanProperties2);

        knownBeanProperties = new RADProperty[beanProperties1.length
                              + beanProperties2.length + otherCount];
        System.arraycopy(beanProperties1, 0,
                         knownBeanProperties, 0,
                         beanProperties1.length);
        System.arraycopy(beanProperties2, 0,
                         knownBeanProperties, beanProperties1.length,
                         beanProperties2.length);
        
        int where = beanProperties1.length + beanProperties2.length;
        
        for (Map.Entry<Object,FormProperty[]> entry : otherProperties.entrySet()) {
            RADProperty[] catProps = (RADProperty[])entry.getValue();
            System.arraycopy(catProps, 0,
                knownBeanProperties, where,
                catProps.length);
            where += catProps.length;
        }

        actionProperties = actionProps;
    }

    private void createBindingProperties() {
        Collection<BindingDescriptor>[] props = FormEditor.getBindingSupport(formModel).getBindingDescriptors(this);
        bindingProperties = new BindingProperty[props.length][];
        for (int i=0; i<props.length; i++) {
            bindingProperties[i] = new BindingProperty[props[i].size()];
            int j = 0;
            for (BindingDescriptor desc : props[i]) {
                bindingProperties[i][j++] = new BindingProperty(this, desc);
            }
        }       
    }

    private void createEventProperties() {
        EventSetDescriptor[] eventSets = getBeanInfo().getEventSetDescriptors();

        List<EventProperty> eventPropList = new ArrayList<EventProperty>(eventSets.length * 5);

        for (int i=0; i < eventSets.length; i++) {
            EventSetDescriptor desc = eventSets[i];
            Method[] methods = desc.getListenerMethods();
            for (int j=0; j < methods.length; j++) {
                String eventId = FormEvents.getEventIdName(methods[j]);
                Object prop = nameToProperty.get(eventId);
                assert (prop == null) || (prop instanceof EventProperty);
                EventProperty eventProp = (EventProperty)prop;
                if (eventProp == null)
                    eventProp = createEventProperty(eventId, desc, methods[j]);
                eventPropList.add(eventProp);
            }
        }

        EventProperty[] eventProps = new EventProperty[eventPropList.size()];
        eventPropList.toArray(eventProps);

        knownEvents = new Event[eventProps.length];
        for (int i=0; i < eventProps.length; i++)
            knownEvents[i] = eventProps[i].getEvent();

        FormUtils.sortProperties(eventProps);
        eventProperties = eventProps;
    }

    protected RADProperty createBeanProperty(PropertyDescriptor desc,
                                             Object[] propAccessClsf,
                                             Object[] propParentChildDepClsf)
    {
        if (desc.getPropertyType() == null)
            return null;

        RADProperty prop = null;
        if(desc instanceof FakePropertyDescriptor){
            try {
                prop = new FakeRADProperty(this, (FakePropertyDescriptor) desc);   
            } catch (IntrospectionException ex) { // should not happen
                ex.printStackTrace();
		return null;
            }             
        } else {
            prop = new RADProperty(this, desc);
        }

	int access = FormUtils.getPropertyAccess(desc, propAccessClsf);
	if (access != 0)
	    prop.setAccessType(access);

        String parentChildDependency = FormUtils.getPropertyParentChildDependency(
                                         desc, propParentChildDepClsf);
        if (parentChildDependency != null)
            prop.setValue(parentChildDependency, Boolean.TRUE);

	setPropertyListener(prop);

	// prop.addPropertyChangeListener(getPropertyListener());
	nameToProperty.put(desc.getName(), prop);

        return prop;
    }
 
    private EventProperty createEventProperty(String eventId,
                                                EventSetDescriptor eventDesc,
                                                Method eventMethod)
    {
        EventProperty prop = new EventProperty(new Event(this,
                                                         eventDesc,
                                                         eventMethod),
                                               eventId);
        nameToProperty.put(eventId, prop);
        return prop;
    }

    /** Called to modify original bean properties obtained from BeanInfo.
     * Properties may be added, removed etc. - due to specific needs.
     */
    protected void changePropertiesExplicitly(List<FormProperty> prefProps,
                                              List<FormProperty> normalProps,
                                              List<FormProperty> expertProps) {
         // hack for buttons - add fake property for ButtonGroup
        if (getBeanInstance() instanceof javax.swing.AbstractButton) {
            try {
                RADProperty prop = new ButtonGroupProperty(this);
                setPropertyListener(prop);
//                prop.addPropertyChangeListener(getPropertyListener());
                nameToProperty.put(prop.getName(), prop);

                Object propCategory = FormUtils.getPropertyCategory(
                    prop.getPropertyDescriptor(),
                    FormUtils.getPropertiesCategoryClsf(
                        beanClass, getBeanInfo().getBeanDescriptor()));

                if (propCategory == FormUtils.PROP_PREFERRED)
                    prefProps.add(prop);
                else normalProps.add(prop);
            }
            catch (IntrospectionException ex) {} // should not happen
        } else if (getBeanInstance() instanceof javax.swing.JTable) {
            try {
                PropertyDescriptor pd = new PropertyDescriptor("rowHeight", javax.swing.JTable.class); // NOI18N
                RADProperty prop = createBeanProperty(pd, null, null);
                nameToProperty.put("rowHeight", prop); // NOI18N
                normalProps.add(prop);
            } catch (IntrospectionException ex) {
                ex.printStackTrace();
            }
        }
        
        // PENDING improve performance - keep lookup result, listen on it etc.
        boolean modified = false;
        Lookup.Template<PropertyModifier> template = new Lookup.Template<PropertyModifier>(PropertyModifier.class);
        Collection<? extends PropertyModifier> modifiers = Lookup.getDefault().lookup(template).allInstances();
        for (PropertyModifier modifier : modifiers) {
            modified |= modifier.modifyProperties(this, prefProps, normalProps, expertProps);
        }
        
        if (modified) {
            checkForAddedProperties(prefProps);
            checkForAddedProperties(normalProps);
            checkForAddedProperties(expertProps);
        }
    }
    
    private void checkForAddedProperties(List props) {
        for (Object o : props) {
            FormProperty prop = (FormProperty)o;
            String propName = prop.getName();
            if (!nameToProperty.containsKey(propName)) {
                nameToProperty.put(propName, prop);
                setPropertyListener(prop);
            }
        }
    }

    protected PropertyChangeListener createPropertyListener() {
        return new PropertyListenerConvertor();
    }

    protected void setPropertyListener(FormProperty property) {
        if (propertyListener == null)
            propertyListener = createPropertyListener();
        if (propertyListener != null) {
            property.addPropertyChangeListener(propertyListener);
            if (propertyListener instanceof FormProperty.ValueConvertor)
                property.addValueConvertor((FormProperty.ValueConvertor)propertyListener);
        }
    }

    /** Listener class for listening to changes in component's properties.
     */
    private class PropertyListenerConvertor implements PropertyChangeListener, FormProperty.ValueConvertor {
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof FormProperty))
                return;

            FormProperty property = (FormProperty) source;
            String propName = property.getName();
            String eventName = evt.getPropertyName();

            if (FormProperty.PROP_VALUE.equals(eventName)
                || FormProperty.PROP_VALUE_AND_EDITOR.equals(eventName))
            {   // property value has changed (or value and editor together)
                resourcePropertyChanged(evt);

                Object oldValue = evt.getOldValue();
                Object newValue = evt.getNewValue();
                formModel.fireComponentPropertyChanged(
                              RADComponent.this, propName, oldValue, newValue);

                if (getNodeReference() != null) { // propagate the change to node
                    if (FormProperty.PROP_VALUE_AND_EDITOR.equals(eventName)) {
                        oldValue = ((FormProperty.ValueWithEditor)oldValue).getValue();
                        newValue = ((FormProperty.ValueWithEditor)newValue).getValue();
                    }

                    getNodeReference().firePropertyChangeHelper(
//                                                null, null, null);
                                           propName, oldValue, newValue);
                }
            }
            else if (FormProperty.CURRENT_EDITOR.equals(eventName)) {
                // property editor has changed - don't fire to FormModel,
                // only to component node
                if (getNodeReference() != null)
                    getNodeReference().firePropertyChangeHelper(
                                            propName, null, null);
            }
        }

        public Object convert(Object value, FormProperty property) {
            return resourcePropertyConvert(value, property);
        }
    }

    // -----
    // resources/i18n automation

    Object resourcePropertyConvert(Object value, FormProperty property) {
        if (isInModel() && formModel.isUndoRedoRecording()
                && property.getPropertyContext().getFormModel() != null) {
            Object resVal = ResourceSupport.makeResource(value, property);
            if (resVal != value)
                return resVal;
        }
        return value; // do nothing
    }

    void resourceComponentRename(String oldName, String newName) {
        if (isInModel()) {
            ResourceSupport.componentRenamed(this, oldName, newName);
        }
    }

    void resourcePropertyChanged(PropertyChangeEvent ev) {
        if (isInModel() && formModel.isFormLoaded()) {
            ResourceSupport.updateStoredValue(
                    FormProperty.getEnclosedValue(ev.getOldValue()), // in case it is ValueWithEditor
                    FormProperty.getEnclosedValue(ev.getNewValue()), // in case it is ValueWithEditor
                    (FormProperty)ev.getSource());
        }
    }

    // ----------

    private static class PropertyIterator implements java.util.Iterator {
        private FormProperty[] properties;
        private FormProperty.Filter filter;

        private FormProperty next;
        private int index;

        PropertyIterator(FormProperty[] properties,
                         FormProperty.Filter filter)
        {
            this.properties = properties;
            this.filter = filter;
        }

        public boolean hasNext() {
            if (next == null)
                next = getNextProperty();
            return next != null;
        }

        public Object next() {
            if (next == null)
                next = getNextProperty();
            if (next != null) {
                Object prop = next;
                next = null;
                return prop;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private FormProperty getNextProperty() {
            while (index < properties.length) {
                FormProperty prop = properties[index++];
                if (filter.accept(prop))
                    return prop;
            }
            return null;
        }
    }

    // -----------------------------------------------------------------------------
    // Debug methods

    @Override
    public String toString() {
        return super.toString() + ", name: "+getName()+", class: "+getBeanClass()+", beaninfo: "+getBeanInfo() + ", instance: "+getBeanInstance(); // NOI18N
    }

    public void debugChangedValues() {
    }

    // ----------
    // a reference to a metacomponent - used instead of a metacomponent, may
    // become invalid when the component is removed

    interface ComponentReference {
        RADComponent getComponent();
    }

    // ------------------------------------
    // some hacks for ButtonGroup "component" ...

    // pseudo-property for buttons - holds ButtonGroup in which button
    // is placed; kind of "reversed" property
    static class ButtonGroupProperty extends RADProperty {
        ButtonGroupProperty(RADComponent comp) throws IntrospectionException {
            super(comp,
                  new FakePropertyDescriptor("buttonGroup", // NOI18N
                                             javax.swing.ButtonGroup.class));
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            setShortDescription(FormUtils.getBundleString("HINT_ButtonGroup")); // NOI18N
        }

        @Override
        public boolean supportsDefaultValue() {
            return true;
        }

        @Override
        public Object getDefaultValue() {
            return null;
        }

        @Override
        public PropertyEditor getExpliciteEditor() {
            return new ButtonGroupPropertyEditor();
        }

        @Override
        String getWholeSetterCode(String groupName) {
            return groupName != null ?
                groupName + ".add(" + getRADComponent().getName() + ");" : // NOI18N
                null;
        }
    }

    // property editor for selecting ButtonGroup (for ButtonGroupProperty)
    public static class ButtonGroupPropertyEditor extends ComponentChooserEditor {
        public ButtonGroupPropertyEditor() {
            super();
            setBeanTypes(new Class[] { javax.swing.ButtonGroup.class });
            setComponentCategory(NONVISUAL_COMPONENTS);
        }
        
        @Override
        public String getDisplayName() {
            return NbBundle.getBundle(getClass()).getString("CTL_ButtonGroupPropertyEditor_DisplayName"); // NOI18N
        }
    }
    
    public void setValid(boolean valid){
        this.valid = valid;
    }
    
    protected boolean isValid() {
        return valid;
    }
    
    private class FakeBeanInfo extends SimpleBeanInfo {
        
        private List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
        
        @Override
        public BeanDescriptor getBeanDescriptor() {
            return (beanInfo == this) ? null : beanInfo.getBeanDescriptor();            
        }

        @Override
        public PropertyDescriptor[] getPropertyDescriptors() {
            return propertyDescriptors.toArray(new PropertyDescriptor[propertyDescriptors.size()]);
        }

        @Override
        public EventSetDescriptor[] getEventSetDescriptors() {            
	    return new EventSetDescriptor[0];
        }        

        void addPropertyDescriptor(String propertyName, Class propertyClass) {
            try {
                propertyDescriptors.add(new FakePropertyDescriptor(propertyName, propertyClass));
            } catch (IntrospectionException ex) {
                ex.printStackTrace(); // should not happen
            }
        }
                
        void addPropertyDescriptor(PropertyDescriptor pd) {
            propertyDescriptors.add(pd);
        }
        
        void removePropertyDescriptors() {
            propertyDescriptors.clear();
        }
    }
}
