/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.example;

import java.io.*;
import java.util.*;
import org.netbeans.editor.ext.html.dtd.ReaderProvider;
import org.netbeans.editor.ext.html.dtd.Registry;

public class SAReaderProvider implements ReaderProvider {

    private static final String DTD_FOLDER = "DTDs"; // NOI18
    private static final String CATALOG_FILE_NAME = "catalog"; // NOI18N

    Map mapping = null;
    boolean valid = false;
    File dtdSetFolder;

    public static void setupReaders() {
        //We are not able to track changes!
        File rootFolder = Editor.getDistributionDirectory();
        
	//We are not able to track changes!
        File dtdFolder = findFile( rootFolder, DTD_FOLDER );
        if( dtdFolder != null) {
            processSubfolders( dtdFolder );
        }
    }
    
    

    public SAReaderProvider( File folder ) {
        dtdSetFolder = folder;
        revalidate(true);
	//We will not be able to catch changes in folder!
    }

    public Collection getIdentifiers() {
        return valid ? mapping.keySet() : new HashSet(0);
    }
    
    private static File findFile(File folder, String fileName) {
       if (fileName == null || folder == null) {
          return null;
       }
       
       File[] files = folder.listFiles();
       
       for (int cntr = 0; cntr < files.length; cntr++) {
          if (fileName.equals(files[cntr].getName())) {
	     return files[cntr];
	  }
       }
       return null;
    }

    public Reader getReaderForIdentifier( String identifier, String filename) {
        if( !valid ) return null;
        
        String fileName = (String)mapping.get( identifier );
        if( fileName == null ) return null;
        if( dtdSetFolder == null ) return null;
        
        File file = findFile( dtdSetFolder, fileName );
        if( file == null ) return null;
        
        try {
            return new InputStreamReader( new FileInputStream (file ) );
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
            File catalog = findFile( dtdSetFolder, CATALOG_FILE_NAME );

            if( catalog == null ) {
                mapping = null;
            } else try {
                mapping = parseCatalog( new InputStreamReader( new FileInputStream( catalog ) ) );
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
            if( findFile( dtdSetFolder, fname ) == null ) {
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
            if( st.hasMoreTokens() && "PUBLIC".equals( st.nextToken() ) && st.hasMoreTokens() ) {
                st.nextToken( "\"" );
		if( !st.hasMoreTokens() ) continue;
                String id = st.nextToken( "\"" );

		if( !st.hasMoreTokens() ) continue;
                st.nextToken( " \t\n\r\f" );
		
		if( !st.hasMoreTokens() ) continue;
                String file = st.nextToken();
                hashmap.put( id, file );
            }
        }
        return hashmap;
    }

    private static void processSubfolders( File dtdFolder ) {
        File[] files = dtdFolder.listFiles();
	
        for (int cntr = 0; cntr < files.length; cntr++) {
	    File file = files[cntr];
	    
	    if (file != null && file.isDirectory()) {
               addFolder( file );
	    }
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

    private static void addFolder( File folder ) {
        SAReaderProvider prov = new SAReaderProvider( folder );
        folder2provider.put( folder.getName(), prov );
        Registry.registerReaderProvider( prov );
    }

    private static void removeFolder( File folder ) {
        SAReaderProvider prov = (SAReaderProvider)folder2provider.remove( folder.getName() );
        if( prov != null ) Registry.unregisterReaderProvider( prov );
    }

}

