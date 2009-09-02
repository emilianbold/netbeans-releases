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

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.netbeans.swing.layouts.SharedLayoutParentPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.beans.PropertyVetoException;
import org.openide.nodes.FilterNode;

/**
 *
 * @author Tim Boudreau
 */
public class PlatformAndDevicePanel extends SharedLayoutParentPanel implements ExplorerManager.Provider {

    JCProjectProperties props;
    private final ExplorerManager mgr = new ExplorerManager();
    DevicePanel2 devicePanel = new DevicePanel2();
    PlatformPanel platformPanel = new PlatformPanel();

    public PlatformAndDevicePanel(JCProjectProperties props) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(platformPanel);
        add(devicePanel);
        if (props != null) {
            setProperties(props);
        }
    }

    public PlatformAndDevicePanel() {
        this(null);
    }

    Lookup getLookup() {
        return devicePanel.getLookup();
    }

    void setRoot(Node root) {
        mgr.setRootContext(root);
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    void setServerAndDevice(String activePlatform, String activeDevice) {
        //If we have a default device set from the ProjectDefinitionPanel's
        //constructor, and we're being passed nulls because nothing was
        //yet stored in the wizard descriptor, don't destroy the default
        //value already in it
        JCProjectProperties tempProps = props == null || props.getProject() != null ? new JCProjectProperties() : props;

        if (activeDevice != null) {
            tempProps.setActiveDevice(activeDevice);
        }
        if (activePlatform != null) {
            tempProps.setPlatformName(activePlatform);
        }
        setProperties(tempProps);
    }

    public void setProperties(final JCProjectProperties props) {
        this.props = props;
        platformPanel.setProperties(props);
        devicePanel.setProperties(props);
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
                DataObject ob = Utils.createFakeJavacardPlatform(platformName);
                FileObject fld = ob.getPrimaryFile().getParent();
                final Node root = new AbstractNode(Children.create(new JavacardPlatformChildren(fld) {
                    @Override
                    protected void onAllNodesCreated() {
                        Node n = findNodeNamed(platformName);
                        setSelectedNode(n);
//                        mgr.setExploredContext(n);
                    }
                }, false));
                setRoot(root);
            }
        });
    }
}
