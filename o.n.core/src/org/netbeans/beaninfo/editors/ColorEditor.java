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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.*;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** A property editor for Color class.
 * (Final only for performance, can be unfinaled if desired).
 *
 * @author   Jan Jancura, Ian Formanek
 */
public final class ColorEditor implements PropertyEditor, XMLPropertyEditor {
    // static .....................................................................................
    /** Color chooser instance. */
    private static JColorChooser staticChooser;

    /** AWT Palette mode. */
    public static final int AWT_PALETTE = 1;
    /** System Palette mode. */
    public static final int SYSTEM_PALETTE = 2;
    /** Swing Palette mode. */
    public static final int SWING_PALETTE = 3;

    /** Localized names of AWT colors. */
    private static String awtColorNames[];

    /** AWT colors used in AWT Palette. */
    private static final Color awtColors[] = {
        Color.white, Color.lightGray, Color.gray, Color.darkGray,
        Color.black, Color.red, Color.pink, Color.orange, Color.yellow,
        Color.green, Color.magenta, Color.cyan, Color.blue };

    /** Localized names of system colors. */
    private static String systemColorNames[];

    /** Names of system colors. <em>Note:</em> not localizable,
     * those names corresponds to programatical names. */
    private static final String systemGenerate[] = {
        "activeCaption", "activeCaptionBorder", // NOI18N
        "activeCaptionText", "control", "controlDkShadow", // NOI18N
        "controlHighlight", "controlLtHighlight", // NOI18N
        "controlShadow", "controlText", "desktop", // NOI18N
        "inactiveCaption", "inactiveCaptionBorder", // NOI18N
        "inactiveCaptionText", "info", "infoText", "menu", // NOI18N
        "menuText", "scrollbar", "text", "textHighlight", // NOI18N
        "textHighlightText", "textInactiveText", "textText", // NOI18N
        "window", "windowBorder", "windowText"}; // NOI18N

    /** System colors used in System Palette. */
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
     * They are also cleared when l&f changes. */
    private static String swingColorNames[];
    
    /** Swing colors used in Swing Palette. */
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
    /** Used palette type. */
    public int palette = AWT_PALETTE;
    /** Selected color. */
    private SuperColor color;
    /** Property change support. Helper field. */
    private PropertyChangeSupport support;


    /** Gets <code>staticChooser</code> instance. */
    public static JColorChooser getStaticChooser () {
        if (staticChooser == null) {
            staticChooser = new JColorChooser () {
                                public void setColor (Color c) {
                                    if (c == null) return;
                                    super.setColor (c);
                                }
                            };
            staticChooser.addChooserPanel (
                new NbColorChooserPanel (AWT_PALETTE, getAWTColorNames(), awtColors,
                                         getString ("CTL_AWTPalette"))
            );
            initSwingConstants();
            staticChooser.addChooserPanel (
                new NbColorChooserPanel (SWING_PALETTE, swingColorNames, swingColors,
                                         getString ("CTL_SwingPalette"))
            );
            staticChooser.addChooserPanel (
                new NbColorChooserPanel (SYSTEM_PALETTE, getSystemColorNames(), systemColors,
                                         getString ("CTL_SystemPalette"))
            );
        }
        return staticChooser;
    }

    // init .......................................................................................

    /** Creates color editor. */
    public ColorEditor() {
        support = new PropertyChangeSupport (this);
    }


    // main methods .......................................................................................

    /** Gets value. Implements <code>PropertyEditor</code> interface.
     * @return <code>Color</code> value or <code>null</code> */
    public Object getValue () {
        if (color == null) return null;
        return new Color(color.getRGB());
    }

    /** Sets value. Implements <code>PropertyEditor</code> interface.
     * @param object object to set, accepts <code>Color</code> 
     * or <code>SuperColor<code> types */
    public void setValue (Object object) {
        if(object != null) {
            if (object instanceof SuperColor) {
                color = (SuperColor) object;
            } else if (object instanceof Color) {
                color = new SuperColor((Color) object);
            }
        } else {
            color = null;
        }
        
        support.firePropertyChange ("", null, null); // NOI18N
    }

    /** Gets value as text. Implements <code>PropertyEditor</code> interface. */
    public String getAsText () {
        if (color == null) return "null"; // NOI18N
        return color.getAsText ();
    }

    /** Sets value ad text. Implements <code>PropertyEditor</code> interface. */
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
                i = getIndex (getAWTColorNames(), string);
                if (i < 0) break;
                setValue (new SuperColor (string, AWT_PALETTE, awtColors [i]));
                return;
            case SYSTEM_PALETTE:
                i = getIndex (getSystemColorNames(), string);
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

    /** Gets java inititalization string. Implements <code>PropertyEditor</code> interface. */
    public String getJavaInitializationString () {
        if (color == null) return "null"; // NOI18N
        if (color.getID () == null)
            return "new java.awt.Color (" + color.getRed () + ", " + color.getGreen () + // NOI18N
                   ", " + color.getBlue () + ")"; // NOI18N

        switch (color.getPalette ()) {
        default:
        case AWT_PALETTE:
            return "java.awt.Color." + color.getID (); // NOI18N
        case SYSTEM_PALETTE:
            return "java.awt.SystemColor." + systemGenerate [getIndex (getSystemColorNames(), color.getID ())]; // NOI18N
        case SWING_PALETTE:
            initSwingConstants();
            int i = getIndex (swingColorNames, color.getID ());
            if (i < 0) return "new java.awt.Color (" + color.getRed () + ", " + color.getGreen () + // NOI18N
                                  ", " + color.getBlue () + ")"; // NOI18N
            return "(java.awt.Color) javax.swing.UIManager.getDefaults ().get (\"" + // NOI18N
                   color.getID () + "\")"; // NOI18N
        }
    }

    /** Get tags possible for choosing value. Implements <code>PropertyEditor</code> interface. */
    public String[] getTags () {
        switch (palette) {
            case AWT_PALETTE:
                return getAWTColorNames();
            case SYSTEM_PALETTE:
                return getSystemColorNames();
            case SWING_PALETTE:
                initSwingConstants();
                return swingColorNames;
            default: 
                return getAWTColorNames();
        }
    }

    /** Insicates whether this editor is paintable. Implements <code>PropertyEditor</code> interface.
     * @return <code>true</code> */
    public boolean isPaintable () {
        return true;
    }

    /** Paints the current value. Implements <code>ProepertyEditor</code> interface. */
    public void paintValue(Graphics g, Rectangle rectangle) {
        int px;

        if (this.color != null) {
            Color color = g.getColor();
            g.drawRect(rectangle.x, rectangle.y + rectangle.height / 2 - 5 , 10, 10);
            g.setColor(this.color);
            g.fillRect(rectangle.x + 1, rectangle.y + rectangle.height / 2 - 4 , 9, 9);
            g.setColor(color);
            px = 18;
        }
        else px = 0;

        FontMetrics fm = g.getFontMetrics();
        g.drawString(getAsText(), rectangle.x + px, rectangle.y +
                      (rectangle.height - fm.getHeight()) / 2 + fm.getAscent());
    }

    /** Indicates whether this editor supports custom editing. 
     * Implements <code>PropertyEditor</code> interface.
     * @return <code>true</code> */
    public boolean supportsCustomEditor () {
        return true;
    }

    /** Gets custom editor. Implements <code>PropertyEditor</code> interface.
     * *return <code>NbColorChooser</code> instance */
    public Component getCustomEditor () {
        return new NbColorChooser (this, getStaticChooser ());
    }

    /** Adds property change listener. */
    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    /** Removes property change listner. */
    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

    // helper methods .......................................................................................
    /** Gets array of localized AWT color names. */
    private static synchronized String[] getAWTColorNames() {
        if(awtColorNames == null) {
            awtColorNames = new String[] {
                getString("LAB_White"),
                getString("LAB_LightGray"),
                getString("LAB_Gray"),
                getString("LAB_DarkGray"),
                getString("LAB_Black"),
                getString("LAB_Red"),
                getString("LAB_Pink"),
                getString("LAB_Orange"),
                getString("LAB_Yellow"),
                getString("LAB_Green"),
                getString("LAB_Magenta"),
                getString("LAB_Cyan"),
                getString("LAB_Blue")
            };
        }
        
        return awtColorNames;
    }

    /** Gets array of localize system color names. */
    private static synchronized String[] getSystemColorNames() {
        if(systemColorNames == null) {
            systemColorNames = new String[] {
                getString("LAB_ActiveCaption"),
                getString("LAB_ActiveCaptionBorder"),
                getString("LAB_ActiveCaptionText"),
                getString("LAB_Control"),
                getString("LAB_ControlDkShadow"),
                getString("LAB_ControlHighlight"),
                getString("LAB_ControlLtHighlight"),
                getString("LAB_ControlShadow"),
                getString("LAB_ControlText"),
                getString("LAB_Desktop"),
                getString("LAB_InactiveCaption"),
                getString("LAB_InactiveCaptionBorder"),
                getString("LAB_InactiveCaptionText"),
                getString("LAB_Info"),
                getString("LAB_InfoText"),
                getString("LAB_Menu"),
                getString("LAB_MenuText"),
                getString("LAB_Scrollbar"),
                getString("LAB_Text"),
                getString("LAB_TextHighlight"),
                getString("LAB_TextHighlightText"),
                getString("LAB_TextInactiveText"),
                getString("LAB_TextText"),
                getString("LAB_Window"),
                getString("LAB_WindowBorder"),
                getString("LAB_WindowText")
            };
        }
        
        return systemColorNames;
    }

    /** Gets localized string. 
     * @param key key from bundle from the package like this source */
    private static String getString(String key) {
        return NbBundle.getBundle(ColorEditor.class).getString(key);
    }

    /** Gets index of name from array. */
    private static int getIndex (Object[] names, Object name) {
        int i, k = names.length;
        for (i = 0; i < k; i++)
            if (names [i].equals (name)) return i;
        return -1;
    }

    /** Initialized fields used in Swing Palette. */
    private static void initSwingConstants() {
        if (swingColorNames != null)
            return;

        UIDefaults def = UIManager.getDefaults ();
        Enumeration e = def.keys ();
        
        List names = new ArrayList(def.size());
        
        while (e.hasMoreElements ()) {
            Object k = e.nextElement ();
            if (! (k instanceof String))
                continue;
            Object v = def.get (k);
            if (! (v instanceof Color))
                continue;
            names.add((String)k);
        }
        
        Collections.sort(names);

        swingColorNames = new String [names.size ()];
        
        names.toArray(swingColorNames);
        
        //    QuickSorter.STRING.sort (swingColorNames);
        swingColors = new Color [swingColorNames.length];
        int i, k = swingColorNames.length;
        for (i = 0; i < k; i++)
            swingColors [i] = (Color) def.get (swingColorNames [i]);
    }


    // innerclasses ............................................................................................
    /** Panel used as custom property editor. */
    private static class NbColorChooser extends JPanel implements ChangeListener {
        /** Color property editor */
        private final ColorEditor editor;
        /** Color chooser instance */
        private final JColorChooser chooser;
        /** Reference to model which holds the color selected in the color chooser */
        private final ColorSelectionModel selectionModel;

        static final long serialVersionUID =-6230228701104365037L;
        
        
        /** Creates new <code>NbColorChooser</code>. */
        public NbColorChooser (final ColorEditor editor,
                               final JColorChooser chooser) {
            this.editor = editor;
            this.chooser = chooser;
            selectionModel = chooser.getSelectionModel();
            setLayout (new BorderLayout ());
            add (chooser, BorderLayout.CENTER);
            chooser.setColor ((Color)editor.getValue ());
            selectionModel.addChangeListener (this);
            
            HelpCtx.setHelpIDString (this, NbColorChooser.class.getName ());
        }

        /** Overrides superclass method. Adds removing of change listener. */
        public void removeNotify () {
            super.removeNotify();
            selectionModel.removeChangeListener (this);
        }

        /** Overrides superclass method. Adds 50 pixels to each side. */
        public Dimension getPreferredSize () {
            Dimension s = super.getPreferredSize ();
            return new Dimension (s.width + 50, s.height + 10);
        }

        /** Implementats <code>ChangeListener</code> interface */
        public void stateChanged (ChangeEvent evt) {
            editor.setValue(selectionModel.getSelectedColor());
        }

    } // End of class NbColorChooser.


    /** Color belonging to palette and keeping its ID. */
    private static class SuperColor extends Color {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 6147637669184334151L;

        /** ID of this color. */
        private String id = null;
        /** Palette where it belongs. */
        private int palette = 0;

        SuperColor (Color color) {
            super (color.getRed (), color.getGreen (), color.getBlue ());
            int i = getIndex (ColorEditor.awtColors, color);
            if (i < 0) return;
            id = getAWTColorNames()[i];
        }

        SuperColor (String id, int palette, Color color) {
            super (color.getRed (), color.getGreen (), color.getBlue ());
            this.id = id;
            this.palette = palette;
        }

        /** Gets ID of this color. */
        private String getID () {
            return id;
        }

        /** Gets palette of this color. */
        private int getPalette () {
            return palette;
        }

        /** Gets as text this color value. */
        private String getAsText () {
            if (id != null) return id;
            return "[" + getRed () + "," + getGreen () + "," + getBlue () + "]"; // NOI18N
        }
    } // End of class SuperColor.

    /** Color chooser panel which can be added into JColorChooser */
    private static final class NbColorChooserPanel extends AbstractColorChooserPanel
    implements ListSelectionListener {
        /** Generated Serialized Version UID */
        static final long serialVersionUID = -2792992315444428631L;
        /** List holding palette colors */
        private JList list;

        /** Arraay of names of colors. */
        private String [] names;
        /** Arraay of colors. */
        private Color [] colors;
        /** Selected color. */
        private Color color;
        /** Palette type. */
        private int palette;
        
        /** Name for display of this chooser panel. */
        private String displayName;
        

        /** Constructs our chooser panel with specified
        * palette, names and colors to be shown in the list */
        NbColorChooserPanel (final int palette, final String[] names,
                             final Color[] colors, final String displayName) {
            this.names = names;
            this.colors = colors;
            this.palette = palette;
            this.displayName = displayName;
        }

        /** Builds - creates a chooser */
        protected void buildChooser () {
            setLayout (new BorderLayout ());
            add (BorderLayout.CENTER,
                 new JScrollPane (list = new JList (names)));
            list.setCellRenderer (new MyListCellRenderer ());
            list.addListSelectionListener (this);
        }

        /** Get called when state of selected color changes */
        public void updateChooser () {
            Color c = color;
            if ((c instanceof SuperColor) && (palette == ((SuperColor)c).getPalette ())) {
                int i = getIndex (names, ((SuperColor)c).getID ());
                list.setSelectedIndex (i);
            } else list.clearSelection ();
        }

        /** @return display name of the chooser */
        public String getDisplayName() {
            return displayName;
        }

        /** No icon */
        public Icon getSmallDisplayIcon() {
            return null;
        }

        /** No icon */
        public Icon getLargeDisplayIcon() {
            return null;
        }

        /** ListSelectionListener interface implementation */
        public void valueChanged(ListSelectionEvent e) {
            if (!list.isSelectionEmpty ()) {
                int i = list.getSelectedIndex ();
                getColorSelectionModel().setSelectedColor(
                    new SuperColor (names [i], palette, colors [i]));
            }
        }

        /** Setter for <code>color</code> property. */
        public void setColor (final Color newColor) {
            getColorSelectionModel().setSelectedColor(newColor);
        }

        /** Getter for <code>color</code> property. */
        public Color getColor () {
            return getColorFromModel();
        }

        
        /** Renderer for cell of the list showing palette colors */
        private final class MyListCellRenderer extends JPanel implements ListCellRenderer {

            /** Selected flag. */
            private boolean selected;
            /** Focus flag. */
            private boolean hasFocus;
            /** Selected index. */
            private int index;

            /** Generated serial version UID. */
            static final long serialVersionUID =-8877709520578055594L;
            
            
            /** Creates a new MyListCellRenderer */
            public MyListCellRenderer () {
                this.setOpaque (true);
                this.setBorder (new EmptyBorder (1, 1, 1, 1));
            }

            /** Overrides default preferredSize impl.
             * @return Standard method returned preferredSize
             * (depends on font size only).
             */
            public Dimension getPreferredSize () {
                try {
                    FontMetrics fontMetrics = this.getFontMetrics(this.getFont());
                    return new Dimension (
                               fontMetrics.stringWidth (names [index]) + 30,
                               fontMetrics.getHeight () + 4
                           );
                } catch (NullPointerException e) {
                    return new Dimension (10, 10);
                }
            }

            /** Paints this component. Overrides superclass method. */
            public void paint (Graphics g) {
                Dimension rectangle = this.getSize ();
                Color color = g.getColor ();

                if(selected) {
                    g.setColor (UIManager.getColor ("List.selectionBackground")); // NOI18N
                } else {
                    g.setColor (UIManager.getColor ("List.background")); // NOI18N
                }
                
                g.fillRect (0, 0, rectangle.width - 1, rectangle.height - 1);

                if (hasFocus) {
                    g.setColor (Color.black);
                    g.drawRect (0, 0, rectangle.width - 1, rectangle.height - 1);
                }

                g.setColor (Color.black);
                g.drawRect (6, rectangle.height / 2 - 5 , 10, 10);
                g.setColor (colors [index]);
                g.fillRect (7, rectangle.height / 2 - 4 , 9, 9);
                
                if(selected) {
                    g.setColor (UIManager.getColor ("List.selectionForeground")); // NOI18N
                } else {
                    g.setColor (UIManager.getColor ("List.foreground")); // NOI18N
                }
                
                FontMetrics fm = g.getFontMetrics ();
                g.drawString (names [index], 22, (rectangle.height - fm.getHeight ()) / 2 + fm.getAscent ());
                g.setColor (color);
            }

            /** This is the only method defined by ListCellRenderer.  We just
             * reconfigure the Jlabel each time we're called.
             */
            public Component getListCellRendererComponent (
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
        } // End of class MyListCellRenderer.
    } // End of class NbColorChooserPanel.

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    /** Name of color element. */
    public static final String XML_COLOR = "Color"; // NOI18N

    /** Name of type attribute. */
    public static final String ATTR_TYPE = "type"; // NOI18N
    /** Name of red attribute. */
    public static final String ATTR_RED = "red"; // NOI18N
    /** Name of green attribute. */
    public static final String ATTR_GREEN = "green"; // NOI18N
    /** Name of blue attribute. */
    public static final String ATTR_BLUE = "blue"; // NOI18N
    /** Name of id attribute. */
    public static final String ATTR_ID = "id"; // NOI18N
    /** Name of palette attribute. */
    public static final String ATTR_PALETTE = "palette"; // NOI18N

    /** Value of palette. */
    public static final String VALUE_PALETTE = "palette"; // NOI18N
    /** Value of rgb. */
    public static final String VALUE_RGB = "rgb"; // NOI18N

    
    /** Called to load property value from specified XML subtree. If succesfully loaded,
     * Implements <code>XMLPropertyEditor</code> interface.
     * the value should be available via the getValue method.
     * An IOException should be thrown when the value cannot be restored from the specified XML element
     * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
     * @exception IOException thrown when the value cannot be restored from the specified XML element
     */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_COLOR.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            String type = attributes.getNamedItem (ATTR_TYPE).getNodeValue ();
            String red = attributes.getNamedItem (ATTR_RED).getNodeValue ();
            String green = attributes.getNamedItem (ATTR_GREEN).getNodeValue ();
            String blue = attributes.getNamedItem (ATTR_BLUE).getNodeValue ();
            if (VALUE_PALETTE.equals (type)) {
                String id = attributes.getNamedItem (ATTR_ID).getNodeValue ();
                String palette = attributes.getNamedItem (ATTR_PALETTE).getNodeValue ();
                setValue (new SuperColor (id, Integer.parseInt (palette), new Color (Integer.parseInt (red, 16), Integer.parseInt (green, 16), Integer.parseInt (blue, 16))));
            } else {
                setValue (new SuperColor (new Color (Integer.parseInt (red, 16), Integer.parseInt (green, 16), Integer.parseInt (blue, 16))));
            }
        } catch (NullPointerException e) {
            throw new java.io.IOException ();
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
     * Implemtns <code>XMLPropertyEdtitor</code> interface.
     * setValue method prior to calling this method.
     * @param doc The XML document to store the XML in - should be used for creating nodes only
     * @return the XML DOM element representing a subtree of XML from which the value should be loaded
     */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        if (color == null) {
            IllegalArgumentException iae = new IllegalArgumentException();
            ErrorManager manager = TopManager.getDefault().getErrorManager();
            manager.annotate(iae, ErrorManager.EXCEPTION, null, 
                getString("MSG_ColorIsNotInitialized"), null, null); // NOI18N
            manager.notify(iae);
            return null;
        }
        
        org.w3c.dom.Element el = doc.createElement (XML_COLOR);
        el.setAttribute (ATTR_TYPE, (color.getID () == null) ? VALUE_RGB : VALUE_PALETTE);
        el.setAttribute (ATTR_RED, Integer.toHexString (color.getRed ()));
        el.setAttribute (ATTR_GREEN, Integer.toHexString (color.getGreen ()));
        el.setAttribute (ATTR_BLUE, Integer.toHexString (color.getBlue ()));
        if (color.getID () != null) {
            el.setAttribute (ATTR_ID, color.getID ());
            el.setAttribute (ATTR_PALETTE, Integer.toString (color.getPalette ()));
        }
        return el;
    }

}
