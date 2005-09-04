/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.beans.*;
import java.util.*;
import java.lang.reflect.Method;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.datatransfer.NewType;

import org.netbeans.modules.form.codestructure.*;

/**
 *
 * @author Ian Formanek
 */

public class RADComponent /*implements FormDesignValue, java.io.Serializable*/ {

    // -----------------------------------------------------------------------------
    // Static variables

//    public static final String SYNTHETIC_PREFIX = "synthetic_"; // NOI18N
//    public static final String PROP_NAME = SYNTHETIC_PREFIX + "Name"; // NOI18N
    public static final String PROP_NAME = "variableName"; // NOI18N

    static final NewType[] NO_NEW_TYPES = {};
    static final RADProperty[] NO_PROPERTIES = {};

    // -----------------------------------------------------------------------------
    // Private variables

    private static int idCounter;

    private String id = Integer.toString(++idCounter);

    private Class beanClass;
    private Object beanInstance;
    private BeanInfo beanInfo;
//    private String componentName;

//    private boolean readOnly;

    protected Node.PropertySet[] propertySets;
    private Node.Property[] syntheticProperties;
    private RADProperty[] beanProperties1;
    private RADProperty[] beanProperties2;
    private EventProperty[] eventProperties;
    private Map otherProperties;
    private List actionProperties;

    private RADProperty[] knownBeanProperties;
    private Event[] knownEvents; // must be grouped by EventSetDescriptor

    private PropertyChangeListener propertyListener;

    private Map auxValues;
    protected Map nameToProperty;

    private RADComponent parentComponent;

    private FormModel formModel;
    private boolean inModel;

    private RADComponentNode componentNode;

    private CodeExpression componentCodeExpression;

//    private String gotoMethod;

    private String storedName; // component name preserved e.g. for remove undo

    // -----------------------------------------------------------------------------
    // Constructors & Initialization

    /** Called to initialize the component with specified FormModel.
     * @param formModel the FormModel of the form into which this component
     * will be added 
     */
    public boolean initialize(FormModel formModel) {
        if (this.formModel == null) {
            this.formModel = formModel;
//            readOnly = formModel.isReadOnly();

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
    public Object initInstance(Class beanClass) throws Exception {
        if (beanClass == null)
            throw new NullPointerException();

        if (this.beanClass != beanClass && this.beanClass != null) {
            beanInfo = null;
            clearProperties();
        }

        this.beanClass = beanClass;

        Object bean = createBeanInstance();
        getBeanInfo(); // force BeanInfo creation here - will be needed, may fail
        createCodeExpression();
        setBeanInstance(bean);

        return beanInstance;
    }

    /** Sets the bean instance represented by this meta component.
     * The meta component is fully initialized after this method returns.
     * @param beanInstance the bean to be represented by this meta component
     */
    public void setInstance(Object beanInstance) {
        if (this.beanClass != beanInstance.getClass())
            beanInfo = null;
        clearProperties();

        this.beanClass = beanInstance.getClass();

        getBeanInfo(); // force BeanInfo creation here - will be needed, may fail
        createCodeExpression();
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
//    throws InstantiationException, IllegalAccessException
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

    void setNodeReference(RADComponentNode node) {
        this.componentNode = node;
    }

    protected void createCodeExpression() {
        if (componentCodeExpression == null) {
            CodeStructure codeStructure = formModel.getCodeStructure();
            componentCodeExpression = codeStructure.createExpression(
                                   FormCodeSupport.createOrigin(this));
            codeStructure.registerExpression(componentCodeExpression);

            if (formModel.getTopRADComponent() != this)
                formModel.getCodeStructure().createVariableForExpression(
                                               componentCodeExpression,
                                               0x30DF, // default type
                                               storedName);
        }
    }

    final void removeCodeExpression() {
        if (componentCodeExpression != null) {
            CodeVariable var = componentCodeExpression.getVariable();
            if (var != null)
                storedName = var.getName();
            CodeStructure.removeExpression(componentCodeExpression);
        }
    }

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

    public final String getId() {
        return id;
    }

    public final boolean isReadOnly() {
        return formModel.isReadOnly(); //readOnly;
    }

    /** Provides access to the Class of the bean represented by this RADComponent
     * @return the Class of the bean represented by this RADComponent
     */
    public final Class getBeanClass() {
        return beanClass;
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

//    /** FormDesignValue implementation.
//     * @return description of the design value.
//     */
//    public String getDescription() {
//        return getName();
//    }
//
//    /**  FormDesignValue implementation.
//     * Provides a value which should be used during design-time as real value
//     * of the property (in case that RADComponent is used as property value).
//     * @return the bean instance of RADComponent
//     */
//    public Object getDesignValue() {
//        return getBeanInstance();
//    }

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
        if (beanInfo == null)
            beanInfo = BeanSupport.createBeanInfo(beanClass);
        return beanInfo;
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
        if (componentCodeExpression == null)
            return;

        CodeVariable var = componentCodeExpression.getVariable();
        if (var == null || name.equals(var.getName()))
            return;
        // [maybe we should handle the component name differently if there is
        //  no variable for the component]

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

        String oldName = var.getName();

        formModel.getCodeStructure().renameVariable(oldName, name);

        renameDefaultEventHandlers(oldName, name);
        // [possibility of renaming default event handlers should be probably
        // configurable in options]

        formModel.fireSyntheticPropertyChanged(this, PROP_NAME,
                                               oldName, name);

        if (getNodeReference() != null)
            getNodeReference().updateName();
    }

    void setStoredName(String name) {
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

/*
    / ** Restore name of component. If stored name is already in use or is
     * null then create a new name. * /
    void useStoredName() {
        if (storedName == null && componentName != null
            //&& !formModel.getVariablePool().isReserved(componentName)
            ) {
            formModel.getVariablePool().reserveName(componentName);
            return;
        }
        
        String oldName = componentName;
        componentName = storedName;

        if (storedName == null || formModel.getVariablePool().isReserved(storedName)) {
            componentName = formModel.getVariablePool().getNewName(beanClass);
        }
        
        formModel.getVariablePool().createVariable(componentName, beanClass);

//        formModel.fireFormChanged();
        
        if (getNodeReference() != null) {
            getNodeReference().updateName();
        }
    }

    / ** @return component name preserved between Cut and Paste * /
    String getStoredName() {
        return storedName;
    }

    / ** Can be called to store the component name into special variable to preserve it between Cut and Paste * /
    void storeName() {
        storedName = componentName;
    }
*/

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
            auxValues = new TreeMap();
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

    final void setInModel(boolean in) {
        inModel = in;
        formModel.updateMapping(this, in);
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
            ArrayList propSets = new ArrayList(5);
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
                                              Class propertyType,
                                              boolean fromAll)
    {
        Node.Property prop = (Node.Property) nameToProperty.get(name);
        if (prop == null && fromAll) {
            if (beanProperties1 == null && !name.startsWith("$")) // NOI18N
                createBeanProperties();
            if (eventProperties == null && name.startsWith("$")) // NOI18N
                createEventProperties();

            prop = (Node.Property) nameToProperty.get(name);
        }
        return prop != null
                 && (propertyType == null
                     || propertyType.isAssignableFrom(prop.getClass())) ?
               prop : null;
    }

    public Node.Property getPropertyByName(String name) {
        return getPropertyByName(name, null, true);
    }

    public final RADProperty getBeanProperty(String name) {
        return (RADProperty) getPropertyByName(name, RADProperty.class, true);
    }

    public RADProperty[] getBeanProperties(String[] propNames) {
        RADProperty[] properties = new RADProperty[propNames.length];
        PropertyDescriptor[] descriptors = null;

        boolean empty = knownBeanProperties == null;
        int validCount = 0;
        List newProps = null;
        Object[] propAccessClsf = null;

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
                        if (propAccessClsf == null)
                            propAccessClsf = FormUtils.getPropertiesAccessClsf(beanClass);

                        prop = createBeanProperty(descriptors[j], propAccessClsf);

                        if (!empty) {
                            if (newProps == null)
                                newProps = new ArrayList();
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
                knownProps[i + knownBeanProperties.length] = (RADProperty)
                                                             newProps.get(i);

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
        List newEvents = null;

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
                                        newEvents = new ArrayList();
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
                known[i + knownEvents.length] = (Event) newEvents.get(i);

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
        else nameToProperty = new HashMap();

        propertySets = null;
        syntheticProperties = null;
        beanProperties1 = null;
        beanProperties2 = null;
        knownBeanProperties = null;
        eventProperties = null;
        knownEvents = null;
    }

    protected void createPropertySets(List propSets) {
        if (beanProperties1 == null)
            createBeanProperties();

        ResourceBundle bundle = FormUtils.getBundle();

        propSets.add(new Node.PropertySet(
                "properties", // NOI18N
                bundle.getString("CTL_PropertiesTab"), // NOI18N
                bundle.getString("CTL_PropertiesTabHint")) // NOI18N
        {
            public Node.Property[] getProperties() {
                return getBeanProperties1();
            }
        });

        Node.PropertySet ps;
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

        if (beanProperties2.length > 0)
            propSets.add(new Node.PropertySet(
                    "properties2", // NOI18N
                    bundle.getString("CTL_Properties2Tab"), // NOI18N
                    bundle.getString("CTL_Properties2TabHint")) // NOI18N
            {
                public Node.Property[] getProperties() {
                    return getBeanProperties2();
                }
            });

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
        return FormEditor.getCodeGenerator(formModel).getSyntheticProperties(this);
    }

    private void createBeanProperties() {
        ArrayList prefProps = new ArrayList();
        ArrayList normalProps = new ArrayList();
        ArrayList expertProps = new ArrayList();
        Map otherProps = new TreeMap();
        List actionProps = new LinkedList();

        Object[] propsCats = FormUtils.getPropertiesCategoryClsf(
                                 beanClass, getBeanInfo().getBeanDescriptor());
        Object[] propsAccess = FormUtils.getPropertiesAccessClsf(beanClass);

        PropertyDescriptor[] props = getBeanInfo().getPropertyDescriptors();
        for (int i = 0; i < props.length; i++) {
            PropertyDescriptor pd = props[i];
            boolean action = (pd.getValue("action") != null); // NOI18N
            Object category = pd.getValue("category"); // NOI18N
            List listToAdd;
            
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
                listToAdd = (List)otherProps.get(category);
                if (listToAdd == null) {
                    listToAdd = new ArrayList();
                    otherProps.put(category, listToAdd);
                }
            }
            
            FormProperty prop = (FormProperty) nameToProperty.get(pd.getName());
            if (prop == null)
                prop = createBeanProperty(pd, propsAccess);

            if (prop != null) {
                listToAdd.add(prop);
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
        
        Iterator entries = otherProps.entrySet().iterator();
        RADProperty[] array = new RADProperty[0];
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry)entries.next();
            ArrayList catProps = (ArrayList)entry.getValue();
            otherCount += catProps.size();
            entry.setValue(catProps.toArray(array));
        }
        otherProperties = otherProps;
        
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
        
        entries = otherProperties.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry)entries.next();
            RADProperty[] catProps = (RADProperty[])entry.getValue();
            System.arraycopy(catProps, 0,
                knownBeanProperties, where,
                catProps.length);
            where += catProps.length;
        }

        actionProperties = actionProps;
    }

    private void createEventProperties() {
        EventSetDescriptor[] eventSets = getBeanInfo().getEventSetDescriptors();

        List eventPropList = new ArrayList(eventSets.length * 5);

        for (int i=0; i < eventSets.length; i++) {
            EventSetDescriptor desc = eventSets[i];
            Method[] methods = desc.getListenerMethods();
            for (int j=0; j < methods.length; j++) {
                String eventId = FormEvents.getEventIdName(methods[j]);
                Object prop = nameToProperty.get(eventId);
                if (prop == null)
                    prop = createEventProperty(eventId, desc, methods[j]);
                eventPropList.add(prop);
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
                                             Object[] propAccessClsf)
    {
        if (desc.getPropertyType() == null)
            return null;

        RADProperty prop = new RADProperty(this, desc);

        int access = FormUtils.getPropertyAccess(desc, propAccessClsf);
        if (access != 0)
            prop.setAccessType(access);

        setPropertyListener(prop);
//        prop.addPropertyChangeListener(getPropertyListener());
        nameToProperty.put(desc.getName(), prop);

        return prop;
    }

    protected EventProperty createEventProperty(String eventId,
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
    protected void changePropertiesExplicitly(List prefProps,
                                              List normalProps,
                                              List expertProps) {
         // hack for buttons - add fake property for ButtonGroup
        if (getBeanInstance() instanceof javax.swing.AbstractButton)
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
    }

    protected PropertyChangeListener createPropertyListener() {
        return new PropertyListener();
    }

    protected void setPropertyListener(FormProperty property) {
        if (propertyListener == null)
            propertyListener = createPropertyListener();
        if (propertyListener != null)
            property.addPropertyChangeListener(propertyListener);
    }
//    protected PropertyChangeListener getPropertyListener() {
//        if (propertyListener == null)
//            propertyListener = createPropertyListener();
//        return propertyListener;
//    }

    /** Listener class for listening to changes in component's properties.
     */
    protected class PropertyListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof FormProperty))
                return;

            String propName = ((FormProperty)source).getName();
            String eventName = evt.getPropertyName();

            if (FormProperty.PROP_VALUE.equals(eventName)
                || FormProperty.PROP_VALUE_AND_EDITOR.equals(eventName))
            {   // property value has changed (or value and editor together)
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

    // ----------
/*
    // Serialization of RADComponent was made for copy/pasting - JDK on
    // Mac OS X used to force the serialization for the transferable object
    // though it could just keep the instance (copied within one JVM) - as it
    // works normally on other OSes. Issue 12050.

    Object writeReplace() {
        return new Replace(this);
    }

    private static class Replace implements java.io.Serializable {
        private FormDataObject dobj;
        private String compId;

        Replace(RADComponent comp) {
            // reference to RADComponent is stored; we expect the form
            // containing the component remains opened all the time
            dobj = FormEditorSupport.getFormDataObject(comp.getFormModel());
            compId = comp.getId();
        }

        Object readResolve() { // throws java.io.ObjectStreamException
            FormModel[] forms = FormEditorSupport.getOpenedForms();
            for (int i=0; i < forms.length; i++) {
                FormModel form = forms[i];
                if (dobj.equals(FormEditorSupport.getFormDataObject(form)))
                    return form.getMetaComponent(compId);
            }
            return null; // or throw some exception?
        }
    }
*/

    // -----------------------------------------------------------------------------
    // Debug methods

    public java.lang.String toString() {
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

        public boolean supportsDefaultValue() {
            return true;
        }

        public Object getDefaultValue() {
            return null;
        }

        public PropertyEditor getExpliciteEditor() {
            return new ButtonGroupPropertyEditor();
        }

        String getWholeSetterCode() {
            String groupName = getJavaInitializationString();
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
            setComponentCategory(OTHER_COMPONENTS);
        }
    }
}
