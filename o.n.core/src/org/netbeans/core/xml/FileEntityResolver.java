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

package org.netbeans.core.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;

import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.Lookup;
import org.openide.xml.EntityCatalog;


/** 
 * Entity resolver which loads entities (typically DTDs) from fixed
 * locations in the system file system, according to public ID.
 * <p>
 * It expects that PUBLIC has at maximum three "//" parts 
 * (standard // vendor // entity name // language). It is basically
 * converted to <tt>"/xml/entities/{vendor}/{entity_name}"</tt> resource name.
 * <p>
 * It also attaches <tt>Environment</tt> according to registrations
 * at <tt>/xml/lookups/</tt> area. There can be registered:
 * <tt>Environment.Provider</tt> or deprecated <tt>XMLDataObject.Processor</tt>
 * and <tt>XMLDataObject.Info</tt> instances.
 * <p>
 * All above are core implementation features.
 *
 * @author  Jaroslav Tulach
 */
public class FileEntityResolver extends EntityCatalog implements Environment.Provider {
    private static final String ENTITY_PREFIX = "/xml/entities"; // NOI18N
    private static final String LOOKUP_PREFIX = "/xml/lookups"; // NOI18N
    
    /** Constructor
     */
    public FileEntityResolver() {
    }
    
    /** Tries to find the entity on system file system.
     */
    public InputSource resolveEntity(String publicID, String systemID) throws IOException, SAXException {
        if (publicID == null) {
            return null;
        }


        String id = convertPublicId (publicID);
        
        StringBuffer sb = new StringBuffer (200);
        sb.append (ENTITY_PREFIX);
        sb.append (id);
        
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().findResource (sb.toString ());
        if (fo != null) {
            return new InputSource (fo.getInputStream ());
        } else {
            return null;
        }
    }
    
    /** A method that tries to find the correct lookup for given XMLDataObject.
     * @return the lookup
     */
    public Lookup getEnvironment(DataObject obj) {
        if (obj instanceof XMLDataObject) {
            XMLDataObject xml = (XMLDataObject)obj;
            
            String id;
            try {
                id = xml.getDocument ().getDoctype ().getPublicId ();
            } catch (IOException ex) {
                TopManager.getDefault ().getErrorManager().notify (ex);
                return null;
            } catch (org.xml.sax.SAXException ex) {
                TopManager.getDefault ().getErrorManager().notify (ex);
                return null;
            }

            if (id == null) {
                return null;
            }
            
            id = convertPublicId (id);
            
            StringBuffer sb = new StringBuffer (200);
            sb.append (LOOKUP_PREFIX);
            sb.append (id);
            int len = sb.length ();
            // at least for now
            sb.append (".instance"); // NOI18N 
            FileObject fo = Repository.getDefault ().getDefaultFileSystem ().findResource (sb.toString ());
            if (fo == null) {
                // try to find a file with xml extension
                sb.setLength (len);
                sb.append (".xml"); // NOI18N
                fo = Repository.getDefault ().getDefaultFileSystem ().findResource (sb.toString ());
            }
            
            if (fo != null) {
                try {
                    DataObject dataobj = DataObject.find (fo);
                    InstanceCookie cookie = (InstanceCookie)dataobj.getCookie (InstanceCookie.class);
                    if (cookie != null) {
                        Object inst = cookie.instanceCreate ();
                        if (inst instanceof Environment.Provider) {
                            return ((Environment.Provider)inst).getEnvironment (obj);
                        }
                        
                        if (inst instanceof XMLDataObject.Processor) {
                            // convert provider
                            XMLDataObject.Info info = new XMLDataObject.Info ();
                            info.addProcessorClass (inst.getClass ());
                            inst = info;
                        }
                        
                        if (inst instanceof XMLDataObject.Info) {
                            return createInfoLookup (xml, ((XMLDataObject.Info)inst));
                        }
                        
                    }
                } catch (IOException ex) {
                    TopManager.getDefault ().getErrorManager ().notify (ex);
                } catch (ClassNotFoundException ex) {
                    TopManager.getDefault ().getErrorManager ().notify (ex);
                }
            }
        }
        return null;
    }
    
    /** Ugly hack to get to openide hidden functionality.
     */
    private static java.lang.reflect.Method method;
    private static Lookup createInfoLookup (XMLDataObject obj, XMLDataObject.Info info) {
        // well, it is a hack, but just for default compatibility
        if (method == null) {
            try {
                method = XMLDataObject.class.getDeclaredMethod ("createInfoLookup", new Class[] { // NOI18N
                    XMLDataObject.class,
                    XMLDataObject.Info.class
                });
                method.setAccessible (true);
            } catch (Exception ex) {
                TopManager.getDefault ().getErrorManager ().notify (ex);
                return null;
            }
        }
        try {
            return (Lookup)method.invoke (null, new Object[] { obj, info });
        } catch (Exception ex) {
            TopManager.getDefault ().getErrorManager ().notify (ex);
            return null;
        }
    }

    /** Converts the publicID into filesystem friendly name.
     * <p>
     * It expects that PUBLIC has at maximum three "//" parts 
     * (standard // vendor // entity name // language). It is basically
     * converted to "vendor/entity_name" resource name.
     *
     * @see EntityCatalog
     */
    private static String convertPublicId (String publicID) {
        char[] arr = publicID.toCharArray ();


        int numberofslashes = 0;
        int state = 0;
        int write = 0;
        OUT: for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];

            switch (state) {
            case 0:
                // initial state 
                if (ch == '+' || ch == '-') {
                    // do not write that char
                    continue;
                }
                // switch to regular state
                state = 1;
                // fallthru
            case 1:
                // regular state expecting any character
                if (ch == '/') {
                    state = 2;
                    if (++numberofslashes == 3) {
                        // last part of the ID, exit
                        break OUT;
                    }
                    arr[write++] = '/';
                    continue;
                }
                break;
            case 2:
                // previous character was /
                if (ch == '/') {
                    // ignore second / and write nothing
                    continue;
                }
                state = 1;
                break;
            }

            // write the char into the array
            if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9') {
                arr[write++] = ch;
            } else {
                arr[write++] = '_';
            }
        }

        return new String (arr, 0, write);
    }
    
}
