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
package org.netbeans.modules.xml.schema.refactoring.ui;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
//import javax.jmi.reflect.RefObject;
import javax.swing.*;
import org.netbeans.modules.xml.schema.refactoring.query.CustomizerResults;
import org.netbeans.modules.xml.schema.refactoring.query.Query;
import org.openide.ErrorManager;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.windows.TopComponent;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;
import org.openide.util.WeakListeners;

/**
 * Panel for showing proposed changes (refactoring elements) of any refactoring.
 *
 * @author  Pavel Flaska, Martin Matula
 * @author  Jeri Lockhart
 */
//public class QueryPanel extends JPanel implements InvalidationListener {
public class QueryPanel extends JPanel   implements NodeListener   {
    public static final long serialVersionUID = 1L;
    
    // PRIVATE FIELDS      
    
    private final Query query;
    
    private transient boolean isVisible = false;
  //  private transient ParametersPanel parametersPanel = null;
      
    private WeakReference refCallerTC;
    
    private AnalysisViewer analysisViewer;    
        
    private CustomizerResults results;
    
    
    
    
    public QueryPanel(Query query) {
        this(query,null);
    }
    
    
    @SuppressWarnings("unchecked")
    public QueryPanel(Query query, TopComponent caller) {
        if (caller!=null)
            refCallerTC = new WeakReference(caller);
        this.query = query;
        initialize();
        refresh(false);
    }
    
    
    public static void checkEventThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException(
                    "This must happen in event thread!")); //NOI18N
        }
    }
    
    /* initializes all the query */
    private void initialize() {
        checkEventThread();
        setFocusCycleRoot(true);
        setLayout(new BorderLayout());
        setName(query.getShortName());
        
        analysisViewer = new AnalysisViewer();
//        analysisViewer.addPropertyChangeListener(
//                AnalysisViewer.PROP_GRAPH_NODE_SELECTION_CHANGED_RELAY,this);
        analysisViewer.getPanel().setMinimumSize(new Dimension(10,10));
        analysisViewer.getPanel().setPreferredSize(new Dimension(10,10));
        add(analysisViewer, BorderLayout.CENTER);        
        validate();
        addDataOjectNodeListener();
    }
    
    private void addDataOjectNodeListener() {
        SchemaModel queryModel = query.getModel();
        assert queryModel != null : "null query model";
        DataObject dobj = (DataObject) queryModel.getModelSource().getLookup().lookup(DataObject.class);
        assert dobj != null : "model source lookup has no data object";
        Node node = dobj.getNodeDelegate();
        node.addNodeListener((NodeListener)WeakListeners.create(NodeListener.class, this, node));
    }
    
    void close() {
        
        QueryPanelContainer.getUsagesComponent().removePanel(this);
        closeNotify();
    }
    
    public boolean getIsVisible() {
        return this.isVisible;
    }
    
    public void setIsVisible(boolean isVisible){
        this.isVisible = isVisible;
    }
    
    private void refresh(final boolean showParametersPanel) {
        checkEventThread();
        query.runQuery(this, analysisViewer);

    }
    
    // disables all components in a given container
    private static void disableComponents(Container c) {
        checkEventThread();
        Component children[] = c.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i].isEnabled()) {
                children[i].setEnabled(false);
            }
            if (children[i] instanceof Container) {
                disableComponents((Container) children[i]);
            }
        }
    }
    
    protected void closeNotify() {
//        UndoWatcher.stopWatching(this);
        if (refCallerTC != null) {
            TopComponent tc = (TopComponent) refCallerTC.get();
            if (tc != null && tc.isShowing()) {
                tc.requestActive();
            }
        }
        //super.closeNotify();
    } 
    
    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////
    
  
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Notifies this component that it now has a parent component.
     * When this method is invoked, the chain of parent components is
     * set up with <code>KeyboardAction</code> event listeners.
     *
     *
     * @see #registerKeyboardAction
     */
//    public void addNotify() {
//        super.addNotify();
//        //  When the NavigatorPanel's NavNode HighlightInSchemaViewAction
//        // is performed on a NavNode, the NavigatorPanel fires an event
//        //  to the QueryPanelContainer.  The RPC then fires
//        //   the PROP_HIGHLIGHT_IN_SCHEMA_VIEW to the AnalysisView
//        //  The AnalysisView then calls SchemaRepresentation.show()
//        QueryPanelContainer rpc = QueryPanelContainer.getUsagesComponent();
//        if (rpc != null){
//            rpc.addPropertyChangeListener(AnalysisViewer.PROP_HIGHLIGHT_IN_SCHEMA_VIEW,
//                    analysisViewer);
//        }
//    }

    public void childrenRemoved(NodeMemberEvent ev) {
    }

    public void childrenAdded(NodeMemberEvent ev) {
    }

    public void nodeDestroyed(NodeEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                close();
            }
        });
    }

    public void childrenReordered(NodeReorderEvent ev) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
    }
    
    
  
} // end Refactor Panel
