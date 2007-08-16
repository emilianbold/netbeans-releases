/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * <h4>About this tag</h4>
 * 
 *     <p>This tag renders two listboxes (one for available options, one
 *     for selected options) together with buttons to move the options
 *     between the lists, and optionally to order the selected options. 
 *     Use the <code>items</code> attribute to associate the component
 *     with an array, collection or map of
 *     <code>com.sun.rave.web.ui.model.Option</code> and the
 *     <code>selected</code> attribute to associate the component with a
 *     model object that represents the selected items. The model object
 *     must be an array of objects, whose values must be represented by
 *     the values on the <code>com.sun.rave.web.ui.model.Option</code>s. </p> 
 * 
 *     <h4>Component layout and Facet structure</h4>
 *     <p>The component can be laid out using either horizonal layout
 *     (the default) or vertical layout (if the <code>vertical</code> 
 *     attribute is set to true). 
 * 
 * In the diagrams below, facet child
 *     components are shown as <span style="color:lightblue">light
 * blue</span>. Non-facet areas are <span
 * style="color:violet">violet</span>.</p>  
 * 
 *     <h5>Horizontal layout</h5> 
 * 
 * <p> In horizontal layout, the component
 *     label (or header) may be shown either above the component
 *     (if the <code>labelOnTop</code>attribute is true) or next to the
 *     component as shown in the diagram. 
 * </p> 
 * 
 * <style type="text/css">
 * table.AddRmvBtnTbl .Btn1 {width:100%}
 * table.AddRmvBtnTbl .Btn1Hov {width:100%}
 * table.AddRmvBtnTbl .Btn1Dis {width:100%}
 * table.AddRmvBtnTbl .Btn2 {width:100%}
 * table.AddRmvBtnTbl .Btn2Hov {width:100%}
 * table.AddRmvBtnTbl .Btn2Dis {width:100%}
 * .AddRmvHrzWin, .AddRmvHrzBwn {margin-top:3px}
 * .AddRmvHrzDiv {float:left; display:inline-table; margin:3px}  
 * .AddRmvHrzLst {display:inline-table; margin:3px}  
 * select {margin:3px}  
 * .spacer {margin:3px}  
 * </style> 
 * 
 * <div id="addremove1:list_enclosing">
 * <span class="AddRmvHrzDiv">
 * <span style="background:lightblue">header</span>
 * </span>
 * 
 * <span class="AddRmvHrzDiv">&nbsp;</span>
 * 
 * <div class="AddRmvHrzDiv">
 * <span style="background:lightblue">availableLabel</span>
 * <br />
 * <select style="background:violet" size="12">
 * <option>List of available items</option> 
 * </select>
 * </div>
 * <div class="AddRmvHrzDiv">
 * <span>&nbsp;</span>
 * <br />
 * 
 * <div style="padding-left:10;padding-right:10">
 * <table class="AddRmvBtnTbl">
 * <tr>
 * <td align="center" width="125px">
 * 
 * <span style="background:lightblue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addButton&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
 * 
 * <div class="AddRmvHrzWin">
 * <span style="background:lightblue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addAllButton&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
 * </div>
 * 
 * <div class="AddRmvHrzBwn">
 * <span style="background:lightblue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;removeButton&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
 * </div>
 * 
 * <div class="AddRmvHrzBwn">
 * <span style="background:lightblue">&nbsp;&nbsp;&nbsp;removeAllButton&nbsp;&nbsp;</span>
 * </div>
 * 
 * <div class="AddRmvHrzBwn">
 * <span style="background:lightblue">&nbsp;&nbsp;&nbsp;&nbsp;moveUpButton&nbsp;&nbsp;&nbsp;&nbsp;</span>
 * </div>
 * 
 * <div class="AddRmvHrzBwn">
 * <span style="background:lightblue">&nbsp;&nbsp;moveDownButton&nbsp;&nbsp;</span>
 * </div>
 * 
 * </td>
 * </tr>
 * </table>
 * </div>
 * </div>
 * 
 * <div class="AddRmvHrzLst">
 * <span style="background:lightblue">selectedLabel</span>
 * <br />
 * <select style="background:violet" size="12">
 * <option>List of selected items</option> 
 * </select>
 * </div>
 * </div> 
 * <span>&nbsp;</span> 
 * <div> 
 *     <span style="background:lightblue">
 *       footer
 *     </span> 
 * 
 * </div> 
 * 
 * 
 * <h5>Vertical layout</h5>
 * 
 * 
 * <div id="addremove1:list_enclosing">
 * 
 * <div class="spacer">
 * <span style="background:lightblue">header</span>
 * </div>
 * 
 * <div class="spacer">
 * <span style="background:lightblue">availableLabel</span>
 * <br />
 * <select style="background:violet" size="12">
 * <option>List of available items</option> 
 * </select>
 * </div>
 * 
 * <div class="spacer"> 
 * <span style="background:lightblue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addButton&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
 * 
 * <span>&nbsp; 
 * 
 * <span style="background:lightblue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addAllButton&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
 * 
 * <span>&nbsp; 
 * 
 * <span style="background:lightblue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;removeButton&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
 * 
 * <span>&nbsp; 
 * 
 * <span style="background:lightblue">&nbsp;&nbsp;&nbsp;removeAllButton&nbsp;&nbsp;</span>
 * 
 * 
 * <div class="spacer">
 * <span style="background:lightblue">selectedLabel</span>
 * <br />
 * <select style="background:violet" size="12">
 * <option>List of selected items</option> 
 * </select>
 * </div>
 * 
 * 
 * <div class="spacer"> 
 * <span style="background:lightblue">&nbsp;&nbsp;&nbsp;&nbsp;moveUpButton&nbsp;&nbsp;&nbsp;&nbsp;</span>
 * 
 * <span>&nbsp;</span> 
 * 
 * <span style="background:lightblue">&nbsp;&nbsp;moveDownButton&nbsp;&nbsp;</span>
 * </div>
 * </div> 
 * <div> 
 *     <span style="background:lightblue">
 *       footer
 *     </span> 
 * 
 * </div> 
 * 
 * 
 * 
 * 
 *     <h4>Facets</h4>
 * 
 *     <ul>
 * 
 * <li> 
 * <code>addButton:</code>
 * Use this facet to replace the standard "Add" button. If
 * you use a facet to replace this component, the JSF ID of this
 * component should be the ID given to the AddRemove component with
 * <code>_addButton</code> appended at the end. If you wish to use the
 * JavaScript function associated with the default button, use the <code>
 * add()</code> function. See the section on JavaScript for details.
 * </li> 
 * 
 * <li> 
 * <code>removeButton:</code>
 * Use this facet to replace the standard "Remove"
 * button. If 
 * you use a facet to replace this component, the JSF ID of this
 * component should be the ID given to the AddRemove component with
 * <code>_removeButton</code> appended at the end. If you wish to use the
 * JavaScript function associated with the default button, use the <code>
 * remove()</code> function. See the section on JavaScript for details.
 * </li> 
 * 
 * <li> 
 * <code>addAllButton:</code>
 * Use this facet to replace the standard "Add All"
 * button. If 
 * you use a facet to replace this component, the JSF ID of this
 * component should be the ID given to the AddRemove component with
 * <code>_addAllButton</code> appended at the end. If you wish to use the
 * JavaScript function associated with the default button, use the <code>
 * addAll()</code> function. See the section on JavaScript for details.
 * </li> 
 * 
 * <li> 
 * <code>removeAllButton:</code>
 * Use this facet to replace the standard "Remove All"
 * button. If 
 * you use a facet to replace this component, the JSF ID of this
 * component should be the ID given to the AddRemove component with
 * <code>_removeAllButton</code> appended at the end. If you wish to use the
 * JavaScript function associated with the default button, use the <code>
 * removeAll()</code> function. See the section on JavaScript for details.
 * </li> 
 * 
 * 
 * <li> 
 * <code>moveUpButton:</code>
 * Use this facet to replace the standard "Move Up"
 * button. If 
 * you use a facet to replace this component, the JSF ID of this
 * component should be the ID given to the AddRemove component with
 * <code>_moveUpButton</code> appended at the end. If you wish to use the
 * JavaScript function associated with the default button, use the <code>
 * moveUp()</code> function. See the section on JavaScript for details.
 * </li> 
 * 
 * <li> 
 * <code>moveDownButton:</code>
 * Use this facet to replace the standard "Move Down"
 * button. If 
 * you use a facet to replace this component, the JSF ID of this
 * component should be the ID given to the AddRemove component with
 * <code>_moveDownButton</code> appended at the end. If you wish to use the
 * JavaScript function associated with the default button, use the <code>
 * moveDown()</code> function. See the section on JavaScript for details.
 * </li> 
 * 
 * <li> 
 * <code>header:</code>
 * Use this facet to create a header for the
 * component. The facet will replace the component label. 
 * </li> 
 * 
 * <li> 
 * <code>footer:</code>
 * Use this facet to create a footer for the
 * component.
 * 
 * </li> 
 *     </ul>
 * 
 *     <h4>Client-side JavaScript functions</h4>
 * 
 * <p>When the component is rendered, a JavaScript object corresponding
 * to the component is created. The name of the variable is AddRemove_
 * followed by the component's DOM id where the colons have been replaced
 * by underscores. For example, if the id of the component is
 * <code>listform:addremove</code> then the JavaScript variable name will
 * be <code>AddRemove_listform_addremove</code>. To manipulate the
 * component on the client side, you may invoke functions on the
 * JavaScript object. With reference to the id above, to add all elements
 * on the available list that the user has selected, invoke 
 * <code> AddRemove_listform_addremove.add()</code>. 
 * </p> 
 * 
 *     <ul>
 *     <li><code>add()</code>: the highlighted items on the available list
 *     are moved to the selected list. </li> 
 *     <li><code>addAll()</code>: all non-disabled  items on the available list
 *     are moved to the selected list. </li> 
 *     <li><code>remove()</code>: the highlighted items on the selected list
 *     are moved to the available list. </li> 
 *     <li><code>removeAll()</code>: all non-disabled  items on the selected list
 *     are moved to the available list. </li> 
 *     <li><code>moveUp()</code>: the highlighted items on the selected list
 *     are moved up one position. </li> 
 *     <li><code>moveDown()</code>: the highlighted items on the selected list
 *     are moved down one position. </li> 
 *     <li><code>updateButtons()</code>: this function ensures that the
 *     buttons are enabled/disabled based on the current selections in
 *     the lists. Invoke this function if you programmatically modify the
 *     selections on the available or selected list using client-side
 *     scripts. You do not need to invoke it when using any of the
 *     functions listed above, as they already invoke this function at
 *     the end. </li>   
 *     </ul>
 * 
 *     <h4>Configuring the AddRemove tag</h4>
 * 
 *     <h4>Examples</h4>
 * <p>The component gets the options from a managed bean called
 * AirportBean. The selections are stored in another managed bean
 * (AirportSelectionBean). The <code>selectAll</code> attribute indicates that the
 * <code>Add All</code> and <code>Remove All</code> buttons should be
 * shown. A label for the component as a whole (<code>label</code>) is shown
 * next to the component (<code>labelOnTop</code> is false). Labels have
 * been specified for the list of available items and for the list of
 * selected items. The <code>sorted</code> attribute indicates that the options on
 * the list will be shown in alphabetical order.</p>
 * <pre>
 *         &lt;ui:addRemove id="list"
 *                       items="#{AirportBean.airports}"
 *                       selected="#{AirportSelectionBean.airportSel}"
 *                       label="Select airports"
 *                       availableItemsLabel="Available Airports"
 *                       selectedItemsLabel="Selected Airports"
 *                       selectAll="true"
 * 		      sorted="true"
 *                       labelOnTop="false"/&gt;
 * </pre>
 * 
 * <p>As in the previous example, with the following exceptions: The
 * component is rendered using vertical layout (in this case, the main
 * component label is always rendered above the component). 
 * The <code>moveButtons</code> attribute indicates that the
 * <code>Move Up</code> and <code>Move Down</code> buttons should be
 * shown. </p> 
 * <pre> 
 *         &lt;ui:addRemove id="list"
 *                       items="#{AirportBean.airports}"
 *                       selected="#{AirportSelectionBean.airportSel}"
 *                       label="Select some names"
 *                       availableItemsLabel="Available Names"
 *                       selectedItemsLabel="Selected Names"
 *                       selectAll="true"
 *                       moveButtons="true"
 *                       vertical="true"/&gt;
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class AddRemoveBase extends com.sun.rave.web.ui.component.ListSelector {

    /**
     * <p>Construct a new <code>AddRemoveBase</code>.</p>
     */
    public AddRemoveBase() {
        super();
        setRendererType("com.sun.rave.web.ui.AddRemove");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.AddRemove";
    }

    // availableItemsLabel
    private String availableItemsLabel = null;

    /**
 * <p>The label for the available list</p>
     */
    public String getAvailableItemsLabel() {
        if (this.availableItemsLabel != null) {
            return this.availableItemsLabel;
        }
        ValueBinding _vb = getValueBinding("availableItemsLabel");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The label for the available list</p>
     * @see #getAvailableItemsLabel()
     */
    public void setAvailableItemsLabel(String availableItemsLabel) {
        this.availableItemsLabel = availableItemsLabel;
    }

    // duplicateSelections
    private boolean duplicateSelections = false;
    private boolean duplicateSelections_set = false;

    /**
 * <p>Set this attribute to true if the component should allow items
 *       from the available list to be added more than one to the
 *       selected list, that is, if the selected list should allow duplicate entries.</p>
     */
    public boolean isDuplicateSelections() {
        if (this.duplicateSelections_set) {
            return this.duplicateSelections;
        }
        ValueBinding _vb = getValueBinding("duplicateSelections");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Set this attribute to true if the component should allow items
 *       from the available list to be added more than one to the
 *       selected list, that is, if the selected list should allow duplicate entries.</p>
     * @see #isDuplicateSelections()
     */
    public void setDuplicateSelections(boolean duplicateSelections) {
        this.duplicateSelections = duplicateSelections;
        this.duplicateSelections_set = true;
    }

    // moveButtons
    private boolean moveButtons = false;
    private boolean moveButtons_set = false;

    /**
 * <p>Show the Move Up and Move Down buttons</p>
     */
    public boolean isMoveButtons() {
        if (this.moveButtons_set) {
            return this.moveButtons;
        }
        ValueBinding _vb = getValueBinding("moveButtons");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Show the Move Up and Move Down buttons</p>
     * @see #isMoveButtons()
     */
    public void setMoveButtons(boolean moveButtons) {
        this.moveButtons = moveButtons;
        this.moveButtons_set = true;
    }

    // selectAll
    private boolean selectAll = false;
    private boolean selectAll_set = false;

    /**
 * <p>Show the Add All and Remove All buttons</p>
     */
    public boolean isSelectAll() {
        if (this.selectAll_set) {
            return this.selectAll;
        }
        ValueBinding _vb = getValueBinding("selectAll");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Show the Add All and Remove All buttons</p>
     * @see #isSelectAll()
     */
    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
        this.selectAll_set = true;
    }

    // selectedItemsLabel
    private String selectedItemsLabel = null;

    /**
 * <p>The label for the selected list</p>
     */
    public String getSelectedItemsLabel() {
        if (this.selectedItemsLabel != null) {
            return this.selectedItemsLabel;
        }
        ValueBinding _vb = getValueBinding("selectedItemsLabel");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The label for the selected list</p>
     * @see #getSelectedItemsLabel()
     */
    public void setSelectedItemsLabel(String selectedItemsLabel) {
        this.selectedItemsLabel = selectedItemsLabel;
    }

    // sorted
    private boolean sorted = false;
    private boolean sorted_set = false;

    /**
 * <p>If true, the items on the available options list are shown in alphabetical
 *         order. The item on the selected options list are also shown in alphabetical order,
 *         unless the moveButtons attribute is true, in which case the user is expected to
 *         order the elements.</p>
     */
    public boolean isSorted() {
        if (this.sorted_set) {
            return this.sorted;
        }
        ValueBinding _vb = getValueBinding("sorted");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>If true, the items on the available options list are shown in alphabetical
 *         order. The item on the selected options list are also shown in alphabetical order,
 *         unless the moveButtons attribute is true, in which case the user is expected to
 *         order the elements.</p>
     * @see #isSorted()
     */
    public void setSorted(boolean sorted) {
        this.sorted = sorted;
        this.sorted_set = true;
    }

    // toolTip
    private String toolTip = null;

    /**
 * <p>Display the text as a tooltip for this component</p>
     */
    public String getToolTip() {
        if (this.toolTip != null) {
            return this.toolTip;
        }
        ValueBinding _vb = getValueBinding("toolTip");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Display the text as a tooltip for this component</p>
     * @see #getToolTip()
     */
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    // vertical
    private boolean vertical = false;
    private boolean vertical_set = false;

    /**
 * <p>Use vertical layout instead of the default horizontal one</p>
     */
    public boolean isVertical() {
        if (this.vertical_set) {
            return this.vertical;
        }
        ValueBinding _vb = getValueBinding("vertical");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Use vertical layout instead of the default horizontal one</p>
     * @see #isVertical()
     */
    public void setVertical(boolean vertical) {
        this.vertical = vertical;
        this.vertical_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.availableItemsLabel = (String) _values[1];
        this.duplicateSelections = ((Boolean) _values[2]).booleanValue();
        this.duplicateSelections_set = ((Boolean) _values[3]).booleanValue();
        this.moveButtons = ((Boolean) _values[4]).booleanValue();
        this.moveButtons_set = ((Boolean) _values[5]).booleanValue();
        this.selectAll = ((Boolean) _values[6]).booleanValue();
        this.selectAll_set = ((Boolean) _values[7]).booleanValue();
        this.selectedItemsLabel = (String) _values[8];
        this.sorted = ((Boolean) _values[9]).booleanValue();
        this.sorted_set = ((Boolean) _values[10]).booleanValue();
        this.toolTip = (String) _values[11];
        this.vertical = ((Boolean) _values[12]).booleanValue();
        this.vertical_set = ((Boolean) _values[13]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[14];
        _values[0] = super.saveState(_context);
        _values[1] = this.availableItemsLabel;
        _values[2] = this.duplicateSelections ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.duplicateSelections_set ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.moveButtons ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.moveButtons_set ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.selectAll ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.selectAll_set ? Boolean.TRUE : Boolean.FALSE;
        _values[8] = this.selectedItemsLabel;
        _values[9] = this.sorted ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.sorted_set ? Boolean.TRUE : Boolean.FALSE;
        _values[11] = this.toolTip;
        _values[12] = this.vertical ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.vertical_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
