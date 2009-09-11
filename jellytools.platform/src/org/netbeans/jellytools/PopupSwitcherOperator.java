/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import javax.swing.JTable;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.WindowWaiter;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * Class to handle PopupSwitcher - e.g. the popup that is shown after clicking
 * the down arrow button in the editor window and shows a table
 * of opened documents.
 *
 * @author Vojtech.Sigler@sun.com
 */
public class PopupSwitcherOperator extends JTableOperator {

    /**
     * Constructor.
     * Waits for first popup window with a JTable in it.
     */
    public PopupSwitcherOperator()
    {
      this(waitPopupSwitcher());
    }

    /**
     * Constructor.
     *
     * @param lrTable a JTable
     */
    public PopupSwitcherOperator(JTable lrTable)
    {
        super(lrTable);
    }

    /**
     * Finds and selects an item with given name.
     * @param isName name of the item to be selected
     */
    public void selectItem(String isName)
    {
        Point lrLocation = this.findCell(isName, 0);
        if(!lrLocation.equals(new Point(-1,-1)))
        {
            selectCell(lrLocation.y, lrLocation.x);
        } 
        else
        {
            throw new JemmyException("Cannot select item \""+isName+"\".");
        }
    }

    /**
     * Selects indexth item in the table starting from top left.
     *
     * @param inIndex index of the item to be selected
     */
    public void selectItem(int inIndex)
    {

        int lnRowCount = getRowCount();

        if (lnRowCount == 0)
            throw new JemmyException("Cannot select item with index \""+ inIndex +"\", table is empty.");

        int lnColumn = 0;

        if (inIndex > lnRowCount)
        {
            lnColumn = inIndex / lnRowCount;
            inIndex = inIndex % lnRowCount;
        }
        selectCell(inIndex, lnColumn);
    }

    /**
     * Selects an item with the given coordinates.
     *
     * @param inRow row of the item
     * @param inColumn column of the item
     */
    public void selectItem(int inRow, int inColumn)
    {
        selectCell(inRow, inColumn);
    }

    /**
     * Waits for a popup window to show and then for a JTable in it.
     *
     * @return found JTable
     */
    public static JTable waitPopupSwitcher()
    {
        Window lrPopupWindow = null;

        try
        {
            lrPopupWindow = new WindowWaiter().waitWindow(new PopupChooser(ComponentSearcher.
                           getTrueChooser("Any Popup window")));
        }
        catch (InterruptedException e)
        {
            throw new JemmyException("Waiting for popup window interrupted.");
        }

        return JTableOperator.waitJTable(lrPopupWindow, ComponentSearcher.
                           getTrueChooser("Any JTable"));
    }

    private static class PopupChooser implements ComponentChooser
    {
        private ComponentChooser subChooser;

        public PopupChooser(ComponentChooser irChooser)
        {
            subChooser = irChooser;
        }

        public boolean checkComponent(Component irComponent) {
            Class lrClass = irComponent.getClass();
            do {
                if(lrClass.getName().startsWith("javax.swing.Popup")) {
                    return(subChooser.checkComponent(irComponent));
                }
            } while((lrClass = lrClass.getSuperclass()) != null);
	    return(false);
        }

        public String getDescription() {
            return subChooser.getDescription();
        }

    }
}
