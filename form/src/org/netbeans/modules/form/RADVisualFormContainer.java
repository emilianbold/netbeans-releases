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

import org.openide.*;
import org.openide.nodes.*;
import org.openide.explorer.propertysheet.editors.*;
import com.netbeans.developerx.loaders.form.formeditor.layouts.*;
import com.netbeans.developer.modules.loaders.form.forminfo.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JMenuBar;
import javax.swing.JComboBox;
import java.util.Hashtable;

/** RADVisualFormContainer represents the top-level container of the form and the form itself
* during design time.
*
* @author Ian Formanek
*/
public class RADVisualFormContainer extends RADVisualContainer implements FormContainer {
  public static final String PROP_MENU_BAR = "menuBar"; // NOI18N
  public static final String PROP_FORM_SIZE_POLICY = "formSizePolicy"; // NOI18N
  public static final String PROP_FORM_SIZE = "formSize"; // NOI18N
  public static final String PROP_FORM_POSITION = "formPosition"; // NOI18N
  public static final String PROP_GENERATE_POSITION = "generatePosition"; // NOI18N
  public static final String PROP_GENERATE_SIZE = "generateSize"; // NOI18N
  public static final String PROP_GENERATE_CENTER = "generateCenter"; // NOI18N

  public static final Hashtable encodingList = new Hashtable ();
  static {
    encodingList.put ("US-ASCII (English)", "US-ASCII"); // NOI18N
    encodingList.put ("UTF-8 (Compressed Unicode)", "UTF-8"); // NOI18N
    encodingList.put ("UTF-16 (Compressed UCS)", "UTF-16"); // NOI18N
    encodingList.put ("ISO-10646-UCS-2 (Raw Unicode)", "ISO-10646-UCS-2"); // NOI18N
    encodingList.put ("ISO-10646-UCS-4 (Raw UCS)", "ISO-10646-UCS-4"); // NOTE: no support for ISO-10646-UCS-4 yet. [from XmlReader] // NOI18N
    encodingList.put ("ISO-8859-1 (Latin-1, western Europe)", "ISO-8859-1"); // NOI18N
    encodingList.put ("ISO-8859-2 (Latin-2, eastern Europe)", "ISO-8859-2"); // NOI18N
    encodingList.put ("ISO-8859-3 (Latin-3, southern Europe)", "ISO-8859-3"); // NOI18N
    encodingList.put ("ISO-8859-4 (Latin-4, northern Europe)", "ISO-8859-4"); // NOI18N
    encodingList.put ("ISO-8859-5 (ASCII plus Cyrillic)", "ISO-8859-5"); // NOI18N
    encodingList.put ("ISO-8859-6 (ASCII plus Arabic)", "ISO-8859-6"); // NOI18N
    encodingList.put ("ISO-8859-7 (ASCII plus Greek)", "ISO-8859-7"); // NOI18N
    encodingList.put ("ISO-8859-8 (ASCII plus Hebrew)", "ISO-8859-8"); // NOI18N
    encodingList.put ("ISO-8859-9 (Latin-5, Turkish)", "ISO-8859-9"); // NOI18N
    encodingList.put ("ISO-2022-JP (Japanese)", "ISO-2022-JP"); // NOI18N
    encodingList.put ("Shift_JIS (Japanese, Windows)", "Shift_JIS"); // NOI18N
    encodingList.put ("EUC-JP (Japanese, UNIX)", "EUC-JP"); // NOI18N
    encodingList.put ("Big5 (Chinese, Taiwan)", "Big5"); // NOI18N
    encodingList.put ("GB2312 (Chinese, mainland China)", "GB2312"); // NOI18N
    encodingList.put ("KOI8-R (Russian)", "KOI8-R"); // NOI18N
    encodingList.put ("ISO-2022-KR (Korea)", "ISO-2022-KR"); // NOI18N
    encodingList.put ("EUC-KR (Korean, UNIX)", "EUC-KR"); // NOI18N
    encodingList.put ("ISO-2022-CN (Chinese)", "ISO-2022-CN"); // NOI18N

    encodingList.put ("EBCDIC-CP-US (EBCDIC: US)", "EBCDIC-CP-US"); // NOI18N
    encodingList.put ("EBCDIC-CP-CA (EBCDIC: Canada)", "EBCDIC-CP-CA"); // NOI18N
    encodingList.put ("EBCDIC-CP-NL (EBCDIC: Netherlands)", "EBCDIC-CP-NL"); // NOI18N
    encodingList.put ("EBCDIC-CP-WT (like EBCDIC-CP-US)", "EBCDIC-CP-WT"); // NOI18N
    encodingList.put ("EBCDIC-CP-DK (EBCDIC: Denmark)", "EBCDIC-CP-DK"); // NOI18N
    encodingList.put ("EBCDIC-CP-NO (EBCDIC: Norway)", "EBCDIC-CP-NO"); // NOI18N
    encodingList.put ("EBCDIC-CP-FI (EBCDIC: Finland)", "EBCDIC-CP-FI"); // NOI18N
    encodingList.put ("EBCDIC-CP-SE (EBCDIC: Sweden)", "EBCDIC-CP-SE"); // NOI18N
    encodingList.put ("EBCDIC-CP-IT (EBCDIC: Italy)", "EBCDIC-CP-IT"); // NOI18N
    encodingList.put ("EBCDIC-CP-ES (EBCDIC: Spain, Latin America)", "EBCDIC-CP-ES"); // NOI18N
    encodingList.put ("EBCDIC-CP-GB (EBCDIC: Great Britain)", "EBCDIC-CP-GB"); // NOI18N
    encodingList.put ("EBCDIC-CP-FR (EBCDIC: France)", "EBCDIC-CP-FR"); // NOI18N
    encodingList.put ("EBCDIC-CP-AR1 (EBCDIC: Arabic)", "EBCDIC-CP-AR1"); // NOI18N
    encodingList.put ("EBCDIC-CP-HE (EBCDIC: Hebrew)", "EBCDIC-CP-HE"); // NOI18N
    encodingList.put ("EBCDIC-CP-BE (like EBCDIC-CP-CH)", "EBCDIC-CP-BE"); // NOI18N
    encodingList.put ("EBCDIC-CP-CH (EBCDIC: Switzerland)", "EBCDIC-CP-CH"); // NOI18N
    encodingList.put ("EBCDIC-CP-ROECE (EBCDIC: Roece)", "EBCDIC-CP-ROECE"); // NOI18N
    encodingList.put ("EBCDIC-CP-YU (EBCDIC: Yogoslavia)", "EBCDIC-CP-YU"); // NOI18N
    encodingList.put ("EBCDIC-CP-IS (EBCDIC: Iceland)", "EBCDIC-CP-IS"); // NOI18N
    encodingList.put ("EBCDIC-CP-AR2 (EBCDIC: Urdu)", "EBCDIC-CP-AR2"); // NOI18N
  }
  protected static final String AUX_MENU_COMPONENT = "RADVisualFormContainer_MenuComponent"; // NOI18N

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

  /** Called to obtain a Java code to be used to generate code to access the container for adding subcomponents.
  * It is expected that the returned code is either "" (in which case the form is the container) or is a name of variable
  * or method call ending with "." (e.g. "container.getContentPane ().").
  * This implementation simply delegates to FormInfo.getContainerGenName ().
  * @return the prefix code for generating code to add subcomponents to this container
  */
  public String getContainerGenName () {
    return formInfo.getContainerGenName ();
  }

// ------------------------------------------------------------------------------
// Form synthetic properties

  public FormInfo getFormInfo () {
    return formInfo;
  }

  /** Getter for the Name property of the component - overriden to provide non-null value, 
  * as the top-level component does not have a variable
  * @return current value of the Name property
  */
  public String getName () {
    return FormEditor.getFormBundle ().getString ("CTL_FormTopContainerName");
  }

  /** Setter for the Name property of the component - usually maps to variable declaration for holding the 
  * instance of the component
  * @param value new value of the Name property
  */
  public void setName (String value) {
    // noop in forms
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
      if (menu != null) { // menu with the specified name not found
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
    getFormManager ().fireCodeChange ();
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
    getFormManager ().fireCodeChange ();
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
    getFormManager ().fireCodeChange ();
  }

  public boolean getGeneratePosition () {
    return generatePosition;
  }

  public void setGeneratePosition (boolean value) {
    // [PENDING - set as aux value]
    generatePosition = value;
    getFormManager ().fireCodeChange ();
  }

  public boolean getGenerateSize () {
    return generateSize;
  }

  public void setGenerateSize (boolean value) {
    // [PENDING - set as aux value]
    generateSize = value;
    getFormManager ().fireCodeChange ();
  }

  public boolean getGenerateCenter () {
    return generateCenter;
  }

  public void setGenerateCenter (boolean value) {
    // [PENDING - set as aux value]
    generateCenter = value;
    getFormManager ().fireCodeChange ();
  }

  public int getFormSizePolicy () {
    if (formInfo instanceof JAppletFormInfo
        || formInfo instanceof AppletFormInfo
        || formInfo instanceof JPanelFormInfo
        || formInfo instanceof PanelFormInfo) {
      return GEN_NOTHING;
    }

    return formSizePolicy;
  }

  public void setFormSizePolicy (int value) {
    // [PENDING - set as aux value]
    formSizePolicy = value;
    getFormManager ().fireCodeChange ();
  }

// ------------------------------------------------------------------------------
// End of form synthetic properties

  protected Node.Property[] createSyntheticProperties () {
    if (!getFormManager ().getFormEditorSupport ().supportsAdvancedFeatures ()) {
      if ((formInfo instanceof JMenuBarContainer) || (formInfo instanceof MenuBarContainer)) {
        return new Node.Property[] { createMenuProperty () } ;
      } else {
        return new Node.Property[0];
      }
    }

    Node.Property policyProperty = new PropertySupport.ReadWrite (PROP_FORM_SIZE_POLICY, Integer.TYPE, 
        FormEditor.getFormBundle ().getString ("MSG_FormSizePolicy"), 
        FormEditor.getFormBundle ().getString ("MSG_FormSizePolicy")) {
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


    Node.Property sizeProperty = new PropertySupport.ReadWrite (PROP_FORM_SIZE, Dimension.class, 
        FormEditor.getFormBundle ().getString ("MSG_FormSize"), 
        FormEditor.getFormBundle ().getString ("MSG_FormSize")) {
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

     Node.Property positionProperty = new PropertySupport.ReadWrite (PROP_FORM_POSITION, Point.class, 
        FormEditor.getFormBundle ().getString ("MSG_FormPosition"), 
        FormEditor.getFormBundle ().getString ("MSG_FormPosition")) {
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

    Node.Property genPositionProperty = new PropertySupport.ReadWrite (PROP_GENERATE_POSITION, Boolean.TYPE, 
        FormEditor.getFormBundle ().getString ("MSG_GeneratePosition"), 
        FormEditor.getFormBundle ().getString ("MSG_GeneratePosition")) {
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

    Node.Property genSizeProperty = new PropertySupport.ReadWrite (PROP_GENERATE_SIZE, Boolean.TYPE, 
        FormEditor.getFormBundle ().getString ("MSG_GenerateSize"), 
        FormEditor.getFormBundle ().getString ("MSG_GenerateSize")) {
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

    Node.Property genCenterProperty = new PropertySupport.ReadWrite (PROP_GENERATE_CENTER, Boolean.TYPE, 
        FormEditor.getFormBundle ().getString ("MSG_GenerateCenter"), 
        FormEditor.getFormBundle ().getString ("MSG_GenerateCenter")) {
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

    Node.Property genEncodingProperty = new EncodingProperty ("encoding", String.class, // NOI18N
        FormEditor.getFormBundle ().getString ("MSG_FormEncoding"),
        FormEditor.getFormBundle ().getString ("MSG_FormEncodingDesc")) {
      public void setValue (Object value) {
        if (!(value instanceof String)) {
          throw new IllegalArgumentException ();
        }
        getFormManager ().setEncoding ((String) value);
      }
    
      
      public Object getValue () {
        Object value = getFormManager ().getEncoding ();
        if (value == null) {
          value = ""; // NOI18N
        }
        return value;
      }

    };

    // the order of if's is important, JAppletFormInfo implements
    // JMenuBarContainer
    
    if (formInfo instanceof JAppletFormInfo) {
      return new Node.Property[] { createMenuProperty(),
                                   genEncodingProperty, };
    }
    else if (formInfo instanceof AppletFormInfo
             || formInfo instanceof PanelFormInfo
             || formInfo instanceof JPanelFormInfo) {
      return new Node.Property[] { genEncodingProperty, };
    }
    else if (formInfo instanceof JMenuBarContainer
        || formInfo instanceof MenuBarContainer) {
      return new Node.Property[] { createMenuProperty(),
                                   sizeProperty,
                                   positionProperty,
                                   policyProperty,
                                   genPositionProperty,
                                   genSizeProperty,
                                   genCenterProperty,
                                   genEncodingProperty,
      };
    }
    else {
      return new Node.Property[] { sizeProperty,
                                   positionProperty,
                                   policyProperty,
                                   genPositionProperty,
                                   genSizeProperty,
                                   genCenterProperty,
                                   genEncodingProperty,
      };
    }
  }

  private Node.Property createMenuProperty () {
    return new PropertySupport.ReadWrite (PROP_MENU_BAR, String.class, 
        FormEditor.getFormBundle ().getString ("MSG_MenuBar"), 
        FormEditor.getFormBundle ().getString ("MSG_MenuBarDesc")) {
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

  abstract class EncodingProperty extends PropertySupport.ReadWrite {
    EncodingProperty (String name, Class type, String displayName, String shortDescription) {
      super (name, type, displayName, shortDescription);
    }
    
    /** Editor with list of encodings */
    public java.beans.PropertyEditor getPropertyEditor () {
      return new EncodingEditor ();
    }
    
    class EncodingEditor extends java.beans.PropertyEditorSupport implements EnhancedPropertyEditor {
      /**
      * @return true if this PropertyEditor provides a enhanced in-place custom 
      *              property editor, false otherwise
      */
      public boolean hasInPlaceCustomEditor () {
        return true;
      }
      
      public void setAsText (String value) {
        String newValue = (String) encodingList.get (value);
        if (newValue == null)
          newValue = value;

        // XXX(-tdt) test if the encoding is supported by the JDK
        
        try {
          String x = new String(new byte[0], 0, 0, newValue);
        }
        catch (java.io.UnsupportedEncodingException ex) {
          throw new IllegalArgumentException(
              FormEditor.getFormBundle().getString("ERR_UnsupportedEncoding"));
        }
        
        setValue (newValue);
        if (!getFormManager ().getFormObject ().isModified ()) {
          getFormManager ().getFormObject ().setModified (true);
        }
      }
      public String getAsText () {
        return getValue ().toString ();
      }
          
      public java.awt.Component getInPlaceCustomEditor () {
        final JComboBox eventBox = new JComboBox ();
        eventBox.setEditable(true);

        java.util.Iterator iter = encodingList.keySet().iterator();
        while (iter.hasNext())
          eventBox.addItem(iter.next());

        eventBox.setSelectedItem(getAsText());
        eventBox.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent e) {
              try {
                setAsText((String) eventBox.getEditor().getItem());
              }
              catch (IllegalArgumentException ex) {
                TopManager.getDefault().notify(new NotifyDescriptor.Message(
                    ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
              }
            }
          }
        );
        return eventBox;
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
 *  27   Gandalf-post-FCS1.24.1.1    3/20/00  Tran Duc Trung  
 *  26   Gandalf-post-FCS1.24.1.0    3/20/00  Tran Duc Trung  FIX #6008: don't 
 *       generate resize code for applet and panel
 *  25   Gandalf   1.24        1/13/00  Ian Formanek    NOI18N #2
 *  24   Gandalf   1.23        1/12/00  Pavel Buzek     I18N
 *  23   Gandalf   1.22        1/11/00  Pavel Buzek     
 *  22   Gandalf   1.21        1/10/00  Pavel Buzek     #5088
 *  21   Gandalf   1.20        1/5/00   Ian Formanek    NOI18N
 *  20   Gandalf   1.19        12/14/99 Pavel Buzek     
 *  19   Gandalf   1.18        12/13/99 Pavel Buzek     
 *  18   Gandalf   1.17        11/24/99 Pavel Buzek     list of encodings for 
 *       Encoding property, editor changed to combo box
 *  17   Gandalf   1.16        11/15/99 Pavel Buzek     property for encoding
 *  16   Gandalf   1.15        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        10/9/99  Ian Formanek    Fixed bug 4045 - 
 *       Exception while opening form with menus. The form did not open due to 
 *       this exception.
 *  14   Gandalf   1.13        9/29/99  Ian Formanek    codeChanged added to 
 *       FormListener
 *  13   Gandalf   1.12        8/15/99  Ian Formanek    getContainerGenName 
 *       usage clarified
 *  12   Gandalf   1.11        7/30/99  Ian Formanek    Fixed bugs 2915 - 
 *       Changing "viewport" property of the JScrollPane does not work - 
 *       "Property" and 2916 - Changing "viewport" property of the JScrollPane 
 *       does not work - "Method Call"
 *  11   Gandalf   1.10        7/25/99  Ian Formanek    Variables management 
 *       moved to RADComponent
 *  10   Gandalf   1.9         7/14/99  Ian Formanek    synthetic "menu" 
 *       property does not require supportsAdvancedFeatures () to return true in
 *       the current persistence manager
 *  9    Gandalf   1.8         7/11/99  Ian Formanek    Some synthetic 
 *       properties are available only if supportsAdvancedFeatures of current 
 *       persistence manager returns true
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
