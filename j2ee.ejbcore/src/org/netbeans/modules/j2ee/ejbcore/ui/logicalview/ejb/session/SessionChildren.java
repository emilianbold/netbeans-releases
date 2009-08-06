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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.SessionMethodController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Chris Webster
 * @author Martin Adamek
 */

public final class SessionChildren extends Children.Keys<SessionChildren.Key> implements PropertyChangeListener {
    
    // indexes into fields with results for model query
    private final static int REMOTE = 0;
    private final static int LOCAL = 1;
    private final static int BEAN = 2;

    public enum Key {REMOTE, LOCAL, BEAN};
    
    private final ClasspathInfo cpInfo;
    private final String ejbClass;
    private final EjbJar ejbModule;;
    private final SessionMethodController controller;
    
    public SessionChildren(EjbViewController ejbViewController) {
        this.cpInfo = ejbViewController.getClasspathInfo();
        this.ejbClass = ejbViewController.getEjbClass();
        this.ejbModule = ejbViewController.getEjbModule();
        controller = new SessionMethodController(ejbClass, ejbModule.getMetadataModel());
    }
    
    protected void addNotify() {
        super.addNotify();
        try {
            updateKeys();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        //TODO: RETOUCHE listening on model for logical view
//        session.addPropertyChangeListener(this);
    }
    
    private void updateKeys() throws IOException {
        final boolean[] results = new boolean[] { false, false, true };
        
        ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws Exception {
                Session entity = (Session) metadata.findByEjbClass(ejbClass);
                if (entity != null) {
                    results[REMOTE] = entity.getRemote() != null;
                    results[LOCAL] = entity.getLocal() != null;
//                    results[BEAN] = controller.allowsNoInterface();
                }
                return null;
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List<Key> keys = new ArrayList<Key>();
                if (results[REMOTE]) { keys.add(Key.REMOTE); }
                if (results[LOCAL]) { keys.add(Key.LOCAL); }
                if (results[BEAN]) { keys.add(Key.BEAN); }
                setKeys(keys);
            }
        });
    }
    
    protected void removeNotify() {
//        session.removePropertyChangeListener(this);
        setKeys(Collections.<Key>emptyList());
        super.removeNotify();
    }
    
    protected Node[] createNodes(Key key) {
        if (Key.LOCAL.equals(key)) {
            Children children = new MethodChildren(cpInfo, controller, controller.getLocalInterfaces(), MethodsNode.ViewType.LOCAL);
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, MethodsNode.ViewType.LOCAL);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/LocalMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_LocalMethods"));
            return new Node[] { n };
        }
        if (Key.REMOTE.equals(key)) {
            Children children = new MethodChildren(cpInfo, controller, controller.getRemoteInterfaces(), MethodsNode.ViewType.REMOTE);
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, MethodsNode.ViewType.REMOTE);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/RemoteMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_RemoteMethods"));
            return new Node[] { n };
        }
        if (Key.BEAN.equals(key)) {
            Children children = new MethodChildren(cpInfo, controller,Arrays.asList(controller.getBeanClass()), MethodsNode.ViewType.NO_INTERFACE);
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, MethodsNode.ViewType.NO_INTERFACE);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/MethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_BeanMethods"));
            return new Node[] { n };
        }
        return null;
    }
    
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
                try {
                    updateKeys();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        });
    }
    
}
