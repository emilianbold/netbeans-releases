/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.libraries;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.loaders.DataObject;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.j2ee.ejbjarproject.UpdateHelper;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;


/**
 * This class decorates package nodes and file nodes under the Libraries Nodes.
 * It removes all actions from these nodes except of file node's {@link OpenAction}
 * and package node's {@link FindAction} It also adds the {@link ShowJavadocAction}
 * to both file and package nodes. It also adds {@link RemoveClassPathRootAction} to
 * class path roots.
 */
class ActionFilterNode extends FilterNode {

    private static final int MODE_ROOT = 1;
    private static final int MODE_PACKAGE = 2;
    private static final int MODE_FILE = 3;
    private static final int MODE_FILE_CONTENT = 4;

    private final int mode;
    private Action[] actionCache;

    /**
     * Creates new ActionFilterNode for class path root
     * @param original the original node
     * @param helper used for implementing {@link RemoveClassPathRootAction.Removable} or null if
     * the node should not have the {@link RemoveClassPathRootAction}
     * @param classPathId ant property name of classpath to which these classpath root belongs or null if
     * the node should not have the {@link RemoveClassPathRootAction}
     * @param entryId ant property name of this classpath root or null if
     * the node should not have the {@link RemoveClassPathRootAction}
     * @return ActionFilterNode
     */
    static ActionFilterNode create (Node original, UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper, 
                                    String classPathId, String entryId, String includedLibrariesElement) {
        DataObject dobj = (DataObject) original.getLookup().lookup(DataObject.class);
        assert dobj != null;
        FileObject root =  dobj.getPrimaryFile();
        Lookup lkp = new ProxyLookup (new Lookup[] {original.getLookup(), helper == null ?
            Lookups.singleton (new JavadocProvider(root,root)) :
            Lookups.fixed (new Object[] {new Removable (helper, eval, refHelper, classPathId, entryId, includedLibrariesElement),
            new JavadocProvider(root,root)})});
        return new ActionFilterNode (original, helper == null ? MODE_PACKAGE : MODE_ROOT, root, lkp);
    }



    private ActionFilterNode (Node original, int mode, FileObject cpRoot, FileObject resource) {
        this (original, mode, cpRoot,
            new ProxyLookup(new Lookup[] {original.getLookup(),Lookups.singleton(new JavadocProvider(cpRoot,resource))}));
    }

    private ActionFilterNode (Node original, int mode) {
        super (original, new ActionFilterChildren (original, mode, null));
        this.mode = mode;
    }

    private ActionFilterNode (Node original, int mode, FileObject root, Lookup lkp) {
        super (original, new ActionFilterChildren (original, mode,root),lkp);
        this.mode = mode;
    }

    public Action[] getActions(boolean context) {
        Action[] result = initActions();        
        return result;
    }


    public Action getPreferredAction() {
        if (mode == MODE_FILE) {
            Action[] actions = initActions();
            if (actions.length > 0 && (actions[0] instanceof OpenAction)) {
                return actions[0];
            }
        }
        return null;
    }

    private Action[] initActions () {
        if (actionCache == null) {
            List result = new ArrayList(2);
            if (mode == MODE_FILE) {
                Action[] superActions = super.getActions(false);
                for (int i=0; i<superActions.length; i++) {
                    if (superActions[i] instanceof OpenAction) {
                        result.add (superActions[i]);
                    }
                }
                result.add (SystemAction.get(ShowJavadocAction.class));
            }
            else if (mode == MODE_PACKAGE || mode == MODE_ROOT) {
                result.add (SystemAction.get(ShowJavadocAction.class));
                Action[] superActions = super.getActions(false);
                for (int i=0; i<superActions.length; i++) {
                    if (superActions[i] instanceof FindAction) {
                        result.add (superActions[i]);
                    }
                }                
                if (mode == MODE_ROOT) {
                    result.add (SystemAction.get(RemoveClassPathRootAction.class));
                }
            }            
            actionCache = (Action[]) result.toArray(new Action[result.size()]);
        }
        return actionCache;
    }

    private static class ActionFilterChildren extends Children {

        private final int mode;
        private final FileObject cpRoot;

        ActionFilterChildren (Node original, int mode, FileObject cpRooot) {
            super (original);
            this.mode = mode;
            this.cpRoot = cpRooot;
        }

        protected Node[] createNodes(Object key) {
            Node n = (Node) key;
            switch (mode) {
                case MODE_ROOT:
                case MODE_PACKAGE:
                    DataObject dobj = (DataObject) n.getCookie(org.openide.loaders.DataObject.class);
                    if (dobj == null) {
                        assert false : "DataNode without DataObject in Lookup";  //NOI18N
                        return new Node[0];
                    }
                    else if (dobj.getPrimaryFile().isFolder()) {
                        return new Node[] {new ActionFilterNode ((Node)key, MODE_PACKAGE,cpRoot,dobj.getPrimaryFile())};
                    }
                    else {
                        return new Node[] {new ActionFilterNode ((Node)key, MODE_FILE,cpRoot,dobj.getPrimaryFile())};
                    }
                case MODE_FILE:
                case MODE_FILE_CONTENT:
                    return new Node[] {new ActionFilterNode ((Node)key, MODE_FILE_CONTENT)};
                default:
                    assert false : "Unknown mode";  //NOI18N
                    return new Node[0];
            }
        }
    }

    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {

        private final FileObject cpRoot;
        private final FileObject resource;

        JavadocProvider (FileObject cpRoot, FileObject resource) {
            this.cpRoot = cpRoot;
            this.resource = resource;
        }

        public boolean hasJavadoc() {
            try {
                return resource != null && JavadocForBinaryQuery.findJavadoc(cpRoot.getURL()).getRoots().length>0;
            } catch (FileStateInvalidException fsi) {
                return false;
            }
        }

        public void showJavadoc() {
            try {
                String relativeName = FileUtil.getRelativePath(cpRoot,resource);
                URL[] urls = JavadocForBinaryQuery.findJavadoc(cpRoot.getURL()).getRoots();
                URL pageURL;
                if (relativeName.length()==0) {
                    pageURL = ShowJavadocAction.findJavadoc ("/overview-summary.html",urls); //NOI18N
                    if (pageURL == null) {
                        pageURL = ShowJavadocAction.findJavadoc ("/index.html",urls); //NOI18N
                    }                    
                }
                else if (resource.isFolder()) {
                    //XXX Are the names the same also in the localized javadoc?                    
                    pageURL = ShowJavadocAction.findJavadoc ("/package-summary.html",urls); //NOI18N
                }
                else {
                    String javadocFileName = relativeName.substring(0,relativeName.lastIndexOf('.'))+".html"; //NOI18Ns
                    pageURL = ShowJavadocAction.findJavadoc (javadocFileName,urls);
                }
                ShowJavadocAction.showJavaDoc(pageURL,relativeName.replace('/','.'));  //NOI18N
            } catch (FileStateInvalidException fsi) {
                ErrorManager.getDefault().notify (fsi);
            }
        }
    }

   private static class Removable implements RemoveClassPathRootAction.Removable {

       private final UpdateHelper helper;
       private final PropertyEvaluator eval;
       private final ReferenceHelper refHelper;
       private final String classPathId;
       private final String entryId;
       private final String includedLibrariesElement;
       private final ClassPathSupport cs;

       Removable (UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper, String classPathId, String entryId, String includedLibrariesElement) {
           this.helper = helper;
           this.eval = eval;
           this.refHelper = refHelper;
           this.classPathId = classPathId;
           this.includedLibrariesElement = includedLibrariesElement;
           this.entryId = entryId;
           
            this.cs = new ClassPathSupport( eval, refHelper, helper.getAntProjectHelper(), 
                                            EjbJarProjectProperties.WELL_KNOWN_PATHS, 
                                            EjbJarProjectProperties.LIBRARY_PREFIX, 
                                            EjbJarProjectProperties.LIBRARY_SUFFIX, 
                                            EjbJarProjectProperties.ANT_ARTIFACT_PREFIX );        
       }


       public boolean canRemove () {
            //Allow to remove only entries from PROJECT_PROPERTIES, same behaviour as the project customizer
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            return props.getProperty (classPathId) != null;
        }

       public Project remove() {
           // Different implementation than j2seproject's one, because
           // we need to remove the library entry from project.xml
           
           // The caller has write access to ProjectManager
           // and ensures the project will be saved.           
           
           boolean removed = false;
           EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
           String raw = props.getProperty (classPathId);
           List resources = cs.itemsList( raw, includedLibrariesElement );
           for (Iterator i = resources.iterator(); i.hasNext();) {
               ClassPathSupport.Item item = (ClassPathSupport.Item)i.next();
               if (entryId.equals(EjbJarProjectProperties.getAntPropertyName(item.getReference()))) {
                   i.remove();
                   removed = true;
               }
           }
           if (removed) {
               String[] itemRefs = cs.encodeToStrings(resources.iterator(), includedLibrariesElement);
               props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
               props.setProperty(classPathId, itemRefs);
               helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);

               //update lib references in private properties
               EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
               ArrayList l = new ArrayList ();
               l.addAll(resources);
               EjbJarProjectProperties.storeLibrariesLocations(l.iterator(), privateProps);
               helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);

               return FileOwnerQuery.getOwner(helper.getAntProjectHelper().getProjectDirectory());
           } else {
               return null;
           }
       }
   }
}
