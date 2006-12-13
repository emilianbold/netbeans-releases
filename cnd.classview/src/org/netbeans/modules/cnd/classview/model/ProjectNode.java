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
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.netbeans.modules.cnd.classview.Diagnostic;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import java.awt.Image;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.modelutil.Tracer;
import org.openide.nodes.*;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.netbeans.api.project.*;

import  org.netbeans.modules.cnd.api.model.*;


/**
 * @author Vladimir Kvasihn
 */
public class ProjectNode extends NPNode {
    
    private static class ProjectNodeComparator extends CVUtil.NamespaceNodesComparator {
        public int compare(Object o1, Object o2) {
//            if( o1 instanceof LoadingNode &&! (o2 instanceof  LoadingNode) ) {
//                return -1;
//            }
//            else if( o2 instanceof LoadingNode &&! (o1 instanceof  LoadingNode) ) {
//                return +1;
//            }
            
            if( o1 instanceof LoadingNode ) {
                if( ! ( o2 instanceof LoadingNode ) ) {
                    return -1;
                }
            }
            else if( o2 instanceof LoadingNode ) {
                if( ! ( o1 instanceof LoadingNode ) ) {
                    return +1;
                }
            }
            
            return super.compare(o1, o2);
        }
    };
    
    public ProjectNode(final CsmProject project) {
        super();
//        super(new Children.SortedArray());
//        ((Children.SortedArray) getChildren()).setComparator(new ProjectNodeComparator());
        this.project = project;
        setName(project.getName());
        setDisplayName(project.getName());
        addLoadingNode();
        Runnable filler;
        if( project.isStable(null) ) {
            filler = new Runnable() {
                public void run() {
		    first = false;
                    fill();
                    removeLoadingNode();
                    if( Diagnostic.DUMP_MODEL ) {
                        dump(System.err);
                    }
                }
            };
        }
        else {
            filler = new Runnable() {
                public void run() {
                    project.waitParse();
		    if( project.isValid() ) {
                        if( first ) {
                            first = false;
                            fill();
                        }
			removeLoadingNode();
			if( Diagnostic.DUMP_MODEL ) {
			    dump(System.err);
			}
		    }
                }
            };
        }
        
        CsmModelAccessor.getModel().enqueue(filler, "Class View: " + project.getName());
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
    
    protected Comparator getComparator() {
        return new ProjectNodeComparator();
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
    
    private void addLoadingNode() {
        loadingNodes = new Node[] { CVUtil.createLoadingNode() };
        final Children children = getChildren();
        children.MUTEX.writeAccess(new Runnable(){
            public void run() {
                children.add( loadingNodes  );
            }
        });
    }

    private void removeLoadingNode() {
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
    

//    protected void fill() {
//        if( NO_REFRESH ) {
//            project.getGlobalNamespace().getDeclarations(); // just to ensure that parsing ended
//            removeLoadingNode();
//        } 
//        else {
//            super.fill();
//        }
//        if( Diagnostic.DEBUG ) {
//            new CsmTracer().dumpModel(getNamespace());
//        }
//    }    
    
    public boolean update(CsmChangeEvent e) {
	if( !isDismissed() ) {
            CsmProject prj = getProject();
	    if( prj != null && e.getChangedProjects().contains(prj) ) {
		if( first ) {
		    first = false;
                    CsmNamespace ns = getNamespace();
                    if (ns != null){
                        NodeUtil.fillNodeWithNamespaceContent(this, ns, true);
                    }
		    return true;
		}
		return super.update(e);
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
                    Diagnostic.trace("Creating a map.");
                    traverse(new BaseNode.Callback() {
                        public void call(BaseNode node) {
                            map.put(node, node);
                        }
                    });
                    time = System.currentTimeMillis() - time;
                    System.gc();
                    mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - mem;
                    Diagnostic.trace("A map is created. Used time: " + time + " Used Memory: " + mem/1024 + " Kb");
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
        super.dismiss();
        project = null;
    }
    
    //private ProjectInformation pi;
    private CsmProject project;
    private Node[] loadingNodes = null;
    volatile private boolean first = true;


}
