/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.spi.project.ui.support;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.util.Lookup;

/**
 * Factory class for creation of {@link org.netbeans.spi.project.LookupMerger} instances.
 * @author mkleint
 * @since org.netbeans.modules.projectuiapi 1.19
 */
public final class UILookupMergerSupport {
    
    /** Creates a new instance of LookupMergerSupport */
    private UILookupMergerSupport() {
    }
    
    /**
     * Create a {@link org.netbeans.spi.project.LookupMerger} instance 
     * for {@link org.netbeans.spi.project.ui.RecommendedTemplates}. Allows to merge 
     * templates from multiple sources. 
     * @return instance to include in project lookup
     */
    public static LookupMerger<RecommendedTemplates> createRecommendedTemplatesMerger() {
        return new RecommendedMerger();
    }
    
    /**
     * Create a {@link org.netbeans.spi.project.LookupMerger} instance 
     * for {@link org.netbeans.spi.project.ui.PrivilegedTemplates}. Allows to merge 
     * templates from multiple sources. 
     * @return instance to include in project lookup
     */
    public static LookupMerger<PrivilegedTemplates> createPrivilegedTemplatesMerger() {
        return new PrivilegedMerger();
    }
    
    /**
     * Create a {@link org.netbeans.spi.project.LookupMerger} instance 
     * for {@link org.netbeans.spi.project.ui.ProjectOpenedHook}. The merger makes sure all registered
     * <code>ProjectOpenedHook</code> instances are called and that the default instance is called first.
     * @param defaultInstance - the default {@link org.netbeans.spi.project.ui.ProjectOpenedHook} instance or null if
     * a default privileged instance is not required.
     * @return instance to include in project lookup
     * @since org.netbeans.modules.projectuiapi 1.24
     */
    public static LookupMerger<ProjectOpenedHook> createProjectOpenHookMerger(ProjectOpenedHook defaultInstance) {
        return new OpenMerger(defaultInstance);
    }
    
    private static class PrivilegedMerger implements LookupMerger<PrivilegedTemplates> {
        public Class<PrivilegedTemplates> getMergeableClass() {
            return PrivilegedTemplates.class;
        }

        public PrivilegedTemplates merge(Lookup lookup) {
            return new PrivilegedTemplatesImpl(lookup);
        }
    }
    
    private static class RecommendedMerger implements LookupMerger<RecommendedTemplates> {
        
        public Class<RecommendedTemplates> getMergeableClass() {
            return RecommendedTemplates.class;
        }

        public RecommendedTemplates merge(Lookup lookup) {
            return new RecommendedTemplatesImpl(lookup);
        }
    }
    
    private static class OpenMerger implements LookupMerger<ProjectOpenedHook> {
        private ProjectOpenedHook defaultInstance;

        OpenMerger(ProjectOpenedHook def) {
            defaultInstance = def;
        }
        public Class<ProjectOpenedHook> getMergeableClass() {
            return ProjectOpenedHook.class;
        }

        public ProjectOpenedHook merge(Lookup lookup) {
            return new OpenHookImpl(defaultInstance, lookup);
        }
        
    }
    
    private static class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        
        private Lookup lkp;
        
        public PrivilegedTemplatesImpl(Lookup lkp) {
            this.lkp = lkp;
        }
        
        public String[] getPrivilegedTemplates() {
            Set<String> templates = new LinkedHashSet<String>();
            for (PrivilegedTemplates pt : lkp.lookupAll(PrivilegedTemplates.class)) {
                String[] temp = pt.getPrivilegedTemplates();
                if (temp == null) {
                    throw new IllegalStateException(pt.getClass().getName() + " returns null from getPrivilegedTemplates() method."); //NOI18N
                }
                templates.addAll(Arrays.asList(temp));
            }
            return templates.toArray(new String[templates.size()]);
        }
    }
    
    private static class RecommendedTemplatesImpl implements RecommendedTemplates {
        
        private Lookup lkp;
        
        public RecommendedTemplatesImpl(Lookup lkp) {
            this.lkp = lkp;
        }
        
        public String[] getRecommendedTypes() {
            Set<String> templates = new LinkedHashSet<String>();
            for (RecommendedTemplates pt : lkp.lookupAll(RecommendedTemplates.class)) {
                String[] temp = pt.getRecommendedTypes();
                if (temp == null) {
                    throw new IllegalStateException(pt.getClass().getName() + " returns null from getRecommendedTemplates() method."); //NOI18N
                }
                templates.addAll(Arrays.asList(temp));
            }
            return templates.toArray(new String[templates.size()]);
        }
        
    }
    
    private static class OpenHookImpl extends ProjectOpenedHook {

        private ProjectOpenedHook defaultInstance;
        private Lookup lkp;        
        
        OpenHookImpl(ProjectOpenedHook def, Lookup lkp) {
            defaultInstance = def;
            this.lkp = lkp; 
            //shall we listen on ProjectOpenedHook instance changes in lookup and 
            // call close on the disappearing ones?
        }
        
        protected void projectOpened() {
            if (defaultInstance != null) {
                ProjectOpenedTrampoline.DEFAULT.projectOpened(defaultInstance);
            }
            for (ProjectOpenedHook poh : lkp.lookupAll(ProjectOpenedHook.class)) {
                // just to make sure..
                if (poh != defaultInstance) {
                    ProjectOpenedTrampoline.DEFAULT.projectOpened(poh);
                }
            }
        }

        protected void projectClosed() {
            if (defaultInstance != null) {
                ProjectOpenedTrampoline.DEFAULT.projectClosed(defaultInstance);
            }
            for (ProjectOpenedHook poh : lkp.lookupAll(ProjectOpenedHook.class)) {
                // just to make sure..
                if (poh != defaultInstance) {
                    ProjectOpenedTrampoline.DEFAULT.projectClosed(poh);
                }
            }
        }
        
    }
    
    
}
