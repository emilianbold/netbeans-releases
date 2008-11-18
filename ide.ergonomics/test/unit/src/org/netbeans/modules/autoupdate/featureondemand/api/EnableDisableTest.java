package org.netbeans.modules.autoupdate.featureondemand.api;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

public final class EnableDisableTest extends NbTestCase {

    public EnableDisableTest(String n) {
        super(n);
    }
    
    @Override
    protected void setUp() throws Exception {
        Logger.getLogger("org.netbeans.core.startup").setLevel(Level.OFF);
        URI uri = ModuleInfo.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        File jar = new File(uri);
        System.setProperty("netbeans.home", jar.getParentFile().getParent());
        System.setProperty("netbeans.user", getWorkDirPath());
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    public void testLayerForAutoupdateUIDisabled() throws Exception {
        File dbp = new File(getWorkDir(), "dbproject");
        File db = new File(dbp, "project.properties");
        dbp.mkdirs();
        db.createNewFile();
        
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject folder = fs.findResource("Actions/Test");
        
        boolean found = false;
        for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (info.getCodeNameBase().equals("org.netbeans.modules.autoupdate.ui")) {
                assertTrue("Module is enabled", info.isEnabled());
                found = true;
            }
        }
        if (!found) {
            fail("Not found autoupdate.ui");
        }
        
        assertNull("testing layer is not loaded, as the module is enabled: ", folder);

    }
    
}


