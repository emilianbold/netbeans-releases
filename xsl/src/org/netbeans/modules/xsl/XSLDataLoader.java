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
package org.netbeans.modules.xsl;


import org.openide.filesystems.*;
import org.openide.loaders.*;

import org.netbeans.modules.xml.core.XMLDataLoader;

/**
 * XSL object loader. It is mime type based.
 *
 * @author Libor Kramolis
 */
public final class XSLDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 6494980346565290872L;

    /** Creates a new instance of SchemaLoader */
    public XSLDataLoader() {
        super ("org.netbeans.modules.xsl.XSLDataObject"); // NOI18N
    }

    /** Does initialization. Initializes display name,
     * extension list and the actions. */
    protected void initialize () {
        super.initialize();
        
        ExtensionList ext = getExtensions();
        ext.addMimeType (XSLDataObject.MIME_TYPE);
        setExtensions (ext);
    }
    
    protected String actionsContext() {
        return "Loaders/application/xslt+xml/Actions/"; // NOI18N
    }
    
    /**
     * Lazy init name.
     */
    protected String defaultDisplayName () {
        return Util.THIS.getString ("NAME_XSLDataLoader");
    }
    
    /** Creates the right primary entry for given primary file.
     *
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new XMLDataLoader.XMLFileEntry (obj, primaryFile);  //adds smart templating
    }

    /** Creates the right data object for given primary file.
     * It is guaranteed that the provided file is realy primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
            throws DataObjectExistsException, java.io.IOException {
        return new XSLDataObject (primaryFile, this);
    }
    
}
