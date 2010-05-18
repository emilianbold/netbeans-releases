/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.api.model;

import org.openide.ErrorManager;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class is in the API for debugging purpose only.
 *<p>
 * Note: Use for debugging purpose only.
 * 
 * @author David Kaspar
 */
public class Debug {

    private Debug () {
    }

    private static int stackTraceFriendIndex = Integer.MIN_VALUE;

    private static void init () {
        if (stackTraceFriendIndex >= 0)
            return;
        StackTraceElement[] stackTrace = Thread.currentThread ().getStackTrace ();
        for (int a = 0; a < stackTrace.length; a ++) {
            StackTraceElement stackTraceElement = stackTrace[a];
            if (Debug.class.getName ().equals (stackTraceElement.getClassName ())  &&  "init".equals (stackTraceElement.getMethodName ())) {
                stackTraceFriendIndex = a + 2;
                return;
            }
        }
    }

    /**
     * Prints a warning message.
     * @param messages the messages
     */
    public static void warning (Object... messages) {
        init ();
        if (messages.length == 1 && messages[0] instanceof Throwable) {
            ((Throwable) messages[0]).printStackTrace (System.err);
            return;
        }

        StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
        StringBuilder sb = new StringBuilder ();
        sb.append ("WARNING: ").append (stack[stackTraceFriendIndex - 1]).append ('\n'); // NOI18N
        if (stackTraceFriendIndex < stack.length)
            sb.append ("         ").append (stack[stackTraceFriendIndex]).append ('\n'); // NOI18N
        for (Object message : messages)
            sb.append (" | ").append (message); // NOI18N
        System.err.println (sb.toString ());
    }

    /**
     * Checks whether a method (from where this isFriend method is called) is called from a specified class.
     * @param clazz the class
     * @return true if it is called from friend class
     */
    public static boolean isFriend (Class clazz) {
        init ();
        StackTraceElement whoCalled = Thread.currentThread ().getStackTrace ()[stackTraceFriendIndex];
        return whoCalled.getClassName ().equals (clazz.getName ());
    }

    /**
     * Checks whether a method (from where this isFriend method is called) is called from a specified method of a specified class.
     * @param clazz  the class
     * @param method the method
     * @return true if it is called from friend class
     */
    public static boolean isFriend (Class clazz, String method) {
        init ();
        StackTraceElement whoCalled = Thread.currentThread ().getStackTrace ()[stackTraceFriendIndex];
        return whoCalled.getClassName ().equals (clazz.getName ()) && whoCalled.getMethodName ().equals (method);
    }

    /**
     * Checks whether a method (from where this isFriend method is called) is called from a specified method of a specified class.
     * @param className the class name
     * @param method the method
     * @return true if it is called from friend class
     */
    public static boolean isFriend (String className, String method) {
        init ();
        StackTraceElement whoCalled = Thread.currentThread ().getStackTrace ()[stackTraceFriendIndex];
        return whoCalled.getClassName ().equals (className) && whoCalled.getMethodName ().equals (method);
    }

    /**
     * Shows an error dialog with error message.
     * <p>
     * Note: Usually used like: throw Debug.error ("message");
     * @param messages the messages
     * @return the exception that could be use for escaping from a method
     */
    public static RuntimeException error (Object... messages) {
        init ();
        Throwable exception;
        if (messages.length == 1  &&  messages[0] instanceof Throwable) {
            exception = (Throwable) messages[0];
        } else {
            StringBuilder sb = new StringBuilder ();
            StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
            sb.append ("ERROR: ").append (stack[stackTraceFriendIndex - 1]).append ('\n'); // NOI18N
            if (stackTraceFriendIndex < stack.length)
                sb.append ("       ").append (stack[stackTraceFriendIndex]).append ('\n'); // NOI18N
            for (Object message : messages)
                sb.append (" | ").append (message); // NOI18N

            exception = new RuntimeException (sb.toString ());
        }
        ErrorManager.getDefault ().notify (ErrorManager.ERROR, exception);
        throw new RuntimeException (exception);
    }

    /**
     * Shows an error dialog with illegal-state message.
     * <p/>
     * Note: Usually used like: throw Debug.illegalState ("message");
     * @param messages the messages
     * @return the exception that could be use for escaping from a method
     */
    public static RuntimeException illegalState (Object... messages) {
        init ();
        Throwable exception;
        if (messages.length == 1 && messages[0] instanceof Throwable) {
            exception = (Throwable) messages[0];
        } else {
            StringBuilder sb = new StringBuilder ("ILLEGAL STATE: "); // NOI18N
            sb.append (Thread.currentThread ().getStackTrace ()[stackTraceFriendIndex - 1]);
            sb.append ('\n');
            for (Object message : messages)
                sb.append (" | ").append (message); // NOI18N

            exception = new IllegalStateException (sb.toString ());
        }
        ErrorManager.getDefault ().notify (ErrorManager.ERROR, exception);
        throw new RuntimeException (exception);
    }

    /**
     * Shows an error dialog with illegal-argument message.
     * <p/>
     * Note: Usually used like: throw Debug.illegalArgument ("message");
     * @param messages the messages
     * @return the exception that could be use for escaping from a method
     */
    public static RuntimeException illegalArgument (Object... messages) {
        init ();
        Throwable exception;
        if (messages.length == 1 && messages[0] instanceof Throwable) {
            exception = (Throwable) messages[0];
        } else {
            StringBuilder sb = new StringBuilder ("ILLEGAL ARGUMENT: "); // NOI18N
            sb.append (Thread.currentThread ().getStackTrace ()[stackTraceFriendIndex - 1]);
            sb.append ('\n'); // NOI18N
            for (Object message : messages)
                sb.append (" | ").append (message); // NOI18N

            exception = new IllegalArgumentException (sb.toString ());
        }
        ErrorManager.getDefault ().notify (ErrorManager.ERROR, exception);
        throw new RuntimeException (exception);
    }

    /**
     * Prints the tree of components stored in the document.
     * @param document the document
     */
    public static void dumpDocument (DesignDocument document) {
        assert document != null;
        dumpComponent (document.getRootComponent ());
    }

    /**
     * Prints the tree of components of a specific component
     * @param component the component
     */
    public static void dumpComponent (DesignComponent component) {
        if (component == null) {
            System.out.println ("No component to dump"); // NOI18N
            return;
        }
        System.out.println ("Dumping component: " + component.getComponentID ()); // NOI18N
        component.dumpComponent (""); // NOI18N
    }

    /**
     * Collects all component references from a property value.
     * @param propertyValue the property value for inspection
     * @param references the hash set of components for storing results
     */
    public static void collectAllComponentReferences (PropertyValue propertyValue, Collection<DesignComponent> references) {
        propertyValue.collectAllComponentReferences (references);
    }

    /**
     * Checks whether a specified component is referenced from the main tree of components under document root.
     * @param referenceComponent the reference component
     * @return true, if the reference component is referenced
     */
    public static boolean isComponentReferencedInRootTree (DesignComponent referenceComponent) {
        assert referenceComponent != null;
        return isComponentReferencedInRootTree (referenceComponent.getDocument ().getRootComponent (), referenceComponent);
    }

    private static boolean isComponentReferencedInRootTree (DesignComponent treeComponent, DesignComponent referenceComponent) {
        ArrayList<DesignComponent> referenced = new ArrayList<DesignComponent> ();
        ComponentDescriptor descriptor = treeComponent.getComponentDescriptor ();

        if (descriptor != null) {
            for (PropertyDescriptor propertyDescriptor : descriptor.getPropertyDescriptors ()) {
                PropertyValue propertyValue = treeComponent.readProperty (propertyDescriptor.getName ());
                referenced.clear ();
                Debug.collectAllComponentReferences (propertyValue, referenced);
                if (referenced.contains (referenceComponent))
                    return true;
            }
        }

        for (DesignComponent child : treeComponent.getComponents ())
            if (isComponentReferencedInRootTree (child, referenceComponent))
                return true;

        return false;
    }

}
