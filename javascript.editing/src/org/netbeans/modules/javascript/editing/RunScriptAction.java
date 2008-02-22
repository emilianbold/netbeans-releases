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

package org.netbeans.modules.javascript.editing;

import java.awt.event.ActionEvent;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.gsf.api.EditorAction;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;


/**
 * Run a JavaScript file.
 * Based on the relevant runScript method in Schliemann's JavaScript class.
 * 
 * @author Jan Jancura
 * @author Dan Prusa
 * @author Tor Norbye
 */
public class RunScriptAction extends AbstractAction implements EditorAction {
    
    Boolean enabled;
    
    public RunScriptAction() {
        super(NbBundle.getMessage(RunScriptAction.class, "js-run-action"));
        putValue("PopupMenuText", NbBundle.getBundle(RunScriptAction.class).getString("popup-js-run-action")); // NOI18N
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        actionPerformed(target);
    }

    public String getActionName() {
        return "js-run-action";
    }

    public Class getShortDescriptionBundleClass() {
        return RunScriptAction.class;
    }

    @Override
    public boolean isEnabled() {
        if (enabled == null) {
            try {
                ClassLoader cl = RunScriptAction.class.getClassLoader ();
                Class managerClass = cl.loadClass ("javax.script.ScriptEngineManager");

                enabled = Boolean.valueOf(managerClass != null);
            } catch (ClassNotFoundException ex) {
                enabled = Boolean.FALSE;
            }
        }

        return enabled.booleanValue();
    }

    public void actionPerformed(ActionEvent ev) {
        JTextComponent pane = NbUtilities.getOpenPane();

        if (pane != null) {
            actionPerformed(pane);
        }
    }

    @SuppressWarnings("unchecked")
    void actionPerformed(final JTextComponent comp) {
        if (comp.getCaret() == null) {
            return;
        }
    
        RequestProcessor.getDefault().post(new Runnable () {
            public void run() {
                ClassLoader cl = RunScriptAction.class.getClassLoader ();
                InputOutput io = null;
                FileObject fo = null;
                try {
        //        ScriptEngineManager manager = new ScriptEngineManager ();
        //        ScriptEngine engine = manager.getEngineByMimeType ("text/javascript");
                    Class managerClass = cl.loadClass ("javax.script.ScriptEngineManager");
                    Object manager = managerClass.newInstance();
                    Method getEngineByMimeType = managerClass.getMethod ("getEngineByMimeType", new Class[] {String.class});
                    Object engine = getEngineByMimeType.invoke (manager, new Object[] {"text/javascript"});

                    Document doc = comp.getDocument ();
                    DataObject dob = NbEditorUtilities.getDataObject (doc);
                    String name = dob.getPrimaryFile ().getNameExt ();
                    fo = dob.getPrimaryFile();
                    SaveCookie saveCookie = dob.getLookup ().lookup (SaveCookie.class);
                    if (saveCookie != null)
                        try {
                            saveCookie.save ();
                        } catch (IOException ex) {
                            ErrorManager.getDefault ().notify (ex);
                        }

        //            ScriptContext context = engine.getContext ();
                    Class engineClass = cl.loadClass ("javax.script.ScriptEngine");
                    Method getContext = engineClass.getMethod ("getContext", new Class[] {});
                    Object context = getContext.invoke (engine, new Object[] {});
                    Method put = engineClass.getMethod ("put", new Class[] {String.class, Object.class});
                    put.invoke(engine, new Object[] {"javax.script.filename", fo.getPath()});

                    io = IOProvider.getDefault ().getIO ("Run " + name, false);

        //            context.setWriter (io.getOut ());
        //            context.setErrorWriter (io.getErr ());
        //            context.setReader (io.getIn ());
                    Class contextClass = cl.loadClass("javax.script.ScriptContext");
                    Method setWriter = contextClass.getMethod ("setWriter", new Class[] {Writer.class});
                    Method setErrorWriter = contextClass.getMethod ("setErrorWriter", new Class[] {Writer.class});
                    Method setReader = contextClass.getMethod ("setReader", new Class[] {Reader.class});
                    setWriter.invoke (context, new Object[] {io.getOut ()});
                    setErrorWriter.invoke (context, new Object[] {io.getErr ()});
                    setReader.invoke (context, new Object[] {io.getIn ()});

                    io.getOut().reset ();
                    io.select ();

        //            Object o = engine.eval (doc.getText (0, doc.getLength ()));
                    Method eval = engineClass.getMethod ("eval", new Class[] {String.class});
                    Object o = eval.invoke (engine, new Object[] {doc.getText (0, doc.getLength ())});

                    if (o != null)
                        DialogDisplayer.getDefault ().notify (new NotifyDescriptor.Message ("Result: " + o));

                } catch (InvocationTargetException ex) {
                    try {
                        Class scriptExceptionClass = cl.loadClass("javax.script.ScriptException");
                        if (ex.getCause () != null && 
                            scriptExceptionClass.isAssignableFrom (ex.getCause ().getClass ())
                        )
                            if (io != null) {
                                String msg = ex.getCause ().getMessage ();
                                int line = 0;
                                if (msg.startsWith("sun.org.mozilla")) { //NOI18N
                                    msg = msg.substring(msg.indexOf(':') + 1);
                                    msg = msg.substring(0, msg.lastIndexOf('(')).trim() + " " + msg.substring(msg.lastIndexOf(')') + 1).trim();
                                    try {
                                        line = Integer.valueOf(msg.substring(msg.lastIndexOf("number") + 7)); //NOI18N
                                    } catch (NumberFormatException nfe) {
                                        //cannot parse, jump at line zero
                                    }
                                }
                                io.getOut().println(msg, new OutputProcessor(fo, line));
                            }
                        else
                            ErrorManager.getDefault ().notify (ex);
                    } catch (Exception ex2) {
                        ErrorManager.getDefault ().notify (ex2);
                    }
                } catch (Exception ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            }
        });
    }
    
    /**
     * HACK - this class should not be here; we should use
     *    org.netbeans.modules.languages.execution
     * instead!  Just copied from Schliemann for now to get things done.
     */
    public static class OutputProcessor implements OutputListener {

        public static final Logger LOGGER = Logger.getLogger(OutputListener.class.getName());

        private final FileObject file;
        private final int lineno;

        OutputProcessor(FileObject file, int lineno) {
            if (lineno < 0) {
                lineno = 0;
            }

            // TODO : columns?
            this.file = file;
            this.lineno = lineno;
        }

        public void outputLineSelected(OutputEvent ev) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void outputLineAction(OutputEvent ev) {
            open(file, lineno);
        }

        public void outputLineCleared(OutputEvent ev) {
        }

        public static boolean open(final FileObject fo, final int lineno) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            open(fo, lineno);
                        }
                    });

                return true; // not exactly accurate, but....
            }

            try {
                DataObject od = DataObject.find(fo);
                EditorCookie ec = (EditorCookie)od.getCookie(EditorCookie.class);
                LineCookie lc = (LineCookie)od.getCookie(LineCookie.class);

                if ((ec != null) && (lc != null)) {
                    Document doc = ec.openDocument();

                    if (doc != null) {
                        int line = lineno;

                        if (line < 1) {
                            line = 1;
                        }

                        Line l = lc.getLineSet().getCurrent(line - 1);

                        if (l != null) {
                            l.show(Line.SHOW_GOTO);

                            return true;
                        }
                    }
                }

                OpenCookie oc = (OpenCookie)od.getCookie(OpenCookie.class);

                if (oc != null) {
                    oc.open();

                    return true;
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }

            return false;
        }
    }
}
