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
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author ks152834
 */
public abstract class ApplicationLocationPanel extends DefaultWizardPanel {
    private JTextPane    messagePane;
    private JLabel       locationLabel;
    private JTextField   locationField;
    private JButton      locationButton;
    
    private JLabel       listLabel;
    private JList        list;
    
    private JPanel       spacer;
    private JPanel       listReplacement;
    
    private JLabel       errorLabel;
    
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
        final String messageContentType = systemUtils.parseString(getProperty(MESSAGE_CONTENT_TYPE_PROPERTY), getClassLoader());
        messagePane.setContentType(messageContentType);
        
        final String messageText = systemUtils.parseString(getProperty(MESSAGE_TEXT_PROPERTY), getClassLoader());
        messagePane.setText(messageText);
        
        final String locationLabelText = systemUtils.parseString(getProperty(LOCATION_LABEL_TEXT_PROPERTY), getClassLoader());
        locationLabel.setText(stringUtils.stripMnemonic(locationLabelText));
        locationLabel.setDisplayedMnemonic(stringUtils.fetchMnemonic(locationLabelText));
        
        final String locationButtonText = systemUtils.parseString(getProperty(LOCATION_BUTTON_TEXT_PROPERTY), getClassLoader());
        locationButton.setText(stringUtils.stripMnemonic(locationButtonText));
        locationButton.setMnemonic(stringUtils.fetchMnemonic(locationButtonText));
        
        final String listLabelText = systemUtils.parseString(getProperty(LIST_LABEL_TEXT_PROPERTY), getClassLoader());
        listLabel.setText(stringUtils.stripMnemonic(listLabelText));
        listLabel.setDisplayedMnemonic(stringUtils.fetchMnemonic(listLabelText));
        
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
                locationField.setText(SystemUtils.getInstance().parsePath(DEFAULT_LOCATION).getAbsolutePath());
            }
            
            setListVisibility(false);
        }
    }
    
    public void initComponents() {
        setLayout(new GridBagLayout());
        
        messagePane = new JTextPane();
        messagePane.setContentType("text/plain");
        messagePane.setOpaque(false);
        messagePane.setEditable(false);
        
        locationField = new JTextField();
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
        
        locationLabel = new JLabel();
        locationLabel.setLabelFor(locationField);
        
        locationButton = new JButton();
        if (SystemUtils.Platform.isMacOS()) {
            locationButton.setOpaque(false);
        }
        locationButton.addActionListener(new ActionListener() {
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
        
        errorLabel = new JLabel();
        errorLabel.setIcon(emptyIcon);
        errorLabel.setText(" ");
        
        add(messagePane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
        add(locationLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 0, 11), 0, 0));
        add(locationField, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 11, 0, 4), 0, 0));
        add(locationButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 0, 0, 11), 0, 0));
        add(spacer, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 0, 11), 0, 0));
        add(listLabel, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 11, 0, 11), 0, 0));
        add(list, new GridBagConstraints(0, 5, 2, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 11, 11, 11), 0, 0));
        add(listReplacement, new GridBagConstraints(0, 6, 2, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 11, 0, 11), 0, 0));
        add(errorLabel, new GridBagConstraints(0, 7, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(7, 11, 11, 11), 0, 0));
        
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
    }
    
    public void evaluateNextButtonClick() {
        String value    = locationField.getText().trim();
        File   location = new File(value);
        
        String errorMessage = validateLocation(value);
        
        if (errorMessage != null) {
            ErrorManager.getInstance().notify(ErrorLevel.ERROR, errorMessage);
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
        final LocationsListModel model = (LocationsListModel) list.getModel();
        final String value = locationField.getText().trim();
        
        final String errorMessage = validateLocation(value);
        if (errorMessage == null) {
            errorLabel.setIcon(emptyIcon);
            errorLabel.setText(" ");
            
            getNextButton().setEnabled(true);
        } else {
            errorLabel.setIcon(errorIcon);
            errorLabel.setText(errorMessage);
            
            getNextButton().setEnabled(false);
        }
        
        for (int i = 0; i < model.getSize(); i++) {
            final String element = (String) model.getLocationAt(i).getAbsolutePath();
            
            if (value.equals(element)) {
                list.setSelectedIndex(i);
                return;
            }
        }
        
        list.clearSelection();
    }
    
    private void setListVisibility(boolean state) {
        listLabel.setVisible(state);
        listLabel.setEnabled(state);
        
        list.setVisible(state);
        list.setEnabled(state);
        
        listReplacement.setVisible(!state);
        listReplacement.setEnabled(!state);
    }
    
    private static StringUtils   stringUtils   = StringUtils.getInstance();
    private static SystemUtils   systemUtils   = SystemUtils.getInstance();
    private static ResourceUtils resourceUtils = ResourceUtils.getInstance();
    
    public static final String MESSAGE_TEXT_PROPERTY = "message.text";
    public static final String MESSAGE_TEXT_NOTHING_FOUND_PROPERTY = "message.text.nothing.found";
    public static final String MESSAGE_CONTENT_TYPE_PROPERTY = "message.content.type";
    public static final String LOCATION_LABEL_TEXT_PROPERTY = "location.label.text";
    public static final String LOCATION_BUTTON_TEXT_PROPERTY = "location.button.text";
    public static final String LIST_LABEL_TEXT_PROPERTY = "list.label.text";
    
    public static final String DEFAULT_MESSAGE_TEXT = resourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.message.text");
    public static final String DEFAULT_MESSAGE_TEXT_NOTHING_FOUND = resourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.message.text.nothing.found");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE = resourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.message.content.type");
    public static final String DEFAULT_LOCATION_LABEL_TEXT = resourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.location.label.text");
    public static final String DEFAULT_LOCATION_BUTTON_TEXT = resourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.location.button.text");
    public static final String DEFAULT_LIST_LABEL_TEXT = resourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.list.label.text");
    
    public static final String DEFAULT_LOCATION = resourceUtils.getString(ApplicationLocationPanel.class, "ApplicationLocationPanel.default.location");
    
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
