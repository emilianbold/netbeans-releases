/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.html;

import java.io.*;
import java.util.*;
import org.netbeans.editor.ext.html.dtd.ReaderProvider;
import org.netbeans.editor.ext.html.dtd.Registry;
import org.openide.filesystems.*;

public class NbReaderProvider implements ReaderProvider {

    private static final String DTD_FOLDER = "DTDs"; // NOI18 // NOI18N
    private static final String CATALOG_FILE_NAME = "catalog"; // NOI18N

    Map mapping = null;
    boolean valid = false;
    FileObject dtdSetFolder;

    public static void setupReaders() {
        FileObject rootFolder = Repository.getDefault().getDefaultFileSystem().getRoot();
        rootFolder.addFileChangeListener( new RootFolderListener() );
        
        FileObject dtdFolder = rootFolder.getFileObject( DTD_FOLDER );
        if( dtdFolder != null) {
            dtdFolder.addFileChangeListener( new DTDFolderListener() );
            processSubfolders( dtdFolder );
        }
    }
    
    

    public NbReaderProvider( FileObject folder ) {
        dtdSetFolder = folder;
        revalidate(true);
        folder.addFileChangeListener( new ProviderFolderListener() );
    }

    public Collection getIdentifiers() {
        return valid ? mapping.keySet() : new HashSet(0);
    }

    public Reader getReaderForIdentifier( String identifier, String filename) {
        if( !valid ) return null;
        
        String fileName = (String)mapping.get( identifier );
        if( fileName == null ) return null;
        if( dtdSetFolder == null ) return null;
        
        FileObject file = dtdSetFolder.getFileObject( fileName );
        if( fileName == null ) return null;
        
        try {
            return new InputStreamReader( file.getInputStream() );
        } catch( FileNotFoundException exc ) {
            return null;
        }
    }

    private void invalidate() {
        if( valid ) {
            valid = false;
            Registry.invalidateReaderProvider( this );
        }
    }

    private boolean revalidate( boolean flag ) {
        if( mapping == null || flag ) {
            FileObject catalog = dtdSetFolder.getFileObject( CATALOG_FILE_NAME );

            if( catalog == null ) {
                mapping = null;
            } else try {
                mapping = parseCatalog( new InputStreamReader( catalog.getInputStream() ) );
            } catch( FileNotFoundException exc ) {
                mapping = null;
            }
            
            if( mapping == null ) {
                invalidate();
                return false;
            }
        }
        
        // check the availabilily
        Collection files = mapping.values();
        boolean all = true;
        for( Iterator it = files.iterator(); it.hasNext(); ) {
            String fname = (String)it.next();
            if( dtdSetFolder.getFileObject( fname ) == null ) {
                all = false;
                break;
            }
        }
        if( !all ) invalidate();
        valid = all;
        return valid;
    }

    private Map parseCatalog( Reader catalogReader ) {
        HashMap hashmap = new HashMap();
        LineNumberReader reader = new LineNumberReader( catalogReader );
        
        for( ;; ) {
            String line;

            try {
                line = reader.readLine();
            } catch( IOException exc ) {
                return null;
            }
            
            if( line == null ) break;
            
            StringTokenizer st = new StringTokenizer( line );
            if( st.hasMoreTokens() && "PUBLIC".equals( st.nextToken() ) && st.hasMoreTokens() ) { // NOI18N
                st.nextToken( "\"" ); // NOI18N
		if( !st.hasMoreTokens() ) continue;
                String id = st.nextToken( "\"" ); // NOI18N

		if( !st.hasMoreTokens() ) continue;
                st.nextToken( " \t\n\r\f" ); // NOI18N
		
		if( !st.hasMoreTokens() ) continue;
                String file = st.nextToken();
                hashmap.put( id, file );
            }
        }
        return hashmap;
    }

    private static void processSubfolders( FileObject dtdFolder ) {
        FileObject folder;
        for( Enumeration en = dtdFolder.getFolders(false); en.hasMoreElements(); ) {
            folder = (FileObject)en.nextElement();
            addFolder( folder );
        }
    }

    static Map folder2provider = new HashMap();

    
    private static void removeSubfolders() {
        Iterator it = folder2provider.entrySet().iterator();
        folder2provider = new HashMap();
        while( it.hasNext() ) {
            Map.Entry entry = (Map.Entry)it.next();
            ReaderProvider prov = (ReaderProvider)entry.getValue();
            Registry.unregisterReaderProvider( prov );
        }
    }

    private static void addFolder( FileObject folder ) {
        NbReaderProvider prov = new NbReaderProvider( folder );
        folder2provider.put( folder.getNameExt(), prov );
        Registry.registerReaderProvider( prov );
    }

    private static void removeFolder( FileObject folder ) {
        NbReaderProvider prov = (NbReaderProvider)folder2provider.remove( folder.getNameExt() );
        if( prov != null ) Registry.unregisterReaderProvider( prov );
    }


    private class ProviderFolderListener extends FileChangeAdapter {

        public void fileDataCreated( FileEvent fev ) {
            FileObject file = fev.getFile();
            if( !valid ) {
                boolean flag = true;
                revalidate( flag );
            }
        }

        public void fileDeleted( FileEvent fev ) {
            if( valid )
                revalidate( true );
        }

        public void fileRenamed( FileRenameEvent fev ) {
            invalidate();
            revalidate(true);
        }

        public void fileChanged( FileEvent fev ) {
            invalidate();
            revalidate(true);
        }
    }


    private static class RootFolderListener extends FileChangeAdapter {

        public void fileFolderCreated( FileEvent fev ) {
            FileObject file = fev.getFile();
            if( DTD_FOLDER.equals( file.getNameExt() ) ) {
                file.addFileChangeListener( new DTDFolderListener() );
                NbReaderProvider.processSubfolders( file );
            }
        }

        public void fileDeleted( FileEvent fev ) {
            FileObject file = fev.getFile();
            if( DTD_FOLDER.equals( file.getNameExt() ) )
                NbReaderProvider.removeSubfolders();
        }
    }

    
    private static class DTDFolderListener extends FileChangeAdapter {

        public void fileFolderCreated( FileEvent fev ) {
            FileObject file = fev.getFile();
            NbReaderProvider.addFolder( file );
        }
        
        public void fileDeleted( FileEvent fev ) {
            FileObject file = fev.getFile();
            NbReaderProvider.removeFolder( file );
        }
    }
    
}
