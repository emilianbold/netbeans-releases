/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.util.List;
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
public interface IElement extends IBaseElement, Node.Cookie
{

    /** Retrieves the name of the actual element, such as 'Class'. */
    public String getElementType();

    /**
    * Retrieves the name of the element typically used for creating icons.
    * It is composed of the element type and any other information needed to
    * make this type unique, such as 'Class' or 'PseudoState_Interface'
    *
    * The default implementation of this routine just returns the element type.
    *
    * @return The icon type of this element in string form
    */
    public String getExpandedElementType();

    public boolean getHasExpandedElementType();
    
    /**
     * Retrieves a element type name that can be displayed to the user.
     * @return A user friendly element type name.
     */
    public String getDisplayElementType();

    /** Adds an element to this Element's collection of owned elements.*/
    public IElement addElement(IElement elem);

    /** Removes an element from the owned elements collection. */
    public IElement removeElement(IElement elem);

    /** Retrieves a collection of elements owned by this element. */
    public ETList<IElement> getElements();

    /** Sets / Gets the owner of this element. */
    public IElement getOwner();

    /** Sets / Gets the owner of this element. */
    public void setOwner(IElement elem);

    /** the owner of this element that is also a Package element. Retrieves the first element going up the owning hierarchy. */
    public IPackage getOwningPackage();

    /**  whether or not an element with the passed in id is owned by this element. */
    public boolean isOwnedElement(String id);

    public boolean isOwnedElement(IElement elem);

    public String getTopLevelId();

    /** Adds a source Flow relationship to this element. */
    public void addSourceFlow(IFlow flow);

    /** Removes a source Flow relationship from this element. */
    public void removeSourceFlow(IFlow flow);

    /** Retrieves the collection of source Flow relationships. */
    public ETList<IFlow> getSourceFlows();

    /** get Flow relationship to this element. */
    public void addTargetFlow(IFlow flow);

    /** Removes a target Flow relationship to this element. */
    public void removeTargetFlow(IFlow flow);

    /** Retrieves the collection of target Flow relationships. */
    public ETList<IFlow> getTargetFlows();

    /**  TaggedValues that are associated with this Element. Standard tags are not included ( e.g., documentation ) */
    public ETList<ITaggedValue> getTaggedValues();

    /** Retrieves the tagged values in a comma delimited string. */
    public String getTaggedValuesAsString();
    
    /** Retrieves the tagged values a  list of "name=value" string */
    public List<String> getTaggedValuesAsList();

    /** Adds a UML 1.3 conformant TaggedValue to this Element. */
    public ITaggedValue addTaggedValue(String tagName, String value);

    /** Removes the TaggedValue from this element's collection. */
    public void removeTaggedValue(ITaggedValue tag);

    /** Retrieves a tagged value based on the name of the tag passed in. */
    public ITaggedValue getTaggedValueByName(String tagName);

    /** Retrieves all the TaggedValues with the specified name. */
    public ETList<ITaggedValue> getTaggedValuesByName(String tagName);

    /** Sets / Gets the documentation for this element. */
    public String getDocumentation();

    /**           Sets / Gets the documentation for this element. */
    public void setDocumentation(String doc);

    /** Adds a PresentationElement to this element. */
    public IPresentationElement addPresentationElement(IPresentationElement elem);

    /** Removes the passed in PresentationElement from this element. */
    public void removePresentationElement(IPresentationElement elem);

    /** Retrieves all the PresentationElements representing this element. */
    public ETList<IPresentationElement> getPresentationElements();

    /** Determines whether or not the passed in presentation element is already associated with this element. */
    public boolean isPresent(IPresentationElement elem);

    /** Removes all PresentationElements from this element. */
    public void removePresentationElements();

    /** Retrieves a presentation element by ID */
    public IPresentationElement getPresentationElementById(String id);

    /** Retrieves the ID of the top level namespace for this element. In most cases, this is the ID for the Project the element is in. */
    public String topLevelId();

    /**           Retrieves all the artifacts associated with this element. The collection returned will contain IArtifact interfaces. */
    public ETList<IElement> getAssociatedArtifacts();

    /** Retrieves the IProject this element is a part of. */
    public IProject getProject();

    /** Adds this element to the Referencing side of the passed in Reference. */
    public IReference addReferencingReference(IReference ref);

    /**           Removes a referencing Reference relationship from this element. */
    public void removeReferencingReference(IReference ref);

    /**           Retrieves the collection of referencing Reference relationships. */
    public ETList<IReference> getReferencingReferences();

    /** Adds this element to the Referred side of the passed in Reference. */
    public IReference addReferredReference(IReference ref);

    /**           Removes a referred Reference relationship from this element. */
    public void removeReferredReference(IReference ref);

    /**           Retrieves the collection of referred Reference relationships. */
    public ETList<IReference> getReferredReferences();

    /** Number of owned elements this element owns. */
    public long getElementCount();

    public long getSourceFlowCount();

    public long getTargetFlowCount();

    public long getTaggedValueCount();

    public long getPresentationElementCount();

    public long getAssociatedArtifactCount();

    public long getReferencingReferenceCount();

    public long getReferredReferenceCount();

    /** Retrieves all tagged values, including standard tags.. */
    public ETList<ITaggedValue> getAllTaggedValues();

    /** Retrieves a collection of ILanguage interfaces that indicate what language specific artifacts this element, or a parent, is associated with. */
    public ETList<ILanguage> getLanguages();

    /** Retrieves a collection of SourceFileArtifacts that contain absolute paths to source files associated with this element.  The collection returned will contain IArtifact interfaces. */
    public ETList<IElement> getSourceFiles();

    /** Associates a source file with the model element. */
    public void addSourceFile(String fileName);

    /** add the source file only if the element doesn't already has one with this fileName*/
    public void addSourceFileNotDuplicate(String fileName);

    /** Removes a source file from the model element.  The model element will no longer be associatied with the model element. */
    public void removeSourceFile(String fileName);

    /** Retrieves a collection of SourceFileArtifacts that contain absolute paths to source files associated with this element.  The collection returned will contain IArtifact interfaces. */
    public ETList<IElement> getSourceFiles2(String language);

    /** Retrieves a collection of SourceFileArtifacts that contain absolute paths to source files associated with this element.  The collection returned will contain IArtifact interfaces. */
    public ETList<IElement> getSourceFiles3(ILanguage language);

    /** 
     * get the SourceFileArtifact with the given fileName
     */
    public IElement getSourceFile(String fileName);

    /** check to see if this Element has a SourceFileArtifact with the given fileName*/
    public boolean hasSourceFile(String fileName);

    /** Retrieves a collection of stereotypes that are currently applied to this Element. The out parameter is an IStereotypes collection. */
    public ETList<Object> getAppliedStereotypes();

    /** Retrieves the number of stereotypes that are currently applied to this Element. */
    public int getNumAppliedStereotypes();

    /** Retrieves a collection of stereotypes in string form << xxx, yyy >>. NULL string is returned if no stereotypes exist. */
    public String getAppliedStereotypesAsString(boolean honorAliasing);

    public String getAppliedStereotypesList();

    public ETList<String> getAppliedStereotypesAsString();

    /** Applies the passed in IStereotype to this element. The in parameter is an IStereotype. */
    public void applyStereotype(Object stereotype);

    /** Removes the passed in IStereotype from this element. The in parameter is an IStereotype. */
    public void removeStereotype(Object stereotype);

    /** Removes all the IStereotypes from this element. */
    public void removeStereotypes();

    /** Applies the Stereotype with the passed in name to this element. */
    public Object applyStereotype2(String name);

    /** Removes the Stereotype that matches the passed in name from this element. */
    public void removeStereotype2(String name);

    /** Takes the cononical form of stereotypes <<xx,yy>> and sets this elements stereotypes to match the input string. */
    public void applyNewStereotypes(String name);

    /** Retrieves the applied stereotype that matches the name passed in. */
    public Object retrieveAppliedStereotype(String name);

    /** Adds a Constraint to this Element. This Element directly owns the Constraint. */
    public void addOwnedConstraint(IConstraint constraint);

    /** Removes a Constraint that this Element owns. */
    public void removeOwnedConstraint(IConstraint constraint);

    /** Retrieves all the Constraints owned by this Element. */
    public ETList<IConstraint> getOwnedConstraints();

    /** Creates a new Constraint. */
    public IConstraint createConstraint(String name, String expr);

    /** Determines whether or not the current element and the element passed in are in the same Project. */
    public boolean inSameProject(IElement elem);

    public void deleteReferenceRelations();

    public void deleteFlowRelations();

    /**needed for element to display properly in navigation dialog.*/
    public String toString();

    public String getConstraintsAsString();

    public ETList<String> getPossibleCollectionTypes();

    public String getPossibleCollectionTypesAsString();
}
