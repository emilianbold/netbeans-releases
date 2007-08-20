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

/**
 * @author Vladimir Kvasihn
 */
public class ProjectNode extends NPNode {
    public static final boolean EXPORT = Boolean.getBoolean("cnd.classview.export"); // NOI18N
    
    public ProjectNode(final CsmProject project, Children.Array key) {
        super(key);
        this.project = project;
        init(project);
    }
    
    private void init(CsmProject project){
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
    
    @Override
    public Image getIcon(int param) {
        return Utilities.loadImage("org/netbeans/modules/cnd/classview/resources/Project_explorer/Project.png"); // NOI18N
    }
    
    @Override
    public Image getOpenedIcon(int param) {
        return Utilities.loadImage("org/netbeans/modules/cnd/classview/resources/Project_explorer/Project_open.png"); // NOI18N
    }
    
    public CsmProject getProject() {
        return project;
    }
    
    @Override
    public Action getPreferredAction() {
        if( Diagnostic.DEBUG ) {
            return new TraverseAction();
        } else if(EXPORT) {
            return new ExportAction();
        } else {
            return super.getPreferredAction();
        }
    }
    
    private CsmProject project;
    private Node[] loadingNodes = null;
    
    private class TraverseAction extends AbstractAction {
        private Map<BaseNode,BaseNode> map;
        public TraverseAction() {
            putValue(Action.NAME, "Measure traverse project node time and memory."); //NOI18N
        }
        
        public void actionPerformed(ActionEvent e) {
            map = new HashMap<BaseNode,BaseNode>();
            System.gc();
            long time = System.currentTimeMillis();
            long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            String message = "Creating a map."; // NOI18N
            if (Diagnostic.DEBUG) {
                Diagnostic.trace(message);
            } else {
                System.out.println(message);
            }
            traverse(new BaseNode.Callback() {
                public void call(BaseNode node) {
                    map.put(node, node);
                }
            });
            time = System.currentTimeMillis() - time;
            System.gc();
            mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - mem;
            message = "A map is created. Used time: " + time + " Used Memory: " + mem/1024 + " Kb"; // NOI18N
            if (Diagnostic.DEBUG) {
                Diagnostic.trace(message);
            } else {
                System.out.println(message);
            }
            map = null;
        }
        public String getName() {
            return (String) getValue(NAME);
        }
    }
    
    private class ExportAction extends AbstractAction {
        public ExportAction() {
            putValue(Action.NAME, "Export project node."); //NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            dump(System.out);
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        if( Diagnostic.DEBUG || EXPORT) {
            return new Action[] {new TraverseAction(),new ExportAction()};
        }
        return new Action[0];
    }
}
