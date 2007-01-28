/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.rave.faces.converter;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.DateTimeConverter;


/**
 * <p><code>Converter<code> implementation for <code>java.util.Calendar</code>
 * values.  Delegates detailed processing, and all property settings, to an
 * internal instance of <code>dateTimeConverter</code> that is used to perform
 * the actual conversion processing.</p>
 */

public class CalendarConverter implements Converter, StateHolder {


    // ------------------------------------------------------ Manifest Constants


    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "com.sun.jsfcl.convert.Calendar";


    private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    
    // ------------------------------------------------------ Instance Variables


    /**
     * <p>Instance of <code>DateTimeConverter that is delegated to for
     * all actual processing.</p>
     */
    private DateTimeConverter converter = new DateTimeConverter();


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the style to be used to format or parse dates.  If not set,
     * the default value, <code>default<code>, is returned.</p>
     */
    public String getDateStyle() {

        return converter.getDateStyle();

    }


    /**
     * <p>Set the style to be used to format or parse dates.  Valid values
     * are <code>default</code>, <code>short</code>, <code>medium</code>,
     * <code>long</code>, and <code>full</code>.
     * An invalid value will cause a {@link ConverterException} when
     * <code>getAsObject()</code> or <code>getAsString()</code> is called.</p>
     *
     * @param dateStyle The new style code
     */
    public void setDateStyle(String dateStyle) {

	converter.setDateStyle(dateStyle);

    }


    /**
     * <p>Return the <code>Locale</code> to be used when parsing or formatting
     * dates and times. If not explicitly set, the <code>Locale</code> stored
     * in the {@link javax.faces.component.UIViewRoot} for the current 
     * request is returned.</p>
     */
    public Locale getLocale() {

	return converter.getLocale();

    }


    /**
     * <p>Set the <code>Locale</code> to be used when parsing or formatting
     * dates and times.  If set to <code>null</code>, the <code>Locale</code> 
     * stored in the {@link javax.faces.component.UIViewRoot} for the current 
     * request will be utilized.</p>
     *
     * @param locale The new <code>Locale</code> (or <code>null</code>)
     */
    public void setLocale(Locale locale) {

	converter.setLocale(locale);

    }


    /**
     * <p>Return the format pattern to be used when formatting and
     * parsing dates and times.</p>
     */
    public String getPattern() {

	return converter.getPattern();

    }


    /**
     * <p>Set the format pattern to be used when formatting and parsing
     * dates and times.  Valid values are those supported by
     * <code>java.text.SimpleDateFormat</code>.
     * An invalid value will cause a {@link ConverterException} when
     * <code>getAsObject()</code> or <code>getAsString()</code> is called.</p>
     *
     * @param pattern The new format pattern
     */
    public void setPattern(String pattern) {

	converter.setPattern(pattern);

    }


    /**
     * <p>Return the style to be used to format or parse times.  If not set,
     * the default value, <code>default</code>, is returned.</p>
     */
    public String getTimeStyle() {

	return converter.getTimeStyle();

    }


    /**
     * <p>Set the style to be used to format or parse times.  Valid values
     * are <code>default</code>, <code>short</code>, <code>medium</code>,
     * <code>long</code>, and <code>full</code>.
     * An invalid value will cause a {@link ConverterException} when
     * <code>getAsObject()</code> or <code>getAsString()</code> is called.</p>
     *
     * @param timeStyle The new style code
     */
    public void setTimeStyle(String timeStyle) {

	converter.setTimeStyle(timeStyle);

    }


    /**
     * <p>Return the <code>TimeZone</code> used to interpret a time value.
     * If not explicitly set, the default time zone of <code>GMT</code> 
     * returned.</p>
     */
    public TimeZone getTimeZone() {

	return converter.getTimeZone();

    }


    /**
     * <p>Set the <code>TimeZone</code> used to interpret a time value.</p>
     *
     * @param timeZone The new time zone
     */
    public void setTimeZone(TimeZone timeZone) {

	converter.setTimeZone(timeZone);

    }


    /**
     * <p>Return the type of value to be formatted or parsed.
     * If not explicitly set, the default type, <code>date</code> 
     * is returned.</p>
     */
    public String getType() {

	return converter.getType();

    }


    /**
     * <p>Set the type of value to be formatted or parsed.
     * Valid values are <code>both</code>, <code>date</code>, or
     * <code>time</code>.
     * An invalid value will cause a {@link ConverterException} when
     * <code>getAsObject()</code> or <code>getAsString()</code> is called.</p>
     *
     * @param type The new date style
     */
    public void setType(String type) {

	converter.setType(type);

    }


    // ------------------------------------------------------- Converter Methods

    /**
     * @exception ConverterException 
     * @exception NullPointerException 
     */ 
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {

        Date date = (Date) converter.getAsObject(context, component, value);
        if (date == null) {
            return null;
        }

        Locale locale = getLocale();
        TimeZone timeZone = getTimeZone();
        Calendar instance = null;
        if (timeZone != null) {
            instance = Calendar.getInstance(timeZone, locale);
        } else {
            instance = Calendar.getInstance(locale);
        }
        instance.setTime(date);
        return instance;

    }

    /**
     * @exception ConverterException
     * @exception NullPointerException 
     */ 
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {

        if ((value != null) && (value instanceof Calendar)) {
	    return converter.getAsString(context, component,
                                         ((Calendar) value).getTime());
	} else {
            return converter.getAsString(context, component, value);
	}

    }


    // ----------------------------------------------------- StateHolder Methods


    public Object saveState(FacesContext context) {

	return ((StateHolder) converter).saveState(context);

    }


    public void restoreState(FacesContext context, Object state) {

        DateTimeConverter converter = new DateTimeConverter();
	converter.restoreState(context, state);

    }


    private boolean transientFlag = false;


    public boolean isTransient() {
        return (transientFlag);
    }


    public void setTransient(boolean transientFlag) {
        this.transientFlag = transientFlag;
    }

}
