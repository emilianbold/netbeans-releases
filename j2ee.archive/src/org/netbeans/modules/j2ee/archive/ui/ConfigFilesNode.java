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

package org.netbeans.modules.j2ee.archive.ui;

import java.awt.Image;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ludo
 * Node for all the config files of all the sub-modules of this bianry archive
 */
public class ConfigFilesNode  extends FilterNode {
    private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
    
    //private static Image CONFIGURATION_FILES_BADGE = Utilities.loadImage( "org/netbeans/modules/j2ee/earproject/ui/resources/archive.gif", true ); // NOI18N
    
    public ConfigFilesNode(DataFolder folder) {
        super(folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER));
    }
    
    public Image getIcon( int type ) {
        return computeIcon( false, type );
    }
    
    public Image getOpenedIcon( int type ) {
        return computeIcon( true, type );
    }
    
    private Image computeIcon( boolean opened, int type ) {
        Node folderNode = getOriginal();
        Image image = opened ? folderNode.getOpenedIcon( type ) : folderNode.getIcon( type );
        return image;
    }
    
    public boolean canCopy() {
        return false;
    }
    
    public boolean canCut() {
        return false;
    }
    
    public boolean canRename() {
        return false;
    }
    
    public boolean canDestroy() {
        return false;
    }
    
    public Action[] getActions( boolean context ) {
        return new Action[] {
            //CommonProjectActions.newFileAction(),
            //SystemAction.get(org.openide.actions.NewTemplateAction.class),
            SystemAction.get(org.openide.actions.FindAction.class),
        };
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(RootNode.class, "LBL_Node_DocBase"); //NOI18N
    }
    
    protected static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
        
        public boolean acceptDataObject(DataObject obj) {
            boolean retVal = false;
            FileObject fo = obj.getPrimaryFile();
            if (!"classes".equals(fo.getName()) && !"lib".equals(fo.getName())) {       // NOI18N
                retVal = VisibilityQuery.getDefault().isVisible( fo );
            }
            return retVal;
        }
        
        public void stateChanged( ChangeEvent e) {
            Object[] listeners = ell.getListenerList();
            ChangeEvent event = null;
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i] == ChangeListener.class) {
                    if ( event == null) {
                        event = new ChangeEvent( this );
                    }
                    ((ChangeListener)listeners[i+1]).stateChanged( event );
                }
            }
        }
        
        public void addChangeListener( ChangeListener listener ) {
            ell.add( ChangeListener.class, listener );
        }
        
        public void removeChangeListener( ChangeListener listener ) {
            ell.remove( ChangeListener.class, listener );
        }
        
    }

 
}
