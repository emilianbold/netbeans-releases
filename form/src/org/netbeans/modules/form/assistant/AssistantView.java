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
 */
package org.netbeans.modules.form.assistant;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import javax.swing.*;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import org.netbeans.modules.form.FormLoaderSettings;
import org.openide.util.Utilities;

/**
 * Assistant view.
 *
 * @author Jan Stola
 */
public class AssistantView extends JPanel {
    private JLabel messageLabel;
    private AssistantModel model;
    
    public AssistantView(AssistantModel model) {
        this.model = model;

        Listener listener = new Listener();
        model.addPropertyChangeListener(listener);

        setBackground(FormLoaderSettings.getInstance().getFormDesignerBackgroundColor());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getForeground()));
        setToolTipText(null);

        // Message label
        messageLabel = new JLabel();
        messageLabel.setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/form/resources/lightbulb.gif"))); // NOI18N

        // Close button
        JButton closeButton = new JButton("x"); // NOI18N
        closeButton.setFont(Font.getFont("SansSerif")); // NOI18N
        closeButton.setOpaque(false);
        closeButton.setFocusPainted(false);
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(listener);
        // Workaround for GroupLayout.BASELINE == GroupLayout.CENTER bug
        JPanel panel = new JPanel(new BorderLayout(0,0));
        panel.setOpaque(false);
        panel.add(closeButton);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .add(12)
                .add(messageLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(12));
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .add(2)
                .add(layout.createParallelGroup(GroupLayout.CENTER)
                    .add(messageLabel, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .add(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(2));
    }

    private class Listener implements ActionListener, PropertyChangeListener {

        public void actionPerformed(ActionEvent e) {
            FormLoaderSettings.getInstance().setAssistantShown(false);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String[] messages = model.getMessages();
            String message = null;
            if (messages != null) {
                int index = (int)(Math.random()*messages.length);
                message = messages[index];
            }
            if (model.getAdditionalContext() != null) {
                messages = model.getAdditionalMessages();
                if (messages != null) {
                    int index = (int)(Math.random()*messages.length);
                    message = "<html>" + message + "<br>" + messages[index]; // NOI18N
                }
            }
            Object[] params = model.getParameters();
            if (params != null) {
                message = MessageFormat.format(message, params);
            }
            messageLabel.setText(message);
        }

    }

}
