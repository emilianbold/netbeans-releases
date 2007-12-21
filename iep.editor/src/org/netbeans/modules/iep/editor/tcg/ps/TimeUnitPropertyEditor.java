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

package org.netbeans.modules.iep.editor.tcg.ps;

import org.netbeans.modules.iep.editor.share.SharedConstants;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/*
 * TimeUnitPropertyEditor.java
 *
 * Created on April 26, 2006, 11:05 AM
 *
 * @author Bing Lu
 */

public class TimeUnitPropertyEditor extends EnumPropertyEditor implements SharedConstants {
    private static final Logger mLogger = Logger.getLogger(TimeUnitPropertyEditor.class.getName()); //NOI18N
    
    /**
     * Creates a new instance of FeatureStatusPropertyEditor
     */
    public TimeUnitPropertyEditor() {
        super(new String[]{
                    NbBundle.getMessage(TimeUnitPropertyEditor.class, "TimeUnitPropertyEditor.second"), //NOI18N
                    NbBundle.getMessage(TimeUnitPropertyEditor.class, "TimeUnitPropertyEditor.minute"), //NOI18N
                    NbBundle.getMessage(TimeUnitPropertyEditor.class, "TimeUnitPropertyEditor.hour"), //NOI18N
                    NbBundle.getMessage(TimeUnitPropertyEditor.class, "TimeUnitPropertyEditor.day"), //NOI18N
                    NbBundle.getMessage(TimeUnitPropertyEditor.class, "TimeUnitPropertyEditor.week"), //NOI18N
              },
              new Object[]{
                    TIME_UNIT_SECOND, //NOI18N
                    TIME_UNIT_MINUTE, //NOI18N
                    TIME_UNIT_HOUR, //NOI18N
                    TIME_UNIT_DAY, //NOI18N
                    TIME_UNIT_WEEK, //NOI18N
              }
        );
    }    
}
