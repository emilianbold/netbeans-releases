/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.classview.model;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.cnd.classview.Diagnostic;
import java.awt.Image;
import org.openide.nodes.*;
import org.openide.util.Utilities;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.SmartChangeEvent;
import org.netbeans.modules.cnd.classview.model.CVUtil.FillingDone;


/**
 * @author Vladimir Kvasihn
 */
public class ProjectNode extends NPNode {
    
    public ProjectNode(final CsmProject project) {
        super(project, project.getGlobalNamespace(), new FillingDone());
        this.project = project;
        setName(project.getName());
        setDisplayName(project.getName());
    }
    
    protected CsmNamespace getNamespace() {
        CsmProject prj = getProject();
        if (prj != null){
            return prj.getGlobalNamespace();
        }
        return null;
    }
    
    protected boolean isSubNamspace(CsmNamespace ns) {
        if (ns != null) {
            CsmNamespace parent = ns.getParent();
            if( parent.isGlobal() ) {
                if( parent == getNamespace() ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Image getIcon(int param) {
        return Utilities.loadImage("org/netbeans/modules/cnd/classview/resources/Project_explorer/Project.png"); // NOI18N
    }
    
    public Image getOpenedIcon(int param) {
        return Utilities.loadImage("org/netbeans/modules/cnd/classview/resources/Project_explorer/Project_open.png"); // NOI18N
    }
    
    
    public CsmProject getProject() {
        return project;
    }
    
    public void addLoadingNode() {
        if( loadingNodes == null ) {
            loadingNodes = new Node[] { CVUtil.createLoadingNode() };
            final Children children = getChildren();
            children.MUTEX.writeAccess(new Runnable(){
                public void run() {
                    children.add( loadingNodes  );
                }
            });
        }
    }

    public void removeLoadingNode() {
        if( loadingNodes != null ) {
            final Children children = getChildren();
            children.MUTEX.writeAccess(new Runnable(){
                public void run() {
                    children.remove(loadingNodes);
                    loadingNodes = null;
                }
            });
        }
    }
    
    public boolean update(SmartChangeEvent e) {
	if( !isDismissed()) {
            CsmProject prj = getProject();
	    if( prj != null && e.getChangedProjects().contains(prj) ) {
                if (isInited()){
                    return super.update(e);
                }
	    }
	}
        return false;
    }
    
    private Map map;
    public Action getPreferredAction() {
        if( Diagnostic.DEBUG ) {
            return new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    map = new HashMap();
                    System.gc();
                    long time = System.currentTimeMillis();
                    long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    Diagnostic.trace("Creating a map."); // NOI18N
                    traverse(new BaseNode.Callback() {
                        public void call(BaseNode node) {
                            map.put(node, node);
                        }
                    });
                    time = System.currentTimeMillis() - time;
                    System.gc();
                    mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - mem;
                    Diagnostic.trace("A map is created. Used time: " + time + " Used Memory: " + mem/1024 + " Kb"); // NOI18N
                    map = null;
                }
            };
        }
        else {
            return super.getPreferredAction();
        }
    }
    
    public void dismiss() {
        setDismissed();
        if (isInited()){
            super.dismiss();
        }
        project = null;
    }
    
    private CsmProject project;
    private Node[] loadingNodes = null;

}
