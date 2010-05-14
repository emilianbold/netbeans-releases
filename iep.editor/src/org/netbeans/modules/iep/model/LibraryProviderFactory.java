package org.netbeans.modules.iep.model;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.tbls.model.LibraryProvider;

public class LibraryProviderFactory {

    private List<LibraryProvider> mLibraryProviders = new ArrayList<LibraryProvider>();
    
    private static LibraryProviderFactory mInstance;
    
    private LibraryProviderFactory() {
        initialize();
    }
    
    public static LibraryProviderFactory getDefault() {
        
        if(mInstance == null) {
            mInstance = new LibraryProviderFactory();
        }
        
        return mInstance;
    }
    
    private void initialize() {
        mLibraryProviders.add(new DefaultLibraryProvider());
        mLibraryProviders.add(new MetadataLibraryProvider());
    }
    
    public List<LibraryProvider> getAlLibraryProvider() {
        return mLibraryProviders;
    }
}
