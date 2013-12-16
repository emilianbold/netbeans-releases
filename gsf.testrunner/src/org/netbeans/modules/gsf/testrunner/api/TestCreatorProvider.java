/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf.testrunner.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gsf.testrunner.plugin.RootsProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author theofanis
 */
public abstract class TestCreatorProvider {
    
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Registration {

        /**
         * Display name of the TestCreatorProvider.
         */
        String displayName();
    }
    
    public abstract boolean enable(Node[] activatedNodes);
    
    public abstract void createTests(Context context);

    public static SourceGroup getSourceGroup(FileObject file, Project prj) {
        Sources src = ProjectUtils.getSources(prj);
        String type = "";
        Collection<? extends RootsProvider> providers = Lookup.getDefault().lookupAll(RootsProvider.class);
        for (RootsProvider provider : providers) {
            type = provider.getSourceRootType();
            break;
        }
        SourceGroup[] srcGrps = src.getSourceGroups(type);
        for (SourceGroup srcGrp : srcGrps) {
            FileObject rootFolder = srcGrp.getRootFolder();
            if (((file == rootFolder) || FileUtil.isParentOf(rootFolder, file)) 
                    && srcGrp.contains(file)) {
                return srcGrp;
            }
        }
        return null;
    }

    public static final class Context {

        private boolean singleClass;
        private String testClassName;
        private FileObject targetFolder;
        private Node[] activatedNodes;
        private boolean integrationTests;
        
        public Context(Node[] activatedNodes) {
            this.activatedNodes = activatedNodes;
        }

        public Node[] getActivatedNodes() {
            return activatedNodes;
        }

        public boolean isSingleClass() {
            return singleClass;
        }

        public void setSingleClass(boolean singleClass) {
            this.singleClass = singleClass;
        }

        public FileObject getTargetFolder() {
            return targetFolder;
        }

        public void setTargetFolder(FileObject targetFolder) {
            this.targetFolder = targetFolder;
        }

        public String getTestClassName() {
            return testClassName;
        }

        public void setTestClassName(String testClassName) {
            this.testClassName = testClassName;
        }

        public boolean isIntegrationTests() {
            return integrationTests;
        }

        public void setIntegrationTests(boolean integrationTests) {
            this.integrationTests = integrationTests;
        }
        
    }
    
}
