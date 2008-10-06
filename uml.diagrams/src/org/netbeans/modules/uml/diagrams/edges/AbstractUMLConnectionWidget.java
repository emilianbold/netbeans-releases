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
package org.netbeans.modules.uml.diagrams.edges;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author treyspiva
 */
public abstract class AbstractUMLConnectionWidget extends UMLEdgeWidget
        implements PropertyChangeListener
{
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(lookupContent);
    
    public AbstractUMLConnectionWidget(Scene scene)
    {
        super(scene);
        
        initLabelManager();
        
    }

    protected void initLabelManager() 
    {
        LabelManager labelManager = createLabelManager();
        if(labelManager != null)
        {
            addToLookup(labelManager);
        }
    }

    protected void addToLookup(Object item)
    {
        lookupContent.add(item);
    }
    
    @Override
    public Lookup getLookup()
    {
        return lookup;
    }
    
    protected LabelManager createLabelManager()
    {
        return new BasicUMLLabelManager(this);
    }
    
    public void propertyChange(PropertyChangeEvent evt)
    {
        LabelManager manager = getLookup().lookup(LabelManager.class);
        if(manager != null)
        {
            manager.propertyChange(evt);
        }
    }
    
    @Override
    public void notifyStateChanged(ObjectState previousState, ObjectState state)
    {
        super.notifyStateChanged(previousState, state);
        
        boolean select = state.isSelected();
        boolean wasSelected = previousState.isSelected();

        if (select && !wasSelected)
        {
//            setBorder(BorderFactory.createResizeBorder(5));
        }
        else if (!select && wasSelected)
        {
//            setBorder(BorderFactory.createEmptyBorder());
        }
    //else do nothing
    }
    
    public void initialize(IPresentationElement element)
    {
        
    }
}
