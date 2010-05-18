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

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.java.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public abstract class CommandProducer extends ComponentProducer {

    public static final String PRODUCER_ID_BACK_COMMAND = "#BackCommand"; // NOI18N
    public static final String PRODUCER_ID_CANCEL_COMMAND = "#CancelCommand"; // NOI18N
    public static final String PRODUCER_ID_EXIT_COMMAND = "#ExitCommand"; //NOI18N
    public static final String PRODUCER_ID_HELP_COMMAND = "#HelpCommand"; //NOI18N
    public static final String PRODUCER_ID_ITEM_COMMAND = "#ItemCommand"; //NOI18N
    public static final String PRODUCER_ID_OK_COMMAND = "#OkCommand"; //NOI18N
    public static final String PRODUCER_ID_SCREEN_COMMAND = "#ScreenCommand"; //NOI18N
    public static final String PRODUCER_ID_STOP_COMMAND = "#StopCommand"; //NOI18N

    private String lower;
    private String upper;
    private int type;

    private CommandProducer(String lower, String upper, int type) {
        super("#" + upper + "Command", CommandCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_COMMANDS, NbBundle.getMessage (CommandProducer.class, "DISP_Command", upper), NbBundle.getMessage (CommandProducer.class, "TTIP_Command", upper), CommandCD.ICON_PATH, CommandCD.LARGE_ICON_PATH)); // NOI18N
        this.lower = lower;
        this.upper = upper;
        this.type = type;
    }

    @Override
    public Result postInitialize (DesignDocument document, DesignComponent mainComponent) {
        mainComponent.writeProperty(ClassCD.PROP_INSTANCE_NAME, InstanceNameResolver.createFromSuggested(mainComponent, lower + "Command")); // NOI18N
        mainComponent.writeProperty(CommandCD.PROP_LABEL, MidpTypes.createStringValue (upper));
        mainComponent.writeProperty(CommandCD.PROP_TYPE, MidpTypes.createIntegerValue(type));
        return new ComponentProducer.Result(mainComponent);
    }

     public Boolean checkValidity(DesignDocument document, boolean useCachedValue) {
        if (useCachedValue) {
            return MidpJavaSupport.getCache(document).checkValidityCached(CommandCD.TYPEID);
        }
        return MidpJavaSupport.checkValidity(document, CommandCD.TYPEID);
    }

    public static final class BackCommand extends CommandProducer { public BackCommand() { super("back", NbBundle.getMessage (CommandProducer.class, "DISP_Command_Back"), CommandCD.VALUE_BACK); } } // NOI18N
    public static final class CancelCommand extends CommandProducer { public CancelCommand() { super("cancel", NbBundle.getMessage (CommandProducer.class, "DISP_Command_Cancel"), CommandCD.VALUE_CANCEL); } } // NOI18N
    public static final class ExitCommand extends CommandProducer { public ExitCommand() { super("exit", NbBundle.getMessage (CommandProducer.class, "DISP_Command_Exit"), CommandCD.VALUE_EXIT); } } // NOI18N
    public static final class HelpCommand extends CommandProducer { public HelpCommand() { super("help", NbBundle.getMessage (CommandProducer.class, "DISP_Command_Help"), CommandCD.VALUE_HELP); } } // NOI18N
    public static final class ItemCommand extends CommandProducer { public ItemCommand() { super("item", NbBundle.getMessage (CommandProducer.class, "DISP_Command_Item"), CommandCD.VALUE_ITEM); } } // NOI18N
    public static final class OkCommand extends CommandProducer { public OkCommand() { super("ok", NbBundle.getMessage (CommandProducer.class, "DISP_Command_Ok"), CommandCD.VALUE_OK); } } // NOI18N
    public static final class ScreenCommand extends CommandProducer { public ScreenCommand() { super("screen", NbBundle.getMessage (CommandProducer.class, "DISP_Command_Screen"), CommandCD.VALUE_SCREEN); } } // NOI18N
    public static final class StopCommand extends CommandProducer { public StopCommand() { super("stop", NbBundle.getMessage (CommandProducer.class, "DISP_Command_Stop"), CommandCD.VALUE_STOP); } } // NOI18N

}
