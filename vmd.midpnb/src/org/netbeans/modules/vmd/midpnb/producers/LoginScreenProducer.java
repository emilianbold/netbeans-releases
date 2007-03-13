/*
 * FileBrowserProducer.java
 *
 * Created on February 2, 2007, 9:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.vmd.midpnb.producers;

import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.producers.MidpComponentProducer;
import org.netbeans.modules.vmd.midpnb.components.commands.LoginScreenLoginCommandCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.LoginScreenCD;
import org.netbeans.modules.vmd.midpnb.components.sources.LoginScreenLoginCommandEventSourceCD;

/**
 *
 * @author Karol Harezlak
 */
public class LoginScreenProducer extends MidpComponentProducer {
    
    public LoginScreenProducer() {
        super(LoginScreenCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, "Login Screen", "Login Screen", LoginScreenCD.ICON_PATH, LoginScreenCD.ICON_LARGE_PATH)); // NOI18N
    }
    
    public Result createComponent(DesignDocument document) {
        DesignComponent loginScreen = document.createComponent(LoginScreenCD.TYPEID);
        DesignComponent loginCommand = MidpDocumentSupport.getSingletonCommand(document, LoginScreenLoginCommandCD.TYPEID);
        DesignComponent loginEventSource = document.createComponent(LoginScreenLoginCommandEventSourceCD.TYPEID);
        loginEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(loginScreen));
        loginEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(loginCommand));
        MidpDocumentSupport.addEventSource(loginScreen, DisplayableCD.PROP_COMMANDS, loginEventSource);
        
        return new Result(loginScreen, loginCommand, loginEventSource);
    }
    
    public boolean checkValidity(DesignDocument document) {
        return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
    }
}
