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


package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBaseElement;

import org.dom4j.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 *
 */
public class ConnectableElement extends NamedElement 
								implements IConnectableElement
{
	public void addEnd( IConnectorEnd connector )
	{
		final IConnectorEnd connect = connector;
		new ElementConnector<IConnectableElement>().addChildAndConnect(
										this, true, 
										"end", "end", connect,
										 new IBackPointer<IConnectableElement>() 
										 {
											 public void execute(IConnectableElement obj) 
											 {
												connect.setPart(obj);
											 }
										 }									
										);
	}

	public void removeEnd( IConnectorEnd connector )
	{
		final IConnectorEnd connect = connector;
		new ElementConnector<IConnectableElement>().removeByID
							   (
								 this,connect,"end",
								 new IBackPointer<IConnectableElement>() 
								 {
									public void execute(IConnectableElement obj) 
									{
										connect.setPart(obj);
									}
								 }										
								);
	}
	
	public ETList<IConnectorEnd> getEnds()
	{
		ElementCollector<IConnectorEnd> collector = new ElementCollector<IConnectorEnd>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"end", IConnectorEnd.class);				
	}
	
	public void addRoleContext( IStructuredClassifier classifier )
	{
		final IStructuredClassifier classi = classifier;
		new ElementConnector<IConnectableElement>().addChildAndConnect(
										this, true, 
										"roleContext", "roleContext",classi,
										 new IBackPointer<IConnectableElement>() 
										 {
											 public void execute(IConnectableElement obj) 
											 {
												classi.addRole(obj);
											 }
										 }										
										);
	}
	
	public void removeRoleContext(final IStructuredClassifier classifier)
	{
		new ElementConnector<IConnectableElement>().removeByID(
										this, classifier, "roleContext", 
										 new IBackPointer<IConnectableElement>() 
										 {
											 public void execute(IConnectableElement obj) 
											 {
												classifier.removeRole(obj);
											 }
										 }		
										);
	}
	
	public ETList<IStructuredClassifier> getRoleContexts()
	{
		ElementCollector<IStructuredClassifier> collector = new ElementCollector<IStructuredClassifier>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"roleContext", IStructuredClassifier.class);					
	}
}


