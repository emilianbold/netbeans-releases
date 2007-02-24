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



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.inspector.TSEInspectorProperty;
import com.tomsawyer.editor.inspector.TSEInspectorPropertyID;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSConstSize;
import com.tomsawyer.drawing.geometry.TSConstSize;
//import com.tomsawyer.util.TSExpTransform;
import com.tomsawyer.drawing.geometry.TSExpTransform;
import com.tomsawyer.util.TSProperty;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

/*
 * @author KevinM
 */
public interface IETGraphObjectUI {
	public IDrawEngine getDrawEngine();
	public void setDrawEngine(IDrawEngine newDrawEngine);

	public IDrawingAreaControl getDrawingArea();
	public void setDrawingArea(IDrawingAreaControl control);

	public String getDrawEngineClass();
	public void setDrawEngineClass(String string);

	public IElement getModelElement();
	public void setModelElement(IElement element);

	public String getInitStringValue();
	public void setInitStringValue(String string);
	public ITSGraphObject getTSObject();

	// This method returns a clone of this object. 
	Object clone();

	//This method copies all properties of the specified UI to this UI. 
	void copy(TSEObjectUI sourceUI);

	//This method draws the object represented by this UI. 
	abstract void draw(TSEGraphics graphics);

	//This method draws the outline of the object represented by this UI. 
	abstract void drawOutline(TSEGraphics graphics);

	//This method draws the object represented by this UI in a selected state. 
	abstract void drawSelected(TSEGraphics graphics);

	//This method draws the outline of the object represented by this UI in a selected state. 
	abstract void drawSelectedOutline(TSEGraphics graphics);

	//This method returns the y coordinate of the bottom side of the bounding box of the owner object in local coordinate system. 
	abstract double getBottom();

	//This method returns the bounding box that fully encloses this object in local coordinate system. 
	TSConstRect getBounds();

	//This method returns the x coordinate of the bounding box that fully encloses this object in local coordinate system. 
	double getCenterX();

	//This method returns the y coordinate of the bounding box that fully encloses this object in local coordinate system. 
	double getCenterY();

	//This method returns a list of all properties not set to their default values. 
	List getChangedProperties();

	//This method returns the default color in which selected objects are to be drawn. 
	TSEColor getDefaultSelectedColor();

	//This method returns the height of the bounding box that fully encloses this object in local coordinate system. 
	double getHeight();

	//This method returns a TSEInspectorProperty with the specified ID. 
	TSEInspectorProperty getInspectorProperty(TSEInspectorPropertyID id);

	//This method adds inspector property IDs to the specified list. 
	void getInspectorPropertyIDs(List idList);

	//This method returns the extents of the region of the world that needs repainting when the owner of this UI gets modified. 
	abstract TSConstRect getInvalidRegion(TSTransform root2device, TSExpTransform local2root);

	//This method returns the x coordinate of the left side of the bounding box of the owner object in local coordinate system. 
	abstract double getLeft();

	//This method returns the transform from the UI's owner's local coordinate system to it's main graph window's coordinate system. 
	TSTransform getLocalToMainGraphWindowTransform();

	//This method returns the owner of this UI. 
	abstract TSEObject getOwner();

	//This method returns a list of all properties associated with this object. 
	List getProperties();

	//This method returns the property with the specified name. 
	TSProperty getProperty(String name);

	//This method returns the x coordinate of the right side of the bounding box of the owner object in local coordinate system. 
	abstract double getRight();

	//This method returns the color in which selected objects are to be drawn. 
	TSEColor getSelectedColor();

	//This method returns the size of the bounding box that fully encloses this object in local coordinate system. 
	TSConstSize getSize();

	//This method returns the y coordinate of the top side of the bounding box of the owner object in local coordinate system. 
	abstract double getTop();

	//This method returns the width of the bounding box that fully encloses this object in local coordinate system. 
	double getWidth();

	//This method returns whether or not the specified rectangle intersects with the bounding box of this object. 
	abstract boolean intersects(double left, double bottom, double right, double top);

	//This method returns whether or not the specified rectangle intersects with the bounding box of this object. 
	boolean intersects(TSConstRect rectangle);

	//This method is called just before this UI's owner object is discarded from a graph. 
	void onOwnerDiscarded();

	//This method is called just after this UI's owner object is inserted into a graph. 
	void onOwnerInserted();

	//This method is called just before this UI's owner object is removed from a graph. 
	void onOwnerRemoved();

	//This method resets the properties of this UI object to their default values. 
	void reset();

	//This method sets a property changed by the inspector. 
	int setInspectorProperty(TSEInspectorPropertyID id, TSEInspectorProperty property);

	//This method sets the specified property of this UI. 
	void setProperty(TSProperty property);

	//This method sets the color in which selected objects are to be drawn. 
	void setSelectedColor(TSEColor selectedColor);

	/**
	 * Retrieves the graphics context for the node ui.
	 * 
	 * @param graphics The TS graphics class.
	 * @return The graphics context.
	 */
	public IDrawInfo getDrawInfo(TSEGraphics graphics);

	/**
	 * Retrieves the graphics context for the node ui.
	 * 
	 * @param graphics The TS graphics class.
	 * @return The graphics context.
	 */
	public IDrawInfo getDrawInfo();

	/// Returns the Model Element XMIID that was loaded from the file.
	public String getReloadedModelElementXMIID();
	/// Sets the Model Element XMIID that will be persisted to the file.
	public void setReloadedModelElementXMIID(String newVal);
	/// Returns the Toplevel Element XMIID that was loaded from the file.
	public String getReloadedTopLevelXMIID();
	/// Sets the Toplevel Element XMIID that will be persisted to the file.
	public void setReloadedTopLevelXMIID(String newVal);
	/// Returns the Toplevel Element XMIID that was loaded from the file.
	public String getReloadedPresentationXMIID();
	/// Sets the Toplevel Element XMIID that will be persisted to the file.
	public void setReloadedPresentationXMIID(String newVal);
	/// Returns the Toplevel Element XMIID that was loaded from the file.
	public String getReloadedOwnerPresentationXMIID();
	/// Sets the Toplevel Element XMIID that will be persisted to the file.
	public void setReloadedOwnerPresentationXMIID(String newVal);
	public IStrings getReferredElements();
	public void setReferredElements(IStrings newVal);
	public boolean getWasModelElementDeleted();
	public void setWasModelElementDeleted(boolean newVal);
	public boolean getFailedToCreateDrawEngine();
	public void setFailedToCreateDrawEngine(boolean newVal);
	public IPresentationElement createPresentationElement(IElement pElem);

	public IElement createNew(INamespace space, String initStr);

	public String getTopLevelMEIDValue();
	
	/// Returns true if any portion of this object is visbible on the screen.
	public boolean isOnTheScreen(TSEGraphics g);
	}
