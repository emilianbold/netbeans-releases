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

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import javax.swing.Action;

import java.util.List;


import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import org.openide.util.Utilities;
import org.netbeans.api.project.Project;

import org.netbeans.modules.j2ee.common.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.ui.actions.OpenModuleProjectAction;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.j2ee.earproject.ProjectPropertyProvider;
import org.netbeans.modules.j2ee.common.ui.customizer.ArchiveProjectProperties;
import org.netbeans.api.project.FileOwnerQuery;

/**
 * A simple node with no children.
 * Often used in conjunction with some kind of underlying data model, where
 * each node represents an element in that model. In this case, you should see
 * the Container Node template which will permit you to create a whole tree of
 * such nodes with the proper behavior.
 * @author vkraemer
 * @author Ludovic Champenois
 */
public class ModuleNode extends AbstractNode implements Node.Cookie {
    private VisualClassPathItem key;
    private AntProjectHelper helper;
    
    // will frequently accept an element from some data model in the constructor:
    public ModuleNode(VisualClassPathItem key, AntProjectHelper helper) {
        super(Children.LEAF);
        this.key = key;
        this.helper = helper;
        setName("preferablyUniqueNameForThisNodeAmongSiblings"); // or, super.setName if needed
        setDisplayName(key.getCompletePathInArchive()); // toString());
        setShortDescription(NbBundle.getMessage(ModuleNode.class, "HINT_ModuleNode"));//NOI18N
    }
    
    static private Action[] actions = null;
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        if (null == actions) {
            actions = new Action[] {
        };
        getCookieSet().add(this);
        }
        return actions;
    }
    public Image getIcon(int type){
        if (key.toString().endsWith("war")) //FIXME
            return Utilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/WebModuleNode.gif");//NOI18N
        else
            return Utilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/EjbModuleNodeIcon.gif");//NOI18N
            
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    void removeFromJarContent() {
        List newList = new java.util.ArrayList();
        Project p = FileOwnerQuery.getOwner(helper.getProjectDirectory());
        ProjectPropertyProvider ppp =
                (ProjectPropertyProvider) p.getLookup().lookup(ProjectPropertyProvider.class);
        ArchiveProjectProperties epp = ppp.getProjectProperties();
       Object t = epp.get(EarProjectProperties.JAR_CONTENT_ADDITIONAL);
        if (!(t instanceof List)) {
            assert false : "jar content isn't a List???";
            return;
        }
        List vcpis = (List) t;
        newList.addAll(vcpis);
        newList.remove(key);
        epp.put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, newList);
        epp.store();
                try {
                    org.netbeans.api.project.ProjectManager.getDefault().saveProject(epp.getProject());
                }
                catch ( java.io.IOException ex ) {
                    org.openide.ErrorManager.getDefault().notify( ex );
                }
    }
    
    public VisualClassPathItem getVCPI() {
        return key;
    }
    
    // Handle copying and cutting specially:
    /**/
    public boolean canCopy() {
        return false;
    }
  
}
