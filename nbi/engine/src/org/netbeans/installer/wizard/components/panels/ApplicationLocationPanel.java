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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiList;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextField;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;

/**
 *
 * @author ks152834
 */
public abstract class ApplicationLocationPanel extends ErrorMessagePanel {
    private NbiTextPane    messagePane;
    private NbiLabel       locationLabel;
    private NbiTextField   locationField;
    private NbiButton      locationButton;
    
    private NbiLabel       listLabel;
    private NbiList        list;
    
    private NbiPanel       spacer;
    private NbiPanel       listReplacement;
    
    private JFileChooser fileChooser;
    
    public ApplicationLocationPanel() {
        setProperty(MESSAGE_TEXT_PROPERTY, DEFAULT_MESSAGE_TEXT);
        setProperty(MESSAGE_TEXT_NOTHING_FOUND_PROPERTY, DEFAULT_MESSAGE_TEXT_NOTHING_FOUND);
        setProperty(MESSAGE_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_CONTENT_TYPE);
        setProperty(LOCATION_LABEL_TEXT_PROPERTY, DEFAULT_LOCATION_LABEL_TEXT);
        setProperty(LOCATION_BUTTON_TEXT_PROPERTY, DEFAULT_LOCATION_BUTTON_TEXT);
        setProperty(LIST_LABEL_TEXT_PROPERTY, DEFAULT_LIST_LABEL_TEXT);
    }
    
    public void initialize() {
        messagePane.setContentType(getProperty(MESSAGE_CONTENT_TYPE_PROPERTY));
        messagePane.setText(getProperty(MESSAGE_TEXT_PROPERTY));
        locationLabel.setText(getProperty(LOCATION_LABEL_TEXT_PROPERTY));
        locationButton.setText(getProperty(LOCATION_BUTTON_TEXT_PROPERTY));
        listLabel.setText(getProperty(LIST_LABEL_TEXT_PROPERTY));
        
        LocationsListModel model = new LocationsListModel(getLocations());
        list.setModel(model);
        
        File selectedLocation = getSelectedLocation();
        if (model.getSize() > 0) {
            if (selectedLocation != null) {
                locationField.setText(selectedLocation.getAbsolutePath());
            } else {
                locationField.setText(model.getLocationAt(0).getAbsolutePath());
            }
            
            setListVisibility(true);
        } else {
            if (selectedLocation != null) {
                locationField.setText(selectedLocation.getAbsolutePath());
            } else {
                locationField.setText(SystemUtils.parsePath(DEFAULT_LOCATION).getAbsolutePath());
            }
            
            setListVisibility(false);
        }
    }
    
    public void initComponents() {
        messagePane = new NbiTextPane();
        
        locationField = new NbiTextField();
        locationField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent event) {
                locationChanged();
            }
            
            public void insertUpdate(DocumentEvent event) {
                locationChanged();
            }
            
            public void removeUpdate(DocumentEvent event) {
                locationChanged();
            }
        });
        
        locationLabel = new NbiLabel();
        locationLabel.setLabelFor(locationField);
        
        locationButton = new NbiButton();
        locationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                browseButtonPressed();
            }
        });
        
        spacer = new NbiPanel();
        
        list = new NbiList();
        list.setCellRenderer(new LocationsListCellRenderer());
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    listSelectionChanged();
                }
            }
        });
        
        listLabel = new NbiLabel();
        listLabel.setLabelFor(list);
        
        listReplacement = new NbiPanel();
        
        add(messagePane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
        add(locationLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 0, 11), 0, 0));
        add(locationField, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 11, 0, 4), 0, 0));
        add(locationButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 0, 0, 11), 0, 0));
        add(spacer, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 0, 11), 0, 0));
        add(listLabel, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 0, 11), 0, 0));
        add(list, new GridBagConstraints(0, 5, 2, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 11, 11, 11), 0, 0));
        add(listReplacement, new GridBagConstraints(0, 6, 2, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 11, 0, 11), 0, 0));
        
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
    }
    
    public void evaluateNextButtonClick() {
        String value    = locationField.getText().trim();
        File   location = new File(value);
        
        String errorMessage = validateLocation(value);
        
        if (errorMessage != null) {
            ErrorManager.notify(ErrorLevel.ERROR, errorMessage);
        } else {
            setLocation(location);
            super.evaluateNextButtonClick();
        }
    }
    
    public abstract Map<String, File> getLocations();
    
    public abstract File getSelectedLocation();
    
    public abstract String validateLocation(String value);
    
    public abstract void setLocation(File location);
    
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
    
    private void locationChanged() {
        updateErrorMessage();
        
        final LocationsListModel model = (LocationsListModel) list.getModel();
        final String value = locationField.getText().trim();
        
        for (int i = 0; i < model.getSize(); i++) {
            final String element = (String) model.getLocationAt(i).getAbsolutePath();
            
            if (value.equals(element)) {
                list.setSelectedIndex(i);
                return;
            }
        }
        
        list.clearSelection();
    }
    
    public String validateInput() {
        if (list.getModel() instanceof LocationsListModel) {
            final LocationsListModel model = 
                    (LocationsListModel) list.getModel();
            final String value = locationField.getText().trim();
            
            return validateLocation(value);
        }
        
        return null;
    }
    
    private void setListVisibility(boolean state) {
        listLabel.setVisible(state);
        listLabel.setEnabled(state);
        
        list.setVisible(state);
        list.setEnabled(state);
        
        listReplacement.setVisible(!state);
        listReplacement.setEnabled(!state);
    }
    
    public static final String MESSAGE_TEXT_PROPERTY = "message.text";
    public static final String MESSAGE_TEXT_NOTHING_FOUND_PROPERTY = "message.text.nothing.found";
    public static final String MESSAGE_CONTENT_TYPE_PROPERTY = "message.content.type";
    public static final String LOCATION_LABEL_TEXT_PROPERTY = "location.label.text";
    public static final String LOCATION_BUTTON_TEXT_PROPERTY = "location.button.text";
    public static final String LIST_LABEL_TEXT_PROPERTY = "list.label.text";
    
    public static final String DEFAULT_MESSAGE_TEXT = ResourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.message.text");
    public static final String DEFAULT_MESSAGE_TEXT_NOTHING_FOUND = ResourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.message.text.nothing.found");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE = ResourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.message.content.type");
    public static final String DEFAULT_LOCATION_LABEL_TEXT = ResourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.location.label.text");
    public static final String DEFAULT_LOCATION_BUTTON_TEXT = ResourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.location.button.text");
    public static final String DEFAULT_LIST_LABEL_TEXT = ResourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.list.label.text");
    
    public static final String DEFAULT_LOCATION = ResourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.location");
    
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
