/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

import org.netbeans.modules.cnd.MIMENames;

/**
 *
 * @author Alexander Simon
 */
public class FortranDataLoader extends CndAbstractDataLoader {
    
    private static FortranDataLoader instance;

    /** Serial version number */
    static final long serialVersionUID = 6801389470714975686L;

    /** The suffix list for Fortran primary files */
    private static final String[] fortranExtensions =
	   { "f", "F", "f90", "F90", "f95", "F95", "for", "il", "mod" };// NOI18N

    protected FortranDataLoader() {
	super("org.netbeans.modules.cnd.loaders.FortranDataObject"); // NOI18N
        instance = this;
        createExtentions(fortranExtensions);
    }

    public static FortranDataLoader getInstance(){
        if (instance == null) {
            instance = SharedClassObject.findObject(FortranDataLoader.class, true);
        }
        return instance;
    }

    /** set the default display name */
    protected String defaultDisplayName() {
	return NbBundle.getMessage(CndAbstractDataLoader.class, "PROP_FortranDataLoader_Name"); // NOI18N
    }

    protected String getMimeType(){
        return MIMENames.FORTRAN_MIME_TYPE;
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new FortranDataObject(primaryFile, this);
    }
}
