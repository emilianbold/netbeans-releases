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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.ui.basic.spi.AppInfoProvider;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.ReferencingNodeProvider;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;

/**
 *
 * @author  Ajit Bhate
 */
public class AnnotationChildren extends CategorizedChildren<Annotation> {
    public AnnotationChildren(SchemaUIContext context,
            SchemaComponentReference<Annotation> reference) {
        super(context,reference);
    }
    
    
    /**
     *
     *
     */
    protected List<Node> createKeys() {
        List<Node> keys=super.createKeys();
        Lookup.Result providerLookups =
                Lookup.getDefault().lookup(new Lookup.Template(AppInfoProvider.class));
        Collection providers = providerLookups.allInstances();
        if (providers != null && !providers.isEmpty()) {
            ArrayList<Node> customNodes = new ArrayList<Node>();
            ArrayList<AppInfo> customAppInfos = new ArrayList<AppInfo>();
            Node parent = getNode();
            if(parent!=null) {
                SchemaComponentNode scn = (SchemaComponentNode)parent.getCookie
                        (SchemaComponentNode.class);
                if(scn!=null) {
                ArrayList<Node> path = new ArrayList<Node>();
                    path.add(parent);
                    while(true) {
                        parent = parent.getParentNode();
                        if(parent == null) {
                            ReferencingNodeProvider refProvider =
                                    (ReferencingNodeProvider)path.get(0).getLookup().
                                    lookup(ReferencingNodeProvider.class);
                            if(refProvider!=null) parent = refProvider.getNode();
                        }
                        if (parent == null) break;
                        path.add(0,parent);
                    }
                    for(Object provider:providers) {
                        Node customNode = null;
                        AppInfoProvider aiProvider = (AppInfoProvider)provider;
                        if(aiProvider.isActive(getReference().get().getModel()) &&
                                (customNode = aiProvider.getNode(path))!=null) {
                            customNodes.add(customNode);
                            AppInfo customAppInfo = (AppInfo)customNode.
                                    getLookup().lookup(AppInfo.class);
                            if(customAppInfo!=null)
                                customAppInfos.add(customAppInfo);
                        }
                    }
                }
            }
            for(int idx = keys.size(); idx>0; idx--) {
                Node n = keys.get(idx-1);
                SchemaComponentNode scn = (SchemaComponentNode)n.getCookie
                        (SchemaComponentNode.class);
                if(scn!=null && scn.getReference().get() instanceof AppInfo) {
                    AppInfo appInfo = (AppInfo)scn.getReference().get();
                    if(customAppInfos.contains(appInfo)) keys.remove(n);
                }
            }
            keys.addAll(customNodes);
        }
        
        return keys;
    }
}
