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

import org.netbeans.modules.web.client.javascript.debugger.api.NbJSContextProviderWrapper;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSFileObjectLocation;
import org.netbeans.modules.web.client.tools.api.JSAbstractLocation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

public final class NbJSFileObjectBreakpoint extends NbJSBreakpoint {
    public static final String PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    
    private Line line;
    private int lineNumber;
    /**
     * Netbeans Javascript Breakpoint
     *
     * @param line
     *            if line is null throws NPE.
     */
    NbJSFileObjectBreakpoint(final Line line) {
        this(createNbJSAbstactLocation(line));
        this.line = line;
        this.lineNumber = getLineNumber(line);
    }
    
    NbJSFileObjectBreakpoint(JSAbstractLocation location ){
        super(location);
    }
    


    public FileObject getFileObject() {
        if( line != null ) {
            DataObject dataObject = (DataObject)line.getLookup().lookup(DataObject.class);
            return dataObject.getPrimaryFile();
        }
        return null;
    }


    private static final JSAbstractLocation createNbJSAbstactLocation ( Line line ) {
        
        if( line == null ){
            throw new NullPointerException("Line can not be null in order to create a NbJSFileObjectBreakpoint");
        }
        
        DataObject dataObject = (DataObject) line.getLookup().lookup(DataObject.class);
        JSAbstractLocation loc = new NbJSFileObjectLocation(dataObject.getPrimaryFile(), line.getLineNumber() + 1);
        return loc;
    }
    
    @Override
    public NbJSFileObjectLocation getLocation() {
        // TODO Auto-generated method stub
        JSAbstractLocation loc = super.getLocation();
        assert loc instanceof NbJSFileObjectLocation;
        return (NbJSFileObjectLocation)loc;
    }
    
    @Override
    public Line getLine() {
        return line;
    }
    
    public void setLine(Line line) {
        Line orLine = line;
        this.line = line;
        if (line == null) {
            throw new NullPointerException("Line is null");
        }
        setLocation( createNbJSAbstactLocation(line) );
        firePropertyChange(PROP_LINE_NUMBER, getLineNumber(), getLineNumber(line));
        lineNumber = getLineNumber(line);
    }
    
    public int getLineNumber() {
         return lineNumber;
    }
    
    static int getLineNumber(Line line) {
        return line.getLineNumber() + 1;
    }
    
    @Override
    public void notifyUpdated(Object source) {
        if(source instanceof Line) {
            //firePropertyChange(PROP_LINE_NUMBER, getLineNumber(), getLineNumber((Line)source));
            line = (Line)source;
            lineNumber = getLineNumber(line);
            location = createNbJSAbstactLocation(line);
        }

        super.notifyUpdated(source);
    }

}
