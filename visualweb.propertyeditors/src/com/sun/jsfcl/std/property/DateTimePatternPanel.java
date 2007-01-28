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
package com.sun.jsfcl.std.property;

import java.awt.GridBagConstraints;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DateTimePatternPanel extends AbstractPropertyJPanel {

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    protected JTextField nowOutputField;
    protected JTextField patternInputField;
    protected JTextField sampleInputField;
    protected JTextField sampleOutputField;

    /**
     *
     */
    public DateTimePatternPanel(DateTimePatternPropertyEditor propertyEditor,
        DesignProperty liveProperty) {

        super(propertyEditor, liveProperty);
        updateNowOutputField();
    }

    /**
     * Add a PropertyChangeListener to this panel's list of listeners. The
     * listener will be notified of all property changes. The property editor 
     * that creates this panel <strong>must</strong> register itself as a 
     * property change listener, so that actions taken in the panel are 
     * communicated back to the IDE.
     *
     * @param listener The listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Remove listener from this panel's list of listeners.
     *
     * @param listener The listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    public void documentEvent(DocumentEvent event) {

        if (initializing) {
            return;
        }
        if (event.getDocument() == patternInputField.getDocument()) {
            handlePatternInputDocumentEvent(event);
        }
        if (event.getDocument() == sampleInputField.getDocument()) {
            handleSampleInputDocumentEvent(event);
        }
    }

    public DateTimePatternPropertyEditor getDateTimePatternPropertyEditor() {

        return (DateTimePatternPropertyEditor)getPropertyEditor();
    }

    protected String getPattern() {
        String result;

        result = patternInputField.getText();
        if (result == null) {
            result = ""; //NOI18N
        }
        return result;
    }

    protected String getSample() {
        String result;

        result = sampleInputField.getText();
        if (result == null) {
            result = ""; //NOI18N
        }
        return result;
    }
    
    String previousPattern;

    public void handlePatternInputDocumentEvent(DocumentEvent event) {
        updateNowOutputField();
        String property =
            this.getDateTimePatternPropertyEditor().getDesignProperty().getPropertyDescriptor().getName();
        this.propertyChangeSupport.firePropertyChange( property, previousPattern, this.getPattern());
        previousPattern = this.getPattern();
    }

    public void handleSampleInputDocumentEvent(DocumentEvent event) {
        updateSampleOutputField();
    }

    public void initializeComponents() {
        GridBagConstraints gridBagConstraints;
        JLabel label;

        setLayout(new java.awt.GridBagLayout());

        /*
         * Screen looks something like this
         *   Pattern:  [                                                ]
         *   Sample
         *       In       [                                                 ]
         *       Out    <                                    >
         *       Now   <                                    >
         */

        label = new javax.swing.JLabel();
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setText(BundleHolder.bundle.getMessage("pattern")); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(label, gridBagConstraints);

        patternInputField = new JTextField();
        patternInputField.getDocument().addDocumentListener(this);
        patternInputField.setText(getPropertyEditor().getAsText());
        patternInputField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("com/sun/jsfcl/std/property/Bundle").getString("pattern"));
        patternInputField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("com/sun/jsfcl/std/property/Bundle").getString("input_pattern"));
        label.setLabelFor(patternInputField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 280;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(patternInputField, gridBagConstraints);

        /*
                label = new javax.swing.JLabel();
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setText("Now");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 4;
                gridBagConstraints.ipadx = 0;
                gridBagConstraints.ipady = 0;
                gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
                gridBagConstraints.fill = GridBagConstraints.BOTH;
                add(label, gridBagConstraints);
         */
        nowOutputField = new JTextField();
        nowOutputField.setText(""); //NOI18N
        nowOutputField.setEditable(false);
        nowOutputField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("com/sun/jsfcl/std/property/Bundle").getString("pattern"));
        nowOutputField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("com/sun/jsfcl/std/property/Bundle").getString("output_pattern"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(nowOutputField, gridBagConstraints);
        /*
                label = new javax.swing.JLabel();
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setText("Sample");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.gridwidth = 3;
                gridBagConstraints.ipadx = 0;
                gridBagConstraints.ipady = 0;
                gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
                gridBagConstraints.fill = GridBagConstraints.BOTH;
                add(label, gridBagConstraints);
         */
        label = new javax.swing.JLabel();
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setText(BundleHolder.bundle.getMessage("test")); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(label, gridBagConstraints);

        sampleInputField = new JTextField();
        sampleInputField.getDocument().addDocumentListener(this);
        sampleInputField.setText(""); //NOI18N
        sampleInputField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("com/sun/jsfcl/std/property/Bundle").getString("test"));
        sampleInputField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("com/sun/jsfcl/std/property/Bundle").getString("input_test"));
        label.setLabelFor(sampleInputField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 200;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(sampleInputField, gridBagConstraints);
        /*
                label = new javax.swing.JLabel();
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setText("Out");
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 3;
                gridBagConstraints.ipadx = 0;
                gridBagConstraints.ipady = 0;
                gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
                gridBagConstraints.fill = GridBagConstraints.BOTH;
                add(label, gridBagConstraints);
         */
        sampleOutputField = new JTextField();
        sampleOutputField.setText(""); //NOI18N
        sampleOutputField.setEditable(false);
        sampleOutputField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("com/sun/jsfcl/std/property/Bundle").getString("test"));
        sampleOutputField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("com/sun/jsfcl/std/property/Bundle").getString("output_test"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(sampleOutputField, gridBagConstraints);

    }

    protected void updateNowOutputField() {
        String output;

        try {
            String pattern;
            SimpleDateFormat format;
            Date date;

            date = new Date();
            pattern = getPattern();
            format = new SimpleDateFormat(pattern);
            output = format.format(date);
        } catch (Throwable t) {
            output = "** ERROR: " + t.getMessage(); //NOI18N
        }
        nowOutputField.setText(output);
    }

    protected void updateSampleOutputField() {
        String output;

        try {
            String pattern, sample;
            SimpleDateFormat inputFormat;
            DateFormat outputFormat;
            Date date;

            pattern = getPattern();
            inputFormat = new SimpleDateFormat(pattern);
            sample = getSample();
            date = inputFormat.parse(sample);
            outputFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
            output = outputFormat.format(date);
        } catch (Throwable t) {
            output = "** ERROR: " + t.getMessage(); //NOI18N
        }
        sampleOutputField.setText(output);
    }

}
