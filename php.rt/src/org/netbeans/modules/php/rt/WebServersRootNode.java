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
package org.netbeans.modules.php.rt;

import java.util.Collection;
import java.util.Collections;

import javax.swing.Action;

import org.netbeans.modules.php.rt.actions.AddHostAction;
import org.netbeans.modules.php.rt.resources.ResourceMarker;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class WebServersRootNode extends AbstractNode implements HostListener { 
    

    static final String WEB_SERVERS     = "WEB-SERVERS";                // NOI18N
    
    static final String LBL_WEB_SERVERS = "LBL_Web_Servers";            // NOI18N 
    
    static final String LBL_DESCRIPTION = "LBL_Description";            // NOI18N
    

    public WebServersRootNode( ) {
        super( new WebServerChildren() );
        setName( WEB_SERVERS );
        setDisplayName ( NbBundle.getMessage( ResourceMarker.class , 
                LBL_WEB_SERVERS ));
        setIconBaseWithExtension( ResourceMarker.getLocation()+
                ResourceMarker.SERVERS_ICON);
        setShortDescription( NbBundle.getMessage( ResourceMarker.class, 
                LBL_DESCRIPTION));
        
        WebServerRegistry.getInstance().addListener( this );
    }

    /* (non-Javadoc)
     * @see org.openide.nodes.Node#getActions(boolean)
     */
    @Override
    public Action[] getActions( boolean context ) {
        return  ACTIONS;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.php.webservers.HostListener#hostAdded(org.netbeans.modules.php.webservers.Host)
     */
    public void hostAdded( Host host ) {
        updateKeys();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.webservers.HostListener#hostRemoved(org.netbeans.modules.php.webservers.Host)
     */
    public void hostRemoved( Host host ) {
        updateKeys();        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.webservers.HostListener#hostUpdated(org.netbeans.modules.php.webservers.Host)
     */
    public void hostUpdated( Host host ) {
        updateKeys();        
    }
    
    
    private void updateKeys() {
        ( (WebServerChildren)getChildren()).updateKeys();
    }
    
    private static class WebServerChildren extends Children.Keys {

        public WebServerChildren() {
            updateKeys();
        }

        /* (non-Javadoc)
         * @see org.openide.nodes.Children.Keys#createNodes(java.lang.Object)
         */
        @Override
        protected Node[] createNodes( Object key )
        {
            Host host = (Host)key; 
            Node node = host.getProvider().createNode(host);
            if ( node != null ) {
                return new Node[]{ node };
            }
            else {
                return new Node[0];
            }
        }

        void updateKeys() {
            Collection<Host> collection = 
                WebServerRegistry.getInstance().getHosts();
            
            setKeys( Collections.emptySet() );
            setKeys( collection );
            
        }
        
    }
    
    private static final Action[] ACTIONS = new Action[] {  new AddHostAction() };

}
