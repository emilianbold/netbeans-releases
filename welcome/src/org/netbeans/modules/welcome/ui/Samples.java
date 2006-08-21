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
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import org.netbeans.modules.welcome.content.ContentPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.LinkButton;
import org.netbeans.modules.welcome.content.SampleProjectLink;
import org.netbeans.modules.welcome.content.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;

/**
 *
 * @author S. Aubrecht
 */
public class Samples extends ContentPanel implements Constants {

    protected int row = 0;

    /** Creates a new instance of RecentProjects */
    public Samples() {
        super( BundleSupport.getLabel( "Samples" ) ); // NOI18N
        setOpaque( true );
        setBackground( DEFAULT_BACKGROUND_COLOR );
        
        setContent( buildContent() );

        NewProjectButton button = new NewProjectButton();

        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );

        panel.add( button, new GridBagConstraints(0,1,1,1,0.0,0.0,
                GridBagConstraints.SOUTHWEST,GridBagConstraints.HORIZONTAL,
                new Insets(5,5,0,5),0,0) );
        panel.add( new JLabel(), new GridBagConstraints(1,1,1,1,1.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
                new Insets(0,0,0,0),0,0) );

        setBottomContent( panel );
    }
    
    private JComponent buildContent() {
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );

        createLinks( panel );

        panel.add( new JLabel(), new GridBagConstraints(0, row++, 1, 1, 0.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0 ) );

        JScrollPane scroll = new RelativeSizeScrollPane( panel, 0.30f, 30 );
        scroll.getViewport().setOpaque( false );
        scroll.setOpaque( false );
        scroll.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scroll.setBorder( BorderFactory.createEmptyBorder() );
        return scroll;
    }

    protected void addLink( JPanel panel, String category, String title ) {
        SampleProjectLink link = new SampleProjectLink( category, null, title );
        link.setForeground( HEADER_TEXT_COLOR );
        panel.add( link, new GridBagConstraints( 0, row++, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(row==1 ? UNDER_HEADER_MARGIN : ROW_MARGIN,TEXT_INSETS_LEFT+3,
                0,TEXT_INSETS_RIGHT/2+UIManager.getInt("ScrollBar.width")), 0, 0 ) ); // NOI18N
    }

    protected void createLinks( JPanel panel ) {
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Project/Samples" ); // NOI18N
        DataFolder df = DataFolder.findFolder( root );
        DataObject[] children = df.getChildren();
        for( int i=0; i<children.length; i++ ) {
            try {
                createLinkForCategory( panel, children[i] );
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void createLinkForCategory( JPanel panel, DataObject categoryDO ) throws DataObjectNotFoundException {
        if( categoryDO instanceof DataShadow ) {
            categoryDO = ((DataShadow)categoryDO).getOriginal();
        }

        String category = null;

        FileObject origFile = categoryDO.getPrimaryFile();
        DataFolder df = DataFolder.findFolder( origFile );
        if( null != df ) {
            DataObject[] categoryChildren = df.getChildren();
            boolean hasSubFoldersOnly = true;
            for( int i=0; i<categoryChildren.length; i++ ) {
                if( !categoryChildren[i].getPrimaryFile().isFolder() ) {
                    hasSubFoldersOnly = false;
                    break;
                }
            }
            if( hasSubFoldersOnly && categoryChildren.length > 0 ) {
                origFile = categoryChildren[0].getPrimaryFile();
            }
        }
        category = origFile.getPath();
        category = category.replaceFirst( "Templates/Project/", "" ); // NOI18N // NOI18N


        String label = categoryDO.getNodeDelegate().getDisplayName();

        addLink( panel, category, label );
    }

    private static class NewProjectButton extends LinkButton {
        public NewProjectButton() {
            super( BundleSupport.getLabel( "NewProject" ), false ); // NOI18N
            setFont( HEADER_FONT );
            setForeground( HEADER_TEXT_COLOR );
        }

        public void actionPerformed(ActionEvent e) {
            Action a = Utils.findAction( "Actions/Project/org-netbeans-modules-project-ui-NewProject.instance" ); // NOI18N
            if( null != a ) {
                a.actionPerformed( e );
            }
        }
    }
}
