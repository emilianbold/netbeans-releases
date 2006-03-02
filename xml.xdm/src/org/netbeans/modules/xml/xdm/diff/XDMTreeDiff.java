/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * XDMTreeDiff.java
 *
 * Created on February 1, 2006, 4:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Document;

/**
 *
 * @author Owner
 */
public class XDMTreeDiff {

	private ElementIdentity eID;
	
	/** Creates a new instance of XDMTreeDiff */
	public XDMTreeDiff() {
		//establish DOM element identities
		eID = new ElementIdentity();
		String[][] nameIdID = { { "name" }, { "id" }, { "ref" } };
		eID.setDefaultID( nameIdID );
	}
	
	/** Creates a new instance of XDMTreeDiff */
	public XDMTreeDiff(ElementIdentity eID) {
		this.eID = eID;
	}	
	
	private List<DiffEvent> performDiff(Document src1, Document src2) {
		return new DiffFinder(eID).findDiff(src1, src2);
	}
	
	public List<DiffEvent> performDiff(XDMModel model, Document src2) {
		List<DiffEvent> deList = performDiff(model.getDocument(), src2);	
		return deList;
	}
	
	public List<DiffEvent> performDiffAndMutate(XDMModel model, Document src2) {
		List<DiffEvent> deList = performDiff(model.getDocument(), src2);
		new MergeDiff(eID).merge(model, deList);		
		return deList;
	}
}
