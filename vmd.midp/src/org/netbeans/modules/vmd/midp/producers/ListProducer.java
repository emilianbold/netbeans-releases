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
package org.netbeans.modules.vmd.midp.producers;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.commands.ListSelectCommandCD;
import org.netbeans.modules.vmd.midp.components.sources.ListSelectCommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.handlers.ListEventHandlerCD;
import org.netbeans.modules.vmd.midp.components.displayables.ListCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public class ListProducer extends MidpComponentProducer {

    public ListProducer () {
        super (ListCD.TYPEID, new PaletteDescriptor (MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(ListProducer.class, "DISP_List"), NbBundle.getMessage(ListProducer.class, "TTIP_List"), ListCD.ICON_PATH, ListCD.ICON_LARGE_PATH)); // NOI18N
    }

    @Override
    public Result postInitialize (DesignDocument document, DesignComponent list) {
        DesignComponent listSelectCommand = MidpDocumentSupport.getSingletonCommand (document, ListSelectCommandCD.TYPEID);

        DesignComponent listSelectCommandEventSource = document.createComponent (ListSelectCommandEventSourceCD.TYPEID);
        listSelectCommandEventSource.writeProperty (CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference (list));
        listSelectCommandEventSource.writeProperty (CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference (listSelectCommand));
        MidpDocumentSupport.addEventSource (list, DisplayableCD.PROP_COMMANDS, listSelectCommandEventSource);

        DesignComponent listEventHandler = document.createComponent (ListEventHandlerCD.TYPEID);
        MidpDocumentSupport.updateEventHandlerWithNew (listSelectCommandEventSource, listEventHandler);
        list.writeProperty(ListCD.PROP_SELECT_COMMAND, PropertyValue.createComponentReference (listSelectCommandEventSource));
        
        return new Result (list, listSelectCommandEventSource, listEventHandler);
    }

}
