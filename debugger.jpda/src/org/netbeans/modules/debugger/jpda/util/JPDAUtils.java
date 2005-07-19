/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.util;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.request.EventRequestManager;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
* Utilities for debugger.
*
* @author Jan Jancura
*/
public class JPDAUtils {


    // testing methods .........................................................................

    public static void printFeatures (VirtualMachine virtualMachine) {
        System.out.println ("canAddMethod " + virtualMachine.canAddMethod ());
        //System.out.println ("canBeModified " + virtualMachine.canBeModified ());
        System.out.println ("canGetBytecodes " + virtualMachine.canGetBytecodes ());
        System.out.println ("canGetCurrentContendedMonitor " + virtualMachine.canGetCurrentContendedMonitor ());
        System.out.println ("canGetMonitorInfo " + virtualMachine.canGetMonitorInfo ());
        System.out.println ("canGetOwnedMonitorInfo " + virtualMachine.canGetOwnedMonitorInfo ());
        System.out.println ("canGetSourceDebugExtension " + virtualMachine.canGetSourceDebugExtension ());
        System.out.println ("canGetSyntheticAttribute " + virtualMachine.canGetSyntheticAttribute ());
        System.out.println ("canPopFrames " + virtualMachine.canPopFrames ());
        System.out.println ("canRedefineClasses " + virtualMachine.canRedefineClasses ());
        System.out.println ("canRequestVMDeathEvent " + virtualMachine.canRequestVMDeathEvent ());
        System.out.println ("canUnrestrictedlyRedefineClasses " + virtualMachine.canUnrestrictedlyRedefineClasses ());
        System.out.println ("canUseInstanceFilters " + virtualMachine.canUseInstanceFilters ());
        System.out.println ("canWatchFieldAccess " + virtualMachine.canWatchFieldAccess ());
        System.out.println ("canWatchFieldModification " + virtualMachine.canWatchFieldModification ());
    }
   
    public static void showMethods (ReferenceType rt) {
        System.out.println ("  ============================================"); // NOI18N
        System.out.println ("  Methods for " + rt.name ()); // NOI18N
        List l = rt.methods ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println (((Method) l.get (i)).name () + " ; " + // NOI18N
                                ((Method) l.get (i)).signature ());

        System.out.println ("  ============================================"); // NOI18N
    }

    public static void showLinesForClass (ReferenceType rt) {
        System.out.println ("  ============================================"); // NOI18N
        System.out.println ("  Lines for " + rt.name ()); // NOI18N
        List l = null;
        try {
            l = rt.allLineLocations ();
        } catch (AbsentInformationException e) {
        }
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("   " + ((Location) l.get (i)).lineNumber () + " : " + // NOI18N
                                ((Location) l.get (i)).codeIndex ()
                               );

        System.out.println ("  ============================================"); // NOI18N
    }

    public static void showRequests (EventRequestManager requestManager) {
        System.out.println ("  ============================================"); // NOI18N
        List l = requestManager.breakpointRequests ();
        System.out.println ("  Break request: " + l.size ()); // NOI18N
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        l = requestManager.classPrepareRequests ();
        System.out.println ("  Class prepare request: " + l.size ()); // NOI18N
        k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        l = requestManager.accessWatchpointRequests ();
        System.out.println ("  Access watch request: " + l.size ()); // NOI18N
        k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        l = requestManager.classUnloadRequests ();
        System.out.println ("  Class unload request: " + l.size ()); // NOI18N
        k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        l = requestManager.exceptionRequests ();
        System.out.println ("  Exception request: " + l.size ()); // NOI18N
        k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        l = requestManager.methodEntryRequests ();
        System.out.println ("  Method entry request: " + l.size ()); // NOI18N
        k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        l = requestManager.methodExitRequests ();
        System.out.println ("  Method exit request: " + l.size ()); // NOI18N
        k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        l = requestManager.modificationWatchpointRequests ();
        System.out.println ("  Modif watch request: " + l.size ()); // NOI18N
        k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        l = requestManager.stepRequests ();
        System.out.println ("  Step request: " + l.size ()); // NOI18N
        k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        l = requestManager.threadDeathRequests ();
        System.out.println ("  Thread death entry request: " + l.size ()); // NOI18N
        k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        l = requestManager.threadStartRequests ();
        System.out.println ("  Thread start request: " + l.size ()); // NOI18N
        k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("    " + l.get (i));
        System.out.println ("  ============================================"); // NOI18N
        
    }

    public static void showConnectors (List l) {
        int i, k = l.size ();
        for (i = 0; i < k; i++) showConnector ((Connector) l.get (i));
    }

    public static void showConnector (Connector connector) {
        System.out.println ("  ============================================"); // NOI18N
        System.out.println ("  Connector: " + connector); // NOI18N
        System.out.println ("    name: " + connector.name ()); // NOI18N
        System.out.println ("    description: " + connector.description ()); // NOI18N
        System.out.println ("    transport: " + (connector.transport () != null ? connector.transport ().name () : "null")); // NOI18N
        showProperties (connector.defaultArguments ());
        System.out.println ("  ============================================"); // NOI18N
    }

    public static void showThread (ThreadReference tr) {
        System.out.println ("  ============================================"); // NOI18N
        try {
            System.out.println ("  Thread: " + tr.name ()); // NOI18N
        } catch (Exception e) {
            System.out.println ("  Thread: " + e); // NOI18N
        }
        
        try {
            System.out.println ("    status: " + tr.status ()); // NOI18N
        } catch (Exception e) {
            System.out.println ("    status: " + e); // NOI18N
        }
        
        try {
            System.out.println ("    isSuspended: " + tr.isSuspended ()); // NOI18N
        } catch (Exception e) {
            System.out.println ("    isSuspended: " + e); // NOI18N
        }
        
        try {
            System.out.println ("    frameCount: " + tr.frameCount ()); // NOI18N
        } catch (Exception e) {
            System.out.println ("    frameCount: " + e); // NOI18N
        }
        
        try {
            System.out.println ("    location: " + tr.frame (0)); // NOI18N
        } catch (Exception e) {
            System.out.println ("    location: " + e); // NOI18N
        }
        System.out.println ("  ============================================"); // NOI18N
    }


    private static void showProperties (Map properties) {
        Iterator i = properties.keySet ().iterator ();
        while (i.hasNext ()) {
            Object k = i.next ();
            Connector.Argument a = (Connector.Argument) properties.get (k);
            System.out.println ("    property: " + k + " > " + a.name ()); // NOI18N
            System.out.println ("      desc: " + a.description ()); // NOI18N
            System.out.println ("      mustSpecify: " + a.mustSpecify ()); // NOI18N
            System.out.println ("      value: " + a.value ()); // NOI18N
        }
    }

    public static void listGroup (String s, ThreadGroupReference g) {
        List l = g.threadGroups ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            System.out.println (s + "Thread Group: " + l.get (i) + " : " + // NOI18N
                                ((ThreadGroupReference)l.get (i)).name ()
                               );
            listGroup (s + "  ", (ThreadGroupReference)l.get (i)); // NOI18N
        }
        l = g.threads ();
        k = l.size ();
        for (i = 0; i < k; i++) {
            System.out.println (s + "Thread: " + l.get (i) + " : " + // NOI18N
                                ((ThreadReference)l.get (i)).name ()
                               );
        }
    }

    private static void listGroups (List g) {
        System.out.println ("  ============================================"); // NOI18N
        int i, k = g.size ();
        for (i = 0; i < k; i++) {
            System.out.println ("Thread Group: " + g.get (i) + " : " + // NOI18N
                                ((ThreadGroupReference)g.get (i)).name ()
                               );
            listGroup ("  ", (ThreadGroupReference)g.get (i)); // NOI18N
        }
        System.out.println ("  ============================================"); // NOI18N
    }
}
