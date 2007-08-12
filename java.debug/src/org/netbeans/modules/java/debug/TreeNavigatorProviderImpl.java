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
package org.netbeans.modules.java.debug;

import com.sun.source.util.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class TreeNavigatorProviderImpl implements NavigatorPanel {
    
    private JComponent panel;
    private final ExplorerManager manager = new ExplorerManager();
    
    /**
     * Default constructor for layer instance.
     */
    public TreeNavigatorProviderImpl() {
        manager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    setHighlights(TreeNavigatorJavaSourceFactory.getInstance().getFile(), manager);
                }
            }
        });
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(TreeNavigatorProviderImpl.class, "NM_Trees");
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(TreeNavigatorProviderImpl.class, "SD_Trees");
    }
    
    public JComponent getComponent() {
        if (panel == null) {
            final BeanTreeView view = new BeanTreeView();
            view.setRootVisible(true);
            view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            class Panel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
                // Make sure action context works correctly:
                private final Lookup lookup = ExplorerUtils.createLookup(manager, new ActionMap());
                {
                    setLayout(new BorderLayout());
                    add(view, BorderLayout.CENTER);
                }
                public ExplorerManager getExplorerManager() {
                    return manager;
                }
                public Lookup getLookup() {
                    return lookup;
                }
            }
            panel = new Panel();
        }
        return panel;
    }
    
    public Lookup getLookup() {
        return null;
    }

    public void panelActivated(Lookup context) {
        TreeNavigatorJavaSourceFactory.getInstance().setLookup(context, new TaskImpl());
    }

    public void panelDeactivated() {
        TreeNavigatorJavaSourceFactory.getInstance().setLookup(Lookup.EMPTY, null);
    }

    static OffsetsBag getBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(TreeNavigatorProviderImpl.class);
                
        if (bag == null) {
            doc.putProperty(TreeNavigatorProviderImpl.class, bag = new OffsetsBag(doc));
        }
        
        return bag;
    }
    
    static void setHighlights(FileObject file, ExplorerManager manager) {
        if (file == null) {
            return;
        }
        try {
            DataObject od = DataObject.find(file);

            EditorCookie ec = od.getLookup().lookup(EditorCookie.class);

            if (ec == null) {
                return;
            }
            Document doc = ec.getDocument();

            if (doc == null) {
                return;
            }
            OffsetsBag bag = new OffsetsBag(doc);

            for (Node n : manager.getSelectedNodes()) {
                if (n instanceof OffsetProvider) {
                    OffsetProvider p = (OffsetProvider) n;
                    final int start = p.getStart();
                    final int end = p.getEnd();
                    
                    if (start >= 0 && end >= 0) {
                        bag.addHighlight(start, end, HIGHLIGHT);
                    }
                }
            }

            getBag(doc).setHighlights(bag);
        } catch (DataObjectNotFoundException ex) {
            Logger.getLogger(TreeNavigatorProviderImpl.class.getName()).log(Level.FINE, null, ex);
        }
    }
    
    private static final AttributeSet HIGHLIGHT = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(224, 224, 224));
    
    private final class TaskImpl implements CancellableTask<CompilationInfo> {
        
        public void cancel() {
        }

        public void run(CompilationInfo info) {
            manager.setRootContext(TreeNode.getTree(info, new TreePath(info.getCompilationUnit())));
        }
        
    }
    
}
