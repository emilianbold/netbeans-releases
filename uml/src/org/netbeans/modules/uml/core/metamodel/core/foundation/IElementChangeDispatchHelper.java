/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
