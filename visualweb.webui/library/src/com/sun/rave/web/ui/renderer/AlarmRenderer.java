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
package com.sun.rave.web.ui.renderer;

import java.io.IOException;
import java.util.Properties;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.rave.web.ui.component.Alarm;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>Renderer for an {@link Alarm} component.</p>
 *
 */
public class AlarmRenderer extends ImageRenderer {

    /** Creates a new instance of AlarmRenderer */
    public AlarmRenderer() {
        // default constructor
    }

    /**
     * Render the image element's attributes
     *
     * @param context The current FacesContext
     * @param component The ImageComponent object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderAttributes(FacesContext context, UIComponent component, 
            ResponseWriter writer) throws IOException {
	Alarm alarm = (Alarm) component;
	setAlarmProperties(alarm, ThemeUtilities.getTheme(context));
	super.renderAttributes(context, alarm, writer);	   
    }

    private void setAlarmProperties(Alarm alarm, Theme theme) {
	String severity = alarm.getSeverity();
	if (severity == null || severity.trim().length() == 0) {
	    severity = alarm.DEFAULT_SEVERITY;
        }
    
	// Start off with the theme image
	severity = severity.toLowerCase();
        if (severity.equals(alarm.SEVERITY_CRITICAL)) {
	    alarm.setIcon(ThemeImages.ALARM_CRITICAL_MEDIUM);
	    alarm.setAlt(theme.getMessage("Alarm.criticalImageAltText"));
            alarm.setToolTip(theme.getMessage("Alarm.criticalImageAltText"));
	} else if (severity.equals(alarm.SEVERITY_MAJOR)) {
	    alarm.setIcon(ThemeImages.ALARM_MAJOR_MEDIUM);
	    alarm.setAlt(theme.getMessage("Alarm.majorImageAltText"));
            alarm.setToolTip(theme.getMessage("Alarm.majorImageAltText"));
	} else if (severity.equals(alarm.SEVERITY_MINOR)) {
	    alarm.setIcon(ThemeImages.ALARM_MINOR_MEDIUM);
	    alarm.setAlt(theme.getMessage("Alarm.minorImageAltText"));
            alarm.setToolTip(theme.getMessage("Alarm.minorImageAltText"));
	} else if (severity.equals(alarm.SEVERITY_DOWN)) {
	    alarm.setIcon(ThemeImages.ALARM_DOWN_MEDIUM);
	    alarm.setAlt(theme.getMessage("Alarm.downImageAltText"));
            alarm.setToolTip(theme.getMessage("Alarm.downImageAltText"));
	} else { // SEVERITY_OK
            alarm.setIcon(null); // Clear previous rendered values for OK.
	    alarm.setAlt(null);
            alarm.setToolTip(null);
	}
    }
}
