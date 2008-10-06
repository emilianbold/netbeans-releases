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
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.ActionButton;
import org.netbeans.modules.welcome.content.BackgroundPanel;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.Utils;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
class GetStarted extends BackgroundPanel implements Constants {

    private int row;

    /** Creates a new instance of RecentProjects */
    public GetStarted() {
        super( new GridBagLayout() );
        buildContent();
    }
    
    private void buildContent() {
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource( "WelcomePage/GettingStartedLinks" ); // NOI18N
        DataFolder folder = DataFolder.findFolder( root );
        DataObject[] children = folder.getChildren();
        for( int i=0; i<children.length; i++ ) {
            if( children[i].getPrimaryFile().isFolder() ) {
                String headerText = children[i].getNodeDelegate().getDisplayName();
                JLabel lblTitle = new JLabel( headerText );
                lblTitle.setFont( BUTTON_FONT );
                lblTitle.setHorizontalAlignment( JLabel.LEFT );
                lblTitle.setOpaque( true );
                lblTitle.setBorder( HEADER_TEXT_BORDER );
                add( lblTitle, new GridBagConstraints( 0,row++,1,1,1.0,0.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0,0,0,0), 0, 0 ) );

                DataFolder subFolder = DataFolder.findFolder( children[i].getPrimaryFile() );
                DataObject[] subFolderChildren = subFolder.getChildren();
                for( int j=0; j<subFolderChildren.length; j++ ) {
                    row = addLink( row, subFolderChildren[j] );
                }
                    
            } else {
                row = addLink( row, children[i] );
            }
        }

        add( new JLabel(), new GridBagConstraints(0, row++, 1, 1, 0.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, new Insets(0,0,0,0), 0, 0 ) );
    }

    private int addLink( int row, DataObject dob ) {
        FileObject file = dob.getPrimaryFile();
        String fileName = file.getName();
        if( !fileName.endsWith("_default") ) {
            String prefCluster = getPreferredCluster();
            if( !fileName.endsWith(prefCluster) ) {
                return row;
            } 
        }
        OpenCookie oc = (OpenCookie)dob.getCookie( InstanceCookie.class );
        if( null != oc ) {
            JPanel panel = new BackgroundPanel( new GridBagLayout() );
            
            LinkAction la = new LinkAction( dob );
            ActionButton lb = new ActionButton( la, false, Utils.getUrlString( dob ) );
            panel.add( lb, new GridBagConstraints(1,0,1,3,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );
            lb.setFont( GET_STARTED_FONT );
            
            panel.add( new JLabel(), 
                    new GridBagConstraints(2,0,1,3,1.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
            
            String bundleName = (String)dob.getPrimaryFile().getAttribute("SystemFileSystem.localizingBundle");//NOI18N
            if( null != bundleName ) {
                ResourceBundle bundle = NbBundle.getBundle(bundleName);
                Object imgKey = dob.getPrimaryFile().getAttribute("imageKey"); //NOI18N
                if( null != imgKey ) {
                    String imgLocation = bundle.getString(imgKey.toString());
                    Image img = ImageUtilities.loadImage(imgLocation, true);
                    JLabel lbl = new JLabel( new ImageIcon(img) );
                    lbl.setVerticalAlignment( SwingConstants.TOP );
                    panel.add( lbl, 
                            new GridBagConstraints(0,0,1,3,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,18),0,0) );
                }
            }
                
            lb.getAccessibleContext().setAccessibleName( lb.getText() );
            lb.getAccessibleContext().setAccessibleDescription( 
                    BundleSupport.getAccessibilityDescription( "GettingStarted", lb.getText() ) ); //NOI18N
            add( panel, new GridBagConstraints( 0,row++,1,1,1.0,0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0,0,7,0), 0, 0 ) );
        }
        return row;
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
    
    private String getPreferredCluster() {
        
        String preferredCluster = "java";
        try {
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("/productid"); // NOI18N
            if (fo != null) {
                InputStream is = fo.getInputStream();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader (is));
                    String clusterList = r.readLine().trim().toLowerCase();
                    if( clusterList.contains("java") ) {
                        preferredCluster = "java";
                    } else if( clusterList.contains("ruby") ) {
                        preferredCluster = "ruby";
                    } else if( clusterList.contains("cnd") ) {
                        preferredCluster = "cnd";
                    } else if( clusterList.contains("php") ) {
                        preferredCluster = "php";
                    }
                } finally {
                    is.close();
                }
            }
        } catch (IOException ignore) {
            Logger.getLogger(GetStarted.class.getName()).log(Level.FINE, null, ignore);
        }
        return preferredCluster;
    }
}
