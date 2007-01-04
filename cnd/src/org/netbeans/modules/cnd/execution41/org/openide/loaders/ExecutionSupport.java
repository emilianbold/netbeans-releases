/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.execution41.org.openide.loaders;

import java.beans.*;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.openide.loaders.MultiDataObject;
import org.openide.loaders.Environment;

import org.netbeans.modules.cnd.execution41.org.openide.cookies.ExecCookie;
import org.netbeans.modules.cnd.execution41.org.openide.cookies.ArgumentsCookie;
import org.netbeans.modules.cnd.execution41.org.openide.execution.Executor;

import org.openide.*;
import org.openide.ErrorManager.Annotation;
import org.openide.execution.*;
import org.openide.explorer.propertysheet.*;
import org.openide.filesystems.*;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.Lookup;

/** Support for execution of a data object.
* @author Jaroslav Tulach, Jesse Glick
* @since 3.14
*/
public class ExecutionSupport extends Object
    implements ExecCookie, ArgumentsCookie {
    /** extended attribute for the type of executor */
    private static final String EA_EXECUTOR = "NetBeansAttrExecutor"; // NOI18N
    /** extended attribute for attributes */
    private static final String EA_ARGUMENTS = "NetBeansAttrArguments"; // NOI18N

    // copy from JavaNode
    /** Name of property providing argument parameter list. */
    public static final String PROP_FILE_PARAMS   = "params"; // NOI18N
    /** Name of property providing a custom {@link Executor} for a file. */
    public static final String PROP_EXECUTION     = "execution"; // NOI18N

    /** entry to be associated with */
    private MultiDataObject.Entry entry;
    
    /**  readOnlyAttrs is name of virtual attribute. This name of virtual attribute 
     * is shared between classes (and should be changed everywhere): 
     * - org.openide.filesystems.DefaultAttributes
     * - org.openide.loaders.ExecutionSupport
     * - org.openide.loaders.CompilerSupport
     * - org.netbeans.core.ExJarFileSystem
     */    
    protected final static String READONLY_ATTRIBUTES = "readOnlyAttrs"; //NOI18N

    /** Create new support for given entry. The file is taken from the
    * entry and is updated if the entry moves or renames itself.
    * @param entry entry to create instance from
    */
    public ExecutionSupport (MultiDataObject.Entry entry) {
        this.entry = entry;
    }
    
    /** Get the associated file that can be executed.
     * @return the file that can be executed
     */
    protected MultiDataObject.Entry getEntry() {
        return entry;
    }

    /* Starts the class.
    */
    public void start () {
        Executor exec = getExecutor (entry);
        if (exec == null) {
            exec = defaultExecutor ();
        }

        try {
            exec.execute(entry.getDataObject());
        } catch (final IOException ex) {
            Mutex.EVENT.readAccess (new Runnable () {
                                        public void run () {
                                            if (startFailed (ex)) {
                                                // restart
                                                ExecutionSupport.this.start ();
                                            }
                                        }
                                    });
        }
    }

    /** Called when invocation of the executor fails. Allows to do some
    * modifications to the type of execution and try it again.
    *
    * @param ex exeception that occured during execution
    * @return true if the execution should be restarted
    */
    protected boolean startFailed (IOException ex) {
        return false;
    }

    /** This method allows subclasses to override the default
    * executor they want to use for debugging.
    *
    * @return current implementation returns Executor.getDefault ()
    */
    protected Executor defaultExecutor () {
        return Executor.getDefault ();
    }


    /** Set the executor for a given file object.
     * Uses file attributes to store this information.
    * @param entry entry to set the executor for
    * @param exec executor to use
    * @exception IOException if executor cannot be set
    */
    public static void setExecutor (MultiDataObject.Entry entry, Executor exec) throws IOException {
    }

    /** Get the executor for a given file object.
    * @param entry entry to obtain the executor for
    * @return executor associated with the file, or <code>null</code> if the default should be used
    */
    public static Executor getExecutor (MultiDataObject.Entry entry) {
        return null;
    }

    /* Sets execution arguments for the associated entry.
    * @param args array of arguments
    * @exception IOException if arguments cannot be set
    */
    public void setArguments (String[] args) throws IOException {
        entry.getFile ().setAttribute (EA_ARGUMENTS, args);
    }

    /** Set execution arguments for a given entry.
    * @param entry the entry
    * @param args array of arguments
    * @exception IOException if arguments cannot be set
    */
    public static void setArguments (MultiDataObject.Entry entry, String[] args) throws IOException {
        entry.getFile ().setAttribute (EA_ARGUMENTS, args);
    }

    /* Getter for arguments associated with given file.
    * @return the arguments or empty array if no arguments associated
    */
    public String[] getArguments () {
        return getArguments (entry);
    }

    /** Get the arguments associated with a given entry.
    * @param entry the entry
    * @return the arguments, or an empty array if no arguments are specified
    */
    public static String[] getArguments(MultiDataObject.Entry entry) {
        Object o = entry.getFile ().getAttribute (EA_ARGUMENTS);
        if (o != null && (o instanceof String[])) {
            return (String[]) o;
        } else {
            return new String[] { };
        }
    }

    /** Helper method that creates default properties for execution of
    * a given support.
    * Includes properties to set the executor; debugger; and arguments.
    *
    * @param set sheet set to add properties to
    */
    public void addProperties (Sheet.Set set) {
        set.put(createParamsProperty());
        set.put(createExecutorProperty ());
    }

    /** Creates the fileparams property for entry.
    * @return the property
    */
    private PropertySupport createParamsProperty () {        
        PropertySupport result = new PropertySupport.ReadWrite (
                    PROP_FILE_PARAMS,
                    String.class,
                    getString("PROP_fileParams"),
                    getString("HINT_fileParams")
                ) {
                    public Object getValue() {
                        String[] args = getArguments ();
                        /*
                        StringBuffer b = new StringBuffer(50);
                        for (int i = 0; i < args.length; i++) {
                            b.append(args[i]).append(' ');
                        }
                        return b.toString();
                         */
                        return Utilities.escapeParameters(args);
                    }
                    public void setValue (Object val) throws InvocationTargetException {
                        if (val instanceof String) {
                            try {
                                setArguments(Utilities.parseParameters((String)val));
                            } catch(IOException e) {
                                throw new InvocationTargetException (e);
                            }
                        }
                        else {
                            throw new IllegalArgumentException();
                        }
                    }

                    public boolean supportsDefaultValue () {
                        return true;
                    }

                    public void restoreDefaultValue () throws InvocationTargetException {
                        try {
                            setArguments(null);
                        } catch(IOException e) {
                            throw new InvocationTargetException (e);
                        }
                    }

                   public boolean canWrite () {
                       Boolean isReadOnly = (Boolean)entry.getFile().getAttribute(READONLY_ATTRIBUTES);
                       return (isReadOnly == null)?false:(!isReadOnly.booleanValue());
                   }
                };
        //String editor hint to use a JTextField, not a JTextArea for the
        //custom editor.  Arguments can't be multiline anyway.
        result.setValue("oneline", Boolean.TRUE); // NOI18N
        return result;
    }
    
    /** Creates the executor property for entry.
    * @return the property
    */
    private PropertySupport createExecutorProperty () {
        return new PropertySupport.ReadWrite (
                   PROP_EXECUTION,
                   Executor.class,
                   getString("PROP_execution"),
                   getString("HINT_execution")
               ) {
                   public Object getValue() {
                       Executor e = getExecutor (entry);
                       if (e == null)
                           return defaultExecutor ();
                       else
                           return e;
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       try {
                           setExecutor(entry, (Executor) val);
                       } catch (IOException ex) {
                           throw new InvocationTargetException (ex);
                       }
                   }
                   public boolean supportsDefaultValue () {
                       return true;
                   }

                   public void restoreDefaultValue () throws InvocationTargetException {
                       setValue (null);
                   }
                   
                   public boolean canWrite () {
                       Boolean isReadOnly = (Boolean)entry.getFile().getAttribute(READONLY_ATTRIBUTES);
                       return (isReadOnly == null)?false:(!isReadOnly.booleanValue());
                   }
               };
    }

    /** @return a localized String */
    static String getString(String s) {
        return NbBundle.getMessage(Executor.class, s);
    }

}
