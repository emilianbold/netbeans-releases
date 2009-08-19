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
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;

/**
 *  An operator to handle org.netbeans.swing.outline.Outline component used
 * e.g. in debugger views.
 *
 *
 * Warning: Do not use yet!! Incomplete, under development and most probably still buggy!
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

    public TreePath findNextPathElement(final TreePath irParentPath, final String isName, final int inIndex)
    {
        final Outline lrOutline = getOutline();

        if (!isExpanded(irParentPath))
            expandPath(irParentPath);
        
        //create a list of rows we will search in
        int lnRowSpan = lrOutline.getOutlineModel().getChildCount(irParentPath.getLastPathComponent());
        int lnStartRow = getRowForPath(irParentPath) + 1;
        int[] lrRowsToSearch = new int[lnRowSpan];
        for (int i = 0; i < lnRowSpan; i++)
        {
            lrRowsToSearch[i] = lnStartRow + i;
        }
        
        TreePath lrTreePath;
        Timeouts lrTimes = getTimeouts().cloneThis();

        //behavior should be similar to JTreeOperator, so we can use its timeout values
	lrTimes.setTimeout("Waiter.WaitingTime", getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout"));
        try
        {
            Waiter lrWaiter = new Waiter(new Waitable() {
                public Object actionProduced(Object anObject) {
                    int[] lrRows = (int[]) anObject;

                    Point lrFindPoint = findCell(isName, lrRows, new int[]{getTreeColumnIndex()}, inIndex);

                    //no cell found
                    if (lrFindPoint.equals(new Point(-1,-1)))
                        return null;

                    //y is row, x is not important since we're asking for a row in the tree
                    TreePath lrPath = lrOutline.getLayoutCache().getPathForRow(lrFindPoint.y);

                    //cell found, but it is not on the level we're looking for
                    if (lrPath.getPathCount() > irParentPath.getPathCount() + 1)
                        return null;

                    return lrPath;
                }
                public String getDescription() {
                    return("Tree node cell with name '" + isName + "' not present.");
                }
            });

            lrWaiter.setTimeouts(lrTimes);
            lrTreePath = (TreePath)lrWaiter.waitAction(lrRowsToSearch);
        }
        catch (InterruptedException e) {
        throw new JemmyException("Interrupted.", e);
        }

        return lrTreePath;
    }

    public TreePath findNextPathElement(TreePath irParentPath, String isName)
    {
        return findNextPathElement(irParentPath, isName, 0);
    }

    public OutlineNode getRootNode(String isName)
    {
        return getRootNode(isName, 0);
    }

    public OutlineNode getRootNode(final String isName, final int inIndex)
    {
        TreePath lrParentPath = new TreePath(getOutline().getOutlineModel().getRoot());

        return new OutlineNode(this, findNextPathElement(lrParentPath, isName, inIndex));
    }

    public TreePath findPath(TreePath irParentPath, String isPath)
    {
        int lnDelimIndex = isPath.indexOf("|");        

        if (lnDelimIndex > -1)
        {
            TreePath lrFoundPath = findNextPathElement(irParentPath, isPath.substring(0, lnDelimIndex));
            return findPath(lrFoundPath, isPath.substring(lnDelimIndex + 1));
        }

        return findNextPathElement(irParentPath, isPath);
    }

    public void expandPath(final TreePath irTP)
    {
        runMapping(new MapVoidAction("expandPath")
        {
            public void map()
            {
                getOutline().expandPath(irTP);
            }
        });
    }

    public boolean isExpanded(final TreePath irTP)
    {
        return((Boolean)runMapping(new MapAction("isExpanded")
        {
		public Object map()
                {
		    return(getOutline().isExpanded(irTP));
		}
        }));
    }

    protected int getVisibleRootModifier()
    {
        return getOutline().isRootVisible() ? 0 : -1;
    }

    public Point getLocationForPath(TreePath irTreePath)
    {
        int lnX = getTreeColumnIndex();

        int lnY = getRowForPath(irTreePath);

        return (lnY == -1) ? new Point(-1, -1) : new Point(lnX, lnY);
    }

    public int getRowForPath(TreePath irTreePath)
    {        
        if (irTreePath.getParentPath() == null)
            return getVisibleRootModifier();

        if (!getOutline().isExpanded(irTreePath.getParentPath())) 
            expandPath(irTreePath.getParentPath());

        int lnRow = -1;

        while (irTreePath.getParentPath() != null)
        {
            lnRow += 1 + getPrecedingSiblingsRowSpan(irTreePath);
            irTreePath = irTreePath.getParentPath();
        }

        return lnRow;
    }

    protected int getPrecedingSiblingsRowSpan(TreePath irTreePath)
    {
        OutlineModel lrModel = getOutline().getOutlineModel();

        if (irTreePath.getParentPath() == null)
            return 0 + getVisibleRootModifier();

        Object lrLast = irTreePath.getLastPathComponent();
        TreePath lrParent = irTreePath.getParentPath();
        int lnRowSpan = 0;

        int lnIndex = lrModel.getIndexOfChild(lrParent.getLastPathComponent(), lrLast);

        for (int i = lnIndex - 1; i >= 0; i--)
        {
            Object lrSibling = lrModel.getChild(lrParent.getLastPathComponent(), i);
            lnRowSpan += getRowSpanOfLastElement(irTreePath.pathByAddingChild(lrSibling));
        }

        return lnRowSpan;
    }

    protected int getRowSpanOfLastElement(TreePath irTreePath)
    {
        OutlineModel lrModel = getOutline().getOutlineModel();

        if (!lrModel.getTreePathSupport().isExpanded(irTreePath))
            return 1;
        
        Object lrLast = irTreePath.getLastPathComponent();
        int lnRowspan = 1; //1 for the current node
        int lnChildCount = lrModel.getChildCount(lrLast);

        for (int i = 0; i < lnChildCount; i++)
        {
            Object lnChild = lrModel.getChild(lrLast, i);

            TreePath lrTempPath = irTreePath.pathByAddingChild(lnChild);
            lnRowspan += getRowSpanOfLastElement(lrTempPath);
        }

        return lnRowspan;
    }

    private static class OutlineFinder implements ComponentChooser
    {
        private ComponentChooser subFinder;

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
