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

package org.netbeans.modules.websvc.wsdl.config;

import java.io.IOException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** Loader for WsCompile Configuration File DataObjects.
 *
 * @author Peter Williams
 */
public class WsCompileConfigDataLoader extends UniFileLoader {

    private static final String JAXRPC_1_1_CONFIG_MIME_TYPE = "text/jaxrpc-config-1-1"; // NOI18N

    public WsCompileConfigDataLoader() {
        super ("org.netbeans.modules.websvc.wsdl.config.WsCompileConfigDataObject"); // NOI18N
    }

    protected void initialize() {
        super.initialize();

        ExtensionList extensions = new ExtensionList();
        extensions.addMimeType(JAXRPC_1_1_CONFIG_MIME_TYPE);
        setExtensions(extensions);
    }


    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new WsCompileConfigDataObject(primaryFile, this);
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getBundle(WsCompileConfigDataLoader.class).getString("LBL_WsCompileConfigLoader_Name"); // NOI18N
    }
    
    protected String actionsContext() {
        return "Loaders/text/jaxrpc-config-1-1/Actions/"; // NOI18N
    }
}
