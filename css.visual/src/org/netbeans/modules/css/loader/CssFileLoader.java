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

/*
 * CssFileLoader.java
 *
 * Created on December 8, 2004, 10:06 PM
 */

package org.netbeans.modules.css.loader;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Data loader that recognizes and loads the CSS files
 * @author Winston Prakash
 * @version 1.0
 */
public class CssFileLoader extends UniFileLoader{

    public static final String CSS_MIME_TYPE = "text/x-css"; //NOI18N

    /** Creates a new instance of CssFileLoader */
    public CssFileLoader() {
        super(org.netbeans.modules.css.loader.CssDataObject.class.getName());
    }

    /** Get the default display name of this loader.
     * @return default display name
     */
    protected String defaultDisplayName() {
        return NbBundle.getMessage(CssFileLoader.class, "CssLoaderName"); // NOI18N
    }

    /**
     * Initialize shared state of this shared class (SharedClassObject)
     */
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(CSS_MIME_TYPE);
    }

    protected FileObject findPrimaryFile(FileObject fo) {
        if (fo.isFolder()) {
            return null;
        }

        FileObject primaryFile = super.findPrimaryFile(fo);
        if (primaryFile == null) {
            return null;
        }
        return primaryFile;
    }

    /**
     * Create the data object for a given primary file.
     * @return  data object for the file
     */
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, java.io.IOException {
        return new CssDataObject(primaryFile, this);
    }

    protected String actionsContext() {
        return "Loaders/" + CSS_MIME_TYPE + "/Actions";
    }
}
