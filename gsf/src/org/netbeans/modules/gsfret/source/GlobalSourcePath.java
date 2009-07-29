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

package org.netbeans.modules.gsfret.source/*.classpath*/;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.api.classpath.GlobalPathRegistry;
import org.netbeans.modules.gsfpath.api.classpath.GlobalPathRegistryEvent;
import org.netbeans.modules.gsfpath.api.classpath.GlobalPathRegistryListener;
import org.netbeans.modules.gsfpath.api.queries.SourceForBinaryQuery;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathImplementation;
import org.netbeans.modules.gsfpath.spi.classpath.PathResourceImplementation;
import org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Tomas Zezula
 */
public class GlobalSourcePath {
    
    public static final String PROP_INCLUDES = ClassPath.PROP_INCLUDES;
    
    private static GlobalSourcePath instance;
    
    private final GlobalPathRegistry gpr;
    private List<? extends PathResourceImplementation> resources;
    private List<? extends PathResourceImplementation> unknownResources;
    private List<? extends PathResourceImplementation> binaryResources;
    private Set<ClassPath> activeCps;
    private Map<URL, SourceForBinaryQuery.Result> sourceResults;
    private Map<URL, URL[]> translatedRoots;
    private Map<URL, WeakValue> unknownRoots;
    private long timeStamp;             //Lamport event ordering
    private Runnable debugCallBack;
    
    private final SourcePathImplementation sourcePath;
    private final BinaryPathImplementation binaryPath;
    private final UnknownSourcePathImplementation unknownSourcePath;
    
    private final Listener listener;
    
    private PropertyChangeListener excludesListener;

    /** Creates a new instance of GlobalSourcePath */
    private GlobalSourcePath() {
        this.listener = new Listener ();
        this.sourcePath = new SourcePathImplementation ();
        this.binaryPath = new BinaryPathImplementation ();
        this.unknownSourcePath = new UnknownSourcePathImplementation ();
        this.timeStamp = -1;
        this.gpr = GlobalPathRegistry.getDefault();
        this.activeCps = Collections.emptySet();
        this.sourceResults = Collections.emptyMap();
        this.unknownRoots = new HashMap<URL, WeakValue>();
        this.translatedRoots = new HashMap<URL, URL[]> ();
        this.gpr.addGlobalPathRegistryListener ((GlobalPathRegistryListener)WeakListeners.create(GlobalPathRegistryListener.class,this.listener,this.gpr));        
    }
    
    
    public void setExcludesListener (final PropertyChangeListener listener) throws TooManyListenersException {
        if (this.excludesListener != null) {
            throw new TooManyListenersException ();
        }
        this.excludesListener=listener;
    }
    
    public URL[] getSourceRootForBinaryRoot (final URL binaryRoot, final ClassPath definingClassPath, final boolean fire) {
        URL[] result = this.translatedRoots.get(binaryRoot);
        if (result != null) {
            if (result.length > 0) {
                return result;
            }
            else {
                return null;
            }
        } 
        else {
            List<URL> cacheRoots = new ArrayList<URL> ();
            Collection<? extends PathResourceImplementation> unknownRes = getSources(SourceForBinaryQuery.findSourceRoots(binaryRoot).getRoots(),cacheRoots,null);
            if (unknownRes.isEmpty()) {
                return null;
            }
            else {                
                result = new URL[cacheRoots.size()];
                synchronized (this) {
                    Iterator<URL> it = cacheRoots.iterator();
                    for (int i=0; it.hasNext(); i++) {
                        result[i] = it.next ();
                        unknownRoots.put(result[i],new WeakValue(definingClassPath,result[i]));
                    }
                }
                if (fire) {
                    this.resetCacheAndFire();
                }
                return result;
            }
        }
    }
    
    public ClassPathImplementation getSourcePath () {
        return this.sourcePath;
    }
    
    public ClassPathImplementation getUnknownSourcePath () {
        return this.unknownSourcePath;
    }
    
    public ClassPathImplementation getBinaryPath () {
        return this.binaryPath;
    }
    
    private void resetCacheAndFire () {
        synchronized (this) {
            this.resources = null;
            this.binaryResources = null;
            this.unknownResources = null;
            this.timeStamp++;
        }
        this.sourcePath.firePropertyChange ();
        this.binaryPath.firePropertyChange ();
        this.unknownSourcePath.firePropertyChange();
    }
    
    private static Result createResources (final Request r) {
        assert r != null;
        Set<PathResourceImplementation> result = new HashSet<PathResourceImplementation> ();
        Set<PathResourceImplementation> unknownResult = new HashSet<PathResourceImplementation> ();
        Set<PathResourceImplementation> binaryResult = new HashSet<PathResourceImplementation> ();
        final Map<URL,URL[]> translatedRoots = new HashMap<URL, URL[]>();
        Set<ClassPath> newCps = new HashSet<ClassPath> ();
        for (ClassPath cp : r.sourceCps) {
            boolean isNew = !r.oldCps.remove(cp);
            for (ClassPath.Entry entry : cp.entries()) {
                try {
                    URL url = entry.getURL ();
                    if (!url.toExternalForm ().endsWith ("/"))
                        url = new URL (url.toExternalForm () + "/");
                    result.add (ClassPathSupport.createResource (url));
                } catch (IOException ioe) {
                    ErrorManager.getDefault ().notify (ioe);
                }
            }
            boolean notContained = newCps.add (cp);
            if (isNew && notContained) {
               cp.addPropertyChangeListener(r.propertyListener);
            }
        }
        // BEGIN TOR MODIFICATIONS
        // NEW: Treat bootCps as a source path, not a binary -  I want to scan
        // directories
        for (ClassPath cp : r.bootCps) {
            boolean isNew = !r.oldCps.remove(cp);
            for (ClassPath.Entry entry : cp.entries()) {
                try {
                    URL url = entry.getURL ();
                    if (!url.toExternalForm ().endsWith ("/"))
                        url = new URL (url.toExternalForm () + "/");
                    result.add (ClassPathSupport.createResource (url));
                } catch (IOException ioe) {
                    ErrorManager.getDefault ().notify (ioe);
                }
            }
            boolean notContained = newCps.add (cp);
            if (isNew && notContained) {
                cp.addPropertyChangeListener(r.propertyListener);
            }
        }
        // END TOR MODIFICATIONS
        Map<URL,SourceForBinaryQuery.Result> newSR = new HashMap<URL,SourceForBinaryQuery.Result> ();
        /*
        for (ClassPath cp : r.bootCps) {
            boolean isNew = !r.oldCps.remove(cp);
            for (ClassPath.Entry entry : cp.entries()) {
                URL url = entry.getURL();
                if (!translatedRoots.containsKey(url)) {
                    SourceForBinaryQuery.Result sr = r.oldSR.remove (url);
                    boolean isNewSR;
                    if (sr == null) {
                        sr = SourceForBinaryQuery.findSourceRoots(url);
                        isNewSR = true;                    
                    }
                    else {
                        isNewSR = false;
                    }    
                    assert !newSR.containsKey(url);
                    newSR.put(url,sr);
                    List<URL> cacheURLs = new ArrayList<URL> ();
                    Collection<? extends PathResourceImplementation> srcRoots = getSources (sr.getRoots(), cacheURLs, r.unknownRoots);
                    if (srcRoots.isEmpty()) {
                        binaryResult.add (ClassPathSupport.createResource(url));
                    }
                    else {
                        result.addAll(srcRoots);
                    }
                    translatedRoots.put(url, cacheURLs.toArray(new URL[cacheURLs.size()]));
                    if (isNewSR) {
                        sr.addChangeListener(r.changeListener);
                    }
                }                
            }
            boolean notContained = newCps.add (cp);
            if (isNew && notContained) {
                cp.addPropertyChangeListener(r.propertyListener);
            }
        }
         */
        
        for (ClassPath cp : r.compileCps) {
            boolean isNew = !r.oldCps.remove(cp);
            for (ClassPath.Entry entry : cp.entries()) {
                URL url = entry.getURL();
                if (!translatedRoots.containsKey(url)) {
                    SourceForBinaryQuery.Result sr = r.oldSR.remove (url);
                    boolean isNewSR;
                    if (sr == null) {
                        sr = SourceForBinaryQuery.findSourceRoots(url);
                        isNewSR = true;                    
                    }
                    else {
                        isNewSR = false;
                    }
                    assert !newSR.containsKey(url);
                    newSR.put(url,sr);
                    List<URL> cacheURLs = new ArrayList<URL> ();
                    Collection<? extends PathResourceImplementation> srcRoots = getSources(sr.getRoots(),cacheURLs, r.unknownRoots);
                    if (srcRoots.isEmpty()) {
                        try {
                            if (!url.toExternalForm ().endsWith ("/"))
                                url = new URL (url.toExternalForm () + "/");
                            binaryResult.add (ClassPathSupport.createResource (url));
                        } catch (IOException ioe) {
                            ErrorManager.getDefault ().notify (ioe);
                        }
                    }
                    else {
                        result.addAll(srcRoots);
                    }
                    translatedRoots.put(url, cacheURLs.toArray(new URL[cacheURLs.size()]));
                    if (isNewSR) {
                        sr.addChangeListener(r.changeListener);
                    }
                }
            }
            boolean notContained = newCps.add (cp);
            if (isNew && notContained) {
                cp.addPropertyChangeListener(r.propertyListener);
            }
        }
        
        for (ClassPath cp : r.oldCps) {
            cp.removePropertyChangeListener(r.propertyListener);
        }
        
        for (Map.Entry<URL,SourceForBinaryQuery.Result> entry : r.oldSR.entrySet()) {
            entry.getValue().removeChangeListener(r.changeListener);
        }                        
        for (URL unknownRoot : r.unknownRoots.keySet()) {
            try {
                if (!unknownRoot.toExternalForm ().endsWith ("/"))
                    unknownRoot = new URL (unknownRoot.toExternalForm () + "/");
                unknownResult.add (ClassPathSupport.createResource (unknownRoot));
            } catch (IOException ioe) {
                ErrorManager.getDefault ().notify (ioe);
            }
        }        
        return new Result (r.timeStamp, new ArrayList<PathResourceImplementation> (result), new ArrayList(binaryResult), new ArrayList<PathResourceImplementation>(unknownResult),
                newCps,newSR,translatedRoots, r.unknownRoots);
    }    
    
    /**
     * Unit test method, used to set a callback which is called
     * form getResources to emulate a race condition.
     * The access to debugCallBack is not synchronized, it should be
     * set before test and unset after test.
     * @param callBack to be called
     *
     **/
    void setDebugCallBack (final Runnable callBack) {
        this.debugCallBack = callBack;
    }
    
    private static Collection <? extends PathResourceImplementation> getSources (final FileObject[] roots, final List<URL> cacheDirs, final Map<URL, WeakValue> unknownRoots) {
        assert roots != null;        
        URL[] urls = new URL[roots.length];
        boolean add = true;
        for (int i=0; i<roots.length; i++) {
            try {
                URL url = roots[i].getURL();
                if (!"file".equals(url.getProtocol())) {     //NOI18N
                    add = false;
                    break;
                }
                urls[i] = url;
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(e);
            }
        }        
        if (add) {
            List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation> (roots.length);
            for (int i=0; i<urls.length; i++) {
                if (cacheDirs != null) {
                    cacheDirs.add (urls[i]);                        
                }
                if (unknownRoots != null) {
                    unknownRoots.remove (urls[i]);
                }
                try {
                    URL url = urls[i];
                    if (!url.toExternalForm ().endsWith ("/"))
                        url = new URL (url.toExternalForm () + "/");
                    result.add (ClassPathSupport.createResource (url));
                } catch (IOException ioe) {
                    ErrorManager.getDefault ().notify (ioe);
                }
            }
            return result;
        }
        return Collections.<PathResourceImplementation>emptySet();
    }
       
    private class WeakValue extends WeakReference<ClassPath> implements Runnable {
        
        private URL key;
        
        public WeakValue (ClassPath ref, URL key) {
            super (ref, Utilities.activeReferenceQueue());
            assert key != null;
            this.key = key;                        
        }                
        
        public void run () {
            boolean fire = false;
            synchronized (GlobalSourcePath.this) {                
                fire = (GlobalSourcePath.this.unknownRoots.remove (key) != null);                
            }
            if (fire) {                
                GlobalSourcePath.this.resetCacheAndFire();
            }
        }        
    }
    
    private long getTimeStamp () {
        return this.timeStamp;        
    }    
    
    private static class Request {
        
        final long timeStamp;
        final Set<ClassPath> sourceCps;
        final Set<ClassPath> bootCps;
        final Set<ClassPath> compileCps;
        final Set<ClassPath> oldCps;
        final Map <URL, SourceForBinaryQuery.Result> oldSR;
        final Map<URL, WeakValue> unknownRoots;
        final PropertyChangeListener propertyListener;
        final ChangeListener changeListener;
        
        public Request (final long timeStamp, final Set<ClassPath> sourceCps, final Set<ClassPath> bootCps, final Set<ClassPath> compileCps,
            final Set<ClassPath> oldCps, final Map <URL, SourceForBinaryQuery.Result> oldSR, final Map<URL, WeakValue> unknownRoots,
            final PropertyChangeListener propertyListener, final ChangeListener changeListener) {
            assert sourceCps != null;
            assert bootCps != null;
            assert compileCps != null;
            assert oldCps != null;
            assert oldSR != null;
            assert unknownRoots != null;
            assert propertyListener != null;
            assert changeListener != null;
            
            this.timeStamp = timeStamp;
            this.sourceCps = sourceCps;
            this.bootCps = bootCps;
            this.compileCps = compileCps;
            this.oldCps = oldCps;
            this.oldSR = oldSR;
            this.unknownRoots = unknownRoots;
            this.propertyListener = propertyListener;
            this.changeListener = changeListener;
        }
    }
    
    private static class Result {
     
        final long timeStamp;
        final List<? extends PathResourceImplementation> resources;
        final List<? extends PathResourceImplementation> binaryResources;
        final List<? extends PathResourceImplementation> unknownResources;
        final Set<ClassPath> newCps;
        final Map<URL, SourceForBinaryQuery.Result> newSR;
        final Map<URL, URL[]> translatedRoots;
        final Map<URL, WeakValue> unknownRoots;
        
        public Result (final long timeStamp, final List<? extends PathResourceImplementation> resources,
            final List<? extends PathResourceImplementation> binaryResources,
            final List<? extends PathResourceImplementation> unknownResources,
            final Set<ClassPath> newCps,
            final Map<URL, SourceForBinaryQuery.Result> newSR, final Map<URL, URL[]> translatedRoots,
            final Map<URL, WeakValue> unknownRoots) {
            assert resources != null;
            assert binaryResources != null;
            assert unknownResources != null;
            assert newCps != null;
            assert newSR  != null;
            assert translatedRoots != null;
            this.timeStamp = timeStamp;
            this.resources = resources;
            this.binaryResources = binaryResources;
            this.unknownResources = unknownResources;
            this.newCps = newCps;
            this.newSR = newSR;
            this.translatedRoots = translatedRoots;
            this.unknownRoots = unknownRoots;
        }
    }
    
    private class SourcePathImplementation implements ClassPathImplementation {
        
        private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
        
    
        public List<? extends PathResourceImplementation> getResources() {
            Request request;
            synchronized (GlobalSourcePath.this) {
                if (GlobalSourcePath.this.resources != null) {
                    return GlobalSourcePath.this.resources;
                }
                request = new Request (
                    GlobalSourcePath.this.getTimeStamp(),
                    GlobalSourcePath.this.gpr.getPaths(ClassPath.SOURCE),
                    GlobalSourcePath.this.gpr.getPaths(ClassPath.BOOT), 
                    GlobalSourcePath.this.gpr.getPaths(ClassPath.COMPILE),
                    new HashSet (GlobalSourcePath.this.activeCps),
                    new HashMap (GlobalSourcePath.this.sourceResults),
                    new HashMap<URL,WeakValue> (GlobalSourcePath.this.unknownRoots),
                    GlobalSourcePath.this.listener,
                    GlobalSourcePath.this.listener);
            }
            Result res = createResources (request);
            if (GlobalSourcePath.this.debugCallBack != null) {
                GlobalSourcePath.this.debugCallBack.run();
            }
            synchronized (this) {            
                if (GlobalSourcePath.this.getTimeStamp() == res.timeStamp) {
                    if (GlobalSourcePath.this.resources == null) {
                        GlobalSourcePath.this.resources = res.resources;
                        GlobalSourcePath.this.binaryResources = res.binaryResources;
                        GlobalSourcePath.this.unknownResources = res.unknownResources;
                        GlobalSourcePath.this.activeCps = res.newCps;
                        GlobalSourcePath.this.sourceResults = res.newSR;
                        GlobalSourcePath.this.translatedRoots = res.translatedRoots;
                        GlobalSourcePath.this.unknownRoots = res.unknownRoots;
                    }
                    return GlobalSourcePath.this.resources;
                }            
                else {
                    return res.resources;
                }
            }
        }
    
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            assert listener != null;
            if (this.listeners == null) {
                this.listeners = new ArrayList<PropertyChangeListener> ();
            }
            this.listeners.add (listener);        
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            assert listener != null;
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove(listener);
        }
        
        void firePropertyChange () {        
            PropertyChangeListener[] _listeners;
            synchronized (this) {
                if (this.listeners == null) {
                    return;
                }
                _listeners = this.listeners.toArray(new PropertyChangeListener[this.listeners.size()]);            
            }
            PropertyChangeEvent event = new PropertyChangeEvent (this,PROP_RESOURCES,null,null);
            for (PropertyChangeListener l : _listeners) {
                l.propertyChange (event);
            }
        }
    }
    
    private class UnknownSourcePathImplementation implements ClassPathImplementation {
        
        private List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener> ();
    
        public List<? extends PathResourceImplementation> getResources() {
            Request request;
            synchronized (GlobalSourcePath.this) {
                if (GlobalSourcePath.this.unknownResources != null) {
                    return GlobalSourcePath.this.unknownResources;
                }
                request = new Request (
                    GlobalSourcePath.this.getTimeStamp(),
                    GlobalSourcePath.this.gpr.getPaths(ClassPath.SOURCE),
                    GlobalSourcePath.this.gpr.getPaths(ClassPath.BOOT), 
                    GlobalSourcePath.this.gpr.getPaths(ClassPath.COMPILE),
                    new HashSet (GlobalSourcePath.this.activeCps),
                    new HashMap (GlobalSourcePath.this.sourceResults),
                    new HashMap<URL, WeakValue> (GlobalSourcePath.this.unknownRoots),
                    GlobalSourcePath.this.listener,
                    GlobalSourcePath.this.listener);
            }
            Result res = createResources (request);
            if (GlobalSourcePath.this.debugCallBack != null) {
                GlobalSourcePath.this.debugCallBack.run();
            }
            synchronized (this) {            
                if (GlobalSourcePath.this.getTimeStamp() == res.timeStamp) {
                    if (GlobalSourcePath.this.binaryResources == null) {
                        GlobalSourcePath.this.resources = res.resources;
                        GlobalSourcePath.this.binaryResources = res.binaryResources;
                        GlobalSourcePath.this.unknownResources = res.unknownResources;
                        GlobalSourcePath.this.activeCps = res.newCps;
                        GlobalSourcePath.this.sourceResults = res.newSR;
                        GlobalSourcePath.this.translatedRoots = res.translatedRoots;                    
                        GlobalSourcePath.this.unknownRoots = res.unknownRoots;
                    }
                    return GlobalSourcePath.this.unknownResources;
                }            
                else {
                    return res.unknownResources;
                }
            }
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            assert listener != null;
            this.listeners.add (listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            assert listener != null;
            this.listeners.remove (listener);
        }
        
        
        void firePropertyChange () {        
            PropertyChangeEvent event = new PropertyChangeEvent (this,PROP_RESOURCES,null,null);
            for (PropertyChangeListener l : this.listeners) {
                l.propertyChange (event);
            }
        }
}
    
    private class BinaryPathImplementation implements ClassPathImplementation {
        private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    
        public List<? extends PathResourceImplementation> getResources() {
            Request request;
            synchronized (GlobalSourcePath.this) {
                if (GlobalSourcePath.this.binaryResources != null) {
                    return GlobalSourcePath.this.binaryResources;
                }
                request = new Request (
                    GlobalSourcePath.this.getTimeStamp(),
                    GlobalSourcePath.this.gpr.getPaths(ClassPath.SOURCE),
                    GlobalSourcePath.this.gpr.getPaths(ClassPath.BOOT), 
                    GlobalSourcePath.this.gpr.getPaths(ClassPath.COMPILE),
                    new HashSet(GlobalSourcePath.this.activeCps),
                    new HashMap(GlobalSourcePath.this.sourceResults),
                    new HashMap<URL, WeakValue> (GlobalSourcePath.this.unknownRoots),
                    GlobalSourcePath.this.listener,
                    GlobalSourcePath.this.listener);
            }
            Result res = createResources (request);
            if (GlobalSourcePath.this.debugCallBack != null) {
                GlobalSourcePath.this.debugCallBack.run();
            }
            synchronized (this) {            
                if (GlobalSourcePath.this.getTimeStamp() == res.timeStamp) {
                    if (GlobalSourcePath.this.binaryResources == null) {
                        GlobalSourcePath.this.resources = res.resources;
                        GlobalSourcePath.this.binaryResources = res.binaryResources;
                        GlobalSourcePath.this.unknownResources = res.unknownResources;
                        GlobalSourcePath.this.activeCps = res.newCps;
                        GlobalSourcePath.this.sourceResults = res.newSR;
                        GlobalSourcePath.this.translatedRoots = res.translatedRoots;                    
                        GlobalSourcePath.this.unknownRoots = res.unknownRoots;
                    }
                    return GlobalSourcePath.this.binaryResources;
                }            
                else {
                    return res.binaryResources;
                }
            }
        }
        
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            assert listener != null;
            if (this.listeners == null) {
                this.listeners = new ArrayList<PropertyChangeListener> ();
            }
            this.listeners.add (listener);        
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            assert listener != null;
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove(listener);
        }
        
        void firePropertyChange () {        
            PropertyChangeListener[] _listeners;
            synchronized (this) {
                if (this.listeners == null) {
                    return;
                }
                _listeners = this.listeners.toArray(new PropertyChangeListener[this.listeners.size()]);            
            }
            PropertyChangeEvent event = new PropertyChangeEvent (this,PROP_RESOURCES,null,null);
            for (PropertyChangeListener l : _listeners) {
                l.propertyChange (event);
            }
        }
    }
    
    
    private class Listener implements GlobalPathRegistryListener, PropertyChangeListener, ChangeListener {
        
            private Object lastPropagationId;
        
            public void pathsAdded(GlobalPathRegistryEvent event) {
                resetCacheAndFire ();
            }

            public void pathsRemoved(GlobalPathRegistryEvent event) {
                resetCacheAndFire ();
            }        

            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (ClassPath.PROP_ENTRIES.equals(propName)) {
                    resetCacheAndFire ();
                }
                else if (ClassPath.PROP_INCLUDES.equals(propName)) {
                    if (excludesListener != null) {
                        final Object newPropagationId = evt.getPropagationId();
                        if (newPropagationId == null || lastPropagationId != newPropagationId) {                                                    
                            PropertyChangeEvent event = new PropertyChangeEvent (this,PROP_INCLUDES,evt.getSource(),evt.getSource());
                            excludesListener.propertyChange(event);
                        }                        
                        lastPropagationId = newPropagationId;
                    }
                }
            }
    
            public void stateChanged (ChangeEvent event) {
                resetCacheAndFire();
            }
    }
    
    public static synchronized GlobalSourcePath getDefault () {
        if (instance == null) {
            instance = new GlobalSourcePath ();
        }
        return instance;
    }
    
}
