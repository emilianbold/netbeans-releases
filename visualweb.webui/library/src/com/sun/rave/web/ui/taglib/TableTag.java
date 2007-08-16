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

public class TableTag extends UIComponentTag {

    /**
     * <p>Return the requested component type.</p>
     */
    public String getComponentType() {
        return "com.sun.rave.web.ui.Table";
    }

    /**
     * <p>Return the requested renderer type.</p>
     */
    public String getRendererType() {
        return "com.sun.rave.web.ui.Table";
    }

    /**
     * <p>Release any allocated tag handler attributes.</p>
     */
    public void release() {
        super.release();
        augmentTitle = null;
        cellPadding = null;
        cellSpacing = null;
        clearSortButton = null;
        deselectMultipleButton = null;
        deselectMultipleButtonOnClick = null;
        deselectSingleButton = null;
        deselectSingleButtonOnClick = null;
        extraActionBottomHtml = null;
        extraActionTopHtml = null;
        extraFooterHtml = null;
        extraPanelHtml = null;
        extraTitleHtml = null;
        filterId = null;
        filterPanelFocusId = null;
        filterText = null;
        footerText = null;
        hiddenSelectedRows = null;
        itemsText = null;
        lite = null;
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
        paginateButton = null;
        paginationControls = null;
        preferencesPanelFocusId = null;
        selectMultipleButton = null;
        selectMultipleButtonOnClick = null;
        sortPanelFocusId = null;
        sortPanelToggleButton = null;
        style = null;
        styleClass = null;
        summary = null;
        tabIndex = null;
        title = null;
        toolTip = null;
        visible = null;
        width = null;
    }

    /**
     * <p>Transfer tag attributes to component properties.</p>
     */
    protected void setProperties(UIComponent _component) {
        super.setProperties(_component);
        if (augmentTitle != null) {
            if (isValueReference(augmentTitle)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(augmentTitle);
                _component.setValueBinding("augmentTitle", _vb);
            } else {
                _component.getAttributes().put("augmentTitle", Boolean.valueOf(augmentTitle));
            }
        }
        if (cellPadding != null) {
            if (isValueReference(cellPadding)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(cellPadding);
                _component.setValueBinding("cellPadding", _vb);
            } else {
                _component.getAttributes().put("cellPadding", cellPadding);
            }
        }
        if (cellSpacing != null) {
            if (isValueReference(cellSpacing)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(cellSpacing);
                _component.setValueBinding("cellSpacing", _vb);
            } else {
                _component.getAttributes().put("cellSpacing", cellSpacing);
            }
        }
        if (clearSortButton != null) {
            if (isValueReference(clearSortButton)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(clearSortButton);
                _component.setValueBinding("clearSortButton", _vb);
            } else {
                _component.getAttributes().put("clearSortButton", Boolean.valueOf(clearSortButton));
            }
        }
        if (deselectMultipleButton != null) {
            if (isValueReference(deselectMultipleButton)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(deselectMultipleButton);
                _component.setValueBinding("deselectMultipleButton", _vb);
            } else {
                _component.getAttributes().put("deselectMultipleButton", Boolean.valueOf(deselectMultipleButton));
            }
        }
        if (deselectMultipleButtonOnClick != null) {
            if (isValueReference(deselectMultipleButtonOnClick)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(deselectMultipleButtonOnClick);
                _component.setValueBinding("deselectMultipleButtonOnClick", _vb);
            } else {
                _component.getAttributes().put("deselectMultipleButtonOnClick", deselectMultipleButtonOnClick);
            }
        }
        if (deselectSingleButton != null) {
            if (isValueReference(deselectSingleButton)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(deselectSingleButton);
                _component.setValueBinding("deselectSingleButton", _vb);
            } else {
                _component.getAttributes().put("deselectSingleButton", Boolean.valueOf(deselectSingleButton));
            }
        }
        if (deselectSingleButtonOnClick != null) {
            if (isValueReference(deselectSingleButtonOnClick)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(deselectSingleButtonOnClick);
                _component.setValueBinding("deselectSingleButtonOnClick", _vb);
            } else {
                _component.getAttributes().put("deselectSingleButtonOnClick", deselectSingleButtonOnClick);
            }
        }
        if (extraActionBottomHtml != null) {
            if (isValueReference(extraActionBottomHtml)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(extraActionBottomHtml);
                _component.setValueBinding("extraActionBottomHtml", _vb);
            } else {
                _component.getAttributes().put("extraActionBottomHtml", extraActionBottomHtml);
            }
        }
        if (extraActionTopHtml != null) {
            if (isValueReference(extraActionTopHtml)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(extraActionTopHtml);
                _component.setValueBinding("extraActionTopHtml", _vb);
            } else {
                _component.getAttributes().put("extraActionTopHtml", extraActionTopHtml);
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
        if (extraPanelHtml != null) {
            if (isValueReference(extraPanelHtml)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(extraPanelHtml);
                _component.setValueBinding("extraPanelHtml", _vb);
            } else {
                _component.getAttributes().put("extraPanelHtml", extraPanelHtml);
            }
        }
        if (extraTitleHtml != null) {
            if (isValueReference(extraTitleHtml)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(extraTitleHtml);
                _component.setValueBinding("extraTitleHtml", _vb);
            } else {
                _component.getAttributes().put("extraTitleHtml", extraTitleHtml);
            }
        }
        if (filterId != null) {
            if (isValueReference(filterId)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(filterId);
                _component.setValueBinding("filterId", _vb);
            } else {
                _component.getAttributes().put("filterId", filterId);
            }
        }
        if (filterPanelFocusId != null) {
            if (isValueReference(filterPanelFocusId)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(filterPanelFocusId);
                _component.setValueBinding("filterPanelFocusId", _vb);
            } else {
                _component.getAttributes().put("filterPanelFocusId", filterPanelFocusId);
            }
        }
        if (filterText != null) {
            if (isValueReference(filterText)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(filterText);
                _component.setValueBinding("filterText", _vb);
            } else {
                _component.getAttributes().put("filterText", filterText);
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
        if (hiddenSelectedRows != null) {
            if (isValueReference(hiddenSelectedRows)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(hiddenSelectedRows);
                _component.setValueBinding("hiddenSelectedRows", _vb);
            } else {
                _component.getAttributes().put("hiddenSelectedRows", Boolean.valueOf(hiddenSelectedRows));
            }
        }
        if (itemsText != null) {
            if (isValueReference(itemsText)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(itemsText);
                _component.setValueBinding("itemsText", _vb);
            } else {
                _component.getAttributes().put("itemsText", itemsText);
            }
        }
        if (lite != null) {
            if (isValueReference(lite)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(lite);
                _component.setValueBinding("lite", _vb);
            } else {
                _component.getAttributes().put("lite", Boolean.valueOf(lite));
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
        if (paginateButton != null) {
            if (isValueReference(paginateButton)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(paginateButton);
                _component.setValueBinding("paginateButton", _vb);
            } else {
                _component.getAttributes().put("paginateButton", Boolean.valueOf(paginateButton));
            }
        }
        if (paginationControls != null) {
            if (isValueReference(paginationControls)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(paginationControls);
                _component.setValueBinding("paginationControls", _vb);
            } else {
                _component.getAttributes().put("paginationControls", Boolean.valueOf(paginationControls));
            }
        }
        if (preferencesPanelFocusId != null) {
            if (isValueReference(preferencesPanelFocusId)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(preferencesPanelFocusId);
                _component.setValueBinding("preferencesPanelFocusId", _vb);
            } else {
                _component.getAttributes().put("preferencesPanelFocusId", preferencesPanelFocusId);
            }
        }
        if (selectMultipleButton != null) {
            if (isValueReference(selectMultipleButton)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(selectMultipleButton);
                _component.setValueBinding("selectMultipleButton", _vb);
            } else {
                _component.getAttributes().put("selectMultipleButton", Boolean.valueOf(selectMultipleButton));
            }
        }
        if (selectMultipleButtonOnClick != null) {
            if (isValueReference(selectMultipleButtonOnClick)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(selectMultipleButtonOnClick);
                _component.setValueBinding("selectMultipleButtonOnClick", _vb);
            } else {
                _component.getAttributes().put("selectMultipleButtonOnClick", selectMultipleButtonOnClick);
            }
        }
        if (sortPanelFocusId != null) {
            if (isValueReference(sortPanelFocusId)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(sortPanelFocusId);
                _component.setValueBinding("sortPanelFocusId", _vb);
            } else {
                _component.getAttributes().put("sortPanelFocusId", sortPanelFocusId);
            }
        }
        if (sortPanelToggleButton != null) {
            if (isValueReference(sortPanelToggleButton)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(sortPanelToggleButton);
                _component.setValueBinding("sortPanelToggleButton", _vb);
            } else {
                _component.getAttributes().put("sortPanelToggleButton", Boolean.valueOf(sortPanelToggleButton));
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
        if (summary != null) {
            if (isValueReference(summary)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(summary);
                _component.setValueBinding("summary", _vb);
            } else {
                _component.getAttributes().put("summary", summary);
            }
        }
        if (tabIndex != null) {
            if (isValueReference(tabIndex)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(tabIndex);
                _component.setValueBinding("tabIndex", _vb);
            } else {
                _component.getAttributes().put("tabIndex", Integer.valueOf(tabIndex));
            }
        }
        if (title != null) {
            if (isValueReference(title)) {
                ValueBinding _vb = getFacesContext().getApplication().createValueBinding(title);
                _component.setValueBinding("title", _vb);
            } else {
                _component.getAttributes().put("title", title);
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

    // augmentTitle
    private String augmentTitle = null;
    public void setAugmentTitle(String augmentTitle) {
        this.augmentTitle = augmentTitle;
    }

    // cellPadding
    private String cellPadding = null;
    public void setCellPadding(String cellPadding) {
        this.cellPadding = cellPadding;
    }

    // cellSpacing
    private String cellSpacing = null;
    public void setCellSpacing(String cellSpacing) {
        this.cellSpacing = cellSpacing;
    }

    // clearSortButton
    private String clearSortButton = null;
    public void setClearSortButton(String clearSortButton) {
        this.clearSortButton = clearSortButton;
    }

    // deselectMultipleButton
    private String deselectMultipleButton = null;
    public void setDeselectMultipleButton(String deselectMultipleButton) {
        this.deselectMultipleButton = deselectMultipleButton;
    }

    // deselectMultipleButtonOnClick
    private String deselectMultipleButtonOnClick = null;
    public void setDeselectMultipleButtonOnClick(String deselectMultipleButtonOnClick) {
        this.deselectMultipleButtonOnClick = deselectMultipleButtonOnClick;
    }

    // deselectSingleButton
    private String deselectSingleButton = null;
    public void setDeselectSingleButton(String deselectSingleButton) {
        this.deselectSingleButton = deselectSingleButton;
    }

    // deselectSingleButtonOnClick
    private String deselectSingleButtonOnClick = null;
    public void setDeselectSingleButtonOnClick(String deselectSingleButtonOnClick) {
        this.deselectSingleButtonOnClick = deselectSingleButtonOnClick;
    }

    // extraActionBottomHtml
    private String extraActionBottomHtml = null;
    public void setExtraActionBottomHtml(String extraActionBottomHtml) {
        this.extraActionBottomHtml = extraActionBottomHtml;
    }

    // extraActionTopHtml
    private String extraActionTopHtml = null;
    public void setExtraActionTopHtml(String extraActionTopHtml) {
        this.extraActionTopHtml = extraActionTopHtml;
    }

    // extraFooterHtml
    private String extraFooterHtml = null;
    public void setExtraFooterHtml(String extraFooterHtml) {
        this.extraFooterHtml = extraFooterHtml;
    }

    // extraPanelHtml
    private String extraPanelHtml = null;
    public void setExtraPanelHtml(String extraPanelHtml) {
        this.extraPanelHtml = extraPanelHtml;
    }

    // extraTitleHtml
    private String extraTitleHtml = null;
    public void setExtraTitleHtml(String extraTitleHtml) {
        this.extraTitleHtml = extraTitleHtml;
    }

    // filterId
    private String filterId = null;
    public void setFilterId(String filterId) {
        this.filterId = filterId;
    }

    // filterPanelFocusId
    private String filterPanelFocusId = null;
    public void setFilterPanelFocusId(String filterPanelFocusId) {
        this.filterPanelFocusId = filterPanelFocusId;
    }

    // filterText
    private String filterText = null;
    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    // footerText
    private String footerText = null;
    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    // hiddenSelectedRows
    private String hiddenSelectedRows = null;
    public void setHiddenSelectedRows(String hiddenSelectedRows) {
        this.hiddenSelectedRows = hiddenSelectedRows;
    }

    // itemsText
    private String itemsText = null;
    public void setItemsText(String itemsText) {
        this.itemsText = itemsText;
    }

    // lite
    private String lite = null;
    public void setLite(String lite) {
        this.lite = lite;
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

    // paginateButton
    private String paginateButton = null;
    public void setPaginateButton(String paginateButton) {
        this.paginateButton = paginateButton;
    }

    // paginationControls
    private String paginationControls = null;
    public void setPaginationControls(String paginationControls) {
        this.paginationControls = paginationControls;
    }

    // preferencesPanelFocusId
    private String preferencesPanelFocusId = null;
    public void setPreferencesPanelFocusId(String preferencesPanelFocusId) {
        this.preferencesPanelFocusId = preferencesPanelFocusId;
    }

    // selectMultipleButton
    private String selectMultipleButton = null;
    public void setSelectMultipleButton(String selectMultipleButton) {
        this.selectMultipleButton = selectMultipleButton;
    }

    // selectMultipleButtonOnClick
    private String selectMultipleButtonOnClick = null;
    public void setSelectMultipleButtonOnClick(String selectMultipleButtonOnClick) {
        this.selectMultipleButtonOnClick = selectMultipleButtonOnClick;
    }

    // sortPanelFocusId
    private String sortPanelFocusId = null;
    public void setSortPanelFocusId(String sortPanelFocusId) {
        this.sortPanelFocusId = sortPanelFocusId;
    }

    // sortPanelToggleButton
    private String sortPanelToggleButton = null;
    public void setSortPanelToggleButton(String sortPanelToggleButton) {
        this.sortPanelToggleButton = sortPanelToggleButton;
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

    // summary
    private String summary = null;
    public void setSummary(String summary) {
        this.summary = summary;
    }

    // tabIndex
    private String tabIndex = null;
    public void setTabIndex(String tabIndex) {
        this.tabIndex = tabIndex;
    }

    // title
    private String title = null;
    public void setTitle(String title) {
        this.title = title;
    }

    // toolTip
    private String toolTip = null;
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
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
