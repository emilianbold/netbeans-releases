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
package org.netbeans.modules.xml.axi.visitor;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.Compositor;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class PrintAXITreeVisitor extends DeepAXITreeVisitor {
            
    /**
     * Creates a new instance of PrintAXITreeVisitor
     */
    public PrintAXITreeVisitor() {
        super();
    }
    
    protected void visitChildren(AXIComponent component) {
        if(PRINT_TO_CONSOLE)
            printModel(component);
        
        depth++;
        super.visitChildren(component);
        depth--;
    }
    
    private void printModel(AXIComponent component) {
        StringBuffer buffer = new StringBuffer();
        if(component instanceof Compositor) {
            Compositor compositor = (Compositor)component;
            buffer.append((getTab() == null) ? compositor : getTab() + compositor);
            buffer.append("<min=" + compositor.getMinOccurs() + ":max=" + compositor.getMaxOccurs() + ">");
        }
        if(component instanceof Element) {
            Element element = (Element)component;
            buffer.append((getTab() == null) ? element.getName() : getTab() + element.getName());
            if(element.getAttributes().size() != 0) {
                buffer.append("<" + getAttributes(element) + ">");
            }
            buffer.append("<min=" + element.getMinOccurs() + ":max=" + element.getMaxOccurs() + ">");
        }
        if(component instanceof AnyElement) {
            AnyElement element = (AnyElement)component;
            buffer.append((getTab() == null) ? element : getTab() + element);
        }
        
        System.out.println(buffer.toString());
    }
        
    
    private String getAttributes(Element element) {
        StringBuffer attrs = new StringBuffer();
        for(AbstractAttribute attr : element.getAttributes()) {
            attrs.append(attr+":");
        }
        if(attrs.length() > 0)
            return attrs.toString().substring(0, attrs.length()-1);
        else
            return attrs.toString();
    }
    
    private String getTab() {
        String tabStr = "++++";
        
        if(depth == 0) {
            return null;
        }
        
        StringBuffer tab = new StringBuffer();
        for(int i=0; i<depth ; i++) {
            tab.append(tabStr);
        }
        return tab.toString();
    }
    
    private int depth = 0;
    private static boolean PRINT_TO_CONSOLE= false;
}
