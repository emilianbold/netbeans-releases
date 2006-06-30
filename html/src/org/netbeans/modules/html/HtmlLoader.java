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

package org.netbeans.modules.html;

import java.io.IOException;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Loader for Html DataObjects.
 *
 * @author Jan Jancura
 */
public class HtmlLoader extends UniFileLoader {

    private static final long serialVersionUID = -5809935261731217882L;

    public HtmlLoader() {
        super("org.netbeans.modules.html.HtmlDataObject"); // NOI18N
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType("text/html"); // NOI18N
    }
    
    protected MultiDataObject createMultiObject(final FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new HtmlDataObject(primaryFile, this);
    }
    
    /** Get the default display name of this loader.
     * @return default display name
     */
    protected String defaultDisplayName() {
        return NbBundle.getMessage(HtmlLoader.class, "PROP_HtmlLoader_Name");
    }
    
    protected String actionsContext() {
        return "Loaders/text/html/Actions/"; // NOI18N
    }
    
}
