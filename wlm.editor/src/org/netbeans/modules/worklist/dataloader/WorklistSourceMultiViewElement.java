/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.dataloader;

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.util.UIUtilities;
import org.netbeans.modules.xml.validation.ui.ShowCookie;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.awt.UndoRedo;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.text.CloneableEditor;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author anjeleevich
 */
class WorklistSourceMultiViewElement extends CloneableEditor 
        implements MultiViewElement 
{
    private static final long serialVersionUID = 4403502726950453345L;
    
    transient private JComponent toolbar;
    transient private MultiViewElementCallback multiViewObserver;
    private WorklistDataObject dataObject;
    private Node rootNode;
    /** current selection*/
    private Node selectedNode;
    /** listens to selected node destroyed event */
    private NodeAdapter nl;
    /** Timer which countdowns the "update selected element node" time. */
    private Timer timerSelNodes;
    /** Listener on caret movements */
    private CaretListener caretListener;
    
    public WorklistSourceMultiViewElement() {
        super();
    }
    
    public WorklistSourceMultiViewElement(WorklistDataObject dataObject) {
        super(dataObject.getWlmEditorSupport());
        this.dataObject = dataObject;
        
        dataObject.getWlmEditorSupport().initializeCloneableEditor(this);
        initialize();
    }

    public static void gotoSource(Component component, 
            DataObject data) 
    {
      if (!(component instanceof DocumentComponent)) {
          return;
      }
      DocumentComponent document = (DocumentComponent) component;
      LineCookie lc = data.getCookie(LineCookie.class);

      if (lc == null) {
          return;
      }
      int lineNum = getLineNum(document);

      if (lineNum < 0) {
          return;
      }
      final Line line = lc.getLineSet().getCurrent(lineNum);
      final int column = getColumnNum(document);

      if (column < 0) {
        return;
      }
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
              // previous: line.show(Line.SHOW_GOTO, column);
              if (line != null) {
                line.show(Line.ShowOpenType.OPEN,
                        Line.ShowVisibilityType.FOCUS);
              }
//todo r              openActiveSourceEditor();
          }
      });
    }
    
    private static int getLineNum(DocumentComponent entity) {
        StyledDocument document = entity.getModel().getModelSource()
                .getLookup().lookup(StyledDocument.class);

        if (document == null) {
            return -1;
        }
        
        return NbDocument.findLineNumber(document, entity.findPosition());
    }
    
    private static int getColumnNum(DocumentComponent entity) {
        StyledDocument document = entity.getModel().getModelSource()
                .getLookup().lookup(StyledDocument.class);

        if (document == null) {
          return -1;
        }
        return NbDocument.findLineColumn(document, entity.findPosition());
    }

    /**
     * create lookup, caretlistener, timer
     */
    private void initialize() {
//        ShowCookie showCookie = new ShowCookie() {
//            public void show(ResultItem resultItem) {
//                if(isActiveTC()) {
//                    Component component = resultItem.getComponents();
//                    if (component.getModel() == null) return; //may have been deleted.
//                    
//                    UIUtilities.annotateSourceView(dataObject, (DocumentComponent) component, 
//                            resultItem.getDescription(), true);
//                    if(component instanceof WLMComponent) {
//                        int position = ((WLMComponent)component).findPosition();
//                        getEditorPane().setCaretPosition(position);
//                    } else {
//                        int line = resultItem.getLineNumber();
//                        try {
//                            int position = NbDocument.findLineOffset(
//                                    (StyledDocument)getEditorPane().getDocument(),line);
//                            getEditorPane().setCaretPosition(position);
//                        } catch (IndexOutOfBoundsException iob) {
//                            // nothing
//                        }
//                    }
//                }
//            }
//        };

        // create and associate lookup
        Node delegate = dataObject.getNodeDelegate();
//        Lookup lookup = new ProxyLookup(
//                Lookups.fixed(getActionMap(), dataObject, delegate),
//                delegate.getLookup());
        SourceCookieProxyLookup lookup = new SourceCookieProxyLookup(new Lookup[] {
            Lookups.fixed(new Object[] {
                // Need ActionMap in lookup so editor actions work.
                getActionMap(),
                // Need the data object registered in the lookup so that the
                // projectui code will close our open editor windows when the
                // project is closed.
                dataObject
            }), 
            dataObject.getLookup(),
//            Lookups.singleton(this),
//            delegate.getLookup(),
        }, delegate);        
        associateLookup(lookup);
        addPropertyChangeListener("activatedNodes", lookup);
    }
    
    public JComponent getToolbarRepresentation() {
        Document doc = getEditorPane().getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
            if (toolbar == null) {
                toolbar = ((NbDocument.CustomToolbar) doc)
                        .createToolbar(getEditorPane());
            }
            return toolbar;
        }
        return null;
    }
    
    public JComponent getVisualRepresentation() {
        return this; 
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;
    }
    
    @Override
    public void requestVisible() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }
    
    @Override
    public void requestActive() {
        if (multiViewObserver != null) {
            multiViewObserver.requestActive();
        } else {
            super.requestActive();
        }
    }
    
    @Override
    protected String preferredID() {
        return "WorklistSourceMultiViewElementTC";  //  NOI18N
    }
    
    @Override
    public UndoRedo getUndoRedo() {
        return dataObject.getWlmEditorSupport().getUndoManager();
    }

    /**
     * The close last method should be called only for the last clone. 
     * If there are still existing clones this method must return false. The
     * implementation from the FormEditor always returns true but this is 
     * not the expected behavior. The intention is to close the editor support
     * once the last editor has been closed, using the silent close to avoid
     * displaying a new dialog which is already being displayed via the 
     * close handler. 
     */ 
    @Override
    protected boolean closeLast() {
        WorklistEditorSupport support = dataObject.getWlmEditorSupport();
        JEditorPane[] editors = support.getOpenedPanes();
        if (editors == null || editors.length == 0) {
            return support.silentClose();
        }
        return false;
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!WorklistEditorSupport.isLastView(multiViewObserver
                .getTopComponent())) 
        {
            return CloseOperationState.STATE_OK;
        }
        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                "ID_TEXT_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }
    
    @Override
    public void componentActivated() {
        JEditorPane p = getEditorPane();
        if (p != null) {
            p.addCaretListener(caretListener);
        }

        Node nodeDelegate = dataObject.getNodeDelegate();
        if (nodeDelegate != null) {
            setActivatedNodes(new Node[] { nodeDelegate });
        }

        if(timerSelNodes!=null) {
            timerSelNodes.restart();
        }
        super.componentActivated();            
        WorklistEditorSupport editor = dataObject.getWlmEditorSupport();
        editor.addUndoManagerToDocument();
//        WorklistMultiViewFactory.updateGroupVisibility(
//                WorklistSourceMultiViewDescription.PREFERRED_ID);
    }
    
    @Override
    public void componentDeactivated() {
        // Note: componentDeactivated() is called when the entire
        // MultiViewTopComponent is deactivated, _not_ when switching
        // between the multiview elements.
        JEditorPane p = getEditorPane();
        if (p != null) {
            p.removeCaretListener(caretListener);
        }
        synchronized (this) {
            if (selectionTask != null) {
                selectionTask.cancel();
                selectionTask = null;
            }
        }
        if(timerSelNodes!=null) {
            timerSelNodes.stop();
        }
        super.componentDeactivated();
        WorklistEditorSupport editor = dataObject.getWlmEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
//        
//        WorklistMultiViewFactory.updateGroupVisibility(
//                WorklistSourceMultiViewDescription.PREFERRED_ID);
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
    }
    
    
    /*
     * In other non-multiview editors, the text editor is the one that is docked into the editor mode.
     * and super.canClose() returns true and schedules the clean up of editor kit, 
     * which in turn calls uninstallUI in NbEditorUI class.
     * 
     * In our case, we need to explicitly call setEditorKit(null), so that uninstallUI gets called.
     * So our editor gets removed from different caches and propertychangesupports.
     * 
     * (non-Javadoc)
     * @see org.openide.text.CloneableEditor#componentClosed()
     */
    @Override
    public void componentClosed() {
        super.componentClosed();
        cleanup();
    }
    
    private void cleanup() {
        rootNode = null;
        if (selectedNode != null) selectedNode.removeNodeListener(nl);
        nl = null;
        selectedNode = null;
        toolbar = null;
        setActivatedNodes(new Node[0]);
        caretListener = null;
        multiViewObserver = null;
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        WorklistEditorSupport editor = dataObject.getWlmEditorSupport();
        editor.addUndoManagerToDocument();
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
        WorklistEditorSupport editor = dataObject.getWlmEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(dataObject);
    }

    @Override
    public void readExternal(ObjectInput in) throws 
            IOException, ClassNotFoundException 
    {
        super.readExternal(in);
        Object firstObject = in.readObject();
        if (firstObject instanceof WorklistDataObject) {
            dataObject = (WorklistDataObject) firstObject;
            initialize();
        }
    }

    /* task */
    private transient RequestProcessor.Task selectionTask = null;

    protected boolean isActiveTC() {
        if (multiViewObserver != null)
            return getRegistry().getActivated() 
                    == multiViewObserver.getTopComponent();

        return false;
    }
}
