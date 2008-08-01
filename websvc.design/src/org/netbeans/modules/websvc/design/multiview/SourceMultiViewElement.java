/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.websvc.design.multiview;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * The source editor element for JaxWs node.
 *
 * @author Ajit Bhate
 */
public class SourceMultiViewElement extends CloneableEditor
        implements MultiViewElement {
    private static final long serialVersionUID = 4403502726950453345L;
    private transient JComponent toolbar;
    private transient MultiViewElementCallback multiViewCallback;
    private transient Lookup myLookup;
    
    /**
     * Constructs a new instance of SourceMultiViewElement.
     */
    public SourceMultiViewElement() {
        // Needed for deserialization, do not remove.
        this(null);
    }
    
    /**
     * Constructs a new instance of SourceMultiViewElement.
     * 
     * @param support 
     */
    public SourceMultiViewElement(DataEditorSupport support) {
        super(support);
        initialize();
   }
    
    private void initialize() {
        ShowComponentCookie showCookie = new ShowComponentCookie() {            
            public void show(Object param) {
                if(!(param instanceof ElementHandle)) return;
                final ElementHandle handle = (ElementHandle)param;
                try {
                    JavaSource targetSource = JavaSource.forFileObject(getEditorSupport().getDataObject().getPrimaryFile());
                    CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
                       public void run(WorkingCopy workingCopy) throws java.io.IOException {
                            workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Element element = handle.resolve(workingCopy);
                            SourcePositions srcPos = workingCopy.getTrees().getSourcePositions();
                            int position = -1;
                            // use visitor approach later
                            switch(element.getKind()) {
                            case METHOD:
                                MethodTree methodTree = workingCopy.getTrees().getTree((ExecutableElement)element);
                                BlockTree methodBody = methodTree.getBody();
                                Tree tree = methodBody;
                                if(!methodBody.getStatements().isEmpty())
                                    tree = methodBody.getStatements().get(0);
                                position = (int) srcPos.getStartPosition(workingCopy.getCompilationUnit(), tree);
                                break;
                            }
                            if(position>0) {
                                getEditorPane().setCaretPosition(position);
                            }
                        }
                        public void cancel() {
                        }
                    };
                    targetSource.runModificationTask(task).commit();
                } catch (Exception ex) {
                }
            }
        };
        myLookup = Lookups.fixed(showCookie);
        
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
    
    @Override
    public void updateName() {
        super.updateName();
        //update html displayname of the main multiview component
        // fix bug 122727
        updateMultiViewHtmlDisplayName();
    }
    public void setMultiViewCallback(final MultiViewElementCallback callback) {
        multiViewCallback = callback;
        //set html displayname of the main multiview component
        updateMultiViewHtmlDisplayName();
    }
    
    private void updateMultiViewHtmlDisplayName() {
        if(multiViewCallback!=null) {
            if (EventQueue.isDispatchThread()) {
                multiViewCallback.getTopComponent().setHtmlDisplayName(getHtmlDisplayName());
            } else {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        multiViewCallback.getTopComponent().setHtmlDisplayName(getHtmlDisplayName());
                    }
                });
            }
        }
    }
    @Override
    public void componentActivated() {
        super.componentActivated();
    }
    
    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        setActivatedNodes(new Node[] {getEditorSupport().getDataObject().getNodeDelegate()});
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
        setActivatedNodes(new Node[] {});
    }
    
    @Override
    public void open() {
        if (multiViewCallback != null) {
            multiViewCallback.requestVisible();
        } else {
            super.open();
        }
        
    }
    
    @Override
    public void requestVisible() {
        if (multiViewCallback != null)
            multiViewCallback.requestVisible();
        else
            super.requestVisible();
    }
    
    @Override
    public void requestActive() {
        if (multiViewCallback != null)
            multiViewCallback.requestActive();
        else
            super.requestActive();
    }
    
    @Override
    protected String preferredID() {
        return getClass().getName();
    }
    
    
    @Override
    public UndoRedo getUndoRedo() {
        return super.getUndoRedo();
    }
    
    @Override
    protected boolean closeLast() {
        if(MultiViewSupport.getNumberOfClones(multiViewCallback.getTopComponent()) == 0) {
            // this is the last editor component so call super.closeLast
            return super.closeLast();
        }
        return true;
    }
    
    public CloseOperationState canCloseElement() {
        // if this is not the last cloned editor component, closing is OK
        if(!getEditorSupport().isModified() ||
                MultiViewSupport.getNumberOfClones(multiViewCallback.getTopComponent()) > 1) {
            return CloseOperationState.STATE_OK;
        }
        // return a state which will save/discard changes and is called by close handler
        return MultiViewFactory.createUnsafeCloseState(
                MultiViewSupport.SOURCE_UNSAFE_CLOSE,
                new AbstractAction() {
                    public void actionPerformed(ActionEvent arg0) {
                        //save changes
                        try {
                            getEditorSupport().saveDocument();
                            getEditorSupport().getDataObject().setModified(false);
                        } catch (IOException ex) {
                        }
                    }
                },
                new AbstractAction() {
                    public void actionPerformed(ActionEvent arg0) {
                        //discard changes
                    }
                });
    }
    
    private DataEditorSupport getEditorSupport() {
        return (DataEditorSupport) cloneableEditorSupport();
    }

    @Override
    public Lookup getLookup() {
        return new ProxyLookup(super.getLookup(), myLookup);
     }

}
