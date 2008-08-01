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

package org.netbeans.modules.web.project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileUtil;

/**
 * Miscellaneous utilities for the web project module.
 * Mainly addded to bring the web project in sync with the j2se project.
 *
 * @author Andrei Badea
 */
public final class WebProjectUtil {

    private WebProjectUtil() {
        super();
    }

    /**
     * Creates an URL of a classpath or sourcepath root
     * For the existing directory it returns the URL obtained from {@link File#toUri()}
     * For archive file it returns an URL of the root of the archive file
     * For non existing directory it fixes the ending '/'
     * @param root the file of a root
     * @param offset a path relative to the root file or null (eg. src/ for jar:file:///lib.jar!/src/)" 
     * @return an URL of the root
     * @throws MalformedURLException if the URL cannot be created
     */
    public static URL getRootURL (File root, String offset) throws MalformedURLException {
        URL url = root.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        } else if (!root.exists()) {
            url = new URL(url.toExternalForm() + "/"); // NOI18N
        }
        if (offset != null) {
            assert offset.endsWith("/");    //NOI18N
            url = new URL(url.toExternalForm() + offset); // NOI18N
        }
        return url;
    }
    
    public static LibraryChooser.Filter getFilter(WebProject p) {
        LibraryChooser.Filter filter = null;
        if ("1.3".equals(WebModule.getWebModule(p.getProjectDirectory()).getJ2eePlatformVersion())) { // NOI18N
            filter = new LibraryChooser.Filter() {
                public boolean accept(Library library) {
                    if ("javascript".equals(library.getType())) { //NOI18N
                        return false;
                    }
                    try {
                        library.getContent("classpath"); //NOI18N
                    } catch (IllegalArgumentException ex) {
                        return false;
                    }
                    return !library.getName().matches("jstl11|jaxrpc16|toplink|Spring|jaxws20|jaxb20|struts|jsf"); // NOI18N
                }
            };
        }
        return filter;
    }

}
