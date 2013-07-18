/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.glassfish.tools.ide.data.TaskEvent;
import org.glassfish.tools.ide.admin.TaskState;
import org.glassfish.tools.ide.admin.TaskStateListener;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.openide.util.NbBundle;

/**
 * Basic common functionality of commands execution.
 * <p/>
 * @author Peter Williams, Tomas Kraus
 */
public abstract class BasicTask<V> implements Callable<V> {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Wait duration (ms) between server status checks. */
    public static final int DELAY = 250;
    
    /** Maximum amount of time (in ms) to wait for server to start. */
    public static final int START_TIMEOUT = 300000;
    
    /** Maximum amount of time (in ms) to wait for server to stop. */
    public static final int STOP_TIMEOUT = 180000;

    /** Unit (ms) for the DELAY and START_TIMEOUT constants. */
    public static final TimeUnit TIMEUNIT = TimeUnit.MILLISECONDS;

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish instance accessed in this task. */
    GlassfishInstance instance;

    /** Callback to retrieve state changes. */
    protected TaskStateListener [] stateListener;

    /** Name of GlassFish instance accessed in this task. */
    protected String instanceName;

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Command execution is implemented as <code>call()</code> method in child
     * classes.
     * <p/>
     * @return Command execution result.
     */
    @Override
    public abstract V call();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of <code>BasicTask</code> class.
     * <p/>
     * @param instance GlassFish instance accessed in this task.
     * @param stateListener Callback listeners used to retrieve state changes.
     */
    public BasicTask(GlassfishInstance instance, TaskStateListener... stateListener) {
        this.instance = instance;
        this.stateListener = stateListener;
        this.instanceName = instance.getProperty(
                GlassfishModule.DISPLAY_NAME_ATTR);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Call all registered callback listeners to inform about state change.
     * <p/>
     * @param stateType New state of current command execution sent
     *        to listeners. This value will be returned by this method.
     * @param resName Name of the resource to look for message.
     * @param args Additional arguments passed to message.
     * @return Passed new state of current command.
     */
    protected final TaskState fireOperationStateChanged(
            TaskState stateType, TaskEvent te, String resName, String... args) {
        if(stateListener != null && stateListener.length > 0) {
            String msg = NbBundle.getMessage(BasicTask.class, resName, args);
            for(int i = 0; i < stateListener.length; i++) {
                if(stateListener[i] != null) {
                    stateListener[i].operationStateChanged(stateType, te, msg);
                }
            }
        }
        return stateType;
    }
}
