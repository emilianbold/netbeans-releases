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
import org.netbeans.modules.vmd.midpnb.components.commands.FileBrowserOpenCommandCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.FileBrowserCD;
import org.netbeans.modules.vmd.midpnb.components.sources.FileBrowserOpenCommandEventSourceCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class FileBrowserProducer extends MidpComponentProducer {
    
    public FileBrowserProducer() {
        super(FileBrowserCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, NbBundle.getMessage(FileBrowserProducer.class, "DISP_File_Browser"), NbBundle.getMessage(FileBrowserProducer.class, "TTIP_File_Browser"), FileBrowserCD.ICON_PATH, FileBrowserCD.ICON_LARGE_PATH)); // NOI18N
    }

    @Override
    public Result postInitialize (DesignDocument document, DesignComponent fileBrowser) {
        DesignComponent openCommand = MidpDocumentSupport.getSingletonCommand(document, FileBrowserOpenCommandCD.TYPEID);
        DesignComponent openEventSource = document.createComponent(FileBrowserOpenCommandEventSourceCD.TYPEID);
        openEventSource.writeProperty(CommandEventSourceCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference(fileBrowser));
        openEventSource.writeProperty(CommandEventSourceCD.PROP_COMMAND, PropertyValue.createComponentReference(openCommand));
        MidpDocumentSupport.addEventSource(fileBrowser, DisplayableCD.PROP_COMMANDS, openEventSource);
        
        return new Result(fileBrowser, openCommand, openEventSource);
    }
    
    @Override
    public boolean checkValidity(DesignDocument document) {
        return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.List"); // NOI18N
    }
}
