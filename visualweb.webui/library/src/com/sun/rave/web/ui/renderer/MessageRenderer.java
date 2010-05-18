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
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIMessages;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.rave.web.ui.component.Message;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.FacesMessageUtils;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>This class is responsible for rendering the Message component.</p>
 */
public class MessageRenderer extends AbstractRenderer {

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
	Message message = (Message) component;
	
	String forComponentId = message.getFor();
	FacesMessage msg = null;
	Iterator msgIt = null;
	
	if (Beans.isDesignTime()) {
            // At design-time, prepare a default message
            String summary = null;
      
            if (forComponentId == null || forComponentId.length() == 0) {
                summary = MessageUtil.getMessage(context, 
                    "com.sun.rave.web.ui.renderer.Bundle", "Message.default.summary"); //NOI18N
                // <RAVE>
                renderMessage(context, component, writer, new FacesMessage(summary));
                // </RAVE>
            }
            else {
                summary = MessageUtil.getMessage(context,
                    "com.sun.rave.web.ui.renderer.Bundle", "Message.for.summary", //NOI18N
                    new String[] {forComponentId});
                // <RAVE>
                String detail = MessageUtil.getMessage(context,
                    "com.sun.rave.web.ui.renderer.Bundle", "Message.for.detail", //NOI18N
                    new String[] {forComponentId});
                
                renderMessage(context, component, writer, 
                        new FacesMessage(summary, detail));
                // </RAVE>
            }   
        } else if (forComponentId != null) {
	    // Get the run-time messages for this component, if any
	    msgIt = FacesMessageUtils.getMessageIterator(context, 
			forComponentId, message);
	    if (msgIt.hasNext()) {
		msg = (FacesMessage) msgIt.next();
		renderMessage(context, component, writer, msg);
	    }
	}
    }
            
    /**
     * Renders the Message text
     *     
     * @param context The current FacesContext
     * @param component The Message object to use
     * @param writer The current ResponseWriter
     * @param fMsg The FacesMessage message
     *
     * @exception IOException if an input/output error occurs
     */
    public void renderMessage(FacesContext context, 
            UIComponent component, ResponseWriter writer,
	    FacesMessage fMsg) throws IOException {

	Message message = (Message) component;
	String summary = null;
	String detail = null;

	// Check if there is both summary and detail messages to show
	if (message.isShowSummary()) {
	    summary = fMsg.getSummary();
	    if ((summary != null) && (summary.length() <= 0)) {
		summary = null;
	    }
	}
	if (message.isShowDetail()) {
	    detail = fMsg.getDetail();
	    if ((detail != null) && (detail.length() <= 0)) {
		detail = null;
	    }
	}

	if (summary == null && detail == null) 
	    return;

        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);
	boolean wroteSpanId = false;
        boolean wroteDivId = false;
        
        // <RAVE>
        String style = message.getStyle();
        String styleClass = message.getStyleClass();
        // Render the style/styleClass attributes in a surrounding div
	if (summary != null || detail != null) {        
	   renderUserStyles(context, message, writer, style, styleClass);
           wroteDivId = true;
	}
                       
        // if user has defined severity based style, use that instead of
        // default.
        String severityStyleClass = null;
        if (fMsg.getSeverity() == FacesMessage.SEVERITY_INFO) {                
            severityStyleClass = theme.getStyleClass(ThemeStyles.MESSAGE_INFO);
        } else if (fMsg.getSeverity() == FacesMessage.SEVERITY_WARN) {                
            severityStyleClass = theme.getStyleClass(ThemeStyles.MESSAGE_WARN);
        } else if (fMsg.getSeverity() == FacesMessage.SEVERITY_ERROR) {                
            severityStyleClass = theme.getStyleClass(ThemeStyles.MESSAGE_ERROR);
        } else if (fMsg.getSeverity() == FacesMessage.SEVERITY_FATAL) {                
            severityStyleClass = theme.getStyleClass(ThemeStyles.MESSAGE_FATAL);
        }
        
	if (summary != null) {
            if (severityStyleClass == null || severityStyleClass.length() == 0
                    || Beans.isDesignTime()) {
	        styleClass =  theme.getStyleClass(
			     ThemeStyles.MESSAGE_FIELD_SUMMARY_TEXT);
            } else {
                styleClass = severityStyleClass;
            }            
	    renderMessageText(context, message, writer, 
			      summary, styleClass, wroteSpanId);
            wroteSpanId = true;
	}

	if (detail != null) {
            if (severityStyleClass == null || severityStyleClass.length() == 0
                    || Beans.isDesignTime()) {
	        styleClass =  theme.getStyleClass(ThemeStyles.MESSAGE_FIELD_TEXT);
            } else {
                styleClass = severityStyleClass;
            }            
	    if (summary != null) 
		detail = " " + detail;
	    renderMessageText(context, message, writer, 
			      detail, styleClass, wroteSpanId);
	}
        
        if (wroteSpanId) {
	    writer.endElement("span"); // NOI18N
	}

	if (wroteDivId) {
	    writer.endElement("div"); // NOI18N
	}
        //</RAVE>
    }

    /**
     * Helper method to write message text.
     *
     * @param context The current FacesContext
     * @param message The Message object to use
     * @param writer The current ResponseWriter
     * @param msgText The message text
     * @param textStyle The text style
     * @param wroteOpeningSpanId Flag to indicate whether opening 
     *        span and id were written
     * 
     * @exception IOException if an input/output error occurs
     */
    private void renderMessageText(FacesContext context, Message message, 
		ResponseWriter writer, String msgText, String textStyleClass,
		boolean wroteOpeningSpanId) 
	        throws IOException {
        //<RAVE>
	//if (!wroteOpeningSpanId) {
	//    renderUserStyles(context, message, writer, textStyle, textStyleClass);
	//} else {
        //</RAVE>
        if (!wroteOpeningSpanId) {
	    writer.startElement("span", message); //NOI18N		
	    writer.writeAttribute("class", textStyleClass, "class"); //NOI18N
	}
	// Check if the message be HTML escaped (true by default).
	/*
	if (!message.isEscape()) {
	    writer.write(msgText);
	} else {
	    writer.writeText(msgText, null);
	}
	*/        
	writer.writeText(msgText, null);
        //<RAVE>
	//writer.endElement("span"); // NOI18N
        //</RAVE>
    }

    /**
     * Helper method to write styles if specified. 
     * The span is written out even if no style/styleClass was specified.
     *
     * @param context The current FacesContext
     * @param message The Message object to use
     * @param writer The current ResponseWriter
     * @param styleClass The message style class
     *
     * @exception IOException if an input/output error occurs
     */
    private void renderUserStyles(FacesContext context, 
            Message message, ResponseWriter writer, String userStyle,
	    String styleClass) throws IOException {
	// <RAVE>
        String id = message.getClientId(context); 
	
	writer.startElement("div", message); //NO18N
	writer.writeAttribute("id", id, "id"); //NOI18N
	    
	if (userStyle != null && userStyle.length() > 0) {
	    writer.writeAttribute("style", userStyle, "style"); //NOI18N
	}
        // </RAVE>
	RenderingUtilities.renderStyleClass(context, writer,
					    message, styleClass);
    }

}
