/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import org.openide.ErrorManager;

/** Provides basic information required to start executing a class.
*
* @author Ales Novak
//* @deprecated Java-specific API. Please use {@link Executor#execute(org.openide.loaders.DataObject)}.
*             <a href="http://www.netbeans.org/download/dev/javadoc/OpenAPIs/org/openide/doc-files/upgrade.html#3.5i-sep-II-ExecInfo">More info</a>
*/
public class ExecInfo {

    /* TBD
    private static boolean warned = false;
    */

    /** param for execution */
    private String[] argv;
    /** class to exec */
    private String className;

    /** Create a new descriptor.
    * @param className the name of the class to execute
    * @param argv an array of arguments for the class (may be empty but not <code>null</code>)
    */
    public ExecInfo (String className, String[] argv) {
        this.argv = argv;
        this.className = className;
        /* TBD
        if (!warned) {
            warned = true;
            if (ErrorManager.getDefault().isNotifiable(ErrorManager.INFORMATIONAL)) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Throwable("ExecInfo is deprecated - please use Executor.execute(DataObject) instead: http://www.netbeans.org/download/dev/javadoc/OpenAPIs/org/openide/doc-files/upgrade.html#3.5i-sep-II-ExecInfo")); // NOI18N
            }
        }
        */
    }

    /** Create a new descriptor with no arguments.
    * @param className the name of the class to execute
    */
    public ExecInfo (String className) {
        this(className, new String[] {});
    }

    /** Get the arguments (typically passed to <code>main(String[])</code>).
    * @return the arguments (never <code>null</code>)
    */
    public String[] getArguments () {
        return argv;
    }

    /** Get the name of the class to execute.
    * This must typically have a <code>public static void main(String[])</code> method.
    * @return the class name
    */
    public String getClassName () {
        return className;
    }
}
