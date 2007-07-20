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

package org.netbeans.modules.tasklist.impl;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Default implementtion of TaskScanningScope. The scope is the currently edited file.
 * 
 * @author S. Aubrecht
 */
public class CurrentEditorScanningScope extends TaskScanningScope 
        implements PropertyChangeListener, Runnable {
    
    private FileObject currentFile = null;
    private Callback callback;
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup;
    
    /** Creates a new instance of CurrentEditorScope */
    public CurrentEditorScanningScope( String displayName, String description, Image icon ) {
        super( displayName, description, icon );
    }
    
    public static CurrentEditorScanningScope create() {
        return new CurrentEditorScanningScope(
                NbBundle.getBundle( CurrentEditorScanningScope.class ).getString( "LBL_CurrentEditorScope" ), //NOI18N)
                NbBundle.getBundle( CurrentEditorScanningScope.class ).getString( "HINT_CurrentEditorScope" ), //NOI18N
                Utilities.loadImage( "org/netbeans/modules/tasklist/ui/resources/cur_editor_scope.png" ) //NOI18N
                );
    }
    
    public Iterator<FileObject> iterator() {
        ArrayList<FileObject> list = new ArrayList<FileObject>( 1 );
        if( null != currentFile )
            list.add( currentFile );
        return list.iterator();
    }
    
    @Override
    public boolean isInScope( FileObject resource ) {
        if( null == resource )
            return false;
        return null != currentFile && currentFile.equals( resource );
    }
    
    public Lookup getLookup() {
        if( null == lookup ) {
            lookup = new AbstractLookup( lookupContent );
        }
        return lookup;
    }
    
    public void attach( Callback newCallback ) {
        if( null != newCallback && null == callback ) {
            WindowManager.getDefault().getRegistry().addPropertyChangeListener( this );
        } else if( null == newCallback && null != callback ) {
            WindowManager.getDefault().getRegistry().removePropertyChangeListener( this );
            if (null != currentFile) {
                lookupContent.remove(currentFile);
            }
            currentFile = null;
        }
        if( null != newCallback && newCallback != this.callback ) {
            this.callback = newCallback;
            if( SwingUtilities.isEventDispatchThread() ) {
                run();
            } else {
                SwingUtilities.invokeLater( this );
            }
        }
        this.callback = newCallback;
    }
    
    public void propertyChange( PropertyChangeEvent e ) {
        if( TopComponent.Registry.PROP_ACTIVATED_NODES.equals( e.getPropertyName() )
            || TopComponent.Registry.PROP_OPENED.equals( e.getPropertyName() )
            || TopComponent.Registry.PROP_ACTIVATED.equals( e.getPropertyName() ) ) {
            
            run();
        }
    }
    
    public void run() {
        FileObject newActiveFile = getCurrentFile();
        if( (null == currentFile && null != newActiveFile)
            || (null != currentFile && null == newActiveFile )
            || (null != currentFile && null != newActiveFile 
                && !currentFile.equals(newActiveFile)) ) {

            if( null != currentFile )
                lookupContent.remove( currentFile );
            if( null != newActiveFile )
                lookupContent.add( newActiveFile );
            currentFile = newActiveFile;
            //notify the TaskManager that user activated other file
            if( null != callback )
                callback.refresh();
        } else {
            currentFile = newActiveFile;
        }
    }
    
    private FileObject getCurrentFile() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        
        TopComponent activeTc = registry.getActivated();
        FileObject newFile = getFileFromTopComponent( activeTc );
        
        ArrayList<FileObject> availableFiles = new ArrayList<FileObject>(3);
        if( null == newFile ) {
            Collection<TopComponent> openedTcs = new ArrayList<TopComponent>( registry.getOpened());
            for( Iterator i=openedTcs.iterator(); i.hasNext(); ) {
                TopComponent tc = (TopComponent)i.next();
                
                FileObject file = getFileFromTopComponent( tc );
                if( null != file ) {
                    availableFiles.add( file );
                }
            }
            if( null != currentFile && (availableFiles.contains( currentFile ) ) )
                newFile = currentFile;
            else if( availableFiles.size() > 0 )
                newFile = availableFiles.get( 0 );
        }
        return newFile;
    }
    
    private FileObject getFileFromTopComponent( final TopComponent tc ) {
        if( null == tc || !tc.isShowing() )
            return null;
        if( WindowManager.getDefault().isOpenedEditorTopComponent( tc ) ) {
            DataObject dob = tc.getLookup().lookup( DataObject.class );
            if( null != dob ) {
                return dob.getPrimaryFile();
            }
        }
        return null;
    }
}
