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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.netbeans.api.java.source.JavaSource;
/**
 * Displays folders and Java source files under a source node.
 * @author Marian Petras, Jesse Glick
 */
public class MBeanChildren extends JavaChildren {

    private static final String JAVA_MIME_TYPE = "text/x-java"; //NOI18N
    
    public MBeanChildren(Node parent) {
        super(parent);
    }
    
    protected Node[] createNodes(Node originalNode) {
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
                JavaSource foClass = JavaModelHelper.getSource(fo);
                accepted = JavaModelHelper.testMBeanCompliance(foClass);
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
