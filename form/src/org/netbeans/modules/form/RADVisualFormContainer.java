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
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JMenuBar;

/** RADVisualFormContainer represents the top-level container of the form and the form itself
* during design time.
*
* @author Ian Formanek
*/
public class RADVisualFormContainer extends RADVisualContainer implements FormContainer {
  public static final String PROP_MENU_BAR = "menuBar";
  public static final String PROP_FORM_SIZE_POLICY = "formSizePolicy";
  public static final String PROP_FORM_SIZE = "formSize";
  public static final String PROP_FORM_POSITION = "formPosition";
  public static final String PROP_GENERATE_POSITION = "generatePosition";
  public static final String PROP_GENERATE_SIZE = "generateSize";
  public static final String PROP_GENERATE_CENTER = "generateCenter";

  protected static final String AUX_MENU_COMPONENT = "RADVisualFormContainer_MenuComponent";

  public static final int GEN_BOUNDS = 0;
  public static final int GEN_PACK = 1;
  public static final int GEN_NOTHING = 2;

  /** Localized string for no menu. */
  static final String NO_MENU = FormEditor.getFormBundle ().getString ("CTL_NoMenu");
  
  private FormInfo formInfo;
  private Container topContainer;
  private Container topAddContainer;

  // Synthetic properties of form
  private RADComponent menu;
  private boolean menuInitialized = false;
  private Dimension formSize = new Dimension (FormEditor.DEFAULT_FORM_WIDTH, FormEditor.DEFAULT_FORM_HEIGHT);
  private Point formPosition;
  private boolean generatePosition = true;
  private boolean generateSize = true;
  private boolean generateCenter = true;
  private int formSizePolicy = GEN_NOTHING;
  
  public RADVisualFormContainer (FormInfo formInfo) {
    super ();
    this.formInfo = formInfo;
    topContainer = formInfo.getTopContainer ();
    topAddContainer = formInfo.getTopAddContainer ();
  }
  
  /** @return The JavaBean visual container represented by this RADVisualComponent */
  public Container getContainer () {
    return topAddContainer;
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

// ------------------------------------------------------------------------------
// Form synthetic properties

  public FormInfo getFormInfo () {
    return formInfo;
  }

  public String getFormMenu () {
    if (!menuInitialized) {
      String menuName = (String)getAuxValue (AUX_MENU_COMPONENT);
      if (menuName != null) {
        ArrayList list = getAvailableMenus ();
        for (Iterator it = list.iterator (); it.hasNext ();) {
          RADComponent comp = (RADComponent)it.next ();
          if (comp.getName ().equals (menuName)) {
            menu = comp;
            break;
          }
        }
      }
      menuInitialized = true;
    }
    if (menu == null) return null;
    else return menu.getName ();
  }

  public void setFormMenu (String value) {
    setAuxValue (AUX_MENU_COMPONENT, value);

    if (value != null) {
      ArrayList list = getAvailableMenus ();
      for (Iterator it = list.iterator (); it.hasNext ();) {
        RADComponent comp = (RADComponent)it.next ();
        if (comp.getName ().equals (value)) {
          menu = comp;
        }
      }
      // set the real menu
      if (formInfo instanceof JMenuBarContainer) {
        if (menu.getBeanInstance () instanceof JMenuBar) {
          ((JMenuBarContainer)formInfo).setJMenuBar ((JMenuBar)menu.getBeanInstance ());
        }
      } else if (formInfo instanceof MenuBarContainer) {
        if (menu.getBeanInstance () instanceof MenuBar) {
          ((MenuBarContainer)formInfo).setMenuBar ((MenuBar)menu.getBeanInstance ());
        }
      }
    } else {
      menu = null;
      // set the real menu
      if (formInfo instanceof JMenuBarContainer) {
        ((JMenuBarContainer)formInfo).setJMenuBar (null);
      } else if (formInfo instanceof MenuBarContainer) {
        ((MenuBarContainer)formInfo).setMenuBar (null);
      }
    }
    getFormManager ().fireFormChange ();
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
    // [PENDING - set as aux value]
    generatePosition = value;
    getFormManager ().fireFormChange ();
  }

  public boolean getGenerateSize () {
    return generateSize;
  }

  public void setGenerateSize (boolean value) {
    // [PENDING - set as aux value]
    generateSize = value;
    getFormManager ().fireFormChange ();
  }

  public boolean getGenerateCenter () {
    return generateCenter;
  }

  public void setGenerateCenter (boolean value) {
    // [PENDING - set as aux value]
    generateCenter = value;
    getFormManager ().fireFormChange ();
  }

  public int getFormSizePolicy () {
    return formSizePolicy;
  }

  public void setFormSizePolicy (int value) {
    // [PENDING - set as aux value]
    formSizePolicy = value;
    getFormManager ().fireFormChange ();
  }

// ------------------------------------------------------------------------------
// End of form synthetic properties

  protected Node.Property[] createSyntheticProperties () {

    Node.Property policyProperty = new PropertySupport.ReadWrite (PROP_FORM_SIZE_POLICY, Integer.TYPE, "form size policy", "form size policy") {
      public Object getValue () throws
      IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        return new Integer (getFormSizePolicy ());
      }
  
      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        if (!(val instanceof Integer)) throw new IllegalArgumentException ();
        setFormSizePolicy (((Integer)val).intValue ());
      }

      /** Editor for alignment */
      public java.beans.PropertyEditor getPropertyEditor () {
        return new SizePolicyEditor ();
      }
      
    };


    Node.Property sizeProperty = new PropertySupport.ReadWrite (PROP_FORM_SIZE, Dimension.class, "form size", "form size") {
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

    Node.Property positionProperty = new PropertySupport.ReadWrite (PROP_FORM_POSITION, Point.class, "form position", "form position") {
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

    Node.Property genPositionProperty = new PropertySupport.ReadWrite (PROP_GENERATE_POSITION, Boolean.TYPE, "generate position", "generate position") {
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

    Node.Property genSizeProperty = new PropertySupport.ReadWrite (PROP_GENERATE_SIZE, Boolean.TYPE, "generate size", "generate size") {
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

    Node.Property genCenterProperty = new PropertySupport.ReadWrite (PROP_GENERATE_CENTER, Boolean.TYPE, "generate center", "generate center") {
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

    if ((formInfo instanceof JMenuBarContainer) || (formInfo instanceof MenuBarContainer)) {
      Node.Property[] ret = new Node.Property [7];

     Node.Property menuProperty = new PropertySupport.ReadWrite (PROP_MENU_BAR, String.class, "menu bar", "menu bar of the form") {
        public Object getValue () throws
        IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
          String s = getFormMenu ();
          return (s == null) ? NO_MENU : s;
        }
    
        public void setValue (Object val) throws IllegalAccessException,
        IllegalArgumentException, java.lang.reflect.InvocationTargetException {
          if (!(val instanceof String)) throw new IllegalArgumentException ();
          String s = (String) val;
          setFormMenu(s.equals(NO_MENU) ? null : s);
        }
  
        /** Editor for alignment */
        public java.beans.PropertyEditor getPropertyEditor () {
          return new FormMenuEditor ();
        }
        
      };

      ret[0] = menuProperty;
      ret[1] = sizeProperty;
      ret[2] = positionProperty;
      ret[3] = policyProperty;
      ret[4] = genPositionProperty;
      ret[5] = genSizeProperty;
      ret[6] = genCenterProperty;
      return ret;
    } else {
      Node.Property[] ret = new Node.Property [6];
      ret[0] = sizeProperty;
      ret[1] = positionProperty;
      ret[2] = policyProperty;
      ret[3] = genPositionProperty;
      ret[4] = genSizeProperty;
      ret[5] = genCenterProperty;
      return ret;
    }
  }

  ArrayList getAvailableMenus() {
    ArrayList list = new ArrayList();
    RADComponent[] comps = getFormManager ().getNonVisualComponents ();
    int size = comps.length;
    boolean swing = (formInfo instanceof JMenuBarContainer);
    
    for (int i = 0; i < size; i++) {
      if (comps[i] instanceof RADMenuComponent) {
        RADMenuComponent n = (RADMenuComponent) comps[i];
        if ((swing && (n.getMenuItemType () == RADMenuComponent.T_JMENUBAR)) ||
            (!swing && (n.getMenuItemType () == RADMenuComponent.T_MENUBAR)))
          list.add (n);
      }
    }
    return list;
  }

// ------------------------------------------------------------------------------------------
// Innerclasses

  final public static class SizePolicyEditor extends java.beans.PropertyEditorSupport {
    /** Display Names for alignment. */
    private static final String[] names = { 
      FormEditor.getFormBundle ().getString ("VALUE_sizepolicy_full"),
      FormEditor.getFormBundle ().getString ("VALUE_sizepolicy_pack"),
      FormEditor.getFormBundle ().getString ("VALUE_sizepolicy_none"),
    }; 

    /** @return names of the possible directions */
    public String[] getTags () {
      return names;
    }

    /** @return text for the current value */
    public String getAsText () {
      int value = ((Integer)getValue ()).intValue ();
      return names[value];
    }

    /** Setter.
    * @param str string equal to one value from directions array
    */
    public void setAsText (String str) {
      if (names[0].equals (str))
        setValue (new Integer (0));
      else if (names[1].equals (str))
        setValue (new Integer (1)); 
      else if (names[2].equals (str))
        setValue (new Integer (2)); 
    }
  }

  final public class FormMenuEditor extends java.beans.PropertyEditorSupport {

    /** @return names of the possible directions */
    public String[] getTags () {
      ArrayList list = getAvailableMenus ();
      RADComponent[] comps = new RADComponent [list.size ()];
      list.toArray (comps);
      String[] names = new String[comps.length + 1];
      names[0] = NO_MENU; // No Menu
      for (int i = 0; i < comps.length; i++) {
        names[i+1] = comps[i].getName ();
      }
      return names;
    }

    /** @return text for the current value */
    public String getAsText () {
      return (String)getValue ();
    }

    /** Setter.
    * @param str string equal to one value from directions array
    */
    public void setAsText (String str) {
      setValue (str);
    }
  }
}

/*
 * Log
 *  8    Gandalf   1.7         7/9/99   Ian Formanek    Fixed setting "No Menu"
 *  7    Gandalf   1.6         7/9/99   Ian Formanek    menu editor improvements
 *  6    Gandalf   1.5         7/5/99   Ian Formanek    menu bar property, 
 *       constants for properties
 *  5    Gandalf   1.4         6/25/99  Ian Formanek    Improved Size Policy 
 *  4    Gandalf   1.3         6/24/99  Ian Formanek    Generation of size for 
 *       visaul forms
 *  3    Gandalf   1.2         6/6/99   Ian Formanek    New FormInfo design 
 *       employed to provide correct top-level bean properties
 *  2    Gandalf   1.1         5/11/99  Ian Formanek    Build 318 version
 *  1    Gandalf   1.0         5/10/99  Ian Formanek    
 * $
 */
