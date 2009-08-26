/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class JsfIndex {

    private final QuerySupport index;

    /** Creates a new instance of JsIndex */
    private JsfIndex(QuerySupport index) {
        this.index = index;
    }

    private static JsfIndex get(FileObject[] roots) {
        try {
            return new JsfIndex(QuerySupport.forRoots(JsfIndexer.Factory.NAME,
                    JsfIndexer.Factory.VERSION,
                    roots));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return new JsfIndex(null);
        }

    }

    public static JsfIndex get(WebModule wm) {
        return get(wm.getDocumentBase());
    }

    public static JsfIndex get(HtmlParserResult result) {
        FileObject file = result.getSnapshot().getSource().getFileObject();
        return get(file);

    }
    public static JsfIndex get(FileObject file) {
        return get(ClassPath.getClassPath(file, ClassPath.SOURCE).getRoots());
    }

    public Collection<String> getAllCompositeLibraryNames() {
        Collection<String> libNames = new ArrayList<String>();
        try {
            Collection<? extends IndexResult> results = index.query(CompositeComponentModel.LIBRARY_NAME_KEY, "", QuerySupport.Kind.PREFIX, CompositeComponentModel.LIBRARY_NAME_KEY);
            for (IndexResult result : results) {
                String libraryName = result.getValue(CompositeComponentModel.LIBRARY_NAME_KEY);
                libNames.add(libraryName);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return libNames;
    }

    public Collection<String> getCompositeLibraryComponents(String libraryName) {
        Collection<String> components = new ArrayList<String>();
        try {
            Collection<? extends IndexResult> results = index.query(CompositeComponentModel.LIBRARY_NAME_KEY, libraryName, QuerySupport.Kind.EXACT, CompositeComponentModel.LIBRARY_NAME_KEY);
            for (IndexResult result : results) {
                FileObject file = result.getFile();
                components.add(file.getName());
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return components;
    }
    


}
