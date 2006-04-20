/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.ContentPanel;
import org.netbeans.modules.welcome.content.LinkButton;
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
public class GetStarted extends ContentPanel {

    private int row;

    /** Creates a new instance of RecentProjects */
    public GetStarted() {
        super( BundleSupport.getLabel( "GettingStarted" ) ); // NOI18N

        setContent( buildContent() );
    }

    private JComponent buildContent() {
        int row = 0;
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource( "WelcomePage/GettingStartedLinks" ); // NOI18N
        DataFolder folder = DataFolder.findFolder( root );
        DataObject[] children = folder.getChildren();
        for( int i=0; i<children.length; i++ ) {
            if( children[i].getPrimaryFile().isFolder() ) {
                String headerText = children[i].getNodeDelegate().getDisplayName();
                JLabel lblTitle = new JLabel( headerText );
                lblTitle.setFont( HEADER_FONT );
                lblTitle.setForeground( DEFAULT_TEXT_COLOR );
                lblTitle.setHorizontalAlignment( JLabel.LEFT );
                lblTitle.setOpaque( false );
                lblTitle.setBorder( HEADER_TEXT_BORDER );
                panel.add( lblTitle, new GridBagConstraints( 0,row++,1,1,1.0,0.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets(row==1 ? UNDER_HEADER_MARGIN : SECTION_MARGIN,0,
                        UNDER_SECTION_MARGIN,2*TEXT_INSETS_RIGHT), 0, 0 ) );

                DataFolder subFolder = DataFolder.findFolder( children[i].getPrimaryFile() );
                DataObject[] subFolderChildren = subFolder.getChildren();
                for( int j=0; j<subFolderChildren.length; j++ ) {
                    row = addLink( panel, row, subFolderChildren[j] );
                }
                    
            } else {
                row = addLink( panel, row, children[i] );
            }
        }

        panel.add( new JLabel(), new GridBagConstraints(1, row++, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,0,15,0), 0, 0 ) );

        JScrollPane scroll = new RelativeSizeScrollPane( panel, 0.70f, 50 );
        scroll.getViewport().setOpaque( false );
        scroll.setOpaque( false );
        scroll.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scroll.setBorder( BorderFactory.createEmptyBorder(0,0,20,0) );
        return scroll;
    }

    private int addLink( JPanel panel, int row, DataObject dob ) {
        OpenCookie oc = (OpenCookie)dob.getCookie( InstanceCookie.class );
        if( null != oc ) {
            LinkAction la = new LinkAction( dob );
            LinkButton lb = new LinkButton( la, true );
            lb.setForeground( HEADER_TEXT_COLOR );
            panel.add( lb, new GridBagConstraints( 0,row++,1,1,0.0,0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(row==1? UNDER_HEADER_MARGIN : ROW_MARGIN,TEXT_INSETS_LEFT+3,0,2*TEXT_INSETS_RIGHT), 0, 0 ) );
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
            OpenCookie oc = (OpenCookie)dob.getCookie( OpenCookie.class );
            if( null != oc )
                oc.open();
        }
    }
}
