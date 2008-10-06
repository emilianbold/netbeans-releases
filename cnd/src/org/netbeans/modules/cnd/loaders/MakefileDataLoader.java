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

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import org.netbeans.modules.cnd.utils.MIMENames;

import org.netbeans.modules.cnd.editor.filecreation.ExtensionsSettings;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;


/** Recognizes single files in the Repository as being of a certain type */
public class MakefileDataLoader extends CndAbstractDataLoader {

    /** Serial version number */
    static final long serialVersionUID = -7148711275717543299L;

    private static MakefileDataLoader instance = null;

    public MakefileDataLoader() {
	super("org.netbeans.modules.cnd.loaders.MakefileDataObject"); // NOI18N
        init();
    }
    
    /** Do various initializations */
    private void init() {
        instance = this;
    }

    public static MakefileDataLoader getInstance(){
        if (instance == null) {
            instance = SharedClassObject.findObject(MakefileDataLoader.class, true);
        }
        return instance;
    }
    
    @Override
    protected String actionsContext () {
        return "Loaders/text/x-make/Actions/"; // NOI18N
    }

    /** set the default display name */
    @Override
    protected String defaultDisplayName() {
	return NbBundle.getMessage(MakefileDataLoader.class,
			    "PROP_MakefileDataLoader_Name"); // NOI18N
    }

    /** Create the DataObject */
    protected MultiDataObject createMultiObject(FileObject primaryFile)
                throws DataObjectExistsException, IOException {
	return new MakefileDataObject(primaryFile, this);
    }

    /** Find the primary file */
    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        if (fo.isFolder()) {
            return null;
        }
        String mimeType = fo.getMIMEType();
        if (MIMENames.MAKEFILE_MIME_TYPE.equals(mimeType)) {
            return fo;
        }
	return null;
    }

    public String getDisplayNameForExtensionList() {
	throw new UnsupportedOperationException();
    }

    public String getSettingsName() {
        return ExtensionsSettings.MAKEFILE;
    }

    @Override
    protected String getMimeType() {
        return MIMENames.MAKEFILE_MIME_TYPE;
    }
}

