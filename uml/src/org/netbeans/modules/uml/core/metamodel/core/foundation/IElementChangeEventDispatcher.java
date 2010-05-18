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
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
public interface IElementChangeEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers an event sink to handle element modified events.
	*/
	public void registerForElementModifiedEvents( IElementModifiedEventsSink Handler );

	/**
	 * Removes a sink listening for element modified events.
	*/
	public void revokeElementModifiedSink( IElementModifiedEventsSink Handler );

	/**
	 * Registers an event sink to handle element modified events. This sink will be notified even if events are blocked.
	*/
	public void registerForGuarenteedElementModifiedEvents( IElementModifiedEventsSink Handler );

	/**
	 * Removes a sink listening for element modified events.
	*/
	public void revokeGuarenteedElementModifiedSink( IElementModifiedEventsSink Handler );

	/**
	 * Registers an event sink to handle meta attribute modified events.
	*/
	public void registerForMetaAttributeModifiedEvents( IMetaAttributeModifiedEventsSink Handler );

	/**
	 * Removes a sink listening for meta attribute modified events.
	*/
	public void revokeMetaAttributeModifiedSink( IMetaAttributeModifiedEventsSink Handler );

	/**
	 * Registers an event sink to handle documentation modified events.
	*/
	public void registerForDocumentationModifiedEvents( IDocumentationModifiedEventsSink Handler );

	/**
	 * Removes a sink listening for documentation modified events.
	*/
	public void revokeDocumentationModifiedSink( IDocumentationModifiedEventsSink Handler );

	/**
	 * Registers an event sink to handle namespace modified events.
	*/
	public void registerForNamespaceModifiedEvents( INamespaceModifiedEventsSink Handler );

	/**
	 * Removes a sink listening for namespace modified events.
	*/
	public void revokeNamespaceModifiedSink( INamespaceModifiedEventsSink Handler );

	/**
	 * Registers an event sink to handle named element modified events.
	*/
	public void registerForNamedElementEvents( INamedElementEventsSink Handler );

	/**
	 * Removes a sink listening for named element modified events.
	*/
	public void revokeNamedElementSink( INamedElementEventsSink Handler );

	/**
	 * Registers an event sink to handle import modified events.
	*/
	public void registerForImportEventsSink( IImportEventsSink Handler );

	/**
	 * Removes a sink listening for import modified events.
	*/
	public void revokeImportEventsSink( IImportEventsSink Handler);

	/**
	 * Registers an event sink to handle external element events.
	*/
	public void registerForExternalElementEventsSink( IExternalElementEventsSink Handler );

	/**
	 * Removes a sink listening for external element events.
	*/
	public void revokeExternalElementEventsSink( IExternalElementEventsSink Handler );

	/**
	 * Registers an event sink to handle external element events.
	*/
	public void registerForStereotypeEventsSink( IStereotypeEventsSink Handler );

	/**
	 * Removes a sink listening for external element events.
	*/
	public void revokeStereotypeEventsSink( IStereotypeEventsSink Handler );

	/**
	 * Registers an event sink to handle external element events.
	*/
	public void registerForRedefinableElementModifiedEvents( IRedefinableElementModifiedEventsSink Handler );

	/**
	 * Removes a sink listening for external element events.
	*/
	public void revokeRedefinableElementModifiedEvents( IRedefinableElementModifiedEventsSink Handler  );

	public void registerForPackageEventsSink(IPackageEventsSink handler);

	public void revokePackageEventsSink(IPackageEventsSink handler);


	/**
	 * Calling this method will result in the firing of any listeners who register for element modified events.
	*/
	public boolean fireElementPreModified( IVersionableElement element, IEventPayload Payload );

	/**
	 * Calling this method will result in the firing of any listeners who register for element modified events.
	*/
	public void fireElementModified( IVersionableElement element, IEventPayload Payload );

	/**
	 * Calling this method will result in the firing of any listeners who register for element modified events.
	*/
	public boolean fireMetaAttributePreModified( IMetaAttributeModifiedEventPayload Payload );

	/**
	 * Fired whenever the XMI attribute value of an element has been modified.
	*/
	public void fireMetaAttributeModified( IMetaAttributeModifiedEventPayload Payload );

	/**
	 * Fire whenever the documentation field of an element is about to be modified..
	*/
	public boolean fireDocumentationPreModified( IElement element, String doc, IEventPayload Payload );

	/**
	 * Fire whenever an element's documentation field has been modified.
	*/
	public void fireDocumentationModified( IElement element, IEventPayload Payload );

	/**
	 * Fire whenever the documentation field of an element is about to be modified..
	*/
	public boolean firePreElementAddedToNamespace( INamespace space, INamedElement elementToAdd, IEventPayload Payload );

	/**
	 * Fire whenever an element's documentation field has been modified.
	*/
	public void fireElementAddedToNamespace( INamespace space, INamedElement elementToAdd, IEventPayload Payload );

	/**
	 * Fired whenever the name of the passed in element is about to change.
	*/
	public boolean firePreNameModified( INamedElement element, String proposedName, IEventPayload Payload );

	/**
	 * Fired whenever the element's name has changed.
	*/
	public void fireNameModified( INamedElement element, IEventPayload Payload );

	/**
	 * Fired whenever the visibility value of the passed in element is about to change.
	*/
	public boolean firePreVisibilityModified( INamedElement element, /* VisibilityKind */ int proposedValue, IEventPayload Payload );

	/**
	 * Fired whenever the visibility value of the passed in element has changed.
	*/
	public void fireVisibilityModified( INamedElement element, IEventPayload Payload );

	/**
	 * Fired whenever the name of the passed in element is about to change.
	*/
	public boolean firePrePackageImport( IPackage importingPackage, IPackage importedPackage, INamespace owner, IEventPayload Payload );

	/**
	 * Fired whenever the element's name has changed.
	*/
	public void firePackageImported( IPackageImport packImport, IEventPayload Payload );

	/**
	 * Fired whenever the visibility value of the passed in element is about to change.
	*/
	public boolean firePreElementImport( IPackage importingPackage, IElement elem, INamespace owner, IEventPayload Payload );

	/**
	 * Fired whenever the visibility value of the passed in element has changed.
	*/
	public void fireElementImported( IElementImport elImport, IEventPayload Payload );

	/**
	 * Fired whenever an element is about to be loaded from an .etx file.
	*/
	public boolean fireExternalElementPreLoaded( String uri, IEventPayload Payload );

	/**
	 * Fired whenever an element was loaded from an etx file.
	*/
	public void fireExternalElementLoaded( IVersionableElement element, IEventPayload Payload );

	/**
	 * Fired whenever an element is about to be extracted from the current project and placed into an .etx file.
	*/
	public boolean firePreInitialExtraction( String fileName, IVersionableElement element, IEventPayload Payload );

	/**
	 * Fired whenever an element has been extracted to a .etx file.
	*/
	public void fireInitialExtraction( IVersionableElement element, IEventPayload Payload );

	/**
	 * Fired whenever the alias name of the passed in element is about to change.
	*/
	public boolean firePreAliasNameModified( INamedElement element, String proposedName, IEventPayload Payload );

	/**
	 * Fired whenever the element's alias name has changed.
	*/
	public void fireAliasNameModified( INamedElement element, IEventPayload Payload );

	/**
	 * Fired whenever a a stereotype is about to be applied to the passed in Element.
	*/
	public boolean firePreStereotypeApplied( Object pStereotype, IElement element, IEventPayload Payload );

	/**
	 * Fired right after a stereotype was applied to the passed in element.
	*/
	public void fireStereotypeApplied( Object pStereotype, IElement element, IEventPayload Payload );

	/**
	 * Fired whenever a a stereotype is about to be Deleted to the passed in Element.
	*/
	public boolean firePreStereotypeDeleted( Object pStereotype, IElement element, IEventPayload Payload );

	/**
	 * Fired right after a stereotype was Deleted to the passed in element.
	*/
	public void fireStereotypeDeleted( Object pStereotype, IElement element, IEventPayload Payload );

	/**
	 * Fired whenever an element is about to be modified.
	*/
	public boolean firePreFinalModified( IRedefinableElement element, boolean proposedValue, IEventPayload Payload );

	/**
	 * Fired whenever an element is modified.
	*/
	public void fireFinalModified( IRedefinableElement element, IEventPayload Payload );

	/**
	 * Fired whenever a redefined element is about to be added to a IRedefinableElement.
	*/
	public boolean firePreRedefinedElementAdded( IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IEventPayload Payload );

	/**
	 * Fired whenever a redefined element is added to a IRedefinableElement.
	*/
	public void fireRedefinedElementAdded( IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IEventPayload Payload );

	/**
	 * Fired whenever a redefined element is about to be removed to a IRedefinableElement.
	*/
	public boolean firePreRedefinedElementRemoved( IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IEventPayload Payload );

	/**
	 * Fired whenever a redefined element is removed to a IRedefinableElement.
	*/
	public void fireRedefinedElementRemoved( IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IEventPayload Payload );

	/**
	 * Fired whenever a redefining element is about to be added to a IRedefinableElement.
	*/
	public boolean firePreRedefiningElementAdded( IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IEventPayload Payload );

	/**
	 * Fired whenever a redefining element is added to a IRedefinableElement.
	*/
	public void fireRedefiningElementAdded( IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IEventPayload Payload );

	/**
	 * Fired whenever a redefining element is about to be removed to a IRedefinableElement.
	*/
	public boolean firePreRedefiningElementRemoved( IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IEventPayload Payload );

	/**
	 * Fired whenever a redefining element is removed to a IRedefinableElement.
	*/
	public void fireRedefiningElementRemoved( IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IEventPayload Payload );

	/**
	 * Fired whenever the name of element is about to change to the name of an existing element.
	*/
	public boolean firePreNameCollision( INamedElement element, String proposedName, ETList<INamedElement> collidingElements, IEventPayload Payload );

	/**
	 * Fired whenever the name of element has changed to the name of an existing element.
	*/
	public void fireNameCollision( INamedElement element, ETList<INamedElement> collidingElements, IEventPayload Payload );

	public boolean firePreSourceDirModified(IPackage element, String proposedSourceDir, IEventPayload payload);
								  
	public void fireSourceDirModified(IPackage element, IEventPayload payload); 

}
