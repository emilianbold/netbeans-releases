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
package com.sun.rave.web.ui.renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import com.sun.rave.web.ui.component.AddRemove;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.ListSelector;
import com.sun.rave.web.ui.component.StaticText;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeJavascript;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>Renderer for a {@link com.sun.rave.web.ui.component.AddRemove} component.</p>
 */

public class AddRemoveRenderer extends ListRendererBase {
    
    private final static boolean DEBUG             = false;
    private final static String SELECTED_ID        = "_selected"; //NOI18N
    private final static String AVAILABLE_ID       = "_available"; //NOI18N
    private final static String ITEMS_ID           = "_item_list"; //NOI18N
    
    /**
     * <p>Render the list.
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * end should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException {
        
        if(DEBUG) log("encodeEnd()");
        
        if(component instanceof AddRemove) {
            renderListComponent((AddRemove)component, context, getStyles(context));
        } else {
            String message = "Component " + component.toString() +     //NOI18N
                    " has been associated with a ListboxRenderer. " +  //NOI18N
                    " This renderer can only be used by components " + //NOI18N
                    " that extend com.sun.rave.web.ui.component.Selector."; //NOI18N
            throw new FacesException(message);
        }
    }
    
    /**
     * <p>This method determines whether the component should be
     * rendered as a standalone list, or laid out together with a
     * label that was defined as part of the component.</p>
     *
     * <p>A label will be rendered if either of the following is
     * true:</p>
     * <ul>
     * <li>The page author defined a label facet; or</li>
     * <li>The page author specified text in the label attribute.</li>
     * </ul>
     * <p>If there is a label, the component will be laid out using a
     * HTML table. If not, the component will be rendered as a
     * standalone HTML <tt>select</tt> element.</p>
     * @param component The component associated with the
     * renderer. Must be a subclass of ListSelector.
     * @param context The FacesContext of the request
     * @param styles A String array of styles used to render the
     * component. The first item of the array is the name of the
     * JavaScript method that handles change event. The second item is
     * the style used when the list is enabled. The third style is the
     * one to use when the list is disabled. The fourth item is the
     * style to use for an item that is enabled, the fifth to use for
     * an item that is disabled, and the sixth to use when the item is
     * selected.
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    void renderListComponent(AddRemove component, FacesContext context,
            String[] styles)
            throws IOException {
        
        if(DEBUG) log("renderListComponent()");
        
        if(component.isReadOnly()) {
            UIComponent label = component.getSelectedLabelComponent();
            super.renderReadOnlyList(component, label, context, styles[19]);
            return;
        }
        
        String id = component.getClientId(context);
        String jsObject = null;
        Object jsO = component.getAttributes().get(AddRemove.JSOBJECT);
        if(jsO == null) {
            jsObject = id.replace(':', '_');
            jsObject = "AddRemove_".concat(jsObject);
            component.getAttributes().put(AddRemove.JSOBJECT, jsObject);
        } else {
            jsObject = jsO.toString();
        }
        
        ResponseWriter writer = context.getResponseWriter();
        
        // We should check if this one is already printed on the
        // page...
        writer.writeText("\n", null);
        writer.startElement("script", component); // NOI18N
        writer.writeAttribute("type", "text/javascript", null); // NOI18N
        writer.writeURIAttribute("src", styles[16], null); // NOI18N
        writer.endElement("script"); // NOI18N
        writer.write("\n"); // NOI18N
        
        renderOpenEncloser(component, context, "div", styles[19]); //NOI18N
        
        if(component.isVertical()) {
            renderVerticalAddRemove(component, context, writer, styles);
        } else {
            renderHorizontalAddRemove(component, context, writer, styles);
        }
        
        UIComponent footerComponent =
                component.getFacet(AddRemove.FOOTER_FACET);
        if(footerComponent != null) {
            writer.startElement("div", component); //NOI18N
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(footerComponent, context);
            writer.writeText("\n", null); //NOI18N
            writer.endElement("div"); //NOI18N
            writer.writeText("\n", null); //NOI18N
        }
        
        RenderingUtilities.renderHiddenField(component, writer,
                id.concat(ITEMS_ID),
                component.getAllValues());
        writer.writeText("\n", null); //NOI18N
        
        // Value field
        renderHiddenValue(component, context, writer, styles[19]);
        
        writer.writeText("\n", null); //NOI18N
        writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        
        // Initialize the JavaScript variable
        StringBuffer jsBuffer = new StringBuffer(200);
        jsBuffer.append("var ");                //NOI18N
        jsBuffer.append(jsObject);
        jsBuffer.append(" = new AddRemove(\'"); //NOI18N
        jsBuffer.append(id);
        jsBuffer.append("\', \'");              //NOI18N
        jsBuffer.append(component.getSeparator());
        jsBuffer.append("\');\n");              //NOI18N
        jsBuffer.append(jsObject);
        jsBuffer.append(AddRemove.UPDATEBUTTONS_FUNCTION);
        
        if(component.isDuplicateSelections()) {
            jsBuffer.append("\n");              //NOI18N
            jsBuffer.append(jsObject);
            jsBuffer.append(AddRemove.MULTIPLEADDITIONS_FUNCTION);
        }
        
        writer.writeText("\n", null);        //NOI18N
        writer.startElement("script", component); // NOI18N
        writer.writeAttribute("type", "text/javascript", null); // NOI18N
        writer.writeText(jsBuffer.toString(), null);
        writer.writeText("\n", null);        //NOI18N
        writer.endElement("script");         // NOI18N
        writer.write("\n");                  // NOI18N
        
    }
    
    private void renderHorizontalAddRemove(AddRemove component,
            FacesContext context,
            ResponseWriter writer,
            String[] styles)
            throws IOException {
        
        
        // If the label goes on top, render it first...
        
        UIComponent headerComponent = component.getHeaderComponent();
        if(headerComponent != null) {
            
            if(!component.isLabelOnTop()) {
                writer.writeText("\n", null); //NOI18N
                writer.startElement("span", component);
                writer.writeAttribute("class", styles[17], null);
                writer.writeText("\n", null); //NOI18N
                
                
                RenderingUtilities.renderComponent(headerComponent, context);
                writer.writeText("\n", null); //NOI18N
                writer.endElement("span"); //NOI18N
                writer.writeText("\n", null); //NOI18N
            } else {
                RenderingUtilities.renderComponent(headerComponent, context);
                writer.startElement("br", component); //NOI18N
                writer.endElement("br"); //NOI18N
            }
        }
        
        // <RAVE>
        // Put the three columns into a table so that they can be displayed horizontally
        // in a table cell as in portlet. See bug 6299233
        
        writer.writeText("\n", null); //NOI18N
        writer.startElement("div", component); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.startElement("table", component); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeText("\n", null); //NOI18N
        
        // <RAVE>
        
        // First column: available items
        // <RAVE>
        renderTableCellStart(component, writer);
        // <RAVE>
        renderColumnTop(component, writer, styles[17]);
        RenderingUtilities.renderComponent
                (component.getAvailableLabelComponent(), context);
        renderColumnMiddle(component, writer);
        renderAvailableList(component, context, styles);
        renderColumnBottom(writer);
        // <RAVE>
        renderTableCellEnd(writer);
        // <RAVE>
        
        // Second column: button row
        // <RAVE>
        renderTableCellStart(component, writer);
        // <RAVE>
        renderColumnTop(component, writer, styles[17]);
        // We need to render a space holder with an &nbsp; for formatting
        writer.startElement("span", component); //NOI18N
        // Do not change this to "writeText", it will format the '&'!
        writer.write("&nbsp;"); //NOI18N
        writer.endElement("span"); //NOI18N
        renderColumnMiddle(component, writer);
        renderButtons(component, context, writer, styles);
        renderColumnBottom(writer);
        // <RAVE>
        renderTableCellEnd(writer);
        // <RAVE>
        
        // Third column: selected list row
        // <RAVE>
        renderTableCellStart(component, writer);
        // <RAVE>
        renderColumnTop(component, writer, styles[17]);
        RenderingUtilities.renderComponent
                (component.getSelectedLabelComponent(), context);
        renderColumnMiddle(component, writer);
        renderSelectedList(component, context, styles);
        renderColumnBottom(writer);
        // <RAVE>
        renderTableCellEnd(writer);
        // <RAVE>
        
        // <RAVE>
        // Close the table row
        writer.endElement("tr"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.endElement("table"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        // <RAVE>
        
        writer.startElement("div", component); //NOI18N
        writer.writeAttribute("class", styles[18], null); //NOI18N
        writer.endElement("div"); //NOI18N
        
    }
    
    // <RAVE>
    private void renderTableCellStart(AddRemove component, ResponseWriter writer) throws IOException{
        writer.startElement("td", component); //NOI18N
        writer.writeAttribute("align", "center", null);  //NOI18N
        writer.writeAttribute("valign", "top", null);  //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    // <RAVE>
    
    // <RAVE>
    private void renderTableCellEnd(ResponseWriter writer) throws IOException{
        writer.endElement("td"); //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    // <RAVE>
    
    private void renderVerticalAddRemove(AddRemove component,
            FacesContext context,
            ResponseWriter writer,
            String[] styles)
            throws IOException {
        
        // First column: available items
        
        writer.startElement("div", component); //NOI18N
        RenderingUtilities.renderComponent
                (component.getAvailableLabelComponent(), context);
        writer.endElement("div");
        
        writer.startElement("div", component); //NOI18N
        renderAvailableList(component, context, styles);
        writer.endElement("div");
        
        writer.startElement("div", component); //NOI18N
        renderAddButtonRow(component, context, writer, styles);
        writer.endElement("div");
        
        writer.startElement("div", component); //NOI18N
        RenderingUtilities.renderComponent
                (component.getSelectedLabelComponent(), context);
        writer.endElement("div");
        
        writer.startElement("div", component); //NOI18N
        renderSelectedList(component, context, styles);
        writer.endElement("div");
        
        if(component.isMoveButtons()) {
            writer.startElement("div", component); //NOI18N
            renderMoveButtonRow(component, context, writer, styles);
        }
        writer.endElement("div");
    }
    
    private void renderColumnTop(AddRemove addRemove, ResponseWriter writer, String style)
    throws IOException {
        
        // Render the available elements
        writer.startElement("div", addRemove); //NOI18N
        writer.writeAttribute("class", style, null);  //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    private void renderColumnMiddle(AddRemove addRemove, ResponseWriter writer)
    throws IOException {
        writer.writeText("\n", null); //NOI18N
        writer.startElement("br", addRemove); //NOI18N
        writer.endElement("br"); //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    
    private void renderColumnBottom(ResponseWriter writer)
    throws IOException {
        writer.writeText("\n", null); //NOI18N
        writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    private void renderButtons(AddRemove component, FacesContext context,
            ResponseWriter writer, String[] styles)
            throws IOException {
        
        
        writer.writeText("\n", null); //NOI18N
        writer.startElement("div", component); //NOI18N
        //writer.writeAttribute("style", styles[20], null);  //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.startElement("table", component); //NOI18N
        writer.writeAttribute("class", styles[10], null); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.startElement("td", component); //NOI18N
        writer.writeAttribute("align", "center", null);  //NOI18N
        writer.writeAttribute("width", "125px", null);  //NOI18N
        writer.writeText("\n", null); //NOI18N
        UIComponent addButton = component.getAddButtonComponent(context);
        if (addButton.getParent() == null) {
            addButton.setParent(component);
            RenderingUtilities.renderComponent(addButton, context);
            addButton.setParent(null);
        } else {
            RenderingUtilities.renderComponent(addButton, context);
        }
        writer.writeText("\n", null); //NOI18N
        
        if(component.isSelectAll()) {
            renderButton(component, component.getAddAllButtonComponent(),
                    styles[14], writer, context);
        }
        String buttonStyle = null;
        if(component.isSelectAll()) {
            buttonStyle = styles[15];
        } else {
            buttonStyle = styles[14];
        }
        renderButton(component, component.getRemoveButtonComponent(), buttonStyle,
                writer, context);
        if(component.isSelectAll()) {
            renderButton(component, component.getRemoveAllButtonComponent(),
                    styles[14], writer, context);
        }
        
        if(component.isMoveButtons()) {
            renderButton(component, component.getMoveUpButtonComponent(),
                    styles[15], writer, context);
            renderButton(component, component.getMoveDownButtonComponent(),
                    styles[14], writer, context);
        }
        
        writer.endElement("td"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.endElement("tr"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.endElement("table"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    private void renderAddButtonRow(AddRemove component,
            FacesContext context,
            ResponseWriter writer,
            String[] styles)
            throws IOException {
        
        writer.startElement("table", component); //NOI18N
        writer.writeAttribute("class", styles[10], null); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeAttribute("valign", "top", null); //NOI18N
        writer.writeText("\n", null); //NOI18N
        
        renderButtonVertical(component, component.getAddButtonComponent(context),
                styles[11], styles[20], writer, context);
        
        if(component.isSelectAll()) {
            renderButtonVertical(component, component.getAddAllButtonComponent(),
                    styles[12], styles[20], writer, context);
        }
        renderButtonVertical(component, component.getRemoveButtonComponent(),
                styles[13], styles[20], writer, context);
        
        if(component.isSelectAll()) {
            renderButtonVertical(component, component.getRemoveAllButtonComponent(),
                    styles[12], styles[20], writer, context);
        }
        
        writer.endElement("tr"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.endElement("table"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        
    }
    
    private void renderMoveButtonRow(AddRemove component,
            FacesContext context,
            ResponseWriter writer,
            String[] styles)
            throws IOException {
        
        
        writer.startElement("table", component); //NOI18N
        writer.writeAttribute("class", styles[10], null); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeAttribute("valign", "top", null); //NOI18N
        writer.writeText("\n", null); //NOI18N
        
        renderButtonVertical(component, component.getMoveUpButtonComponent(),
                styles[11], styles[20], writer, context);
        renderButtonVertical(component, component.getMoveDownButtonComponent(),
                styles[12], styles[20], writer, context);
        
        writer.endElement("tr"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.endElement("table"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        
    }
    
    private void renderButton(AddRemove addRemove,
            UIComponent comp,
            String style,
            ResponseWriter writer,
            FacesContext context)
            throws IOException {
        
        if(comp == null) return;
        
        writer.startElement("div", addRemove); //NOI18N
        writer.writeAttribute("class", style, null);  //NOI18N
        writer.writeText("\n", null); //NOI18N
        if (comp.getParent() == null) {
            comp.setParent(addRemove);
            RenderingUtilities.renderComponent(comp, context);
            comp.setParent(null);
        } else {
            RenderingUtilities.renderComponent(comp, context);
        }
        writer.writeText("\n", null); //NOI18N
        writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    private void renderButtonVertical(AddRemove addRemove,
            UIComponent comp,
            String style,
            String divStyle,
            ResponseWriter writer,
            FacesContext context)
            throws IOException {
        
        if(comp == null) return;
        
        writer.startElement("td", addRemove); //NOI18N
        writer.writeAttribute("class", divStyle, null);  //NOI18N
        writer.startElement("div", addRemove); //NOI18N
        writer.writeAttribute("class", style, null);  //NOI18N
        writer.writeText("\n", null); //NOI18N
        if (comp.getParent() == null) {
            comp.setParent(addRemove);
            RenderingUtilities.renderComponent(comp, context);
            comp.setParent(null);
        } else {
            RenderingUtilities.renderComponent(comp, context);
        }
        writer.writeText("\n", null); //NOI18N
        writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.endElement("td"); //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    /**
     * This is the base method for rendering a HTML select
     * element. This method is based on the functionality of the RI
     * version, so it invokes a method renderSelectItems which in term
     * invokes renderSelectItem. Currently, this renderer requires for
     * the options to be specified using the JSF SelectItem construct,
     * but this should be replaced with a Lockhart version, because
     * the JSF version lacks the ability to associate an id with the
     * list item. I'm not sure whether it should be possible to use
     * SelectItem as well yet.
     * @param component The UI Component associated with the
     * renderer.
     * @param context The FacesContext of the request
     * @param styles A String array of styles used to render the
     * component. The first item of the array is the name of the
     * JavaScript method that handles change event. The second item is
     * the style used when the list is enabled. The third style is the
     * one to use when the list is disabled. The fourth item is the
     * style to use for an item that is enabled, the fifth to use for
     * an item that is disabled, and the sixth to use when the item is
     * selected.
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    protected void renderAvailableList(AddRemove component, FacesContext context,
            String[] styles)
            throws IOException {
        
        String id = component.getClientId(context).concat(AVAILABLE_ID);
        
        // Set the style class
        String styleClass = styles[1];
        if(component.isDisabled()) styleClass = styles[2];
        
        ResponseWriter writer = context.getResponseWriter();
        
        // This stuff is from the RI...
        //Util.doAssert(writer != null);
        
        // This stuff is from the RI... Not sure what it is supposed to
        // accomplish?
        // String redisplay = "" + attributeMap.get("redisplay");
        
        // (redisplay == null || !redisplay.equals("true")) {
        //currentValue = "";
        //}
        
        
        writer.startElement("select", component);              //NOI18N
        writer.writeAttribute("class", styleClass, null);      //NOI18N
        writer.writeAttribute("id", id, null);                 //NOI18N
        writer.writeAttribute("name", id, null);           //NOI18N
        
        StringBuffer jsBuffer = new StringBuffer(200);
        jsBuffer.append(AddRemove.JAVASCRIPT_PREFIX);
        jsBuffer.append(component.getAttributes().get(AddRemove.JSOBJECT));
        jsBuffer.append(AddRemove.ADD_FUNCTION);
        jsBuffer.append(AddRemove.RETURN);
        writer.writeAttribute("ondblclick", jsBuffer.toString(), null);
        
        jsBuffer = new StringBuffer(200);
        jsBuffer.append(AddRemove.JAVASCRIPT_PREFIX);
        jsBuffer.append(component.getAttributes().get(AddRemove.JSOBJECT));
        jsBuffer.append(AddRemove.AVAILABLE_ONCHANGE_FUNCTION);
        jsBuffer.append(AddRemove.RETURN);
        writer.writeAttribute("onchange", jsBuffer.toString(), null);
        
        int size = component.getRows();
        writer.writeAttribute("size", String.valueOf(size), null); //NOI18N
        writer.writeAttribute("multiple", "multiple", null); //NOI18N
        
        if(component.isDisabled()) {
            writer.writeAttribute("disabled",  //NOI18N
                    "disabled",  //NOI18N
                    "disabled"); //NOI18N
        }
        
        String tooltip = component.getToolTip();
        if(tooltip != null) {
            writer.writeAttribute("title", tooltip, null); //NOI18N
        }
        
        if(DEBUG) log("Setting onchange event handler");
        //writer.writeAttribute("onchange", script, null);    //NOI18N
        
        int tabindex = component.getTabIndex();
        if(tabindex > 0 && tabindex < 32767) {
            writer.writeAttribute("tabindex",               //NOI18N
                    String.valueOf(tabindex),
                    "tabindex");              //NOI18N
        }
        
        RenderingUtilities.writeStringAttributes(component, writer,
                STRING_ATTRIBUTES);
        
        
        writer.writeText("\n", null); //NOI18N
        
        renderListOptions(component, component.getListItems(context, true),
                writer, styles);
        
        writer.endElement("select");  //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    /**
     * This is the base method for rendering a HTML select
     * element. This method is based on the functionality of the RI
     * version, so it invokes a method renderSelectItems which in term
     * invokes renderSelectItem. Currently, this renderer requires for
     * the options to be specified using the JSF SelectItem construct,
     * but this should be replaced with a Lockhart version, because
     * the JSF version lacks the ability to associate an id with the
     * list item. I'm not sure whether it should be possible to use
     * SelectItem as well yet.
     * @param component The UI Component associated with the
     * renderer.
     * @param context The FacesContext of the request
     * @param styles A String array of styles used to render the
     * component. The first item of the array is the name of the
     * JavaScript method that handles change event. The second item is
     * the style used when the list is enabled. The third style is the
     * one to use when the list is disabled. The fourth item is the
     * style to use for an item that is enabled, the fifth to use for
     * an item that is disabled, and the sixth to use when the item is
     * selected.
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    protected void renderSelectedList(AddRemove component,
            FacesContext context,
            String[] styles)
            throws IOException {
        
        String id = component.getClientId(context).concat(SELECTED_ID);
        
        // Set the style class
        String styleClass = styles[1];
        if(component.isDisabled()) {
            styleClass = styles[2];
        }
        
        ResponseWriter writer = context.getResponseWriter();
        
        // This stuff is from the RI...
        //Util.doAssert(writer != null);
        
        // This stuff is from the RI... Not sure what it is supposed to
        // accomplish?
        // String redisplay = "" + attributeMap.get("redisplay");
        
        // (redisplay == null || !redisplay.equals("true")) {
        //currentValue = "";
        //}
        
        
        writer.startElement("select", component);              //NOI18N
        writer.writeAttribute("class", styleClass, null);      //NOI18N
        writer.writeAttribute("id", id, null);                 //NOI18N
        writer.writeAttribute("size",                          //NOI18N
                String.valueOf(component.getRows()), null);
        writer.writeAttribute("multiple", "multiple", null); //NOI18N
        
        
        StringBuffer jsBuffer = new StringBuffer(200);
        jsBuffer.append(AddRemove.JAVASCRIPT_PREFIX);
        jsBuffer.append(component.getAttributes().get(AddRemove.JSOBJECT));
        jsBuffer.append(AddRemove.REMOVE_FUNCTION);
        jsBuffer.append(AddRemove.RETURN);
        writer.writeAttribute("ondblclick", jsBuffer.toString(), null); //NOI18N
        
        jsBuffer = new StringBuffer(200);
        jsBuffer.append(AddRemove.JAVASCRIPT_PREFIX);
        jsBuffer.append(component.getAttributes().get(AddRemove.JSOBJECT));
        jsBuffer.append(AddRemove.SELECTED_ONCHANGE_FUNCTION);
        jsBuffer.append(AddRemove.RETURN);
        writer.writeAttribute("onchange", jsBuffer.toString(), null); //NOI18N
        
        if(component.isDisabled()) {
            writer.writeAttribute("disabled",  //NOI18N
                    "disabled",  //NOI18N
                    "disabled"); //NOI18N
        }
        
        // TODO
        /*
        String tooltip = component.getToolTip();
        if(tooltip != null) {
            writer.writeAttribute("title", tooltip, null); //NOI18N
        }
         */
        
        if(DEBUG) log("Setting onchange event handler");
        /*
        String script =
            getJavaScript(component.getOnChange(),
                          styles[0],
                          id);
        writer.writeAttribute("onchange", script, null);    //NOI18N
         */
        
        int tabindex = component.getTabIndex();
        if(tabindex > 0 && tabindex < 32767) {
            writer.writeAttribute("tabindex",               //NOI18N
                    String.valueOf(tabindex),
                    "tabindex");              //NOI18N
        }
        
        RenderingUtilities.writeStringAttributes(component, writer,
                STRING_ATTRIBUTES);
        writer.writeText("\n", null); //NOI18N
        renderListOptions(component, component.getSelectedListItems(),
                writer, styles);
        writer.endElement("select");  //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    /**
     * Overrides encodeChildren of Renderer to do nothing. This
     * renderer renders its own children, but not through this
     * method.
     * @param context The FacesContext of the request
     * @param component The component associated with the
     * renderer. Must be a subclass of ListSelector.
     * @throws java.io.IOException if something goes wrong while writing the children
     */
    public void encodeChildren(javax.faces.context.FacesContext context,
            javax.faces.component.UIComponent component)
            throws java.io.IOException {
        return;
    }
    
    /**
     * <p>Render the appropriate element end, depending on the value of the
     * <code>type</code> property.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param monospace <code>UIComponent</code> if true, use the monospace
     * styles to render the list.
     *
     * @exception IOException if an input/output error occurs
     */
    private String[] getStyles(FacesContext context) {
        
        if(DEBUG) log("getStyles()");
        
        Theme theme = ThemeUtilities.getTheme(context);
        
        String[] styles = new String[21];
        styles[0] = "listbox_changed"; //NOI18N
        styles[1] = theme.getStyleClass(ThemeStyles.LIST);
        styles[2] = theme.getStyleClass(ThemeStyles.LIST_DISABLED);
        styles[3] = theme.getStyleClass(ThemeStyles.LIST_OPTION);
        styles[4] = theme.getStyleClass(ThemeStyles.LIST_OPTION_DISABLED);
        styles[5] = theme.getStyleClass(ThemeStyles.LIST_OPTION_SELECTED);
        styles[6] = theme.getStyleClass(ThemeStyles.LIST_OPTION_GROUP);
        styles[7] = theme.getStyleClass(ThemeStyles.LIST_OPTION_SEPARATOR);
        styles[8] = theme.getStyleClass(ThemeStyles.ADDREMOVE_LABEL);
        styles[9] = theme.getStyleClass(ThemeStyles.ADDREMOVE_LABEL2);
        styles[10] = theme.getStyleClass(ThemeStyles.ADDREMOVE_BUTTON_TABLE);
        styles[11] = theme.getStyleClass(ThemeStyles.ADDREMOVE_VERTICAL_FIRST);
        styles[12] = theme.getStyleClass(ThemeStyles.ADDREMOVE_VERTICAL_WITHIN);
        styles[13] = theme.getStyleClass(ThemeStyles.ADDREMOVE_VERTICAL_BETWEEN);
        styles[14] = theme.getStyleClass(ThemeStyles.ADDREMOVE_HORIZONTAL_WITHIN);
        styles[15] = theme.getStyleClass(ThemeStyles.ADDREMOVE_HORIZONTAL_BETWEEN);
        styles[16] = theme.getPathToJSFile(ThemeJavascript.ADD_REMOVE);
        styles[17] = theme.getStyleClass(ThemeStyles.ADDREMOVE_HORIZONTAL_ALIGN);
        styles[18] = theme.getStyleClass(ThemeStyles.ADDREMOVE_HORIZONTAL_LAST);
        styles[19] = theme.getStyleClass(ThemeStyles.HIDDEN);
        styles[20] = theme.getStyleClass(ThemeStyles.ADDREMOVE_VERTICAL_BUTTON);
        return styles;
    }
    
    /**
     * Decodes the value of the component
     * @param context The FacesContext of the request
     * @param component The component instance to be decoded
     */
    public void decode(FacesContext context, UIComponent component) {
        
        if(DEBUG) log("decode()");
        String id = component.getClientId(context).concat(ListSelector.VALUE_ID);
        super.decode(context, component, id);
    }
}

