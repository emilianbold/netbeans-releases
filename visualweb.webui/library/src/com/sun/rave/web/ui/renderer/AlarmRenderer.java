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
