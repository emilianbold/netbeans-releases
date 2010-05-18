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


package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IActor;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ILifeline extends INamedElement
{
	/**
	 * Adds to the internal collection of IPartDecomposition interfaces.
	*/
	public void addPartDecomposition( IPartDecomposition decom );

	/**
	 * Removes the specified decom from this lifeline.
	*/
	public void removePartDecomposition( IPartDecomposition decom );

	/**
	 * Retrieves the collection of IPartDecompositions interfaces.
	*/
	public ETList<IPartDecomposition> getPartDecompositions();

	/**
	 * Sets / Gets the Part (set) or Attribute or Parameter that it represents.
	*/
	public ITypedElement getRepresents();

	/**
	 * Sets / Gets the Part (set) or Attribute or Parameter that it represents.
	*/
	public void setRepresents( ITypedElement value );

	/**
	 * property Discriminator
	*/
	public IExpression getDiscriminator();

	/**
	 * property Discriminator
	*/
	public void setDiscriminator( IExpression value );

	/**
	 * Adds an event to the ordered sequence of events on this life line.
	*/
	public void addEvent( IEventOccurrence pEvent );

	/**
	 * Removes the specified event from this life line.
	*/
	public void removeEvent( IEventOccurrence pEvent );

	/**
	 * Retrieves the collection of events along this life line.
	*/
	public ETList<IEventOccurrence> getEvents();

	/**
	 * Sets / Gets the interaction this life line is a part of.
	*/
	public IInteraction getInteraction();

	/**
	 * Sets / Gets the interaction this life line is a part of.
	*/
	public void setInteraction( IInteraction value );

	/**
	 * Adds a fragment to the collection of fragments that are currently covering this life line.
	*/
	public void addCoveringFragment( IInteractionFragment frag );

	/**
	 * Removes the specified fragment from the collection of fragments covering this lifeline.
	*/
	public void removeCoveringFragment( IInteractionFragment frag );

	/**
	 * Retrieves the collection of life lines this fragment covers.
	*/
	public ETList<IInteractionFragment> getCoveringFragments();

	/**
	 * Creates a new Message, specifically oriented towards the invocation of the passed in Operation.
	*/
	public IMessage createMessage( IInteractionFragment fromOwner, IElement toElement, IInteractionFragment toOwner, IOperation oper, /* MessageKind */ int kind );

	/**
	 * Creates and then Inserts a new Message right before the message passed in, specifically oriented towards the invocation of the passed in Operation.
	*/
	public IMessage insertMessage( IMessage fromBeforeMessage, IInteractionFragment fromOwner, IElement toElement, IInteractionFragment toOwner, IOperation oper, /* MessageKind */ int kind );

	/**
	 * Deletes the passed in message, and all dependent elements, such as the AtomicFragments associated with the Message.
	*/
	public void deleteMessage( IMessage pMessage );

	/**
	 * Creates a new Message, in addition to the lifeline that the message is directed to.
	*/
	public IMessage createCreationalMessage( ILifeline toLine );

	/**
	 * Creates a new Message, in addition to the lifeline that the message is directed to.
	*/
	public IActionOccurrence createDestructor();

	/**
	 * Connects this lifeline to the passed in Classifier.
	*/
	public void initializeWith( IClassifier classifier );

	/**
	 * Connects this lifeline to the passed in Class.
	*/
	public void initializeWithClass( IClass clazz );

	/**
	 * Connects this lifeline to the passed in Actor.
	*/
	public void initializeWithActor( IActor pActor );

	/**
	 * Connects this lifeline to the passed in Component.
	*/
	public void initializeWithComponent( IComponent pComponent );

	/**
	 * The element this Lifeline currently represents via the part, attribute, or parameter of that element.
	*/
	public IClassifier getRepresentingClassifier();

	/**
	 * The element this Lifeline currently represents via the part, attribute, or parameter of that element.
	*/
	public void setRepresentingClassifier( IClassifier value );

	/**
	 * The element this Lifeline currently represents via the part, attribute, or parameter of that element. Set by name.
	*/
	public void setRepresentingClassifier2( String classifierName );
    
        /**
         * Sets the representing classifier on this Lifeline by the name or alias
         * of that Classifier
         *
         * @param alias The name of the Classifier to locate and set.
         */
        public void setRepresentingClassifierWithAlias( String alias );
    
        /**
         * Sets the flag to indicate if this lifeline is the Actor lifeline 
         *
         * @param the boolean value to indicate if this lifeline is the Actor lifeline 
         */
        public void setIsActorLifeline(boolean val);
        
        /**
         * Gets the flag to indicate if this lifeline is the Actor lifeline 
         */
        public boolean getIsActorLifeline();
}
