/**************************************************************************
*
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
* The Original Software is the HTTP Javadoc Filesystem.
* The Initial Developer of the Original Software is Jeffrey A. Keyser.
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
