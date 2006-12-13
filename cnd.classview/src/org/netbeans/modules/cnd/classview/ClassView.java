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

package org.netbeans.modules.cnd.classview;

import org.netbeans.modules.cnd.classview.model.BaseNode;
import org.netbeans.modules.cnd.classview.model.CVUtil;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.*;
import org.openide.util.RequestProcessor;
//import org.openide.util.NbBundle;
import org.netbeans.modules.cnd.classview.resources.I18n;


/**
 * View as such
 * @author Vladimir Kvasihn
 */
public class ClassView extends JComponent implements ExplorerManager.Provider, CsmModelListener, CsmModelStateListener {

    /** composited view */
    protected BeanTreeView view;
    
    private ClassViewModel model = new ClassViewModel();
    
    private boolean fillingModel = false;
    
    private ExplorerManager manager;
    
    private boolean listening = false;

    private static final boolean TRACE_MODEL_CHANGE_EVENTS = Boolean.getBoolean("cnd.classview.trace.events");
    
    public ClassView() {
        
        view = new BeanTreeView();
        view.setRootVisible(false);
        view.setDragSource(true);
        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);
        
        this.manager = new ExplorerManager();
        ActionMap map = this.getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
//        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
//        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
//        map.put("delete", ExplorerUtils.actionDelete (manager, true)); // or false
//
//        // following line tells the top component which lookup should be associated with it
//        associateLookup (ExplorerUtils.createLookup (manager, map));
        
        setupRootContext(createEmptyRoot());
    }
    
    public void startup() {
        if( Diagnostic.DEBUG ) Diagnostic.trace(">>> ClassView is starting up");
        addRemoveListeners(true);
        startFillingModel();
    }
    
    public void shutdown() {
        if( Diagnostic.DEBUG ) Diagnostic.trace(">>> ClassView is shutting down");
        addRemoveListeners(false);
        if( model != null ) {
            model.dispose();
        }
    }
    
    private void addRemoveListeners(boolean add) {
        if( add ) {
            if( Diagnostic.DEBUG ) Diagnostic.trace(">>> adding model listeners");
            CsmModelAccessor.getModel().addModelListener(this);
        }
        else {
            if( Diagnostic.DEBUG ) Diagnostic.trace(">>> removing model listeners");
            CsmModelAccessor.getModel().removeModelListener(this);
        }
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public void projectOpened(CsmProject project) {
        if( Diagnostic.DEBUG ) Diagnostic.trace("\n@@@ PROJECT OPENED " + project);
	model.updateProjects();
	setupRootContext(model.getRoot());
    }

    public void projectClosed(CsmProject project) {
        if( Diagnostic.DEBUG ) Diagnostic.trace("\n@@@ PROJECT CLOSEED " + project);
	model.updateProjects();
	setupRootContext(model.getRoot());
        // release Class View project nodes when projects are closed
        if (CsmModelAccessor.getModel().projects().size()==0){
            model.dispose();
            model = new ClassViewModel();
            setupRootContext(createEmptyRoot());
        }
    }
    
    public void modelChanged(CsmChangeEvent e) {
	if( TRACE_MODEL_CHANGE_EVENTS ) {
	    new CsmTracer().dumpModelChangeEvent(e);
	}
	model.scheduleUpdate(e);
    }
    
    public void modelStateChanged(CsmModelState newState, CsmModelState oldState) {
        if( newState == CsmModelState.OFF ) {
            shutdown();
        }
        else if( newState == CsmModelState.ON ) {
            startup();
        }
    }
    
    
    private void startFillingModel() {
	
	if( CsmModelAccessor.getModel().projects().isEmpty() ) {
	    return;
	}
		
        synchronized(this) {
            if( Diagnostic.DEBUG ) Diagnostic.trace("startFillingModel; fillingModel=" + fillingModel + " this.hash=" + this.hashCode());
            if( fillingModel ) {
                if( Diagnostic.DEBUG ) Diagnostic.trace("  already running");
                return;
            }
            fillingModel = true;
            if( Diagnostic.DEBUG ) Diagnostic.trace("  fillingModel set to true");
        }
        
        setupRootContext(CVUtil.createLoadingRoot());
        // We are in event queue now.
	// So though Filler.run seems not to be rather light 
	// we better launch it in a thread!
        ModelFiller filler = new ModelFiller();
        //filler.run();
	CsmModelAccessor.getModel().enqueue(filler, "Class View initial filler");
        
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//		setupRootContext(CVUtil.createLoadingRoot());
//                ModelFiller filler = new ModelFiller();
//                //filler.start();
//                //RequestProcessor.getDefault().post(filler);
//                CsmModelAccessor.getModel().enqueue(filler, "Class View");
//            }
//        });
        
    }
    
    Node createEmptyRoot() {
        return new AbstractNode(Children.LEAF);
    }
    
    
    // VK: code is copied from org.netbeans.modules.favorites.Tab class
    /** Exchanges deserialized root context to projects root context
     * to keep the uniquennes. */
    protected void setupRootContext(Node rc) {
        getExplorerManager().setRootContext(rc);
        //setIcon(rc.getIcon(BeanInfo.ICON_COLOR_16x16));
        setToolTipText(I18n.getMessage("ClassViewTitle"));	// NOI18N
        setName(I18n.getMessage("ClassViewTooltip"));	// NOI18N
    }
    
    private class ModelFiller /*extends Thread*/ implements Runnable {
        
        public ModelFiller() {
        }
        
        public void run() {
            
            if( Diagnostic.DEBUG ) Diagnostic.trace("ModelFiller started");
            if( model != null ) {
                model.dispose();
            }
            model = new ClassViewModel();

            long t = System.currentTimeMillis();
            final Node root = model.getRoot();
                    
            t = System.currentTimeMillis() - t;
            if( Diagnostic.DEBUG ) Diagnostic.trace("#### Model filling took " + t + " ms");
            
            synchronized(ClassView.this) {
                fillingModel = false;
                if( Diagnostic.DEBUG ) Diagnostic.trace("fillingModel set to false");
            }
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if( Diagnostic.DEBUG ) Diagnostic.trace("setting root context");
                    setupRootContext(root);
                }
            });
        }
        
    };
    
}
