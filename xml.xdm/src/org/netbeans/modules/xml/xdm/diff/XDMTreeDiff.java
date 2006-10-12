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

package org.netbeans.modules.xml.xdm.diff;

import java.util.List;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.w3c.dom.NodeList;

/*
 * This class is the Facade to find diff between 2 XML documents as well
 * as merge the changes detected back to the original XDM model
 *
 * @author Ayub Khan
 */
public class XDMTreeDiff {
    
    private ElementIdentity eID;
    
    /** Creates a new instance of XDMTreeDiff */
    public XDMTreeDiff(ElementIdentity eID) {
        this.eID = eID;
    }
    
    public List<Difference> performDiff(Document doc1, Document doc2) {
//		System.out.println("doc1==>");
//		printDocument(doc1);
//		System.out.println("doc2==>");
//		printDocument(doc2);
        List<Difference> deList = new DiffFinder(eID).findDiff(doc1, doc2);
        return deList;
    }
    
    public List<Difference> performDiff(XDMModel model, Document doc2) {
        return performDiff(model.getDocument(), doc2);
    }
    
    public List<Difference> performDiffAndMutate(XDMModel model, Document doc2) {
        List<Difference> deList = performDiff(model.getDocument(), doc2);
        mergeDiff(model, deList);
        return deList;
    }
    
    public static void mergeDiff(XDMModel model, List<Difference> deList) {
        new MergeDiff().merge(model, deList);
    }
    
    public static void printDocument(Node node) {
        String name = node.getNodeName();
        if(node instanceof Element && node.getAttributes().getLength() > 0)
            name+=node.getAttributes().item(0).getNodeValue();
        System.out.println("node: "+name+" id:"+node.getId());
        NodeList childs = node.getChildNodes();
        for(int i=0;i<childs.getLength();i++)
            printDocument((Node) childs.item(i));
    }
}
