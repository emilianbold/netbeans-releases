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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor.wizard;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.netbeans.modules.iep.editor.PlanDataObject;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

public class Utility {
    
    public static String IEP_TARGETNAMESPACE_PREFIX = "http://enterprise.netbeans.org/iep";
    
    public static GlobalElement findGlobalElement(Schema schema, String elementName) {
        Collection gElem = schema.findAllGlobalElements();
        if (gElem !=null){
            Iterator iter = gElem.iterator();
            while (iter.hasNext()) {
                GlobalElement elem = (GlobalElement) iter.next();
                if (elem.getName().equals(elementName)) {
                    return elem;
                }
            }
        }
        return null;
    }
    
    public static GlobalType findGlobalType(Schema schema, String typeName) {
        Collection gTypes = schema.findAllGlobalTypes();
        if (gTypes !=null){
            Iterator iter = gTypes.iterator();
            while (iter.hasNext()) {
                GlobalType type = (GlobalType) iter.next();
                if (type.getName().equals(typeName)) {
                    return type;
                }
            }
        }
        return null;
    }
    
    public static String fromQNameToString(QName qname) {
        if (qname.getPrefix() != null && qname.getPrefix().trim().length() > 0) {
            return qname.getPrefix() + ":" + qname.getLocalPart();
        }
        return qname.getLocalPart();
    }
    
    
    
    /**
     * Expands nodes on the treeview till given levels
     * @param tv the treeview object
     * @param level the level till which the nodes should be expanded. 0 means none.
     * @param rootNode the rootNode
     */
    public static void expandNodes(TreeView tv, int level, Node rootNode) {
        if (level == 0) return;
        
        Children children = rootNode.getChildren();
        if (children != null) {
            Node[]  nodes = children.getNodes();
            if (nodes != null) {
                for (int i= 0; i < nodes.length; i++) {
                    tv.expandNode(nodes[i]); //Expand node
                    expandNodes(tv, level - 1, nodes[i]); //expand children
                }
            }
        }
    }
    
    
    public static String generateTargetNamespace(PlanDataObject dataObject) {
        Project project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        StringBuffer tns = new StringBuffer(IEP_TARGETNAMESPACE_PREFIX);
        
        if(project != null) {
            ProjectInformation pi = ProjectUtils.getInformation(project);
            tns.append("/");
            tns.append(pi.getName());
            tns.append("/");
            tns.append(dataObject.getName());
        } else {
            tns.append("/");
            tns.append(dataObject.getName());
        }
        
        
        return tns.toString();
    }
    
    
}
