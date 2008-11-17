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
package org.netbeans.modules.cnd.execution41.org.openide.actions;

import org.openide.ErrorManager;

import java.lang.reflect.Method;
import java.util.*;

import org.netbeans.modules.cnd.execution41.org.openide.cookies.ExecCookie;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

/** Execute a class.
 * Is enabled if the only selected node implements
 * {@link ExecCookie}.
 * @see org.openide.execution
 *
 * @author   Ian Formanek, Jaroslav Tulach, Jan Jancura
 */
public class ExecuteAction extends CookieAction {

    /** should we run compilation before execution */
    private static boolean runCompilation;

    /** Set whether files should be compiled before execution.
     * @param run <code>true</code> if they should
     * @deprecated Only works if the <code>org.openide.compiler</code> module is enabled.
     */
    @Deprecated
    public static void setRunCompilation(boolean run) {
        runCompilation = run;
    }

    /** Test whether files will be compiled before execution.
     * By default they will.
     * @return <code>true</code> if they will be
     * @deprecated Only works if the <code>org.openide.compiler</code> module is enabled.
     */
    @Deprecated
    public static boolean getRunCompilation() {
        return runCompilation;
    }

    // init ..........................................................................................
    protected Class[] cookieClasses() {
        return new Class[]{ExecCookie.class};
    }

    protected void performAction(final Node[] activatedNodes) {
        // Running ExecCookie should be fast. But running compilation before
        // may take a long time. To be safe, do it asynch.
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                execute(activatedNodes, runCompilation);
            }
        });
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    protected int mode() {
        return MODE_ANY;
    }

    public String getName() {
        return NbBundle.getMessage(ExecuteAction.class, "Execute"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ExecuteAction.class);
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/execute.gif"; // NOI18N
    }

    // utility methods
    /** Execute a list of items by cookie.
     *
     * @param execCookies list of {@link ExecCookie}s (any may be <code>null</code>)
     */
    public static void execute(Iterator<ExecCookie> execCookies) {
        while (execCookies.hasNext()) {
            ExecCookie cookie = execCookies.next();
            if (cookie != null) {
                cookie.start();
            }
        }
    }

    /** Execute some data objects.
     *
     * @param dataObjects the data objects (should have {@link ExecCookie} on them if they are to be used)
     * @param compileBefore <code>true</code> to compile before executing
     * @return true if compilation succeeded or was not performed, false if compilation failed
     */
    public static boolean execute(DataObject[] dataObjects, boolean compileBefore) {
        // search all DataObjects with unique ExecCookies/StartCookies -
        // - it is possible, that multiple activated nodes have the same exec cookie and
        // we have to prevent running it multiple times
        Set<ExecCookie> execute = new HashSet<ExecCookie>();

        for (int i = 0; i < dataObjects.length; i++) {
            ExecCookie exec = dataObjects[i].getCookie(ExecCookie.class);
            if (exec != null) {
                execute.add(exec);
            }
        }
        // compile
        if (compileBefore && !compile("compileDataObjects", dataObjects)) { // NOI18N
            return false;
        }

        // execute
        execute(execute.iterator());
        return true;
    }

    /** Execute some nodes.
     *
     * @param nodes the nodes (should have {@link ExecCookie} on them if they are to be used)
     * @param compileBefore <code>true</code> to compile before executing
     * @return true if compilation succeeded or was not performed, false if compilation failed
     */
    public static boolean execute(Node[] nodes, boolean compileBefore) {
        // find all activatedNodes with unique ExecCookies/StartCookies -
        // - it is possible, that multiple activated nodes have the same exec cookie and
        // we have to prevent running it multiple times
        Set<ExecCookie> execute = new HashSet<ExecCookie>();

        for (int i = 0; i < nodes.length; i++) {
            ExecCookie exec = nodes[i].getCookie(ExecCookie.class);
            if (exec != null) {
                execute.add(exec);
            }
        }

        // compile
        if (compileBefore && !compile("compileNodes", nodes)) { // NOI18N
            return false;
        }

        // execute
        execute(execute.iterator());
        return true;
    }

    private static boolean compile(String methodName, Object[] args) {
        try {
            Class c = (Lookup.getDefault().lookup(ClassLoader.class)).loadClass("org.openide.actions.AbstractCompileAction"); // NOI18N
            Method m = c.getDeclaredMethod(methodName, new Class[]{args.getClass()});
            return ((Boolean) m.invoke(null, new Object[]{args})).booleanValue();
        } catch (ClassNotFoundException e) {
            // Failed, but not implausible.
            return false;
        } catch (Exception e) {
            // Something else wrong.
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return false;
        }
    }
}
