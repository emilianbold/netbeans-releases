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

package org.netbeans.modules.debugger.jpda;

import java.util.ArrayList;
import java.util.List;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * Utility calls of methods defined in JDK 1.6 and newer, through reflection.
 * 
 * @author Martin Entlicher
 */
public class Java6Methods {
    
    private static final boolean IS_JDK_16 = !System.getProperty("java.version").startsWith("1.5"); // NOI18N
    
    /** Creates a new instance of Java6Methods */
    private Java6Methods() {
    }
    
    public static boolean isJDK6() {
        return IS_JDK_16;
    }
    
    public static long[] instanceCounts(VirtualMachine vm, List<ReferenceType> refTypes) {
        try {
            java.lang.reflect.Method method = VirtualMachine.class.getMethod("instanceCounts", new Class[] { List.class });
            Object instanceCounts = method.invoke(vm, new Object[] { refTypes });
            return (long[]) instanceCounts;
        } catch (Exception ex) {
            Logger.getLogger(Java6Methods.class.getName()).log(Level.INFO, "", ex);
        }
        return new long[refTypes.size()];
    }
    
    public static List<ObjectReference> instances(ReferenceType refType, long maxInstances) {
        try {
            java.lang.reflect.Method method = ReferenceType.class.getMethod("instances", new Class[] { Long.TYPE });
            Object instances = method.invoke(refType, new Object[] { maxInstances });
            return (List<ObjectReference>) instances;
        } catch (Exception ex) {
            Logger.getLogger(Java6Methods.class.getName()).log(Level.INFO, "", ex);
        }
        return new ArrayList<ObjectReference>();
    }
    
    public static List<ObjectReference> referringObjects(ObjectReference ref, long maxReferrers) {
        
        try {
            java.lang.reflect.Method method = ObjectReference.class.getMethod("referringObjects", new Class[] { Long.TYPE });
            Object referringObjects = method.invoke(ref, new Object[] { maxReferrers });
            return (List<ObjectReference>) referringObjects;
        } catch (Exception ex) {
            Logger.getLogger(Java6Methods.class.getName()).log(Level.INFO, "", ex);
        }
        return new ArrayList<ObjectReference>();
    }

    public static boolean canRequestMonitorEvents(VirtualMachine vm) {
        try {
            java.lang.reflect.Method method = VirtualMachine.class.getMethod("canRequestMonitorEvents", new Class[] {});
            Boolean can = (Boolean) method.invoke(vm, new Object[] {});
            return can;
        } catch (Exception ex) {
            Logger.getLogger(Java6Methods.class.getName()).log(Level.INFO, "", ex);
            return false;
        }
    }

    public static EventRequest createMonitorContendedEnteredRequest(EventRequestManager erm) {
        try {
            java.lang.reflect.Method method = EventRequestManager.class.getMethod("createMonitorContendedEnteredRequest", new Class[] {});
            EventRequest request = (EventRequest) method.invoke(erm, new Object[] {});
            return request;
        } catch (Exception ex) {
            Logger.getLogger(Java6Methods.class.getName()).log(Level.INFO, "", ex);
            return null;
        }
    }

    public static void addThreadFilter2MonitorContendedEnteredRequest(EventRequest monitorContendedEnteredRequest, ThreadReference thread) {
        try {
            Class monitorContendedEnteredRequestClass = Lookup.getDefault().lookup(ClassLoader.class).loadClass("com.sun.jdi.request.MonitorContendedEnteredRequest");
            java.lang.reflect.Method method = monitorContendedEnteredRequestClass.getMethod("addThreadFilter", ThreadReference.class);
            method.invoke(monitorContendedEnteredRequest, thread);
        } catch (Exception ex) {
            Logger.getLogger(Java6Methods.class.getName()).log(Level.INFO, "", ex);
        }
    }
    
}
