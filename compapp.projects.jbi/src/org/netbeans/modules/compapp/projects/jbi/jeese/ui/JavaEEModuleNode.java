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

/*
 * JavaEEModuleNode.java
 *
 * Created on October 12, 2006, 3:20 PM
 */

package org.netbeans.modules.compapp.projects.jbi.jeese.ui;

import java.awt.Image;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.jeese.actions.DeleteJavaEEModuleAction;
import org.netbeans.modules.compapp.projects.jbi.jeese.actions.JavaEEModulePropertiesAction;
import org.netbeans.modules.compapp.projects.jbi.jeese.actions.ServerResourcesAction;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.Utilities;

/**
 *
 * @author root
 */
public class JavaEEModuleNode extends AbstractNode {
    private JbiProject jbiProject;
    private VisualClassPathItem vcpi;
    private Lookup lookup;
        
    public JavaEEModuleNode( VisualClassPathItem vcpi,
            JbiProject project) {
        super( Children.LEAF, buildLookup( vcpi, project ) );
        this.vcpi = vcpi;
        this.jbiProject = project;
    }
    
    private static Lookup buildLookup(
            VisualClassPathItem vcpi, JbiProject project) {
        return Lookups.fixed( new Object[] {
            vcpi,
            project,
        }
        );
    }
    
    public String getName() {
        return vcpi.toString();
    }
    
    public String getDisplayName() {
        return vcpi.toString();
    }
    
    public VisualClassPathItem getRefernece() {
        return vcpi;
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(DeleteAction.class),            
            null,
            SystemAction.get( JavaEEModulePropertiesAction.class),
            null,
            SystemAction.get(ServerResourcesAction.class)
        };
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public void destroy() throws IOException {
        super.destroy();
        
        DeleteJavaEEModuleAction deleteModuleAction =
                SystemAction.get(DeleteJavaEEModuleAction.class);
        deleteModuleAction.performAction(new Node[] {this});
    }

    private Image getProjIcon(){
        Icon ic = null;
        Image ret = null;
        if (this.vcpi != null){
            ic = vcpi.getProjectIcon();
            if (ic instanceof ImageIcon){
                ret = ((ImageIcon)ic).getImage();
            }
        }
        
        return ret;
    }
        
    public Image getIcon(int type) {
        Image ret = getProjIcon();
        if (ret == null){
            ret = Utilities.loadImage(
                "org/netbeans/modules/compapp/projects/jbi/ui/resources/j2seProject.gif"); // NOI18N;
        }
        return ret;
    }
    
    public Image getOpenedIcon(int type) {
        Image ret = getProjIcon();
        if (ret == null){
            ret = Utilities.loadImage(
                "org/netbeans/modules/compapp/projects/jbi/ui/resources/j2seProject.gif"); // NOI18N;            
        }
        return ret;        
    }    
}
