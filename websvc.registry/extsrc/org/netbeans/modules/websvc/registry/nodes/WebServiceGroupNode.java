/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
        setIconBaseWithExtension("org/netbeans/modules/websvc/registry/resources/folder.png");
		
		getCookieSet().add(this);
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
