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

package org.netbeans.modules.j2ee.persistence.unit;

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * @author Martin Adamek
 */
public class PUDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-persistence1.0";
    /**
     * A workaround for issue 95675.
     */
    private static final String REQUIRED_EXTENSION = "xml-jpa";
    
    public PUDataLoader() {
        super(PUDataLoader.class.getName());
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
        getExtensions().addExtension(REQUIRED_EXTENSION);
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(PUDataLoader.class, "LBL_loaderName"); // NOI18N
    }
    
    protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
        // a workaround for issue 95675 
        boolean parse = !REQUIRED_EXTENSION.equals(pf.getExt());
        return new PUDataObject(pf, this, parse);
    }
    
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
    
    protected FileObject findPrimaryFile(FileObject fo) {
        FileObject superFo = super.findPrimaryFile(fo);
        return (superFo != null && FileOwnerQuery.getOwner(superFo) != null)
                ? superFo : null;
    }
    
}
