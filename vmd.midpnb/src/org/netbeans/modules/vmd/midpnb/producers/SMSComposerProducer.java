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

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.java.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.SMSComposerSendCommandCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.SMSComposerCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SMSComposerSendCommandEventSourceCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class SMSComposerProducer extends MidpComponentProducer {
    
    public SMSComposerProducer() {
        super(SMSComposerCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(SMSComposerProducer.class, "DISP_SMS_Composer"), NbBundle.getMessage(SMSComposerProducer.class, "TTIP_SMS_Composer"), SMSComposerCD.ICON_PATH, SMSComposerCD.ICON_LARGE_PATH)); // NOI18N
    }
    
    @Override
    public Result postInitialize (DesignDocument document, DesignComponent smsComposer) {
        DesignComponent sendCommand = MidpDocumentSupport.getSingletonCommand(document, SMSComposerSendCommandCD.TYPEID);
        DesignComponent smsEventSource = document.createComponent(SMSComposerSendCommandEventSourceCD.TYPEID);
        smsEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(smsComposer));
        smsEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(sendCommand));
        MidpDocumentSupport.addEventSource(smsComposer, DisplayableCD.PROP_COMMANDS, smsEventSource);
        smsComposer.writeProperty(SMSComposerCD.PROP_BGK_COLOR, MidpTypes.createIntegerValue(-3355444));
        smsComposer.writeProperty(SMSComposerCD.PROP_FRG_COLOR, MidpTypes.createIntegerValue(-16777216));
        smsComposer.writeProperty(SMSComposerCD.PROP_PHONE_NUMEBR_LABEL, MidpTypes.createStringValue(SMSComposerCD.PHONE_NUMBER_LABEL));
        smsComposer.writeProperty(SMSComposerCD.PROP_MESSAGE_LABEL, MidpTypes.createStringValue(SMSComposerCD.MESSAGE_LABEL));
        return new Result(smsComposer, sendCommand, smsEventSource);
    }
    
    @Override
    public Boolean checkValidity(DesignDocument document, boolean useCachedValue) {
        if (useCachedValue) {
            return MidpJavaSupport.getCache(document).checkValidityCached("javax.wireless.messaging.TextMessage"); // NOI18N
        }
        return MidpJavaSupport.checkValidity(document, "javax.wireless.messaging.TextMessage"); // NOI18N
    }
}
