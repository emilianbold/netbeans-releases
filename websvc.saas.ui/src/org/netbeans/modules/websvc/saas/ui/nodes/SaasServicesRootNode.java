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
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.ui.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.jaxb.Group;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author nam
 */
public class SaasServicesRootNode extends AbstractNode {
    static final SaasGroup PLACE_HOLDER_GROUP = new SaasGroup(null, new Group());
    
    public SaasServicesRootNode() {
        super(new RootNodeChildren(PLACE_HOLDER_GROUP));
    }

    @Override
    public String getName() {
        return "rootSaasGroup";
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SaasServicesRootNode.class, "Web_Services");
    }
    
    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(SaasServicesRootNode.class, "Web_Services_Desc");
    }
    
    static final java.awt.Image ICON =
            org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/webservicegroup.png" ); //NOI18N
    
    @Override
    public Image getIcon(int type){
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return ICON;
    }
    
    static class RootNodeChildren extends SaasGroupNodeChildren {

        public RootNodeChildren(SaasGroup group) {
            super(group);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == SaasServicesModel.getInstance().getRootGroup() ||
                evt.getNewValue() == SaasServicesModel.State.READY) {
                super.setGroup(SaasServicesModel.getInstance().getRootGroup());
                updateKeys();
            }
            super.propertyChange(evt);
        }
    
        @Override
        protected void updateKeys() {
            if (needsWait()) {
                SaasServicesModel.getInstance().initRootGroup();
                setKeys(SaasNodeChildren.WAIT_HOLDER);
            } else {
                super.updateKeys();
            }
        }
        
        private boolean needsWait() {
            return SaasServicesModel.getInstance().getState() != SaasServicesModel.State.READY;
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            if (needsWait()) {
                return SaasNodeChildren.getWaitNode();
            }
            return super.createNodes(key);
        }
    }
}
