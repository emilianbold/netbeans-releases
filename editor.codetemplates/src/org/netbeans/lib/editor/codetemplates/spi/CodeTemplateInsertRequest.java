/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates.spi;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.lib.editor.codetemplates.CodeTemplateInsertHandler;
import org.netbeans.lib.editor.codetemplates.CodeTemplateParameterImpl;
import org.netbeans.lib.editor.codetemplates.CodeTemplateSpiPackageAccessor;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;

/**
 * Code template insert request parses the code template's text
 * to gather the data necessary to insert
 * the particular code template into the document (such as the template's parameters).
 *
 * <h3>State</h3>
 * The insert request can be in three states:
 * <ul>
 *   <li>It is not inserted into the document yet.
 *     Both {@link #isInserted()} and {@link #isReleased()}
 *     return false. Registered {@link CodeTemplateProcessor}s
 *     will be asked to fill in the default values into the parameters.
 *   <li>It is inserted and the user modifies the parameters' values in the document.
 *     {@link #isInserted()} returns true and {@link #isReleased()} returns false.
 *   <li>It is released. {@link #isReleased()} returns true. There is no more
 *     work to do. Code templates processor(s) servicing the request will be released.
 * </ul>
 *
 * <h3>Parameters</h3>
 * The code template's text is first parsed to find the parameters.
 * Each first occurrence of a parameter with particular name define
 * a master parameter. All the other occurrences of a parameter with the same name
 * define slave parameters (of the previously defined master).
 *
 * @see CodeTemplateParameter
 * 
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateInsertRequest {
    
    static {
        CodeTemplateSpiPackageAccessor.register(new SpiAccessor());
    }
    
    private final CodeTemplateInsertHandler handler;
    
    CodeTemplateInsertRequest(CodeTemplateInsertHandler handler) {
        this.handler = handler;
    }

    /**
     * Get code template associated with this insert request.
     */
    public CodeTemplate getCodeTemplate() {
        return handler.getCodeTemplate();
    }

    /**
     * Get the text component into which the template should be inserted
     * at the current caret position.
     */
    public JTextComponent getComponent() {
        return handler.getComponent();
    }
    
    /**
     * Get list of master parameters in the order they are located
     * in the code template text.
     * <br>
     * The master parameters can be explored by the code template processor
     * and their default values can be changed as necessary.
     *
     */
    public List/*<CodeTemplateParameter>*/ getMasterParameters() {
        return handler.getMasterParameters();
    }
    
    /**
     * Get master parameter with the given name.
     *
     * @param name non-null name of the master parameter to be searched.
     * @return master parameter with the given name or null if no such
     *  parameter exists.
     */
    public CodeTemplateParameter getMasterParameter(String name) {
        for (Iterator it = getMasterParameters().iterator(); it.hasNext();) {
            CodeTemplateParameter master = (CodeTemplateParameter)it.next();
            if (name.equals(master.getName())) {
                return master;
            }
        }
        return null;
    }

    /**
     * Get all the parameters (masters and slaves)
     * present in the code template text in the order as they occur
     * in the parametrized text.
     * @see #getMasterParameters()
     */
    public List/*<CodeTemplateParameter>*/ getAllParameters() {
        return handler.getAllParameters();
    }
    
    /**
     * Check whether the code template that this request
     * represents was already inserted into the document.
     *
     * @return true if the code template was already inserted into the document
     *  and the inserted default values are being modified by the user
     *  which can result into
     *  {@link CodeTemplateProcessor#parameterValueChanged(CodeTemplateParameter, boolean)}.
     *  <p/>
     *  Returns false if the code template was not yet inserted into the document
     *  i.e. the {@link CodeTemplateProcessor#updateDefaultValues()}
     *  is currently being called on the registered processors.
     * @see #isReleased()
     */
    public boolean isInserted() {
        return handler.isInserted();
    }
    
    /**
     * Check whether this request is already released which means
     * that the code template was inserted and values of all the parameters
     * were modified by the user so there is no more work to be done.
     *
     * @return whether this request is already released or not.
     *  If the request was not yet released then {@link #isInserted()}
     *  gives additional info whether request is inserted into the document or not.
     * @see #isInserted()
     */
    public boolean isReleased() {
        return handler.isReleased();
    }
    
    /**
     * Get the present parametrized text handled by this request.
     * <br/>
     * By default the code template's parametrized text obtained
     * by {@link CodeTemplate#getParametrizedText()} is used.
     * <br/>
     * The parametrized text can be modified by {@link #setParametrizedText(String)}.
     */
    public String getParametrizedText() {
        return handler.getParametrizedText();
    }
    
    /**
     * Set the parametrized text to a new value.
     * <br/>
     * This may be necessary if some parameters are just artificial
     * and should be expanded by a particular code template processor
     * before the regular processing.
     * <br/>
     * Once this method is called the new parametrized text will be parsed
     * and a fresh new list of parameters will be created.
     *
     * @param parametrizedText new parametrized text to be used.
     */
    public void setParametrizedText(String parametrizedText) {
        handler.setParametrizedText(parametrizedText);
    }

    /**
     * Get the text where all the parameters are replaced
     * by their present values.
     * <br/>
     * Unless any parameter's value gets changed this text
     * would be inserted into the document once all the code template processors
     * finish their processing.
     */
    public String getInsertText() {
        return handler.getInsertText();
    }
    

    private static final class SpiAccessor extends CodeTemplateSpiPackageAccessor {
        
        public CodeTemplateInsertRequest createInsertRequest(CodeTemplateInsertHandler handler) {
            return new CodeTemplateInsertRequest(handler);
        }

        public CodeTemplateParameter createParameter(CodeTemplateParameterImpl impl) {
            return new CodeTemplateParameter(impl);
        }
        
        public CodeTemplateParameterImpl getImpl(CodeTemplateParameter parameter) {
            return parameter.getImpl();
        }

    }

}
