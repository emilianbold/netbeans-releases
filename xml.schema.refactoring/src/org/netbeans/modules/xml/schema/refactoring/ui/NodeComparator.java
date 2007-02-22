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
/*
 * NodeComparator.java
 *
 * Created on April 12, 2006, 1:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.ui;

import java.text.Collator;
import java.util.Comparator;
import org.openide.nodes.Node;

/**
 *
 * @author Jeri Lockhart
 */

public class NodeComparator implements Comparator {

    public int compare(Object n1, Object n2) {

        if (n1 instanceof Node && n2 instanceof Node){
            return compare((Node)n1, (Node)n2);
        }
        return -1;
    }

    public int compare(Node n1, Node n2){
        Collator coltr = Collator.getInstance();
        return coltr.compare(n1.getName(), n2.getName());
    }
}
    