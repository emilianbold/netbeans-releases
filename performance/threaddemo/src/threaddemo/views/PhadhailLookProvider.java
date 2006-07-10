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

package threaddemo.views;

import java.util.Enumeration;
import org.netbeans.spi.looks.Look;
import org.netbeans.spi.looks.LookProvider;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import threaddemo.model.Phadhail;

/**
 * A look selector matching PhadhailLook.
 * @author Jesse Glick
 */
final class PhadhailLookProvider implements LookProvider {

    private static final Look PHADHAIL_LOOK = new PhadhailLook();
    private static final Look STRING_LOOK = new StringLook();
    private static final Look ELEMENT_LOOK = new ElementLook();
    
    public PhadhailLookProvider() {}
    
    public Enumeration getLooksForObject(Object representedObject) {
        if (representedObject instanceof Phadhail) {
            return Enumerations.singleton(PHADHAIL_LOOK);
        } else if (representedObject instanceof String) {
            return Enumerations.singleton(STRING_LOOK);
        } else {
            assert representedObject instanceof Element : representedObject;
            assert representedObject instanceof EventTarget : representedObject;
            return Enumerations.singleton(ELEMENT_LOOK);
        }
    }
    
    /**
     * Just shows plain text nodes - markers.
     */
    private static final class StringLook extends Look {
        public StringLook() {
            super("StringLook");
        }
        public String getDisplayName() {
            return "Simple Messages";
        }
        public String getName(Object o, Lookup l) {
            return (String)o;
        }
        public String getDisplayName(Object o, Lookup l) {
            return (String)o;
        }
        public boolean isLeaf(Object o, Lookup l) {
            return true;
        }
    }
    
}
