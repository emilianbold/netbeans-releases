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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.netbeans.api.java.source.JavaSource;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class CacheBinaryForSourceQuery {
    
    private static Method javaIndexGetClassFolderMethod;
    
    public static Result findCacheBinaryRoots(final URL sourceRoot) {
        assert sourceRoot != null;
        return new Result(sourceRoot);
    }
    
    private static File getClassFolder(URL url) {
        try {
            if (javaIndexGetClassFolderMethod == null) {
                Class clazz = Class.forName("org.netbeans.modules.java.source.indexing.JavaIndex", true, JavaSource.class.getClassLoader()); //NOI18N
                javaIndexGetClassFolderMethod = clazz.getMethod("getClassFolder", URL.class, boolean.class); //NOI18N
            }
            return (File) javaIndexGetClassFolderMethod.invoke(null, url, false);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private CacheBinaryForSourceQuery() {
    }
    
    public static final class Result {
        
        private final URL sourceRoot;
        
        private Result(final URL sourceRoot) {
            this.sourceRoot = sourceRoot;
        }
        
        public URL[] getRoots() {
            File f = getClassFolder(sourceRoot);
            if (f != null) {
                try {
                    return new URL[]{f.toURI().toURL()};
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return new URL[0];
        }
    }
}
