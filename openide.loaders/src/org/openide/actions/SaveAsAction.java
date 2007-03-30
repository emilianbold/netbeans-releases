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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.SaveAsCapable;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to save document under a different file name and/or extension.
 * The action is enabled for editor windows only.
 * 
 * @since 6.3
 * @author S. Aubrecht
 */
final public class SaveAsAction extends AbstractAction implements ContextAwareAction, LookupListener, PropertyChangeListener {

    private Lookup context;
    private Lookup.Result<SaveAsCapable> lkpInfo;
    
    public SaveAsAction() {
        this( Utilities.actionsGlobalContext() );
        TopComponent.getRegistry().addPropertyChangeListener( this );
    }
    
    private SaveAsAction( Lookup context ) {
        super( NbBundle.getMessage(DataObject.class, "CTL_SaveAsAction") ); //NOI18N
        this.context = context;
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    void init() {
        assert SwingUtilities.isEventDispatchThread() 
               : "this shall be called just from AWT thread";

        if (lkpInfo != null) {
            return;
        }

        //The thing we want to listen for the presence or absence of
        //on the global selection
        Lookup.Template<SaveAsCapable> tpl = new Lookup.Template<SaveAsCapable>(SaveAsCapable.class);
        lkpInfo = context.lookup (tpl);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }

    public boolean isEnabled() {
        init();
        return super.isEnabled();
    }

    public void actionPerformed(ActionEvent e) {
        init();
        Collection<? extends SaveAsCapable> inst = lkpInfo.allInstances();
        if( inst.size() > 0 ) {
            SaveAsCapable saveAs = inst.iterator().next();
            File newFile = getNewFileName();
            if( null != newFile ) {
                FileObject newFolder = FileUtil.toFileObject( newFile.getParentFile() );
                FileObject newFileObj = FileUtil.toFileObject( newFile );
                try {
                    if( null == newFileObj ) {
                        String newFilename = getFileName( newFile );
                        String newExtension = FileUtil.getExtension( newFile.getName() );
                        newFileObj = newFolder.createData( newFilename, newExtension );
                    }
                    saveAs.saveAs( newFileObj );
                } catch( IOException ioE ) {
                    Logger.getLogger( getClass().getName() ).log( 
                            Level.WARNING, NbBundle.getMessage( DataObject.class, "MSG_SaveAsFailed" ), ioE ); //NOI18N
                }
            }
        }
    }
    
    /**
     * Get the name part without the extension of the given file
     * @param file
     * @return name part of the given file
     */
    private static String getFileName( File file ) {
        String fileName = file.getName();
        int index = fileName.lastIndexOf( '.' );
        if( index > 0 )
            return fileName.substring( 0, index );
        return fileName;
    }


    public void resultChanged(LookupEvent ev) {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        
        setEnabled (null != lkpInfo && lkpInfo.allItems().size() != 0 && null != tc && WindowManager.getDefault().isEditorTopComponent( tc ) );
    }
    
    /**
     * Show file 'save as' dialog window to ask user for a new file name.
     * @return File selected by the user or null if no file was selected.
     */
    private File getNewFileName() {
        File newFile = null;
        FileObject currentFileObject = null;//((Env)env).getFile();
        if( null != currentFileObject )
            newFile = FileUtil.toFile( currentFileObject );

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle( NbBundle.getMessage(DataObject.class, "LBL_SaveAsTitle" ) ); //NOI18N
        chooser.setMultiSelectionEnabled( false );
        if( null != newFile )
            chooser.setSelectedFile( newFile );
        File origFile = newFile;
        if( JFileChooser.APPROVE_OPTION != chooser.showSaveDialog( WindowManager.getDefault().getMainWindow() ) ) {
            return null;
        }
        newFile = chooser.getSelectedFile();
        if( null == newFile || newFile.equals( origFile ) )
            return null;

        //create target folder if necessary    
        File targetFolder = newFile.getParentFile();
        if( !targetFolder.exists() )
            targetFolder.mkdirs();
        FileObject targetFileObjectFolder = FileUtil.toFileObject( targetFolder );
        if( null == targetFileObjectFolder ) {
            NotifyDescriptor error = new NotifyDescriptor( 
                    NbBundle.getMessage(DataObject.class, "MSG_CannotCreateTargetFolder"), //NOI18N
                    NbBundle.getMessage(DataObject.class, "LBL_SaveAsTitle"), //NOI18N
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[] {NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION );
            DialogDisplayer.getDefault().notify( error );
            return null;
        }
        return newFile;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new SaveAsAction( actionContext );
    }

    public void propertyChange(PropertyChangeEvent arg0) {
        resultChanged( null );
    }
}

