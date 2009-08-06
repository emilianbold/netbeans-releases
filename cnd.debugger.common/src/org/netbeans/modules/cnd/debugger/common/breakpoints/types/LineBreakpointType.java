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

package org.netbeans.modules.cnd.debugger.common.breakpoints.types;

import javax.swing.JComponent;
import org.netbeans.modules.cnd.debugger.common.breakpoints.customizers.LineBreakpointPanel;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.openide.util.NbBundle;

/**
 * Implementation of a line breakpoint.
 *
 * @author   Egor Ushakov
 */
public class LineBreakpointType extends BreakpointType {

    public String getCategoryDisplayName() {
        return NbBundle.getMessage(LineBreakpointType.class,
                    "CTL_Common_breakpoint_events_category_name"); // NOI18N
    }
    
    public JComponent getCustomizer() {
        return new LineBreakpointPanel ();
    }
    
    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(LineBreakpointType.class, "CTL_Common_Line_Breakpoint"); // NOI18N
    }
    
    /**
     *  Tell debuggercore if this should be the default breakpoint.
     *
     *  Currently we always return false because we want to defer to FunctionBreakpointType.
     *  Eventually, this class and FunctionBreakpointType should both become smart enough
     *  that FBT is the default if the cursor is inside a function and LBT if its outside
     *  of a function (in both cases, its false if the current file in the editor isn't a
     *  C, C++, or Fortran file)
     */
    public boolean isDefault() {	
	return false;	// do false for now because FunctionBreakpointType currently
			// overrides this anyway.
    }
}

