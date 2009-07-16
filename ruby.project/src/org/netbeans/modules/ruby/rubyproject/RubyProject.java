/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.rubyproject;

import java.io.IOException;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.ruby.platform.RubyPlatformProvider;
import org.netbeans.modules.ruby.RubyLanguage;
import org.netbeans.modules.ruby.codecoverage.RubyCoverageProvider;
import org.netbeans.modules.ruby.rubyproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.ruby.rubyproject.queries.RubyProjectEncodingQueryImpl;
import org.netbeans.modules.ruby.rubyproject.ui.RubyLogicalViewProvider;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.RubyCompositePanelProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Represents one plain Ruby project.
 * @author Jesse Glick, et al.
 */
public final class RubyProject extends RubyBaseProject {
    
    private static final Icon RUBY_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/ruby/rubyproject/ui/resources/jruby.png", false); // NOI18N

    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    
    RubyProject(final RakeProjectHelper helper) throws IOException {
        super(helper, RubyProjectType.PROJECT_CONFIGURATION_NAMESPACE);
    }

    protected @Override Icon getIcon() {
        return RUBY_PROJECT_ICON;
    }

    public @Override String toString() {
        return "RubyProject[" + FileUtil.getFileDisplayName(getProjectDirectory()) + "]"; // NOI18N
    }

    protected @Override Lookup createLookup(final AuxiliaryConfiguration aux,
            final AuxiliaryProperties auxProperties,
            final ProjectInformation info,
            final ProjectOpenedHook projectOpenedHook) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        Lookup base = Lookups.fixed(new Object[] {
            info,
            aux,
            auxProperties,
            helper.createCacheDirectoryProvider(),
            spp,
            new RubyActionProvider( this, this.updateHelper ),
            new RubyLogicalViewProvider(this, this.updateHelper, evaluator(), refHelper),
            new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(),getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper, this.genFilesHelper),        
            projectOpenedHook,
            new RubySources(this, helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            new RubySharabilityQuery (this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()), //Does not use APH to get/put properties/cfgdata
            new RecommendedTemplatesImpl (this.updateHelper),
            this, // never cast an externally obtained Project to RubyProject - use lookup instead
            new RubyProjectOperations(this),
            new RubyConfigurationProvider(this, RubyCompositePanelProvider.RUN),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            encodingQueryImpl,
            new TemplateAttributesProviderImpl(helper, encodingQueryImpl),
            evaluator(),
            new RubyFileLocator(null, this),
            new RubyCoverageProvider(this),
            new RubyPlatformProvider(evaluator())
        });
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-ruby-rubyproject/Lookup"); //NOI18N
    }
    
    protected @Override void registerClassPath() {
        // register project's classpaths to GlobalPathRegistry
        ClassPathProviderImpl cpProvider = getLookup().lookup(ClassPathProviderImpl.class);
        GlobalPathRegistry.getDefault().register(RubyLanguage.BOOT, cpProvider.getProjectClassPaths(RubyLanguage.BOOT));
        GlobalPathRegistry.getDefault().register(RubyLanguage.SOURCE, cpProvider.getProjectClassPaths(RubyLanguage.SOURCE));
    }
    
    protected @Override void unregisterClassPath() {
        // unregister project's classpaths to GlobalPathRegistry
        ClassPathProviderImpl cpProvider = getLookup().lookup(ClassPathProviderImpl.class);
        //GlobalPathRegistry.getDefault().unregister(RubyLanguage.BOOT, cpProvider.getProjectClassPaths(RubyLanguage.BOOT));
        GlobalPathRegistry.getDefault().unregister(RubyLanguage.SOURCE, cpProvider.getProjectClassPaths(RubyLanguage.SOURCE));
    }

    @Override
    public FileObject[] getSourceRootFiles() {
        return getSourceRoots().getRoots();
    }

    @Override
    public FileObject[] getTestSourceRootFiles() {
        return getTestSourceRoots().getRoots();
    }
   
    /**
     * Returns the source roots of this project
     * @return project's source roots
     */
    public synchronized SourceRoots getSourceRoots() {        
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = new SourceRoots(this.updateHelper, evaluator(), getReferenceHelper(), "source-roots", false, "src.{0}{1}.dir"); //NOI18N
        }
        return this.sourceRoots;
    }
    
    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = new SourceRoots(this.updateHelper, evaluator(), getReferenceHelper(), "test-roots", true, "test.{0}{1}.dir"); //NOI18N
        }
        return this.testRoots;
    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        RecommendedTemplatesImpl (UpdateHelper helper) {
            this.helper = helper;
        }
        
        private final UpdateHelper helper;
        
        // List of primarily supported templates
        
        private static final String[] APPLICATION_TYPES = new String[] { 
            "ruby",         // NOI18N
            "XML",                  // NOI18N
            "simple-files"          // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Ruby/main.rb", // NOI18N
            "Templates/Ruby/test.rb", // NOI18N
            "Templates/Ruby/class.rb", // NOI18N
            "Templates/Ruby/module.rb", // NOI18N
            "Templates/Ruby/suite.rb", // NOI18N
            "Templates/Ruby/rspec.rb", // NOI18N
        };
        
        public String[] getRecommendedTypes() {
            return APPLICATION_TYPES;
        }
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }
    
}
