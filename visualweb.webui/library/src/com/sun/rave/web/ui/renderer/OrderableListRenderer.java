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
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import com.sun.rave.web.ui.component.ListSelector;
import com.sun.rave.web.ui.component.OrderableList;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeJavascript;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;
/**
 *
 * @author avk
 */
public class OrderableListRenderer extends ListRendererBase {
    

    private final static boolean DEBUG = false;
 
    /**
     * <p>Render the orderable list component
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * end should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException {
        
        if(DEBUG) log("encodeEnd()"); //NOI18N

	if(component instanceof OrderableList) { 
            
            String jsID = component.getClientId(context);
            String jsObject = null;
            Object jsO = component.getAttributes().get(OrderableList.JSOBJECT);
            if(jsO == null) {
                jsObject = jsID.replace(':', '_');
                jsObject = "OrderableList_".concat(jsObject);
                component.getAttributes().put(OrderableList.JSOBJECT, jsObject);
            } else {
                jsObject = jsO.toString();
            }

	    renderListComponent((OrderableList)component, 
                                jsObject,
				context, 
                                getStyles(component, context)); 
	} 
	else { 
	    String message = "Component " + component.toString() + //NOI18N
		" has been associated with an OrderableListRenderer. " + //NOI18N
		" This renderer can only be used by components " + //NOI18N
		" that extend com.sun.rave.web.ui.component.Selector."; //NOI18N
	    throw new FacesException(message); 
	}
    }
    

    /**
     * Retrieve user input from the UI. 
     * @param context The FacesContext of this request
     * @param component The component associated with the renderer
     */
    public void decode(FacesContext context, UIComponent component) {
        
        if(DEBUG) log("decode()");
        String id = component.getClientId(context).concat(ListSelector.VALUE_ID);
        super.decode(context, component, id);
        
        if(DEBUG && ((OrderableList)component).getSubmittedValue() != null) { 
           log("Submitted value is not null");
        }
        return;
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
     void renderListComponent(OrderableList component, String jsObject, 
                              FacesContext context, String[] styles)
	throws IOException {

        if(DEBUG) log("renderListComponent()");

	if(component.isReadOnly()) { 
            UIComponent label = component.getHeaderComponent(); 
            super.renderReadOnlyList(component, label, context, styles[15]); 
	    return; 
	} 
        
        ResponseWriter writer = context.getResponseWriter();

	// We should check if this one is already printed on the
	// page... 
	writer.writeText("\n", null); 
	writer.startElement("script", component); // NOI18N
	writer.writeAttribute("type", "text/javascript", null); // NOI18N
	writer.writeURIAttribute("src", styles[13], null); // NOI18N
	writer.endElement("script"); // NOI18N
	writer.write("\n"); // NOI18N
        
        renderOpenEncloser(component, context, "div", styles[15]); //NOI18N

        // If the label goes on top, render it first... 

	UIComponent headerComponent = component.getHeaderComponent(); 
        if(headerComponent != null) { 
            
            if(!component.isLabelOnTop()) {
                writer.writeText("\n", null); //NOI18N
                writer.startElement("span", component);
                writer.writeAttribute("class", styles[10], null);
                writer.writeText("\n", null); //NOI18N
                
                
                RenderingUtilities.renderComponent(headerComponent, context);
                writer.writeText("\n", null); //NOI18N
                writer.endElement("span"); //NOI18N
                writer.writeText("\n", null); //NOI18N              
            }
            else {
                RenderingUtilities.renderComponent(headerComponent, context);
                writer.startElement("br", component); //NOI18N
                writer.endElement("br"); //NOI18N
            }
        }

	// First column: available items
	renderColumnTop(component, writer, styles[10]); 
        String id = component.getClientId(context).concat(ListSelector.LIST_ID);
	renderList(component, id, context, styles); 
	renderColumnBottom(writer); 


	// Second column: button row
	renderColumnTop(component, writer, styles[10]); 
	renderButtons(component, context, writer, styles); 
	renderColumnBottom(writer); 
        
        writer.startElement("div", component); //NOI18N
        writer.writeAttribute("class", styles[11], null); //NOI18N
        writer.endElement("div"); //NOI18N


	UIComponent footerComponent = 
	    component.getFacet(OrderableList.FOOTER_FACET); 
	if(footerComponent != null) {
            writer.startElement("div", component); //NOI18N
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(footerComponent, context);
            writer.writeText("\n", null); //NOI18N
            writer.endElement("div"); //NOI18N
            writer.writeText("\n", null); //NOI18N
        }
        
        String jsID = component.getClientId(context);

	// The value field
	// Call super renderValueField ?
	//
	/*
        RenderingUtilities.renderHiddenField
                (component, writer, jsID.concat(VALUES_ID), 
                 component.getValueAsString(context, component.getSeparator()));
	*/
	renderHiddenValue(component, context, writer, styles[15]);

        writer.writeText("\n", null); //NOI18N
	writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N

        // Initialize the JavaScript variable
        StringBuffer jsBuffer = new StringBuffer(200); 
	jsBuffer.append("var ");                //NOI18N
	jsBuffer.append(jsObject); 
	jsBuffer.append(" = new OrderableList(\'"); //NOI18N
	jsBuffer.append(jsID); 
	jsBuffer.append("\', \'");              //NOI18N
	jsBuffer.append(styles[14]); 
	jsBuffer.append("\');\n");              //NOI18N
	jsBuffer.append(jsObject); 
	jsBuffer.append(OrderableList.UPDATEBUTTONS_FUNCTION);

	writer.writeText("\n", null);        //NOI18N 
	writer.startElement("script", component); // NOI18N
	writer.writeAttribute("type", "text/javascript", null); // NOI18N
	writer.writeText(jsBuffer.toString(), null); 
	writer.writeText("\n", null);        //NOI18N
	writer.endElement("script");         // NOI18N
	writer.write("\n");                  // NOI18N
        
        writer.writeText("\n", null); //NOI18N
	writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N

    }

     private void renderColumnTop(OrderableList component, 
                                  ResponseWriter writer, 
                                  String style)
     throws IOException {
         
         // Render the available elements
         writer.startElement("div", component); //NOI18N
         writer.writeAttribute("class", style, null);  //NOI18N
         writer.writeText("\n", null); //NOI18N
     }

     private void renderColumnBottom(ResponseWriter writer)
     throws IOException {
         writer.writeText("\n", null); //NOI18N
         writer.endElement("div"); //NOI18N
         writer.writeText("\n", null); //NOI18N
     }
     
     
     private void renderButtons(OrderableList component, FacesContext context,
             ResponseWriter writer, String[] styles)
             throws IOException {
         
         
         writer.writeText("\n", null); //NOI18N
         writer.startElement("div", component); //NOI18N
         String style = "padding-left:10;padding-right:10"; //NOI18N
         writer.writeAttribute("style", style, null);  //NOI18N
         writer.writeText("\n", null); //NOI18N
         writer.startElement("table", component); //NOI18N
         writer.writeAttribute("class", styles[12], null); //NOI18N
         writer.writeText("\n", null); //NOI18N
         writer.startElement("tr", component); //NOI18N
         writer.writeText("\n", null); //NOI18N
         writer.startElement("td", component); //NOI18N
         writer.writeAttribute("align", "center", null);  //NOI18N
         writer.writeAttribute("width", "125", null);  //NOI18N
         writer.writeText("\n", null); //NOI18N
         RenderingUtilities.renderComponent(
                 component.getMoveUpButtonComponent(context), context);
         writer.writeText("\n", null); //NOI18N
         
         
         renderButton(component, component.getMoveDownButtonComponent(context),
                 styles[9], writer, context);
         
	 if(component.isMoveTopBottom()) { 
	     renderButton(component, component.getMoveTopButtonComponent(context), 
			  styles[8], writer, context);
	     renderButton(component, component.getMoveBottomButtonComponent(context),
			  styles[9], writer, context);
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
       
        private void renderButton(OrderableList list, 
                                  UIComponent comp, 
                                  String style, 
			          ResponseWriter writer, 
                                  FacesContext context) 
        throws IOException { 
        
	if(comp == null) return; 

	writer.startElement("div", list); //NOI18N
	writer.writeAttribute("class", style, null);  //NOI18N
	writer.writeText("\n", null); //NOI18N
	RenderingUtilities.renderComponent(comp, context);
	writer.writeText("\n", null); //NOI18N
	writer.endElement("div"); //NOI18N
	writer.writeText("\n", null); //NOI18N
    }
        
    /**
     * Overrides encodeChildren of Renderer to do nothing. This
     * renderer renders its own children, but not through this
     * method. 
     * @param context The FacesContext of the request
     * @param component The component associated with the
     * renderer. Must be a subclass of ListSelector.
     */
    public void encodeChildren(javax.faces.context.FacesContext context,
                           javax.faces.component.UIComponent component)
	throws java.io.IOException { 
	return;
    } 

    /**
     * <p>Renders a component in a table row.</p>
     * @param component The component
     * @param context The FacesContext of the request
     * @throws java.io.IOException if the renderer fails to write to
     * the response
     */
    private void addComponentSingleRow(OrderableList list, 
                                       UIComponent component, 
				       FacesContext context) 
	throws IOException {
	
        ResponseWriter writer = context.getResponseWriter();
	writer.startElement("tr", list);                    //NOI18N
	writer.writeText("\n", null);                       //NOI18N
	writer.startElement("td", list);                    //NOI18N
	RenderingUtilities.renderComponent(component, context); 
	writer.writeText("\n", null);                       //NOI18N
	// Perhaps this should depend on the dir?
	// writer.writeAttribute("align", "left", null); 
	writer.endElement("td");                            //NOI18N
	writer.writeText("\n", null);                       //NOI18N
	writer.endElement("tr");                            //NOI18N
	writer.writeText("\n", null);                       //NOI18N
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
    private String[] getStyles(UIComponent component, 
                               FacesContext context) { 
        
        if(DEBUG) log("getStyles()"); 
        
        Theme theme = ThemeUtilities.getTheme(context); 
        
        
        StringBuffer jsBuffer = new StringBuffer(200);
	jsBuffer = new StringBuffer(200);
        jsBuffer.append(OrderableList.JAVASCRIPT_PREFIX);
        jsBuffer.append(component.getAttributes().get(OrderableList.JSOBJECT));
        jsBuffer.append(OrderableList.ONCHANGE_FUNCTION);
        jsBuffer.append(OrderableList.RETURN);
        
        String[] styles = new String[16]; 

        styles[0] = jsBuffer.toString();
        styles[1] = theme.getStyleClass(ThemeStyles.LIST);
	styles[2] = theme.getStyleClass(ThemeStyles.LIST_DISABLED);
        styles[3] = theme.getStyleClass(ThemeStyles.LIST_OPTION);
        styles[4] = theme.getStyleClass(ThemeStyles.LIST_OPTION_DISABLED);
        styles[5] = theme.getStyleClass(ThemeStyles.LIST_OPTION_SELECTED);
        styles[6] = theme.getStyleClass(ThemeStyles.LIST_OPTION_GROUP);
        styles[7] = theme.getStyleClass(ThemeStyles.LIST_OPTION_SEPARATOR);
        styles[8] = theme.getStyleClass(ThemeStyles.ADDREMOVE_HORIZONTAL_BETWEEN);
        styles[9] = theme.getStyleClass(ThemeStyles.ADDREMOVE_HORIZONTAL_WITHIN);
	styles[10] = theme.getStyleClass(ThemeStyles.ADDREMOVE_HORIZONTAL_ALIGN);
        styles[11] = theme.getStyleClass(ThemeStyles.ADDREMOVE_HORIZONTAL_LAST);
        styles[12] = theme.getStyleClass(ThemeStyles.ADDREMOVE_BUTTON_TABLE);
        styles[13] = theme.getPathToJSFile(ThemeJavascript.ORDERABLE_LIST);
        styles[14] = theme.getMessage("OrderableList.moveMessage"); //NOI18N
        styles[15] = theme.getStyleClass(ThemeStyles.HIDDEN); 
        return styles; 
    } 
}

