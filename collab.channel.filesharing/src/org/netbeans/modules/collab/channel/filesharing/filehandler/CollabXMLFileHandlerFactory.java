/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import org.openide.util.*;

import java.util.*;


/**
 * XML FileHandler Factory
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabXMLFileHandlerFactory extends Object implements CollabFileHandlerFactory {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* FileHandlerFactory ID */
    public static final String XML_FILEHANDLER_FACTORY_ID = "xmlfilehandler"; // NOI18N

    /* mime-type of the instance created by this factory */
    public static final String XML_MIME_TYPE = "text/xml"; // NOI18N

    /* mapping of file extensions to content-types */
    private java.util.Dictionary map = new java.util.Hashtable();

    /**
     * constructor
     *
     */
    public CollabXMLFileHandlerFactory() {
        super();
        setMIMEType("xml", "text/xml"); // NOI18N
        setMIMEType("xsl", "text/xml"); // NOI18N
        setMIMEType("xsd", "text/xml"); // NOI18N

        //setMIMEType("form", "text/xml"); // NOI18N		
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods
    ////////////////////////////////////////////////////////////////////////////          

    /**
     * getter for ID
     *
     * @return        ID
     */
    public String getID() {
        return XML_FILEHANDLER_FACTORY_ID; // NOI18N
    }

    /**
     * getter for displayName
     *
     * @return        displayName
     */
    public String getDisplayName() {
        return NbBundle.getMessage(CollabXMLFileHandlerFactory.class, "LBL_CollabXMLFileHandlerFactory_DisplayName"); // NOI18N
    }

    /**
     * create FileHandler instance
     *
     * @return        CollabFileHandler
     */
    public CollabFileHandler createCollabFileHandler() {
        return new CollabXMLFileHandler();
    }

    /**
     *
     * @return
     */
    public static CollabXMLFileHandlerFactory getDefault() {
        return getDefault(true);
    }

    /**
     *
     * @return
     */
    public static CollabXMLFileHandlerFactory getDefaultNoLookup() {
        return getDefault(false);
    }

    /**
     *
     * @return FileHandler Factory
     */
    private static CollabXMLFileHandlerFactory getDefault(boolean doIDELookup) {
        if (doIDELookup) {
            return (CollabXMLFileHandlerFactory) Lookup.getDefault().lookup(CollabXMLFileHandlerFactory.class);
        } else {
            return new CollabXMLFileHandlerFactory();
        }
    }

    /**
     * test if the factory object support the given mimeType or fileExt
     *
     * @param mimeType
     * @param fileExt
     * @return
     */
    public boolean canHandleMIMEType(String mimeType, String fileExt) {
        return canHandleMIMEType(mimeType) || canHandleFileExt(fileExt);
    }

    /**
     * test if the factory object support the given mimeType
     *
     * @param mimeType
     * @return
     */
    public boolean canHandleMIMEType(String mimeType) {
        if (mimeType == null) {
            return false;
        }

        Enumeration values = map.elements();

        while (values.hasMoreElements()) {
            String supportedMimeType = (String) values.nextElement();

            if (supportedMimeType.equals(mimeType)) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     *
     */
    private boolean canHandleFileExt(String fileExt) {
        if ((fileExt == null) || fileExt.equals("")) {
            return false;
        }

        String mimeType = getMIMEType(fileExt);

        if (mimeType == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * getter for contentType
     *
     * @return        contentType
     */
    public String getContentType() {
        return XML_MIME_TYPE;
    }

    /** Obtain MIME type for a well-known extension.
    * If there is a case-sensitive match, that is used, else will fall back
    * to a case-insensitive match.
    * @param ext the extension: <code>"jar"</code>, <code>"zip"</code>, etc.
    * @return the MIME type for the extension, or <code>null</code> if the extension is unrecognized
    * @deprecated in favour of {@link #getMIMEType(FileObject) getMIMEType(FileObject)} as MIME cannot
    * be generaly detected by a file object extension.
    */
    public String getMIMEType(String ext) {
        String s = (String) map.get(ext);

        if (s != null) {
            return s;
        } else {
            return (String) map.get(ext.toLowerCase());
        }
    }

    /**
     * Register MIME type for a new extension.
     * Note that you may register a case-sensitive extension if that is
     * relevant (for example <samp>*.C</samp> for C++) but if you register
     * a lowercase extension it will by default apply to uppercase extensions
     * too (for use on Windows or generally for situations where filenames
     * become accidentally upcased).
     * @see #getMIMEType
     * @deprecated You should instead use the more general {@link MIMEResolver} system.
     * @param ext the file extension (should be lowercase unless you specifically care about case)
     * @param mimeType the new MIME type
     */
    public void setMIMEType(String ext, String mimeType) {
        synchronized (map) {
            String old = (String) map.get(ext);

            if (old == null) {
                map.put(ext, mimeType);
            } else {
                if (!old.equals(mimeType)) {
                    throw new IllegalArgumentException(
                        "Cannot overwrite existing MIME type mapping for " + //NOI18N
                        "extension `" + ext + "' with " + mimeType + //NOI18N
                        " (was " + old + ")"
                    ); // NOI18N
                }
            }
        }
    }
}
