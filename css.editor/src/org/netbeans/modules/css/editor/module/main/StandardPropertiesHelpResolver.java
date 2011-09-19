/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.main;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.editor.URLRetriever;
import org.netbeans.modules.css.editor.module.spi.CssModule;
import org.netbeans.modules.css.editor.module.spi.HelpResolver;
import org.netbeans.modules.css.editor.module.spi.Property;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author mfukala@netbeans.org
 */
public class StandardPropertiesHelpResolver extends HelpResolver {

    private static final Logger LOGGER = Logger.getLogger(HelpResolver.class.getName());
    private static final String SPEC_ARCHIVE_NAME = "docs/css3-spec.zip"; //NOI18N
    private static final AtomicReference<String> SPEC_ARCHIVE_INTERNAL_URL =
            new AtomicReference<String>();
    private static final String W3C_SPEC_URL_PREFIX = "http://www.w3.org/TR/"; //NOI18N
    private static final String MODULE_ARCHIVE_PATH = "www.w3.org/TR/"; //NOI18N
    private static final String INDEX_HTML_FILE_NAME = "index.html"; //NOI18N
    

    @Override
    public String getHelp(Property property) {
        CssModule cssModule = property.getCssModule();
        if(cssModule == null) {
            return null;
        }
        String moduleDocBase = cssModule.getSpecificationURL();
        if(moduleDocBase == null) {
            return null;
        }
        
        if (moduleDocBase.startsWith(W3C_SPEC_URL_PREFIX)) {
            String moduleFolderName = moduleDocBase.substring(W3C_SPEC_URL_PREFIX.length());
            StringBuilder propertyUrl = new StringBuilder();
            propertyUrl.append(getSpecURL());
            propertyUrl.append(MODULE_ARCHIVE_PATH);
            propertyUrl.append(moduleFolderName);
            propertyUrl.append('/');
            propertyUrl.append(INDEX_HTML_FILE_NAME);
            propertyUrl.append('#');
            propertyUrl.append(property.getName());
            try {
                URL propertyHelpURL = new URL(propertyUrl.toString());
                String urlContent = URLRetriever.getURLContentAndCache(propertyHelpURL);
                return urlContent;
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, null, ex);
                return null;
            }

        } else {
            return null;

        }

    }

    @Override
    public URL resolveLink(Property property, String link) {
        return null;
    }

    @Override
    public int getPriority() {
        return 500;
    }

    private String getSpecURL() {
        SPEC_ARCHIVE_INTERNAL_URL.compareAndSet(null, createSpecURL());
        return SPEC_ARCHIVE_INTERNAL_URL.get();
    }

    private String createSpecURL() {
        File file = InstalledFileLocator.getDefault().locate(SPEC_ARCHIVE_NAME, null, false); //NoI18N
        if (file != null) {
            try {
                URL urll = file.toURI().toURL(); //toURI should escape the illegal characters like spaces
                assert FileUtil.isArchiveFile(urll);
                return FileUtil.getArchiveRoot(urll).toExternalForm();
            } catch (java.net.MalformedURLException e) {
                //should not happen
                LOGGER.log(Level.SEVERE, String.format("Error obtaining archive root URL for file '%s'", file.getAbsolutePath()), e); //NOI18N
            }
        } else {
            LOGGER.warning(String.format("Cannot locate the css documentation file '%s'.", SPEC_ARCHIVE_NAME)); //NOI18N
        }
        return null;
    }
}
