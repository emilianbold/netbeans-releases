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
package com.sun.rave.web.ui.taglib;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.webapp.UIComponentTag;
import com.sun.rave.web.ui.el.ConstantMethodBinding;

/**
 * <p>Auto-generated component tag class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public class TableRowGroupTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.TableRowGroup";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.TableRowGroup";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        aboveColumnFooter = null;
        aboveColumnHeader = null;
        align = null;
        collapsed = null;
        emptyDataMsg = null;
        extraFooterHtml = null;
        extraHeaderHtml = null;
        first = null;
        footerText = null;
        groupToggleButton = null;
        headerText = null;
        multipleColumnFooters = null;
        multipleTableColumnFooters = null;
        onClick = null;
        onDblClick = null;
        onKeyDown = null;
        onKeyPress = null;
        onKeyUp = null;
        onMouseDown = null;
        onMouseMove = null;
        onMouseOut = null;
        onMouseOver = null;
        onMouseUp = null;
        rows = null;
        selectMultipleToggleButton = null;
        selected = null;
        sourceData = null;
        sourceVar = null;
        styleClasses = null;
        tableDataFilter = null;
        tableDataSorter = null;
        toolTip = null;
        valign = null;
        visible = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (aboveColumnFooter != null) {
            if (isValueReference(aboveColumnFooter)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(aboveColumnFooter);
                _component.setValueBinding("aboveColumnFooter", _vb);
            } else {
                _component.getAttributes().put("aboveColumnFooter", Boolean.valueOf(aboveColumnFooter));
            }
        }
        if (aboveColumnHeader != null) {
            if (isValueReference(aboveColumnHeader)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(aboveColumnHeader);
                _component.setValueBinding("aboveColumnHeader", _vb);
            } else {
                _component.getAttributes().put("aboveColumnHeader", Boolean.valueOf(aboveColumnHeader));
            }
        }
        if (align != null) {
            if (isValueReference(align)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(align);
                _component.setValueBinding("align", _vb);
            } else {
                _component.getAttributes().put("align", align);
            }
        }
        if (collapsed != null) {
            if (isValueReference(collapsed)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(collapsed);
                _component.setValueBinding("collapsed", _vb);
            } else {
                _component.getAttributes().put("collapsed", Boolean.valueOf(collapsed));
            }
        }
        if (emptyDataMsg != null) {
            if (isValueReference(emptyDataMsg)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(emptyDataMsg);
                _component.setValueBinding("emptyDataMsg", _vb);
            } else {
                _component.getAttributes().put("emptyDataMsg", emptyDataMsg);
            }
        }
        if (extraFooterHtml != null) {
            if (isValueReference(extraFooterHtml)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(extraFooterHtml);
                _component.setValueBinding("extraFooterHtml", _vb);
            } else {
                _component.getAttributes().put("extraFooterHtml", extraFooterHtml);
            }
        }
        if (extraHeaderHtml != null) {
            if (isValueReference(extraHeaderHtml)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(extraHeaderHtml);
                _component.setValueBinding("extraHeaderHtml", _vb);
            } else {
                _component.getAttributes().put("extraHeaderHtml", extraHeaderHtml);
            }
        }
        if (first != null) {
            if (isValueReference(first)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(first);
                _component.setValueBinding("first", _vb);
            } else {
                _component.getAttributes().put("first", Integer.valueOf(first));
            }
        }
        if (footerText != null) {
            if (isValueReference(footerText)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(footerText);
                _component.setValueBinding("footerText", _vb);
            } else {
                _component.getAttributes().put("footerText", footerText);
            }
        }
        if (groupToggleButton != null) {
            if (isValueReference(groupToggleButton)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(groupToggleButton);
                _component.setValueBinding("groupToggleButton", _vb);
            } else {
                _component.getAttributes().put("groupToggleButton", Boolean.valueOf(groupToggleButton));
            }
        }
        if (headerText != null) {
            if (isValueReference(headerText)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(headerText);
                _component.setValueBinding("headerText", _vb);
            } else {
                _component.getAttributes().put("headerText", headerText);
            }
        }
        if (multipleColumnFooters != null) {
            if (isValueReference(multipleColumnFooters)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(multipleColumnFooters);
                _component.setValueBinding("multipleColumnFooters", _vb);
            } else {
                _component.getAttributes().put("multipleColumnFooters", Boolean.valueOf(multipleColumnFooters));
            }
        }
        if (multipleTableColumnFooters != null) {
            if (isValueReference(multipleTableColumnFooters)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(multipleTableColumnFooters);
                _component.setValueBinding("multipleTableColumnFooters", _vb);
            } else {
                _component.getAttributes().put("multipleTableColumnFooters", Boolean.valueOf(multipleTableColumnFooters));
            }
        }
        if (onClick != null) {
            if (isValueReference(onClick)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onClick);
                _component.setValueBinding("onClick", _vb);
            } else {
                _component.getAttributes().put("onClick", onClick);
            }
        }
        if (onDblClick != null) {
            if (isValueReference(onDblClick)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onDblClick);
                _component.setValueBinding("onDblClick", _vb);
            } else {
                _component.getAttributes().put("onDblClick", onDblClick);
            }
        }
        if (onKeyDown != null) {
            if (isValueReference(onKeyDown)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onKeyDown);
                _component.setValueBinding("onKeyDown", _vb);
            } else {
                _component.getAttributes().put("onKeyDown", onKeyDown);
            }
        }
        if (onKeyPress != null) {
            if (isValueReference(onKeyPress)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onKeyPress);
                _component.setValueBinding("onKeyPress", _vb);
            } else {
                _component.getAttributes().put("onKeyPress", onKeyPress);
            }
        }
        if (onKeyUp != null) {
            if (isValueReference(onKeyUp)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onKeyUp);
                _component.setValueBinding("onKeyUp", _vb);
            } else {
                _component.getAttributes().put("onKeyUp", onKeyUp);
            }
        }
        if (onMouseDown != null) {
            if (isValueReference(onMouseDown)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onMouseDown);
                _component.setValueBinding("onMouseDown", _vb);
            } else {
                _component.getAttributes().put("onMouseDown", onMouseDown);
            }
        }
        if (onMouseMove != null) {
            if (isValueReference(onMouseMove)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onMouseMove);
                _component.setValueBinding("onMouseMove", _vb);
            } else {
                _component.getAttributes().put("onMouseMove", onMouseMove);
            }
        }
        if (onMouseOut != null) {
            if (isValueReference(onMouseOut)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onMouseOut);
                _component.setValueBinding("onMouseOut", _vb);
            } else {
                _component.getAttributes().put("onMouseOut", onMouseOut);
            }
        }
        if (onMouseOver != null) {
            if (isValueReference(onMouseOver)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onMouseOver);
                _component.setValueBinding("onMouseOver", _vb);
            } else {
                _component.getAttributes().put("onMouseOver", onMouseOver);
            }
        }
        if (onMouseUp != null) {
            if (isValueReference(onMouseUp)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(onMouseUp);
                _component.setValueBinding("onMouseUp", _vb);
            } else {
                _component.getAttributes().put("onMouseUp", onMouseUp);
            }
        }
        if (rows != null) {
            if (isValueReference(rows)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(rows);
                _component.setValueBinding("rows", _vb);
            } else {
                _component.getAttributes().put("rows", Integer.valueOf(rows));
            }
        }
        if (selectMultipleToggleButton != null) {
            if (isValueReference(selectMultipleToggleButton)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(selectMultipleToggleButton);
                _component.setValueBinding("selectMultipleToggleButton", _vb);
            } else {
                _component.getAttributes().put("selectMultipleToggleButton", Boolean.valueOf(selectMultipleToggleButton));
            }
        }
        if (selected != null) {
            if (isValueReference(selected)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(selected);
                _component.setValueBinding("selected", _vb);
            } else {
                _component.getAttributes().put("selected", Boolean.valueOf(selected));
            }
        }
        if (sourceData != null) {
            if (isValueReference(sourceData)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(sourceData);
                _component.setValueBinding("sourceData", _vb);
            } else {
                _component.getAttributes().put("sourceData", sourceData);
            }
        }
        if (sourceVar != null) {
            if (isValueReference(sourceVar)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(sourceVar);
                _component.setValueBinding("sourceVar", _vb);
            } else {
                _component.getAttributes().put("sourceVar", sourceVar);
            }
        }
        if (styleClasses != null) {
            if (isValueReference(styleClasses)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(styleClasses);
                _component.setValueBinding("styleClasses", _vb);
            } else {
                _component.getAttributes().put("styleClasses", styleClasses);
            }
        }
        if (tableDataFilter != null) {
            if (isValueReference(tableDataFilter)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(tableDataFilter);
                _component.setValueBinding("tableDataFilter", _vb);
            } else {
                _component.getAttributes().put("tableDataFilter", tableDataFilter);
            }
        }
        if (tableDataSorter != null) {
            if (isValueReference(tableDataSorter)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(tableDataSorter);
                _component.setValueBinding("tableDataSorter", _vb);
            } else {
                _component.getAttributes().put("tableDataSorter", tableDataSorter);
            }
        }
        if (toolTip != null) {
            if (isValueReference(toolTip)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(toolTip);
                _component.setValueBinding("toolTip", _vb);
            } else {
                _component.getAttributes().put("toolTip", toolTip);
            }
        }
        if (valign != null) {
            if (isValueReference(valign)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(valign);
                _component.setValueBinding("valign", _vb);
            } else {
                _component.getAttributes().put("valign", valign);
            }
        }
        if (visible != null) {
            if (isValueReference(visible)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(visible);
                _component.setValueBinding("visible", _vb);
            } else {
                _component.getAttributes().put("visible", Boolean.valueOf(visible));
            }
        }
    }

    // aboveColumnFooter
    private String aboveColumnFooter = null;
    public void setAboveColumnFooter(String aboveColumnFooter) {
        this.aboveColumnFooter = aboveColumnFooter;
    }

    // aboveColumnHeader
    private String aboveColumnHeader = null;
    public void setAboveColumnHeader(String aboveColumnHeader) {
        this.aboveColumnHeader = aboveColumnHeader;
    }

    // align
    private String align = null;
    public void setAlign(String align) {
        this.align = align;
    }

    // collapsed
    private String collapsed = null;
    public void setCollapsed(String collapsed) {
        this.collapsed = collapsed;
    }

    // emptyDataMsg
    private String emptyDataMsg = null;
    public void setEmptyDataMsg(String emptyDataMsg) {
        this.emptyDataMsg = emptyDataMsg;
    }

    // extraFooterHtml
    private String extraFooterHtml = null;
    public void setExtraFooterHtml(String extraFooterHtml) {
        this.extraFooterHtml = extraFooterHtml;
    }

    // extraHeaderHtml
    private String extraHeaderHtml = null;
    public void setExtraHeaderHtml(String extraHeaderHtml) {
        this.extraHeaderHtml = extraHeaderHtml;
    }

    // first
    private String first = null;
    public void setFirst(String first) {
        this.first = first;
    }

    // footerText
    private String footerText = null;
    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    // groupToggleButton
    private String groupToggleButton = null;
    public void setGroupToggleButton(String groupToggleButton) {
        this.groupToggleButton = groupToggleButton;
    }

    // headerText
    private String headerText = null;
    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    // multipleColumnFooters
    private String multipleColumnFooters = null;
    public void setMultipleColumnFooters(String multipleColumnFooters) {
        this.multipleColumnFooters = multipleColumnFooters;
    }

    // multipleTableColumnFooters
    private String multipleTableColumnFooters = null;
    public void setMultipleTableColumnFooters(String multipleTableColumnFooters) {
        this.multipleTableColumnFooters = multipleTableColumnFooters;
    }

    // onClick
    private String onClick = null;
    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    // onDblClick
    private String onDblClick = null;
    public void setOnDblClick(String onDblClick) {
        this.onDblClick = onDblClick;
    }

    // onKeyDown
    private String onKeyDown = null;
    public void setOnKeyDown(String onKeyDown) {
        this.onKeyDown = onKeyDown;
    }

    // onKeyPress
    private String onKeyPress = null;
    public void setOnKeyPress(String onKeyPress) {
        this.onKeyPress = onKeyPress;
    }

    // onKeyUp
    private String onKeyUp = null;
    public void setOnKeyUp(String onKeyUp) {
        this.onKeyUp = onKeyUp;
    }

    // onMouseDown
    private String onMouseDown = null;
    public void setOnMouseDown(String onMouseDown) {
        this.onMouseDown = onMouseDown;
    }

    // onMouseMove
    private String onMouseMove = null;
    public void setOnMouseMove(String onMouseMove) {
        this.onMouseMove = onMouseMove;
    }

    // onMouseOut
    private String onMouseOut = null;
    public void setOnMouseOut(String onMouseOut) {
        this.onMouseOut = onMouseOut;
    }

    // onMouseOver
    private String onMouseOver = null;
    public void setOnMouseOver(String onMouseOver) {
        this.onMouseOver = onMouseOver;
    }

    // onMouseUp
    private String onMouseUp = null;
    public void setOnMouseUp(String onMouseUp) {
        this.onMouseUp = onMouseUp;
    }

    // rows
    private String rows = null;
    public void setRows(String rows) {
        this.rows = rows;
    }

    // selectMultipleToggleButton
    private String selectMultipleToggleButton = null;
    public void setSelectMultipleToggleButton(String selectMultipleToggleButton) {
        this.selectMultipleToggleButton = selectMultipleToggleButton;
    }

    // selected
    private String selected = null;
    public void setSelected(String selected) {
        this.selected = selected;
    }

    // sourceData
    private String sourceData = null;
    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }

    // sourceVar
    private String sourceVar = null;
    public void setSourceVar(String sourceVar) {
        this.sourceVar = sourceVar;
    }

    // styleClasses
    private String styleClasses = null;
    public void setStyleClasses(String styleClasses) {
        this.styleClasses = styleClasses;
    }

    // tableDataFilter
    private String tableDataFilter = null;
    public void setTableDataFilter(String tableDataFilter) {
        this.tableDataFilter = tableDataFilter;
    }

    // tableDataSorter
    private String tableDataSorter = null;
    public void setTableDataSorter(String tableDataSorter) {
        this.tableDataSorter = tableDataSorter;
    }

    // toolTip
    private String toolTip = null;
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    // valign
    private String valign = null;
    public void setValign(String valign) {
        this.valign = valign;
    }

    // visible
    private String visible = null;
    public void setVisible(String visible) {
        this.visible = visible;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}
