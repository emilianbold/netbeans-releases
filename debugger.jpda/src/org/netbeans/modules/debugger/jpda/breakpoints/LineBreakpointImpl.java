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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.InternalException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.BreakpointRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;



/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class LineBreakpointImpl extends ClassBasedBreakpoint {
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.breakpoints"); // NOI18N
    
    private int                 lineNumber;
    private int                 lineNumberForUpdate = -1;
    private BreakpointsReader   reader;
    
    
    public LineBreakpointImpl (
        LineBreakpoint breakpoint, 
        BreakpointsReader reader,
        JPDADebuggerImpl debugger,
        Session session,
        SourcePath sourcePath
    ) {
        super (breakpoint, reader, debugger, session);
        this.reader = reader;
        updateLineNumber();
        setSourceRoot(sourcePath.getSourceRoot(breakpoint.getURL()));
        set ();
    }

    private synchronized void updateLineNumber() {
        // int line = getBreakpoint().getLineNumber();
        // We need to retrieve the original line number which is associated
        // with the start of this session.
        lineNumber = EditorContextBridge.getContext().getLineNumber(
                getBreakpoint(),
                getDebugger());
   }

    @Override
    protected LineBreakpoint getBreakpoint() {
        return (LineBreakpoint) super.getBreakpoint();
    }
    
    @Override
    void fixed () {
        logger.fine("LineBreakpoint fixed: "+this);
        updateLineNumber();
        super.fixed ();
    }

    @Override
    protected boolean isApplicable() {
        LineBreakpoint breakpoint = getBreakpoint();
        String[] preferredSourceRoot = new String[] { null };
        String sourcePath = getDebugger().getEngineContext().getRelativePath(breakpoint.getURL(), '/', true);
        if (sourcePath == null) {
            return false;
        }
        boolean isInSources = false;
        {
            String srcRoot = getSourceRoot();
            if (srcRoot != null) {
                String[] sourceRoots = getDebugger().getEngineContext().getSourceRoots();
                for (int i = 0; i < sourceRoots.length; i++) {
                    if (srcRoot.equals(sourceRoots[i])) {
                        isInSources = true;
                    }
                }
            }
        }
        // Test if className exists in project sources:
        if (!isInSources) {
            return false;
        }
        if (isInSources && !isEnabled(sourcePath, preferredSourceRoot)) {
            return false;
        }
        return true;
    }
    
    protected void setRequests () {
        LineBreakpoint breakpoint = getBreakpoint();
        updateLineNumber();
        String[] preferredSourceRoot = new String[] { null };
        String sourcePath = getDebugger().getEngineContext().getRelativePath(breakpoint.getURL(), '/', true);
        if (sourcePath == null) {
            String reason = NbBundle.getMessage(LineBreakpointImpl.class,
                                                "MSG_NoSourceRoot",
                                                breakpoint.getURL());
            setInvalid(reason);
            return ;
        }
        String className = breakpoint.getPreferredClassName();
        if (className == null) {
            className = reader.findCachedClassName(breakpoint);
            if (className == null) {
                className = EditorContextBridge.getContext().getClassName (
                    breakpoint.getURL (), 
                    lineNumber
                );
                if (className != null && className.length() > 0) {
                    reader.storeCachedClassName(breakpoint, className);
                }
            }
        }
        if (className == null || className.length() == 0) {
            logger.warning("Class name not defined for breakpoint "+breakpoint);
            setValidity(Breakpoint.VALIDITY.INVALID, NbBundle.getMessage(LineBreakpointImpl.class, "MSG_NoBPClass"));
            return ;
        }

        boolean isInSources = false;
        {
            String srcRoot = getSourceRoot();
            if (srcRoot != null) {
                String[] sourceRoots = getDebugger().getEngineContext().getSourceRoots();
                for (int i = 0; i < sourceRoots.length; i++) {
                    if (srcRoot.equals(sourceRoots[i])) {
                        isInSources = true;
                    }
                }
            }
        }
        // Test if className exists in project sources:
        if (!isInSources && classExistsInSources(className, getDebugger().getEngineContext().getProjectSourceRoots())) {
            logger.fine("LineBreakpoint "+breakpoint+" NOT submitted, URL "+breakpoint.getURL()+" not in sources, but class "+className+" exist in sources.");
            return ;
        }
        if (isInSources && !isEnabled(sourcePath, preferredSourceRoot)) {
            String reason = NbBundle.getMessage(LineBreakpointImpl.class,
                                                "MSG_DifferentPrefferedSourceRoot",
                                                preferredSourceRoot[0]);
            setInvalid(reason);
            logger.fine("LineBreakpoint "+breakpoint+" NOT submitted, because of '"+reason+"'.");
            return ;
        }
        logger.fine("LineBreakpoint "+breakpoint+" - setting request for "+className);
        setClassRequests (
            new String[] {
                className // The class name is correct even for inner classes now
            }, 
            new String [0],
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
        );
        checkLoadedClasses (className, null);
    }

    private void setInvalid(String reason) {
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "Unable to submit line breakpoint to "+getBreakpoint().getURL()+
                " at line "+lineNumber+", reason: "+reason);
        setValidity(Breakpoint.VALIDITY.INVALID, reason);
    }

    private static boolean classExistsInSources(final String className, String[] projectSourceRoots) {
        /*
        ClassIndexManager cim = ClassIndexManager.getDefault();
        List<FileObject> sourcePaths = new ArrayList<FileObject>(projectSourceRoots.length);
        for (String sr : projectSourceRoots) {
            FileObject fo = getFileObject(sr);
            if (fo != null) {
                sourcePaths.add(fo);
                ClassIndexImpl ci;
                try {
                    ci = cim.getUsagesQuery(fo.getURL());
                    if (ci != null) {
                        String sourceName = ci.getSourceName(className);
                        if (sourceName != null) {
                            return true;
                        }
                    }
                } catch (FileStateInvalidException ex) {
                    continue;
                } catch (java.io.IOException ioex) {
                    continue;
                }
            }
        }
        return false;
         */
        List<FileObject> sourcePaths = new ArrayList<FileObject>(projectSourceRoots.length);
        for (String sr : projectSourceRoots) {
            FileObject fo = getFileObject(sr);
            if (fo != null) {
                sourcePaths.add(fo);
            }
        }
        ClassPath cp = ClassPathSupport.createClassPath(sourcePaths.toArray(new FileObject[0]));
        ClassPathSupport.createClassPath(new FileObject[] {});
        ClasspathInfo cpInfo = ClasspathInfo.create(ClassPathSupport.createClassPath(new FileObject[] {}),
                                                    ClassPathSupport.createClassPath(new FileObject[] {}),
                                                    cp);
        //ClassIndex ci = cpInfo.getClassIndex();
        JavaSource js = JavaSource.create(cpInfo);
        final boolean[] found = new boolean[] { false };
        try {
            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController cc) throws Exception {
                    TypeElement te = cc.getElements().getTypeElement(className);
                    if (te != null) { // found
                        found[0] = true;
                    }
                }
            }, false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return found[0];
        /*
        SourceUtils.getFile(null, null);
        ClasspathInfo.create(null, null, cp);

        cp = org.netbeans.modules.java.source.classpath.SourcePath.create(cp, true);
        try {
            ClassLoader cl = cp.getClassLoader(true);
            FileObject fo = cp.findResource(className.replace('.', '/').concat(".class"));
            Class c = cl.loadClass(className);
            System.err.println("classExistsInSources("+className+"): fo = "+fo+", class = "+c);
            return c != null;
        } catch (ClassNotFoundException ex) {
            return false;
        }
        */
    }

    /**
     * Returns FileObject for given String.
     */
    private static FileObject getFileObject (String file) {
        File f = new File (file);
        FileObject fo = FileUtil.toFileObject (f);
        String path = null;
        if (fo == null && file.contains("!/")) {
            int index = file.indexOf("!/");
            f = new File(file.substring(0, index));
            fo = FileUtil.toFileObject (f);
            path = file.substring(index + "!/".length());
        }
        if (fo != null && FileUtil.isArchiveFile (fo)) {
            fo = FileUtil.getArchiveRoot (fo);
            if (path !=null) {
                fo = fo.getFileObject(path);
            }
        }
        return fo;
    }

    @Override
    protected synchronized void classLoaded (List<ReferenceType> referenceTypes) {
        LineBreakpoint breakpoint = getBreakpoint();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Classes "+referenceTypes+" loaded for breakpoint "+breakpoint);
        }
        boolean submitted = false;
        String failReason = null;
        ReferenceType noLocRefType = null;
        int lineNumberToSet = lineNumber;
        String currFailReason = null;

        // if there is no location available, find correct line candidate and run the body again
        for (int counter = 0; counter < 2; counter++) {
            for (ReferenceType referenceType : referenceTypes) {
                String[] reason = new String[] { null };
                boolean[] isNoLocReason = new boolean[1];
                List locations = getLocations (
                    referenceType,
                    breakpoint.getStratum (),
                    breakpoint.getSourceName (),
                    breakpoint.getSourcePath(),
                    lineNumberToSet,
                    reason,
                    isNoLocReason
                );
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Locations in "+referenceType+" are: "+locations+", reason = '"+reason[0]);//+"', HAVE PARENT = "+haveParent);
                }
                if (locations.isEmpty()) {
                    failReason = reason[0];
                    if (isNoLocReason[0]) {
                        noLocRefType = referenceType;
                    }
                    continue;
                }
                for (Iterator it = locations.iterator(); it.hasNext();) {
                    Location location = (Location)it.next();
                    try {
                        BreakpointRequest br = EventRequestManagerWrapper.
                            createBreakpointRequest (getEventRequestManager (), location);
                        setFilters(br);
                        addEventRequest (br);
                        submitted = true;
                        //System.out.println("Breakpoint " + br + location + "created");
                    } catch (VMDisconnectedExceptionWrapper e) {
                    } catch (InternalExceptionWrapper e) {
                    } catch (ObjectCollectedExceptionWrapper e) {
                    }
                }
            } // for
            if (counter == 0) {
                if (!submitted && noLocRefType != null) {
                    int newLineNumber = findBreakableLine(breakpoint.getURL(), lineNumber);
                    if (newLineNumber != lineNumber && newLineNumber >= 0 &&
                            findBreakpoint(breakpoint.getURL(), newLineNumber) == null) {
                        lineNumberToSet = newLineNumber;
                        currFailReason = failReason;
                        failReason = null;
                        continue;
                    }
                }
                break;
            } else { // counter == 1
                if (!submitted) {
                    // we failed to find nearest location, roll back to values from the first run
                    failReason = currFailReason;
                }
            }
        } // for
        if (submitted) {
            if (lineNumber != lineNumberToSet) {
                lineNumberForUpdate = lineNumberToSet;
                breakpoint.setLineNumber(lineNumberToSet);
            }
            setValidity(Breakpoint.VALIDITY.VALID, failReason); // failReason is != null for partially submitted breakpoints (to some classes only)
        } else {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Unable to submit line breakpoint to "+referenceTypes.get(0).name()+
                    " at line "+lineNumber+", reason: "+failReason);
            setValidity(Breakpoint.VALIDITY.INVALID, failReason);
        }
    }
    
    protected EventRequest createEventRequest(EventRequest oldRequest) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        Location location = BreakpointRequestWrapper.location((BreakpointRequest) oldRequest);
        BreakpointRequest br = EventRequestManagerWrapper.createBreakpointRequest(getEventRequestManager(), location);
        setFilters(br);
        return br;
    }
    
    private void setFilters(BreakpointRequest br) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        JPDAThread[] threadFilters = getBreakpoint().getThreadFilters(getDebugger());
        if (threadFilters != null && threadFilters.length > 0) {
            for (JPDAThread t : threadFilters) {
                BreakpointRequestWrapper.addThreadFilter(br, ((JPDAThreadImpl) t).getThreadReference());
            }
        }
        ObjectVariable[] varFilters = getBreakpoint().getInstanceFilters(getDebugger());
        if (varFilters != null && varFilters.length > 0) {
            for (ObjectVariable v : varFilters) {
                BreakpointRequestWrapper.addInstanceFilter(br, (ObjectReference) ((JDIVariable) v).getJDIValue());
            }
        }
    }

    public boolean processCondition(Event event) {
        if (event instanceof BreakpointEvent) {
            try {
                return processCondition(event, getBreakpoint().getCondition (),
                        LocatableEventWrapper.thread((BreakpointEvent) event), null);
            } catch (InternalExceptionWrapper ex) {
                return true;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return true;
            }
        } else {
            return true; // Empty condition, always satisfied.
        }
    }

    @Override
    public boolean exec (Event event) {
        if (event instanceof BreakpointEvent) {
            try {
                return perform (
                    event,
                    LocatableEventWrapper.thread((BreakpointEvent) event),
                    LocationWrapper.declaringType(LocatableWrapper.location((LocatableEvent) event)),
                    null
                );
            } catch (InternalExceptionWrapper ex) {
                return false;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return false;
            }
        }
        return super.exec (event);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (LineBreakpoint.PROP_LINE_NUMBER.equals(evt.getPropertyName())) {
            if (lineNumberForUpdate != -1) {
                lineNumber = lineNumberForUpdate;
                lineNumberForUpdate = -1;
                return; // do not call super.propertyChange(evt);
            }
            int old = lineNumber;
            updateLineNumber();
            //System.err.println("LineBreakpointImpl.propertyChange("+evt+")");
            //System.err.println("  old line = "+old+", new line = "+lineNumber);
            //System.err.println("  BP line = "+getBreakpoint().getLineNumber());
            if (lineNumber == old) {
                // No change, skip it
                return ;
            }
        }
        super.propertyChange(evt);
    }
    
    
    private static List getLocations (
        ReferenceType referenceType,
        String stratum,
        String sourceName,
        String bpSourcePath,
        int lineNumber,
        String[] reason,
        boolean[] noLocationReason
    ) {
        try {
            reason[0] = null;
            noLocationReason[0] = false;
            List locations = locationsOfLineInClass(referenceType, stratum,
                                                    sourceName, bpSourcePath,
                                                    lineNumber, reason);
            /* Obsolete, no special handling of inner classes, referenceType is
               the correct class now.
             if (locations.isEmpty()) {
                // add lines from innerclasses
                Iterator i = referenceType.nestedTypes ().iterator ();
                while (i.hasNext ()) {
                    ReferenceType rt = (ReferenceType) i.next ();
                    locations = locationsOfLineInClass(rt, stratum, sourceName,
                                                       bpSourcePath, lineNumber,
                                                       reason);
                    if (!locations.isEmpty()) {
                        break;
                    }
                }
            }*/
            if (locations.isEmpty() && reason[0] == null) {
                reason[0] = NbBundle.getMessage(LineBreakpointImpl.class, "MSG_NoLocation", Integer.toString(lineNumber), referenceType.name());
                noLocationReason[0] = true;
            }
            return locations;
        } catch (AbsentInformationException ex) {
            // we are not able to create breakpoint in this situation. 
            // should we write some message?!?
            // We should indicate somehow that the breakpoint is invalid...
            reason[0] = NbBundle.getMessage(LineBreakpointImpl.class, "MSG_NoLineInfo", referenceType.name());
        } catch (ObjectCollectedException ex) {
            // no problem, breakpoint will be created next time the class 
            // is loaded
            // should not occurre. see [51034]
            reason[0] = ex.getLocalizedMessage();
        } catch (ClassNotPreparedException ex) {
            // should not occurre. VirtualMachine.allClasses () returns prepared
            // classes only. But...
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        } catch (InternalException iex) {
            // Something wrong in JDI
            ErrorManager.getDefault().annotate(iex, 
                    NbBundle.getMessage(LineBreakpointImpl.class,
                    "MSG_jdi_internal_error") );
            ErrorManager.getDefault().notify(iex);
            // We should indicate somehow that the breakpoint is invalid...
            reason[0] = iex.getLocalizedMessage();
        }
        return Collections.EMPTY_LIST;
    }
    
    private static List<Location> locationsOfLineInClass(
        ReferenceType referenceType,
        String stratum,
        String sourceName,
        String bpSourcePath,
        int lineNumber,
        String[] reason) throws AbsentInformationException, ObjectCollectedException,
                                ClassNotPreparedException, InternalException {
        List<Location> list;
        try {
            list = ReferenceTypeWrapper.locationsOfLine0(referenceType, stratum, sourceName, lineNumber);
        } catch (ClassNotPreparedExceptionWrapper ex) {
            throw ex.getCause();
        }

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("LineBreakpoint: locations for ReferenceType=" +
                    referenceType + ", stratum=" + stratum + 
                    ", source name=" + sourceName + ", bpSourcePath=" +
                    bpSourcePath+", lineNumber=" + lineNumber + 
                    " are: {" + list + "}");
        }
        if (!list.isEmpty ()) {
            if (bpSourcePath == null)
                return list;
            bpSourcePath = bpSourcePath.replace(java.io.File.separatorChar, '/');
            ArrayList<Location> locations = new ArrayList<Location>(list.size());
            for (Iterator<Location> it = list.iterator(); it.hasNext();) {
                Location l = it.next();
                String lSourcePath;
                try {
                    lSourcePath = LocationWrapper.sourcePath(l).replace(java.io.File.separatorChar, '/');
                } catch (InternalExceptionWrapper ex) {
                    return Collections.emptyList();
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return Collections.emptyList();
                }
                lSourcePath = normalize(lSourcePath);
                if (lSourcePath.equals(bpSourcePath)) {
                    locations.add(l);
                } else {
                    reason[0] = "Breakpoint source path '"+bpSourcePath+"' is different from the location source path '"+lSourcePath+"'.";
                }
            }
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("LineBreakpoint: relevant location(s) for path '" + bpSourcePath + "': " + locations);
            }
            if (!locations.isEmpty())
                return locations;
        }
        return Collections.emptyList();
    }
    
    /**
     * Normalizes the given path by removing unnecessary "." and ".." sequences.
     * This normalization is needed because the compiler stores source paths like "foo/../inc.jsp" into .class files. 
     * Such paths are not supported by our ClassPath API.
     * TODO: compiler bug? report to JDK?
     * 
     * @param path path to normalize
     * @return normalized path without "." and ".." elements
     */ 
    private static String normalize(String path) {
      Pattern thisDirectoryPattern = Pattern.compile("(/|\\A)\\./");
      Pattern parentDirectoryPattern = Pattern.compile("(/|\\A)([^/]+?)/\\.\\./");
      
      for (Matcher m = thisDirectoryPattern.matcher(path); m.find(); )
      {
        path = m.replaceAll("$1");
        m = thisDirectoryPattern.matcher(path);
      }
      for (Matcher m = parentDirectoryPattern.matcher(path); m.find(); )
      {
        if (!m.group(2).equals("..")) {
          path = path.substring(0, m.start()) + m.group(1) + path.substring(m.end());
          m = parentDirectoryPattern.matcher(path);        
        }
      }
      return path;
    }

    private int findBreakableLine(String url, final int lineNumber) {
        FileObject fileObj = null;
        try {
            fileObj = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
        }
        if (fileObj == null) return lineNumber;
        DataObject dobj = null;
        try {
            dobj = DataObject.find(fileObj);
        } catch (DataObjectNotFoundException ex) {
        }
        if (dobj == null) return lineNumber;
        final EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
        if (ec == null) return lineNumber;
        final BaseDocument doc;
        try {
            doc = (BaseDocument) ec.openDocument();
        } catch (UserQuestionException uqex) {
            // ignored
            return lineNumber;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return lineNumber;
        }
        final int rowStartOffset = Utilities.getRowStartFromLineOffset(doc, lineNumber - 1);
        JavaSource js = JavaSource.forFileObject(fileObj);
        if (js == null) return lineNumber;
        final int[] result = new int[] {lineNumber};
        final Future<Void> scanFinished;
        try {
            scanFinished = js.runWhenScanFinished(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController ci) throws Exception {
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    SourcePositions positions = ci.getTrees().getSourcePositions();
                    CompilationUnitTree compUnit = ci.getCompilationUnit();
                    TreeUtilities treeUtils = ci.getTreeUtilities();

                    TreePath path = treeUtils.pathFor(rowStartOffset);
                    Tree tree = path.getLeaf();
                    int startOffs = (int)positions.getStartPosition(compUnit, tree);
                    int outerLineNumber = Utilities.getLineOffset(doc, startOffs) + 1;
                    if (outerLineNumber == lineNumber) return;
                    Tree.Kind kind = tree.getKind();
                    if (kind == Tree.Kind.COMPILATION_UNIT || kind == Tree.Kind.CLASS) return;
                    if (kind == Tree.Kind.BLOCK) {
                        BlockTree blockTree = (BlockTree)tree;
                        Tree previousTree = null;
                        int previousTreeEndOffset = -1;
                        for (StatementTree sTree : blockTree.getStatements()) {
                            int end = (int)positions.getStartPosition(compUnit, sTree);
                            if (end <= rowStartOffset && end > previousTreeEndOffset) {
                                previousTree = sTree;
                                previousTreeEndOffset = end;
                            } else if (end > rowStartOffset) {
                                break;
                            }
                        } // for
                        if (previousTree == null) {
                            tree = path.getParentPath().getLeaf();
                            kind = tree.getKind();
                            if (kind != Tree.Kind.COMPILATION_UNIT && kind != Tree.Kind.CLASS) {
                                previousTree = tree;
                            } else {
                                return;
                            }
                        }
                        startOffs = (int)positions.getStartPosition(compUnit, previousTree);
                        outerLineNumber = Utilities.getLineOffset(doc, startOffs) + 1;
                    } // if
                    result[0] = outerLineNumber;
                }
            }, true);
            if (!scanFinished.isDone()) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    return lineNumber;
                } else {
                    try {
                        scanFinished.get();
                    } catch (InterruptedException iex) {
                        return lineNumber;
                    } catch (java.util.concurrent.ExecutionException eex) {
                        ErrorManager.getDefault().notify(eex);
                        return lineNumber;
                    }
                }
            }
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return lineNumber;
        }
        return result[0];
    }

    static LineBreakpoint findBreakpoint (String url, int lineNumber) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof LineBreakpoint)) {
                continue;
            }
            LineBreakpoint lb = (LineBreakpoint) breakpoints[i];
            if (!lb.getURL ().equals (url)) continue;
            if (lb.getLineNumber() == lineNumber) {
                return lb;
            }
        }
        return null;
    }

}

