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
package org.netbeans.modules.javacard.project.ui;

import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.platform.ServersPanel;
import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.netbeans.swing.layouts.SharedLayoutPanel;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ChoiceView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

/**
 *
 * @author Tim Boudreau
 */
public class DevicePanel2 extends SharedLayoutPanel implements ExplorerManager.Provider, ActionListener, PropertyChangeListener {

    private final ExplorerManager mgr = new ExplorerManager();
    private ExplorerManager parentManager;
    private JCProjectProperties props;
    private final SettableProxyLookup lkp = new SettableProxyLookup();
    private final ChoiceView choice = new ChoiceView();
    private final JButton button = new JButton(NbBundle.getMessage(DevicePanel2.class,
            "LBL_MANAGE_DEVICES")); //NOI18N
    private final JLabel lbl = new JLabel(NbBundle.getMessage(DevicePanel2.class,
            "LBL_DEVICES")); //NOI18N

    public DevicePanel2() {
        this(null);
    }

    public DevicePanel2(final JCProjectProperties props) {
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
                    DataObject dob = b.lookup(DataObject.class);
                    if (props != null && dob != null) {
                        props.setActiveDevice(dob.getName());
                    }
                }
            }
        });
        if (props != null) {
            setProperties(props);
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
        result.setDisplayName(NbBundle.getMessage(DevicePanel2.class, "LBL_NO_PLATFORM_SELECTED")); //NOI18N
        return result;
    }

    private Node invalidPlatformNode() {
        AbstractNode result = new AbstractNode(Children.LEAF);
        result.setIconBaseWithExtension("org/netbeans/modules/javacard/resources/empty.png"); //NOI18N
        result.setDisplayName(NbBundle.getMessage(DevicePanel2.class, "LBL_INVALID_PLATFORM_SELECTED")); //NOI18N
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
        FileObject fo = Utils.sfsFolderForDeviceConfigsForPlatformNamed(props.getPlatformName(), true);
        DataObject dob;
        try {
            dob = DataObject.find(fo);
            new ServersPanel(dob.getNodeDelegate()).showDialog();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        propertyChange(null);
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
            if (sel.length == 1) { //always will be
                DataObject dob = sel[0].getLookup().lookup(DataObject.class);
                props.setPlatformName(dob.getName());
                JavacardPlatform pform = sel[0].getLookup().lookup(JavacardPlatform.class);
                if (pform == null || !pform.isValid()) {
                    setRoot(invalidPlatformNode());
                    button.setEnabled(false);
                    choice.setEnabled(false);
                } else {
                    button.setEnabled(true);
                    choice.setEnabled(true);
                    FileObject fld = Utils.sfsFolderForDeviceConfigsForPlatformNamed(props.getPlatformName(), true);
                    String deviceName = props.getActiveDevice();
                    final DataFolder df = DataFolder.findFolder(fld);
                    String toSelect = null;
                    for (FileObject fo : fld.getChildren()) {
                        if (JCConstants.JAVACARD_DEVICE_FILE_EXTENSION.equals(fo.getExt())) {
                            try {
                                if (deviceName == null) {
                                    toSelect = DataObject.find(fo).getNodeDelegate().getName();
                                    break;
                                } else {
                                    if (deviceName.equals(DataObject.find(fo).getNodeDelegate().getName())) {
                                        toSelect = DataObject.find(fo).getNodeDelegate().getName();
                                    }
                                }
                            } catch (DataObjectNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            break;
                        }
                    }
                    if (toSelect != null) {
                        final String select = toSelect;
                        setRoot(df.getNodeDelegate());
                        Mutex.EVENT.postReadRequest(new Runnable() {

                            public void run() {
                                df.getNodeDelegate().getChildren().getNodes(true);
                                setSelectedNode(mgr.getRootContext().getChildren().findChild(select));
                            }
                        });
                    } else {
                        //We have an invalid device;  this will give us a
                        //folder in a multifilesystem with a fake entry for
                        //the invalid device
                        FileObject file = Utils.folderForInvalidDeviceConfigsForPlatformNamed(sel[0].getLookup().lookup(DataObject.class).getName(), deviceName);
                        final DataFolder dof = DataFolder.findFolder(file);
                        setRoot(dof.getNodeDelegate());
                        final String select = deviceName;
                        Mutex.EVENT.postReadRequest(new Runnable() {

                            public void run() {
                                dof.getNodeDelegate().getChildren().getNodes(true);
                                Node n = mgr.getRootContext().getChildren().findChild(select);
                                setSelectedNode(n);
                            }
                        });
                    }
                }
            }
        }
        updateLookup();
    }

    void setProperties(JCProjectProperties props) {
        this.props = props;
    }
}
