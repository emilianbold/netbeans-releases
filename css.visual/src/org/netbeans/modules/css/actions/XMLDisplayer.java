/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.css.actions;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.net.*;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.*;
import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.NbBundle;
import org.openide.windows.*;

import org.xml.sax.SAXParseException;


/**
 * Handles output window for XML parser, see display.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class XMLDisplayer {

    //0 extends message, 1 line number, 2 url of external entity
    private final String FORMAT = "{0} [{1}] {2}"; // NOI18N

    /** output tab */
    private InputOutput xmlIO;

    /** writer to that tab */
    private OutputWriter ow = null;

    
    /** Creates new XMLDisplayer */
    public XMLDisplayer() {
        this(NbBundle.getMessage (XMLDisplayer.class, "TITLE_XML_check_window"));
    }

    protected XMLDisplayer(String tab) {
        initInputOutput(tab);
    }

    /**
     * Display plain message in output window.
     */
    public void display(String msg) {
        ow.println(msg);
    }
    
    /**
     * Displayed message may also take focus the window. Sutable for the last message.
     */
    public void display(String msg, boolean takeFocus) {        
        if (takeFocus) {
            boolean wasFocusTaken = xmlIO.isFocusTaken();
            xmlIO.select();
            xmlIO.setFocusTaken(true);
            ow.println(msg);
            xmlIO.setFocusTaken(wasFocusTaken);
        } else {
            ow.println(msg);
        }
    }

    /**
     * Try to move InputOutput to front. Suitable for last message.
     */
    public final void moveToFront() {
        boolean wasFocusTaken = xmlIO.isFocusTaken();
        xmlIO.select();
        xmlIO.setFocusTaken(true);
        ow.write("\r");
        xmlIO.setFocusTaken(wasFocusTaken);        
    }
    
    /** Show using SAX parser error format */
    public void display(DataObject dobj, SAXParseException sex) {
        
        // resolve actual data object that caused exception
        // it may differ from XML document for external entities
        
        DataObject actualDataObject = null;  
        try {
            FileObject fo = URLMapper.findFileObject(new URL(sex.getSystemId()));
            if (fo != null) {
                actualDataObject = DataObject.find(fo);
            }
        } catch (MalformedURLException ex) {
            // we test for null
        } catch (DataObjectNotFoundException ex) {
            // we test for null            
        }

        // external should contain systemID for unresolned external entities
        
        String external = ""; // NOI18N
        
        if (actualDataObject == null) {
            external = sex.getSystemId();
        }
        
        
        display (
            actualDataObject, sex.getMessage(), external,
            new Integer( sex.getLineNumber() ),
            new Integer( sex.getColumnNumber() )
        );
    }


    /** Show it in output tab formatted and with attached  controller. */
    protected void display(DataObject dobj, String message, String ext, Integer line, Integer col) {

        
        Object[] args = new Object[] {
                            message,
                            line,
                            ext
                        };

        String text = MessageFormat.format(FORMAT, args);

        try {
            if (dobj == null) throw new IOException("catchIt"); // NOI18N
            IOCtl ec = new IOCtl (
                           dobj,
                           Math.max(line.intValue() - 1, 0),
                           Math.max(col.intValue() - 1, 0)
                       );
            ow.println(text, ec);
        } catch (IOException catchIt) {
            ow.println(text);     // print without controller         
        }

    }

    /** Set output writer used by this displayer.
    * Share existing, clear content on reuse.
    */
    private void initInputOutput (String name) {
        if (ow != null) return;
        xmlIO = IOProvider.getDefault().getIO(name, false);
        xmlIO.setFocusTaken (false);
        ow = xmlIO.getOut();
        try {
            ow.reset();
        } catch (java.io.IOException ex) {
            //bad luck
        }
    }

    final class IOCtl implements OutputListener {
        /** line we check */
        Line xline;

        /** column with the err */
        int column;

        /**
        * @param fo is a FileObject with an error
        * @param line is a line with the error
        * @param column is a column with the error
        * @param text text to display to status line
        * @exception FileNotFoundException
        */
        public IOCtl (DataObject data, int line, int column)
        throws java.io.IOException {
            this.column = column;
            LineCookie cookie = data.getCookie(LineCookie.class);
            if (cookie == null) {
                throw new java.io.FileNotFoundException ();
            }
            xline = cookie.getLineSet ().getOriginal (line);
        }

        public void outputLineSelected (OutputEvent ev) {
            try {
                xline.markError();
                xline.show(ShowOpenType.NONE, ShowVisibilityType.NONE, column);
            } catch (IndexOutOfBoundsException ex) {
            } catch (ClassCastException ex) {
                // This is hack because of CloneableEditorSupport error -- see CloneableEditorSupport:1193
            }
        }

        public void outputLineAction (OutputEvent ev) {
            try {
                xline.markError();
                xline.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
            } catch (IndexOutOfBoundsException ex) {
            } catch (ClassCastException ex) {
                // This is hack because of CloneableEditorSupport error -- see CloneableEditorSupport:1193
            }
        }

        public void outputLineCleared (OutputEvent ev) {
            try {
                xline.unmarkError();
            } catch (IndexOutOfBoundsException ex) {
            }
        }
    }

}
