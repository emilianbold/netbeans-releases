/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jsploader;

import java.io.*;
import java.beans.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import javax.swing.SwingUtilities;

import org.openide.*;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.loaders.DataNode;
import org.openide.loaders.CompilerSupport;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.src.*;
import org.openide.actions.OpenAction;
import org.openide.src.nodes.SourceChildren;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.Lookup;
import org.openide.execution.Executor;
import org.openide.debugger.DebuggerType;

import org.netbeans.modules.web.core.WebExecSupport;
import org.netbeans.modules.j2ee.impl.ServerExecSupport;
import org.netbeans.modules.web.core.LanguageEditor;
import org.netbeans.modules.web.core.FeatureFactory;

import org.netbeans.modules.java.Util;

/** The node representation of <code>JspDataObject</code> for internet files.
*
* @author Petr Jiricka
*/
public class JspNode extends DataNode {

    private static final String EXECUTION_SET_NAME = "Execution"; // NOI18N
    private static final String SHEETNAME_TEXT_PROPERTIES = "textProperties"; // NOI18N
    private static final String PROP_ENCODING = "encoding"; // NOI18N

    private static final String ICON_BASE = "org/netbeans/modules/web/core/resources/jspObject"; // NOI18N

    public static final String PROP_REQUEST_PARAMS   = "requestparams"; // NOI18N

    /** Create a node for the internet data object using the default children.
    * @param jdo the data object to represent
    */
    public JspNode (JspDataObject jdo) {
        super(jdo, Children.LEAF);
        initialize();
    }

    private void initialize () {
        setIconBase(ICON_BASE);
        setDefaultAction (SystemAction.get (OpenAction.class));
    }


    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        initialize();
    }

    public DataObject getDataObject() {
        return super.getDataObject();
    }
    
    /** Create the property sheet.
    * Subclasses may want to override this and add additional properties.
    * @return the sheet
    */
    protected Sheet createSheet () {
        Sheet.Set ps;

        Sheet sheet = super.createSheet();

        ps = sheet.get(Sheet.PROPERTIES);
        // content language
        ps.put(new PropertySupport.ReadWrite (
                   JspDataObject.PROP_CONTENT_LANGUAGE,
                   String.class,
                   NbBundle.getBundle(LanguageEditor.class).getString("PROP_contentLanguage"),
                   NbBundle.getBundle(LanguageEditor.class).getString("HINT_contentLanguage")
               ) {
                   public Object getValue() {
                       return ((JspDataObject)getDataObject()).getContentLanguage();
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       if (val instanceof String) {
                           try {
                               ((JspDataObject)getDataObject()).setContentLanguage((String)val);
                           } catch(IOException e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                       else {
                           throw new IllegalArgumentException();
                       }
                   }
                   public PropertyEditor getPropertyEditor() {
                       return new LanguageEditor(FeatureFactory.getFactory().getJSPContentLanguages());
                   }
               }
            );
        // scripting language
        ps.put(new PropertySupport.ReadWrite (
                   JspDataObject.PROP_SCRIPTING_LANGUAGE,
                   String.class,
                   NbBundle.getBundle(LanguageEditor.class).getString("PROP_scriptingLanguage"),
                   NbBundle.getBundle(LanguageEditor.class).getString("HINT_scriptingLanguage")
               ) {
                   public Object getValue() {
                       return ((JspDataObject)getDataObject()).getScriptingLanguage();
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       if (val instanceof String) {
                           try {
                               ((JspDataObject)getDataObject()).setScriptingLanguage((String)val);
                           } catch(IOException e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                       else {
                           throw new IllegalArgumentException();
                       }
                   }
                   public PropertyEditor getPropertyEditor() {
                       return new LanguageEditor(FeatureFactory.getFactory().getJSPScriptingLanguages());
                   }
               }
            );
        // encoding
/*        ps.put(new PropertySupport.ReadWrite (
                   JspDataObject.PROP_ENCODING,
                   String.class,
                   NbBundle.getBundle(EncodingEditor.class).getString("PROP_encoding"),
                   NbBundle.getBundle(EncodingEditor.class).getString("HINT_encoding")
               ) {
                   public Object getValue() {
                       return ((JspDataObject)getDataObject()).getEncoding();
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       if (val instanceof String) {
                           try {
                               ((JspDataObject)getDataObject()).setEncoding((String)val);
                           } catch(IOException e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                       else {
                           throw new IllegalArgumentException();
                       }
                   }
                   public PropertyEditor getPropertyEditor() {
                       return new EncodingEditor();
                   }
               }
            );*/

        ps = new Sheet.Set ();
        ps.setName(EXECUTION_SET_NAME);
        ps.setDisplayName(NbBundle.getBundle(JspNode.class).getString("PROP_executionSetName"));
        ps.setShortDescription(NbBundle.getBundle(JspNode.class).getString("HINT_executionSetName"));

        ps.put(new PropertySupport.ReadWrite (
                   PROP_REQUEST_PARAMS,
                   String.class,
                   NbBundle.getBundle(JspNode.class).getString("PROP_requestParams"),
                   NbBundle.getBundle(JspNode.class).getString("HINT_requestParams")
               ) {
                   public Object getValue() {
                       return getRequestParams(((MultiDataObject)getDataObject()).getPrimaryEntry());
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       if (val instanceof String) {
                           try {
                               setRequestParams(((MultiDataObject)getDataObject()).getPrimaryEntry(), (String)val);
                           } catch(IOException e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                       else {
                           throw new IllegalArgumentException();
                       }
                   }
               }
              );

        // add execution/debugger properties
        ServerExecSupport wes = (ServerExecSupport)getDataObject().getCookie(ServerExecSupport.class);
        if (wes != null)
            wes.addProperties(ps);

        // remove the params property
        //ps.remove(ExecSupport.PROP_FILE_PARAMS);
        // remove the debugger type property
        //ps.remove(ExecSupport.PROP_DEBUGGER_TYPE);

        // add compilation properties
        JspDataObject mdo = (JspDataObject)getDataObject ().getCookie (JspDataObject.class);
        if (mdo != null) {
            CompilerSupport comp = (CompilerSupport)mdo.getCookieSet0 ().getCookie (CompilerSupport.class);
            if (comp != null) comp.addProperties(ps);
        }

        // change the display name and hint
        Node.Property compProp = ps.get(JspCompilerSupport.PROP_COMPILER_TYPE);
        if (compProp != null) {
            compProp.setDisplayName(NbBundle.getBundle(JspNode.class).getString("CTL_ServletCompilerPropertyName"));
            compProp.setShortDescription(NbBundle.getBundle(JspNode.class).getString("CTL_ServletCompilerPropertyHint"));
        }

        sheet.put(ps);

        // text sheet
        ps = new Sheet.Set();
        ps.setName(SHEETNAME_TEXT_PROPERTIES);
        ps.setDisplayName(NbBundle.getBundle(JspNode.class).getString("PROP_textfileSetName")); // NOI18N
        ps.setShortDescription(NbBundle.getBundle(JspNode.class).getString("HINT_textfileSetName")); // NOI18N
        ps.put(new PropertySupport.ReadWrite(PROP_ENCODING, 
            String.class, NbBundle.getBundle(JspNode.class).getString("PROP_fileEncoding"), // NOI18N
                          NbBundle.getBundle(JspNode.class).getString("HINT_fileEncoding")) { // NOI18N
            public Object getValue() {
                String enc = JspDataObject.getFileEncoding0(getDataObject().getPrimaryFile());
                if (enc == null)
                    return getDefaultEncodingDisplay();
                else
                    return enc;
            }
            
            public void setValue(Object enc) throws InvocationTargetException {
                String encoding = (String)enc;
                if (isDefaultEncoding(encoding)) {
                    encoding = null;
                }
                else {
                    try {
                        sun.io.CharToByteConverter.getConverter(encoding);
                    } catch (IOException ex) {
                        InvocationTargetException t =  new InvocationTargetException(ex);
                        wrapThrowable(t, ex,
                            java.text.MessageFormat.format(NbBundle.getBundle(JspNode.class).getString("FMT_UnsupportedEncoding"), // NOI18N
                                new Object[] {
                                    encoding
                                }
                            ));
                        throw t;
                    }
                }
                try {
                    Util.setFileEncoding(getDataObject().getPrimaryFile(), encoding);
                    // clear the old attribute (backward compatibility)
                    getDataObject().getPrimaryFile().setAttribute ("AttrEncoding", null);   // NOI18N
                } catch (IOException ex) {
                    throw new InvocationTargetException(ex);
                }
            }
            
            /** Finds out whether encoding enc entered from the keyboard is 
             * the same as the platform default encoging. */
            private boolean isDefaultEncoding(String enc) {
                if (enc == null)
                    return true;
                enc = enc.trim();
                if (enc.equals(""))
                    return true;
                if (enc.equals(JspDataObject.getDefaultEncoding()))
                    return true;
                if (enc.equals(getDefaultEncodingDisplay()))
                    return true;
                return false;
            }
            
            /** Returms the display value for the default encoding. */
            private String getDefaultEncodingDisplay() {
                String enc = JspDataObject.getDefaultEncoding();
                return MessageFormat.format(
                    NbBundle.getBundle(JspNode.class).getString("FMT_DefaultEncoding"),
                    new Object[] {enc});
            }
        });
        sheet.put(ps);
        
        return sheet;
    }

    static final void wrapThrowable(Throwable outer, Throwable inner, String message) {
        ((ErrorManager)Lookup.getDefault().lookup(ErrorManager.class)).annotate(
            outer, ErrorManager.USER, null, message, inner, null);
    }

    /** Set request parameters for a given entry.
    * @param entry the entry
    * @param args array of arguments
    * @exception IOException if arguments cannot be set
    */
    static void setRequestParams(MultiDataObject.Entry entry, String params) throws IOException {
        StringBuffer newParams=new StringBuffer();
        String s=null;
        if (params!=null){
            for (int i=0;i<params.length();i++) {
                char ch = params.charAt(i);
                if ((int)ch!=13 && (int)ch!=10) newParams.append(ch);
            }
            s=newParams.toString();
            if (s.length()==0) s=null;
        } 
        WebExecSupport.setQueryString(entry.getFile (), s);
    }

    /** Get the request parameters associated with a given entry.
    * @param entry the entry
    * @return the arguments, or an empty string if no arguments are specified
    */
    static String getRequestParams(MultiDataObject.Entry entry) {
        return WebExecSupport.getQueryString(entry.getFile ());
    }

    /** Get the icon base.
    * This should be a resource path, e.g. <code>/some/path/</code>,
    * where icons are held. Subclasses may override this.
    * @return the icon base
    * @see #getIcons
    */
    protected String getIconBase() {
        return ICON_BASE;
    }

}

/*
 * Log
 */
