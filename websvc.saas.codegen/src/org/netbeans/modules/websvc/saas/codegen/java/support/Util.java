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

import com.sun.source.tree.ClassTree;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.saas.codegen.java.AbstractGenerator;
import org.netbeans.modules.websvc.saas.codegen.java.Constants;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.SaasCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.java.model.GenericResourceBean;
import org.netbeans.modules.websvc.saas.codegen.java.model.JaxwsOperationInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.ApiKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SaasAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication.Login;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SignedUrlAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
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
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication.Token;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;

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
    private static final String BUILD_XML_PATH = "build.xml"; // NOI18N
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
    
    public static boolean isJsp(DataObject d) {
        if (d != null && "jsp".equals(d.getPrimaryFile().getExt())) //NOI18N
        {
            return true;
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
        return project.getProjectDirectory().getFileObject(BUILD_XML_PATH);
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
        String resourceName = Inflector.getInstance().camelize(normailizeName(name) + GenericResourceBean.RESOURCE_SUFFIX);
        return resourceName.substring(0, 1).toUpperCase() + resourceName.substring(1);
    }

    public static String deriveMethodName(final String name) {
        return Inflector.getInstance().camelize(normailizeName(name), true);
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
    
    public static List<ParameterInfo> filterParametersByAuth(SaasAuthenticationType authType,
            SaasAuthentication auth, List<ParameterInfo> params) {
        List<ParameterInfo> filterParams = new ArrayList<ParameterInfo>();
        if(params != null) {
            for (ParameterInfo param : params) {
                if(authType == SaasAuthenticationType.API_KEY) {
                    ApiKeyAuthentication apiKey = (ApiKeyAuthentication)auth;
                    if(param.getName().equals(apiKey.getApiKeyName())) {
                        continue;
                    }
                } else if(authType == SaasAuthenticationType.SESSION_KEY) {
                    SessionKeyAuthentication sessionKey = (SessionKeyAuthentication)auth;
                    if(param.getName().equals(sessionKey.getApiKeyName()) || 
                            param.getName().equals(sessionKey.getSessionKeyName()) ||
                                param.getName().equals(sessionKey.getSigKeyName())) {
                        continue;
                    }
                } else if(authType == SaasAuthenticationType.SIGNED_URL) {
                    SignedUrlAuthentication signedUrl = (SignedUrlAuthentication)auth;
                    if(param.getName().equals(signedUrl.getSigKeyName())) {
                        continue;
                    }
                }
                filterParams.add(param);
            }
        }
        return filterParams;
    }
    
    public static List<ParameterInfo> filterParameters(List<ParameterInfo> params, ParamFilter[] filters) {
        List<ParameterInfo> filterParams = new ArrayList<ParameterInfo>();
        if(params != null) {
            for (ParameterInfo param : params) {
                for (ParamFilter filter : filters) {
                    if (filter == ParamFilter.FIXED && param.getFixed() != null) {
                        continue;
                    }
                    filterParams.add(param);
                }
            }
        }
        return filterParams;
    }
    
    public static String createSessionKeyLoginBodyForWeb(WadlSaasBean bean, 
            String groupName, String paramVariableName) throws IOException {
        String methodBody = "";
        if(bean.getAuthenticationType() != SaasAuthenticationType.SESSION_KEY)
            return null;
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication)bean.getAuthentication();
        Login login = sessionKey.getLogin();
        if(login != null) {
            String methodName = null;
            SessionKeyAuthentication.Method method = login.getMethod();
            if(method != null) {
                methodName = method.getHref();
                if(methodName == null)
                    return methodBody;
                else
                    methodName = methodName.startsWith("#")?methodName.substring(1):methodName;
            }
            String tokenName = getTokenName(sessionKey);
            String tokenMethodName = getTokenMethodName(sessionKey);
            methodBody += "    try {\n";
            methodBody += "        javax.servlet.http.HttpSession session = request.getSession(true);\n";
            methodBody += "        if ("+getVariableName(sessionKey.getSessionKeyName())+" != null) \n";
            methodBody += "            return;\n";
            methodBody += "        String "+tokenName+" = "+tokenMethodName+"("+getSessionKeyLoginArgumentsForWeb()+");\n";

            methodBody += "        if ("+tokenName+" != null) {\n";

            methodBody += "           session.removeAttribute(\""+groupName+"_auth_token\");\n";
            Map<String, String> tokenMap = new HashMap<String, String>();
            methodBody += getLoginBody(login, bean, groupName, tokenMap);
            for (Entry e : tokenMap.entrySet()) {
                String name = (String) e.getKey();
                String val = (String) e.getValue();
                methodBody += "              session.setAttribute(\""+groupName+"_"+val+"\", "+name+");\n";
            }

            methodBody += "              String returnUrl = (String) session.getAttribute(\""+groupName+"_return_url\");\n";
                
            methodBody += "              if (returnUrl != null) {\n";
            methodBody += "                session.removeAttribute(\""+groupName+"_return_url\");\n";
            methodBody += "                response.sendRedirect(returnUrl);\n";
            methodBody += "              }\n";
            methodBody += "            } else {\n";
            methodBody += "                session.setAttribute(\""+groupName+"_return_url\", request.getRequestURI());\n";
            methodBody += "                response.sendRedirect(\""+groupName+"Login\");\n";
            methodBody += "            }\n";
            methodBody += "        } catch (IOException ex) {\n";
            methodBody += "            Logger.getLogger("+groupName+Constants.SERVICE_AUTHENTICATOR+".class.getName()).log(Level.SEVERE, null, ex);\n";
            methodBody += "        }\n\n";
        }
        return methodBody;
    }
    
    public static String getLoginBody(Login login, WadlSaasBean bean, String groupName, Map<String, String> tokenMap) throws IOException {
        SessionKeyAuthentication.Method method = login.getMethod();
        String methodName = null;
        if(method != null) {
            methodName = method.getHref();
            if(methodName == null)
                return "";
            else
                methodName = methodName.startsWith("#")?methodName.substring(1):methodName;
        } 
        String methodBody = "";
        methodBody += "                    String method = \""+methodName+"\";\n";
        methodBody += "                    String v = \"1.0\";\n\n";
        String sigId = "sig";
        if(login.getSignId() != null)
            sigId = login.getSignId();
        List<ParameterInfo> signParams = login.getParameters();
        if(signParams != null && signParams.size() > 0) {
            String paramStr = "";
            paramStr += "        String "+sigId+" = sign(secret, \n";
            paramStr += getSignParamUsage(signParams, groupName);
            paramStr += ");\n\n";
            methodBody += paramStr;
        }

        String queryParamsCode = "";
        if(method != null) {
            String id = method.getId();
            if(id != null) {
                String[] tokens = id.split(",");
                for (String token : tokens) {
                    String[] tokenElem = token.split("=");
                    if (tokenElem.length == 2) {
                        String val = tokenElem[1];
                        if (val.startsWith("{")) {
                            val = val.substring(1);
                        }
                        if (val.endsWith("{")) {
                            val = val.substring(val.length() - 1);
                        }
                        tokenMap.put(getVariableName(tokenElem[0]), val);
                    }
                }
            }
            String href = method.getHref();
            if(href != null) {
                Application app = bean.getMethod().getSaas().getWadlModel();
                org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethod = 
                        SaasUtil.wadlMethodFromIdRef(app, href);
                if(wadlMethod != null) {
                    ArrayList<ParameterInfo> params = bean.findWadlParams(wadlMethod);
                    Resource parentResource = SaasUtil.getParentResource(app, wadlMethod);
                    if(parentResource != null)
                        bean.findWadlParams(params, parentResource.getParam());
                    if(params != null && 
                            params.size() > 0) {
                        queryParamsCode = Util.getHeaderOrParameterDefinition(params, Constants.QUERY_PARAMS, false);
                    }
                }
            }
        }

        //Insert parameter declaration
        methodBody += "        "+queryParamsCode;

        methodBody += "             "+Constants.REST_CONNECTION+" conn = new "+Constants.REST_CONNECTION+"(\""+bean.getUrl()+"\"";
        if(!queryParamsCode.trim().equals(""))
            methodBody += ", "+Constants.QUERY_PARAMS;
        methodBody += ");\n";

        methodBody += "                    String result = conn.get();\n";

        for (Entry e : tokenMap.entrySet()) {
            String name = (String) e.getKey();
            String val = (String) e.getValue();
            methodBody += "                    "+name+" = result.substring(result.indexOf(\"<"+val+">\") + 13,\n";
            methodBody += "                            result.indexOf(\"</"+val+">\"));\n\n";
        }
        return methodBody;
    }
    
    /*
     */ 
    public static String getSignParamUsage(List<ParameterInfo> signParams, 
            String groupName) {
        String paramStr = "                new String[][] {\n";
        for(ParameterInfo p:signParams) {
            String name = p.getName();
            String varName = getVariableName(name);
            String[] pIds = getParamIds(p, groupName);
            if(pIds != null) {//process special case
                varName = pIds[1];
            }
            paramStr += "                    {\""+name+"\", "+varName+"},\n";
        }
        paramStr += "        }\n";
        return paramStr;
    }
        
    public static String[] getParamIds(ParameterInfo p, String groupName) {
        if(p.getId() != null) {//process special case
            String[] pElems = p.getId().split("=");
            if(pElems.length == 2) {
                String val = pElems[1];
                if(val.startsWith("{"))
                    val = val.substring(1);
                if(val.endsWith("}"))
                    val = val.substring(0, val.length()-1);
                val = getVariableName(val);
                //val = getAuthenticatorClassName(groupName)+ "." +
                //        "get"+val.substring(0,1).toUpperCase()+val.substring(1)+"()";
                return new String[]{pElems[0], val};
            }
        }
        return null;
    }
    
    public static String getAuthenticatorClassName(String groupName) {
        return groupName+Constants.SERVICE_AUTHENTICATOR;
    }
    
    public static String getAuthorizationFrameClassName(String groupName) {
        return groupName+Constants.SERVICE_AUTHORIZATION_FRAME;
    }
    
    public static String createSessionKeyTokenBodyForWeb(WadlSaasBean bean, 
            String groupName, String paramVariableName, String saasServicePackageName) throws IOException {
        String methodBody = "";
        methodBody += "        javax.servlet.http.HttpSession session = request.getSession(true);\n";
        methodBody += "        return (String) session.getAttribute(\""+groupName+"_auth_token\");\n";
        return methodBody;
    }
     
    public static String getTokenMethodName(SessionKeyAuthentication sessionKey) {
        String methodName = getTokenName(sessionKey);
        return methodName = "get"+methodName.substring(0,1).toUpperCase()+methodName.substring(1);
    }
    
    public static String getTokenName(SessionKeyAuthentication sessionKey) {
        String methodName = "token";
        Token token = sessionKey.getToken();
        if(token != null && token.getId() != null) {
            methodName = token.getId();
        }
        return getVariableName(methodName);
    }
    
    public static String getHeaderOrParameterUsage(List<ParameterInfo> params) {
        String paramUsage = "";
        for (ParameterInfo param : params) {
            String name = getParameterName(param, true, true, true);
            paramUsage +=  name + ", ";
        }
        if (params.size() > 0) {
            paramUsage = paramUsage.substring(0, paramUsage.length() - 2);
        }
        return paramUsage;
    }
    
    public static String getHeaderOrParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate) {
        String paramsStr = null;
        StringBuffer sb = new StringBuffer();
        for (ParameterInfo param : params) {
            String paramName = getParameterName(param);
            String paramVal = null;
            if(evaluate || param.isApiKey()) {
                paramVal = findParamValue(param);
                if (param.getType() != String.class) {
                    sb.append("{\"" + paramName + "\", \"" + paramVal + "\".toString()},\n");
                } else {
                    if(paramVal != null)
                        sb.append("{\"" + paramName + "\", \"" + paramVal + "\"},\n");
                    else
                        sb.append("{\"" + paramName + "\", null},\n");
                }
            } else {
                sb.append("{\"" + paramName + "\", " + getParameterName(param, true, true, true) + "},\n");
            }
        }
        paramsStr = sb.toString();
        if (params.size() > 0) {
            paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
        }
        
        String paramCode = "";
        paramCode += "             String[][] "+varName+" = new String[][]{\n";
        paramCode += "                 " + paramsStr + "\n";
        paramCode += "             };\n";
        return paramCode;
    }
    
    public static String getParameterName(ParameterInfo param) {
        return param.getName();
    }
    
    public static String getParameterName(ParameterInfo param, 
            boolean camelize, boolean normalize) {
        return getParameterName(param, camelize, normalize, false);
    }
    
    public static String getParameterName(ParameterInfo param, 
            boolean camelize, boolean normalize, boolean trimBraces) {
        String name = param.getName();
        if (Util.isKeyword(name)) {
            name += "Param";
        }
        
        if(trimBraces && param.getStyle() == ParamStyle.TEMPLATE 
                && name.startsWith("{") && name.endsWith("}")) {
            name = name.substring(0, name.length()-1);
        }
        return getParameterName(name, camelize, normalize);
    }
    
    public static String getParameterName(String name, 
            boolean camelize, boolean normalize) {
        if(normalize) {
            name = Util.normailizeName(name);
        }
        if(camelize) {
            name = Inflector.getInstance().camelize(name, true);
        }
        return name;
    }
    
    
    public static String getVariableName(String name) {
        return getVariableName(name, true, true, true);
    }
    
    public static String getVariableName(final String name, 
            boolean camelize, boolean normalize, boolean trimBraces) {
        String varName = name;
        if(trimBraces && varName.startsWith("{") && varName.endsWith("}")) {
            varName = varName.substring(0, varName.length()-1);
        }
        if(normalize) {
            varName = Util.normailizeName(varName);
        }
        if(camelize) {
            varName = Inflector.getInstance().camelize(varName, true);
        }
        if (Util.isKeyword(varName)) {
            varName += "Param";
        }
        return varName;
    }
    
    public static String findParamValue(ParameterInfo param) {
        String paramVal = null;
        if(param.isApiKey()) {
            paramVal = "\"+apiKey+\"";
        } else if(param.getStyle() == ParamStyle.TEMPLATE) {
            if(param.getDefaultValue() != null)
                paramVal = param.getDefaultValue().toString();
            else
                paramVal = "";
        } else if(param.getStyle() == ParamStyle.HEADER) {
            if(param.getDefaultValue() != null)
                paramVal = param.getDefaultValue().toString();
            else
                paramVal = getParameterName(param).toLowerCase();
        } else {
            if(param.isFixed())
                paramVal = param.getFixed();
            else {
                if(param.isRequired())
                    paramVal = "";
                if(param.getDefaultValue() != null)
                    paramVal = param.getDefaultValue().toString();
            }
        }
        return paramVal;
    }

    public static void createSessionKeyAuthorizationClassesForWeb(
            WadlSaasBean bean, Project project,
            String groupName, String saasServicePackageName, FileObject targetFolder, 
            JavaSource loginJS, FileObject loginFile, 
            JavaSource callbackJS, FileObject callbackFile,
            final String[] parameters, final Object[] paramTypes) throws IOException {
        SaasAuthenticationType authType = bean.getAuthenticationType();
        if(authType == SaasAuthenticationType.SESSION_KEY) {
            
            String fileId = "Login";// NoI18n
            String methodName = "processRequest";// NoI18n
            String authFileName = groupName+fileId;
            loginJS = JavaSourceHelper.createJavaSource(
                    SaasCodeGenerator.TEMPLATES_SAAS+authType.getClassIdentifier()+fileId+".java",
                    targetFolder, saasServicePackageName, authFileName);// NOI18n
            Set<FileObject> files = new HashSet<FileObject>(loginJS.getFileObjects());
            if (files != null && files.size() > 0) {
                loginFile = files.iterator().next();
            }
            
            if(!JavaSourceHelper.isContainsMethod(loginJS, methodName, parameters, paramTypes)) {
                addServletMethod(bean, groupName, methodName, loginJS, 
                    parameters, paramTypes, 
                    "{ \n" + getServletLoginBody(bean, groupName) + "\n }");
            }
            
            fileId = "Callback";// NOI18n
            authFileName = groupName+fileId;
            callbackJS = JavaSourceHelper.createJavaSource(
                    SaasCodeGenerator.TEMPLATES_SAAS+authType.getClassIdentifier()+fileId+".java",
                    targetFolder, saasServicePackageName, authFileName);// NOI18n
            files = new HashSet<FileObject>(callbackJS.getFileObjects());
            if (files != null && files.size() > 0) {
                callbackFile = files.iterator().next();
            }
            
            if(!JavaSourceHelper.isContainsMethod(callbackJS, methodName, parameters, paramTypes)) {
                addServletMethod(bean, groupName, methodName, callbackJS, 
                    parameters, paramTypes, 
                    "{ \n" + getServletCallbackBody(bean, groupName) + "\n }");
            }
            //Make entry into web.xml for login and callback servlets
            Map<String, String> filesMap = new HashMap<String, String>();
            filesMap.put(loginFile.getName(), saasServicePackageName+"."+loginFile.getName());
            filesMap.put(callbackFile.getName(), saasServicePackageName+"."+callbackFile.getName());
            addAuthorizationClassesToWebDescriptor(project, filesMap);
        }
    }
    
    public static String getServletLoginBody(WadlSaasBean bean, 
            String groupName) throws IOException {
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication)bean.getAuthentication();
        SessionKeyAuthentication.Token token = sessionKey.getToken();
        SessionKeyAuthentication.Token.Prompt prompt = token.getPrompt();
        String url = prompt.getWebUrl();
        String tokenName = "authToken";
        String tokenId = "auth_token";
        if(token != null) {
            tokenName = Util.getTokenName(sessionKey);
            tokenId = token.getId()!=null?token.getId():tokenId;
        }
        String methodBody = "";
        methodBody += "        response.setContentType(\"text/html;charset=UTF-8\");\n";
        methodBody += "        PrintWriter out = response.getWriter();\n";
        methodBody += "        try {\n";
        methodBody += "            out.println(\"<html>\");\n";
        methodBody += "            out.println(\"<head>\");\n";
        methodBody += "            out.println(\"<title>Servlet "+groupName+"Login</title>\");\n";
        methodBody += "            out.println(\"</head>\");\n";
        methodBody += "            out.println(\"<body>\");\n";
        methodBody += "            out.println(\"<h1>Servlet "+groupName+"Login at \" + request.getContextPath() + \"</h1>\");\n";
            
        methodBody += "            HttpSession session = request.getSession(true);\n";
            
        methodBody += "            String "+tokenName+" = (String) session.getAttribute(\""+groupName+"_"+tokenId+"\");\n";
            
        methodBody += "            if ("+tokenName+" != null) {\n";
        methodBody += "                out.println(\"<p>Already logged in.</b>\");\n";
        methodBody += "            } else {\n";
        String apiKeyName = getVariableName(sessionKey.getApiKeyName());
        methodBody += "                String apiKey = "+groupName+
                Constants.SERVICE_AUTHENTICATOR+".get"+
                apiKeyName.substring(0,1).toUpperCase()+apiKeyName.substring(1)+"();\n";
        methodBody += "                String loginUrl = \"<a href="+Util.getTokenPromptUrl(token, url) +">"+groupName+" Login</a>\";\n";
        methodBody += "                out.println(loginUrl);\n";
        methodBody += "            }\n";
        methodBody += "            out.println(\"</body>\");\n";
        methodBody += "            out.println(\"</html>\");\n";
        methodBody += "        } finally {\n";
        methodBody += "            out.close();\n";
        methodBody += "        }\n";
        return methodBody;
    }
    
    public static String getServletCallbackBody(WadlSaasBean bean, 
            String groupName) throws IOException {
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication)bean.getAuthentication();
        String sessionKeyName = getVariableName(sessionKey.getSessionKeyName());
        String tokenName = "authToken";
        String tokenId = "auth_token";
        Token token = sessionKey.getToken();
        if(token != null) {
            tokenName = Util.getTokenName(sessionKey);
            tokenId = token.getId()!=null?token.getId():tokenId;
        }
        String name = Util.getParameterName(sessionKey.getSessionKeyName(), true, true);
        String methodBody = "";
        methodBody += "        response.setContentType(\"text/html;charset=UTF-8\");\n";
        methodBody += "        PrintWriter out = response.getWriter();\n";
        methodBody += "        try {\n";
        methodBody += "            HttpSession session = request.getSession(true);\n";
        methodBody += "            String "+tokenName+" = request.getParameter(\""+tokenId+"\");\n";
        methodBody += "            session.setAttribute(\""+groupName+"_"+tokenId+"\", "+tokenName+");\n";
            
        methodBody += "            "+groupName+Constants.SERVICE_AUTHENTICATOR+".login("+getSessionKeyLoginArgumentsForWeb()+");\n";
        methodBody += "            String "+sessionKeyName+" = "+groupName+Constants.SERVICE_AUTHENTICATOR+"."+Util.getSessionKeyMethodName(name)+"();\n";
         
        methodBody += "            out.println(\"<html>\");\n";
        methodBody += "            out.println(\"<head>\");\n";
        methodBody += "            out.println(\"<title>Servlet "+groupName+"Callback</title>\");\n";
        methodBody += "            out.println(\"</head>\");\n";
        methodBody += "            out.println(\"<body>\");\n";
        methodBody += "            out.println(\"<h1>Servlet "+groupName+"Callback at \" + request.getContextPath() + \"</h1>\");\n";
        methodBody += "            out.println(\"<p> Your Session Key is \" + "+sessionKeyName+" + \"</p>\");\n";
        methodBody += "            out.println(\"</body>\");\n";
        methodBody += "            out.println(\"</html>\");\n";

        methodBody += "        } finally {\n";
        methodBody += "            out.close();\n";
        methodBody += "        }\n";
        return methodBody;
    }
    
    public static String getSessionKeyMethodName(String name) {
         String methodName = getVariableName(name);
         methodName = "get"+methodName.substring(0, 1).toUpperCase()+methodName.substring(1);
         return methodName;
    }
    
    public static String getTokenPromptUrl(SessionKeyAuthentication.Token token,
            String url) {
        String loginUrl = "";
        if (token.getPrompt() != null) {
            int index = url.indexOf("?");
            loginUrl =  url.substring(0, index + 1);
            String params = url.substring(index + 1);
            String[] tokens = params.split("&");
            for (String tokenE : tokens) {
                String[] tokenElem = tokenE.split("=");
                if (tokenElem.length == 2) {
                    String paramVal = tokenElem[1];
                    if (paramVal.startsWith("{"))
                        paramVal = paramVal.substring(1);
                    if (paramVal.endsWith("}"))
                        paramVal = paramVal.substring(0, paramVal.length() - 1);
                    
                    if (paramVal.indexOf(":") != -1) {
                        loginUrl += tokenElem[0] + "=" + getVariableName(paramVal.substring(paramVal.indexOf(":") + 1)) + "&";
                    } else {
                        loginUrl += tokenElem[0] + "=\"+" + getVariableName(paramVal) + "+\"&";
                    }
                    
                }
            }
            if(loginUrl.endsWith("+\"&"))
                loginUrl = loginUrl.substring(0, loginUrl.length() - 3);
            else if(loginUrl.endsWith("&"))
                loginUrl = loginUrl.substring(0, loginUrl.length() - 1);
        }
        return loginUrl;
    }
    
    public static List<ParameterInfo> getAuthenticatorMethodParametersForWeb() {
        List<ParameterInfo> params = new ArrayList<ParameterInfo>();
        params.add(new ParameterInfo("request", Object.class, 
                "javax.servlet.http.HttpServletRequest"));
        params.add(new ParameterInfo("response", Object.class, 
                "javax.servlet.http.HttpServletResponse"));
        return params;
    }

    public static List<ParameterInfo> getServiceMethodParametersForWeb(WadlSaasBean bean) {
        List<ParameterInfo> params = new ArrayList<ParameterInfo>();
        params.addAll(getAuthenticatorMethodParametersForWeb());
        params.addAll(bean.filterParametersByAuth(bean.filterParameters(
                new ParamFilter[]{ParamFilter.FIXED})));
        return params;
    }
    
    public static String getSessionKeyLoginArgumentsForWeb() {
        return getHeaderOrParameterUsage(getAuthenticatorMethodParametersForWeb());
    }
    
    /**
     *  Return target and generated file objects
     */
    public static void addServletMethod(final WadlSaasBean bean, 
            String groupName, final String methodName, final JavaSource source, 
            final String[] parameters, final Object[] paramTypes, 
            final String bodyText) throws IOException {
                
        if(JavaSourceHelper.isContainsMethod(source, methodName, parameters, paramTypes))
            return;
        
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);

                javax.lang.model.element.Modifier[] modifiers = Constants.PROTECTED;

                String type = Constants.VOID;

                String comment = "Retrieves representation of an instance of " + bean.getQualifiedClassName() + "\n";// NOI18N
                for (String param : parameters) {
                    comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", param);// NOI18N
                }
                comment += "@return an instance of "+type;// NOI18N
                ClassTree initial = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree tree = JavaSourceHelper.addMethod(copy, initial,
                        modifiers, null, null,
                        methodName, type, parameters, paramTypes,
                        null, null, new String[]{"javax.servlet.ServletException", "java.io.IOException"},
                        bodyText, comment);      //NOI18N
                copy.rewrite(initial, tree);
            }
            
        });
        result.commit();
    }

    public static FileObject getWebXmlFile(Project p) {
        SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups("web");
        for (SourceGroup group : groups) {
            FileObject root = group.getRootFolder();
            java.util.Enumeration<? extends FileObject> files = root.getData(true);
            while (files.hasMoreElements()) {
                FileObject fobj = files.nextElement();
                if(fobj.getNameExt().equals("web.xml")) {
                    return fobj;
                }
            }
        }
        return null;
    }
    
    public static void addAuthorizationClassesToWebDescriptor(Project p, 
            Map<String, String> filesMap) throws IOException {
        for(Map.Entry e:filesMap.entrySet()) {
            String name = (String) e.getKey();
            String qName = (String) e.getValue();
            addServiceEntriesToDD(p, name, qName);
        }
    }
    
    /**
     * This is to support non-JSR 109 containers. In this case, a regular jaxws web service
     * is created and the deployment descriptor is updated with the jaxws-ri servlet and
     * listener.
     */
    public static void addServiceEntriesToDD(Project p, String servletName,
            String servletClassName) {
        WebApp webApp = getWebApp(p);
        if(webApp != null){
            Servlet servlet = null;
            Listener listener = null;
            try{
                servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"},
                        new Object[]{servletName,servletClassName}, "ServletName");
                servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                ServletMapping servletMapping = (ServletMapping)
                webApp.addBean("ServletMapping", new String[]{"ServletName","UrlPattern"},
                        new Object[]{servletName, "/" + servletName}, "ServletName");
                // This also saves server specific configuration, if necessary.
                webApp.write(getDeploymentDescriptor(p));
            } catch (ClassNotFoundException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            } catch (NameAlreadyUsedException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            } catch (IOException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            }
        }
    }
    
    public static FileObject getDeploymentDescriptor(Project p) {
        FileObject webInfFo = getWebInf(p);
        if (webInfFo==null) {
            if (isProjectOpened(p)) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(Util.class,"MSG_WebInfCorrupted"), // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE));
            }
            return null;
        }
        return getWebInf(p).getFileObject("web.xml");//NoI18n
    }
    
    public static FileObject getWebInf(Project p) {
        WebModule webModule = WebModule.getWebModule(p.getProjectDirectory());
        if(webModule != null){
            return webModule.getWebInf();
        }
        return null;
    }
    
    public static WebApp getWebApp(Project p) {
        try {
            FileObject deploymentDescriptor = getDeploymentDescriptor(p);
            if(deploymentDescriptor != null) {
                return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, e.getLocalizedMessage());
        }
        return null;
    }
    
    public static boolean isProjectOpened(Project p) {
        // XXX workaround: OpenProjects.getDefault() can be null
        // when called from ProjectOpenedHook.projectOpened() upon IDE startup
        if (OpenProjects.getDefault() == null)
            return true;
        
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(p))
                return true;
        }
        return false;
    }
}
