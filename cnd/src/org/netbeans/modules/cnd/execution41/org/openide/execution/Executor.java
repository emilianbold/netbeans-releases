/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.execution41.org.openide.execution;

import java.io.IOException;
import java.util.*;

import org.openide.execution.ExecutorTask;

import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.WeakSet;

/** Implements the execution of a class.
 * There may be several different types of executors installed in the system,
 * some of which may only be appropriate for certain types of objects
 * (e.g., applets or servlets).
 * The two standard ones, both assuming a main method (i.e. a standalone Java program),
 * are {@link ThreadExecutor} (internal execution)
 * and {@link ProcessExecutor} (external execution).
 * <p>This class <em>currently</em> has a property editor in the default IDE property
 * editor search path.
 * <p>Override {@link #execute(DataObject)}.
 * @author Jaroslav Tulach
 */
public abstract class Executor {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -5089771565951633752L;

    /** Execute a class given by name with some arguments in this executor.
     * @param info information describing what to execute
     * @return a task object that can be used to control the running process
     * @exception IOException if the execution cannot be started (class is missing, etc.)
    //* @deprecated This method is a relic of Java-specific execution. New <code>Executor</code>
    //*             implementations are encouraged to override {@link #execute(DataObject)} to be the actual implementation.
    //*             <a href="http://www.netbeans.org/download/dev/javadoc/OpenAPIs/org/openide/doc-files/upgrade.html#3.5i-sep-II-ExecInfo">More info</a>
     */
    public ExecutorTask execute(ExecInfo info) throws IOException {
        throw new IOException("ExecInfo is deprecated"); // NOI18N
    }
    private final static Set warnedClasses = new WeakSet(); // Set<Class>

    /** Executes a dataobject.
     * The default implementation should not be used; treat this method
     * as abstract.
     * @param obj object to execute
     * @return a task object that can be used to control the running process
     * @exception IOException if the execution cannot be started (class is missing, etc.)
     */
    public ExecutorTask execute(DataObject obj) throws IOException {
        throw new IOException("No longer works"); // NOI18N
        /*
    Class c = getClass();
    synchronized (warnedClasses) {
    if (warnedClasses.add(c)) {
    ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning - " + c.getName() + " should have overridden execute(DataObject); falling back on deprecated ExecInfo usage; see: http://www.netbeans.org/download/dev/javadoc/OpenAPIs/org/openide/doc-files/upgrade.html#3.5i-sep-II-ExecInfo");
    }
    }
    String[] params;
    ArgumentsCookie ac = (ArgumentsCookie) obj.getCookie(ArgumentsCookie.class);
    if (ac != null) {
    params = ac.getArguments();
    } else {
    params = new String[0];
    }
    return execute(new ExecInfo(obj.getPrimaryFile().getPackageName ('.'), params));
     */
    }

    /** Instruct the execution engine whether
     * the process might need I/O communication with the user.
     * If I/O is needed, a tab in the output window may be opened for the process;
     * otherwise the output is discarded and reads will fail.
     * <p>The default implementation returns <code>true</code>.
     * @return <code>true</code> if the process needs I/O
     */
    public boolean needsIO() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(Executor.class);
    }

    /** Get all registered executors in the system's execution engine.
     * @return enumeration of <code>Executor</code>s
    //* @deprecated Please use {@link org.openide.util.Lookup} instead.
     */
    public static Enumeration executors() {
        return Collections.enumeration(Lookup.getDefault().lookupAll(Executor.class));
    }

    /** Find the
     * executor implemented as a given class, among the executors registered to the
     * execution engine.
     * <P>
     * This should be used during (de-)serialization
     * of the specific executor for a data object: only store its class name
     * and then try to find the executor implemented by that class later.
     *
     * @param clazz the class of the executor looked for
     * @return the desired executor or <code>null</code> if it does not exist
    //* @deprecated Please use {@link org.openide.util.Lookup} instead.
     */
    public static Executor find(Class<? extends Executor> clazz) {
        return (Executor) Lookup.getDefault().lookup(clazz);
    }

    /** Find the
     * executor with requested name, among the executors registered to the
     * execution engine.
     * <P>
     * This should be used during (de-)serialization
     * of the specific executor for a data object: only store its name
     * and then try to find the executor later.
     *
     * @param name (display) name of executor to find
     * @return the desired executor or <code>null</code> if it does not exist
     */
    public static Executor find(String name) {
        return null;
    }

    /** Get the default executor for the system's execution engine.
     * <p>You may actually want {@link org.openide.loaders.ExecutionSupport#getExecutor}.
     * @return the default executor
    //* @deprecated The notion of a default executor for all file types is probably meaningless.
     */
    public static Executor getDefault() {
        Enumeration en = executors();
        return en.hasMoreElements() ? (Executor) en.nextElement() : NoExecutor.getInstance();
    }
}
