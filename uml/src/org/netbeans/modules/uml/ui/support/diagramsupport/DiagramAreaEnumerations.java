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


/*
 *
 * Created on Jun 19, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.diagramsupport;

/**
 *
 * @author Trey Spiva
 */
public interface DiagramAreaEnumerations
{
   /** Delayed initialization of a diagram - sequence diagram uses this. */
   public final static int SAK_DELAYED_INITIALIZATION = 0;

   /** Unlock the project tree */
   public final static int SAK_TREE_UNLOCK = 1;

   /** Unlock and refresh the project tree */
   public final static int SAK_TREE_UNLOCK_REFRESH = 2;

   /** Refresh the drawing area */
   public final static int SAK_REFRESH_DIAGRAM = 3;
   
   /** Calls the trackbar's layout command */
   public final static int SAK_LAYOUT_TRACKBAR = 4;
   
   /** Notification that an element has been selected */
   public final static int SPAK_SELECT_EVENT = 5;
   
   /** Delete the selected elements */
   public final static int SAK_DELETE_SELECTED = 6;
   
   /** Found an invalid zoom.  Go back to 1.0 */
   public final static int SAK_FOUND_INVALID_ZOOM = 7;
   
   /** Asks the user about disabling auto aliasing */
   public final static int SAK_DISABLE_AUTO_ALIASING = 8;
   
   /** Sets focus to this diagram */
   public final static int SAK_SETFOCUS = 9;
   
   /** Force a discover relationships for the diagram. */
   public final static int SAK_DISCOVER_RELATIONSHIPS = 10;

   /** Size the IPresentationElements node to its contents */
   public final static int SPAK_SIZETOCONTENTS = 0;
   
   /** Discards the presentation elements bends (if it is an edge) */
   public final static int SPAK_DISCARDALLBENDS = 1;
   
   /** Adds the presentation element to the track bar */
   public final static int SPAK_ADDTOTRACKBAR = 2;
   
   /** Updates the track bar element associated with the presentation element */
   public final static int SPAK_UPDATE_TRACKBAR = 3;
   
   /** Moves a lifeline so that its create message is horizontal */
   public final static int SPAK_LIFELINE_MAKECREATEHORIZONTAL = 4;
   
   /** Move and/or size the IPresentationElements node to its contents */
   public final static int SPAK_REPOSITIONTOCONTENTS = 5;
   
   /** Invalidate (and thus redraw) this IPresentationElement */
   public final static int SPAK_INVALIDATE = 6;
   
   /** Edits this label */
   public final static int SPAK_EDITLABEL = 7;
   
   /** Calls ValidateNode() for the draw engine of this IPresentationElement */
   public final static int SPAK_VALIDATENODE = 8;
   
   /** Moves the presentation element behind any contained elements in the stacking order */
   public final static int SPAK_MOVEBEHINDCONTAINED = 9;
   
   /** Deletes and reinitializes all labels */
   public final static int SPAK_DELETEANDREINITIALIZEALLLABELS = 10;
   
   /** Autoroutes the edge (if it is an edge) */
   public final static int SPAK_AUTOROUTE_EDGES = 11;
   
   /** Resizes a container to contian the presentation element */
   public final static int SPAK_RESIZETOCONTAIN = 12;
   
   /** Selects the presentation element */
   public final static int SPAK_SELECT = 13;
   
   /** Deselects the presentation element */
   public final static int SPAK_DESELECT = 14;
   
   /** Performs relationship discovery on the presentation elements */
   public final static int SPAK_DISCOVER_RELATIONSHIPS = 15;
   
   /** Does a relayout of any labels */
   public final static int SPAK_RELAYOUTALLLABELS = 16;


   /** Handles the deep sync broadcast action = 0; which can happen while presentation elements are being created...a bad spot */
   public final static int SEAK_DEEPSYNC_BROADCAST = 0;
   
   /** Handles the deep sync broadcast action = 0; it also resizes them to contents. */
   public final static int SEAK_DEEPSYNC_AND_RESIZE_BROADCAST = 1;
   
   /** Performs relationship discovery on these elements */
   public final static int SEAK_DISCOVER_RELATIONSHIPS = 2;
   
   /** Reconnects presentation elements to their model elements */
   public final static int SEAK_RECONNECT_PRESENTATION_ELEMENTS = 3;
   
   /** Resize the IPresentationElement the logical delta from the current coordinates */
   public final static int TAK_RESIZE = 0;
   
   /** Resize the IPresentationElement to the specified logical coordinates */
   public final static int TAK_RESIZETO = 1;
   
   /** Move the IPresentationElement the logical delta from the current coordinates */
   public final static int TAK_MOVE = 2;
   
   /** Move the IPresentationElement according to the logical coordinates */
   public final static int TAK_MOVETO = 3;
   
   /** Change the layout of the diagram */
   public final static int TAK_LAYOUTCHANGE = 4;
   
   /** Change the layout of the diagram without asking the user for permission */
   public final static int TAK_LAYOUTCHANGE_SILENT = 5;
   
   /** Change the layout of the diagram = 0; ignoring containment */
   public final static int TAK_LAYOUTCHANGE_IGNORECONTAINMENT = 6;
   
   /** Change the layout of the diagram = 0; ignoring containment = 0; and silent */
   public final static int TAK_LAYOUTCHANGE_IGNORECONTAINMENT_SILENT = 7;
   
   /** Perform a deep sync on the diagram */
   public final static int SBK_DEEP_SYNC = 0;
   
   /** Turn tooltips on */
   public final static int SBK_TOOLTIPS_ON = 1;
   
   /** Turn tooltips off */
   public final static int SBK_TOOLTIPS_OFF = 2;
   
   /** Clear clipboard */
   public final static int SBK_CLEAR_CLIPBOARD = 3;
   /** Tells all diagrams to flush their delayed action queue. */
   
   /** Perform a deep sync on the presentation elements attached to the model element(s) and resize to contents */
   public final static int EBK_DEEP_SYNC = 0;
   
   /** Perform a deep sync on the presentation elements attached to the model element(s) and resize to contents */
   public final static int EBK_DEEP_SYNC_AND_RESIZE = 1;
   
   /** Compartment has been selected */
   public final static int CAK_SELECTED = 0;
   
   /** Compartment has been unselected */
   public final static int CAK_UNSELECTED = 1;
   
   /** Collapse this compartment */
   public final static int CAK_COLLAPSE = 2;
   
   /** Uncollapse this compartment */
   public final static int CAK_UNCOLLAPSE = 3;
   
   /** Namespace property has changed */
   public final static int DAPK_NAMESPACE = 0;
   
   /** Name property has changed */
   public final static int DAPK_NAME = 1;
   
   /** Documentation property has changed */
   public final static int DAPK_DOCUMENTATION = 2;
   
   /** The layout has changed. */
   public final static int DAPK_LAYOUT = 3;
   
   /** The dirty state has changed. */
   public final static int DAPK_DIRTYSTATE = 4;
   
   /** Zoom has changed. */
   public final static int DAPK_ZOOM = 5;
   
   /** Readonly state has changed. */
   public final static int DAPK_READONLY = 6;
   
   /** Show alias state has changed. */
   public final static int DAPK_FRIENDLYNAMESCHANGE = 7;
   
   /** Alias property has changed */
   public final static int  DAPK_ALIAS = 8;
}
