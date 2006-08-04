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

package org.netbeans.modules.ant.freeform;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.modules.ant.freeform.spi.LookupMerger;
import org.netbeans.spi.project.ActionProvider;
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
    
    public Class<?>[] getMergeableClasses() {
        return new Class<?>[] {
            PrivilegedTemplates.class,
            ActionProvider.class,
        };
    }
    
    public Object merge(Lookup lookup, Class<?> clazz) throws IllegalArgumentException {
        if (clazz == PrivilegedTemplates.class) {
            return new PrivilegedTemplatesImpl(lookup);
        } else if (clazz == ActionProvider.class) {
            return new ActionProviderImpl(lookup);
        } else {
            throw new IllegalArgumentException("merging of " + clazz + " is not supported"); // NOI18N
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

    /**
     * Permits any nature to add actions to the project.
     */
    private static class ActionProviderImpl implements ActionProvider {
        
        private final Lookup lkp;
        
        public ActionProviderImpl(Lookup lkp) {
            this.lkp = lkp;
        }
        
        private Collection<? extends ActionProvider> delegates() {
            Collection<? extends ActionProvider> all = lkp.lookupAll(ActionProvider.class);
            assert !all.contains(this) : all;
            return all;
        }
        
        // XXX delegate directly to single impl if only one

        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            boolean supported = false;
            for (ActionProvider ap : delegates()) {
                if (Arrays.asList(ap.getSupportedActions()).contains(command)) {
                    return ap.isActionEnabled(command, context);
                }
            }
            // Not supported by anyone.
            throw new IllegalArgumentException(command);
        }

        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            for (ActionProvider ap : delegates()) {
                if (Arrays.asList(ap.getSupportedActions()).contains(command)) {
                    ap.invokeAction(command, context);
                    return;
                }
            }
            throw new IllegalArgumentException(command);
        }

        public String[] getSupportedActions() {
            Set<String> actions = new HashSet<String>();
            for (ActionProvider ap : delegates()) {
                actions.addAll(Arrays.asList(ap.getSupportedActions()));
            }
            return actions.toArray(new String[actions.size()]);
        }
        
    }
    
}
