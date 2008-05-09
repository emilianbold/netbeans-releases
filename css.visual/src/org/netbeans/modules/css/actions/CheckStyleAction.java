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

import java.awt.event.ActionEvent;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;

import org.openide.*;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.filesystems.*;
import org.openide.cookies.*;

import org.w3c.css.sac.*;

import org.netbeans.modules.css.*;

/**
 * Action that reparses stylesheet and reports any syntax errors.
 *
 * @author Petr Kuzel
 * @author Marek Fukala
 */
public class CheckStyleAction extends BaseAction implements ErrorHandler, DocumentHandler {
    
    public static final String checkStyleAction =  NbBundle.getMessage(CheckStyleAction.class, "NAME_check_CSS"); // NOI18N
    
    public CheckStyleAction() {
        super(checkStyleAction);
            putValue("helpID", CheckStyleAction.class.getName()); // NOI18N
            putValue(SHORT_DESCRIPTION, checkStyleAction);
            putValue(ICON_RESOURCE_PROPERTY, "org/netbeans/modules/css/resources/checkStyleAction.gif"); // NOI18N
    }
    
    //check status
    private boolean failed;
    private int warnings;
    
    private DataObject csso;
    private CSSDisplayer disp;      //and its displayer
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        
        failed = false;
        warnings = 0;
        
        disp = new CSSDisplayer();
        
        Parser parser = new org.w3c.flute.parser.Parser();
        parser.setErrorHandler(this);
        parser.setDocumentHandler(this);
        
        BaseDocument bdoc = Utilities.getDocument(target);
        if(bdoc == null) {
            return ; //no document?!?!
        }
        csso = NbEditorUtilities.getDataObject(bdoc);
        if(csso == null) {
            return ; //document not backuped by DataObject
        }
        
        try {
            //save it first
            SaveCookie cake = csso.getCookie(SaveCookie.class);
            if (cake != null)
                cake.save();
            
            String uri = csso.getPrimaryFile().getURL().toExternalForm();
            
            parser.parseStyleSheet(uri);
        } catch (IOException ex) {
            // ??? provide better feedback TopManager.getDefault().getErrorManager().notify(ex);
            failed = true;
        } catch (CSSParseException ex) {
            // ??? provide better feedback
            failed = true;
        } catch (Throwable t) {
            disp.display("Unexpected exception from CSS parser: " + t.getMessage()); //NOI18N
            Logger.global.log(Level.INFO, "Unexpected exception from CSS parser", t); //NOI18N
            failed = true;
        }
        
        if ( ( failed == true ) ||
                ( warnings > 0 ) ) {
            disp.moveToFront();
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage (XMLDisplayer.class, "TEXT_PART_CSS_checking") + " " + getStatus() + "."); // NOI18N
    }
    
    
    private String getStatus() {
        return failed ? NbBundle.getMessage (XMLDisplayer.class, "TEXT_PART_failed") : warnings>1 ? NbBundle.getMessage (XMLDisplayer.class, "TEXT_PART_finished_with_warnings") : NbBundle.getMessage (XMLDisplayer.class, "TEXT_PART_finished");
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~ PARSER LISTENER ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public void warning(CSSParseException exception) throws CSSException {
        disp.display(csso, exception);
        warnings++;
    }
    
    public void error(CSSParseException exception) throws CSSException {
        disp.display(csso, exception);
        failed = true;
    }
    
    public void fatalError(CSSParseException exception) throws CSSException {
        disp.display(csso, exception);
        failed = true;
    }
    
    // ~~~~~~~~~~~~~~~~~ VOID ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
    
    public void startDocument(InputSource source) throws CSSException {
    }
    
    public void endDocument(InputSource source) throws CSSException {
    }
    
    public void comment(String text) throws CSSException {
    }
    
    public void ignorableAtRule(String atRule) throws CSSException {
    }
    
    public void namespaceDeclaration(String prefix,String uri) throws CSSException {
    }
    
    public void importStyle(String uri,SACMediaList media,String defaultNamespaceURI) throws CSSException {
    }
    
    public void startMedia(SACMediaList media) throws CSSException {
    }
    
    public void endMedia(SACMediaList media) throws CSSException {
    }
    
    public void startPage(String name,String pseudo_page) throws CSSException {
    }
    
    public void endPage(String name,String pseudo_page) throws CSSException {
    }
    
    public void startFontFace() throws CSSException {
    }
    
    public void endFontFace() throws CSSException {
    }
    
    public void startSelector(SelectorList selectors) throws CSSException {
    }
    
    public void endSelector(SelectorList selectors) throws CSSException {
    }
    
    public void property(String name,LexicalUnit value,boolean important) throws CSSException {
    }
}
