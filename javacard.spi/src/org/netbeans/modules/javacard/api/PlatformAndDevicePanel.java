/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.javacard.api;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.modules.javacard.common.GuiUtils;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.Cards;
import org.netbeans.modules.javacard.spi.DeviceManagerDialogProvider;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import org.netbeans.modules.javacard.spi.PlatformAndDeviceProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.netbeans.modules.javacard.spi.impl.TempPlatformAndDeviceProvider;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationGroupProvider;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.HtmlRenderer.Renderer;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Tim Boudreau
 */
public final class PlatformAndDevicePanel extends JPanel implements ActionListener, ValidationGroupProvider, Lookup.Provider {
    private final JComboBox platforms;
    private final JComboBox devices;
    private final JButton manageCardsButton = new JButton(NbBundle.getMessage(PlatformAndDevicePanel.class,
            "LBL_MANAGE_DEVICES"));
    private final JButton managePlatformsButton = new JButton(NbBundle.getMessage(PlatformAndDevicePanel.class,
            "LBL_MANAGE_PLATFORMS"));
    private final R r = new R();
    private final ValidationGroup grp = ValidationGroup.create();
    private PlatformAndDeviceProvider props;
    private final InstanceContent content = new InstanceContent();
    private final AbstractLookup lkp = new AbstractLookup(content);

    public PlatformAndDevicePanel() {
        this (new TempPlatformAndDeviceProvider());
    }

    public PlatformAndDevicePanel(PlatformAndDeviceProvider props) {
        super (new GridBagLayout());
        if (props == null) {
            props = new TempPlatformAndDeviceProvider();
        }
        this.props = props;
        platforms = new JComboBox();
        devices = new JComboBox();
        platforms.setRenderer(r);
        devices.setRenderer(r);
        fullInit();
        int gap = Utilities.isMac() ? 12 : 5;
        setBorder (BorderFactory.createEmptyBorder (gap, gap, gap, gap));
        JLabel platformsLabel = new JLabel(NbBundle.getMessage(PlatformAndDevicePanel.class, "LBL_PLATFORMS")); //NOI18N
        platformsLabel.setLabelFor(platforms);
        JLabel cardsLabel = new JLabel(NbBundle.getMessage(PlatformAndDevicePanel.class, "LBL_DEVICES"));
        cardsLabel.setLabelFor(devices);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, gap, gap);
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add (platformsLabel, gbc);
        gbc.gridy = 1;
        add (cardsLabel, gbc);

        gbc.insets = new Insets(0, gap, 0, 0);
        gbc.gridx = 2;
        gbc.weightx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add (platforms, gbc);
        gbc.gridy = 1;
        add (devices, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, gap, 0);
        gbc.gridy = 0;
        gbc.gridx = 3;
        add (managePlatformsButton, gbc);
        gbc.gridy = 1;
        add (manageCardsButton, gbc);

        manageCardsButton.addActionListener(this);
        managePlatformsButton.addActionListener(this);
        platforms.addActionListener(this);
        devices.addActionListener(this);
        GuiUtils.prepareContainer(this);
        grp.add(platforms, new PlatformValidator());
        grp.add(devices, new CardValidator());
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.CustomizeDevice"); //NOI18N
    }

    public Lookup getLookup() {
        return lkp;
    }

    void fullInit() {
        platforms.setModel(new PlatformsModel());
        platformsChanged();
    }

    public void setPlatformAndCard(PlatformAndDeviceProvider prov) {
        if (prov == null) {
            prov = new TempPlatformAndDeviceProvider();
        }
        this.props = prov;
        fullInit();
        updateLookup();
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (o == platforms) {
            platformsChanged();
        } else if (o == devices) {
            cardsChanged();
        } else if (o == managePlatformsButton) {
            managePlatforms();
        } else if (o == manageCardsButton) {
            manageCards();
        }
        updateLookup();
    }

    private void platformsChanged() {
        if (platforms == null) {
            return;
        }
        JavacardPlatform pform = platforms.getSelectedItem() instanceof JavacardPlatform ? (JavacardPlatform) platforms.getSelectedItem() : null;
        updateCardsModel();
        devices.setEnabled(pform != null && pform.isValid());
        DataObject dob = Utils.findPlatformDataObjectNamed(props.getPlatformName());
        DeviceManagerDialogProvider prov = dob == null ? null : dob.getLookup().lookup(DeviceManagerDialogProvider.class);
        manageCardsButton.setEnabled(pform != null && pform.isValid() && prov != null);
        grp.validateAll();
        updateLookup();
    }

    private void updateCardsModel() {
        JavacardPlatform pform = platforms.getSelectedItem() instanceof JavacardPlatform ? (JavacardPlatform) platforms.getSelectedItem() : null;
        if (pform != null) {
            devices.setModel(new CardsModel(pform));
            props.setPlatformName(pform.getSystemName());
        } else {
            devices.setModel(new CardsModel(JavacardPlatform.createBrokenJavacardPlatform("null")));
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        grp.validateAll();
        updateLookup();
    }

    private void updateLookup() {
        Set<Object> stuff = new HashSet<Object>();
        JavacardPlatform pform = platforms.getSelectedItem() instanceof JavacardPlatform ? (JavacardPlatform) platforms.getSelectedItem() : null;
        if (pform != null) {
            stuff.add(pform);
        }
        if (devices.getSelectedItem() instanceof Card) {
            stuff.add (devices.getSelectedItem());
        }
        content.set(stuff, null);
    }

    private void cardsChanged() {
        Card card = devices.getSelectedItem() instanceof Card ? (Card) devices.getSelectedItem() : null;
        if (card != null) {
            props.setActiveDevice(card.getSystemId());
        }
        grp.validateAll();
        updateLookup();
    }

    private void managePlatforms() {
        JavacardPlatform toSelect = (JavacardPlatform) platforms.getSelectedItem();
        PlatformsCustomizer.showCustomizer(toSelect);
        fullInit();
    }

    private void manageCards() {
        DataObject dob = Utils.findPlatformDataObjectNamed(props.getPlatformName());
        DeviceManagerDialogProvider prov = dob.getLookup().lookup(DeviceManagerDialogProvider.class);
        assert prov != null;
        prov.showManageDevicesDialog(this);
        updateCardsModel();
    }

    public ValidationGroup getValidationGroup() {
        return grp;
    }

    public void setPlatformAndCard(String activePlatform, String activeDevice) {
        //If we have a default device set from the ProjectDefinitionPanel's
        //constructor, and we're being passed nulls because nothing was
        //yet stored in the wizard descriptor, don't destroy the default
        //value already in it
        PlatformAndDeviceProvider tempProps = props == null ? new TempPlatformAndDeviceProvider() : props;

        if (activeDevice != null) {
            tempProps.setActiveDevice(activeDevice);
        }
        if (activePlatform != null) {
            tempProps.setPlatformName(activePlatform);
        }
        setPlatformAndCard(tempProps);
        updateLookup();
    }

    private static class PlatformValidator implements Validator<ComboBoxModel> {

        public boolean validate(Problems prblms, String string, ComboBoxModel t) {
            if (t.getSelectedItem() == null || !(t.getSelectedItem() instanceof JavacardPlatform)) {
                prblms.add(NbBundle.getMessage(PlatformValidator.class, "LBL_NO_PLATFORM_SELECTED")); //NOI18N
                return false;
            }
            if (t.getSelectedItem() instanceof JavacardPlatform) {
                JavacardPlatform p = (JavacardPlatform) t.getSelectedItem();
                if (!p.isValid()) {
                    String nm = p.getDisplayName();
                    prblms.add(NbBundle.getMessage(PlatformValidator.class, "MSG_BAD_PLATFORM",
                            nm)); //NOI18N
                    return false;
                }
            }
            return true;
        }
    }

    private static class CardValidator implements Validator<ComboBoxModel> {

        public boolean validate(Problems prblms, String string, ComboBoxModel t) {
            if (t.getSelectedItem() == null || !(t.getSelectedItem() instanceof Card)) {
                prblms.add(NbBundle.getMessage(PlatformValidator.class, "LBL_NO_CARD_SELECTED")); //NOI18N
                return false;
            }
            if (t.getSelectedItem() instanceof Card) {
                Card c = (Card) t.getSelectedItem();
                if (!c.isValid()) {
                    prblms.add(NbBundle.getMessage(PlatformValidator.class, "MSG_BAD_CARD")); //NOI18N
                    return false;
                }
            }
            return true;
        }

    }

    private class R extends DefaultListCellRenderer {
        Renderer ren = HtmlRenderer.createRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean leadSelection) {
            Image image = null;
            if (value instanceof JavacardPlatform) {
                JavacardPlatform pl = (JavacardPlatform) value;
                value = pl.getDisplayName();
                DataObject dob = Utils.findPlatformDataObjectNamed(pl.getSystemName());
                image = dob == null ? null : dob.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
            } else if (value instanceof Card) {
                Card card = (Card) value;
                CardInfo info = card.getLookup().lookup(CardInfo.class);
                value = info.getDisplayName();
                image = info.getIcon();
                if (!card.isValid()) {
                    value = "<font color='!controlShadow'>" + value;
                }
            } else {
                if (value == null) {
                    value = NbBundle.getMessage(R.class, "NULL");
                }
            }
            ren.setHtml(true);
            JLabel lbl = (JLabel) ren.getListCellRendererComponent(list, value, index, isSelected, leadSelection);
            ren.setHtml(true);
            if (image != null) {
                ren.setIcon(ImageUtilities.image2Icon(image));
                ren.setIconTextGap(3);
            }
            ren.setText(value.toString());
            return lbl;
        }
    }

    private final class PlatformsModel implements ComboBoxModel, FileChangeListener, Runnable {
        private JavacardPlatform sel;
        private final List<JavacardPlatform> list = Collections.synchronizedList(new ArrayList<JavacardPlatform>());
        PlatformsModel() {
            String name = props == null ? null : props.getPlatformName();
            refresh();
            if (name != null) {
                boolean found = false;
                for (JavacardPlatform fo : list) {
                    if (name.equals(fo.getSystemName())) {
                        sel = fo;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    String expectedCard = props == null ? null : props.getActiveDevice();
                    if (expectedCard != null) {
                        sel = JavacardPlatform.createBrokenJavacardPlatform(name, Arrays.asList(expectedCard));
                    } else {
                        sel = JavacardPlatform.createBrokenJavacardPlatform(name);
                    }
                }
            }
        }

        void refresh() {
            JavacardPlatform old = sel;
            list.clear();
            boolean found = false;
            JavacardPlatform first = null;
            for (FileObject fo : Utils.findAllRegisteredJavacardPlatformFiles()) {
                try {
                    JavacardPlatform pform = DataObject.find(fo).getNodeDelegate().getLookup().lookup(JavacardPlatform.class);
                    list.add(pform);
                    if (first == null) {
                        first = pform;
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
                found = old != null && old.equals(sel);
            }
            if (!found) {
                if (old != null) {
                    sel = JavacardPlatform.createBrokenJavacardPlatform(old.getSystemName());
                } else {
                    sel = first;
                }
            }
            for (ListDataListener l : ls) {
                l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, list.size()));
            }
            platformsChanged();
        }

        public void setSelectedItem(Object anItem) {
            if (anItem == null && list.size() > 0) {
                anItem = list.get(0);
            }
            boolean change;
            if (anItem == null) {
                change = true;
                sel = null;
            }
            JavacardPlatform jc = (JavacardPlatform) anItem;
            if (sel != jc) {
                sel = jc;
                for (ListDataListener l : ls) {
                    l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
                }
            }
            updateLookup();
        }

        public Object getSelectedItem() {
            if (sel == null && list.size() == 0) {
                return NbBundle.getMessage(PlatformAndDevicePanel.class,
                        "MSG_CREATE_A_PLATFORM"); //NOI18N
            }
            return sel;
        }

        public int getSize() {
            return list.size();
        }

        public Object getElementAt(int index) {
            return list.get(index);
        }

        private final List<ListDataListener> ls = new ArrayList<ListDataListener>();
        public void addListDataListener(ListDataListener l) {
            ls.add(l);
        }

        public void removeListDataListener(ListDataListener l) {
            ls.remove(l);
        }

        public void run() {
            try {
                refresh();
            } finally {
                enqueued = false;
            }
        }

        private volatile boolean enqueued;
        public void doRefresh() {
            if (!enqueued) EventQueue.invokeLater(this);
        }

        public void fileFolderCreated(FileEvent fe) {
            //do nothing
        }

        public void fileDataCreated(FileEvent fe) {
            refresh();
        }

        public void fileChanged(FileEvent fe) {
            refresh();
        }

        public void fileDeleted(FileEvent fe) {
            refresh();
        }

        public void fileRenamed(FileRenameEvent fe) {
            refresh();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            //do nothing
        }
    }

    private class CardsModel implements ComboBoxModel, ChangeListener {
        private final Cards cards;
        private final List<Card> list = new ArrayList<Card>();
        private Card sel;
        public CardsModel(JavacardPlatform pl) {
            this.cards = pl.getCards();
            refresh();
            cards.addChangeListener(WeakListeners.change(this, cards));
        }

        void refresh() {
            list.addAll(cards.getCards(false));
            String name = props == null ? null : props.getActiveDevice();
            if (name != null) {
                Card c = cards.find(name, true);
                if (!list.contains(c)) {
                    list.add(c);
                }
                sel = c;
            }
        }

        public void setSelectedItem(Object anItem) {
            if (anItem == null && list.size() > 0) {
                anItem = list.get(0);
            }
            sel = (Card) anItem;
            updateLookup();
        }

        public Object getSelectedItem() {
            if (sel == null) {
                return "<font color='!controlShadow'>" + //NOI18N
                        NbBundle.getMessage(CardsModel.class, "MSG_NO_CARDS"); //NOI18N
            }
            return sel;
        }

        public int getSize() {
            return list.size();
        }

        public Object getElementAt(int index) {
            return index == -1 ? sel : list.get(index);
        }

        private final List<ListDataListener> ls = new ArrayList<ListDataListener>();
        public void addListDataListener(ListDataListener l) {
            ls.add(l);
        }

        public void removeListDataListener(ListDataListener l) {
            ls.remove(l);
        }

        public void stateChanged(ChangeEvent e) {
            refresh();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    grp.validateAll();
                }
            });
            
        }
    }

    /*

    PlatformAndDeviceProvider props;
    private final ExplorerManager mgr = new ExplorerManager();
    DevicePanel devicePanel = new DevicePanel();
    PlatformPanel platformPanel = new PlatformPanel();

    public PlatformAndDevicePanel(PlatformAndDeviceProvider props) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(platformPanel);
        add(devicePanel);
        if (props != null) {
            setPlatformAndCard(props);
        }
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.CustomizeDevice"); //NOI18N
    }

    public PlatformAndDevicePanel() {
        this(null);
    }

    public Lookup getLookup() {
        return devicePanel.getLookup();
    }

    void setRoot(Node root) {
        mgr.setRootContext(root);
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    public void setPlatformAndCard(String activePlatform, String activeDevice) {
        //If we have a default device set from the ProjectDefinitionPanel's
        //constructor, and we're being passed nulls because nothing was
        //yet stored in the wizard descriptor, don't destroy the default
        //value already in it
        PlatformAndDeviceProvider tempProps = props == null ? new TempPlatformAndDeviceProvider() : props;

        if (activeDevice != null) {
            tempProps.setActiveDevice(activeDevice);
        }
        if (activePlatform != null) {
            tempProps.setPlatformName(activePlatform);
        }
        setPlatformAndCard(tempProps);
    }

    public void setPlatformAndCard(final PlatformAndDeviceProvider props) {
        this.props = props;
        platformPanel.setPlatformFrom(props);
        devicePanel.setPlatformAndDevice(props);
        //Force children initialization with some slightly
        //ridiculous contortions.  This call *should* block for
        //children creation but in fact doesn't.  However, our ChildFactory
        //will re-trigger an update when the time comes
        mgr.getRootContext().getChildren().getNodes(true);
        Children children = Children.create(new JavacardPlatformChildren() {

            @Override
            protected void onAllNodesCreated() {
                assert PlatformAndDevicePanel.this.props != null;
                String platformName = props.getPlatformName();
                String deviceName = props.getActiveDevice();
                updateSelectedNode(platformName, deviceName);
            }
        }, false);
        AbstractNode realRoot = new AbstractNode(children);
        realRoot.setIconBaseWithExtension("org/netbeans/modules/javacard/resources/empty.png"); //NOI18N
        realRoot.setDisplayName(NbBundle.getMessage(PlatformAndDevicePanel.class,
                "LBL_SELECT_SERVER")); //NOI18N
        setRoot(realRoot);
    }

    private void setSelectedNode(Node n) {
        try {
            mgr.setSelectedNodes(new Node[]{n});
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void updateSelectedNode(final String platformName, String deviceName) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                Node[] nodes = mgr.getRootContext().getChildren().getNodes(true);
                for (Node n : nodes) {
                    DataObject dob = n.getLookup().lookup(DataObject.class);
                    if (dob != null && platformName.equals(dob.getName())) {
                        setSelectedNode(n);
                        return;
                    }
                }
            }
        });
    }
     */
}
