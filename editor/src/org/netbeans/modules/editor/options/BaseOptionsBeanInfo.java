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

package org.netbeans.modules.editor.options;

import java.beans.*;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent.KeyBinding;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import org.netbeans.editor.BaseCaret;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.MultiKeyBinding;

/** BeanInfo for base options
*
* @author Miloslav Metelka, Ales Novak
*/
public class BaseOptionsBeanInfo extends SimpleBeanInfo {

  private ResourceBundle bundle;

  /** Prefix of the icon location. */
  private String iconPrefix;
  
  /** Prefix for getting localized strings for property name and hint */
  private String bundlePrefix;

  /** Icons for compiler settings objects. */
  private Image icon;
  private Image icon32;

  /** Propertydescriptors */
  PropertyDescriptor[] descriptors;
  
  private static final String[] EXPERT_PROP_NAMES = new String[] {
    BaseOptions.CARET_BLINK_RATE_PROP,
    BaseOptions.CARET_ITALIC_INSERT_MODE_PROP,
    BaseOptions.CARET_ITALIC_OVERWRITE_MODE_PROP,
    BaseOptions.CARET_TYPE_INSERT_MODE_PROP,
    BaseOptions.CARET_TYPE_OVERWRITE_MODE_PROP,
    BaseOptions.CARET_COLOR_INSERT_MODE_PROP,
    BaseOptions.CARET_COLOR_OVERWRITE_MODE_PROP,
    BaseOptions.HIGHLIGHT_CARET_ROW_PROP,
    BaseOptions.HIGHLIGHT_MATCHING_BRACKET_PROP,
    BaseOptions.LINE_HEIGHT_CORRECTION_PROP,
    BaseOptions.LINE_NUMBER_MARGIN_PROP,
    BaseOptions.MARGIN_PROP,
    BaseOptions.SCROLL_JUMP_INSETS_PROP,
    BaseOptions.SCROLL_FIND_INSETS_PROP,
    BaseOptions.STATUS_BAR_CARET_DELAY_PROP,
    BaseOptions.STATUS_BAR_VISIBLE_PROP,
    BaseOptions.TEXT_LIMIT_LINE_COLOR_PROP,
    BaseOptions.TEXT_LIMIT_LINE_VISIBLE_PROP,
    BaseOptions.TEXT_LIMIT_WIDTH_PROP,
  };


  public BaseOptionsBeanInfo() {
    this("/org/netbeans/modules/editor/resources/baseOptions"); // NOI18N
  }

  public BaseOptionsBeanInfo(String iconPrefix) {
    this(iconPrefix, ""); // NOI18N
  }
  
  public BaseOptionsBeanInfo(String iconPrefix, String bundlePrefix) {
    this.iconPrefix = iconPrefix;
    this.bundlePrefix = bundlePrefix;
  }

  /*
  * @return Returns an array of PropertyDescriptors
  * describing the editable properties supported by this bean.
  */
  public PropertyDescriptor[] getPropertyDescriptors () {
    if (descriptors == null) {
      String[] propNames = getPropNames();
      try {
        descriptors = new PropertyDescriptor[propNames.length];
        
        for (int i = 0; i < propNames.length; i++) {
          descriptors[i] = new PropertyDescriptor(propNames[i], getBeanClass());
          descriptors[i].setDisplayName(getString("PROP_" + bundlePrefix + propNames[i])); // NOI18N
          descriptors[i].setShortDescription(getString("HINT_" + bundlePrefix + propNames[i])); // NOI18N
        }

        setPropertyEditor(BaseOptions.ABBREV_MAP_PROP, AbbrevsEditor.class);
        setPropertyEditor(BaseOptions.CARET_TYPE_INSERT_MODE_PROP, CaretTypeEditor.class);
        setPropertyEditor(BaseOptions.CARET_TYPE_OVERWRITE_MODE_PROP, CaretTypeEditor.class);
        setPropertyEditor(BaseOptions.KEY_BINDING_LIST_PROP, KeyBindingsEditor.class);
        setPropertyEditor(BaseOptions.COLORING_MAP_PROP, ColoringArrayEditor.class);
        setPropertyEditor(BaseOptions.SCROLL_JUMP_INSETS_PROP, ScrollInsetsEditor.class);
        setPropertyEditor(BaseOptions.SCROLL_FIND_INSETS_PROP, ScrollInsetsEditor.class);

        setExpert(EXPERT_PROP_NAMES);

      } catch (IntrospectionException e) {
        descriptors = new PropertyDescriptor[0];
      }
    }
    return descriptors;
  }

  protected Class getBeanClass() {
    return BaseOptions.class;
  }
  
  protected String[] getPropNames() {
    return BaseOptions.BASE_PROP_NAMES;
  }
  
  protected PropertyDescriptor getPD(String prop) {
    String[] propNames = getPropNames();
    for (int i = 0; i < descriptors.length; i++) {
      if (prop.equals(propNames[i])) {
        return descriptors[i];
      }
    }
    return null;
  }
  
  protected void setPropertyEditor(String propName, Class propEditor) {
    PropertyDescriptor pd = getPD(propName);
    if (pd != null) {
      pd.setPropertyEditorClass(propEditor);
    }
  }
  
  protected void setExpert(String[] propNames) {
    for (int i = 0; i < propNames.length; i++) {
      PropertyDescriptor pd = getPD(propNames[i]);
      if (pd != null) {
        pd.setExpert(true);
      }
    }
  }
  
  protected void setHidden(String[] propNames) {
    for (int i = 0; i < propNames.length; i++) {
      PropertyDescriptor pd = getPD(propNames[i]);
      if (pd != null) {
        pd.setHidden(true);
      }
    }
  }    

  /* @param type Desired type of the icon
  * @return returns the Java loader's icon
  */
  public Image getIcon(final int type) {
    if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
      if (icon == null)
        icon = loadImage(iconPrefix + ".gif"); // NOI18N
      return icon;
    }
    else {
      if (icon32 == null)
        icon32 = loadImage(iconPrefix + "32.gif"); // NOI18N
      return icon32;
    }
  }

  /** @return localized string */
  protected String getString(String s) {
    if (bundle == null) {
      bundle = NbBundle.getBundle(BaseOptionsBeanInfo.class);
    }
    return bundle.getString(s);
  }

  // ------------------------ carets --------------------------------
  
  public static class CaretTypeEditor extends PropertyEditorSupport {

    private static ResourceBundle bundle;

    private static String[] tags = new String[] {
      BaseCaret.LINE_CARET,
      BaseCaret.THIN_LINE_CARET,
      BaseCaret.BLOCK_CARET
    };

    private static String[] locTags = new String[] {
      getString("LINE_CARET"), // NOI18N
      getString("THIN_LINE_CARET"), // NOI18N
      getString("BLOCK_CARET") // NOI18N
    };
      
    public String[] getTags() {
      return locTags;
    }

    public void setAsText(String txt) {
      for (int i = 0; i < locTags.length; i++) {
        if (locTags[i].equals(txt)) {
          setValue(tags[i]);
          break;
        }
      }
    }

    public String getAsText() {
      String val = (String) getValue();
      for (int i = 0; i < tags.length; i++) {
        if (tags[i].equals(val)) {
          return locTags[i];
        }
      }
      throw new IllegalStateException();
    }

    static String getString(String s) {
      if (bundle == null) {
        bundle = NbBundle.getBundle(BaseOptionsBeanInfo.class);
      }
      return bundle.getString(s);
    }

  }
}

/*
* Log
*  14   Gandalf-post-FCS1.11.1.1    3/10/00  Petr Nejedly    Added support for 
*       percentual scroll-jump insets
*  13   Gandalf-post-FCS1.11.1.0    2/28/00  Petr Nejedly    Redesign of 
*       ColoringEditor
*  12   Gandalf   1.11        1/13/00  Miloslav Metelka Localization
*  11   Gandalf   1.10        12/28/99 Miloslav Metelka 
*  10   Gandalf   1.9         11/16/99 Miloslav Metelka throwing of 
*       IllegalArgumentException
*  9    Gandalf   1.8         11/14/99 Miloslav Metelka 
*  8    Gandalf   1.7         11/11/99 Miloslav Metelka 
*  7    Gandalf   1.6         11/5/99  Jesse Glick     Context help jumbo patch.
*  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         9/15/99  Miloslav Metelka 
*  4    Gandalf   1.3         8/27/99  Miloslav Metelka 
*  3    Gandalf   1.2         8/17/99  Miloslav Metelka 
*  2    Gandalf   1.1         7/29/99  Miloslav Metelka 
*  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
* $
*/
