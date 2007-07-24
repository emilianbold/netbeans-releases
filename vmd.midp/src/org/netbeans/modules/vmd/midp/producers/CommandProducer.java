/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.midp.producers;

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
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

    public Result postInitialize (DesignDocument document, DesignComponent mainComponent) {
        mainComponent.writeProperty(ClassCD.PROP_INSTANCE_NAME, InstanceNameResolver.createFromSuggested(mainComponent, lower + "Command")); // NOI18N
        mainComponent.writeProperty(CommandCD.PROP_LABEL, MidpTypes.createStringValue (upper));
        mainComponent.writeProperty(CommandCD.PROP_TYPE, MidpTypes.createIntegerValue(type));
        return new ComponentProducer.Result(mainComponent);
    }

    public boolean checkValidity(DesignDocument document) {
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
