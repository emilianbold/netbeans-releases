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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo;

import java.util.logging.Logger;

import com.nwoods.jgo.JGoView;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.autolayout.TreeLayout;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.IMapperAutoLayout;
import org.netbeans.modules.soa.mapper.common.IMapperView;

/**
 * <p>
 *
 * Title: </p> BasicCanvasAutoLayout<p>
 *
 * Description: </p> BasicCanvasAutoLayout using JGoLayeredDigraphAutoLayout
 * object to perform the default canvas layout function.<p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */
public class BasicCanvasAutoLayout
    implements IMapperAutoLayout {

    /**
     * the jgo view this auto layout on
     */
    private JGoView mView;

    /**
     * the mapper canvas contains the jgo view
     */
    private IMapperCanvasView mCanvasView;

    //private DirectedGraphTableLayout mLayout;
    private TreeLayout mLayout;

    private Logger mLogger = Logger.getLogger(BasicCanvasAutoLayout.class.getName());

    /**
     * Creates a new BasicCanvasAutoLayout object with the specified mapper
     * canvas view.
     *
     * @param canvasView  Description of the Parameter
     */
    public BasicCanvasAutoLayout(IMapperCanvasView canvasView) {
        mCanvasView = canvasView;
        mView = (JGoView) canvasView.getCanvas().getUIComponent();
        //mLayout = new DirectedGraphTableLayout();
        mLayout = new TreeLayout();
    }

    /**
     * Return the mapper view that this layout performs to.
     *
     * @return   the mapper view that this layout performs to.
     */
    public IMapperView getView() {
        return mCanvasView;
    }

    /**
     * Instaniate an JGoLayeredDigraphAutoLayout with defualt configuration to
     * perform the auto layout of this JGoView specified by the constructor.
     */
    public void autoLayout() {
        if (mView.getDocument().getDefaultLayer().getNumObjects() == 0) {
            return;
        }

        // the paint method can initialize the children width and height
        if (mView.getGraphics() != null) {
            //mView.paint(mView.getGraphics());
            mView.repaint();
        }

        mLayout.performLayout(mView.getDocument().getDefaultLayer());

        // NOTE: JGoAutoLayout has a bug for reuse. Need to instaniate a new
        // JGoLayoutObject each time to performLayout.

/*
        new JGoLayeredDigraphAutoLayout(
            mView.getDocument(),
            new JGoNetwork(mView.getDocument().getDefaultLayer()),
            15,
            15,
            JGoLayeredDigraphAutoLayout.LD_DIRECTION_RIGHT,
            JGoLayeredDigraphAutoLayout.LD_CYCLEREMOVE_DFS,
            JGoLayeredDigraphAutoLayout.LD_LAYERING_OPTIMALLINKLENGTH,
            JGoLayeredDigraphAutoLayout.LD_INITIALIZE_DFSOUT,
            4,
            JGoLayeredDigraphAutoLayout.LD_AGGRESSIVE_TRUE).performLayout();



        //            mAutoLayout.setNetwork(new JGoNetwork (mView.getDocument()));
        //            mAutoLayout.performLayout();
*/
        mView.invalidate();
        mView.repaint();
    }
}
