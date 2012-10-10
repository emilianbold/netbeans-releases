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

package org.netbeans.modules.web.project;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Lookup;

/**
 * Create a simple instance of LookupMerger for ActionProvider. It takes all
 * implementations it finds in the provided lookup and iterates them until a
 * result is found.
 *
 * @author Martin Janicek
 */
@LookupMerger.Registration(
    projectType = {
        "org-netbeans-modules-web-project"
    }
)
public class WebProjectLookupMerger implements LookupMerger<ActionProvider> {

    @Override
    public Class<ActionProvider> getMergeableClass() {
        return ActionProvider.class;
    }

    @Override
    public ActionProvider merge(Lookup lookup) {
        return new ActionProviderMerger(lookup);
    }

    private static class ActionProviderMerger implements ActionProvider {

        private final Lookup lookup;

        public ActionProviderMerger(Lookup lookup) {
            this.lookup = lookup;
        }

        /*
         * Merges all supported actions from all providers.
         * Does not preserve order of actions.
         */
        @Override
        public String[] getSupportedActions() {
            Set<String> resultSet = new HashSet<String>();
            for (ActionProvider impl : lookup.lookupAll(ActionProvider.class)) {
                resultSet.addAll(Arrays.asList(impl.getSupportedActions()));
            }
            return resultSet.toArray(new String[resultSet.size()]);
        }

        /*
         * Iterates over all providers and calls invokeAction on the first found
         * provider that has this action enabled.
         */
        @Override
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            for (ActionProvider impl : lookup.lookupAll(ActionProvider.class)) {
                if (impl.isActionEnabled(command, context)) {
                    impl.invokeAction(command, context);
                    return;
                }
            }
        }

        /*
         * Returns true if at least one provider returns true.
         */
        @Override
        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            for (ActionProvider impl : lookup.lookupAll(ActionProvider.class)) {
                if (impl.isActionEnabled(command, context)) {
                    return true;
                }
            }
            return false;
        }
    }
}
