/**************************************************************************
*
*                          Sun Public License Notice
*
* The contents of this file are subject to the Sun Public License Version
* 1.0 (the "License"). You may not use this file except in compliance with
* the License. A copy of the License is available at http://www.sun.com/
*
* The Original Code is the HTTP Javadoc Filesystem.
* The Initial Developer of the Original Code is Jeffrey A. Keyser.
* Portions created by Jeffrey A. Keyser are Copyright (C) 2000-2002.
* All Rights Reserved.
*
* Contributor(s): Jeffrey A. Keyser.
*
**************************************************************************/


package org.netbeans.modules.javadoc.httpfs;

import java.net.URL;
import org.openide.filesystems.*;


/**
 * <p>Represents an individual file found on the file sysetm.</p>
 */
public class HTTPURLMapper
    extends URLMapper {

    /**
     * Creates a new instance of HTTPURLMapper.
     */
    public HTTPURLMapper(
    ) {

        // Do nothing

    }


    /**
     * Get an array of FileObjects for this url.
     *
     * @param fileURL URL for wanted FileObjects.
     *
     * @return Always null.
     */
    public FileObject[] getFileObjects(
        URL fileURL
    ) {

        return null;

    }


    /**
     * For HTTPFileObjects, returns the URL of the underlying file on requests
     * for external or network URLs.
     *
     * @return URL for a file represented by the HTTPFileObject, or null.
     */
    public URL getURL(
        FileObject  file,
        int         urlType
    ) {

        // URL to return for this file
        URL returnURL;


        if( file instanceof HTTPFileObject && ( urlType == EXTERNAL || urlType == NETWORK ) ) {

            returnURL = ((HTTPFileObject)file).fileURL;

        } else {

            returnURL = null;

        }
        return returnURL;

    }

}
