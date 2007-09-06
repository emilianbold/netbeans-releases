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

package org.netbeans.modules.cnd.classview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyVetoException;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import java.awt.*;
import javax.swing.*;
import javax.swing.JComponent.AccessibleJComponent;
import javax.swing.text.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.*;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.openide.util.Exceptions;


/**
 * View as such
 * @author Vladimir Kvasihn
 */
public class ClassView extends JComponent implements ExplorerManager.Provider, Accessible {
    
    /** composited view */
    protected BeanTreeView view;
    private ClassViewModel model;// = new ClassViewModel();
    private ViewMouseListener mouseListener = new ViewMouseListener();
    private ExplorerManager manager;
    
    private static final boolean TRACE_MODEL_CHANGE_EVENTS = Boolean.getBoolean("cnd.classview.trace.events"); // NOI18N
    
    public ClassView() {
        setLayout(new BorderLayout());
        //init();
        manager = new ExplorerManager();
        ActionMap map = this.getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        setupRootContext(createEmptyRoot());
    }

    /*package local*/ void selectInClasses(final CsmOffsetableDeclaration decl) {
      	CsmModelAccessor.getModel().enqueue(new Runnable() {
	    public void run() {
		if (model != null) {
                    Node node = model.findDeclaration(decl);
                    if (node != null) {
                        try {
                            setUserActivity();
                            getExplorerManager().setSelectedNodes(new Node[]{node});
                        } catch (PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
		}
	    }
	}, "Class View: select in classes"); // NOI18N

    }
    
    private void init(){
        view = new BeanTreeView();
        view.setRootVisible(false);
        view.setDragSource(true);
        add(view, BorderLayout.CENTER);
        setToolTipText(I18n.getMessage("ClassViewTitle")); // NOI18N
        setName(I18n.getMessage("ClassViewTooltip")); // NOI18N
    }
    
    /* Read accessible context
     * @return - accessible context
     */
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
                
                @Override
                public String getAccessibleName() {
                    if (accessibleName != null) {
                        return accessibleName;
                    }
                    
                    return getName();
                }
                
                /* Fix for 19344: Null accessible decription of all TopComponents on JDK1.4 */
                @Override
                public String getToolTipText() {
                    return ClassView.this.getToolTipText();
                }
            };
        }
        
        return accessibleContext;
    }
    
    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return view.requestFocusInWindow();
    }
    
    // In the SDI, requestFocus is called rather than requestFocusInWindow:
    @Override
    public void requestFocus() {
        super.requestFocus();
        view.requestFocus();
    }
    
    private void addRemoveViewListeners(boolean add){
        Component[] scroll = view.getComponents();
        if (scroll != null){
            for(int i = 0; i < scroll.length; i++){
                Component comp = scroll[i];
                if (comp instanceof JScrollBar) {
                    if (add) {
                        comp.addMouseListener(mouseListener);
                        comp.addMouseMotionListener(mouseListener);
                    } else {
                        comp.removeMouseListener(mouseListener);
                        comp.removeMouseMotionListener(mouseListener);
                    }
                }
            }
        }
        JViewport port = view.getViewport();
        Component[] comp = port.getComponents();
        if (comp != null && comp.length>0) {
            if (add) {
                comp[0].addMouseListener(mouseListener);
                comp[0].addMouseMotionListener(mouseListener);
            } else {
                comp[0].removeMouseListener(mouseListener);
                comp[0].removeMouseMotionListener(mouseListener);
            }
        }
    }
    
    private Timer userActivity = null;
    /**
     * delay on user activity.
     */
    private static final int USER_MOUSE_ACTIVITY_DELAY = 2000;
    
    private void setUserActivity(){
        if (userActivity == null) {
            userActivity = new Timer(USER_MOUSE_ACTIVITY_DELAY, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    stopViewModify();
                }
            });
        }
        userActivity.restart();
        if (model != null && !model.isUserActivity()) {
            model.setUserActivity(true);
            //System.out.println("Start user activity");
        }
    }
    
    private void stopViewModify(){
        if (model != null) {
            model.setUserActivity(false);
        }
        //System.out.println("Stop user activity");
        if (userActivity != null) {
            userActivity.stop();
        }
    }
    
    /*package local*/ void startup() {
        if( Diagnostic.DEBUG ) Diagnostic.trace("ClassesV: startup()"); // NOI18N
        if( model != null ) {
            model.dispose();
        }
        init();
        model = new ClassViewModel();
        //setupRootContext(CVUtil.createLoadingRoot());
        addRemoveViewListeners(true);
        if( CsmModelAccessor.getModel().projects().isEmpty() ) {
            setupRootContext(createEmptyRoot());
        } else {
            setupRootContext(model.getRoot());
        }
    }
    
    /*package local*/ void shutdown() {
        if( Diagnostic.DEBUG ) Diagnostic.trace("ClassesV: shutdown()"); // NOI18N
        addRemoveViewListeners(false);
        if( model != null ) {
            model.dispose();
            model = null;
        }
        stopViewModify();
        remove(view);
        view = null;
        userActivity =null;
        mouseListener = null;
        setupRootContext(createEmptyRoot());
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    /*package local*/ void projectOpened(final CsmProject project) {
        if( Diagnostic.DEBUG ) Diagnostic.trace("ClassesV: projectOpened() "+project); // NOI18N
//	CsmModelAccessor.getModel().enqueue(new Runnable() {
//	    public void run() {
		if (model != null) {
		    model.openProject(project);
		    setupRootContext(model.getRoot());
		}
//	    }
//	}, "Class View: creating project node"); // NOI18N
    }
    
    /*package local*/ void projectClosed(CsmProject project) {
        if( Diagnostic.DEBUG ) Diagnostic.trace("ClassesV: projectClosed() " + project); // NOI18N
        if (model != null && !getExplorerManager().getRootContext().isLeaf()) {
            model.closeProject(project);
            RootNode root = model.getRoot();
            Children children = root.getChildren();
            if ((children instanceof ProjectsKeyArray) && ((ProjectsKeyArray) children).isEmpty()){
                setupRootContext(createEmptyRoot());
            } else {
                setupRootContext(root);
            }
        }
    }
    
    /*package local*/ void modelChanged(CsmChangeEvent e) {
        if( TRACE_MODEL_CHANGE_EVENTS ) {
            new CsmTracer().dumpModelChangeEvent(e);
        }
        if (model != null) {
            model.scheduleUpdate(e);
        }
    }
    
    private Node createEmptyRoot() {
        return Node.EMPTY;
    }
    
    // VK: code is copied from org.netbeans.modules.favorites.Tab class
    /** Exchanges deserialized root context to projects root context
     * to keep the uniquennes. */
    private void setupRootContext(final Node rc) {
        if (getExplorerManager().getRootContext() != rc){
            try {
                getExplorerManager().setSelectedNodes(new Node[0]);
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
            if (Diagnostic.DEBUG) Diagnostic.trace("ClassesV: setupRootContext() " + rc); // NOI18N
            getExplorerManager().setRootContext(rc);
        }
    }
    
    private class ViewMouseListener implements MouseListener, MouseMotionListener{
        
        public void mouseClicked(MouseEvent e) {
            setUserActivity();
        }
        
        public void mouseEntered(MouseEvent e) {
            setUserActivity();
        }
        
        public void mouseExited(MouseEvent e) {
            setUserActivity();
        }
        
        public void mousePressed(MouseEvent e) {
            setUserActivity();
        }
        
        public void mouseReleased(MouseEvent e) {
            setUserActivity();
        }
        
        public void mouseDragged(MouseEvent e) {
            setUserActivity();
        }
        
        public void mouseMoved(MouseEvent e) {
            setUserActivity();
        }
    }
    
}
