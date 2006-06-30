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

/**
 * Completion context description that holds additional context information.
 * Instances can be reliably queried only for properties that represent
 * references to other nodes such as siblings, parents, attributes, etc.
 * <p>
 * <b>Note:</b> this interface is never implemented by a <code>GrammarQuery</code>
 * provider.
 *
 * @author Petr Kuzel
 */
public interface HintContext extends Node {

    /**
     * Property representing text that already forms context Node.
     * E.g. for <sample>&lt;elem<blink>|</blink>ent attr="dsD"></sample>
     * it will return <sample>"elem"</sample> string.
     * @return String representing prefix as entered by user
     * that can be used for refining results 
     */
    String getCurrentPrefix();
}

