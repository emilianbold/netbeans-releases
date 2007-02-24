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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEColor;

import java.awt.Color;

/*
 *
 * @author KevinM
 *
 */
public interface IEdgeDrawEngine extends IDrawEngine
{
	/**
	 * Tells the edge to swap ends.
	*/
	public long swapEdgeEnds( int nNewSourceEndID, int nNewTargetEndID );

	/**
	 * Does this edge allow reconnections?
	*/
	public boolean getAllowReconnection();


	/**
	 * Returns the color that the edge last used
	 */
	public TSEColor getColor();
	public TSEColor getSelectedColor();
	public TSEColor getStateColor();
	
        /**
         * Returns the String that gets appended to the meta type init string, can be null
         * generally used for the assocation, composition, aggregation edge drawEngines.
         * It has been added so the Connection tools can stay abstract IEdgeDrawEngine drivers.
         */
	public String getMetaTypeInitString();

	/*
	 * Returns the Edge.
	 */
	public TSEEdge getEdge();

	/**
	 * Verify the ends are correct
	 */
	public void verifyEdgeEnds();
	
	/*
	 * Returns the Color used when drawing Edges (Relations)
	 */
	public Color getLineColor();
	public int setLineColor(String resourceName, int r, int g, int b);
	public int setLineColor(String resourceName, Color color);
}
