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


package org.netbeans.modules.uml.ui.products.ad.graphobjects;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEConnector;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEObject;
//import com.tomsawyer.editor.state.TSEMoveSelectedState;
import com.tomsawyer.editor.tool.TSEMoveSelectedTool;
import com.tomsawyer.editor.ui.TSEConnectorUI;
import com.tomsawyer.graph.TSGraphObject;

/*
 * 
 * @author KevinM
 *
 */
public class ETConnector extends TSEConnector implements ITSGraphObject
{
	/**
	 * @param the connector's owner object
	 */
	protected ETConnector(TSGraphObject owner) {
		super(owner);
	}

	/**
	 * Constructor of the class. This constructor should be implemented
	 * to enable <code>TSEConnector</code> inheritance.
	 */
	protected ETConnector()
	{
		// call the equivalent constructor for the super class
		super();

		// perform class specific initialization here
		// ...
	}
	

	/**
	 * This method copies attributes of the source object to this 
	 * object. The source object has to be of the type compatible
	 * with this class (equal or derived). The method should make a
	 * deep copy of all instance variables declared in this class.
	 * Variables of simple (non-object) types are automatically copied
	 * by the call to the copy method of the super class.
	 *
	 * @param sourceObject  the source from which all attributes must
	 *                      be copied
	 */
	public void copy(Object sourceObject)
	{
		// copy the attributes of the super class first
		super.copy(sourceObject);

		// copy any class specific attributes here
		// ...
	}

	// add class-specific methods, instance and class variables
	// ...
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#copy(org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject)
	 */
	public void copy(ITSGraphObject objToClone) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#delete()
	 */
	public void delete() {
		TSEConnectorUI ui = this.getUI() instanceof TSEConnectorUI ? (TSEConnectorUI)getUI() : null;
		TSENode ownerNode = this.getOwner() instanceof TSENode ? (TSENode) getOwner() : null;
		if (ownerNode != null)
			ownerNode.discard(this);
		if (ui != null){
			ui.setOwner(null);
		}			
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#getETUI()
	 */
	public IETGraphObjectUI getETUI() {
		return super.getUI() instanceof IETGraphObjectUI ? (IETGraphObjectUI)super.getUI() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#getObject()
	 */
	public TSEObject getObject() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isEdge()
	 */
	public boolean isEdge() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isLabel()
	 */
	public boolean isLabel() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#isNode()
	 */
	public boolean isNode() {
		return false;
	}

	public boolean isConnector(){
		return true;
	}
	
	public boolean isPathNode(){
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject#setText(java.lang.Object)
	 */
	public void setText(Object text) {
		setTag(text);
	}
	
   /* (non-Javadoc)
    * @see com.tomsawyer.drawing.TSSolidGeometricObject#setBounds(double, double, double, double)
    */
   public void setBounds(double arg0, double arg1, double arg2, double arg3)
   {
      // Here for debugging support
      super.setBounds(arg0, arg1, arg2, arg3);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.drawing.TSSolidGeometricObject#setCenter(double, double)
    */
   public void setCenter(double arg0, double arg1)
   {
		// Here for debugging support
      super.setCenter(arg0, arg1);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.graph.TSGraphObject#setVisible(boolean)
    */
   public void setVisible(boolean arg0)
   {
		// Here for debugging support
      super.setVisible(arg0);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.graph.TSGraphObject#onDiscard(com.tomsawyer.graph.TSGraphObject)
    */
   public void onDiscard(TSGraphObject arg0)
   {
		// Here for debugging support
      super.onDiscard(arg0);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.graph.TSGraphObject#onRemove(com.tomsawyer.graph.TSGraphObject)
    */
   public void onRemove(TSGraphObject arg0)
   {
		// Here for debugging support
      super.onRemove(arg0);
   }
   
	protected TSEGraphWindow getGraphWindow()
	{
		IDrawingAreaControl ctrl = getDrawingAreaControl();
		return ctrl != null ? ctrl.getGraphWindow() : null;
	}

	public IDrawingAreaControl getDrawingAreaControl() {
		return getETUI() != null ? getETUI().getDrawingArea() : null;
	}
	
	public void setSelected(boolean selected){
		//if (this.getGraphWindow() != null && getGraphWindow().getCurrentState() instanceof TSEMoveSelectedState)
		if (this.getGraphWindow() != null && getGraphWindow().getCurrentState() instanceof TSEMoveSelectedTool)
		{
			ETSystem.out.println("Warning: can not change selection lists while in TSEMoveSelectedState state.");
			return;
		}
		super.setSelected(selected);
	}
	
}
