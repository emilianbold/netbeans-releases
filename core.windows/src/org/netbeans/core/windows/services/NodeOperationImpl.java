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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.services;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.WeakHashMap;
import org.netbeans.core.NbMainExplorer;
import org.netbeans.core.NbSheet;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.WeakSet;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.border.EmptyBorder;


// XXX Before as org.netbeans.core.NbNodeOperation.

/** Class that provides operations on nodes. Any part of system can
 * ask for opening a customizer or explorer on any node. These actions
 * are accessible thru this class.
 *
 * @author  Ian Formanek
 */
public final class NodeOperationImpl extends NodeOperation {

    /** Shows an explorer on the given root Node.
    * @param n the Node that will be the rootContext of the explored hierarchy
    */
    public void explore (final Node n) {
        Mutex.EVENT.readAccess (new Runnable () {
                public void run () {
                    NbMainExplorer.ExplorerTab et = new NonPersistentExplorerTab ();
                    et.setRootContext (n);
                    et.adjustComponentPersistence();

                    Mode target = WindowManager.getDefault().findMode("explorer");
                    if (target != null) {
                        target.dockInto(et);
                    }
                    et.open();
                    et.requestActive();
                }
            });
    }

    /** Tries to open customization for specified node. The dialog is
    * open in modal mode and the function returns after successful
    * customization.
    *
    * @param n the node to customize
    * @return <CODE>true</CODE> if the node has customizer,
    * <CODE>false</CODE> if not
    */
    public boolean customize (Node n) {
        final Component customizer = n.getCustomizer ();
        if (customizer == null) return false;
        return Mutex.EVENT.readAccess (new Mutex.Action<Boolean> () {
                public Boolean run () {
                    if (customizer instanceof NbPresenter) { // #9466
                        ((NbPresenter) customizer).pack ();
                        ((NbPresenter) customizer).show ();
                        return Boolean.TRUE;
                    }
                    if (customizer instanceof Window) {
                        ((Window) customizer).pack ();
                        customizer.setVisible (true);
                        return Boolean.TRUE;
                    }
                    
                    // preserve help context and explorer provider of customizer
                    JPanel p = null;
                    if (customizer instanceof ExplorerManager.Provider) {
                        p = new ExplorerProviderFwd(customizer, (ExplorerManager.Provider)customizer);
                    } else {
                        p = new HelpFwdPanel(customizer);
                    }
                    p.setLayout(new BorderLayout());
                    p.getAccessibleContext().setAccessibleDescription(
                        NbBundle.getMessage(NodeOperationImpl.class, "CTL_Customizer_dialog_title"));
                    
                    // #21547 adjust for XML that relies on container managed borders
                    // please DELETE after #19821 is fixed, immediatelly
                    if (customizer.getClass().getName().startsWith("org.netbeans.modules.xml.catalog")) {  // NOI18N
                        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 11));
                    }
                    // end of future DELETE
                    
                    p.add(customizer, BorderLayout.CENTER);
                    
                    // present it
                    DialogDescriptor dd = new DialogDescriptor
                        (p, 
                         NbBundle.getMessage(NodeOperationImpl.class, "CTL_Customizer_dialog_title"));
                    dd.setOptions(new Object[] { DialogDescriptor.CLOSED_OPTION });

                    Dialog dialog = org.openide.DialogDisplayer.getDefault ().createDialog(dd);
                    dialog.pack();
                    dialog.setVisible(true);
                    return Boolean.TRUE;
                }
            }).booleanValue ();
    }

    /** Panel, decorates given inner panel with forwarding of help context.
     * It will probably also decorate customizer with border (when #19821 is fixed)
     */
     private static class HelpFwdPanel extends JPanel implements HelpCtx.Provider {
        private Component innerComp;
        private boolean active = false;
        
        /** Not instantiatable outside */
        private HelpFwdPanel (Component innerComp) {
            this.innerComp = innerComp;
        }
        
        public HelpCtx getHelpCtx () {
            try {
                //??? eliminate recursion it delegates to parent (this)
                if (active) return null;
                active = true;
                return HelpCtx.findHelp(innerComp);  
            } finally  {
                active = false;
            }
        }
                                                   
    } // end of HelpFwdPanel
    
    /** Decorates given panel with explorer provider functionality, forwarding
     * to given original provider. */
    private static final class ExplorerProviderFwd extends HelpFwdPanel
                implements ExplorerManager.Provider {
        private ExplorerManager.Provider explProvider;
        
        /** Not instantiatable outside */
        private ExplorerProviderFwd (Component innerComp, ExplorerManager.Provider explProvider) {
            super(innerComp);
            this.explProvider = explProvider;
        }
                                                      
        /** Forwards to original explorer provider.
         */
        public ExplorerManager getExplorerManager() {
            return explProvider.getExplorerManager();
        }
        
     } // end of CustomizerDecorator

    /** Opens a modal propertySheet on given Node
    * @param n the node to show properties for
    */
    public void showProperties (Node n) {
        Dialog d = findCachedPropertiesDialog( n );
        if( null == d ) {
            NbSheet s = new NbSheet ();
            Node[] nds = new Node[] { n };
            s.setNodes (nds);
            openProperties(s, nds);
        } else {
            d.setVisible( true );
            d.toFront();
            FocusTraversalPolicy ftp = d.getFocusTraversalPolicy();
            if( null != ftp && null != ftp.getDefaultComponent(d) ) {
                ftp.getDefaultComponent(d).requestFocusInWindow();
            } else {
                d.requestFocusInWindow();
            }
        }
    }

    /** Opens a modal propertySheet on given set of Nodes
    * @param n the array of nodes to show properties for
    */
    public void showProperties (Node[] nodes) {
        Dialog d = findCachedPropertiesDialog( nodes );
        if( null == d ) {
            NbSheet s = new NbSheet ();
            s.setNodes (nodes);
            openProperties(s, nodes);
        } else {
            d.setVisible( true );
            d.toFront();
            FocusTraversalPolicy ftp = d.getFocusTraversalPolicy();
            if( null != ftp && null != ftp.getDefaultComponent(d) ) {
                ftp.getDefaultComponent(d).requestFocusInWindow();
            } else {
                d.requestFocusInWindow();
            }
        }
    }
    
    //#79126 - cache the open properties windows and reuse them if the Nodes 
    //are the same
    private static WeakSet<Node[]> nodeCache = new WeakSet<Node[]>();
    private static WeakHashMap<Node[], Dialog> dialogCache = new WeakHashMap<Node[], Dialog>();
    
    private static Dialog findCachedPropertiesDialog( Node n ) {
        return findCachedPropertiesDialog( new Node[] { n } );
    }
    
    private static Dialog findCachedPropertiesDialog( Node[] nodes ) {
        for( Iterator<Node[]> it=nodeCache.iterator(); it.hasNext(); ) {
            Node[] cached = it.next();
            if( cached.length != nodes.length )
                continue;
            boolean match = true;
            for( int i=0; i<cached.length; i++ ) {
                if( !cached[i].equals( nodes[i] ) ) {
                    match = false;
                    break;
                }
            }
            if( match ) {
                return dialogCache.get( cached );
            }
        }
        return null;
    }

    /** Opens explorer for specified root in modal mode. The set
    * of selected components is returned as a result. The acceptor
    * should be asked each time selected nodes changes to accept or
    * reject the current result. This should affect for example the
    * <EM>OK</EM> button.
    *
    * @param title is a title that will be displayed as a title of the window
    * @param root the root to explore
    * @param acceptor the class that is asked for accepting or rejecting
    *    current selection
    * @param top is a component that will be displayed on the top
    * @return array of selected (and accepted) nodes
    *
    * @exception UserCancelException selection interrupted by user
    */
    public Node[] select (String title, String rootTitle, Node root, NodeAcceptor acceptor, Component top)
    throws UserCancelException {
        final FileSelector selector = new FileSelector(rootTitle, root, acceptor, top);
        selector.setBorder(new EmptyBorder(12, 12, 0, 12));
        DialogDescriptor dd = new DialogDescriptor(selector, title, true, 
                                                   selector.getOptions(), 
                                                   selector.getSelectOption(), DialogDescriptor.DEFAULT_ALIGN,
                                                   HelpCtx.DEFAULT_HELP, null);
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret != selector.getSelectOption()) {
            throw new UserCancelException ();
        }
        return selector.getNodes ();
    }

    /** Helper method, opens properties top component in single mode
    * and requests a focus for it */
    private static void openProperties (final TopComponent tc, final Node[] nds) {
        // XXX #36492 in NbSheet the name is set asynch from setNodes.
//        Mutex.EVENT.readAccess (new Runnable () { // PENDING
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run () {
                    boolean modal;
                    if(NbPresenter.currentModalDialog == null) {
                        modal = false;
                    } else {
                        modal = true;
                    }
                    
                    Dialog dlg = org.openide.DialogDisplayer.getDefault().createDialog(new DialogDescriptor (
                        tc,
                        tc.getName(),
                        modal,
                        new Object [] {DialogDescriptor.CLOSED_OPTION},
                        DialogDescriptor.CLOSED_OPTION,
                        DialogDescriptor.BOTTOM_ALIGN,
                        null,
                        null
                    ));
                    //fix for issue #40323
                    SheetNodesListener listener = new SheetNodesListener(dlg, tc);
                    listener.attach(nds);
                    
                    nodeCache.add( nds );
                    dialogCache.put( nds, dlg );
                    
                    dlg.setVisible(true);
                }
            });
    }
    
    /** Not serializable explorer tab used in explore from here.
     */
    private static class NonPersistentExplorerTab extends NbMainExplorer.ExplorerTab {
        public int getPersistenceType() {
            return PERSISTENCE_NEVER;
        }
        
        protected String preferredID() {
            return "NonPersistentExplorerTab";
        }
    }

    /**
     * fix for issue #40323 the prop dialog needs to be closed when the nodes it displayes are destroyed.
     */
    private static class SheetNodesListener extends NodeAdapter implements PropertyChangeListener {

        private Dialog dialog;
        private Set<Node> listenerSet;
        /** top component we listen to for name changes */ 
        private TopComponent tc;
        
        SheetNodesListener(Dialog dialog, TopComponent tc) {
            this.dialog = dialog;
            this.tc = tc;
            tc.addPropertyChangeListener(this);
        }
        
        public void propertyChange (PropertyChangeEvent pce) {
            if ("name".equals(pce.getPropertyName())) {
                dialog.setTitle((String) pce.getNewValue());
            }
        }
        
        public void attach(Node[] nodes) {
            listenerSet = new HashSet<Node>(nodes.length * 2);
            for (int i = 0; i < nodes.length; i++) {
                listenerSet.add(nodes[i]);
                nodes[i].addNodeListener(this);
            };
        }

        /** Fired when the node is deleted.
         * @param ev event describing the node
         */
        public void nodeDestroyed(NodeEvent ev) {
            Node destroyedNode = ev.getNode();
            // stop to listen to destroyed node
            destroyedNode.removeNodeListener(this);
            listenerSet.remove(destroyedNode);
            // close top component (our outer class) if last node was destroyed
            if (listenerSet.isEmpty()) {
                // #68943 - stop to listen, as we are waving goodbye :-)
                tc.removePropertyChangeListener(this);
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        dialog.setVisible(false);
                        dialog.dispose();
                        dialog = null;
                    }
                });
            }
        }
    }
    
}
