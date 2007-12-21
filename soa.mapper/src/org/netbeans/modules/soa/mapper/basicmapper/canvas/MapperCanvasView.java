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

package org.netbeans.modules.soa.mapper.basicmapper.canvas;

import org.netbeans.modules.soa.mapper.basicmapper.BasicMapperView;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasAutoLayout;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.IMapperViewModel;
import java.awt.Component;

/**
 * <p>
 *
 * Title: </p> MapperCanvasView<p>
 *
 * Description: </p> MapperCanvasView provide a basic implementation of IMapperCanvasView.<p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */

public class MapperCanvasView
     extends BasicMapperView
     implements IMapperCanvasView {

    /**
     * the mapper canvas component instance
     */
    private ICanvasView mCanvasView;

    /**
     * Constructor for the MapperCanvasView object
     */
    public MapperCanvasView() {
        mCanvasView = new BasicCanvasView(this);
        setAutoLayout(new BasicCanvasAutoLayout(this));
        setViewComponent(mCanvasView.getUIComponent());
    }

    /**
     * Return the mapper canvas view.
     *
     * @return   the mapper canvas view.
     */
    public ICanvasView getCanvas() {
        return mCanvasView;
    }

    /**
     * Return the visiual component of the canvas.
     *
     * @return the visiual component of the canvas.
     */
    public Component getCanvasComponent() {
        return (Component) mCanvasView;
    }

    /**
     * Set the view model of this view should display.
     *
     * @param model  the link mode to display
     */
    public void setViewModel(IMapperViewModel model) {
        super.setViewModel(model);
        ((BasicCanvasView) getCanvas()).setViewModel(model);
    }
}
