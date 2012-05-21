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
package org.netbeans.modules.groovy.editor.api.parser;

import groovy.lang.GroovyClassLoader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;

/**
 *
 * @author Tomas Zezula
 */
//@NotThreadSafe //Should be guarded by parsing.api infrastructure
public final class ClassNodeCache {
    
    private static final Logger LOG =
            Logger.getLogger(ClassNodeCache.class.getName());
    private static final ThreadLocal<ClassNodeCache> instance = 
            new ThreadLocal<ClassNodeCache>();
    
    private static final int DEFAULT_NON_EXISTENT_CACHE_SIZE = 10000;
    private static final int NON_EXISTENT_CACHE_SIZE = Integer.getInteger(
            "groovy.editor.ClassNodeCache.nonExistent.size",
            DEFAULT_NON_EXISTENT_CACHE_SIZE);
    
    private final Map<CharSequence,ClassNode> cache;
    private final Map<CharSequence,Void> nonExistent;
    private Reference<JavaSource> resolver;
    private Reference<GroovyClassLoader> transformationLoaderRef;
    private Reference<GroovyClassLoader> resolveLoaderRef;
    private long invocationCount;
    private long hitCount;
    
    private ClassNodeCache() {
        this.cache = new HashMap<CharSequence, ClassNode>();
        this.nonExistent = new LinkedHashMap<CharSequence, Void>() {
            @Override
            protected boolean removeEldestEntry(Entry<CharSequence, Void> eldest) {
                if (size() > NON_EXISTENT_CACHE_SIZE) {
                    LOG.log(
                        Level.FINE,
                        "Non existent cache full, removing : {0}",    //NOI18N
                        eldest.getKey());
                    return true;
                }
                return false;
            }
        };
        LOG.fine("ClassNodeCache created");     //NOI18N
    }
    
    @CheckForNull
    public ClassNode get(@NonNull final CharSequence name) {        
        final ClassNode result = cache.get(name);
        if (LOG.isLoggable(Level.FINER)) {
            invocationCount++;
            if (result != null) {
                hitCount++;
            } else {
                LOG.log(
                    Level.FINEST,
                    "No binding for: {0}",   //NOI18N
                    name);
            }
            LOG.log(
                Level.FINER,
                "Hit ratio: {0}%",  //NOI18N
                (double)hitCount/invocationCount*100);
        }
        return result;
    }
    
    public boolean isNonExistent (@NonNull final CharSequence name) {
        final boolean res = nonExistent.containsKey(name);
        if (LOG.isLoggable(Level.FINER)) {
            invocationCount++;
            if (res) {
                hitCount++;
            } else {
                LOG.log(
                    Level.FINEST,
                    "No binding for: {0}",   //NOI18N
                    name);
            }
            LOG.log(
                Level.FINER,
                "Hit ratio: {0}%",  //NOI18N
                (double)hitCount/invocationCount*100);
        }
        return res;
    }
    
    public void put (
        @NonNull final CharSequence name,
        @NullAllowed final ClassNode node) {
        if (node != null) {
            LOG.log(
                Level.FINE,
                "Added binding for: {0}",    //NOI18N
                name);
            cache.put(name,node);
        } else {
            LOG.log(
                Level.FINE,
                "Added nonexistent class: {0}",    //NOI18N
                name);
            nonExistent.put(name, null);
        }
    }
    
    public boolean containsKey(@NonNull final CharSequence name) {
        final boolean result = cache.containsKey(name);
        if (LOG.isLoggable(Level.FINER)) {
            invocationCount++;
            if (result) {
                hitCount++;
            } else {
                LOG.log(
                    Level.FINEST,
                    "No binding for: {0}",   //NOI18N
                    name);
            }
            LOG.log(
                Level.FINER,
                "Hit ratio: {0}%",  //NOI18N
                (double)hitCount/invocationCount*100);
        }
        return result;
        
    }
    
    @NonNull
    public JavaSource createResolver(@NonNull final ClasspathInfo info) {
        JavaSource src = resolver == null ? null : resolver.get();
        if (src == null) {
            LOG.log(Level.FINE,"Javac resolver created.");  //NOI18N
            src = JavaSource.create(info);
            resolver = new SoftReference<JavaSource>(src);
        }
        return src;
    }
    
    public GroovyClassLoader createTransformationLoader(
            @NonNull final ClassPath allResources,
            @NonNull final CompilerConfiguration configuration) {        
        GroovyClassLoader transformationLoader = transformationLoaderRef == null ? null : transformationLoaderRef.get();
        if (transformationLoader == null) {
            LOG.log(Level.FINE,"Transformation ClassLoader created.");  //NOI18N
            transformationLoader = 
                new GroovyParser.TransformationClassLoader(
                    CompilationUnit.class.getClassLoader(),
                    allResources,
                    configuration);
            transformationLoaderRef = new SoftReference<GroovyClassLoader>(transformationLoader);
        }
        return transformationLoader;
    }
    
    public GroovyClassLoader createResolveLoader(
            @NonNull final ClassPath allResources,
            @NonNull final CompilerConfiguration configuration) {
        GroovyClassLoader resolveLoader = resolveLoaderRef == null ? null : resolveLoaderRef.get();
        if (resolveLoader == null) {
            LOG.log(Level.FINE,"Resolver ClassLoader created.");  //NOI18N
            resolveLoader = new GroovyParser.ParsingClassLoader(
                    allResources,
                    configuration,
                    this);
            resolveLoaderRef = new SoftReference<GroovyClassLoader>(resolveLoader);
        }
        return resolveLoader;
    }
    
    @NonNull
    public static ClassNodeCache get() {
        ClassNodeCache c = instance.get();
        if (c == null) {
            c = new ClassNodeCache();
        }
        return c;
    }    
    
    public static ClassNodeCache createThreadLocalInstance() {
        final ClassNodeCache c = new ClassNodeCache();
        instance.set(c);
        LOG.log(
            Level.FINE,
            "ClassNodeCache attached to thread: {0}",    //NOI18N
            Thread.currentThread().getId());
        return c;
    }
    
    public static void clearThreadLocalInstance() {        
        instance.remove();
        LOG.log(
            Level.FINE,
            "ClassNodeCache removed from thread: {0}",    //NOI18N
            Thread.currentThread().getId());
    }
}
