/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * PPCompletionProvider.java
 *
 * Created on July 18, 2005, 2:45 PM
 *
 */
package org.netbeans.modules.mobility.editor;

import java.io.IOException;
import java.io.StringReader;
import org.netbeans.api.project.Project;

import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.mobility.antext.preprocessor.*;
import org.openide.util.NbBundle;

import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.util.*;


/**
 * @author bohemius
 */
public final class PPCompletionProvider implements CompletionProvider {
    
    private int myQueryType;
    public static final int DIRECTIVE_COMPLETION_QUERY = 998;
    public static final int VARIABLE_COMPLETION_QUERY = 999;
    
    final private static String DEBUG="debug";
    final private static String ELSE="else";
    final private static String ENDIF="endif";
    final private static String ENDDEBUG="enddebug";
    final private static String PREFIX="//#";
    final private static String DESCDIR="DESC_DIRECTIVE_CC_";
    
    public static final String[] DEBUG_LEVELS = {DEBUG, "info", "warn", "error", "fatal"}; //NOI18N
    
    private static final String[] operatorTokens = {"==", "<=", ">=", "!=", "&&", "||"};
    private static final char[] operatorChars = {'^',',','@','('};
    private final static String[] directives = {"if", "ifdef", "ifndef", "elifdef", "elifndef", "elif", ELSE, ENDIF, DEBUG,
    "mdebug", ENDDEBUG, "define", "undefine", "condition"};
        
    public int getAutoQueryTypes(final JTextComponent component, final String typedText) {
        int ret = 0;
        
        if (typedText != null && typedText.endsWith("#")) {// NOI18N
            ret |= COMPLETION_QUERY_TYPE;
            this.myQueryType = DIRECTIVE_COMPLETION_QUERY;
        }
        if (typedText != null && typedText.endsWith(" ")) {
            ret |= COMPLETION_QUERY_TYPE;
            this.myQueryType = VARIABLE_COMPLETION_QUERY;
        }
        
        final String line = getLine(component);
        if (!canBeValidPPLine(line))
            ret = 0;
        Project p = (J2MEProjectUtils.getProjectForDocument(component.getDocument()));
        ProjectConfigurationsHelper hlp = p==null ? null : p.getLookup().lookup(ProjectConfigurationsHelper.class);
        if (hlp == null || !hlp.isPreprocessorOn())
            ret = 0;
        return ret;
    }
    
    public static String getLine(final JTextComponent component) {
        return getLine(component, component.getCaret().getDot());
    }
    
    public static String getLine(final JTextComponent component, final int offset) {
        final BaseDocument workingDocument = org.netbeans.editor.Utilities.getDocument(component);
        try {
            final int lineStartOffset = org.netbeans.editor.Utilities.getRowStart(workingDocument, offset);
            final int lineEndOffset = org.netbeans.editor.Utilities.getRowEnd(workingDocument, offset);
            return String.valueOf(workingDocument.getChars(lineStartOffset, lineEndOffset - lineStartOffset));
        } catch (BadLocationException ble) {
            return "";
        }
    }
    
    public static boolean canBeValidPPLine(final String line) {
        final StringTokenizer st = new StringTokenizer(line);
        boolean flag = false;
        while (st.hasMoreElements()) {
            final String s = st.nextToken();
            if (s.startsWith(PREFIX)) {
                if (flag) return false;
                flag = true;
            }
        }
        return flag;
    }
    
    public static boolean isLegalOperatorToken(final String op) {
        for (int i = 0; i < operatorTokens.length; i++) {
            if (operatorTokens[i].equals(op)) return true;
        }
        return false;
    }
    
    public static boolean isLegalOperatorChar(final char op) {
        for (int i = 0; i < operatorChars.length; i++) {
            if (operatorChars[i]==op) return true;
        }
        return false;
    }
    
    public static boolean isValidDirective(final String directive) {
        for (int i = 0; i < directives.length; i++) {
            if (directive.equals(directives[i]))
                return true;
        }
        return false;
    }
    
    public static boolean canBeValidDirective(final String directive) {
        for (int i = 0; i < directives.length; i++) {
            if (directives[i].startsWith(directive))
                return true;
        }
        return false;
    }
    
    public static boolean canBeValidVariable(final String variable) {
        final PreprocessorScanner ps = new PreprocessorScanner(new StringReader(variable));
        ps.yybegin(PreprocessorScanner.COMMAND);
        PPToken ppt = null;
        try {
            ppt = ps.nextToken();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return (ppt != null && ppt.getType() == LineParserTokens.ABILITY);
    }
    
    
    public CompletionTask createTask(final int queryType, final JTextComponent component) {
        Project p = (J2MEProjectUtils.getProjectForDocument(component.getDocument()));
        ProjectConfigurationsHelper hlp = p==null ? null : p.getLookup().lookup(ProjectConfigurationsHelper.class);
        if (hlp != null && hlp.isPreprocessorOn()) {
            try {
                final int offset = component.getCaret().getDot();
                final String line = getLine(component, offset);
                
                if (canBeValidPPLine(line)) {
                    final int ancor = org.netbeans.editor.Utilities.getPreviousWord(component, offset);
                    final String word = component.getDocument().getText(ancor, offset - ancor);
                    if (canBeValidDirective(word)) {
                        //The variable name could still starts with "el" for example so have to check that //# is before the directive
                        if (component.getDocument().getText(ancor - 3, 3).equals(PREFIX)) {
                            this.myQueryType = DIRECTIVE_COMPLETION_QUERY;
                        } else
                            this.myQueryType = VARIABLE_COMPLETION_QUERY;
                    } else if (PREFIX.equals(word))
                        this.myQueryType = DIRECTIVE_COMPLETION_QUERY;
                    else
                        this.myQueryType = VARIABLE_COMPLETION_QUERY;
                } else
                    return null;
            } catch (BadLocationException e) {
            }
            if (queryType == COMPLETION_QUERY_TYPE && this.myQueryType == DIRECTIVE_COMPLETION_QUERY)
                return new AsyncCompletionTask(new DirectiveQuery(), component);
            if (queryType == COMPLETION_QUERY_TYPE && this.myQueryType == VARIABLE_COMPLETION_QUERY)
                return new AsyncCompletionTask(new VariableQuery(), component);
        }
        return null;
    }
    
    static final class VariableQuery extends AsyncCompletionQuery {
        
        private ArrayList<PPVariableCompletionItem> debugItems, variableItems;
        private Set<String> variableSet;
        private JTextComponent component;
        private ProjectConfigurationsHelper pch;
        
        private String filterPrefix;
        private int ancor;
        
        protected void prepareQuery(final JTextComponent component) {
            this.component = component;
            this.variableItems = new ArrayList<PPVariableCompletionItem>();
            this.debugItems = new ArrayList<PPVariableCompletionItem>();
            this.variableSet = new TreeSet<String>();
            
            this.pch = J2MEProjectUtils.getCfgHelperForDoc(component.getDocument());
            
            final ProjectConfiguration devConfigs[] = pch.getConfigurations().toArray(new ProjectConfiguration[0]);
            for (int i = 0; i < devConfigs.length; i++) {
                variableItems.add(new PPVariableCompletionItem(devConfigs[i].getDisplayName(),
                        NbBundle.getMessage(PPCompletionProvider.class, "DESC_CONFIG_CC")));
                final Map<String,String> varMap = pch.getAbilitiesFor(devConfigs[i]);
                for ( final String varName : varMap.keySet() ) {
                    variableSet.add(varName);
                }
            }
            //now we have a set of unique variable names, so add them to the variableItems
            for ( final String varName : variableSet ) {
                variableItems.add(new PPVariableCompletionItem(varName, getVarInfo(varName)));
            }
            //add available debug levels
            for (int i = 0; i < DEBUG_LEVELS.length; i++)
                debugItems.add(new PPVariableCompletionItem(DEBUG_LEVELS[i],
                        NbBundle.getMessage(PPCompletionProvider.class, "DESC_DEBUG_CC_" + (i + 1))));
        }
        
        
        protected void query(final CompletionResultSet resultSet, @SuppressWarnings("unused")
		final Document doc, final int caretOffset) {
            try {
                final int ancor = Utilities.getPreviousWord(this.component, caretOffset);
                final PPToken tok = new PreprocessorScanner(new StringReader(org.netbeans.modules.mobility.editor.Utilities.getLine(this.component))).nextToken();
                if (canFilter(this.component)) {
                    this.ancor = ancor;
                    resultSet.setTitle(NbBundle.getMessage(PPCompletionProvider.class, "DESC_TITLE_VAR_CC"));
                    resultSet.setAnchorOffset(ancor);
                    //see what the previous directive word is and offer completion items accordingly
                    if (tok.getType() == LineParserTokens.COMMAND_DEBUG || tok.getType() == LineParserTokens.COMMAND_MDEBUG)
                        resultSet.addAllItems(getFilteredData(this.debugItems, filterPrefix));
                    else
                        resultSet.addAllItems(getFilteredData(this.variableItems, filterPrefix));
                } else
                    Completion.get().hideCompletion();
                
            } catch (BadLocationException ble) {
            } catch (IOException ioe) {
            }
            resultSet.finish();
        }
        
        protected boolean canFilter(final JTextComponent component) {
            filterPrefix = null;
            try {
                final int ancor = Utilities.getPreviousWord(component, component.getCaret().getDot());
                final String origFilterPrefix = component.getDocument().getText(ancor, component.getCaret().getDot() - ancor);
                filterPrefix = origFilterPrefix.trim();
                //Should do a check if the expression can be legal with something like boolean isLegalExpTemplate() which should be generated by JFlex
                if (!"".equals(filterPrefix) && (filterPrefix.length()==1 ? isLegalOperatorChar(filterPrefix.charAt(filterPrefix.length()-1)) : isLegalOperatorToken(filterPrefix.substring(filterPrefix.length()-2))))
                    filterPrefix = "";
                else {
                    if (origFilterPrefix.length() > filterPrefix.length()) {
                        if (isValidDirective(filterPrefix))
                            if (filterPrefix.equals(ENDIF) || filterPrefix.equals(ENDDEBUG) || filterPrefix.equals(ELSE))
                                filterPrefix = null;
                            else
                                filterPrefix = "";
                        else
                            filterPrefix = null;
                    } else {
                        if (isValidDirective(filterPrefix))
                            filterPrefix = null;
                        else if (!canBeValidVariable(filterPrefix))
                            filterPrefix = null;
                    }
                }
            } catch (BadLocationException e) {
                // filterPrefix stays null -> no filtering
            }
            return (filterPrefix != null);
        }
        
        protected void filter(final CompletionResultSet resultSet) {
            if (filterPrefix != null) {
                resultSet.setTitle(NbBundle.getMessage(PPCompletionProvider.class, "DESC_TITLE_VAR_CC"));
                resultSet.setAnchorOffset(this.ancor);
                resultSet.addAllItems(getFilteredData(this.variableItems, filterPrefix));
                resultSet.finish();
            }
        }
        
        private Collection<PPVariableCompletionItem> getFilteredData(final Collection<PPVariableCompletionItem> data, final String prefix) {
            final List<PPVariableCompletionItem> ret = new ArrayList<PPVariableCompletionItem>();
            for ( final PPVariableCompletionItem item : data ) {
                final String name = item.getSortText().toString();
                if (name.startsWith(prefix))
                    ret.add(item);
            }
            return ret;
        }
        
        private String getVarInfo(final Object variableName) {
            final Map<String,String> activeAbilities = pch.getAbilitiesFor(pch.getActiveConfiguration());
            if (!activeAbilities.containsKey(variableName))
                return NbBundle.getMessage(PPCompletionProvider.class, "DESC_UNDEFINED_CC");
            if (activeAbilities.get(variableName) == null)
                return NbBundle.getMessage(PPCompletionProvider.class, "DESC_DEFINED_CC");
         	String value = activeAbilities.get(variableName);
            if (value.length() > 30)
                value = value.substring(0, 29) + "...";
            return NbBundle.getMessage(PPCompletionProvider.class, "DESC_DEFINED_CC") + " <b>(" +
                    value + ")</b>";
        }
        
    }
    
    static final class DirectiveQuery extends AsyncCompletionQuery {
        
        private final static String[] directives = {"if", "ifdef", "ifndef", "elifdef", "elifndef", "elif", ENDIF, ELSE, DEBUG,
        "mdebug", ENDDEBUG, "define", "undefine", "condition"};
        private ArrayList<PPDirectiveCompletionItem> directiveItems;
        
        private String filterPrefix;
        private JTextComponent component;
        private int ancor;
        
        protected void query(final CompletionResultSet resultSet, @SuppressWarnings("unused")
		final Document doc, final int caretOffset) {
            try {
                final int ancor = Utilities.getPreviousWord(this.component, caretOffset);
                if (canFilter(this.component)) {
                    this.ancor = ancor;
                    resultSet.setTitle(NbBundle.getMessage(PPCompletionProvider.class, "DESC_TITLE_DIRE_CC"));
                    resultSet.setAnchorOffset(ancor);
                    resultSet.addAllItems(getFilteredData(this.directiveItems, filterPrefix));
                }
            } catch (BadLocationException ble) {
            }
            resultSet.finish();
        }
        
        protected boolean canFilter(final JTextComponent component) {
            filterPrefix = null;
            try {
                final int ancor = Utilities.getPreviousWord(component, component.getCaret().getDot());
                filterPrefix = component.getDocument().getText(ancor, component.getCaret().getDot() - ancor);
                if (PREFIX.equals(filterPrefix))
                    filterPrefix = "";
                else if (!canBeValidDirective(filterPrefix) || "".equals(filterPrefix))
                    filterPrefix = null;
            } catch (BadLocationException e) {
                // filterPrefix stays null -> no filtering
            }
            return (filterPrefix != null);
        }
        
        protected void filter(final CompletionResultSet resultSet) {
            if (filterPrefix != null) {
                resultSet.setTitle("Available directives");
                resultSet.setAnchorOffset(this.ancor);
                resultSet.addAllItems(getFilteredData(this.directiveItems, filterPrefix));
                resultSet.finish();
            }
        }
        
        private Collection<PPDirectiveCompletionItem> getFilteredData(final Collection<PPDirectiveCompletionItem> data, final String prefix) {
            final List<PPDirectiveCompletionItem> ret = new ArrayList<PPDirectiveCompletionItem>();
            for ( final PPDirectiveCompletionItem item : data ) {
                final String name = item.getSortText().toString();
                if (name.startsWith(prefix))
                    ret.add(item);
            }
            return ret;
        }
        
        @SuppressWarnings("unchecked")
		private PPBlockInfo getBlock(final JTextComponent component) {
            try {
                final BaseDocument doc = (BaseDocument) component.getDocument();
                final int lineNumber = Utilities.getLineOffset(doc, component.getCaret().getDot());
                final ArrayList<PPLine> lineList = (ArrayList) doc.getProperty(DocumentPreprocessor.PREPROCESSOR_LINE_LIST);
                if (lineList != null) {
                    PPLine myPpLine = null;
                    if (lineNumber >= 0 && lineNumber < lineList.size()) {
                        myPpLine = lineList.get(lineNumber);
                        if (myPpLine != null)
                            return myPpLine.getBlock();
                    }
                }
            } catch (BadLocationException ble) {
            }
            return null;
        }
        
        private boolean provideEndIf(final JTextComponent component) {
            final PPBlockInfo b = this.getBlock(component);
            return isIf(b) || isElse(b) || isElif(b);
        }
        
        private boolean provideElse(final JTextComponent component) {
            final PPBlockInfo b = this.getBlock(component);
            return isIf(b) || isElif(b);
        }
        
        private boolean isIf(final PPBlockInfo b) {
            return b != null && (b.getType() == PPLine.IF || b.getType() == PPLine.IFDEF || b.getType() == PPLine.IFNDEF);
        }
        
        private boolean isElse(final PPBlockInfo b) {
            return b != null && b.getType() == PPLine.ELSE;
        }
        
        private boolean isElif(final PPBlockInfo b) {
            return b != null && (b.getType() == PPLine.ELIF || b.getType() == PPLine.ELIFDEF || b.getType() == PPLine.ELIFNDEF);
        }
        
        protected void prepareQuery(final JTextComponent component) {
            this.component = component;
            directiveItems = new ArrayList<PPDirectiveCompletionItem>();
            for (int i = 0; i < directives.length; i++) {
                if (directives[i].startsWith("if") || directives[i].equals(DEBUG) || directives[i].equals("mdebug") || directives[i].equals(ENDDEBUG) ||
                        directives[i].equals("define") || directives[i].equals("undefine") || directives[i].equals("condition"))
                    directiveItems.add(new PPDirectiveCompletionItem(directives[i], NbBundle.getMessage(PPCompletionProvider.class, DESCDIR + (i + 1))));
                else {
                    if (provideEndIf(component) && directives[i].equals(ENDIF))
                        directiveItems.add(new PPDirectiveCompletionItem(directives[i], NbBundle.getMessage(PPCompletionProvider.class, DESCDIR + (i + 1))));
                    if (provideElse(component) && (directives[i].equals("elif") || directives[i].equals("elifdef") || directives[i].equals("elifndef")))
                        directiveItems.add(new PPDirectiveCompletionItem(directives[i], NbBundle.getMessage(PPCompletionProvider.class, DESCDIR + (i + 1))));
                    if (provideElse(component) && (directives[i].equals(ELSE)))
                        directiveItems.add(new PPDirectiveCompletionItem(directives[i], NbBundle.getMessage(PPCompletionProvider.class, DESCDIR + (i + 1))));
                }
            }
        }
        
    }
    
}
