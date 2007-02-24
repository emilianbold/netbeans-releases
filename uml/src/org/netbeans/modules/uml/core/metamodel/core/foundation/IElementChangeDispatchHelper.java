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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.eventframework.IEventDispatchHelper;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IElementChangeDispatchHelper extends IEventDispatchHelper
{
	/**
	 * method DispatchElementPreModified
	*/
	public boolean dispatchElementPreModified( IVersionableElement element );

	/**
	 * method DispatchElementModified
	*/
	public long dispatchElementModified( IVersionableElement element );

	/**
	 * method DispatchMetaAttrPreMod
	*/
	public boolean dispatchMetaAttrPreMod( IVersionableElement element, String attrName, String NewValue, IMetaAttributeModifiedEventPayload Payload );

	/**
	 * method DispatchMetaAttrModified
	*/
	public long dispatchMetaAttrModified( IMetaAttributeModifiedEventPayload Payload );

	/**
	 * Dispatches the OnDocumentationPreModified event.
	*/
	public boolean dispatchDocPreModified( IElement element, String doc );

	/**
	 * Dispatches the OnDocumentationModified event.
	*/
	public long dispatchDocModified( IElement element );

	/**
	 * Dispatches the OnPreElementAddedToNamespace event.
	*/
	public boolean dispatchPreElementAddedToNamespace( INamespace space, INamedElement elementToAdd );

	/**
	 * Dispatches the OnDocumentationModified event.
	*/
	public long dispatchElementAddedToNamespace( INamespace space, INamedElement elementToAdd );

	/**
	 * Fired whenever the name of the passed in element is about to change.
	*/
	public boolean dispatchPreNameModified( INamedElement element, String proposedName );

	/**
	 * Fired whenever the element's name has changed.
	*/
	public long dispatchNameModified( INamedElement element );

	/**
	 * Fired whenever the visibility value of the passed in element is about to change.
	*/
	public boolean dispatchPreVisibilityModified( INamedElement element, /* VisibilityKind */ int proposedValue );

	/**
	 * Fired whenever the visibility value of the passed in element has changed.
	*/
	public long dispatchVisibilityModified( INamedElement element );

	/**
	 * Fired whenever the alias name of the passed in element is about to change.
	*/
	public boolean dispatchPreAliasNameModified( INamedElement element, String proposedName );

	/**
	 * Fired whenever the element's name has changed.
	*/
	public long dispatchAliasNameModified( INamedElement element );

	/**
	 * Collection of elements that are colliding with a particular name change event
	*/
	public ETList<INamedElement> getCollidingElements();

	/**
	 * Collection of elements that are colliding with a particular name change event
	*/
	public void setCollidingElements( ETList<INamedElement> value );

	/**
	 * Fired whenever a name collision is about to occur while renaming a NamedElement. The CollidingElements property must be set before firing this method.
	*/
	public boolean dispatchPreNameCollision( INamedElement element, String proposedValue );

	/**
	 * Fired whenever a name collision has occurred while renaming a NamedElement. The CollidingElements property must be set before firing this method.
	*/
	public long dispatchNameCollision( INamedElement element );

}
