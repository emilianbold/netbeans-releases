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



package org.netbeans.modules.uml.ui.products.ad.ADDrawEngines;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;

/**
 * @author KevinM
 *
 */
public interface IADDrawEngineButtonHandler extends IProductButtonHandler {
	
	public final static int MBK_SEPARATOR = 0;
	public final static int MBK_PROPERTIES = 1;
	public final static int MBK_LIFELINE_REALIZE_PART_PORT = 2;
	public final static int MBK_LIFELINE_REALIZE_PART_ATTRIBUTE = 3;
	public final static int MBK_LIFELINE_REALIZE_PART_PARAMETER = 4;
	public final static int MBK_LINK_END_ORDINARY_AGGREGATE = 5;
	public final static int MBK_LINK_END_COMPOSITE_AGGREGATE = 6;
	public final static int MBK_LINK_END_REMOVE_AGGREGATE = 7;
	public final static int MBK_LINK_END_NAVIGABLE = 8;
	public final static int MBK_LINK_END_REVERSE_ENDS = 9;
	// Does a layout of the link to move the labels back to their original positions
	public final static int MBK_RELAYOUT_LABELS = 10;
	// Shows the associationname, if not there it creates one
	public final static int MBK_SHOW_ASSOCIATION_NAME = 11;
	// Shows both rolenames
	public final static int MBK_SHOW_BOTH_ROLENAMES = 12;
	// Shows both multiplicities
	public final static int MBK_SHOW_BOTH_MULTIPLICITIES = 13;
	// Shows the rolename for the end we're nearest to
	public final static int MBK_SHOW_ROLENAME = 14;
	// Shows the interface name
	public final static int MBK_SHOW_INTERFACENAME = 15;
	// Shows the multiplicity for the end we're nearest to
	public final static int MBK_SHOW_MULTIPLICITY = 16;
	// Sets the multiplicity to 0..1
	public final static int MBK_SET_MULTIPLICITY_0_1 = 17;
	// Sets the multiplicity to 0..*
	public final static int MBK_SET_MULTIPLICITY_0_STAR = 18;
	// Sets the multiplicity to *
	public final static int MBK_SET_MULTIPLICITY_STAR = 19;
	// Sets the multiplicity to 1
	public final static int MBK_SET_MULTIPLICITY_1 = 20;
	// Sets the multiplicity to 1..*
	public final static int MBK_SET_MULTIPLICITY_1_STAR = 21;
	// Shows all labels
	public final static int MBK_SHOW_ALL_LABELS = 22;
	// Customizes the compartments
	public final static int MBK_CUSTOMIZE = 23;
	// Shows the stereotype
	public final static int MBK_SHOW_STEREOTYPE = 24;
	// Shows the name
	public final static int MBK_SHOW_NAME_LABEL = 25;
	// Shows the binding on a derivation
	public final static int MBK_SHOW_BINDING = 26;
	// Shows the guard condition on the activity edge
	public final static int MBK_SHOW_GUARD_CONDITION = 27;
	// Shows the pre condition on the transition
	public final static int MBK_SHOW_PRE_CONDITION = 28;
	// Shows the post condition on the transition
	public final static int MBK_SHOW_POST_CONDITION = 29;
	// Shows the name on the activity edge
	public final static int MBK_SHOW_ACTIVITYEDGE_NAME = 30;
	// Inserts an extension point into a use case
	public final static int MBK_INSERT_USECASE_EXTENSIONPOINT = 31;
	// Used to change graphic states
	public final static int MBK_SHAPE_RECTANGLE = 32;
	public final static int MBK_SHAPE_ROUNDED_RECTANGLE = 33;
	public final static int MBK_SHAPE_ELLIPSE = 34;
	public final static int MBK_SHAPE_PENTAGON = 35;
	public final static int MBK_SHAPE_HEXAGON1 = 36;
	public final static int MBK_SHAPE_HEXAGON2 = 37;
	public final static int MBK_SHAPE_OCTAGON = 38;
	public final static int MBK_SHAPE_TRIANGLE = 39;
	public final static int MBK_SHAPE_TRIANGLE_DOWN = 40;
	public final static int MBK_SHAPE_TRIANGLE_LEFT = 41;
	public final static int MBK_SHAPE_TRIANGLE_RIGHT = 42;
	public final static int MBK_SHAPE_DIAMOND = 43;
	public final static int MBK_SHAPE_PARALLELOGRAM = 44;
	public final static int MBK_SHAPE_STAR = 45;
	public final static int MBK_SHAPE_CROSS = 46;
	// Enable and disable containment
	public final static int MBK_CONTAINMENT_ENABLE = 47;
	public final static int MBK_CONTAINMENT_DISABLE = 48;
	// Enables and disables the events and transitions compartment
	public final static int MBK_STATE_EVENT_TRANSITIONS_COMPARTMENT_SHOW = 49;
	public final static int MBK_STATE_EVENT_TRANSITIONS_COMPARTMENT_HIDE = 50;
	// Pseudostate stuff
	public final static int MBK_SHOW_PSEUDOSTATE_NAME = 51;
	// FinalState stuff
	public final static int MBK_SHOW_FINALSTATE_NAME = 52;
	// Adding and Removing Ports from components
	public final static int MBK_COMPONENT_PORT_NAME = 53;
	public final static int MBK_COMPONENT_PORT_NAME_END = MBK_COMPONENT_PORT_NAME + 100;
	// Shows qualifiers
	public final static int MBK_QUALIFIERS = MBK_COMPONENT_PORT_NAME_END + 1;
	public final static int MBK_INVALID = -1;
								
	
	/// Adds Port on Component specific stuff
	public void addPortMenuItems(IDrawEngine pDrawEngine, IProductContextMenu pContextMenu);

	/// Adds Node specific stuff
	public void addCustomizeMenuItems(IProductContextMenu pContextMenu);

	/// Adds PseudoState specific stuff
	public void addPseudoStateMenuItems(IProductContextMenu pContextMenu);

	/// Adds FinalState specific stuff
	public void addFinalStateMenuItems(IProductContextMenu pContextMenu);

	/// Adds the state events and transitions list compartment enable/disable stuff
	public void addEventTransitionMenuItems(IProductContextMenu pContextMenu, boolean bShow);

	/// Adds Interface Edge specific stuff
	public void addInterfaceEdgeMenuItems(IProductContextMenu pContextMenu);

	/// Adds Association and Aggregation Edge specific stuff
	public void addAssociationAndAggregationEdgeMenuItems(IProductContextMenu pContextMenu, IElement pLinkElement);

	/// Adds Activity Edge specific stuff
	public void addActivityEdgeMenuItems(IProductContextMenu pContextMenu, IElement pLinkElement);

	/// Adds Transition Edge specific stuff
	public void addTransitionEdgeMenuItems(IProductContextMenu pContextMenu, IElement pLinkElement);

	/// Adds Association Edge set multiplicity stuff
	public void addAssociationEndSetMultiplicityMenuItems(IProductContextMenu pContextMenu);

	/// Adds the Association menu items for controlling name, both ends and both multiplicities
	public void addAssociationMultiLabelSelectionsPullright(IProductContextMenu pContextMenu, boolean bInMiddle);

	/// Adds the Association menu items when the location is CMPK_END or CMPK_START
	public void addAssociationEndLabelsPullright(IProductContextMenu pContextMenu);

	/// Adds the Qualifiers button when the location is CMPK_END or CMPK_START
	public void addQualifiersButton(IProductContextMenu pContextMenu);

	/// Adds the name label
	public void addNameLabelPullright(IDrawEngine pEngine, IProductContextMenu pContextMenu);

	/// Adds the stereotype label
	public void addStereotypeLabelPullright(IDrawEngine pEngine, IProductContextMenu pContextMenu);

	/// Adds the binding label
	public void addBindLabelPullright(IProductContextMenu pContextMenu);

	/// Adds the ability to represent new parts (Port, Attribute and Parameter)
	public void addRepresentPartButtons(IProductContextMenu pContextMenu);

	/// Used to change the shape of the graphics
	public void addGraphicShapePullright(IProductContextMenu pContextMenu);

	/// Used to turn containment on and off on graphic shapes
	public void addContainmentOnOffPullright(IProductContextMenu pContextMenu);

	/// Called when a specific button is called.  The menuSelected is of kind MenuButtonKind.
	public boolean handleButton(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int menuSelected);

	/// Set the menu button sensitivity
	public void setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind);

	/// Loads a string based on an ID
	public String loadString(String id);

	/// Is the parent diagram readonly
	public boolean parentDiagramIsReadOnly();
	
	public void setDrawEngine(IDrawEngine drawengine);
	public IDrawEngine getDrawEngine();
}
