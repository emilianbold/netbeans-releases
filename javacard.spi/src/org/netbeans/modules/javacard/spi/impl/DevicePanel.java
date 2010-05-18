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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.spi.impl;

import java.awt.EventQueue;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.swing.layouts.SharedLayoutPanel;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ChoiceView;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.Cards;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.DeviceManagerDialogProvider;
import org.netbeans.modules.javacard.spi.PlatformAndDeviceProvider;

/**
 *
 * @author Tim Boudreau
 */
public class DevicePanel extends SharedLayoutPanel implements ExplorerManager.Provider, ActionListener, PropertyChangeListener {

    private final ExplorerManager mgr = new ExplorerManager();
    private ExplorerManager parentManager;
    private PlatformAndDeviceProvider props;
    private final SettableProxyLookup lkp = new SettableProxyLookup();
    private final ChoiceView choice = new ChoiceView();
    private final JButton button = new JButton(NbBundle.getMessage(DevicePanel.class,
            "LBL_MANAGE_DEVICES")); //NOI18N
    private final JLabel lbl = new JLabel(NbBundle.getMessage(DevicePanel.class,
            "LBL_DEVICES")); //NOI18N

    public DevicePanel() {
        this(null);
    }

    public DevicePanel(final PlatformAndDeviceProvider props) {
        add(lbl);
        add(choice);
        add(button);
        button.addActionListener(this);
        Mnemonics.setLocalizedText(button, button.getText());
        Mnemonics.setLocalizedText(lbl, lbl.getText());
        lbl.setLabelFor(choice);
        mgr.setRootContext(noPlatformNode());
        mgr.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    updateLookup();
                    Lookup b = mgr.getSelectedNodes().length > 0 ? mgr.getSelectedNodes()[0].getLookup() : Lookup.EMPTY;
                    Card card = b.lookup(Card.class);
                    if (DevicePanel.this.props != null && card != null) {
                        DevicePanel.this.props.setActiveDevice(card.getSystemId());
                    }
                }
            }
        });
        if (props != null) {
            setPlatformAndDevice(props);
        }
    }

    public Lookup getLookup() {
        return lkp;
    }

    void setRoot(Node n) {
        mgr.setRootContext(n);
    }

    void setSelectedNode(Node n) {
        try {
            if (n != null) { //Can be null w/ very severely broken project properties
                mgr.setSelectedNodes(new Node[]{n});
            } else {
                mgr.setSelectedNodes(new Node[0]);
            }
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    private Node noPlatformNode() {
        AbstractNode result = new AbstractNode(Children.LEAF);
        result.setIconBaseWithExtension("org/netbeans/modules/javacard/resources/empty.png"); //NOI18N
        result.setDisplayName(NbBundle.getMessage(DevicePanel.class, "LBL_NO_PLATFORM_SELECTED")); //NOI18N
        return result;
    }

    private Node invalidPlatformNode() {
        AbstractNode result = new AbstractNode(Children.LEAF);
        result.setIconBaseWithExtension("org/netbeans/modules/javacard/resources/empty.png"); //NOI18N
        result.setDisplayName(NbBundle.getMessage(DevicePanel.class, "LBL_INVALID_PLATFORM_SELECTED")); //NOI18N
        return result;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ExplorerManager.Provider prov = (ExplorerManager.Provider) SwingUtilities.getAncestorOfClass(ExplorerManager.Provider.class, this);
        if (prov != null) {
            parentManager = prov.getExplorerManager();
            parentManager.addPropertyChangeListener(this);
            propertyChange(null);
        }
    }

    @Override
    public void removeNotify() {
        if (parentManager != null) {
            parentManager.removePropertyChangeListener(this);
            parentManager = null;
        }
        super.removeNotify();
    }

    public void actionPerformed(ActionEvent e) {
        DataObject dob = Utils.findPlatformDataObjectNamed(props.getPlatformName());
        assert dob != null : "Button invoked w/o any selected platform"; //NOI18N
        DeviceManagerDialogProvider prov = dob.getLookup().lookup(DeviceManagerDialogProvider.class);
        assert prov != null;
        prov.showManageDevicesDialog(this);
    }

    private void updateLookup() {
        Lookup a = parentManager == null || parentManager.getSelectedNodes().length == 0 ? Lookup.EMPTY : parentManager.getSelectedNodes()[0].getLookup();
        Lookup b = mgr.getSelectedNodes().length > 0 ? mgr.getSelectedNodes()[0].getLookup() : Lookup.EMPTY;
        lkp.setFirstLookup(a);
        lkp.setSecondLookup(b);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null || ExplorerManager.PROP_ROOT_CONTEXT.equals(evt.getPropertyName()) || ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] sel = parentManager.getSelectedNodes();
            final String cardName = props.getActiveDevice();
            JavacardPlatform pform = sel == null || sel.length == 0 ? null :
                sel[0].getLookup().lookup(JavacardPlatform.class);
            if (pform == null || !pform.isValid()) {
                setRoot(invalidPlatformNode());
                button.setEnabled(false);
                choice.setEnabled(false);
            } else {
                Cards cards = pform.getCards();
                setRoot(new AbstractNode(cards.createChildren(cardName)));
                    //Get everybody's mutexes out of each other's way
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            Node selNode = null;
                            Node[] nds = mgr.getRootContext().getChildren().getNodes(true);
                            for (Node n : nds) {
                                Card card = n.getLookup().lookup(Card.class);
                                if (card != null && cardName.equals(card.getSystemId())) {
                                    selNode = n;
                                    break;
                                }
                            }
                            Node n = mgr.getRootContext().getChildren().findChild(cardName);
                            if (n != null) {
                                setSelectedNode(n);
                                Card c = n.getLookup().lookup(Card.class);
                                boolean enableButton = c != null && c.isValid();
                                if (enableButton) {
                                    enableButton = parentManager.getSelectedNodes()[0].getLookup().lookup(
                                            DeviceManagerDialogProvider.class) != null;
                                }
                                button.setEnabled(enableButton);
                                choice.setEnabled(true);
                            } else {
                                button.setEnabled(false);
                                choice.setEnabled(true);
                            }
                        }
                    });
            }
        }
        updateLookup();
    }

    public void setPlatformAndDevice(PlatformAndDeviceProvider props) {
        this.props = props;
    }
}
