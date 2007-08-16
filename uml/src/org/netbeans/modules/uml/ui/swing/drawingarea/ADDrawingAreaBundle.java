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

package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import org.openide.util.NbBundle;
/**
 * This class holds locale-specific objects. By changing values
 * one can create locale-specific versions of application resources.
 * The name of the resource bundle should reflect the locale for which
 * it is designed.
 */
public class ADDrawingAreaBundle extends ADDrawingAreaResourceBundle
{
        private static ResourceBundle mBundle = NbBundle.getBundle(ADDrawingAreaBundle.class);
	/**
	 * This method gives access to the resource table defined
	 * in this resource bundle.
	 */
	public Object[][] getContents()
	{
		return (contents);
	}

	/**
	 * This is the menu shortcut key mask for this platform
	 */
	private static int MENU_SHORTCUT_KEY_MASK = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

	// ---------------------------------------------------------------------
	// Section: Resource table
	// ---------------------------------------------------------------------
	//
	static final Object[][] contents = {

		// --------------------------------------------------------------------
		// Section: Toolbars
		//
		// The follwing section defines toolbars of the application.
		// Each toolbar must start with a "toolbar" keyword followed by
		// anything else. The value on the right defines the name of
		// the toolbar resource, which is used to determine if the
		// toolbar is floatable, where it is located, etc. Next follows
		// a number of items. An item with an empty name becomes a
		// separator. Each toolbar definition ends with "toolbar" with
		// an empty string in the value field of the resource array.
		// --------------------------------------------------------------------

		// AD Editor main toolbar
		{ "toolbar.main", "toolbar.main" }, {			
			//"item", "main.printDiagram" }, {
                        "item", "main.select" }, {			
//			"item", "" }, {			
			"item", "main.pan" }, {
			//"item", "main.changeSpacing" }, {
			"item", "main.zoomWithMarquee" }, {
			"item", "main.zoomInteractively" }, {
			"item", "main.navigateLink" }, {
                        "item", "" }, {		
                        "item", "main.previewDiagram" }, {
                        "item", "" }, {
			"item", "main.overviewWindow" }, {
			"item", "" }, {                        
                        "item", "main.ExportAsImage" }, {
			"item", "main.diagramSync" }, {
//			"item", "main.showFriendly" }, {
			"item", "main.relationshipDiscovery" }, {
			"item", "" }, {
			"item", "main.fitToWindow" }, {
			"item", "zoom.comboBox" }, {
			"item", "main.zoomIn" }, {
			"item", "main.zoomOut" }, {
			"item", "" }, {
			"item", "main.moveForword" }, {
			"item", "main.moveToFront" }, {
			"item", "main.moveBackward" }, {
			"item", "main.moveToBack" }, {
			//"item", "main.layout.circularLayout" }, {
                        "item", "" }, {		
			"item", "main.layout.hierarchicalLayout" }, {
			"item", "main.layout.orthogonalLayout" }, {
			"item", "main.layout.symmetricLayout" }, {
			//"item", "main.layout.treeLayout" }, {
			"item", "main.layout.layoutSequenceDiagram" }, {
			//"item", "main.layout.relayout" }, {
			"item", "main.layout.incrementalLayout" }, {
			"toolbar.main", "" },

		// toolbar resources specifying location and floatability 
		{
			"toolbar.main.location", BorderLayout.NORTH }, {
			"toolbar.main.sublocation", BorderLayout.NORTH }, {
			"toolbar.main.orientation", "horizontal" }, {
			"toolbar.main.floatable", "false" }, {

			"toolbar.uml.location", BorderLayout.NORTH }, {
			"toolbar.uml.sublocation", BorderLayout.CENTER }, {
			"toolbar.uml.orientation", "horizontal" }, {
			"toolbar.uml.floatable", "false" },

		// ----------------------------------------------------------------------
		// Section: Items specifications
		//
		// Each may have a number of properties, defined here. The most obvious
		// are:
		// text - how the item is shown to the user,
		// mnemonic - which letter activates the item,
		// command - the action associated with the item,
		// icon - the image that is shown with the item,
		// tooltip - the text shown when the mouse hovers above the item
		// accelerator - the hot key that activates the item
		// ----------------------------------------------------------------------

		// AD Editor toolbars
		{
			"main.previewDiagram.text",  mBundle.getString("main.previewDiagram.text") }, {
			"main.previewDiagram.command", ADDrawingAreaConstants.PRINT_PREVIEW }, {
			"main.previewDiagram.icon", "print-preview.png" }, {
			"main.previewDiagram.tooltip",  mBundle.getString("main.previewDiagram.tooltip") }, {

			//"main.printDiagram.text",  mBundle.getString("main.printDiagram.text") }, {
			//"main.printDiagram.command", ADDrawingAreaConstants.PRINT_GRAPH }, {
			//"main.printDiagram.icon", "Print.gif" }, {
			//"main.printDiagram.tooltip", mBundle.getString("main.printDiagram.tooltip") }, {

			"main.ExportAsImage.text",  mBundle.getString("main.ExportAsImage.text") }, {
			"main.ExportAsImage.command", ADDrawingAreaConstants.SAVE_AS_IMAGE }, {
			"main.ExportAsImage.icon", "export-as-image.png" }, {
			"main.ExportAsImage.tooltip", mBundle.getString("main.ExportAsImage.tooltip") }, {

                        "main.select.group", "select_pan_zoom" }, {
                        "main.select.default", "true" }, {
                        "main.select.checked", "true" }, {
			"main.select.text", mBundle.getString("main.select.text") }, {
			"main.select.command", ADDrawingAreaConstants.SELECT_STATE }, {
			"main.select.icon", "selection-arrow.png" }, {
			"main.select.tooltip", mBundle.getString("main.select.tooltip") }, {

                        "main.pan.group", "select_pan_zoom" }, {    
			"main.pan.text", mBundle.getString("main.pan.text") }, {
			"main.pan.command", ADDrawingAreaConstants.PAN_STATE }, {
			"main.pan.icon", "pan.png" }, {
			"main.pan.tooltip", mBundle.getString("main.pan.tooltip") }, {

// conover - icon does not exist
//			"main.changeSpacing.text", mBundle.getString("main.changeSpacing.text") }, {
//			"main.changeSpacing.command", ADDrawingAreaConstants.CHANGE_SPACING }, {
//			"main.changeSpacing.icon", "ChangeSpacing.gif" }, {
//			"main.changeSpacing.tooltip", mBundle.getString("main.changeSpacing.tooltip") }, {

                        "main.zoomWithMarquee.group", "select_pan_zoom" }, {    
			"main.zoomWithMarquee.text", mBundle.getString("main.zoomWithMarquee.text") }, {
			"main.zoomWithMarquee.command", ADDrawingAreaConstants.ZOOM_STATE }, {
			"main.zoomWithMarquee.icon", "magnify.png" }, {
			"main.zoomWithMarquee.tooltip", mBundle.getString("main.zoomWithMarquee.tooltip") }, {

                        "main.zoomInteractively.group", "select_pan_zoom" }, {    
			"main.zoomInteractively.text", mBundle.getString("main.zoomInteractively.text") }, {
			"main.zoomInteractively.command", ADDrawingAreaConstants.INTERACTIVE_ZOOM_STATE }, {
			"main.zoomInteractively.icon", "interactive-zoom.png" }, {
			"main.zoomInteractively.tooltip", mBundle.getString("main.zoomInteractively.tooltip") }, {

                        "main.navigateLink.group", "select_pan_zoom" }, {
			"main.navigateLink.text", mBundle.getString("main.navigateLink.text") }, {
			"main.navigateLink.command", ADDrawingAreaConstants.EDGE_NAVIGATION_STATE }, {
			"main.navigateLink.icon", "navigate-link.png" }, {
			"main.navigateLink.tooltip", mBundle.getString("main.navigateLink.tooltip") }, {

			"main.overviewWindow.text", mBundle.getString("main.overviewWindow.text") }, {
			"main.overviewWindow.command", ADDrawingAreaConstants.OVERVIEW_WINDOW }, {
			"main.overviewWindow.icon", "overview.png" }, {
			"main.overviewWindow.tooltip", mBundle.getString("main.overviewWindow.tooltip") }, {

			"main.diagramSync.text", mBundle.getString("main.diagramSync.text") }, {
			"main.diagramSync.command", ADDrawingAreaConstants.DIAGRAM_SYNC }, {
			"main.diagramSync.icon", "sync-diagrams.png" }, {
			"main.diagramSync.tooltip", mBundle.getString("main.diagramSync.tooltip") }, {

//			"main.showFriendly.text", mBundle.getString("main.showFriendly.text") }, {
//			"main.showFriendly.command", ADDrawingAreaConstants.SHOW_FRIENDLY }, {
//			"main.showFriendly.icon", "show-friendly-names.png" }, {
//			"main.showFriendly.tooltip", mBundle.getString("main.showFriendly.tooltip") }, {

			"main.relationshipDiscovery.text", mBundle.getString("main.relationshipDiscovery.text") }, {
			"main.relationshipDiscovery.command", ADDrawingAreaConstants.RELATION_DISCOVERY }, {
			"main.relationshipDiscovery.icon", "relationship-discovery.png" }, {
			"main.relationshipDiscovery.tooltip", mBundle.getString("main.relationshipDiscovery.tooltip") }, {

			"main.fitToWindow.text", mBundle.getString("main.fitToWindow.text") }, {
			"main.fitToWindow.command", ADDrawingAreaConstants.ZOOM_AUTO_FIT }, {
			"main.fitToWindow.icon", "fit-to-window.png" }, {
			"main.fitToWindow.tooltip", mBundle.getString("main.fitToWindow.tooltip") }, {

			"main.zoomIn.text", mBundle.getString("main.zoomIn.text") }, {
			"main.zoomIn.command", ADDrawingAreaConstants.ZOOM_IN }, {
			"main.zoomIn.icon", "zoom-in.png" }, {
			"main.zoomIn.tooltip", mBundle.getString("main.zoomIn.tooltip") }, {

			"main.zoomOut.text", mBundle.getString("main.zoomOut.text") }, {
			"main.zoomOut.command", ADDrawingAreaConstants.ZOOM_OUT }, {
			"main.zoomOut.icon", "zoom-out.png" }, {
			"main.zoomOut.tooltip", mBundle.getString("main.zoomOut.tooltip") }, {

			"main.moveForword.text", mBundle.getString("main.moveForword.text") }, {
			"main.moveForword.command", ADDrawingAreaConstants.MOVE_FORWORD }, {
			"main.moveForword.icon", "move-forward.png" }, {
			"main.moveForword.tooltip", mBundle.getString("main.moveForword.tooltip") }, {

			"main.moveToFront.text", mBundle.getString("main.moveToFront.text") }, {
			"main.moveToFront.command", ADDrawingAreaConstants.MOVE_TO_FRONT }, {
			"main.moveToFront.icon", "move-front.png" }, {
			"main.moveToFront.tooltip", mBundle.getString("main.moveToFront.tooltip") }, {

			"main.moveBackward.text", mBundle.getString("main.moveBackward.text") }, {
			"main.moveBackward.command", ADDrawingAreaConstants.MOVE_BACKWARD }, {
			"main.moveBackward.icon", "move-backward.png" }, {
			"main.moveBackward.tooltip", mBundle.getString("main.moveBackward.tooltip") }, {

			"main.moveToBack.text", mBundle.getString("main.moveToBack.text") }, {
			"main.moveToBack.command", ADDrawingAreaConstants.MOVE_TO_BACK }, {
			"main.moveToBack.icon", "move-to-back.png" }, {
			"main.moveToBack.tooltip", mBundle.getString("main.moveToBack.tooltip") }, {

			//"main.layout.circularLayout.text", mBundle.getString("main.layout.circularLayout.text") }, {
			//"main.layout.circularLayout.icon", "CircularLayout.gif" }, {
			//"main.layout.circularLayout.tooltip", mBundle.getString("main.layout.circularLayout.tooltip") }, {
			//"main.layout.circularLayout.command", ADDrawingAreaConstants.APPLY_LAYOUT + "." + TSDGraph.CIRCULAR }, {
                        
                        "main.layout.hierarchicalLayout.group", "layout" }, {
			"main.layout.hierarchicalLayout.text", mBundle.getString("main.layout.hierarchicalLayout.text") }, {
			"main.layout.hierarchicalLayout.icon", "hierarchical-layout.png" }, {
			"main.layout.hierarchicalLayout.tooltip", mBundle.getString("main.layout.hierarchicalLayout.tooltip") }, {
			//"main.layout.hierarchicalLayout.command", ADDrawingAreaConstants.APPLY_LAYOUT + "." + TSDGraph.HIERARCHICAL }, {
                        "main.layout.hierarchicalLayout.command", ADDrawingAreaConstants.APPLY_LAYOUT + "." + ILayoutKind.LK_HIERARCHICAL_LAYOUT }, {

                        "main.layout.orthogonalLayout.group", "layout" }, {    
			"main.layout.orthogonalLayout.text", mBundle.getString("main.layout.orthogonalLayout.text") }, {
			"main.layout.orthogonalLayout.icon", "orthogonal-layout.png" }, {
			"main.layout.orthogonalLayout.tooltip", mBundle.getString("main.layout.orthogonalLayout.tooltip") }, {
			//"main.layout.orthogonalLayout.command", ADDrawingAreaConstants.APPLY_LAYOUT + "." + TSDGraph.ORTHOGONAL }, {
                         "main.layout.orthogonalLayout.command", ADDrawingAreaConstants.APPLY_LAYOUT + "." + ILayoutKind.LK_ORTHOGONAL_LAYOUT}, {
                          
                        "main.layout.symmetricLayout.group", "layout" }, {     
			"main.layout.symmetricLayout.text", mBundle.getString("main.layout.symmetricLayout.text") }, {
			"main.layout.symmetricLayout.icon", "symmetric-layout.png" }, {
			"main.layout.symmetricLayout.tooltip", mBundle.getString("main.layout.symmetricLayout.tooltip") }, {
			//"main.layout.symmetricLayout.command", ADDrawingAreaConstants.APPLY_LAYOUT + "." + TSDGraph.SYMMETRIC }, {
                        "main.layout.symmetricLayout.command", ADDrawingAreaConstants.APPLY_LAYOUT + "." + ILayoutKind.LK_SYMMETRIC_LAYOUT }, {

			//"main.layout.treeLayout.text", mBundle.getString("main.layout.treeLayout.text") }, {
			//"main.layout.treeLayout.icon", "TreeLayout.gif" }, {
			//"main.layout.treeLayout.tooltip", mBundle.getString("main.layout.treeLayout.tooltip") }, {
			//"main.layout.treeLayout.command", ADDrawingAreaConstants.APPLY_LAYOUT + "." + TSDGraph.TREE }, {
                        //"main.layout.treeLayout.command", ADDrawingAreaConstants.APPLY_LAYOUT + "." + ILayoutKind.LK_TREE_LAYOUT }, {

			"main.layout.layoutSequenceDiagram.text", mBundle.getString("main.layout.layoutSequenceDiagram.text") }, {
			"main.layout.layoutSequenceDiagram.icon", "sequence-layout.png" }, {
			"main.layout.layoutSequenceDiagram.tooltip", mBundle.getString("main.layout.layoutSequenceDiagram.tooltip") }, {
			"main.layout.layoutSequenceDiagram.command", ADDrawingAreaConstants.SEQUENCE_LAYOUT }, {

			//"main.layout.relayout.text", mBundle.getString("main.layout.relayout.text") }, {
			//"main.layout.relayout.icon", "Relayout.gif" }, {
			//"main.layout.relayout.tooltip", mBundle.getString("main.layout.relayout.tooltip") }, {
			//"main.layout.relayout.command", ADDrawingAreaConstants.APPLY_LAYOUT }, {

			"main.layout.incrementalLayout.text", mBundle.getString("main.layout.incrementalLayout.text") }, {
			"main.layout.incrementalLayout.icon", "incremental-layout.png" }, {
			"main.layout.incrementalLayout.tooltip", mBundle.getString("main.layout.incrementalLayout.tooltip") }, {
			"main.layout.incrementalLayout.command", ADDrawingAreaConstants.INCREMENTAL_LAYOUT },

		//		----------------------------------------------------------------------
		//		Section: Key binding
		//
		//		This section defines the key bindings. The key binding
		//		starts with the keyword "key". The number that follows
		//		is the index number which must be increased when you add
		//		new key bindings.
		//		Each key event has a key code, modifier value, command and
		//		an focus value.
		//		----------------------------------------------------------------------

		{
			"key.1.keyCode", String.valueOf(KeyEvent.VK_PLUS)}, {
			"key.1.modifiers", String.valueOf(0)}, {
			"key.1.command", ADDrawingAreaConstants.ZOOM_IN }, {
			"key.1.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.2.keyCode", String.valueOf(KeyEvent.VK_ADD)}, {
			"key.2.modifiers", String.valueOf(0)}, {
			"key.2.command", ADDrawingAreaConstants.ZOOM_IN }, {
			"key.2.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.3.keyCode", String.valueOf(KeyEvent.VK_EQUALS)}, {
			"key.3.modifiers", String.valueOf(0)}, {
			"key.3.command", ADDrawingAreaConstants.ZOOM_IN }, {
			"key.3.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.4.keyCode", String.valueOf(KeyEvent.VK_MINUS)}, {
			"key.4.modifiers", String.valueOf(0)}, {
			"key.4.command", ADDrawingAreaConstants.ZOOM_OUT }, {
			"key.4.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.5.keyCode", String.valueOf(KeyEvent.VK_SUBTRACT)}, {
			"key.5.modifiers", String.valueOf(0)}, {
			"key.5.command", ADDrawingAreaConstants.ZOOM_OUT }, {
			"key.5.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.6.keyCode", String.valueOf(KeyEvent.VK_PLUS)}, {
			"key.6.modifiers", String.valueOf(KeyEvent.SHIFT_DOWN_MASK)}, {
			"key.6.command", ADDrawingAreaConstants.ZOOM_IN }, {
			"key.6.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.7.keyCode", String.valueOf(KeyEvent.VK_ADD)}, {
			"key.7.modifiers", String.valueOf(KeyEvent.SHIFT_DOWN_MASK)}, {
			"key.7.command", ADDrawingAreaConstants.ZOOM_IN }, {
			"key.7.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.8.keyCode", String.valueOf(KeyEvent.VK_EQUALS)}, {
			"key.8.modifiers", String.valueOf(KeyEvent.SHIFT_DOWN_MASK)}, {
			"key.8.command", ADDrawingAreaConstants.ZOOM_IN }, {
			"key.8.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.9.keyCode", String.valueOf(KeyEvent.VK_MINUS)}, {
			"key.9.modifiers", String.valueOf(KeyEvent.SHIFT_DOWN_MASK)}, {
			"key.9.command", ADDrawingAreaConstants.ZOOM_OUT }, {
			"key.9.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.10.keyCode", String.valueOf(KeyEvent.VK_SUBTRACT)}, {
			"key.10.modifiers", String.valueOf(KeyEvent.SHIFT_DOWN_MASK)}, {
			"key.10.command", ADDrawingAreaConstants.ZOOM_OUT }, {
			"key.10.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.11.keyCode", String.valueOf(KeyEvent.VK_PLUS)}, {
			"key.11.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.11.command", ADDrawingAreaConstants.ZOOM_IN }, {
			"key.11.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.12.keyCode", String.valueOf(KeyEvent.VK_ADD)}, {
			"key.12.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.12.command", ADDrawingAreaConstants.ZOOM_IN }, {
			"key.12.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.13.keyCode", String.valueOf(KeyEvent.VK_EQUALS)}, {
			"key.13.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.13.command", ADDrawingAreaConstants.ZOOM_IN }, {
			"key.13.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.14.keyCode", String.valueOf(KeyEvent.VK_MINUS)}, {
			"key.14.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.14.command", ADDrawingAreaConstants.ZOOM_OUT }, {
			"key.14.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.15.keyCode", String.valueOf(KeyEvent.VK_SUBTRACT)}, {
			"key.15.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.15.command", ADDrawingAreaConstants.ZOOM_OUT }, {
			"key.15.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.16.keyCode", String.valueOf(KeyEvent.VK_BACK_SPACE)}, {
			"key.16.modifiers", String.valueOf(0)}, {
			"key.16.command", ADDrawingAreaConstants.DELETE_SELECTED }, {
			"key.16.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.17.keyCode", String.valueOf(KeyEvent.VK_ESCAPE)}, {
			"key.17.modifiers", String.valueOf(0)}, {
			"key.17.command", ADDrawingAreaConstants.ACTION_ABORT }, {
                        "key.17.focus", String.valueOf(JComponent.WHEN_IN_FOCUSED_WINDOW)}, {
//			"key.17.focus", String.valueOf(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)}, {

			"key.18.keyCode", String.valueOf(KeyEvent.VK_LEFT)}, {
			"key.18.modifiers", String.valueOf(0)}, {
			"key.18.command", ADDrawingAreaConstants.SCROLL_LEFT }, {
			"key.18.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.19.keyCode", String.valueOf(KeyEvent.VK_RIGHT)}, {
			"key.19.modifiers", String.valueOf(0)}, {
			"key.19.command", ADDrawingAreaConstants.SCROLL_RIGHT }, {
			"key.19.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.20.keyCode", String.valueOf(KeyEvent.VK_UP)}, {
			"key.20.modifiers", String.valueOf(0)}, {
			"key.20.command", ADDrawingAreaConstants.SCROLL_UP }, {
			"key.20.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.21.keyCode", String.valueOf(KeyEvent.VK_DOWN)}, {
			"key.21.modifiers", String.valueOf(0)}, {
			"key.21.command", ADDrawingAreaConstants.SCROLL_DOWN }, {
			"key.21.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.22.keyCode", String.valueOf(KeyEvent.VK_SPACE)}, {
			"key.22.modifiers", String.valueOf(0)}, {
			"key.22.command", ADDrawingAreaConstants.NEXT_STATE }, {
			"key.22.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.23.keyCode", String.valueOf(KeyEvent.VK_LEFT)}, {
			"key.23.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.23.command", ADDrawingAreaConstants.MOVE_LEFT }, {
			"key.23.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.24.keyCode", String.valueOf(KeyEvent.VK_RIGHT)}, {
			"key.24.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.24.command", ADDrawingAreaConstants.MOVE_RIGHT }, {
			"key.24.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.25.keyCode", String.valueOf(KeyEvent.VK_UP)}, {
			"key.25.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.25.command", ADDrawingAreaConstants.MOVE_UP }, {
			"key.25.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.26.keyCode", String.valueOf(KeyEvent.VK_DOWN)}, {
			"key.26.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.26.command", ADDrawingAreaConstants.MOVE_DOWN }, {
			"key.26.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.27.keyCode", String.valueOf(KeyEvent.VK_F6)}, {
			"key.27.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.27.command", ADDrawingAreaConstants.NEXT_WINDOW }, {
			"key.27.focus", String.valueOf(JComponent.WHEN_FOCUSED)} , {

			"key.28.keyCode", String.valueOf(KeyEvent.VK_TAB)}, {
                        "key.28.modifiers", String.valueOf(KeyEvent.CTRL_DOWN_MASK)}, {
			"key.28.command", ADDrawingAreaConstants.NEXT_WINDOW }, {
			"key.28.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.29.keyCode", String.valueOf(KeyEvent.VK_F8)}, {
			"key.29.modifiers", String.valueOf(0)}, {
			"key.29.command", ADDrawingAreaConstants.OVERVIEW_WINDOW }, {
			"key.29.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {


                        // 30 through 33 are to resize selected elements
                        // incrementally, 5 units at a time
			"key.30.keyCode", String.valueOf(KeyEvent.VK_UP)}, {
			"key.30.modifiers", String.valueOf(
                            KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)}, {
			"key.30.command", ADDrawingAreaConstants.NODE_RESIZE_TALLER}, {
			"key.30.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.31.keyCode", String.valueOf(KeyEvent.VK_DOWN)}, {
			"key.31.modifiers", String.valueOf(
                            KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)}, {
			"key.31.command", ADDrawingAreaConstants.NODE_RESIZE_SHORTER}, {
			"key.31.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.32.keyCode", String.valueOf(KeyEvent.VK_RIGHT)}, {
			"key.32.modifiers", String.valueOf(
                            KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)}, {
			"key.32.command", ADDrawingAreaConstants.NODE_RESIZE_WIDER}, {
			"key.32.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, {

			"key.33.keyCode", String.valueOf(KeyEvent.VK_LEFT)}, {
			"key.33.modifiers", String.valueOf(
                            KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)}, {
			"key.33.command", ADDrawingAreaConstants.NODE_RESIZE_THINNER}, {
			"key.33.focus", String.valueOf(JComponent.WHEN_FOCUSED)}, // {

                        
//                        copy and paste this section to add a new keystroke
//                        increment the * and enter the appropriate keystroke.
//                        Also **add a { to the end of the above line**.
//			"key.*.keyCode", String.valueOf(KeyEvent.VK_DELETE)}, {
//			"key.*.modifiers", String.valueOf(0)}, {
//			"key.*.command", ADDrawingAreaConstants.DELETE_SELECTED }, {
//			"key.*.focus", String.valueOf(JComponent.WHEN_FOCUSED)},

                // ----------------------------------------------------------------------
		// Section: Application resources
		//
		// ---------------------------------------------------------------------


		{
			"layout.server.type", "local" }, {
			"layout.server.name", "LayoutServer" }, {
			"layout.server.host", "" }, {
			"layout.server.url", "" }, {

			"graphwindow.default.width", "600" }, {
			"graphwindow.default.height", "400" }, {
			"editor.undo.limit", "50" }, {
			// "editor.icon", "Describe.jpg" }, {
			"editor.icon", "uml-project.png" }, {
			"dialog.zoom.title", mBundle.getString("dialog.zoom.title") }, {
			"dialog.zoom.message", mBundle.getString("dialog.zoom.message") }, {

			"dialog.zoomError.title", mBundle.getString("dialog.zoomError.title") }, {
			"dialog.zoomError.message",NbBundle.getMessage(ADDrawingAreaBundle.class,
                                        "dialog.zoomError.message", 
                                        ADDrawingAreaConstants.X_PLACEHOLDER,
                                        ADDrawingAreaConstants.Y_PLACEHOLDER) }, {

			"dialog.notImplemented.title", mBundle.getString("dialog.notImplemented.title") }, {
			"dialog.notImplemented.message", mBundle.getString("dialog.notImplemented.message") }, {
			"dialog.pasteError.title", mBundle.getString("dialog.pasteError.title") }, {
			"dialog.pasteError.message", mBundle.getString("dialog.pasteError.message") }, {

			"dialog.gridSize.title", mBundle.getString("dialog.gridSize.title") }, {
			"dialog.gridSize.message", mBundle.getString("dialog.gridSize.message") }, {

			"dialog.gridSizeError.title", mBundle.getString("dialog.gridSizeError.title") }, {
			"dialog.gridSizeError.message", mBundle.getString("dialog.gridSizeError.message") }, {

// conover - image does not exist
//			"dialog.open.title", mBundle.getString("dialog.open.title") }, {
//			"dialog.open.message", mBundle.getString("dialog.open.message") }, {
//			"dialog.open.icon", "open.gif" }, {

			"dialog.openError.fileNotFound.title", mBundle.getString("dialog.openError.fileNotFound.title") }, {
			"dialog.openError.fileNotFound.message", NbBundle.getMessage(ADDrawingAreaBundle.class,
                                        "dialog.openError.fileNotFound.message", 
					ADDrawingAreaConstants.FILENAME_PLACEHOLDER) }, {

			"dialog.openError.general.title", mBundle.getString("dialog.openError.general.title") }, {
			"dialog.openError.general.message",NbBundle.getMessage(ADDrawingAreaBundle.class,
                                        "dialog.openError.general.message",
                                        ADDrawingAreaConstants.FILENAME_PLACEHOLDER) }, {

			"dialog.saveAs.title", mBundle.getString("dialog.saveAs.title") }, {

			"dialog.saveAsImage.title", mBundle.getString("dialog.saveAsImage.title") }, {

			"dialog.saveConfirm.title", mBundle.getString("dialog.saveConfirm.title") }, {
			"dialog.saveConfirm.message", NbBundle.getMessage(ADDrawingAreaBundle.class, 
                                        "dialog.saveConfirm.message", 
                                        ADDrawingAreaConstants.FILENAME_PLACEHOLDER) }, {

			"dialog.discardConfirm.title", mBundle.getString("dialog.discardConfirm.title") }, {
			"dialog.discardConfirm.message", NbBundle.getMessage(ADDrawingAreaBundle.class, 
                                        "dialog.discardConfirm.message",
                                        ADDrawingAreaConstants.FILENAME_PLACEHOLDER) }, {

			"dialog.overWriteConfirm.title", mBundle.getString("dialog.overWriteConfirm.title") }, {
			"dialog.overWriteConfirm.message", NbBundle.getMessage(ADDrawingAreaBundle.class, 
                                        "dialog.overWriteConfirm.message",
                                        ADDrawingAreaConstants.FILENAME_PLACEHOLDER) }, {

			"dialog.saveError.pathNotFound.title", mBundle.getString("dialog.saveError.pathNotFound.title") }, {
			"dialog.saveError.pathNotFound.message",NbBundle.getMessage(ADDrawingAreaBundle.class, 
                                        "dialog.saveError.pathNotFound.message",
                                        ADDrawingAreaConstants.FILENAME_PLACEHOLDER) }, {

			"dialog.layoutError.title", mBundle.getString("dialog.layoutError.title") }, {
			"dialog.layoutError.message", mBundle.getString("dialog.layoutError.message") }, {

			"dialog.objectProperties.title", mBundle.getString("dialog.objectProperties.title") }, {

			"dialog.overviewWindow.title", mBundle.getString("dialog.overviewWindow.title") }, {

			"dialog.nodePalette.title", mBundle.getString("dialog.nodePalette.title") }, {

			"dialog.printSetup.title", mBundle.getString("dialog.printSetup.title") }, {

			"dialog.printPreview.title", mBundle.getString("dialog.printPreview.title") }, {

			"dialog.error.title", mBundle.getString("dialog.error.title") }, {
			"dialog.licenseerror.title", mBundle.getString("dialog.licenseerror.title") }, {
			//"file.plain.extension", "gmf" }, { //jyothi
			//"file.compressed.extension", "gmz" }, { //jyothi
                        "file.plain.extension", "tsv" }, {
			"file.compressed.extension", "tsvz" }, {

			//"file.plain.description", "Graph Model File (*.gmf)" }, {
			//"file.compressed.description", "Compressed Graph Model File (*.gmz)" }, {
                        "file.plain.description", "Tom Sawyer Visualization (*.tsv)" }, {
			"file.compressed.description", "Compressed Tom Sawyer Visualization (*.tsvz)" }, {
                            
			"file.etlp.extension", "etlp" }, {

			"file.noname", "untitled" },


		// ---------------------------------------------------------------------
		// Section: Strings
		//
		// This section specifies string resources used within the
		// editor. Replace the 'value' strings with translations for
		// internationalization.
		// ---------------------------------------------------------------------

		// generic strings
		{
			"string.OK", mBundle.getString("string.OK") }, {
			"string.Cancel", mBundle.getString("string.Cancel") }, {
			"string.Help", mBundle.getString("string.Help") }, {

			"string.Edge", mBundle.getString("string.Edge") }, {

			"string.Above", mBundle.getString("string.Above") }, {
			"string.Below", mBundle.getString("string.Below") }, {
			"string.Top", mBundle.getString("string.Top") }, {
			"string.Bottom", mBundle.getString("string.Bottom") }, {
			"string.Left", mBundle.getString("string.Left") }, {
			"string.Right", mBundle.getString("string.Right") }, {
			"string.Center", mBundle.getString("string.Center") }, {
			"string.Dont_Care", mBundle.getString("string.Dont_Care") }, {

			"string.Untitled", mBundle.getString("string.Untitled") }, {

			"string.Off", mBundle.getString("string.Off") },

		// Node strings
		{
			"string.End_Color", mBundle.getString("string.End_Color") }, {
			"string.Desired_Group_ID", mBundle.getString("string.Desired_Group_ID") }, {
			"string.Actual_Group_ID", mBundle.getString("string.Actual_Group_ID") }, {
			"string.Left_Ports", mBundle.getString("string.Left_Ports") }, {
			"string.Right_Ports", mBundle.getString("string.Right_Ports") }, {
			"string.Top_Ports", mBundle.getString("string.Top_Ports") }, {
			"string.Bottom_Ports", mBundle.getString("string.Bottom_Ports") }, {
			"string.Invertible", mBundle.getString("string.Invertible") }, {
			"string.Stretchable", mBundle.getString("string.Stretchable") }, {
			"string.Constrained", mBundle.getString("string.Constrained") }, {
			"string.Resize_Style", mBundle.getString("string.Resize_Style") }, {
			"string.Desired_Root_Node", mBundle.getString("string.Desired_Root_Node") }, {
			"string.Is_Root_Node", mBundle.getString("string.Is_Root_Node") }, {
			"string.Horizontally_Stretchable", mBundle.getString("string.Horizontally_Stretchable") }, {
			"string.Vertically_Stretchable", mBundle.getString("string.Vertically_Stretchable") }, {
			"string.Preserve_Aspect_Ratio", mBundle.getString("string.Preserve_Aspect_Ratio") }, {
			"string.Force_Preserve_Aspect", mBundle.getString("string.Force_Preserve_Aspect") }, {
			"string.Desired_Level_Number", mBundle.getString("string.Desired_Level_Number") }, {
			"string.Actual_Level_Number", mBundle.getString("string.Actual_Level_Number") },

		// Edge strings
		{
			"string.Source_Port_Number", mBundle.getString("string.Source_Port_Number") }, {
			"string.Source_Port_Style", mBundle.getString("string.Source_Port_Style") }, {
			"string.Target_Port_Number", mBundle.getString("string.Target_Port_Number") }, {
			"string.Target_Port_Style", mBundle.getString("string.Target_Port_Style") }, {
			"string.Non_Leveling", mBundle.getString("string.Non_Leveling") }, {
			"string.Desired_Tree_Edge", mBundle.getString("string.Desired_Tree_Edge") }, {
			"string.Is_Tree_Edge", mBundle.getString("string.Is_Tree_Edge") }, {
			"string.None", mBundle.getString("string.None") }, {
			"string.Top_or_Bottom", mBundle.getString("string.Top_or_Bottom") }, {
			"string.Left_or_Right", mBundle.getString("string.Left_or_Right") }, {
			"string.Edge_Strength", mBundle.getString("string.Edge_Strength") }, {
			"string.Desired_Edge_Length", mBundle.getString("string.Desired_Edge_Length") }, {
			"string.Source", mBundle.getString("string.Source") }, {
			"string.Target", mBundle.getString("string.Target") }, {
			"string.Association", mBundle.getString("string.Association") },

		// Connector strings
		{
			"string.Movable", mBundle.getString("string.Movable") },

		// Label strings
		{
			"string.Inside", mBundle.getString("string.Inside") }, {
			"string.Outside", mBundle.getString("string.Outside") }, {
			"string.Global", mBundle.getString("string.Global") }, {
			"string.Orientation", mBundle.getString("string.Orientation") }, {
			"string.Region", mBundle.getString("string.Region") }, {
			"string.Location", mBundle.getString("string.Location") }
                        
	};

}
