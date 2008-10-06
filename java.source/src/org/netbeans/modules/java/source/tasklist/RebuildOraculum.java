/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.source.tasklist;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class RebuildOraculum {
    
    private static final String DEPRECATED = "DEPRECATED"; //NOI18N

    private static Map<URL, Map<ElementHandle, Collection<String>>> url2Members = new HashMap<URL, Map<ElementHandle, Collection<String>>>();
    
    static synchronized void putMembers(URL file, Map<ElementHandle, Collection<String>> members) {
        url2Members.clear();
        url2Members.put(file, members);
    }
    
    private static synchronized Map<ElementHandle, Collection<String>> getMembers(URL file) {
        Map<ElementHandle, Collection<String>> result = url2Members.get(file);
        
        return result != null ? result : new HashMap<ElementHandle, Collection<String>>();
    }
    
    private static RebuildOraculum INSTANCE = new RebuildOraculum();
    
    public static RebuildOraculum get() {
        return INSTANCE;
    }
    
    private RebuildOraculum() {
    }
    
    private static String convertToSourceName (String binaryName) {
        binaryName = binaryName.replace ('.','/');  //NOI18N
        int index = binaryName.lastIndexOf('/');    //NOI18N
        if (index < 0) {
            index = 0;
        }
        index = binaryName.indexOf(index,'$');      //NOI18N
        if (index > 0) {
            binaryName = binaryName.substring(0, index);
        }
        return binaryName + ".java";                //NOI18N
    }
    
    private static final Pattern ANONYMOUS = Pattern.compile("\\$[0-9]"); //NOI18N
    
    public List<File> findFilesToRebuild(File root, URL file, ClasspathInfo cpInfo, Map<ElementHandle, Collection<String>> currentMembers) {
        long startTime = System.currentTimeMillis();
        long endTime   = -1;
        
        try {
        Logger.getLogger(RebuildOraculum.class.getName()).log(Level.FINE, "members={0}", getMembers(file));
        Logger.getLogger(RebuildOraculum.class.getName()).log(Level.FINE, "currentMembers={0}", currentMembers);
        
        Map<ElementHandle, Collection<String>> added = new HashMap<ElementHandle, Collection<String>>(currentMembers);
        
        for (ElementHandle h : getMembers(file).keySet()) {
            added.remove(h);
        }
        
        Map<ElementHandle, Collection<String>> removed = new HashMap<ElementHandle, Collection<String>>(getMembers(file));
        
        for (ElementHandle h : currentMembers.keySet()) {
            removed.remove(h);
        }
        
        Map<ElementHandle, Collection<String>> changedElements = new HashMap<ElementHandle, Collection<String>>(getMembers(file));
        
        for (Iterator<ElementHandle> it = changedElements.keySet().iterator(); it.hasNext(); ) {
            ElementHandle h = it.next();
            
            Collection<String> original = changedElements.get(h);
            Collection<String> current  = currentMembers.get(h);
            
            if (original == null || current == null || original.equals(current)) {
                it.remove();
            }
        }
        
        synchronized (RebuildOraculum.class) {
            if (url2Members.containsKey(file)) {
                putMembers(file, currentMembers);
            }
        }
        
        Collection<ElementHandle<TypeElement>> classes = new ArrayList<ElementHandle<TypeElement>>();
        
        //a really simple heuristics:
        if (!added.isEmpty() || !removed.isEmpty() || !changedElements.isEmpty()) {
            for (ElementHandle h : currentMembers.keySet()) {
                if (h.getKind().isClass() || h.getKind().isInterface()) {
                    classes.add(h);
                }
            }
        }
        
        if (classes.isEmpty()) {
            return Collections.<File>emptyList();
        }
        
        endTime = System.currentTimeMillis();
        
        ClassIndex ci = cpInfo.getClassIndex();
        
        return findAllDependent(root, file, ci, classes);
        } finally {
            if (endTime == (-1)) {
                endTime = System.currentTimeMillis();
            }
            
            if (file != null) {
                Logger.getLogger("TIMER").log(Level.FINE, "RebuildOraculum: findFilesToRebuild total",
                        new Object[] {file, System.currentTimeMillis() - startTime});
                Logger.getLogger("TIMER").log(Level.FINE, "RebuildOraculum: quick heuristics",
                        new Object[] {file, endTime - startTime});
            }
        }
    }
    
    public static List<File> findAllDependent(File root, URL file, ClassIndex ci, Collection<ElementHandle<TypeElement>> classes) {
        //performance: filter out anonymous innerclasses:
        for (Iterator<ElementHandle<TypeElement>> i = classes.iterator(); i.hasNext(); ) {
            if (ANONYMOUS.matcher(i.next().getBinaryName()).find()) {
                i.remove();
            }
        }
        
        Set<ElementHandle<TypeElement>> toParse = new HashSet<ElementHandle<TypeElement>>(classes);
        
        long start = System.currentTimeMillis();
        
        boolean changed = true;
        
        while (changed) {
            Set<ElementHandle<TypeElement>> orig = new HashSet<ElementHandle<TypeElement>>(toParse);
            
            for (ElementHandle<TypeElement> e : orig) {
                toParse.addAll(ci.getElements(e, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
            }
            
            changed = !orig.equals(toParse);
        }
        
        Set<ElementHandle<TypeElement>> orig = new HashSet<ElementHandle<TypeElement>>(toParse);
        
        for (ElementHandle<TypeElement> e : orig) {
            toParse.addAll(ci.getElements(e, EnumSet.complementOf(EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS)), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        }
        
        toParse.removeAll(classes);
        
        if (file != null) {
            Logger.getLogger("TIMER").log(Level.FINE, "Deps - Handles",
                new Object[] {file, System.currentTimeMillis() - start});
            Logger.getLogger("TIMER").log(Level.FINE, "Deps - Handles #",
                new Object[] {file, toParse.size()});
        }
        
        long cur = System.currentTimeMillis();
        
        Set<File> files = new HashSet<File>();
        
        for (ElementHandle<TypeElement> e : toParse) {
            String sourceName = convertToSourceName(e.getBinaryName());
            File source = new File(root, sourceName);
            
            if (source.canRead())
                files.add(FileUtil.normalizeFile(source));
            
//            FileObject f = SourceUtils.getFile(e, cpInfo);
//
//            if (f == null/* || !FileUtil.isParentOf(rootFO, f)*/)
//                continue;
//
//            files.add(FileUtil.toFile(f));
        }
        
        files.remove(null);
        
        if (file != null) {
            Logger.getLogger("TIMER").log(Level.FINE, "Deps - Files",
                new Object[] {file, System.currentTimeMillis() - cur});
            Logger.getLogger("TIMER").log(Level.FINE, "Deps - Files #",
                new Object[] {file, files.size()});
        }
        
        return new ArrayList<File>(files);
    }
    
    static synchronized boolean isInitialized(URL file) {
        return url2Members.containsKey(file);
    }
    
    private static Collection<String> getExtendedModifiers(Elements elements, Element el) {
        Set<String> result = new HashSet<String>();
        
        for (Modifier m : el.getModifiers()) {
            result.add(m.name());
        }
        
        if (elements.isDeprecated(el)) {
            result.add(DEPRECATED);
        }
        
        return result;
    }
    
    public static Map<ElementHandle, Collection<String>> sortOut(Elements elements, Iterable<? extends TypeElement> topLevelElements) {
        Map<ElementHandle, Collection<String>> types = new HashMap<ElementHandle, Collection<String>>();
        Queue<TypeElement> toHandle = new LinkedList<TypeElement>();
        for (TypeElement te : topLevelElements) {
            toHandle.offer(te);
        }
        
        while (!toHandle.isEmpty()) {
            TypeElement te = toHandle.poll();
            if (te==null) {
                //workaround for 6443073
                //see Symbol.java:601
                //see JavacTaskImpl.java:367
                continue;
            }
            
            types.put(ElementHandle.create(te), getExtendedModifiers(elements, te));
            
            for (Element e : te.getEnclosedElements()) {
                switch (e.getKind()) {
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                    toHandle.offer((TypeElement) e);
                    break;
                case METHOD:
                case FIELD:
                case ENUM_CONSTANT:
                    types.put(ElementHandle.create(e), getExtendedModifiers(elements, e));
                }
            }
        }
        
        return types;
    }
    
}
