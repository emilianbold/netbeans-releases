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

package org.netbeans.modules.ruby.rhtml.loaders;

import java.io.IOException;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class RhtmlDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 1L;
    
    public RhtmlDataLoader() {
        super("org.netbeans.modules.ruby.rhtml.loaders.RhtmlDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(RhtmlDataLoader.class, "LBL_Rhtml_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(RhtmlTokenId.MIME_TYPE);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new RhtmlDataObject(primaryFile, this);
    }
    
    protected String actionsContext() {
        return "Loaders/" + RhtmlTokenId.MIME_TYPE + "/Actions";
    }
    
}
