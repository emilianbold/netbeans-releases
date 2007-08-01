/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.WeakHashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import com.sun.java.swing.plaf.windows.WindowsFileChooserUI;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import java.util.ResourceBundle;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.nodes.Node;
import org.openide.ErrorManager;

/**
 * @deprecated
 */
public class StandardUrlPanel extends JPanel implements PropertyChangeListener, ActionListener,
    ChangeListener, EnhancedCustomPropertyEditor, DocumentListener {

    static String copyString = "Copy"; //NOI18N
    static String linkString = "Link"; //NOI18N
    protected static WeakHashMap lastDirectoryByProject = new WeakHashMap();
    protected static final String LAST_DIRECTORY_KEY_NO_PROJECT = "DEFAULT"; // NOI18N

    private static final ResourceBundle bundle = 
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle");

    JTabbedPane tabs = new JTabbedPane();

    JFileChooser filePanel;
    HTTPPanel httpPanel;

    JTextField valueTextField = new JTextField();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel valueLabel = new JLabel();

    JRadioButton copyButton = new JRadioButton(bundle.getString("copy")); //NOI18N
    JRadioButton linkButton = new JRadioButton(bundle.getString("link")); //NOI18N
    

    ButtonGroup group = new ButtonGroup();

    RadioListener myListener = new RadioListener();

    private boolean initialized = false;

    /** The DesignProperty for the property that is being edited */
    protected DesignProperty liveProperty;
    protected DesignContext liveContext;

    protected Node.Property property;

    /** Opening mode.*/
    // private int mode = JFileChooser.FILES_ONLY;

    /** Filter for files to show. */
    // private javax.swing.filechooser.FileFilter fileFilter;

    /** Base directory to which to show relative path, if is set. */
    private File baseDirectory;

    public StandardUrlPanel() {
        try {
            jbInit();
            copyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(StandardUrlPanel.class, "COPY_BUTTON_ACCESS_DESC"));
            linkButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(StandardUrlPanel.class, "LINK_BUTTON_ACCESS_DESC"));
            copyButton.setMnemonic(org.openide.util.NbBundle.getMessage(StandardUrlPanel.class, "COPY_BUTTON_MNEMONIC").charAt(0));
            linkButton.setMnemonic(org.openide.util.NbBundle.getMessage(StandardUrlPanel.class, "LINK_BUTTON_MNEMONIC").charAt(0));
            valueTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(StandardUrlPanel.class, "VALUE_TEXTFIELD_ACCESS_NAME"));
            valueTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(StandardUrlPanel.class, "VALUE_TEXTFIELD_ACCESS_DESC"));
            tabs.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(StandardUrlPanel.class, "TAB_PANE_ACCESS_NAME"));
            tabs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(StandardUrlPanel.class, "TAB_PANE_ACCESS_DESC"));            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Image loadImage(final String resourceName, final Class clazz) {

        try {
            java.awt.image.ImageProducer ip = (java.awt.image.ImageProducer)
                java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                public Object run() {
                    java.net.URL url;
                    if ((url = clazz.getResource(resourceName)) == null) {
                        return null;
                    } else {
                        try {
                            return url.getContent();
                        } catch (java.io.IOException ioe) {
                            return null;
                        }
                    }
                }
            });

            if (ip == null) {
                return null;
            }
            java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
            return tk.createImage(ip);
        } catch (Exception ex) {
            return null;
        }
    }

    private void jbInit() throws Exception {

        copyButton.setActionCommand(copyString);
        linkButton.setActionCommand(linkString);

        group.add(copyButton);
        group.add(linkButton);
        linkButton.setSelected(true);

        copyButton.addActionListener(myListener);
        linkButton.addActionListener(myListener);

        this.setLayout(gridBagLayout1);
        valueLabel.setText(bundle.getString("urlPanelCurrSetting")); //NOI18N
        valueTextField.setText(""); //NOI18N
        this.add(valueLabel,
            new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
            GridBagConstraints.HORIZONTAL, new Insets(8, 8, 2, 8), 0, 0));
        this.add(valueTextField,
            new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 8, 4, 8), 0, 0));
        this.add(tabs,
            new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));
        this.add(copyButton,
            new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        this.add(linkButton,
            new GridBagConstraints(GridBagConstraints.RELATIVE, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }

    protected File getLastDirectoryUsed() {

        Object key;
        if (getDesignProperty() == null) {
            key = LAST_DIRECTORY_KEY_NO_PROJECT;
        } else {
            key = getDesignProperty().getDesignBean().getDesignContext().getProject();
        }
        File dir = (File)lastDirectoryByProject.get(key);
        if (dir == null) {
            dir = getRelativeRootDirectory();
            lastDirectoryByProject.put(key, dir);
        }
        return dir;
    }

    protected void setLastDirectoryUsed(File dir) {

        Object key;
        if (getDesignProperty() == null) {
            key = LAST_DIRECTORY_KEY_NO_PROJECT;
        } else {
            key = getDesignProperty().getDesignBean().getDesignContext().getProject();
        }
        lastDirectoryByProject.put(key, dir);
    }

    /**
     * Specified by PropertyChangeListener, for the JFileChooser
     */
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getSource() == filePanel) {
            File file = filePanel.getSelectedFile();
            if (file == null) {
                file = filePanel.getCurrentDirectory();
            }
            processValueFile(file);
            ignoreValueTextFieldChanges = true;
            try {
                valueTextField.setText(propertyValue);
            } finally {
                ignoreValueTextFieldChanges = false;
            }
        }
    }

    /**
     * Specified by ActionListener, for the HTTPPanel TextField
     */
    public void actionPerformed(ActionEvent event) {

        if (event.getSource() == shortCutPanelMyProjectButton) {
            filePanel.setCurrentDirectory(getRelativeRootDirectory());
            return;
        }
    }

    /**
     * Specified by ChangeListener, for the tabs
     */
    public void stateChanged(ChangeEvent evt) {

    }

    protected DesignProperty getDesignProperty() {

        return liveProperty;
    }

    public void setDesignProperty(DesignProperty prop) {

        this.liveProperty = prop;
        if (prop != null) {
            this.liveContext = prop.getDesignBean().getDesignContext();
        }
    }

    public void setProperty(Node.Property prop) {

        this.property = prop;
    }

    public void setDesignContext(DesignContext context) {

        this.liveContext = context;
    }

    public void changedUpdate(DocumentEvent event) {

        if (event.getDocument() == httpPanel.textField.getDocument()) {
            httpTextFieldChanged();
        }
        if (event.getDocument() == valueTextField.getDocument()) {
            valueTextFieldChanged();
        }
    }

    public void insertUpdate(DocumentEvent event) {

        if (event.getDocument() == httpPanel.textField.getDocument()) {
            httpTextFieldChanged();
        }
        if (event.getDocument() == valueTextField.getDocument()) {
            valueTextFieldChanged();
        }
    }

    public void removeUpdate(DocumentEvent event) {

        if (event.getDocument() == httpPanel.textField.getDocument()) {
            httpTextFieldChanged();
        }
        if (event.getDocument() == valueTextField.getDocument()) {
            valueTextFieldChanged();
        }
    }

    public void httpTextFieldChanged() {

        processValueUri(httpPanel.textField.getText());
        ignoreValueTextFieldChanges = true;
        try {
            valueTextField.setText(propertyValue);
        } finally {
            ignoreValueTextFieldChanges = false;
        }
    }

    protected boolean ignoreValueTextFieldChanges;

    public void valueTextFieldChanged() {

        if (ignoreValueTextFieldChanges) {
            return;
        }
        processValueString(valueTextField.getText());
    }

    // Set the initial Property value to show in the Editor

    public void initialize() {

        if (!initialized) {
            filePanel = org.netbeans.modules.visualweb.extension.openide.awt.JFileChooser_RAVE.getJFileChooser();
            if (filePanel.getUI() instanceof WindowsFileChooserUI) {
                // IF anything goes wrong just ignore it and hope all goes well
                try {
                    tweakWindowsFileChooserUI(filePanel, (WindowsFileChooserUI)filePanel.getUI());
                } catch (Throwable t) {
//                    t.printStackTrace();
                }
            }
            filePanel.setControlButtonsAreShown(false);
            filePanel.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
//            filePanel.addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY, this);
            filePanel.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, this);
//            filePanel.addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY, this);
            tabs.add(filePanel, bundle.getString("file")); //NOI18N

            httpPanel = new HTTPPanel();
            tabs.add(httpPanel, bundle.getString("url")); //NOI18N

            tabs.addChangeListener(this);
            // Now initialize the panel with the current value

            String original = null;
            if (liveProperty == null) {
            	if (property != null) {
	                try {
	                    original = (String)property.getValue();
	                } catch (Exception e) {
	                    throw new RuntimeException(e);
	                }
            	}
            } else {
                original = (String)liveProperty.getValue();
            }

            if (original == null || original.trim().length() == 0) {
                tabs.setSelectedComponent(filePanel);
            } else {
                tabs.setSelectedComponent(httpPanel);
            }
            httpPanel.textField.setText(original);
            httpTextFieldChanged();

            valueTextField.getDocument().addDocumentListener(this);
            httpPanel.textField.getDocument().addDocumentListener(this);

            File dir = getLastDirectoryUsed();
            filePanel.setCurrentDirectory(dir);
            initialized = true;
        }
    }

    JToggleButton shortCutPanelMyProjectButton;

    protected void tweakWindowsFileChooserUI(JFileChooser fileChooser, WindowsFileChooserUI ui) {

        File projectDirectory = getRelativeRootDirectory();
        if (projectDirectory == null) {
            return;
        }
        JToolBar shortCutPanel;
        try {
            Field shortCutPanelField = WindowsFileChooserUI.class.getDeclaredField("shortCutPanel"); // NOI18N
            boolean oldAccessibleState = shortCutPanelField.isAccessible();
            shortCutPanelField.setAccessible(true);
            shortCutPanel = (JToolBar)shortCutPanelField.get(ui);
            shortCutPanelField.setAccessible(false);
        } catch (Exception e) {
            // we can't do it for some reason, but its not important
            return;
        }
        if (shortCutPanel == null) {
            return;
        }
        FileSystemView fsv = fileChooser.getFileSystemView();
        if (fsv.isFileSystemRoot(projectDirectory)) {
            // Create special File wrapper for drive path
            projectDirectory = fsv.createFileObject(projectDirectory.getAbsolutePath());
        }
        String folderName = fsv.getSystemDisplayName(projectDirectory);
        int index = folderName.lastIndexOf(File.separatorChar);
        if (index >= 0 && index < folderName.length() - 1) {
            folderName = folderName.substring(index + 1);
        }
        boolean xp = false;
        try {
            Class clazz = Class.forName("com.sun.java.swing.plaf.windows.XPStyle"); // NOI18N
            Method method = clazz.getDeclaredMethod("getXP", new Class[0]); // NOI18N
            method.setAccessible(true);
            Object object = method.invoke(null, new Object[0]);
            method.setAccessible(false);
            xp = object != null;
        } catch (Exception e) {
            //		    e.printStackTrace();
        }
        Icon icon = null;
        if (xp) {
            Image image = loadImage("urlpanel_myproject.png", getClass()); //NOI18N
            icon = new ImageIcon(image, bundle.getString("myProject")); //NOI18N
        }
        if (icon == null) {
            icon = fsv.getSystemIcon(projectDirectory);
            folderName = bundle.getString("myProject");
        }
        final Dimension buttonSize = new Dimension(83, xp ? 69 : 54);
        shortCutPanelMyProjectButton = new JToggleButton(folderName, icon);
        if (xp) {
            shortCutPanelMyProjectButton.setIconTextGap(2);
            shortCutPanelMyProjectButton.setMargin(new Insets(2, 2, 2, 2));
            shortCutPanelMyProjectButton.setText("<html><center>" + // NOI18N
                bundle.getString("myProject") + //NOI18N
                "</center></html>"); // NOI18N
        } else {
            Color fgColor = new Color(UIManager.getColor("List.selectionForeground").getRGB()); // NOI18N
            shortCutPanelMyProjectButton.setBackground(fileChooser.getBackground());
            shortCutPanelMyProjectButton.setForeground(fileChooser.getForeground());
        }
        shortCutPanelMyProjectButton.setHorizontalTextPosition(JToggleButton.CENTER);
        shortCutPanelMyProjectButton.setVerticalTextPosition(JToggleButton.BOTTOM);
        shortCutPanelMyProjectButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        shortCutPanelMyProjectButton.setPreferredSize(buttonSize);
       // shortCutPanelMyProjectButton.setMaximumSize(buttonSize);
        shortCutPanelMyProjectButton.addActionListener(this);
        shortCutPanel.add(shortCutPanelMyProjectButton, 0);
        shortCutPanel.add(Box.createRigidArea(new Dimension(1, 1)), 1);
        Component components[] = shortCutPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JToggleButton) {
                JToggleButton button = (JToggleButton)components[i];
                ButtonModel buttonModel = button.getModel();
                try {
                    Field field = buttonModel.getClass().getSuperclass().getDeclaredField("group"); // NOI18N
                    field.setAccessible(true);
                    ButtonGroup group = (ButtonGroup)field.get(buttonModel);
                    field.setAccessible(false);
                    if (group != null) {
                        group.add(shortCutPanelMyProjectButton);
                        break;
                    }
                } catch (Exception e) {
                    //	                e.printStackTrace();
                }
            }
        }
    }

    class HTTPPanel extends JPanel {

        JLabel httpLabel = new JLabel();
        JTextField textField = new JTextField();

        // Constructor for inner class
        HTTPPanel() {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            httpLabel.setText(bundle.getString("enterUrlHttp")); //NOI18N
            httpLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
            this.add(httpLabel);
            textField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
              textField.getPreferredSize().height));
            textField.setAlignmentX(JTextField.LEFT_ALIGNMENT);
            textField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(StandardUrlPanel.class, "URL_TEXTFIELD_ACCESS_NAME"));
            textField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(StandardUrlPanel.class, "URL_TEXTFIELD_ACCESS_DESC"));
            this.add(textField);
        }
    }

    // Called when the user enters or selects a file url

    class RadioListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
        }
    }

    protected File relativeRootDirectoryCache;
    protected boolean relativeRootDirectoryCacheSet;

    /**
     * Return null if I can't figure it out.
     * @return
     */
    protected File getRelativeRootDirectory() {

        if (!relativeRootDirectoryCacheSet) {
            relativeRootDirectoryCache = getRelativeRootDirectoryImp();
            relativeRootDirectoryCacheSet = true;
            if (relativeDirectoryCache != null && relativeRootDirectoryCache != null) {
                // If root and relative are same path, then make them identical objects
                if (relativeDirectoryCache.getPath().equals(relativeRootDirectoryCache.getPath())) {
                    relativeRootDirectoryCache = relativeDirectoryCache;
                }
            }
        }
        return relativeRootDirectoryCache;
    }

    protected File relativeDirectoryCache;
    protected boolean relativeDirectoryCacheSet;

    /**
     * Return null if I can't figure it out.
     * @return
     */
    protected File getRelativeDirectory() {

        if (!relativeDirectoryCacheSet) {
            relativeDirectoryCache = getRelativeDirectoryImp();
            relativeDirectoryCacheSet = true;
        }
        return relativeDirectoryCache;
    }

    protected File getFileFromUrl(URL url) {

        File result = null;
        if (url != null && "file".equals(url.getProtocol())) { // NOI18N
            if (url.getAuthority() == null) {
                result = new File(url.getPath());
            } else {
                result = new File(url.getAuthority(), url.getPath());
            }
        }
        if (result != null && result.isFile()) {
            result = result.getParentFile();
        }
        return result;
    }

    protected File getRelativeRootDirectoryImp() {

        /*
         * I need to be able to figure out
         * 1) if a file is contained withint a project's web tree
         * 2) if contained within a project's web tree, give me its relative URI
         *
         *  HACK
         * Here I am doing 1) in a VERY HACKED way.
         * I end up doing 2) by combining the result of this with getRelativeDirectory().
         */
        File rootFile = getRelativeDirectory();
        if (rootFile == null) {
            return null;
        }
        try {
            while (rootFile != null) {
                boolean isProjectRoot = isProjectRootFolder(rootFile);
                if (isProjectRoot) {
                    break;
                }
                rootFile = rootFile.getParentFile();
            }
            if (rootFile == null) {
                return getRelativeDirectory();
            }
            if (liveContext == null) {
                // HACK BAD - what happens if src not proper folder
                rootFile = new File(rootFile, "src/web"); // NOI18N
            } else {
                // HACK
                String sourcePath = (String) liveContext.getProject().getProjectData("sourceRoot"); // NOI18N
                rootFile = new File(rootFile, sourcePath);
                rootFile = new File(rootFile, "web"); // NOI18N
            }
            return rootFile;
        } catch (Exception e) {
            return getRelativeDirectory();
        }
    }

    /* EAT:
     *    Method cloned from com.sun.rave.project.model.Project
     *    HACK
     *  Determine whether this directory appears to be a project root folder
     *  @param f potential project folder
     *  @return true if the directory appears to contain "well known" project artifacts
     *  otherwise false.
     */
    public boolean isProjectRootFolder(File f) {
        final String PROJECT_DATA = "project-data"; // NOI18N
        final String FILE_EXTENSION = "prj"; // NOI18N
        final String PROJECT_FILE = "project." + FILE_EXTENSION; // NOI18N

        if (!f.isDirectory()) {
            return false;
        }
        File subF = new File(f, PROJECT_DATA);
        if (!subF.exists()) {
            return false;
        }
        subF = new File(subF, PROJECT_FILE);
        if (!subF.exists()) {
            return false;
        }
        return true;
    }

    public void setRelativeRootDirectory(File file) {

        relativeRootDirectoryCache = file;
        relativeRootDirectoryCacheSet = true;
    }

    protected File getRelativeDirectoryImp() {

        if (liveContext == null) {
            return null;
        }
        URL url = liveContext.resolveResource(""); // NOI18N
        File contextFile = getFileFromUrl(url);
        try {
            contextFile = contextFile.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
        return contextFile;
    }

    public void setRelativeDirectory(File file) {

        relativeDirectoryCache = file;
        if (file != null && file.isFile()) {
            relativeDirectoryCache = relativeDirectoryCache.getParentFile();
        }
        relativeDirectoryCacheSet = true;
    }

    public Object getPropertyValue() throws IllegalStateException {

        try {
            Object result = getPropertyValueImp();
            return result;
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                bundle.getString("urlPanelErroDialogTitle"), //NOI18N
                JOptionPane.WARNING_MESSAGE);
            throw e;
        }
    }

    protected String propertyValue;

    protected Object getPropertyValueImp() {

        if (delayedFileToAdd != null) {
            if (!delayedFileToAdd.exists()) {
                throw new IllegalStateException(bundle.getString("fileSpecifiedNotExist")); //NOI18N
            }
            try {
                // XXX #6336303 Making image (and also Css style) property context-relative,
                // i.e. all such files are added into resources.
                // TODO Missing convenient API(designtime/insync) to add context-relative resources.
                // For now basically copied the impl from UrlPropertyPanel -> propertyeditors).
                DesignProject designProject = liveContext.getProject();
                URI uri = new URI("web/resources/" + encodeUrl(delayedFileToAdd.getName())); // NOI18N
                if(designProject.getResourceFile(uri) == null) {
                    designProject.addResource(delayedFileToAdd.toURI().toURL(), uri);
                }
                propertyValue = "/resources/" + delayedFileToAdd.getName(); // NOI18N
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            } catch (URISyntaxException use) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, use);
            }
        }
        if (delayedSetLastDirectoryUsed != null) {
            setLastDirectoryUsed(delayedSetLastDirectoryUsed);
        }
        return propertyValue;
    }

    // XXX Copied from com.sun.rave.propertyeditors.UrlPropertyEditor!
    /**
     * Convert a file system path to a URL by converting unsafe characters into
     * numeric character entity references. The unsafe characters are listed in
     * in the IETF specification of URLs
     * (<a href="http://www.ietf.org/rfc/rfc1738.txt">RFC 1738</a>). Safe URL
     * characters are all printable ASCII characters, with the exception of the
     * space characters, '#', <', '>', '%', '[', ']', '{', '}', and '~'. This
     * method differs from {@link java.net.URLEncoder.encode(String)}, in that
     * it is intended for encoding the path portion of a URL, not the query
     * string.
     */
    private static String encodeUrl(String url) {
        if (url == null || url.length() == 0)
            return url;
        StringBuffer buffer = new StringBuffer();
        String anchor = null;
        int index = url.lastIndexOf('#');
        if (index > 0) {
            anchor = url.substring(index + 1);
            url = url.substring(0, index);
        }
        char[] chars = url.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] <= '\u0020') {
                buffer.append('%');
                buffer.append(Integer.toHexString((int) chars[i]));
            } else {
                switch(chars[i]) {
                    case '\u0009': // Tab
                        buffer.append("%09");
                        break;
                    case '\u0020': // Space
                        buffer.append("%20");
                        break;
                    case '#':
                        buffer.append("%23");
                        break;
                    case '%':
                        buffer.append("%25");
                        break;
                    case '<':
                        buffer.append("%3C");
                        break;
                    case '>':
                        buffer.append("%3E");
                        break;
                    case '[':
                        buffer.append("%5B");
                        break;
                    case ']':
                        buffer.append("%5D");
                        break;
                    case '{':
                        buffer.append("%7B");
                        break;
                    case '}':
                        buffer.append("%7D");
                        break;
                    case '~':
                        buffer.append("%7E");
                        break;
                    default:
                        buffer.append(chars[i]);
                }
            }
        }
        if (anchor != null) {
            buffer.append('#');
            buffer.append(anchor);
        }
        if (buffer.length() == url.length())
            return url;
        return buffer.toString();
    }
    
    
    protected File delayedFileToAdd;
    protected File delayedSetLastDirectoryUsed;

    protected void processValueFile(File file) {

        if (file.isAbsolute()) {
            try {
                file = file.getCanonicalFile();
                String fileAbsolutePath = file.toURI().toString();
                String rootAbsolutePath;
                if (getRelativeRootDirectory() == null) {
                    rootAbsolutePath = null;
                } else {
                    rootAbsolutePath = getRelativeRootDirectory().toURI().toString();
                }
                if (rootAbsolutePath != null && fileAbsolutePath.startsWith(rootAbsolutePath)) {
                    // file is inside project tree
                    linkButton.setSelected(true);
                    linkButton.setEnabled(true);
                    copyButton.setEnabled(false);
                    delayedFileToAdd = null;
                    delayedSetLastDirectoryUsed = file.getParentFile();
                    propertyValue = fileAbsolutePath.substring(rootAbsolutePath.length());
                    if (getRelativeRootDirectory() != getRelativeDirectory()) {
                        // make URI relative to directory in which the doc being modified is in
                        File fileDir = file;
                        if (file.isFile()) {
                            fileDir = file.getParentFile();
                        }
                        ArrayList filePathList = getPathList(fileDir, getRelativeRootDirectory());
                        ArrayList relativePathList = getPathList(getRelativeDirectory(),
                            getRelativeRootDirectory());
                        int index = 0;
                        // find the first non matching sub dir
                        for (; index < filePathList.size() && index < relativePathList.size();
                            index++) {
                            if (!filePathList.get(index).equals(relativePathList.get(index))) {
                                break;
                            }
                        }
                        StringBuffer stringBuffer = new StringBuffer();
                        // create a file that goes up to match found
                        for (int i = index; i < relativePathList.size(); i++) {
                            stringBuffer.append("../"); // NOI18N
                        }
                        // create a file that goes down from match found
                        for (int i = index; i < filePathList.size(); i++) {
                            stringBuffer.append(filePathList.get(i));
                            stringBuffer.append("/"); // NOI18N
                        }
                        if (file.isFile()) {
                            stringBuffer.append(file.getName());
                        }
                        propertyValue = stringBuffer.toString();
                    }
                    return;
                } else {
                    // file is somewhere else on local drive
                    copyButton.setSelected(true);
                    copyButton.setEnabled(true);
                    linkButton.setEnabled(false);
                    delayedFileToAdd = file;
                    delayedSetLastDirectoryUsed = file.getParentFile();
                    propertyValue = file.getPath();
                    return;
                }
            } catch (IOException e) {
            }
        }
        processValueUri(file.getPath());
    }

    /*
     * Return list of sub dirs between from and to.
     * Assume from is inside to.
     */
    protected ArrayList getPathList(File from, File to) {

        String toPath = to.getPath();
        ArrayList result = new ArrayList();
        while (!from.getPath().equals(toPath)) {
            File parent = from.getParentFile();
            String subDir = from.getPath().substring(parent.getPath().length() +
                File.separator.length());
            result.add(subDir);
            from = parent;
        }
        Collections.reverse(result);
        return result;
    }

    protected void processValueString(String valueString) {

        File file = new File(valueString);
        // can we treat valueString a file ?
        try {
            // is it a value file specification
            // dont keep the value since getCanonicalPath() forces an absolute path
            file.getCanonicalPath();
            // will only fall through if the file name was ok
            processValueFile(file);
            return;
        } catch (IOException e) {
        }
        processValueUri(valueString);
    }

    protected void processValueUri(String uriString) {

        delayedFileToAdd = null;
        delayedSetLastDirectoryUsed = null;
        linkButton.setSelected(true);
        linkButton.setEnabled(true);
        copyButton.setEnabled(false);
        propertyValue = uriString.replace('\\','/');
    }

    public void customizerApply() {

        liveProperty.setValue(getPropertyValue());
    }
 
    /**
     * Override addNotify in order to set foucs on the text field.
     * Before calling requestFocusInWindow() on the field, we have to
     * make sure that the field is displayable. When super.addNotify()
     * returns, we know that a corresponding peer object is created,
     * so it is safe to set focus then.
     */ 
    public void addNotify() {
        super.addNotify();        
        // Set focus on the text field for accessibility.
        valueTextField.requestFocusInWindow();
    }
}
