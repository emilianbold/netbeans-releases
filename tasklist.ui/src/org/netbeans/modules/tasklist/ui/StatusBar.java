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

package org.netbeans.modules.tasklist.ui;

import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import org.netbeans.modules.tasklist.impl.TaskList;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;

/**
 *
 * @author S. Aubrecht
 */
class StatusBar extends JLabel {

    private TaskList tasks;
    private TaskList.Listener listener;
    
    /** Creates a new instance of StatusBar */
    public StatusBar( TaskList tasks ) {
        this.tasks = tasks;
        listener = new TaskList.Listener() {
            public void tasksAdded(List<? extends Task> tasks) {
                updateText();
            }

            public void tasksRemoved(List<? extends Task> tasks) {
                updateText();
            }

            public void cleared() {
                updateText();
            }
        };
        
        updateText();
    }

    private void updateText() {
        StringBuffer buffer = new StringBuffer();
        for( TaskGroup tg : TaskGroup.getGroups() ) {
            int count = tasks.countTasks( tg );
            if( count > 0 ) {
                if( buffer.length() > 0 )
                    buffer.append( "  " ); //NOI18N
                else 
                    buffer.append( ' ' );
                buffer.append( tg.getDisplayName() );
                buffer.append( ": " ); //NOI18N
                buffer.append( count );
            }
        }
        buffer.append( ' ' );
        setText( buffer.toString() );
    }
    
    public void removeNotify() {
        super.removeNotify();
        
        tasks.removeListener( listener );
    }
    
    public void addNotify() {
        super.addNotify();
        
        tasks.addListener( listener );
    }
}
