/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.node;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.clientproject.grunt.TargetLister;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
@MIMEResolver.Registration(displayName = "package.json", resource = "npm-resolver.xml", position = 125)
@ActionID(id = "org.netbeans.modules.web.clientproject.grunt.NpmInstallAction", category = "Build")
@ActionRegistration(displayName = "#CTL_NpmInstallAction", lazy=false)
@ActionReferences(value = {
    @ActionReference(position = 906, path = "Editors/text/package+x-json/Popup"),
    @ActionReference(position = 156, path = "Loaders/text/package+x-json/Actions"),
    @ActionReference(path="Projects/org-netbeans-modules-web-clientproject/Actions", position = 170)
})

public class NpmInstallAction extends AbstractAction implements ContextAwareAction {
    
    public @Override void actionPerformed(ActionEvent e) {
        assert false;
    }
    
    public @Override Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }
    
    @NbBundle.Messages({
        "CTL_NpmInstall=Npm install"
    })
    private static final class ContextAction extends AbstractAction {

        private FileObject package_json;
        
        public ContextAction(Lookup context) {
            super(Bundle.CTL_NpmInstall());
            Project p = context.lookup(Project.class);
            if (p!=null) {
                package_json = p.getProjectDirectory().getFileObject("package.json");//NOI18N
            } else {
                DataObject dob = context.lookup(DataObject.class);
                package_json = dob.getPrimaryFile();
            }
            setEnabled(package_json!=null);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }
        
        @NbBundle.Messages({
             "# {0} - project name",
             "TTL_npm_install=npm install ({0})"
         })
        public @Override
        void actionPerformed(ActionEvent e) {
            try {
                Project p = FileOwnerQuery.getOwner(package_json);
                String display = p!=null?ProjectUtils.getInformation(p).getDisplayName():package_json.getParent().getName();
                new NodeExecutor(
                        Bundle.TTL_npm_install(display),
                        "npm",
                        package_json.getParent(), new String[]{"install"}).execute(); //NOI18N
                TargetLister.invalidateCache(package_json.getParent().getFileObject("Gruntfile.js")); //NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}