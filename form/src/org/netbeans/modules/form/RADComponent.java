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

package com.netbeans.developer.modules.loaders.form.formeditor;

import com.netbeans.ide.nodes.*;

import java.beans.BeanInfo;
import java.util.HashMap;

/** 
*
* @author Ian Formanek
*/
public class RADComponent {

// -----------------------------------------------------------------------------
// Private variables

  private Class beanClass;
  private Object beanInstance;
  private BeanInfo beanInfo;

  private String componentName;
  
  private Node.PropertySet[] beanPropertySets;
  private Node.Property[] beanProperties;
  private Node.Property[] beanExpertProperties;
  private Node.Property[] beanEvents;

  private HashMap auxValues;
  private HashMap propertyValues;

// -----------------------------------------------------------------------------
// Constructors

  public RADComponent () {
    propertyValues = new HashMap (30);
    auxValues = new HashMap (10);
  }

// -----------------------------------------------------------------------------
// Public interface

  public void setComponent (Class beanClass) {
    this.beanClass = beanClass;
    beanInstance = BeanSupport.createBeanInstance (beanClass);
    beanInfo = BeanSupport.createBeanInfo (beanClass);
    beanProperties = BeanSupport.createBeanProperties (beanInstance);
    beanExpertProperties = BeanSupport.createBeanExpertProperties (beanClass);
  }
  
  public Class getComponentClass () {
    return beanClass;
  }

  public String getName () {
    return componentName;
  }

  public void setName (String value) {
    componentName = value;
    // [PENDING - fire change]
  }
  
  public Node.PropertySet[] getProperties () {
    if (beanPropertySets == null) {
      if (beanExpertProperties.length != 0) {
        // No expert properties
        beanPropertySets = new Node.PropertySet [] {
          new Node.PropertySet ("properties", "Properties", "Properties") {
            public Node.Property[] getProperties () {
              
              return beanProperties;
            }
          },
/*          new Node.PropertySet ("events", "Events", "Events") {
            public Node.Property[] getProperties () {
              return beanEvents;
            }
          }, */
        };
      } else {
        beanPropertySets = new Node.PropertySet [] {
          new Node.PropertySet ("properties", "Properties", "Properties") {
            public Node.Property[] getProperties () {
              return beanProperties;
            }
          },
          new Node.PropertySet ("expert", "Expert", "Expert Properties") {
            public Node.Property[] getProperties () {
              return beanExpertProperties;
            }
          },
/*          new Node.PropertySet ("events", "Events", "Events") {
            public Node.Property[] getProperties () {
              return beanEvents;
            }
          }, */
        };
      }
    }
    return beanPropertySets;
  }
  
  public void setAuxiliaryValue (String key, Object value) {
    auxValues.put (key, value);
  }

  public Object getAuxiliaryValue (String key) {
    return auxValues.get (key);
  }

// -----------------------------------------------------------------------------
// Protected interface

  protected Node.Property[] getSynthesizedProperties () {
    return new Node.Property[0];
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

}

/*
 * Log
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
