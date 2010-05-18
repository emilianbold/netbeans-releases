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
 * Software is Sun Microsystems, Inc. Portions Copyright 2003 Sun
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

package org.netbeans.modules.search;

import java.io.IOException;
import java.io.InputStream;

/**
 * Object input stream with a customized class resolution.
 * It allows to read search criteria (saved as instances of class
 * {@link org.openidex.search.SearchType SearchType} or its subclass), although
 * some of the classes are not defined by the Utilities module (these classes
 * are called &quot;external&quot; in the rest of this text).
 *
 * <p>The following three assumptions are used in the algorithm
 *    for class resolution:</p>
 * <ul>
 *     <li>for each type of search criterion to be read,
 *         a default instance of the class representing the criterion
 *         is registered in a lookup</li>
 *     <li>during serialization, the first external class to be resolved
 *         is the class representing the search criterion
 *         (and hence its instance is available in the lookup)</li>
 *     <li>the classloader which has loaded the search criterion's class
 *         is able to load all other external classes needed during the search
 *         criterion's deserialization</li>
 * </ul>
 * 
 * @see  #resolveClass
 * @see  org.openidex.search.SearchType  SearchType
 * @author  Marian Petras
 */
class SearchTypeInputStream extends java.io.ObjectInputStream {
    
    /**
     * holds the resolved <code>SearchType</code> class so that its classloader
     * may be used for resolution of other classes
     *
     * @see  #extClassLoader
     * @see  #resolveExtClass
     */
    private Class extSearchType = null;
    /**
     * classloader of the resolved <code>SearchType</code>
     *
     * @see  #extSearchType
     */
    private ClassLoader extClassLoader = null;

    /** Creates a new instance of SearchTypeInputStream */
    public SearchTypeInputStream(InputStream in) throws IOException {
        super(in);
    }
    
    /**
     * Loads the local class equivalent of the specified stream class
     * description. Uses the default
     * {@link java.io.ObjectInputStream ObjectInputStream}'s method first and
     * if it fails, tries to lookup the class among the registered
     * <code>SearchType</code>s or to use their classloaders.
     */
    protected Class resolveClass(java.io.ObjectStreamClass objectStreamClass)
            throws IOException, ClassNotFoundException {
        try {
            return super.resolveClass(objectStreamClass);
        } catch (ClassNotFoundException ex) {
            Class extClass = resolveExtClass(objectStreamClass.getName());
            if (extClass != null) {
                return extClass;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Resolves the specified class.
     * If this method is used for the first time (on this instance), it just
     * tries to lookup the class among the registed <code>SearchType</code>s.
     * Next time this method is called, it uses the <code>SearchType</code>'s
     * classloader for loading classes.
     * 
     * @param  className  name of a class to be resolved
     * @return  local class matching the specified class name
     *          or <code>null</code> if no matching class was found
     */
    private Class resolveExtClass(final String className) {
        if (extSearchType == null) {

            /* first time class resolution must be for the ext. SearchType */
            extSearchType = Utils.searchTypeForName(className);
            return extSearchType;
        }

        /*
         * We expect that the classloader that was used for loading
         * the Search Type class is suitable also for other classes:
         */
        if (extClassLoader == null) {
            try {
                extClassLoader = extSearchType.getClassLoader();
            } catch (SecurityException ex) {
                return null;
            }
        }
        try {
            return extClassLoader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
    
}
