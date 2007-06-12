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

package org.netbeans.modules.xml.schema;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;

/**
 * XML Schema loader. It is mime type based.
 *
 * @author  Petr Kuzel
 * @author  Jeri Lockhart
 */
public class SchemaDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 3924626446133520078L;

    public static final String MIME_TYPE = "application/x-schema+xml";                 // NOI18N

    /**
     * Creates a new instance of SchemaDataLoader
     */
    public SchemaDataLoader() {
        super("org.netbeans.modules.xml.schema.SchemaDataObject");                  // NOI18N

    }
    
    protected String actionsContext() {
	return "Loaders/text/x-schema+xml/Actions/";
    }
    
    /** Does initialization. Initializes display name,
     * extension list and the actions. */
    protected void initialize () {
        super.initialize();
        
        ExtensionList ext = getExtensions();
        ext.addMimeType (MIME_TYPE);
        setExtensions (ext);
    }
    
    /**
     * Lazy init name.
     */
    protected String defaultDisplayName () {
        return NbBundle.getMessage (SchemaDataLoader.class,"LBL_SchemaDataLoader_name");
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
        return new SchemaDataObject (primaryFile, this);
    }
    
}
