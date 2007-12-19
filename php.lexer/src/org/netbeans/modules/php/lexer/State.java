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
package org.netbeans.modules.php.lexer;


/**
 * @author ads
 *
 */
class State {

    public enum States {
        INIT,               // initial lexer state = content language ( CL )
        ISA_LT,             // after '<' char
        ISA_LT_PC,          // after '<%' - comment or directive or scriptlet
        ISA_LT_Q,           // after '<?' 
        ISI_SCRIPTLET,      // inside scriptlet/declaration/expression
        ISP_SCRIPTLET_PC,   // just after % in scriptlet
        ISI_PHP,            // inside php part
        ISP_PHP,            // after ? in php part
        ISA_LT_QP,          // after <?p
        ISA_LT_QPH,         // after <?ph
        ISA_LT_QPHP,        // after <?php
    }
    
    void setCurrentState( States state ) {
        /*
         * Annulate substate in case of changing state.
         */
        if ( state != myState ) {
            mySubstate = null;
        }
        myState = state;
    }
    
    States getCurrentState() {
        return myState;
    }
    
    void setSubstate( Object subState ) {
        mySubstate = subState;
    }
    
    Object getSubstate() {
        return mySubstate;
    }
    
    StringBuilder getSavedData() {
        return myData;
    }
    
    void setSavedData( StringBuilder builder ) {
        myData = builder;
    }
    
    StringBuilder getCurrentData() {
        return myCurrentData;
    }
    
    void setCurrentData( StringBuilder builder ) {
        myCurrentData = builder;
    }
    
    private States myState;
    
    /*
     * This is subordinate state for main stated <code>myState</code>.
     * It is saved here for possibility to annulate it when state has changed.
     */
    private Object mySubstate;
    
    private StringBuilder myData;
    
    private StringBuilder myCurrentData;

}
