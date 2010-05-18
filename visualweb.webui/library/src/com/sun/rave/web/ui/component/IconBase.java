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
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * <p>Use the <code>ui:icon</code> tag to display a theme-specific image in the 
 * rendered HTML page. The icon attribute used in the <code>ui:icon</code> tag is 
 * a key value that is mapped to a URL in theme properties file. The key is used 
 * to look up the appropriate image source and related attributes from the current
 * theme. By specifying a key, you avoid the need to specify predefined constants 
 * such as height and width. The image can also be seamlessly changed when a 
 * different theme is selected.</p>
 * <p>Note: currently the list of icons that you can use is not publicly 
 * supported, but the icon names are specified in the 
 * <code>/com/sun/rave/web/ui/suntheme/SunTheme.properties</code> file. The names are 
 * listed as resource keys of the format <code>image.ICON_NAME</code>. Use only 
 * the part of the key that follows image. For example, if the key is 
 * <code>image.ALARM_CRITICAL_SMALL</code>, you should specify 
 * <code>ALARM_CRITICAL_SMALL</code> as the value of the icon attribute of the 
 * <code>ui:icon</code> tag. A list of supported icon values will be published in
 * the near future.<br>
 * <h3>HTML Elements and Layout</h3>
 * The rendered HTML page displays an XHTML compliant <code>&lt;img&gt;</code>
 * element with any applicable element attributes. Some attributes are determined 
 * by the theme, and others can be specified through the <code>ui:icon</code> tag 
 * attributes.<br>
 * <h3>Theme Identifiers</h3>
 * TBD.
 * <h3>Client Side Javascript Functions</h3>
 * None.
 * <br>
 * <h3>Examples</h3>
 * <h4>Example 1: Create an icon</h4>
 * <code>&lt;ui:icon icon="ALARM_CRITICAL_SMALL" /&gt;
 * <br>
 * <br>
 * </code>This will generate the following img element: <br>
 * &nbsp;&nbsp; <br>
 * <code>&lt;img src="com_sun_rave_web_ui/images/alarms/alarm_critical_small.gif" height="10" width="10" /&gt;
 * </code><br>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class IconBase extends com.sun.rave.web.ui.component.ImageComponent {

    /**
     * <p>Construct a new <code>IconBase</code>.</p>
     */
    public IconBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Icon");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Icon";
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[1];
        _values[0] = super.saveState(_context);
        return _values;
    }

}
