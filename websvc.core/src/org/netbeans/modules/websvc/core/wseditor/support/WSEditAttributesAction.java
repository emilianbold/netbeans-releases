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

package org.netbeans.modules.websvc.core.wseditor.support;

import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditorProvider;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditorProviderRegistry;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

public final class WSEditAttributesAction extends NodeAction {
    
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            final EditWSAttributesCookie cookie = activatedNodes[0].getLookup().lookup(EditWSAttributesCookie.class);
            if (cookie!=null) {
                Runnable task = new Runnable() {
                    public void run() {
                        cookie.openWSAttributesEditor();
                    }
                };
                RequestProcessor.getDefault().post(task, 10);
            }
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(WSEditAttributesAction.class, "CTL_WSEditAttributesAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    private WSEditorProviderRegistry populateWSEditorProviderRegistry(){
        WSEditorProviderRegistry registry = WSEditorProviderRegistry.getDefault();
        if(registry.getEditorProviders().isEmpty()){
            Lookup.Result<WSEditorProvider> results = Lookup.getDefault().
                    lookup(new Lookup.Template<WSEditorProvider>(WSEditorProvider.class));
            Collection<? extends WSEditorProvider> services = results.allInstances();
            //System.out.println("###number of providers: " + services.size());
            for(WSEditorProvider provider : services){
                registry.register(provider);
            }
        }
        return registry;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        
        if(activatedNodes.length == 1){
            WSEditorProviderRegistry registry =
                    populateWSEditorProviderRegistry();
            Set<WSEditorProvider> providers = registry.getEditorProviders();
            if(providers.size() == 0){
                return false;
            }
            Node node = activatedNodes[0];
            for(WSEditorProvider provider : providers){
                //look for the first one that is enabled and return true
                if(provider.enable(node)){
                    return true;
                }
            }
        }
        return false;
        
    }
}

