/*
 * Copyright (c) 2007, Sun Microsystems, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems, Inc. nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * DocumentEditorApp.java
 */

package documenteditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;

/**
 * This is a very simple example of a SingleFrameApplication that
 * loads and saves a single text document. Although it does not
 * possess all of the usual trappings of a single-document app, 
 * like versioning or support for undo/redo, it does serve
 * to highlight how to use actions, resources, and tasks.
 * <p>
 * This file contains the main class of the application. It extends the 
 * Swing Application Framework's {@code SingleFrameApplication} class 
 * and therefore takes care or simplifies things like 
 * loading resources and saving the session state.
 * <p>
 * This class calls the {@code DocumentEditorView} class, which 
 * contains the code for constructing the user interface and 
 * the application logic.
 *
 * <p>
 * The application's state is defined by two read-only bound properties:
 * <dl>
 * <dt><strong>File {@link #getFile file}</strong><dt>
 * <dd>The current text File being edited.</dd>
 * <dt><strong>boolean {@link modified #isModified}</strong><dt>
 * <dd>True if the current file needs to be saved.</dd>
 * </dl>
 * These properties are updated when the user interacts with the
 * application.  They can be used as binding sources, to monitor
 * the application's state.
 * 
 * <p> 
 * This application defines a small set of actions for opening
 * and saving files: {@link #open open}, {@link #save save}, 
 * and {@link #saveAs saveAs}.  It inherits 
 * {@code cut/copy/paste/delete} ProxyActions from the
 * {@code Application} class.  The ProxyActions perform their
 * action not on the component they're bound to (menu items and
 * toolbar buttons), but on the component that currently 
 * has the keyboard focus.  Their enabled state tracks the
 * selection value of the component with the keyboard focus, 
 * as well as the contents of the system clipboard.
 * 
 * <p>
 * The action code that reads and writes files, runs asynchronously
 * on background threads.  The {@link #open open}, {@link #save save}, 
 * and {@link #saveAs saveAs} actions all return a Task object which
 * encapsulates the work that will be done on a background thread.
 * The {@link #showAboutBox showAboutBox} and 
 * {@link #closeAboutBox closeAboutBox} actions do their work
 * synchronously.
 * 
 * <p>
 * <strong>Warning:</strong> this application is intended as a simple 
 * example, not as a robust text editor.  Read it, don't use it.
 */
public class DocumentEditorApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
       show(new DocumentEditorView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of DocumentEditorApp
     */
    public static DocumentEditorApp getApplication() {
        return Application.getInstance(DocumentEditorApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(DocumentEditorApp.class, args);
    }

    /**
     * A Task that saves a text String to a file.  The file is not appended
     * to, its contents are replaced by the String.
     */
    static class SaveTextFileTask extends Task<Void, Void> {

        private final File file;
        private final String text;

        /**
         * Construct a SaveTextFileTask.
         *
         * @param file The file to save to
         * @param text The new contents of the file
         */
        SaveTextFileTask(Application app, File file, String text) {
            super(app);
            this.file = file;
            this.text = text;
        }

        /**
         * Return the File that the {@link #getText text} will be
         * written to.
         *
         * @return the value of the read-only file property.
         */
        public final File getFile() {
            return file;
        }

        /**
         * Return the String that will be written to the
         * {@link #getFile file}.
         *
         * @return the value of the read-only text property.
         */
        public final String getText() {
            return text;
        }

        private void renameFile(File oldFile, File newFile) throws IOException {
            if (!oldFile.renameTo(newFile)) {
                String fmt = "file rename failed: %s => %s";
                throw new IOException(String.format(fmt, oldFile, newFile));
            }
        }

        /**
         * Writes the {@code text} to the specified {@code file}.  The
         * implementation is conservative: the {@code text} is initially
         * written to ${file}.tmp, then the original file is renamed
         * ${file}.bak, and finally the temporary file is renamed to ${file}.
         * The Task's {@code progress} property is updated as the text is
         * written.
         * <p>
         * If this Task is cancelled before writing the temporary file
         * has been completed, ${file.tmp} is deleted.
         * <p>
         * The conservative algorithm for saving to a file was lifted from
         * the FileSaver class described by Ian Darwin here:
         * <a href="http://javacook.darwinsys.com/new_recipes/10saveuserdata.jsp">
         * http://javacook.darwinsys.com/new_recipes/10saveuserdata.jsp
         * </a>.
         *
         * @return null
         */
        @Override
        protected Void doInBackground() throws IOException {
            String absPath = file.getAbsolutePath();
            File tmpFile = new File(absPath + ".tmp");
            tmpFile.createNewFile();
            tmpFile.deleteOnExit();
            File backupFile = new File(absPath + ".bak");
            BufferedWriter out = null;
            int fileLength = text.length();
            int blockSize = Math.max(1024, 1 + ((fileLength - 1) / 100));
            try {
                out = new BufferedWriter(new FileWriter(tmpFile));
                int offset = 0;
                while (!isCancelled() && (offset < fileLength)) {
                    int length = Math.min(blockSize, fileLength - offset);
                    out.write(text, offset, length);
                    offset += blockSize;
                    setProgress(Math.min(offset, fileLength), 0, fileLength);
                }
            } finally {
                if (out != null) {
                    out.close();
                }
            }
            if (!isCancelled()) {
                backupFile.delete();
                if (file.exists()) {
                    renameFile(file, backupFile);
                }
                renameFile(tmpFile, file);
            } else {
                tmpFile.delete();
            }
            return null;
        }
    }

    /**
     * A Task that loads the contents of a file into a String.
     */
    static class LoadTextFileTask extends Task<String, Void> {

        private final File file;

        /**
         * Construct a LoadTextFileTask.
         *
         * @param file the file to load from.
         */
        LoadTextFileTask(SingleFrameApplication application, File file) {
            super(application);
            this.file = file;
        }

        /**
         * Return the file being loaded.
         *
         * @return the value of the read-only file property.
         */
        public final File getFile() {
            return file;
        }

        /**
         * Load the file into a String and return it.  The
         * {@code progress} property is updated as the file is loaded.
         * <p>
         * If this task is cancelled before the entire file has been
         * read, null is returned.
         *
         * @return the contents of the {code file} as a String or null
         */
        @Override
        protected String doInBackground() throws IOException {
            int fileLength = (int) file.length();
            int nChars = -1;
            // progress updates after every blockSize chars read
            int blockSize = Math.max(1024, fileLength / 100);
            int p = blockSize;
            char[] buffer = new char[32];
            StringBuilder contents = new StringBuilder();
            BufferedReader rdr = new BufferedReader(new FileReader(file));
            while (!isCancelled() && (nChars = rdr.read(buffer)) != -1) {
                contents.append(buffer, 0, nChars);
                if (contents.length() > p) {
                    p += blockSize;
                    setProgress(contents.length(), 0, fileLength);
                }
            }
            if (!isCancelled()) {
                return contents.toString();
            } else {
                return null;
            }
        }
    }
}
