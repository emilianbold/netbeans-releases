/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import java.io.IOException;
import java.io.File;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;

import org.openide.*;
import org.openide.loaders.DataObject;
import org.openide.filesystems.*;
import org.openide.util.*;

import org.netbeans.modules.xml.core.lib.GuiUtil;

/**
 * Extremely simple dialog with one input line. Invoke using
 * {@link #getFileObject} method.
 */
public final class SelectFileDialog {

    private Util.NameCheck check;

    private Prompt selectDD;
    private FileObject folder;
    private String ext;

    /**
     *
     * @param folder parent folder that will host created file
     * @param name default file name
     * @param ext default file.extension
     */
    public SelectFileDialog (FileObject folder, String name, String ext) {

        this (folder, name, ext, Util.JAVA_CHECK);
    }

    public SelectFileDialog (FileObject folder, String name, String ext, Util.NameCheck check) {
        this.folder = folder;
        this.ext = ext;
        this.check = check;
        this.selectDD = new Prompt (
                Util.THIS.getString ("PROP_fileNameTitle") + " *." + ext, // NOI18N
                Util.THIS.getString("PROP_fileName"),
                name
        );
    }

    /**
     * Get file object that have user selected
     * @throws IOException if cancelled or invalid data entered
     */
    public FileObject getFileObject () throws IOException {
        FileObject newFO = null;

        while ( newFO == null ) {
            DialogDisplayer.getDefault().notify(selectDD);
            if (selectDD.getValue() != NotifyDescriptor.OK_OPTION) {
                throw new UserCancelException();
            }
            final String newName = selectDD.getInputText();

            newFO = folder.getFileObject (newName, ext);
        
            if ( ( newFO == null ) ||
                 ( newFO.isVirtual() == true ) ) {

                FileSystem fs = folder.getFileSystem();
                final FileObject tempFile = newFO;
                
                fs.runAtomicAction (new FileSystem.AtomicAction () {
                        public void run () throws IOException {

                            if ( ( tempFile != null ) &&
                                 tempFile.isVirtual() ) {
                                tempFile.delete();
                            }

                            try {
                                folder.createData (newName, ext);
                            } catch (IOException exc) {
                                NotifyDescriptor desc = new NotifyDescriptor.Message
                                    (Util.THIS.getString ("MSG_cannot_create_data", newName + "." + ext), NotifyDescriptor.WARNING_MESSAGE);
                                DialogDisplayer.getDefault().notify (desc);

                                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (exc);
                            }
                        }
                    });
                
                newFO = folder.getFileObject (newName, ext);

            } else if (newFO != null) {
                DataObject data = DataObject.find(newFO);
                if (data.isModified() || data.isValid() == false) {
                    NotifyDescriptor message = new NotifyDescriptor.Message(Util.THIS.getString("BK0001"), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(message);
                    throw new UserCancelException();
                } else if (! GuiUtil.confirmAction (Util.THIS.getString ("PROP_replaceMsg",
                                                                newName, ext ) )) {
                    throw new UserCancelException();
                }
            }
        } // while

        return newFO;
    }


    /** One input line, verified on content change. */
    private class Prompt extends NotifyDescriptor.InputLine implements DocumentListener{

        public Prompt(String title, String label, String text) {
            super(label, title);
            setInputText(text);
            textField.getDocument().addDocumentListener(this);
        }

        public void changedUpdate(DocumentEvent e) {
            verifyInput();
        }

        public void insertUpdate(DocumentEvent e) {
            verifyInput();
        }

        public void removeUpdate(DocumentEvent e) {
            verifyInput();
        }

        private void verifyInput() {
            String typedText = textField.getText();
            // no relative paths allowed #24693
            if (typedText.indexOf(File.separatorChar) != -1) {
                selectDD.setValid(false);
            } else {
                selectDD.setValid(check.checkName(typedText));
            }
        }

    }
    
}
