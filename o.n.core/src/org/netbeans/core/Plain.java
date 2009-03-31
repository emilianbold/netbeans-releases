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

package org.netbeans.core;

import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.startup.ModuleSystem;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/** Default implementation of TopManager that is used when
* the system is used without initialization.
*
* @author Jaroslav Tulach
*/
public class Plain extends NbTopManager implements Runnable, ChangeListener {

    private final StatusDisplayer status;

    /** Creates new Plain. */
    public Plain() {
        if (Boolean.getBoolean("org.netbeans.core.Plain.CULPRIT")) Thread.dumpStack(); // NOI18N
        status = StatusDisplayer.getDefault();
        status.addChangeListener(this);
    }
  
    private ModuleSystem moduleSystem;
  

    /** Test method to check whether some level of interactivity is enabled.
    * @param il mask composed of the constants of IL_XXXX
    * @return true if such level is enabled
    */
    public boolean isInteractive (int il) {
        return (IL_WINDOWS & il) == IL_WINDOWS;
    }

    /** Prints the stack trace of an exception.
    */
    public void notifyException (Throwable t) {
        t.printStackTrace();
    }

  /** */
  public Object notify (org.openide.NotifyDescriptor nd) {
    new Exception("TopManager.notify()").printStackTrace(); // NOI18N
    System.out.println("MSG = " + nd.getMessage()); // NOI18N
    Object[] options = nd.getOptions();
    System.out.print("("); // NOI18N
    for(int i = 0; i < options.length; i++) {
      if (i != 0) System.out.print(", "); // NOI18N
      System.out.print(options[i]);
    }
    System.out.println(")"); // NOI18N
    return new Object();
  }
  
    /** Create the module system. Subclasses may override. */
    protected ModuleSystem createModuleSystem() throws IOException {
        FileSystem fs = FileUtil.getConfigRoot().getFileSystem();
        return new ModuleSystem(fs);
    }
  
    /** Initializaton of modules if user directory provided.
     */
    public void run() {
        try {
            moduleSystem = createModuleSystem();
        } catch (IOException ioe) {
            notifyException(ioe);
            return;
        }
        moduleSystem.loadBootModules();
        try {
            if (!FileUtil.getConfigRoot().getFileSystem().isReadOnly()) {
                moduleSystem.readList();
                moduleSystem.restore();
                NbLoaderPool.installationFinished();
            } else {
                NbLoaderPool.installationFinished();
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
  
    /** Get the module subsystem.  */
    public ModuleSystem getModuleSystem() {
        return moduleSystem;
    }
  
    public void stateChanged(ChangeEvent e) {
        System.out.println(status.getStatusText());
    }
    
}
