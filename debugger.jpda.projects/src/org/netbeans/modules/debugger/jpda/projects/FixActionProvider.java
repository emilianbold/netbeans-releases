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

package org.netbeans.modules.debugger.jpda.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
*
* @author   Jan Jancura
*/
public class FixActionProvider extends ActionsProviderSupport {

    private JPDADebugger debugger;
    private SourcePathProviderImpl sp;
    private Listener listener;
    private boolean isFixCommandSupported;
    private static final RequestProcessor hotFixRP = new RequestProcessor("Java Debugger HotFix", 1);
    
    
    public FixActionProvider (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        sp = (SourcePathProviderImpl) lookupProvider.lookupFirst(null, SourcePathProvider.class);
        
        listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener (listener);
        debugger.addPropertyChangeListener (JPDADebugger.PROP_STATE, listener);
        //debugger.addPropertyChangeListener ("classesToReload", listener);
        ClassesToReload.getInstance().addPropertyChangeListener(listener);
        EditorContextDispatcher.getDefault().addPropertyChangeListener("text/x-java", listener);
        
        setEnabled (
            ActionsManager.ACTION_FIX,
            shouldBeEnabled ()
        );
    }

    private void destroy () {
        debugger.removePropertyChangeListener (JPDADebugger.PROP_STATE, listener);
        //debugger.removePropertyChangeListener ("classesToReload", listener);
        ClassesToReload.getInstance().removePropertyChangeListener(listener);
        MainProjectManager.getDefault ().removePropertyChangeListener (listener);
        EditorContextDispatcher.getDefault().removePropertyChangeListener (listener);
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_FIX);
    }
    
    public void doAction (Object action) {
        if (!isFixCommandSupported) {
            Map<String, FileObject> classes = ClassesToReload.getInstance().popClassesToReload(debugger, sp.getSourceRootsFO());
            reloadClasses(debugger, classes);
            //applyClassesToReload(getCurrentProject());
            return ;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        invokeAction();
                    }
                });
            } catch (InterruptedException iex) {
                // Procceed
            } catch (java.lang.reflect.InvocationTargetException itex) {
                ErrorManager.getDefault().notify(itex);
            }
        } else {
            invokeAction();
        }
    }

    private void invokeAction() {
        ((ActionProvider) getCurrentProject().getLookup ().lookup (
                ActionProvider.class
            )).invokeAction (
                JavaProjectConstants.COMMAND_DEBUG_FIX, 
                getLookup ()
            );
    }

    /**
     * Returns the project that the active node's fileobject belongs to. 
     * If this cannot be determined for some reason, returns the main project.
     *  
     * @return the project that the active node's fileobject belongs to
     */ 
    private Project getCurrentProject() {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        if (nodes == null || nodes.length == 0) return MainProjectManager.getDefault().getMainProject();
        DataObject dao = (DataObject) nodes[0].getCookie(DataObject.class);
        if (dao == null || !dao.isValid()) {
            return MainProjectManager.getDefault().getMainProject();
        }
        return FileOwnerQuery.getOwner(dao.getPrimaryFile());        
    }
    
    private boolean shouldBeEnabled () {
        // check if current debugger supports this action
        if (!debugger.canFixClasses()) return false;
        // check if current project supports this action
        isFixCommandSupported = false;
        Project p = getCurrentProject();
        if (p != null) {
            ActionProvider actionProvider = (ActionProvider) p.getLookup ().
                lookup (ActionProvider.class);
            if (actionProvider != null) {
                String[] sa = actionProvider.getSupportedActions ();
                int i, k = sa.length;
                for (i = 0; i < k; i++) {
                    if (JavaProjectConstants.COMMAND_DEBUG_FIX.equals (sa [i])) {
                        break;
                    }
                }
                isFixCommandSupported = i < k &&
                        actionProvider.isActionEnabled(JavaProjectConstants.COMMAND_DEBUG_FIX, getLookup());
            }
        }
        if (!isFixCommandSupported) {
            // No fix command, let's see whether we have some changed classes to reload:
            return ClassesToReload.getInstance().hasClassesToReload(debugger, sp.getSourceRootsFO());
            /*Sources sources = ProjectUtils.getSources(p);
            SourceGroup[] srcGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (SourceGroup srcGroup : srcGroups) {
                FileObject src = srcGroup.getRootFolder();
                if (hasClassesToReload(debugger, src)) {
                    return true;
                }
            }
            return false;
             */
        } else {
            return true;
        }
    }
    
    private Lookup getLookup () {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        int i, k = nodes.length;
        ArrayList l = new ArrayList ();
        for (i = 0; i < k; i++) {
            DataObject dobj = (DataObject)nodes [i].getCookie (DataObject.class);
            if (dobj != null && dobj.isValid())
                l.add (dobj);
        }
        return Lookups.fixed (l.toArray (new DataObject [l.size ()]));
    }
    
    static void reloadClasses(final JPDADebugger debugger, Map<String, FileObject> classes) {
        final Map map = new HashMap();
        for (String className : classes.keySet()) {
            FileObject fo = classes.get(className);
            InputStream is = null;
            try {
                is = fo.getInputStream();
                long fileSize = fo.getSize();
                byte[] bytecode = new byte[(int) fileSize];
                is.read(bytecode);
                map.put(className,
                        bytecode);
                System.out.println(" " + className);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        if (map.size() == 0) {
            //System.out.println(" No class to reload");
            return ;
        }

        hotFixRP.post(new Runnable() {
            public void run() {
                String error = null;
                try {
                    debugger.fixClasses(map);
                } catch (UnsupportedOperationException uoex) {
                    error = NbBundle.getMessage(SourcePathProviderImpl.class, "MSG_FixUnsupported", uoex.getLocalizedMessage());
                } catch (NoClassDefFoundError ncdfex) {
                    error = NbBundle.getMessage(SourcePathProviderImpl.class, "MSG_FixMismatch", ncdfex.getLocalizedMessage());
                } catch (VerifyError ver) {
                    error = NbBundle.getMessage(SourcePathProviderImpl.class, "MSG_FixVerifierProblems", ver.getLocalizedMessage());
                } catch (UnsupportedClassVersionError ucver) {
                    error = NbBundle.getMessage(SourcePathProviderImpl.class, "MSG_FixUnsupportedVersion", ucver.getLocalizedMessage());
                } catch (ClassFormatError cfer) {
                    error = NbBundle.getMessage(SourcePathProviderImpl.class, "MSG_FixNotValid", cfer.getLocalizedMessage());
                } catch (ClassCircularityError ccer) {
                    error = NbBundle.getMessage(SourcePathProviderImpl.class, "MSG_FixCircularity", ccer.getLocalizedMessage());
                } catch (RuntimeException vmdisc) {
                //} catch (VMDisconnectedException vmdisc) {
                    if ("com.sun.jdi.VMDisconnectedException".equals(vmdisc.getClass().getName())) {
                        //BuildArtifactMapper.removeArtifactsUpdatedListener(url, ArtifactsUpdatedImpl.this);
                        return ;
                    } else {
                        throw vmdisc;
                    }
                }
                if (error != null) {
                    notifyError(error);
                } else {
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(SourcePathProviderImpl.class, "MSG_FixSuccess"));
                }
            }
        });
    }

    static void notifyError(String error) {
        NotifyDescriptor nd = new NotifyDescriptor.Message(error, NotifyDescriptor.Message.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(nd);
        StatusDisplayer.getDefault().setStatusText(error);
    }

    private class Listener implements PropertyChangeListener, 
    DebuggerManagerListener {
        public Listener () {}
        
        public void propertyChange (PropertyChangeEvent e) {
            boolean en = shouldBeEnabled ();
            setEnabled (
                ActionsManager.ACTION_FIX,
                en
            );
            if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) 
                destroy ();
        }
        public void sessionRemoved (Session session) {}
        public void breakpointAdded (Breakpoint breakpoint) {}
        public void breakpointRemoved (Breakpoint breakpoint) {}
        public Breakpoint[] initBreakpoints () {return new Breakpoint [0];}
        public void initWatches () {}
        public void sessionAdded (Session session) {}
        public void watchAdded (Watch watch) {}
        public void watchRemoved (Watch watch) {}
        public void engineAdded (DebuggerEngine engine) {}
        public void engineRemoved (DebuggerEngine engine) {}
    }


    public static class ClassesToReload {

        private static ClassesToReload instance;

        // debugger -> src root FileObject -> class name -> class FileObject
        private Map<JPDADebugger, Map<FileObject, Map<String, FileObject>>> classesByDebugger =
                new WeakHashMap<JPDADebugger, Map<FileObject, Map<String, FileObject>>>();
        private PropertyChangeSupport pch = new PropertyChangeSupport(this);

        private ClassesToReload() {}

        public static synchronized ClassesToReload getInstance() {
            if (instance == null) {
                instance = new ClassesToReload();
            }
            return instance;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            pch.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pch.removePropertyChangeListener(l);
        }

        public void addClassToReload(JPDADebugger debugger, FileObject src,
                                     String className, FileObject fo) {
            synchronized (this) {
                Map<FileObject, Map<String, FileObject>> srcRoots = classesByDebugger.get(debugger);
                if (srcRoots == null) {
                    srcRoots = new HashMap<FileObject, Map<String, FileObject>>();
                    classesByDebugger.put(debugger, srcRoots);
                }
                Map<String, FileObject> classes = srcRoots.get(src);
                if (classes == null) {
                    classes = new HashMap<String, FileObject>();
                    srcRoots.put(src, classes);
                }
                classes.put(className, fo);
            }
            pch.firePropertyChange("classesToReload", null, className);
        }

        public synchronized boolean hasClassesToReload(JPDADebugger debugger, Set<FileObject> enabledSourceRoots) {
            Map<FileObject, Map<String, FileObject>> srcRoots = classesByDebugger.get(debugger);
            if (srcRoots != null) {
                for (FileObject src : srcRoots.keySet()) {
                    if (enabledSourceRoots.contains(src)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Map<String, FileObject> popClassesToReload(JPDADebugger debugger, Set<FileObject> enabledSourceRoots) {
            Map<String, FileObject> classes = new HashMap<String, FileObject>();
            synchronized (this) {
                Map<FileObject, Map<String, FileObject>> srcRoots = classesByDebugger.get(debugger);
                if (srcRoots != null) {
                    Set<FileObject> sourceRoots = new HashSet<FileObject>(srcRoots.keySet());
                    for (FileObject src : sourceRoots) {
                        if (enabledSourceRoots.contains(src)) {
                            classes.putAll(srcRoots.remove(src));
                        }
                    }
                }
            }
            if (classes.size() > 0) {
                pch.firePropertyChange("classesToReload", null, null);
            }
            return classes;
        }

    }
}
