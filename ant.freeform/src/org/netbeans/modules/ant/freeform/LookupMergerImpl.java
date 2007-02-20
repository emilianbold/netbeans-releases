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
import java.util.Set;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;

/**
 * Merges ActionProvider 
 *
 * @author David Konecny, Milos Kleint
 */
public class LookupMergerImpl implements LookupMerger<ActionProvider> {

    public LookupMergerImpl() {
    }
    

    public Class<ActionProvider> getMergeableClass() {
        return ActionProvider.class;
    }

    public ActionProvider merge(Lookup lookup) {
        return new ActionProviderImpl(lookup);
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
