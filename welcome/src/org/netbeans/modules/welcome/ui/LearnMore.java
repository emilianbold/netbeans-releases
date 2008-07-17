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

package org.netbeans.modules.welcome.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.ActionButton;
import org.netbeans.modules.welcome.content.BackgroundPanel;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.Utils;
import org.netbeans.modules.welcome.content.WebLink;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author S. Aubrecht
 */
class LearnMore extends BackgroundPanel implements Constants {

    /** Creates a new instance of RecentProjects */
    public LearnMore() {
        super( new GridBagLayout() );
        buildContent();
    }
    
    private void buildContent() {
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource( "WelcomePage/LearnMoreLinks" ); // NOI18N
        DataFolder folder = DataFolder.findFolder( root );
        DataObject[] children = folder.getChildren();
        JPanel panel = new JPanel( new GridLayout(0, 2, 15, 5) );
        panel.setOpaque( false );
        for( int i=0; i<children.length; i++ ) {
            addLink( panel, children[i] );
        }
        
        add( panel, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0 ) );

        WebLink more = new WebLink( "MoreTutorials", false ); //NOI18N
        add( more, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(20,0,0,0), 0, 0 ) );
        add( new JLabel(), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0 ) );
    }

    private void addLink( JPanel panel, DataObject dob ) {
        OpenCookie oc = (OpenCookie)dob.getCookie( InstanceCookie.class );
        if( null != oc ) {
            LinkAction la = new LinkAction( dob );
            ActionButton lb = new ActionButton( la, true, Utils.getUrlString( dob ) );
            lb.getAccessibleContext().setAccessibleName( lb.getText() );
            lb.getAccessibleContext().setAccessibleDescription( 
                    BundleSupport.getAccessibilityDescription( "LearnMore", lb.getText() ) ); //NOI18N
            JPanel inner = new BackgroundPanel( new GridBagLayout() );
            inner.add( lb, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0 ) );
            inner.add( new JLabel(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0 ) );
            panel.add( inner );
        }
    }

    private static class LinkAction extends AbstractAction {
        private DataObject dob;
        public LinkAction( DataObject dob ) {
            super( dob.getNodeDelegate().getDisplayName() );
            this.dob = dob;
        }

        public void actionPerformed(ActionEvent e) {
            OpenCookie oc = dob.getCookie( OpenCookie.class );
            if( null != oc )
                oc.open();
        }
    }
}
