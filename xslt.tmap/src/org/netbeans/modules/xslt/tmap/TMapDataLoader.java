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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xslt.tmap;

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapDataLoader extends UniFileLoader {
    
    public static final String MIME_TYPE = "text/x-tmap+xml";// NOI18N
    public static final String TRANSFORMMAP_XML = "transformmap.xml";// NOI18N
    public static final String LOADER_NAME = "LBL_loader_name"; // NOI18N
    public static final String ACTION_CONTEXT = "Loaders/" + 
            MIME_TYPE + "/Actions"; // NOI18N
    
    private static final long serialVersionUID = 1L;
    
    public TMapDataLoader() {
        super("org.netbeans.modules.xslt.tmap.TMapDataObject");
    }
    
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(TMapDataLoader.class, LOADER_NAME);
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(MIME_TYPE);
    }
    
    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new TMapDataObject(primaryFile, this);
    }
    
    @Override
    protected FileObject findPrimaryFile( FileObject fo ) {
        FileObject primaryFo = null;
        String extension = fo.getNameExt();
        if (extension.equals(TRANSFORMMAP_XML)) 
        {
            // recognize xslt file only in context of XSLT project
            primaryFo = isXsltProjectContext(fo) ? fo : null;
        }
        return primaryFo;
    }    
    
    // TODO m
    private boolean isXsltProjectContext(FileObject fo) {
        boolean isXsltProject = false;
        
        Project project = FileOwnerQuery.getOwner(fo);
        FileObject source = project == null ? null : Util.getProjectSource(project);

        isXsltProject = source != null && source.equals(fo.getParent());
        
        return isXsltProject;
    }
    
    
    @Override
    protected String actionsContext() {
        return ACTION_CONTEXT;
    }
    
}
