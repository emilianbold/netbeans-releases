/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.netbeans.modules.ant.freeform.spi.LookupMerger;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.openide.util.Lookup;

/**
 * Merges PrivilegedTemplates - all unique templates are returned.
 * Order is undefined - depends on the lookup.
 *
 * @author David Konecny
 */
public class LookupMergerImpl implements LookupMerger {

    public LookupMergerImpl() {}
    
    public Class[] getMergeableClasses() {
        return new Class[]{PrivilegedTemplates.class};
    }
    
    public Object merge(Lookup lookup, Class clazz) {
        if (clazz == PrivilegedTemplates.class) {
            return new PrivilegedTemplatesImpl(lookup);
        }
        throw new IllegalArgumentException("merging of "+clazz+" is not supported"); // NOI18N
    }
    
    private static class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        
        private Lookup lkp;
        
        public PrivilegedTemplatesImpl(Lookup lkp) {
            this.lkp = lkp;
        }
        
        public String[] getPrivilegedTemplates() {
            LinkedHashSet templates = new LinkedHashSet();
            Iterator it = lkp.lookup(new Lookup.Template(PrivilegedTemplates.class)).allInstances().iterator();
            while (it.hasNext()) {
                PrivilegedTemplates pt = (PrivilegedTemplates)it.next();
                templates.addAll(Arrays.asList(pt.getPrivilegedTemplates()));
            }
            return (String[])templates.toArray(new String[templates.size()]);
        }
        
    }
    
}
