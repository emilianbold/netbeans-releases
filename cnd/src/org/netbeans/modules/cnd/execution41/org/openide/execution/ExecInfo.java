/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
