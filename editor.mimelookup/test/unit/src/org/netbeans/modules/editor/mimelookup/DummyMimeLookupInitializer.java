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
    
    private final MimePath mimePath;
    
    /** Creates a new instance of DummyMimeDataProvider */
    public DummyMimeLookupInitializer() {
//        System.out.println("Creating DummyMimeLookupInitializer");
        this.mimePath = MimePath.EMPTY;
    }

    private DummyMimeLookupInitializer(MimePath mimePath) {
        this.mimePath = mimePath;
    }
    
    public Lookup.Result child(String mimeType) {
        return Lookups.singleton(new DummyMimeLookupInitializer(MimePath.get(mimePath, mimeType))).lookupResult(MimeLookupInitializer.class);
    }

    public Lookup lookup() {
//        System.out.println("DummyMimeLookupInitializer creating Marker");
        if (mimePath.size() == 1) {
            return Lookups.singleton(new Marker());
        } else {
            return null;
        }
    }
    
    public static final class Marker {
        
    } // End of Marker class
}
