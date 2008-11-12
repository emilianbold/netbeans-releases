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
package org.netbeans.modules.java.debug;

import com.sun.source.util.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
            view.setUseSubstringInQuickSearch(true);
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
            OffsetsBag bag = new OffsetsBag(doc, true);

            for (Node n : manager.getSelectedNodes()) {
                if (n instanceof OffsetProvider) {
                    OffsetProvider p = (OffsetProvider) n;
                    final int start = p.getStart();
                    final int end = p.getEnd();
                    final int pref = p.getPreferredPosition();
                    
                    if (start >= 0 && end >= 0) {
                        bag.addHighlight(start, end, HIGHLIGHT);
                    }
                    
                    if (pref >= 0) {
                        bag.addHighlight(pref, pref+1, HIGHLIGHT_PREF);
                    }
                }
            }

            getBag(doc).setHighlights(bag);
        } catch (DataObjectNotFoundException ex) {
            Logger.getLogger(TreeNavigatorProviderImpl.class.getName()).log(Level.FINE, null, ex);
        }
    }
    
    private static final AttributeSet HIGHLIGHT = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(224, 224, 224));
    private static final AttributeSet HIGHLIGHT_PREF = AttributesUtilities.createImmutable(StyleConstants.Underline, new Color(30, 255, 0));
    
    private final class TaskImpl implements CancellableTask<CompilationInfo> {
        
        public void cancel() {
        }

        public void run(CompilationInfo info) {
            manager.setRootContext(TreeNode.getTree(info, new TreePath(info.getCompilationUnit())));
        }
        
    }
    
}
