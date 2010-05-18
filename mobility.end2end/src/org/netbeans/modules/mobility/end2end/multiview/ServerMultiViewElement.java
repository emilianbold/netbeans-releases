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
