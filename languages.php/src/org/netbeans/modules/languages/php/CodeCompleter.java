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
package org.netbeans.modules.languages.php;

import org.netbeans.modules.php.editor.completion.CodeCompletionContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Completable;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.Element;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.languages.php.lang.Operators;
import org.netbeans.modules.php.editor.TokenUtils;
import org.netbeans.modules.php.editor.completion.CodeTemplateProvider;
import org.netbeans.modules.php.editor.completion.CompletionResultProvider;
import org.netbeans.modules.php.editor.completion.DocumentableElement;
import org.netbeans.modules.php.editor.completion.ForeignScope;
import org.netbeans.modules.php.editor.completion.SelectionTemplates;
import org.netbeans.modules.php.editor.completion.TemplateContext;
import org.netbeans.modules.php.model.Literal;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * @author ads, vvg
 *
 */
public class CodeCompleter implements Completable {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Completable#complete(org.netbeans.modules.gsf.api.CompilationInfo, int, java.lang.String, org.netbeans.modules.gsf.api.NameKind, org.netbeans.modules.gsf.api.Completable.QueryType, boolean, org.netbeans.modules.gsf.api.HtmlFormatter)
     */
    public List<CompletionProposal> complete( CompilationInfo info,
            int caretOffset, String prefix, NameKind kind, QueryType queryType,
            boolean caseSensitive, HtmlFormatter formatter )
    {
        PhpModel model;
        try {
            model = getPhpModel(info, caretOffset); // IAE
            checkPhp(model, caretOffset);
        } catch (IllegalArgumentException ioe) {
            // process foreign scope
            return getProposals(null, info, caretOffset, prefix, kind, 
                    queryType, caseSensitive, formatter);
        }
        model.writeLock();
        try {
            model.sync();
            SourceElement element = model.findSourceElement(caretOffset);
            return getProposals(element, info, caretOffset, prefix, kind, 
                    queryType, caseSensitive, formatter);
        } finally {
            model.writeUnlock();
        }
   }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Completable#document(org.netbeans.modules.gsf.api.CompilationInfo, org.netbeans.modules.gsf.api.Element)
     */
    public String document( CompilationInfo info, Element element ) {
        if(element instanceof DocumentableElement) {
            return  ((DocumentableElement)element).getDocumentation();
        }
        return null;
    }

    public ElementHandle resolveLink(String link, ElementHandle elementHandle) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Completable#getApplicableTemplates(org.netbeans.modules.gsf.api.CompilationInfo, int, int)
     */
    
    /**
     * Returns the set of the applicable templates for a given text selection.
     * 
     * @param info {@link org.netbeans.modules.gsf.api.CompilationInfo} about context of
     * the given text selection. 
     * @param start the start offset of the selection.
     * @param end the end offset of the selection, or -1 if there is no 
     * selection. 
     * @return the set of the applicable templates if any, otherwise 
     * {@link java.util.Collections#emptySet()}.
     */
    public Set<String> getApplicableTemplates( CompilationInfo info,
            int start, int end )
    {
        /*
         * This is the same as abbrevations. But abbrevations are called 
         * via "tab" or smth other. This mechanism provides 
         * possibility to insert abbrevation via Ctl-Space.
         * This method returns abbrevations ( code templates ) that 
         * is applicable for selection ( from start to end ).
         * Real text that will be used for insertion in document should
         * be placed into abbrevaton machanism ( see layer.xml , 
         * codetemplates.xml atttribute value ). 
         */
        TemplateContext context = 
                TemplateContext.newTemplateContext(info, start, end);
        // Currentelly only one provider is defined.
        CodeTemplateProvider provider = new SelectionTemplates();
        if(provider.isApplicable(context)) {
            return provider.getAbbreviationSet(context);
        }
        return Collections.emptySet();
    }
        

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Completable#getPrefix(org.netbeans.modules.gsf.api.CompilationInfo, int, boolean)
     */
    public String getPrefix( CompilationInfo info, int caretOffset,
            boolean upToOffset )
    {
        try {
            Document doc = info.getDocument(); // IOException
            PhpModel model = getPhpModel(info, caretOffset); // check args IllegalArgumentException
            checkPhp(model, caretOffset);
            return TokenUtils.getEnteredEmbeddedTokenText(doc, caretOffset, 
                    upToOffset);
        }
        catch (Exception e) {
            // error(e);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Completable#resolveTemplateVariable(java.lang.String, org.netbeans.modules.gsf.api.CompilationInfo, int, java.lang.String, java.util.Map)
     */
    public String resolveTemplateVariable( String variable,
            CompilationInfo info, int caretOffset, String name, Map parameters )
    {
        // TODO Auto-generated method stub
        /*
         * This methods relates to getApplicableTemplates() method.
         * Latter method returns paramatrized string with some templates
         * ( variables ). These templates should be resolved to 
         * their valies via this method.  
         */
        if (parameters != null && parameters.containsKey(ATTR_UNUSEDLOCAL)) {
            return suggestName(info, caretOffset, name, parameters);
        }

        return null;
    }
    
    /**
     * Returns parameter info.
     * @param info
     * @param caretOffset
     * @param proposal a cache for looking up tip proposal - usually null 
     * (shortlived).
     * @return a <code>ParameterInfo</code> instance with actual info about 
     * a parameter specified by the <code>caretOffset</code> if it is possible,
     * otherwise <code>ParameterInfo.NONE</code>.
     * @see org.netbeans.modules.gsf.api.Completable#parameters(org.netbeans.modules.gsf.api.CompilationInfo, int, org.netbeans.modules.gsf.api.CompletionProposal)
     */
    public ParameterInfo parameters( CompilationInfo info, int caretOffset, 
            CompletionProposal proposal ) 
    {
        /*
         * It seems this method should return information about formal
         * parameters in method . Information will be used 
         * in tooltip.
         */
        PhpModel model;
        try {
            model = getPhpModel(info, caretOffset); // IAE
            checkPhp(model, caretOffset);
        } catch (IllegalArgumentException ioe) {
//            error(ioe);
            return ParameterInfo.NONE;
        }
        model.writeLock();
        try {
            model.sync();
            SourceElement element = model.findSourceElement(caretOffset);
            if (element != null && element.getElementType().equals( Literal.class )) {
                Literal l = (Literal)element;
                List<String> names = new ArrayList<String>();
                names.add(l.getText());
                int index = 0;
                return new ParameterInfo(names, index, l.getOffset());
            }
        } finally {
            model.writeUnlock();
        }
        return ParameterInfo.NONE;
    }
    
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        if(typedText == null || typedText.length() == 0) {
            return QueryType.NONE;
        }
        char c = typedText.charAt(0);
        switch(c) {
            case '\n':
            case '(':
            case '[':
            case '{': {
                return QueryType.STOP;
            }
            case VARIABLE_PREFIX: {
                return QueryType.COMPLETION;
            }
        }
        if(typedText.startsWith(SCOPE_RESOLUTION_OPERATOR)) {
            return QueryType.COMPLETION;
        }
        return QueryType.NONE;
    }
    
    private String suggestName(CompilationInfo info, int caretOffset, 
            String prefix, Map params) {
        // TODO FixMe
        return "$var1";
    }

  
//   public ParameterInfo getParameterInfo(CompilationInfo info, int caretOffset,
//                                          CompletionProposal proposal) {
//        List<String> names = new ArrayList<String>();
//        List<FormalParameter> fpl = getFormalParameterList();
//        int index = 0;
//        for(FormalParameter fp: fpl) {
//            if( isCaretInside(caretOffset, fp) ) {
//                names.add(fp.getName());
//                return new ParameterInfo(names, index, fp.getOffset());
//            }
//            index++;
//        }
//        return null; // we must not be here!
//    }

    private List<CompletionProposal> getProposals( SourceElement element,
            CompilationInfo info, int caretOffset, String prefix,
            NameKind kind, QueryType queryType, boolean caseSensitive,
            HtmlFormatter formatter )
    {
        CodeCompletionContext context = new CodeCompletionContext(element, info,
                caretOffset, prefix, kind, queryType, caseSensitive, formatter);
        context.setCurrentSourceElement(element);

//        while (element != null) {
//            for (CompletionResultProvider provider : myProviders) {
//                if (provider.isApplicable(element)) {
//                    return provider.getProposals(element, element, info, 
//                            caretOffset, prefix, kind, queryType,
//                            caseSensitive, formatter);
//                }
//            }
//            element = element.getParent();
//        }
//        return null;
        
        List<CompletionProposal> allProposals = 
                new ArrayList<CompletionProposal>();
        List<CompletionResultProvider> involvedProviders = 
                new ArrayList<CompletionResultProvider>();
//        while (element != null) {
//            for (CompletionResultProvider provider : myProviders) {
//                if (provider.isApplicable(context) && 
//                    !involvedProviders.contains(provider)) 
//                {
//                    involvedProviders.add(provider);
//                    List<CompletionProposal> proposals = 
//                            provider.getProposals(context);
//                    if ( proposals != null && !proposals.isEmpty()) {
//                        // TODO: decide about adding unique proposals only.  
//                        allProposals.addAll(proposals);    
//                    }
//                }
//            }
//            element = element.getParent();
//            context.setCurrentSourceElement(element);
//        }
        for (CompletionResultProvider provider : myProviders) {
            if (!involvedProviders.contains(provider) && 
                provider.isApplicable(context)) 
            {
                involvedProviders.add(provider);
                List<CompletionProposal> proposals = 
                        provider.getProposals(context);
                if ( proposals != null && !proposals.isEmpty()) {
                    // TODO: decide about adding unique proposals only.  
                    allProposals.addAll(proposals);    
                }
            }
        }

        
        return allProposals;
    }
    
    private SourceElement findSourceElement(PhpModel model, int caretOffset) {
            return model.findSourceElement( caretOffset );
    }
    
    /**
     * Returns <code>PhpModel</code>.
     * @param info
     * @param caretOffset
     * @return <code>PhpModel</code> or <code>null</code> 
     * @throws java.lang.IllegalArgumentException if either 
     * <code>info</code> or <code>caretOffset</code> is incorrect from viewpoint
     * of the PHP model.
     */
    private PhpModel getPhpModel(CompilationInfo info, int caretOffset)
            throws IllegalArgumentException {
        ParserResult result = info.getParserResult();
        if ( result == null || ! (result instanceof PhpParseResult) ) {
            throw new IllegalArgumentException();
        }
        PhpModel model = ((PhpParseResult) result).getModel();
        return model;
    }
    
    private void checkPhp(PhpModel model, int caretOffset) 
        throws IllegalArgumentException {
        if(!TokenUtils.checkPhp( model.getDocument() , caretOffset )) {
            throw new IllegalArgumentException();
        }
    }

    private void error(Exception e) {
        ErrorManager.getDefault().notify( e );
    }


    private Collection<? extends CompletionResultProvider> myProviders = 
                Lookup.getDefault().lookupAll(CompletionResultProvider.class);

    /** Live code template parameter: compute an unused local variable name */
    private static final String ATTR_UNUSEDLOCAL = "unusedlocal"; // NOI18N

    /**
     * {@link http://www.php.net/manual/en/language.variables.php#language.variables.basics}
     */
    private static final char VARIABLE_PREFIX = '$'; // NOI18N

    /**
     * {@link http://www.php.net/manual/en/language.variables.php#language.variables.basics}
     */
    private static final String SCOPE_RESOLUTION_OPERATOR = Operators.SCOPE_RESOLUTION.value();
}
