package org.netbeans.modules.j2ee.persistence.sourcetestsupport;

import java.util.Enumeration;
import org.netbeans.modules.java.JavaDataLoader;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.util.Enumerations;



// Copied from org.netbeans.modules.j2ee.common.source
public class FakeJavaDataLoaderPool extends DataLoaderPool {
    
    public Enumeration<? extends DataLoader> loaders() {
        return Enumerations.singleton(new JavaDataLoader());
    }
}