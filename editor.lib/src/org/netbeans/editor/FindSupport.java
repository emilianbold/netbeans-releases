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

package org.netbeans.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.editor.lib2.search.EditorFindSupport;

/**
* Find management
*
* @author Miloslav Metelka
* @version 1.00
* @deprecated Without any replacement.
*/
public class FindSupport {

    public static final String REVERT_MAP = EditorFindSupport.REVERT_MAP;
    public static final String FIND_HISTORY_PROP = EditorFindSupport.FIND_HISTORY_PROP;
    public static final String FIND_HISTORY_CHANGED_PROP = EditorFindSupport.FIND_HISTORY_CHANGED_PROP;
    
    /** Shared instance of FindSupport class */
    static FindSupport findSupport;

    /** Support for firing change events */
    WeakPropertyChangeSupport changeSupport = new WeakPropertyChangeSupport();
    
    SearchPatternWrapper lastSelected;
    List historyList;/*<SearchPatternWrapper>*/

    private FindSupport() {
        // prevent instance creation
    }

    /** Get shared instance of find support */
    public static FindSupport getFindSupport() {
        if (findSupport == null) {
            findSupport = new FindSupport();
        }
        return findSupport;
    }

    public Map getDefaultFindProperties() {
        return EditorFindSupport.getInstance().createDefaultFindProperties();
    }

    public Map getFindProperties() {
        return EditorFindSupport.getInstance().getFindProperties();
    }

    /** Get find property with specified name */
    public Object getFindProperty(String name) {
        return EditorFindSupport.getInstance().getFindProperty(name);
    }

    int[] getBlocks(int[] blocks, BaseDocument doc,
                    int startPos, int endPos) throws BadLocationException {
        return EditorFindSupport.getInstance().getBlocks(blocks, doc, startPos, endPos);
    }

    /** Set find property with specified name and fire change.
    */
    public void putFindProperty(String name, Object newValue) {
        EditorFindSupport.getInstance().putFindProperty(name, newValue);
    }

    /** Add/replace properties from some other map
    * to current find properties. If the added properties
    * are different than the original ones,
    * the property change is fired.
    */
    public void putFindProperties(Map propsToAdd) {
        EditorFindSupport.getInstance().putFindProperties(propsToAdd);
    }
    
    public void setBlockSearchHighlight(int startSelection, int endSelection){
        EditorFindSupport.getInstance().setBlockSearchHighlight(startSelection, endSelection);
    }
    
    public boolean incSearch(Map props, int caretPos) {
        return EditorFindSupport.getInstance().incSearch(props, caretPos);
    }

    public void incSearchReset() {
        EditorFindSupport.getInstance().incSearchReset();
    }
    
    /** Find the text from the caret position.
    * @param props search properties
    * @param oppositeDir whether search in opposite direction
    */
    public boolean find(Map props, boolean oppositeDir) {
        return EditorFindSupport.getInstance().find(props, oppositeDir);
    }

    /** Find the searched expression
    * @param startPos position from which to search. It must be inside the block.
    * @param blockStartPos starting position of the block. It must
    *   be valid position greater or equal than zero. It must be lower than
    *   or equal to blockEndPos (except blockEndPos=-1).
    * @param blockEndPos ending position of the block. It can be -1 for the end
    *   of document. It must be greater or equal than blockStartPos (except blockEndPos=-1).
    * @param props search properties
    * @param oppositeDir whether search in opposite direction
    * @param displayWrap whether display messages about the wrapping
    * @return either null when nothing was found or integer array with three members
    *    ret[0] - starting position of the found string
    *    ret[1] - ending position of the found string
    *    ret[2] - 1 or 0 when wrap was or wasn't performed in order to find the string 
    */
    public int[] findInBlock(JTextComponent c, int startPos, int blockStartPos,
        int blockEndPos, Map props, boolean oppositeDir) throws BadLocationException
    {
        return EditorFindSupport.getInstance().findInBlock(
            c, startPos, blockStartPos, blockEndPos, props, oppositeDir);
    }

    public boolean replace(Map props, boolean oppositeDir) throws BadLocationException {
        return EditorFindSupport.getInstance().replace(props, oppositeDir);
    }

    public void replaceAll(Map props) {
        EditorFindSupport.getInstance().replaceAll(props);
    }

// TODO: remove
//    /** Get position of wrap mark for some document */
//    public int getWrapSearchMarkPos(BaseDocument doc) {
//        return EditorFindSupport.getInstance().getWrapSearchMarkPos(doc);
//    }
//
//    /** Set new position of wrap mark for some document */
//    public void setWrapSearchMarkPos(BaseDocument doc, int pos) {
//        EditorFindSupport.getInstance().setWrapSearchMarkPos(doc, pos);
//    }

    /** Add weak listener to listen to change of any property. The caller must
    * hold the listener object in some instance variable to prevent it
    * from being garbage collected.
    */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        EditorFindSupport.getInstance().addPropertyChangeListener(new WeakPropL(l));
    }

    public synchronized void addPropertyChangeListener(
        String findPropertyName, PropertyChangeListener l)
    {
        EditorFindSupport.getInstance().addPropertyChangeListener(
            findPropertyName, new WeakPropL(l));
    }

    /** Remove listener for changes in properties */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        // no-op
    }

    void firePropertyChange(String settingName, Object oldValue, Object newValue) {
        EditorFindSupport.getInstance().firePropertyChange(settingName, oldValue, newValue);
    }

    public void setHistory(List/*<SearchPatternWrapper>*/ spwList){
        EditorFindSupport.getInstance().setHistory(spwList);
    }
    
    public List/*<SearchPatternWrapper>*/ getHistory(){
        return EditorFindSupport.getInstance().getHistory();
    }
    
    public void setLastSelected(SearchPatternWrapper spw) {
        EditorFindSupport.SPW editorSpw = new EditorFindSupport.SPW(
            spw.getSearchExpression(), spw.isWholeWords(), spw.isMatchCase(), spw.isRegExp()
        );
        EditorFindSupport.getInstance().setLastSelected(editorSpw);
    }
    
    public SearchPatternWrapper getLastSelected(){
        EditorFindSupport.SPW spw =  EditorFindSupport.getInstance().getLastSelected();
        return new SearchPatternWrapper(spw.getSearchExpression(), spw.isWholeWords(), spw.isMatchCase(), spw.isRegExp());
    }
    
    public void addToHistory(SearchPatternWrapper spw){
        EditorFindSupport.SPW editorSpw = new EditorFindSupport.SPW(
            spw.getSearchExpression(), spw.isWholeWords(), spw.isMatchCase(), spw.isRegExp()
        );
        EditorFindSupport.getInstance().addToHistory(editorSpw);
    }
    
    public static class SearchPatternWrapper{
        private String searchExpression;
        private boolean wholeWords;
        private boolean matchCase;
        private boolean regExp;
        
        public SearchPatternWrapper(String searchExpression, boolean wholeWords,
            boolean matchCase, boolean regExp){
            this.searchExpression = searchExpression;
            this.wholeWords = wholeWords;
            this.matchCase = matchCase;
            this.regExp = regExp;
        }
        
        /** @return searchExpression */
        public String getSearchExpression(){
            return searchExpression;
        }

        /** @return true if the wholeWords parameter was used during search performing */
        public boolean isWholeWords(){
            return wholeWords;
        }

        /** @return true if the matchCase parameter was used during search performing */
        public boolean isMatchCase(){
            return matchCase;
        }

        /** @return true if the regExp parameter was used during search performing */
        public boolean isRegExp(){
            return regExp;
        }
        
        public boolean equals(Object obj){
            if (!(obj instanceof SearchPatternWrapper)){
                return false;
            }
            SearchPatternWrapper sp = (SearchPatternWrapper)obj;
            return (this.searchExpression.equals(sp.getSearchExpression()) &&
                    this.wholeWords == sp.isWholeWords() &&
                    this.matchCase == sp.isMatchCase() &&
                    this.regExp == sp.isRegExp());
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + (this.wholeWords ? 1:0);
            result = 37*result + (this.matchCase ? 1:0);
            result = 37*result + (this.regExp ? 1:0);
            result = 37*result + this.searchExpression.hashCode();
            return result;
        }
        
        public String toString(){
            StringBuffer sb = new StringBuffer("[SearchPatternWrapper:]\nsearchExpression:"+searchExpression);//NOI18N
            sb.append('\n');
            sb.append("wholeWords:");//NOI18N
            sb.append(wholeWords);
            sb.append('\n');
            sb.append("matchCase:");//NOI18N
            sb.append(matchCase);
            sb.append('\n');
            sb.append("regExp:");//NOI18N
            sb.append(regExp);
            return  sb.toString();
        }
    } // End of SearchPatternWrapper class
    
    private static final class WeakPropL extends WeakReference implements PropertyChangeListener {
        
        public WeakPropL(PropertyChangeListener origL) {
            super(origL);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener origL = (PropertyChangeListener) get();
            if (origL != null) {
                origL.propertyChange(new PropertyChangeEvent(this, evt.getPropertyName(), 
                    convert(evt.getOldValue()), convert(evt.getNewValue())));
            } else {
                EditorFindSupport.getInstance().removePropertyChangeListener(this);
            }
        }
        
        private Object convert(Object o) {
            if (o instanceof EditorFindSupport.SPW) {
                EditorFindSupport.SPW spw = (EditorFindSupport.SPW) o;
                return new SearchPatternWrapper(
                    spw.getSearchExpression(), spw.isWholeWords(), spw.isMatchCase(), spw.isRegExp());
            } else {
                return o;
            }
        }
    } // End of WeakPropL class
}
