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

package org.netbeans.modules.ant.freeform.spi;

/**
 * This interface is used to compute the help context for a freeform project.
 * Each {@link ProjectNature} should register an implementation in its lookup.
 * See {@link #getHelpIDFragment} to find out what are the requirements on the help
 * id fragments.
 *
 * If it is necessary to compute a help context for a freeform project, all
 * {@link HelpIDFragmentProvider}s registered in the project's lookup are asked to 
 * provide the fragments. The fragments are then lexicographically sorted and
 * concatenated (separated by dots) into one string, used as a base for the help id.
 *
 * @author Jan Lahoda
 * @since 1.11.1
 */
public interface HelpIDFragmentProvider {
    
    /**
     * Returns a help id fragment defined by the implementor. The method should return
     * the same string each time it is called (more preciselly, it is required that
     * <code>getHelpIDFragment().equals(getHelpIDFragment())</code>, but is allowed to
     * <code>getHelpIDFragment() != getHelpIDFragment()</code>). The string should be unique
     * among all the freeform project natures. The string is required to match this
     * regular expression: <code>([A-Za-z0-9])+</code>.
     *
     * Please note that the returned fragment is part of the contract between the
     * code and documentation, so be carefull when you need to change it.
     *
     * @return a non-null help id fragment, fullfilling the above conditions.
     */
    public String getHelpIDFragment();
    
}
