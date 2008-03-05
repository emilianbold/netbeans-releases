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

package org.netbeans.modules.websvc.saas.codegen.java.support;

import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Container;
import java.io.IOException;
import javax.swing.JComponent;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.saas.codegen.java.JaxRsCodeGenerator;
import org.openide.util.Utilities;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.websvc.saas.codegen.java.AbstractGenerator;
import org.netbeans.modules.websvc.saas.codegen.java.Constants;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.java.model.GenericResourceBean;
import org.netbeans.modules.websvc.saas.codegen.java.model.JaxwsOperationInfo;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Copy of j2ee/utilities Util class
 *  
 * TODO: Should move some of the methods into o.n.m.w.r.support.Utils class
 * since that's the package used for sharing all the utility classes.
 * 
 */
public class Util {
    public static final String TYPE_DOC_ROOT="doc_root"; //NOI18N
    public static final String AT = "@"; //NOI18N
    public static final String APATH = AT + Constants.PATH_ANNOTATION;      //NOI18N
    public static final String AGET = AT + Constants.GET_ANNOTATION;      //NOI18N
    public static final String APOST = AT + Constants.POST_ANNOTATION;      //NOI18N
    public static final String APUT = AT + Constants.PUT_ANNOTATION;      //NOI18N
    public static final String ADELETE = AT + Constants.DELETE_ANNOTATION;      //NOI18N
    public static final String SCANNING_IN_PROGRESS = "ScanningInProgress";//NOI18N
    
    /*
     * Check if the primary file of d is a REST Resource
     */ 
    public static boolean isRestJavaFile(DataObject d) {
        try {
            if (d == null || !"java".equals(d.getPrimaryFile().getExt())) //NOI18N
            {
                return false;
            }
            EditorCookie ec = d.getCookie(EditorCookie.class);
            if (ec == null) {
                return false;
            }
            javax.swing.text.Document doc = ec.getDocument();
            if (doc != null) {
                String docText = doc.getText(0, doc.getLength());

                return (docText.indexOf(APATH) != -1) ||
                        (docText.indexOf(AGET) != -1) ||
                        (docText.indexOf(APOST) != -1) ||
                        (docText.indexOf(APUT) != -1) ||
                        (docText.indexOf(ADELETE) != -1);
            }
        } catch (BadLocationException ex) {
        }
        return false;
    }

    public static boolean isServlet(DataObject d) {
        try {
            if (d == null || !"java".equals(d.getPrimaryFile().getExt())) //NOI18N
            {
                return false;
            }
            EditorCookie ec = d.getCookie(EditorCookie.class);
            if (ec == null) {
                return false;
            }
            javax.swing.text.Document doc = ec.getDocument();
            if (doc != null) {
                String docText = doc.getText(0, doc.getLength());

                return (docText.indexOf("extends HttpServlet") != -1);
            }
        } catch (BadLocationException ex) {
        }
        return false;
    }

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
    
    static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; //NOI18N;

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
        return upperFirstChar(resourceName) + JaxRsCodeGenerator.RESOURCE_SUFFIX;
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
    
    public static String singularize(String name) {
        // get around inflector bug:  'address' -> 'addres'
        if (name.endsWith("ss")) {
            String plural = Inflector.getInstance().pluralize(name);
            if (! name.equals(plural)) {
                return name;
            }
        }
        return Inflector.getInstance().singularize(name);
    }
    
    public static String pluralize(String name) {
        return Inflector.getInstance().pluralize(singularize(name));
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
    private static HashSet<String> keywords;
    
    
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
            primitiveTypes.put("int[]", Integer[].class);
            primitiveTypes.put("boolean", Boolean.class);
            primitiveTypes.put("boolean[]", Boolean[].class);
            primitiveTypes.put("byte", Byte.class);
            primitiveTypes.put("byte[]", Byte[].class);
            primitiveTypes.put("char", Character.class);
            primitiveTypes.put("char[]", Character[].class);
            primitiveTypes.put("double", Double.class);
            primitiveTypes.put("double[]", Double[].class);
            primitiveTypes.put("float", Float.class);
            primitiveTypes.put("float[]", Float[].class);
            primitiveTypes.put("long", Long.class);
            primitiveTypes.put("long[]", Long[].class);
            primitiveTypes.put("short", Short.class);
            primitiveTypes.put("short[]", Short[].class);
        }
        return primitiveTypes.get(typeName);
    }
    
    public static boolean isKeyword(String name) {
        if (keywords == null) {
            keywords = new HashSet<String>();
            
            keywords.add("abstract");
            keywords.add("assert");
            keywords.add("boolean");
            keywords.add("break");
            keywords.add("byte");
            keywords.add("case");
            keywords.add("catch");
            keywords.add("char");
            keywords.add("class");
            keywords.add("const");
            keywords.add("continue");
            keywords.add("default");
            keywords.add("do");
            keywords.add("double");
            keywords.add("else");
            keywords.add("enum");
            keywords.add("extends");
            keywords.add("final");
            keywords.add("finally");
            keywords.add("float");
            keywords.add("for");
            keywords.add("goto");
            keywords.add("if");
            keywords.add("implements");
            keywords.add("import");
            keywords.add("instanceof");
            keywords.add("int");
            keywords.add("interface");
            keywords.add("long");
            keywords.add("native");
            keywords.add("new");
            keywords.add("package");
            keywords.add("private");
            keywords.add("protected");
            keywords.add("public");
            keywords.add("return");
            keywords.add("short");
            keywords.add("static");
            keywords.add("strictfp");
            keywords.add("super");
            keywords.add("switch");
            keywords.add("synchronized");
            keywords.add("this");
            keywords.add("throw");
            keywords.add("throws");
            keywords.add("transient");
            keywords.add("try");
            keywords.add("void");
            keywords.add("volatile");
            keywords.add("while");
        }
        
        return keywords.contains(name);
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
    
    public static String stripPackageName(String name) {
        int index = name.lastIndexOf(".");          //NOI18N
        
        if (index > 0) {
            return name.substring(index+1);
        }
        return name;
    }
    
    public static Collection<String> sortKeys(Collection<String> keys) {
        Collection<String> sortedKeys = new TreeSet<String>(
                new Comparator<String> () {
            public int compare(String str1, String str2) {
                return str1.compareTo(str2);
            }
        });
        
        sortedKeys.addAll(keys);
        return sortedKeys;
    }
    
    public static void showMethod(FileObject source, String methodName) throws IOException {
        try {
            DataObject dataObj = DataObject.find(source);          
            JavaSource javaSource = JavaSource.forFileObject(source);
            
            // Force a save to make sure to make sure the line position in
            // the editor is in sync with the java source.
            SaveCookie sc = (SaveCookie) dataObj.getCookie(SaveCookie.class);
     
            if (sc != null) {
                sc.save();
            }
            
            LineCookie lc = (LineCookie) dataObj.getCookie(LineCookie.class);
            
            if (lc != null) {
                Util.checkScanning(false);
                final long[] position = JavaSourceHelper.getPosition(javaSource, methodName);
                final Line line = lc.getLineSet().getOriginal((int) position[0]);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        line.show(Line.SHOW_SHOW, (int) position[1]);
                    }
                });
            }
        } catch (Exception de) {
            if(de instanceof IOException && de.getMessage().equals(Util.SCANNING_IN_PROGRESS)) {
                throw new IOException(Util.SCANNING_IN_PROGRESS);
            } else {
                de.printStackTrace();
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
            }
        }    
    }

    public static Method getValueOfMethod(Class type) {
        try {
            Method method = type.getDeclaredMethod("valueOf", String.class);
            if (method == null || ! Modifier.isStatic(method.getModifiers())) {
                return null;
            }
            return method;
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Constructor getConstructorWithStringParam(Class type) {
        try {
            return type.getConstructor(String.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    /** Finds all projects in given lookup. If the command is not null it will check 
     * whther given command is enabled on all projects. If and only if all projects
     * have the command supported it will return array including the project. If there
     * is one project with the command disabled it will return empty array.
     */
    public static Project[] getProjectsFromLookup(Lookup lookup) {    
        Set<Project> result = new HashSet<Project>();
        for (Project p : lookup.lookupAll(Project.class)) {
            result.add(p);
        }
        // Now try to guess the project from dataobjects
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                result.add( p );
            }
        }
        Project[] projectsArray = result.toArray(new Project[result.size()]);
        return projectsArray;
    }

    public static FileObject findBuildXml(Project project) {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    public static DataObject createDataObjectFromTemplate(String template, 
            FileObject targetFolder, String targetName) throws IOException {
        assert template != null;
        assert targetFolder != null;
        
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject templateFO = defaultFS.findResource(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);

        return templateDO.createFromTemplate(dataFolder, targetName);
    }
 
    public static String deriveResourceName(final String name) {
        return Inflector.getInstance().camelize(normailizeName(name) + GenericResourceBean.RESOURCE_SUFFIX);
    }

    public static String deriveUriTemplate(final String name) {
        return Inflector.getInstance().camelize(normailizeName(name), true) + "/"; //NOI18N
    }
    
    public static MimeType[] deriveMimeTypes(JaxwsOperationInfo[] operations) {
        if (String.class.getName().equals(operations[operations.length-1].getOperation().getReturnTypeName())) {
            return new MimeType[] { MimeType.HTML };
        } else {
            return new MimeType[] { MimeType.XML };//TODO  MimeType.JSON };
        }
    }
    
    public static String normailizeName(final String name) {
        String normalized = name;
        normalized = normalized.replaceAll("\\p{Punct}", "_");
        normalized = normalized.replaceAll("\\p{Space}", "_");
        return normalized;
    }

    public static boolean isScanningInProgress(boolean showMessage) {
        try {
            Thread.sleep(2000);
            if(SourceUtils.isScanInProgress()) {
                if(showMessage) {
                    String message = NbBundle.getMessage(AbstractGenerator.class, 
                            "MSG_ScanningInProgress"); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }
                return true;
            }
        } catch (InterruptedException ex) {
        }
        return false;
    }

    public static void checkScanning() throws IOException{
        if(Util.isScanningInProgress(true)) {
            throw new IOException(SCANNING_IN_PROGRESS);
        }
    }
    
    public static void checkScanning(boolean showMessage) throws IOException{
        if(Util.isScanningInProgress(showMessage)) {
            throw new IOException(SCANNING_IN_PROGRESS);
        }
    }
}
