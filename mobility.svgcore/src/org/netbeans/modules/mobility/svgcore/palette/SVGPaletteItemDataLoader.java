package org.netbeans.modules.mobility.svgcore.palette;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class SVGPaletteItemDataLoader extends UniFileLoader {
    public static final String REQUIRED_MIME = "text/x-svg-palette-item"; //NOI18N
    private static final long serialVersionUID = 1L;

    public SVGPaletteItemDataLoader() {
        super("org.netbeans.modules.mobility.svgcore.palette.SVGPaletteItemDataObject"); //NOI18N
    }

    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(SVGPaletteItemDataLoader.class, "LBL_SVGPaletteItem_loader_name"); //NOI18N
    }

    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new SVGPaletteItemDataObject(primaryFile, this);
    }

    @Override
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions"; //NOI18N
    }
}