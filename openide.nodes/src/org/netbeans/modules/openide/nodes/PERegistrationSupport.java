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
package org.netbeans.modules.openide.nodes;

import java.beans.PropertyEditorManager;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Class for registering property editors.
 * 
 * @author Jan Horvath <jhorvath@netbeans.org>
 */
public final class PERegistrationSupport {
    
    static final String LOOKUP_PATH = "Services/PropertyEditorManager"; //NOI18N
    static final String PACKAGE = "packagePath"; //NOI18N
    static final String EDITOR_CLASS = "propertyEditorClass"; //NOI18N
    
    static ClassReg clsReg = null;
    static PkgReg pkgReg = null;
    
    public static synchronized void registerPropertyEditors() {
        if (clsReg == null) {
            clsReg = new ClassReg();
        }
        if (pkgReg == null) {
            pkgReg = new PkgReg();
        }
    }
    
    /**
     * Creates instance of <code>PEPackageRegistration</code> based on layer.xml
     * attribute values
     * 
     * @param attrs attributes loaded from layer.xml
     * @return
     */
    public static PEPackageRegistration createPackageRegistration(final Map attrs) {
        String pkg = (String) attrs.get(PACKAGE);
        return new PEPackageRegistration(pkg);
    }
    
    /**
     * Creates instance of <code>PEClassRegistration</code> based on layer.xml
     * attribute values
     * 
     * @param attrs attributes loaded from layer.xml
     * @return
     */
    public static PEClassRegistration createClassRegistration(final Map attrs) {
        String editorClass = (String) attrs.get(EDITOR_CLASS);
        Set<String> targetTypes = new LinkedHashSet<String> ();
        for (int i = 1; ; i++) {
            String targetType = (String) attrs.get("targetType." + i); //NOI18N
            if (targetType == null) {
                break;
            }
            targetTypes.add(targetType);
        }
        return new PEClassRegistration(editorClass, targetTypes);
    }
    
    /**
     * returns Class from canonical class name like <code>java.lang.String[]</code>
     */
    protected static Class<?> getClassFromCanonicalName(String name) throws ClassNotFoundException {
        Class<?> result;
        String type = name;
        int dimensions = 0;
        while (type.endsWith("[]")) { //NOI18N
            dimensions++;
            type = type.substring(0, type.length() - 2);
        }
        if ("byte".equals(type)) { //NOI18N
            result = byte.class;
        } else if ("short".equals(type)) { //NOI18N
            result = short.class;
        } else if ("char".equals(type)) { //NOI18N
            result = char.class;
        } else if ("int".equals(type)) { //NOI18N
            result = int.class;
        } else if ("long".equals(type)) { //NOI18N
            result = long.class;
        } else if ("float".equals(type)) { //NOI18N
            result = float.class;
        } else if ("double".equals(type)) { //NOI18N
            result = double.class;
        } else if ("boolean".equals(type)) { //NOI18N
            result = boolean.class;
        } else {
            ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
            result = Class.forName(type, true, clsLoader);
        }
        if (dimensions > 0) {
            int d[] = new int[dimensions];
            for (int i = 0; i < d.length; i++) {
                d[i] = 0;
            }
            result = Array.newInstance(result, d).getClass();
        }
        return result;
    }
    
    public static class PEPackageRegistration {
        final String pkg;

        PEPackageRegistration(String pkg) {
            this.pkg = pkg;
        }
    }
    
    public static class PEClassRegistration {
        final Set<String> targetTypes;
        final String editorClass;
        
        PEClassRegistration(String editorClass, Set<String> targetTypes) {
            this.editorClass = editorClass;
            this.targetTypes = targetTypes;
        }
    }
    
    private static final class ClassReg implements LookupListener {
        Result<PEClassRegistration> lookupResult = null;
        
        ClassReg() {
            lookupResult = Lookup.getDefault().lookupResult(PERegistrationSupport.PEClassRegistration.class);
            register();
            lookupResult.addLookupListener(this);
        }
                
        synchronized void register() {
            lookupResult = Lookup.getDefault().lookupResult(PERegistrationSupport.PEClassRegistration.class);
            ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
            
            for (PEClassRegistration clsReg : lookupResult.allInstances()) {
                for (String type : clsReg.targetTypes) {
                    try {
                        Class<?> cls = getClassFromCanonicalName(type);
                        Class<?> editorCls = Class.forName(clsReg.editorClass, true, clsLoader);
                        PropertyEditorManager.registerEditor(cls, editorCls);
                    } catch (ClassNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        
        @Override
        public void resultChanged(LookupEvent ev) {
            register();
        }
    }
    
    static final class PkgReg implements LookupListener {
        Result<PEPackageRegistration> pkgResult;
        private static List<String> originalPath = null;
        
        PkgReg() {
            if (originalPath == null) {
                originalPath = Arrays.asList(PropertyEditorManager.getEditorSearchPath());
            }
            pkgResult = Lookup.getDefault().lookupResult(PERegistrationSupport.PEPackageRegistration.class);
            register();
            pkgResult.addLookupListener(this);
        }
        
        synchronized void register() {
            pkgResult = Lookup.getDefault().lookupResult(PERegistrationSupport.PEPackageRegistration.class);
            Set<String> newPath = new HashSet<String> ();
            for (PEPackageRegistration pkgReg : pkgResult.allInstances()) {
                newPath.add(pkgReg.pkg);
            }
            newPath.addAll(originalPath);
            PropertyEditorManager.setEditorSearchPath(newPath.toArray(new String[newPath.size()]));
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            register();
        }
    }
}
