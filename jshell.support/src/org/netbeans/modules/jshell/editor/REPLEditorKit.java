/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jshell.editor;

import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.NbEditorKit;
//import org.netbeans.modules.editor.plain.PlainKit;

/**
 *
 * @author sdedic
 */
@MimeRegistration(service = EditorKit.class, mimeType = "text/x-repl")
public class REPLEditorKit extends NbEditorKit {

    @Override
    public String getContentType() {
        return "text/x-repl"; // NOI18N
    }

    @Override
    public void deinstall(JEditorPane c) {
        super.deinstall(c);
    }

    @Override
    public void install(JEditorPane c) {
        super.install(c);
    }
}
