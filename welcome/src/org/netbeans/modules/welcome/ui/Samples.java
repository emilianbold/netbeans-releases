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
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BackgroundPanel;
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
class Samples extends BackgroundPanel implements Constants {

    /** Creates a new instance of RecentProjects */
    public Samples() {
        super( new GridBagLayout() );
        
        createLinks();
    }
    
    protected void addLink( JPanel panel, String category, String title ) {
        SampleProjectLink link = new SampleProjectLink( category, null, title );
        JPanel inner = new JPanel( new GridBagLayout() );
        inner.setOpaque(false);
        inner.add( link, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0 ) );
        inner.add( new JLabel(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0 ) );
        panel.add( inner );
    }

    protected void createLinks() {
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Project/Samples" ); // NOI18N
        DataFolder df = DataFolder.findFolder( root );
        DataObject[] children = df.getChildren();
        JPanel panel = new JPanel(new GridLayout( 0, 2, 15, 5 ));
        panel.setOpaque( false );
        for( int i=0; i<children.length; i++ ) {
            try {
                createLinkForCategory( panel, children[i] );
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        
        add( panel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0,0,0,0), 0, 0 ) ); // NOI18N
        
        add( new JLabel(), new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0,0,0,0), 0, 0 ) ); // NOI18N
    }

    protected void createLinkForCategory( JPanel panel, DataObject categoryDO ) throws DataObjectNotFoundException {
        if( categoryDO instanceof DataShadow ) {
            categoryDO = ((DataShadow)categoryDO).getOriginal();
        }

        String category = null;

        FileObject origFile = categoryDO.getPrimaryFile();
        if( !origFile.isFolder() )
            return;
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
}
