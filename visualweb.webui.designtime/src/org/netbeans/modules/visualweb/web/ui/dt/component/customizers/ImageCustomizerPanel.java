/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.web.ui.dt.component.customizers;

import org.netbeans.modules.visualweb.propertyeditors.UrlPropertyEditor;
import com.sun.rave.web.ui.component.ImageHyperlink;
import java.io.IOException;
import java.net.URI;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.io.File;
import java.net.URL;
import javax.faces.context.FacesContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.propertyeditors.domains.Element;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProject;
import org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.ThemeIconsDomain;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;


/**
 *
 * @author  jhoff, Gowri
 */
public class ImageCustomizerPanel extends javax.swing.JPanel implements PropertyChangeListener {
    ImageIcon thumbnail = null; // for showing preview
    File file = null; // file selected from file tab
    protected static final String LAST_DIRECTORY_KEY_NO_PROJECT = "DEFAULT"; // NOI18N
    protected static WeakHashMap lastDirectoryByProject = new WeakHashMap();
    private String tabName = DesignMessageUtil.getMessage(ImageCustomizer.class, "FileTab"); // NOI18N
    private DesignBean designBean;
    private DesignContext designContext;
    private DesignProperty property;
    private FacesContext facesContext;
    private static ArrayList urlArray = new ArrayList();


    /** Creates new form ImageCustomizerPanel */
    public ImageCustomizerPanel(DesignBean designBean) {
        this.designBean = designBean;
        designContext = designBean.getDesignContext();
        initComponents();
        //imagePreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/visualweb/web/ui/dt/component/customizers/javalogo-color.gif")));
        describeThemeIcon.setFont(lblIconIdentifier.getFont());
        iconList.setSelectedIndex(0);
        iconList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listItemValueChanged(evt);
            }
        });
        imageFileChooser.setFileFilter(new ImageFilter());
        cbxExpression.addItem("<" + DesignMessageUtil.getMessage(ImageCustomizer.class, "ComboMessage") + ">"); // NOI18N
        fillURLComboBox();
        cbxExpression.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                urlLoadImage();
                cbxExpression.grabFocus();
            }

        });
        imageFileChooser.addPropertyChangeListener(this);
        File dir = getLastDirectoryUsed();
        imageFileChooser.setCurrentDirectory(dir);
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                urlLoadImage();
            }
        });
        fillList();
        fillCustomizer();
        rbFilePanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showImage();
            }
        });
        rbURLPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showImage();
            }
        });
        rbIconPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showImage();
            }
        });
    }


    /**
     * This is to fill the combobox with previously entered urls
     *
     */
    
    
    public void fillURLComboBox() {
        int size = urlArray.size();
        for(int i=0; i<size; i++) {
            if (!urlArray.get(i).equals("<" + DesignMessageUtil.getMessage(ImageCustomizer.class, "ComboMessage") + ">")) { // NOI18N
                cbxExpression.addItem(urlArray.get(i));
            }
        }
    }
    
    
    
    /**
     * This method gets called to show the image preview
     * when the user switches tabs.
     *
     */
    
    
    public void showImage() {
        if (rbFilePanel.isSelected()) {
            if (imageFileChooser.getSelectedFile() != null) {
                file = imageFileChooser.getSelectedFile();
                if (file.getPath().startsWith("/resources")) { // NOI18N
                    try {
                        File f = designContext.getProject().getResourceFile(new URI("web/resources/" + file.getName()));
                        if (f == null) {
                            imagePreview.setIcon(null);
                            lblImagePath.setText(DesignMessageUtil.getMessage(ImageCustomizer.class, "FileError")); // NOI18N
                        } else {
                            if (f.exists()) {
                                URL url = f.toURI().toURL();
                                lblImagePath.setText(url.getPath());
                                loadImage(new ImageIcon(url));
                            } else {
                                // Need to check if this else is necessary
                            }
                        }
                        imageFileChooser.setSelectedFile(new File(file.getPath()));
                        imageFileChooser.setCurrentDirectory(new File(file.getPath()));
                        repaint();
                    } catch (Exception e) {
                        
                    }
                } else if (file != null) {
                    loadImage(new ImageIcon(file.getPath()));
                    lblImagePath.setText(imageFileChooser.getSelectedFile().getPath());
                    repaint();
                }
            } else {
                file = null;
                imagePreview.setIcon(null);
                lblImagePath.setText("");
                
            }
            
        } else if (rbIconPanel.isSelected()) {
            if (iconList.getSelectedIndex() != -1) {
                String selected = iconList.getSelectedValue().toString();
                com.sun.rave.web.ui.component.Icon themeIcon = this.getThemeIcon((FacesDesignContext) designBean.getDesignContext(), selected);
                if( themeIcon != null ) {
                    try {
                        URL themeUrl = new URL(themeIcon.getUrl());
                        loadImage(new ImageIcon(themeUrl));
                        lblImagePath.setText(selected);
                    } catch(Exception e) {
                        
                    }
                }
            } else {
                imagePreview.setIcon(null);
                lblImagePath.setText("");
            }
        } else if (rbURLPanel.isSelected()) {
            cbxExpression.grabFocus();
            if (cbxExpression.getModel().getSize() != 1) {
                if (!cbxExpression.getSelectedItem().toString().equals("<" + DesignMessageUtil.getMessage(ImageCustomizer.class, "ComboMessage") + ">")) { // NOI18N
                    urlLoadImage();
                } else {
                    imagePreview.setIcon(null);
                    lblImagePath.setText("");
                }
            } else {
                imagePreview.setIcon(null);
                lblImagePath.setText("");
            }
        }
        
    }
    
    
    private void listItemValueChanged(javax.swing.event.ListSelectionEvent evt) {
        if (iconList.getSelectedIndex() != -1) {
            if (evt.getSource() != null) {
                tabName = DesignMessageUtil.getMessage(ImageCustomizer.class, "ThemeIconTab"); // NOI18N
                JList list = (JList)evt.getSource();
                String selected = iconList.getSelectedValue().toString();
                com.sun.rave.web.ui.component.Icon themeIcon = this.getThemeIcon((FacesDesignContext) designBean.getDesignContext(), selected);
                if( themeIcon != null ) {
                    try {
                        URL themeUrl = new URL(themeIcon.getUrl());
                        loadImage(new ImageIcon(themeUrl));
                        lblImagePath.setText(selected);
                    } catch(Exception e) {
                        
                    }
                }
            }
        }
    }
    
    /**
     * This fills the iconList with available ThemeIcons
     *
     */
    
    public void fillList() {
        ThemeIconsDomain themeIconsDomain = new ThemeIconsDomain();
        Element[] elements = themeIconsDomain.getElements();
        String[] listData = new String[elements.length];
        for(int i=0; i<elements.length; i++) {
            listData[i] = elements[i].getValue().toString();
        }
        iconList.setListData(listData);
    }
    
    /**
     *  This method is called to fill the fields with previously set values
     *  when the user invokes the customizer from Image/ImageHyperlink
     *  component's context menu.
     */
    
    public void fillCustomizer() {
        property = designBean.getProperty("icon"); // NOI18N
        String iconValue = (String)property.getValue();
        if (iconValue != null) {
            iconList.setSelectedValue(iconValue, true);
            lblImagePath.setText(iconValue);
            com.sun.rave.web.ui.component.Icon themeIcon = this.getThemeIcon((FacesDesignContext) designBean.getDesignContext(), iconValue);
            if( themeIcon != null ) {
                try {
                    URL themeUrl = new URL(themeIcon.getUrl());
                    CardLayout cl = (CardLayout)imagePanel.getLayout();
                    cl.show(imagePanel, "icon");
                    loadImage(new ImageIcon(themeUrl));
                    lblImagePath.setText(iconValue);
                    rbIconPanel.setSelected(true);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            
        }
        
        property = designBean.getProperty("imageURL");    // NOI18N
        try {
            if (property != null) {
                String strUrl = (String)property.getValue();
                
                if (strUrl != null) {
                    if (strUrl.startsWith("/resources")) { // NOI18N
                        File newFile = designContext.getProject().getResourceFile(new URI("web" + strUrl));
                        imageFileChooser.setSelectedFile(new File(strUrl));
                        imageFileChooser.setCurrentDirectory(new File(strUrl));
                        rbFilePanel.setSelected(true);
                        CardLayout cl = (CardLayout)imagePanel.getLayout();
                        cl.show(imagePanel, "file");
                        loadImage(new ImageIcon(newFile.toURI().toURL()));
                        lblImagePath.setText(strUrl);
                        repaint();
                    } else {
                        cbxExpression.setSelectedItem(strUrl);
                        URL url = new URL(strUrl);
                        rbURLPanel.setSelected(true);
                        CardLayout cl = (CardLayout)imagePanel.getLayout();
                        cl.show(imagePanel, "url");
                        
                        ImageIcon icon = new ImageIcon((Image)Toolkit.getDefaultToolkit().createImage(url));
                        lblImagePath.setText(strUrl);
                        loadImage(icon);
                        repaint();
                    }
                    
                }
            } else {
                property = designBean.getProperty("url"); // NOI18N
                String strUrl = (String)property.getValue();
                if (strUrl != null) {
                    if (strUrl.startsWith("/resources")) { // NOI18N
                        File newFile = designContext.getProject().getResourceFile(new URI("web" + strUrl));
                        imageFileChooser.setSelectedFile(newFile);
                        imageFileChooser.setCurrentDirectory(newFile);
                        rbFilePanel.setSelected(true);
                        CardLayout cl = (CardLayout)imagePanel.getLayout();
                        cl.show(imagePanel, "file");
                        loadImage(new ImageIcon(newFile.toURI().toURL()));
                        lblImagePath.setText(newFile.getPath());
                        repaint();
                    } else {
                        cbxExpression.setSelectedItem(strUrl);
                        URL url = new URL(strUrl);
                        rbURLPanel.setSelected(true);
                        CardLayout cl = (CardLayout)imagePanel.getLayout();
                        cl.show(imagePanel, "url");
                        
                        ImageIcon icon = new ImageIcon((Image)Toolkit.getDefaultToolkit().createImage(url));
                        lblImagePath.setText(strUrl);
                        loadImage(icon);
                        repaint();
                    }
                    
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * This code was taken from old Image Customizer.
     * Shows the recently used directory when the File option is selected.
     * Need to change this code as there is lot of hack.
     *
     */
    
    protected void setLastDirectoryUsed(File dir) {
        
        Object key;
        if (property == null) {
            key = LAST_DIRECTORY_KEY_NO_PROJECT;
        } else {
            key = property.getDesignBean().getDesignContext().getProject();
        }
        lastDirectoryByProject.put(key, dir);
    }
    
    
    
    protected File getLastDirectoryUsed() {
        
        Object key;
        if (designBean.getProperties() == null) {
            key = "DEFAULT";
        } else {
            key = designBean.getDesignContext().getProject();
        }
        File dir = (File)lastDirectoryByProject.get(key);
        if (dir == null) {
            dir = getRelativeRootDirectory();
            lastDirectoryByProject.put(key, dir);
        }
        return dir;
    }
    
    protected File relativeRootDirectoryCache;
    protected boolean relativeRootDirectoryCacheSet;
    
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
    
    protected File getRelativeRootDirectoryImp() {
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
            if (designContext == null) {
                rootFile = new File(rootFile, "src/web"); // NOI18N
            } else {
                String sourcePath = (String) designContext.getProject().getProjectData("sourceRoot"); // NOI18N
                rootFile = new File(rootFile, sourcePath);
                rootFile = new File(rootFile, "web"); // NOI18N
            }
            return rootFile;
        } catch (Exception e) {
            return getRelativeDirectory();
        }
    }
    
    
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
    
    protected File getRelativeDirectoryImp() {
        
        if (designContext == null) {
            return null;
        }
        URL url = designContext.resolveResource(""); // NOI18N
        File contextFile = getFileFromUrl(url);
        try {
            contextFile = contextFile.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
        return contextFile;
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
    
    
    /**
     * This method is called when the user fills the combobox with
     * some URL.
     *
     */
    
    public void urlLoadImage() {
        try {
            if (cbxExpression.getSelectedItem().toString().equals("")) {
                return;
            }
            if (!cbxExpression.getSelectedItem().toString().equals("<" + DesignMessageUtil.getMessage(ImageCustomizer.class, "ComboMessage") + ">")) { // NOI18N
                addItemToComboxBox(cbxExpression.getSelectedItem().toString());
                if (cbxExpression.getModel().getSize() > 1) {
                    String comboExpression = cbxExpression.getSelectedItem().toString().trim();
                    if (comboExpression.startsWith("/resources")) { // NOI18N
                        File f =  designContext.getProject().getResourceFile(new URI("web" + comboExpression));
                        if (f == null) {
                            imagePreview.setIcon(null);
                            lblImagePath.setText(DesignMessageUtil.getMessage(ImageCustomizer.class, "FileError")); // NOI18N
                        } else {
                            if (f.exists()) {
                                URL url = f.toURI().toURL();
                                lblImagePath.setText(comboExpression);
                                loadImage(new ImageIcon(url));
                            } else {
                                // Need to check if this else is necessary
                            }
                        }
                        
                    } else {
                        URL url = new URL(comboExpression);
                        Image img = Toolkit.getDefaultToolkit().getImage(url);
                        tabName = DesignMessageUtil.getMessage(ImageCustomizer.class, "OtherTab"); // NOI18N
                        loadImage(new ImageIcon(img));
                        if (imagePreview.getIcon().getIconHeight() == -1) {
                            imagePreview.setIcon(null);
                            lblImagePath.setText(DesignMessageUtil.getMessage(ImageCustomizer.class, "InvalidURL")); // NOI18N
                        } else {
                            lblImagePath.setText(comboExpression);
                            repaint();
                        }
                    }
                }
            }
            
        } catch(Exception ex) {
            imagePreview.setIcon(null);
            lblImagePath.setText(DesignMessageUtil.getMessage(ImageCustomizer.class, "InvalidURL")); // NOI18N
        }
    }
    
    
    /**
     * Adds items to the combox box that are entered by the user dynamically.
     *
     */
    
    public void addItemToComboxBox(String itemUrl) {
        int size = cbxExpression.getModel().getSize();
        boolean flag = false;
        for(int i=0; i<size; i++) {
            if (!urlArray.contains(cbxExpression.getItemAt(i).toString().trim())) {
                urlArray.add(cbxExpression.getItemAt(i));
            }
            
        }
        
        if (!urlArray.contains(itemUrl)){
            cbxExpression.addItem(itemUrl);
        }
        
    }
    
    /**
     * This method is called when the user selects a file from File tab .
     * This gets the new file info and creates an ImageIcon to show the preview.
     *
     */
    
    public void propertyChange(PropertyChangeEvent e) {
        boolean update = false;
        String prop = e.getPropertyName();
        
        if (imageFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            file = null;
            update = false;
        } else if (imageFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            file = (File)e.getNewValue();
            update = true;
        }
        
        if (update == true) {
            thumbnail = null;
            if (isShowing()) {
                tabName = DesignMessageUtil.getMessage(ImageCustomizer.class, "FileTab"); // NOI18N
                if (imageFileChooser.getSelectedFile() != null) {
                    if (file != null) {
                        loadImage(new ImageIcon(file.getPath()));
                        lblImagePath.setText(file.getPath());
                        repaint();
                    }
                } else {
                    file = null;
                }
                
            }
        }
    }
    
    /**
     * Gets the image and shows as a thumbnail
     *
     */
    
    
    public void loadImage(ImageIcon icon) {
        ImageIcon tmpIcon = icon;
        if (tmpIcon != null) {
            if (tmpIcon.getIconWidth() > 90) {
                thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(90,  -1,  Image.SCALE_DEFAULT));
            } else {
                thumbnail = tmpIcon;
            }
            imagePreview.setIcon(thumbnail);
        }
        
    }
    
    /**
     * Sets icon, image, imageURL properties for image and imageHyperlink components
     * This method checks to see if there there is imageURL property. If its there
     * then this method will set the imageURL property else it sets the url property
     *
     */
    
    public Result customizerApply() {
        String newUrl;
        if(rbIconPanel.isSelected()) {
		property = designBean.getProperty("url"); //NOI18N
		property.unset();
            property = designBean.getProperty("icon"); // NOI18N
            property.setValue(iconList.getSelectedValue());
            
        } else if (rbURLPanel.isSelected()) {
            property = designBean.getProperty("icon");
            property.unset();
            if (cbxExpression.getModel().getSize() >1 ) {
                if (!cbxExpression.getSelectedItem().toString().equals("<" + DesignMessageUtil.getMessage(ImageCustomizer.class, "ComboMessage") + ">")) { // NOI18N
                    property = designBean.getProperty("imageURL"); // NOI18N
                    if (property != null) {
                        newUrl = cbxExpression.getModel().getSelectedItem().toString().replace('\\','/');
                        property.setValue(newUrl);
                    } else {
                        property = designBean.getProperty("url"); // NOI18N
                        newUrl = cbxExpression.getModel().getSelectedItem().toString().replace('\\','/');
                        property.setValue(newUrl);
                    }
                }
            }
        } else if(rbFilePanel.isSelected()) {
            try {
                property = designBean.getProperty("icon");
                property.unset();
                if (imageFileChooser.getSelectedFile() != null) {
                    // check if setting image's proeprty or imageHyperlink's imageURL property
                    if(designBean.getInstance() instanceof ImageHyperlink){
                        property = designBean.getProperty("imageURL"); // NOI18N
                    }else{
                        property = designBean.getProperty("url"); // NOI18N
                    }
                    
                    URL url = imageFileChooser.getSelectedFile().toURI().toURL();
                    //newUrl = designContext.addResource(url, true);
                    // Add to project web root resources directory (see bug 6316775)
                    String encodedFileName = UrlPropertyEditor.encodeUrl(imageFileChooser.getSelectedFile().getName());
                    File newFile = designContext.getProject().getResourceFile(new URI("web/resources/" + encodedFileName));
                    if (newFile == null) {
                        newUrl = "/" + designContext.getProject().addResource(url, new URI("web/resources" + "/" + encodedFileName)).getPath();
                        newUrl = "/resources/" + encodedFileName;
                        property.setValue(newUrl);
                    } else {
                        if (newFile.exists()) {
                            newUrl = "/resources/" + UrlPropertyEditor.encodeUrl(imageFileChooser.getSelectedFile().getName());  // NOI18N
                            property.setValue(newUrl);
                        } else {
                            newUrl = "/" + designContext.getProject().addResource(url, new URI("web/resources" + "/" + encodedFileName)).getPath();
                            newUrl = "/resources/" + encodedFileName;
                            property.setValue(newUrl);
                            
                        }
                    }
                    setLastDirectoryUsed(imageFileChooser.getSelectedFile().getParentFile());
                }
            } catch(Exception exc) {
                exc.printStackTrace();
            }
        }
        Result result = new Result(true);
        return result;
        
    }
    
    public DesignContext getDesignContext() {
        return designContext;
    }
    
    
    
    public boolean isModified() {
        return true;
    }
    
    
    public void moveSlider() {
        int h = new Double(this.getSize().getHeight() - previewPanel.getSize().getHeight()).intValue();
        splitPane.setDividerLocation(h);
    }
    
    private com.sun.rave.web.ui.component.Icon getThemeIcon(FacesDesignContext facesDesignContext, String iconValue) {
        FacesDesignProject facesDesignProject = (FacesDesignProject)facesDesignContext.getProject();
        FacesContext facesContext =((FacesDesignContext) designBean.getDesignContext()).getFacesContext();
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        com.sun.rave.web.ui.component.Icon themeIcon = null;
        try {
            Thread.currentThread().setContextClassLoader(facesDesignProject.getContextClassLoader());
            themeIcon = com.sun.rave.web.ui.util.ThemeUtilities.getTheme(facesContext).getIcon(iconValue);
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
        return themeIcon;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        otherButtonGroup = new javax.swing.ButtonGroup();
        imageTypeButtonGroup = new javax.swing.ButtonGroup();
        splitPane = new javax.swing.JSplitPane();
        scrollPreview = new javax.swing.JScrollPane();
        previewPanel = new javax.swing.JPanel();
        imagePreview = new javax.swing.JLabel();
        lblImagePath = new javax.swing.JLabel();
        lblPreview = new javax.swing.JLabel();
        imagePanel = new javax.swing.JPanel();
        filePanel = new javax.swing.JPanel();
        imageFileChooser = new javax.swing.JFileChooser();
        otherPanel = new javax.swing.JPanel();
        btnRefresh = new javax.swing.JButton();
        lblSpacer = new javax.swing.JLabel();
        cbxExpression = new javax.swing.JComboBox();
        lblImageSource = new javax.swing.JLabel();
        iconPanel = new javax.swing.JPanel();
        lblIconIdentifier = new javax.swing.JLabel();
        scrollList = new javax.swing.JScrollPane();
        iconList = new javax.swing.JList();
        describeThemeIcon = new javax.swing.JTextArea();
        radioButtonsPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        rbFilePanel = new javax.swing.JRadioButton();
        rbURLPanel = new javax.swing.JRadioButton();
        rbIconPanel = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        splitPane.setDividerLocation(350);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(1.0);

        previewPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        previewPanel.add(imagePreview, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        previewPanel.add(lblImagePath, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/sun/rave/web/ui/component/customizers/Bundle-DT"); // NOI18N
        lblPreview.setText(bundle.getString("PreviewLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        previewPanel.add(lblPreview, gridBagConstraints);
        lblPreview.getAccessibleContext().setAccessibleName(bundle.getString("ACC_PreviewName")); // NOI18N
        lblPreview.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_PreivewDesc")); // NOI18N

        scrollPreview.setViewportView(previewPanel);
        previewPanel.getAccessibleContext().setAccessibleName(bundle.getString("ACC_PreviewPaneName")); // NOI18N
        previewPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_PreviewPaneDesc")); // NOI18N

        splitPane.setRightComponent(scrollPreview);

        imagePanel.setLayout(new java.awt.CardLayout());

        filePanel.setLayout(new java.awt.GridBagLayout());

        imageFileChooser.setControlButtonsAreShown(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel.add(imageFileChooser, gridBagConstraints);
        imageFileChooser.getAccessibleContext().setAccessibleName(bundle.getString("ACC_FileChooserName")); // NOI18N
        imageFileChooser.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_FileChooserPaneDesc")); // NOI18N

        imagePanel.add(filePanel, "file");

        otherPanel.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/customizers/Bundle-DT"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, bundle1.getString("RefreshButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        otherPanel.add(btnRefresh, gridBagConstraints);
        btnRefresh.getAccessibleContext().setAccessibleName(bundle.getString("ACC_Preview_Name")); // NOI18N
        btnRefresh.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_PreviewDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        otherPanel.add(lblSpacer, gridBagConstraints);

        cbxExpression.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        otherPanel.add(cbxExpression, gridBagConstraints);
        cbxExpression.getAccessibleContext().setAccessibleName(bundle.getString("ACC_EnterURLName")); // NOI18N
        cbxExpression.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_EnterURLDesc")); // NOI18N

        lblImageSource.setLabelFor(cbxExpression);
        org.openide.awt.Mnemonics.setLocalizedText(lblImageSource, bundle1.getString("ImageSourceLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        otherPanel.add(lblImageSource, gridBagConstraints);
        lblImageSource.getAccessibleContext().setAccessibleName(bundle.getString("Acc_ImageSourceName")); // NOI18N
        lblImageSource.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_ImageSourceDesc")); // NOI18N

        imagePanel.add(otherPanel, "url");

        iconPanel.setLayout(new java.awt.GridBagLayout());

        lblIconIdentifier.setLabelFor(iconList);
        org.openide.awt.Mnemonics.setLocalizedText(lblIconIdentifier, bundle1.getString("IconIdentifierLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        iconPanel.add(lblIconIdentifier, gridBagConstraints);
        lblIconIdentifier.getAccessibleContext().setAccessibleName(bundle.getString("ACC_IconIdentifierName")); // NOI18N
        lblIconIdentifier.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_Icon IdentifierDesc")); // NOI18N

        iconList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollList.setViewportView(iconList);
        iconList.getAccessibleContext().setAccessibleName(bundle.getString("iconListLabel")); // NOI18N
        iconList.getAccessibleContext().setAccessibleDescription(bundle.getString("iconListDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        iconPanel.add(scrollList, gridBagConstraints);
        scrollList.getAccessibleContext().setAccessibleName(bundle.getString("ACC_IconListName")); // NOI18N
        scrollList.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_IconListDesc")); // NOI18N

        describeThemeIcon.setBackground(java.awt.SystemColor.control);
        describeThemeIcon.setEditable(false);
        describeThemeIcon.setLineWrap(true);
        describeThemeIcon.setText(bundle.getString("IconDescriptionText")); // NOI18N
        describeThemeIcon.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        iconPanel.add(describeThemeIcon, gridBagConstraints);
        describeThemeIcon.getAccessibleContext().setAccessibleName(bundle.getString("ACC_IconListLabelName")); // NOI18N
        describeThemeIcon.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_IconListLabelDesc")); // NOI18N

        imagePanel.add(iconPanel, "icon");

        splitPane.setLeftComponent(imagePanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(splitPane, gridBagConstraints);

        radioButtonsPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridLayout(1, 0, 10, 0));

        imageTypeButtonGroup.add(rbFilePanel);
        rbFilePanel.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbFilePanel, bundle1.getString("FileTab")); // NOI18N
        rbFilePanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbFilePanelActionPerformed(evt);
            }
        });
        jPanel1.add(rbFilePanel);
        rbFilePanel.getAccessibleContext().setAccessibleName(bundle.getString("ACC_ChooseFileName")); // NOI18N
        rbFilePanel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_ChooseFileDesc")); // NOI18N

        imageTypeButtonGroup.add(rbURLPanel);
        org.openide.awt.Mnemonics.setLocalizedText(rbURLPanel, bundle1.getString("OtherTab")); // NOI18N
        rbURLPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbURLPanelActionPerformed(evt);
            }
        });
        jPanel1.add(rbURLPanel);
        rbURLPanel.getAccessibleContext().setAccessibleName(bundle.getString("ACC_EnterURLName")); // NOI18N
        rbURLPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_EnterURLDesc")); // NOI18N

        imageTypeButtonGroup.add(rbIconPanel);
        org.openide.awt.Mnemonics.setLocalizedText(rbIconPanel, bundle1.getString("ThemeIconTab")); // NOI18N
        rbIconPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbIconPanelActionPerformed(evt);
            }
        });
        jPanel1.add(rbIconPanel);
        rbIconPanel.getAccessibleContext().setAccessibleName(bundle.getString("ACC_SetThemeIconName")); // NOI18N
        rbIconPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_SetThemeIconDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        radioButtonsPanel.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(radioButtonsPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(bundle.getString("ACC_ImageCustomizerName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACC_ImageCustomizerDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void rbIconPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbIconPanelActionPerformed
// TODO add your handling code here:
        CardLayout cl = (CardLayout)imagePanel.getLayout();
        cl.show(imagePanel, "icon");
    }//GEN-LAST:event_rbIconPanelActionPerformed
    
    private void rbURLPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbURLPanelActionPerformed
// TODO add your handling code here:
        CardLayout cl = (CardLayout)imagePanel.getLayout();
        cl.show(imagePanel, "url");
    }//GEN-LAST:event_rbURLPanelActionPerformed
    
    private void rbFilePanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFilePanelActionPerformed
// TODO add your handling code here:
        CardLayout cl = (CardLayout)imagePanel.getLayout();
        cl.show(imagePanel, "file");
    }//GEN-LAST:event_rbFilePanelActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox cbxExpression;
    private javax.swing.JTextArea describeThemeIcon;
    private javax.swing.JPanel filePanel;
    private javax.swing.JList iconList;
    private javax.swing.JPanel iconPanel;
    private javax.swing.JFileChooser imageFileChooser;
    private javax.swing.JPanel imagePanel;
    private javax.swing.JLabel imagePreview;
    private javax.swing.ButtonGroup imageTypeButtonGroup;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblIconIdentifier;
    private javax.swing.JLabel lblImagePath;
    private javax.swing.JLabel lblImageSource;
    private javax.swing.JLabel lblPreview;
    private javax.swing.JLabel lblSpacer;
    private javax.swing.ButtonGroup otherButtonGroup;
    private javax.swing.JPanel otherPanel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JPanel radioButtonsPanel;
    private javax.swing.JRadioButton rbFilePanel;
    private javax.swing.JRadioButton rbIconPanel;
    private javax.swing.JRadioButton rbURLPanel;
    private javax.swing.JScrollPane scrollList;
    private javax.swing.JScrollPane scrollPreview;
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables
    
}
