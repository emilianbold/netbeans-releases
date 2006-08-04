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
 * ClientMultiViewElement.java
 *
 * Created on July 22, 2005, 11:21 AM
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
public class ClientMultiViewElement extends ToolBarMultiViewElement {
    
    private SectionView view;
    private ToolBarDesignEditor comp;    
    private int index;
    
    protected ClientViewFactory factory;
    
    private boolean needInit = true;
    
    /** Creates a new instance of ClientMultiViewElement */
    public ClientMultiViewElement( E2EDataObject dataObject, int index ) {
        super( dataObject );
        this.index = index;
        
        comp = new ToolBarDesignEditor();
        factory = new ClientViewFactory( comp, dataObject );
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
        if( needInit ) {
            repaintView();
            needInit = false;
        }
    }
    
    protected void repaintView() {
        view = new ClientView();
        comp.setContentView( view );
        final Object lastActive = comp.getLastActive();
        if( lastActive != null ) {
            view.openPanel( lastActive );
        } else {
            view.openPanel( "clientOptions" );  // NOI18N
            view.openPanel( "clientGeneralInfo" );  // NOI18N
            view.openPanel( "services" );  // NOI18N
        }
        view.checkValidity();
    }
    
    public SectionView getSectionView() {
        return view;
    }
    
    public CloseOperationState canCloseElement() {
        // FIXME: save state
        return CloseOperationState.STATE_OK;
    }
    
    private class ClientView extends SectionView {
        
        final private Node servicesNode, clientGeneralInfoNode, clientOptionsNode;
        
        public ClientView() {
            super( factory );
            
            servicesNode = new ServicesNode();
            addSection( new SectionPanel( this, servicesNode, ClientViewFactory.PROP_PANEL_SERVICES ));
            
            clientGeneralInfoNode = new ClientGeneralInfoNode();
            addSection( new SectionPanel( this, clientGeneralInfoNode, ClientViewFactory.PROP_PANEL_CLIENT_GENERAL ));
            
            clientOptionsNode = new ClientOptionsNode();
            addSection( new SectionPanel( this, clientOptionsNode, ClientViewFactory.PROP_PANEL_CLIENT_OPTIONS )); // NOI18N
            
            Children rootChildren = new Children.Array();
            rootChildren.add( new Node[]{ servicesNode, clientGeneralInfoNode, clientOptionsNode });
            AbstractNode root = new AbstractNode( rootChildren );
            setRoot( root );
        }
        
        Node getServicesNode() {
            return servicesNode;
        }
        
        Node getClientGeneralInfoNode() {
            return clientGeneralInfoNode;
        }
        
        Node getClientOptionsNode() {
            return clientOptionsNode;
        }
    }
    
    /**
     * Represents section for Services view
     */
    private class ServicesNode extends AbstractNode {
        ServicesNode() {
            super( org.openide.nodes.Children.LEAF );
            setDisplayName( NbBundle.getMessage( ClientMultiViewElement.class, "TTL_ServicesSection" ));
            setIconBaseWithExtension("org/netbeans/modules/mobility/end2end/resources/e2eclienticon.png"); //NOI18N
            componentOpened();
        }
        
        public HelpCtx getHelpCtx() {
            // FIXME: help prefix fix
            return HelpCtx.DEFAULT_HELP;
        }
    }
    
    /**
     * Represents section for General Information about client
     */
    private class ClientGeneralInfoNode extends AbstractNode {
        ClientGeneralInfoNode() {
            super( org.openide.nodes.Children.LEAF );
            setDisplayName( NbBundle.getMessage( ClientMultiViewElement.class, "TTL_ClientGeneralInfoSection" ));
            setIconBaseWithExtension("org/netbeans/modules/mobility/end2end/resources/e2eclienticon.png"); //NOI18N
        }
        
        public HelpCtx getHelpCtx() {
            // FIXME: help prefix fix
            return HelpCtx.DEFAULT_HELP;
        }
    }
    
    /**
     * Represents section for Client Options
     */
    private class ClientOptionsNode extends AbstractNode {
        ClientOptionsNode() {
            super( org.openide.nodes.Children.LEAF );
            setDisplayName( NbBundle.getMessage( ClientMultiViewElement.class, "TTL_ClientOptionsSection" ));
            setIconBaseWithExtension("org/netbeans/modules/mobility/end2end/resources/e2eclienticon.png"); //NOI18N
        }
        
        public HelpCtx getHelpCtx() {
            // FIXME: help prefix fix
            return HelpCtx.DEFAULT_HELP;
        }
    }
}
