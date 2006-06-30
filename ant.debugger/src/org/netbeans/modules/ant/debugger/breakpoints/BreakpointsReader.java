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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.debugger.breakpoints;

import java.lang.IllegalArgumentException;
import java.lang.IndexOutOfBoundsException;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;


/**
 *
 * @author Jan Jancura
 */
public class BreakpointsReader implements Properties.Reader {
    
    
    public String [] getSupportedClassNames () {
        return new String[] {
            AntBreakpoint.class.getName (), 
        };
    }
    
    public Object read (String typeID, Properties properties) {
        if (!(typeID.equals (AntBreakpoint.class.getName ())))
            return null;
        
        Line line = getLine (
            properties.getString ("url", null),
            properties.getInt ("lineNumber", 1));
        if (line == null) return null;
        return new AntBreakpoint (line);
    }
    
    public void write (Object object, Properties properties) {
        AntBreakpoint b = (AntBreakpoint) object;
        FileObject fo = (FileObject) b.getLine ().getLookup ().
            lookup (FileObject.class);
        try {
            properties.setString ("url", fo.getURL ().toString ());
            properties.setInt (
                "lineNumber", 
                b.getLine ().getLineNumber ()
            );
        } catch (FileStateInvalidException ex) {
            ex.printStackTrace ();
        }
    }
    

    private Line getLine (String url, int lineNumber) {
        FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }
        if (file == null) return null;
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find (file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
        if (dataObject == null) return null;
        LineCookie lineCookie = (LineCookie) dataObject.getCookie
            (LineCookie.class);
        if (lineCookie == null) return null;
        Line.Set ls = lineCookie.getLineSet ();
        if (ls == null) return null;
        try {
            return ls.getCurrent (lineNumber);
        } catch (IndexOutOfBoundsException e) {
        } catch (IllegalArgumentException e) {
        }
        return null;
    }
}
