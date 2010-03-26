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

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;

import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ClassPrepareEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ClassUnloadEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.ClassPrepareRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.ClassUnloadRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public abstract class ClassBasedBreakpoint extends BreakpointImpl {
    
    private String sourceRoot;
    private final Object SOURCE_ROOT_LOCK = new Object();
    private SourceRootsChangedListener srChListener;
    private PropertyChangeListener weakSrChListener;
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.breakpoints"); // NOI18N

    public ClassBasedBreakpoint (
        JPDABreakpoint breakpoint, 
        JPDADebuggerImpl debugger,
        Session session
    ) {
        super (breakpoint, null, debugger, session);
    }
    
    public ClassBasedBreakpoint (
        JPDABreakpoint breakpoint, 
        BreakpointsReader reader,
        JPDADebuggerImpl debugger,
        Session session
    ) {
        super (breakpoint, reader, debugger, session);
    }
    
    protected final void setSourceRoot(String sourceRoot) {
        synchronized (SOURCE_ROOT_LOCK) {
            this.sourceRoot = sourceRoot;
            if (sourceRoot != null && srChListener == null) {
                srChListener = new SourceRootsChangedListener();
                getDebugger().getEngineContext().addPropertyChangeListener(
                        weakSrChListener = WeakListeners.propertyChange(srChListener,
                                                     getDebugger().getEngineContext()));
            } else if (sourceRoot == null) {
                srChListener = null; // release the listener
            }
        }
    }
    
    protected final String getSourceRoot() {
        synchronized (SOURCE_ROOT_LOCK) {
            return sourceRoot;
        }
    }
    
    @Override
    protected void remove () {
        super.remove();
        synchronized (SOURCE_ROOT_LOCK) {
            if (srChListener != null) {
                getDebugger().getEngineContext().removePropertyChangeListener(weakSrChListener);
                srChListener = null;
            }
        }
    }
    
    @Override
    protected boolean isEnabled() {
        synchronized (SOURCE_ROOT_LOCK) {
            String sourceRoot = getSourceRoot();
            if (sourceRoot == null) {
                return true;
            }
            String[] sourceRoots = getDebugger().getEngineContext().getSourceRoots();
            for (int i = 0; i < sourceRoots.length; i++) {
                if (sourceRoot.equals(sourceRoots[i])) {
                    return true;
                }
            }
            String[] projectSourceRoots = getDebugger().getEngineContext().getProjectSourceRoots();
            for (int i = 0; i < projectSourceRoots.length; i++) {
                if (sourceRoot.equals(projectSourceRoots[i])) {
                    setValidity(VALIDITY.INVALID,
                                NbBundle.getMessage(ClassBasedBreakpoint.class,
                                            "MSG_DisabledSourceRoot",
                                            sourceRoot));
                    return false;
                }
            }
            // Breakpoint is not in debugger's source roots,
            // though it still might get hit if the app loads additional classes...
            return true;
            /*if (logger.isLoggable(Level.FINE)) {
                logger.fine("Breakpoint "+getBreakpoint()+
                            " NOT submitted because it's source root "+sourceRoot+
                            " is not contained in debugger's source roots: "+
                            java.util.Arrays.asList(sourceRoots));
            }
            return false;
             */
        }
    }
    
    /** Check whether the breakpoint belongs to the first matched source root. */
    protected boolean isEnabled(String sourcePath, String[] preferredSourceRoot) {
        synchronized (SOURCE_ROOT_LOCK) {
            String sourceRoot = getSourceRoot();
            if (sourceRoot == null) {
                return true;
            }
            String url = getDebugger().getEngineContext().getURL(sourcePath, true);
            if (url == null) { // In some pathological situations, the source is not found.
                ErrorManager.getDefault().log(ErrorManager.WARNING, "No URL found for source path "+sourcePath);
                return false;
            }
            String urlRoot = getDebugger().getEngineContext().getSourceRoot(url);
            if (urlRoot == null) {
                return true;
            }
            preferredSourceRoot[0] = urlRoot;
            return sourceRoot.equals(urlRoot);
        }
    }
    
    protected void setClassRequests (
        String[] classFilters,
        String[] classExclusionFilters,
        int breakpointType
    ) {
        setClassRequests(classFilters, classExclusionFilters, breakpointType, true);
    }
    
    protected void setClassRequests (
        String[] classFilters,
        String[] classExclusionFilters,
        int breakpointType,
        boolean ignoreHitCountOnClassLoad
    ) {
        try {
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED) != 0
            ) {
                ClassPrepareRequest cpr = EventRequestManagerWrapper.
                        createClassPrepareRequest (getEventRequestManager());
                int i, k = classFilters.length;
                for (i = 0; i < k; i++) {
                    ClassPrepareRequestWrapper.addClassFilter (cpr, classFilters [i]);
                    if (logger.isLoggable(Level.FINE))
                        logger.fine("Set class load request: " + classFilters [i]);
                }
                k = classExclusionFilters.length;
                for (i = 0; i < k; i++) {
                    ClassPrepareRequestWrapper.addClassExclusionFilter (cpr, classExclusionFilters [i]);
                    if (logger.isLoggable(Level.FINE))
                        logger.fine("Set class load exclusion request: " + classExclusionFilters [i]);
                }
                addEventRequest (cpr, ignoreHitCountOnClassLoad);
            }
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED) != 0
            ) {
                ClassUnloadRequest cur = EventRequestManagerWrapper.
                        createClassUnloadRequest (getEventRequestManager());
                int i, k = classFilters.length;
                for (i = 0; i < k; i++) {
                    ClassUnloadRequestWrapper.addClassFilter (cur, classFilters [i]);
                    if (logger.isLoggable(Level.FINE))
                        logger.fine("Set class unload request: " + classFilters [i]);
                }
                k = classExclusionFilters.length;
                for (i = 0; i < k; i++) {
                    ClassUnloadRequestWrapper.addClassExclusionFilter (cur, classExclusionFilters [i]);
                    if (logger.isLoggable(Level.FINE))
                        logger.fine("Set class unload exclusion request: " + classExclusionFilters [i]);
                }
                addEventRequest (cur, false);
            }
        } catch (VMDisconnectedExceptionWrapper e) {
        } catch (InternalExceptionWrapper e) {
        } catch (ObjectCollectedExceptionWrapper e) {
        }
    }
    
    protected boolean checkLoadedClasses (
        String className, String[] classExclusionFilters
    ) {
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return false;
        boolean all = className.startsWith("*") || className.endsWith("*"); // NOI18N
        logger.fine("Check loaded classes: " + className + ", will load all classes: " + all); // NOI18N
        boolean matched = false;
        Iterator i = null;
        if (all) {
            i = VirtualMachineWrapper.allClasses0(vm).iterator ();
        } else {
            i = VirtualMachineWrapper.classesByName0(vm, className).iterator ();
        }
        List<ReferenceType> loadedClasses = null;
        while (i.hasNext ()) {
            ReferenceType referenceType = (ReferenceType) i.next ();
            if (!ReferenceTypeWrapper.isPrepared0(referenceType)) {
                // Ignore not prepared classes, we should receive ClassPrepareEvent later.
                continue;
            }
//                if (verbose)
//                    System.out.println("B     cls: " + referenceType);
            try {
                ClassLoaderReference clref = ReferenceTypeWrapper.classLoader(referenceType);
                if (clref != null && ObjectReferenceWrapper.isCollected(clref)) {
                    // Ignore classes whose class loaders are gone.
                    continue;
                }
                String name = ReferenceTypeWrapper.name (referenceType);
                if (match (name, className)) {
                    boolean excluded = false;
                    if (classExclusionFilters != null) {
                        for (String exFilter : classExclusionFilters) {
                            if (match(name, exFilter)) {
                                excluded = true;
                                break;
                            }
                        }
                    }
                    if (!excluded) {
                        if (logger.isLoggable(Level.FINE))
                            logger.fine(" Class loaded: " + referenceType);
                        if (loadedClasses == null) {
                            loadedClasses = Collections.singletonList(referenceType);
                        } else {
                            if (loadedClasses.size() == 1) {
                                loadedClasses = new ArrayList<ReferenceType>(loadedClasses);
                            }
                            loadedClasses.add(referenceType);
                        }
                        //classLoaded (referenceType);
                        matched = true;
                    }
                }
            } catch (InternalExceptionWrapper e) {
            } catch (VMDisconnectedExceptionWrapper e) {
            } catch (ObjectCollectedExceptionWrapper e) {
            }
        }
        if (loadedClasses != null && loadedClasses.size() > 0) {
            ReferenceType preferredType;
            try {
                preferredType = JPDAUtils.getPreferredReferenceType(loadedClasses, logger);
            } catch (VMDisconnectedExceptionWrapper ex) {
                return false;
            }
            if (preferredType != null) {
                loadedClasses = Collections.singletonList(preferredType);
            }
            classLoaded(loadedClasses);
        }
        return matched;
    }

    public boolean exec (Event event) {
        try {
            if (event instanceof ClassPrepareEvent) {
                if (logger.isLoggable(Level.FINE))
                    logger.fine(" Class loaded: " + ClassPrepareEventWrapper.referenceType((ClassPrepareEvent) event));
                classLoaded(Collections.singletonList(ClassPrepareEventWrapper.referenceType((ClassPrepareEvent) event)));
            } else if (event instanceof ClassUnloadEvent) {
                if (logger.isLoggable(Level.FINE))
                    logger.fine(" Class unloaded: " + ClassUnloadEventWrapper.className((ClassUnloadEvent) event));
                classUnloaded(ClassUnloadEventWrapper.className((ClassUnloadEvent) event));
            }
        } catch (InternalExceptionWrapper ex) {
            return false;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        return true;
    }

    public void removed(EventRequest eventRequest) {
    }
    
    protected void classLoaded (List<ReferenceType> referenceTypes) {}
    protected void classUnloaded (String className) {}
    
    
    private class SourceRootsChangedListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourcePathProvider.PROP_SOURCE_ROOTS.equals(evt.getPropertyName())) {
                update();
            }
        }
        
    }
}

