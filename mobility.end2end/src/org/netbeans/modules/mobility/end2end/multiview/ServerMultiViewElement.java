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

/*
 * ServerMultiViewElement.java
 *
 * Created on July 22, 2005, 1:43 PM
 *
 */
package org.netbeans.modules.mobility.end2end.multiview;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Michal Skvor
 */
public class ServerMultiViewElement extends ToolBarMultiViewElement {
    
    private SectionView view;
    private ToolBarDesignEditor comp;
    private int index;
    
    protected ServerViewFactory factory;
    
    private boolean needInit = true;
    
    /** Creates a new instance of ClientMultiViewElement */
    public ServerMultiViewElement( E2EDataObject dataObject, int index ) {
        super( dataObject );
        this.index = index;
        
        comp = new ToolBarDesignEditor();
        factory = new ServerViewFactory( comp, dataObject );
        setVisualEditor( comp );
        RequestProcessor.getDefault().create( new Runnable() {
            public void run() {
                javax.swing.SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        repaintView();
                    }
                });
            }
        });
    }
    
    public void componentShowing() {
        super.componentShowing();
        dObj.setLastOpenView( index );
        if( needInit /* || !dObj.isDocumentParseable() */) {
            repaintView();
            needInit = false;
        }
    }
    
    protected void repaintView() {
        view = new ServerView();
        comp.setContentView( view );
        
        final Object lastActive = comp.getLastActive();
        if( lastActive != null ) {
            view.openPanel( lastActive );
        } else {
            view.openPanel( "server" );  // NOI18N
        }
        
        view.checkValidity();
    }
    
    public SectionView getSectionView() {
        // FIXME: devel hack
        return new ServerView();
    }
    
    public CloseOperationState canCloseElement() {
        // FIXME: save state
        return CloseOperationState.STATE_OK;
    }
    
    private class ServerView extends SectionView {
        
        final private Node serverNode;
        
        public ServerView() {
            super( factory );
            
            serverNode = new ServerNode();
            addSection( new SectionPanel( this, serverNode, "server" ));    // NOI18N
            
            Children rootChildren = new Children.Array();
            rootChildren.add( new Node[]{ serverNode });
            AbstractNode root = new AbstractNode( rootChildren );
            setRoot( root );
        }
        
        Node getServerNode() {
            return serverNode;
        }
    }
    
    private class ServerNode extends AbstractNode {
        ServerNode() {
            super( org.openide.nodes.Children.LEAF );
            setDisplayName( NbBundle.getMessage( ServerMultiViewElement.class, "TTL_Server_Information" ));
            setIconBaseWithExtension("org/netbeans/modules/mobility/end2end/resources/server.png"); //NOI18N
        }
        
        public HelpCtx getHelpCtx() {
            // FIXME: help prefix fix
            return HelpCtx.DEFAULT_HELP;
        }
    }
}
