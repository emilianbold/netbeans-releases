/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.soa.palette.java;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.palette.PaletteFilter;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


/**
 *
 * @author gpatil
 */
public class OTDPaletteFilter extends PaletteFilter {
    private static final String J2SE_PROJECT = "org.netbeans.modules.java.j2seproject.J2SEProject"; //NOI18N
    private static final String EJB_PROJECT = "org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject"; //NOI18N
    private static final String WEB_PROJECT = "org.netbeans.modules.web.project.WebProject"; //NOI18N
    
            
    public OTDPaletteFilter() {
    }

    public boolean isValidCategory(Lookup lkp) {
        if (isValidProjectForCategory(lkp)){
            return true;
        } else {
            return false;
        }
        
    }

    public boolean isValidItem(Lookup lkp) {
        return isValidFileOpened();
    }

    private boolean isValidProjectForCategory(Lookup lkp){
        boolean ret = true;
        String cat = ""; 
        if (!isValidFileOpened()){
            return false;
        }
        
        Node node = lkp.lookup(Node.class);
        if (node != null){
            cat = node.getName();
        }
        
        // JAXB
        if ("JAXB".equals(cat)){ //NOI18N
            String pt = getProjectType();
            if (J2SE_PROJECT.equals(pt) || 
               WEB_PROJECT.equals(pt) || 
               EJB_PROJECT.equals(pt)){
                   return true;
            } else {
                return false;
            }
        }
        
        // POJO
        if ("OpenESB_POJO_Engine".equals(cat)){ //NOI18N
            String pt = getProjectType();
            if (J2SE_PROJECT.equals(pt)){
                   return true;
            } else {
                return false;
            }
        }
        
        // JCA
        if ("JCA".equals(cat)){ //NOI18N
            String pt = getProjectType();
            if (EJB_PROJECT.equals(pt)|| 
               WEB_PROJECT.equals(pt)){
                   return true;
            } else {
                return false;
            }
        }
        
        // JMS 
        if ("JMS".equals(cat)){ //NOI18N
            String pt = getProjectType();
            if (EJB_PROJECT.equals(pt)|| 
               WEB_PROJECT.equals(pt)){
                   return true;
            } else {
                return false;
            }
        }
        
        // Mapper Tedmet        
        if ("Tedmet".equals(cat)){ //NOI18N
            String pt = getProjectType();
            if (J2SE_PROJECT.equals(pt) || 
               WEB_PROJECT.equals(pt) || 
               EJB_PROJECT.equals(pt)){
                   return true;
            } else {
                return false;
            }
        }
        
        return ret;
    }
    
    private String getProjectType(){
        String ret = "" ;
        TopComponent.Registry registry = TopComponent.getRegistry();
        TopComponent tc = registry.getActivated();
        if(tc != null) {
            Lookup lkup = tc.getLookup();
            if(lkup != null) {
                DataObject dObj = lkup.lookup(DataObject.class);
                if(dObj != null) {
                    FileObject fObj = dObj.getPrimaryFile();
                    Project project = FileOwnerQuery.getOwner(fObj);
                    if(project != null) {
                        ret = project.getClass().getName();
                    }                                    
                }                    
            }
        }
        return ret;
    }
    
    private boolean isValidFileOpened() {
        boolean result = false;
        
        TopComponent.Registry registry = TopComponent.getRegistry();
        TopComponent tc = registry.getActivated();
        if(tc != null) {
            Lookup lkup = tc.getLookup();
            if(lkup != null) {
                DataObject dObj = lkup.lookup(DataObject.class);
                if(dObj != null) {
                    String ext = dObj.getPrimaryFile().getExt();
                    if(ext != null) {
                        ext = ext.toLowerCase();
                        if(ext.equals("java") ) { // NOI18N
                            return true;
                        }
                    }
                }
            }
        }
      
        return result;
    }
}