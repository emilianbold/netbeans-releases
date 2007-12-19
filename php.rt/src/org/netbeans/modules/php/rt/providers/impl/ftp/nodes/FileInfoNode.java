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
package org.netbeans.modules.php.rt.providers.impl.ftp.nodes;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.Action;
import org.netbeans.modules.php.rt.providers.impl.DefaultServerCustomizer;
import org.netbeans.modules.php.rt.providers.impl.ServerCustomizerComponent;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpHostImpl;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpServerFileCustomizerComponent;
import org.netbeans.modules.php.rt.providers.impl.ftp.nodes.actions.RefreshAction;
import org.netbeans.modules.php.rt.providers.impl.nodes.AbstractInfoNode;
import org.netbeans.modules.php.rt.resources.ResourceMarker;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.openide.actions.CustomizeAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author avk
 */
public class FileInfoNode extends AbstractInfoNode {

    public FileInfoNode(FtpHostImpl host) {
        super(Children.LEAF, 
                createLookup(new FtpBaseObjectNodeChildren(), host));
        setChildren(getLookup().lookup(Children.class));
        initNode(host);
    }

    private void initNode(FtpHostImpl host){
        String name = getNodeName(host);
        String description = getNodeDescription(host);

        setName(name);
        setDisplayName(name);
        setIcon();
        setShortDescription(description);
    }

    private String getNodeName(FtpHostImpl host){
        return truncateName(getNodeDescription(host));
    }
    
    private String getNodeDescription(FtpHostImpl host){
        String name = FtpHostImpl.Helper.getFtpUrl(host);
        if (name == null){
            name = FtpHostImpl.Helper.noFtpMessage();
        }
        return name;
    }
    
    public void setIcon() {
        setIconBaseWithExtension(ResourceMarker.getLocation() 
                + ResourceMarker.SERVER_FILE_ICON);
    }

    public void setErrorIcon() {
        setIconBaseWithExtension(ResourceMarker.getLocation() 
                + ResourceMarker.SERVER_FILE_ERROR_ICON);
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
        
        ArrayList<Action> actionsList = new ArrayList();
        
        actionsList.addAll(Arrays.asList(ACTIONS));
        
        if (hasCustomizer()) {
            actionsList.add(SystemAction.get(CustomizeAction.class));
        }
        
        return actionsList.toArray(new Action[]{});
   }

    @Override
    protected ServerCustomizerComponent getCustomizerComponent(
            DefaultServerCustomizer parentDialog) 
    {
        Host host = getLookup().lookup(Host.class);
        if (host != null && host instanceof FtpHostImpl) {
            FtpHostImpl impl = (FtpHostImpl) host;
            return new FtpServerFileCustomizerComponent(impl, parentDialog);
        }
        return null;
    }

    static Lookup createLookup(FtpBaseObjectNodeChildren children, FtpHostImpl host) {
        InstanceContent ic = new InstanceContent();

        if (host != null) {
            ic.add(host);
        }
        ic.add(children);
        return new AbstractLookup(ic);
    }

    private static final Action[] ACTIONS = new Action[]{
        SystemAction.get(RefreshAction.class)
    };
}
