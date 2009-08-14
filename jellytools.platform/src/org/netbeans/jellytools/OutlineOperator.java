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
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.nodes.OutlineNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.swing.outline.Outline;

/**
 *  An operator to handle org.netbeans.swing.outline.Outline component used
 * e.g. in debugger views.
 *
 * @author Vojtech.Sigler@sun.com
 */
public class OutlineOperator extends JTableOperator {

    public OutlineOperator(ContainerOperator cont) {
        this(cont, 0);
    }

    public OutlineOperator(ContainerOperator cont, int index) {
        super((Outline)cont.waitSubComponent(
                new OutlineFinder(ComponentSearcher.
                           getTrueChooser("Any Outline")),
			   index));
	copyEnvironment(cont);
    }

    public OutlineOperator(Outline outline) {
        super(outline);
    }

    protected Outline getOutline()
    {
        return (Outline)getSource();
    }

    public int getTreeColumnIndex()
    {
        int lnNumColumns = this.getColumnCount();

        for (int i = 0; i < lnNumColumns; i++)
        {
            if (convertColumnIndexToModel(i) == 0)
                return i;
        }

        return -1;
    }

    public OutlineNode getRootNode(String isName)
    {
        return getRootNode(isName, 0);
    }

    public OutlineNode getRootNode(final String isName, final int inIndex)
    {
        final Outline lrOutline = getOutline();


        final Point lrCellPoint = this.findCell(isName, getTreeColumnIndex());
        if (lrCellPoint.equals(new Point(-1,-1)))
        {
            try
            {
                (new Waiter(new Waitable() {
                    public Object actionProduced(Object anObject) {
                        Point lrFindPoint = findCell(isName, null, new int[]{getTreeColumnIndex()}, inIndex);

                        //no cell found
                        if (lrFindPoint.equals(new Point(-1,-1)))
                            return null;

                        //cell found, but it is not a root node
                        if (lrOutline.getClosestPathForLocation(lrFindPoint.x, lrFindPoint.y).getPathCount() <= 2)
                            return null;

                        lrCellPoint.setLocation(lrFindPoint);
                        return Boolean.TRUE;
                    }
                    public String getDescription() {
                        return("Root tree node cell with name '" + isName + "' not present.");
                    }
                })).waitAction(null);
            }
            catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
            }
        }

        //TODO: pass this from the waiter directly
        TreePath lrTreePath = lrOutline.getClosestPathForLocation(lrCellPoint.x, lrCellPoint.y);

        return new OutlineNode(this, lrTreePath);
    }

    public void expandPath(TreePath irTP)
    {
        getOutline().expandPath(irTP);
    }
/*
    public Point getLocationForPath(TreePath irTreePath)
    {
        getOutline().get
    }
*/
    static class OutlineFinder implements ComponentChooser
    {
        ComponentChooser subFinder;

        public OutlineFinder(ComponentChooser finder)
        {
            subFinder = finder;
        }

        public boolean checkComponent(Component comp) {
            Class cls = comp.getClass();
            do {
                if(cls.getName().equals("org.netbeans.swing.outline.Outline")) {
                    return(subFinder.checkComponent(comp));
                }
            } while((cls = cls.getSuperclass()) != null);
	    return(false);
        }

        public String getDescription() {
            return subFinder.getDescription();
        }
        
    }

}
