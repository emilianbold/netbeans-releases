/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.mixeddev;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.services.CsmSymbolResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectPlatform;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public final class MixedDevUtils {
        
    public static final String DOT = "."; // NOI18N
    
    public static final String COMMA = ","; // NOI18N
    
    public static final String LPAREN = "("; // NOI18N
    
    public static final String RPAREN = ")"; // NOI18N
    
    public static final String SCOPE = "::"; // NOI18N
    
    public static final String POINTER = "*"; // NOI18N
    
    public static String stringize(Collection<? extends CharSequence> collection, CharSequence separator) {
        boolean first = true;
        StringBuilder result = new StringBuilder();
        for (CharSequence seq : collection) {
            if (!first) {
                result.append(separator);
            } else {
                first = false;
            }
            result.append(seq);
        }
        return result.toString();
    }
    
    public static String repeat(String pattern, int times) {
        StringBuilder sb = new StringBuilder();
        while (times-- > 0) {
            sb.append(pattern);
        }
        return sb.toString();
    }
    
    public static <K, V> Map<K, V> createMapping(Pair<K, V> ... pairs) {
        Map<K, V> mapping = new HashMap<K, V>();
        for (Pair<K, V> pair : pairs) {
            mapping.put(pair.first(), pair.second());
        }
        return Collections.unmodifiableMap(mapping);
    }    
    
    public static interface Converter<F, T> {

        T convert(F from);

    }

    public static <F, T> T[] transform(F[] from, Converter<F, T> converter, Class<T> toClass) {
        T[] to = (T[]) Array.newInstance(toClass, from.length);
        for (int i = 0; i < from.length; i++) {
            to[i] = converter.convert(from[i]);
        }
        return to;
    }

    public static <F, T> List<T> transform(List<F> from, Converter<F, T> converter) {
        List<T> to = new ArrayList<T>(from.size());
        for (F f : from) {
            to.add(converter.convert(f));
        }
        return to;
    }    
    
    public static <T> List<T> toList(Iterable<T> iterable) {
        List<T> result = new ArrayList<>();
        for (T t : iterable) {
            result.add(t);
        }
        return result;
    }
    
    public static Iterable<NativeProject> findNativeProjects() {
        final Project[] projects = OpenProjects.getDefault().getOpenProjects();
        return new Iterable<NativeProject>() {
            @Override
            public Iterator<NativeProject> iterator() {
                return new Iterator<NativeProject>() {
                    
                    private int i = 0;
                    
                    private NativeProject nextProject = findNext();

                    @Override
                    public boolean hasNext() {
                        return nextProject != null;
                    }

                    @Override
                    public NativeProject next() {
                        NativeProject current = nextProject;
                        nextProject = findNext();
                        return current;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported."); // NOI18N
                    }
                    
                    private NativeProject findNext() {
                        NativeProject nativeProject = null;
                        while (nativeProject == null && i < projects.length) {
                            nativeProject = projects[i].getLookup().lookup(NativeProject.class);
                            ++i;
                        }
                        return nativeProject;
                    }
                };
            }
        };
    }
    
    public static CsmOffsetable findCppSymbol(String cppNames[]) {
        if (cppNames != null) {
            for (NativeProject nativeProject : findNativeProjects()) {
                for (String cppName : cppNames) {
                    Collection<CsmOffsetable> candidates = CsmSymbolResolver.resolveSymbol(nativeProject, cppName);
                    if (!candidates.isEmpty()) {
                        return candidates.iterator().next();
                    }
                }
            }
        }
        return null;
    }

    private MixedDevUtils() {
        throw new AssertionError("Not instantiable");
    }
}
