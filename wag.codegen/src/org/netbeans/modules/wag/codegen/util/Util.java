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
package org.netbeans.modules.wag.codegen.util;

import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Container;
import java.awt.Dialog;
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
import org.openide.util.Utilities;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wag.codegen.Constants;
import org.netbeans.modules.wag.codegen.WagClientCodeGenerator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.wag.codegen.ui.CodeSetupPanel;
import org.netbeans.modules.wag.codegen.ui.ProgressDialog;
import org.netbeans.modules.wag.manager.model.WagServiceParameter;
import org.openide.DialogDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Copy of j2ee/utilities Util class
 *  
 * TODO: Should move some of the methods into o.n.m.w.r.support.Utils class
 * since that's the package used for sharing all the utility classes.
 * 
 */
public class Util {

    public static final String TYPE_DOC_ROOT = "doc_root"; //NOI18N
    public static final String AT = "@"; //NOI18N
    public static final String APATH = AT + Constants.PATH_ANNOTATION;      //NOI18N
    public static final String AGET = AT + Constants.GET_ANNOTATION;      //NOI18N
    public static final String APOST = AT + Constants.POST_ANNOTATION;      //NOI18N
    public static final String APUT = AT + Constants.PUT_ANNOTATION;      //NOI18N
    public static final String ADELETE = AT + Constants.DELETE_ANNOTATION;      //NOI18N
    public static final String SCANNING_IN_PROGRESS = "ScanningInProgress";//NOI18N
    public static final String BUILD_XML_PATH = "build.xml"; // NOI18N
    public static final String VAR_NAMES_RESULT = "result";
    public static final String WIZARD_PANEL_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    public static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; //NOI18N;
    
    public static boolean isJsp(Document doc) {
        if(doc == null)
            return false;
        Object mimeType = doc.getProperty("mimeType"); //NOI18N
        if (mimeType != null && "text/x-jsp".equals(mimeType)) { //NOI18N
            return true;
        }
        return false;
    }
    
    public static boolean isJava(Document doc) {
        if(doc == null)
            return false;
        Object mimeType = doc.getProperty("mimeType"); //NOI18N
        System.out.println("isJava() mimeType = " + mimeType);
        if (mimeType != null && "text/x-java".equals(mimeType)) { //NOI18N
            return true;
        }
        return false;
    }
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if (label != null) {
            label.setText(newLabel);
        }
    }

    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if (label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if (c != null) {
                c.setVisible(false);
            }
        }
    }

    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents(Component[] components, Collection allComponents) {
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                allComponents.add(components[i]);
                if (((Container) components[i]).getComponentCount() != 0) {
                    getAllComponents(((Container) components[i]).getComponents(), allComponents);
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
        while (iterator.hasNext()) {
            Component c = (Component) iterator.next();
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                if (label.getText().equals(labelText)) {
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

    public static String lowerFirstChar(String name) {
        if (name.length() == 0) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toLowerCase(name.charAt(0)));
        return sb.toString();
    }

    public static String upperFirstChar(String name) {
        if (name.length() == 0) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        return sb.toString();
    }

    public static String[] ensureTypes(String[] types) {
        if (types == null || types.length == 0 || types[0].length() == 0) {
            types = new String[]{String.class.getName()                    };
        }
        return types;
    }

    private static Map<String, Class> primitiveTypes;
    private static Map<String, Class> primitiveClassTypes;
    
    private static HashSet<String> keywords;

    public static Class getType(Project project, String typeName) {
        List<ClassPath> classPaths = new ArrayList<ClassPath>();
                
        //hack for PHP
        if(classPaths.size() == 0){
            try {
                Class ret = getPrimitiveClassType(typeName);
                if(ret != null){
                    return ret;
                }
                return Class.forName(typeName);
            } catch (ClassNotFoundException ex) {
                return java.lang.Object.class;
            }
        }

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
    
    
    public static Class getPrimitiveClassType(String type) {
        if (primitiveClassTypes == null) {
            primitiveClassTypes = new HashMap<String, Class>();
            primitiveClassTypes.put("int", Integer.TYPE);
            primitiveClassTypes.put("boolean", Boolean.TYPE);
            primitiveClassTypes.put("byte", Byte.TYPE);
            primitiveClassTypes.put("char", Character.TYPE);
            primitiveClassTypes.put("double", Double.TYPE);
            primitiveClassTypes.put("float", Float.TYPE);
            primitiveClassTypes.put("long", Long.TYPE);
            primitiveClassTypes.put("short", Short.TYPE);
        }
        return primitiveClassTypes.get(type);
    }

    public static Class getPrimitiveType(String typeName) {
        if (primitiveTypes == null) {
            primitiveTypes = new HashMap<String, Class>();
            primitiveTypes.put("int", Integer.TYPE);
            primitiveTypes.put("int[]", int[].class);
            primitiveTypes.put("boolean", Boolean.TYPE);
            primitiveTypes.put("boolean[]", boolean[].class);
            primitiveTypes.put("byte", Byte.TYPE);
            primitiveTypes.put("byte[]", byte[].class);
            primitiveTypes.put("char", Character.TYPE);
            primitiveTypes.put("char[]", char[].class);
            primitiveTypes.put("double", Double.TYPE);
            primitiveTypes.put("double[]", double[].class);
            primitiveTypes.put("float", Float.TYPE);
            primitiveTypes.put("float[]", float[].class);
            primitiveTypes.put("long", Long.TYPE);
            primitiveTypes.put("long[]", long[].class);
            primitiveTypes.put("short", Short.TYPE);
            primitiveTypes.put("short[]", short[].class);
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
        } catch (ClassNotFoundException ex) {
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
            if (!Utilities.isJavaIdentifier(s)) {
                return false;
            }
        }
        return true;
    }

    public static String stripPackageName(String name) {
        int index = name.lastIndexOf(".");          //NOI18N

        if (index > 0) {
            return name.substring(index + 1);
        }
        return name;
    }

    public static Collection<String> sortKeys(Collection<String> keys) {
        Collection<String> sortedKeys = new TreeSet<String>(
                new Comparator<String>() {

                    public int compare(String str1, String str2) {
                        return str1.compareTo(str2);
                    }
                });

        sortedKeys.addAll(keys);
        return sortedKeys;
    }

    public static Method getValueOfMethod(Class type) {
        try {
            Method method = type.getDeclaredMethod("valueOf", String.class);
            if (method == null || !Modifier.isStatic(method.getModifiers())) {
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
            if (p != null) {
                result.add(p);
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

        FileObject templateFO = FileUtil.getConfigFile(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);

        //Check if already exists
        String fileName = targetName;
        if (fileName == null) {
            fileName = templateFO.getName();
        }
        FileObject targetFO = targetFolder.getFileObject(fileName, templateFO.getExt());
        if (targetFO != null) {
            return DataFolder.find(targetFO);
        }

        return templateDO.createFromTemplate(dataFolder, targetName);
    }

 
    public static String getAuthenticatorClassName(String groupName) {
        return groupName + Constants.SERVICE_AUTHENTICATOR;
    }

    public static String getAuthorizationFrameClassName(String groupName) {
        return groupName + Constants.SERVICE_AUTHORIZATION_FRAME;
    }


    public static void showUnsupportedDropMessage(Object[] args) {
        String message = NbBundle.getMessage(CodeSetupPanel.class,
                "WARN_UnsupportedDropTarget", args); // NOI18N
        NotifyDescriptor desc = new NotifyDescriptor.Message(message,
                NotifyDescriptor.Message.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(desc);
    }
  

    private static boolean isPrimitive(String typeName) {
        return typeName.equals("integer") || typeName.equals("string") || typeName.equals("boolean") ||
                typeName.equals("float") || typeName.equals("long");  //NOI18N
    }

    public static String getQuotedValue(String value) {
        String normalized = value;
        if (normalized.startsWith("\"")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("\"")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return "\"" + normalized + "\"";
    }

    public static Document getDocument(FileObject f) throws IOException {
        try {
            DataObject d = DataObject.find(f);
            EditorCookie ec = d.getCookie(EditorCookie.class);
            Document doc = ec.openDocument();
            if (doc == null) {
                throw new IOException("Document cannot be opened for : " + f.getPath());
            }
            return doc;
        } catch (DataObjectNotFoundException ex) {
            throw new IOException("DataObject does not exist for : " + f.getPath());
        }
    }

  
    public static boolean showDialog(String displayName, List<WagServiceParameter> allParams,
            Document targetDoc) {
        if (!allParams.isEmpty()) {
            boolean showParamTypes = Util.isJava(targetDoc) || Util.isJsp(targetDoc);
            CodeSetupPanel panel = new CodeSetupPanel(allParams, showParamTypes);

            DialogDescriptor desc = new DialogDescriptor(panel,
                    NbBundle.getMessage(CodeSetupPanel.class,
                    "LBL_CustomizeSaasService", displayName));

            Dialog d = DialogDisplayer.getDefault().createDialog(desc);
            panel.setDialog(d);
            d.setVisible(true);
            Object response = (desc.getValue() != null) ? desc.getValue() : NotifyDescriptor.CLOSED_OPTION;
            if (response.equals(NotifyDescriptor.CANCEL_OPTION) ||
                    response.equals(NotifyDescriptor.CLOSED_OPTION)) {
                return false;
            }
        }
        return true;
    }
    
    public static void doGenerateCode(WagClientCodeGenerator codegen,
            ProgressDialog progress, List<Exception> errors) {
        try {
            codegen.initProgressReporting(progress.getProgressHandle());
            codegen.generate();
        } catch (IOException ex) {
            if (!ex.getMessage().equals(Util.SCANNING_IN_PROGRESS)) {
                errors.add(ex);
            }
        }
    }

}
