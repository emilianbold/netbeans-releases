/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.editor.facelets;

import com.sun.faces.spi.ConfigurationResourceProvider;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.ServletContext;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.web.jsf.editor.index.JsfBinaryIndexer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;

/**
 * Facelets taglibs provider searching for the descriptors on project's source classpath.
 *
 * @author marekfukala
 */
class FaceletTaglibraryConfigProvider implements ConfigurationResourceProvider {

    private static final String FACELETS_LIB_SUFFIX = ".taglib.xml"; //NOI18N

    private ClassPath classpath;

    public FaceletTaglibraryConfigProvider(ClassPath classpath) {
        this.classpath = classpath;
    }

    @Override
    public Collection<URL> getResources(ServletContext sc) {
        List<FileObject> metainfs = classpath.findAllResources("META-INF"); //NOI18N

        Collection<URL> urls = new ArrayList<URL>();
        for(FileObject metainf : metainfs) {
            Collection<FileObject> descriptors = JsfBinaryIndexer.findLibraryDescriptors(metainf, FACELETS_LIB_SUFFIX);
            for(FileObject fo : descriptors) {
                try {
                    urls.add(fo.getURL());
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        return urls;
    }


}
