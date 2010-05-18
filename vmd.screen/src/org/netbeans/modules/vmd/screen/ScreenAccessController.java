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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.screen;

import org.netbeans.modules.vmd.api.model.AccessController;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.screen.editor.EditedScreenSupport;
import org.netbeans.modules.vmd.screen.device.DevicePanel;
import org.netbeans.modules.vmd.screen.resource.ResourcePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfoPresenter;

/**
 * TODO - implement refresh of only components those were claimed as dirty by their ScreenPresenters - similar to Flow
 *
 * @author David Kaspar
 */
public final class ScreenAccessController implements AccessController, EditedScreenSupport.Listener {
    
    private final DesignDocument document;
    
    private JPanel mainPanel;
    private DevicePanel devicePanel;
    private final ResourcePanel resourcePanel;
    
    private final JComboBox editedScreenCombo;
    private final ActionListener editedScreenComboListener;
    
    private DesignComponent editedScreen;
    private List<DesignComponent> allEditableScreens = Collections.emptyList();
    
    // Called in document transaction
    public ScreenAccessController(final DesignDocument document) {
        this.document = document;
        resourcePanel = new ResourcePanel(this);
        editedScreenCombo = new JComboBox();
        editedScreenCombo.setRenderer(new EditedComboRenderer());
        editedScreenComboListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                document.getTransactionManager().readAccess(new Runnable() {
                    public void run() {
                        DesignComponent component = (DesignComponent) editedScreenCombo.getSelectedItem();
                        EditedScreenSupport.getSupportForDocument(document).setEditedScreenComponentID(component != null ? component.getComponentID() : -1);
                    }
                });
            }
        };
        editedScreenCombo.addActionListener(editedScreenComboListener);
        EditedScreenSupport.getSupportForDocument(document).addListener(this); // TODO - maybe possible memory leak, since "this" is an access controller holding design document
    }
    
    public void editedScreenChanged(long editedScreenComponentID) {
        editedScreen = document.getComponentByUID(EditedScreenSupport.getSupportForDocument(document).getEditedScreenComponentID());
        editedScreenCombo.removeActionListener(editedScreenComboListener);
        editedScreenCombo.setSelectedItem(editedScreen);
        editedScreenCombo.addActionListener(editedScreenComboListener);
        
        refreshPanels();
    }
    
    // Called in document transaction
    public void writeAccess(Runnable runnable) {
        runnable.run();
    }
    
    // Called in document transaction
    public void notifyEventFiring(DesignEvent event) {
    }
    
    // Called in document transaction
    public void notifyEventFired(DesignEvent event) {
        //TODO Debug warnig when device presenter is not present
        if (!isDeviceInfoExists())
            return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                document.getTransactionManager().readAccess(new Runnable() {
                    public void run() {
                        refreshModel();
                    }
                });
            }
        });
    }
    
    // called in AWT and document transaction
    // call this method from notifyEventFired only
    private void refreshModel() {
        if (devicePanel == null) {
            devicePanel = new DevicePanel(this);
            mainPanel = new MainPanel(devicePanel, resourcePanel);
        }
        allEditableScreens = EditedScreenSupport.getAllEditableScreensInDocument(document);
        
        editedScreenCombo.removeActionListener(editedScreenComboListener);
        editedScreenCombo.setModel(new DefaultComboBoxModel(allEditableScreens.toArray()));
        if (! allEditableScreens.contains(editedScreen))
            editedScreen = null;
        editedScreenCombo.setSelectedItem(editedScreen);
        editedScreenCombo.addActionListener(editedScreenComboListener);
        
        if (editedScreen == null  &&  allEditableScreens.size() > 0) {
            editedScreen = allEditableScreens.get(0);
            EditedScreenSupport.getSupportForDocument(document).setEditedScreenComponentID(editedScreen.getComponentID());
            return;
        }
        
        refreshPanels();
    }
    
    public void refreshPanels() {
        if (devicePanel == null)
            return;
        devicePanel.reload();
        resourcePanel.reload();
        mainPanel.validate();
    }
    
    // Called in document transaction
    public void notifyComponentsCreated(Collection<DesignComponent> createdComponents) {
    }
    
    // Called in AWT
    void showNotify() {
        // TODO
    }
    
    // Called in AWT
    void hideNotify() {
        // TODO
    }
    
    JComponent getMainPanel() {
        return mainPanel;
    }
    
    JComponent getToolBar() {
        return editedScreenCombo;
    }
    
    public DesignDocument getDocument() {
        return document;
    }
    
    // called in AWT and document transaction
    public DesignComponent getEditedScreen() {
        assert SwingUtilities.isEventDispatchThread();
        assert document.getTransactionManager().isAccess();
        return editedScreen;
    }
    
    public void setScreenSize(Dimension deviceScreenSize) {
        if (devicePanel != null)
            devicePanel.setScreenSize(deviceScreenSize);
    }
    
    private class EditedComboRenderer extends DefaultListCellRenderer {
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            final Image[] image = new Image[1];
            final String[] label = new String[1];
            if (value != null) {
                final DesignComponent dc = (DesignComponent) value;
                document.getTransactionManager().readAccess(new Runnable() {
                    public void run() {
                        InfoPresenter presenter = dc.getPresenter(InfoPresenter.class);
                        label[0] = presenter.getDisplayName(InfoPresenter.NameType.PRIMARY);
                        image[0] = presenter.getIcon(InfoPresenter.IconType.COLOR_16x16);
                    }
                });
            }
            super.getListCellRendererComponent(list, label[0], index, isSelected, cellHasFocus);
            if (image[0] != null)
                setIcon(new ImageIcon(image[0]));

            return this;
        }
    }
    
    private boolean isDeviceInfoExists() {
        DesignComponent rootComponent = document.getRootComponent();
        if (rootComponent == null)
            return false;
        ScreenDeviceInfoPresenter presenter = rootComponent.getPresenter(ScreenDeviceInfoPresenter.class);
        if (presenter == null || presenter.getScreenDeviceInfo() == null)
            return false;
        return true;
    }
    
}
