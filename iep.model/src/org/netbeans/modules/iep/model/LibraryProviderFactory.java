package org.netbeans.modules.iep.model;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.iep.model.spi.LibraryProvider;
import org.openide.util.Lookup;


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
        Lookup.Result results = Lookup.getDefault().lookup(new Lookup.Template(LibraryProvider.class));
        for (Object service : results.allInstances()){
        	LibraryProvider provider = (LibraryProvider) service;
        	mLibraryProviders.add(provider);
        }
    }
	
	public List<LibraryProvider> getAlLibraryProvider() {
		return mLibraryProviders;
	}
}
