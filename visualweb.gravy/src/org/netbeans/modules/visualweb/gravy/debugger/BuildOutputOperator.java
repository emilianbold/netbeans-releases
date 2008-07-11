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

package org.netbeans.modules.visualweb.gravy.debugger;

import org.netbeans.modules.visualweb.gravy.TopComponentOperator;
import org.netbeans.modules.visualweb.gravy.Util;

import java.awt.Component;

import javax.swing.JDesktopPane;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JTextPaneOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.ContainerOperator;

/** 
 * BuildOutputOperator class 
 */
public class BuildOutputOperator extends TopComponentOperator{
    JTextComponentOperator textComponent = null;
    TermOperator termOper = null;

    public BuildOutputOperator(ContainerOperator parent) {
        super(parent, new BuildOutputChooser("Build Output Window"));
    }

    public BuildOutputOperator() {
        this(Util.getMainWindow());
    }

    /** Show build output window.
     */
    public static BuildOutputOperator show() {
        return(new BuildOutputOperator(Util.getMainWindow()));
    }

    /** Return text area of the build output window.
     */
    public JTextComponentOperator getTextComponent() {
        if(textComponent == null) {
            textComponent = new JTextComponentOperator(this);
        }
        return(textComponent);
    }

    /** Get text from build output.
     */
    public String getTextOutput(){
        if(termOper == null) {
            termOper = new TermOperator(this);
        }
        return termOper.getText();
    }

    public static class BuildOutputChooser implements ComponentChooser {
        String ID;
        private Operator.StringComparator comparator;
        public BuildOutputChooser(String ID) {
            this(ID, new Operator.DefaultStringComparator(false, false));
        }
        public BuildOutputChooser(String ID, Operator.StringComparator comparator){
            this.ID = ID;
            this.comparator = comparator;
        }
        public boolean checkComponent(Component comp) {
            if(comp !=null && ((Component)comp).getName()!=null)
             return(comp instanceof Component && ((Component)comp).getName().startsWith(ID) );
            else return false;
        }
        public String getDescription() {
            return("A Component with \"" + ID + "\" ID");
        }
    }
}
