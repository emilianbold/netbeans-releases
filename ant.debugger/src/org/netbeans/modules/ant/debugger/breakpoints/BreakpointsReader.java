/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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
        
        return new AntBreakpoint (getLine (
            properties.getString ("url", null),
            properties.getInt ("lineNumber", 1)
        ));
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
            return ls.getCurrent (lineNumber - 1);
        } catch (IndexOutOfBoundsException e) {
        } catch (IllegalArgumentException e) {
        }
        return null;
    }
}
