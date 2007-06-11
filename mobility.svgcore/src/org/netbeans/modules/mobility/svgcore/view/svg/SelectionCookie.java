/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore.view.svg;

import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.openide.nodes.Node;

public interface SelectionCookie extends Node.Cookie {
    public void updateSelection(SVGDataObject doj, DocumentElement elem, boolean doubleClick);
}    