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
 */

package org.netbeans.modules.vmd.screen;

import org.netbeans.modules.vmd.api.model.AccessController;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.screen.resource.ResourcePanel;
import org.netbeans.modules.vmd.screen.device.DevicePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * TODO - implement refresh of only components those were claimed as dirty by their ScreenPresenters - similar to Flow
 *
 * @author David Kaspar
 */
public final class ScreenAccessController implements AccessController {

    private final DesignDocument document;

    private final JPanel mainPanel;
    private final DevicePanel devicePanel;
    private final ResourcePanel resourcePanel;

    private final JComboBox editedScreenCombo;
    private final ActionListener editedScreenComboListener;

    private DesignComponent editedScreen;
    private List<DesignComponent> allEditableScreens = Collections.emptyList ();

    // Called in document transaction
    public ScreenAccessController (DesignDocument document) {
        this.document = document;

        devicePanel = new DevicePanel (this);
        resourcePanel = new ResourcePanel (this);
        mainPanel = new MainPanel (devicePanel, resourcePanel);

        editedScreenCombo = new JComboBox ();
        editedScreenCombo.setRenderer (new EditedComboRenderer ());
        editedScreenComboListener = new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                ScreenAccessController.this.document.getTransactionManager ().readAccess (new Runnable() {
                    public void run () {
                        setEditedScreen ((DesignComponent) editedScreenCombo.getSelectedItem ());
                        refreshPanels ();
                    }
                });
            }
        };
        editedScreenCombo.addActionListener (editedScreenComboListener);
    }

    // Called in document transaction
    public void writeAccess (Runnable runnable) {
        runnable.run ();
    }

    // Called in document transaction
    public void notifyEventFiring (DesignEvent event) {
    }

    // Called in document transaction
    public void notifyEventFired (DesignEvent event) {
        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                document.getTransactionManager().readAccess(new Runnable() {
                    public void run() {
                        refreshModel ();
                    }
                });
            }
        });
    }
    
    // called in AWT and document transaction
    private void refreshModel () {
        allEditableScreens = getEditableScreens ();

        editedScreenCombo.removeActionListener (editedScreenComboListener);
        editedScreenCombo.setModel (new DefaultComboBoxModel (allEditableScreens.toArray ()));
        editedScreenCombo.setSelectedItem (editedScreen);
        editedScreenCombo.addActionListener (editedScreenComboListener);

        if (editedScreen == null  ||  ! allEditableScreens.contains (editedScreen))
            setEditedScreen (allEditableScreens.size () > 0 ? allEditableScreens.get (0) : null);

        refreshPanels ();
    }

    private List<DesignComponent> getEditableScreens () {
        ArrayList<DesignComponent> screens = new ArrayList<DesignComponent> ();
        for (DesignComponent component : DocumentSupport.gatherAllComponentsContainingPresenterClass (document, ScreenDisplayPresenter.class)) {
            if (component.getPresenter (ScreenDisplayPresenter.class).isTopLevelDisplay ())
                screens.add (component);
        }
        return screens;
    }

    // called in AWT and document transaction
    public void setEditedScreen (DesignComponent component) {
        // TODO - hideNotify
        editedScreen = component;
        editedScreenCombo.removeActionListener (editedScreenComboListener);
        editedScreenCombo.setSelectedItem (component);
        editedScreenCombo.addActionListener (editedScreenComboListener);
        // TODO - reload
    }

    private void refreshPanels () {
        devicePanel.reload ();
        resourcePanel.reload ();
        mainPanel.validate ();
    }

    // Called in document transaction
    public void notifyComponentsCreated (Collection<DesignComponent> createdComponents) {
    }

    // Called in AWT
    void showNotify () {
        // TODO
    }

    // Called in AWT
    void hideNotify () {
        // TODO
    }

    JComponent getMainPanel () {
        return mainPanel;
    }

    JComponent getToolBar () {
        return editedScreenCombo;
    }

    public DesignDocument getDocument () {
        return document;
    }

    // called in AWT and document transaction
    public DesignComponent getEditedScreen () {
        assert SwingUtilities.isEventDispatchThread ();
        assert document.getTransactionManager ().isAccess ();
        return editedScreen;
    }

    private class EditedComboRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            final Image[] image = new Image[1];
            final String[] label = new String[1];
            if (value != null) {
                final DesignComponent dc = (DesignComponent) value;
                document.getTransactionManager ().readAccess (new Runnable() {
                    public void run () {
                        InfoPresenter presenter = dc.getPresenter (InfoPresenter.class);
                        label[0] = presenter.getDisplayName (InfoPresenter.NameType.PRIMARY);
                        image[0] = presenter.getIcon (InfoPresenter.IconType.COLOR_16x16);
                    }
                });
            }
            super.getListCellRendererComponent (list, label[0], index, isSelected, cellHasFocus);
            if (image[0] != null)
                setIcon (new ImageIcon (image[0]));
            return this;
        }
    }

}
