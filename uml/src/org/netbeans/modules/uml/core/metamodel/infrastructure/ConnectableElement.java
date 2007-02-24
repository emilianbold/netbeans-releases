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


