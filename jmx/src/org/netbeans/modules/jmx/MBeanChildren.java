/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.javacore.api.JavaModel;

/**
 * Displays folders and Java source files under a source node.
 * @author Marian Petras, Jesse Glick
 */
public class MBeanChildren extends JavaChildren {
    
    private static final String JAVA_MIME_TYPE = "text/x-java"; //NOI18N
    
    public MBeanChildren(Node parent) {
        super(parent);
    }
    
    protected Node[] createNodes(Object original) {
        Node originalNode = (Node) original;
        Node newNode;
        
        Object cookie = originalNode.getCookie(DataObject.class);
        if (cookie == null) {
            newNode = copyNode(originalNode);
        } else {
            DataObject dataObj = (DataObject) cookie;
            
            FileObject primaryFile = dataObj.getPrimaryFile();
            if (primaryFile.isFolder()) {
                newNode = new FilterNode(originalNode, new MBeanChildren(originalNode));
            } else if (primaryFile.getMIMEType().equals(JAVA_MIME_TYPE)) {
                boolean accepted  = true;
                try {
                DataObject dob = (DataObject) originalNode.getCookie(DataObject.class);
                FileObject fo = null;
                if (dob != null) fo = dob.getPrimaryFile();
                JavaClass foClass = WizardHelpers.getJavaClass(
                        JavaModel.getResource(fo),fo.getName());
                accepted = Introspector.testCompliance(foClass)&&
                        Introspector.checkCreation(foClass);
                } catch (Exception e) {
                    accepted  = false;
                }
                if (!accepted)
                    newNode = null;
                else {
                    newNode = new FilterNode(originalNode, Children.LEAF);
                    newNode.setDisplayName(primaryFile.getName());
                }
            } else {
                newNode = null;
            }
        }
        
        return (newNode != null) ? new Node[] {newNode}
                                 : new Node[0];
    }
    
}
