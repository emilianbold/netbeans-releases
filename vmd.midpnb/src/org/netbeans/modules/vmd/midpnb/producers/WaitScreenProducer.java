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
package org.netbeans.modules.vmd.midpnb.producers;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.java.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.WaitScreenFailureCommandCD;
import org.netbeans.modules.vmd.midpnb.components.commands.WaitScreenSuccessCommandCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.WaitScreenCD;
import org.netbeans.modules.vmd.midpnb.components.sources.WaitScreenFailureCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.sources.WaitScreenSuccessCommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class WaitScreenProducer extends MidpComponentProducer {
   
    public WaitScreenProducer() {
       super(WaitScreenCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(WaitScreenProducer.class, "DISP_Wait_Screen"), NbBundle.getMessage(WaitScreenProducer.class, "TTIP_Wait_Screen"), WaitScreenCD.ICON_PATH, WaitScreenCD.ICON_LARGE_PATH)); // NOI18N
    }

    @Override
    public Result postInitialize (DesignDocument document, DesignComponent waitScreen) {
        return produceWaitScreen (document, waitScreen, true);
    }

    public static Result produceWaitScreen (DesignDocument document, DesignComponent waitScreen, boolean createTask) {
        DesignComponent successCommand = MidpDocumentSupport.getSingletonCommand (document, WaitScreenSuccessCommandCD.TYPEID);
        DesignComponent failureCommand = MidpDocumentSupport.getSingletonCommand (document, WaitScreenFailureCommandCD.TYPEID);

        DesignComponent successEventSource = document.createComponent(WaitScreenSuccessCommandEventSourceCD.TYPEID);
        DesignComponent failureEventSource = document.createComponent(WaitScreenFailureCommandEventSourceCD.TYPEID);

        successEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(waitScreen));
        successEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(successCommand));

        failureEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(waitScreen));
        failureEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(failureCommand));

        MidpDocumentSupport.addEventSource(waitScreen, DisplayableCD.PROP_COMMANDS, successEventSource);
        MidpDocumentSupport.addEventSource(waitScreen, DisplayableCD.PROP_COMMANDS, failureEventSource);

        if (createTask) {
            DesignComponent task = document.createComponent (SimpleCancellableTaskCD.TYPEID);
            MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID).addComponent (task);
            waitScreen.writeProperty (WaitScreenCD.PROP_TASK, PropertyValue.createComponentReference (task));

            return new Result (waitScreen, successCommand, failureCommand, successEventSource, failureEventSource, task);
        } else
            return new Result (waitScreen, successCommand, failureCommand, successEventSource, failureEventSource);
    }

    @Override
    public Boolean checkValidity(DesignDocument document, boolean useCachedValue) {
        if (useCachedValue) {
            return MidpJavaSupport.getCache(document).checkValidityCached("javax.microedition.lcdui.Canvas"); // NOI18N
        }
        return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
    }
    
}
