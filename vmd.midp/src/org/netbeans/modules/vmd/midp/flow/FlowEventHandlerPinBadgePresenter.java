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
package org.netbeans.modules.vmd.midp.flow;

import org.netbeans.modules.vmd.api.flow.FlowPinBadgePresenter;
import org.netbeans.modules.vmd.api.flow.visual.FlowBadgeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowNodeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowPinDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.midp.components.handlers.EventHandlerCD;

import java.awt.*;

/**
 * @author David Kaspar
 */
// TODO - add dependency to a presenterChanged on eventSource component
public class FlowEventHandlerPinBadgePresenter extends FlowPinBadgePresenter {

    private FlowBadgeDescriptor pinBadgeDescriptor;
    private final EventHandlerPinBadgeDecoratorBehaviour ctrl = new EventHandlerPinBadgeDecoratorBehaviour ();
    private Image badge;
    private int order;

    public FlowEventHandlerPinBadgePresenter (Image badge, int order) {
        this.badge = badge;
        this.order = order;
    }

    protected DesignComponent getEventSourceComponent () {
        return getComponent ().readProperty (EventHandlerCD.PROP_EVENT_SOURCE).getComponent ();
    }

    protected final FlowNodeDescriptor getNodeDescriptor () {
        return FlowEventSourcePinPresenter.getNodeDescriptor (getEventSourceComponent ());
    }

    public final FlowPinDescriptor getPinDescriptor () {
        return FlowEventSourcePinPresenter.getPinDescriptor (getEventSourceComponent ());
    }

    public FlowBadgeDescriptor getPinBadgeDescriptor () {
        return pinBadgeDescriptor;
    }

    protected boolean isBadgeAvailable () {
        return true;
    }

    public final void updateDescriptors () {
        final DesignComponent component = getComponent ();
        if (component == null) {
            return;
        }
        if (! isBadgeAvailable ()) {
            pinBadgeDescriptor = null;
        } else if (component != null)  {
            pinBadgeDescriptor = new FlowBadgeDescriptor (component, FlowIDSupport.createEventHandlerPinBadgeID (component));
        }
    }

    protected DesignEventFilter getEventFilter () {
        return new DesignEventFilter ().addComponentFilter (getComponent (), false).addHierarchyFilter (getComponent (), false);
    }

    public FlowBadgeDescriptor.BadgeDecorator getDecorator () {
        return ctrl;
    }

    public FlowBadgeDescriptor.BadgeBehaviour getBehaviour () {
        return ctrl;
    }

    private class EventHandlerPinBadgeDecoratorBehaviour implements FlowBadgeDescriptor.BadgeDecorator, FlowBadgeDescriptor.BadgeBehaviour {

        public int getOrder (FlowBadgeDescriptor descriptor) {
            return FlowEventHandlerPinBadgePresenter.this.order;
        }

        public Image getImage (FlowBadgeDescriptor descriptor) {
            return FlowEventHandlerPinBadgePresenter.this.badge;
        }

    }

}
