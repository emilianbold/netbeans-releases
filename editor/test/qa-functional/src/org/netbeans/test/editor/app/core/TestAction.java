/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.core;

import org.netbeans.test.editor.app.core.TestNode;
import org.w3c.dom.Element;
/**
 *
 * @author  ehucka
 * @version 
 */
public abstract class TestAction extends TestNode {

    /** Creates new TestAction */
    public TestAction(String name) {
        super(name);
    }
    
    public TestAction(Element node) {
        super(node);
    }
    
    public Element toXML(Element node) {
        return super.toXML(node);
    }
    
    public boolean isParent() {
        return false;
    }
}