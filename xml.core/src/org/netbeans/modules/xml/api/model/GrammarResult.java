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

package org.netbeans.modules.xml.api.model;

import org.w3c.dom.Node;
import javax.swing.Icon;

/**
 * It represents additonal properties of a result option.
 * <p>
 * It enriches DOM Node with information useful for presenting
 * query result option to a user. Also all children or siblings of this
 * result must be <code>GrammarResult</code> instances.
 * <p>
 * It can have children representing mandatory content.
 * However it is up to client if it uses the mandatory content.
 * <p>
 * As in whole package provide only readonly DOM level 1 implementation. 
 *
 * @author  Petr Kuzel
 * @stereotype description 
 */
public interface GrammarResult extends Node {

    //Node getNode() use instead of extends Node
    //boolean isDefault()
    //boolean isImplied()
    //boolean isRequired()
    //boolean isFixed()
    
    /**
     * @return name that is presented to user or <code>null</code> if
     * <code>getNodeName()</code> is enough.
     */
    String getDisplayName();

    /**
     * @return provide additional information simplifing decision
     * (suitable for tooltip) or <code>null</code>
     */
    String getDescription();

    /**
     * @param kind icon kind as given by BeanInfo
     * @return an icon - a visual hint or <code>null</code>
     */
    Icon getIcon(int kind);

    /**
     * For elements provide hint whether element has empty content model.
     * @return true element has empty content model (no childs) and can
     * be completed in empty element form i.e. <code>&lt;ement/></code>.
     * @since 6th Aug 2004
     */
    boolean isEmptyElement();
}
