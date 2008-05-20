/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.componentssupport.ui.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author avk
 */
public class BaseHelper {

    public static final String UTF_8            = "UTF-8";                       // NOI18N
    public static final String XML_EXTENSION    = ".xml";                        // NOI18N
    public static final String ZIP_EXTENSION    = ".zip";                        // NOI18N

    public static final String SRC              = "src/";                        // NOI18N
    public static final String BUNDLE_PROPERTIES 
                                                = "Bundle.properties";           // NOI18N
    public static final String LAYER_XML        = "layer.xml";                   // NOI18N

    private static final String TEMPLATES_LAYER_FOLDER        
                                    = "Templates/MobilityCustomComponent-files/";// NOI18N
    
    /**
     * Convenience method for loading {@link EditableProperties} from a {@link
     * FileObject}. New items will alphabetizied by key.
     *
     * @param propsFO file representing properties file
     * @exception FileNotFoundException if the file represented by the given
     *            FileObject does not exists, is a folder rather than a regular
     *            file or is invalid. i.e. as it is thrown by {@link
     *            FileObject#getInputStream()}.
     */
    public static EditableProperties loadProperties(FileObject propsFO) throws IOException {
        InputStream propsIS = propsFO.getInputStream();
        EditableProperties props = new EditableProperties(true);
        try {
            props.load(propsIS);
        } finally {
            propsIS.close();
        }
        return props;
    }
    
    /**
     * Convenience method for storing {@link EditableProperties} into a {@link
     * FileObject}.
     *
     * @param propsFO file representing where properties will be stored
     * @param props properties to be stored
     * @exception IOException if properties cannot be written to the file
     */
    public static void storeProperties(FileObject propsFO, EditableProperties props) throws IOException {
        FileLock lock = propsFO.lock();
        try {
            OutputStream os = propsFO.getOutputStream(lock);
            try {
                props.store(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }

    /**
     * Convenience method to load a file template from the standard location.
     * @param name a simple filename
     * @return that file from the <code>Templates/NetBeansModuleDevelopment-files</code> layer folder
     */
    public static FileObject getTemplate(String name) {
        FileObject f = Repository.getDefault().getDefaultFileSystem().
                findResource(TEMPLATES_LAYER_FOLDER + name);
        assert f != null : name;
        return f;
    }

    
    public static void copyByteAfterByte(FileObject source, FileObject target) throws IOException {
            InputStream is = source.getInputStream();
            try {
                copyByteAfterByte(is, target);
            } finally {
                is.close();
            }
    }

    public static void copyByteAfterByte( InputStream is, FileObject target  )
            throws IOException
    {
        OutputStream out = target.getOutputStream();
        try {
            FileUtil.copy(is, out);
        }
        finally {
            out.close();
        }
    }
}
