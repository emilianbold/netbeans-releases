/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xsd;

import javax.swing.Icon;
import org.netbeans.modules.xml.spi.dom.AbstractNode;
import org.netbeans.modules.xml.api.model.GrammarResult;


/**
 *
 * @author  Ales Novak
 * @author Petr Kuzel
 */
abstract class AbstractResultNode extends AbstractNode implements GrammarResult {
        
    public Icon getIcon(int kind) {
        return null;
    }

    /**
     * @output provide additional information simplifiing decision
     */
    public String getDescription() {
        return getNodeName() + " desc";
    }

    /**
     * @output text representing name of suitable entity
     * //??? is it really needed
     */
    public String getText() {
        return getNodeName();
    }

    /** Default implementation returns false. */
    public boolean isEmptyElement() {
        return false;
    }

    /**
     * @output name that is presented to user
     */
    public String getDisplayName() {
        return getNodeName() + " disp";
    }
}
