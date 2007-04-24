/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class MIDletEditorOperator extends TopComponentOperator {

    public MIDletEditorOperator(String midletName) {
        super(midletName);
    }
    /**
     * Find midlet operator located certain top component
     * @param midletName name of the top component
     * @return MIDletEditorOperator
     */
    public static MIDletEditorOperator findMIDletEditorOperator(String midletName) {
        StringComparator oldOperator = Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(new DefaultStringComparator(true, true));
        MIDletEditorOperator midletOperator =  new MIDletEditorOperator(midletName);
        Operator.setDefaultStringComparator(oldOperator);
        return midletOperator;        
    }
    
    public void switchToSource() {
         switchToViewByName("Source");        
    }
    
    public void switchToScreen() {
         switchToViewByName("Screen");          
    }
    
    public void switchToFlow() {
         switchToViewByName("Flow");        
    }
    
    public void switchToAnalyze() {
         switchToViewByName("Analyze");
    }
    
    public void switchToViewByName(String viewName) {
        JToggleButtonOperator viewButton = new JToggleButtonOperator(this,viewName); // NOI18N
        
        if(!viewButton.isSelected())
            viewButton.pushNoBlock();            
    }
}
