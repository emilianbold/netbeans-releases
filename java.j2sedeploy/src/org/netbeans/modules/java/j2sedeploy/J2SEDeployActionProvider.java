/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2sedeploy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(
    service = ActionProvider.class,
    projectTypes={@LookupProvider.Registration.ProjectType(id="org-netbeans-modules-java-j2seproject",position=500)})
public class J2SEDeployActionProvider implements ActionProvider {

    public static final String COMMAND_PACKAGE_INSTALLERS = "package-installers"; //NOI18N
    public static final String COMMAND_PACKAGE_IMAGE = "package-image";           //NOI18N
    public static final String COMMAND_PACKAGE_ALL = "package-all";               //NOI18N
    public static final String PROP_NATIVE_BUILDING = "native.bundling.enabled";    //NOI18N

    private static final RequestProcessor RP = new RequestProcessor(J2SEDeployActionProvider.class);


    private final Listener listener;


    public J2SEDeployActionProvider(@NonNull final Project prj) {
        this.listener = new Listener(prj);
    }

    @Override
    public String[] getSupportedActions() {
        return new String[] {
            COMMAND_PACKAGE_ALL,
            COMMAND_PACKAGE_IMAGE,
            COMMAND_PACKAGE_INSTALLERS
        };
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return listener.isEnabled();
    }    

    private static final class Listener implements Runnable, PropertyChangeListener {

        private final Project prj;
        private final RequestProcessor.Task refresh;
        private final AtomicBoolean initialized;
        private volatile Boolean cachedEnabled;

        Listener(@NonNull final Project prj) {
            Parameters.notNull("prj", prj); //NOI18N
            this.prj = prj;
            this.initialized = new AtomicBoolean();
            refresh = RP.create(this);                    
        }

        @Override
        public void run() {
            ProjectManager.mutex().readAccess(new Runnable() {
                @Override
                public void run() {
                    isEnabled();
                }
            });
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || PROP_NATIVE_BUILDING.equals(propName)) {
                cachedEnabled = null;
                refresh.schedule(0);
            }
        }

        boolean isEnabled() {
            Boolean res = cachedEnabled;
            if (res != null) {
                return res;
            }
            final J2SEPropertyEvaluator j2seEval = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
            if (j2seEval == null) {
               cachedEnabled = res = Boolean.FALSE;
            } else {
                final PropertyEvaluator eval = j2seEval.evaluator();
                if (initialized.compareAndSet(false, true)) {
                    eval.addPropertyChangeListener(this);
                }
                cachedEnabled = res = isTrue(eval.getProperty(PROP_NATIVE_BUILDING));
            }
            return res;
        }

        private static boolean isTrue(@NullAllowed String value) {
            return "true".equals(value) ||  //NOI18N
                   "yes".equals(value)  ||  //NOI18N
                   "on".equals(value);      //NOI18N
        }
    }

}
