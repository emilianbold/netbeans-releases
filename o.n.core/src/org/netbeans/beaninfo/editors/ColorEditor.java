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
import java.util.Collections;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** A property editor for Color class.
* (Final only for performance, can be unfinaled if desired)
*
* @author   Jan Jancura, Ian Formanek
*/
public final class ColorEditor implements PropertyEditor, org.openide.explorer.propertysheet.editors.XMLPropertyEditor {
    // static .....................................................................................

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle (ColorEditor.class);

    private static JColorChooser staticChooser;

    public static final int AWT_PALETTE = 1;
    public static final int SYSTEM_PALETTE = 2;
    public static final int SWING_PALETTE = 3;

    private static final String awtColorNames[] = {
        "white", "lightGray", "gray", "darkGray", "black", // NOI18N
        "red", "pink", "orange", "yellow", "green", "magenta", // NOI18N
        "cyan", "blue" }; // NOI18N

    private static final Color awtColors[] = {
        Color.white, Color.lightGray, Color.gray, Color.darkGray,
        Color.black, Color.red, Color.pink, Color.orange, Color.yellow,
        Color.green, Color.magenta, Color.cyan, Color.blue };

    private static final String systemColorNames[] = {
        "Active Caption", "Active Caption Border", // NOI18N
        "Active Caption Text", "Control", "Control Dk Shadow", // NOI18N
        "Control Highlight", "Control Lt Highlight", // NOI18N
        "Control Shadow", "Control Text", "Desktop", // NOI18N
        "Inactive Caption", "Inactive Caption Border", // NOI18N
        "Inactive Caption Text", "Info", "Info Text", "Menu", // NOI18N
        "Menu Text", "Scrollbar", "Text", "Text Highlight", // NOI18N
        "Text Highlight Text", "Text Inactive Text", "Text Text", // NOI18N
        "Window", "Window Border", "Window Text"}; // NOI18N

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
                new NbColorChooserPanel (AWT_PALETTE, awtColorNames, awtColors,
                                         bundle.getString ("CTL_AWTPalette"))
            );
            initSwingConstants();
            staticChooser.addChooserPanel (
                new NbColorChooserPanel (SWING_PALETTE, swingColorNames, swingColors,
                                         bundle.getString ("CTL_SwingPalette"))
            );
            staticChooser.addChooserPanel (
                new NbColorChooserPanel (SYSTEM_PALETTE, systemColorNames, systemColors,
                                         bundle.getString ("CTL_SystemPalette"))
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
            if (!(object instanceof Color)) throw new IllegalArgumentException (object.toString ());
            if (object instanceof SuperColor) color = (SuperColor) object;
            else color = new SuperColor ((Color) object);
        }
        support.firePropertyChange ("", null, null); // NOI18N
    }

    public String getAsText () {
        if (color == null) return "null"; // NOI18N
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
        if (color == null) return "null"; // NOI18N
        if (color.getID () == null)
            return "new java.awt.Color (" + color.getRed () + ", " + color.getGreen () + // NOI18N
                   ", " + color.getBlue () + ")"; // NOI18N

        switch (color.getPalette ()) {
        default:
        case AWT_PALETTE:
            return "java.awt.Color." + color.getID (); // NOI18N
        case SYSTEM_PALETTE:
            return "java.awt.SystemColor." + systemGenerate [getIndex (systemColorNames, color.getID ())]; // NOI18N
        case SWING_PALETTE:
            initSwingConstants();
            int i = getIndex (swingColorNames, color.getID ());
            if (i < 0) return "new java.awt.Color (" + color.getRed () + ", " + color.getGreen () + // NOI18N
                                  ", " + color.getBlue () + ")"; // NOI18N
            return "(java.awt.Color) javax.swing.UIManager.getDefaults ().get (\"" + // NOI18N
                   color.getID () + "\")"; // NOI18N
        }
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
        }
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
        Collections.sort (names);

        swingColorNames = new String [names.size ()];
        names.copyInto (swingColorNames);
        //    QuickSorter.STRING.sort (swingColorNames);
        swingColors = new Color [swingColorNames.length];
        int i, k = swingColorNames.length;
        for (i = 0; i < k; i++)
            swingColors [i] = (Color) def.get (swingColorNames [i]);
    }


    // innerclasses ............................................................................................

    static class NbColorChooser extends JPanel implements ChangeListener {
        /** Color property editor */
        final ColorEditor editor;
        /** Color chooser instance */
        final JColorChooser chooser;
        /** Reference to model which holds the color selected in the color chooser */
        final ColorSelectionModel selectionModel;

        static final long serialVersionUID =-6230228701104365037L;
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

        public void removeNotify () {
            selectionModel.removeChangeListener (this);
        }

        public Dimension getPreferredSize () {
            Dimension s = super.getPreferredSize ();
            return new Dimension (s.width + 50, s.height + 10);
        }

        /*** implementation of the ChangeListener interface */
        public void stateChanged (ChangeEvent evt) {
            editor.setValue(selectionModel.getSelectedColor());
        }

    }

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
            return "[" + getRed () + "," + getGreen () + "," + getBlue () + "]"; // NOI18N
        }
    }

    /** Color chooser panel which can be added into JColorChooser */
    static final class NbColorChooserPanel extends AbstractColorChooserPanel
        implements ListSelectionListener {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -2792992315444428631L;
        /** List holding palette colors */
        JList list;

        String [] names;
        Color [] colors;
        Color color;
        int palette;
        /** Name for display of this chooser panel */
        String displayName;

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

        public void setColor (final Color newColor) {
            getColorSelectionModel().setSelectedColor(newColor);
        }

        public Color getColor () {
            return getColorFromModel();
        }

        /** Cell of the list showing palette colors */
        final class MyListCellRenderer extends JPanel implements ListCellRenderer {

            protected Border hasFocusBorder = new LineBorder (UIManager.getColor ("List.focusCellHighlight")); // NOI18N
            protected Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

            boolean selected, hasFocus;
            int index;

            static final long serialVersionUID =-8877709520578055594L;
            /** Creates a new MyListCellRenderer */
            public MyListCellRenderer () {
                setOpaque (true);
                setBorder (new EmptyBorder (1, 1, 1, 1));
            }

            /** Overrides default preferredSize impl.
            * @return Standard method returned preferredSize
            * (depends on font size only).
            */
            public Dimension getPreferredSize () {
                try {
                    FontMetrics fontMetrics = getFontMetrics(getFont());
                    return new Dimension (
                               fontMetrics.stringWidth (names [index]) + 30,
                               fontMetrics.getHeight () + 4
                           );
                } catch (NullPointerException e) {
                    return new Dimension (10, 10);
                }
            }

            public void paint (Graphics g) {
                Dimension rectangle = getSize ();
                Color color = g.getColor ();

                if (selected) g.setColor (UIManager.getColor ("List.selectionBackground")); // NOI18N
                else g.setColor (UIManager.getColor ("List.background")); // NOI18N
                g.fillRect (0, 0, rectangle.width - 1, rectangle.height - 1);

                if (hasFocus) {
                    g.setColor (Color.black);
                    g.drawRect (0, 0, rectangle.width - 1, rectangle.height - 1);
                }

                g.setColor (Color.black);
                g.drawRect (6, rectangle.height / 2 - 5 , 10, 10);
                g.setColor (colors [index]);
                g.fillRect (7, rectangle.height / 2 - 4 , 9, 9);
                if (selected) g.setColor (UIManager.getColor ("List.selectionForeground")); // NOI18N
                else g.setColor (UIManager.getColor ("List.foreground")); // NOI18N
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

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String XML_COLOR = "Color"; // NOI18N

    public static final String ATTR_TYPE = "type"; // NOI18N
    public static final String ATTR_RED = "red"; // NOI18N
    public static final String ATTR_GREEN = "green"; // NOI18N
    public static final String ATTR_BLUE = "blue"; // NOI18N
    public static final String ATTR_ID = "id"; // NOI18N
    public static final String ATTR_PALETTE = "palette"; // NOI18N

    public static final String VALUE_PALETTE = "palette"; // NOI18N
    public static final String VALUE_RGB = "rgb"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
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
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
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

/*
 * Log
 *  22   Gandalf   1.21        1/13/00  Petr Jiricka    i18n
 *  21   Gandalf   1.20        1/13/00  Petr Jiricka    i18n
 *  20   Gandalf   1.19        12/8/99  Petr Nejedly    Enabled setValue(null)
 *  19   Gandalf   1.18        11/26/99 Patrik Knakal   
 *  18   Gandalf   1.17        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  17   Gandalf   1.16        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  16   Gandalf   1.15        7/30/99  Ian Formanek    Fixed bug 2480 - Color 
 *       attribute can have haxedecimal values but int is expected.
 *  15   Gandalf   1.14        7/13/99  Ian Formanek    Fixed readFromXML
 *  14   Gandalf   1.13        7/12/99  Ian Formanek    Implements 
 *       XMLPropertyEditor
 *  13   Gandalf   1.12        7/8/99   Jesse Glick     Context help.
 *  12   Gandalf   1.11        6/28/99  Ian Formanek    throws 
 *       IllegalArgumentException if not passed Color instance in setValue
 *  11   Gandalf   1.10        6/27/99  Ian Formanek    Ignores non-Color values
 *  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         4/16/99  Libor Martinek  
 *  8    Gandalf   1.7         3/4/99   Jan Jancura     QuickSorter removed
 *  7    Gandalf   1.6         3/4/99   Jan Jancura     bundle moved
 *  6    Gandalf   1.5         2/5/99   David Simonek   
 *  5    Gandalf   1.4         2/5/99   Petr Hamernik   bugfix
 *  4    Gandalf   1.3         2/4/99   David Simonek   bugfix #1038
 *  3    Gandalf   1.2         2/4/99   Petr Hamernik   
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    some cotemporarily 
 *       commented out to compile under JDK 1.2
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
