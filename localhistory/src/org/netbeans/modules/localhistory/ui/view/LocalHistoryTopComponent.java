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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.localhistory.ui.view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.awt.UndoRedo;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.versioning.history.LinkButton;
import org.netbeans.modules.versioning.util.SearchHistorySupport;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Top component which displays something.
 * 
 * @author Tomas Stupka
 */
@MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",
        iconBase="", // no icon
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="text.history", 
        mimeType="",
        position=1000000 // lets leave some space in case somebody really wants to be the last
)
final public class LocalHistoryTopComponent extends TopComponent implements MultiViewElement {

    private static LocalHistoryTopComponent instance;
    private LocalHistoryFileView masterView;
    static final String PREFERRED_ID = "text.history";
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo(); 
    private JPanel toolBar;
    private boolean isPartOfMultiview = false;
    
    public LocalHistoryTopComponent() {
        initComponents();
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            setBackground(UIManager.getColor("NbExplorerView.background"));     // NOI18N
        }
        setToolTipText(NbBundle.getMessage(LocalHistoryTopComponent.class, "HINT_LocalHistoryTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
    }

    public LocalHistoryTopComponent(Lookup context) {
        this();
        isPartOfMultiview = true;
        DataObject dataObject = context.lookup(DataObject.class);

        List<File> files = new LinkedList<File>();
        if (dataObject instanceof DataShadow) {
            dataObject = ((DataShadow) dataObject).getOriginal();
        }
        if (dataObject != null) {
            Collection<File> doFiles = toFileCollection(dataObject.files());
            files.addAll(doFiles);
        }
        init(files.toArray(new File[files.size()]));    
    }
    
    private Collection<File> toFileCollection(Collection<? extends FileObject> fileObjects) {
        Set<File> files = new HashSet<File>(fileObjects.size()*4/3+1);
        for (FileObject fo : fileObjects) {
            files.add(FileUtil.toFile(fo));
        }
        files.remove(null);
        return files;
    }        

    public void init(final File... files) {   
        final LocalHistoryFileView fileView = new LocalHistoryFileView();                
        LocalHistoryDiffView diffView = new LocalHistoryDiffView(this); 
        fileView.getExplorerManager().addPropertyChangeListener(diffView); 
        fileView.getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {                            
                    LocalHistoryTopComponent.this.setActivatedNodes((Node[]) evt.getNewValue());  
                }
            } 
        });
        
        // XXX should be solved in a more general way - not ony for LocalHistoryFileView 
        this.masterView = fileView; 
        splitPane.setTopComponent(masterView.getPanel());   
        splitPane.setBottomComponent(diffView.getPanel());                   
        masterView.requestActive();
        
        LocalHistory.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                initToolbar();
                fileView.refresh(files);
            }

            private boolean initToolbar() {
                if (files.length == 0) {
                    return true;
                }
                FileObject fo = FileUtil.toFileObject(files[0]);
                if (fo == null) {
                    return true;
                }
                final Object attr = fo.getAttribute(SearchHistorySupport.PROVIDED_EXTENSIONS_SEARCH_HISTORY);
                if (attr == null || !(attr instanceof SearchHistorySupport)) {
                    return true;
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ((Toolbar)LocalHistoryTopComponent.this.getToolbarRepresentation()).setSupport((SearchHistorySupport) attr);
                    }
                });
                return false;
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

        splitPane.setDividerLocation(150);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane();
    // End of variables declaration//GEN-END:variables

    public UndoRedo getUndoRedo() {
        return delegatingUndoRedo;
    }
    
    void setDiffView(JComponent currentDiffView) {
        delegatingUndoRedo.setDiffView(currentDiffView);
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized LocalHistoryTopComponent getDefault() {
        if (instance == null) {
            instance = new LocalHistoryTopComponent();
        }
        return instance;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public void componentOpened() {
        super.componentOpened();
    }

    public void componentClosed() {
        if(masterView != null) {
            masterView.close();
        }
        super.componentClosed();
    }

    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        synchronized(this) {
            if(toolBar == null) { 
                toolBar = new Toolbar();
            }
        }
        return toolBar;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {

    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return LocalHistoryTopComponent.getDefault();
        }
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
}

    @Override
    public void componentActivated() {
        super.componentActivated();
        if(masterView != null) {
            masterView.requestActive();
        }
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }  
    
    private class Toolbar extends JPanel {
        private LinkButton searchHistoryButton;
        private SearchHistorySupport support;
        
        public Toolbar() {
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setOpaque(false);
            setBackground(Color.white);
            setLayout(new GridBagLayout());
            
            JLabel label = new JLabel(NbBundle.getMessage(this.getClass(), "LBL_LocalHistory")); // NOI18N
            Font f = label.getFont();
            label.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
            
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.CENTER;
            c.weightx = 1;
            add(label, c); 
  
            searchHistoryButton = new LinkButton(NbBundle.getMessage(this.getClass(), "LBL_ShowVersioningHistory")); // NOI18N
            searchHistoryButton.setVisible(false);
            searchHistoryButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LocalHistory.getInstance().getParallelRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            if(support == null) {
                                return;
                            }
                            try {
                                support.searchHistory(-1);
                            } catch (IOException ex) {
                                LocalHistory.LOG.log(Level.WARNING, null, ex);
                            }
                        }
                    });
                }
            });
            
            c = new GridBagConstraints();
            c.anchor = GridBagConstraints.EAST;
            c.weightx = 0;
            add(searchHistoryButton, c); 
            
        }

        public void setSupport(SearchHistorySupport support) {
            this.support = support;
            searchHistoryButton.setVisible(true);
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(isPartOfMultiview ? 
                           "org.netbeans.modules.localhistory.ui.view.LHHistoryTab" :               // NO18N
                           "org.netbeans.modules.localhistory.ui.view.LocalHistoryTopComponent");   // NO18N
    }
}
