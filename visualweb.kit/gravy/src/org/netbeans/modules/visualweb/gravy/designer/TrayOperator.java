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

package org.netbeans.modules.visualweb.gravy.designer;

import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.TimeoutExpiredException; 
import java.awt.*;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.DocumentOutlineOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.nodes.Node;

/**
 * This class implements test functionality for the window "Tray".
 */
public class TrayOperator extends DocumentOutlineOperator{
    /**
     * Creates an instance of this class.
     */
    public TrayOperator(){
        super(RaveWindowOperator.getDefaultRave());
    }

    private JTreeOperator rowsetTreeOperator=null;

    /**
     * Initializes (if necessary) and returns an object JTreeOperator 
     * for the rowset tree.
     * @return the appropriate object JTreeOperator
     */
    public JTreeOperator getRowsetTreeOperator() {
        if (rowsetTreeOperator == null) {
            rowsetTreeOperator = new JTreeOperator(this,1);
        }
        return rowsetTreeOperator;
   }

    private void clickSessionBean(){
        JTreeOperator tree = getStructTreeOperator();
        tree.expandPath(tree.findPath("SessionBean1"));
    }

    private void pushRowSetPopup_(String rowSetName, String menu) {
        getRowsetTreeOperator().callPopupOnPath(getRowsetTreeOperator().findPath(new String[]{rowSetName},false,false));
            new JPopupMenuOperator().pushMenuNoBlock(menu);
    }

    private void selectRowSet_(String rowSetName){
        getRowsetTreeOperator().selectPath(getRowsetTreeOperator().findPath(new String[]{rowSetName},false,false));
    }

    private void openRowSet_(String rowSetName){
        DocumentOutlineOperator doo = new DocumentOutlineOperator(Util.getMainWindow());
        doo.makeComponentVisible();
        Node node = new Node(doo.getStructTreeOperator(), "SessionBean1|"+rowSetName);
        node.callPopup().pushMenu("Edit SQL Statement");
        Util.wait(3000);

        //getRowsetTreeOperator().clickOnPath(getRowsetTreeOperator().findPath(new String[]{rowSetName},false,false),2);
    }

    /**
     * Clicks an item of a popup menu, related to a row set.
     * @param rowSetName a name of row set
     * @param menu a name of popup menu item
     */
    public void pushRowSetPopup(String rowSetName, String menu) {
        TestUtils.printComponentList(this);
        try{
            pushRowSetPopup_(rowSetName, menu);
            return;
        }catch (TimeoutExpiredException ex){}
        clickSessionBean();
        pushRowSetPopup_(rowSetName, menu);
    }

    /**
     * Selects a row set.
     * @param rowSetName a name of row set
     */
    public void selectRowSet(String rowSetName){
        try{
            selectRowSet_(rowSetName);
            return;
        }catch (TimeoutExpiredException ex){}
        clickSessionBean();
        selectRowSet_(rowSetName);
    }

    /**
     * Opens a row set.
     * @param rowSetName a name of row set
     */
    public void openRowSet(String rowSetName){
        try{
            openRowSet_(rowSetName);
            return;
        }catch (TimeoutExpiredException ex){}
        clickSessionBean();
        openRowSet_(rowSetName);
    }
}
