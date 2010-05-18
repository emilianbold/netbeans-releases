package org.netbeans.modules.iep.editor.palette;

import java.util.List;

import org.netbeans.modules.iep.editor.IEPSettings;
import org.netbeans.spi.palette.PaletteFilter;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class IepPaletteFilter extends PaletteFilter {

    @Override
    public boolean isValidCategory(Lookup lkp) {
        List<String> betaOperatorCategories = IEPSettings.getDefault().getDisabledOperatorCategoriesAsList();
        if(betaOperatorCategories != null) {
            Node n = lkp.lookup(Node.class);
            if(n!= null) {
                String name = n.getName();
                if(betaOperatorCategories.contains(name)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override
    public boolean isValidItem(Lookup lkp) {
        List<String> betaOperators = IEPSettings.getDefault().getDisabledOperatorsAsList();
        if(betaOperators != null) {
            Node n = lkp.lookup(Node.class);
            if(n!= null) {
                String name = n.getName();
                if(betaOperators.contains(name)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    

}
