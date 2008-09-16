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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Container;
import javax.swing.JComponent;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBean;
import org.netbeans.modules.websvc.rest.support.Inflector;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 * Copy of j2ee/utilities Util class
 *  
 * TODO: Should move some of the methods into o.n.m.w.r.support.Utils class
 * since that's the package used for sharing all the utility classes.
 * 
 */
public class Util {
    public static final String TYPE_DOC_ROOT="doc_root"; //NOI18N
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if(label != null) {
            label.setText(newLabel);
        }
    }
    
    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if(label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if(c != null) {
                c.setVisible(false);
            }
        }
    }
    
    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents( Component[] components, Collection allComponents ) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }
    
    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        Vector allComponents = new Vector();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator iterator = allComponents.iterator();
        while(iterator.hasNext()) {
            Component c = (Component)iterator.next();
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the simple class for the passed fully-qualified class name.
     */
    public static String getClassName(String fqClassName) {
        int dot = fqClassName.lastIndexOf("."); // NOI18N
        if (dot >= 0 && dot < fqClassName.length() - 1) {
            return fqClassName.substring(dot + 1);
        } else {
            return fqClassName;
        }
    }
    
    /**
     * Returns the package name of the passed fully-qualified class name.
     */
    public static String getPackageName(String fqClassName) {
        int dot = fqClassName.lastIndexOf("."); // NOI18N
        if (dot >= 0 && dot < fqClassName.length() - 1) {
            return fqClassName.substring(0, dot);
        } else {
            return ""; // NOI18N
        }
    }
    
    /**
     * Returns the SourceGroup of the passesd project which contains the
     * fully-qualified class name.
     */
    public static SourceGroup getClassSourceGroup(Project project, String fqClassName) {
        String classFile = fqClassName.replace('.', '/') + ".java"; // NOI18N
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject classFO = sourceGroup.getRootFolder().getFileObject(classFile);
            if (classFO != null) {
                return sourceGroup;
            }
        }
        return null;
    }
    
    static final String WIZARD_PANEL_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; //NOI18N;
    
    public static void mergeSteps(WizardDescriptor wizard, WizardDescriptor.Panel[] panels, String[] steps) {
        Object prop = wizard.getProperty(WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        int offset;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            offset = beforeSteps.length;
            if (offset > 0 && ("...".equals(beforeSteps[offset - 1]))) {// NOI18N
                offset--;
            }
        } else {
            beforeSteps = null;
            offset = 0;
        }
        String[] resultSteps = new String[ (offset) + panels.length];
        for (int i = 0; i < offset; i++) {
            resultSteps[i] = beforeSteps[i];
        }
        setSteps(panels, steps, resultSteps, offset);
    }
    
    private static void setSteps(WizardDescriptor.Panel[] panels, String[] steps, String[] resultSteps, int offset) {
        int n = steps == null ? 0 : steps.length;
        for (int i = 0; i < panels.length; i++) {
            final JComponent component = (JComponent) panels[i].getComponent();
            String step = i < n ? steps[i] : null;
            if (step == null) {
                step = component.getName();
            }
            component.putClientProperty(WIZARD_PANEL_CONTENT_DATA, resultSteps);
            component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, new Integer(i));
            component.getAccessibleContext().setAccessibleDescription(step);
            resultSteps[i + offset] = step;
        }
    }
    
    public static void setSteps(WizardDescriptor.Panel[] panels, String[] steps) {
        setSteps(panels, steps, steps, 0);
    }
    
    public static String lowerFirstChar(String name) {
        if (name.length() == 0) return name;
        
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toLowerCase(name.charAt(0)));
        return sb.toString();
    }
    
    public static String upperFirstChar(String name) {
        if (name.length() == 0) return name;
        
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        return sb.toString();
    }
    
    public static String deriveResourceClassName(String resourceName) {
        return upperFirstChar(resourceName) + EntityResourcesGenerator.RESOURCE_SUFFIX;
    }

    public static String deriveUri(String resourceName, String currentUri) {
        if (resourceName.length() == 0 || currentUri == null || currentUri.length() == 0 || currentUri.charAt(0) != '/') {
            return currentUri;
        }
        resourceName = lowerFirstChar(resourceName);
        resourceName = pluralize(resourceName);
        String root = currentUri;
        String params = null;
        int lastIndex = currentUri.indexOf('{');
        if (lastIndex > -1) {
            params = root.substring(lastIndex-1);
            root = root.substring(0, lastIndex-1); /* ../{id} we are excluding the ending '/' */
            if (root.length() == 0) {
                return currentUri;
            }
        }


        lastIndex = root.lastIndexOf('/');
        if (lastIndex == -1) {
            return currentUri;
        }

        root = root.substring(0, lastIndex);
        String ret = root + "/" + resourceName;
        if (params != null) {
            ret += params;
        }
        return ret;
    }

    public static String deriveContainerClassName(String resourceName) {
        return deriveResourceClassName(Inflector.getInstance().pluralize((resourceName)));
    }
    
//    public static String singularize(String name) {
//        // get around inflector bug:  'address' -> 'addres'
//        if (name.endsWith("ss")) {
//            String plural = Inflector.getInstance().pluralize(name);
//            if (! name.equals(plural)) {
//                return name;
//            }
//        }
//        return Inflector.getInstance().singularize(name);
//    }
//    
    public static String pluralize(String name) {
        String pluralName = Inflector.getInstance().pluralize(name);
        
        if (name.equals(pluralName)) {
            return name + Constants.COLLECTION;         //NOI18N
        } else {
            return pluralName;
        }
    }

    public static String[] ensureTypes(String[] types) {
        if (types == null || types.length == 0 || types[0].length() == 0) {
            types = new String[] { String.class.getName() };
        }
        return types;
    }
    
    public static SourceGroup[] getSourceGroups(Project project) {
        SourceGroup[] sourceGroups = null;

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] docRoot = sources.getSourceGroups(TYPE_DOC_ROOT);
        SourceGroup[] srcRoots = SourceGroupSupport.getJavaSourceGroups(project);
            
        if (docRoot != null && srcRoots != null) {
            sourceGroups = new SourceGroup[docRoot.length + srcRoots.length];
            System.arraycopy(docRoot, 0, sourceGroups, 0, docRoot.length);
            System.arraycopy(srcRoots, 0, sourceGroups, docRoot.length, srcRoots.length);
        }
            
        if (sourceGroups==null || sourceGroups.length==0) {
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }
        return sourceGroups;
    }

    private static Map<String,Class> primitiveTypes;
    
    public static Class getType(Project project, String typeName) {    
        List<ClassPath> classPaths = SourceGroupSupport.gerClassPath(project);
        
        for (ClassPath cp : classPaths) {
            try {
                Class ret = Util.getPrimitiveType(typeName);
                if (ret != null) {
                    return ret;
                }
                ClassLoader cl = cp.getClassLoader(true);
                ret = getGenericRawType(typeName, cl);
                if (ret != null) {
                    return ret;
                }
                if (cl != null) {
                    return cl.loadClass(typeName);
                }
            } catch (ClassNotFoundException ex) {
                //Logger.global.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return null; 
    }
    
    public static Class getPrimitiveType(String typeName) {
        if (primitiveTypes == null) {
            primitiveTypes = new HashMap<String,Class>();
            primitiveTypes.put("int", Integer.class);
            primitiveTypes.put("int[]", int[].class);
            primitiveTypes.put("java.lang.Integer[]", Integer[].class);
            primitiveTypes.put("boolean", Boolean.class);
            primitiveTypes.put("boolean[]", boolean[].class);
            primitiveTypes.put("java.lang.Boolean[]", Boolean[].class);
            primitiveTypes.put("byte", Byte.class);
            primitiveTypes.put("byte[]", byte[].class);
            primitiveTypes.put("java.lang.Byte[]", Byte[].class);
            primitiveTypes.put("char", Character.class);
            primitiveTypes.put("char[]", char[].class);
            primitiveTypes.put("java.lang.Character[]", Character[].class);
            primitiveTypes.put("double", Double.class);
            primitiveTypes.put("double[]", double[].class);
            primitiveTypes.put("java.lang.Double[]", Double[].class);
            primitiveTypes.put("float", Float.class);
            primitiveTypes.put("float[]", float[].class);
            primitiveTypes.put("java.lang.Float[]", Float[].class);
            primitiveTypes.put("long", Long.class);
            primitiveTypes.put("long[]", long[].class);
            primitiveTypes.put("java.lang.Long[]", Long[].class);
            primitiveTypes.put("short", Short.class);
            primitiveTypes.put("short[]", short[].class);
            primitiveTypes.put("java.lang.Short[]", Short[].class);
        }
        return primitiveTypes.get(typeName);
    }
    
    public static Class getGenericRawType(String typeName, ClassLoader loader) {
        int i = typeName.indexOf('<');
        if (i < 1) {
            return null;
        }
        String raw = typeName.substring(0, i);
        try {
            return loader.loadClass(raw);
        } catch(ClassNotFoundException ex) {
            Logger.global.log(Level.INFO, "", ex);
            return null;
        }
    }
    
    public static boolean isValidPackageName(String packageName) {
        if (packageName == null || packageName.endsWith(".")) {
            return false;
        }
        
        String[] segments = packageName.split("\\.");
        for (String s : segments) {
            if (! Utilities.isJavaIdentifier(s)) {
                return false;
            }
        }
        return true;
    }
    
    public static ClasspathInfo getClasspathInfo(Project p) {
        FileObject fileObject = p.getProjectDirectory();
        return ClasspathInfo.create(
                ClassPath.getClassPath(fileObject, ClassPath.BOOT), // JDK classes
                ClassPath.getClassPath(fileObject, ClassPath.COMPILE), // classpath from dependent projects and libraries
                ClassPath.getClassPath(fileObject, ClassPath.SOURCE)); // source classpath
    }
}
