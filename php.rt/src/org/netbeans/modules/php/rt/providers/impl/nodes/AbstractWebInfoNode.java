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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl.nodes;

import org.netbeans.modules.php.rt.providers.impl.ftp.nodes.*;
import javax.swing.Action;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.resources.ResourceMarker;
import org.openide.actions.CustomizeAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author avk
 */
public abstract class AbstractWebInfoNode extends AbstractInfoNode {
    
    public AbstractWebInfoNode( HostImpl host ) {
        this(Children.LEAF, host);
    }
    
    public AbstractWebInfoNode(Children children, HostImpl host ) {
        super(children, createLookup(host));
        
        setDisplayName( getNodeName(host) );
        setName(getDisplayName());
        setIconBaseWithExtension(ResourceMarker.getLocation()
                + ResourceMarker.SERVER_WEB_ICON);
        setShortDescription( getNodeDescription(host) );
    }
    
    private String getNodeName(HostImpl host){
        return truncateName(getNodeDescription(host));
    }
    
    private String getNodeDescription(HostImpl host){
        String name = HostImpl.Helper.getHttpUrl(host);
        if (name == null){
            name = HostImpl.Helper.noHttpMessage();
        }
        return name;
    }
    
    @Override
    public boolean hasCustomizer() {
        Node parent = getParentNode();
        if (parent != null) {
            return parent.hasCustomizer();
        }
        return false;
    }

    @Override
    public Action[] getActions(boolean context) {
        if (hasCustomizer()) {
            return new Action[]{SystemAction.get(CustomizeAction.class)};
        }
        return new Action[]{};
    }
    
    static Lookup createLookup( HostImpl host ) {
        if (host != null) {
            return Lookups.fixed(new Object[] { host });
        }
        else {
            return null;
        }
    }
    
}
