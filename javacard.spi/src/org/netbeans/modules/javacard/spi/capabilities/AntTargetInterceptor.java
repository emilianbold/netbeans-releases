/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.spi.capabilities;

import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javacard.spi.ActionNames;
import org.netbeans.modules.javacard.spi.ICardCapability;

/**
 * Interface which can be available from a Card's Lookup/getCapabiliy() method,
 * which allows for
 * Card implementations to inject properties into an Ant process invoked from
 * the IDE, bypass target invocation altogether, and get notification once
 * a target has been successfully invoked.
 * <p/>
 * Card implementations can use this interface to
 * <ul>
 * <li>Provide properties for the Card as defined in the specification, which
 * may be consumed by Ant tasks.  For the Reference Implementation, there is
 * an actual Properties file on disk which is loaded by the Ant script.  An
 * implementation of an actual card might have no such properties file (since
 * physical devices may need such info computed on the fly), and instead inject
 * properties into the build which are required in order for the deployment or
 * instantiation tasks to function</li>
 * <li>A card may intercept invocation of the Ant task, and completely override
 * the project's action implementation to build the project some other way and
 * deploy whatever is necessary (this will make deployment outside the
 * IDE impossible, but that may be a non-issue for some vendors)
 * </li>
 * <li>
 * Receive notification after a target has been run.
 * </li>
 *
 * @author Tim Boudreau
 */
public interface AntTargetInterceptor extends ICardCapability {
    /**
     * Called before the Ant target in question is invoked via an action
     * on the project.
     * @param p The project
     * @param target The Ant target being invoked
     * @param antProperties A Properties object which can be added to in order
     * to inject properties into the Ant process
     * @return true if invocation of the target should proceed, false to completely
     * override target invocation
     */
    boolean onBeforeInvokeTarget (Project p, AntTarget target, Properties antProperties);
    /**
     * Called after a target has been invoked
     * @param p The project
     * @param target The target
     */
    void onAfterInvokeTarget (Project p, AntTarget target);
}
