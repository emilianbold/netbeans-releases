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
        return null;
    }
}
