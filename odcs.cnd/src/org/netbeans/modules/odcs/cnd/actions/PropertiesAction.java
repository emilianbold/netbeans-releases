/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.odcs.cnd.actions;

import java.awt.event.ActionEvent;
import java.beans.IntrospectionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.odcs.cnd.json.VMDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Ilia Gromov
 */
public class PropertiesAction extends AbstractAction {

    private static final ImageIcon ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/cnd/resources/gear.png", true); // NOI18N
    private static final Logger LOG = Logger.getLogger(PropertiesAction.class.getName());

    private final VMDescriptor desc;

    @NbBundle.Messages({
        "remotevm.properties.action.text=Properties"
    })
    public PropertiesAction(VMDescriptor desc) {
        super(Bundle.remotevm_properties_action_text(), ICON);
        this.desc = desc;
    }

    @NbBundle.Messages({
        "# {0} - vm name",
        "remotevm.properties.title=Properties for {0}"
    })
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            BeanNode<VMDescriptor> beanNode = new BeanNode<>(desc);
            PropertySheet propertySheet = new PropertySheet();
            propertySheet.setNodes(new Node[]{beanNode});

            DialogDescriptor dd = new DialogDescriptor(propertySheet, Bundle.remotevm_properties_title(desc.getHostname()));
            DialogDisplayer.getDefault().notify(dd);
        } catch (IntrospectionException ex) {
            LOG.log(Level.INFO, "Can't show properties", ex);
        }
    }
}
