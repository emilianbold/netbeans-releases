/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.views;

import java.util.Collections;
import org.openide.nodes.*;
import threaddemo.model.Phadhail;

/**
 * A plain node view of a phadhail tree.
 * @author Jesse Glick
 */
final class PhadhailNode extends AbstractNode {
    
    private final Phadhail ph;
    
    public PhadhailNode(Phadhail ph) {
        super(ph.hasChildren() ? new PhadhailChildren(ph) : Children.LEAF);
        this.ph = ph;
    }
    
    public String getDisplayName() {
        return ph.getDisplayName();
    }
    
    private static final class PhadhailChildren extends Children.Keys {
        
        private final Phadhail ph;
        
        public PhadhailChildren(Phadhail ph) {
            this.ph = ph;
        }
        
        protected void addNotify() {
            setKeys(ph.getChildren());
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        protected Node[] createNodes(Object key) {
            return new Node[] {new PhadhailNode((Phadhail)key)};
        }
        
    }
    
}
