/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
