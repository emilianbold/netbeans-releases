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

package org.netbeans.modules.form;

import org.openide.explorer.propertysheet.editors.*;
import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import javax.swing.JTextField;

/* TODO
   - indexed properties
*/

/** RADComponent is a class which represents a single component used and instantiated
 * during design time.  It provides its properties and events.
 * Proper initialization order: <UL>
 * <LI> comp = new RADComponent();
 * <LI> comp.initialize(formManager);
 * <LI> comp.setComponent(class) or comp.setInstance(instance)
 * </UL>
 * @author Ian Formanek
 */
public class RADComponent {

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

    private Node.PropertySet[] beanPropertySets;
    private Node.Property[] syntheticProperties;
    private Node.Property[] beanProperties;
    private Node.Property[] beanProperties2;
    private Node.Property[] beanEvents;
    private RADComponent.RADProperty[] allProperties;

    private HashMap auxValues;
    private HashMap valuesCache;
    private HashMap editorsCache;
    private HashMap nameToProperty;
    private Map defaultPropertyValues;

    private FormManager2 formManager;
    private EventsList eventsList;
    private String gotoMethod;
    private boolean readOnly;

    private String storedName; // component name preserved between Cut and Paste

    // FINALIZE DEBUG METHOD
    public void finalize() throws Throwable {
        super.finalize();
        if (System.getProperty("netbeans.debug.form.finalize") != null) { // NOI18N
            System.out.println("finalized: "+this.getClass().getName()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    // -----------------------------------------------------------------------------
    // Constructors & Initialization

    /** Creates a new RADComponent */
    public RADComponent() {
        auxValues = new HashMap(10);
    }

    /** Called to initialize the component with specified FormManager .
     * @param formManager the FormManager of the form into which this component will be added 
     */
    public void initialize(FormManager2 formManager) {
        this.formManager = formManager;
        readOnly = formManager.getFormObject().isReadOnly();
    }

    /** Called to set the bean to be represented by this RADComponent.
     * This method creates a new instance of the bean. This RADComponent class is fully initialized after this method returns.
     * Can be called only once and is mutually exclusive with setInstance()
     * @param beanClass the class of the bean to be represented by this class
     * @see #setInstance
     */
    public void setComponent(Class beanClass) {
        if (this.beanClass != null) {
            throw new InternalError("Component already initialized: current: "+this.beanClass +", new: "+beanClass);// NOI18N
        }

        this.beanClass = beanClass;
        beanInstance = createBeanInstance();
        beanInfo = null; //BeanSupport.createBeanInfo(beanClass);

        initInternal();
    }

    /** Called to set the bean to be represented by this RADComponent.
     * This method uses the instance provided. This RADComponent class is fully initialized after this method returns.
     * Can be called only once and is mutually exclusive with setComponent()
     * @param beanInstance the bean to be represented by this class
     * @see #setComponent
     */
    public void setInstance(Object beanInstance) {
        if (this.beanClass != null) {
            throw new InternalError("Component already initialized: current: "+this.beanClass +", new: "+beanClass); // NOI18N
        }
        this.beanClass = beanInstance.getClass();
        this.beanInstance = beanInstance;
        beanInfo = BeanSupport.createBeanInfo(beanClass);

        initInternal();
        PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < props.length; i++) {
            if (FormUtils.isIgnoredProperty(beanInstance.getClass(), props[i].getName())) {
                // ignore some properties and do not make copies of their values
                continue;
            }
            RADProperty prop =(RADProperty)nameToProperty.get(props[i].getName());
            try {
                if (prop == null)       // unknown property. ignore
                    continue;
                if ((!prop.canRead()) ||(!prop.canWrite()))
                    continue;

                Object currentValue = prop.getValue();
                Object defaultValue = getDefaultPropertyValues().get(props[i].getName());
                if (!Utilities.compareObjects(currentValue, defaultValue)) {
                    prop.setChanged(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //        if (// [PENDING] notify exception
                // simply ignore this property
            }
        }
        // [PENDING - initialize changed properties]
    }

    /** Called to create the instance of the bean. Default implementation simply creates instance
     * of the bean's class using the default constructor.  Top-level container(the form object itself) 
     * will redefine this to use FormInfo to create the instance, as e.g. Dialogs cannot be created using 
     * the default constructor.
     * Note: this method is called only if the setComponent method is used, if setInstance is used, no new instance is created.
     * @return the instance of the bean that will be used during design time 
     */
    protected Object createBeanInstance() {
        return BeanSupport.createBeanInstance(beanClass);
    }

    /** Used by TuborgPersistenceManager */
    void initDeserializedEvents(java.util.Hashtable eventHandlers) {
        getComponentEvents();
        eventsList.initEvents(eventHandlers);
    }

    void setNodeReference(RADComponentNode node) {
        this.componentNode = node;
        componentNode.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (evt.getPropertyName() != null && evt.getPropertyName().equals("variableName")) { // NOI18N // [CHECK]
                    String oldName =(String) evt.getOldValue();
                    String newName =(String) evt.getNewValue();
                    EventsList.EventSet[] esets = getEventsList().getEventSets();
                    for (int i=0, n=esets.length; i<n; i++) {
                        EventsList.Event [] evts = esets [i].getEvents();
                        for (int j=0, m=evts.length; j<m; j++) {
                            String defaultName = FormUtils.getDefaultEventName(oldName, evts[j].getListenerMethod());
                            for (java.util.Iterator iter = evts[j].getHandlers().iterator(); iter.hasNext();) {
                                EventsManager.EventHandler eh =(EventsManager.EventHandler) iter.next();
                                if (eh.getName().equals(defaultName)) {
                                    String newValue = FormUtils.getDefaultEventName(newName, evts[j].getListenerMethod());
                                    formManager.getEventsManager().renameEventHandler(eh, newValue);
                                    formManager.fireEventRenamed(RADComponent.this, eh, defaultName);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
                                                );
    }

    private void initInternal() {
        nameToProperty = new HashMap();

        syntheticProperties = null; //createSyntheticProperties();
        beanProperties = null;
        beanProperties2 = null;

        beanEvents = null; //createEventsProperties();

        defaultPropertyValues = null; //BeanSupport.getDefaultPropertyValues(beanClass);
    }

    // -----------------------------------------------------------------------------
    // Public interface

    /** Provides access to the Class of the bean represented by this RADComponent
     * @return the Class of the bean represented by this RADComponent
     */
    public Class getBeanClass() {
        return beanClass;
    }

    /** Provides access to the real instance of the bean represented by this RADComponent
     * @return the instance of the bean represented by this RADComponent
     */
    public Object getBeanInstance() {
        return beanInstance;
    }

    /** Provides access to BeanInfo of the bean represented by this RADComponent
     * @return the BeanInfo of the bean represented by this RADComponent
     */
    public BeanInfo getBeanInfo() {
        if (beanInfo == null) {
            beanInfo = BeanSupport.createBeanInfo(beanClass);
        }
        return beanInfo;
    }

    /** This method can be used to check whether the bean represented by this RADCOmponent has hidden-state.
     * @return true if the component has hidden state, false otherwise
     */
    public boolean hasHiddenState() {
        String name = getBeanClass().getName();
        if (name.startsWith("java")) {
            return false;
        } else if (name.startsWith("org.")) {
            int idx = name.indexOf('.', 4);
            if (idx < 0) {
                idx = name.length();
            }
            name = name.substring(4, idx);
            if (name.equals("netbeans") || name.equals("openide")) {
                return false;
            }
        }
        return (getBeanInfo().getBeanDescriptor().getValue("hidden-state") != null); // NOI18N
    }

    public boolean readOnly() {
        return readOnly;
    }

    /** Getter for the Name property of the component - usually maps to variable declaration for holding the
     * instance of the component
     * @return current value of the Name property
     */
    public String getName() {
        return componentName;
    }

    /** Setter for the Name property of the component - usually maps to variable declaration for holding the
     * instance of the component
     * @param value new value of the Name property
     */
    public void setName(String value) {
        if ((componentName != null) &&(componentName.equals(value))) return; // same name => no change
        if (getFormManager().getVariablesPool().findVariableType(value) != null) return; // variable already exist => ignore
        if (!org.openide.util.Utilities.isJavaIdentifier(value)) return;

        String oldName = componentName;
        componentName = value;
        if (oldName != null) {
            getFormManager().getVariablesPool().deleteVariable(oldName);
        }
        getFormManager().getVariablesPool().createVariable(componentName, beanClass);

        if (oldName != null && getBeanInstance() instanceof javax.swing.JInternalFrame) {
            String postInit =
                (String) getAuxValue(JavaCodeGenerator.AUX_CREATE_CODE_POST);
            String oldStr = oldName + ".setVisible(true);"; // NOI18N
            String newStr = componentName + ".setVisible(true);"; // NOI18N
            setAuxValue(JavaCodeGenerator.AUX_CREATE_CODE_POST,
                        Utilities.replaceString(postInit, oldStr, newStr));;
         }
        
        getFormManager().fireComponentChanged(this, PROP_NAME, oldName, componentName);
        if (getNodeReference() != null) {
            getNodeReference().updateName();
        }
    }

    /** Restore name of component. If stored name is already in use or is null create new name. */
    void useStoredName() {
        String oldName = componentName;
        componentName = storedName;
        if (storedName == null || getFormManager().getVariablesPool().isReserved(storedName)) {
            componentName = getFormManager().getVariablesPool().getNewName(beanClass);
        }
        getFormManager().getVariablesPool().createVariable(componentName, beanClass);

        getFormManager().fireComponentChanged(this, PROP_NAME, oldName, componentName);
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

    /** Allows to add an auxiliary <name, value> pair, which is persistent in Gandalf.
     * The current value can be obtainer using getAuxValue(aux property name) method.
     * To remove aux value for specified property name, use setAuxValue(name, null).
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

    /** Provides access to the FormManager class which manages the form in which this component has been added.
     * The FormManager is the central class for obtaining informations about the form
     * @return the FormManager which manages the form into which this component has been added
     */
    public FormManager2 getFormManager() {
        return formManager;
    }

    public final EventsList getEventsList() {
        createEventsProperties();
        return eventsList;
    }
    
    public EventsList getEventsListImpl() {
        if (eventsList == null) {
            eventsList = new EventsList(this);
        }
        return eventsList;
    }
    

    /** @return the map of all currently set aux value - pairs of <String, Object> */
    public Map getAuxValues() {
        return auxValues;
    }

    /** Support for new types that can be created in this node.
     * @return array of new type operations that are allowed
     */
    public NewType[] getNewTypes() {
        return NO_NEW_TYPES;
    }

    RADComponent.RADProperty[] getAllProperties() {
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
        if (beanPropertySets != null)
            return beanPropertySets;

        if (beanProperties == null)
            createBeanProperties();

        ArrayList propSets = new ArrayList(4);

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

        beanPropertySets =
            (Node.PropertySet[]) propSets.toArray(new Node.PropertySet[propSets.size()]);
        return beanPropertySets;
    }

    /** Provides access to the Node which represents this RADComponent
     * @return the RADComponentNode which represents this RADComponent
     */
    public RADComponentNode getNodeReference() {
        return componentNode;
    }

    // -----------------------------------------------------------------------------
    // Access to component Properties

    public Node.Property[] getSyntheticProperties() {
        if (syntheticProperties == null) {
            syntheticProperties = createSyntheticProperties();
        }
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

    public Node.Property[] getComponentEvents() {
        if (beanEvents == null) {
            beanEvents = createEventsProperties();
        }
        return beanEvents;
    }
    
    Map getDefaultPropertyValues() {
        if (defaultPropertyValues == null) {
            defaultPropertyValues = BeanSupport.getDefaultPropertyValues(beanClass);
        }
        return defaultPropertyValues;
    }
    
    /** Can be used to obtain RADProperty of property with specified name
     * @param name the name of the property - the same as returned from PropertyDescriptor.getName()
     * @return the RADProperty representing the specified property or null if property with specified name does not exist
     */
    public RADProperty getPropertyByName(String name) {
        if (beanProperties == null)
            createBeanProperties();
        return(RADProperty) nameToProperty.get(name);
    }

    /** This method can be used to obtain default property value of the specified property.
     * @return the default property value or null, which means that the default value is null or cannot be obtained(write only property, ...)
     */
    public Object getDefaultPropertyValue(RADProperty prop) {
        if (defaultPropertyValues == null) {
            defaultPropertyValues = BeanSupport.getDefaultPropertyValues(beanClass);
        }
        return defaultPropertyValues.get(prop);
    }

    // -----------------------------------------------------------------------------
    // Protected interface

    protected boolean hasDefaultEvent() {
        getComponentEvents();
        return(eventsList.getDefaultEvent() != null);
    }

    protected void attachDefaultEvent() {
        getComponentEvents();
        EventsList.Event defaultEvt = eventsList.getDefaultEvent();
        Vector handlers = defaultEvt.getHandlers();
        if ((handlers == null || handlers.size() == 0) && !readOnly)
            defaultEvt.createDefaultEventHandler();
        defaultEvt.gotoEventHandler();
    }

    protected Node.Property[] createSyntheticProperties() {
        return getFormManager().getCodeGenerator().getSyntheticProperties(this);
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

    protected Node.Property[] createEventsProperties() {
        eventsList = getEventsListImpl();

        Node.Property[] nodeEvents = new Node.Property[eventsList.getEventCount()];
        int idx = 0;
        EventsList.EventSet[] eventSets = eventsList.getEventSets();

        for (int i = 0; i < eventSets.length; i++) {
            EventsList.Event[] events = eventSets[i].getEvents();
            for (int j = 0; j < events.length; j++) {
                Node.Property ep = new EventProperty(events[j]) {

                    public Object getValue() {
                        if (event.getHandlers().size() == 0)
                            lastSelectedHandler = null;
                        else
                            lastSelectedHandler =((EventsManager.EventHandler) event.getHandlers().get(0)).getName();
                        return lastSelectedHandler;
                    }

                    public void setValue(Object val) throws IllegalArgumentException {
                        if (val == null)
                            return;
                        if (!(val instanceof HandlerSetChange)) {
                            if (val instanceof String) {
                                HandlerSetChange change = new HandlerSetChange();
                                if (event.getHandlers().size() > 0) {
                                    change.getRenamedNewNames().add((String)val);
                                    change.getRenamedOldNames().add((String) getValue());
                                }
                                else change.getAdded().add((String)val);
                                val = change;
                            }
                            else {
                                throw new IllegalArgumentException();
                            }
                        }
                        Hashtable handlersByName = new Hashtable();
                        Vector handlers = event.getHandlers();

                        for (Iterator it = handlers.iterator(); it.hasNext();) {
                            EventsManager.EventHandler h =(EventsManager.EventHandler) it.next();
                            handlersByName.put(h.getName(), h);
                        }

                        HandlerSetChange change =(HandlerSetChange) val;
                        if (change.hasRemoved()) {
                            for (Iterator iter = change.getRemoved().iterator(); iter.hasNext();) {
                                EventsManager.EventHandler handler =(EventsManager.EventHandler) handlersByName.get((String) iter.next());
                                formManager.getEventsManager().removeEventHandler(event, handler);
                                formManager.fireEventRemoved(RADComponent.this, handler);
                            }
                        }
                        if (change.hasRenamed()) {
                            for (int k=0, n = change.getRenamedOldNames().size(); k<n; k++) {
                                String oldName =(String) change.getRenamedOldNames().get(k);
                                String newName =(String) change.getRenamedNewNames().get(k);
                                if (!Utilities.isJavaIdentifier(newName)) continue;
                                if (newName.equals(oldName)) continue; // no change
                                EventsManager.EventHandler handler =(EventsManager.EventHandler) handlersByName.get(oldName);
                                formManager.getEventsManager().renameEventHandler(handler, newName);
                                formManager.fireEventRenamed(RADComponent.this, handler, oldName);
                            }
                        }
                        if (change.hasAdded()) {
                            for (Iterator iter = change.getAdded().iterator(); iter.hasNext();) {
                                String handlerName =(String) iter.next();
                                if (!Utilities.isJavaIdentifier(handlerName)) {
                                    TopManager.getDefault().notify(new NotifyDescriptor.Message(java.text.MessageFormat.format(FormEditor.getFormBundle().getString("FMT_MSG_InvalidJavaIdentifier"), new Object [] {handlerName}), NotifyDescriptor.ERROR_MESSAGE));
                                    continue;
                                }
                                // adding event handler
                                if (formManager.getEventsManager().addEventHandler(event, handlerName)) {
                                    EventsManager.EventHandler handler =(EventsManager.EventHandler) event.getHandlers().get(event.getHandlers().size() -1);
                                    formManager.fireEventAdded(RADComponent.this, handler);
                                }
                                else return;
                            }
                        }
                        String newSelectedHandler = ""; // NOI18N
                        if (event.getHandlers().size() >0)
                            newSelectedHandler =((EventsManager.EventHandler) event.getHandlers().get(0)).getName();
                        getNodeReference().firePropertyChangeHelper(this.getName(), lastSelectedHandler, newSelectedHandler);
                        ((java.beans.PropertyEditorSupport)getPropertyEditor()).firePropertyChange();
                    }

                    public boolean canWrite() {
                        return !RADComponent.this.readOnly;
                    }
                };
                nodeEvents[idx++] = ep;
            }
        }
        return nodeEvents;

    }

    /** This method can be used to correctly set property value of specified property on the bean represented by this RADComponent.
     * Used during deserialization.
     * @param desc The property to change
     * @param value the new value of the property
     */
    void restorePropertyValue(PropertyDescriptor desc, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        setPropertyValue(desc, value);
        Object defValue = getDefaultPropertyValues().get(desc.getName());
        // add the property to the list of changed properties
        RADProperty prop =(RADProperty)nameToProperty.get(desc.getName());
        if (prop != null)
            prop.setChanged(true);
    }

    // -----------------------------------------------------------------------------
    // Protected interface to working with properties on bean instance

    protected Object getPropertyValue(PropertyDescriptor desc) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (isChangedValue(desc)) {
            return getChangedValue(desc);
        }
        Method readMethod = desc.getReadMethod();
        if (readMethod == null) {
            throw new IllegalAccessException();
        }
        return readMethod.invoke(getBeanInstance(), new Object[0]);
    }

    protected void setPropertyValue(PropertyDescriptor desc, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        cacheValue(desc, value);

        // [PENDING - property names to cache]
        if ("enabled".equals(desc.getName()) || // NOI18N
            "visible".equals(desc.getName())) // NOI18N
        {
            // values of these properties are just cached, not represented during design-time
            return;
        }

        Method writeMethod = desc.getWriteMethod();
        if (writeMethod == null) {
            throw new IllegalAccessException();
        }
        Object valueToSet = value;
        if (value instanceof FormDesignValue) {
            valueToSet =((FormDesignValue)value).getDesignValue(RADComponent.this);
            if (valueToSet == FormDesignValue.IGNORED_VALUE) return; // ignore this value, as it is not value to be reflected during design-time
        }
        writeMethod.invoke(getBeanInstance(), new Object[] { valueToSet });
    }

    protected Object getIndexedPropertyValue(IndexedPropertyDescriptor desc, int index) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method readMethod = desc.getIndexedReadMethod();
        if (readMethod == null) {
            throw new IllegalAccessException();
        }
        return readMethod.invoke(getBeanInstance(), new Object[] { new Integer(index) });
    }

    protected void setIndexedPropertyValue(IndexedPropertyDescriptor desc, int index, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method writeMethod = desc.getIndexedWriteMethod();
        if (writeMethod == null) {
            throw new IllegalAccessException();
        }
        writeMethod.invoke(getBeanInstance(), new Object[] { new Integer(index), value });
    }

    protected void cacheValue(PropertyDescriptor desc, Object value) {
        if (valuesCache == null) {
            valuesCache = new HashMap(10);
            editorsCache = new HashMap(10);
        }
        valuesCache.put(desc, value);
    }

    protected boolean isChangedValue(PropertyDescriptor desc) {
        if (valuesCache == null) {
            return false;
        }
        return valuesCache.containsKey(desc);
    }

    protected Object getChangedValue(PropertyDescriptor desc) {
        if (valuesCache == null) {
            throw new InternalError();
        }
        return valuesCache.get(desc);
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
                    PropertyDescriptor desc = prop.getPropertyDescriptor();
                    try {
                        System.out.println("Changed Property: "+desc.getName()+", value: "+prop.getValue()); // NOI18N
                    } catch (Exception e) {
                        // ignore problems
                    }
                }
            }
            System.out.println("--------------------------------------------------------------------------------------"); // NOI18N
        }
    }

    // -----------------------------------------------------------------------------
    // Properties and Inner Classes

    public void notifyPropertiesChange() {
        if (componentNode != null) componentNode.notifyPropertiesChange();
    }

    public interface RADProperty {
        public String getName();
        public PropertyDescriptor getPropertyDescriptor();
        public PropertyEditor getPropertyEditor();
        public PropertyEditor getCurrentEditor();
        public PropertyEditor getExpliciteEditor();
        public void setCurrentEditor(PropertyEditor editor);
        public RADComponent getRADComponent();
        public boolean canRead();
        public Object getValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
        public boolean canWrite();
        public void setValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
        public boolean supportsDefaultValue();
        public void restoreDefaultValue();
        public Object getDefaultValue();

        public String getPreCode();
        public String getPostCode();
        public void setPreCode(String value);
        public void setPostCode(String value);

        public boolean isChanged();
        public void setChanged(boolean value);
    }

    private Node.Property createProperty(final PropertyDescriptor desc) {
        Node.Property prop;
        if (desc instanceof IndexedPropertyDescriptor) {
            prop = new RADIndexedPropertyImpl((IndexedPropertyDescriptor)desc);
        } else {
            prop = new RADPropertyImpl(desc);
        }

        prop.setName(desc.getName());
        prop.setDisplayName(desc.getDisplayName());
        prop.setShortDescription(desc.getShortDescription());

        nameToProperty.put(desc.getName(), prop);
        return prop;
    }

    class RADPropertyImpl extends Node.Property implements RADProperty {
        private PropertyEditor currentPropertyEditor;
        private PropertyDescriptor desc;
        String preCode = null;                      // custom pre-initialization code to be used before calling the property setter
        String postCode = null;                     // custom post-initialization code to be used after calling the property setter
        boolean changed = false;

        RADPropertyImpl(PropertyDescriptor desc) {
            super(desc.getPropertyType());
            this.desc = desc;
        }

        public PropertyDescriptor getPropertyDescriptor() {
            return desc;
        }

        public RADComponent getRADComponent() {
            return RADComponent.this;
        }

        public PropertyEditor getCurrentEditor() {
            if (currentPropertyEditor == null) {
                currentPropertyEditor = findDefaultEditor(desc);
            }
            return currentPropertyEditor;
        }

        public void setCurrentEditor(PropertyEditor value) {
            currentPropertyEditor = value;
        }

        /** Test whether the property is readable.
         * @return <CODE>true</CODE> if it is
         */
        public boolean canRead() {
            return(desc.getReadMethod() != null);
        }

        /** Get the value.
         * @return the value of the property
         * @exception IllegalAccessException cannot access the called method
         * @exception IllegalArgumentException wrong argument
         * @exception InvocationTargetException an exception during invocation
         */
        public Object getValue() throws
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return getPropertyValue(desc);
        }

        /** Test whether the property is writable.
         * @return <CODE>true</CODE> if the read of the value is supported
         */
        public boolean canWrite() {
            return !RADComponent.this.readOnly
                     && desc.getWriteMethod() != null;
        }

        /** Set the value.
         * @param val the new value of the property
         * @exception IllegalAccessException cannot access the called method
         * @exception IllegalArgumentException wrong argument
         * @exception InvocationTargetException an exception during invocation
         */
        public void setValue(Object val) throws IllegalAccessException,
                                                IllegalArgumentException, InvocationTargetException {
            Object old = null;

            if (canRead()) {
                try {
                    old = getValue();
                } catch (IllegalArgumentException e) {  // no problem -> keep null
                } catch (IllegalAccessException e) {    // no problem -> keep null
                } catch (InvocationTargetException e) { // no problem -> keep null
                }
            }

            if (old == val) return; // no change
            if ((old != null) &&(val != null) &&(val.equals(old))) return; // no change

            try {
                setPropertyValue(desc, val);
            } catch (IllegalArgumentException e) {  // no problem -> keep null
            } catch (IllegalAccessException e) {    // no problem -> keep null
            } catch (InvocationTargetException e) { // no problem -> keep null
            }


            boolean isChanged = false;
            if (getDefaultPropertyValues().containsKey(desc.getName())) { // if there is reasonable default
                Object defValue = getDefaultPropertyValues().get(desc.getName());
                isChanged = !Utilities.compareObjects(defValue, val);
            } else { // no default => always treat is as changed
                isChanged = true;
            }
            setChanged(isChanged);

            debugChangedValues();

            getFormManager().firePropertyChanged(RADComponent.this, desc.getName(), old, val);
            if (componentNode != null) componentNode.firePropertyChangeHelper(RADPropertyImpl.this.getName(), old, val);

            if (RADComponent.this instanceof RADVisualComponent) {
                if (beanInstance instanceof javax.swing.JComponent) {
                    ((javax.swing.JComponent)beanInstance).repaint();
                    ((javax.swing.JComponent)beanInstance).revalidate();
                } else {
                    java.awt.Container cc =((java.awt.Component)beanInstance).getParent();
                    if ((cc != null) &&(cc.getParent() != null)) {
                        cc.getParent().validate();
                    }
                }
            }
        }

        /** Test whether the property had a default value.
         * @return <code>true</code> if it does
         */
        public boolean supportsDefaultValue() {
            return getDefaultPropertyValues().containsKey(desc.getName()); // true if there is reasonable default
        }

        /** Restore this property to its default value, if supported.
         */
        public void restoreDefaultValue() {
            // 1. remove the property from list of changed values, so that the code for it is not generated
            setChanged(false);

            Object old = null;

            if (canRead()) {
                try {
                    old = getValue();
                } catch (IllegalArgumentException e) {  // no problem -> keep null
                } catch (IllegalAccessException e) {    // no problem -> keep null
                } catch (InvocationTargetException e) { // no problem -> keep null
                }
            }

            // 2. restore the default property value
            if (getDefaultPropertyValues().containsKey(desc.getName())) { // if there is reasonable default
                Object def = getDefaultPropertyValues().get(desc.getName());
                try {
                    setValue(def);
                } catch (IllegalAccessException e) {
                    // what to do, ignore...
                } catch (IllegalArgumentException e) {
                    // what to do, ignore...
                } catch (InvocationTargetException e) {
                    // what to do, ignore...
                }
                getFormManager().firePropertyChanged(RADComponent.this, desc.getName(), old, def);
                notifyPropertiesChange();
                if (componentNode != null) componentNode.firePropertyChangeHelper(RADPropertyImpl.this.getName(), old, def);
            }
            // [PENDING - test]
        }

        public Object getDefaultValue() {
            return getDefaultPropertyValues().get(desc.getName());
        }

        /* Returns property editor for this property.
         * @return the property editor or <CODE>null</CODE> if there should not be
         *    any editor.
         */
        public PropertyEditor getPropertyEditor() {
            // FormPropertyEditor is one of the advanced features that must be supported bu the
            // persistence manager to be available
            if (!getFormManager().getFormEditorSupport().supportsAdvancedFeatures()) {
                PropertyEditor prEd = findDefaultEditor(desc);
                if (prEd instanceof FormAwareEditor) {
                    ((FormAwareEditor)prEd).setRADComponent(RADComponent.this, RADPropertyImpl.this);
                }
                if (prEd instanceof org.openide.explorer.propertysheet.editors.NodePropertyEditor) {
                    ((org.openide.explorer.propertysheet.editors.NodePropertyEditor)prEd).attach(new org.openide.nodes.Node[] { getNodeReference() });
                }
                return prEd;
            }
            // the property editor cannot be reused as it is not reentrant !!! [IAN]
            PropertyEditor defaultEditor = findDefaultEditor(desc);
            FormPropertyEditor editor = null;
            if (defaultEditor != null) {
                editor = new FormPropertyEditor(RADComponent.this, desc.getPropertyType(), RADPropertyImpl.this);
            }
            return editor;
        }

        public PropertyEditor getExpliciteEditor() {
            if (desc.getPropertyEditorClass() != null) {
                try {
                    return(PropertyEditor) desc.getPropertyEditorClass().newInstance();
                } catch (Exception ex) {
                    if (System.getProperty("netbeans.debug.exceptions") != null) ex.printStackTrace(); // NOI18N
                }
            }
            return null;
        }

        private PropertyEditor findDefaultEditor(PropertyDescriptor desc) {
            PropertyEditor defaultEditor = getExpliciteEditor();
            if (defaultEditor == null) {
                return FormPropertyEditorManager.findEditor(desc.getPropertyType());
            } else {
                return defaultEditor;
            }
        }

        public String getPreCode() {
            return preCode;
        }

        public void setPreCode(String value) {
            if ((preCode == null && value != null) || (preCode != null && !preCode.equals(value))) {
                preCode = value;
                getFormManager().firePropertyChanged(RADComponent.this, desc.getName(), null, null);
            }
        }

        public String getPostCode() {
            return postCode;
        }

        public void setPostCode(String value) {
            if ((postCode == null && value != null) || (postCode != null && !postCode.equals(value))) {
                postCode = value;
                getFormManager().firePropertyChanged(RADComponent.this, desc.getName(), null, null);
            }
        }

        public boolean isChanged() {
            return changed;
        }

        public void setChanged(boolean value) {
            changed = value;
        }
    }


    class RADIndexedPropertyImpl extends Node.IndexedProperty implements RADProperty {
        private PropertyEditor currentEditor;
        private IndexedPropertyDescriptor desc;
        String preCode = null;                      // custom pre-initialization code to be used before calling the property setter
        String postCode = null;                     // custom post-initialization code to be used after calling the property setter
        boolean changed = false;

        RADIndexedPropertyImpl(IndexedPropertyDescriptor desc) {
            super(getIndexedType(desc), desc.getIndexedPropertyType());
            this.desc = desc;
            currentEditor = findDefaultIndexedEditor(desc);
        }

        public PropertyDescriptor getPropertyDescriptor() {
            return desc;
        }

        public PropertyEditor getCurrentEditor() {
            return currentEditor;
        }

        public void setCurrentEditor(PropertyEditor editor) {
            currentEditor = editor;
        }

        public RADComponent getRADComponent() {
            return RADComponent.this;
        }

        /** Test whether the property is readable.
         * @return <CODE>true</CODE> if it is
         */
        public boolean canRead() {
            return(desc.getReadMethod() != null);
        }

        /** Get the value.
         * @return the value of the property
         * @exception IllegalAccessException cannot access the called method
         * @exception IllegalArgumentException wrong argument
         * @exception InvocationTargetException an exception during invocation
         */
        public Object getValue() throws
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return getPropertyValue(desc);
        }

        /** Test whether the property is writable.
         * @return <CODE>true</CODE> if the read of the value is supported
         */
        public boolean canWrite() {
            return(desc.getWriteMethod() != null);
        }

        /** Set the value.
         * @param val the new value of the property
         * @exception IllegalAccessException cannot access the called method
         * @exception IllegalArgumentException wrong argument
         * @exception InvocationTargetException an exception during invocation
         */
        public void setValue(Object val) throws IllegalAccessException,
                                                IllegalArgumentException, InvocationTargetException {
            Object old = null;

            if (canRead()) {
                try {
                    old = getValue();
                    if (Utilities.compareObjects(old, val)) return; // no change
                } catch (IllegalArgumentException e) {  // no problem -> keep null
                } catch (IllegalAccessException e) {    // no problem -> keep null
                } catch (InvocationTargetException e) { // no problem -> keep null
                }
            }


            try {
                setPropertyValue(desc, val);
            } catch (IllegalArgumentException e) {  // no problem -> keep null
            } catch (IllegalAccessException e) {    // no problem -> keep null
            } catch (InvocationTargetException e) { // no problem -> keep null
            }

            boolean isChanged = false;
            if (getDefaultPropertyValues().containsKey(desc.getName())) { // if there is reasonable default
                Object defValue = getDefaultPropertyValues().get(desc.getName());
                isChanged = !Utilities.compareObjects(defValue, val);
            } else { // no default => always treat is as changed
                isChanged = true;
            }
            setChanged(isChanged);
            debugChangedValues();
            getFormManager().firePropertyChanged(RADComponent.this, desc.getName(), old, val);
            if (componentNode != null) componentNode.firePropertyChangeHelper(RADIndexedPropertyImpl.this.getName(), old, val);
            if (RADComponent.this instanceof RADVisualComponent) {
                if (beanInstance instanceof javax.swing.JComponent) {
                    ((javax.swing.JComponent)beanInstance).repaint();
                    ((javax.swing.JComponent)beanInstance).revalidate();
                } else {
                    java.awt.Container cc =((java.awt.Component)beanInstance).getParent();
                    if ((cc != null) &&(cc.getParent() != null)) {
                        cc.getParent().validate();
                    }
                }
            }
        }

        /** Test whether the property had a default value.
         * @return <code>true</code> if it does
         */
        public boolean supportsDefaultValue() {
            return getDefaultPropertyValues().containsKey(desc.getName()); // true if there is reasonable default
        }

        /** Restore this property to its default value, if supported.
         */
        public void restoreDefaultValue() {
            // 1. remove the property from list of changed values, so that the code for it is not generated
            setChanged(false);

            Object old = null;

            if (canRead()) {
                try {
                    old = getValue();
                } catch (IllegalArgumentException e) {  // no problem -> keep null
                } catch (IllegalAccessException e) {    // no problem -> keep null
                } catch (InvocationTargetException e) { // no problem -> keep null
                }
            }

            // 2. restore the default property value
            if (getDefaultPropertyValues().containsKey(desc.getName())) { // if there is reasonable default
                Object def = getDefaultPropertyValues().get(desc.getName());
                try {
                    setValue(def);
                } catch (IllegalAccessException e) {
                    // what to do, ignore...
                } catch (IllegalArgumentException e) {
                    // what to do, ignore...
                } catch (InvocationTargetException e) {
                    // what to do, ignore...
                }
                getFormManager().firePropertyChanged(RADComponent.this, desc.getName(), old, def);
                if (componentNode != null) componentNode.firePropertyChangeHelper(RADIndexedPropertyImpl.this.getName(), old, def);
            }
            // [PENDING - test]
        }

        public Object getDefaultValue() {
            return getDefaultPropertyValues().get(desc.getName());
        }

        /* Returns property editor for this property.
         * @return the property editor or <CODE>null</CODE> if there should not be
         *    any editor.
         */
        public PropertyEditor getPropertyEditor() {
            // FormPropertyEditor is one of the advanced features that must be supported bu the
            // persistence manager to be available
            if (!getFormManager().getFormEditorSupport().supportsAdvancedFeatures()) {
                PropertyEditor prEd = findDefaultIndexedEditor(desc);
                if (prEd instanceof FormAwareEditor) {
                    ((FormAwareEditor)prEd).setRADComponent(RADComponent.this, RADIndexedPropertyImpl.this);
                }
                if (prEd instanceof org.openide.explorer.propertysheet.editors.NodePropertyEditor) {
                    ((org.openide.explorer.propertysheet.editors.NodePropertyEditor)prEd).attach(new org.openide.nodes.Node[] { getNodeReference() });
                }
                return prEd;
            }

            // the property editor cannot be reused as it is not reentrant !!! [IAN]

            PropertyEditor defaultEditor = findDefaultIndexedEditor(desc);
            FormPropertyEditor editor = null;
            if (defaultEditor != null) {
                editor = new FormPropertyEditor(RADComponent.this, desc.getIndexedPropertyType(), RADIndexedPropertyImpl.this);
            }
            return editor;
        }

        public PropertyEditor getExpliciteEditor() {
            if (desc.getPropertyEditorClass() != null) {
                try {
                    return(PropertyEditor) desc.getPropertyEditorClass().newInstance();
                } catch (Exception ex) {
                    if (System.getProperty("netbeans.debug.exceptions") != null) ex.printStackTrace(); // NOI18N
                }
            }
            return null;
        }

        private PropertyEditor findDefaultIndexedEditor(IndexedPropertyDescriptor desc) {
            PropertyEditor defaultEditor = getExpliciteEditor();
            if (defaultEditor == null) {
                return FormPropertyEditorManager.findEditor(desc.getIndexedPropertyType());
            } else {
                return defaultEditor;
            }
        }

        /** Test whether the property is readable by index.
         * @return <CODE>true</CODE> if so
         */
        public boolean canIndexedRead() {
            return(desc.getIndexedReadMethod() != null);
        }

        /** Get the value of the property at an index.
         *
         * @param indx the index
         * @return the value at that index
         * @exception IllegalAccessException cannot access the called method
         * @exception IllegalArgumentException wrong argument
         * @exception InvocationTargetException an exception during invocation
         */
        public Object getIndexedValue(int index) throws
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return null;
            // [PENDING indexed]
        }

        /** Test whether the property is writable by index.
         * @return <CODE>true</CODE> if so
         */
        public boolean canIndexedWrite() {
            return(desc.getIndexedWriteMethod() != null);
        }

        /** Set the value of the property at an index.
         *
         * @param indx the index
         * @param val the value to set
         * @exception IllegalAccessException cannot access the called method
         * @exception IllegalArgumentException wrong argument
         * @exception InvocationTargetException an exception during invocation
         */
        public void setIndexedValue(int indx, Object val) throws
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            // [PENDING indexed]
        }

        /** Get a property editor for individual elements in this property.
         * @return the property editor for elements
         */
        //    public PropertyEditor getIndexedPropertyEditor() { // [PENDING indexed]
        //      return java.beans.PropertyEditorManager.findEditor(elementType);
        //    }

        public String getPreCode() {
            return preCode;
        }

        public void setPreCode(String value) {
            if ((preCode == null && value != null) || (preCode != null && !preCode.equals(value))) {
                preCode = value;
                getFormManager().firePropertyChanged(RADComponent.this, desc.getName(), null, null);
            }
        }

        public String getPostCode() {
            return postCode;
        }

        public void setPostCode(String value) {
            if ((postCode == null && value != null) || (postCode != null && !postCode.equals(value))) {
                postCode = value;
                getFormManager().firePropertyChanged(RADComponent.this, desc.getName(), null, null);
            }
        }

        public boolean isChanged() {
            return changed;
        }

        public void setChanged(boolean value) {
            changed = value;
        }
    }

    /** Utility method for obtaining array type for indexed properties */
    private static Class getIndexedType(IndexedPropertyDescriptor desc) {
        Class valueType = desc.getPropertyType();
        if (valueType == null) {
            try {
                valueType = org.openide.TopManager.getDefault().currentClassLoader().loadClass(
                    "[L" + desc.getIndexedPropertyType().getName() + ";" // NOI18N
                    );
            } catch (Exception e) {
                valueType = Object[].class;
            }
        }
        return valueType;
    }


    abstract class EventProperty extends PropertySupport.ReadWrite {
        EventsList.Event event;
        String lastSelectedHandler;

        EventProperty(EventsList.Event event) {
            super(FormEditor.EVENT_PREFIX + event.getName(),
                  String.class,
                  event.getName(),
                  event.getName());
            this.event = event;
            setShortDescription(event.getListenerMethod().getDeclaringClass().getName());
        }

        /** Returns property editor for this property.
         * @return the property editor or <CODE>null</CODE> if there should not be
         *    any editor.
         */
        public java.beans.PropertyEditor getPropertyEditor() {
            return new EventEditor();
        }

        class HandlerSetChange {
            boolean hasAdded() {
                return(added !=null && added.size()>0);
            }
            boolean hasRemoved() {
                return(removed !=null && removed.size()>0);
            }
            boolean hasRenamed() {
                return(renamedOldName !=null && renamedOldName.size()>0);
            }
            Vector getAdded() {
                if (added == null) added = new Vector();
                return added;
            }
            Vector getRemoved() {
                if (removed == null) removed = new Vector();
                return removed;
            }
            Vector getRenamedOldNames() {
                if (renamedOldName == null) renamedOldName = new Vector();
                return renamedOldName;
            }
            Vector getRenamedNewNames() {
                if (renamedNewName == null) renamedNewName = new Vector();
                return renamedNewName;
            }
            private Vector added;
            private Vector removed;
            private Vector renamedOldName;
            private Vector renamedNewName;
        }

        class EventEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
            public String getAsText() {
                if (this.getValue() == null)
                    return FormEditor.getFormBundle().getString("CTL_NoEvent");
                else
                    return(String) this.getValue();
            }

            public void setAsText(String selected) {
                HandlerSetChange change = new HandlerSetChange();
                if (this.getValue() == null) {               // new event
                    change.getAdded().add(selected);
                } else {                                 // rename
                    change.getRenamedNewNames().add(selected);
                    change.getRenamedOldNames().add(getAsText());
                }
                this.setValue(change);
            }

            public boolean supportsEditingTaggedValues() {
                return false;
            }
            /**
             * @return Returns custom property editor to be showen inside the property
             *         sheet.
             */
            public java.awt.Component getInPlaceCustomEditor() {
                Vector handlers = event.getHandlers();
                if (formManager.getFormEditorSupport().supportsAdvancedFeatures()) {
                    final javax.swing.JComboBox eventCombo = new javax.swing.JComboBox();
                    eventCombo.setEditable(!RADComponent.this.readOnly);

                    if (handlers.size() == 0) {
                        eventCombo.getEditor().setItem(FormUtils.getDefaultEventName(RADComponent.this, event.getListenerMethod()));
                    } else {
                        for (int i=0, n=handlers.size(); i<n; i++) {
                            eventCombo.addItem(((EventsManager.EventHandler) handlers.get(i)).getName()); // [PENDING]
                        }
                    }

                    if (!RADComponent.this.readOnly)
                        eventCombo.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                String selected =(String) eventCombo.getEditor().getItem();
                                lastSelectedHandler = selected;
                                event.gotoEventHandler(selected);
                            }
                        });
                    else
                        eventCombo.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                String selected = (String) eventCombo.getSelectedItem();
                                event.gotoEventHandler(selected);
                            }
                        });

                    if (!RADComponent.this.readOnly) {
                        eventCombo.addFocusListener(new java.awt.event.FocusAdapter() {
                            public void focusGained(java.awt.event.FocusEvent evt) {
                                Vector hand = event.getHandlers();
                                eventCombo.removeAllItems();
                                if (hand.size() == 0) {
                                    eventCombo.getEditor().setItem(FormUtils.getDefaultEventName(RADComponent.this, event.getListenerMethod()));
                                } else {
                                    for (int i=0, n=hand.size(); i<n; i++) {
                                        eventCombo.addItem(((EventsManager.EventHandler) hand.get(i)).getName());
                                    }
                                }
                            }
                        }
                                                    );
                        eventCombo.getEditor().addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                String selected =(String) eventCombo.getEditor().getItem();
                                String oldname = lastSelectedHandler;
                                lastSelectedHandler = selected;
                                boolean removed = false;
                                boolean isNew = true;
                                String items[] = new String[eventCombo.getItemCount()];
                                for (int i=0, n=eventCombo.getItemCount(); i<n; i++) {
                                    items[i] =(String) eventCombo.getItemAt(i);
                                    if (eventCombo.getItemAt(i).equals(selected)) {
                                        isNew = false;
                                    }
                                }
                                if (isNew) {
                                    HandlerSetChange change = new HandlerSetChange();
                                    if (eventCombo.getItemCount()==0) {     // event added
                                        change.getAdded().add(selected);
                                        eventCombo.addItem(selected);
                                    }
                                    else {
                                        if (selected.equals("")) {          // event deleted
                                            change.getRemoved().add(oldname);
                                            removed = true;
                                        }
                                        else {                              // event renamed
                                            change.getRenamedOldNames().add(oldname);
                                            change.getRenamedNewNames().add(selected);
                                        }
                                    }
                                    EventEditor.this.setValue(change);
                                }
                                if (!removed) event.gotoEventHandler(selected);
                            }
                        }
                                                             );
                    }
                    return eventCombo;
                } else {
                    final JTextField eventField = new JTextField();
                    if (handlers.size() == 0) {
                        eventField.setText(FormUtils.getDefaultEventName(RADComponent.this, event.getListenerMethod()));
                    } else {
                        eventField.setText(((EventsManager.EventHandler) handlers.get(0)).getName());
                    }
                    if (RADComponent.this.readOnly)
                        eventField.setEditable(false);
                    else
                        eventField.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                setAsText(eventField.getText());
                            }
                        }
                                                 );
                    return eventField;
                }
            }

            /**
             * @return true if this PropertyEditor provides a enhanced in-place custom 
             *              property editor, false otherwise
             */
            public boolean hasInPlaceCustomEditor() {
                return !RADComponent.this.readOnly || event.getHandlers().size() > 0;
            }

            public boolean supportsCustomEditor() {
                return //!RADComponent.this.readOnly &&
                       formManager.getFormEditorSupport().supportsAdvancedFeatures();
            }

            public java.awt.Component getCustomEditor() {
                if (RADComponent.this.readOnly) return null;
                final EventCustomEditor ed = new EventCustomEditor(EventProperty.this);
                DialogDescriptor dd = new DialogDescriptor(ed,
                                                           java.text.MessageFormat.format(FormEditor.getFormBundle().getString("FMT_MSG_HandlersFor"), new Object [] {event.getName()}),
                                                           true,
                                                           new java.awt.event.ActionListener() {
                                                               public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                                   if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                                                                       ed.doChanges();
                                                                   }
                                                               }
                                                           }
                                                           );
                return TopManager.getDefault().createDialog(dd);
            }
        }
    }
}
