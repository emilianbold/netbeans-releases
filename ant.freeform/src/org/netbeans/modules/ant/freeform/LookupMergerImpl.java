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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;

/**
 * Merges ActionProvider 
 *
 * @author David Konecny, Milos Kleint
 */
public class LookupMergerImpl implements LookupMerger<ActionProvider> {

    private static final Logger LOG = Logger.getLogger(LookupMergerImpl.class.getName());

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
            ActionProvider master = null;
            List<ActionProvider> aps = new ArrayList<ActionProvider>();
            for (ActionProvider ap : lkp.lookupAll(ActionProvider.class)) {
                if (ap instanceof Actions) {
                    assert master == null;
                    master = ap;
                } else {
                    assert ap != this;
                    aps.add(ap);
                }
            }
            assert master != null;
            aps.add(0, master); // #97436: plain Actions takes precedence.
            return aps;
        }

        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            boolean supported = false;
            for (ActionProvider ap : delegates()) {
                if (Arrays.asList(ap.getSupportedActions()).contains(command)) {
                    supported = true;
                    boolean enabled = ap.isActionEnabled(command, context);
                    LOG.log(Level.FINE, "delegate {0} says enabled={1} for {2} in {3}", new Object[] {ap, enabled, command, context});
                    if (enabled) {
                        return true;
                    }
                }
            }
            if (supported) {
                return false;
            } else {
                // Not supported by anyone.
                throw new IllegalArgumentException(command);
            }
        }

        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            for (ActionProvider ap : delegates()) {
                if (Arrays.asList(ap.getSupportedActions()).contains(command) && ap.isActionEnabled(command, context)) {
                    LOG.log(Level.FINE, "delegating {0} on {1} to {2}", new Object[] {command, context, ap});
                    ap.invokeAction(command, context);
                    return;
                }
            }
            throw new IllegalArgumentException(command);
        }

        public String[] getSupportedActions() {
            Set<String> actions = new HashSet<String>();
            Collection<? extends ActionProvider> aps = delegates();
            for (ActionProvider ap : aps) {
                actions.addAll(Arrays.asList(ap.getSupportedActions()));
            }
            LOG.log(Level.FINE, "delegates {0} report supported actions {1}", new Object[] {aps, actions});
            return actions.toArray(new String[actions.size()]);
        }
        
    }
    
}
