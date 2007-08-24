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
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.SampleProjectLink;
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
class Samples extends JPanel implements Constants {

    protected int row = 0;
    protected int col = 0;
    protected int maxRow = 0;

    /** Creates a new instance of RecentProjects */
    public Samples() {
        super( new GridBagLayout() );
        setOpaque( false );
        
        buildContent();
    }
    
    private void buildContent() {
        createLinks();

        add( new JLabel(), new GridBagConstraints(col++, maxRow == 0 ? row++ : maxRow+1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0 ) );
    }

    protected void addLink( String category, String title ) {
        SampleProjectLink link = new SampleProjectLink( category, null, title );
        add( link, new GridBagConstraints( col, row++, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0,col == 0 ? 0 : 20, 3, 0), 0, 0 ) ); // NOI18N
    }

    protected void createLinks() {
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Project/Samples" ); // NOI18N
        DataFolder df = DataFolder.findFolder( root );
        DataObject[] children = df.getChildren();
        for( int i=0; i<children.length; i++ ) {
            try {
                createLinkForCategory( children[i] );
                if( children.length >= 8 && i+1 == children.length/2+children.length%2 ) {
                    col = 1;
                    maxRow = row;
                    row = 0;
                }

            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void createLinkForCategory( DataObject categoryDO ) throws DataObjectNotFoundException {
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

        addLink( category, label );
    }
}
