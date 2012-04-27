/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.findbugs;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import edu.umd.cs.findbugs.RecursiveFileSearch;
import edu.umd.cs.findbugs.classfile.ICodeBaseEntry;
import edu.umd.cs.findbugs.classfile.ICodeBaseIterator;
import edu.umd.cs.findbugs.classfile.ICodeBaseLocator;
import edu.umd.cs.findbugs.classfile.impl.DirectoryCodeBase;
import edu.umd.cs.findbugs.classfile.impl.DirectoryCodeBaseEntry;

import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public final class NbDirectoryCodeBase extends DirectoryCodeBase {

    private boolean searchPerformed;

    NbDirectoryCodeBase(ICodeBaseLocator codeBaseLocator, File directory) {
        super(codeBaseLocator, directory);
        searchPerformed = false;
    }

    @Override
    public ICodeBaseIterator iterator() throws InterruptedException {
        if (!searchPerformed) {
            getRecursiveFileSearch().search();
            searchPerformed = true;
        }

        return new ICodeBaseIterator() {

            Iterator<String> fileNameIterator = getRecursiveFileSearch().fileNameIterator();

            @Override
            public boolean hasNext() throws InterruptedException {
                return fileNameIterator != null && fileNameIterator.hasNext();
            }

            @Override
            public ICodeBaseEntry next() throws InterruptedException {
                final String fileName = fileNameIterator.next();

                // Make the filename relative to the directory
                String resourceName = getResourceName(fileName);

                // Update last modified time
                File file = new File(fileName);
                long modTime = file.lastModified();
                addLastModifiedTime(modTime);

                return new Entry(NbDirectoryCodeBase.this, resourceName);
            }
        };
    }

    @Override
    public ICodeBaseEntry lookupResource(String resourceName) {
        if (resourceName.endsWith(".class")) {
            ICodeBaseEntry result = lookupResource(resourceName.substring(0, resourceName.length() - "class".length()) + "sig");

            if (result != null) return result;
        }

        return super.lookupResource(resourceName);
    }

    private RecursiveFileSearch getRecursiveFileSearch() {
        try {
            Field fld = DirectoryCodeBase.class.getDeclaredField("rfs"); //NOI18N
            fld.setAccessible(true);
            return (RecursiveFileSearch) fld.get(this);
        } catch (IllegalAccessException iae) {
            Exceptions.printStackTrace(iae);
        } catch (NoSuchFieldException nsfe) {
            Exceptions.printStackTrace(nsfe);
        }
        return null;
    }

    private String getResourceName(String fileName) {
        try {
            Method method = DirectoryCodeBase.class.getDeclaredMethod("getResourceName", String.class); //NOI18N
            method.setAccessible(true);
            return (String) method.invoke(this, fileName);
        } catch (IllegalAccessException iae) {
            Exceptions.printStackTrace(iae);
        } catch (InvocationTargetException ite) {
            Exceptions.printStackTrace(ite);
        } catch (NoSuchMethodException nsme) {
            Exceptions.printStackTrace(nsme);
        }
        return null;
    }

    private static final class Entry extends DirectoryCodeBaseEntry {

        public Entry(DirectoryCodeBase codeBase, String realResourceName) {
            super(codeBase, realResourceName);
        }

        @Override
        public String getResourceName() {
            String resourceName = super.getResourceName();
            if (resourceName.endsWith(".sig")) { //NOI18N
                resourceName = resourceName.substring(0, resourceName.length() - 4) + ".class"; //NOI18N
            }
            return resourceName;
        }
    }
}
