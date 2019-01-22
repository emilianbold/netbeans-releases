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
package org.netbeans.modules.websvc.rest.wadl.design.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.websvc.rest.wadl.design.loader.ShowCookie;
import org.netbeans.modules.websvc.rest.wadl.design.loader.WadlDataLoader;
import org.netbeans.modules.websvc.rest.wadl.design.loader.WadlDataObject;
import org.netbeans.modules.websvc.rest.wadl.design.loader.WadlEditorSupport;
import org.netbeans.modules.websvc.rest.wadl.model.WadlComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * The source editor for schema documents.
 *
 * 
 */
@MultiViewElement.Registration(
        displayName ="#LBL_sourceView_name",// NOI18N
    iconBase=WadlDataObject.WADL_ICON_BASE_WITH_EXT,
    persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
    preferredID=MultiViewSupport.SOURCE_VIEW_ID,
    mimeType=WadlDataLoader.MIME_TYPE,            
    position=1000
        )
public class SourceMultiViewElement extends CloneableEditor
        implements MultiViewElement 
{

    private static final long serialVersionUID = 4403502726950453345L;
    private transient JComponent toolbar;
    private transient MultiViewElementCallback multiViewCallback;
    private WadlDataObject wadlDataObject;

    /**
     * Constructs a new instance of WadlSourceMultiViewElement.
     */
    public SourceMultiViewElement() {
        // Needed for deserialization, do not remove.
        super();
    }

    /**
     * Constructs a new instance of WadlSourceMultiViewElement.
     *
     * @param  dobj  schema data object being edited.
     */
    public SourceMultiViewElement(Lookup context) {
        super(context.lookup(WadlDataObject.class).getWadlEditorSupport());
        this.wadlDataObject = context.lookup(WadlDataObject.class);

        // Initialize the editor support properly, which only needs to be
        // done when the editor is created (deserialization is working
        // due to CloneableEditor.readResolve() initializing the editor).
        // Note that this relies on the source view being the first in the
        // array of MultiViewDescription instances in WadlMultiViewSupport,
        // since that results in the source view being created and opened
        // by default, only to be hidden when the DataObject default action
        // makes the columns view appear.
        // This initialization fixes CR 6380287 by ensuring that the Node
        // listener is registered with the DataObject Node delegate.
        wadlDataObject.getWadlEditorSupport().initializeCloneableEditor(this);
        initialize();
    }

    /**
     * create lookup, caretlistener, timer
     */
    private void initialize() {
        ShowCookie showCookie = new ShowCookie() {

            public void show(ResultItem resultItem) {
                if (isActiveTC()) {
                    try {
                        int position = 0;
                        Component component = resultItem.getComponents();
                        if (component instanceof WadlComponent) {
                            position = ((WadlComponent) component).findPosition();
                            getEditorPane().setCaretPosition(position);
                            return;
                        }
                        int line = resultItem.getLineNumber();
                        position = NbDocument.findLineOffset(
                                (StyledDocument) getEditorPane().getDocument(), line);
                        getEditorPane().setCaretPosition(position);
                    } catch (Exception ex) {
                        getEditorPane().setCaretPosition(0);
                    //worst case, let open the document in editor
                    //do not throw one exception.
                    }
                }
            }
        };

        // create and associate lookup
        Node delegate = wadlDataObject.getNodeDelegate();
        SourceCookieProxyLookup lookup = new SourceCookieProxyLookup(new Lookup[]{
                    Lookups.fixed(new Object[]{
                        // Need ActionMap in lookup so editor actions work.
                        getActionMap(),
                        // Need the data object registered in the lookup so that the
                        // projectui code will close our open editor windows when the
                        // project is closed.
                        wadlDataObject,
                        // The Show Cookie in lookup to show schema component
                        showCookie,
                    }),
                }, delegate);
        associateLookup(lookup);
        addPropertyChangeListener("activatedNodes", lookup);

        caretListener = new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                timerSelNodes.restart();
            }
        };

        timerSelNodes = new Timer(1, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!isActiveTC() || getEditorPane() == null) {
                    return;
                }
                try {
                    WadlEditorSupport support = wadlDataObject.getWadlEditorSupport();
                    if (support == null || support.getModel().inSync()) {
                        return;
                    }
                } catch (IOException ex) {
                    return;
                }
                selectElementsAtOffset();
            }
        });
        timerSelNodes.setRepeats(false);
    }

    public JComponent getToolbarRepresentation() {
        Document doc = getEditorPane().getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
            if (toolbar == null) {
                toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(getEditorPane());
            }
            return toolbar;
        }
        return null;
    }

    public JComponent getVisualRepresentation() {
        return this;
    }

    public void setMultiViewCallback(final MultiViewElementCallback callback) {
        multiViewCallback = callback;
    }

    public void requestVisible() {
        if (multiViewCallback != null) {
            multiViewCallback.requestVisible();
        } else {
            super.requestVisible();
        }
    }

    public void requestActive() {
        if (multiViewCallback != null) {
            multiViewCallback.requestActive();
        } else {
            super.requestActive();
        }
    }

    protected String preferredID() {
        return getClass().getName();
    }

    public UndoRedo getUndoRedo() {
        return wadlDataObject.getWadlEditorSupport().getUndoManager();
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
    protected boolean closeLast() {
        WadlEditorSupport ses = wadlDataObject.getWadlEditorSupport();
        JEditorPane[] editors = ses.getOpenedPanes();
        if (editors == null || editors.length == 0) {
            return ses.silentClose();
        }
        return false;
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!MultiViewSupport.isLastView(multiViewCallback.getTopComponent())) {
            return CloseOperationState.STATE_OK;
        }
        AbstractAction save = new AbstractAction(){
                    public void actionPerformed(ActionEvent arg0) {
                        //save changes
                        try {
                            wadlDataObject.getWadlEditorSupport().saveDocument();
                        } catch (IOException ex) {
                        }
                    }

                };
        save.putValue(Action.LONG_DESCRIPTION, NbBundle.getMessage(DataObject.class,
                            "MSG_SaveFile", // NOI18N
                            wadlDataObject.getPrimaryFile().getNameExt()));     
        return MultiViewFactory.createUnsafeCloseState(
                "ID_TEXT_CLOSING", // NOI18N
                save,
                MultiViewFactory.NOOP_CLOSE_ACTION);
        // return a placeholder state - to be sure our CloseHandler is called
        /*return MultiViewFactory.createUnsafeCloseState(
                "ID_TEXT_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);*/
    }

    public void componentActivated() {
        JEditorPane p = getEditorPane();
        if (p != null) {
            p.addCaretListener(caretListener);
        }
        if (timerSelNodes != null) {
            timerSelNodes.restart();
        }
        super.componentActivated();
        WadlEditorSupport editor = wadlDataObject.getWadlEditorSupport();
        editor.addUndoManagerToDocument();
    }

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
        if (timerSelNodes != null) {
            timerSelNodes.stop();
        }
        super.componentDeactivated();
        WadlEditorSupport editor = wadlDataObject.getWadlEditorSupport();
//        try {
//            if(editor.getDocument() != null) {
//                editor.getDocument().insertString(0, "a", null);
//                editor.getDocument().remove(0, 1);
//                editor.saveDocument();
//            }
//        } catch (IOException ex) {
//        } catch (BadLocationException ex) {
//        }
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
    }

    public void componentOpened() {
        super.componentOpened();
    }

    public void componentClosed() {
        super.componentClosed();
    }

    public void componentShowing() {
        super.componentShowing();
        WadlEditorSupport editor = wadlDataObject.getWadlEditorSupport();
        editor.addUndoManagerToDocument();
    }

    public void componentHidden() {
        super.componentHidden();
        WadlEditorSupport editor = wadlDataObject.getWadlEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // The superclass persists things such as the caret position.
        super.writeExternal(out);
        out.writeObject(wadlDataObject);
    }

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        // Since we are persistent and not created by the descriptor when
        // deserialized, we need to retrieve the data object for ourselves.
        Object firstObject = in.readObject();
        if (firstObject instanceof WadlDataObject) {
            wadlDataObject = (WadlDataObject) firstObject;
        }
        initialize();
    }    
    
    
    // node support
    /** Root node of schema model */
    private Node rootNode;
    /** current selection*/
    private Node selectedNode;
    /** listens to selected node destroyed event */
    private NodeAdapter nl;
    /** Timer which countdowns the "update selected element node" time. */
    private Timer timerSelNodes;
    /** Listener on caret movements */
    private CaretListener caretListener;
    /* task */
    private transient RequestProcessor.Task selectionTask = null;

    /** Selects element at the caret position. */
    void selectElementsAtOffset() {
        if (selectionTask != null) {
            selectionTask.cancel();
            selectionTask = null;
        }
        RequestProcessor rp = new RequestProcessor("schema source view processor " + hashCode());
        selectionTask = rp.create(new Runnable() {

            public void run() {
                if (!isActiveTC() || wadlDataObject == null ||
                        !wadlDataObject.isValid() || wadlDataObject.isTemplate()) {
                    return;
                }
                Node n = null;//findNode(getEditorPane().getCaret().getDot());
                // default to node delegate if node not found
                if (n == null) {
                    setActivatedNodes(new Node[]{
                                wadlDataObject.getNodeDelegate()
                            });
                } else {
                    if (selectedNode != n) {
                        if (nl == null) {
                            nl = new NodeAdapter() {

                                public void nodeDestroyed(NodeEvent ev) {
                                    if (ev.getNode() == selectedNode) {
                                        selectElementsAtOffset();
                                    }
                                }
                            };
                        } else if (selectedNode != null) {
                            selectedNode.removeNodeListener(nl);
                        }
                        selectedNode = n;
                        selectedNode.addNodeListener(nl);
                        setActivatedNodes(new Node[]{selectedNode});
                    }
                }
            }
        });
        if (EventQueue.isDispatchThread()) {
            selectionTask.run();
        } else {
            EventQueue.invokeLater(selectionTask);
        }
    }
//	private Node findNode(int offset)
//	{
//		try
//		{
//			WadlEditorSupport support = schemaDataObject.getWadlEditorSupport();
//			if(support==null) return null;
//			WadlModel model = support.getModel();
//			if(model==null||model.getState()!=WadlModel.State.VALID) return null;
//			if(rootNode==null)
//				rootNode = new StructuralWadlNodeFactory(support.getModel(),
//						schemaDataObject.getNodeDelegate().getLookup()).createRootNode();
//			if(rootNode==null) return null;
//			WadlComponent sc = (WadlComponent) support.getModel().
//					findComponent(offset);
//			if(sc==null) return null;
//			List<Node> path = UIUtilities.findPathFromRoot(rootNode,sc);
//			if(!path.isEmpty())
//				return path.get(path.size()-1);
//		}
//		catch (IOException ioe)
//		{
//		}
//		return null;
//	}
    protected boolean isActiveTC() {
        return getRegistry().getActivated() == multiViewCallback.getTopComponent();
    }
}
