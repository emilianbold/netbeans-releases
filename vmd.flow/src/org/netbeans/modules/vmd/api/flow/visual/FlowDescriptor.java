/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.api.flow.visual;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.modules.vmd.api.model.DesignComponent;

import java.awt.datatransfer.Transferable;
import java.util.List;

/**
 * @author David Kaspar
 */
public abstract class FlowDescriptor {

    private DesignComponent representedComponent;
    private String descriptorID;

    protected FlowDescriptor (DesignComponent representedComponent, String descriptorID) {
        assert representedComponent != null && descriptorID != null;
        this.representedComponent = representedComponent;
        this.descriptorID = descriptorID;
    }

    public final DesignComponent getRepresentedComponent () {
        return representedComponent;
    }

    public final String getDescriptorID () {
        return descriptorID;
    }

    @Override
    public final boolean equals (Object o) {
        if (this == o)
            return true;
        if (o == null || getClass () != o.getClass ())
            return false;
        FlowDescriptor desc = (FlowDescriptor) o;
        return representedComponent == desc.representedComponent && descriptorID.equals (desc.descriptorID);
    }

    @Override
    public final int hashCode () {
        return 29 * representedComponent.hashCode () + descriptorID.hashCode ();
    }

    @Override
    public String toString () {
        return getClass ().getSimpleName () + ":" + representedComponent.getComponentID () + ":" + descriptorID; // NOI18N
    }

    public interface Decorator {

    }

    public interface Behaviour {

    }

    public interface BadgeDecorator extends Decorator {

        void updateBadges (FlowDescriptor descriptor, FlowScene scene, List<FlowBadgeDescriptor> badges);

    }

    public interface AcceptActionBehaviour extends Behaviour {

        boolean isAcceptable (FlowDescriptor descriptor, Transferable transferable);

        void accept (FlowDescriptor descriptor, Transferable transferable);

    }

    public interface SelectActionBehaviour extends Behaviour {

        boolean select (FlowDescriptor descriptor, int modifiers);

    }

    public interface RenameActionBehaviour extends Behaviour {

        boolean isEditable (FlowDescriptor descriptor);

        String getText (FlowDescriptor descriptor);

        void setText (FlowDescriptor descriptor, String text);

    }

    public interface EditActionBehaviour extends Behaviour {

        void edit (FlowDescriptor descriptor);
        
    }

    // HINT - FlowDescriptor.KeyActionBehaviour is used for FlowScene/rootComponent only
    public interface KeyActionBehaviour extends Behaviour {

        boolean keyPressed (WidgetAction.WidgetKeyEvent e);

    }

}
