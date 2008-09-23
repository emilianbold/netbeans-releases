/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSContextProviderWrapper;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSSource;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSURILocation;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSFactory;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSEditorUtil;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSUtil;
import org.netbeans.modules.web.client.tools.api.JSAbstractLocation;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

public final class NbJSURIBreakpoint extends NbJSBreakpoint {
    private WeakReference<Line> ownerLineRef;

    /**
     * Netbeans Javascript Breakpoint
     * 
     * @param line
     *            if line is null throws NPE.
     */

    // NbJSURIBreakpoint(final Line line) {
    // super(line);
    // }
    /**
     * This constructor should only be used if you do not have a line.
     * 
     * @param urlPath
     * @param lineNum
     */
    NbJSURIBreakpoint(final String urlPath, final int lineNum) {
        super(createNbJSAbstactLocation(urlPath, lineNum));
    }


    // @Override
    // public JSAbstractLocation createNbJSAbstactLocation(Line line) {
    // DataObject dataObject = (DataObject) line.getLookup().lookup(
    // DataObject.class);
    // return createNbJSAbstactLocation(dataObject.getPrimaryFile().getPath(),
    // line.getLineNumber() + 1);
    // }

    public static final JSAbstractLocation createNbJSAbstactLocation(
            String urlPath, int lineNum) {
        assert checkAbsolute(urlPath);
        JSAbstractLocation loc = new JSURILocation(urlPath, lineNum, 0);
        return loc;
    }
    
    private static boolean checkAbsolute(final String strURL){
        URI uri;
        try {
            uri = new URI(strURL);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return uri.isAbsolute();
    }

    @Override
    public JSURILocation getLocation() {
        // TODO Auto-generated method stub
        JSAbstractLocation loc = super.getLocation();
        assert loc instanceof JSURILocation;
        return (JSURILocation) loc;
    }

    @Override
    public FileObject getFileObject() {
        Session session = DebuggerManager.getDebuggerManager().getCurrentSession();
        if( session == null ){
            return null;
        } 
        DebuggerEngine engine = session.getCurrentEngine();
        if (engine == null ){
            return null;
        }
        return getFileObject(engine);
    }
    
    public void setOwnerLine(Line line) {
        this.ownerLineRef = new WeakReference<Line>(line);
    }
    
    public Line getOwnerLine() {
        return ownerLineRef != null ? ownerLineRef.get() : null;
    }
    
    public FileObject getFileObject(DebuggerEngine engine) {
        assert engine != null;

        FileObject fo = null;
        NbJSDebugger jsDebugger = engine.lookupFirst(null, NbJSDebugger.class);
        if (jsDebugger != null) {
            if (!jsDebugger.isIgnoringQueryStrings() || getLocation().getURI().getQuery() == null) {
                fo = jsDebugger.getURLFileObjectForSource(createJSSource());
            }
        }
        return fo;
    }

    private JSSource createJSSource() {
        URI uri = getLocation().getURI();
        assert uri.isAbsolute();
        return JSFactory.createJSSource(getLocation().getURI().toString());
    }

    public Line getLine() {
        FileObject fo = getFileObject();
        return getLine(fo);
    }
    
    public Line getLine(DebuggerEngine e){
        FileObject fo4Session = getFileObject(e);
        return getLine(fo4Session);
    }
    
    private final Line getLine(FileObject fo){
        if (fo != null) {
            LineCookie lineCookie = NbJSEditorUtil.getLineCookie(fo);
            if (lineCookie == null) {
                return null;
            }
            Line.Set ls = lineCookie.getLineSet();
            if (ls == null) {
                return null;
            }
            try {
                return ls.getCurrent(getLineNumber() - 1);
            } catch (IndexOutOfBoundsException e) {
                NbJSUtil.LOGGER.log(Level.FINE, e.getMessage(), e);
            } catch (IllegalArgumentException e) {
                NbJSUtil.LOGGER.log(Level.FINE, e.getMessage(), e);
            }
        }
        return null;
    }
    
    public void setLine( String uri, int lineNum ){
        JSAbstractLocation orLocation = getLocation();
        JSAbstractLocation newLocation = createNbJSAbstactLocation(uri, lineNum);
        setLocation(newLocation);
        firePropertyChange(Line.PROP_LINE_NUMBER, orLocation, newLocation);
    }

    // XXX this method is never called
    public void setLine(Line line) {
        if( line == null ){
            throw new NullPointerException("Can not set line to null.  Try setLine(uri,lineNum) instead");
        }
        
        Line orLine = line;
        DataObject dataObject = (DataObject) line.getLookup().lookup(
                DataObject.class);
        setLocation(createNbJSAbstactLocation(dataObject.getPrimaryFile()
                .getPath(), line.getLineNumber() + 1)); 
        firePropertyChange(Line.PROP_LINE_NUMBER, orLine, line);
    }

    public int getLineNumber() {
        return getLocation().getLineNumber();
    }
    

    @Override
    protected void firePropertyChange(String name, Object o, Object n) {
        super.firePropertyChange(name, o, n);
        // TODO Auto-generated method stub
        if( getFileObject() == null){
            NbJSContextProviderWrapper.getBreakpointModel().fireChanges();
        } 
    }
    
    @Override
    public int hashCode() {
            int hashCode = 100 * getLocation().getURI().toString().hashCode();
            hashCode += getLineNumber();
            return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !( obj instanceof NbJSURIBreakpoint )){
            return false;
        }
        NbJSURIBreakpoint bp = (NbJSURIBreakpoint)obj;
      
        String strMyURI = getLocation().getURI().toString();
        if( !bp.getLocation().getURI().toString().equals(strMyURI) ){
            return false;
        }
        if (bp.getLineNumber() != getLineNumber()) {
            return false;
        }
        return true;
    }

}
