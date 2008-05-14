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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes.activity;

import java.awt.Color;
import java.util.ResourceBundle;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.diagrams.nodes.UMLLabelNodeWidget;
import org.netbeans.modules.uml.drawingarea.border.ResizeBorder;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.openide.util.NbBundle;

/**
 *
 * @author thuy
 */
public abstract class ControlNodeWidget extends UMLLabelNodeWidget
{
    protected static int DEFAULT_OUTER_RADIUS = 14;
    protected static int DEFAULT_INNER_RADIUS = 8;
    private String contextPaletteRes = "UML/context-palette/ActivityControl";
    protected static ResourceBundle bundle = NbBundle.getBundle(UMLLabelNodeWidget.class);

    public ControlNodeWidget(Scene scene)
    {
        // Context palette is on, customizable Default node is off by default.
        this(scene, true);
    }

    public ControlNodeWidget(Scene scene, boolean contextPalette)
    {
        super(scene);
        if (contextPalette)
        {
            addToLookup(initializeContextPalette());
        }
    }

    protected DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize(contextPaletteRes);
        return paletteModel;
    }
    
    private static int RESIZE_SIZE = 5;
    private static ResizeBorder NON_RESIZABLE_BORDER =
            new ResizeBorder(RESIZE_SIZE, Color.BLACK,
                             new ResizeProvider.ControlPoint[]{});

    protected void processStateChange(ObjectState previousState,
                                       ObjectState state)
    {
        boolean select = state.isSelected();
        boolean wasSelected = previousState.isSelected();

        if (select && !wasSelected)
        {
            setBorder(NON_RESIZABLE_BORDER);
        }
        else
        {
            if (!select && wasSelected)
            {
                setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }
}

    
