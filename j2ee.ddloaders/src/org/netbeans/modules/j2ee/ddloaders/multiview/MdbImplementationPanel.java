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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.MdbImplementationForm;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author pfiala
 */
public class MdbImplementationPanel extends MdbImplementationForm {

    /**
     * Creates new form MdbImplementationForm
     *
     * @param sectionNodeView enclosing SectionNodeView object
     */
    public MdbImplementationPanel(SectionNodeView sectionNodeView, MessageDriven messageDriven) {
        super(sectionNodeView);
        JButton moveClassButton = getMoveClassButton();
        final String className = getBeanClassTextField().getText().trim();
        moveClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.activateMoveClassUI(className);
            }
        });
        JButton renameClassButton = getRenameClassButton();
        renameClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.activateRenameClassUI(className);
            }
        });


    }

    protected void propertyChanged(Object source, String propertyName, Object oldValue, Object newValue) {
        super.propertyChanged(source, propertyName, oldValue, newValue);
    }
}
