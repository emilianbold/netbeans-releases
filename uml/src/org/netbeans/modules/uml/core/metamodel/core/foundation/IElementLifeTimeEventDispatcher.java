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


public interface IElementLifeTimeEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers an event sink to handle element lifetime events.
	*/
	public void registerForLifeTimeEvents( IElementLifeTimeEventsSink Handler );

	/**
	 * Revokes the sink handler.
	*/
	public void revokeLifeTimeSink( IElementLifeTimeEventsSink Handler );

	/**
	 * Registers an event sink to handle element lifetime events.
	*/
	public void registerForDisposalEvents( IElementDisposalEventsSink Handler );

	/**
	 * Revokes the sink handler.
	*/
	public void revokeDisposalSink( IElementDisposalEventsSink Handler  );

	/**
	 * Registers an event sink to handle element unknown classifier events.
	*/
	public void registerForUnknownClassifierEvents( IUnknownClassifierEventsSink Handler );

	/**
	 * Revokes the sink handler.
	*/
	public void revokeUnknownClassifierSink( IUnknownClassifierEventsSink Handler );

	/**
	 * method FireElementPreCreate
	*/
	public boolean fireElementPreCreate( String ElementType, IEventPayload Payload );

	/**
	 * method FireElementCreated
	*/
	public void fireElementCreated( IVersionableElement element, IEventPayload Payload );

	/**
	 * method FireElementPreDelete
	*/
	public boolean fireElementPreDelete( IVersionableElement ver, IEventPayload Payload );

	/**
	 * method FireElementDeleted
	*/
	public void fireElementDeleted( IVersionableElement element, IEventPayload Payload );

	/**
	 * method FireElementDeleted
	*/
	public boolean firePreDisposeElements( ETList<IVersionableElement> pElements, IEventPayload Payload );

	/**
	 * Fired whenever after an element is created.
	*/
	public void fireDisposedElements( ETList<IVersionableElement> pElements, IEventPayload Payload );

	/**
	 * Fired whenever an element is about to be duplicated.
	*/
	public boolean fireElementPreDuplicated( IVersionableElement element, IEventPayload Payload );

	/**
	 * Fired after an element has been duplicated.
	*/
	public void fireElementDuplicated( IVersionableElement element, IEventPayload Payload );

	/**
	 * Fired when a new classifier is about to be created as specified by the unknown classifier preference.
	*/
	public boolean firePreUnknownCreate( String typeToCreate, IEventPayload Payload );

	/**
	 * Fired when a new classifier has been created as specified by the unknown classifier preference..
	*/
	public void fireUnknownCreated( INamedElement newType, IEventPayload Payload );

}
