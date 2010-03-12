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

package org.netbeans.modules.java.source;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.LuceneIndexMBean;
import org.netbeans.modules.java.source.usages.LuceneIndexMBeanImpl;
import org.netbeans.modules.java.source.util.LowMemoryNotifierMBean;
import org.netbeans.modules.java.source.util.LowMemoryNotifierMBeanImpl;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hrebejk
 * @author Tomas Zezula
 */
public class JBrowseModule extends ModuleInstall {
    
    private static final boolean ENABLE_MBEANS = Boolean.getBoolean("org.netbeans.modules.java.source.enableMBeans");  //NOI18N
    private static final Logger log = Logger.getLogger(JBrowseModule.class.getName());
    
    /** Creates a new instance of JBrowseModule */
    public JBrowseModule() {
    }

    public @Override void restored() {
        super.restored();
        if (ENABLE_MBEANS) {
            registerMBeans();
        }

        //XXX:
        //#143234: javac caches content of all jar files in a static map, which leads to memory leaks affecting the IDE
        //when "internal" execution of javac is used
        //the property below disables the caches
        //java.project might be a better place (currently does not have a ModuleInstall)
        System.setProperty("useJavaUtilZip", "true"); //NOI18N
    }   
    
    public @Override void close () {
        super.close();
        try {
            ClassIndexManager.getDefault().takeWriteLock(new ClassIndexManager.ExceptionAction<Void>() {
                 public @Override Void run() throws IOException {
                     ClassIndexManager.getDefault().close();
                     return null;
                 }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }  
        catch (InterruptedException e) {
            Exceptions.printStackTrace(e);
        }
        if (ENABLE_MBEANS) {
            unregisterMBeans();
        }
    }
    
    private static void registerMBeans() {
        try {
            MBeanServer mgs = ManagementFactory.getPlatformMBeanServer();
            mgs.registerMBean (new LowMemoryNotifierMBeanImpl(), new ObjectName (LowMemoryNotifierMBean.OBJECT_NAME));
            mgs.registerMBean( LuceneIndexMBeanImpl.getDefault(), new ObjectName (LuceneIndexMBean.OBJECT_NAME));
        } catch (NotCompliantMBeanException e) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, e.getMessage(), e);
        }
        catch (MalformedObjectNameException e) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, e.getMessage(), e);
        }
        catch (InstanceAlreadyExistsException e) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, e.getMessage(), e);
        }
        catch (MBeanRegistrationException e) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    private static void unregisterMBeans() {
        try {
            MBeanServer mgs = ManagementFactory.getPlatformMBeanServer();
            mgs.unregisterMBean (new ObjectName (LowMemoryNotifierMBean.OBJECT_NAME));
            mgs.unregisterMBean (new ObjectName (LuceneIndexMBean.OBJECT_NAME));
        } catch (MalformedObjectNameException e) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, e.getMessage(), e);
        }
        catch (InstanceNotFoundException e) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, e.getMessage(), e);
        }
        catch (MBeanRegistrationException e) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
