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
 * DefaultVisitor.java
 *
 * Created on March 10, 2006, 12:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.axi.visitor;

import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.datatype.Datatype;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class DefaultVisitor implements AXIVisitor {
        
    /**
     * Creates a new instance of DefaultVisitor
     */
    public DefaultVisitor() {
    }
    
    public void visit(AXIDocument root) {        
    }
    
    public void visit(Element element) {        
    }
    
    public void visit(AnyElement element) {
    }
    
    public void visit(Attribute attribute) {        
    }
        
    public void visit(AnyAttribute attribute) {        
    }
    
    public void visit(Compositor compositor) {
    }
    
    public void visit(ContentModel contentModel) {      
    }
    
    public void visit(Datatype datatype) {      
    }	
}
