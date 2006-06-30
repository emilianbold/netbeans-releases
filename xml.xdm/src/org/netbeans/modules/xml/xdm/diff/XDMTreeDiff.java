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
