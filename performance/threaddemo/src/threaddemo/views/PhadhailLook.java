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

import java.util.*;
import javax.swing.Action;
import org.netbeans.api.looks.*;
import org.netbeans.spi.looks.*;
import threaddemo.model.Phadhail;

/**
 * A look which wraps phadhails.
 * @author Jesse Glick
 */
final class PhadhailLook extends DefaultLook {
    
    PhadhailLook() {
        super("PhadhailLook");
    }
    
    public List getChildObjects(Look.NodeSubstitute substitute) {
        Phadhail ph = (Phadhail)substitute.getRepresentedObject();
        return Arrays.asList(ph.getChildren());
    }
    
    public String getDisplayName(Look.NodeSubstitute substitute) {
        Phadhail ph = (Phadhail)substitute.getRepresentedObject();
        return ph.getDisplayName();
    }
    
    public boolean isLeaf(Look.NodeSubstitute substitute) {
        Phadhail ph = (Phadhail)substitute.getRepresentedObject();
        return !ph.hasChildren();
    }
    
    public Action[] getActions(Look.NodeSubstitute substitute) {
        // Do not look in JNDI context - want to keep this a standalone demo.
        return null;
    }
    
}
