/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author Alexander Simon
 */
public class TemplateAttributesProviderImpl implements CreateFromTemplateAttributesProvider {

    private final MakeProject project;
    private final FileEncodingQueryImplementation encodingQuery;
    private static final Logger LOG = Logger.getLogger(TemplateAttributesProviderImpl.class.getName());


    public TemplateAttributesProviderImpl(MakeProject project, FileEncodingQueryImplementation encodingQuery) {
        this.project = project;
        this.encodingQuery = encodingQuery;
    }

    @Override
    public Map<String, ?> attributesFor(DataObject template, DataFolder target, String name) {
        Map<String, String> values = new HashMap<>();
        Properties priv = project.getProjectProperties(false);
        Properties props = project.getProjectProperties(true);
        String licensePath = priv.getProperty("project.licensePath"); // NOI18N
        if (licensePath == null) {
            licensePath = props.getProperty("project.licensePath"); // NOI18N
        }
        if (licensePath != null) {
            values.put("licensePath", licensePath); // NOI18N
        }
        String license = priv.getProperty("project.license"); // NOI18N
        if (license == null) {
            license = props.getProperty("project.license"); // NOI18N
        }
        if (license != null) {
            values.put("license", license); // NOI18N
        }
        Charset charset = encodingQuery.getEncoding(target.getPrimaryFile());
        String encoding = (charset != null) ? charset.name() : null;
        if (encoding != null) {
            values.put("encoding", encoding); // NOI18N
        }
        try {
            ProjectInformation info = ProjectUtils.getInformation(project);
            if (info != null) {
                String pname = info.getName();
                if (pname != null) {
                    values.put("name", pname);// NOI18N
                }
                String pdname = info.getDisplayName();
                if (pdname != null) {
                    values.put("displayName", pdname);// NOI18N
                }
            }
        } catch (Exception ex) {
            //not really important, just log.
            Logger.getLogger(TemplateAttributesProviderImpl.class.getName()).log(Level.FINE, "", ex);
        }
       if (values.isEmpty()) {
            return null;
        } else {
            return Collections.singletonMap("project", values); // NOI18N
        }
    }
}
