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
import javax.swing.event.*;

import com.netbeans.ide.util.NbBundle;

/**
* A property editor for Font class.
*
* @version  0.10, 17 Jun 1998
*/
public class FontEditor implements PropertyEditor {

  // static .....................................................................................

  // the bundle to use
  static ResourceBundle bundle = NbBundle.getBundle (
    FontEditor.class);

  static final String[] fonts = Toolkit.getDefaultToolkit ().getFontList ();

  static final Integer[] sizes = new Integer [] {
    new Integer (3),
    new Integer (5),
    new Integer (8),
    new Integer (10),
    new Integer (12),
    new Integer (14),
    new Integer (18),
    new Integer (24),
    new Integer (36),
    new Integer (48)
  };

  static final String[] styles = new String [] {
    bundle.getString ("CTL_Plain"),
    bundle.getString ("CTL_Bold"),
    bundle.getString ("CTL_Italic"),
    bundle.getString ("CTL_BoldItalic")
  };

  // variables ..................................................................................

  private Font font;
  private String fontName;
  private PropertyChangeSupport support;


  // init .......................................................................................

  public FontEditor() {
    support = new PropertyChangeSupport (this);
  }


  // main methods .......................................................................................

  public Object getValue () {
    return font;
  }

  public void setValue (Object object) {
    font = (Font) object;

    fontName = font.getName () + " " + font.getSize () + " " + getStyleName (font.getStyle ());

    support.firePropertyChange ("", null, null);
  }

  public String getAsText () {
    return null;
  }

  public void setAsText (String string) {
    return;
  }

  public String getJavaInitializationString () {
    return "new java.awt.Font (\"" + font.getName () + "\", " + font.getStyle () +
           ", " + font.getSize () + ")";
  }

  public String[] getTags () {
    return null;
  }

  public boolean isPaintable () {
    return true;
  }

  public void paintValue (Graphics g, Rectangle rectangle) {
    Color color = g.getColor ();
    Font f = g.getFont ();
    g.setColor (Color.black);
    FontMetrics fm = g.getFontMetrics (font);
    g.setFont (font);
    g.drawString (fontName, rectangle.x + 4, rectangle.y +
      (rectangle.height - fm.getHeight ()) / 2 + fm.getAscent ());
    g.setColor (color);
    g.setFont (f);
  }

  public boolean supportsCustomEditor () {
    return true;
  }

  public Component getCustomEditor () {
    return new FontPanel ();
  }

  public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
    support.addPropertyChangeListener (propertyChangeListener);
  }

  public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
    support.removePropertyChangeListener (propertyChangeListener);
  }


  // helper methods .......................................................................................

  String getStyleName (int i) {
    if ((i & Font.BOLD) > 0)
      if ((i & Font.ITALIC) > 0) return bundle.getString ("CTL_BoldItalic");
      else return bundle.getString ("CTL_Bold");
    else
    if ((i & Font.ITALIC) > 0) return bundle.getString ("CTL_Italic");
    else return bundle.getString ("CTL_Plain");
  }

  // innerclasses ............................................................................................

  class FontPanel extends JPanel {

    JTextField tfFont, tfStyle, tfSize;
    JList lFont, lStyle, lSize;

    FontPanel () {
      setLayout (new BorderLayout ());

      GridBagLayout la = new GridBagLayout ();
      GridBagConstraints c = new GridBagConstraints ();
      setLayout (la);

      c.gridwidth = 1;
      c.weightx = 1.0;
      c.insets = new Insets (3, 3, 3, 3);
      c.anchor = GridBagConstraints.WEST;
      JLabel l = new JLabel (bundle.getString ("CTL_Font"));
      la.setConstraints (l, c);
      add (l);

      l = new JLabel (bundle.getString ("CTL_FontStyle"));
      la.setConstraints (l, c);
      add (l);

      c.gridwidth = GridBagConstraints.REMAINDER;
      l = new JLabel (bundle.getString ("CTL_Size"));
      la.setConstraints (l, c);
      add (l);

      c.gridwidth = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      tfFont = new JTextField (FontEditor.this.font.getName ());
      tfFont.setEnabled (false);
      la.setConstraints (tfFont, c);
      add (tfFont);

      tfStyle = new JTextField (getStyleName (FontEditor.this.font.getStyle ()));
      tfStyle.setEnabled (false);
      la.setConstraints (tfStyle, c);
      add (tfStyle);

      c.gridwidth = GridBagConstraints.REMAINDER;
      tfSize = new JTextField ("" + FontEditor.this.font.getSize ());
      tfSize.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e) {
          setValue ();
        }
      });
      la.setConstraints (tfSize, c);
      add (tfSize);

      c.gridwidth = 1;
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1.0;
      c.weighty = 1.0;
      lFont = new JList (fonts);
      lFont.setVisibleRowCount (5);
      lFont.addListSelectionListener (new ListSelectionListener () {
          public void valueChanged (ListSelectionEvent e) {
            if (!lFont.isSelectionEmpty ()) {
              int i = lFont.getSelectedIndex ();
              tfFont.setText (fonts [i]);
              setValue ();
            }
          }
        }
      );
      JScrollPane sp = new JScrollPane (lFont);
      sp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      la.setConstraints (sp, c);
      add (sp);

      lStyle = new JList (styles);
      lStyle.setVisibleRowCount (5);
      lStyle.addListSelectionListener (new ListSelectionListener () {
          public void valueChanged (ListSelectionEvent e) {
            if (!lStyle.isSelectionEmpty ()) {
              int i = lStyle.getSelectedIndex ();
              tfStyle.setText (styles [i]);
              setValue ();
            }
          }
        }
      );
      sp = new JScrollPane (lStyle);
      sp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      la.setConstraints (sp, c);
      add (sp);

      c.gridwidth = GridBagConstraints.REMAINDER;
      lSize = new JList (sizes);
      lSize.setVisibleRowCount (5);
      lSize.addListSelectionListener (new ListSelectionListener () {
          public void valueChanged (ListSelectionEvent e) {
            if (!lSize.isSelectionEmpty ()) {
              int i = lSize.getSelectedIndex ();
              tfSize.setText ("" + sizes [i]);
              setValue ();
            }
          }
        }
      );
      sp = new JScrollPane (lSize);
      sp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      la.setConstraints (sp, c);
      add (sp);

      c.gridwidth = GridBagConstraints.REMAINDER;
      c.weighty = 2.0;
      JPanel p = new JPanel (new BorderLayout ());
      p.setBorder (new TitledBorder (bundle.getString ("CTL_Preview")));

      JPanel pp = new JPanel () {
        public Dimension getPreferredSize () {
          return new Dimension (150, 60);
        }

        public void paint (Graphics g) {
//          super.paint (g);
          FontEditor.this.paintValue (g, new Rectangle (0, 0, getSize ().width - 1, getSize ().height - 1));
        }
      };
      p.add ("Center", pp);
      la.setConstraints (p, c);
      add (p);
    }

     public Dimension getPreferredSize () {
       return new Dimension (400, 250);
     }

    void setValue () {
      int size = 12;
      try {
        size = Integer.parseInt (tfSize.getText ());
      } catch (NumberFormatException e) {
        return;
      }
      int i = lStyle.getSelectedIndex (), ii = Font.PLAIN;
      switch (i) {
        case 0: ii = Font.PLAIN;break;
        case 1: ii = Font.BOLD;break;
        case 2: ii = Font.ITALIC;break;
        case 3: ii = Font.BOLD | Font.ITALIC;break;
      }
      FontEditor.this.setValue (new Font (tfFont.getText (), ii, size));
      invalidate();
      getParent ().validate();
      repaint();
    }
  }
}

/*
 * Log
 *  2    Gandalf   1.1         3/4/99   Jan Jancura     bundle moved
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */




