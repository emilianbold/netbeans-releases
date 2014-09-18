/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.core.output2;

import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.io.base.BaseIOProvider;
import org.openide.io.base.BaseInputOutput;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider of I/O instances for the org.openide.io.base API.
 *
 * @author jhavlin
 */
@ServiceProvider(service = BaseIOProvider.class, position = 100)
public class NbIOProviderBase extends BaseIOProvider {

    private static final Logger LOG = Logger.getLogger(
            NbIOProviderBase.class.getName());

    @Override
    public String getName() {
        return SharedProvider.getInstance().getName();
    }

    @Override
    public BaseInputOutput getIO(String name, boolean newIO,
            Lookup context, EventListener... actions) {

        Action[] actionArray = convertActions(actions);
        return SharedProvider.getInstance().getIO(name, newIO, actionArray,
                null);
    }

    private Action[] convertActions(EventListener[] actions) {
        Action[] actionArray = new Action[actions.length];
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] instanceof Action) {
                actionArray[i] = (Action) actions[i];
            } else if (actions[i] == null) {
                LOG.log(Level.WARNING, "Null action",                   //NOI18N
                        new IllegalArgumentException());
                return new Action[0];
            } else {
                LOG.log(Level.WARNING, "Only actions of type javax."    //NOI18N
                        + "swing.Action are supported, but object of "  //NOI18N
                        + "type " + actions[i].getClass() + " found",   //NOI18N
                        new IllegalArgumentException());
                return new Action[0];
            }
        }
        return actionArray;
    }

    @Override
    public boolean isActionTypeSupported(
            Class<? extends EventListener> cls) {
        return Action.class.isAssignableFrom(cls);
    }
}
