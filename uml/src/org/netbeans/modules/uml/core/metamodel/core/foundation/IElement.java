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

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

import org.openide.nodes.Node;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IElement extends IBaseElement, Node.Cookie {

//   Retrieves the name of the actual element, such as 'Class'.
//     HRESULT ElementType([out, retval] BSTR* type );
  public String getElementType();

  public String getExpandedElementType();

  public boolean getHasExpandedElementType();

//             Adds an element to this Element's collection of owned elements.
//     HRESULT AddElement([in] IElement* element);
  public IElement addElement(IElement elem);

//             Removes an element from the owned elements collection.
//     HRESULT RemoveElement([in] IElement* element);
  public IElement removeElement(IElement elem);

//             Retrieves a collection of elements owned by this element.
//     HRESULT Elements([out, retval] IElements** pVal);
  public ETList<IElement> getElements();

//             Sets / Gets the owner of this element.
//     HRESULT Owner([out, retval] IElement* *pVal);
  public IElement getOwner();

//             Sets / Gets the owner of this element.
//     HRESULT Owner([in] IElement* newVal);
  public void setOwner(IElement elem);

//   Retrieves the owner of this element that is also a Package element. Retrieves the first element going up the owning hierarchy.
//     HRESULT OwningPackage([out, retval] IPackage* *pVal);
  public IPackage getOwningPackage();

//   Determines whether or not an element with the passed in id is owned by this element.
//     HRESULT IsOwnedElement( [in] BSTR elementID, [out, retval] VARIANT_BOOL* found );
  public boolean isOwnedElement(String id);

  public boolean isOwnedElement(IElement elem);

  public String getTopLevelId();

//   Adds a source Flow relationship to this element.
//     HRESULT AddSourceFlow([in] IFlow* flow );
  public void addSourceFlow(IFlow flow);

//             Removes a source Flow relationship from this element.
//     HRESULT RemoveSourceFlow([in] IFlow* flow );
  public void removeSourceFlow(IFlow flow);

//             Retrieves the collection of source Flow relationships.
//     HRESULT SourceFlows([out, retval] IFlows* *pVal);
  public ETList<IFlow> getSourceFlows();

//   Adds a target Flow relationship to this element.
//     HRESULT AddTargetFlow([in] IFlow* flow );
  public void addTargetFlow(IFlow flow);

//             Removes a target Flow relationship to this element.
//     HRESULT RemoveTargetFlow([in] IFlow* flow );
  public void removeTargetFlow(IFlow flow);

//             Retrieves the collection of target Flow relationships.
//     HRESULT TargetFlows([out, retval] IFlows* *pVal);
  public ETList<IFlow> getTargetFlows();

//   The set of TaggedValues that are associated with this Element. Standard tags are not included ( e.g., documentation )
//     HRESULT TaggedValues([out, retval] ITaggedValues** pVal);
  public ETList<ITaggedValue> getTaggedValues();
  
  /** Retrieves the tagged values in a comma delimited string. */
  public String getTaggedValuesAsString();


//   Adds a UML 1.3 conformant TaggedValue to this Element.
//     HRESULT AddTaggedValue([in] BSTR tagName, [in] BSTR value, [out, retval] ITaggedValue** pVal);
  public ITaggedValue addTaggedValue(String tagName, String value);

//   Removes the TaggedValue from this element's collection.
//     HRESULT RemoveTaggedValue([in] ITaggedValue* tag );
  public void removeTaggedValue(ITaggedValue tag);

//   Retrieves a tagged value based on the name of the tag passed in.
//     HRESULT TaggedValueByName( [in]BSTR tagName, [out,retval] ITaggedValue** tag );
  public ITaggedValue getTaggedValueByName(String tagName);

//   Retrieves all the TaggedValues with the specified name.
//     HRESULT TaggedValuesByName( [in]BSTR tagName, [out,retval] ITaggedValues** tags );
  public ETList<ITaggedValue> getTaggedValuesByName(String tagName);

//   Sets / Gets the documentation for this element.
//     HRESULT Documentation([out, retval] BSTR *pVal);
  public String getDocumentation();

//             Sets / Gets the documentation for this element.
//     HRESULT Documentation([in] BSTR newVal);
  public void setDocumentation(String doc);

//   Adds a PresentationElement to this element.
//     HRESULT AddPresentationElement([in] IPresentationElement* newVal );
  public IPresentationElement addPresentationElement(IPresentationElement elem);

//   Removes the passed in PresentationElement from this element.
//     HRESULT RemovePresentationElement([in] IPresentationElement* pVal );
  public void removePresentationElement(IPresentationElement elem);

//   Retrieves all the PresentationElements representing this element.
//     HRESULT PresentationElements([out, retval] IPresentationElements** pVal );
  public ETList<IPresentationElement> getPresentationElements();

//   Determines whether or not the passed in presentation element is already associated with this element.
//     HRESULT IsPresent([in] IPresentationElement* pVal, [out,retval] VARIANT_BOOL* isPresent );
  public boolean isPresent(IPresentationElement elem);

//   Removes all PresentationElements from this element.
//     HRESULT RemoveAllPresentationElements();
  public void removePresentationElements();

//   Retrieves a presentation element by ID
//     HRESULT GetPresentationElementByID( [in] BSTR id, [out, retval] IPresentationElement** element );
  public IPresentationElement getPresentationElementById(String id);

//   Retrieves the ID of the top level namespace for this element. In most cases, this is the ID for the Project the element is in.
//     HRESULT TopLevelID( [out,retval] BSTR* topID );
  public String topLevelId();

//             Retrieves all the artifacts associated with this element. The collection returned will contain IArtifact interfaces.
//     HRESULT AssociatedArtifacts([out, retval] IElements* *artifacts );
  public ETList<IElement> getAssociatedArtifacts();

//   Retrieves the IProject this element is a part of.
//     HRESULT Project( [out,retval] IDispatch** pProj );
  public IProject getProject();

//   Adds this element to the Referencing side of the passed in Reference.
//     HRESULT AddReferencingReference([in] IReference* pRef );
  public IReference addReferencingReference(IReference ref);

//             Removes a referencing Reference relationship from this element.
//     HRESULT RemoveReferencingReference([in] IReference* pRef);
  public void removeReferencingReference(IReference ref);

//             Retrieves the collection of referencing Reference relationships.
//     HRESULT ReferencingReferences([out, retval] IReferences* *pVal);
  public ETList<IReference> getReferencingReferences();

//   Adds this element to the Referred side of the passed in Reference.
//     HRESULT AddReferredReference([in] IReference* pRef );
  public IReference addReferredReference(IReference ref);

//             Removes a referred Reference relationship from this element.
//     HRESULT RemoveReferredReference([in] IReference* pRef);
  public void removeReferredReference(IReference ref);

//             Retrieves the collection of referred Reference relationships.
//     HRESULT ReferredReferences([out, retval] IReferences* *pVal);
  public ETList<IReference> getReferredReferences();

//   Number of owned elements this element owns.
//     HRESULT ElementCount([out, retval] long* pVal);
  public long getElementCount();

//   SourceFlowCount.
//     HRESULT SourceFlowCount([out, retval] long* pVal);
  public long getSourceFlowCount();

//   .
//     HRESULT TargetFlowCount([out, retval] long* pVal);
  public long getTargetFlowCount();

//   .
//     HRESULT TaggedValueCount([out, retval] long* pVal);
  public long getTaggedValueCount();

//   .
//     HRESULT PresentationElementCount([out, retval] long* pVal);
  public long getPresentationElementCount();

//   .
//     HRESULT AssociatedArtifactCount([out, retval] long* pVal);
  public long getAssociatedArtifactCount();

//   .
//     HRESULT ReferencingReferenceCount([out, retval] long* pVal);
  public long getReferencingReferenceCount();

//   .
//     HRESULT ReferredReferenceCount([out, retval] long* pVal);
  public long getReferredReferenceCount();

//   Retrieves all tagged values, including standard tags..
//     HRESULT AllTaggedValues([out, retval] ITaggedValues** pVal);
  public ETList<ITaggedValue> getAllTaggedValues();

//   Retrieves a collection of ILanguage interfaces that indicate what language specific artifacts this element, or a parent, is associated with.
//     HRESULT Languages([out, retval] ILanguages** pVal);
  public ETList<ILanguage> getLanguages();

//   Retrieves a collection of SourceFileArtifacts that contain absolute paths to source files associated with this element.  The collection returned will contain IArtifact interfaces.
//     HRESULT SourceFiles([out, retval] IElements* *pVal );
  public ETList<IElement> getSourceFiles();

//   Associates a source file with the model element.
//     HRESULT AddSourceFile([in] BSTR filename);
  public void addSourceFile(String fileName);

  // add the source file only if the element doesn't already has one with this fileName
  public void addSourceFileNotDuplicate(String fileName);
  
//   Removes a source file from the model element.  The model element will no longer be associatied with the model element.
//     HRESULT RemoveSourceFile([in] BSTR filename);
  public void removeSourceFile(String fileName);

//   Retrieves a collection of SourceFileArtifacts that contain absolute paths to source files associated with this element.  The collection returned will contain IArtifact interfaces.
//     HRESULT SourceFiles2( [in]BSTR lang, [out, retval] IElements* *pVal );
  public ETList<IElement> getSourceFiles2(String language);

//   Retrieves a collection of SourceFileArtifacts that contain absolute paths to source files associated with this element.  The collection returned will contain IArtifact interfaces.
//     HRESULT SourceFiles3( [in]ILanguage* pLang, [out, retval] IElements* *pVal );
  public ETList<IElement> getSourceFiles3(ILanguage language);

  // get the SourceFileArtifact with the given fileName
  public IElement getSourceFile(String fileName);
  
  // check to see if this Element has a SourceFileArtifact with the given fileName
  public boolean hasSourceFile(String fileName);

//   Retrieves a collection of stereotypes that are currently applied to this Element. The out parameter is an IStereotypes collection.
//     HRESULT AppliedStereotypes( [out, retval] IDispatch** pStereotypes );
  public ETList<Object> getAppliedStereotypes();

//   Retrieves the number of stereotypes that are currently applied to this Element.
//     HRESULT NumAppliedStereotypes( [out, retval] long* pNumStereotypes );
  public int getNumAppliedStereotypes();

//   Retrieves a collection of stereotypes in string form << xxx, yyy >>. NULL string is returned if no stereotypes exist.
//     HRESULT AppliedStereotypesAsString( [out, retval] BSTR* sStereotypeString );
  public String getAppliedStereotypesAsString(boolean honorAliasing);
  public String getAppliedStereotypesList();
  public ETList<String> getAppliedStereotypesAsString();

//   Applies the passed in IStereotype to this element. The in parameter is an IStereotype.
//     HRESULT ApplyStereotype([in] IDispatch* pIStereotype );
  public void applyStereotype(Object stereotype);

//   Removes the passed in IStereotype from this element. The in parameter is an IStereotype.
//     HRESULT RemoveStereotype([in] IDispatch* pIStereotype );
  public void removeStereotype(Object stereotype);

//   Removes all the IStereotypes from this element.
//     HRESULT RemoveStereotypes();
  public void removeStereotypes();

//   Applies the Stereotype with the passed in name to this element.
//     HRESULT ApplyStereotype2([in] BSTR stereotypeName, [out, retval] IDispatch** pStereotype );
  public Object applyStereotype2(String name);

//   Removes the Stereotype that matches the passed in name from this element.
//     HRESULT RemoveStereotype2([in] BSTR stereotypeName );
  public void removeStereotype2(String name);

//   Takes the cononical form of stereotypes <<xx,yy>> and sets this elements stereotypes to match the input string.
//     HRESULT ApplyNewStereotypes([in] BSTR sCononicalStereotypeString );
  public void applyNewStereotypes(String name);

//   Retrieves the applied stereotype that matches the name passed in.
//     HRESULT RetrieveAppliedStereotype([in] BSTR name, [out,retval] IDispatch** sType );
  public Object retrieveAppliedStereotype(String name);

//   Adds a Constraint to this Element. This Element directly owns the Constraint.
//     HRESULT AddOwnedConstraint([in] IConstraint* newVal );
  public void addOwnedConstraint(IConstraint constraint);

//   Removes a Constraint that this Element owns.
//     HRESULT RemoveOwnedConstraint([in] IConstraint* pVal );
  public void removeOwnedConstraint(IConstraint constraint);

//   Retrieves all the Constraints owned by this Element.
//     HRESULT OwnedConstraints([out, retval] IConstraints** pVal );
  public ETList<IConstraint> getOwnedConstraints();

//   Creates a new Constraint.
//     HRESULT CreateConstraint([in] BSTR sName, [in] BSTR sExpression, [out, retval] IConstraint** newConstraint );
  public IConstraint createConstraint(String name, String expr);

//   Determines whether or not the current element and the element passed in are in the same Project.
//     HRESULT InSameProject( IElement* pElement, [out, retval] VARIANT_BOOL* projectsAreTheSame );
  public boolean inSameProject(IElement elem);

  public void deleteReferenceRelations();
  public void deleteFlowRelations();

	//needed for element to display properly in navigation dialog.
	public String toString();

  public String getConstraintsAsString();
}
