/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsfTemplateUtils {

    private static final String LOCALIZING_BUNDLE = "SystemFileSystem.localizingBundle"; //NOI18N
    private static final Comparator TEMPLATE_COMPARATOR = new TemplateComparator();

    public static final String TEMPLATE_SNIPET_BP = "/Templates/JSF/JSF_From_Entity_Snippets";

    public static String getLocalizedName(FileObject fo) {
        String name = fo.getNameExt();
        String bundleName = (String) fo.getAttribute(LOCALIZING_BUNDLE);
        if (bundleName != null) {
            try {
                bundleName = org.openide.util.Utilities.translate(bundleName);
                ResourceBundle b = NbBundle.getBundle(bundleName);
                String localizedName = b.getString(fo.getPath());
                if (localizedName != null) {
                    name = localizedName;
                }
            } catch (MissingResourceException ex) {
            // ignore
            }
        }

        return name;
    }

    public static List<Template> getSnippetTemplates() {
        List<Template> result = new ArrayList<>();
        FileObject templateRoot = FileUtil.getConfigRoot().getFileObject(TEMPLATE_SNIPET_BP);
        assert templateRoot != null; // at least the base templates should be registered
        Enumeration<? extends FileObject> children = templateRoot.getChildren(false);
        while (children.hasMoreElements()) {
            FileObject folder = children.nextElement();
            Object position = folder.getAttribute("position");
            if (position == null || !(position instanceof Integer)) {
                result.add(new Template(folder.getName(), getLocalizedName(folder)));
            } else {
                result.add(new Template(folder.getName(), getLocalizedName(folder), (Integer) position));
            }
        }
        Collections.sort(result, TEMPLATE_COMPARATOR);
        return result;
    }

    public static String getTemplatePath(String basePath, String templatesStyle, String template) {
        return basePath + "/" + templatesStyle + "/" + template; //NOI18N
    }

    public static String getSnippetTemplatePath(String templatesStyle, String template) {
        return TEMPLATE_SNIPET_BP + "/" + templatesStyle + "/" + template; //NOI18N
    }

    public static class TemplateComparator implements Comparator<Template> {
        @Override
        public int compare(Template o1, Template o2) {
            return o1.getPosition() - o2.getPosition();
        }
    }

    public static class Template {

        private final String name;
        private final String displayName;
        private final int position;

        public Template(String name, String displayName) {
            this(name, displayName, Integer.MAX_VALUE);
        }

        public Template(String name, String displayName, int position) {
            this.name = name;
            this.displayName = displayName;
            this.position = position;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getPosition() {
            return position;
        }

    }
}
