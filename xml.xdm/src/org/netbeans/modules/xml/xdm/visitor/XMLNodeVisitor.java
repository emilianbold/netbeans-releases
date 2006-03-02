/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * XMLNodeVisitor.java
 *
 * Created on August 2, 2005, 9:48 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.visitor;

import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Text;

/**
 * A visitor for the set of text related nodes.
 * @author Chris Webster
 */
public interface XMLNodeVisitor {
	void visit(Attribute attr);
	void visit(Document doc);
	void visit(Element e);
	void visit(Text txt);
        
}      
        
        