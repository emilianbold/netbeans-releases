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

/**
 * Allows removing broken library references from a project while the project
 * is being open.
 *
 * <p>Implementations of this interface are returned by {@link BrokenLibraryRefFilterProvider}. 
 * When a web project is opened the {@link #removeLibraryReference} method is called
 * for all broken library references. If at least one implementation returns
 * <code>true</code>, the library reference is removed.</p>
 *
 * @author Andrei Badea
 */
public interface BrokenLibraryRefFilter {

    /**
     * Return <code>true</code> from this method to remove the
     * reference to the given library.
     * @param  libraryName the name of a library to which a broken
     *         reference exists; never null.
     * @return true to remove this reference from the project; false otherwise
     */
    boolean removeLibraryReference(String libraryName);
}
