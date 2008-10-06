 /* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 /*
 /* Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 /* The contents of this file are subject to the terms of either the GNU
 /* General Public License Version 2 only ("GPL") or the Common
 /* Development and Distribution License("CDDL") (collectively, the
 /* "License"). You may not use this file except in compliance with the
 /* License. You can obtain a copy of the License at
 /* http://www.netbeans.org/cddl-gplv2.html
 /* or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 /* specific language governing permissions and limitations under the
 /* License.  When distributing the software, include this License Header
 /* Notice in each file and include the License file at
 /* nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 /* particular file as subject to the "Classpath" exception as provided
 /* by Sun in the GPL Version 2 section of the License file that
 /* accompanied this code. If applicable, add the following below the
 /* License Header, with the fields enclosed by brackets [] replaced by
 /* your own identifying information:
 /* "Portions Copyrighted [year] [name of copyright owner]"
 /*
 /* Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 /*
 /* If you wish your version of this file to be governed by only the CDDL
 /* or only the GPL Version 2, indicate your decision by adding
 /* "[Contributor] elects to include this software in this distribution
 /* under the [CDDL or GPL Version 2] license." If you do not indicate a
 /* single choice of license, a recipient has the option to distribute
 /* your version of this file under either the CDDL, the GPL Version 2 or
 /* to extend the choice of license to its licensees as provided above.
 /* However, if you add GPL Version 2 code and therefore, elected the GPL
 /* Version 2 license, then the option applies only if the new code is
 /* made subject to such option by the copyright holder.
 */

package org.netbeans.modules.visualweb.gravy.navigation;

//import org.netbeans.modules.web.jsf.navigation.NavigationView;
//import org.netbeans.modules.visualweb.navigation.archive.Page;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Operator for pages onto "Page Navigation" pane.
 */

public class PageOperator {
    private NavigatorOperator nav = null;
    private String name = null;
    public PageOperator(NavigatorOperator navOperator, String name) {
        nav = navOperator;
        this.name = name;
       // findPage();
    }

//    /**
//     * Push given item of popup menu.
//     * @param menuText Name of given item.
//     */
//    public void pushPopup(String menuText) {
//        clickForPopup();
//        new JPopupMenuOperator().pushMenu(menuText);
//    }
//
//    /**
//     * Call popup menu on page.
//     */
//    public void clickForPopup() {
//        nav.clickForPopup(getX()+3,getY()+3);
//    }
//
//    /**
//     * Double click on page.
//     */
//    public void mouseDoubleClick() {
//        nav.clickMouse(getX()+3,getY()+3, 2);
//    }
//
//    /**
//     * Select this page.
//     */
//    public void select() {
//        nav.clickMouse(getX(), getY());
//    }
//
//    /**
//     * Get x-coordinate of this page.
//     * return x-coordinate of the page.
//     */
//    public int getX() {
//        return findPage().x();
//    }
//    
//    /**
//     * Get y-coordinate of this page.
//     * return y-coordinate of the page.
//     */
//    public int getY() {
//        return findPage().y();
//    }
//
//    /**
//     * Get width of this page.
//     * return Width of the page.
//     */
//    public int getWidth() {
//        return findPage().w();
//    }
//    
//    /**
//     * Get height of this page.
//     * return Height of the page.
//     */
//    public int getHeight() {
//        return findPage().h();
//    }
//    
//    /**
//     * Get necessary page.
//     * return Page.
//     */
//    private Page findPage() {
//          Page page = waitPage();
//  /      return page;
//    }
//
//    private Page waitPage() { 
//        Waiter waiter = new Waiter (new Waitable() {
//            public Object actionProduced(Object obj) {
//                //TODO: Uncommnet once naviation module refactory complete
////                if(nav != null) {
////                    NavigationModel navModel = ((NavigationView)nav.getSource()).getNavigationModel();
////                    List<Page> pages = navModel.getPages();
////                    if(pages.size() == 0) {
////                        return  null;
////                    }
////                    for(int i=0;i <pages.size(); i++) {
////                        Page p = (Page)pages.get(i);
////                        if((p.toString()).indexOf(name) >= 0) {
////                            return p;
////                        }
////                    }
////                }
//                return null;
//            }
//            public String getDescription() {
//                return "Waiting for navigation Page";
//            }
//        });
//        try {
//           Page page = (Page)waiter.waitAction(null);
//           return page;
//        } catch (InterruptedException e) {
//            return null;
//        }
//    }
}
