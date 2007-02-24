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

/**
 * This interface defines all constants used by ADDrawingAreaControl
 */
public interface ADDrawingAreaConstants
{

	// --- ACTIONS ---------------------------------------------------------

	public static final String PRINT_PREVIEW = "PRINT_PREVIEW";
   public static final int PRINT_PREVIEW_CMD = 0;

	public static final String PRINT_GRAPH = "PRINT_GRAPH";
   public static final int PRINT_GRAPH_CMD = 1;

	public static final String SELECT_STATE = "SELECT_STATE";
   public static final int SELECT_STATE_CMD = 2;
   
	public static final String PAN_STATE = "PAN_STATE";
   public static final int PAN_STATE_CMD = 3;
   
	public static final String CHANGE_SPACING = "CHANGE_SPACING";
   public static final int CHANGE_SPACING_CMD = 4;
   
	public static final String ZOOM_STATE = "ZOOM_STATE";
   public static final int ZOOM_STATE_CMD = 5;
   
	public static final String INTERACTIVE_ZOOM_STATE = "INTERACTIVE_ZOOM_STATE";
   public static final int INTERACTIVE_ZOOM_STATE_CMD = 6;
   
	public static final String EDGE_NAVIGATION_STATE = "EDGE_NAVIGATION_STATE";
   public static final int EDGE_NAVIGATION_STATE_CMD = 7;
   
	public static final String OVERVIEW_WINDOW = "OVERVIEW_WINDOW";
   public static final int OVERVIEW_WINDOW_CMD = 8;
   
	public static final String DIAGRAM_SYNC = "DIAGRAM_SYNC";
   public static final int DIAGRAM_SYNC_CMD = 9;
   
	public static final String SHOW_FRIENDLY = "SHOW_FRIENDLY";
   public static final int SHOW_FRIENDLY_CMD = 10;
   
	public static final String RELATION_DISCOVERY = "RELATION_DISCOVERY";
   public static final int RELATION_DISCOVERY_CMD = 11;
   
	public static final String ZOOM_AUTO_FIT = "AUTO_FIT";
   public static final int ZOOM_AUTO_FIT_CMD = 12;
   
	public static final String ZOOM_IN = "ZOOM_IN";
   public static final int ZOOM_IN_CMD = 13;
   
	public static final String ZOOM_OUT = "ZOOM_OUT";
   public static final int ZOOM_OUT_CMD = 14;
   
	public static final String MOVE_FORWORD = "MOVE_FORWORD";
   public static final int MOVE_FORWORD_CMD = 15;
   
	public static final String MOVE_TO_FRONT = "MOVE_TO_FRONT";
   public static final int MOVE_TO_FRONT_CMD = 16;
   
	public static final String MOVE_BACKWARD = "MOVE_BACKWARD";
   public static final int MOVE_BACKWARD_CMD = 0;
   
	public static final String MOVE_TO_BACK = "MOVE_TO_BACK";
   public static final int MOVE_TO_BACK_CMD = 17;
   
	public static final String APPLY_LAYOUT = "APPLY_LAYOUT";
   public static final int APPLY_LAYOUT_CMD = 18;
   
	public static final String SEQUENCE_LAYOUT = "SEQUENCE_LAYOUT";
   public static final int SEQUENCE_LAYOUT_CMD = 19;
   
	public static final String RELAYOUT = "RELAYOUT";
   public static final int RELAYOUT_CMD = 20;
   
	public static final String INCREMENTAL_LAYOUT = "INCREMENTAL_LAYOUT";
   public static final int INCREMENTAL_LAYOUT_CMD = 21;
   
	public static final String CREATE_NODE_STATE = "CREATE_NODE_STATE";
   public static final int ADD_NODE_CMD = 22;
   
	public static final String CREATE_EDGE_STATE = "CREATE_EDGE_STATE";
   public static final int ADD_EDGE_CMD = 23;
   
	public static final String SAVE_AS_IMAGE = "SAVE_AS_IMAGE";
   public static final int SAVE_AS_IMAGE_CMD = 24;
   
   public static final String CREATE_NODE_DECORATOR = "CREATE_NODE_DECORATOR";
   public static final int CREATE_NODE_DECORATOR_CMD = 25;
   

	/**
	 * This command string instructs the application to abort any
	 * action that it is currently carrying out.
	 */
	public static final String ACTION_ABORT = "ACTION_ABORT";
   public static final int ACTION_ABORT_CMD = 26;

	/**
	 * This command string instructs the application to add a label to
	 * the currently selected edge.
	 */
	public static final String ADD_EDGE_LABEL = "ADD_EDGE_LABEL";
   public static final int ADD_EDGE_LABEL_CMD = 27;

	/**
	 * This command string instructs the application to add a label to
	 * the currently selected node.
	 */
	public static final String ADD_NODE_LABEL = "ADD_NODE_LABEL";
   public static final int ADD_NODE_LABEL_CMD = 28;

	/**
	 * This command string instructs the application to add a connector
	 * to the currently selected node.
	 */
	public static final String ADD_NODE_CONNECTOR = "ADD_NODE_CONNECTOR";
   public static final int ADD_NODE_CONNECTOR_CMD = 29;

	/**
	 * This command string instructs the application to terminate.
	 */
	public static final String APP_EXIT = "APP_EXIT";
   public static final int APP_EXIT_CMD = 30;

	/**
	 * This command string instructs the application to clear the
	 * current graph.
	 */
	public static final String CLEAR_ALL = "CLEAR_ALL";
   public static final int CLEAR_ALL_CMD = 31;

	/**
	 * This command string instructs the application to clear the
	 * undo/redo history.
	 */
	public static final String CLEAR_HISTORY = "CLEAR_HISTORY";
   public static final int CLEAR_HISTORY_CMD = 32;

	/**
	 * This command string instructs the application to collapse
	 * the child graph associated with a node.
	 */
	public static final String COLLAPSE = "COLLAPSE";
   public static final int COLLAPSE_CMD = 33;

	/**
	 * This command string instructs the application to collapse all
	 * nested child graphs.
	 */
	public static final String COLLAPSE_ALL = "COLLAPSE_ALL";
   public static final int COLLAPSE_ALL_CMD = 34;

	/**
	 * This command string instructs the application to collapse
	 * the child graphs associated with the selected nodes.
	 */
	public static final String COLLAPSE_SELECTED = "COLLAPSE_SELECTED";
   public static final int COLLAPSE_SELECTED_CMD = 35;

	/**
	 * This command string instructs the application to copy the
	 * selected objects to the clipboard.
	 */
	public static final String COPY_GRAPH = "COPY_GRAPH";
   public static final int COPY_GRAPH_CMD = 36;

	/**
	 * This command string instructs the application to create a child
	 * graph for a node or edge.
	 */
	public static final String CREATE_CHILD_GRAPH = "CREATE_CHILD_GRAPH";
   public static final int CREATE_CHILD_GRAPHCMD = 37;

	/**
	 * This command string instructs the application to cut the
	 * selected objects from the graph manager and place them 
	 * in the clipboard.
	 */
	public static final String CUT_GRAPH = "CUT_GRAPH";
   public static final int CUT_GRAPH_CMD = 38;

	/**
	 * This command string instructs the application to delete the
	 * child graph of the currently selected node or edge.
	 */
	public static final String DELETE_CHILD_GRAPH = "DELETE_CHILD_GRAPH";
   public static final int DELETE_CHILD_GRAPH_CMD = 39;

	/**
	 * This command string instructs the application to delete the
	 * selected objects from the graph manager.
	 */
	public static final String DELETE_SELECTED = "DELETE_SELECTED";
   public static final int DELETE_SELECTED_CMD = 40;

	/**
	 * This command string instructs the application to delete a 
	 * connector from the currently selected node.
	 */
	public static final String DELETE_NODE_CONNECTOR = "DELETE_NODE_CONNECTOR";
   public static final int DELETE_NODE_CONNECTOR_CMD = 41;

	/**
	 * This command string instructs the application to show the
	 * Drawing Preferences dialog
	 */
	public static final String DRAWING_PREFERENCES = "DRAWING_PREFERENCES";
   public static final int DRAWING_PREFERENCES_CMD = 42;

	/**
	 * This command string instructs the application to hide the
	 * slected graph.
	 */
	public static final String HIDE_SELECTED = "HIDE_SELECTED";
   public static final int HIDE_SELECTED_CMD = 43;

	/**
	 * This command string instructs the application to hide one level
	 * childrens of the selected node.
	 */
	public static final String HIDE_CHILDREN_ONE_LEVEL = "HIDE_CHILDREN_ONE_LEVEL";
   public static final int HIDE_CHILDREN_ONE_LEVEL_CMD = 44;

	/**
	 * This command string instructs the application to hide `n` level
	 * childrens of the selected node.
	 */
	public static final String HIDE_CHILDREN_N_LEVEL = "HIDE_CHILDREN_N_LEVEL";
   public static final int HIDE_CHILDREN_N_LEVEL_CMD = 45;

	/**
	 * This command string instructs the application to select the next
	 * graph window and move it to the front.
	 */
	public static final String NEXT_WINDOW = "NEXT_WINDOW";
   public static final int NEXT_WINDOW_CMD = 46;

	/**
	 * This command string instructs the application to duplicate all
	 * selected objects in the graph manager.
	 */
	public static final String DUPLICATE_GRAPH = "DUPLICATE_GRAPH";
   public static final int DUPLICATE_GRAPH_CMD = 47;

	/**
	 * This command string instructs the application to edit the text 
	 * (tag) of the selected objects.
	 */
	public static final String EDIT_TEXT = "EDIT_TEXT";
   public static final int EDIT_TEXT_CMD = 48;

	/**
	 * This command string instructs the application to expand the
	 * child graph associated with a node.
	 */
	public static final String EXPAND = "EXPAND";
   public static final int EXPAND_CMD = 49;

	/**
	 * This command string instructs the application to expand all
	 * nested child graphs.
	 */
	public static final String EXPAND_ALL = "EXPAND_ALL";
   public static final int EXPAND_ALL_CMD = 50;

	/**
	 * This command string instructs the application to expand
	 * the child graphs associated with the selected nodes.
	 */
	public static final String EXPAND_SELECTED = "EXPAND_SELECTED";
   public static final int EXPAND_SELECTED_CMD = 51;

	/**
	 * This command string instructs the application to switch to the
	 * child graph of the selected node or edge.
	 */
	public static final String GOTO_CHILD = "GOTO_CHILD";
   public static final int GOTO_CHILD_CMD = 52;

	/**
	 * This command string instructs the application to switch to the
	 * parent graph of the current graph.
	 */
	public static final String GOTO_PARENT = "GOTO_PARENT";
   public static final int GOTO_PARENT_CMD = 53;

	/**
	 * This command string instructs the application to display the
	 * root (top most) graph of the graph hierarchy.
	 */
	public static final String GOTO_ROOT = "GOTO_ROOT";
   public static final int GOTO_ROOT_CMD = 54;

	/**
	 * This command string instructs the application to change the
	 * size of the grid of the graph window.
	 */
	public static final String GRID_SIZE = "GRID_SIZE";
   public static final int GRID_SIZE_CMD = 55;

	/**
	 * This command string instructs the application to set the user
	 * specified size of the grid of the graph window.
	 */
	public static final String GRID_SIZE_CUSTOM = "GRID_SIZE_CUSTOM";
   public static final int GRID_SIZE_CUSTOM_CMD = 56;

	/**
	 * This command string instructs the application to change the
	 * visibility or type of the grid of the graph window.
	 */
	public static final String GRID_TYPE = "GRID_TYPE";
   public static final int GRID_TYPE_CMD = 57;

	/**
	 * This command string instructs the application to apply an
	 * incremental layout after expand/collapse, hide/unhide or
	 * fold/unfold actions.
	 */
	public static final String INCREMENTAL_LAYOUT_AFTER_ACTION = "INCREMENTAL_LAYOUT_AFTER_ACTION";
   public static final int INCREMENTAL_LAYOUT_AFTER_ACTION_CMD = 58;

	/**
	 * This command string instructs the application to load a graph
	 * from a file.
	 */
	public static final String LOAD_GRAPH = "LOAD_GRAPH";
   public static final int LOAD_GRAPH_CMD = 59;

	/**
	 * This command string instructs the application to close a graph.
	 */
	public static final String CLOSE_GRAPH = "CLOSE_GRAPH";
   public static final int CLOSE_GRAPH_CMD = 60;

	/**
	 * This command string instructs the application to move the 
	 * selection to the left.
	 */
	public static final String MOVE_LEFT = "MOVE_LEFT";
   public static final int MOVE_LEFT_CMD = 61;

	/**
	 * This command string instructs the application to move the 
	 * selection to the right.
	 */
	public static final String MOVE_RIGHT = "MOVE_RIGHT";
   public static final int MOVE_RIGHT_CMD = 62;

	/**
	 * This command string instructs the application to move the 
	 * selection up.
	 */
	public static final String MOVE_UP = "MOVE_UP";
   public static final int MOVE_UP_CMD = 63;

	/**
	 * This command string instructs the application to move the 
	 * selection down.
	 */
	public static final String MOVE_DOWN = "MOVE_DOWN";
   public static final int MOVE_DOWN_CMD = 64;

	/**
	 * This command string instructs the application that the 
	 * selection move is done.
	 */
	public static final String MOVE_DONE = "MOVE_DONE";
   public static final int MOVE_DONE_CMD = 65;

	/**
	 * This command string instructs the application to create a new
	 * graph.
	 */
	public static final String NEW_GRAPH = "NEW_GRAPH";
   public static final int NEW_GRAPH_CMD = 66;

	/**
	 * This command string instructs the application to open a
	 * node palette window.
	 */
	public static final String PALETTE_WINDOW = "PALETTE_WINDOW";
   public static final int PALETTE_WINDOW_CMD = 67;

	/**
	 * This command string instructs the application to paste the
	 * contents of the clipboard into the graph manager.
	 */
	public static final String PASTE_GRAPH = "PASTE_GRAPH";
   public static final int PASTE_GRAPH_CMD = 68;

	/**
	 * This command string instructs the application to show the print
	 * setup dialog.
	 */
	public static final String PRINT_SETUP = "PRINT_SETUP";
   public static final int PRINT_SETUP_CMD = 69;

	/**
	 * This command string instructs the application to redo the last
	 * undone action.
	 */
	public static final String REDO = "REDO";
   public static final int REDO_CMD = 70;

	/**
	 * This command string instructs the application to refresh the
	 * graph window.
	 */
	public static final String REFRESH_GRAPH = "REFRESH_GRAPH";
   public static final int REFRESH_GRAPH_CMD = 71;

	/**
	 * This command string instructs the application to revert all the
	 * changes made to the current graph after it has been loaded.
	 */
	public static final String REVERT_GRAPH = "REVERT_GRAPH";
   public static final int REVERT_GRAPH_CMD = 72;

	/**
	 * This command string instructs the application to toggle
	 * run mode.
	 */
	public static final String RUN_MODE = "RUN_MODE";
   public static final int RUN_MODE_CMD = 73;

	/**
	 * This command string instructs the application to save the graph
	 * to a file.
	 */
	public static final String SAVE_GRAPH = "SAVE_GRAPH";
   public static final int SAVE_GRAPH_CMD = 74;

	/**
	 * This command string instructs the application to save the graph
	 * to a file with a given name and in a given format.
	 */
	public static final String SAVE_GRAPH_AS = "SAVE_GRAPH_AS";
   public static final int SAVE_GRAPH_AS_CMD = 75;

	/**
	 * This command string instructs the application to save the graph
	 * to an image file with a given name and in a given format.
	 */
	public static final String SAVE_GRAPH_AS_IMAGE = "SAVE_GRAPH_AS_IMAGE";
   public static final int SAVE_GRAPH_AS_IMAGE_CMD = 76;

	/**
	 * This command string instructs the application to scroll the view
	 * to the left.
	 */
	public static final String SCROLL_LEFT = "SCROLL_LEFT";
   public static final int SCROLL_LEFT_CMD = 77;

	/**
	 * This command string instructs the application to scroll the view
	 * to the right.
	 */
	public static final String SCROLL_RIGHT = "SCROLL_RIGHT";
   public static final int SCROLL_RIGHT_CMD = 78;

	/**
	 * This command string instructs the application to scroll the view
	 * up.
	 */
	public static final String SCROLL_UP = "SCROLL_UP";
   public static final int SCROLL_UP_CMD = 79;

	/**
	 * This command string instructs the application to scroll the view
	 * down.
	 */
	public static final String SCROLL_DOWN = "SCROLL_DOWN";
   public static final int SCROLL_DOWN_CMD = 80;

	/**
	 * This command string instructs the application to select all
	 * objects in the graph.
	 */
	public static final String SELECT_ALL = "SELECT_ALL";
   public static final int SELECT_ALL_CMD = 81;

	/**
	 * This command string instructs the application to select all
	 * edges in the graph.
	 */
	public static final String SELECT_EDGES = "SELECT_EDGES";
   public static final int SELECT_EDGES_CMD = 82;

	/**
	 * This command string instructs the application to select all
	 * labels in the graph manager.
	 */
	public static final String SELECT_LABELS = "SELECT_LABELS";
   public static final int SELECT_LABELS_CMD = 83;

	/**
	 * This command string instructs the application to select all
	 * nodes in the graph manager.
	 */
	public static final String SELECT_NODES = "SELECT_NODES";
   public static final int SELECT_NODES_CMD = 84;

	/**
	 * This command string instructs the application to snap all
	 * selected nodes, bends, and labels to the grid.
	 */
	public static final String SNAP_TO_GRID = "SNAP_TO_GRID";
   public static final int SNAP_TO_GRID_CMD = 85;

	/**
	 * This command string instructs the application to undo the last
	 * executed action.
	 */
	public static final String UNDO = "UNDO";
   public static final int UNDO_CMD = 86;

	/**
	 * This command string instructs the application to select the
	 * magnification factor specified by the command suffix. If no
	 * suffix is specified the default magnification is 100%.
	 */
	public static final String ZOOM = "ZOOM";
   public static final int ZOOM_CMD = 87;

	/** 
	 * This constant instructs the application that the user
	 * has changed the zoom level.
	 */
	public static final String ZOOM_CHANGE = "ZOOM_CHANGE";
   public static final int ZOOM_CHANGE_CMD = 88;

	/**
	 * This command string instructs the application to set the user
	 * specified magnification factor.
	 */
	public static final String ZOOM_CUSTOM = "ZOOM_CUSTOM";
   public static final int ZOOM_CUSTOM_CMD = 89;

	/**
	 * This command string instructs the application to select the
	 * magnification factor that makes the graph fit in the current
	 * window.
	 */
	public static final String ZOOM_FIT = "ZOOM_FIT";
   public static final int ZOOM_FIT_CMD = 90;

   public static final String NODE_RESIZE = "NODE_RESIZE";
   public static final int NODE_RESIZE_CMD = 91;
   
   public static final String NODE_RESIZE_TALLER = "NODE_RESIZE_TALLER";
   public static final int NODE_RESIZE_TALLER_CMD = 92;
   
   public static final String NODE_RESIZE_SHORTER = "NODE_RESIZE_SHORTER";
   public static final int NODE_RESIZE_SHORTER_CMD = 93;
   
   public static final String NODE_RESIZE_WIDER = "NODE_RESIZE_WIDER";
   public static final int NODE_RESIZE_WIDER_CMD = 94;
   
   public static final String NODE_RESIZE_THINNER = "NODE_RESIZE_THINNER";
   public static final int NODE_RESIZE_THINNER_CMD = 95;
   
	// --- STATES ---------------------------------------------------------

	/**
	 * This command string instructs the application to switch to the
	 * next state.
	 */
	public static final String NEXT_STATE = "NEXT_STATE";

	// --- Default Node and Edge UIs --------------------------------------

	/**
	 * This constant defines the name for the default node UI type.
	 */
	public static final String DEFAULT_NODE_UI = "nodeUI.default";

	/**
	 * This constant defines the name for the default edge UI type.
	 */
	public static final String DEFAULT_EDGE_UI = "edgeUI.default";

	/**
	 * This constant defines the name for the default node label UI
	 * type.
	 */
	public static final String DEFAULT_NODE_LABEL_UI = "nodeLabelUI.default";

	/**
	 * This constant defines the name for the default edge label UI
	 * type.
	 */
	public static final String DEFAULT_EDGE_LABEL_UI = "edgeLabelUI.default";

	/**
	 * This constant defines the command string associated with a tab
	 * popup menu.
	 */
	public static final String TAB_POPUP = "TAB_POPUP";

	// --- Tailor options -------------------------------------------------

	/**
	 * This constant defines the command string associated with
	 * the layout properties dialog.
	 */
	public static final String LAYOUT_PROPERTIES = "LAYOUT_PROPERTIES";

	// --- Placeholder strings --------------------------------------------

	/**
	 * This placeholder string will be replaced by a number.
	 */
	public static final String X_PLACEHOLDER = "%X%";

	/**
	 * This placeholder string will be replaced by a number.
	 */
	public static final String Y_PLACEHOLDER = "%Y%";

	/**
	 * This placeholder string will be replaced by a filename.
	 */
	public static final String FILENAME_PLACEHOLDER = "%FILENAME%";

	/**
	 * This placeholder string will be replaced by the Graph
	 * Editor Toolkit for Java version number.
	 */
	public static final String GETJ_VERSION_PLACEHOLDER = "%GETJ_VERSION%";

	/**
	 * This placeholder string will be replaced by the Graph
	 * Editor Toolkit for Java major version number.
	 */
	public static final String GETJ_MAJOR_VERSION_PLACEHOLDER = "%GETJ_MAJOR_VERSION%";

	/**
	 * This placeholder string will be replaced by the 
	 * layout version number.
	 */
	public static final String LAYOUT_VERSION_PLACEHOLDER = "%LAYOUT_VERSION%";

	/**
	 * This placeholder string will be replaced by the 
	 * available layout styles.
	 */
	public static final String LAYOUT_STYLES_PLACEHOLDER = "%LAYOUT_STYLES%";

	/**
	 * This placeholder string will be replaced by the layout
	 * server type.
	 */
	public static final String LAYOUT_SERVER_TYPE_PLACEHOLDER = "%LAYOUT_SERVER_TYPE%";

	/**
	 * This placeholder string will be replaced by the Java VM version
	 * number.
	 */
	public static final String JAVA_VERSION_PLACEHOLDER = "%JAVA_VERSION%";

	/**
	 * This placeholder string will be replaced by the Graph
	 * Editor Toolkit for Java licensing track.
	 */
	public static final String GETJ_TRACK_PLACEHOLDER = "%GETJ_TRACK%";

	/**
	 * This placeholder string will be replaced by the Graph
	 * Editor Toolkit for Java licensing tier.
	 */
	public static final String GETJ_TIER_PLACEHOLDER = "%GETJ_TIER%";
   
}

