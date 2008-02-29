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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.test.ide;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import junit.framework.Assert;
import org.netbeans.junit.Log;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class WatchProjects {
    private static Logger LOG = Logger.getLogger(WatchProjects.class.getName());
    
    
    private static Method getProjects;
    private static Method closeProjects;
    private static Object projectManager;
    
    private WatchProjects() {
    }
    
    public static void initialize() throws Exception {
        Log.enableInstances(Logger.getLogger("TIMER"), "Project", Level.FINEST);
        
        final ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        Assert.assertNotNull("Classloader must exists", loader);
        LOG.fine("Classloader: " + loader);
        Class pmClass = Class.forName(
            "org.netbeans.api.project.ui.OpenProjects", false, loader); //NOI18N
        LOG.fine("class: " + pmClass);
        Method getDefault = pmClass.getMethod("getDefault");
        LOG.fine("  getDefault: " + getDefault);
        projectManager = getDefault.invoke(null);
             
        getProjects = pmClass.getMethod("getOpenProjects");
        LOG.fine("getOpenProjects: " + getProjects);
        
        Class projectArray = Class.forName("[Lorg.netbeans.api.project.Project;");
        
        closeProjects = pmClass.getMethod("close", projectArray);
        LOG.fine("getOpenProjects: " + getProjects);
    }
    
    public static void assertProjects() throws Exception {
        closeProjects.invoke(
            projectManager,
            getProjects.invoke(projectManager)
        );
        
        if (System.getProperty("java.version").startsWith("1.5")) {
            // hopefully this hack will be needed just on 1.5
            resetJTreeUIs(Frame.getFrames());
            
            // clear input method memory leak on JDK 1.5
            Class<?> inputMethod = Class.forName("sun.awt.im.InputContext");
            Field f = inputMethod.getDeclaredField("previousInputMethod");
            f.setAccessible(true);
            f.set(null, null);
        }
        
        //System.setProperty("assertgc.paths", "20");
        // disabled due to issue 124038
        Log.assertInstances("Checking if all projects are really garbage collected");
    }
    
    private static void resetJTreeUIs(Component[] arr) {
        for (Component c : arr) {
            if (c instanceof JTree) {
                JTree jt = (JTree)c;
                jt.updateUI();
            }
            if (c instanceof Container) {
                Container o = (Container)c;
                resetJTreeUIs(o.getComponents());
            }
        }
    }
}
