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

package org.netbeans.modules.i18n.wizard;

import java.util.*;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.i18n.FactoryRegistry;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileStateInvalidException;
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
final class Util extends org.netbeans.modules.i18n.Util {
    
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
    public static Map createWizardSourceMap() {
        return new TreeMap(new DataObjectComparator());
    }
    
    /** 
     * Create settings based on selected nodes. Finds all accepted data objects. 
     * Used by actions to populate wizard.
     * @param activatedNodes selected nodes 
     * @return map with accepted data objects as keys or empty map if no such
     * data objec were found.
     */
    public static Map createWizardSourceMap(Node[] activatedNodes) {
        Map sourceMap = createWizardSourceMap();
        
        if (activatedNodes != null && activatedNodes.length > 0) {
            final VisibilityQuery visQuery = VisibilityQuery.getDefault();
            for (int i = 0; i < activatedNodes.length; i++) {
                DataObject dobj = (DataObject) activatedNodes[i].getCookie(DataObject.class);
                if (dobj != null && !visQuery.isVisible(dobj.getPrimaryFile())) {
                    continue;
                }

                DataObject.Container container = (DataObject.Container) activatedNodes[i].getCookie(DataObject.Container.class);
                
                if (container != null) {
                    Iterator it = I18nUtil.getAcceptedDataObjects(container).iterator();
                    
                    while(it.hasNext()) {
                        addSource(sourceMap, (DataObject)it.next());
                    }
                }

                if (dobj == null) continue;

                if (FactoryRegistry.hasFactory(dobj.getClass())) {
                    addSource(sourceMap, dobj);
                }
            }
        }
        
        return sourceMap;
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
            Object o;
            DataObject dobj = null;
            Node node = activatedNodes[i];
            
            /*
             * This block of code fixes IssueZilla bug #63461:
             *
             *     Apisupport modules visualizes the contents of layer.xml
             *     in project view. The popup for folders in the layer.xml
             *     contains Tools->Internationalize->* actions.
             *
             *     Generally should hide on nonlocal files, I suppose.
             *
             * Local files are recognized by protocol of the corresponding URL -
             * local files are those that have protocol "file".
             */
            o = node.getCookie(DataObject.class);
            if (o != null) {
                dobj = (DataObject) o;
                FileObject primaryFile = dobj.getPrimaryFile();
                
                boolean isLocal;
                try {
                    isLocal = !primaryFile.isVirtual()
                              && primaryFile.isValid()
                              && primaryFile.getURL().getProtocol()
                                      .equals("file");                  //NOI18N
                } catch (FileStateInvalidException ex) {
                    isLocal = false;
                }
                
                if (isLocal == false) {
                    return false;
                }
            }
            
            Object container = node.getCookie(DataObject.Container.class);
            if (container != null) continue;
//            if (node.getCookie(EditorCookie.class) == null) {
//                return false;
//            }

	    if (dobj == null) return false;
	    
	    // check that the node has project
	    if (FileOwnerQuery.getOwner(dobj.getPrimaryFile()) == null) return false;
        }
        return true;
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
