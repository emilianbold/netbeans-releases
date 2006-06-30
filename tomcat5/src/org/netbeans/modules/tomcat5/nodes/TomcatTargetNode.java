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

package org.netbeans.modules.tomcat5.nodes;
import java.awt.Image;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.netbeans.modules.tomcat5.nodes.actions.RefreshWebModulesAction;
import org.netbeans.modules.tomcat5.nodes.actions.RefreshWebModulesCookie;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Petr Pisl
 */

public class TomcatTargetNode extends AbstractNode {

    /** Creates a new instance of TomcatTargetNode */
    public TomcatTargetNode(Lookup lookup) {
        super(new Children.Array());
        getChildren().add(new Node[] {new WebModuleHolderNode(lookup)});
    }
 
    public class WebModuleHolderNode extends AbstractNode {
        
        private Node iconDelegate;
        
        public WebModuleHolderNode (Lookup lookup){
            super(new TomcatWebModuleChildren(lookup));
            setDisplayName(NbBundle.getMessage(TomcatTargetNode.class, "LBL_WebApps"));  // NOI18N
            getCookieSet().add(new RefreshWebModuleChildren ((TomcatWebModuleChildren)getChildren()));
            iconDelegate = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
        }
        
        public Image getIcon(int type) {
            return iconDelegate.getIcon(type);
        }        

        public Image getOpenedIcon(int type) {
            return iconDelegate.getOpenedIcon(type);
        }
        
        public javax.swing.Action[] getActions(boolean context) {
            return new SystemAction[] {
                   SystemAction.get(RefreshWebModulesAction.class)
               };        
        }
    }
    
    
    class RefreshWebModuleChildren implements RefreshWebModulesCookie {
        TomcatWebModuleChildren children;

        RefreshWebModuleChildren (TomcatWebModuleChildren children){
            this.children = children;
        }

        public void refresh() {
            children.updateKeys();
        }
    }
}
