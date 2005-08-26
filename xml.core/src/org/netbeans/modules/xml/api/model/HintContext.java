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

