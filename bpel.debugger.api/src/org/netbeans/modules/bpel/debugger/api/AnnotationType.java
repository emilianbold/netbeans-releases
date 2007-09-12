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

package org.netbeans.modules.bpel.debugger.api;

import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Yaroslavskiy
 */
public final class AnnotationType {

    public static final AnnotationType CURRENT_POSITION = new AnnotationType(
            "CurrentPC", "LBL_Current_Position"); // NOI18N

    public static final AnnotationType ENABLED_BREAKPOINT = new AnnotationType(
            "Breakpoint", "LBL_Enabled_Breakpoint"); // NOI18N

    public static final AnnotationType DISABLED_BREAKPOINT = new AnnotationType(
            "DisabledBreakpoint", "LBL_Disabled_Breakpoint"); // NOI18N
    
    public static final AnnotationType NEVER_EXECUTED_ELEMENT = new AnnotationType(
            "NeverExecutedElement", "LBL_Never_Executed_Element", true, false); // NOI18N
    
    public static final AnnotationType STARTED_ELEMENT = new AnnotationType(
            "StartedElement", "LBL_Started_Element", true, false); //NOI18N
    
    public static final AnnotationType COMPLETED_ELEMENT = new AnnotationType(
            "CompletedElement", "LBL_Completed_Element", true, false); //NOI18N
    
    public static final AnnotationType FAULTED_ELEMENT = new AnnotationType(
            "FaultedElement", "LBL_Faulted_Element", true, false); //NOI18N
    
    private String myType;
    private String myDescription;
    private boolean myIsForDiagram;
    private boolean myIsForSourceEditor;
    
    private AnnotationType(String type, String nlsDescKey) {
        this(type, nlsDescKey, true, true);
    }
    
    private AnnotationType(
            String type,
            String nlsDescKey,
            boolean isForDiagram,
            boolean isForSourceEditor)
    {
        myType = type;
        myDescription = NbBundle.getMessage(AnnotationType.class, nlsDescKey);
        myIsForDiagram = isForDiagram;
        myIsForSourceEditor = isForSourceEditor;
    }
    
    /**
     * Returns user friendly description for this annotation type.
     * @return user friendly description for this annotation type
     */
    public String getDescription() {
        return myDescription;
    }
    
    /**
     * Returns key <code>String</code> for this annotation type. 
     * @return key <code>String</code> for this annotation type
     */
    public String getType() {
        return myType;
    }
    
    public boolean isForDiagram() {
        return myIsForDiagram;
    }
    
    public boolean isForSourceEditor() {
        return myIsForSourceEditor;
    }
}
