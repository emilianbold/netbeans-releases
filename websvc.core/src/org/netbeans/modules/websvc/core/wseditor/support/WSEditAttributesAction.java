/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
    protected boolean asynchronous() {
        return false;
    }
    
    private WSEditorProviderRegistry populateWSEditorProviderRegistry(){
        WSEditorProviderRegistry registry = WSEditorProviderRegistry.getDefault();
        if(registry.getEditorProviders().isEmpty()){
            Lookup.Result results = Lookup.getDefault().
                    lookup(new Lookup.Template(WSEditorProvider.class));
            Collection<WSEditorProvider> services = results.allInstances();
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

