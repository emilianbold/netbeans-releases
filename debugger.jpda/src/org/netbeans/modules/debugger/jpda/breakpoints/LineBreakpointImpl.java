/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;

import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;



/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class LineBreakpointImpl extends ClassBasedBreakpoint {
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.breakpoints") != null;

    
    private LineBreakpoint      breakpoint;
    private SourcePath          sourcePath;
    private int                 lineNumber;
    
    
    public LineBreakpointImpl (
        LineBreakpoint breakpoint, 
        JPDADebuggerImpl debugger,
        Session session,
        SourcePath sourcePath
    ) {
        super (breakpoint, debugger, session);
        this.breakpoint = breakpoint;
        this.sourcePath = sourcePath;
        lineNumber = breakpoint.getLineNumber ();
        set ();
    }
    
    void fixed () {
        if (verbose)
            System.out.println ("B fix breakpoint impl: " + this);
        lineNumber = breakpoint.getLineNumber ();
        super.fixed ();
    }
    
    protected void setRequests () {
        lineNumber = breakpoint.getLineNumber ();
        String className = EditorContextBridge.getClassName (
            breakpoint.getURL (),
            lineNumber
        );
        if (className == null) {
            //HACK: for JSPs.
            //PENDING
            className = breakpoint.getURL ();
            setClassRequests (
                new String[] {
                    className
                }, 
                new String [0],
                ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
            );
            checkLoadedClasses (className, true);
        } else {
            
            //HACK
            // annonymous innerclasses are generated to outerclass
            // class.inner.annonym -> class$1
            // thats why we should not add class filter for class.inner,
            // but for class!
            int i = className.indexOf ('$');
            if (i > 0) 
                className = className.substring (0, i);
            
            setClassRequests (
                new String[] {
                    className,
                    className + ".*", // innerclasses
                    className + "$*", // innerclasses
                }, 
                new String [0],
                ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
            );
            checkLoadedClasses (className, false);
        }
    }

    protected void classLoaded (ReferenceType referenceType) {
        if (verbose)
            System.out.println ("B class loaded: " + referenceType);
      
        List locations = getLocations (
            referenceType,
            breakpoint.getStratum (),
            breakpoint.getSourceName (),
            breakpoint.getSourcePath(),
            lineNumber
        );
        if (locations.isEmpty()) return; 
        for (Iterator it = locations.iterator(); it.hasNext();) {
            Location location = (Location)it.next();
            try {
                BreakpointRequest br = getEventRequestManager ().
                    createBreakpointRequest (location);
                addEventRequest (br);
            } catch (VMDisconnectedException e) {
            }
        }
    }

    public boolean exec (Event event) {
        if (event instanceof BreakpointEvent)
            return perform (
                breakpoint.getCondition (),
                ((BreakpointEvent) event).thread (),
                ((LocatableEvent) event).location ().declaringType (),
                null
            );
        return super.exec (event);
    }
    
    private static List getLocations (
        ReferenceType referenceType,
        String stratum,
        String sourceName,
        String bpSourcePath,
        int lineNumber
    ) {
        try {
            
            List list = new ArrayList (referenceType.locationsOfLine (
                stratum,
                sourceName,
                lineNumber
            ));

            if (verbose)
                System.out.println ("B   get location: referenceType=" + 
                    referenceType + " stratum=" + stratum + 
                    " source name=" + sourceName + " lineNumber " + lineNumber + 
                    " (#" + list.size () + ")");
            if (!list.isEmpty ()) {
                if (bpSourcePath == null)
                    return list;
                bpSourcePath = bpSourcePath.replace(java.io.File.separatorChar, '/');
                if (verbose)
                    System.out.println("B   source path: " + bpSourcePath);                
                ArrayList locations = new ArrayList();
                for (Iterator it = list.iterator(); it.hasNext();) {
                    Location l = (Location)it.next();
                    String lSourcePath = l.sourcePath().replace(java.io.File.separatorChar, '/');
                    lSourcePath = normalize(lSourcePath);
                    if (lSourcePath.equals(bpSourcePath))
                        locations.add(l);
                }
                if (verbose)
                    System.out.println("B   relevant location(s) for path '" + bpSourcePath + "': " + locations);
                if (!locations.isEmpty())
                    return locations;
            }

            // add lines from innerclasses
            Iterator i = referenceType.nestedTypes ().iterator ();
            while (i.hasNext ()) {
                ReferenceType rt = (ReferenceType) i.next ();
                list = rt.locationsOfLine (
                    stratum,
                    sourceName,
                    lineNumber
                );
                if (verbose)
                    System.out.println ("B   get location2: referenceType=" + 
                        referenceType + " stratum=" + stratum + 
                        " source name=" + sourceName + " lineNumber" + lineNumber + 
                        ":= " + list.size ());
                if (!list.isEmpty()) return list;
            }
        } catch (AbsentInformationException ex) {
            // we are not able to create breakpoint in this situation. 
            // should we write some message?!?
        } catch (ObjectCollectedException ex) {
            // no problem, breakpoint will be created next time the class 
            // is loaded
            // should not occurre. see [51034]
        } catch (ClassNotPreparedException ex) {
            // should not occurre. VirtualMachine.allClasses () returns prepared
            // classes only. But...
            ex.printStackTrace ();
        }
        return new ArrayList();
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
}

