/*
 * BreakpointsReader.java
 *
 * Created on November 28, 2005, 6:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.python.debugger.breakpoints;

import org.netbeans.api.debugger.Properties;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.filesystems.URLMapper;
import java.net.URL;
import java.lang.IllegalArgumentException;
import java.lang.IndexOutOfBoundsException;
import java.net.MalformedURLException;
import org.openide.loaders.DataObject;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObjectNotFoundException;



/**
 *
 * @author jean-yves
 */
public class BreakpointsReader
implements Properties.Reader 
{
  
  /** Creates a new instance of BreakpointsReader */
  public BreakpointsReader() {}

    public String [] getSupportedClassNames () 
    {
      return new String[] {
            PythonBreakpoint.class.getName (), 
      };
    }
  
  public void write (Object object, Properties properties) 
  {
  PythonBreakpoint b = (PythonBreakpoint) object;
    if ( ( b!= null ) &&
         ( b.getLine () != null ) 
       )
    {  
      FileObject fo = (FileObject) b.getLine ().getLookup ().lookup (FileObject.class);
      try 
      {
        properties.setString ("url", fo.getURL ().toString ());
        properties.setInt (
                  "lineNumber", 
                  b.getLine ().getLineNumber ()
              );
      } catch (FileStateInvalidException ex) {
        ex.printStackTrace ();
      }
    }  
  }
    
  public Object read (String typeID, Properties properties) 
  {
    if (!(typeID.equals (PythonBreakpoint.class.getName ())))
      return null;
        
    return new PythonBreakpoint ( getLine (
                                 properties.getString ("url", null),
                                 properties.getInt ("lineNumber", 1)
                               ));
  }

  private Line getLine (String url, int lineNumber) 
  {
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
    } 
    catch (IndexOutOfBoundsException e) {} 
    catch (IllegalArgumentException e) {
    }
    return null;
    }  
}
