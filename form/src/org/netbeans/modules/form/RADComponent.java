/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.form;

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
* <LI> comp = new RADComponent ();
* <LI> comp.initialize (formManager);
* <LI> comp.setComponent (class) or comp.setInstance (instance)
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
  private Node.Property[] beanExpertProperties;
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
  
  private String storedName; // component name preserved between Cut and Paste

  // FINALIZE DEBUG METHOD
  public void finalize () throws Throwable {
    super.finalize ();
    if (System.getProperty ("netbeans.debug.form.finalize") != null) { // NOI18N
      System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
    }
  } // FINALIZE DEBUG METHOD
  
// -----------------------------------------------------------------------------
// Constructors & Initialization

  /** Creates a new RADComponent */
  public RADComponent () {
    auxValues = new HashMap (10);
  }

  /** Called to initialize the component with specified FormManager .
  * @param formManager the FormManager of the form into which this component will be added 
  */
  public void initialize (FormManager2 formManager) {
    this.formManager = formManager;
  }

  /** Called to set the bean to be represented by this RADComponent.
  * This method creates a new instance of the bean. This RADComponent class is fully initialized after this method returns.
  * Can be called only once and is mutually exclusive with setInstance ()
  * @param beanClass the class of the bean to be represented by this class
  * @see #setInstance
  */
  public void setComponent (Class beanClass) {
    if (this.beanClass != null) {
      throw new InternalError ("Component already initialized: current: "+this.beanClass +", new: "+beanClass);// NOI18N
    }

    this.beanClass = beanClass;
    beanInstance = createBeanInstance ();
    beanInfo = BeanSupport.createBeanInfo (beanClass);
    
    initInternal ();
  }

  /** Called to set the bean to be represented by this RADComponent.
  * This method uses the instance provided. This RADComponent class is fully initialized after this method returns.
  * Can be called only once and is mutually exclusive with setComponent ()
  * @param beanInstance the bean to be represented by this class
  * @see #setComponent
  */
  public void setInstance (Object beanInstance) {
    if (this.beanClass != null) {
      throw new InternalError ("Component already initialized: current: "+this.beanClass +", new: "+beanClass); // NOI18N
    }
    this.beanClass = beanInstance.getClass ();
    this.beanInstance = beanInstance;
    beanInfo = BeanSupport.createBeanInfo (beanClass);
    
    initInternal ();
    PropertyDescriptor[] props = beanInfo.getPropertyDescriptors ();
    for (int i = 0; i < props.length; i++) {
      RADProperty prop = (RADProperty)nameToProperty.get (props[i].getName ());
      try {
        if ((!prop.canRead ()) || (!prop.canWrite ())) continue; // ignore this property
        Object currentValue = prop.getValue ();
        Object defaultValue = defaultPropertyValues.get (props[i].getName ());
        if (!Utilities.compareObjects (currentValue, defaultValue)) {
          // add the property to the list of changed properties
          prop.setChanged (true);
        }
      } catch (Exception e) {
//        if ( // [PENDING] notify exception
        // simply ignore this property
      }
    }
    // [PENDING - initialize changed properties]
  }

  /** Called to create the instance of the bean. Default implementation simply creates instance 
  * of the bean's class using the default constructor.  Top-level container (the form object itself) 
  * will redefine this to use FormInfo to create the instance, as e.g. Dialogs cannot be created using 
  * the default constructor.
  * Note: this method is called only if the setComponent method is used, if setInstance is used, no new instance is created.
  * @return the instance of the bean that will be used during design time 
  */
  protected Object createBeanInstance () {
    return BeanSupport.createBeanInstance (beanClass);
  }
  
  /** Used by TuborgPersistenceManager */
  void initDeserializedEvents (java.util.Hashtable eventHandlers) {
    eventsList.initEvents (eventHandlers);
  }
  
  void setNodeReference (RADComponentNode node) {
    this.componentNode = node;
    componentNode.addPropertyChangeListener (new java.beans.PropertyChangeListener () {
        public void propertyChange (java.beans.PropertyChangeEvent evt) {
          if (evt.getPropertyName () != null && evt.getPropertyName ().equals ("variableName")) { // NOI18N // [CHECK]
            String oldName = (String) evt.getOldValue();
            String newName = (String) evt.getNewValue();
            EventsList.EventSet[] esets = getEventsList().getEventSets ();
            for (int i=0, n=esets.length; i<n; i++) {
              EventsList.Event [] evts = esets [i].getEvents ();
              for (int j=0, m=evts.length; j<m; j++) {
                String defaultName = FormUtils.getDefaultEventName(oldName, evts[j].getListenerMethod ());
                for (java.util.Iterator iter = evts[j].getHandlers ().iterator (); iter.hasNext();) {
                  EventsManager.EventHandler eh = (EventsManager.EventHandler) iter.next();
                  if (eh.getName ().equals (defaultName)) {
                    String newValue = FormUtils.getDefaultEventName(newName, evts[j].getListenerMethod ());
                    formManager.getEventsManager ().renameEventHandler (eh, newValue);
                    formManager.fireEventRenamed (RADComponent.this, eh, defaultName);
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

  private void initInternal () {
    nameToProperty = new HashMap ();

    syntheticProperties = createSyntheticProperties ();
    beanProperties = createBeanProperties ();
    beanExpertProperties = createBeanExpertProperties ();

    beanEvents = createEventsProperties ();

    defaultPropertyValues = BeanSupport.getDefaultPropertyValues (beanClass);
  }

// -----------------------------------------------------------------------------
// Public interface

  /** Provides access to the Class of the bean represented by this RADComponent
  * @return the Class of the bean represented by this RADComponent
  */
  public Class getBeanClass () {
    return beanClass;
  }

  /** Provides access to the real instance of the bean represented by this RADComponent
  * @return the instance of the bean represented by this RADComponent
  */
  public Object getBeanInstance () {
    return beanInstance;
  }
  
  /** Provides access to BeanInfo of the bean represented by this RADComponent
  * @return the BeanInfo of the bean represented by this RADComponent
  */
  public BeanInfo getBeanInfo () {
    return beanInfo;
  }

  /** This method can be used to check whether the bean represented by this RADCOmponent has hidden-state.
  * @return true if the component has hidden state, false otherwise
  */
  public boolean hasHiddenState () {
    return (getBeanInfo ().getBeanDescriptor ().getValue ("hidden-state") != null); // NOI18N
  }
  
  /** Getter for the Name property of the component - usually maps to variable declaration for holding the 
  * instance of the component
  * @return current value of the Name property
  */
  public String getName () {
    return componentName;
  }

  /** Setter for the Name property of the component - usually maps to variable declaration for holding the 
  * instance of the component
  * @param value new value of the Name property
  */
  public void setName (String value) {
    if ((componentName != null) && (componentName.equals (value))) return; // same name => no change
    if (getFormManager ().getVariablesPool ().findVariableType (value) != null) return; // variable already exist => ignore
    if (!org.openide.util.Utilities.isJavaIdentifier (value)) return;

    String oldName = componentName;
    componentName = value;
    if (oldName != null) {
      getFormManager ().getVariablesPool ().deleteVariable (oldName);
    }
    getFormManager ().getVariablesPool ().createVariable (componentName, beanClass);

    getFormManager ().fireComponentChanged (this, PROP_NAME, oldName, componentName);
    if (getNodeReference () != null) {
      getNodeReference ().updateName ();
    }
  }
  
  /** @return component name preserved between Cut and Paste */
  String getStoredName () {
    return storedName;
  }
  
  /** Can be called to store the component name into special variable to preserve it between Cut and Paste */
  void storeName () {
    storedName = componentName;
  }
  
  /** Allows to add an auxiliary <name, value> pair, which is persistent in Gandalf.
  * The current value can be obtainer using getAuxValue (aux property name) method.
  * To remove aux value for specified property name, use setAuxValue (name, null).
  * @param key name of the aux property
  * @param value new value of the aux property or null to remove it
  */
  public void setAuxValue (String key, Object value) {
    auxValues.put (key, value);
  }

  /** Allows to obtain an auxiliary value for specified aux property name.
  * @param key name of the aux property
  * @return null if the aux value for specified name is not set
  */
  public Object getAuxValue (String key) {
    return auxValues.get (key);
  }
  
  /** Provides access to the FormManager class which manages the form in which this component has been added.
  * The FormManager is the central class for obtaining informations about the form
  * @return the FormManager which manages the form into which this component has been added
  */
  public FormManager2 getFormManager () {
    return formManager;
  }
  
  public EventsList getEventsList () {
    return eventsList;
  }

  /** @return the map of all currently set aux value - pairs of <String, Object> */
  public Map getAuxValues () {
    return auxValues;
  }
  
  /** Support for new types that can be created in this node.
  * @return array of new type operations that are allowed
  */
  public NewType[] getNewTypes () {
    return NO_NEW_TYPES;
  }

  RADComponent.RADProperty[] getAllProperties () {
    if (allProperties == null) {
      Node.Property[] props = getComponentProperties (); 
      Node.Property[] expertProps = getComponentExpertProperties (); 
      ArrayList list = new ArrayList (props.length + expertProps.length);
      list.addAll (Arrays.asList (props));
      list.addAll (Arrays.asList (expertProps));
      allProperties = FormEditor.sortProperties (list, beanClass);
    }

    return allProperties;
  }

  public Node.PropertySet[] getProperties () {
    if (beanPropertySets == null) {
      if (beanExpertProperties.length == 0) {
        // No expert properties
        beanPropertySets = new Node.PropertySet [] {
          new Node.PropertySet (
            "properties", // NOI18N
            FormEditor.getFormBundle().getString("CTL_PropertiesTab"),
            FormEditor.getFormBundle().getString("CTL_PropertiesTabHint")
          ) {
            public Node.Property[] getProperties () {
              return getComponentProperties ();
            }
          },
          new Node.PropertySet (
            "events", // NOI18N
            FormEditor.getFormBundle().getString("CTL_EventsTab"),
            FormEditor.getFormBundle().getString("CTL_EventsTabHint")
          ) {
            public Node.Property[] getProperties () {
              return getComponentEvents ();
            }
          }, 
          new Node.PropertySet (
            "synthetic", // NOI18N
            FormEditor.getFormBundle().getString("CTL_SyntheticTab"),
            FormEditor.getFormBundle().getString("CTL_SyntheticTabHint")
          ) {
            public Node.Property[] getProperties () {
              return getSyntheticProperties ();
            }
          },
        };
      } else {
        // With expert properties
        beanPropertySets = new Node.PropertySet [] {
          new Node.PropertySet (
            "properties", // NOI18N
            FormEditor.getFormBundle().getString("CTL_PropertiesTab"),
            FormEditor.getFormBundle().getString("CTL_PropertiesTabHint")
          ) {
            public Node.Property[] getProperties () {
              return getComponentProperties ();
            }
          },
          new Node.PropertySet (
            "expert", // NOI18N
            FormEditor.getFormBundle().getString("CTL_ExpertTab"),
            FormEditor.getFormBundle().getString("CTL_ExpertTabHint")
          ) {
            public Node.Property[] getProperties () {
              return getComponentExpertProperties ();
            }
          },
          new Node.PropertySet (
            "events", // NOI18N
            FormEditor.getFormBundle().getString("CTL_EventsTab"),
            FormEditor.getFormBundle().getString("CTL_EventsTabHint")
          ) {
            public Node.Property[] getProperties () {
              return getComponentEvents ();
            }
          }, 
          new Node.PropertySet (
            "synthetic", // NOI18N
            FormEditor.getFormBundle().getString("CTL_SyntheticTab"),
            FormEditor.getFormBundle().getString("CTL_SyntheticTabHint")
          ) {
            public Node.Property[] getProperties () {
              return getSyntheticProperties ();
            }
          },
        };
      }
    }
    return beanPropertySets;
  }

  /** Provides access to the Node which represents this RADComponent
  * @return the RADComponentNode which represents this RADComponent
  */
  public RADComponentNode getNodeReference () {
    return componentNode;
  }

// -----------------------------------------------------------------------------
// Access to component Properties

  public Node.Property[] getSyntheticProperties () {
    return syntheticProperties;
  }

  public Node.Property[] getComponentProperties () {
    return beanProperties;
  }
  
  public Node.Property[] getComponentExpertProperties () {
    return beanExpertProperties;
  }

  public Node.Property[] getComponentEvents () {
    return beanEvents;
  }

  /** Can be used to obtain RADProperty of property with specified name
  * @param name the name of the property - the same as returned from PropertyDescriptor.getName ()
  * @return the RADProperty representing the specified property or null if property with specified name does not exist
  */
  public RADProperty getPropertyByName (String name) {
    return (RADProperty) nameToProperty.get (name);
  }
  
  /** This method can be used to obtain default property value of the specified property. 
  * @return the default property value or null, which means that the default value is null or cannot be obtained (write only property, ...)
  */
  public Object getDefaultPropertyValue (RADProperty prop) {
    return defaultPropertyValues.get (prop);
  }

// -----------------------------------------------------------------------------
// Protected interface

  protected boolean hasDefaultEvent () {
    return (eventsList.getDefaultEvent () != null);
  }

  protected void attachDefaultEvent () {
    EventsList.Event defaultEvt = eventsList.getDefaultEvent ();
    if (defaultEvt.getHandlers () == null)
      defaultEvt.createDefaultEventHandler ();
    defaultEvt.gotoEventHandler ();
  }

  protected Node.Property[] createSyntheticProperties () {
    return getFormManager ().getCodeGenerator ().getSyntheticProperties (this);
  }
  
  protected Node.Property[] createBeanProperties () {
    PropertyDescriptor[] props = beanInfo.getPropertyDescriptors ();  
    ArrayList nodeProps = new ArrayList ();
    for (int i = 0; i < props.length; i++) {
      if ((!props[i].isHidden ()) && (!props[i].isExpert ())) {
        Node.Property prop = createProperty (props[i]);
        nodeProps.add (prop);
      }
    }

    Node.Property[] np = new Node.Property [nodeProps.size ()];
    nodeProps.toArray (np);

    return np;
  }

  protected Node.Property[] createBeanExpertProperties () {
    PropertyDescriptor[] props = beanInfo.getPropertyDescriptors ();
    ArrayList nodeProps = new ArrayList ();
    for (int i = 0; i < props.length; i++) {
      if ((!props[i].isHidden ()) && props[i].isExpert ()) {
        Node.Property prop = createProperty (props[i]);
        nodeProps.add (prop);
      }
    }

    Node.Property[] np = new Node.Property [nodeProps.size ()];
    nodeProps.toArray (np);

    return np;
  }

  protected Node.Property[] createEventsProperties () {
    eventsList = new EventsList (this);

    Node.Property[] nodeEvents = new Node.Property[eventsList.getEventCount ()];
    int idx = 0;
    EventsList.EventSet[] eventSets = eventsList.getEventSets ();

    for (int i = 0; i < eventSets.length; i++) {
      EventsList.Event[] events = eventSets[i].getEvents();
      for (int j = 0; j < events.length; j++) {
        Node.Property ep = new EventProperty (events[j]) {

          public Object getValue () {
            if (event.getHandlers ().size () == 0) 
              lastSelectedHandler = null;
            else
              lastSelectedHandler = ((EventsManager.EventHandler) event.getHandlers ().get (0)).getName ();
            return lastSelectedHandler;
          }

          public void setValue (Object val) throws IllegalArgumentException {
            if (!(val instanceof HandlerSetChange)) {
              throw new IllegalArgumentException();
            }
            Hashtable handlersByName = new Hashtable ();
            Vector handlers = event.getHandlers ();
            
            for (Iterator it = handlers.iterator (); it.hasNext (); ) {
              EventsManager.EventHandler h = (EventsManager.EventHandler) it.next ();
              handlersByName.put(h.getName (), h);
            }
            
            HandlerSetChange change = (HandlerSetChange) val;
            if (change.hasAdded ()) {
              for (Iterator iter = change.getAdded ().iterator (); iter.hasNext(); ) {
                String handlerName = (String) iter.next ();
                if (!Utilities.isJavaIdentifier (handlerName)) {
                  System.out.println(handlerName +" is not a Java identifier"); // [PENDING I18N]
                  continue;
                }
                // adding event handler
                formManager.getEventsManager ().addEventHandler (event, handlerName);
                EventsManager.EventHandler handler = (EventsManager.EventHandler) event.getHandlers ().get (event.getHandlers ().size () -1);
                formManager.fireEventAdded (RADComponent.this, handler);
              }
            }
            if (change.hasRenamed ()) {
              for (int k=0, n = change.getRenamedOldNames ().size (); k<n; k++) {
                String oldName = (String) change.getRenamedOldNames ().get (k);
                String newName = (String) change.getRenamedNewNames ().get (k);
                if (!Utilities.isJavaIdentifier (newName)) continue;
                if (newName.equals (oldName)) continue; // no change
                EventsManager.EventHandler handler = (EventsManager.EventHandler) handlersByName.get(oldName);
                formManager.getEventsManager ().renameEventHandler (handler, newName);
                formManager.fireEventRenamed (RADComponent.this, handler, oldName);
              }
            }
            if (change.hasRemoved ()) {
              for (Iterator iter = change.getRemoved ().iterator (); iter.hasNext(); ) {
                EventsManager.EventHandler handler = (EventsManager.EventHandler) handlersByName.get((String) iter.next ());
                formManager.getEventsManager ().removeEventHandler (event, handler);
                formManager.fireEventRemoved (RADComponent.this, handler);
              }
            }
            String newSelectedHandler = ""; // NOI18N
            if (event.getHandlers ().size () >0)
              newSelectedHandler = ((EventsManager.EventHandler) event.getHandlers ().get (0)).getName ();
            getNodeReference ().firePropertyChangeHelper (this.getName (), lastSelectedHandler, newSelectedHandler);
            ((java.beans.PropertyEditorSupport)getPropertyEditor()).firePropertyChange ();
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
  void restorePropertyValue (PropertyDescriptor desc, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    setPropertyValue (desc, value);
    Object defValue = defaultPropertyValues.get (desc.getName ());
    // add the property to the list of changed properties
    RADProperty prop = (RADProperty)nameToProperty.get (desc.getName ());
    prop.setChanged (true);
  }
  
// -----------------------------------------------------------------------------
// Protected interface to working with properties on bean instance

  protected Object getPropertyValue (PropertyDescriptor desc) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (isChangedValue (desc)) {
      return getChangedValue (desc);
    }
    Method readMethod = desc.getReadMethod ();
    if (readMethod == null) {
      throw new IllegalAccessException ();
    }
    return readMethod.invoke (getBeanInstance (), new Object[0]);
  }

  protected void setPropertyValue (PropertyDescriptor desc, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    cacheValue (desc, value);

    // [PENDING - property names to cache]
    if ("enabled".equals (desc.getName ()) || // NOI18N
        "visible".equals (desc.getName ())) // NOI18N
    {
      // values of these properties are just cached, not represented during design-time
      return;
    } 
    
    Method writeMethod = desc.getWriteMethod ();
    if (writeMethod == null) {
      throw new IllegalAccessException ();
    }
    Object valueToSet = value;
    if (value instanceof FormDesignValue) {
      valueToSet = ((FormDesignValue)value).getDesignValue (RADComponent.this);
      if (valueToSet == FormDesignValue.IGNORED_VALUE) return; // ignore this value, as it is not value to be reflected during design-time
    }
    writeMethod.invoke (getBeanInstance (), new Object[] { valueToSet });
  }

  protected Object getIndexedPropertyValue (IndexedPropertyDescriptor desc, int index) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method readMethod = desc.getIndexedReadMethod ();
    if (readMethod == null) {
      throw new IllegalAccessException ();
    }
    return readMethod.invoke (getBeanInstance (), new Object[] { new Integer (index) });
  }
  
  protected void setIndexedPropertyValue (IndexedPropertyDescriptor desc, int index, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method writeMethod = desc.getIndexedWriteMethod ();
    if (writeMethod == null) {
      throw new IllegalAccessException ();
    }
    writeMethod.invoke (getBeanInstance (), new Object[] { new Integer (index), value });
  }

  protected void cacheValue (PropertyDescriptor desc, Object value) {
    if (valuesCache == null) {
      valuesCache = new HashMap (10);
      editorsCache = new HashMap (10);
    }
    valuesCache.put (desc, value);
  }
  
  protected boolean isChangedValue (PropertyDescriptor desc) {
    if (valuesCache == null) {
      return false;
    }
    return valuesCache.containsKey (desc);
  }

  protected Object getChangedValue (PropertyDescriptor desc) {
    if (valuesCache == null) {
      throw new InternalError ();
    }
    return valuesCache.get (desc);
  }
  
// -----------------------------------------------------------------------------
// Debug methods

  public java.lang.String toString () {
    return super.toString () + ", name: "+getName ()+", class: "+getBeanClass ()+", beaninfo: "+getBeanInfo () + ", instance: "+getBeanInstance (); // NOI18N
  }
  
  public void debugChangedValues () {
    if (System.getProperty ("netbeans.debug.form.full") != null) { // NOI18N
      System.out.println("-- debug.form: Changed property values in: "+this+" -------------------------"); // NOI18N
      for (java.util.Iterator it = nameToProperty.values ().iterator (); it.hasNext ();) {
        RADProperty prop = (RADProperty)it.next ();
        if (prop.isChanged ()) {
          PropertyDescriptor desc = prop.getPropertyDescriptor ();
          try {
            System.out.println("Changed Property: "+desc.getName ()+", value: "+prop.getValue ()); // NOI18N
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

  public void notifyPropertiesChange () {
    if (componentNode != null) componentNode.notifyPropertiesChange ();
  }

  public interface RADProperty {
    public String getName ();
    public PropertyDescriptor getPropertyDescriptor ();
    public PropertyEditor getPropertyEditor ();
    public PropertyEditor getCurrentEditor ();
    public PropertyEditor getExpliciteEditor ();
    public void setCurrentEditor (PropertyEditor editor);
    public RADComponent getRADComponent ();
    public boolean canRead ();
    public Object getValue () throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
    public boolean canWrite ();
    public void setValue (Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
    public boolean supportsDefaultValue ();
    public void restoreDefaultValue ();
    public Object getDefaultValue ();

    public String getPreCode ();
    public String getPostCode ();
    public void setPreCode (String value);
    public void setPostCode (String value);

    public boolean isChanged ();
    public void setChanged (boolean value);
  }

  private Node.Property createProperty (final PropertyDescriptor desc) {
    Node.Property prop;
    if (desc instanceof IndexedPropertyDescriptor) {
      prop = new RADIndexedPropertyImpl ((IndexedPropertyDescriptor)desc);
    } else { 
      prop = new RADPropertyImpl (desc);
    }

    prop.setName (desc.getName ());
    prop.setDisplayName (desc.getDisplayName ());
    prop.setShortDescription (desc.getShortDescription ());

    nameToProperty.put (desc.getName (), prop);
    return prop;
  }

  class RADPropertyImpl extends Node.Property implements RADProperty {
    private PropertyEditor currentPropertyEditor;
    private PropertyDescriptor desc;
    String preCode = null;                      // custom pre-initialization code to be used before calling the property setter
    String postCode = null;                     // custom post-initialization code to be used after calling the property setter
    boolean changed = false;

    RADPropertyImpl (PropertyDescriptor desc) {
      super (desc.getPropertyType ());
      this.desc = desc;
    }

    public PropertyDescriptor getPropertyDescriptor () {
      return desc;
    }

    public RADComponent getRADComponent () {
      return RADComponent.this;
    }

    public PropertyEditor getCurrentEditor () {
      if (currentPropertyEditor == null) {
        currentPropertyEditor = findDefaultEditor (desc);
      }
      return currentPropertyEditor;
    }
    
    public void setCurrentEditor (PropertyEditor value) {
      currentPropertyEditor = value;
    }

    /** Test whether the property is readable.
    * @return <CODE>true</CODE> if it is
    */
    public boolean canRead () {
      return (desc.getReadMethod () != null);
    }

    /** Get the value.
    * @return the value of the property
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public Object getValue () throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      return getPropertyValue (desc);
    }

    /** Test whether the property is writable.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canWrite () {
      return (desc.getWriteMethod () != null);
    }

    /** Set the value.
    * @param val the new value of the property
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public void setValue (Object val) throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
      Object old = null;
      
      if (canRead ()) {
        try {
          old = getValue ();
        } catch (IllegalArgumentException e) {  // no problem -> keep null
        } catch (IllegalAccessException e) {    // no problem -> keep null
        } catch (InvocationTargetException e) { // no problem -> keep null
        }
      }

      if (old == val) return; // no change
      if ((old != null) && (val != null) && (val.equals (old))) return; // no change

      try {
        setPropertyValue (desc, val);
      } catch (IllegalArgumentException e) {  // no problem -> keep null
      } catch (IllegalAccessException e) {    // no problem -> keep null
      } catch (InvocationTargetException e) { // no problem -> keep null
      }
      

      boolean isChanged = false;
      if (defaultPropertyValues.containsKey (desc.getName ())) { // if there is reasonable default
        Object defValue = defaultPropertyValues.get (desc.getName ());
        isChanged = !Utilities.compareObjects (defValue, val);
      } else { // no default => always treat is as changed
        isChanged = true;
      }
      setChanged (isChanged);
      
      debugChangedValues ();

      getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), old, val);
      if (componentNode != null) componentNode.firePropertyChangeHelper (RADPropertyImpl.this.getName (), old, val);

      if (RADComponent.this instanceof RADVisualComponent) {
        if (beanInstance instanceof javax.swing.JComponent) {
          ((javax.swing.JComponent)beanInstance).repaint ();
          ((javax.swing.JComponent)beanInstance).revalidate ();
        } else {
          java.awt.Container cc = ((java.awt.Component)beanInstance).getParent ();
          if ((cc != null) && (cc.getParent () != null)) {
            cc.getParent ().validate ();
          }
        }
      }
    }

    /** Test whether the property had a default value.
    * @return <code>true</code> if it does
    */
    public boolean supportsDefaultValue () {
      return defaultPropertyValues.containsKey (desc.getName ()); // true if there is reasonable default
    }

    /** Restore this property to its default value, if supported.
    */
    public void restoreDefaultValue () {
      // 1. remove the property from list of changed values, so that the code for it is not generated
      setChanged (false);
      
      Object old = null;
      
      if (canRead ()) {
        try {
          old = getValue ();
        } catch (IllegalArgumentException e) {  // no problem -> keep null
        } catch (IllegalAccessException e) {    // no problem -> keep null
        } catch (InvocationTargetException e) { // no problem -> keep null
        }
      }
      
      // 2. restore the default property value
      if (defaultPropertyValues.containsKey (desc.getName ())) { // if there is reasonable default
        Object def = defaultPropertyValues.get (desc.getName ());
        try {
          setValue (def);
        } catch (IllegalAccessException e) {
          // what to do, ignore...
        } catch (IllegalArgumentException e) {
          // what to do, ignore...
        } catch (InvocationTargetException e) {
          // what to do, ignore...
        }
        getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), old, def);
        notifyPropertiesChange ();
        if (componentNode != null) componentNode.firePropertyChangeHelper (RADPropertyImpl.this.getName (), old, def);
      }
      // [PENDING - test]
    }

    public Object getDefaultValue () {
      return defaultPropertyValues.get (desc.getName ());
    }
    
    /* Returns property editor for this property.
    * @return the property editor or <CODE>null</CODE> if there should not be
    *    any editor.
    */
    public PropertyEditor getPropertyEditor () {
      // FormPropertyEditor is one of the advanced features that must be supported bu the 
      // persistence manager to be available
      if (!getFormManager ().getFormEditorSupport ().supportsAdvancedFeatures ()) {
        PropertyEditor prEd = findDefaultEditor (desc);
        if (prEd instanceof FormAwareEditor) {
          ((FormAwareEditor)prEd).setRADComponent (RADComponent.this, RADPropertyImpl.this);
        }
        if (prEd instanceof org.openide.explorer.propertysheet.editors.NodePropertyEditor) {
          ((org.openide.explorer.propertysheet.editors.NodePropertyEditor)prEd).attach (new org.openide.nodes.Node[] { getNodeReference () });
        }
        return prEd;
      }
      // the property editor cannot be reused as it is not reentrant !!! [IAN]
      PropertyEditor defaultEditor = findDefaultEditor (desc);
      FormPropertyEditor editor = null;
      if (defaultEditor != null) {
        editor = new FormPropertyEditor (RADComponent.this, desc.getPropertyType (), RADPropertyImpl.this);
      }
      return editor;
    }

    public PropertyEditor getExpliciteEditor () {
      if (desc.getPropertyEditorClass () != null) {
        try {
          return (PropertyEditor) desc.getPropertyEditorClass ().newInstance ();
        } catch (Exception ex) {
          if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace (); // NOI18N
        }
      } 
      return null;
    }
    
    private PropertyEditor findDefaultEditor (PropertyDescriptor desc) {
      PropertyEditor defaultEditor = getExpliciteEditor ();
      if (defaultEditor == null) {
        return FormPropertyEditorManager.findEditor (desc.getPropertyType ());
      } else {
        return defaultEditor;
      }
    }

    public String getPreCode () {
      return preCode;
    }

    public void setPreCode (String value) {
      preCode = value;
      getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), null, null);
    }

    public String getPostCode () {
      return postCode;
    }

    public void setPostCode (String value) {
      postCode = value;
      getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), null, null);
    }

    public boolean isChanged () {
      return changed;
    }

    public void setChanged (boolean value) {
      changed = value;
    }
  }

  
  class RADIndexedPropertyImpl extends Node.IndexedProperty implements RADProperty {
    private PropertyEditor currentEditor;
    private IndexedPropertyDescriptor desc;
    String preCode = null;                      // custom pre-initialization code to be used before calling the property setter
    String postCode = null;                     // custom post-initialization code to be used after calling the property setter
    boolean changed = false;
    
    RADIndexedPropertyImpl (IndexedPropertyDescriptor desc) {
      super (getIndexedType (desc), desc.getIndexedPropertyType ());
      this.desc = desc;
      currentEditor = findDefaultIndexedEditor (desc);
    }

    public PropertyDescriptor getPropertyDescriptor () {
      return desc;
    }

    public PropertyEditor getCurrentEditor () {
      return currentEditor;
    }
    
    public void setCurrentEditor (PropertyEditor editor) {
      currentEditor = editor;
    }

    public RADComponent getRADComponent () {
      return RADComponent.this;
    }

    /** Test whether the property is readable.
    * @return <CODE>true</CODE> if it is
    */
    public boolean canRead () {
      return (desc.getReadMethod () != null);
    }

    /** Get the value.
    * @return the value of the property
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public Object getValue () throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      return getPropertyValue (desc);
    }

    /** Test whether the property is writable.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canWrite () {
      return (desc.getWriteMethod () != null);
    }

    /** Set the value.
    * @param val the new value of the property
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public void setValue (Object val) throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
      Object old = null;
      
      if (canRead ()) {
        try {
          old = getValue ();
          if (Utilities.compareObjects(old, val)) return; // no change
        } catch (IllegalArgumentException e) {  // no problem -> keep null
        } catch (IllegalAccessException e) {    // no problem -> keep null
        } catch (InvocationTargetException e) { // no problem -> keep null
        }
      }
      

      try {
        setPropertyValue (desc, val);
      } catch (IllegalArgumentException e) {  // no problem -> keep null
      } catch (IllegalAccessException e) {    // no problem -> keep null
      } catch (InvocationTargetException e) { // no problem -> keep null
      }
      
      boolean isChanged = false;
      if (defaultPropertyValues.containsKey (desc.getName ())) { // if there is reasonable default
        Object defValue = defaultPropertyValues.get (desc.getName ());
        isChanged = !Utilities.compareObjects (defValue, val);
      } else { // no default => always treat is as changed
        isChanged = true;
      }
      setChanged (isChanged);
      debugChangedValues ();
      getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), old, val);
      if (componentNode != null) componentNode.firePropertyChangeHelper (RADIndexedPropertyImpl.this.getName (), old, val);
      if (RADComponent.this instanceof RADVisualComponent) {
        if (beanInstance instanceof javax.swing.JComponent) {
          ((javax.swing.JComponent)beanInstance).repaint ();
          ((javax.swing.JComponent)beanInstance).revalidate ();
        } else {
          java.awt.Container cc = ((java.awt.Component)beanInstance).getParent ();
          if ((cc != null) && (cc.getParent () != null)) {
            cc.getParent ().validate ();
          }
        }
      }
    }

    /** Test whether the property had a default value.
    * @return <code>true</code> if it does
    */
    public boolean supportsDefaultValue () {
      return defaultPropertyValues.containsKey (desc.getName ()); // true if there is reasonable default
    }

    /** Restore this property to its default value, if supported.
    */
    public void restoreDefaultValue () {
      // 1. remove the property from list of changed values, so that the code for it is not generated
      setChanged (false);
      
      Object old = null;
      
      if (canRead ()) {
        try {
          old = getValue ();
        } catch (IllegalArgumentException e) {  // no problem -> keep null
        } catch (IllegalAccessException e) {    // no problem -> keep null
        } catch (InvocationTargetException e) { // no problem -> keep null
        }
      }
      
      // 2. restore the default property value
      if (defaultPropertyValues.containsKey (desc.getName ())) { // if there is reasonable default
        Object def = defaultPropertyValues.get (desc.getName ());
        try {
          setValue (def);
        } catch (IllegalAccessException e) {
          // what to do, ignore...
        } catch (IllegalArgumentException e) {
          // what to do, ignore...
        } catch (InvocationTargetException e) {
          // what to do, ignore...
        }
        getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), old, def);
        if (componentNode != null) componentNode.firePropertyChangeHelper (RADIndexedPropertyImpl.this.getName (), old, def);
      }
      // [PENDING - test]
    }

    public Object getDefaultValue () {
      return defaultPropertyValues.get (desc.getName ());
    }
    
    /* Returns property editor for this property.
    * @return the property editor or <CODE>null</CODE> if there should not be
    *    any editor.
    */
    public PropertyEditor getPropertyEditor () {
      // FormPropertyEditor is one of the advanced features that must be supported bu the 
      // persistence manager to be available
      if (!getFormManager ().getFormEditorSupport ().supportsAdvancedFeatures ()) {
        PropertyEditor prEd = findDefaultIndexedEditor (desc);
        if (prEd instanceof FormAwareEditor) {
          ((FormAwareEditor)prEd).setRADComponent (RADComponent.this, RADIndexedPropertyImpl.this);
        }
        if (prEd instanceof org.openide.explorer.propertysheet.editors.NodePropertyEditor) {
          ((org.openide.explorer.propertysheet.editors.NodePropertyEditor)prEd).attach (new org.openide.nodes.Node[] { getNodeReference () });
        }
        return prEd;
      }

      // the property editor cannot be reused as it is not reentrant !!! [IAN]

      PropertyEditor defaultEditor = findDefaultIndexedEditor (desc);
      FormPropertyEditor editor = null;
      if (defaultEditor != null) {
        editor = new FormPropertyEditor (RADComponent.this, desc.getIndexedPropertyType (), RADIndexedPropertyImpl.this);
      }
      return editor;
    }

    public PropertyEditor getExpliciteEditor () {
      if (desc.getPropertyEditorClass () != null) {
        try {
          return (PropertyEditor) desc.getPropertyEditorClass ().newInstance ();
        } catch (Exception ex) {
          if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace (); // NOI18N
        }
      } 
      return null;
    }
    
    private PropertyEditor findDefaultIndexedEditor (IndexedPropertyDescriptor desc) {
      PropertyEditor defaultEditor = getExpliciteEditor ();
      if (defaultEditor == null) {
        return FormPropertyEditorManager.findEditor (desc.getIndexedPropertyType ());
      } else {
        return defaultEditor;
      }
    }

    /** Test whether the property is readable by index.
    * @return <CODE>true</CODE> if so
    */
    public boolean canIndexedRead () {
      return (desc.getIndexedReadMethod () != null);
    }

    /** Get the value of the property at an index.
    *
    * @param indx the index
    * @return the value at that index
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public Object getIndexedValue (int index) throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      return null;
      // [PENDING indexed]
    }

    /** Test whether the property is writable by index.
    * @return <CODE>true</CODE> if so
    */
    public boolean canIndexedWrite () {
      return (desc.getIndexedWriteMethod () != null);
    }

    /** Set the value of the property at an index.
    *
    * @param indx the index
    * @param val the value to set
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public void setIndexedValue (int indx, Object val) throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
      // [PENDING indexed]
    }

    /** Get a property editor for individual elements in this property.
    * @return the property editor for elements
    */
//    public PropertyEditor getIndexedPropertyEditor () { // [PENDING indexed]
//      return java.beans.PropertyEditorManager.findEditor (elementType);
//    }

    public String getPreCode () {
      return preCode;
    }

    public void setPreCode (String value) {
      preCode = value;
      getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), null, null);
    }

    public String getPostCode () {
      return postCode;
    }

    public void setPostCode (String value) {
      postCode = value;
      getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), null, null);
    }

    public boolean isChanged () {
      return changed;
    }

    public void setChanged (boolean value) {
      changed = value;
    }
  }

  /** Utility method for obtaining array type for indexed properties */  
  private static Class getIndexedType (IndexedPropertyDescriptor desc) {
    Class valueType = desc.getPropertyType ();
    if (valueType == null) {
      try {
        valueType = org.openide.TopManager.getDefault ().currentClassLoader ().loadClass (
          "[L" + desc.getIndexedPropertyType ().getName () + ";" // NOI18N
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

    EventProperty (EventsList.Event event) {
      super (FormEditor.EVENT_PREFIX + event.getName(),
            String.class,
            event.getName(),
            event.getName());
      this.event = event;
    }

    /** Returns property editor for this property.
    * @return the property editor or <CODE>null</CODE> if there should not be
    *    any editor.
    */
    public java.beans.PropertyEditor getPropertyEditor () {
      return new EventEditor ();
    }                               

    class HandlerSetChange {
      boolean hasAdded () { 
        return (added !=null && added.size()>0);
      }
      boolean hasRemoved () {
        return (removed !=null && removed.size()>0);
      }     
      boolean hasRenamed () {
        return (renamedOldName !=null && renamedOldName.size()>0);
      }
      Vector getAdded () {
        if (added == null) added = new Vector ();
        return added;
      }
      Vector getRemoved () {
        if (removed == null) removed = new Vector ();
        return removed;
      }
      Vector getRenamedOldNames () {
        if (renamedOldName == null) renamedOldName = new Vector ();
        return renamedOldName;
      }
      Vector getRenamedNewNames () {
        if (renamedNewName == null) renamedNewName = new Vector ();
        return renamedNewName;
      }
      private Vector added;
      private Vector removed;
      private Vector renamedOldName;
      private Vector renamedNewName;
    }
    
    class EventEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
      public String getAsText () {
        if (getValue () == null)
          return FormEditor.getFormBundle().getString("CTL_NoEvent");
        else 
          return (String) getValue ();
      }

      public void setAsText (String selected) {
        HandlerSetChange change = new HandlerSetChange ();
        if (getValue () == null) {               // new event
          change.getAdded ().add (selected);
        } else {                                 // rename
          change.getRenamedNewNames ().add (selected);
          change.getRenamedOldNames ().add (getAsText ());
        }
        setValue (change);
      }

      public boolean supportsEditingTaggedValues () {
        return false;
      }
      /**
      * @return Returns custom property editor to be showen inside the property
      *         sheet.
      */
      public java.awt.Component getInPlaceCustomEditor () {
        if (formManager.getFormEditorSupport ().supportsAdvancedFeatures ()) {
          final javax.swing.JComboBox eventCombo = new javax.swing.JComboBox ();
          eventCombo.setEditable (true);
          
          Vector handlers = event.getHandlers ();
          if (handlers.size () == 0) {
            eventCombo.getEditor().setItem(FormUtils.getDefaultEventName (RADComponent.this, event.getListenerMethod ()));
          } else {
            for (int i=0, n=handlers.size(); i<n; i++) {
              eventCombo.addItem (((EventsManager.EventHandler) handlers.get(i)).getName ()); // [PENDING]
            }
          }
          
          eventCombo.addActionListener(new java.awt.event.ActionListener () {
              public void actionPerformed (java.awt.event.ActionEvent e) {               
                String selected = (String) eventCombo.getEditor().getItem();
                lastSelectedHandler = selected;
                event.gotoEventHandler (selected);              
              }
            }
          );
          eventCombo.addFocusListener (new java.awt.event.FocusAdapter () {
              public void focusGained (java.awt.event.FocusEvent evt) {
                Vector hand = event.getHandlers ();
                eventCombo.removeAllItems ();
                if (hand.size () == 0) {
                  eventCombo.getEditor().setItem(FormUtils.getDefaultEventName (RADComponent.this, event.getListenerMethod ()));
                } else {
                  for (int i=0, n=hand.size(); i<n; i++) {
                    eventCombo.addItem (((EventsManager.EventHandler) hand.get(i)).getName ());
                  }
                }
              }
            }
          );
          eventCombo.getEditor().addActionListener(new java.awt.event.ActionListener () {
              public void actionPerformed (java.awt.event.ActionEvent e) {
                String selected = (String) eventCombo.getEditor().getItem();
                lastSelectedHandler = selected;
                boolean isNew = true;
                String items[] = new String[eventCombo.getItemCount()];
                for (int i=0, n=eventCombo.getItemCount(); i<n; i++) {
                  items[i] = (String) eventCombo.getItemAt(i);
                  if(eventCombo.getItemAt(i).equals(selected)) {
                    isNew = false;
                  }
                }
                if(isNew) {
                  HandlerSetChange change = new HandlerSetChange ();
                  change.getAdded ().add (selected);
                  setValue (change);
                  eventCombo.addItem(selected);
                }
                event.gotoEventHandler (selected);
              }
            }
          );
          return eventCombo;
        } else {
          final JTextField eventField = new JTextField ();
          Vector handlers = event.getHandlers ();
          if (handlers.size () == 0) {
            eventField.setText (FormUtils.getDefaultEventName (RADComponent.this, event.getListenerMethod ()));
          } else {
            eventField.setText (((EventsManager.EventHandler) handlers.get(0)).getName ());
          }
          eventField.addActionListener (new java.awt.event.ActionListener () {
              public void actionPerformed (java.awt.event.ActionEvent e) {
                setAsText (eventField.getText ());
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
      public boolean hasInPlaceCustomEditor () {
        return true;
      }

      public boolean supportsCustomEditor () {
        return true;
      }
      
      public java.awt.Component getCustomEditor () {
        final EventCustomEditor ed = new EventCustomEditor (EventProperty.this);
        DialogDescriptor dd = new DialogDescriptor (ed, "Handlers for " + event.getName (), true, // [PENDING I18N]
          new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
              if (evt.getActionCommand ().equalsIgnoreCase ("ok")) { // [PENDING I18N]
                ed.doChanges ();
              }
            }
          }
        );
        return TopManager.getDefault ().createDialog (dd);
      }
    }
  }
}

/*
 * Log
 *  72   Gandalf   1.71        1/13/00  Pavel Buzek     setText() added to 
 *       EventEditor (it did not work with forms that do not support advanced 
 *       features)
 *  71   Gandalf   1.70        1/12/00  Pavel Buzek     I18N
 *  70   Gandalf   1.69        1/10/00  Pavel Buzek     
 *  69   Gandalf   1.68        1/7/00   Pavel Buzek     patch created in rev. 63
 *       was removed (fixed in bean info)
 *  68   Gandalf   1.67        1/5/00   Ian Formanek    NOI18N
 *  67   Gandalf   1.66        1/3/00   Ian Formanek    
 *  66   Gandalf   1.65        1/2/00   Ian Formanek    Fixed to compile
 *  65   Gandalf   1.64        1/1/00   Ian Formanek    Syntheti tab renamed to 
 *       Code Generation, I18Nzed
 *  64   Gandalf   1.63        12/17/99 Pavel Buzek     patch for 
 *       java.beans.Inspector error (incorrect inspection of java.awt.Cursor)
 *  63   Gandalf   1.62        12/16/99 Pavel Buzek     
 *  62   Gandalf   1.61        12/13/99 Pavel Buzek     
 *  61   Gandalf   1.60        11/26/99 Pavel Buzek     
 *  60   Gandalf   1.59        11/26/99 Pavel Buzek     EventCustomEditor 
 *       changed to panel, displayed via DialogDescriptor
 *  59   Gandalf   1.58        11/25/99 Ian Formanek    Uses Utilities module
 *  58   Gandalf   1.57        11/25/99 Pavel Buzek     support for multiple 
 *       handlers for one event
 *  57   Gandalf   1.56        11/15/99 Ian Formanek    Fixed bug 4717 - On JDK 
 *       1.3, added JInternalFrames are not visible and the generated code does 
 *       not contain the required setVisible call.
 *  56   Gandalf   1.55        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  55   Gandalf   1.54        9/24/99  Ian Formanek    Fixed bug with 
 *       setChanged flag on properties
 *  54   Gandalf   1.53        9/24/99  Ian Formanek    New system of changed 
 *       properties in RADComponent - Fixes bug 3584 - Form Editor should try to
 *       enforce more order in the XML elements in .form.
 *  53   Gandalf   1.52        9/17/99  Ian Formanek    Fixed bug 1825 - 
 *       Property sheets are not synchronized 
 *  52   Gandalf   1.51        9/12/99  Ian Formanek    Fixed setPre/PostCode
 *  51   Gandalf   1.50        9/12/99  Ian Formanek    FormAwareEditor.setRADComponent
 *        changes
 *  50   Gandalf   1.49        9/10/99  Ian Formanek    Pre/Post code added to 
 *       RADProperty
 *  49   Gandalf   1.48        9/7/99   Ian Formanek    Properties access and 
 *       RADProperty interface made public
 *  48   Gandalf   1.47        9/6/99   Ian Formanek    Fixed bug 3252 - 
 *       FormEditor - Label sometimes forgets to resize itself after font 
 *       change.
 *  47   Gandalf   1.46        9/6/99   Ian Formanek    
 *  46   Gandalf   1.45        9/2/99   Ian Formanek    Fixed bug 3698 - When 
 *       the event handler is added or modified, the focus is not transfered to 
 *       the editor.
 *  45   Gandalf   1.44        9/2/99   Ian Formanek    Fixed bug 3696 - When 
 *       connection is copied and pasted into form, the initialization code of 
 *       the ConnectionSource component is not correctly generated. and 3695 - 
 *       Modified properties with null value are not restored correctly when a 
 *       form is reloaded.
 *  44   Gandalf   1.43        8/18/99  Ian Formanek    Fixed bug 3475 - When 
 *       the custom property editor for some properties is cancelled, the setter
 *       code for this property becomes generated.
 *  43   Gandalf   1.42        8/17/99  Ian Formanek    Fixed work with multiple
 *       property editors
 *  42   Gandalf   1.41        8/16/99  Ian Formanek    Fixed bug 3369 - The 
 *       expert properties of beans used in form editor are not accessible, 
 *       beans with no expert properties have empty expert tab.
 *  41   Gandalf   1.40        8/9/99   Ian Formanek    Used currentClassLoader 
 *       to fix problems with loading beans only present in repository
 *  40   Gandalf   1.39        8/6/99   Ian Formanek    setComponent is public, 
 *       added method setInstance
 *  39   Gandalf   1.38        8/1/99   Ian Formanek    NodePropertyEditor 
 *       employed
 *  38   Gandalf   1.37        8/1/99   Ian Formanek    Fixed bug which caused 
 *       properties and property editors to begave potentially very strangely
 *  37   Gandalf   1.36        7/30/99  Ian Formanek    fixed firing event added
 *  36   Gandalf   1.35        7/29/99  Ian Formanek    Fixed bug where form 
 *       aware property editors were not correctly initialized if advanced 
 *       features were not used.
 *  35   Gandalf   1.34        7/28/99  Ian Formanek    Fixed bug 1851 - One can
 *       use a java keywords as variable names .
 *  34   Gandalf   1.33        7/28/99  Ian Formanek    Fixed problem with 
 *       explicite editors when the Tuborg format is preserved (i.e. the 
 *       advanced features not used)
 *  33   Gandalf   1.32        7/25/99  Ian Formanek    Variables management 
 *       moved to RADComponent
 *  32   Gandalf   1.31        7/23/99  Ian Formanek    Fixed firing property 
 *       changes when restoring default value, improved performance when opening
 *       form / adding components
 *  31   Gandalf   1.30        7/16/99  Ian Formanek    default action
 *  30   Gandalf   1.29        7/11/99  Ian Formanek    FormPropertyEditor is 
 *       provided only if the current persistence manager 
 *       supportsAdvancedFeatures ()
 *  29   Gandalf   1.28        7/5/99   Ian Formanek    NewTypes added
 *  28   Gandalf   1.27        7/5/99   Ian Formanek    getComponentInstance->getBeanInstance,
 *        getComponentClass->getBeanClass
 *  27   Gandalf   1.26        7/2/99   Ian Formanek    Fixed bug #2310 - 
 *       Selecting multiple components in Form Window (Hold CTRL) throws an 
 *       exception
 *  26   Gandalf   1.25        6/30/99  Ian Formanek    Added hasHiddenState 
 *       method
 *  25   Gandalf   1.24        6/30/99  Ian Formanek    AuxiliaryValue -> 
 *       AuxValue
 *  24   Gandalf   1.23        6/30/99  Ian Formanek    reflecting change in 
 *       enhanced property editors interfaces
 *  23   Gandalf   1.22        6/27/99  Ian Formanek    Employed 
 *       FormDesignValue.IGNORED_VALUE, Indexed properties fixed
 *  22   Gandalf   1.21        6/27/99  Ian Formanek    !!! Caches all changed 
 *       property values !!!
 *  21   Gandalf   1.20        6/24/99  Ian Formanek    Improved 
 *       FormPropertyEditor towards accepting multiple editors
 *  20   Gandalf   1.19        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  19   Gandalf   1.18        6/6/99   Ian Formanek    New FormInfo design 
 *       employed to provide correct top-level bean properties
 *  18   Gandalf   1.17        5/31/99  Ian Formanek    
 *  17   Gandalf   1.16        5/31/99  Ian Formanek    
 *  16   Gandalf   1.15        5/30/99  Ian Formanek    
 *  15   Gandalf   1.14        5/26/99  Ian Formanek    cleaned
 *  14   Gandalf   1.13        5/24/99  Ian Formanek    
 *  13   Gandalf   1.12        5/23/99  Ian Formanek    Support for 
 *       FormAwareEditor and FormDesignValue
 *  12   Gandalf   1.11        5/15/99  Ian Formanek    
 *  11   Gandalf   1.10        5/15/99  Ian Formanek    
 *  10   Gandalf   1.9         5/14/99  Ian Formanek    
 *  9    Gandalf   1.8         5/14/99  Ian Formanek    
 *  8    Gandalf   1.7         5/12/99  Ian Formanek    
 *  7    Gandalf   1.6         5/11/99  Ian Formanek    Build 318 version
 *  6    Gandalf   1.5         5/10/99  Ian Formanek    
 *  5    Gandalf   1.4         5/5/99   Ian Formanek    
 *  4    Gandalf   1.3         5/4/99   Ian Formanek    Package change
 *  3    Gandalf   1.2         4/29/99  Ian Formanek    
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
