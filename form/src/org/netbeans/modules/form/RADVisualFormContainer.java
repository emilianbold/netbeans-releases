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

import org.openide.nodes.*;
import com.netbeans.developerx.loaders.form.formeditor.layouts.*;
import com.netbeans.developer.modules.loaders.form.forminfo.FormInfo;

import java.awt.*;

/** RADVisualFormContainer represents the top-level container of the form and the form itself
* during design time.
*
* @author Ian Formanek
*/
public class RADVisualFormContainer extends RADVisualContainer implements FormContainer {
  public static final int GEN_BOUNDS = 0;
  public static final int GEN_PACK = 1;
  public static final int GEN_NOTHING = 2;
  
  private FormInfo formInfo;
  private Container topContainer;
  private Dimension formSize;
  private Point formPosition;
  private boolean generatePosition = true;
  private boolean generateSize = true;
  private boolean generateCenter = true;
  private int formSizePolicy = GEN_PACK;
  
  public RADVisualFormContainer (FormInfo formInfo) {
    super ();
    this.formInfo = formInfo;
    topContainer = formInfo.getTopContainer ();
  }
  
  /** @return The JavaBean visual container represented by this RADVisualComponent */
  public Container getContainer () {
    return topContainer;
  }

  /** Called to create the instance of the bean. Default implementation simply creates instance 
  * of the bean's class using the default constructor.  Top-level container (the form object itself) 
  * will redefine this to use FormInfo to create the instance, as e.g. Dialogs cannot be created using 
  * the default constructor 
  * @return the instance of the bean that will be used during design time 
  */
  protected Object createBeanInstance () {
    return formInfo.getFormInstance ();
  }

  public String getContainerGenName () {
    return formInfo.getContainerGenName ();
  }

  public FormInfo getFormInfo () {
    return formInfo;
  }

  public Point getFormPosition () {
    if (formPosition == null) {
      formPosition = topContainer.getLocation ();
    }
    return formPosition;
  }

  public void setFormPosition (Point value) {
    formPosition = value;
    // [PENDING - set on form window if in single mode]
    getFormManager ().fireFormChange ();
  }

  public Dimension getFormSize () {
    if (formSize == null) {
      formSize = topContainer.getSize ();
    }
    return formSize;
  }

  public void setFormSize (Dimension value) {
    formSize = value;
    // [PENDING - set on form window if in single mode]
    getFormManager ().fireFormChange ();
  }

  public boolean getGeneratePosition () {
    return generatePosition;
  }

  public void setGeneratePosition (boolean value) {
    generatePosition = value;
    getFormManager ().fireFormChange ();
  }

  public boolean getGenerateSize () {
    return generateSize;
  }

  public void setGenerateSize (boolean value) {
    generateSize = value;
    getFormManager ().fireFormChange ();
  }

  public boolean getGenerateCenter () {
    return generateCenter;
  }

  public void setGenerateCenter (boolean value) {
    generateCenter = value;
    getFormManager ().fireFormChange ();
  }

  public int getFormSizePolicy () {
    return formSizePolicy;
  }

  public void setFormSizePolicy (int value) {
    formSizePolicy = value;
    getFormManager ().fireFormChange ();
  }

  protected Node.Property[] createSyntheticProperties () {
    Node.Property[] inherited = super.createSyntheticProperties ();
/*    sizeProperty = new PropertySupport.ReadWrite () {
  
      public Object getValue () throws
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return getPropertyValue (desc);
      }
  
      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
        Object old = null;
        
        try {
          old = getValue ();
        } catch (IllegalArgumentException e) {  // no problem -> keep null
        } catch (IllegalAccessException e) {    // no problem -> keep null
        } catch (InvocationTargetException e) { // no problem -> keep null
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
          changedPropertyValues.remove (RADProperty.this);
        } else {
          // add the property to the list of changed properties
          changedPropertyValues.put (RADProperty.this, val);
        }
        debugChangedValues ();
        getFormManager ().firePropertyChanged (RADComponent.this, desc.getName (), old, val);
      }
  
      /** Test whether the property had a default value.
      * @return <code>true</code> if it does
      * /
      public boolean supportsDefaultValue () {
        return true;
      }
  
      /** Restore this property to its default value, if supported.
      * /
      public void restoreDefaultValue () {
        // 1. remove the property from list of changed values, so that the code for it is not generated
        changedPropertyValues.remove (RADProperty.this);
        
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
  
    }; */

    Node.Property policyProperty = new PropertySupport.ReadWrite ("form size policy", Integer.TYPE, "form size policy", "form size policy") {
      public Object getValue () throws
      IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        return new Integer (getFormSizePolicy ());
      }
  
      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        if (!(val instanceof Integer)) throw new IllegalArgumentException ();
        setFormSizePolicy (((Integer)val).intValue ());
      }
    };


    Node.Property sizeProperty = new PropertySupport.ReadWrite ("form size", Dimension.class, "form size", "form size") {
      public Object getValue () throws
      IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        return getFormSize ();
      }
  
      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        if (!(val instanceof Dimension)) throw new IllegalArgumentException ();
        setFormSize ((Dimension)val);
      }
    };

    Node.Property positionProperty = new PropertySupport.ReadWrite ("form position", Point.class, "form position", "form position") {
      public Object getValue () throws
      IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        return getFormPosition ();
      }
  
      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        if (!(val instanceof Point)) throw new IllegalArgumentException ();
        setFormPosition ((Point)val);
      }
    };

    Node.Property genPositionProperty = new PropertySupport.ReadWrite ("generate position", Boolean.TYPE, "generate position", "generate position") {
      public Object getValue () throws
      IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        return new Boolean (getGeneratePosition ());
      }
  
      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        if (!(val instanceof Boolean)) throw new IllegalArgumentException ();
        setGeneratePosition (((Boolean)val).booleanValue ());
      }
    };

    Node.Property genSizeProperty = new PropertySupport.ReadWrite ("generate size", Boolean.TYPE, "generate size", "generate size") {
      public Object getValue () throws
      IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        return new Boolean (getGenerateSize ());
      }
  
      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        if (!(val instanceof Boolean)) throw new IllegalArgumentException ();
        setGenerateSize (((Boolean)val).booleanValue ());
      }
    };

    Node.Property genCenterProperty = new PropertySupport.ReadWrite ("generate center", Boolean.TYPE, "generate center", "generate center") {
      public Object getValue () throws
      IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        return new Boolean (getGenerateCenter ());
      }
  
      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        if (!(val instanceof Boolean)) throw new IllegalArgumentException ();
        setGenerateCenter (((Boolean)val).booleanValue ());
      }
    };

    Node.Property[] ret = new Node.Property [inherited.length + 6];
    System.arraycopy (inherited, 0, ret, 0, inherited.length);
    ret[ret.length - 6] = sizeProperty;
    ret[ret.length - 5] = positionProperty;
    ret[ret.length - 4] = policyProperty;
    ret[ret.length - 3] = genPositionProperty;
    ret[ret.length - 2] = genSizeProperty;
    ret[ret.length - 1] = genCenterProperty;
    return ret;
  }

}

/*
 * Log
 *  4    Gandalf   1.3         6/24/99  Ian Formanek    Generation of size for 
 *       visaul forms
 *  3    Gandalf   1.2         6/6/99   Ian Formanek    New FormInfo design 
 *       employed to provide correct top-level bean properties
 *  2    Gandalf   1.1         5/11/99  Ian Formanek    Build 318 version
 *  1    Gandalf   1.0         5/10/99  Ian Formanek    
 * $
 */
