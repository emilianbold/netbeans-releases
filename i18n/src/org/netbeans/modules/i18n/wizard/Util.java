/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.i18n.wizard;

import java.util.*;

import org.netbeans.modules.i18n.FactoryRegistry;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.LogicalViews;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataFilter;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.util.*;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.FileOwnerQuery;

/**
 * Bundle access, ...
 *
 * @author  Petr Kuzel
 */
final class Util {
    
    public static String getString(String key) {
        return NbBundle.getMessage(org.netbeans.modules.i18n.wizard.Util.class, key);
    }
    
    public static char getChar(String key) {
        return getString(key).charAt(0);
    }

    // Settings ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
     * Create empty settings used in i18n wizards. 
     */
    public static Map createWizardSettings() {
        return new TreeMap(new DataObjectComparator());
    }
    
    /** 
     * Create settings based on selected nodes. Finds all accepted data objects. 
     * Used by actions to populate wizard.
     * @param activatedNodes selected nodes 
     * @return map with accepted data objects as keys or empty map if no such
     * data objec were found.
     */
    public static Map createWizardSettings(Node[] activatedNodes) {
        Map settings = createWizardSettings();
        
        if (activatedNodes != null && activatedNodes.length > 0) {
            for (int i = 0; i < activatedNodes.length; i++) {
                DataObject.Container container = (DataObject.Container) activatedNodes[i].getCookie(DataObject.Container.class);
                
                if (container != null) {
                
                    if (container instanceof DataFolder) {
                        Iterator it = I18nUtil.getAcceptedDataObjects(container).iterator();

                        while(it.hasNext()) {
                            addSource(settings, (DataObject)it.next());
                        }
                    }
                }

                DataObject dobj = (DataObject) activatedNodes[i].getCookie(DataObject.class);
                if (dobj == null) continue;
                if (FactoryRegistry.hasFactory(dobj.getClass())) {
                    addSource(settings, dobj);
                }
            }
        }
        
        return settings;
    }
    
    /** Adds source to source map (I18N wizard settings). If there is already no change is done.
     * If it's added anew then it is tried to find correspondin reousrce, i.e.
     * first resource from the same folder.
     * @param sourceMap settings where to add teh sources
     * @param source source to add */
    public static void addSource(Map sourceMap, DataObject source) {
        if(sourceMap.containsKey(source))
            return;
        
        DataFolder folder = source.getFolder();
        
        if(folder == null) {
            sourceMap.put(source, null);
            return;
        }

        // try to associate Bundle file

        DataObject[] children = folder.getChildren();
        
        for(int i = 0; i < children.length; i++) {
            if(children[i] instanceof PropertiesDataObject) { // PENDING 
                sourceMap.put(source, new SourceData(children[i]));
                return;
            }
        }
        
        // No resource found in the same folder.
        sourceMap.put(source, null);
    }

    /** Shared enableness logic. Either DataObject.Container or EditorCookie must be present on all nodes.*/
    static boolean wizardEnabled(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return false;
        }

        for (int i = 0; i<activatedNodes.length; i++) {
            Node node = activatedNodes[i];
            Object container = node.getCookie(DataObject.Container.class);
            if (container != null) continue;
            if (node.getCookie(EditorCookie.class) == null) {
                return false;
            }

	    DataObject dobj = (DataObject)node.getCookie(DataObject.class);
	    if (dobj == null) return false;
	    
	    // check that the node has project
	    if (FileOwnerQuery.getOwner(dobj.getPrimaryFile()) == null) return false;
        }
        return true;
    }

    /** Prepare node structure showing sources */
    static Node sourcesView(Project prj, DataFilter filter) {
//       Following code is a bit too CPU intensive (memory probably as well)
//       for working with  MasterFS
//
//        DataFilter dataFilter = new DataFilter() {
//            public boolean acceptDataObject (DataObject dataObject) {
//                return (dataObject instanceof DataFolder
//                 || FactoryRegistry.hasFactory(dataObject.getClass()));
//            }
//        };
//
//        Node repositoryNode = RepositoryNodeFactory.getDefault().repository(dataFilter);


        if (prj != null) {
            Node node = LogicalViews.physicalView(prj).createLogicalView();
            return node;
        } else {
            // Thus changing to work with GlobalPathRegistry
            Set paths = GlobalPathRegistry.getDefault().getPaths( ClassPath.SOURCE );
            List roots = new ArrayList();
            for ( Iterator it = paths.iterator(); it.hasNext(); ) {
                ClassPath cp = (ClassPath)it.next();
                roots.addAll( Arrays.asList( cp.getRoots() ) );
            }


            // XXX This is a bit dirty and should be rewritten to Children.Keys
            // XXX The subnodes deserve better names than src and test
            List nodes = new ArrayList();
            Set names = new HashSet();
            for( Iterator it = roots.iterator(); it.hasNext(); ) {
                FileObject fo = (FileObject)it.next();
                if ( names.contains( fo.getPath()) ) {
                    continue;
                }
                names.add( fo.getPath () );
                try {
                    nodes.add( new FilterNode(DataObject.find( fo ).getNodeDelegate()) );
                }
                catch( DataObjectNotFoundException e ) {
                    // Ignore
                }
            }

            Children ch = new Children.Array();
            Node[] nodesArray = new Node[ nodes.size() ];
            nodes.toArray( nodesArray );
            ch.add( nodesArray );

            Node repositoryNode = new AbstractNode( ch );
            repositoryNode.setName( NbBundle.getMessage( SourceWizardPanel.class, "LBL_Sources" ) );
            // XXX Needs some icon.

            return repositoryNode;
        }
    }

    /**
     * Compare data objects according their package and name. 
     */
    private static class DataObjectComparator implements Comparator {

        /** Implements <code>Comparator</code> interface. */
        public int compare(Object o1, Object o2) {
            if(!(o1 instanceof DataObject) || !(o2 instanceof DataObject))
                return 0;
            
            DataObject d1 = (DataObject)o1;
            DataObject d2 = (DataObject)o2;
            
            if(d1 == d2)
                return 0;
            
            if(d1 == null)
                return -1;
            
            if(d2 == null)
                return 1;

            //return d1.getPrimaryFile().getPackageName('.').compareTo(d2.getPrimaryFile().getPackageName('.'));
            return d1.getPrimaryFile().getPath().compareTo( d2.getPrimaryFile().getPath() );
        }
        
        /** Implements <code>Comparator</code> interface method. */
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            else
                return false;
        }
    }

}
