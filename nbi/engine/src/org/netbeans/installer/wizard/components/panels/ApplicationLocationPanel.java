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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components.panels;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.wizard.components.panels.DefaultWizardPanel;

/**
 *
 * @author ks152834
 */
public abstract class ApplicationLocationPanel extends DefaultWizardPanel {
    private JTextPane    textPane;
    private JLabel       locationFieldLabel;
    private JTextField   locationField;
    private JButton      browseButton;
    
    private JLabel       listLabel;
    private JList        list;
    
    private JPanel       spacer;
    private JPanel       listReplacement;
    
    private JFileChooser fileChooser;
    
    public ApplicationLocationPanel() {
        setProperty(TEXT_PROPERTY, DEFAULT_TEXT);
        setProperty(LOCATION_FIELD_LABEL_PROPERTY, DEFAULT_LOCATION_FIELD_LABEL);
        setProperty(BROWSE_BUTTON_LABEL_PROPERTY, DEFAULT_BROWSE_BUTTON_LABEL);
        setProperty(LIST_LABEL_PROPERTY, DEFAULT_LIST_LABEL);
    }
    
    public void initialize() {
        StringUtils stringUtils = StringUtils.getInstance();
        
        textPane.setText(getProperty(TEXT_PROPERTY));
        
        locationFieldLabel.setText(stringUtils.stripMnemonic(getProperty(LOCATION_FIELD_LABEL_PROPERTY)));
        locationFieldLabel.setDisplayedMnemonic(stringUtils.fetchMnemonic(getProperty(LOCATION_FIELD_LABEL_PROPERTY)));
        
        browseButton.setText(stringUtils.stripMnemonic(getProperty(BROWSE_BUTTON_LABEL_PROPERTY)));
        browseButton.setMnemonic(stringUtils.fetchMnemonic(getProperty(BROWSE_BUTTON_LABEL_PROPERTY)));
        
        listLabel.setText(stringUtils.stripMnemonic(getProperty(LIST_LABEL_PROPERTY)));
        listLabel.setDisplayedMnemonic(stringUtils.fetchMnemonic(getProperty(LIST_LABEL_PROPERTY)));
        
        LocationsListModel model = new LocationsListModel(getLocations());
        
        list.setModel(model);
        
        if (model.getSize() > 0) {
            if (getSelectedLocation() != null) {
                locationField.setText(getSelectedLocation().getAbsolutePath());
            } else {
                locationField.setText(model.getLocationAt(0).getAbsolutePath());
            }
            
            listLabel.setVisible(true);
            listLabel.setEnabled(true);
            
            list.setVisible(true);
            list.setEnabled(true);
            
            listReplacement.setVisible(false);
            listReplacement.setEnabled(false);
        } else {
            listLabel.setVisible(false);
            listLabel.setEnabled(false);
            
            list.setVisible(false);
            list.setEnabled(false);
            
            listReplacement.setVisible(true);
            listReplacement.setEnabled(true);
        }
    }
    
    public void initComponents() {
        setLayout(new GridBagLayout());
        
        textPane = new JTextPane();
        textPane.setContentType("text/plain");
        textPane.setOpaque(false);
        textPane.setEditable(false);
        
        locationField = new JTextField();
        locationField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent event) {
                fieldValueChanged();
            }
            
            public void insertUpdate(DocumentEvent event) {
                fieldValueChanged();
            }
            
            public void removeUpdate(DocumentEvent event) {
                fieldValueChanged();
            }
        });
        
        locationFieldLabel = new JLabel();
        locationFieldLabel.setLabelFor(locationField);
        
        browseButton = new JButton();
        if (SystemUtils.getInstance().isMacOS()) {
            browseButton.setOpaque(false);
        }
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                browseButtonPressed();
            }
        });
        
        spacer = new JPanel();
        spacer.setOpaque(false);
        
        list = new JList();
        list.setOpaque(false);
        list.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new LocationsListCellRenderer());
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    listSelectionChanged();
                }
            }
        });
        
        listLabel = new JLabel();
        listLabel.setLabelFor(list);
        
        listReplacement = new JPanel();
        listReplacement.setOpaque(false);
        
        add(textPane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
        add(locationFieldLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 0, 11), 0, 0));
        add(locationField, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 11, 0, 4), 0, 0));
        add(browseButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 0, 0, 11), 0, 0));
        add(spacer, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 0, 11), 0, 0));
        add(listLabel, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 0, 11), 0, 0));
        add(list, new GridBagConstraints(0, 5, 2, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 11, 11, 11), 0, 0));
        add(listReplacement, new GridBagConstraints(0, 6, 2, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 11, 11, 11), 0, 0));
        
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
    }
    
    public void evaluateNextButtonClick() {
        File location = new File(locationField.getText());
        
        if (!validateLocation(location)) {
            ErrorManager.getInstance().notify(ErrorLevel.ERROR, getErrorMessage());
        } else {
            setLocation(location);
            super.evaluateNextButtonClick();
        }
    }
    
    public abstract Map<String, File> getLocations();
    
    public abstract boolean validateLocation(File location);
    
    public abstract String getErrorMessage();
    
    public abstract void setLocation(File location);
    
    public abstract File getSelectedLocation();
    
    private void browseButtonPressed() {
        fileChooser.setSelectedFile(new File(locationField.getText()));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            locationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void listSelectionChanged() {
        final LocationsListModel model = (LocationsListModel) list.getModel();
        
        final int index = list.getSelectedIndex();
        if (index != -1) {
            String location = model.getLocationAt(index).getAbsolutePath();
            if (!location.equals(locationField.getText())) {
                locationField.setText(location);
            }
        }
        
    }
    
    private void fieldValueChanged() {
        final LocationsListModel model = (LocationsListModel) list.getModel();
        
        final String value  = locationField.getText();
        final int length = model.getSize();
        
        for (int i = 0; i < length; i++) {
            final String element = (String) model.getLocationAt(i).getAbsolutePath();
            
            if (value.equals(element)) {
                list.setSelectedIndex(i);
                return;
            }
        }
        
        list.clearSelection();
    }
    
    public static final String TEXT_PROPERTY = "text";
    public static final String LOCATION_FIELD_LABEL_PROPERTY = "location.field.label";
    public static final String BROWSE_BUTTON_LABEL_PROPERTY = "browse.button.label";
    public static final String LIST_LABEL_PROPERTY = "list.label";
    
    public static final String DEFAULT_TEXT = "";
    public static final String DEFAULT_LOCATION_FIELD_LABEL = "";
    public static final String DEFAULT_BROWSE_BUTTON_LABEL = "";
    public static final String DEFAULT_LIST_LABEL = "";
    
    public static final String DEFAULT_LOCATION = "";
    
    private static class LocationsListCellRenderer extends JLabel implements ListCellRenderer {
        public LocationsListCellRenderer() {
            setBorder(new EmptyBorder(2, 2, 2, 2));
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                setOpaque(true);
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                setOpaque(false);
            }
            
            return this;
        }
    }
    
    public static class LocationsListModel implements ListModel {
        private List<String> labels = new LinkedList<String>();
        private List<File>   locations = new LinkedList<File>();
        
        private List<ListDataListener> listeners = new LinkedList<ListDataListener>();
        
        public LocationsListModel(final Map<String, File> locationsMap) {
            for (String label: locationsMap.keySet()) {
                labels.add(label);
                locations.add(locationsMap.get(label));
            }
        }
        
        public LocationsListModel(final List<String> locationsList) {
            for (String location: locationsList) {
                labels.add(location);
                locations.add(new File(location));
            }
        }
        
        public int getSize() {
            return locations.size();
        }
        
        public Object getElementAt(int index) {
            return getLabelAt(index);
        }
        
        public String getLabelAt(int index) {
            return labels.get(index);
        }
        
        public File getLocationAt(int index) {
            return locations.get(index);
        }
        
        public void addListDataListener(ListDataListener listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeListDataListener(ListDataListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }
}
