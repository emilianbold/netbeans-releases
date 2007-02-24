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

/*
 * Created on Mar 5, 2004
 *
 */
package org.netbeans.modules.uml.ui.addins.associateDialog;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.support.finddialog.IFindResults;

/**
 * @author jingmingm
 *
 */
public interface IAssociateDlgGUI
{
	// Display the Associate With Dialog
	public void display();
	
	// Gets/Sets the collection of elements that will become the referencing elements in a reference relationship
	public IFindResults getResults();
	
	// Gets/Sets the collection of elements that will become the referencing elements in a reference relationship 
	public void setResults(IFindResults newVal);
	
	// Gets/Sets the project to search.  If none is provided, the current project will be used 
	public IProject getProject();
	
	// Gets/Sets the project to search.  If none is provided, the current project will be used 
	public void setProject(IProject newVal);
}



