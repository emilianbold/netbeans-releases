/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.editors2;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.propertysheet.*;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;

import org.netbeans.api.project.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.form.FormEditor;

/**
 * PropertyEditor for Icons. Depends on existing DataObject for images.
 * Images must be represented by some DataObject which returns itself
 * as cookie, and has image file as a primary file. File extensions
 * for images is specified in isImage method.
 *
 * @author Jan Jancura, Jan Stola
 */
public class IconEditor extends PropertyEditorSupport implements PropertyEditor, XMLPropertyEditor, ExPropertyEditor, FormAwareEditor {
    /** Type constant for icons from URL. */
    public static final int TYPE_URL = 1;
    /** Type constant for icons from file. */
    public static final int TYPE_FILE = 2;
    /** Type constant for icons from classpath. */
    public static final int TYPE_CLASSPATH = 3;
    /** Name prefix for icons from URL. */
    private static final String URL_PREFIX = "URL"; // NOI18N
    /** Name prefix for icons from file. */
    private static final String FILE_PREFIX = "File"; // NOI18N
    /** Name prefix for icons from file. */
    private static final String CLASSPATH_PREFIX = "Classpath"; // NOI18N
    
    /**
     * Returns localized string from bundle.
     *
     * @param key key for the localized string.
     * @return localized string for the key.
     */
    private static String getString(String key) {
        return org.openide.util.NbBundle.getBundle(IconEditor.class).getString(key);
    }
    
    /**
     * Determines whether the file contains image (it checks that it has
     * extension that is supported).
     *
     * @param s name/path of the file.
     * @return <code>true</code> if the file contains image,
     * returns <code>false</cde> otherwise.
     */
    public static boolean isImage(String s) {
        if (s == null) {
            return false;
        }
        s = s.toLowerCase();
        return s.endsWith(".jpg") || s.endsWith(".gif") || // NOI18N
        s.endsWith(".jpeg") || s.endsWith(".jpe") || // NOI18N
        s.equals("jpg") || s.equals("gif") || // NOI18N
        s.equals("jpeg") || s.equals("jpe"); // NOI18N
    }
    
    /**
     * Duplicates backslashes in the input string.
     *
     * @param s string to duplicate backslashes in.
     * @return input string with duplicated backslashes.
     */
    private static String convert(String s) {
        StringTokenizer st = new StringTokenizer(s, "\\"); // NOI18N
        StringBuffer sb = new StringBuffer();
        if (st.hasMoreElements()) {
            sb.append(st.nextElement());
            while (st.hasMoreElements())
                sb.append("\\\\").append(st.nextElement()); // NOI18N
        }
        return sb.toString();
    }
    
    // variables ..................................................................................
    
    private PropertyEnv propertyEnv;
	private FormModel formModel;
    
    // Special access methods......................................................................
    
    /**
     * Returns source type of the icon.
     *
     * @return the type of icon source - one of <code>TYPE_CLASSPATH</code>,
     * <code>TYPE_FILE</code>, <code>TYPE_URL</code>.
     */
    public int getSourceType() {
        if (getValue() instanceof NbImageIcon)
            return ((NbImageIcon)getValue()).type;
        else
            return TYPE_FILE;
    }
    
    /**
     * Returns icon source name.
     *
     * @return the name of icon's source - depending on the type it can be a URL,
     * file name or resource path to the image on classpath.
     */
    public String getSourceName() {
        if (getValue() instanceof NbImageIcon)
            return ((NbImageIcon)getValue()).name;
        else
            return null;
    }
    
    // PropertyEditor methods .....................................................................
    
    public Object getValue() {
        return super.getValue();
    }
    
    public void setValue(Object object) {
        if (propertyEnv != null) {
            if ( object == null || ((object instanceof NbImageIcon) && ((NbImageIcon)object).stateValid) ) {
                propertyEnv.setState(PropertyEnv.STATE_VALID);
            } else {
                propertyEnv.setState(PropertyEnv.STATE_INVALID);
            }
        }        
        super.setValue((object instanceof NbImageIcon) ? object : null);
    }
    
    public String getAsText() {
        Object val = getValue();
        
        if (val == null) return "null"; // NOI18N
        
        if (val instanceof NbImageIcon) {
            NbImageIcon ii = (NbImageIcon)val;
            switch (ii.type) {
                case TYPE_URL:
                    return URL_PREFIX + ": " + ii.name; // NOI18N
                case TYPE_FILE:
                    return FILE_PREFIX + ": " + ii.name; // NOI18N
                case TYPE_CLASSPATH:
                    return CLASSPATH_PREFIX + ": " + ii.name; // NOI18N
            }
        }
        return null;
    }
    
    public void setAsText(String string) throws IllegalArgumentException {
        setValue(iconFromText(string));
    }
    
    public String getJavaInitializationString() {
        if (getValue() instanceof NbImageIcon) {
            NbImageIcon ii = (NbImageIcon)getValue();
            switch (ii.type) {
                case TYPE_URL: return
                "new javax.swing.JLabel() {\n" + // NOI18N
                "  public javax.swing.Icon getIcon() {\n" + // NOI18N
                "    try {\n" + // NOI18N
                "      return new javax.swing.ImageIcon(\n" + // NOI18N
                "        new java.net.URL(\"" + convert(ii.name) + "\")\n" + // NOI18N
                "      );\n" + // NOI18N
                "    } catch (java.net.MalformedURLException e) {\n" + // NOI18N
                "    }\n" + // NOI18N
                "    return null;\n" + // NOI18N
                "  }\n" + // NOI18N
                "}.getIcon()"; // NOI18N
                case TYPE_FILE: return
                "new javax.swing.ImageIcon(\"" + convert(ii.name) + "\")"; // NOI18N
                case TYPE_CLASSPATH: return
                "new javax.swing.ImageIcon(getClass().getResource(\"" + convert(ii.name) + "\"))"; // NOI18N
            }
        }
        return "null"; // NOI18N
    }
    
    public String[] getTags() {
        return null;
    }
    
    public boolean isPaintable() {
        return false;
    }
    
    public void paintValue(Graphics g, Rectangle rectangle) {
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public Component getCustomEditor() {
        return new IconPanel();
    }
    
    /**
     * Returns URL of the given resource.
     *
     * @param resource name of some resource.
     * @return URL of the given resource.
     */
    private URL findResource(String resource) {
        if (resource.startsWith("/")) { // NOI18N
            resource = resource.substring(1);
        }
        FileObject formFile = FormEditor.getFormDataObject(formModel).getFormFile();
        ClassPath classPath = ClassPath.getClassPath(formFile, ClassPath.SOURCE);
        FileObject resourceObject = classPath.findResource(resource);
        if (resourceObject == null) {
            classPath = ClassPath.getClassPath(formFile, ClassPath.EXECUTE);
            resourceObject = classPath.findResource(resource);
        }
        if (resourceObject == null) {
            return null;
        } else {
            try {
                return resourceObject.getURL();
            } catch (FileStateInvalidException fsie) {
                ErrorManager.getDefault().notify(fsie);
                return null;
            }
        }
    }
    
    public void setFormModel(FormModel model) {
        this.formModel = model;
    }
    
    /**
     * Returns icon for the given source name.
     *
     * @param string source name of the icon.
     * @return icon for the given source name.
     * @throws IllegalArgumentException when the passed value cannot
     * cannot be resolved to an icon.
     */
    private NbImageIcon iconFromText(String string) throws IllegalArgumentException {
        NbImageIcon ii;
        try {
            if (string.startsWith(FILE_PREFIX)) {
                String s = string.substring(FILE_PREFIX.length() + 1).trim();
                ii = new NbImageIcon(s);
                ii.type = TYPE_FILE;
                ii.name = s;
            } else
                if (string.startsWith(CLASSPATH_PREFIX)) {
                    String s = string.substring(CLASSPATH_PREFIX.length() + 1).trim();
                    
                    if((s == null)|| ("".equals(s)) || ("/".equals(s)) // NOI18N
                    || ("///".equals(s)) || (s.endsWith("#"))) {    // NOI18N
                        // #13035
                        // the empty string and couple of others has to be treated specially
                        // since TopManager.getDefault().currentClassLoader().getResource(s);
                        // is able to return non null value for the . And that
                        // wrong non-null value causes problems in new NbImageIcon(...)
                        return null;
                    }
                    
                    URL u = findResource(s);
                    
                    if (u == null) {
                        ii = new NbImageIcon();
                    } else {
                        ii = new NbImageIcon(u);
                    }
                    
                    ii.type = TYPE_CLASSPATH;
                    ii.name = s;
                } else
                    if (string.startsWith(URL_PREFIX)) {
                        String s = string.substring(URL_PREFIX.length() + 1).trim();
                        URL url = new URL(s);
                        ii = new NbImageIcon(url);
                        ii.type = TYPE_URL;
                        ii.name = s;
                    } else
                        if (string.equals("null")) { // NOI18N
                            ii = null;
                        }
                        else {
                            ii = new NbImageIcon(string.trim());
                            ii.type = TYPE_FILE;
                            ii.name = string;
                        }
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException();
            throw iae;
        }
        return ii;
    }
    
    // innerclasses ...............................................................................
    
    public static class NbImageIcon implements FormDesignValue, Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 7018807466471349466L;
        /** The icon itself. */
        private ImageIcon icon = null;
        /** Source type of the icon. */
        private int type;
        /** Name of the icon. */
        private String name;
        /** Is the state valid? */
        private boolean stateValid;
        
        public NbImageIcon() {            
            stateValid = false;
            icon = new ImageIcon("");
        }
        
        NbImageIcon(URL url) {
            type = TYPE_URL;
            stateValid = true;
            icon = new ImageIcon(url);
        }
        
        NbImageIcon(String file) {
            type = TYPE_FILE;
            stateValid = true;
            icon = new ImageIcon(file);
        }
        
        public NbImageIcon(NbImageIcon nbIcon) {
            icon = nbIcon.icon;
            type = nbIcon.type;
            name = nbIcon.name;
            stateValid = nbIcon.stateValid;
        }
        
        String getName() {
            return name;
        }
        
        public Object getDesignValue() {
            return icon;
        }
        
        public String getDescription() {
            return name;
        }
        
    }
    
    
    private class IconPanel extends JPanel implements EnhancedCustomPropertyEditor, ActionListener {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -6904264999063788703L;
        
        private JPanel jPanel1;
        private JLabel jLabel1;
        private JRadioButton rbUrl;
        private JRadioButton rbFile;
        private JRadioButton rbClasspath;
        private JRadioButton rbNoPicture;
        private JLabel jLabel2;
        private JLabel jLabel3;
        private JLabel jLabel4;
        private JLabel jLabel5;
        private JPanel jPanel2;
        private JLabel lName;
        private JTextField tfName;
        private JButton bSelect;
        private JPanel jPanel3;
        private JLabel jLabel7;
        private JScrollPane spImage;
        private JLabel iconLabel;
        
        // ======================================
        
        private NbImageIcon localIcon;
        
        /**
         * Creates new form <code>IconPanel</code>.
         */
        public IconPanel() {
            iconLabel = new JLabel() {
                public boolean isFocusTraversable() {
                    return true;
                }
            };
            iconLabel.setPreferredSize(new Dimension(32, 32));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);
            
            initComponents();
            spImage.setViewportView(iconLabel);
            
            jLabel1.setText(getString("CTL_ImageSourceType")); // NOI18N
            rbUrl.setText(getString("CTL_URL")); // NOI18N
            rbFile.setText(getString("CTL_File")); // NOI18N
            rbClasspath.setText(getString("CTL_Classpath")); // NOI18N
            rbNoPicture.setText(getString("CTL_NoPicture")); // NOI18N
            jLabel2.setText(getString("CTL_URLExample")); // NOI18N
            jLabel3.setText(getString("CTL_FileExample")); // NOI18N
            jLabel4.setText(getString("CTL_ClasspathExample")); // NOI18N
            jLabel5.setText(getString("CTL_Null")); // NOI18N
            lName.setText(getString("CTL_ImageSourceName")); // NOI18N
            lName.setDisplayedMnemonic(getString("CTL_ImageSourceName_mnemonic").charAt(0)); // NOI18N
            lName.setLabelFor(tfName);
            jLabel7.setText(getString("CTL_Preview")); // NOI18N
            jLabel7.setDisplayedMnemonic(getString("CTL_Preview_mnemonic").charAt(0)); // NOI18N
            bSelect.setText(getString("CTL_ButtonSelect")); // NOI18N
            bSelect.setMnemonic(getString("CTL_ButtonSelect_mnemonic").charAt(0)); // NOI18N
            
            jLabel1.setLabelFor(jPanel1);
            jLabel2.setLabelFor(rbUrl);
            jLabel3.setLabelFor(rbFile);
            jLabel4.setLabelFor(rbClasspath);
            jLabel5.setLabelFor(rbNoPicture);
            jLabel7.setLabelFor(iconLabel);
            
            rbUrl.setMnemonic(getString("CTL_URL_mnemonic").charAt(0)); // NOI18N
            rbFile.setMnemonic(getString("CTL_File_mnemonic").charAt(0)); // NOI18N
            rbClasspath.setMnemonic(getString("CTL_Classpath_mnemonic").charAt(0)); // NOI18N
            rbNoPicture.setMnemonic(getString("CTL_NoPicture_mnemonic").charAt(0)); // NOI18N
            
            tfName.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_ImageSourceName")); // NOI18N
            bSelect.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_ButtonSelect")); // NOI18N
            iconLabel.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_Preview")); // NOI18N
            rbUrl.getAccessibleContext().setAccessibleDescription(jLabel2.getText());
            rbFile.getAccessibleContext().setAccessibleDescription(jLabel3.getText());
            rbClasspath.getAccessibleContext().setAccessibleDescription(jLabel4.getText());
            rbNoPicture.getAccessibleContext().setAccessibleDescription(jLabel5.getText());
            
            getAccessibleContext().setAccessibleDescription(getString("ACSD_IconCustomEditor")); // NOI18N
            
            ButtonGroup bg = new ButtonGroup();
            bg.add(rbUrl);
            bg.add(rbFile);
            bg.add(rbClasspath);
            bg.add(rbNoPicture);
            
            Object value = getValue();
            
            if (propertyEnv != null)
                propertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            
            if (value == null || !(value instanceof NbImageIcon)) {
                rbNoPicture.setSelected(true);
                bSelect.setEnabled(false);
                tfName.setEnabled(false);
                return;
            } else {
                localIcon = (NbImageIcon)value;
            }
            
            switch (((NbImageIcon)localIcon).type) {
                case TYPE_URL:
                    rbUrl.setSelected(true);
                    bSelect.setEnabled(false);
                    break;
                case TYPE_FILE:
                    rbFile.setSelected(true);
                    bSelect.setEnabled(true);
                    break;
                case TYPE_CLASSPATH:
                    rbClasspath.setSelected(true);
                    bSelect.setEnabled(true);
                    break;
            }
            tfName.setText(((NbImageIcon)localIcon).name);
            
            updateIcon();
        }
        
        /**
         * This method is called from within the constructor to initialize the form.
         */
        private void initComponents() {
            jPanel1 = new JPanel();
            jLabel1 = new JLabel();
            rbUrl = new JRadioButton();
            rbFile = new JRadioButton();
            rbClasspath = new JRadioButton();
            rbNoPicture = new JRadioButton();
            jLabel2 = new JLabel();
            jLabel3 = new JLabel();
            jLabel4 = new JLabel();
            jLabel5 = new JLabel();
            jPanel2 = new JPanel();
            lName = new JLabel();
            tfName = new JTextField();
            bSelect = new JButton();
            jPanel3 = new JPanel();
            jLabel7 = new JLabel();
            spImage = new JScrollPane();
            
            setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints2;
            
            jPanel1.setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints1;
            
            gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.insets = new Insets(12, 12, 0, 0);
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            jPanel1.add(jLabel1, gridBagConstraints1);
            
            gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 1;
            gridBagConstraints1.insets = new Insets(12, 24, 0, 0);
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            jPanel1.add(rbUrl, gridBagConstraints1);
            
            gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 2;
            gridBagConstraints1.insets = new Insets(0, 24, 0, 0);
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            jPanel1.add(rbFile, gridBagConstraints1);
            
            gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 3;
            gridBagConstraints1.insets = new Insets(0, 24, 0, 0);
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            jPanel1.add(rbClasspath, gridBagConstraints1);
            
            gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 4;
            gridBagConstraints1.insets = new Insets(0, 24, 0, 0);
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            jPanel1.add(rbNoPicture, gridBagConstraints1);
            
            gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.gridy = 1;
            gridBagConstraints1.insets = new Insets(12, 5, 0, 12);
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            jPanel1.add(jLabel2, gridBagConstraints1);
            
            gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.gridy = 2;
            gridBagConstraints1.insets = new Insets(5, 5, 0, 12);
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            jPanel1.add(jLabel3, gridBagConstraints1);
            
            gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.gridy = 3;
            gridBagConstraints1.insets = new Insets(5, 5, 0, 12);
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            jPanel1.add(jLabel4, gridBagConstraints1);
            
            gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.gridy = 4;
            gridBagConstraints1.insets = new Insets(5, 5, 0, 12);
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.weighty = 1.0;
            jPanel1.add(jLabel5, gridBagConstraints1);
            
            gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.fill = GridBagConstraints.BOTH;
            add(jPanel1, gridBagConstraints2);
            
            jPanel2.setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints3;
            
            gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.insets = new Insets(12, 12, 0, 0);
            gridBagConstraints3.anchor = GridBagConstraints.WEST;
            jPanel2.add(lName, gridBagConstraints3);
            
            gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.insets = new Insets(12, 5, 0, 0);
            gridBagConstraints3.anchor = GridBagConstraints.WEST;
            gridBagConstraints3.weightx = 1.0;
            jPanel2.add(tfName, gridBagConstraints3);
            
            gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.insets = new Insets(12, 5, 0, 17);
            gridBagConstraints3.anchor = GridBagConstraints.WEST;
            jPanel2.add(bSelect, gridBagConstraints3);
            
            gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 1;
            gridBagConstraints2.fill = GridBagConstraints.BOTH;
            add(jPanel2, gridBagConstraints2);
            
            jPanel3.setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints4;
            
            gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.insets = new Insets(12, 12, 0, 0);
            gridBagConstraints4.anchor = GridBagConstraints.WEST;
            jPanel3.add(jLabel7, gridBagConstraints4);
            
            gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.gridy = 1;
            gridBagConstraints4.fill = GridBagConstraints.BOTH;
            gridBagConstraints4.insets = new Insets(5, 12, 0, 12);
            gridBagConstraints4.weightx = 1.0;
            gridBagConstraints4.weighty = 1.0;
            jPanel3.add(spImage, gridBagConstraints4);
            
            gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 2;
            gridBagConstraints2.fill = GridBagConstraints.BOTH;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.weighty = 1.0;
            add(jPanel3, gridBagConstraints2);
            
            // listeners .................................................
            
            tfName.addActionListener(this);
            rbFile.addActionListener(this);
            rbUrl.addActionListener(this);
            rbClasspath.addActionListener(this);
            rbNoPicture.addActionListener(this);
            bSelect.addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == tfName) {
                setValue();
            } else if (source == rbFile) {
                bSelect.setEnabled(true);
                tfName.setEnabled(true);
                setValue();
                updateIcon();
            } else if (source == rbUrl) {
                bSelect.setEnabled(false);
                tfName.setEnabled(true);
                setValue();
            } else if (source == rbClasspath) {
                bSelect.setEnabled(true);
                tfName.setEnabled(true);
                setValue();
            } else if (source == rbNoPicture) {
                bSelect.setEnabled(false);
                tfName.setEnabled(false);
                localIcon = null;
                updateIcon();
            } else if (source == bSelect) {
                if(rbFile.isSelected()) {
                    File f = selectFile();
                    if (f != null) {
                        tfName.setText(f.getAbsolutePath());
                        setValue();
                    }
                } else {
                    if (rbClasspath.isSelected()) {
                        String name = selectResource();
                        if (name != null) {
                            tfName.setText("/" + name); // NOI18N
                            setValue();
                        }
                    }
                }
                
            }
        }
        
        
        /**
         * Presents the user with the file chooser type dialog.
         *
         * @returns the file selected or <code>null</code>.
         */
        private File selectFile() {
            final File[] ff = new File[1];
            final FeatureDescriptor fd = new FeatureDescriptor();
            ExPropertyModel epm = new ExPropertyModel() {
                public void setValue(Object val) {
                    ff[0] = (File)val;
                }
                public Object getValue() {
                    return ff[0];
                }
                public Class getPropertyType() {
                    return File.class;
                }
                public Class getPropertyEditorClass() {
                    return null;
                }
                public void addPropertyChangeListener(PropertyChangeListener l) {
                }
                public void removePropertyChangeListener(PropertyChangeListener l) {
                }
                public Object[] getBeans() {
                    return new Object[0];
                }
                public FeatureDescriptor getFeatureDescriptor() {
                    return fd;
                }
            };
            FileFilter filter = new FileFilter() {
                public boolean accept(java.io.File f) {
                    return isImage(f.getName()) || f.isDirectory();
                }
                public String getDescription() {
                    return getString("CTL_ImagesExtensionName"); // NOI18N
                }
            };
            fd.setValue("directories", Boolean.FALSE);  // NOI18N
            fd.setValue("files", Boolean.TRUE);  // NOI18N
            fd.setValue("filter", filter); // NOI18N
            PropertyPanel panel = new PropertyPanel(epm, PropertyPanel.PREF_CUSTOM_EDITOR);
            DialogDescriptor dd = new DialogDescriptor(panel, getString("CTL_OpenDialogName"), true, null); // NOI18N
            Object res = DialogDisplayer.getDefault().notify(dd);
            if (res == DialogDescriptor.OK_OPTION) {
                return ff[0];
            } else {
                return null;
            }
        }
        
        private java.util.List getRoots(ClassPath cp) {
            ArrayList l = new ArrayList(cp.entries().size());
            Iterator eit = cp.entries().iterator();
            while(eit.hasNext()) {
                ClassPath.Entry e = (ClassPath.Entry)eit.next();
                
                // try to map it to sources
                URL url = e.getURL();
                SourceForBinaryQuery.Result r= SourceForBinaryQuery.findSourceRoots(url);
                FileObject [] fos = r.getRoots();
                if (fos.length > 0) {
                    for (int i = 0 ; i < fos.length; i++) l.add(fos[i]);
                } else {
                    if (e.getRoot()!=null)
                        l.add(e.getRoot()); // add the class-path location
                                            // directly
                }
            }
            
            return l;
        }
        
        /**
         * Obtains icon resource from the user.
         *
         * @returns name of the selected resource or <code>null</code>.
         */
        private String selectResource() {
            FileObject formFile = FormEditor.getFormDataObject(formModel).getFormFile();
            ClassPath executeClassPath = ClassPath.getClassPath(formFile, ClassPath.EXECUTE);
            java.util.List roots = (executeClassPath == null) ? Collections.EMPTY_LIST : getRoots(executeClassPath);
            Project project = FileOwnerQuery.getOwner(formFile);
            Node nodes[] = new Node[roots.size()];
            int selRoot = -1;
            try {
                ListIterator iter = roots.listIterator();
                while (iter.hasNext()) {
                    FileObject root = (FileObject)iter.next();
                    DataObject dob = DataObject.find(root);
                    Project owner = FileOwnerQuery.getOwner(root);
                    final String displayName = rootDisplayName(root, owner, owner != project);
                    nodes[iter.previousIndex()] = new RootNode(dob.getNodeDelegate(), displayName);
                }
            } catch (DataObjectNotFoundException donfex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, donfex);
                return null;
            }
            Children children = new Children.Array();
            children.add(nodes);
            final AbstractNode root = new AbstractNode(children);
            root.setIconBase("org/netbeans/modules/form/editors2/iconResourceRoot"); // NOI18N
            root.setDisplayName(getString("CTL_ClassPathName")); // NOI18N
                            
            ResourceSelector selector = new ResourceSelector(root);
            DialogDescriptor dd = new DialogDescriptor(selector, getString("CTL_OpenDialogName")); // NOI18N
            Object res = DialogDisplayer.getDefault().notify(dd);
            nodes = (res == DialogDescriptor.OK_OPTION) ? selector.getNodes() : null;
            String name = null;
            if ((nodes != null) && (nodes.length == 1)) {
                DataObject dob = (DataObject)nodes[0].getCookie(DataObject.class);
                if (dob != null) {
                    FileObject fob = dob.getPrimaryFile();
                    if (fob != null) {                        
                        if (executeClassPath.contains(fob)) {
                            name = executeClassPath.getResourceName(fob);
                        } else {
                            ClassPath sourceClassPath = ClassPath.getClassPath(fob, ClassPath.SOURCE);
                            name = sourceClassPath.getResourceName(fob);
                        }
                    }
                }
            }
            return name;
        }
        
        private String rootDisplayName(FileObject fo, Project owner, boolean withProjectName) {
            if (owner != null) {
                SourceGroup grp = sourceGroup(fo, owner);
                String name = (grp!=null) ? grp.getDisplayName() : FileUtil.getFileDisplayName(fo);
                if (withProjectName) {
                    ProjectInformation pi = (ProjectInformation)owner.getLookup().lookup(ProjectInformation.class);
                    if (pi != null) name  += " ["+pi.getDisplayName()+"]"; // NOI18N
                }
                return name;
            } else
                return FileUtil.getFileDisplayName(fo);
        }
        
        private SourceGroup sourceGroup(FileObject file, Project prj) {
            Sources src = ProjectUtils.getSources(prj);
            SourceGroup[] srcgrps = src.getSourceGroups("java"); // NOI18N
            for (int i = 0 ; i < srcgrps.length; i++) {
                if (file == srcgrps[i].getRootFolder())
                    return srcgrps[i];
            }
            return null;
        }
        
        private Node findSelectedNode(Node root, String name) {
            Node node = root;
            StringTokenizer st = new StringTokenizer(name, "/"); // NOI18N
            while (st.hasMoreTokens()) {
                Children children = node.getChildren();
                String subName = st.nextToken();
                Node nextNode = children.findChild(subName);
                // Last component => try to remove prefix
                if ((nextNode == null) && !st.hasMoreTokens()) {
                    int index = subName.lastIndexOf('.');
                    if (index != -1) {
                        subName = subName.substring(0, index);
                        nextNode = children.findChild(subName);
                    }
                }
                if (nextNode == null) {
                    break;
                } else {
                    node = nextNode;
                }
            }
            return node;
        }
        
        /**
         * Returns the property value that is result of the CustomPropertyEditor.
         *
         * @return the property value that is result of the CustomPropertyEditor.
         * @exception InvalidStateException when the custom property editor does not
         * represent valid property value (and thus it should not be set)
         */
        public Object getPropertyValue() throws IllegalStateException {
            NbImageIcon ii = null;
            String s = tfName.getText().trim();
            
            if ((s == null)|| ("".equals(s)) || ("/".equals(s)) || // NOI18N
            ("///".equals(s)) || (s.endsWith("#"))) {          // NOI18N
                // #13035
                // the empty string and couple of others has to be treated specially
                // since TopManager.getDefault().currentClassLoader().getResource(s);
                // is able to return non null value for the . And that
                // wrong non-null value causes problems in new NbImageIcon(...)
                return null;
            }
            
            try {
                if (rbFile.isSelected()) {
                    ii = new NbImageIcon(s);
                    ii.type = TYPE_FILE;
                    ii.name = s;
                } else
                    if (rbClasspath.isSelected()) {
                        URL url = findResource(s);
                        ii = new NbImageIcon(url);
                        ii.type = TYPE_CLASSPATH;
                        ii.name = s;
                    } else
                        if (rbUrl.isSelected()) {
                            URL url = new URL(s);
                            ii = new NbImageIcon(url);
                            ii.type = TYPE_URL;
                            ii.name = s;
                        }
            } catch (Exception e) {
                IllegalStateException ise = new IllegalStateException(e.toString());
                ErrorManager.getDefault().annotate(ise, ErrorManager.USER, getString("MSG_IllegalValue"), getString("MSG_IllegalValue"), null, new Date()); // NOI18N
                throw ise;
            }
            return ii;
        }
        
        void updateIcon() {
            IconEditor.this.setValue(localIcon);
            iconLabel.setIcon((localIcon == null) ? null : localIcon.icon);
            iconLabel.setEnabled(localIcon != null);
            validate();
        }
        
        void setValue() {
            String val = tfName.getText();
            val.trim();
            if ("".equals(val)) { // NOI18N
                localIcon = null;
                updateIcon();
                return;
            }
            
            String pref = "";   // NOI18N
            if (rbUrl.isSelected()) pref = URL_PREFIX + ": "; // NOI18N
            else
                if (rbFile.isSelected()) pref = FILE_PREFIX + ": "; // NOI18N
                else
                    if (rbClasspath.isSelected()) pref = CLASSPATH_PREFIX + ": "; // NOI18N
            try {
                localIcon = iconFromText(pref + val);
            } catch (IllegalArgumentException ee) {
                localIcon = null;
            }
            updateIcon();
        }
        
    } // end of IconPanel
    
    private static class RootNode extends FilterNode {
        
        RootNode(Node node, String displayName) {
            super(node);
            if (displayName != null) {
                disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME);
                setDisplayName(displayName);
            }
        }
                
    }
    
    private static class ResourceSelector extends JPanel implements ExplorerManager.Provider {
        /** Manages the tree. */
        private ExplorerManager manager = new ExplorerManager();
                
        public ResourceSelector(Node root) {
            ResourceBundle bundle = NbBundle.getBundle(ResourceSelector.class);
            
            setLayout(new BorderLayout(0, 5));
            setBorder(new EmptyBorder(12, 12, 0, 11));
            getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ResourceSelector")); // NOI18N
            getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ResourceSelector")); // NOI18N
            manager.setRootContext(root);
            
            BeanTreeView tree = new BeanTreeView();
            tree.setPopupAllowed(false);
            tree.setDefaultActionAllowed(false);
            // install proper border for tree
            tree.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N
            tree.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ResourceSelectorView")); // NOI18N
            tree.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ResourceSelectorView")); // NOI18N
            add(tree, BorderLayout.CENTER);
        }
        
        /**
         * Gets preferred size. Overrides superclass method.
         * Height is adjusted to 1/2 screen.
         */
        public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            dim.height = Math.max(dim.height, org.openide.util.Utilities.getUsableScreenBounds().height / 2);
            return dim;
        }
        
        /**
         * @return selected nodes
         */
        public Node[] getNodes() {
            return manager.getSelectedNodes();
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
    }
        
    // XMLPropertyEditor implementation ...........................................................
    
    /** Root of the XML representation of the icon. */
    public static final String XML_IMAGE = "Image"; // NOI18N
    
    /** Attribute holding icon type. */
    public static final String ATTR_TYPE = "iconType"; // NOI18N
    /** Attribute holding icon name. */
    public static final String ATTR_NAME = "name"; // NOI18N
    
    public void readFromXML(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_IMAGE.equals(element.getNodeName())) {
            throw new java.io.IOException();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        try {
            int type = Integer.parseInt(attributes.getNamedItem(ATTR_TYPE).getNodeValue());
            String name = attributes.getNamedItem(ATTR_NAME).getNodeValue();
            switch (type) {
                case 0: setValue(null); break;
                case TYPE_URL: setAsText(URL_PREFIX + ": " + name); break; // NOI18N
                case TYPE_FILE: setAsText(FILE_PREFIX + ": " + name); break; // NOI18N
                case TYPE_CLASSPATH: setAsText(CLASSPATH_PREFIX + ": " + name); break; // NOI18N
            }
        } catch (NullPointerException e) {
            java.io.IOException ioe = new java.io.IOException();
            ErrorManager.getDefault().annotate(ioe, e);
            throw ioe;
        }
    }
    
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement(XML_IMAGE);
        if (getValue() instanceof NbImageIcon) {
            NbImageIcon ii = (NbImageIcon)getValue();
            el.setAttribute(ATTR_TYPE, Integer.toString(ii.type));
            el.setAttribute(ATTR_NAME, ii.name);
        } else {
            el.setAttribute(ATTR_TYPE, "0"); // NOI18N
            el.setAttribute(ATTR_NAME, "null"); // NOI18N
        }
        return el;
    }
    
    // ExPropertyEditor implementation
    
    public void attachEnv(PropertyEnv env) {
        propertyEnv = env;
    }
    
}
