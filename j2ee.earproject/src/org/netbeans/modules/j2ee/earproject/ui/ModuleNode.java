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

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.earproject.ProjectPropertyProvider;
import org.netbeans.modules.j2ee.earproject.ui.actions.OpenModuleProjectAction;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * Represents one node in the <em>J2EE Modules</em> node in the EAR project's
 * logical view.
 *
 * @author vkraemer
 * @author Ludovic Champenois
 */
public final class ModuleNode extends AbstractNode implements Node.Cookie {
    
    /** Package-private for unit tests <strong>only</strong>. */
    static final String MODULE_NODE_NAME = "module.node"; // NOI18N
    
    private static Action[] actions;
    
    private final FileObject projectDirectory;
    private final VisualClassPathItem key;
    
    public ModuleNode(final VisualClassPathItem key, final FileObject projectDirectory) {
        super(Children.LEAF);
        this.key = key;
        this.projectDirectory = projectDirectory;
        setName(ModuleNode.MODULE_NODE_NAME);
        setDisplayName(key.getCompletePathInArchive());
        setShortDescription(NbBundle.getMessage(ModuleNode.class, "HINT_ModuleNode"));
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        if (null == actions) {
            actions = new Action[] {
                SystemAction.get(OpenModuleProjectAction.class),
                SystemAction.get(RemoveAction.class)
            };
            getCookieSet().add(this);
        }
        return actions;
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenModuleProjectAction.class);
    }
    
    public Image getIcon(int type) {
        // XXX the "algorithm" based on the ant property name - in the case of
        // application client; is little odd. Also the rest is rather unclear.
        if (key.toString().endsWith("war")) { // NOI18N
            return Utilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/WebModuleNode.gif");//NOI18N
        } else if (key.getRaw().indexOf("j2ee-module-car") > 0) { //NOI18N
            return Utilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/CarModuleNodeIcon.gif");//NOI18N
        } else {
            return Utilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/EjbModuleNodeIcon.gif");//NOI18N
        }
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    void removeFromJarContent() {
        List<VisualClassPathItem> newList = new ArrayList<VisualClassPathItem>();
        Project p = FileOwnerQuery.getOwner(projectDirectory);
        ProjectPropertyProvider ppp =
                (ProjectPropertyProvider) p.getLookup().lookup(ProjectPropertyProvider.class);
        EarProjectProperties epp = ppp.getProjectProperties();
        newList.addAll(epp.getJarContentAdditional());
        newList.remove(key);
        epp.put(EarProjectProperties.JAR_CONTENT_ADDITIONAL, newList);
        epp.store();
    }
    
    public VisualClassPathItem getVCPI() {
        return key;
    }
    
    // Handle copying and cutting specially:
    public boolean canCopy() {
        return false;
    }
  
}
