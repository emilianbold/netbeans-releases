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
import java.io.IOException;
import java.lang.reflect.Field;

import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.IClassFactory;
import edu.umd.cs.findbugs.classfile.IClassPath;
import edu.umd.cs.findbugs.classfile.IClassPathBuilder;
import edu.umd.cs.findbugs.classfile.ICodeBase;
import edu.umd.cs.findbugs.classfile.ICodeBaseEntry;
import edu.umd.cs.findbugs.classfile.ICodeBaseIterator;
import edu.umd.cs.findbugs.classfile.ICodeBaseLocator;
import edu.umd.cs.findbugs.classfile.IErrorLogger;
import edu.umd.cs.findbugs.classfile.impl.ClassFactory;
import edu.umd.cs.findbugs.classfile.impl.DirectoryCodeBase;
import edu.umd.cs.findbugs.classfile.impl.FilesystemCodeBaseLocator;
import edu.umd.cs.findbugs.classfile.impl.SingleFileCodeBase;
import edu.umd.cs.findbugs.classfile.impl.ZipCodeBaseFactory;

import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public final class NbClassFactory implements IClassFactory {

    private static final IClassFactory factory;

    static {
        factory = ClassFactory.instance();
        try {
            Field fld = factory.getClass().getDeclaredField("theInstance"); //NOI18N
            fld.setAccessible(true);
            fld.set(null, new NbClassFactory());
        } catch (IllegalAccessException iae) {
            Exceptions.printStackTrace(iae);
        } catch (NoSuchFieldException nsfe) {
            Exceptions.printStackTrace(nsfe);
        }
    }

    private NbClassFactory() {
    }

    @Override
    public IClassPath createClassPath() {
        return factory.createClassPath();
    }

    @Override
    public IClassPathBuilder createClassPathBuilder(IErrorLogger iel) {
        return factory.createClassPathBuilder(iel);
    }

    @Override
    public ICodeBaseLocator createFilesystemCodeBaseLocator(String string) {
        return new FilesystemCodeBaseLocator(string) {

            @Override
            public ICodeBase openCodeBase() throws IOException {
                String fileName = getPathName();
                File file = new File(fileName);
                if (file.isDirectory()) {
                    return new NbDirectoryCodeBase(this, file);
                } else if (fileName.endsWith(".class") || fileName.endsWith(".sig")) { //NOI18N
                    return new SingleFileCodeBase(this, fileName);
                } else {
                    return ZipCodeBaseFactory.makeZipCodeBase(this, file);
                }
            }
        };
    }

    @Override
    public ICodeBaseLocator createNestedArchiveCodeBaseLocator(ICodeBase icb, String string) {
        return factory.createNestedArchiveCodeBaseLocator(icb, string);
    }

    @Override
    public IAnalysisCache createAnalysisCache(IClassPath icp, BugReporter br) {
        return factory.createAnalysisCache(icp, br);
    }
}
