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

import com.sun.rave.web.ui.util.MessageUtil;
import java.beans.Beans;
import java.io.IOException;
import java.lang.NullPointerException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIMessages;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.rave.web.ui.component.MessageGroup;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.FacesMessageUtils;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>This class is responsible for rendering the Message component.</p>
 */
public class MessageGroupRenderer extends AbstractRenderer {

    /**
     * Renders the Message component.
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     * end should be rendered
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component,
        ResponseWriter writer) throws IOException {
        // End the appropriate element
	MessageGroup msgGrp = (MessageGroup) component;
	Iterator msgIt = null;
	String forComponentId = null;
        
        if (Beans.isDesignTime() && (msgGrp.isShowDetail() || msgGrp.isShowSummary())) {
            StringBuffer resourceNameBuffer = new StringBuffer();
            resourceNameBuffer.append("MessageGroup."); //NOI18N
            if (msgGrp.isShowGlobalOnly())
                resourceNameBuffer.append("global."); //NOI18N
            else
                resourceNameBuffer.append("default."); //NOI18N
            if (msgGrp.isShowDetail() && msgGrp.isShowSummary())
                resourceNameBuffer.append("both"); //NOI18N
            else if (msgGrp.isShowDetail())
                resourceNameBuffer.append("detail"); //NOI18N
            else if (msgGrp.isShowSummary())
                resourceNameBuffer.append("summary"); //NOI18N
            String summary = MessageUtil.getMessage(context, 
                    "com.sun.rave.web.ui.renderer.Bundle", //NOI18N
                    resourceNameBuffer.toString());
            FacesMessage defaultMessage = new FacesMessage();
            defaultMessage.setSummary(summary);
            msgIt = Collections.singletonList(defaultMessage).iterator();
        } else {
            if (msgGrp.isShowGlobalOnly()) {
                forComponentId = ""; // for only global messages
            }
            msgIt = FacesMessageUtils.getMessageIterator(context, 
			 forComponentId, msgGrp);
        }
        if (msgIt.hasNext()) {
            renderMessageGroup(context, msgGrp, writer, msgIt);
        }
    }
            
    /**
     * Renders the Message text
     *     
     * @param context The current FacesContext
     * @param component The VersionPage object to use
     * @param writer The current ResponseWriter
     * @param msg The message
     *
     * @exception IOException if an input/output error occurs
     */
    public void renderMessageGroup(FacesContext context, 
            UIComponent component, ResponseWriter writer,
	    Iterator msgIt) throws IOException {

	MessageGroup msgGrp = (MessageGroup) component;

        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);

	// Render the style/styleClass attributes in a surrounding div
	renderUserStyles(context, msgGrp, writer);
	    
	// Render the opening table
	renderOpeningTable(msgGrp, writer, theme);

	FacesMessage fMsg = null;
	boolean showSummary = msgGrp.isShowSummary();
	boolean showDetail = msgGrp.isShowDetail();
	String summaryStyle = ThemeStyles.MESSAGE_GROUP_SUMMARY_TEXT;
	String detailStyle = ThemeStyles.MESSAGE_GROUP_TEXT;
	String summary = null;
	String detail = null;
        
	while (msgIt.hasNext()) {
	    fMsg = (FacesMessage) msgIt.next();
	    // Check if we should show detail or summary
	    if (showSummary) {
		summary = fMsg.getSummary();
		if ((summary != null) && (summary.length() <= 0)) {
		    summary = null;
		}
	    }
	    if (showDetail) {
		detail = fMsg.getDetail();
		if ((detail != null) && (detail.length() <= 0)) {
		    detail = null;
		}
	    }		

	    if (summary == null && detail == null) 
		continue;
	    
	    writer.startElement("div", msgGrp); //NOI18N
	    writer.writeAttribute("class", //NOI18N
		      theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_DIV),  
				  null); //NOI18N
	    writer.startElement("ul", msgGrp); //NOI18N
	    writer.startElement("li", msgGrp); //NOI18N 
            
            // <RAVE>
            // render theme based style based on severity.
            String severityStyleClass = null;
            if (fMsg.getSeverity() == FacesMessage.SEVERITY_INFO) {                
                severityStyleClass = 
                        theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_INFO);
            } else if (fMsg.getSeverity() == FacesMessage.SEVERITY_WARN) {                
                severityStyleClass = 
                        theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_WARN);
            } else if (fMsg.getSeverity() == FacesMessage.SEVERITY_ERROR) {                
                severityStyleClass = 
                        theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_ERROR);
            } else if (fMsg.getSeverity() == FacesMessage.SEVERITY_FATAL) {                
                severityStyleClass = 
                        theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_FATAL);
            }
                
            if (severityStyleClass != null && severityStyleClass.length() > 0) {
                writer.writeAttribute("class", severityStyleClass, "styleClass");
            } 
            
	    if (summary != null) {
                // renderMessageText(msgGrp, writer, summary, summaryStyle);
                // if severity based style is set, don't use theme based
                // default styles.
                if (severityStyleClass == null || severityStyleClass == "") {
		    renderMessageText(msgGrp, writer, summary, summaryStyle);
                } else {
                    renderMessageText(msgGrp, writer, summary, null);
                }
            }
	   
	    if (detail != null) {    
                // renderMessageText(msgGrp, writer, detail, detailStyle);
                // if severity based style  is set, don't use theme based
                // default styles.
                if (summary != null) {
		    detail = " " + detail;
                }
                if (severityStyleClass == null || severityStyleClass == "") {
                    renderMessageText(msgGrp, writer, detail, detailStyle);
                } else {
                    renderMessageText(msgGrp, writer, detail, null);
                }		
	    }
            //</RAVE>

	    writer.endElement("li"); //NOI18N		    
	    writer.endElement("ul"); //NOI18N	
	    writer.endElement("div"); //NOI18N
	}

	// Close tags
	renderClosingTable(writer);
	writer.endElement("div"); // NOI18N
    }

    /**
     * Helper method to render opening tags for the layout table.
     * 
     * @param msgGrp The MessageGroup object to use
     * @param writer The current ResponseWriter
     * @param theme The theme to use
     *
     * @exception IOException if an input/output error occurs
     */
    public void renderOpeningTable(MessageGroup msgGrp, ResponseWriter writer,
				    Theme theme) throws IOException {
	writer.startElement("table", msgGrp); //NOI18N
        writer.writeAttribute("border", "0", null); //NOI18N
        writer.writeAttribute("cellspacing", "0", null); //NOI18N
        writer.writeAttribute("cellpadding", "0", null); //NTOI18N
        // <RAVE>
        if (msgGrp.getToolTip() != null ) {
            writer.writeAttribute("title", msgGrp.getToolTip(), null); //NOI18N
        }
        // </RAVE>
        writer.writeAttribute("class", //NOI18N
		      theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_TABLE),  
			      null); //NOI18N
        writer.writeText("\n", null); //NOI18N     

        writer.startElement("caption", msgGrp); //NOI18N
        writer.writeAttribute("class", 
		theme.getStyleClass(ThemeStyles.MESSAGE_GROUP_TABLE_TITLE),  
			      null); //NOI18N
	writer.writeText(theme.getMessage("messageGroup.heading"), null);
	writer.endElement("caption"); 
        writer.writeText("\n", null); //NOI18N
	writer.startElement("tr", msgGrp); //NOI18N
	writer.startElement("td", msgGrp); //NOI18N
    }

    /**
     * Helper method to render closing tags for the layout table.
     * 
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    public void renderClosingTable(ResponseWriter writer) throws IOException {
	writer.endElement("td"); //NOI18N
	writer.endElement("tr"); //NOI18N
	writer.endElement("table"); //NOI18N	
    }
    
    /**
     * Helper method to write message text.
     *
     * @param msgGrp The MessageGroup object to use
     * @param writer The current ResponseWriter
     * @param msgText The message text
     * @param textStyle The text style
     * 
     * @exception IOException if an input/output error occurs
     */
    public void renderMessageText(MessageGroup msgGrp, ResponseWriter writer,
	    String msgText, String textStyle) throws IOException {
	writer.startElement("span", msgGrp); //NOI18N	
        // <RAVE>
        if (textStyle != null && textStyle.length() > 0) {
	    writer.writeAttribute("class", textStyle, "class"); //
        }
        //</RAVE>
	// Check if the message be HTML escaped (true by default).
	/*
	if (!msgGrp.isEscape()) {
	    writer.write(msgText);
	} else {
	    writer.writeText(msgText, null);
	}
	*/
	writer.writeText(msgText, null);
	writer.endElement("span"); // NOI18N
    }

    /**
     * Helper method to write styles if specified. 
     * The div is always written out. (okay?)
     *
     * @param context The current FacesContext
     * @param msgGrp The MessageGroup object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    private void renderUserStyles(FacesContext context, 
            MessageGroup msgGrp, ResponseWriter writer) throws IOException {

	String userStyle = msgGrp.getStyle();
	String userStyleClass = msgGrp.getStyleClass();
        String id = msgGrp.getClientId(context); 
	
	writer.startElement("div", msgGrp); //NO18N
	writer.writeAttribute("id", id, "id"); //NOI18N
	    
	if (userStyle != null && userStyle.length() > 0) {
	    writer.writeAttribute("style", userStyle, "style"); //NOI18N
	}
	RenderingUtilities.renderStyleClass(context, writer, msgGrp, null);
    }
}
