package org.netbeans.modules.iep.editor.designer;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.IOType;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.iep.model.share.SharedConstants;

public class OperatorLinkValidator implements SharedConstants {

    public static boolean validateLink(OperatorComponent from, OperatorComponent to) {
        
        //same operator both from and to
        if(from.equals(to)) {
            return false;
        }
        
        //check if there is already a link between from and to
        IEPModel model = from.getModel();
        if(model != null) {
            PlanComponent plan = model.getPlanComponent();
            if(plan != null) {
                LinkComponentContainer lc = plan.getLinkComponentContainer();
                if(lc != null) {
                    LinkComponent link = lc.findLink(from, to);
                    if(link != null) {
                        return false;
                    }
                }
            }
        }
        
        //if from is table
        int maxTableInputAllowed = to.getInt(PROP_STATIC_INPUT_MAX_COUNT);
        if (from.getOutputType().equals(IOType.TABLE)) {
            if (to.getStaticInputList().size() >= maxTableInputAllowed) {
                return false;
            }
        } else if (from.getOutputType().equals(IOType.RELATION) && to.getBoolean(PROP_IS_RELATION_INPUT_STATIC)) {
            if (to.getStaticInputList().size() >= maxTableInputAllowed) {
                return false;
            }
        } else {
            //from is operator which is not table
            
            // Check input count
            int maxInputAllowed = to.getInt(PROP_INPUT_MAX_COUNT);
            if (to.getInputOperatorList().size() >= maxInputAllowed) {
                return false;
            }
           
            // Check type compatibility
            if (!to.getInputType().equals(from.getOutputType())) {
                return false;
            }
        }
        
        return true;
    }
}
