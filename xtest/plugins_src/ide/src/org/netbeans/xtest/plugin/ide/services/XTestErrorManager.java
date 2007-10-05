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
package org.netbeans.xtest.plugin.ide.services;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.openide.*;
import java.util.TreeMap;
import java.util.WeakHashMap;

/**
 * When there is thrown exception from ide the test should fail. This class is made for notify
 * TestCaseResult listener about thrown exception in ide. The funcionality is easily enabled by property of
 * xtest (xtest.ide.error.manager).
 *
 * @author  pzajac, Jiri.Skrivanek@sun.com
 */
public class XTestErrorManager extends ErrorManager {
    /** not yet proccessed exceptions */
    private static ArrayList/*Throwable*/ exceptions = new ArrayList();
    /** maps Throwables to max severity from all annotations */
    private static final Map mapSeverity = new WeakHashMap();
    
    /** all instances of error manager
     */
    private TreeMap instances = new TreeMap();
    
    /** Creates a new instance of XTestErrorManager */
    public XTestErrorManager() {
    }

    /** Maps throwable to max severity. */
    public synchronized Throwable annotate (
        Throwable t,
        int severity, String message, String localizedMessage,
        Throwable stackTrace, java.util.Date date
    ) {
        Object o = mapSeverity.get(t);
        if(o == null) {
            // initial value
            mapSeverity.put(t, new Integer(severity));
        } else {
            // update max value when current severity > stored
            if(severity > ((Integer)o).intValue()) {
                o = new Integer(severity);
            }
        }
        return t;
    }
    
    /** Returns max severity annotated to given throwable. */
    private int getMaxSeverity(int severity, Throwable t) {
        Object o = mapSeverity.get(t);
        if(o != null) {
            int max = ((Integer)o).intValue();
            if(max > severity) {
                return max;
            }
        }
        return severity;
    }

    public org.openide.ErrorManager.Annotation[] findAnnotations(Throwable t) {
        return null;
    }

    public org.openide.ErrorManager getInstance(java.lang.String name) {
        ErrorManager erm = (ErrorManager)instances.get(name);
        if (erm == null) {
            // create new instance
            erm = new XTestErrorManager();
            instances.put(name,erm);
        }
        return erm; 
    }

    public void log(int severity, String s) {
        // ignored
        // these messages are logged to IDE log
    }

    /** It uses the same logic as in org.openide.ErrorManager.DelegatingErrorManager.
     * Test whether a messages with given severity will be logged in advance.
     * Can be used to avoid the construction of complicated and expensive
     * logging messages.
     * @param severity the severity to check, e.g. {@link #EXCEPTION}
     * @return <code>false</code> if the next call to {@link #log(int,String)} with this severity will
     *    discard the message
    */
    public boolean isLoggable(int severity) {
        return severity > INFORMATIONAL;
    }

    public void notify(int severity, Throwable t) {
        severity = getMaxSeverity(severity, t);
        // log only ERROR, EXCEPTION or UNKNOWN severity
        if(severity == ERROR || severity == EXCEPTION || severity == UNKNOWN) {
            // add the exception to exceptions queue of NbTestCase
            exceptions.add(t);
        }
    }

    public Throwable attachAnnotations(Throwable t, org.openide.ErrorManager.Annotation[] arr) {
        return t;
    }

    /** @returns thrown exceptions  
     */
    public static Collection/*Throwable*/ getExceptions() {
        return exceptions;
    }
    
    /** clears container of thrown exceptions
     * this method is called after the test has finished.
     */
    public static void clearExceptions () {
        exceptions.clear();
    }
}
