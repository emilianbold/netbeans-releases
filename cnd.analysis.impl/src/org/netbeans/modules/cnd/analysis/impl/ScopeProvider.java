/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.analysis.impl;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.Project;
import org.netbeans.modules.analysis.spi.AnalysisScopeProvider;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander Simon
 */
@ProjectServiceProvider(projectType="org-netbeans-modules-cnd-makeproject", service=AnalysisScopeProvider.class)
public class ScopeProvider implements AnalysisScopeProvider {

    private final Project project;

    public ScopeProvider(Project project) {
        this.project = project;
    }

    @Override
    public Scope getScope() {
        Collection<FileObject> sourceRoots = null;
        Collection<NonRecursiveFolder> folders = null;
        Collection<FileObject> files = null;
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp.gotDescriptor()) {
            MakeConfigurationDescriptor configurationDescriptor = pdp.getConfigurationDescriptor();
            files = new ArrayList<FileObject>();
            for (Item item : configurationDescriptor.getProjectItems()) {
                PredefinedToolKind defaultTool = item.getDefaultTool();
                if (defaultTool == PredefinedToolKind.CCompiler || defaultTool == PredefinedToolKind.CCCompiler) {
                    files.add(item.getFileObject());
                }
            }
        }
        return Scope.create(sourceRoots, folders, files);
    }
}
