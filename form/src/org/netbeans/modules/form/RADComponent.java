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

/* $Id$ */

package org.netbeans.modules.form;

import org.openide.explorer.propertysheet.editors.*;
import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.InstanceDataObject;

import java.beans.*;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class RADComponent implements FormDesignValue {

    // -----------------------------------------------------------------------------
    // Static variables

    public static final String SYNTHETIC_PREFIX = "synthetic_"; // NOI18N
    public static final String PROP_NAME = SYNTHETIC_PREFIX + "Name"; // NOI18N

    static final NewType[] NO_NEW_TYPES = {};
    static final Node.Property[] NO_PROPERTIES = {};

    // -----------------------------------------------------------------------------
    // Private variables

    private RADComponentNode componentNode;

    private Class beanClass;
    private Object beanInstance;
    private BeanInfo beanInfo;
    private String componentName;

    private boolean readOnly;

    protected Node.PropertySet[] beanPropertySets;
    private Node.Property[] syntheticProperties;
    private Node.Property[] beanProperties;
    private Node.Property[] beanProperties2;
    private Node.Property[] beanEvents;
    private RADProperty[] allProperties;

    private PropertyChangeListener propertyListener;

    private HashMap auxValues;
    protected HashMap nameToProperty;

    private FormModel formModel;
    private ComponentEventHandlers eventsList;
//    private String gotoMethod;

    private String storedName; // component name preserved between Cut and Paste

    // -----------------------------------------------------------------------------
    // Constructors & Initialization

    /** Creates a new RADComponent */
    public RADComponent() {
        auxValues = new HashMap(10);
    }

    /** Called to initialize the component with specified FormModel.
     * @param formModel the FormModel of the form into which this component
     * will be added 
     */
    public void initialize(FormModel formModel) {
        this.formModel = formModel;
        readOnly = formModel.isReadOnly();
    }

    /** Initializes the bean represented by this RADComponent.
     * InstanceCookie is used for creating instance (unless it is InstanceDataObject).
     * If the instance is already available, call setInstance(...) instead.
     */
    public Object initInstance(InstanceCookie ic)
    throws InstantiationException, IllegalAccessException {
        try {
            if (ic instanceof InstanceDataObject)
                setComponent(ic.instanceClass());
            else {
                Object instance = ic.instanceCreate();
                setInstance(instance);
            }
        }
        catch (ClassNotFoundException e1) {
            throw new InstantiationException(e1.getMessage());
        }
        catch (java.io.IOException e2) {
            throw new InstantiationException(e2.getMessage());
        }
        return beanInstance;
    }

    /** Called to set the bean to be represented by this RADComponent.
     * This method creates a new instance of the bean. This RADComponent class
     * is fully initialized after this method returns. Can be called only once
     * and is mutually exclusive with setInstance().
     * @param beanClass the class of the bean to be represented by this class
     * @see #setInstance
     */
    public void setComponent(Class beanClass) {
        if (this.beanClass != null) {
            throw new IllegalStateException("Component already initialized; current: "+this.beanClass +", new: "+beanClass);// NOI18N
        }

        nameToProperty = new HashMap();

        this.beanClass = beanClass;
        setBeanInstance(createBeanInstance());
//        beanInstance = createBeanInstance();
        beanInfo = null;

        // properties will be created on first request
        syntheticProperties = null;
        beanProperties = null;
        beanProperties2 = null;

        eventsList = null;
        beanEvents = null;
    }

    /**
     * Called to set the bean to be represented by this RADComponent.  This
     * method uses the instance provided. This RADComponent class is fully
     * initialized after this method returns.  Can be called only once and is
     * mutually exclusive with setComponent()
     * @param beanInstance the bean to be represented by this class
     * @see #setComponent
     */
    public void setInstance(Object beanInstance) {
// XXX(-tdt) see xformcvt.Import...
//          
//          if (this.beanClass != null) {
//              throw new InternalError("Component already initialized: current: "+this.beanClass +", new: "+beanClass); // NOI18N
//          }
        nameToProperty = new HashMap();

        beanClass = beanInstance.getClass();
        setBeanInstance(beanInstance);
//        this.beanInstance = beanInstance;
//        beanInfo = BeanSupport.createBeanInfo(beanClass);


//        createBeanProperties();

        // other properties and events will be created on first request
        syntheticProperties = null;
        eventsList = null;
        beanEvents = null;

//        PropertyDescriptor[] props = getBeanInfo().getPropertyDescriptors();
        RADProperty[] props = getAllBeanProperties();
        for (int i = 0; i < props.length; i++) {
            if (!FormUtils.isIgnoredProperty(beanClass, props[i].getName())) {
                try {
                    props[i].reinstateProperty();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    // [PENDING] notify exception?
                    // simply ignore this property
                }
            }
            // ignore some properties (why?)
/***********************
            // xformcvt import, I must ignore preferredSize, maximumSize and minimumSize

            if ("preferredSize".equals(props[i].getName())
                || "maximumSize".equals(props[i].getName())
                || "minimumSize".equals(props[i].getName())
                ) {
                continue;
            }
**********************/
        }
    }

    /** Updates the bean instance - e.g. when setting a property
     * requires to re-create the bean.
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
     * Called to create the instance of the bean. Default implementation
     * simply creates instance of the bean's class using the default
     * constructor.  Top-level container (the form object itself) will redefine
     * this to use FormInfo to create the instance, as e.g. Dialogs cannot be
     * created using the default constructor.  Note: this method is called only
     * if the setComponent method is used, if setInstance is used, no new
     * instance is created.
     * @return the instance of the bean that will be used during design time 
     */
    protected Object createBeanInstance() {
        return BeanSupport.createBeanInstance(beanClass);
    }

    /** Sets directly the bean instance. Can be overriden - e.g. when some
     * other instance of another metacomponent needs to be modified too.
     */
    protected void setBeanInstance(Object beanInstance) {
        this.beanInstance = beanInstance;
    }

    /** Used by TuborgPersistenceManager */
    void initDeserializedEvents(java.util.Hashtable eventHandlers) {
        getComponentEvents();
        eventsList.initEvents(eventHandlers);
    }

    void setNodeReference(RADComponentNode node) {
        this.componentNode = node;
    }

    // -----------------------------------------------------------------------------
    // Public interface

    public final boolean isReadOnly() {
        return readOnly;
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

    /** FormDesignValue implementation.
     * @return description of the design value.
     */
    public String getDescription() {
        return getName();
    }

    /**  FormDesignValue implementation.
     * Provides a value which should be used during design-time as real value
     * of the property (in case that RADComponent is used as property value).
     * @return the bean instance of RADComponent
     */
    public Object getDesignValue() {
        return getBeanInstance();
    }

    public Object cloneBeanInstance() {
        Object clone = createBeanInstance();
//        try {
            setProps(clone, getAllBeanProperties());
//        }
//        catch (Exception ex) {
//            ex.printStackTrace(); // XXX
//        }
        return clone;
    }

    static void setProps(Object bean, Node.Property[] props) {//throws Exception {
        for (int i = 0; i < props.length; i++) {
            RADProperty prop = (RADProperty) props[i];
            if (!prop.isChanged())
                continue;

            try {
                Object value = prop.getRealValue();
                if (value == FormDesignValue.IGNORED_VALUE)
                    continue; // ignore this value, as it is not a real value

                Method writeMethod = prop.getPropertyDescriptor().getWriteMethod();
                if (writeMethod != null)
                    writeMethod.invoke(bean, new Object[] { value });
            }
            catch (Exception ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
            }
        }
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
        String name = getBeanClass().getName();
        if (name.startsWith("java"))
            return false;
        else if (name.startsWith("org.")) {
            int idx = name.indexOf('.', 4);
            if (idx < 0) {
                idx = name.length();
            }
            name = name.substring(4, idx);
            if (name.equals("netbeans") || name.equals("openide"))
                return false;
        }
        return getBeanInfo().getBeanDescriptor().getValue("hidden-state") != null; // NOI18N
    }

    /** Getter for the Name of the component - usually maps to variable
     * declaration for holding the instance of the component
     * @return current value of the Name property
     */
    public String getName() {
        return componentName;
    }

    /** Setter for the name of the component - it is the name of component's
     * node and the name of variable declaration for the component in generated code.
     * @param value new name of the component
     */
    public void setName(String value) {
        if (componentName != null && componentName.equals(value))
            return; // the same name => no change
        if (formModel.getVariablePool().findVariableType(value) != null)
            return; // variable of the name already exists => ignore (user??)
        if (!org.openide.util.Utilities.isJavaIdentifier(value))
            return; // invalid name => ignore (user??)

        String oldName = componentName;
        componentName = value;

        if (oldName != null)
            formModel.getVariablePool().deleteVariable(oldName);
        formModel.getVariablePool().createVariable(componentName, beanClass);

        if (oldName != null)
            renameDefaultEventHandlers(oldName, componentName);
        // [PENDING] renaming of default event handlers could be in global options...

        formModel.fireComponentPropertyChanged(this,PROP_NAME,oldName,componentName);
//        formModel.fireFormChanged();
        
        if (getNodeReference() != null) {
            getNodeReference().updateName();
        }
    }

    void renameDefaultEventHandlers(String oldName, String newName) {
        boolean renamed = false; // whether any defualt handler was renamed
        EventSet[] esets = getEventHandlers().getEventSets();
        for (int i=0; i < esets.length; i++) {
            Event [] evts = esets[i].getEvents();

            for (int j=0; j < evts.length; j++) {
                String defaultName = FormUtils.getDefaultEventName(
                    oldName, evts[j].getListenerMethod());

                Iterator iter = evts[j].getHandlers().iterator();
                while (iter.hasNext()) {
                    EventHandler eh = (EventHandler) iter.next();
                    if (eh.getName().equals(defaultName)) {
                        String newValue = FormUtils.getDefaultEventName(newName, evts[j].getListenerMethod());
                        formModel.getFormEventHandlers().renameEventHandler(eh, newValue);
                        renamed = true;
                        break;
                    }
                }
            }
        }

        if (renamed && getNodeReference() != null) {
            getNodeReference().fireComponentPropertySetsChange();
            formModel.fireFormChanged();
        }
    }

    /** Restore name of component. If stored name is already in use or is
     * null then create a new name. */
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

    /** @return component name preserved between Cut and Paste */
    String getStoredName() {
        return storedName;
    }

    /** Can be called to store the component name into special variable to preserve it between Cut and Paste */
    void storeName() {
        storedName = componentName;
    }

    /** Allows to add an auxiliary <name, value> pair, which is persistent
     * in Gandalf. The current value can be obtained using
     * getAuxValue(aux_property_name) method. To remove aux value for specified
     * property name, use setAuxValue(name, null).
     * @param key name of the aux property
     * @param value new value of the aux property or null to remove it
     */
    public void setAuxValue(String key, Object value) {
        auxValues.put(key, value);
    }

    /** Allows to obtain an auxiliary value for specified aux property name.
     * @param key name of the aux property
     * @return null if the aux value for specified name is not set
     */
    public Object getAuxValue(String key) {
        return auxValues.get(key);
    }

    /** Provides access to the FormModel class which manages the form in which
     * this component has been added.
     * @return the FormModel which manages the form into which this component
     *         has been added
     */
    public FormModel getFormModel() {
        return formModel;
    }

    /** @retrun ComponentEventHandlers object that stores component's events
     *          and attached event handlers
     */
    public ComponentEventHandlers getEventHandlers() {
        if (eventsList == null)
            eventsList = new ComponentEventHandlers(this);
        return eventsList;
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

    public RADProperty[] getAllBeanProperties() {
        if (allProperties == null) {
            if (beanProperties == null)
                createBeanProperties();

            ArrayList list = new ArrayList(beanProperties.length + beanProperties2.length);
            list.addAll(Arrays.asList(beanProperties));
            list.addAll(Arrays.asList(beanProperties2));
            allProperties = FormEditor.sortProperties(list, beanClass);
        }

        return allProperties;
    }

    public Node.PropertySet[] getProperties() {
        if (beanPropertySets == null) {
            ArrayList propSets = new ArrayList(5);
            createPropertySets(propSets);
            beanPropertySets = (Node.PropertySet[])propSets.toArray(
                                        new Node.PropertySet[propSets.size()]);
        }
        return beanPropertySets;
    }

    protected void createPropertySets(List propSets) {
        if (beanProperties == null)
            createBeanProperties();

        propSets.add(new Node.PropertySet(
                "properties", // NOI18N
                FormEditor.getFormBundle().getString("CTL_PropertiesTab"),
                FormEditor.getFormBundle().getString("CTL_PropertiesTabHint")
                ) {
            public Node.Property[] getProperties() {
                return getComponentProperties();
            }
        });

        if (beanProperties2.length > 0)
            propSets.add(new Node.PropertySet(
                    "properties2", // NOI18N
                    FormEditor.getFormBundle().getString("CTL_Properties2Tab"),
                    FormEditor.getFormBundle().getString("CTL_Properties2TabHint")
                    ) {
                public Node.Property[] getProperties() {
                    return getComponentProperties2();
                }
            });

        propSets.add(new Node.PropertySet(
                "events", // NOI18N
                FormEditor.getFormBundle().getString("CTL_EventsTab"),
                FormEditor.getFormBundle().getString("CTL_EventsTabHint")
                ) {
            public Node.Property[] getProperties() {
                return getComponentEvents();
            }
        });

        propSets.add(new Node.PropertySet(
                "synthetic", // NOI18N
                FormEditor.getFormBundle().getString("CTL_SyntheticTab"),
                FormEditor.getFormBundle().getString("CTL_SyntheticTabHint")
                ) {
            public Node.Property[] getProperties() {
                return getSyntheticProperties();
            }
        });
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

    Node.Property[] getComponentProperties() {
        if (beanProperties == null)
            createBeanProperties();
        return beanProperties;
    }

    Node.Property[] getComponentProperties2() {
        if (beanProperties2 == null)
            createBeanProperties();
        return beanProperties2;
    }

    Node.Property[] getComponentEvents() {
        if (beanEvents == null)
            beanEvents = createEventsProperties();
        return beanEvents;
    }

    /** Can be used to obtain RADProperty of property with specified name
     * @param name the name of the property - the same as returned from
                   PropertyDescriptor.getName()
     * @return the RADProperty representing the specified property or null
               if property with specified name does not exist
     */
    public RADProperty getPropertyByName(String name) {
        if (beanProperties == null)
            createBeanProperties();
        return (RADProperty) nameToProperty.get(name);
    }

    // -----------------------------------------------------------------------------
    // Protected interface

    protected boolean hasDefaultEvent() {
        getEventHandlers();
        return eventsList.getDefaultEvent() != null;
    }

    protected void attachDefaultEvent() {
        getEventHandlers();
        Event defaultEvt = eventsList.getDefaultEvent();
        Vector handlers = defaultEvt.getHandlers();
        if ((handlers == null || handlers.size() == 0) && !readOnly)
            defaultEvt.createDefaultEventHandler();
        defaultEvt.gotoEventHandler();
    }

    // -----------------------------------------------------------------------------
    // Properties

    protected Node.Property[] createSyntheticProperties() {
        return formModel.getCodeGenerator().getSyntheticProperties(this);
    }

    protected void createBeanProperties() {
        ArrayList prefProps = new ArrayList();
        ArrayList normalProps = new ArrayList();
        ArrayList expertProps = new ArrayList();

        Object[] propsClsf = FormUtils.getPropertiesClassification(getBeanInfo());
        PropertyDescriptor[] props = getBeanInfo().getPropertyDescriptors();

        for (int i = 0; i < props.length; i++) {
            PropertyDescriptor pd = props[i];
            Object propType = FormUtils.getPropertyType(pd, propsClsf);
            List listToAdd;

            if (propType == FormUtils.PROP_PREFERRED)
                listToAdd = prefProps;
            else if (propType == FormUtils.PROP_NORMAL)
                listToAdd = normalProps;
            else if (propType == FormUtils.PROP_EXPERT)
                listToAdd = expertProps;
            else continue; // PROP_HIDDEN

            Node.Property prop = createProperty(pd);
            if (prop != null)
                listToAdd.add(createProperty(pd));
        }

        changePropertiesExplicitly(prefProps, normalProps, expertProps);

        int prefCount = prefProps.size();
        int normalCount = normalProps.size();
        int expertCount = expertProps.size();

        if (prefCount > 0) {
            beanProperties = new Node.Property[prefCount];
            prefProps.toArray(beanProperties);
            if (normalCount + expertCount > 0) {
                normalProps.addAll(expertProps);
                beanProperties2 = new Node.Property[normalCount + expertCount];
                normalProps.toArray(beanProperties2);
            }
            else beanProperties2 = new Node.Property[0];
        }
        else {
            beanProperties = new Node.Property[normalCount];
            normalProps.toArray(beanProperties);
            if (expertCount > 0) {
                beanProperties2 = new Node.Property[expertCount];
                expertProps.toArray(beanProperties2);
            }
            else beanProperties2 = new Node.Property[0];
        }
    }

    /** Called to modify original properties obtained from BeanInfo.
     * Properties may be added, removed etc. - due to specific needs.
     */
    protected void changePropertiesExplicitly(List prefProps,
                                              List normalProps,
                                              List expertProps) {
         // hack for buttons - add fake property for ButtonGroup
        if (getBeanInstance() instanceof javax.swing.AbstractButton)
            try {
                RADProperty prop = new ButtonGroupProperty(this);
                prop.addPropertyChangeListener(getPropertyListener());
                nameToProperty.put(prop.getName(), prop);

                Object propType = FormUtils.getPropertyType(
                            prop.getPropertyDescriptor(),
                            FormUtils.getPropertiesClassification(beanInfo));

                if (propType == FormUtils.PROP_PREFERRED)
                    prefProps.add(prop);
                else normalProps.add(prop);
            }
            catch (IntrospectionException ex) {} // should not happen
    }

    protected Node.Property[] createEventsProperties() {
        getEventHandlers();

        Node.Property[] nodeEvents = new Node.Property[eventsList.getEventCount()];
        int idx = 0;
        EventSet[] eventSets = eventsList.getEventSets();

        for (int i = 0; i < eventSets.length; i++) {
            Event[] events = eventSets[i].getEvents();
            for (int j = 0; j < events.length; j++) {
                nodeEvents[idx++] = new EventProperty(events[j]);
            }
        }
        return nodeEvents;
    }

    protected Node.Property createProperty(final PropertyDescriptor desc) {
        if (desc.getPropertyType() == null)
            return null;

        RADProperty prop = new RADProperty(this, desc);
        prop.addPropertyChangeListener(getPropertyListener());
        nameToProperty.put(desc.getName(), prop);

        // should or should not values of "visible" and "enabled" properties
        // be set (tied) to bean instances?
//        if (("visible".equals(desc.getName()) || "enabled".equals(desc.getName()))
//              && beanInstance instanceof java.awt.Component)
//            prop.setAccessType(FormProperty.DETACHED_WRITE);

        return prop;
    }

    protected PropertyChangeListener createPropertyListener() {
        return new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                RADComponentNode node = getNodeReference();
                if (node == null) return;

                // changes in component's properties should be propagated
                // to it's node
                if (FormProperty.PROP_VALUE.equals(ev.getPropertyName()))
                    node.firePropertyChangeHelper(ev.getPropertyName(),
                                    ev.getOldValue(), ev.getNewValue());

                else if (FormProperty.CURRENT_EDITOR.equals(ev.getPropertyName()))
                    node.fireComponentPropertySetsChange();
            }
        };
    }

    protected PropertyChangeListener getPropertyListener() {
        if (propertyListener == null)
            propertyListener = createPropertyListener();
        return propertyListener;
    }

    // -----------------------------------------------------------------------------
    // Debug methods

    public java.lang.String toString() {
        return super.toString() + ", name: "+getName()+", class: "+getBeanClass()+", beaninfo: "+getBeanInfo() + ", instance: "+getBeanInstance(); // NOI18N
    }

    public void debugChangedValues() {
        if (System.getProperty("netbeans.debug.form.full") != null) { // NOI18N
            System.out.println("-- debug.form: Changed property values in: "+this+" -------------------------"); // NOI18N
            for (java.util.Iterator it = nameToProperty.values().iterator(); it.hasNext();) {
                RADProperty prop =(RADProperty)it.next();
                if (prop.isChanged()) {
//                    PropertyDescriptor desc = prop.getPropertyDescriptor();
                    try {
                        System.out.println("Changed Property: "+prop.getName()+", value: "+prop.getValue()); // NOI18N
                    } catch (Exception e) {
                        // ignore problems
                    }
                }
            }
            System.out.println("--------------------------------------------------------------------------------------"); // NOI18N
        }
    }

    // ------------------------------------
    // innerclasses - some hacks for ButtonGroup...

    // pseudo-property for buttons - holds ButtonGroup in which button
    // is placed; kind of "reversed" property
    static class ButtonGroupProperty extends RADProperty {
        ButtonGroupListener listener = null;

        ButtonGroupProperty(RADComponent comp) throws IntrospectionException {
            super(comp, new FakePropertyDescriptor(
                            "buttonGroup", javax.swing.ButtonGroup.class)); // NOI18N
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            setShortDescription(FormEditor.getFormBundle().getString("HINT_ButtonGroup")); // NOI18N
        }

        public void setValue(Object value) throws IllegalAccessException,
                                                  IllegalArgumentException,
                                                  InvocationTargetException {
            FormModel formModel = getRADComponent().getFormModel();
            Object current = propertyValue;

            if (current instanceof RADComponent) {
                if (value == null)
                    formModel.removeFormModelListener(listener);
            }
            else if (value instanceof RADComponent) {
                if (listener == null)
                    listener = new ButtonGroupListener();
                formModel.addFormModelListener(listener);
            }

            super.setValue(value);
        }

        public void restoreDefaultValue() throws IllegalAccessException,
                                                 InvocationTargetException {
            if (listener != null)
                getRADComponent().getFormModel().removeFormModelListener(listener);
            super.restoreDefaultValue();
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

        public String getWholeSetterCode() {
            String groupName = getJavaInitializationString();
            return groupName != null ?
                groupName + ".add(" + getRADComponent().getName() + ");" : // NOI18N
                null;
        }

        class ButtonGroupListener extends FormModelAdapter {
            public void componentRemoved(FormModelEvent e) {
                try {
                    Object currentGroup = getValue();
                    Object deletedComp = e.getComponent();
                    if (currentGroup == deletedComp)
                        setValue(null);
                }
                catch (Exception ex) {} // getValue()/setValue() should not fail
            }
        }
    }

    // property editor for selecting ButtonGroup (for ButtonGroupProperty)
    public static class ButtonGroupPropertyEditor extends ComponentChooserEditor {
        public ButtonGroupPropertyEditor() {
            super();
            setBeanTypes(new Class[] { javax.swing.ButtonGroup.class });
            setComponentCategory(NONVISUAL_COMPONENTS);
        }
    }
}
