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
package org.netbeans.test.editor.app.core;

import org.netbeans.test.editor.app.gui.*;
import org.netbeans.test.editor.app.core.TestAction;
import org.w3c.dom.Element;
/**
 *
 * @author  ehucka
 * @version
 */
public abstract class TestSetAction extends TestAction {

    /** Creates new TestSetAction */
    public TestSetAction(int num) {
        this("set"+Integer.toString(num));
    }

    public TestSetAction(String name) {
        super(name);
    }

    public TestSetAction(Element node) {
        super(node);
    }

    public Element toXML(Element node) {
        node = super.toXML(node);
        return node;
    }
    
    public void perform() {
        System.err.println("Set Action "+this+" is performing.");
    }
    
    public void stop() {
    }
    
}
