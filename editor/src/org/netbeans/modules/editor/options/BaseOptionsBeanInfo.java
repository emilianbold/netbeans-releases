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

package com.netbeans.developer.modules.text.options;

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

import com.netbeans.editor.BaseCaret;
import com.netbeans.editor.Coloring;
import com.netbeans.editor.MultiKeyBinding;

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
    this("/com/netbeans/developer/modules/text/resources/baseOptions");
  }

  public BaseOptionsBeanInfo(String iconPrefix) {
    this(iconPrefix, "");
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
          descriptors[i].setDisplayName(getString("PROP_" + bundlePrefix + propNames[i]));
          descriptors[i].setShortDescription(getString("HINT_" + bundlePrefix + propNames[i]));
        }

        setPropertyEditor(BaseOptions.ABBREV_MAP_PROP, AbbrevMapEditor.class);
        setPropertyEditor(BaseOptions.CARET_TYPE_INSERT_MODE_PROP, CaretTypeEditor.class);
        setPropertyEditor(BaseOptions.CARET_TYPE_OVERWRITE_MODE_PROP, CaretTypeEditor.class);
        setPropertyEditor(BaseOptions.KEY_BINDING_LIST_PROP, KeyBindingListPropertyEditor.class);
        setPropertyEditor(BaseOptions.COLORING_MAP_PROP, ColoringArrayEditor.class);

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
        icon = loadImage(iconPrefix + ".gif");
      return icon;
    }
    else {
      if (icon32 == null)
        icon32 = loadImage(iconPrefix + "32.gif");
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
      getString("LINE_CARET"),
      getString("THIN_LINE_CARET"),
      getString("BLOCK_CARET")
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

  // ------------------------- string pair editor ------------------------
  public abstract static class PairPropertyEditor extends PropertyEditorSupport {

    public final boolean supportsCustomEditor() {
      return true;
    }
    public final String getAsText() {
      return null;
    }
    public final void setAsText(String s) {
    }

    protected abstract String[] getTexts();
    protected abstract void addInternal(String text);
    protected abstract void removeInternal(String text);
    protected abstract void changeInternal(int idx, String[] text);
    
    KeyBindingsPanel kbPanel;

    public java.awt.Component getCustomEditor() {
      kbPanel = new KeyBindingsPanel();
      kbPanel.setPropertyEditor(this);
      HelpCtx.setHelpIDString (kbPanel, getHelpCtx ().getHelpID ());
      return kbPanel;
    }
    
    protected HelpCtx getHelpCtx () {
      return HelpCtx.DEFAULT_HELP;
    }

  }

  // ------------------------- key bindings ------------------------
  public static class KeyBindingListPropertyEditor extends PairPropertyEditor {

    protected HelpCtx getHelpCtx () {
      return new HelpCtx (KeyBindingListPropertyEditor.class);
    }
    
    protected int getLayoutSize() {
      return ((List) getValue()).size();
    }
    
    protected String[] getTexts() {
      List list = (List) getValue();
      String[] texts = new String[list.size()];
      for (int i = 0; i < texts.length; i++) {
        texts[i] = bind2Text((KeyBinding) list.get(i));
      }
      return texts;
    }

    /*
    protected ActionListener getActionListener(int i) {
      return new KBFieldActListener((List) getValue(), i);
    } */

    protected void addInternal(String text) {
      KeyBinding kb = text2Bind(text);
      ((List) getValue()).add(kb);
//      return new KBFieldActListener((List) getValue(), ((List) getValue()).size() - 1);
    }

    protected void removeInternal(String text) {
      KeyBinding kb = text2Bind(text);
      String act = kb.actionName;

      List list = (List) getValue();
      Iterator iter = list.iterator();

      while (iter.hasNext()) {
        KeyBinding kbind = (KeyBinding) iter.next();
        if (kbind.actionName.equals(act)) {
          iter.remove();
          return;
        }
      }
    }

    protected void changeInternal(int idx, String[] text) {
      KeyBinding kb = text2Bind(text[0] + " " + text[1]);
      List list = (List) getValue();
      list.set(idx, kb);
    }


    // KeyBinding
    static class KBFieldActListener implements ActionListener {
      List list;
      KeyBinding kbind;

      KBFieldActListener(List list, int i) {
        this. list = list;
        kbind = (KeyBinding) list.get(i);
      }
      
      public void actionPerformed(java.awt.event.ActionEvent ev) {
        JTextField my = (JTextField) ev.getSource();
        KeyBinding kb = text2Bind(my.getText());
        list.set(getIndex(), kb);
        this.kbind = kb;
        my.repaint();
      }

      int getIndex() {
        return list.indexOf(kbind);
      }
    }

    static String bind2Text(KeyBinding kb) {
      StringBuffer buff = new StringBuffer(kb.actionName).append(' ');
      if (kb instanceof MultiKeyBinding) {
        MultiKeyBinding mkb = (MultiKeyBinding) kb;
        if (mkb.keys != null) {
          if (mkb.keys.length > 0) {
            buff.append(mkb.keys[0].toString());
            for (int i = 1; i < mkb.keys.length; i++) {
              buff.append(", ").append(key2String(mkb.keys[i]));
            }
          }
          return buff.toString();
        }
      }

      if (kb.key != null) {
        buff.append(key2String(kb.key));
      }
      return buff.toString();
    }

    static KeyBinding text2Bind(String s) {
      int firstSpace = s.indexOf(' ');
      if (firstSpace <= 0) {
        firstSpace = s.length();
      }
      String act = s.substring(0, firstSpace);
      List strokes = new ArrayList();

      s = s.substring(firstSpace);
      while (s.endsWith(",")) {
        s = s.substring(0, s.length() - 1);
      }
      
      int idx = -1, idx2 = -1;

      while ((idx = s.indexOf(',', idx)) > 0) {
        String stroke = s.substring(idx2 + 1, idx).trim();
        if (! stroke.equals("")) {
          KeyStroke ks = string2Key(stroke);
          if (ks == null) {
            throw new IllegalArgumentException();
          }
          strokes.add(ks);
        }
        idx2 = idx++;
      }

      if (idx2 < 0) {
        idx = 0;
      } else {
        idx = idx2 + 1;
      }
      
      if (idx < s.length()) {
        String stroke = s.substring(idx).trim();
        if (! stroke.equals("")) {
          KeyStroke ks = string2Key(stroke);
          if (ks == null) {
//            System.out.println( "bad binding" );
            throw new IllegalArgumentException();
          }
          strokes.add(ks);
        }
      }

      KeyStroke[] kstrokes = new KeyStroke[strokes.size()];
      kstrokes = (KeyStroke[]) strokes.toArray(kstrokes);
      
      if (kstrokes.length == 1) {
        return new KeyBinding(kstrokes[0], act);
      } else if (kstrokes.length == 0) {
        return new KeyBinding(null, act);
      } else {
        return new MultiKeyBinding(kstrokes, act);
      }
    }

    static String key2String(KeyStroke ks) {
      return Utilities.keyToString(ks);
    }

    static KeyStroke string2Key(String str) {
      return Utilities.stringToKey(str);
    }
  }


  // ------------------------- abbrevs ------------------------
  public static class AbbrevMapEditor extends PairPropertyEditor {

    protected HelpCtx getHelpCtx () {
      return new HelpCtx (AbbrevMapEditor.class);
    }
    
    protected String[] getTexts() {
      Map map = (Map) getValue();
      Iterator iter = map.entrySet().iterator();
      String[] texts = new String[map.size()];

      int i = 0;
      while (iter.hasNext()) {
        Entry entry = (Entry) iter.next();
        texts[i++] = entry.getKey() + " " + entry.getValue();
      }

      return texts;
    }

    protected void addInternal(String text) {
      String[] entry = toEntry(text);
      Map map = (Map) getValue();
      map.put(entry[0], entry[1]);
//      return new EntryActListener((Map) getValue());
    }
    
    protected void removeInternal(String text) {
      String[] entry = toEntry(text);
      Map map = (Map) getValue();
      map.remove(entry[0]);
    }

    protected void changeInternal(int i, String[] text) {
      Map map = (Map) getValue();
      map.put(text[0], text[1]);
    }
    


    static class EntryActListener implements java.awt.event.ActionListener {
      Map map;

      EntryActListener(Map map) {
        this.map = map;
      }
      
      public void actionPerformed(java.awt.event.ActionEvent ev) {
        JTextField my = (JTextField) ev.getSource();
        String[] entry = toEntry(my.getText());
        map.put(entry[0], entry[1]);
        my.repaint();
      }
    }

    static String[] toEntry(String s) {
      int firstSpace = s.indexOf(' ');
      if (firstSpace <= 0) {
        throw new IllegalArgumentException();
      }
      String[] entry = new String[2];
      entry[0] = s.substring(0, firstSpace).trim();
      entry[1] = s.substring(firstSpace).trim();
      return entry;
    }

  }
}

/*
* Log
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
