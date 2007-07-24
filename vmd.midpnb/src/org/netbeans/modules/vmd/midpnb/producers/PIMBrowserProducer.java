/*
 * FileBrowserProducer.java
 *
 * Created on February 2, 2007, 9:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.vmd.midpnb.producers;

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
import org.netbeans.modules.vmd.midpnb.components.commands.PIMBrowserOpenCommandCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.PIMBrowserCD;
import org.netbeans.modules.vmd.midpnb.components.sources.PIMBrowserOpenCommandEventSourceCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class PIMBrowserProducer extends MidpComponentProducer {
    
    public PIMBrowserProducer() {
        super(PIMBrowserCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(PIMBrowserProducer.class, "DISP_PIM_Browser"), NbBundle.getMessage(PIMBrowserProducer.class, "TTIP_PIM_Browser"), PIMBrowserCD.ICON_PATH, PIMBrowserCD.ICON_LARGE_PATH)); // NOI18N
    }

    public Result postInitialize (DesignDocument document, DesignComponent pimBrowser) {
        DesignComponent openCommand = MidpDocumentSupport.getSingletonCommand(document, PIMBrowserOpenCommandCD.TYPEID);
        DesignComponent openEventSource = document.createComponent(PIMBrowserOpenCommandEventSourceCD.TYPEID);
        openEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(pimBrowser));
        openEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(openCommand));
        MidpDocumentSupport.addEventSource(pimBrowser, DisplayableCD.PROP_COMMANDS, openEventSource);
        
        return new Result(pimBrowser, openCommand, openEventSource);
    }
    
    public boolean checkValidity(DesignDocument document) {
        return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.List"); // NOI18N
    }
}
