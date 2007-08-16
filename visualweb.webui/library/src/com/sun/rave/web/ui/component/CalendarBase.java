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
 * <p>Use the <code>ui:calendar</code> when the user needs to select a
 *     date. The calendar component displays a text field that expects a
 *     date as input, together with an icon that when clicked displays a
 *     small calendar. The user can either type directly into the
 *     textfield or select a date from the calendar display. 
 * </p>
 * 
 * <h3>HTML Elements and Layout</h3>
 * <p>
 * The component renders several elements: an optional
 * <code>&lt;label&gt;</code>, an <code>&lt;input type="text"&gt;</code>
 * and an <code>&lt;img&gt;</code> element for the icon. They are laid
 * out inside a HTML <code>&lt;table&gt;</code>. </p> <p> The pop-up
 * calendar is a complex component also laid out using a HTML
 * <code>&lt;table&gt;</code>. It has child components corresponding to
 * <code>&lt;ui:dropDown&gt;</code> and
 * <code>&lt;ui:iconHyperlink&gt;</code> (please see these for details)
 * and anchors <code>&lt;a&gt;</code> to represent the dates and the
 * "close" button. </p>
 * 
 * <h3>Configuring the <code>ui:calendar</code> tag </h3>
 * 
 * <p>Use the <code>selectedDate</code> attribute to associate the
 * component with a model object that represents the current value, by
 * setting the attribute's value to an EL expression that corresponds to
 * a property of a backing bean.</p>
 * 
 * <p>By default, the component accepts dates between the current date
 *     and four years out. The years shown in the popup calendar reflect
 *     this range. If a date outside of the range is entered into the
 *     textfield, the component indicates a validation error. To specify
 *     a different range of date, use the <code>minDate</code> and
 *     <code>maxDate</code> attributes. 
 * 
 * <p>To optionally specify a label for the component, use the
 * <code>label</code> attribute, or specify a label facet. </p>
 * 
 *     <h3>Facets</h3>
 * 
 *     <ul>
 *     <li><code>label</code>: use this facet to specify a custom 
 *     component for the label.</li>
 *     <li><code>readOnly</code>: use this facet to specify a custom 
 *     component for displaying the value of this component when it is  marked as readonly. The default is a <code>ui:staticText</code>. </li>
 *     </ul>
 * 
 * 
 *     <h3>Client-side JavaScript functions</h3>
 * 
 *     <p>In all the functions below, <code>&lt;id&gt;</code> should be
 *     the generated id of the TextField component. </p>
 * 
 *     <table cellpadding="2" cellspacing="2" border="1" 
 *            style="text-align: left; width: 100%;">
 *     <tbody>
 *     <tr>
 *     <td style="vertical-align">
 *     <code>[JSOBJECT_NAME]_setDisabled(&lt;id&gt;, &lt;disabled&gt;)</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Enable/disable the field. Set <code>&lt;disabled&gt;</code>
 *     to true to disable the component, or false to enable it.
 *     </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top">
 *     <code>component_setVisible(&lt;id&gt;)</code>
 *   </td>
 *       <td style="vertical-align: top">Hide or show this component.
 *       </td>
 *     </tr>
 *   </tbody>
 * </table>
 * 
 * <h3>Examples</h3>
 * 
 * <h4>Example 1: Basic Popup Calendar</h4>
 * 
 * <p>The component gets the options from a managed bean called
 * <code>CalendarBean</code>. The value of the component
 *     <code>selectedDate</code> is bound to a property of the managed
 *     bean. A label for the component as a whole (<code>label</code>) is
 *     shown next to the component.  
 * </p>
 * 
 * 
 * <p>
 * This example shows how to create a simple calendar.
 * </p>  
 * <p>
 *  <pre>&lt;ui:calendar id="startDate" 
 *               selectedDate="#{CalendarBean.startDate}"
 *               label="Start Date: " /&gt;</pre>
 * </p>                      
 * 
 * <p>Code for the managed bean:<p>
 * 
 * <h4>CalendarBean.java</h4>
 * 
 * <code>import java.io.Serializable;<br>
 * import java.util.Date;<br>
 * import java.util.Calendar;<br>
 * import javax.faces.event.ValueChangeEvent;<br>
 * <br>
 * <br>
 * public class CalendarBean {<br>
 * &nbsp;&nbsp;&nbsp; <br>
 * &nbsp;&nbsp;&nbsp; public CalendarBean() {<br>
 * &nbsp;&nbsp;&nbsp; } <br>
 * &nbsp;&nbsp;&nbsp; <br>
 * &nbsp;&nbsp;&nbsp; private Date startDate = null;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; public Date getStartDate() {<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * return this.startDate;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * <br>
 * &nbsp;&nbsp;&nbsp; public void setStartDate(Date startDate)
 * {<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * this.startDate = startDate;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code><br>
 * 
 * 
 *  The <code>selectAll</code> attribute indicates that the
 * <code>Add All</code> and <code>Remove All</code> buttons should be
 * shown. A label for the component as a whole (<code>label</code>) is shown
 * next to the component (<code>labelOnTop</code> is false). Labels have
 * been specified for the list of available items and for the list of
 * selected items. The <code>sorted</code> attribute indicates that the options on
 * the list will be shown in alphabetical order.</p>
 * 
 * <h4>Example 2: DateFormat Pattern and Range of Dates configured</h4>
 * 
 * <p>The component gets the options from a managed bean called
 * <code>TravelBean</code>. The value of the component
 *     <code>selectedDate</code> is bound to a property
 *     <code>travelDate</code>of the managed
 *     bean. A label for the component as a whole (<code>label</code>) is
 *     shown next to the component; the label is retrieved from a message
 *     bundle. 
 * </p>
 * 
 * <p>The component has been configured to use a pattern for date
 *     representation consisting of four digits for the year, two for the
 *     month, and two for the day, separated by dashes. This pattern, set
 *     using the <code>dateFormatPattern</code> attribute will be used
 *     regardless of locale. With this date format pattern, the default
 *     help string will be "YYYY-MM-DD", which is suitable for English,
 *     but not for other locales where other words are used, so a
 *     different message is retrieved for each locale
 *     (<code>dateFormatPattern</code>). </p> 
 * 
 * <p>
 * The component is also configured to restrict the range of dates that
 *     are valid, so that the first valid date is the day after the day
 *     the component is viewed, and the last valid date is six months
 *     from that date.
 * <p>
 * 
 * <pre>
 *     &lt;ui:calendar id="travelDate" 
 *         selectedDate="#{TravelBean.travelDate}"
 *         label="#{msgs.travelDate}"
 *         dateFormatPattern="yyyy-MM-dd"
 *         dateFormatPatternHelp="#{msgs.dateFormatPattern}"
 *         minDate="#{TravelBean.tomorrowsDate}"
 *         maxDate="#{TravelBean.sixMonthsFromNow}" /&gt;
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class CalendarBase extends com.sun.rave.web.ui.component.Field {

    /**
     * <p>Construct a new <code>CalendarBase</code>.</p>
     */
    public CalendarBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Calendar");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Calendar";
    }

    /**
     * <p>Return the <code>ValueBinding</code> stored for the
     * specified name (if any), respecting any property aliases.</p>
     *
     * @param name Name of value binding to retrieve
     */
    public ValueBinding getValueBinding(String name) {
        if (name.equals("selectedDate")) {
            return super.getValueBinding("value");
        }
        return super.getValueBinding(name);
    }

    /**
     * <p>Set the <code>ValueBinding</code> stored for the
     * specified name (if any), respecting any property
     * aliases.</p>
     *
     * @param name    Name of value binding to set
     * @param binding ValueBinding to set, or null to remove
     */
    public void setValueBinding(String name,ValueBinding binding) {
        if (name.equals("selectedDate")) {
            super.setValueBinding("value", binding);
            return;
        }
        super.setValueBinding(name, binding);
    }

    // dateFormatPattern
    private String dateFormatPattern = null;

    /**
 * <p>The date format pattern to use (i.e. yyyy-MM-dd). The
 *         component uses an instance of
 *       <code>java.text.SimpleDateFormat</code> and you may specify 
 *       a pattern to be used by this component, with the following
 *       restriction: the format pattern must include <code>yyyy</code> (not
 *       <code>yy</code>), <code>MM</code>, and <code>dd</code>; and no
 *       other parts of time may be displayed. If a pattern is not
 *       specified, a locale-specific default is used.</p> 
 *       <p> 
 *       If you change the date format pattern, you may also need to
 *       change the <code>dateFormatPatternHelp</code> attribute. See the
 *       documentation for that attribute. 
 *       </p>
     */
    public String getDateFormatPattern() {
        if (this.dateFormatPattern != null) {
            return this.dateFormatPattern;
        }
        ValueBinding _vb = getValueBinding("dateFormatPattern");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The date format pattern to use (i.e. yyyy-MM-dd). The
 *         component uses an instance of
 *       <code>java.text.SimpleDateFormat</code> and you may specify 
 *       a pattern to be used by this component, with the following
 *       restriction: the format pattern must include <code>yyyy</code> (not
 *       <code>yy</code>), <code>MM</code>, and <code>dd</code>; and no
 *       other parts of time may be displayed. If a pattern is not
 *       specified, a locale-specific default is used.</p> 
 *       <p> 
 *       If you change the date format pattern, you may also need to
 *       change the <code>dateFormatPatternHelp</code> attribute. See the
 *       documentation for that attribute. 
 *       </p>
     * @see #getDateFormatPattern()
     */
    public void setDateFormatPattern(String dateFormatPattern) {
        this.dateFormatPattern = dateFormatPattern;
    }

    // dateFormatPatternHelp
    private String dateFormatPatternHelp = null;

    /**
 * <p>A message below the textfield for the date, indicating the
 *       string format to use when entering a date as text into the
 *       textfield.</p>  
 * 
 *       <p>If the <code>dateFormatPattern</code> attribute has not been
 *       set, there is no need to set this attribute, as an
 *       appropriate locale-specific help string will be shown.</p> 
 * 
 *       <p>However, if the default <code>dateFormatPattern</code> has
 *       been overridden, then you may need to override this attribute
 *       also. The default behavior of the component is to show the
 *       pattern but capitalize it, so for example, if the value of 
 *      <code>dateFormatPattern</code> is <code>yyyy-MM-dd</code>, 
 *       then the default help text will be <code>YYYY-MM-DD</code>. 
 *       This is likely to be inadequate for languages other than
 *       English, in which you may use this attribute to provide
 *       descriptions that are appropriate for each locale.</p>
     */
    public String getDateFormatPatternHelp() {
        if (this.dateFormatPatternHelp != null) {
            return this.dateFormatPatternHelp;
        }
        ValueBinding _vb = getValueBinding("dateFormatPatternHelp");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>A message below the textfield for the date, indicating the
 *       string format to use when entering a date as text into the
 *       textfield.</p>  
 * 
 *       <p>If the <code>dateFormatPattern</code> attribute has not been
 *       set, there is no need to set this attribute, as an
 *       appropriate locale-specific help string will be shown.</p> 
 * 
 *       <p>However, if the default <code>dateFormatPattern</code> has
 *       been overridden, then you may need to override this attribute
 *       also. The default behavior of the component is to show the
 *       pattern but capitalize it, so for example, if the value of 
 *      <code>dateFormatPattern</code> is <code>yyyy-MM-dd</code>, 
 *       then the default help text will be <code>YYYY-MM-DD</code>. 
 *       This is likely to be inadequate for languages other than
 *       English, in which you may use this attribute to provide
 *       descriptions that are appropriate for each locale.</p>
     * @see #getDateFormatPatternHelp()
     */
    public void setDateFormatPatternHelp(String dateFormatPatternHelp) {
        this.dateFormatPatternHelp = dateFormatPatternHelp;
    }

    // maxDate
    private java.util.Date maxDate = null;

    /**
 * <p>A <code>java.util.Date</code> object representing the last
 *       selectable day. The default value is four years after the
 *       <code>minDate</code> (which is evaluated first).</p> 
 *       <p>The value of this attribute is reflected in the years that
 *       are available for selection in the month display. In future
 *       releases of this component, web application users will also not
 *       be able to view months after this date, or select days that
 *       follow this date. At present such dates can be selected, but
 *       will not be validated when the form is submitted.</p>
     */
    public java.util.Date getMaxDate() {
        if (this.maxDate != null) {
            return this.maxDate;
        }
        ValueBinding _vb = getValueBinding("maxDate");
        if (_vb != null) {
            return (java.util.Date) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>A <code>java.util.Date</code> object representing the last
 *       selectable day. The default value is four years after the
 *       <code>minDate</code> (which is evaluated first).</p> 
 *       <p>The value of this attribute is reflected in the years that
 *       are available for selection in the month display. In future
 *       releases of this component, web application users will also not
 *       be able to view months after this date, or select days that
 *       follow this date. At present such dates can be selected, but
 *       will not be validated when the form is submitted.</p>
     * @see #getMaxDate()
     */
    public void setMaxDate(java.util.Date maxDate) {
        this.maxDate = maxDate;
    }

    // minDate
    private java.util.Date minDate = null;

    /**
 * <p>A <code>java.util.Date</code> object representing the first
 *       selectable day. The default value is today's date.</p> 
 *       <p>The value of this attribute is reflected in the years that
 *       are available for selection in the month display. In future
 *       releases of this component, web application users will also not
 *       be able to view months before this date, or select days that
 *       precede this date. At present such dates can be selected, but
 *       will not be validated when the form is submitted.</p>
     */
    public java.util.Date getMinDate() {
        if (this.minDate != null) {
            return this.minDate;
        }
        ValueBinding _vb = getValueBinding("minDate");
        if (_vb != null) {
            return (java.util.Date) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>A <code>java.util.Date</code> object representing the first
 *       selectable day. The default value is today's date.</p> 
 *       <p>The value of this attribute is reflected in the years that
 *       are available for selection in the month display. In future
 *       releases of this component, web application users will also not
 *       be able to view months before this date, or select days that
 *       precede this date. At present such dates can be selected, but
 *       will not be validated when the form is submitted.</p>
     * @see #getMinDate()
     */
    public void setMinDate(java.util.Date minDate) {
        this.minDate = minDate;
    }

    // selectedDate
    /**
 * <p>A <code>java.util.Date</code> object representing the currently
 * 	selected calendar date.</p>
     */
    public java.util.Date getSelectedDate() {
        return (java.util.Date) getValue();
    }

    /**
 * <p>A <code>java.util.Date</code> object representing the currently
 * 	selected calendar date.</p>
     * @see #getSelectedDate()
     */
    public void setSelectedDate(java.util.Date selectedDate) {
        setValue((Object) selectedDate);
    }

    // timeZone
    private java.util.TimeZone timeZone = null;

    /**
 * <p>The <code>java.util.TimeZone</code> used with this
 *       component. Unless set, the default TimeZone for the locale in  
 *       <code>javax.faces.component.UIViewRoot</code> is used.</p>
     */
    public java.util.TimeZone getTimeZone() {
        if (this.timeZone != null) {
            return this.timeZone;
        }
        ValueBinding _vb = getValueBinding("timeZone");
        if (_vb != null) {
            return (java.util.TimeZone) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The <code>java.util.TimeZone</code> used with this
 *       component. Unless set, the default TimeZone for the locale in  
 *       <code>javax.faces.component.UIViewRoot</code> is used.</p>
     * @see #getTimeZone()
     */
    public void setTimeZone(java.util.TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.dateFormatPattern = (String) _values[1];
        this.dateFormatPatternHelp = (String) _values[2];
        this.maxDate = (java.util.Date) _values[3];
        this.minDate = (java.util.Date) _values[4];
        this.timeZone = (java.util.TimeZone) _values[5];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[6];
        _values[0] = super.saveState(_context);
        _values[1] = this.dateFormatPattern;
        _values[2] = this.dateFormatPatternHelp;
        _values[3] = this.maxDate;
        _values[4] = this.minDate;
        _values[5] = this.timeZone;
        return _values;
    }

}
