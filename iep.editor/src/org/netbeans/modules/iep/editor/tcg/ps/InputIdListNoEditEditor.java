package org.netbeans.modules.iep.editor.tcg.ps;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.model.OperatorComponent;

public class InputIdListNoEditEditor extends NoEditEditor {

     public String getAsText() {
         StringBuffer valueBuf = new StringBuffer();
         
         OperatorComponent op = getOperatorComponent();
         List<OperatorComponent> inputs = op.getInputOperatorList();
         Iterator<OperatorComponent> it = inputs.iterator();
         valueBuf.append("[");
         
         while(it.hasNext()) {
             OperatorComponent input = it.next();
             valueBuf.append(input.getDisplayName());
             
             if(it.hasNext()) {
                 valueBuf.append(",");
             }
         }
         
         valueBuf.append("]");
         return valueBuf.toString();
         
     }
}
