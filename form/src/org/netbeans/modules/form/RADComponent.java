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

import com.netbeans.ide.explorer.propertysheet.SpecialPropertyEditor;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.Utilities;

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
  private Node.Property[] beanProperties;
  private Node.Property[] beanExpertProperties;
  private Node.Property[] beanEvents;

  private HashMap auxValues;
  private HashMap changedPropertyValues;
  private HashMap valuesCache;
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
    beanInstance = BeanSupport.createBeanInstance (beanClass);
    beanInfo = BeanSupport.createBeanInfo (beanClass);
    
    nameToProperty = new HashMap ();

    beanProperties = createBeanProperties ();
    beanExpertProperties = createBeanExpertProperties ();

    beanEvents = createEventsProperties ();

    changedPropertyValues = new HashMap ();
    defaultPropertyValues = BeanSupport.getDefaultPropertyValues (beanClass);
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
  
  public Map getChangedProperties () {
    return changedPropertyValues;
  }
  
  public void setAuxiliaryValue (String key, Object value) {
    auxValues.put (key, value);
  }

  public Object getAuxiliaryValue (String key) {
    return auxValues.get (key);
  }
  
  public FormManager2 getFormManager () {
    return formManager;
  }
  
  public EventsList getEventsList () {
    return eventsList;
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
// Protected interface

  protected Node.Property[] getSyntheticProperties () {
    return getFormManager ().getCodeGenerator ().getSyntheticProperties (this);
  }

  protected Node.Property[] getComponentProperties () {
    return beanProperties;
  }
  
  protected Node.Property[] getComponentExpertProperties () {
    return beanExpertProperties;
  }

  protected Node.Property[] getComponentEvents () {
    return beanEvents;
  }

  protected Node.Property[] createBeanProperties () {
    PropertyDescriptor[] props = beanInfo.getPropertyDescriptors ();
    ArrayList nodeProps = new ArrayList ();
    for (int i = 0; i < props.length; i++) {
      if (!props[i].isHidden ()) {
        Node.Property prop = createProperty (props[i]);
        if (prop != null) // [PENDING - temporary]
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
        if (prop != null) // [PENDING - temporary]
          nodeProps.add (prop);
      }
    }

    Node.Property[] np = new Node.Property [nodeProps.size ()];
    nodeProps.toArray (np);

    return np;
  }

  Node.Property getPropertyByName (String name) {
    return (Node.Property) nameToProperty.get (name);
  }
  
  private Node.Property createProperty (final PropertyDescriptor desc) {
    Node.Property prop;
    if (desc instanceof IndexedPropertyDescriptor) {
      return null;
/*      IndexedPropertyDescriptor idesc = (IndexedPropertyDescriptor)desc;

      prop =  new IndexedPropertySupport (
        bean, idesc.getPropertyType (),
        idesc.getIndexedPropertyType(), idesc.getReadMethod (), idesc.getWriteMethod (),
        idesc.getIndexedReadMethod (), idesc.getIndexedWriteMethod ()
      );
      prop.setName (desc.getName ());
      prop.setDisplayName (desc.getDisplayName ());
      prop.setShortDescription (desc.getShortDescription ()); */
    } else { 
      prop = new Node.Property (desc.getPropertyType ()) {
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
            changedPropertyValues.remove (desc);
          } else {
            // add the property to the list of changed properties
            changedPropertyValues.put (desc, val);
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
          changedPropertyValues.remove (desc);
          
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
         if (desc.getPropertyEditorClass () != null)
           try {
             return (PropertyEditor) desc.getPropertyEditorClass ().newInstance ();
           } catch (InstantiationException ex) {
           } catch (IllegalAccessException iex) {
           }
         return super.getPropertyEditor ();
        }

      };
      

      prop.setName (desc.getName ());
      prop.setDisplayName (desc.getDisplayName ());
      prop.setShortDescription (desc.getShortDescription ());
    }

    nameToProperty.put (desc.getName (), prop);
    return prop;
  }

  private Node.Property[] createEventsProperties () {
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
  
  void restorePropertyValue (PropertyDescriptor desc, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    setPropertyValue (desc, value);
    Object defValue = defaultPropertyValues.get (desc.getName ());
    // add the property to the list of changed properties
    changedPropertyValues.put (desc, value);
  }
  
// -----------------------------------------------------------------------------
// Protected interface to working with properties on bean instance

  protected Object getPropertyValue (PropertyDescriptor desc) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (isCachedValue (desc)) {
      return getCachedValue (desc);
    }
    Method readMethod = desc.getReadMethod ();
    if (readMethod == null) {
      throw new IllegalAccessException ();
    }
    return readMethod.invoke (getComponentInstance (), new Object[0]);
  }

  protected void setPropertyValue (PropertyDescriptor desc, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    // [PENDING - property names to cache]
    if ("enabled".equals (desc.getName ()) || 
        "visible".equals (desc.getName ())) 
    {
      // values of these properties are just cached, not represented during design-time
      cacheValue (desc, value);
      return;
    } 
    
    Method writeMethod = desc.getWriteMethod ();
    if (writeMethod == null) {
      throw new IllegalAccessException ();
    }
    writeMethod.invoke (getComponentInstance (), new Object[] { value });
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
    }
    valuesCache.put (desc, value);
  }
  
  protected boolean isCachedValue (PropertyDescriptor desc) {
    if (valuesCache == null) {
      return false;
    }
    return valuesCache.containsKey (desc);
  }

  protected Object getCachedValue (PropertyDescriptor desc) {
    if (valuesCache == null) {
      throw new InternalError ();
    }
    return valuesCache.get (desc);
  }
  
// -----------------------------------------------------------------------------
// Debug methods

  public void debugChangedValues () {
    if (System.getProperty ("netbeans.debug.form.full") != null) {
      System.out.println("-- debug.form: Changed property values in: "+this+" -------------------------");
      for (java.util.Iterator it = changedPropertyValues.keySet ().iterator (); it.hasNext ();) {
        PropertyDescriptor next = (PropertyDescriptor)it.next ();
        System.out.println("Changed Property: "+next.getName ()+", value: "+changedPropertyValues.get (next));
      }
      System.out.println("--------------------------------------------------------------------------------------");
    }
  }

// -----------------------------------------------------------------------------
// Inner Classes

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
    
    class EventEditor extends PropertyEditorSupport implements SpecialPropertyEditor {
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
      * @return true if this PropertyEditor provides a special in-place custom 
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
