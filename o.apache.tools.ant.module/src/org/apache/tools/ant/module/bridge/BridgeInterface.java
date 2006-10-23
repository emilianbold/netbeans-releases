/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.bridge;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.windows.OutputWriter;

/**
 * What is implemented by bridge.jar.
 * @author Jesse Glick
 */
public interface BridgeInterface {

    /**
     * Actually run a build script.
     * @param buildFile an Ant build script
     * @param targets a list of target names to run, or null to run the default target
     * @param in an input stream for console input
     * @param out an output stream with the ability to have hyperlinks
     * @param err an error stream with the ability to have hyperlinks
     * @param properties any Ant properties to define
     * @param verbosity the intended logging level
     * @param displayName a user-presentable name for the session
     * @param interestingOutputCallback will be called if and when some interesting output appears, or input is requested
     * @param handle a progress handle to update if appropriate (switch to sleeping and back to indeterminate)
     * @return true if the build succeeded, false if it failed for any reason
     */
    boolean run(File buildFile, List<String> targets, InputStream in, OutputWriter out, OutputWriter err, Map<String,String> properties, int verbosity, String displayName, Runnable interestingOutputCallback, ProgressHandle handle);
    
    /**
     * Try to stop a running build.
     * The implementation may wait for a while to stop at a safe point,
     * and/or stop forcibly.
     * @param process the thread which is currently running the build (in which {@link #run} was invoked)
     */
    void stop(Thread process);
    
    /**
     * Get some informational value of the Ant version.
     * @return the version
     */
    String getAntVersion();
    
    /**
     * Check whether Ant 1.6 is loaded.
     * If so, additional abilities may be possible, such as namespace support.
     */
    boolean isAnt16();
    
    /**
     * Get a proxy for IntrospectionHelper, to introspect task + type structure.
     */
    IntrospectionHelperProxy getIntrospectionHelper(Class clazz);
    
    /**
     * See Project.toBoolean.
     */
    boolean toBoolean(String val);
    
    /**
     * Get values of an enumeration class.
     * If it is not actually an enumeration class, return null.
     */
    String[] getEnumeratedValues(Class c);
    
}
