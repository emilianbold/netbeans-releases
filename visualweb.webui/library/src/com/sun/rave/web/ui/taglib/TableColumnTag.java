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

public class TableColumnTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.TableColumn";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.TableColumn";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        align = null;
        alignKey = null;
        descending = null;
        embeddedActions = null;
        emptyCell = null;
        extraFooterHtml = null;
        extraHeaderHtml = null;
        extraTableFooterHtml = null;
        footerText = null;
        headerText = null;
        height = null;
        noWrap = null;
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
        rowHeader = null;
        scope = null;
        selectId = null;
        severity = null;
        sort = null;
        sortIcon = null;
        sortImageURL = null;
        spacerColumn = null;
        style = null;
        styleClass = null;
        tableFooterText = null;
        toolTip = null;
        valign = null;
        visible = null;
        width = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (align != null) {
            if (isValueReference(align)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(align);
                _component.setValueBinding("align", _vb);
            } else {
                _component.getAttributes().put("align", align);
            }
        }
        if (alignKey != null) {
            if (isValueReference(alignKey)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(alignKey);
                _component.setValueBinding("alignKey", _vb);
            } else {
                _component.getAttributes().put("alignKey", alignKey);
            }
        }
        if (descending != null) {
            if (isValueReference(descending)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(descending);
                _component.setValueBinding("descending", _vb);
            } else {
                _component.getAttributes().put("descending", Boolean.valueOf(descending));
            }
        }
        if (embeddedActions != null) {
            if (isValueReference(embeddedActions)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(embeddedActions);
                _component.setValueBinding("embeddedActions", _vb);
            } else {
                _component.getAttributes().put("embeddedActions", Boolean.valueOf(embeddedActions));
            }
        }
        if (emptyCell != null) {
            if (isValueReference(emptyCell)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(emptyCell);
                _component.setValueBinding("emptyCell", _vb);
            } else {
                _component.getAttributes().put("emptyCell", Boolean.valueOf(emptyCell));
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
        if (extraTableFooterHtml != null) {
            if (isValueReference(extraTableFooterHtml)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(extraTableFooterHtml);
                _component.setValueBinding("extraTableFooterHtml", _vb);
            } else {
                _component.getAttributes().put("extraTableFooterHtml", extraTableFooterHtml);
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
        if (headerText != null) {
            if (isValueReference(headerText)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(headerText);
                _component.setValueBinding("headerText", _vb);
            } else {
                _component.getAttributes().put("headerText", headerText);
            }
        }
        if (height != null) {
            if (isValueReference(height)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(height);
                _component.setValueBinding("height", _vb);
            } else {
                _component.getAttributes().put("height", height);
            }
        }
        if (noWrap != null) {
            if (isValueReference(noWrap)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(noWrap);
                _component.setValueBinding("noWrap", _vb);
            } else {
                _component.getAttributes().put("noWrap", Boolean.valueOf(noWrap));
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
        if (rowHeader != null) {
            if (isValueReference(rowHeader)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(rowHeader);
                _component.setValueBinding("rowHeader", _vb);
            } else {
                _component.getAttributes().put("rowHeader", Boolean.valueOf(rowHeader));
            }
        }
        if (scope != null) {
            if (isValueReference(scope)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(scope);
                _component.setValueBinding("scope", _vb);
            } else {
                _component.getAttributes().put("scope", scope);
            }
        }
        if (selectId != null) {
            if (isValueReference(selectId)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(selectId);
                _component.setValueBinding("selectId", _vb);
            } else {
                _component.getAttributes().put("selectId", selectId);
            }
        }
        if (severity != null) {
            if (isValueReference(severity)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(severity);
                _component.setValueBinding("severity", _vb);
            } else {
                _component.getAttributes().put("severity", severity);
            }
        }
        if (sort != null) {
            if (isValueReference(sort)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(sort);
                _component.setValueBinding("sort", _vb);
            } else {
                _component.getAttributes().put("sort", sort);
            }
        }
        if (sortIcon != null) {
            if (isValueReference(sortIcon)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(sortIcon);
                _component.setValueBinding("sortIcon", _vb);
            } else {
                _component.getAttributes().put("sortIcon", sortIcon);
            }
        }
        if (sortImageURL != null) {
            if (isValueReference(sortImageURL)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(sortImageURL);
                _component.setValueBinding("sortImageURL", _vb);
            } else {
                _component.getAttributes().put("sortImageURL", sortImageURL);
            }
        }
        if (spacerColumn != null) {
            if (isValueReference(spacerColumn)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(spacerColumn);
                _component.setValueBinding("spacerColumn", _vb);
            } else {
                _component.getAttributes().put("spacerColumn", Boolean.valueOf(spacerColumn));
            }
        }
        if (style != null) {
            if (isValueReference(style)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(style);
                _component.setValueBinding("style", _vb);
            } else {
                _component.getAttributes().put("style", style);
            }
        }
        if (styleClass != null) {
            if (isValueReference(styleClass)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(styleClass);
                _component.setValueBinding("styleClass", _vb);
            } else {
                _component.getAttributes().put("styleClass", styleClass);
            }
        }
        if (tableFooterText != null) {
            if (isValueReference(tableFooterText)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(tableFooterText);
                _component.setValueBinding("tableFooterText", _vb);
            } else {
                _component.getAttributes().put("tableFooterText", tableFooterText);
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
        if (width != null) {
            if (isValueReference(width)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(width);
                _component.setValueBinding("width", _vb);
            } else {
                _component.getAttributes().put("width", width);
            }
        }
    }

    // align
    private String align = null;
    public void setAlign(String align) {
        this.align = align;
    }

    // alignKey
    private String alignKey = null;
    public void setAlignKey(String alignKey) {
        this.alignKey = alignKey;
    }

    // descending
    private String descending = null;
    public void setDescending(String descending) {
        this.descending = descending;
    }

    // embeddedActions
    private String embeddedActions = null;
    public void setEmbeddedActions(String embeddedActions) {
        this.embeddedActions = embeddedActions;
    }

    // emptyCell
    private String emptyCell = null;
    public void setEmptyCell(String emptyCell) {
        this.emptyCell = emptyCell;
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

    // extraTableFooterHtml
    private String extraTableFooterHtml = null;
    public void setExtraTableFooterHtml(String extraTableFooterHtml) {
        this.extraTableFooterHtml = extraTableFooterHtml;
    }

    // footerText
    private String footerText = null;
    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    // headerText
    private String headerText = null;
    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    // height
    private String height = null;
    public void setHeight(String height) {
        this.height = height;
    }

    // noWrap
    private String noWrap = null;
    public void setNoWrap(String noWrap) {
        this.noWrap = noWrap;
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

    // rowHeader
    private String rowHeader = null;
    public void setRowHeader(String rowHeader) {
        this.rowHeader = rowHeader;
    }

    // scope
    private String scope = null;
    public void setScope(String scope) {
        this.scope = scope;
    }

    // selectId
    private String selectId = null;
    public void setSelectId(String selectId) {
        this.selectId = selectId;
    }

    // severity
    private String severity = null;
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    // sort
    private String sort = null;
    public void setSort(String sort) {
        this.sort = sort;
    }

    // sortIcon
    private String sortIcon = null;
    public void setSortIcon(String sortIcon) {
        this.sortIcon = sortIcon;
    }

    // sortImageURL
    private String sortImageURL = null;
    public void setSortImageURL(String sortImageURL) {
        this.sortImageURL = sortImageURL;
    }

    // spacerColumn
    private String spacerColumn = null;
    public void setSpacerColumn(String spacerColumn) {
        this.spacerColumn = spacerColumn;
    }

    // style
    private String style = null;
    public void setStyle(String style) {
        this.style = style;
    }

    // styleClass
    private String styleClass = null;
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    // tableFooterText
    private String tableFooterText = null;
    public void setTableFooterText(String tableFooterText) {
        this.tableFooterText = tableFooterText;
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

    // width
    private String width = null;
    public void setWidth(String width) {
        this.width = width;
    }

    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] = { ActionEvent.class };
    private static Class validatorArgs[] = { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] = { ValueChangeEvent.class };

}
