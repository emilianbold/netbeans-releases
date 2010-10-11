/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.editor.ext.html.parser.spi.HtmlSourceVersionController;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * The fallback version resolver. It reads project's property default-public-id and if
 * set returns the appropriate HtmlVersion instance.
 *
 * @author marekfukala
 */
@ServiceProvider(service = HtmlSourceVersionController.class)
public class ProjectDefaultHtmlSourceVersionController implements HtmlSourceVersionController {

    public static final String HTML_VERSION_PUBLIC_ID_AUX_PROPERTY_NAME = "default-public-id"; //NOI18N
    private static final String HTML_ARTIFICIAL_PUBLIC_ID = "html5";

    @Override
    public HtmlVersion getSourceCodeVersion(HtmlSource source, HtmlVersion detectedVersion) {
        if (detectedVersion != null) {
            return null;
        }
        FileObject file = source.getSourceFileObject();
        if (file == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }
        
        return getDefaultHtmlVersion(project);
    }

    public static HtmlVersion getDefaultHtmlVersion(Project project) {
        Preferences prefs = ProjectUtils.getPreferences(project, HtmlSourceVersionController.class, true);
        String ns = prefs.get(HTML_VERSION_PUBLIC_ID_AUX_PROPERTY_NAME, null);
        if (ns == null) {
            return null;
        }

        if(ns.equals(HTML_ARTIFICIAL_PUBLIC_ID)) {
            ns = null; //html5
        }

        try {
            return HtmlVersion.findByPublicId(ns);
        } catch (IllegalArgumentException e) {
//            Logger.getAnonymousLogger().log(Level.WARNING,
//                    String.format("Invalid value of the auxiliary.org-netbeans-modules-html-editor-lib.htmlversion property: %s ", version)); //NOI18N
        }
        return null;
    }

    public static void setDefaultHtmlVersion(Project project, HtmlVersion version) {
        Preferences prefs = ProjectUtils.getPreferences(project, HtmlSourceVersionController.class, true);
        String publicId = version.getPublicID();
        if(publicId == null) {
            publicId = HTML_ARTIFICIAL_PUBLIC_ID; //html5 has no public id
        }
        prefs.put(HTML_VERSION_PUBLIC_ID_AUX_PROPERTY_NAME, publicId);
    }
}
