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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.ActionButton;
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
class LearnMore extends JPanel implements Constants {

    private int maxRow;

    /** Creates a new instance of RecentProjects */
    public LearnMore() {
        super( new GridBagLayout() );
        setOpaque( false );
        buildContent();
    }
    
    private void buildContent() {
        int row = 0;
        int col = 0;
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource( "WelcomePage/LearnMoreLinks" ); // NOI18N
        DataFolder folder = DataFolder.findFolder( root );
        DataObject[] children = folder.getChildren();
        for( int i=0; i<children.length; i++ ) {
            row = addLink( row, col, children[i] );
            if( children.length >= 8 && i+1 == children.length/2+children.length%2 ) {
                col = 1;
                maxRow = row;
                row = 0;
            }
        }

        WebLink more = new WebLink( "MoreTutorials", false ); //NOI18N
        add( more, new GridBagConstraints(0, maxRow == 0 ? row++ : maxRow+1, col+1, 1, 1.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(15,0,0,0), 0, 0 ) );
        
//        add( new JLabel(), new GridBagConstraints(col+1, 0, 1, 1, 1.0, 0.0,
//                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0 ) );
    }

    private int addLink( int row, int col, DataObject dob ) {
        OpenCookie oc = (OpenCookie)dob.getCookie( InstanceCookie.class );
        if( null != oc ) {
            LinkAction la = new LinkAction( dob );
            ActionButton lb = new ActionButton( la, true, Utils.getUrlString( dob ) );
            lb.getAccessibleContext().setAccessibleName( lb.getText() );
            lb.getAccessibleContext().setAccessibleDescription( 
                    BundleSupport.getAccessibilityDescription( "LearnMore", lb.getText() ) ); //NOI18N
            add( lb, new GridBagConstraints( col,row++,1,1,1.0,0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0,col == 0 ? 0 : 20,5,0), 0, 0 ) );
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
