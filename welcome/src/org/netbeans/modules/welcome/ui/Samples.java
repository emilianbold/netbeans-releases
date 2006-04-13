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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import org.netbeans.modules.welcome.content.ContentPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.SampleProjectLink;
import org.netbeans.modules.welcome.content.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.windows.TopComponent;

/**
 *
 * @author S. Aubrecht
 */
public class Samples extends ContentPanel implements Constants, ActionListener {

    protected int row = 0;

    /** Creates a new instance of RecentProjects */
    public Samples() {
        super( BundleSupport.getLabel( "Samples" ) ); // NOI18N
        setOpaque( true );
        setBackground( DEFAULT_BACKGROUND_COLOR );
        
        setContent( buildContent() );

        JButton button = new JButton( BundleSupport.getLabel( "NewProject" ) ); // NOI18N
        button.setFont( BUTTON_FONT );
        button.setForeground( BUTTON_TEXT_COLOR );
        button.addActionListener( this );

        setBottomContent( button );
    }
    
    private JComponent buildContent() {
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );

        createLinks( panel );

        panel.add( new JLabel(), new GridBagConstraints(1, row++, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0 ) );

        JScrollPane scroll = new RelativeSizeScrollPane( panel, 0.45f, 30 );
        scroll.getViewport().setOpaque( false );
        scroll.setOpaque( false );
        scroll.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scroll.setBorder( BorderFactory.createEmptyBorder() );
        return scroll;
    }

    protected void addLink( JPanel panel, String category, String title ) {
        SampleProjectLink link = new SampleProjectLink( category, null, title );
        panel.add( link, new GridBagConstraints( 0, row++, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(row==1 ? UNDER_HEADER_MARGIN : ROW_MARGIN,TEXT_INSETS_LEFT-3,
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
        category = origFile.getPath();
        category = category.replaceFirst( "Templates/Project/", "" ); // NOI18N // NOI18N

        String label = categoryDO.getNodeDelegate().getDisplayName();

        addLink( panel, category, label );
    }
    
    public void actionPerformed(ActionEvent e) {
        Action a = Utils.findAction( "Actions/Project/org-netbeans-modules-project-ui-NewProject.instance" ); // NOI18N
        if( null != a ) {
            a.actionPerformed( e );
        }
    }
}
