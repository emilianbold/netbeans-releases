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

package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.net.URI;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * FileOwnerQuery dealing with files that are not in the project directory.
 * A typical situation for CND project created from existing sources.
 *
 * @author Alexey Vladykin
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.FileOwnerQueryImplementation.class)
public class MakeProjectFileOwnerQuery implements FileOwnerQueryImplementation {

    public Project getOwner(URI uri) {
        try {
            if ("file".equals(uri.getScheme())) { // NOI18N
                return getOwner(new File(uri));
            }
        } catch (IllegalArgumentException x) {/* skip it */}
        return null;
    }

    public Project getOwner(FileObject fo) {
        File f = FileUtil.toFile(fo);
        return f != null ? getOwner(f) : null;
    }

    private Project getOwner(File f) {
        String path = f.getAbsolutePath();
        for (Project project : OpenProjects.getDefault().getOpenProjects()) {
            ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (provider != null) {
                MakeConfigurationDescriptor descriptor = (MakeConfigurationDescriptor)provider.getConfigurationDescriptor();
                if (descriptor != null && (descriptor.findProjectItemByPath(path) != null
                            || descriptor.findExternalItemByPath(path) != null)) {
                    return project;
                }
            }
        }
        return null;
    }

}
