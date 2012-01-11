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
package org.netbeans.modules.versioning.ui.history;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.versioning.history.LinkButton;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.ui.history.RevisionNode.Filter;
import org.netbeans.modules.versioning.ui.options.HistoryOptions;
import org.openide.cookies.SaveCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
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
        // no icon
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="text.history", 
        mimeType="",
        position=1000000 // lets leave some space in case somebody really wants to be the last
)
final public class HistoryTopComponent extends TopComponent implements MultiViewElement {

    private static HistoryTopComponent instance;
    private LocalHistoryFileView masterView;
    static final String PREFERRED_ID = "text.history";
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo(); 
    private Toolbar toolBar;
    private boolean isPartOfMultiview = false;
    private LocalHistoryDiffView diffView;
    
    public HistoryTopComponent() {
        initComponents();
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            setBackground(UIManager.getColor("NbExplorerView.background"));     // NOI18N
        }
        setToolTipText(NbBundle.getMessage(HistoryTopComponent.class, "HINT_LocalHistoryTopComponent"));
    }

    public HistoryTopComponent(Lookup context) {
        this();
        isPartOfMultiview = true;
        DataObject dataObject = context.lookup(DataObject.class);

        List<File> filesList = new LinkedList<File>();
        if (dataObject instanceof DataShadow) {
            dataObject = ((DataShadow) dataObject).getOriginal();
        }
        if (dataObject != null) {
            Collection<File> doFiles = toFileCollection(dataObject.files());
            filesList.addAll(doFiles);
        }
        
        File[] files = filesList.toArray(new File[filesList.size()]);
        VersioningSystem vs = VersioningSupport.getOwner(files[0]);
        toolBar = new Toolbar(vs, files);
        init(vs, files);    
    }
    
    private Collection<File> toFileCollection(Collection<? extends FileObject> fileObjects) {
        Set<File> files = new HashSet<File>(fileObjects.size()*4/3+1);
        for (FileObject fo : fileObjects) {
            files.add(FileUtil.toFile(fo));
        }
        files.remove(null);
        return files;
    }        

    public void init(VersioningSystem vs, final File... files) {   
        masterView = new LocalHistoryFileView(files, vs, this);
        diffView = new LocalHistoryDiffView(this); 
        
        masterView.getExplorerManager().addPropertyChangeListener(diffView); 
        masterView.getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {                            
                    HistoryTopComponent.this.setActivatedNodes((Node[]) evt.getNewValue());  
                } /*else if(ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                    System.out.println(" " + evt.getOldValue() +  " " + evt.getNewValue());
                }*/
                }
        });
        
        // XXX should be solved in a more general way - not ony for LocalHistoryFileView 
        splitPane.setTopComponent(masterView.getPanel());   
        splitPane.setBottomComponent(diffView.getPanel());                   
        masterView.requestActive();
        
        History.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                masterView.refresh();
            }
                });
            }
    
    Filter getSelectedFilter() {
        return (Filter) getToolbar().filterCombo.getSelectedItem();
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
    public static synchronized HistoryTopComponent getDefault() {
        if (instance == null) {
            instance = new HistoryTopComponent();
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
        return getToolbar();
            }

    private Toolbar getToolbar() {
        return toolBar;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {

    }

    @NbBundle.Messages({
        "MSG_SaveModified=File {0} is modified. Save?"
    })
    @Override
    public CloseOperationState canCloseElement() {
        File[] files = masterView.getFiles();
        if(files.length == 0) {
            return CloseOperationState.STATE_OK;
        }
        FileObject fo = FileUtil.toFileObject(files[0]);
        if(fo != null) {
            final DataObject dataObject;
            try {
                dataObject = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                History.LOG.log(Level.WARNING, null, ex);
                return CloseOperationState.STATE_OK;
            }
            if(dataObject != null && dataObject.isModified()) {
                AbstractAction save = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SaveCookie sc = dataObject.getLookup().lookup(SaveCookie.class);
                        if(sc != null) {
                            try {
                                sc.save();
                            } catch (IOException ex) {
                                History.LOG.log(Level.WARNING, null, ex);
                            }
                        }
                    }
                };
                save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_SaveModified(dataObject.getPrimaryFile().getNameExt()));
                return MultiViewFactory.createUnsafeCloseState("editor", save, null);
            }
        }    
        return CloseOperationState.STATE_OK;
    }

    String getActiveFilterValue() {
        return getToolbar().containsField.isVisible() ? getToolbar().containsField.getText() : null;
    }

    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return HistoryTopComponent.getDefault();
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
    
    private void onFilterChange() {
        Filter filter = getSelectedFilter();
        getToolbar().containsLabel.setVisible(filter instanceof ByUserFilter || filter instanceof ByMsgFilter);
        getToolbar().containsField.setVisible(filter instanceof ByUserFilter || filter instanceof ByMsgFilter);
        masterView.setFilter(getSelectedFilter());
        getToolbar().containsField.requestFocus();
    }    
        
    private class Toolbar extends JToolBar implements ActionListener {
        private JButton nextButton;
        private JButton prevButton;
        private JButton refreshButton;
        private JButton settingsButton;
        private JLabel filterLabel;
        private JComboBox filterCombo;
        private JLabel containsLabel;
        private JTextField containsField;
    
        private Toolbar(VersioningSystem vs, final File... files) {
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setOpaque(false);
            setBackground(Color.white);
            setLayout(new GridBagLayout());
            
            containsLabel = new JLabel(NbBundle.getMessage(HistoryTopComponent.class, "LBL_Contains"));  // NOI18N
            containsField = new JTextField();  
            containsField.setPreferredSize(new Dimension(150, containsField.getPreferredSize().height));
            containsField.getDocument().addDocumentListener(new ContainsListener());
            containsLabel.setVisible(false);
            containsField.setVisible(false);
            filterLabel = new JLabel(NbBundle.getMessage(HistoryTopComponent.class, "LBL_Filter"));  // NOI18N
            filterCombo = new JComboBox();
            filterCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if(value instanceof Filter) {
                        return super.getListCellRendererComponent(list, ((Filter) value).getDisplayName(), index, isSelected, cellHasFocus);
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
            filterCombo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    onFilterChange();
                }
            });
            nextButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/diff-next.png"))); 
            prevButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/diff-prev.png"))); 
            nextButton.addActionListener(this);
            prevButton.addActionListener(this);
            refreshButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/refresh.png"))); 
            refreshButton.addActionListener(this);
            settingsButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/ui/resources/icons/options.png"))); 
            settingsButton.addActionListener(this);
            Filter[] filters;
            if(vs != null && vs.getVCSHistoryProvider() != null) {
                filters = new Filter[] {
                    new AllFilter(), 
                    new VCSFilter((String) vs.getProperty(VersioningSystem.PROP_DISPLAY_NAME)), 
                    new LHFilter(),
                    new ByUserFilter(),
                    new ByMsgFilter()};
                    filterCombo.setModel(new DefaultComboBoxModel(filters)); 
            } else {
                filterCombo.setVisible(false);
                filterLabel.setVisible(false);
            }
            
            nextButton.setBorder(new EmptyBorder(0, 5, 0, 5));
            prevButton.setBorder(new EmptyBorder(0, 5, 0, 5));
            refreshButton.setBorder(new EmptyBorder(0, 0, 0, 0));
            filterLabel.setBorder(new EmptyBorder(0, 15, 0, 5));
            filterCombo.setBorder(new EmptyBorder(0, 5, 0, 5));
            containsLabel.setBorder(new EmptyBorder(0, 5, 0, 10));
            settingsButton.setBorder(new EmptyBorder(0, 5, 0, 10));

            GridBagConstraints c = new GridBagConstraints();
            add(nextButton, c); 
            add(prevButton, c); 
            add(refreshButton, c); 
            add(filterLabel, c); 
            add(filterCombo, c); 
            add(containsLabel, c); 
            add(containsField, c); 
            add(settingsButton);
  
            final Action openSearchHistoryAction = vs != null && vs.getVCSHistoryProvider() != null ? vs.getVCSHistoryProvider().createShowHistoryAction(files) : null;
            if(openSearchHistoryAction != null) {
                LinkButton searchHistoryButton = new LinkButton(NbBundle.getMessage(this.getClass(), "LBL_ShowVersioningHistory", new Object[] {vs.getProperty(VersioningSystem.PROP_DISPLAY_NAME)})); // NOI18N
                searchHistoryButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        History.getInstance().getRequestProcessor().post(new Runnable() {
                            @Override
                            public void run() {
                                openSearchHistoryAction.actionPerformed(e);
                            }
                        }); 
                    }
                });
            
                c = new GridBagConstraints();
                c.anchor = GridBagConstraints.EAST;
                c.weightx = 1;
                add(searchHistoryButton, c); 
            }
        }
            
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == getToolbar().nextButton) {
                diffView.onNextButton();
            } else if(e.getSource() == getToolbar().prevButton) {
                diffView.onPrevButton();
            } else if(e.getSource() == getToolbar().refreshButton) {
                masterView.refresh();
            } else if(e.getSource() == getToolbar().settingsButton) {
                OptionsDisplayer.getDefault().open(OptionsDisplayer.ADVANCED + "/Versioning/" + HistoryOptions.OPTIONS_SUBPATH);
            }
        }
    }

    void disableNavigationButtons() {
        getToolbar().prevButton.setEnabled(false);
        getToolbar().nextButton.setEnabled(false);
    }

    void refreshNavigationButtons(int currentDifference, int diffCount) {
        getToolbar().prevButton.setEnabled(currentDifference > 0);
        getToolbar().nextButton.setEnabled(currentDifference < diffCount - 1);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(isPartOfMultiview ? 
                           "org.netbeans.modules.localhistory.ui.view.LHHistoryTab" :               // NO18N
                           "org.netbeans.modules.localhistory.ui.view.LocalHistoryTopComponent");   // NO18N
    }

    private class AllFilter extends Filter {
        @Override
        public boolean accept(Object value) {
            return true;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HistoryTopComponent.class, "LBL_AllRevisionsFilter"); // NO18N   
        }
    }    
    private class VCSFilter extends Filter {
        private final String vcsName;
        public VCSFilter(String vscName) {
            this.vcsName = vscName;
        }
        @Override
        public boolean accept(Object value) {
            Collection<HistoryEntry> entries = getEntries(value);
            if(entries != null) {
                for (HistoryEntry e : entries) {
                    if(!e.isLocalHistory()) return true;
                }
            }
            return false;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HistoryTopComponent.class, "LBL_VCSRevisionsFilter", new Object[] {vcsName}); // NO18N
        }
    }    
    private class LHFilter extends Filter {
        @Override
        public boolean accept(Object value) {
            Collection<HistoryEntry> entries = getEntries(value);
            if(entries != null) {
                for (HistoryEntry e : entries) {
                    if(e.isLocalHistory()) return true;
                }
            }
            return false;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HistoryTopComponent.class, "LBL_LHRevisionsFilter"); // NO18N
        }
    }       
    private class ByUserFilter extends Filter {
        @Override
        public boolean accept(Object value) {
            String byUser = getToolbar().containsField.getText();
            if(byUser == null || "".equals(byUser)) return true;                // NOI18N
            
            Collection<HistoryEntry> entries = getEntries(value);
            if(entries != null) {
                for (HistoryEntry e : entries) {
                    String user = e.getUsernameShort();
                    if(user.toLowerCase().contains(byUser.toLowerCase())) return true;
                }
            }
            return false;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HistoryTopComponent.class, "LBL_ByUserFilter"); // NO18N
        }
        @Override
        public String getRendererValue(String value) {
            return getFilteredRendererValue(value);
        }        
    }           
    private class ByMsgFilter extends Filter {
        @Override
        public boolean accept(Object value) {
            String byMsg = getToolbar().containsField.getText();
            if(byMsg == null || "".equals(byMsg)) return true;
            
            Collection<HistoryEntry> entries = getEntries(value);
            if(entries != null) {
                for (HistoryEntry e : entries) {
                    String msg = e.getMessage();
                    if(msg.toLowerCase().contains(byMsg.toLowerCase())) return true;
                }
            }
            return false;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(HistoryTopComponent.class, "LBL_ByMsgFilter"); // NO18N
        }

        @Override
        public String getRendererValue(String value) {
            return getFilteredRendererValue(value);
        }

    }           
    private String getFilteredRendererValue(String value) {
        String contains = getToolbar().containsField.getText();
        if(contains == null || "".equals(contains)) {
            return value;
        }            
        StringBuilder sb = new StringBuilder();
        sb.append(value.replace(contains, "<b>" + contains + "</b>")); // NOI18N
        return sb.toString();
    }
    
    private class ContainsListener implements DocumentListener, ActionListener { 
        private final Timer t;
        public ContainsListener() {
            t = new Timer(300, this);
            t.setRepeats(false);
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
            t.start();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            t.start();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            t.start();
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            masterView.fireFilterChanged();
        }
    };    
}
