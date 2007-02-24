/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.modules.uml.ui.support.helpers;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;

import java.awt.Cursor;
import java.awt.Frame;

/**
 * @author KevinM
 *
 * Displays the hourglass curor on the main window until stop is called.
 */
public class ETSmartWaitCursor
{
    protected static int m_refCount = 0;
    
    Frame frame = null;
    boolean m_running = false;
    
    public ETSmartWaitCursor()
    {
        super();
        start();
    }
    
    public static boolean inWaitState()
    {
        return (m_refCount > 0);
    }
    
    
    protected void start()
    {
        m_running = true;
        m_refCount++;
        
        restore();
    }
    
    /**
     * To restore the wait cursor, call this function after performing an operation,
     * such as displaying a message box or dialog box, which might change the wait cursor to another cursor.
     *
     */
    public void restore()
    {
        if( null == frame )
        {
            frame = ProductHelper.getWindowHandle();
        }
        
        if (frame != null)
        {
            frame.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ));
        }
    }
    
    public void stop()
    {
        if( m_running )
        {
//         m_running = false;
            
            if ( --m_refCount <= 0 && frame!=null )
            {
                frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                m_running = false;
            }
            else
            {
                restore();
            }
        }
        else
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
        /*
         * Make sure they don't leave it in wait mode if an exception happens while the cursor is displaying.
         */
    public void finalize()
    {
        try
        {
            stop();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
