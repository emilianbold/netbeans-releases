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

package org.netbeans.modules.beans.beaninfo;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
//import org.openide.explorer.propertysheet.editors.IconEditor.BiImageIcon;

/**
 * PropertyEditor for Icons. Depends on existing DataObject for images.
 * Images must be represented by some DataObject which returns itselv
 * as cookie, and has image file as a primary file. File extensions
 * for images is specified in isImage method.
 *
 * @author Jan Jancura
 */
class BiIconEditor extends PropertyEditorSupport {
    
    /** Standard variable for localization. */
    static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(
    BiIconEditor.class);
    
    public static boolean isImage(String s) {
        s = s.toLowerCase();
        return s.endsWith(".jpg") || s.endsWith(".gif") || // NOI18N
        s.endsWith(".jpeg") || s.endsWith(".jpe") || // NOI18N
        s.equals("jpg") || s.equals("gif") || // NOI18N
        s.equals("jpeg") || s.equals("jpe"); // NOI18N
    }
    
    // variables .................................................................................
    
    private Icon icon;
    
    // init .......................................................................................
    
    public BiIconEditor() {
    }
    
    // Special access methods......................................................................
    
    
    /** @return the name of image's source - depending on the type it can be a URL, file name or
     * resource path to the image on classpath */
    public String getSourceName() {
        if (getValue() instanceof BiImageIcon)
            return ((BiImageIcon)getValue()).name;
        else
            return null;
    }
    
    // PropertyEditor methods .....................................................................
    
    /**
     * @return The value of the property.  Builtin types such as "int" will
     * be wrapped as the corresponding object type such as "java.lang.Integer".
     */
    public Object getValue() {
        return icon;
    }
    
    /**
     * Set (or change) the object that is to be edited.  Builtin types such
     * as "int" must be wrapped as the corresponding object type such as
     * "java.lang.Integer".
     *
     * @param value The new target object to be edited.  Note that this
     *     object should not be modified by the PropertyEditor, rather
     *     the PropertyEditor should create a new object to hold any
     *     modified value.
     */
    public void setValue(Object object) {
        icon = (Icon) object;
    }
    
    /**
     * @return The property value as a human editable string.
     * <p>   Returns null if the value can't be expressed as an editable string.
     * <p>   If a non-null value is returned, then the PropertyEditor should
     *       be prepared to parse that string back in setAsText().
     */
    public String getAsText() {
        Object val = getValue();
        
        if (val == null) return "null"; // NOI18N
        
        if (val instanceof BiImageIcon) {
            BiImageIcon ii = (BiImageIcon)val;
            return ii.name; // NOI18N
        }
        return null;
    }
    
    /**
     * Set the property value by parsing a given String.  May raise
     * java.lang.IllegalArgumentException if either the String is
     * badly formatted or if this kind of property can't be expressed
     * as text.
     * @param text  The string to be parsed.
     */
    public void setAsText(String string) throws IllegalArgumentException {
        setValue(iconFromText(string));
    }
    
    private BiImageIcon iconFromText(String string) throws IllegalArgumentException {
        BiImageIcon ii;
        try {
            if (string.equals("null")) {
                ii = null;
            }
            else {
                URL url = TopManager.getDefault().currentClassLoader().getResource(string);
                ii = new BiImageIcon(url);
                ii.name = string;
            }
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
            throw new IllegalArgumentException(e.toString());
        }
        return ii;
    }
    
    /**
     * @return  True if the class will honor the paintValue method.
     */
    public boolean isPaintable() {
        return false;
    }
    
    /**
     * @return  True if the propertyEditor can provide a custom editor.
     */
    public boolean supportsCustomEditor() {
        return true;
    }
    
    /**
     * A PropertyEditor may choose to make available a full custom Component
     * that edits its property value.  It is the responsibility of the
     * PropertyEditor to hook itself up to its editor Component itself and
     * to report property value changes by firing a PropertyChange event.
     * <P>
     * The higher-level code that calls getCustomEditor may either embed
     * the Component in some larger property sheet, or it may put it in
     * its own individual dialog, or ...
     *
     * @return A java.awt.Component that will allow a human to directly
     *      edit the current property value.  May be null if this is
     *      not supported.
     */
    public java.awt.Component getCustomEditor() {
        return new IconPanel();
    }
    
    public static class BiImageIcon extends ImageIcon implements Externalizable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 7018807466471349466L;
        String name;
        
        public BiImageIcon() {
        }
        
        BiImageIcon(URL url) {
            super(url);
        }
        
        BiImageIcon(String file) {
            super(file);
        }
        
        String getName() {
            return name;
        }
        
        public void writeExternal(ObjectOutput oo) throws IOException {
            oo.writeObject(name);
        }
        
        public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
            name = (String) in.readObject();
            ImageIcon ii = null;
            ii = new ImageIcon(TopManager.getDefault().currentClassLoader().getResource(name));
            setImage(ii.getImage());
        }
    }
    
    class IconPanel extends JPanel implements EnhancedCustomPropertyEditor {
        JRadioButton rbClasspath, rbNoPicture;
        JTextField tfName;
        JButton bSelect;
        JScrollPane spImage;
        
        static final long serialVersionUID =-6904264999063788703L;
        IconPanel() {
            // visual components .............................................
            
            JLabel lab;
            setLayout(new BorderLayout(6, 6));
            setBorder(new EmptyBorder(6, 6, 6, 6));
            JPanel p = new JPanel(new BorderLayout(3, 3));
            JPanel p1 = new JPanel(new BorderLayout());
            p1.setBorder(new TitledBorder(new EtchedBorder(), bundle.getString("CTL_ImageSourceType")));
            JPanel p2 = new JPanel();
            p2.setBorder(new EmptyBorder(0, 3, 0, 3));
            GridBagLayout l = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            p2.setLayout(l);
            c.anchor = GridBagConstraints.WEST;
            
            p2.add(rbClasspath = new JRadioButton(bundle.getString("CTL_Classpath")));
            c.gridwidth = 1;
            l.setConstraints(rbClasspath, c);
            
            p2.add(lab = new JLabel(bundle.getString("CTL_ClasspathExample")));
            c.gridwidth = GridBagConstraints.REMAINDER;
            l.setConstraints(lab, c);
            
            p2.add(rbNoPicture = new JRadioButton(bundle.getString("CTL_NoPicture")));
            c.gridwidth = 1;
            l.setConstraints(rbNoPicture, c);
            
            p2.add(lab = new JLabel(bundle.getString("CTL_Null")));
            c.gridwidth = GridBagConstraints.REMAINDER;
            l.setConstraints(lab, c);
            
            ButtonGroup bg = new ButtonGroup();
            bg.add(rbClasspath);
            bg.add(rbNoPicture);
            rbClasspath.setSelected(true);
            p1.add(p2, "West"); // NOI18N
            p.add(p1, "North"); // NOI18N
            p1 = new JPanel(new BorderLayout(6, 6));
            p1.add(new JLabel(bundle.getString("CTL_ImageSourceName")), "West");
            p1.add(tfName = new JTextField(), "Center"); // NOI18N
            p1.add(bSelect = new JButton("..."), "East"); // NOI18N
            bSelect.setEnabled(false);
            p.add(p1, "South"); // NOI18N
            add(p, "North"); // NOI18N
            spImage = new JScrollPane() {
                public Dimension getPreferredSize() {
                    return new Dimension(60, 60);
                }
            };
            add(spImage, "Center"); // NOI18N
            
            // listeners .................................................
            
            tfName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setValue();
                }
            });
            rbClasspath.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bSelect.setEnabled(true);
                    tfName.setEnabled(true);
                    setValue();
                }
            });
            rbNoPicture.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    bSelect.setEnabled(false);
                    tfName.setEnabled(false);
                    icon = null; //IconEditor.this.setValue(null);
                    updateIcon();
                }
            });
            bSelect.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (rbClasspath.isSelected()) {
                        //            InputPanel ip = new InputPanel();
                        Places places = TopManager.getDefault().getPlaces();
                        Node ds = places.nodes().repository(new DataFilter() {
                            public boolean acceptDataObject(DataObject obj) {
                                // accept only data folders but ignore read only roots of file systems
                                if (obj instanceof DataFolder)
                                    return !obj.getPrimaryFile().isReadOnly() ||
                                    obj.getPrimaryFile().getParent() != null;
                                return isImage(obj.getPrimaryFile().getExt());
                            }
                        });
                        
                        String name;
                        try {
                            // selects one folder from data systems
                            DataObject d = (DataObject)
                            TopManager.getDefault().getNodeOperation().select(
                            bundle.getString("CTL_OpenDialogName"),
                            bundle.getString("CTL_FileSystemName"),
                            TopManager.getDefault().getPlaces().nodes().repository(),
                            new NodeAcceptor() {
                                public boolean acceptNodes(Node[] nodes) {
                                    if ((nodes == null) || (nodes.length != 1))
                                        return false;
                                    return nodes[0].getCookie(DataFolder.class) == null;
                                }
                            },
                            null
                            )[0].getCookie(DataObject.class);
                            name = (d.getPrimaryFile().getPackageNameExt('/', '.'));
                        } catch (org.openide.util.UserCancelException ex) {
                            return;
                        }
                        tfName.setText("/" + name); // NOI18N
                        setValue();
                    }
                }
            });
            // initialization ......................................
 
            updateIcon();
            Icon i = (Icon)getValue();
            if (i == null) {
                rbNoPicture.setSelected(true);
                bSelect.setEnabled(false);
                tfName.setEnabled(false);
                return;
            }
            if (!(i instanceof BiImageIcon)) return;
            
            rbClasspath.setSelected(true);
            bSelect.setEnabled(true);
            tfName.setText(((BiImageIcon)i).name);
            HelpCtx.setHelpIDString(this, IconPanel.class.getName());
        }
        
        void updateIcon() {
            Icon i = (Icon)getValue();
            spImage.setViewportView((i == null) ? new JLabel() : new JLabel(i));
            //      repaint();
            validate();
        }
        
        void setValue() {
            String val = tfName.getText();
            val.trim();
            if ("".equals(val)) { // NOI18N
                icon = null;
                return;
            }
            
            try {
                icon = iconFromText(val);
            } catch (IllegalArgumentException ee) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) ee.printStackTrace(); // NOI18N
            }
            updateIcon();
        }
        
        public Object getPropertyValue() throws IllegalStateException {
            BiImageIcon ii = null;
            String s = tfName.getText().trim();
            try {
                if (rbClasspath.isSelected()) {
                    URL url = TopManager.getDefault().currentClassLoader().getResource(s);
                    ii = new BiImageIcon(url);
                    ii.name = s;
                }
            } catch (Exception e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                throw new IllegalStateException(e.toString());
            }
            return ii;
        }
        
    } // end of IconPanel
}
