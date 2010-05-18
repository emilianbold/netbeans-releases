/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
