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
import org.openide.nodes.*;
import org.openide.util.Utilities;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextField;

/* TODO
 - gotoMethod - jumping to newly created event handlers
 - indexed properties
*/

/** RADComponent is a class which represents a single component used and instantiated
* during design time.  It provides its properties and events.
*
* @author Ian Formanek
*/
public class RADComponent {

// -----------------------------------------------------------------------------
// Static variables

  public static final String SYNTHETIC_PREFIX = "synthetic_";
  public static final String PROP_NAME = SYNTHETIC_PREFIX + "Name";

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

  private HashMap auxValues;
  private HashMap changedPropertyValues;
  private HashMap valuesCache;
  private HashMap editorsCache;
  private HashMap nameToProperty;
  private Map defaultPropertyValues;

  private FormManager2 formManager;
  private EventsList eventsList;

  // FINALIZE DEBUG METHOD
  public void finalize () throws Throwable {
    super.finalize ();
    if (System.getProperty ("netbeans.debug.form.finalize") != null) {
      System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this);
    }
  } // FINALIZE DEBUG METHOD
  
// -----------------------------------------------------------------------------
// Constructors

  public RADComponent () {
    changedPropertyValues = new HashMap (30);
    auxValues = new HashMap (10);
  }

  public void initialize (FormManager2 formManager) {
    this.formManager = formManager;
  }

  /** USed by TuborgPersistenceManager */
  void initDeserializedEvents (java.util.Hashtable eventHandlers) {
    eventsList.initEvents (eventHandlers);
  }
  
  void setComponent (Class beanClass) {
    if (this.beanClass != null) {
      throw new InternalError ("Component already initialized: current: "+this.beanClass +", new: "+beanClass);
    }

    this.beanClass = beanClass;
    beanInstance = createBeanInstance ();
    beanInfo = BeanSupport.createBeanInfo (beanClass);
    
    nameToProperty = new HashMap ();

    syntheticProperties = createSyntheticProperties ();
    beanProperties = createBeanProperties ();
    beanExpertProperties = createBeanExpertProperties ();

    beanEvents = createEventsProperties ();

    changedPropertyValues = new HashMap ();
    defaultPropertyValues = BeanSupport.getDefaultPropertyValues (beanClass);
  }

  /** Called to create the instance of the bean. Default implementation simply creates instance 
  * of the bean's class using the default constructor.  Top-level container (the form object itself) 
  * will redefine this to use FormInfo to create the instance, as e.g. Dialogs cannot be created using 
  * the default constructor 
  * @return the instance of the bean that will be used during design time 
  */
  protected Object createBeanInstance () {
    return BeanSupport.createBeanInstance (beanClass);
  }
  
  void setNodeReference (RADComponentNode node) {
    this.componentNode = node;
  }

  RADComponentNode getNodeReference () {
    return componentNode;
  }

// -----------------------------------------------------------------------------
// Public interface

  public Class getComponentClass () {
    return beanClass;
  }

  public Object getComponentInstance () {
    return beanInstance;
  }
  
  public BeanInfo getBeanInfo () {
    return beanInfo;
  }

  public boolean hasHiddenState () {
    return (getBeanInfo ().getBeanDescriptor ().getValue ("hidden-state") != null);
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
    String oldValue = componentName;
    componentName = value;
    getFormManager ().fireComponentChanged (this, PROP_NAME, oldValue, componentName);
    if (getNodeReference () != null) {
      getNodeReference ().updateName ();
    }
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
  
  public FormManager2 getFormManager () {
    return formManager;
  }
  
  public EventsList getEventsList () {
    return eventsList;
  }

  /** @return the map of all changed properties - pairs of <RADProperty, Object> */
  public Map getChangedProperties () {
    return changedPropertyValues;
  }

  /** @return the map of all currently set aux value - pairs of <String, Object> */
  public Map getAuxValues () {
    return auxValues;
  }
  
  public Node.PropertySet[] getProperties () {
    if (beanPropertySets == null) {
      if (beanExpertProperties.length != 0) {
        // No expert properties
        beanPropertySets = new Node.PropertySet [] {
          new Node.PropertySet ("synthetic", "Synthetic", "Synthetic Properties") {
            public Node.Property[] getProperties () {
              return getSyntheticProperties ();
            }
          },
          new Node.PropertySet ("properties", "Properties", "Properties") {
            public Node.Property[] getProperties () {
              return getComponentProperties ();
            }
          },
          new Node.PropertySet ("events", "Events", "Events") {
            public Node.Property[] getProperties () {
              return getComponentEvents ();
            }
          }, 
        };
      } else {
        // With expert properties
        beanPropertySets = new Node.PropertySet [] {
          new Node.PropertySet ("synthetic", "Synthetic", "Synthetic Properties") {
            public Node.Property[] getProperties () {
              return getSyntheticProperties ();
            }
          },
          new Node.PropertySet ("properties", "Properties", "Properties") {
            public Node.Property[] getProperties () {
              return getComponentProperties ();
            }
          },
          new Node.PropertySet ("expert", "Expert", "Expert Properties") {
            public Node.Property[] getProperties () {
              return getComponentExpertProperties ();
            }
          },
          new Node.PropertySet ("events", "Events", "Events") {
            public Node.Property[] getProperties () {
              return getComponentEvents ();
            }
          }, 
        };
      }
    }
    return beanPropertySets;
  }
  
// -----------------------------------------------------------------------------
// Package-private Access to component Properties

  Node.Property[] getSyntheticProperties () {
    return syntheticProperties;
  }

  Node.Property[] getComponentProperties () {
    return beanProperties;
  }
  
  Node.Property[] getComponentExpertProperties () {
    return beanExpertProperties;
  }

  Node.Property[] getComponentEvents () {
    return beanEvents;
  }

// -----------------------------------------------------------------------------
// Protected interface

  protected Node.Property[] createSyntheticProperties () {
    return getFormManager ().getCodeGenerator ().getSyntheticProperties (this);
  }
  
  protected Node.Property[] createBeanProperties () {
    PropertyDescriptor[] props = beanInfo.getPropertyDescriptors ();
    ArrayList nodeProps = new ArrayList ();
    for (int i = 0; i < props.length; i++) {
      if (!props[i].isHidden ()) {
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
      if (!props[i].isHidden ()) {
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
            if (event.getHandler () == null)
              return FormEditor.getFormBundle().getString("CTL_NoEvent");
            else
              return event.getHandler ().getName ();
          }

          public void setValue (Object val) throws IllegalArgumentException {
            if (!(val instanceof String))
              throw new IllegalArgumentException();

            if ((!("".equals (val))) && (!Utilities.isJavaIdentifier ((String)val)))
              return;

            EventsManager.EventHandler oldValue = event.getHandler ();

            if ("".equals (val)) {  
              
              // removing event hanlder
              if (oldValue == null) return; // no change
              formManager.getEventsManager ().removeEventHandler (event);
              formManager.fireEventRemoved (RADComponent.this, oldValue);
              
            } else {
              
              // adding/changing event handler
              String handlerName = (String) val;
              if (oldValue != null) { // renaming
                if (handlerName.equals (oldValue)) return; // no change
                String oldName = oldValue.getName ();
                formManager.getEventsManager ().renameEventHandler (event.getHandler (), handlerName);
                formManager.fireEventRenamed (RADComponent.this, oldValue, oldName);
                
              } else {
                
                // adding event handler
                formManager.getEventsManager ().addEventHandler (event, handlerName);
                formManager.fireEventRemoved (RADComponent.this, event.getHandler ());
                
              }

//              if ((gotoMethod != null) && gotoMethod.equals (handlerName)) {
//                gotoMethod = null;
//                event.gotoEventHandler ();
//              }
            } 
          } 

        };
        nodeEvents[idx++] = ep;
      }
    }
    return nodeEvents;

  }
  
  Node.Property getPropertyByName (String name) {
    return (Node.Property) nameToProperty.get (name);
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

  void restorePropertyValue (PropertyDescriptor desc, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    setPropertyValue (desc, value);
    Object defValue = defaultPropertyValues.get (desc.getName ());
    // add the property to the list of changed properties
    RADProperty prop = (RADProperty)nameToProperty.get (desc.getName ());
    changedPropertyValues.put (prop, value);
  }
  
// -----------------------------------------------------------------------------
// Protected interface to working with properties on bean instance

  protected Object getPropertyValue (PropertyDescriptor desc) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//System.out.println ("1: "+desc.getName ());
    if (isChangedValue (desc)) {
      return getChangedValue (desc);
    }
//System.out.println ("2: "+desc.getName ());
    Method readMethod = desc.getReadMethod ();
    if (readMethod == null) {
      throw new IllegalAccessException ();
    }
    return readMethod.invoke (getComponentInstance (), new Object[0]);
  }

  protected void setPropertyValue (PropertyDescriptor desc, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    cacheValue (desc, value);

    // [PENDING - property names to cache]
    if ("enabled".equals (desc.getName ()) || 
        "visible".equals (desc.getName ())) 
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
    writeMethod.invoke (getComponentInstance (), new Object[] { valueToSet });
  }

  protected Object getIndexedPropertyValue (IndexedPropertyDescriptor desc, int index) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method readMethod = desc.getIndexedReadMethod ();
    if (readMethod == null) {
      throw new IllegalAccessException ();
    }
    return readMethod.invoke (getComponentInstance (), new Object[] { new Integer (index) });
  }
  
  protected void setIndexedPropertyValue (IndexedPropertyDescriptor desc, int index, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method writeMethod = desc.getIndexedWriteMethod ();
    if (writeMethod == null) {
      throw new IllegalAccessException ();
    }
    writeMethod.invoke (getComponentInstance (), new Object[] { new Integer (index), value });
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

  public String toString () {
    return super.toString () + ", name: "+getName ()+", class: "+getComponentClass ()+", beaninfo: "+getBeanInfo () + ", instance: "+getComponentInstance ();
  }
  
  public void debugChangedValues () {
    if (System.getProperty ("netbeans.debug.form.full") != null) {
      System.out.println("-- debug.form: Changed property values in: "+this+" -------------------------");
      for (java.util.Iterator it = changedPropertyValues.keySet ().iterator (); it.hasNext ();) {
        RADProperty prop = (RADProperty)it.next ();
        PropertyDescriptor desc = prop.getPropertyDescriptor ();
        System.out.println("Changed Property: "+desc.getName ()+", value: "+changedPropertyValues.get (prop));
      }
      System.out.println("--------------------------------------------------------------------------------------");
    }
  }

// -----------------------------------------------------------------------------
// Inner Classes

  interface RADProperty {
    public PropertyDescriptor getPropertyDescriptor ();
    public PropertyEditor getPropertyEditor ();
    public PropertyEditor getCurrentEditor ();
    public void setCurrentEditor (PropertyEditor editor);
    public RADComponent getRADComponent ();
    public boolean canRead ();
    public Object getValue () throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
    public boolean canWrite ();
    public void setValue (Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
  }

  class RADPropertyImpl extends Node.Property implements RADProperty {
    private PropertyEditor currentEditor;
    private PropertyDescriptor desc;
    
    RADPropertyImpl (PropertyDescriptor desc) {
      super (desc.getPropertyType ());
      this.desc = desc;
      PropertyEditor[] allEditors = FormPropertyEditorManager.getAllEditors (desc.getPropertyType (), false);
      if ((allEditors != null) && (allEditors.length > 0)) {
        currentEditor = allEditors[0];
      }
    }

    public PropertyDescriptor getPropertyDescriptor () {
      return desc;
    }

    public RADComponent getRADComponent () {
      return RADComponent.this;
    }

    public PropertyEditor getCurrentEditor () {
      return currentEditor;
    }
    
    public void setCurrentEditor (PropertyEditor editor) {
      currentEditor = editor;
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
      
      try {
        setPropertyValue (desc, val);
      } catch (IllegalArgumentException e) {  // no problem -> keep null
      } catch (IllegalAccessException e) {    // no problem -> keep null
      } catch (InvocationTargetException e) { // no problem -> keep null
      }
      
      Object defValue = defaultPropertyValues.get (desc.getName ());
      if ((defValue != null) && (val != null) && (defValue.equals (val))) {
        // resetting to default value
        changedPropertyValues.remove (RADPropertyImpl.this);
      } else {
        // add the property to the list of changed properties
        changedPropertyValues.put (RADPropertyImpl.this, val);
      }
      debugChangedValues ();
      getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), old, val);
    }

    /** Test whether the property had a default value.
    * @return <code>true</code> if it does
    */
    public boolean supportsDefaultValue () {
      return true;
    }

    /** Restore this property to its default value, if supported.
    */
    public void restoreDefaultValue () {
      // 1. remove the property from list of changed values, so that the code for it is not generated
      changedPropertyValues.remove (RADPropertyImpl.this);
      
      // 2. restore the default property value
      Object def = defaultPropertyValues.get (desc.getName ());
      if (def != null) {
        try {
          setValue (def);
        } catch (IllegalAccessException e) {
          // what to do, ignore...
        } catch (IllegalArgumentException e) {
          // what to do, ignore...
        } catch (InvocationTargetException e) {
          // what to do, ignore...
        }
      }
      // [PENDING - test]
    }

    /* Returns property editor for this property.
    * @return the property editor or <CODE>null</CODE> if there should not be
    *    any editor.
    */
    public PropertyEditor getPropertyEditor () {
      // the property editor cannot be reused as it is not reentrant !!! [IAN]

      PropertyEditor defaultEditor = null;
      if (desc.getPropertyEditorClass () != null) {
        try {
          defaultEditor = (PropertyEditor) desc.getPropertyEditorClass ().newInstance ();
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException iex) {
        }
      } else {
        defaultEditor = FormPropertyEditorManager.findEditor (desc.getPropertyType ());
      }
      FormPropertyEditor editor = null;
      if (defaultEditor != null) {
        editor = new FormPropertyEditor (RADComponent.this, desc.getPropertyType (), RADPropertyImpl.this, defaultEditor);
      }
      return editor;
    }
  }

  
  class RADIndexedPropertyImpl extends Node.IndexedProperty implements RADProperty {
    private PropertyEditor currentEditor;
    private IndexedPropertyDescriptor desc;
    
    RADIndexedPropertyImpl (IndexedPropertyDescriptor desc) {
      super (getIndexedType (desc), desc.getIndexedPropertyType ());
      this.desc = desc;
      PropertyEditor[] allEditors = FormPropertyEditorManager.getAllEditors (desc.getIndexedPropertyType (), false);
      if ((allEditors != null) && (allEditors.length > 0)) {
        currentEditor = allEditors[0];
      }
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
      
      Object defValue = defaultPropertyValues.get (desc.getName ());
      if ((defValue != null) && (val != null) && (defValue.equals (val))) {
        // resetting to default value
        changedPropertyValues.remove (RADIndexedPropertyImpl.this);
      } else {
        // add the property to the list of changed properties
        changedPropertyValues.put (RADIndexedPropertyImpl.this, val);
      }
      debugChangedValues ();
      getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), old, val);
    }

    /** Test whether the property had a default value.
    * @return <code>true</code> if it does
    */
    public boolean supportsDefaultValue () {
      return true;
    }

    /** Restore this property to its default value, if supported.
    */
    public void restoreDefaultValue () {
      // 1. remove the property from list of changed values, so that the code for it is not generated
      changedPropertyValues.remove (RADIndexedPropertyImpl.this);
      
      // 2. restore the default property value
      Object def = defaultPropertyValues.get (desc.getName ());
      if (def != null) {
        try {
          setValue (def);
        } catch (IllegalAccessException e) {
          // what to do, ignore...
        } catch (IllegalArgumentException e) {
          // what to do, ignore...
        } catch (InvocationTargetException e) {
          // what to do, ignore...
        }
      }
      // [PENDING - test]
    }

    /* Returns property editor for this property.
    * @return the property editor or <CODE>null</CODE> if there should not be
    *    any editor.
    */
    public PropertyEditor getPropertyEditor () {
      // the property editor cannot be reused as it is not reentrant !!! [IAN]

      PropertyEditor defaultEditor = null;
      if (desc.getPropertyEditorClass () != null) {
        try {
          defaultEditor = (PropertyEditor) desc.getPropertyEditorClass ().newInstance ();
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException iex) {
        }
      } else {
        defaultEditor = FormPropertyEditorManager.findEditor (desc.getIndexedPropertyType ());
      }
      FormPropertyEditor editor = null;
      if (defaultEditor != null) {
        editor = new FormPropertyEditor (RADComponent.this, desc.getIndexedPropertyType (), RADIndexedPropertyImpl.this, defaultEditor);
      }
      return editor;
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
  }

  /** Utility method for obtaining array type for indexed properties */  
  private static Class getIndexedType (IndexedPropertyDescriptor desc) {
    Class valueType = desc.getPropertyType ();
    if (valueType == null) {
      try {
        valueType = org.openide.TopManager.getDefault ().systemClassLoader ().loadClass (
          "[L" + desc.getIndexedPropertyType ().getName () + ";"
        );
      } catch (Exception e) {
        valueType = Object[].class;
      }
    }
    return valueType;
  }


  abstract class EventProperty extends PropertySupport.ReadWrite {
    EventsList.Event event;

    EventProperty (EventsList.Event event) {
      super (FormEditor.EVENT_PREFIX + event.getName(),
            String.class,
            event.getName(),
            event.getName());
      this.event = event;
/*      event.addPropertyChangeListener (new PropertyChangeListener () {
          public void propertyChange (PropertyChangeEvent evt) {
            firePropertyChangeHelper (EventProperty.this.getName (), evt.getOldValue (), evt.getNewValue ());
          }
        }
      ); */ // [PENDING]
    }

    /** Returns property editor for this property.
    * @return the property editor or <CODE>null</CODE> if there should not be
    *    any editor.
    */
    public java.beans.PropertyEditor getPropertyEditor () {
      return new EventEditor ();
    }                               
    
    class EventEditor extends PropertyEditorSupport implements EnhancedPropertyEditor {
      public void setAsText (String string) {
        setValue(string);
//        gotoMethod = string; // [PENDING]
      }
                                   
      private String getEditText () {
        if (event.getHandler () == null)
          return FormUtils.getDefaultEventName (RADComponent.this, event.getListenerMethod ());
        else 
          return event.getHandler ().getName (); // [PENDING]
      }
      
      /**
      * @return Returns custom property editor to be showen inside the property
      *         sheet.
      */
      public java.awt.Component getInPlaceCustomEditor () {
        final JTextField eventField = new JTextField ();
        eventField.setText (getEditText ());
        eventField.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent e) {
              setAsText (eventField.getText ());
            }
          }
        );
        return eventField;
      }
    
      /**
      * @return true if this PropertyEditor provides a enhanced in-place custom 
      *              property editor, false otherwise
      */
      public boolean hasInPlaceCustomEditor () {
        return true;
      }
    
      /**
      * @return true if this property editor provides tagged values and
      * a custom strings in the choice should be accepted too, false otherwise
      */
      public boolean supportsEditingTaggedValues () {
        return false;
      }
    }
  }

}

/*
 * Log
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
