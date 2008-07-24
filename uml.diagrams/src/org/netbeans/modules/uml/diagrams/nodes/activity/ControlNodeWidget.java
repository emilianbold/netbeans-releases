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

import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.diagrams.nodes.UMLLabelNodeWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author thuy
 */
public abstract class ControlNodeWidget extends UMLLabelNodeWidget
{
    protected static int DEFAULT_OUTER_RADIUS = 10;
    protected static int DEFAULT_INNER_RADIUS = 5;
    private String contextPaletteRes;
    protected static ResourceBundle bundle = NbBundle.getBundle(UMLLabelNodeWidget.class);
    
    public ControlNodeWidget(Scene scene, String contextPaletteRes)
    {
        super(scene);
        if (contextPaletteRes != null && contextPaletteRes.trim().length() > 0)
        {
            this.contextPaletteRes = contextPaletteRes;
            addToLookup(initializeContextPalette());
        }
    }
    
    protected DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize(contextPaletteRes);
        return paletteModel;
    }
    
    
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        IElement element = (IElement) event.getSource();
        String propName = event.getPropertyName();
        UMLLabelWidget labelWidget = getLabelWidget();
         if (element instanceof INamedElement)
        {
            if (labelWidget != null && propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
            {
                String label = ((INamedElement) element).getNameWithAlias();
                labelWidget.setLabel(label);
                if ( label != null && label.trim().length() > 0 && !labelWidget.isVisible())
                {
                    labelWidget.setVisible(true);
                }
            } else
            {
                super.propertyChange(event);
            }
        } 
    }
}

    
