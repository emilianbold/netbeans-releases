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

package org.netbeans.modules.url;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Data loader which recognizes URL files.
 *
 * @author Ian Formanek
 */
public class URLDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    static final long serialVersionUID =-7407252842873642582L;
    /** MIME-type of URL files */
    private static final String URL_MIME_TYPE = "text/url";             //NOI18N
    
    
    /** Creates a new URLDataLoader without the extension. */
    public URLDataLoader() {
        super("org.netbeans.modules.url.URLDataObject");                //NOI18N
    }

    
    /**
     * Initializes this loader. This method is called only once the first time
     * this loader is used (not for each instance).
     */
    protected void initialize () {
        super.initialize();

        ExtensionList ext = new ExtensionList();
        ext.addMimeType(URL_MIME_TYPE);
        ext.addMimeType("text/x-url");                                  //NOI18N
        setExtensions(ext);
    }

    /** */
    protected String defaultDisplayName() {
        return NbBundle.getMessage(URLDataLoader.class,
                                   "PROP_URLLoader_Name");              //NOI18N
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    protected String actionsContext () {
        return "Loaders/text/url/Actions/";                             //NOI18N
    }
    
    /**
     * @return  <code>URLDataObject</code> for the specified file
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, IOException {
        return new URLDataObject(primaryFile, this);
    }

}
