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
import java.text.DateFormat;
import java.io.IOException;
import java.util.MissingResourceException;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIComponent;

import com.sun.rave.web.ui.component.Calendar;
import com.sun.rave.web.ui.component.CalendarMonth;
import com.sun.rave.web.ui.component.ImageHyperlink;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.theme.ThemeJavascript;
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;
import java.text.SimpleDateFormat;

/**
 * <p>Renders an instance of the Calendar component.</p>
 *
 */
public class CalendarRenderer extends FieldRenderer {
    
    private final static boolean DEBUG = false;
    
    /** Creates a new instance of CalendarRenderer. */
    public CalendarRenderer() {
    }
    
    /**
     * <p>Render the component end element.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @exception IOException if an input/output error occurs
     */
    public void encodeEnd(FacesContext context, UIComponent component)
    throws IOException {
        
        if(!(component instanceof Calendar)) {
            Object[] params = { component.toString(),
                    this.getClass().getName(),
                    Calendar.class.getName() };
                    String message = MessageUtil.getMessage
                            ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
                            "Renderer.component", params);              //NOI18N
                    throw new FacesException(message);
        }
        
        Calendar calendar = (Calendar)component;
        boolean readOnly = calendar.isReadOnly();
        
        ResponseWriter writer = context.getResponseWriter();
        String[] styles = getStyles(calendar, context);
        String clientId = calendar.getClientId(context);
        
        // Write the JavaScript
        includeJsFile(writer, calendar, styles[12]);
        
        // Start a table
        renderTableStart(calendar, styles[18], styles[2], context, writer);
        
        // Table cell for the label
        UIComponent label = calendar.getLabelComponent(context, null);
        if(label != null) {
            renderCellStart(calendar, styles[6], writer);
            RenderingUtilities.renderComponent(label, context);
            renderCellEnd(writer);
        }
        
        
        // Table cell for the field and the date format string
        
        if(readOnly) {
            renderCellStart(calendar, styles[6], writer);
            UIComponent text = calendar.getReadOnlyComponent(context);
            RenderingUtilities.renderComponent(text, context);
            if(calendar.getValue() != null)  {
                renderPattern(calendar, styles[7], styles[2], context, writer);
            }
        } else {
            renderCellStart(calendar, styles[4], writer);
            renderInput(calendar, "text", clientId.concat(calendar.INPUT_ID),
                    false, styles, context, writer);
            writer.write("\n");
            renderPattern(calendar, styles[7], styles[2], context, writer);
        }
        
        renderCellEnd(writer);
        
        if(!readOnly) {
            // <rave> Fix popup so that it always appears near button. eeg 2005-11-04

            // Table cell for the link
            //renderCellStart(calendar, styles[5], writer);

            // Start of table cell
            writer.startElement("td", calendar);
            writer.writeAttribute("valign", "top", null);//NOI18N
            writer.writeText("\n", null);
            
            // Create a common CSS containing block used for positioning
            writer.startElement("div", calendar);
            writer.writeAttribute("style", "position: relative;", null);// NOI18N
            writer.writeText("\n", null);
            
            // This is the span for the link
            writer.startElement("span", calendar);
            writer.writeAttribute("class", styles[5], null); //NOI18N
            writer.writeText("\n", null);

            ImageHyperlink link =
                    calendar.getDatePickerLink(context);
            writer.startElement("span", calendar);
            if(calendar.isDisabled()) {
                writer.writeAttribute("style", "display:none;", null);
            }
            writer.write("\n");
            
            link.setIcon(styles[14]);
            link.setAlt(styles[13]);
            link.setToolTip(styles[13]);
            RenderingUtilities.renderComponent(link, context);
            
            // Close span for disable
            writer.endElement("span");
            writer.write("\n");

            // Close link span
            writer.endElement("span");
            writer.write("\n");
            
            renderDatePicker(context, writer, styles, calendar);
            
            // Close remaining div and table cell
            writer.endElement("div");
            writer.writeText("\n", null);
            writer.endElement("td");
            writer.writeText("\n", null);
            // </rave>
        }
        // End the table
        renderTableEnd(writer);
        writer.writeText("\n", null);      
    }

    // <rave> Fix popup so that it always appears near button. eeg 2005-11-04
    private void renderDatePicker(FacesContext context, ResponseWriter writer, String[] styles, Calendar calendar) throws IOException {
        // render date picker
        CalendarMonth datePicker = calendar.getDatePicker();
        Object value = calendar.getSubmittedValue();
        if(value != null) {
            try {
                Object dO =
                        ConversionUtilities.convertValueToObject(calendar,
                        (String)value,
                        context);
                datePicker.setValue(dO);
            } catch(Exception ex) {
                // do nothing
            }
        } else if(calendar.getValue() != null) {
            datePicker.setValue(calendar.getValue());
        }
        datePicker.initCalendarControls(calendar.getJavaScriptObjectName(context));
        RenderingUtilities.renderComponent(datePicker, context);
        //JS should be initialized by CalendarMonth, not by this component....
        writer.write(getJavaScriptInitializer(calendar, styles, context));
    }
    // </rave>
    
    private void renderTableStart(Calendar calendar, String rootStyle,
            String hiddenStyle, FacesContext context, ResponseWriter writer)
            throws IOException {
        
        writer.startElement("table", calendar);
        writer.writeAttribute("border", "0", null); // NOI18N
        writer.writeAttribute("cellspacing", "0", null); // NOI18N
        writer.writeAttribute("cellpadding", "0", null); // NOI18N
        writer.writeAttribute("title", "", null); // NOI18N
        writer.writeAttribute("id", calendar.getClientId(context) , "id"); //NOI18N
        String style = calendar.getStyle();
        if(style != null && style.length() > 0) {
            writer.writeAttribute("style", style, "style"); //NOI18N
        }
        
        style = getStyleClass(calendar, hiddenStyle);
        // <RAVE>
        // Append a styleclass to top level table element so we can use it to
        // fix a pluto portal bug by restoring the initial width value
        if (style == null) {
            style = rootStyle;
        } else {
            style += " " + rootStyle;
        }
        // </RAVE>
        if(style != null) {
            writer.writeAttribute("class", style, "class"); //NOI18N
        }
        writer.writeText("\n", null);
        writer.startElement("tr", calendar);
        writer.writeText("\n", null);
    }
    
    private void renderCellStart(Calendar calendar, String style,
            ResponseWriter writer)
            throws IOException {
        
        writer.startElement("td", calendar);
        writer.writeAttribute("valign", "top", null);//NOI18N
        writer.writeText("\n", null);
        writer.startElement("span", calendar);
        writer.writeAttribute("class", style, null); //NOI18N
        writer.writeText("\n", null);
    }
    
    private void renderCellEnd(ResponseWriter writer)
    throws IOException {
        
        writer.writeText("\n", null);
        writer.endElement("span");
        writer.writeText("\n", null);
        writer.endElement("td");
        writer.writeText("\n", null);
    }
    
    private void renderTableEnd(ResponseWriter writer)
    throws IOException {
        
        writer.endElement("tr"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        writer.endElement("table"); //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    private void renderPattern(Calendar calendar, String styleClass,
            String hiddenStyle, FacesContext context, ResponseWriter writer)
            throws IOException {
        
        String hint = calendar.getDateFormatPatternHelp();
        if(hint == null) {
            String pattern = calendar.getDatePicker().getDateFormatPattern(); 

            try {  
                hint = ThemeUtilities.getTheme(context).getMessage("calendar.".concat(pattern)); 
            }
            catch(MissingResourceException mre) { 
                hint = pattern.toLowerCase();
            }       
        }
        if(hint != null) {
            writer.startElement("div", calendar); //NOI18N
            String id = calendar.getClientId(context);
            id = id.concat(Calendar.PATTERN_ID);
            writer.writeAttribute("id", id, null); //NOI18N
            String style = styleClass;
            if(calendar.isDisabled()) {
                style = style.concat(" ").concat(hiddenStyle);
            }
            writer.writeAttribute("class", style, null); //NOI18N
            writer.writeText(hint, null);
            writer.endElement("div"); //NOI18N
        }
    }
    
    // TODO - simplify this
    private String getJavaScriptInitializer(Calendar calendar,
            String[] styles,
            FacesContext context) {
        
        if(DEBUG) log("getJavaScriptInitializer()"); //NOI18N
        
        // construct a buffer for the object js
        String jsName = calendar.getJavaScriptObjectName(context);
        
        StringBuffer js = new StringBuffer(300);
        
        js.append("\n<script type=\"text/javascript\">\nvar ")
        .append(jsName)
        .append(" = new ui_Calendar(");
        
        // First argument is the first day of the week
        
        int firstDay = calendar.getDatePicker().getCalendar().getFirstDayOfWeek();
        String day = null;
        if(firstDay == java.util.Calendar.SUNDAY) {
            day = "0";
        } else  if(firstDay == java.util.Calendar.MONDAY) {
            day = "1";
        } else  if(firstDay == java.util.Calendar.FRIDAY) {
            day = "5";
        } else  if(firstDay == java.util.Calendar.SATURDAY) {
            day = "6";
        } else  if(firstDay == java.util.Calendar.TUESDAY) {
            day = "2";
        } else  if(firstDay == java.util.Calendar.WEDNESDAY) {
            day = "3";
        } else  if(firstDay == java.util.Calendar.THURSDAY) {
            day = "4";
        }
        
        // Argument 1: first day of week
        js.append(day)
        .append(", '");
        
        // Argument 2: the ID of the field
        js.append(calendar.getClientId(context).concat(Calendar.INPUT_ID))
        .append("', '");
        
        // Argument 3: the ID of the pattern
        js.append(calendar.getClientId(context).concat(Calendar.PATTERN_ID))
        .append("', '");
        
        // Argument 4: the ID of the image used as a link to open the
        // calendar
        js.append(calendar.getDatePickerLink(context).getClientId(context))
        .append("', '");
        
        // Argument 5: the ID of the CalendarMonth component
        String id = calendar.getDatePicker().getClientId(context);
        js.append(id)
        .append("', '");
        
        // Argument 6: month menu id
        js.append(calendar.getDatePicker().getMonthMenu().getClientId(context))
        .append("', '");
        
        // Argument 7:
        js.append(calendar.getDatePicker().getYearMenu().getClientId(context))
        .append("', '");
        
        // Argument 8: the last row id
        js.append(id)
        .append(":row5")
        .append("', '");
        
        // Argument 9: the show button style
        js.append(styles[8])
        .append("', '");
        
        // Argument 10: is the hide button style
        js.append(styles[9])
        .append("', '");
        
        // Argument 11: dateformat pattern
        js.append(calendar.getDatePicker().getDateFormatPattern())
        .append("', '");
        
        // Argument 12: date link style
        js.append(styles[10])
        .append("', '");
        
        // Argument 13: date link style
        js.append(styles[11])
        .append("', '");
        
        // Argument 14: date link style
        js.append(styles[15])
        .append("', '");
        
        // Argument 15: date link style
        js.append(styles[16])
        .append("', '");
        
        // Argument 16: date link style
        js.append(styles[17])
        .append("', '");
        
        // Argument 17: hidden style
        js.append(styles[2])
        .append("');\n</script>\n");
        
        return js.toString();
    }
    
    private void includeJsFile(ResponseWriter writer, Calendar calendar,
            String src) throws IOException {
        writer.startElement("script", calendar); //NOI18N
        writer.writeAttribute("type", "text/javascript", null); //NOI18N
        writer.writeAttribute("src", src, null); //NOI18N
        writer.endElement("script"); //NOI18N
        writer.write("\n"); //NOI18N
    }
    
    String[] getStyles(Calendar calendar, FacesContext context) {
        Theme theme = ThemeUtilities.getTheme(context);
        String[] styles = new String[19];
        styles[0] = theme.getStyleClass(ThemeStyles.TEXT_FIELD);
        styles[1] = theme.getStyleClass(ThemeStyles.TEXT_FIELD_DISABLED);
        styles[2] = theme.getStyleClass(ThemeStyles.HIDDEN);
        styles[3] = "";
        styles[4] = ""; // used to be CalPopFld, removed
        styles[5] = theme.getStyleClass(ThemeStyles.CALENDAR_FIELD_IMAGE);
        styles[6] = theme.getStyleClass(ThemeStyles.CALENDAR_FIELD_LABEL);
        styles[7] = theme.getStyleClass(ThemeStyles.HELP_FIELD_TEXT);
        styles[8] = theme.getIcon(ThemeImages.CALENDAR_BUTTON).getUrl();
        styles[9] = theme.getIcon(ThemeImages.CALENDAR_BUTTON_FLIP).getUrl();
        styles[10] = theme.getStyleClass(ThemeStyles.DATE_TIME_LINK);
        styles[11] = theme.getStyleClass(ThemeStyles.DATE_TIME_OTHER_LINK);
        styles[12] = theme.getPathToJSFile(ThemeJavascript.CALENDAR);
        styles[13] = theme.getMessage("calendar.popupImageAlt");
        styles[14] = ThemeImages.CALENDAR_BUTTON;
        // Style for the selected date
        styles[15] = theme.getStyleClass(ThemeStyles.DATE_TIME_BOLD_LINK);
        // Style for selected date on the edge
        styles[16] = theme.getStyleClass(ThemeStyles.DATE_TIME_OTHER_BOLD_LINK);
        // Style for today's date
        styles[17] = theme.getStyleClass(ThemeStyles.DATE_TIME_TODAY_LINK);
        // <RAVE>
        styles[18] = theme.getStyleClass(ThemeStyles.CALENDAR_ROOT_TABLE);
        // </RAVE>
        return styles;
    }
}
