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


//*****************************************************************************
//*****************************************************************************

package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.CoreNameCollisionListener;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink;

/**
 * @author sumitabhk
 *
 */
public class NameCollisionEventSink implements INamedElementEventsSink,
										ICoreProductInitEventsSink,
										IEditControlEventSink
{
	private CoreNameCollisionListener m_ListenerToAdvise = null;

	/**
	 * 
	 */
	public NameCollisionEventSink()
	{
		super();
	}
	// INamedElementsEventSink
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreNameModified(INamedElement element, String proposedName, IResultCell cell) {
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onNameModified(INamedElement element, IResultCell cell) {
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreVisibilityModified(INamedElement element, int proposedValue, IResultCell cell) {
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onVisibilityModified(INamedElement element, IResultCell cell) {
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell) 
	{
		if (m_ListenerToAdvise != null)
		{
			m_ListenerToAdvise.onPreAliasNameModified(element,proposedName, cell );
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onAliasNameModified(INamedElement element, IResultCell cell) 
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreNameCollision(INamedElement element, String proposedName, ETList<INamedElement> collidingElements, IResultCell cell) 
	{
		if (m_ListenerToAdvise != null)
		{
			m_ListenerToAdvise.onPreNameCollision(element,proposedName,collidingElements,cell);
		}

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onNameCollision(INamedElement element, ETList<INamedElement> collidingElements, IResultCell cell) 
	{
		if (m_ListenerToAdvise != null)
		{
			m_ListenerToAdvise.onNameCollision(element,collidingElements,cell);
		}		
	}
	
	// ICoreProductInitEventsSink
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreInit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreInit(ICoreProduct pVal, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductInitialized(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreQuit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreQuit(ICoreProduct pProduct, IResultCell cell)
	{
		if (m_ListenerToAdvise != null)
		{
			m_ListenerToAdvise.onCoreProductPreQuit();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductPreSaved(ICoreProduct pProduct, IResultCell cell)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onCoreProductSaved(ICoreProduct newVal, IResultCell cell)
	{
	}
	// IEditControlEventSink
	/**
	 * Fired when data not consistent with the selected mask is passed
	*/
	public void onPreInvalidData( String ErrorData, IResultCell cell )
	{
	}

	/**
	 * Fired when data not consistent with the selected mask is passed
	*/
	public void onInvalidData( String ErrorData, IResultCell cell )
	{
	}

	/**
	 * Fired when user toggles Insert/Overstrike mode via the Insert key
	*/
	public void onPreOverstrike( boolean bOverstrike, IResultCell cell ){
	}

	/**
	 * Fired when user toggles Insert/Overstrike mode via the Insert key
	*/
	public void onOverstrike( boolean bOverstrike, IResultCell cell ){
	}

	/**
	 * The control is about to gain focus
	*/
	public void onPreActivate( IEditControl pControl, IResultCell cell ){
	}

	/**
	 * The control has gained focus
	*/
	public void onActivate( IEditControl pControl, IResultCell cell ){
	}

	/**
	 * The control has lost focus
	*/
	public void onDeactivate( IEditControl pControl, IResultCell cell )
	{
		if (m_ListenerToAdvise != null)
		{
			m_ListenerToAdvise.onDeactivate(pControl);
		}
	}

	/**
	 * Sets an AxEditEvents object as owner of this event sink. Events will be routed to the owner
	*/
	public void setEventOwner( /* long */ int pOwner ){
	}

	/**
	 * Model element data is about to be saved.
	*/
	public void onPreCommit( IResultCell cell ){
	}

	/**
	 * Model element data has been saved.
	*/
	public void onPostCommit( IResultCell cell ){
	}
	
	public void setListenerToAdvise(CoreNameCollisionListener pListener)
	{
		m_ListenerToAdvise = pListener;
	}

}


