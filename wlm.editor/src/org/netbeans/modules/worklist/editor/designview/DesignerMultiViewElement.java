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

package org.netbeans.modules.worklist.editor.designview;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.worklist.dataloader.WorklistDataObject;
import org.netbeans.modules.worklist.editor.utils.LookupUtils;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author anjeleevich
 */
public class DesignerMultiViewElement extends TopComponent 
        implements MultiViewElement 
{
//    private PropertyChangeListener listener = new PropertyChangeListener() {
//        public void propertyChange(PropertyChangeEvent evt) {
//            if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
//                Utils.runInAwtDispatchThread(new Runnable() {
//                    public void run() {
//                        if (null != callback){
//                            callback.getTopComponent().setDisplayName(
//                                    dataObject.getEditorSupport().messageName());
//                        }
//                    }
//                });
//            }
//        }
//    };    
    
    private WorklistDataObject dataObject;
    Node delegate;
    private DesignView designView;
    private JToolBar toolBar;
    private MultiViewElementCallback callback;
    private CookieProxyLookup cookieProxyLookup;

    private InstanceContent nodesLookupContent;
    private Lookup nodesLookup;
    
    private Lookup designViewLookup;

    private PropertyChangeListener activeNodesAndLookupSync 
            = new PropertyChangeListener()
    {
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(evt
                    .getPropertyName()))
            {
                Node[] nodes = getActivatedNodes();
                LookupUtils.updateNodesInLookup(nodes, nodesLookup,
                        nodesLookupContent);
            }
        }
    };
    
    public DesignerMultiViewElement(WorklistDataObject dataObject) {
        this.dataObject = dataObject;
        this.delegate = dataObject.getNodeDelegate();
        
        designView = new DesignView(this, dataObject);
        
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.addSeparator();
        toolBar.add(new ValidateAction(dataObject));
        toolBar.addSeparator();
        toolBar.add(designView.getExpandLDAPCompactBrowserButton());
        
        setLayout(new BorderLayout());
        add(designView, BorderLayout.CENTER);

        nodesLookupContent = new InstanceContent();
        nodesLookup = new AbstractLookup(nodesLookupContent);

        designViewLookup = new ProxyLookup(dataObject.getLookup(),
                Lookups.singleton(designView),
//                delegate.getLookup(),
                Lookups.singleton(dataObject),
                nodesLookup);

        associateLookup(designViewLookup);

        addPropertyChangeListener(activeNodesAndLookupSync);

        setActivatedNodes(new Node[] { delegate });

        initialize();
    }

    private void initialize() {
        cookieProxyLookup = new CookieProxyLookup(new Lookup[] {
                Lookups.fixed(new Object[] {
                        // Need ActionMap in lookup so our actions are used.
                        // actionMap,
                        // Need the data object registered in the lookup so that the
                        // projectui code will close our open editor windows when the
                        // project is closed.
                        dataObject,
                }),
                dataObject.getLookup(),// this lookup contain objects that are used in OM clients
                Lookups.singleton(this),

                // Lookups.singleton(SoaPaletteFactory.getPalette()),
                // myNodesMediator.getLookup(),
                // The Node delegate Lookup must be the last one in the list
                // for the CookieProxyLookup to work properly.
                delegate.getLookup(),
        }, delegate);

        // associateLookup(cookieProxyLookup);
    }

    @Override
    public Lookup getLookup() {
        return designViewLookup;
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }

    @Override
    public void componentClosed() {
        designView.getLDAPCompactBrowser().back();
        super.componentClosed();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
    }
    
    public JComponent getVisualRepresentation() {
        return this;
    }

    public JComponent getToolbarRepresentation() {
        return toolBar;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public UndoRedo getUndoRedo() {
        return dataObject.getWlmEditorSupport().getUndoManager();
    }
}
