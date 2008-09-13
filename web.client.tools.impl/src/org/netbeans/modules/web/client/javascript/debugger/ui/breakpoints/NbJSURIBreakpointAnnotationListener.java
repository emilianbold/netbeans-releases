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

package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;

public final class NbJSURIBreakpointAnnotationListener extends NbJSBreakpointAnnotationListener {

    private final List<NbJSURIBreakpoint> uriBreakpoints = new CopyOnWriteArrayList<NbJSURIBreakpoint>();
    private final Map<DebuggerEngine, Map<NbJSURIBreakpoint, Annotation>> engineToBreakpointsToAnnotations = new ConcurrentHashMap<DebuggerEngine, Map<NbJSURIBreakpoint, Annotation>>();
    
    private final Map<Annotation, Breakpoint> lingeringAnnotations = new WeakHashMap<Annotation, Breakpoint>();
    private final List<WeakReference<NbJSBreakpointAnnotation>> nonEngineAnnotations = new ArrayList<WeakReference<NbJSBreakpointAnnotation>>();

    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS, DebuggerManager.PROP_DEBUGGER_ENGINES };
    }

    @Override
    public void breakpointAdded(final Breakpoint b) {
        if (!(b instanceof NbJSURIBreakpoint))
            return;
        NbJSURIBreakpoint uriBp = (NbJSURIBreakpoint) b;
        
        /* Add a breakpoint to all current engine */
        addBreakpointAnnotation((NbJSURIBreakpoint) uriBp);
        
        /* Add this breakpoint to my breakpoints list */
        uriBreakpoints.add(uriBp);
    }

    @Override
    public void breakpointRemoved(final Breakpoint b) {
        if (!(b instanceof NbJSURIBreakpoint))
            return;

        NbJSURIBreakpoint uriBp = (NbJSURIBreakpoint) b;
         
        /* Remove this breakpoint from all engine */
        removeBreakpointAnnotation(uriBp);

        /* remove this breakpoint from my breakpoints list */
        uriBreakpoints.remove(uriBp);
    }

    @Override
    public void engineAdded(DebuggerEngine engine ){
        /* Add all breakpoints to this engine */
        NbJSDebugger debugger = engine.lookupFirst(null, NbJSDebugger.class);
        if( debugger == null ){
            return;
        }
        assert !engineToBreakpointsToAnnotations.containsKey(engine);
        
        /* Add this engine to my engines list */
        engineToBreakpointsToAnnotations.put(engine, new HashMap<NbJSURIBreakpoint,Annotation>());

        /* Add all breakpoints to this session */
        for( NbJSURIBreakpoint uriBreakpoint : uriBreakpoints){
            addBreakpointAnnotation(uriBreakpoint, engine);
        }
        
        super.engineAdded(engine);
    }

    @Override
    public void engineRemoved(DebuggerEngine engine){
        assert engine != null;

        /* Remove the engine */
        Map<NbJSURIBreakpoint, Annotation> map = engineToBreakpointsToAnnotations.get(engine);
        if (map != null) {
            synchronized (lingeringAnnotations) {
                for (Entry<NbJSURIBreakpoint, Annotation> entry : map.entrySet()) {
                    lingeringAnnotations.put(entry.getValue(), entry.getKey());
                }
            }
        }
        engineToBreakpointsToAnnotations.remove(engine);
        
        /* I don't think I need to remove the annotation because it is closing. */
        super.engineRemoved(engine);
    }

    @Override
    protected final void addBreakpointAnnotation(final NbJSBreakpoint b){ 
        assert b instanceof NbJSURIBreakpoint;
        
        // #146102
        boolean addedOwner = false;
        NbJSURIBreakpoint bp = (NbJSURIBreakpoint)b;
        Line line = bp.getOwnerLine();
        DataObject dobj = line != null ? line.getLookup().lookup(DataObject.class) : null;
        FileObject ownerFile = dobj != null ? dobj.getPrimaryFile() : null;
        
        for( DebuggerEngine engine : engineToBreakpointsToAnnotations.keySet()){ 
            addedOwner = addedOwner || addBreakpointAnnotation((NbJSURIBreakpoint)b,engine,ownerFile);
        }
        
        if (!addedOwner && line != null) {
            NbJSBreakpointAnnotation debugAnnotation = new NbJSBreakpointAnnotation(line, b);
            synchronized (nonEngineAnnotations) {
                nonEngineAnnotations.add(new WeakReference<NbJSBreakpointAnnotation>(debugAnnotation));
            }
        }
        
        b.addPropertyChangeListener(getEnableBreakpointPropertyChangeListener());
    }
    
    @Override
    protected final void removeBreakpointAnnotation(final NbJSBreakpoint b){
        assert b instanceof NbJSURIBreakpoint;

        boolean annotationFound = false;

        for( DebuggerEngine engine : engineToBreakpointsToAnnotations.keySet()){ 
            removeBreakpointAnnotation((NbJSURIBreakpoint)b,engine);
            annotationFound = true;
        }
        
        synchronized (nonEngineAnnotations) {
            Iterator<WeakReference<NbJSBreakpointAnnotation>> listIter = nonEngineAnnotations.iterator();
            while (listIter.hasNext()) {
                NbJSBreakpointAnnotation nextAnnotation = listIter.next().get();
                if (nextAnnotation == null) {
                    listIter.remove();
                } else if (nextAnnotation.getBreakpoint() == b) {
                    nextAnnotation.detach();
                    listIter.remove();
                }
            }
        }
        
        synchronized (lingeringAnnotations) {
            if (!annotationFound && lingeringAnnotations.containsValue(b)) {
                Set<Annotation> keysToRemove = new LinkedHashSet<Annotation>();
                for (Entry<Annotation, Breakpoint> entry : lingeringAnnotations.entrySet()) {
                    if (entry.getValue() == b) {
                        Annotation annotation = entry.getKey();
                        if (annotation != null) {
                            annotation.detach();
                        }
                        keysToRemove.add(annotation);
                    }
                }
                
                for (Annotation annotation : keysToRemove) {
                    lingeringAnnotations.remove(annotation);
                }
            }
        }
        assert enableBreakpointPropertyChangeListener != null;
        b.removePropertyChangeListener(enableBreakpointPropertyChangeListener);
    }
    
    private final boolean addBreakpointAnnotation(final NbJSURIBreakpoint b, final DebuggerEngine engine) {
        return addBreakpointAnnotation(b, engine, null);
    }

    /**
     * Adds a breakpoint for a given engine
     * @param b the breakpoint to add
     * @param engine for which to add the breakpoint
     */
    private final boolean addBreakpointAnnotation(final NbJSURIBreakpoint b, final DebuggerEngine engine, final FileObject ownerFile) {
        Line line = b.getLine(engine);
        
        if (line != null) {
            Annotation debugAnnotation = new NbJSBreakpointAnnotation(line, b);
            Map<NbJSURIBreakpoint,Annotation>map = engineToBreakpointsToAnnotations.get(engine);
            map.put(b, debugAnnotation);
            
            return ownerFile != null && b.getFileObject(engine) == ownerFile;
        }
        
        return false;
    }
    
    /**
     * Removes a breakpoint for a given engine
     * @param b the breakpoint to add
     * @param engine for which to add the breakpoint
     */
    private final void removeBreakpointAnnotation(final NbJSURIBreakpoint b, final DebuggerEngine engine) {
        Map<NbJSURIBreakpoint,Annotation>map = engineToBreakpointsToAnnotations.get(engine);
        Annotation annotation = map.remove(b);
        if (annotation == null)
            return;
        annotation.detach();
    }
    


}
