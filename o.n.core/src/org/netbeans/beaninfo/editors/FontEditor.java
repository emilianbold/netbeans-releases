/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

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

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
* A property editor for Font class.
*
* @author Ian Formanek
*/
public class FontEditor implements PropertyEditor, XMLPropertyEditor {

    // static .....................................................................................

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle (
                                       FontEditor.class);

    static final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment ().getAvailableFontFamilyNames();

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
        if (!(object instanceof Font )) {
            font = new Font( fonts[0], Font.PLAIN, 10 );
        } else font = (Font) object;

        fontName = font.getName () + " " + font.getSize () + " " + getStyleName (font.getStyle ()); // NOI18N

        support.firePropertyChange ("", null, null); // NOI18N
    }

    public String getAsText () {
        return null;
    }

    public void setAsText (String string) {
        return;
    }

    public String getJavaInitializationString () {
        return "new java.awt.Font (\"" + font.getName () + "\", " + font.getStyle () + // NOI18N
               ", " + font.getSize () + ")"; // NOI18N
    }

    public String[] getTags () {
        return null;
    }

    public boolean isPaintable () {
        return true;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
        Font f = g.getFont ();
        FontMetrics fm = g.getFontMetrics (font);
        g.setFont (font);
        g.drawString (fontName,
                      rectangle.x + (rectangle.width - fm.stringWidth(fontName)) / 2,
                      rectangle.y + (rectangle.height - fm.getHeight ()) / 2 + fm.getAscent ());
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

        static final long serialVersionUID =8377025140456676594L;
        
        FontPanel () {
            setLayout (new BorderLayout ());

            GridBagLayout la = new GridBagLayout ();
            GridBagConstraints c = new GridBagConstraints ();
            setLayout (la);

            c.gridwidth = 1;
            c.weightx = 1.0;
            c.insets = new Insets (0, 0, 0, 0);
            c.anchor = GridBagConstraints.WEST;
            JLabel l = new JLabel (bundle.getString ("CTL_Font"));                    //NoI18N
            l.setDisplayedMnemonic(bundle.getString ("CTL_Font_mnemonic").charAt(0)); //NoI18N
            l.setLabelFor(lFont);
            la.setConstraints (l, c);
            add (l);
            
            c.insets = new Insets (0, 5, 0, 0);  
            l = new JLabel (bundle.getString ("CTL_FontStyle"));                           //NoI18N  
            l.setDisplayedMnemonic(bundle.getString ("CTL_FontStyle_mnemonic").charAt(0)); //NoI18N
            l.setLabelFor(lStyle);
            la.setConstraints (l, c);
            add (l);

            c.insets = new Insets (0, 5, 0, 0);
            c.gridwidth = GridBagConstraints.REMAINDER;
            l = new JLabel (bundle.getString ("CTL_Size"));                           //NoI18N
            l.setDisplayedMnemonic(bundle.getString ("CTL_Size_mnemonic").charAt(0)); //NoI18N
            l.setLabelFor(tfSize);
            la.setConstraints (l, c);
            add (l);

            c.insets = new Insets (5, 0, 0, 0);
            c.gridwidth = 1;            
            c.fill = GridBagConstraints.HORIZONTAL;
            tfFont = new JTextField (FontEditor.this.font.getName ());
            tfFont.setEnabled (false);
            la.setConstraints (tfFont, c);
            add (tfFont);

            c.insets = new Insets (5, 5, 0, 0);
            tfStyle = new JTextField (getStyleName (FontEditor.this.font.getStyle ()));
            tfStyle.setEnabled (false);
            la.setConstraints (tfStyle, c);
            add (tfStyle);

            c.insets = new Insets (5, 5, 0, 0);
            c.gridwidth = GridBagConstraints.REMAINDER;
            tfSize = new JTextField ("" + FontEditor.this.font.getSize ()); // NOI18N
            tfSize.addActionListener (new ActionListener () {
                                          public void actionPerformed (ActionEvent e) {
                                              setValue ();
                                          }
                                      });
            tfSize.addFocusListener (new java.awt.event.FocusAdapter () {
                                         public void focusLost (java.awt.event.FocusEvent evt) {
                                             setValue ();
                                         }
                                     });
            la.setConstraints (tfSize, c);
            add (tfSize);

            c.gridwidth = 1;
            c.insets = new Insets (5, 0, 0, 0);
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
            c.insets = new Insets (5, 5, 0, 0);
            la.setConstraints (sp, c);
            add (sp);

            c.gridwidth = GridBagConstraints.REMAINDER;
            lSize = new JList (sizes);
            lSize.setVisibleRowCount (5);
            lSize.addListSelectionListener (new ListSelectionListener () {
                                                public void valueChanged (ListSelectionEvent e) {
                                                    if (!lSize.isSelectionEmpty ()) {
                                                        int i = lSize.getSelectedIndex ();
                                                        tfSize.setText ("" + sizes [i]); // NOI18N
                                                        setValue ();
                                                    }
                                                }
                                            }
                                           );
            sp = new JScrollPane (lSize);
            sp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            c.insets = new Insets (5, 5, 0, 0);
            la.setConstraints (sp, c);
            add (sp);

            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weighty = 2.0;
            JPanel p = new JPanel (new BorderLayout());
            p.setBorder (new TitledBorder (" " + bundle.getString ("CTL_Preview") + " "));

            JPanel pp = new JPanel () {
                            public Dimension getPreferredSize () {
                                return new Dimension (150, 60);
                            }

                            public void paint (Graphics g) {
                                //          super.paint (g);
                                FontEditor.this.paintValue (g, new Rectangle (0, 0, this.getSize().width - 1, this.getSize().height - 1));
                            }
                        };
            p.add ("Center", pp); // NOI18N
            c.insets = new Insets (12, 0, 0, 0);
            la.setConstraints (p, c);
            add (p);

            HelpCtx.setHelpIDString (this, FontPanel.class.getName ());
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

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String XML_FONT = "Font"; // NOI18N

    public static final String ATTR_NAME = "name"; // NOI18N
    public static final String ATTR_STYLE = "style"; // NOI18N
    public static final String ATTR_SIZE = "size"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_FONT.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            String name = attributes.getNamedItem (ATTR_NAME).getNodeValue ();
            String style = attributes.getNamedItem (ATTR_STYLE).getNodeValue (); // [PENDING - style names]
            String size = attributes.getNamedItem (ATTR_SIZE).getNodeValue ();
            setValue (new Font (name, Integer.parseInt (style), Integer.parseInt (size)));
        } catch (NullPointerException e) {
            throw new java.io.IOException ();
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement (XML_FONT);
        el.setAttribute (ATTR_NAME, font.getName ());
        el.setAttribute (ATTR_STYLE, Integer.toString (font.getStyle ()));
        el.setAttribute (ATTR_SIZE, Integer.toString (font.getSize ()));
        return el;
    }

}
