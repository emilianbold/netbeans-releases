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
package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayDataFlavorSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.openide.util.Utilities;
import org.openide.util.Exceptions;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;

/**
 * @author David Kaspar
 */
public class ItemDisplayPresenter extends ScreenDisplayPresenter {

    private JPanel panel;
    private WrappedLabel label;
    private JComponent contentComponent;

    public ItemDisplayPresenter() {
        panel = new JPanel() {

            @Override
            public JPopupMenu getComponentPopupMenu() {
                return Utilities.actionsToPopup(ActionsSupport.createActionsArray(getRelatedComponent()), this);
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);

        // Fix for #79636 - Screen designer tab traversal
        ScreenSupport.addKeyboardSupport(this);

        label = new WrappedLabel() {

            @Override
            protected int getLabelWidth() {
                return (int) panel.getSize().getWidth();
            }
        };
        Font bold = label.getFont().deriveFont(Font.BOLD);
        label.setFont(bold);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = GridBagConstraints.REMAINDER;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.NORTH;
        panel.add(label, constraints);
    }

    public boolean isTopLevelDisplay() {
        return false;
    }

    public Collection<DesignComponent> getChildren() {
        return Collections.emptyList();
    }

    public JComponent getView() {
        return panel;
    }

    protected final JPanel getViewPanel() {
        return panel;
    }

    protected final void setContentComponent(JComponent contentComponent) {
        panel.setVisible(false);
        if (this.contentComponent != null) {
            panel.remove(this.contentComponent);
        }
        this.contentComponent = contentComponent;
        if (contentComponent != null) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridx = GridBagConstraints.REMAINDER;
            constraints.gridy = GridBagConstraints.RELATIVE;
            constraints.anchor = GridBagConstraints.NORTHEAST;

            panel.add(contentComponent, constraints);
        }
        panel.setVisible(true);
        panel.invalidate();
        panel.validate();
        panel.repaint();
    }

    public void reload(ScreenDeviceInfo deviceInfo) {
        String text = null;
        if (MidpDatabindingSupport.getConnector(getComponent(), ItemCD.PROP_LABEL) != null) {
            text = java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/screen/display/Bundle").getString("LBL_Databinding"); //NOI18N 
        } else {
            text = text = MidpValueSupport.getHumanReadableString(getComponent().readProperty(ItemCD.PROP_LABEL));
        }
        label.setText(text);

        int width = -1;
        if (getComponent().readProperty(ItemCD.PROP_PREFERRED_WIDTH).getKind() == PropertyValue.Kind.VALUE) {
            width = Integer.parseInt(getComponent().readProperty(ItemCD.PROP_PREFERRED_WIDTH).getPrimitiveValue().toString());
        }
        label.setPreferedWidth(width);

        label.repaint();
        label.revalidate();
    }

    public Shape getSelectionShape() {
        return new Rectangle(panel.getSize());
    }

    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        return Collections.singleton(
                new ScreenPropertyDescriptor(getComponent(), label, new ScreenStringPropertyEditor(ItemCD.PROP_LABEL)));
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    @Override
    public AcceptSuggestion createSuggestion(Transferable transferable) {
        if (!(transferable.isDataFlavorSupported(ScreenDisplayDataFlavorSupport.HORIZONTAL_POSITION_DATA_FLAVOR))) {
            return null;
        }
        if (!(transferable.isDataFlavorSupported(ScreenDisplayDataFlavorSupport.VERTICAL_POSITION_DATA_FLAVOR))) {
            return null;
        }

        ScreenDeviceInfo.Edge horizontalPosition = null;
        ScreenDeviceInfo.Edge verticalPosition = null;

        try {
            horizontalPosition = (ScreenDeviceInfo.Edge) transferable.getTransferData(ScreenDisplayDataFlavorSupport.HORIZONTAL_POSITION_DATA_FLAVOR);
            verticalPosition = (ScreenDeviceInfo.Edge) transferable.getTransferData(ScreenDisplayDataFlavorSupport.VERTICAL_POSITION_DATA_FLAVOR);
        } catch (UnsupportedFlavorException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new ScreenMoveArrayAcceptSuggestion(horizontalPosition, verticalPosition);
    }
}
