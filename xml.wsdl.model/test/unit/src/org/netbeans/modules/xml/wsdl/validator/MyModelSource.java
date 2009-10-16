/*
 * MyModelSource.java
 *
 * Created on February 7, 2007, 12:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.validator;

import java.io.File;
import java.net.URI;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author radval
 */
class MyModelSource extends ModelSource {
         
         private URI mURI;
         
         public MyModelSource(Lookup lookup, boolean editable, URI uri){
            super(lookup, editable);
            this.mURI = uri;
        }
         
        public Lookup getLookup(){
            Lookup l1 = super.getLookup();
            InstanceContent ic = new InstanceContent();
            ic.add(new File(mURI));
            Lookup l2 = new AbstractLookup(ic);

            
            
            ProxyLookup pl = new ProxyLookup(new Lookup[] {l1, l2});
           
            return pl;
                
        } 
     }
