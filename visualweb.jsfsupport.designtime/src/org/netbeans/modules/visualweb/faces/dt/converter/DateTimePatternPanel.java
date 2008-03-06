/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.visualweb.faces.dt.converter;

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
import java.util.ResourceBundle;
import org.netbeans.modules.visualweb.faces.dt.AbstractPropertyJPanel;

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
    
    private static ResourceBundle bundle = 
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt.converter.Bundle");

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
        label.setText(bundle.getString("dateTime_pattern")); //NOI18N
        label.setLabelFor(patternInputField);
        label.setDisplayedMnemonic(bundle.getString("Pattern_mnemonic").charAt(0));
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
        patternInputField.getAccessibleContext().setAccessibleName(bundle.getString("input_pattern"));
        patternInputField.getAccessibleContext().setAccessibleDescription(bundle.getString("input_pattern"));
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
        nowOutputField.getAccessibleContext().setAccessibleName(bundle.getString("output_pattern"));
        nowOutputField.getAccessibleContext().setAccessibleDescription(bundle.getString("output_pattern"));
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
        label.setText(bundle.getString("test")); //NOI18N
        label.setLabelFor(sampleInputField);
        label.setDisplayedMnemonic(bundle.getString("Test_mnemonic").charAt(0));
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
        sampleInputField.getAccessibleContext().setAccessibleName(bundle.getString("test"));
        sampleInputField.getAccessibleContext().setAccessibleDescription(bundle.getString("input_test"));
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
        sampleOutputField.getAccessibleContext().setAccessibleName(bundle.getString("test"));
        sampleOutputField.getAccessibleContext().setAccessibleDescription(bundle.getString("output_test"));
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
