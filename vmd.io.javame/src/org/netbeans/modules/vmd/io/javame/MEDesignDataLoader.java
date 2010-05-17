/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.io.javame;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.mobility.editor.pub.J2MEDataLoader;

/**
 * @author David Kaspar
 */
public final class MEDesignDataLoader extends J2MEDataLoader {

    static final long serialVersionUID = 7259154257404524113L;

    static final String EXT_JAVA = "java"; // NOI18N
    static final String EXT_DESIGN = "vmd"; // NOI18N

    public MEDesignDataLoader () {
        super ("org.netbeans.modules.vmd.io.javame.MEDesignDataObject"); // NOI18N
    }

    protected SystemAction[] defaultActions () {
        return new SystemAction[]{
                SystemAction.get (OpenAction.class),
                SystemAction.get (EditAction.class),
                SystemAction.get (SaveAction.class),
                SystemAction.get (FileSystemAction.class),
                null,
                SystemAction.get (ToolsAction.class),
                SystemAction.get (PropertiesAction.class)
        };
    }

    protected String defaultDisplayName () {
        return NbBundle.getMessage (MEDesignDataLoader.class, "DISP_DefaultName"); // NOI18N
    }

    protected FileObject findPrimaryFile (FileObject fileObject) {
        String ext = fileObject.getExt ();
        if (EXT_DESIGN.equals (ext))
            return FileUtil.findBrother (fileObject, EXT_JAVA);
        if (EXT_JAVA.equals (ext))
            if (FileUtil.findBrother (fileObject, EXT_DESIGN) != null)
                return fileObject;
        return null;
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, java.io.IOException {
        return new MEDesignDataObject (primaryFile, FileUtil.findBrother (primaryFile, EXT_DESIGN), this);
    }

    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject dataObject, FileObject primaryFile) {
        return JavaDataSupport.createJavaFileEntry (dataObject, primaryFile);
    }

    public MultiDataObject.Entry createSecondaryEntry (MultiDataObject dataObject, FileObject secondaryFile) {
        if (EXT_DESIGN.equals (secondaryFile.getExt ()))
            return new FileEntry (dataObject, secondaryFile);
        else
            return null;
    }

}
