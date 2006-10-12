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

package org.netbeans.modules.j2ee.ddloaders.ejb;

import java.io.IOException;
//import java.util.Vector;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;

import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ddloaders.multiview.*;

/** Recognizes deployment descriptors of ejb modules.
 *
 *@see EjbJar30DataLoader
 *
 * @author Ludovic Champenois
 */
public class EjbJarDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 8616780278674213L;
    private static final String REQUIRED_MIME_PREFIX_1 = "text/x-dd-ejbjar2.0"; // NOI18N
    private static final String REQUIRED_MIME_PREFIX_2 = "text/x-dd-ejbjar2.1"; // NOI18N

    public EjbJarDataLoader () {
        this("org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject");  // NOI18N
    }

    public EjbJarDataLoader(String name){
        super(name);
    }

    protected String defaultDisplayName () {
        return NbBundle.getMessage (EjbJarDataLoader.class, "LBL_loaderName");
    }
    
    protected String actionsContext() {
        return "Loaders/text/x-dd/Actions/"; // NOI18N
    }
    
    protected void initialize () {
         super.initialize ();
         String[] supportedTypes = getSupportedMimeTypes(); 
         for (int i = 0; i < supportedTypes.length; i++) {
             getExtensions().addMimeType(supportedTypes[i]);
         }
     }

    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new EjbJarMultiViewDataObject(primaryFile, this);
    }

    /**
     *@return Array containing MIME types that this loader supports.
     */
    protected String[] getSupportedMimeTypes(){
        return new String[]{REQUIRED_MIME_PREFIX_1, REQUIRED_MIME_PREFIX_2};
    }
}
