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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.RelationshipDiscovery;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.keymap.DiagramInputkeyMapper;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class DiscoverRelationshipAction extends AbstractAction
{

    private DesignerScene scene;

    public DiscoverRelationshipAction(DesignerScene scene)
    {
        this.scene = scene;
        putValue(Action.SMALL_ICON, ImageUtil.instance().getIcon("relationship-discovery.png"));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(DiscoverRelationshipAction.class, "LBL_DiscoverRelationshipAction"));
        
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl alt shift H"));
        putValue(DiagramInputkeyMapper.MAC_ACCELERATOR, KeyStroke.getKeyStroke("meta ctrl shift H"));
    }

    public void actionPerformed(ActionEvent event)
    {
        Collection<IPresentationElement> nodes = scene.getNodes();
        if (scene.getSelectedObjects().size() > 0)
        {
            nodes = (Set<IPresentationElement>) scene.getSelectedObjects();
        }
        discoverRelationship(nodes);
    }

    public void discoverRelationship(Collection<IPresentationElement> nodes)
    {
        ArrayList<IElement> elements = new ArrayList<IElement>();

        for (IPresentationElement pe : nodes)
        {
            elements.add(pe.getFirstSubject());
        }
        
        DiagramEngine engine = scene.getEngine();
        RelationshipDiscovery relDiscovery = engine.getRelationshipDiscovery();
        relDiscovery.discoverCommonRelations(elements);
        scene.validate();
    }

    @Override
    public boolean isEnabled()
    {
        return scene.isReadOnly() == false;
    }
    
    
}