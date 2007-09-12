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
