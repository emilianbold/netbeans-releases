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

import java.awt.Image;
import java.beans.*;

/** A BeanInfo for FormLoaderSettings.
* @author Ian Formanek
* @version 0.11, May 22, 1998
*/
public class FormLoaderSettingsBeanInfo extends SimpleBeanInfo {

  /** Icons for url data loader. */
  private static Image icon;
  private static Image icon32;

  /** Array of property descriptors. */
  private static PropertyDescriptor[] desc;

  private static java.util.ResourceBundle formBundle = org.openide.util.NbBundle.getBundle (FormLoaderSettingsBeanInfo.class);

  // initialization of the array of descriptors
  static {
    try {
      if (System.getProperty ("netbeans.full.hack") == null) {
        desc = new PropertyDescriptor[] {
          new PropertyDescriptor (FormLoaderSettings.PROP_INDENT_AWT_HIERARCHY, FormLoaderSettings.class, 
                                  "getIndentAWTHierarchy", "setIndentAWTHierarchy"),
          new PropertyDescriptor (FormLoaderSettings.PROP_SORT_EVENT_SETS, FormLoaderSettings.class, 
                                  "getSortEventSets", "setSortEventSets"),
          new PropertyDescriptor (FormLoaderSettings.PROP_EVENT_VARIABLE_NAME, FormLoaderSettings.class, 
                                  "getEventVariableName", "setEventVariableName"),
          new PropertyDescriptor (FormLoaderSettings.PROP_SHORT_BEAN_NAMES, FormLoaderSettings.class, 
                                  "getShortBeanNames", "setShortBeanNames"),
          new PropertyDescriptor (FormLoaderSettings.PROP_SELECTION_BORDER_SIZE, FormLoaderSettings.class, 
                                  "getSelectionBorderSize", "setSelectionBorderSize"),
          new PropertyDescriptor (FormLoaderSettings.PROP_SELECTION_BORDER_COLOR, FormLoaderSettings.class, 
                                  "getSelectionBorderColor", "setSelectionBorderColor"),
          new PropertyDescriptor (FormLoaderSettings.PROP_CONNECTION_BORDER_COLOR, FormLoaderSettings.class, 
                                  "getConnectionBorderColor", "setConnectionBorderColor"),
          new PropertyDescriptor (FormLoaderSettings.PROP_DRAG_BORDER_COLOR, FormLoaderSettings.class, 
                                  "getDragBorderColor", "setDragBorderColor"),
          new PropertyDescriptor (FormLoaderSettings.PROP_SHOW_GRID, FormLoaderSettings.class, 
                                  "getShowGrid", "setShowGrid"),
          new PropertyDescriptor (FormLoaderSettings.PROP_GRID_X, FormLoaderSettings.class, 
                                  "getGridX", "setGridX"),
          new PropertyDescriptor (FormLoaderSettings.PROP_GRID_Y, FormLoaderSettings.class, 
                                  "getGridY", "setGridY"),
          new PropertyDescriptor (FormLoaderSettings.PROP_APPLY_GRID_TO_POSITION, FormLoaderSettings.class, 
                                  "getApplyGridToPosition", "setApplyGridToPosition"),
          new PropertyDescriptor (FormLoaderSettings.PROP_APPLY_GRID_TO_SIZE, FormLoaderSettings.class, 
                                  "getApplyGridToSize", "setApplyGridToSize"),
          new PropertyDescriptor (FormLoaderSettings.PROP_VARIABLES_MODIFIER, FormLoaderSettings.class, 
                                  "getVariablesModifier", "setVariablesModifier"),
          new PropertyDescriptor (FormLoaderSettings.PROP_EDITOR_SEARCH_PATH, FormLoaderSettings.class, 
                                  "getEditorSearchPath", "setEditorSearchPath"),
          new PropertyDescriptor (FormLoaderSettings.PROP_REGISTERED_EDITORS, FormLoaderSettings.class, 
                                  "getRegisteredEditors", "setRegisteredEditors"),
        };
      } else {
        desc = new PropertyDescriptor[] {
          new PropertyDescriptor (FormLoaderSettings.PROP_INDENT_AWT_HIERARCHY, FormLoaderSettings.class, 
                                  "getIndentAWTHierarchy", "setIndentAWTHierarchy"),
          new PropertyDescriptor (FormLoaderSettings.PROP_SORT_EVENT_SETS, FormLoaderSettings.class, 
                                  "getSortEventSets", "setSortEventSets"),
          new PropertyDescriptor (FormLoaderSettings.PROP_EVENT_VARIABLE_NAME, FormLoaderSettings.class, 
                                  "getEventVariableName", "setEventVariableName"),
          new PropertyDescriptor (FormLoaderSettings.PROP_SHORT_BEAN_NAMES, FormLoaderSettings.class, 
                                  "getShortBeanNames", "setShortBeanNames"),
          new PropertyDescriptor (FormLoaderSettings.PROP_SELECTION_BORDER_SIZE, FormLoaderSettings.class, 
                                  "getSelectionBorderSize", "setSelectionBorderSize"),
          new PropertyDescriptor (FormLoaderSettings.PROP_SELECTION_BORDER_COLOR, FormLoaderSettings.class, 
                                  "getSelectionBorderColor", "setSelectionBorderColor"),
          new PropertyDescriptor (FormLoaderSettings.PROP_CONNECTION_BORDER_COLOR, FormLoaderSettings.class, 
                                  "getConnectionBorderColor", "setConnectionBorderColor"),
          new PropertyDescriptor (FormLoaderSettings.PROP_DRAG_BORDER_COLOR, FormLoaderSettings.class, 
                                  "getDragBorderColor", "setDragBorderColor"),
          new PropertyDescriptor (FormLoaderSettings.PROP_SHOW_GRID, FormLoaderSettings.class, 
                                  "getShowGrid", "setShowGrid"),
          new PropertyDescriptor (FormLoaderSettings.PROP_GRID_X, FormLoaderSettings.class, 
                                  "getGridX", "setGridX"),
          new PropertyDescriptor (FormLoaderSettings.PROP_GRID_Y, FormLoaderSettings.class, 
                                  "getGridY", "setGridY"),
          new PropertyDescriptor (FormLoaderSettings.PROP_APPLY_GRID_TO_POSITION, FormLoaderSettings.class, 
                                  "getApplyGridToPosition", "setApplyGridToPosition"),
          new PropertyDescriptor (FormLoaderSettings.PROP_APPLY_GRID_TO_SIZE, FormLoaderSettings.class, 
                                  "getApplyGridToSize", "setApplyGridToSize"),
          new PropertyDescriptor (FormLoaderSettings.PROP_VARIABLES_MODIFIER, FormLoaderSettings.class, 
                                  "getVariablesModifier", "setVariablesModifier"),
          new PropertyDescriptor (FormLoaderSettings.PROP_EDITOR_SEARCH_PATH, FormLoaderSettings.class, 
                                  "getEditorSearchPath", "setEditorSearchPath"),
          new PropertyDescriptor (FormLoaderSettings.PROP_REGISTERED_EDITORS, FormLoaderSettings.class, 
                                  "getRegisteredEditors", "setRegisteredEditors"),
          new PropertyDescriptor ("emptyFormType", FormLoaderSettings.class, 
                                  "getEmptyFormType", "setEmptyFormType"),
        };

      }
      desc[0].setDisplayName (formBundle.getString ("PROP_INDENT_AWT_HIERARCHY"));
      desc[0].setShortDescription (formBundle.getString ("HINT_INDENT_AWT_HIERARCHY"));
      desc[1].setDisplayName (formBundle.getString ("PROP_SORT_EVENT_SETS"));
      desc[1].setShortDescription (formBundle.getString ("HINT_SORT_EVENT_SETS"));
      desc[1].setExpert (true);
      desc[1].setHidden (true);
      desc[2].setDisplayName (formBundle.getString ("PROP_EVENT_VARIABLE_NAME"));
      desc[2].setShortDescription (formBundle.getString ("HINT_EVENT_VARIABLE_NAME"));
      desc[3].setDisplayName (formBundle.getString ("PROP_SHORT_BEAN_NAMES"));
      desc[3].setShortDescription (formBundle.getString ("HINT_SHORT_BEAN_NAMES"));
      desc[3].setHidden (true);
      desc[3].setExpert (true);
      desc[4].setDisplayName (formBundle.getString ("PROP_SELECTION_BORDER_SIZE"));
      desc[4].setShortDescription (formBundle.getString ("HINT_SELECTION_BORDER_SIZE"));
      desc[4].setExpert (true);
      desc[5].setDisplayName (formBundle.getString ("PROP_SELECTION_BORDER_COLOR"));
      desc[5].setShortDescription (formBundle.getString ("HINT_SELECTION_BORDER_COLOR"));
      desc[5].setExpert (true);
      desc[6].setDisplayName (formBundle.getString ("PROP_CONNECTION_BORDER_COLOR"));
      desc[6].setShortDescription (formBundle.getString ("HINT_CONNECTION_BORDER_COLOR"));
      desc[6].setExpert (true);
      desc[7].setDisplayName (formBundle.getString ("PROP_DRAG_BORDER_COLOR"));
      desc[7].setShortDescription (formBundle.getString ("HINT_DRAG_BORDER_COLOR"));
      desc[7].setExpert (true);
      desc[8].setDisplayName (formBundle.getString ("PROP_SHOW_GRID"));
      desc[8].setShortDescription (formBundle.getString ("HINT_SHOW_GRID"));
      desc[9].setDisplayName (formBundle.getString ("PROP_GRID_X"));
      desc[9].setShortDescription (formBundle.getString ("HINT_GRID_X"));
      desc[10].setDisplayName (formBundle.getString ("PROP_GRID_Y"));
      desc[10].setShortDescription (formBundle.getString ("HINT_GRID_Y"));
      desc[11].setDisplayName (formBundle.getString ("PROP_APPLY_GRID_TO_POSITION"));
      desc[11].setShortDescription (formBundle.getString ("HINT_APPLY_GRID_TO_POSITION"));
      desc[11].setExpert (true);
      desc[12].setDisplayName (formBundle.getString ("PROP_APPLY_GRID_TO_SIZE"));
      desc[12].setShortDescription (formBundle.getString ("HINT_APPLY_GRID_TO_SIZE"));
      desc[12].setExpert (true);
      desc[13].setDisplayName (formBundle.getString ("PROP_VARIABLES_MODIFIER"));
      desc[13].setShortDescription (formBundle.getString ("HINT_VARIABLES_MODIFIER"));
      desc[13].setPropertyEditorClass (ModifierPropertyEditor.class);
      desc[14].setDisplayName (formBundle.getString ("PROP_EDITOR_SEARCH_PATH"));
      desc[14].setShortDescription (formBundle.getString ("HINT_EDITOR_SEARCH_PATH"));
      desc[15].setDisplayName (formBundle.getString ("PROP_REGISTERED_EDITORS"));
      desc[15].setShortDescription (formBundle.getString ("HINT_REGISTERED_EDITORS"));

    } catch (IntrospectionException ex) {
      throw new InternalError ();
    }
  }


  /** Descriptor of valid properties
  * @return array of properties
  */
  public PropertyDescriptor[] getPropertyDescriptors () {
    return desc;
  }

  /** Returns the FormLoaderSettings' icon */
  public Image getIcon(int type) {
    if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
        (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
      if (icon == null)
        icon = loadImage("/com/netbeans/developer/modules/loaders/form/resources/formSettings.gif");
      return icon;
    } else {
      if (icon32 == null)
        icon32 = loadImage ("/com/netbeans/developer/modules/loaders/form/resources/formSettings32.gif");
      return icon32;
    }
  }

  final public static class ModifierPropertyEditor extends java.beans.PropertyEditorSupport {
    /** Display Names for alignment. */
    private static final String[] names = {
      formBundle.getString ("VALUE_PRIVATE"),
      formBundle.getString ("VALUE_PACKAGE_PRIVATE"),
      formBundle.getString ("VALUE_PROTECTED"),
      formBundle.getString ("VALUE_PUBLIC"),
    };

    /** @return names of the possible directions */
    public String[] getTags () {
      return names;
    }

    /** @return text for the current value */
    public String getAsText () {
      int value = ((Integer)getValue ()).intValue ();
      
      if ((value >= 0) && (value < 4)) {
        return names [value];
      }
      else return null;
    }

    /** Setter.
    * @param str string equal to one value from directions array
    */
    public void setAsText (String str) {
      for (int i = 0; i < 4; i ++)
        if (names[i].equals (str)) {
          setValue (new Integer (i));
          return;
        }
    }

  }

}

/*
 * Log
 *  9    Gandalf   1.8         6/10/99  Ian Formanek    loadedBeans -> 
 *       properties rather than FormSettings
 *  8    Gandalf   1.7         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         6/7/99   Ian Formanek    
 *  6    Gandalf   1.5         5/30/99  Ian Formanek    PropertyEditors 
 *       management options
 *  5    Gandalf   1.4         3/29/99  Ian Formanek    
 *  4    Gandalf   1.3         3/29/99  Ian Formanek    Icon change
 *  3    Gandalf   1.2         3/16/99  Ian Formanek    
 *  2    Gandalf   1.1         3/10/99  Ian Formanek    Gandalf updated
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */

