package org.netbeans.modules.tbls.editor.ps;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.share.SharedConstants;

public class InputIdListNoEditEditor extends NoEditEditor implements SharedConstants {

     public String getAsText() {
         StringBuffer valueBuf = new StringBuffer();
         
         OperatorComponent op = getOperatorComponent();
         List<OperatorComponent> inputs = op.getInputOperatorList();
         Iterator<OperatorComponent> it = inputs.iterator();
         valueBuf.append("[");
         
         while(it.hasNext()) {
             OperatorComponent input = it.next();
             valueBuf.append(input.getString(PROP_NAME));
             
             if(it.hasNext()) {
                 valueBuf.append(",");
             }
         }
         
         valueBuf.append("]");
         return valueBuf.toString();
         
     }
}
