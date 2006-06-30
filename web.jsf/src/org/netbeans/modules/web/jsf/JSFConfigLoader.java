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

package org.netbeans.modules.web.jsf;

import java.io.IOException;

import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
//import org.netbeans.modules.struts.actions.SAXParseErrorAction;

/**
 *
 * @author Petr Pisl
 */
public class JSFConfigLoader extends UniFileLoader {
    private static final String REQUIRED_MIME_1_0 = "text/jsf-config1.0"; // NOI18N
    private static final String REQUIRED_MIME_1_1 = "text/jsf-config1.1"; // NOI18N 
   
    public JSFConfigLoader() {
        this("org.netbeans.modules.web.jsf.JSFConfigLoader");
    }
    
    // Can be useful for subclasses:
    protected JSFConfigLoader(String recognizedObjectClass) {
        super(recognizedObjectClass);
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(JSFConfigLoader.class, "LBL_loaderName");
    }
    
    protected void initialize() {
        
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME_1_0);
        getExtensions().addMimeType(REQUIRED_MIME_1_1);
    }
    
    protected String actionsContext() {
        return "Loaders/text/x-jsf+xml/Actions/"; // NOI18N
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new JSFConfigDataObject(primaryFile, this);
    }
}