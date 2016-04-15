/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.otool.debugger.api;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.debugger.Breakpoint;
import org.openide.util.Exceptions;

/**
 *
 * @author Nikolay Koldunov
 */
public class OtoolNativeBreakpoint extends Breakpoint implements java.io.Serializable {
    
    private final int number;
    private volatile boolean enabled = true;
    private final String url;
    private final int lineNumber;
   
    
 
    
    public OtoolNativeBreakpoint(int number, String url, int lineNumber) {
        this.url = url;
        this.number = number;
        this.lineNumber = lineNumber;
    }
    
    
    
    public int getNumber() {
        return number;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getPath() {
        try {
            return new URL (url).getPath();
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return url;
    }
    
    public int getLine() {
        return lineNumber;
    }
    
    public String getIdentity() {   //FIXME maybe we should use Object type for identity column
        return ""+number;
    }

    @Override
    public void disable() {
        enabled = false;
    }

    @Override
    public void enable() {
        enabled = true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
