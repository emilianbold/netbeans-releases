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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */
package org.netbeans.jemmy.drivers.menus;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.drivers.DescriptablePathChooser;
import org.netbeans.jemmy.drivers.MenuDriver;
import org.netbeans.jemmy.drivers.PathChooser;
import org.netbeans.jemmy.drivers.input.RobotDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 *
 * @author shura
 */
public class AppleMenuDriver extends RobotDriver implements MenuDriver {
    
    /**
     * Creates a new instance of AppleMenuDriver
     */
    public AppleMenuDriver() {
        super(new Timeout("apple.system.menu.delay", 100),
                new String[] {"org.netbeans.jemmy.operators.JMenuBarOperator"});
    }
    
    public Object pushMenu(ComponentOperator oper, PathChooser chooser) {
        Timeout maxTime = oper.getTimeouts().create("ComponentOperator.WaitComponentTimeout");
        JMenuBar bar = (JMenuBar)(oper.getSource());
        activateMenu(bar);
        MenuElement menuObject;
        maxTime.start();
        while(!chooser.checkPathComponent(0, (menuObject = getSelectedElement(bar)))) {
            pressKey(KeyEvent.VK_RIGHT, 0);
            releaseKey(KeyEvent.VK_RIGHT, 0);
            if(maxTime.expired()) {
                throw(new TimeoutExpiredException("AppleMenuDriver: can not find an appropriate menu!"));
            }
        }
        for(int depth = 1; depth < chooser.getDepth(); depth++) {
            // TODO - wait for menu item
            int elementIndex = getDesiredElementIndex(menuObject, chooser, depth);
            if(elementIndex == -1) {
                throw(new JemmyException("Unable to find menu (menuitem): " + ((DescriptablePathChooser)chooser).getDescription()));
            }
            for(int i = ((depth == 1) ? 0 : 1); i<=elementIndex; i++) {
                pressKey(KeyEvent.VK_DOWN, 0);
                releaseKey(KeyEvent.VK_DOWN, 0);
            }
            if(depth == chooser.getDepth() - 1) {
                pressKey(KeyEvent.VK_ENTER, 0);
                releaseKey(KeyEvent.VK_ENTER, 0);
                return(null);
            } else {
                pressKey(KeyEvent.VK_RIGHT, 0);
                releaseKey(KeyEvent.VK_RIGHT, 0);
                menuObject = menuObject.getSubElements()[0].getSubElements()[elementIndex];
            }
        }
        return menuObject;
    }
    
    private void activateMenu(JMenuBar bar) {
        if(getSelectedElement(bar) == null) {
            tryToActivate();
            if(getSelectedElement(bar) == null) {
                tryToActivate();
            }
        }
    }
    
    private void tryToActivate() {
        moveMouse(0, 0);
        pressMouse(Operator.getDefaultMouseButton(), 0);
        releaseMouse(Operator.getDefaultMouseButton(), 0);
        pressKey(KeyEvent.VK_RIGHT, 0);
        releaseKey(KeyEvent.VK_RIGHT, 0);
        pressKey(KeyEvent.VK_RIGHT, 0);
        releaseKey(KeyEvent.VK_RIGHT, 0);
    }
    
    private static MenuElement getSelectedElement(MenuElement bar) {
        MenuElement[] subElements = bar.getSubElements();
        for(int i = 0; i < subElements.length; i++) {
            if(subElements[i] instanceof JMenu &&
                    ((JMenu)subElements[i]).isSelected()) {
                return(subElements[i]);
            } else if(subElements[i] instanceof JMenuItem &&
                    ((JMenuItem)subElements[i]).isSelected()) {
                return(subElements[i]);
            }
        }
        return(null);
    }
    
    private static int getDesiredElementIndex(MenuElement bar, PathChooser chooser, int depth) {
        MenuElement[] subElements = bar.getSubElements()[0].getSubElements();
        int realIndex = 0;
        for(int i = 0; i < subElements.length; i++) {
            // do not count invisible and disabled menu items
            if(subElements[i] instanceof JMenuItem && ((JMenuItem)subElements[i]).isVisible() && ((JMenuItem)subElements[i]).isEnabled()) {
                if(chooser.checkPathComponent(depth, subElements[i])) {
                    return realIndex;
                }
                realIndex++;
            }
        }
        return(-1);
    }
}
