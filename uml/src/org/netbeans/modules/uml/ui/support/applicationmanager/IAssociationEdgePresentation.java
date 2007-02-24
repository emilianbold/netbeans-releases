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



package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;

/*
 *
 * @author KevinM
 *
 */
public interface IAssociationEdgePresentation extends IEdgePresentation {
	/*
	 * Returns the qualifier, if there is one, at this end.
	 */
	public IPresentationElement getSourceQualifier();

	/*
	 * Returns the qualifier, if there is one, at this end.
	 */
	public IPresentationElement getTargetQualifier();

	/*
	 * Creates a qualifier at the source location.
	 */
	public boolean createQualifierNodeAtSourceLocation();

	/*
	 * Creates a qualifier at the target location.
	 */
	public boolean createQualifierNodeAtTargetLocation();

	/*
	 * Removes a qualifier at the source location.
	 */
	public boolean removeQualifierNodeAtSourceLocation();

	/*
	 * Removes a qualifier at the target location.
	 */
	public boolean removeQualifierNodeAtTargetLocation();

	/*
	 * Verifies that the qualifier is either a source or target of this edge.
	 */
	public boolean reconnectToQualifierNode(INodePresentation pQualifierNodePE);

	/*
	 * Validate that the qualifiers are correctly displayed.
	 */
	public boolean validateQualifiers();
}
