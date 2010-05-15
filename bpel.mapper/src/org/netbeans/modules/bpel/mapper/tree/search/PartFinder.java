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

package org.netbeans.modules.bpel.mapper.tree.search;

import org.netbeans.modules.soa.ui.tree.impl.SimpleFinder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.xml.wsdl.model.Part;

/**
 *
 * @author nk160297
 */
public class PartFinder extends SimpleFinder {

    private Part mPart;
    
    public PartFinder(Part part) {
        mPart = part;
    }
    
    protected boolean isFit(Object treeItem) {
        if (treeItem == mPart) {
             // found!!!
            return true;
        }
        return false;
    }

    protected boolean drillDeeper(Object treeItem) {
        if (!(treeItem instanceof BpelEntity)) {
            // Stop searching if out of variable tree.
            return false;
        }
        if (treeItem instanceof Part) {
            // Stop searching if the tree item is a part.
            return false;
        }
        return true;
    }

}
