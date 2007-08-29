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

package org.netbeans.modules.welcome.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.ActionButton;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.Utils;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author S. Aubrecht
 */
class GetStarted extends JPanel implements Constants {

    private int row;

    /** Creates a new instance of RecentProjects */
    public GetStarted() {
        super( new GridBagLayout() );
        setOpaque( false );
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
        OpenCookie oc = (OpenCookie)dob.getCookie( InstanceCookie.class );
        if( null != oc ) {
            JPanel panel = new JPanel( new GridBagLayout() );
            panel.setOpaque( false );
            
            LinkAction la = new LinkAction( dob );
            ActionButton lb = new ActionButton( la, false, Utils.getUrlString( dob ) );
            panel.add( lb, new GridBagConstraints(1,0,1,3,0.0,0.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );
            
            panel.add( new JLabel(), 
                    new GridBagConstraints(2,0,GridBagConstraints.REMAINDER,1,1.0,0.0,GridBagConstraints.NORTHWEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
            
            //TODO remove when the 'tour' link is actually available on the web
            lb.setEnabled( !("tour".equals( dob.getName() )) );
            
            String bundleName = (String)dob.getPrimaryFile().getAttribute("SystemFileSystem.localizingBundle");//NOI18N
            if( null != bundleName ) {
                ResourceBundle bundle = NbBundle.getBundle(bundleName);
                Object imgKey = dob.getPrimaryFile().getAttribute("imageKey"); //NOI18N
                if( null != imgKey ) {
                    String imgLocation = bundle.getString(imgKey.toString());
                    Image img = Utilities.loadImage(imgLocation, true);
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
}
