/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.modelutil;

import java.awt.Image;
import java.io.PrintStream;
import java.util.Enumeration;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Abstract base class for CsmNode. 
 * Disadvantage of (previous version) of CsmNode is that it necessarily stores CsmObject.
 * AbstractNode just declares abstract method 
 * CsmObject getCsmObject()
 *
 * @author Vladimir Kvashin
 */

public abstract class AbstractCsmNode extends AbstractNode {

    public AbstractCsmNode(Children children, Lookup lookup) {
        super(children, lookup);
    }

    public AbstractCsmNode(Children children) {
        this(children, null);
    }
    
    public abstract CsmObject getCsmObject();

    public Image getIcon(int param) {
	CsmObject csmObj = getCsmObject();
        return (csmObj == null) ? super.getIcon(param) : CsmImageLoader.getImage(csmObj);
    }
    
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }
    
    public void dump(PrintStream ps) {
	dump(new Tracer(ps));
    }
    
    protected void dump(Tracer tracer) {
	tracer.trace(this.getDisplayName());
	tracer.indent();
	for( Enumeration children = getChildren().nodes(); children.hasMoreElements(); ) {
	    Node child = (Node) children.nextElement();
	    if( child instanceof AbstractCsmNode ) {
		((AbstractCsmNode) child).dump(tracer);
	    }
	}
	tracer.unindent();
    }
}
