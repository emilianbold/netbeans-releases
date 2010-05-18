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
/*
 * DeleteEjbGroupAction.java
 *
 * Created on May 7, 2004, 12:23 AM
 */

package org.netbeans.modules.visualweb.ejb.actions;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.nodes.EjbGroupNode;
import java.util.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * The action invoked when the user clicks on the "Delete"
 * menu item on an EJB group node
 *
 * @author  cao
 */
public class DeleteEjbGroupAction extends NodeAction 
{
    public DeleteEjbGroupAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return true;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage( DeleteEjbGroupAction.class, "DELETE" );
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) 
    {
        // If the user only selected one EJB group to delete, the message will be like
        // Are you sure you want to delete <EJB group name>?
        // If the user selected multiple EJB groups to delete, the message will be like
        // Are you sure you want to delete these <number> items?
        // This makes the dialog consitent with the one popped up from netbeans when the user hit delete key
        
        String grpNameMsg = null;
        String msg = null;
        if( activatedNodes.length == 1 )
        {
            grpNameMsg = activatedNodes[0].getDisplayName();
            msg = NbBundle.getMessage( DeleteEjbGroupAction.class, "DELETE_CONFIRMATION", grpNameMsg );
        }
        else
            msg = NbBundle.getMessage( DeleteEjbGroupAction.class, "DELTE_MULTIPLE_EJB_SETS", Integer.toString(activatedNodes.length) );
       
        // Confirm Object Deletion for deleting one EJB group and
        // Confirm Mulitple Object Deletion for deleting mulitple EJB groups
        
        String title = null;
        if( activatedNodes.length == 1 )
            title = NbBundle.getMessage( DeleteEjbGroupAction.class, "DELETE_DIALOG_TITLE" );
        else
            title = NbBundle.getMessage( DeleteEjbGroupAction.class, "DELETE_DIALOG_TITLE_MUL" );
        
        NotifyDescriptor d = new NotifyDescriptor.Confirmation( msg, title, NotifyDescriptor.YES_NO_OPTION );
        Object response = DialogDisplayer.getDefault().notify(d);
        
        if( response != null && response.equals( NotifyDescriptor.YES_OPTION ) ) 
        {
            Collection removeGroups = new ArrayList();
            for( int i = 0; i < activatedNodes.length; i ++ ){
                Node node = null;
                if(activatedNodes[i] instanceof FilterNode){
                    node = (Node) activatedNodes[i].getCookie(EjbGroupNode.class);
                }else{
                    node = activatedNodes[i];
                }
                removeGroups.add( ((EjbGroupNode)node).getEjbGroup() );
            }
            
            EjbDataModel.getInstance().removeEjbGroups( removeGroups );
        }
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
    
}
