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
package org.netbeans.modules.j2ee.sun.ddloaders;

import java.io.IOException;
import java.util.Arrays;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;


/** Recognizes deployment descriptors of sun descriptor files.
 *
 * @author Peter Williams
 */
public class SunDescriptorDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 8616780278674213L;
    
    // mime types for various sun appserver descriptor files.
//    private static final String MIME_SUNEJBJAR_70 = "text/x-dd-sjsas-ejbjar2.0"; // NOI18N
//    private static final String MIME_SUNEJBJAR_80 = "text/x-dd-sjsas-ejbjar2.1"; // NOI18N
//    private static final String MIME_SUNEJBJAR_81 = "text/x-dd-sjsas-ejbjar2.11"; // NOI18N
//    private static final String MIME_SUNEJBJAR_90 = "text/x-dd-sjsas-ejbjar3.0"; // NOI18N
//    private static final String MIME_SUNWEBAPP_70 = "text/x-dd-sjsas-servlet2.3"; // NOI18N
//    private static final String MIME_SUNWEBAPP_80 = "text/x-dd-sjsas-servlet2.4"; // NOI18N
//    private static final String MIME_SUNWEBAPP_81 = "text/x-dd-sjsas-servlet2.41"; // NOI18N
//    private static final String MIME_SUNWEBAPP_90 = "text/x-dd-sjsas-servlet2.5"; // NOI18N
//    private static final String MIME_SUNAPPLICATION_70 = "text/x-dd-sjsas-application1.3"; // NOI18N
//    private static final String MIME_SUNAPPLICATION_80 = "text/x-dd-sjsas-application1.4"; // NOI18N
//    private static final String MIME_SUNAPPLICATION_90 = "text/x-dd-sjsas-application5.0"; // NOI18N
//    private static final String MIME_SUNAPPCLIENT_70 = "text/x-dd-sjsas-appclient1.3"; // NOI18N
//    private static final String MIME_SUNAPPCLIENT_80 = "text/x-dd-sjsas-appclient1.4"; // NOI18N
//    private static final String MIME_SUNAPPCLIENT_81 = "text/x-dd-sjsas-appclient1.41"; // NOI18N
//    private static final String MIME_SUNAPPCLIENT_90 = "text/x-dd-sjsas-appclient5.0"; // NOI18N

    public SunDescriptorDataLoader() {
        this("org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject");  // NOI18N
    }

    public SunDescriptorDataLoader(String name) {
        super(name);
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage (SunDescriptorDataLoader.class, "LBL_LoaderName"); // NOI18N
    }
    
    protected String actionsContext() {
        return "Loaders/text/x-sun-dd/Actions/"; // NOI18N
    }
    
//    protected void initialize() {
//         super.initialize();
//         String[] supportedTypes = getSupportedMimeTypes(); 
//         for (int i = 0; i < supportedTypes.length; i++) {
//             getExtensions().addMimeType(supportedTypes[i]);
//         }
//     }

    protected FileObject findPrimaryFile(FileObject fo) {
        FileObject result = null;
        
        if(!fo.isFolder() && DDType.getDDType(fo.getNameExt()) != null) {
            result = fo;
        }
        
        return result;
    }
    
    protected MultiDataObject createMultiObject (FileObject primaryFile)
            throws DataObjectExistsException, IOException {
        return new SunDescriptorDataObject(primaryFile, this);
    }

    /**
     *@return Array containing MIME types that this loader supports.
     */
//    protected String[] getSupportedMimeTypes() {
//        return new String[] { 
//            MIME_SUNEJBJAR_70, MIME_SUNEJBJAR_80, MIME_SUNEJBJAR_81, MIME_SUNEJBJAR_90,
//            MIME_SUNWEBAPP_70, MIME_SUNWEBAPP_80, MIME_SUNWEBAPP_81, MIME_SUNWEBAPP_90,
//            MIME_SUNAPPLICATION_70, MIME_SUNAPPLICATION_80, MIME_SUNAPPLICATION_90,
//            MIME_SUNAPPCLIENT_70, MIME_SUNAPPCLIENT_80, MIME_SUNAPPCLIENT_81, MIME_SUNAPPCLIENT_90
//        };
//    }
}
