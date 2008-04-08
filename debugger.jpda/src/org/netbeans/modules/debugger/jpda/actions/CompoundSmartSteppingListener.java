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
package org.netbeans.modules.debugger.jpda.actions;

import java.util.Iterator;
import java.util.List;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback;


/**
 * Loads all different SmartSteppingListeners and delegates to them.
 *
 * @author  Jan Jancura
 */
public class CompoundSmartSteppingListener extends SmartSteppingCallback {


    private List smartSteppings;
    private ContextProvider lookupProvider;
    
    
    private static boolean ssverbose = 
        System.getProperty ("netbeans.debugger.smartstepping") != null;

    
    public CompoundSmartSteppingListener (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        SmartSteppingFilter smartSteppingFilter = lookupProvider.lookupFirst(null, SmartSteppingFilter.class);
        initFilter (smartSteppingFilter);
    }
    
    public void initFilter (SmartSteppingFilter filter) {
        // init list of smart stepping listeners
        smartSteppings = lookupProvider.lookup 
            (null, SmartSteppingCallback.class);
        Iterator i = smartSteppings.iterator ();
        while (i.hasNext ()) {
            SmartSteppingCallback ss = (SmartSteppingCallback) i.next ();
            ss.initFilter (filter);
        }
    }
    
    /**
     * Asks all SmartSteppingListener listeners if executiong should stop on the 
     * current place represented by JPDAThread.
     */
    public boolean stopHere (
        ContextProvider lookupProvider, 
        JPDAThread t, 
        SmartSteppingFilter smartSteppingFilter
    ) {
        if (ssverbose)
            System.out.println("\nSS  CompoundSmartSteppingListener.stopHere? : " + 
                t.getClassName () + '.' +
                t.getMethodName () + ':' +
                t.getLineNumber (null)
            );
        
        Iterator i = smartSteppings.iterator ();
        boolean stop = true;
        while (i.hasNext ()) {
            SmartSteppingCallback ss = (SmartSteppingCallback) i.next ();
            boolean sh = ss.stopHere (lookupProvider, t, smartSteppingFilter);
            stop = stop && sh;
            if (ssverbose)
                System.out.println("SS    " + ss.getClass () + 
                    " = " + sh
                );
        }
        return stop;
    }
}

