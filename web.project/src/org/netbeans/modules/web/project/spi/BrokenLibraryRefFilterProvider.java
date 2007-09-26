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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.project.spi;

import org.netbeans.api.project.Project;

/**
 * Allows removing broken library references from a project while the project
 * is being open.
 *
 * <p>Implementations of this interface are registered in the
 * <code>Projects/org-netbeans-modules-web-project/BrokenLibraryRefFilterProviders</code>
 * folder in the default file system. When a web project is opened,
 * the {@link #createFilter} method of all implementations is called
 * to create {@link BrokenLibraryRefFilter a filter for broken references}. 
 * This filter is then queried for all broken library references in the project.
 * If at least one filter returns <code>true</code>, the library reference is removed.</p>
 *
 * @author Andrei Badea
 */
public interface BrokenLibraryRefFilterProvider {

    /**
     * Creates a filter for the broken library references in the given
     * project.
     * 
     * @param  project the project being opened; never null.
     * @return a filter for the broken library references in <code>project</code>
     *         or null if this project does not need to be filtered.
     */
    public BrokenLibraryRefFilter createFilter(Project project);
}
