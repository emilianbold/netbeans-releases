/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
