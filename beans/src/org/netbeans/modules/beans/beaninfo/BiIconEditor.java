/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans.beaninfo;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import java.text.MessageFormat;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 * PropertyEditor for Icons. Depends on existing DataObject for images.
 * Images must be represented by some DataObject which returns itselv
 * as cookie, and has image file as a primary file. File extensions
 * for images is specified in isImage method.
 *
 * @author Jan Jancura
 */
class BiIconEditor extends PropertyEditorSupport {
    
    private static final String BEAN_ICONEDITOR_HELP = "beans.icon"; // NOI18N
    
    private FileObject sourceFileObject;
    
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
    
    //private Icon icon;
    
    // init .......................................................................................
    
    public BiIconEditor( FileObject sourceFileObject ) {
        this.sourceFileObject = sourceFileObject;
    }
    
    // Special access methods......................................................................
    
    
    /** @return the name of image's source - depending on the type it can be a URL, file name or
     * resource path to the image on classpath */
    public String getSourceName() {
        if (getValue() instanceof BiImageIcon)
            return ((BiImageIcon)getValue()).getName();
        else
            return null;
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
            return ii.getName(); // NOI18N
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
        try { 
            setValue(iconFromText(string));
        }
        catch ( IllegalArgumentException e ) {
            // User inserted incorrect path either report or
            // do nothing
            // For now choosing doing nothing
        }
    }
    
    private BiImageIcon iconFromText(String string) throws IllegalArgumentException {
        BiImageIcon ii;
        try {
            if (string.length() == 0 || string.equals("null")) { // NOI18N
                ii = null;
            }
            else {
                ClassPath cp = ClassPath.getClassPath( sourceFileObject, ClassPath.SOURCE );                
                
                URL url = cp.findResource( string.substring(1) ).getURL();
                ii = new BiImageIcon(url, string);
            }
        } catch (Throwable e) {
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
    
    public static class BiImageIcon extends ImageIcon /* implements Externalizable */ {
        /** generated Serialized Version UID */
        //static final long serialVersionUID = 7018807466471349466L;
        private String name;
        
        public BiImageIcon() {
        }
        
        BiImageIcon(URL url, String name) {
            super(url);
            this.name = name;
        }
        
        BiImageIcon(String file, String name ) {
            super(file);
            this.name = name;
        }
        
        String getName() {
            return name;            
        }
        
        /*
        public void writeExternal(ObjectOutput oo) throws IOException {
            oo.writeObject(name);
        }
        
        public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
            name = (String) in.readObject();
            ImageIcon ii = null;
            ii = new ImageIcon(Repository.getDefault().findResource(name).getURL());
            setImage(ii.getImage());
        }
         */
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
            getAccessibleContext().setAccessibleName(bundle.getString("ACS_IconPanelA11yName"));  // NOI18N
            getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_IconPanelA11yDesc"));  // NOI18N
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
            rbClasspath.setToolTipText(bundle.getString("ACS_ClasspathA11yDesc"));
            rbClasspath.setMnemonic(bundle.getString("CTL_Classpath_Mnemonic").charAt(0));
            c.gridwidth = 1;
            l.setConstraints(rbClasspath, c);
            
            p2.add(lab = new JLabel(bundle.getString("CTL_ClasspathExample")));
            lab.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ClasspathExampleA11yDesc"));
            c.gridwidth = GridBagConstraints.REMAINDER;
            l.setConstraints(lab, c);
            
            p2.add(rbNoPicture = new JRadioButton(bundle.getString("CTL_NoPicture")));
            rbNoPicture.setToolTipText(bundle.getString("ACS_NoPictureA11yDesc"));
            rbNoPicture.setMnemonic(bundle.getString("CTL_NoPicture_Mnemonic").charAt(0));
            c.gridwidth = 1;
            l.setConstraints(rbNoPicture, c);
            
            p2.add(lab = new JLabel(bundle.getString("CTL_Null")));
            lab.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NullA11yDesc"));
            c.gridwidth = GridBagConstraints.REMAINDER;
            l.setConstraints(lab, c);
            
            ButtonGroup bg = new ButtonGroup();
            bg.add(rbClasspath);
            bg.add(rbNoPicture);
            rbClasspath.setSelected(true);
            p1.add(p2, "West"); // NOI18N
            p.add(p1, "North"); // NOI18N
            p1 = new JPanel(new BorderLayout(6, 6));
            JLabel nameLabel = new JLabel(bundle.getString("CTL_ImageSourceName"));
            nameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ImageSourceNameA11yDesc"));
            nameLabel.setDisplayedMnemonic(bundle.getString("CTL_ImageSourceName_Mnemonic").charAt(0));
            p1.add(nameLabel, "West"); // NOI18N
            p1.add(tfName = new JTextField(), "Center"); // NOI18N
            nameLabel.setLabelFor(tfName);
            tfName.getAccessibleContext().setAccessibleName(bundle.getString("ACS_ImageSourceNameTextFieldA11yName"));
            tfName.setToolTipText(bundle.getString("ACS_ImageSourceNameTextFieldA11yDesc"));
            p1.add(bSelect = new JButton("..."), "East"); // NOI18N
            bSelect.getAccessibleContext().setAccessibleName(bundle.getString("ACS_ImageSourceNameBrowseButtonA11yName"));
            bSelect.setToolTipText(bundle.getString("ACS_ImageSourceNameBrowseButtonA11yDesc"));
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
                    
                    BiIconEditor.this.setValue(null);
                    updateIcon();
                }
            });
            bSelect.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (rbClasspath.isSelected()) {
                        String name;
                        try {
                            // selects one folder from data systems
                            DataObject d = (DataObject)
                            NodeOperation.getDefault().select(
                            bundle.getString("CTL_OpenDialogName"),
                            bundle.getString("CTL_FileSystemName"),
                            RepositoryNodeFactory.getDefault().repository(DataFilter.ALL),
                            new NodeAcceptor() {
                                public boolean acceptNodes(Node[] nodes) {
                                    if ((nodes == null) || (nodes.length != 1))
                                        return false;
                                    return nodes[0].getCookie(DataFolder.class) == null;
                                }
                            },
                            null
                            )[0].getCookie(DataObject.class);
                            FileObject sourceFo = d.getPrimaryFile();
                            ClassPath cp = ClassPath.getClassPath( sourceFo, ClassPath.SOURCE );
                            name = cp.getResourceName( sourceFo );
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
            
            HelpCtx.setHelpIDString(this, BEAN_ICONEDITOR_HELP); 
            
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
            tfName.setText(((BiImageIcon)i).getName());
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
                BiIconEditor.this.setValue(null);
                return;
            }
            
            try {
                BiIconEditor.this.setValue(iconFromText(val));
            } catch (IllegalArgumentException ee) {
                // Reporting the exception is maybe too much let's do nothing
                // instead 
                // org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ee);
            }
            updateIcon();
        }
        
        public Object getPropertyValue() throws IllegalStateException {
            BiImageIcon ii = null;
            String s = tfName.getText().trim();
            try {
                if (rbClasspath.isSelected() && s.length() != 0 ) {                    
                    ClassPath cp = ClassPath.getClassPath( sourceFileObject, ClassPath.SOURCE );
                    FileObject f = cp.findResource( s.substring(1) );
                    try{
                        ii = new BiImageIcon(f.getURL(), s);
                    }
                    catch(java.lang.Throwable t){
                        MessageFormat message = new MessageFormat( bundle.getString("CTL_Icon_not_exists")); //NOI18N
                        Object[] form = {s};//CTL_Icon_not_exists=Image class path for {0} is not valid
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message.format(form), NotifyDescriptor.ERROR_MESSAGE ));
                    }
                }
            } catch (Exception e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) e.printStackTrace(); // NOI18N
                throw new IllegalStateException(e.toString());
            }
            BiIconEditor.this.setValue(ii);
            return ii;
        }
        
    } // end of IconPanel
}
