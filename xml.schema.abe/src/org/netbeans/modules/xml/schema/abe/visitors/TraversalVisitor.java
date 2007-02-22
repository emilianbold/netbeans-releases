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
 * TraversalVisitor.java
 *
 * Created on August 31, 2006, 5:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.visitors;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.schema.abe.ABEBaseDropPanel;
import org.netbeans.modules.xml.schema.abe.AbstractUIVisitor;

/**
 *
 * @author girix
 */
public abstract class TraversalVisitor extends AbstractUIVisitor{
    
    private static List<Class<? extends AXIComponent>>  eNcFilterList;
    public static List<Class<? extends AXIComponent>> getEnCFilterList(){
        if(eNcFilterList == null){
            eNcFilterList = new ArrayList<Class<? extends AXIComponent>>();
            eNcFilterList.add(AXIContainer.class);
            eNcFilterList.add(Compositor.class);
        }
        return eNcFilterList;
    }
    public abstract ABEBaseDropPanel getResult();
    
}
