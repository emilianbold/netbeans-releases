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

package org.netbeans.modules.tasklist.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import org.netbeans.spi.tasklist.Task;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
public class OpenTaskAction extends AbstractAction {
    
    private Task task;
    
    /** Creates a new instance of OpenTaskAction */
    public OpenTaskAction( Task task ) {
        super( NbBundle.getMessage( OpenTaskAction.class, "LBL_ShowSource" ) ); //NOI18N
        assert null != task;
        this.task = task;
        setEnabled( canOpenTask() );
    }
    
    public void actionPerformed( ActionEvent e ) {
        if( !canOpenTask() )
            return;
        
        ActionListener al = Accessor.getActionListener( task );
        if( null != al ) {
            al.actionPerformed( e );
            return;
        }
        
        FileObject fileObject = Accessor.getResource( task );
        int line = Accessor.getLine( task )-1;
        
        /* Find a DataObject for the FileObject: */
        final DataObject dataObject;
        try {
            dataObject = DataObject.find(fileObject);
        } catch( DataObjectNotFoundException donfE ) {
            return;
        }

        LineCookie lineCookie = (LineCookie)dataObject.getCookie( LineCookie.class );
        if( null != lineCookie && openAt( lineCookie, line ) ) {
            return;
        }
        
        EditCookie editCookie = (EditCookie)dataObject.getCookie( EditCookie.class );
        if( null != editCookie ) {
            editCookie.edit();
            return;
        }
        
        OpenCookie openCookie = (OpenCookie)dataObject.getCookie( OpenCookie.class );
        if( null != openCookie ) {
            openCookie.open();
            return;
        }
        
        ViewCookie viewCookie = (ViewCookie)dataObject.getCookie( ViewCookie.class );
        if( null != viewCookie ) {
            viewCookie.view();
            return;
        }
    }
    
    private boolean openAt( LineCookie lineCookie, int lineNo ) {
        Line.Set lines = lineCookie.getLineSet();
        try {
            Line line = lines.getCurrent( lineNo );
            if( null == line )
                line = lines.getCurrent( 0 );
            if( null != line ) {
                line.show( Line.SHOW_TOFRONT );
                return true;
            }
        } catch( IndexOutOfBoundsException e ) {
            //probably the document has been modified but not saved yet
        }
        return false;
    }

    private boolean canOpenTask() {
        if( null != Accessor.getActionListener( task ) )
            return true;
        
        FileObject fo = Accessor.getResource( task );
        if( null == fo )
            return false;
        
        DataObject dob = null;
        try {
            dob = DataObject.find( fo );
        } catch( DataObjectNotFoundException donfE ) {
            return false;
        }
        if( Accessor.getLine( task ) > 0 ) {
            return null != dob.getCookie( LineCookie.class );
        }
        
        return null != dob.getCookie( OpenCookie.class ) 
            || null != dob.getCookie( EditCookie.class )
            || null != dob.getCookie( ViewCookie.class );
    }
}
