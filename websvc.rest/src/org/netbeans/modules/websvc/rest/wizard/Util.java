/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBean;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentSetupPanel;
import org.netbeans.modules.websvc.rest.support.Inflector;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;

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
    
    private static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; //NOI18N;
    
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
        return resourceName + EntityResourcesGenerator.RESOURCE_SUFFIX;
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
        if (name.endsWith("s")) {
            String plural = Inflector.getInstance().pluralize(name);
            if (! name.equals(plural)) {
                return name;
            }
        }
        return Inflector.getInstance().singularize(name);
    }
    
    public static String pluralize(String name) {
        return Inflector.getInstance().pluralize(name);
    }
    
    public static String getPluralName(EntityResourceBean bean) {
        if (bean.isContainer()) {
            return bean.getName();
        } else {
            return Inflector.getInstance().pluralize(bean.getName());
        }
    }
    
    public static String getSingularName(EntityResourceBean bean) {
        if (bean.isContainer()) {
            return Inflector.getInstance().singularize(bean.getName());
        } else {
            return bean.getName();
        }
    }

    /**
     * Displays a class chooser dialog and lets the user select a class.
     * @return the qualified name of the chosen class.
     */
    public static String chooseClass(Project project) {
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        try {
            final Node[] sourceGroupNodes
                    = new Node[sourceGroups.length];
            for (int i = 0; i < sourceGroupNodes.length; i++) {
                /*
                 * Note:
                 * Precise structure of this view is *not* specified by the API.
                 */
                Node srcGroupNode
                       = PackageView.createPackageView(sourceGroups[i]);
                sourceGroupNodes[i]
                       = new FilterNode(srcGroupNode,
                                        new JavaChildren(srcGroupNode));
            }
            
            Node rootNode;
            if (sourceGroupNodes.length == 1) {
                rootNode = new FilterNode(
                        sourceGroupNodes[0],
                        new JavaChildren(sourceGroupNodes[0]));
            } else {
                Children children = new Children.Array();
                children.add(sourceGroupNodes);
                
                AbstractNode node = new AbstractNode(children);
                node.setName("Project Source Roots");                   //NOI18N
                node.setDisplayName(
                        NbBundle.getMessage(Util.class, "LBL_Sources"));//NOI18N
                rootNode = node;
            }
            
            NodeAcceptor acceptor = new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    Node.Cookie cookie;
                    return nodes.length == 1
                           && (cookie = nodes[0].getCookie(DataObject.class))
                              != null
                           && ((DataObject) cookie).getPrimaryFile().isFolder()
                              == false;
                }
            };
            
            Node selectedNode = NodeOperation.getDefault().select(
                    NbBundle.getMessage(SingletonSetupPanelVisual.class,
                                        "LBL_WinTitle_SelectClass"),    //NOI18N
                    NbBundle.getMessage(SingletonSetupPanelVisual.class,
                                        "LBL_SelectRepresentationClass"),       //NOI18N
                    rootNode,
                    acceptor)[0];
            
            FileObject selectedFO = ((DataObject) selectedNode.getCookie(DataObject.class)).getPrimaryFile();
            return getClassName(selectedFO);
        } catch (UserCancelException ex) {
            // if the user cancels the choice, do nothing
        }
        return null;
    }
    
    private static String getClassName(FileObject file) {
        return ClassPath.getClassPath(file, ClassPath.SOURCE).getResourceName(file, '.', false);
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
                if (cl != null) {
                    return cl.loadClass(typeName);
                }
            } catch (ClassNotFoundException ex) {
                //Logger.getLogger(RestComponentSetupPanel.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
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
}
