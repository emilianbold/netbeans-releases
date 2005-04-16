/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.nodes;

import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.websvc.registry.actions.*;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import java.awt.Image;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.Action;

/**
 * A second level node representing Group of Web Services
 * @author Winston Prakash
 */
public class WebServiceGroupNode extends AbstractNode implements WebServiceGroupCookie {
    WebServiceGroup websvcGroup;

    public WebServiceGroupNode(WebServiceGroup wsGroup) {
        super(new WebServiceGroupNodeChildren(wsGroup));
        websvcGroup = wsGroup;
        setIconBase("org/netbeans/modules/websvc/registry/resources/folder");
		
		getCookieSet().add(this);
    }

    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/registry/resources/folder-closed.png");
    }

    public Image getOpenedIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/registry/resources/folder-open.png");
    }

    public WebServiceGroup getWebServiceGroup() {
        return websvcGroup;
    }

    public boolean canRename() {
        return true;
    }

    public String getName() {
        return websvcGroup.getName();
    }

    public void setName(String name){
        websvcGroup.setName(name);
        setDisplayName(name);
        this.fireDisplayNameChange(name,null);
    }

//    protected SystemAction[] createActions() {
//        return new SystemAction[] {
//            SystemAction.get(AddWebServiceAction.class),
//            SystemAction.get(DeleteWebServiceGroupAction.class),
//            SystemAction.get(RenameAction.class)
//        };
//    }
	
	public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(AddWebServiceAction.class),
            SystemAction.get(DeleteWebServiceGroupAction.class),
            SystemAction.get(RenameAction.class)
        };
	}

    public boolean canDestroy() {
        return true;
    }

    public void destroy() throws IOException{
        WebServiceListModel wsListModel = WebServiceListModel.getInstance();
        /**
         * Fix for Bug #:5039378
         * We simply need to remove the group from the list model and the list model
         * will take care of removing the children.
         */
        wsListModel.removeWebServiceGroup(websvcGroup.getId());
        super.destroy();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("websvcGroupNode");
    }
}
