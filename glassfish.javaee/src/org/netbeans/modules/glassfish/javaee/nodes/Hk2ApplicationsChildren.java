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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.glassfish.javaee.nodes;

import java.util.Vector;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager;
import org.netbeans.modules.glassfish.javaee.ide.Hk2TargetModuleID;
import org.netbeans.modules.glassfish.javaee.ide.Hk2TargetModuleID;
import org.netbeans.modules.glassfish.javaee.nodes.actions.Refreshable;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class Hk2ApplicationsChildren extends Children.Keys implements Refreshable {
    
    private Lookup lookup;
    private final static Node WAIT_NODE = Hk2ItemNode.createWaitNode();
    
    Hk2ApplicationsChildren(Lookup lookup) {
        this.lookup = lookup;
    }
    
    public void updateKeys(){
        setKeys(new Object[] {WAIT_NODE});
        
        RequestProcessor.getDefault().post(new Runnable() {
            Vector keys = new Vector();
            Hk2DeploymentManager dm = (Hk2DeploymentManager)lookup.lookup(Hk2DeploymentManager.class);
            
            public void run() {
                
                try {
                    for(TargetModuleID id:dm.getRunningModules(ModuleType.EAR, dm.getTargets())) {
                        String name = ((Hk2TargetModuleID)id).getModuleID();
                        keys.add(new Hk2ItemNode(lookup, id, Hk2ItemNode.ItemType.J2EE_APPLICATION));
                    }
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                
                setKeys(keys);
            }
        }, 0);
    }
    
    @Override
    protected void addNotify() {
        updateKeys();
    }
    
    @Override
    protected void removeNotify() {
        setKeys(java.util.Collections.EMPTY_SET);
    }
    
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof Hk2ItemNode){
            return new Node[]{(Hk2ItemNode)key};
        }
        
        if (key instanceof String && key.equals(WAIT_NODE)){
            return new Node[]{WAIT_NODE};
        }
        
        return null;
    }
}