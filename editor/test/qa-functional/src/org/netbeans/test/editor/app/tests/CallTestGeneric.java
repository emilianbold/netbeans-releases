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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.test.editor.app.tests;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.AllPermission;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import org.netbeans.test.editor.app.Main;

import org.netbeans.test.editor.app.util.WriterOutputStream;
//import org.openide.execution.NbClassLoader;
import org.openide.filesystems.Repository;


public class CallTestGeneric {

    private static final boolean debug = true;
    
    private PrintWriter log = null;
    
    protected void log(String what) {
        if (log != null) {
            log.println(what);
            log.flush();
        } else {
            System.err.println(what);
            System.err.flush();
        }
    }
    
    public void runTest(String[] args, final PrintWriter log, final PrintWriter ref) throws Exception {
        PrintStream oout = System.out;
        PrintStream oerr = System.err;
        
        try {
            if (debug)
                System.err.println("Testing internal execution!");
            if (Repository.getDefault() == null) {
                throw new IllegalStateException("Repository.getDefault() == null, probably not internal execution.");
            }
            
            if (debug)
                System.err.println("Redirecting System.err and System.out.");
            System.setErr(new PrintStream(new WriterOutputStream(log)));
            System.setOut(new PrintStream(new WriterOutputStream(ref)));
            
            System.err.println("Before trying to execute:");
            Main.main(args);
/*            NbClassLoader cl = new NbClassLoader();
            System.err.println("Class loader parent: "+cl.getParent());
            PermissionCollection pcoll = new Permissions();
            pcoll.add(new AllPermission());
            cl.setDefaultPermissions(pcoll);
 
            Class callTest = cl.loadClass("org.netbeans.test.editor.app.Main");
            Method method = callTest.getMethod("main", new Class[] {args.getClass()});
 
            System.err.println(method.getReturnType());
 
            Object obj = method.invoke(null, new Object[] {args});*/
        } catch (Exception e) {
            e.printStackTrace(log);
            throw e;
        } finally {
            System.err.flush();
            System.out.flush();
            System.setOut(oout);
            System.setErr(oerr);
        }
    }
    
    public static final void main(String[] args) throws Exception {
        String[] arguments = new String[] {
            "/org/netbeans/test/editor/app/tests/javadoc_test.xml",
            "Javadoc_writting.Common_Java_settings",
            "/org/netbeans/test/editor/app/tests/javadoc_test.xml",
            "Javadoc_writting.JavaDoc_inside",
        };
        PrintWriter log = new PrintWriter(System.err);
        PrintWriter ref = new PrintWriter(System.out);
        
        new CallTestGeneric().runTest(arguments, log, ref);
        
        log.close();
        ref.close();
    }
}
