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

package com.netbeans.developer.editors;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.preview.JColorChooser;
import javax.swing.event.*;

import com.netbeans.ide.util.QuickSorter;
import com.netbeans.ide.util.NbBundle;

/** A property editor for Color class.
* @author   Jan Jancura, Ian Formanek
* @version  0.10, 09 Mar 1998
*/
public class ColorEditor implements PropertyEditor {
  // static .....................................................................................

  // the bundle to use
  static ResourceBundle bundle = NbBundle.getBundle (
    "com.netbeans.developer.impl.locales.ExplorerBundle");

  private static JColorChooser staticChooser;
  
  public static final int AWT_PALETTE = 1;
  public static final int SYSTEM_PALETTE = 2;
  public static final int SWING_PALETTE = 3;

  private static final String awtColorNames[] = {
                                    "white", "lightGray", "gray", "darkGray", "black",
                                    "red", "pink", "orange", "yellow", "green", "magenta",
                                    "cyan", "blue" };

  private static final Color awtColors[] = {
                                    Color.white, Color.lightGray, Color.gray, Color.darkGray,
                                    Color.black, Color.red, Color.pink, Color.orange, Color.yellow,
                                    Color.green, Color.magenta, Color.cyan, Color.blue };

  private static final String systemColorNames[] = {
                                    "Active Caption", "Active Caption Border",
                                    "Active Caption Text", "Control", "Control Dk Shadow",
                                    "Control Highlight", "Control Lt Highlight",
                                    "Control Shadow", "Control Text", "Desktop",
                                    "Inactive Caption", "Inactive Caption Border",
                                    "Inactive Caption Text", "Info", "Info Text", "Menu",
                                    "Menu Text", "Scrollbar", "Text", "Text Highlight",
                                    "Text Highlight Text", "Text Inactive Text", "Text Text",
                                    "Window", "Window Border", "Window Text"};

  private static final String systemGenerate[] = {
                                    "activeCaption", "activeCaptionBorder",
                                    "activeCaptionText", "control", "controlDkShadow",
                                    "controlHighlight", "controlLtHighlight",
                                    "controlShadow", "controlText", "desktop",
                                    "inactiveCaption", "inactiveCaptionBorder",
                                    "inactiveCaptionText", "info", "infoText", "menu",
                                    "menuText", "scrollbar", "text", "textHighlight",
                                    "textHighlightText", "textInactiveText", "textText",
                                    "window", "windowBorder", "windowText"};

  private static final Color systemColors[] = {
                                    SystemColor.activeCaption, SystemColor.activeCaptionBorder,
                                    SystemColor.activeCaptionText, SystemColor.control,
                                    SystemColor.controlDkShadow, SystemColor.controlHighlight,
                                    SystemColor.controlLtHighlight, SystemColor.controlShadow,
                                    SystemColor.controlText, SystemColor.desktop,
                                    SystemColor.inactiveCaption, SystemColor.inactiveCaptionBorder,
                                    SystemColor.inactiveCaptionText, SystemColor.info,
                                    SystemColor.infoText, SystemColor.menu,
                                    SystemColor.menuText, SystemColor.scrollbar, SystemColor.text,
                                    SystemColor.textHighlight, SystemColor.textHighlightText,
                                    SystemColor.textInactiveText, SystemColor.textText,
                                    SystemColor.window, SystemColor.windowBorder,
                                    SystemColor.windowText};

  /** Swing colors names and values are static and lazy initialized.
  * They are also cleared when l&f changes.
  */
  private static String swingColorNames[];
  private static Color swingColors[];

  static {
    UIManager.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        swingColorNames = null;
        swingColors = null;
      }
    });
    swingColorNames = null;
    swingColors = null;
  }
  
  // variables ..................................................................................


  public int palette = AWT_PALETTE;
  private SuperColor color;
  private PropertyChangeSupport support;


  public static JColorChooser getStaticChooser () {
    if (staticChooser == null) {
      staticChooser = new JColorChooser () {
        public void setColor (Color c) {
          if (c == null) return;
          super.setColor (c);
        }
      };
      staticChooser.addChooserPanel (
        bundle.getString ("CTL_AWTPalette"), 
        new NbColorChooserPanel (AWT_PALETTE, awtColorNames, awtColors)
      );
      initSwingConstants();
      staticChooser.addChooserPanel (
        bundle.getString ("CTL_SwingPalette"), 
        new NbColorChooserPanel (SWING_PALETTE, swingColorNames, swingColors)
      );
      staticChooser.addChooserPanel (
        bundle.getString ("CTL_SystemPalette"), 
        new NbColorChooserPanel (SYSTEM_PALETTE, systemColorNames, systemColors)
      );
    }
    return staticChooser;
  }
  
  // init .......................................................................................

  public ColorEditor() {
    support = new PropertyChangeSupport (this);
  }


  // main methods .......................................................................................

  public Object getValue () {
    return color;
  }

  public void setValue (Object object) {
    if (object != null) {
      if (object instanceof SuperColor) color = (SuperColor) object;
      else color = new SuperColor ((Color) object);
    }
    support.firePropertyChange ("", null, null);
  }

  public String getAsText () {
    if (color == null) return "null";
    return color.getAsText ();
  }

  public void setAsText (String string)
  throws IllegalArgumentException {
    int i1 = string.indexOf (44);
    int j1 = string.indexOf (44, i1 + 1);
    try {
      if (i1 < 0 || j1 < 0) throw new Exception ();
      int k = Integer.parseInt (string.substring (0, i1));
      int i2 = Integer.parseInt (string.substring (i1 + 1, j1));
      int j2 = Integer.parseInt (string.substring (j1 + 1));
      setValue (new SuperColor (null, 0, new Color (k, i2, j2)));
    } catch (Exception e) {

      int i;
      switch (palette) {
      default:
      case AWT_PALETTE:
        i = getIndex (awtColorNames, string);
        if (i < 0) break;
        setValue (new SuperColor (string, AWT_PALETTE, awtColors [i]));
        return;
      case SYSTEM_PALETTE:
        i = getIndex (systemColorNames, string);
        if (i < 0) break;
        setValue (new SuperColor (string, SYSTEM_PALETTE, systemColors [i]));
        return;
      case SWING_PALETTE:
        initSwingConstants();
        i = getIndex (swingColorNames, string);
        if (i < 0) break;
        setValue (new SuperColor (string, SWING_PALETTE, swingColors [i]));
        return;
      };
      throw new IllegalArgumentException (string);
    }
    return;
  }

  public String getJavaInitializationString () {
    if (color == null) return "null";
    if (color.getID () == null)
      return "new java.awt.Color (" + color.getRed () + ", " + color.getGreen () +
             ", " + color.getBlue () + ")";

    switch (color.getPalette ()) {
    default:
    case AWT_PALETTE:
      return "java.awt.Color." + color.getID ();
    case SYSTEM_PALETTE:
      return "java.awt.SystemColor." + systemGenerate [getIndex (systemColorNames, color.getID ())];
    case SWING_PALETTE:
      initSwingConstants();
      int i = getIndex (swingColorNames, color.getID ());
      if (i < 0) return "new java.awt.Color (" + color.getRed () + ", " + color.getGreen () +
                        ", " + color.getBlue () + ")";
      return "(java.awt.Color) javax.swing.UIManager.getDefaults ().get (\"" +
             color.getID () + "\")";
    };
  }

  public String[] getTags () {
    switch (palette) {
    case AWT_PALETTE:
      return awtColorNames;
    case SYSTEM_PALETTE:
      return systemColorNames;
    case SWING_PALETTE:
      initSwingConstants();
      return swingColorNames;
    default: return awtColorNames;
    };
  }

  public boolean isPaintable () {
    return true;
  }

  public void paintValue (Graphics g, Rectangle rectangle) {
    Color color = g.getColor ();
    if (this.color != null) {
      g.setColor (Color.black);
      g.drawRect (rectangle.x + 6, rectangle.y + rectangle.height / 2 - 5 , 10, 10);
      g.setColor (this.color);
      g.fillRect (rectangle.x + 7, rectangle.y + rectangle.height / 2 - 4 , 9, 9);
    }
    g.setColor (Color.black);
    FontMetrics fm = g.getFontMetrics ();
    g.drawString (getAsText (), rectangle.x + 22, rectangle.y +
      (rectangle.height - fm.getHeight ()) / 2 + fm.getAscent ());
    g.setColor (color);
  }

  public boolean supportsCustomEditor () {
    return true;
  }

  public Component getCustomEditor () {
    return new NbColorChooser (this, getStaticChooser ());
  }

  public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
    support.addPropertyChangeListener (propertyChangeListener);
  }

  public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
    support.removePropertyChangeListener (propertyChangeListener);
  }

  // helper methods .......................................................................................

  static int getIndex (Object[] names, Object name) {
    int i, k = names.length;
    for (i = 0; i < k; i++)
      if (names [i].equals (name)) return i;
    return -1;
  }

  static void initSwingConstants() {
    if (swingColorNames != null)
      return;

    UIDefaults def = UIManager.getDefaults ();
    Enumeration e = def.keys ();
    Vector names = new Vector ();
    while (e.hasMoreElements ()) {
      Object k = e.nextElement ();
      if (! (k instanceof String))
        continue;
      Object v = def.get (k);
      if (! (v instanceof Color))
        continue;
      names.addElement ((String)k);
    }
    
    swingColorNames = new String [names.size ()];
    names.copyInto (swingColorNames);
    QuickSorter.STRING.sort (swingColorNames);
    swingColors = new Color [swingColorNames.length];
    int i, k = swingColorNames.length;
    for (i = 0; i < k; i++)
      swingColors [i] = (Color) def.get (swingColorNames [i]);
  }


  // innerclasses ............................................................................................

  static class NbColorChooser extends JPanel {
    private PropertyChangeListener listener;
    JColorChooser chooser;
    
    public NbColorChooser (final ColorEditor editor, JColorChooser chooser) {
      this.chooser = chooser;
      setLayout (new BorderLayout ());
      add (chooser, BorderLayout.CENTER);
      chooser.setColor ((Color)editor.getValue ());
      chooser.addPropertyChangeListener (listener = new PropertyChangeListener () {
          public void propertyChange (PropertyChangeEvent evt) {
            if (evt.getPropertyName ().equals (JColorChooser.COLOR_PROPERTY))
              editor.setValue (NbColorChooser.this.chooser.getColor ());
          }
        }
      );
    }
    
    public void removeNotify () {
      chooser.removePropertyChangeListener (listener);
    }
    
    public Dimension getPreferredSize () {
      Dimension s = super.getPreferredSize ();
      return new Dimension (s.width + 50, s.height + 10);
    }

  };

  static class SuperColor extends Color {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6147637669184334151L;
    
    private String id = null;
    private int palette = 0;

    SuperColor (Color color) {
      super (color.getRed (), color.getGreen (), color.getBlue ());
      int i = getIndex (ColorEditor.awtColors, color);
      if (i < 0) return;
      id = awtColorNames [i];
    }

    SuperColor (String id, int palette, Color color) {
      super (color.getRed (), color.getGreen (), color.getBlue ());
      this.id = id;
      this.palette = palette;
    }

    String getID () {
      return id;
    }

    int getPalette () {
      return palette;
    }

    String getAsText () {
      if (id != null) return id;
      return "[" + getRed () + "," + getGreen () + "," + getBlue () + "]";
    }
  }

  static class NbColorChooserPanel extends ColorChooserPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2792992315444428631L;

    JList list;

    String [] names;
    Color [] colors;
    Color color;
    int palette;

    NbColorChooserPanel (final int palette, final String [] names, final Color [] colors) {
      this.names = names;
      this.colors = colors;
      this.palette = palette;

      setLayout (new BorderLayout ());
      add ("Center", new JScrollPane (list = new JList (names)));
      list.setCellRenderer (new MyListCellRenderer ());
      list.addListSelectionListener (new ListSelectionListener () {
          public void valueChanged(ListSelectionEvent e) {
            if (!list.isSelectionEmpty ()) {
              int i = list.getSelectedIndex ();
              Color oldColor = color;
              color = new SuperColor (names [i], palette, colors [i]);
              change (oldColor, color);
            }
          }
        }
      );
    }

    private void change (Color oldColor, Color color) {
      fireColorPropertyChange (oldColor, color);
    }

    public void setColor (Color newColor) {
      color = newColor;
      updateSelections ();
    }

    public Color getColor () {
      return color;
    }

    /**
    * This get called when the panel is added to the chooser.
    */
    public void installChooserPanel() {
    }

    /**
    * This get called when the panel is removed from the chooser.
    */
    public void uninstallChooserPanel() {
    }

    void updateSelections () {
      Color c = color;
      if ((c instanceof SuperColor) && (palette == ((SuperColor)c).getPalette ())) {
        int i = getIndex (names, ((SuperColor)c).getID ());
        list.setSelectedIndex (i);
      } else list.clearSelection ();
    }

    class MyListCellRenderer extends JPanel implements ListCellRenderer {

      protected Border hasFocusBorder = new LineBorder (UIManager.getColor ("List.focusCellHighlight"));
      protected Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

      boolean selected, hasFocus;
      int index;

      /** Creates a new NetbeansListCellRenderer */
      public MyListCellRenderer () {
        setOpaque (true);
        setBorder (new EmptyBorder (1, 1, 1, 1));
      }

      /**
      * @return Standart method returned preferredSize (depends on font size only).
      */
      public Dimension getPreferredSize () {
        try {
          Font font = getFont ();
          FontMetrics fontMetrics = Toolkit.getDefaultToolkit ().getFontMetrics (font);
          return new Dimension (
            fontMetrics.stringWidth (names [index]) + 10,
            fontMetrics.getHeight () + 4
          );
        } catch (NullPointerException e) {
          return new Dimension (10, 10);
        }
      }

      public void paint (Graphics g) {
        Dimension rectangle = getSize ();
        Color color = g.getColor ();

        if (selected) g.setColor (UIManager.getColor ("List.selectionBackground"));
        else g.setColor (UIManager.getColor ("List.background"));
        g.fillRect (0, 0, rectangle.width - 1, rectangle.height - 1);

        if (hasFocus) {
          g.setColor (Color.black);
          g.drawRect (0, 0, rectangle.width - 1, rectangle.height - 1);
        }

        g.setColor (Color.black);
        g.drawRect (6, rectangle.height / 2 - 5 , 10, 10);
        g.setColor (colors [index]);
        g.fillRect (7, rectangle.height / 2 - 4 , 9, 9);
        if (selected) g.setColor (UIManager.getColor ("List.selectionForeground"));
        else g.setColor (UIManager.getColor ("List.foreground"));
        FontMetrics fm = g.getFontMetrics ();
        g.drawString (names [index], 22, (rectangle.height - fm.getHeight ()) / 2 + fm.getAscent ());
        g.setColor (color);
      }

      /** This is the only method defined by ListCellRenderer.  We just
      * reconfigure the Jlabel each time we're called.
      */
      public java.awt.Component getListCellRendererComponent (
        JList list,
        Object value,            // value to display
        int index,               // cell index
        boolean isSelected,      // is the cell selected
        boolean cellHasFocus     // the list and the cell have the focus
      ) {
        this.index = index;
        selected = isSelected;
        hasFocus = cellHasFocus;
        return this;
      }
    }
  }
}

/*
 * Log
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
