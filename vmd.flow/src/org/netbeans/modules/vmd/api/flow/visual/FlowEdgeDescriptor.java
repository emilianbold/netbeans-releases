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

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 * @author David Kaspar
 */
public final class FlowEdgeDescriptor extends FlowDescriptor {

    private FlowPinDescriptor sourcePinDescriptor;
    private boolean dynamicSourcePin;
    private FlowPinDescriptor targetPinDescriptor;
    private boolean dynamicTargetPin;

    public FlowEdgeDescriptor (DesignComponent representedComponent, String descriptorID, FlowPinDescriptor sourcePinDescriptor, boolean dynamicSourcePin, FlowPinDescriptor targetPinDescriptor, boolean dynamicTargetPin) {
        super (representedComponent, descriptorID);
        this.sourcePinDescriptor = sourcePinDescriptor;
        this.dynamicSourcePin = dynamicSourcePin;
        this.targetPinDescriptor = targetPinDescriptor;
        this.dynamicTargetPin = dynamicTargetPin;
    }

    public void update (FlowEdgeDescriptor edge) {
        sourcePinDescriptor = edge.sourcePinDescriptor;
        dynamicSourcePin = edge.dynamicSourcePin;
        targetPinDescriptor = edge.targetPinDescriptor;
        dynamicTargetPin = edge.dynamicTargetPin;
    }

    public FlowPinDescriptor getSourcePinDescriptor () {
        return sourcePinDescriptor;
    }

    public boolean isDynamicSourcePin () {
        return dynamicSourcePin;
    }

    public FlowPinDescriptor getTargetPinDescriptor () {
        return targetPinDescriptor;
    }

    public boolean isDynamicTargetPin () {
        return dynamicTargetPin;
    }

    public interface EdgeDecorator extends Decorator {

        Widget create (FlowEdgeDescriptor descriptor, FlowScene scene);

        void update (FlowEdgeDescriptor descriptor, FlowScene scene);

        void setSourceAnchor (FlowEdgeDescriptor descriptor, FlowScene scene, Anchor sourceAnchor);

        void setTargetAnchor (FlowEdgeDescriptor descriptor, FlowScene scene, Anchor targetAnchor);

    }

    public interface EdgeBehaviour extends Behaviour {

        boolean isSourceReconnectable (FlowEdgeDescriptor descriptor);

        boolean isTargetReconnectable (FlowEdgeDescriptor descriptor);

        boolean isReplacement (FlowEdgeDescriptor descriptor, FlowDescriptor replacementDescriptor, boolean reconnectingSource);

        void setReplacement (FlowEdgeDescriptor descriptor, FlowDescriptor replacementDescriptor, boolean reconnectingSource);

    }

}
