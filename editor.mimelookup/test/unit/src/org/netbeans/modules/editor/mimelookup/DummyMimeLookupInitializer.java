/*
 * DummyMimeDataProvider.java
 *
 * Created on June 14, 2006, 3:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.editor.mimelookup;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeLookupInitializer;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author vita
 */
public class DummyMimeLookupInitializer implements MimeLookupInitializer {
    
    /** Creates a new instance of DummyMimeDataProvider */
    public DummyMimeLookupInitializer() {
//        System.out.println("Creating DummyMimeLookupInitializer");
    }

    public Lookup.Result child(String mimeType) {
        return Lookups.singleton(this).lookupResult(MimeLookupInitializer.class);
    }

    public Lookup lookup() {
//        System.out.println("DummyMimeLookupInitializer creating Marker");
        return Lookups.singleton(new Marker());
    }
    
    public static final class Marker {
        
    } // End of Marker class
}
