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
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
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
    public static LookupMerger createRecommendedTemplatesMerger() {
        return new RecommendedMerger();
    }
    
    /**
     * Create a {@link org.netbeans.spi.project.LookupMerger} instance 
     * for {@link org.netbeans.spi.project.ui.PrivilegedTemplates}. Allows to merge 
     * templates from multiple sources. 
     * @return instance to include in project lookup
     */
    public static LookupMerger createPrivilegedTemplatesMerger() {
        return new PrivilegedMerger();
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
    
    private static class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        
        private Lookup lkp;
        
        public PrivilegedTemplatesImpl(Lookup lkp) {
            this.lkp = lkp;
        }
        
        public String[] getPrivilegedTemplates() {
            Set<String> templates = new LinkedHashSet<String>();
            for (PrivilegedTemplates pt : lkp.lookupAll(PrivilegedTemplates.class)) {
                templates.addAll(Arrays.asList(pt.getPrivilegedTemplates()));
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
                templates.addAll(Arrays.asList(pt.getRecommendedTypes()));
            }
            return templates.toArray(new String[templates.size()]);
        }
        
    }
    
    
}
