/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.beans;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Options for setting properties of generating property patterns from fields
 *
 * @author  Petr Suchomel
 */
public class PropertyActionSettings extends SystemOption {

    public static final String GENERATE_UNDERSCORED= "_"; // NOI18N
    public static final String GENERATE_WITH_THIS  = "this."; // NOI18N
    /** define value for property style of generated property variable */
    public static final String PROP_STYLE   = "prop_style"; // NOI18N
    static final String PROP_NAME_STYLE = "propStyle"; // NOI18N

    private static final long serialVersionUID =45122597471838193L;

    private boolean initializing;

    /** inicialize object
     */
    protected void initialize () {
        super.initialize ();
        try {
            initializing = true;
            if( getProperty (PROP_STYLE) == null )
                setPropStyle(PropertyActionSettings.GENERATE_WITH_THIS);
        } finally {
            initializing = false;
        }
    }

    /** Human readable class name
     * @return readable name
     */
    public String displayName () {
        return NbBundle.getMessage(PropertyActionSettings.class, "PROP_Option_Menu");
    }

    /** Default instance of this system option, for the convenience of associated classes.
     * @return itself, only one instance
     */
    public static PropertyActionSettings getDefault() {
        return (PropertyActionSettings)PropertyActionSettings.findObject(PropertyActionSettings.class,true);
    }

    /** Return setting for generating indexed property
     * @return setting for indexed property
     */
    public String getPropStyle () {
        return (String) getProperty (PROP_STYLE);
    }

    /** Set setting for generating indexed property
     * @param indexed setting for indexed property
     */
    public void setPropStyle (String style ) {
        putProperty (PROP_STYLE, style, false);
        if (!(initializing || isReadExternal()))
            firePropertyChange(PROP_NAME_STYLE, null, null);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PropertyActionSettings.class.getName());
    }
}
