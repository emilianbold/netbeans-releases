/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Picrosystems, Inc. Portions Copyright 1997-2006 Sun
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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.prefs.PreferenceChangeEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ViewFactory;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import java.io.CharArrayWriter;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.EditorKit;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;
import org.netbeans.modules.editor.lib.KitsTracker;
import org.netbeans.modules.editor.lib.NavigationHistory;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
* Editor kit implementation for base document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseKit extends DefaultEditorKit {

    /**
     * Flag indicating that the JTextComponent.paste() is in progress.
     * Checked in BaseDocument.read() to ignore clearing of the regions
     * for trailing-whitespace-removal.
     */
    static ThreadLocal<Boolean> IN_PASTE = new ThreadLocal<Boolean>();

    // -J-Dorg.netbeans.editor.BaseKit.level=FINEST
    private static final Logger LOG = Logger.getLogger(BaseKit.class.getName());
    
    /** split the current line at cursor position */
    public static final String splitLineAction = "split-line"; // NOI18N

    /** Cycle through annotations on the current line */
    public static final String annotationsCyclingAction = "annotations-cycling"; // NOI18N
    
    /** Collapse a fold. Depends on the current caret position. */
    public static final String collapseFoldAction = "collapse-fold"; //NOI18N
    
    /** Expand a fold. Depends on the current caret position. */
    public static final String expandFoldAction = "expand-fold"; //NOI18N
    
    /** Collapse all existing folds in the document. */
    public static final String collapseAllFoldsAction = "collapse-all-folds"; //NOI18N
    
    /** Expand all existing folds in the document. */
    public static final String expandAllFoldsAction = "expand-all-folds"; //NOI18N
    
    /** Move one page up and make or extend selection */
    public static final String selectionPageUpAction = "selection-page-up"; // NOI18N

    /** Move one page down and make or extend selection */
    public static final String selectionPageDownAction = "selection-page-down"; // NOI18N

    /** Remove indentation */
    public static final String removeTabAction = "remove-tab"; // NOI18N

    /** Remove selected block or do nothing - useful for popup menu */
    public static final String removeSelectionAction = "remove-selection"; // NOI18N

    /** Expand the abbreviation */
    public static final String abbrevExpandAction = "abbrev-expand"; // NOI18N

    /** Reset the abbreviation accounting string */
    public static final String abbrevResetAction = "abbrev-reset"; // NOI18N

    /** Remove the word */ 
    //public static final String removeWordAction = "remove-word"; #47709

    /** Remove characters to the begining of the word or 
     *  the previous word if caret is not directly at word */
    public static final String removePreviousWordAction = "remove-word-previous"; // NOI18N

    /** Remove characters to the end of the word or 
     *  the next word if caret is not directly at word */
    public static final String removeNextWordAction = "remove-word-next"; // NOI18N
    
    /** Remove to the begining of the line */
    public static final String removeLineBeginAction = "remove-line-begin"; // NOI18N

    /** Remove line */
    public static final String removeLineAction = "remove-line"; // NOI18N
    
    /** Move selection else line up */
    /* package */ static final String moveSelectionElseLineUpAction = "move-selection-else-line-up"; // NOI18N
    
    /** Move selection else line down */
    /* package */ static final String moveSelectionElseLineDownAction = "move-selection-else-line-down"; // NOI18N
    
    /** Copy selection else line up */
    /* package */ static final String copySelectionElseLineUpAction = "copy-selection-else-line-up"; // NOI18N
    
    /** Copy selection else line down */
    /* package */ static final String copySelectionElseLineDownAction = "copy-selection-else-line-down"; // NOI18N

    /** Toggle the typing mode to overwrite mode or back to insert mode */
    public static final String toggleTypingModeAction = "toggle-typing-mode"; // NOI18N

    /** Change the selected text or current character to uppercase */
    public static final String toUpperCaseAction = "to-upper-case"; // NOI18N

    /** Change the selected text or current character to lowercase */
    public static final String toLowerCaseAction = "to-lower-case"; // NOI18N

    /** Switch the case of the selected text or current character */
    public static final String switchCaseAction = "switch-case"; // NOI18N

    /** Find next occurence action */
    public static final String findNextAction = "find-next"; // NOI18N

    /** Find previous occurence action */
    public static final String findPreviousAction = "find-previous"; // NOI18N

    /** Toggle highlight search action */
    public static final String toggleHighlightSearchAction = "toggle-highlight-search"; // NOI18N

    /** Find current word */
    public static final String findSelectionAction = "find-selection"; // NOI18N

    /** Undo action */
    public static final String undoAction = "undo"; // NOI18N

    /** Redo action */
    public static final String redoAction = "redo"; // NOI18N

    /** Word match next */
    public static final String wordMatchNextAction = "word-match-next"; // NOI18N

    /** Word match prev */
    public static final String wordMatchPrevAction = "word-match-prev"; // NOI18N

    /** Reindent Line action */
    public static final String reindentLineAction = "reindent-line"; // NOI18N

    /** Shift line right action */
    public static final String shiftLineRightAction = "shift-line-right"; // NOI18N

    /** Shift line left action */
    public static final String shiftLineLeftAction = "shift-line-left"; // NOI18N

    /** Action that scrolls the window so that caret is at the center of the window */
    public static final String adjustWindowCenterAction = "adjust-window-center"; // NOI18N

    /** Action that scrolls the window so that caret is at the top of the window */
    public static final String adjustWindowTopAction = "adjust-window-top"; // NOI18N

    /** Action that scrolls the window so that caret is at the bottom of the window */
    public static final String adjustWindowBottomAction = "adjust-window-bottom"; // NOI18N

    /** Action that moves the caret so that caret is at the center of the window */
    public static final String adjustCaretCenterAction = "adjust-caret-center"; // NOI18N

    /** Action that moves the caret so that caret is at the top of the window */
    public static final String adjustCaretTopAction = "adjust-caret-top"; // NOI18N

    /** Action that moves the caret so that caret is at the bottom of the window */
    public static final String adjustCaretBottomAction = "adjust-caret-bottom"; // NOI18N

    /** Format part of the document text using Indent */
    public static final String formatAction = "format"; // NOI18N

    /** First non-white character on the line */
    public static final String firstNonWhiteAction = "first-non-white"; // NOI18N

    /** Last non-white character on the line */
    public static final String lastNonWhiteAction = "last-non-white"; // NOI18N

    /** First non-white character on the line */
    public static final String selectionFirstNonWhiteAction = "selection-first-non-white"; // NOI18N

    /** Last non-white character on the line */
    public static final String selectionLastNonWhiteAction = "selection-last-non-white"; // NOI18N

    /** Select the nearest identifier around caret */
    public static final String selectIdentifierAction = "select-identifier"; // NOI18N

    /** Select the next parameter (after the comma) in the given context */
    public static final String selectNextParameterAction = "select-next-parameter"; // NOI18N

    /** Go to the previous position stored in the jump-list */
    public static final String jumpListNextAction = "jump-list-next"; // NOI18N

    /** Go to the next position stored in the jump-list */
    public static final String jumpListPrevAction = "jump-list-prev"; // NOI18N

    /** Go to the last position in the previous component stored in the jump-list */
    public static final String jumpListNextComponentAction = "jump-list-next-component"; // NOI18N

    /** Go to the next position in the previous component stored in the jump-list */
    public static final String jumpListPrevComponentAction = "jump-list-prev-component"; // NOI18N

    /** Scroll window one line up */
    public static final String scrollUpAction = "scroll-up"; // NOI18N

    /** Scroll window one line down */
    public static final String scrollDownAction = "scroll-down"; // NOI18N

    /** Prefix of all macro-based actions */
    public static final String macroActionPrefix = "macro-"; // NOI18N
    
    /** Start recording of macro. Only one macro recording can be active at the time */
    public static final String startMacroRecordingAction = "start-macro-recording"; //NOI18N
    
    /** Stop the active recording */
    public static final String stopMacroRecordingAction = "stop-macro-recording"; //NOI18N

    /** Name of the action moving caret to the first column on the line */
    public static final String lineFirstColumnAction = "caret-line-first-column"; // NOI18N

    /** Insert the current Date and Time  */
    public static final String insertDateTimeAction = "insert-date-time"; // NOI18N
    
    /** Name of the action moving caret to the first 
     * column on the line and extending the selection
     */
    public static final String selectionLineFirstColumnAction = "selection-line-first-column"; // NOI18N

    /** Name of the action for generating of Glyph Gutter popup menu*/
    public static final String generateGutterPopupAction = "generate-gutter-popup"; // NOI18N

    /** Toggle visibility of line numbers*/
    public static final String toggleLineNumbersAction = "toggle-line-numbers"; // NOI18N

    /** Paste and reformat code */
    public static final String pasteFormatedAction = "paste-formated"; // NOI18N

    /** Starts a new line in code */
    public static final String startNewLineAction = "start-new-line"; // NOI18N    
    
    /** Cut text from caret position to line begining action. */
    public static final String cutToLineBeginAction = "cut-to-line-begin"; // NOI18N    
    
    /** Cut text from caret position to line end action. */
    public static final String cutToLineEndAction = "cut-to-line-end"; // NOI18N
    
    /** Remove all trailing spaces in the document. */
    public static final String removeTrailingSpacesAction = "remove-trailing-spaces"; //NOI18N
    
    public static final String DOC_REPLACE_SELECTION_PROPERTY = "doc-replace-selection-property"; //NOI18N
    
    private static final int KIT_CNT_PREALLOC = 7;

    static final long serialVersionUID = -8570495408376659348L;

    private static final Map<Class, BaseKit> kits = new HashMap<Class, BaseKit>(KIT_CNT_PREALLOC);

    private static final Object KEYMAPS_AND_ACTIONS_LOCK = new String("BaseKit.KEYMAPS_AND_ACTIONS_LOCK"); //NOI18N
    private static final Map<MimePath, KeybindingsAndPreferencesTracker> keymapTrackers = new WeakHashMap<MimePath, KeybindingsAndPreferencesTracker>(KIT_CNT_PREALLOC);
    private static final Map<MimePath, MultiKeymap> kitKeymaps = new WeakHashMap<MimePath, MultiKeymap>(KIT_CNT_PREALLOC);
    private static final Map<MimePath, Action[]> kitActions = new WeakHashMap<MimePath, Action[]>(KIT_CNT_PREALLOC);
    private static final Map<MimePath, Map<String, Action>> kitActionMaps = new WeakHashMap<MimePath, Map<String, Action>>(KIT_CNT_PREALLOC);
    
    private static CopyAction copyActionDef = new CopyAction();
    private static CutAction cutActionDef = new CutAction();
    private static PasteAction pasteActionDef = new PasteAction(false);
    private static DeleteCharAction deletePrevCharActionDef = new DeleteCharAction(deletePrevCharAction, false);
    private static DeleteCharAction deleteNextCharActionDef = new DeleteCharAction(deleteNextCharAction, true);
    private static ActionFactory.RemoveSelectionAction removeSelectionActionDef = new ActionFactory.RemoveSelectionAction();
    private static final Action insertTabActionDef = new InsertTabAction();
    private static final Action removeTabActionDef = new ActionFactory.RemoveTabAction();
    private static final Action insertBreakActionDef = new InsertBreakAction();

    private static ActionFactory.UndoAction undoActionDef = new ActionFactory.UndoAction();
    private static ActionFactory.RedoAction redoActionDef = new ActionFactory.RedoAction();
    
    public static final int MAGIC_POSITION_MAX = Integer.MAX_VALUE - 1;

    private final SearchableKit searchableKit;

//    static SettingsChangeListener settingsListener = new SettingsChangeListener() {
//        public void settingsChange(SettingsChangeEvent evt) {
//            String settingName = (evt != null) ? evt.getSettingName() : null;
//
//            boolean clearActions = (settingName == null
//                    || SettingsNames.CUSTOM_ACTION_LIST.equals(settingName)
//                    || SettingsNames.MACRO_MAP.equals(settingName));
//
//            if (clearActions || SettingsNames.KEY_BINDING_LIST.equals(settingName)) {
//                kitKeymaps.clear();
//            }
//
//            if (clearActions) {
//                kitActions.clear();
//                kitActionMaps.clear();
//            } else { // only refresh action settings
//                Iterator i = kitActions.entrySet().iterator();
//                while (i.hasNext()) {
//                    Map.Entry me = (Map.Entry)i.next();
//                    updateActionSettings((Action[])me.getValue(), evt, (Class)me.getKey());
//                }
//            }
//        }
//    };
//
//    static {
//        Settings.addSettingsChangeListener(settingsListener);
//    }
    
    private void updateActionSettings(Action[] actions) {
        for(Action a : actions) {
            if (a instanceof BaseAction) {
                for(Method m : a.getClass().getDeclaredMethods()) {
                    if (m.getName().equals("settingsChange") && m.getParameterTypes().length == 2 && m.getParameterTypes()[1] == Class.class) { //NOI18N
                        try {
                            m.setAccessible(true);
                            m.invoke(a, null, getClass());
                        } catch (Exception e) {
                            LOG.log(Level.FINE, null, e);
                        }
                        break;
                    }
                }
//                Iterator i = kitActions.entrySet().iterator();
//                while (i.hasNext()) {
//                    Map.Entry me = (Map.Entry)i.next();
//                    updateActionSettings((Action[])me.getValue(), evt, (Class)me.getKey());
//                }
            }
        }
    }

    /**
     * Gets an editor kit from its implemetation class.
     * 
     * <p>Please be careful when using this method and make sure that you understand
     * how it works and what the deference is from using <code>MimeLookup</code>.
     * This method simply creates an instance of <code>BaseKit</code> from
     * its implementation class passed in as a parameter. It completely ignores
     * the registry of editor kits in <code>MimeLookup</code>, which has severe
     * consequences.
     * 
     * <div class="nonnormative">
     * <p>The usuall pattern for using editor kits is to start with a mime type
     * of a document (ie. file) that you want to edit, then use some registry
     * for editor kits to look up the kit for your mime type and finally set the
     * kit in a <code>JTextComponent</code>, let it create a <code>Document</code>
     * and load it with data. The registry can generally be anything, but in Netbeans
     * we use <code>MimeLookup</code> (JDK for example uses 
     * <code>JEditorPane.createEditorKitForContentType</code>).
     * 
     * <p>The editor kits are registered in <code>MimeLookup</code> for each
     * particular mime type and the registry itself does not impose any rules on
     * the editor kit implementations other than extending the <code>EditorKit</code>
     * class. This for example means that the same implemantation of <code>EditorKit</code>
     * can be used for multiple mime types. This is exactly how XML editor kit
     * is reused for various flavors of XML documents (e.g. ant build scripts,
     * web app descriptors, etc).
     * 
     * <p>Netbeans did not always have <code>MimeLookup</code>
     * and it also used a different approach for registering and retrieving
     * editor kits. This old approach was based on implemetation classes rather than on mime
     * types and while it is still more or less functional for the old kit
     * implementations, it is fundamentally broken and should not be used any more.
     * The code below demonstrates probably the biggest mistake when thinking
     * in the old ways.
     * 
     * <pre>
     * // WARNING: The code below is a demonstration of a common mistake that
     * // people do when using <code>BaseKit.getKit</code>.
     * 
     * JTextComponent component = ...; // Let's say we have a component
     * Class kitClass = Utilities.getKitClass(component);
     * String mimeType = BaseKit.getKit(kitClass).getContentType();
     * </pre>
     * 
     * <p>The problem with the above code is that it blindely assumes that each
     * kit class can be uniquely mapped to a mime type. This is not true! The
     * same can be achieved in much easier way, which always works.
     * 
     * <pre>
     * JTextComponent component = ...; // Let's say we have a component
     * String mimeType = component.getUI().getEditorKit(component).getContentType();
     * </pre>
     * </div>
     * 
     * @param kitClass An implementation class of the editor kit that should
     * be returned. If the <code>kitClass</code> is not <code>BaseKit</code> or
     * its subclass the instance of bare <code>BaseKit</code> will be returned.
     * 
     * @return An instance of the <code>kitClass</code> or <code>BaseKit</code>.
     * @deprecated Use <code>CloneableEditorSupport.getEditorKit</code> or
     * <code>MimeLookup</code> instead to find <code>EditorKit</code> for a mime
     * type.
     */
    public static BaseKit getKit(Class kitClass) {
        if (kitClass != null && BaseKit.class.isAssignableFrom(kitClass) && BaseKit.class != kitClass) {
            String mimeType = KitsTracker.getInstance().findMimeType(kitClass);
            if (mimeType != null) {
                EditorKit kit = MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(EditorKit.class);
                if (kit instanceof BaseKit) {
                    return (BaseKit) kit;
                }
            }
        } else {
            kitClass = BaseKit.class;
        }
        
        synchronized (kits) {
            Class classToTry = kitClass;
            for(;;) {
                BaseKit kit = (BaseKit)kits.get(classToTry);
                if (kit == null) {
                    try {
                        kit = (BaseKit)classToTry.newInstance();
                        kits.put(classToTry, kit);
                        return kit;
                    } catch (IllegalAccessException e) {
                        LOG.log(Level.WARNING, "Can't instantiate editor kit from: " + classToTry, e); //NOI18N
                    } catch (InstantiationException e) {
                        LOG.log(Level.WARNING, "Can't instantiate editor kit from: " + classToTry, e); //NOI18N
                    }
                    
                    if (classToTry != BaseKit.class) {
                        classToTry = BaseKit.class;
                    } else {
                        throw new IllegalStateException("Can't create editor kit for: " + kitClass); //NOI18N
                    }
                } else {
                    return kit;
                }
            }
        }
    }

    /**
     * Creates a new instance of <code>BaseKit</code>.
     * 
     * <div class="nonnormative">
     * <p>You should not need to instantiate editor kits
     * directly under normal circumstances. There is a few ways how you can get
     * instance of <code>EditorKit</code> depending on what you already have
     * available:
     * 
     * <ul>
     * <li><b>mime type</b> - Use <code>CloneableEditorSupport.getEditorKit(yourMimeType)</code>
     * to get the <code>EditorKit</code> registered for your mime type or use
     * the following code <code>MimeLookup.getLookup(MimePath.parse(yourMimeType)).lookup(EditorKit.class)</code>
     * and check for <code>null</code>.
     * <li><b>JTextComponent</b> - Simply call
     * <code>JTextComponent.getUI().getEditorKit(JTextComponent)</code> passing
     * in the same component.
     * </ul>
     * </div>
     */
    public BaseKit() {
        // possibly register
        synchronized (kits) {
            if (kits.get(this.getClass()) == null) {
                kits.put(this.getClass(), this); // register itself
            }
        }
        // Directly implementing searchable editor kit would require module dependency changes
        // of any modules using BaseKit reference so make a wrapper instead
        org.netbeans.modules.editor.lib2.actions.EditorActionUtilities.registerSearchableKit(this,
                searchableKit = new SearchableKit(this));
    }

    /** Clone this editor kit */
    public @Override Object clone() {
        return this; // no need to create another instance
    }

    /** Fetches a factory that is suitable for producing
     * views of any models that are produced by this
     * kit.  The default is to have the UI produce the
     * factory, so this method has no implementation.
     *
     * @return the view factory
     */
    public @Override ViewFactory getViewFactory() {
        return ((false || Boolean.getBoolean("org.netbeans.editor.linewrap")) &&
                !Boolean.getBoolean("org.netbeans.editor.linewrap.disable"))
                ? org.netbeans.modules.editor.lib2.view.ViewFactoryImpl.INSTANCE
                : null;
    }

    /** Create caret to navigate through document */
    public @Override Caret createCaret() {
        return new BaseCaret();
    }

    /** Create empty document */
    public @Override Document createDefaultDocument() {
        return new BaseDocument(this.getClass(), true);
    }

    /** 
     * Create new instance of syntax coloring scanner
     * @param doc document to operate on. It can be null in the cases the syntax
     *   creation is not related to the particular document
     * 
     * @deprecated Please use Lexer instead, for details see
     *   <a href="@org-netbeans-modules-lexer@/overview-summary.html">Lexer</a>.
     */
    public Syntax createSyntax(Document doc) {
        return new DefaultSyntax();
    }

    /** 
     * Create the syntax used for formatting.
     * 
     * @deprecated Please use Editor Indentation API instead, for details see
     *   <a href="@org-netbeans-modules-editor-indent@/overview-summary.html">Editor Indentation</a>.
     */
    public Syntax createFormatSyntax(Document doc) {
        return createSyntax(doc);
    }

    /** 
     * Create syntax support
     * 
     * @deprecated Please use Lexer instead, for details see
     *   <a href="@org-netbeans-modules-lexer@/overview-summary.html">Lexer</a>.
     */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new SyntaxSupport(doc);
    }

    /** 
     * Create the formatter appropriate for this kit
     * @deprecated Please use Editor Indentation API instead, for details see
     *   <a href="@org-netbeans-modules-editor-indent@/overview-summary.html">Editor Indentation</a>.
     */
    public Formatter createFormatter() {
        return new Formatter(this.getClass());
    }

    /** Create text UI */
    protected BaseTextUI createTextUI() {
        return new BaseTextUI();
    }

    /** Create extended UI */
    protected EditorUI createEditorUI() {
        return new EditorUI();
    }

    /**
     * Create extended UI for printing a document.
     * @deprecated this method is no longer being called by {@link EditorUI}.
     *  {@link #createPrintEditorUI(BaseDocument, boolean, boolean)} is being
     *  called instead.
     */
    protected EditorUI createPrintEditorUI(BaseDocument doc) {
        return new EditorUI(doc);
    }
    
    /**
     * Create extended UI for printing a document.
     *
     * @param doc document for which the extended UI is being created.
     * @param usePrintColoringMap use printing coloring settings instead
     *  of the regular ones.
     * @param lineNumberEnabled if set to false the line numbers will not be printed.
     *  If set to true the visibility of line numbers depends on the settings
     *  for the line number visibility.
     */
    protected EditorUI createPrintEditorUI(BaseDocument doc,
    boolean usePrintColoringMap, boolean lineNumberEnabled) {
        
        return new EditorUI(doc, usePrintColoringMap, lineNumberEnabled);
    }

    public MultiKeymap getKeymap() {
        synchronized (KEYMAPS_AND_ACTIONS_LOCK) {
            MimePath mimePath = MimePath.parse(getContentType());
            MultiKeymap km = (MultiKeymap)kitKeymaps.get(mimePath);
            
            if (km == null) { // keymap not yet constructed
                // construct new keymap
                km = new MultiKeymap("Keymap for " + mimePath.getPath()); // NOI18N
                
                // retrieve key bindings for this kit and super kits
                KeyBindingSettings kbs = MimeLookup.getLookup(mimePath).lookup(KeyBindingSettings.class);
                List<org.netbeans.api.editor.settings.MultiKeyBinding> mkbList = kbs.getKeyBindings();
                List<JTextComponent.KeyBinding> editorMkbList = new  ArrayList<JTextComponent.KeyBinding>();
                
                for(org.netbeans.api.editor.settings.MultiKeyBinding mkb : mkbList) {
                    List<KeyStroke> keyStrokes = mkb.getKeyStrokeList();
                    MultiKeyBinding editorMkb = new MultiKeyBinding(keyStrokes.toArray(new KeyStroke[keyStrokes.size()]), mkb.getActionName());
                    editorMkbList.add(editorMkb);
                }
                
                // go through all levels and collect key bindings
                km.load(editorMkbList.toArray(new JTextComponent.KeyBinding[editorMkbList.size()]), getActionMap());
                km.setDefaultAction(getActionMap().get(defaultKeyTypedAction));

                kitKeymaps.put(mimePath, km);
            }
            
            return km;
        }
    }

    /** Inserts content from the given stream. */
    public @Override void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        if (doc instanceof BaseDocument) {
            ((BaseDocument)doc).read(in, pos); // delegate it to document
        } else {
            super.read(in, doc, pos);
        }
    }

    /** Writes content from a document to the given stream */
    public @Override void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
        if (doc instanceof BaseDocument) {
            ((BaseDocument)doc).write(out, pos, len);
        } else {
            super.write(out, doc, pos, len);
        }
    }

    /** Creates map with [name, action] pairs from the given
    * array of actions.
    */
    public static void addActionsToMap(Map<String, Action> map, Action[] actions, String logActionsType) {
        boolean fineLoggable = LOG.isLoggable(Level.FINE);
        if (fineLoggable) {
            LOG.fine(logActionsType + " start --------------------\n");
        }
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            if (a == null) {
                LOG.info("actions[] contains null at index " + i +
                        ((i > 0) ? ". Preceding action is " + actions[i - 1] : "."));
                continue;
            }
            String name = (String) a.getValue(Action.NAME);
            if (name == null) {
                LOG.info("Null Action.NAME property of action " + a);
                continue;
            }

            if (fineLoggable) {
                String overriding = map.containsKey(name) ? " OVERRIDING\n" : "\n"; // NOI18N
                LOG.fine("    " + name + ": " + a + overriding); // NOI18N
            }

            map.put(name, a); // NOI18N
        }
        if (fineLoggable) {
            LOG.fine(logActionsType + " end ----------------------\n");
        }
    }

    /** Converts map with [name, action] back
    * to array of actions.
    */
    public static Action[] mapToActions(Map map) {
        Action[] actions = new Action[map.size()];
        int i = 0;
        for (Iterator iter = map.values().iterator() ; iter.hasNext() ;) {
            actions[i++] = (Action)iter.next();
        }
        return actions;
    }

    /** Called after the kit is installed into JEditorPane */
    public @Override void install(JEditorPane c) {
        
        assert (SwingUtilities.isEventDispatchThread()) // expected in AWT only
            : "BaseKit.install() incorrectly called from non-AWT thread."; // NOI18N

        BaseTextUI ui = createTextUI();
        c.setUI(ui);

        String propName = "netbeans.editor.noinputmethods"; // NOI18N
        Object noInputMethods = System.getProperty(propName);
        boolean enableIM;
        if (noInputMethods != null) {
            enableIM = !Boolean.getBoolean(propName);
        } else {
            Preferences prefs = MimeLookup.getLookup(getContentType()).lookup(Preferences.class);
            enableIM = prefs.getBoolean(EditorPreferencesKeys.INPUT_METHODS_ENABLED, EditorPreferencesDefaults.defaultInputMethodsEnabled); //NOI18N
        }

        c.enableInputMethods(enableIM);
        
        org.netbeans.lib.editor.hyperlink.HyperlinkOperation.ensureRegistered(c, getContentType());

        // Mark that the editor's multi keymap adheres to context API in status displayer
        c.putClientProperty("context-api-aware", Boolean.TRUE); // NOI18N
        
        // Add default help IDs derived from the kit's mime type, #61618.
        // If the kit itself is HelpCtx.Provider it will be called from CloneableEditor.getHelpCtx()
        if (!(this instanceof HelpCtx.Provider)) {
            HelpCtx.setHelpIDString(c, getContentType().replace('/', '.').replace('+', '.')); //NOI18N
        }
        
        // setup the keymap tracker and initialize the keymap
        MultiKeymap keymap;
        synchronized (KEYMAPS_AND_ACTIONS_LOCK) {
            MimePath mimePath = MimePath.get(getContentType());
            KeybindingsAndPreferencesTracker tracker = keymapTrackers.get(mimePath);
            if (tracker == null) {
                tracker = new KeybindingsAndPreferencesTracker(mimePath.getPath());
                keymapTrackers.put(mimePath, tracker);
            }
            tracker.addComponent(c);
            keymap = getKeymap();
        }
        
        c.setKeymap(keymap);
        
        executeInstallActions(c);
    }

    protected void executeInstallActions(JEditorPane c) {
        MimePath mimePath = MimePath.parse(getContentType());
        Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);

        @SuppressWarnings("unchecked")
        List<String> actionNamesList = (List<String>) SettingsConversions.callFactory(
                prefs, mimePath, EditorPreferencesKeys.KIT_INSTALL_ACTION_NAME_LIST, null); //NOI18N
        
        List<Action> actionsList = translateActionNameList(actionNamesList); // translate names to actions
        for (Action a : actionsList) {
            a.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
        }
    }

    public @Override void deinstall(JEditorPane c) {
        assert (SwingUtilities.isEventDispatchThread()) // expected in AWT only
            : "BaseKit.deinstall() incorrectly called from non-AWT thread."; // NOI18N

        executeDeinstallActions(c);
        
        // reset the keymap and remove the component from the tracker
        c.setKeymap(null);
        synchronized (KEYMAPS_AND_ACTIONS_LOCK) {
            MimePath mimePath = MimePath.get(getContentType());
            KeybindingsAndPreferencesTracker tracker = keymapTrackers.get(mimePath);
            if (tracker != null) {
                tracker.removeComponent(c);
            }
        }
        
        BaseTextUI.uninstallUIWatcher(c);
        c.updateUI();
        
        // #41209: reset ancestor override flag if previously set
        if (c.getClientProperty("ancestorOverride") != null) { // NOI18N
            c.putClientProperty("ancestorOverride", Boolean.FALSE); // NOI18N
        }
    }

    protected void executeDeinstallActions(JEditorPane c) {
        MimePath mimePath = MimePath.parse(getContentType());
        Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
        
        @SuppressWarnings("unchecked")
        List<String> actionNamesList = (List<String>) SettingsConversions.callFactory(
                prefs, mimePath, EditorPreferencesKeys.KIT_DEINSTALL_ACTION_NAME_LIST, null); //NOI18N
        
        List<Action> actionsList = translateActionNameList(actionNamesList); // translate names to actions
        for (Action a : actionsList) {
            a.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
        }
    }

    /** Initialize document by adding the draw-layers for example. */
    protected void initDocument(BaseDocument doc) {
    }

    /** Create actions that this kit supports. To use the actions of the parent kit
    * it's better instead of using super.createActions() to use
    * getKit(super.getClass()).getActions() because it can reuse existing
    * parent actions.
    */
    protected Action[] createActions() {
        return new Action[] {
                   // new DefaultKeyTypedAction() - overriden in ExtKit
                   insertBreakActionDef,
                   insertTabActionDef,
                   deletePrevCharActionDef,
                   deleteNextCharActionDef,
                   cutActionDef,
                   copyActionDef,
                   pasteActionDef,
                   new PasteAction(true),
                   removeTabActionDef,
                   //new ActionFactory.RemoveWordAction(), #47709
                   removeSelectionActionDef,
                   undoActionDef,
                   redoActionDef,
                   //new ActionFactory.ToggleLineNumbersAction(),
                   new NextWordAction(nextWordAction),
                   new NextWordAction(selectionNextWordAction),
                   new PreviousWordAction(previousWordAction),
                   new PreviousWordAction(selectionPreviousWordAction),
                   new ActionFactory.RemoveWordNextAction(),
                   new ActionFactory.RemoveWordPreviousAction(),
                   new ActionFactory.ToggleHighlightSearchAction(),

                   // Self test actions
                   //      new EditorDebug.SelfTestAction(),
                   //      new EditorDebug.DumpPlanesAction(),
                   //      new EditorDebug.DumpSyntaxMarksAction()
               };
    }

    protected Action[] getCustomActions() {
        MimePath mimePath = MimePath.parse(getContentType());
        Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
        
        @SuppressWarnings("unchecked")
        List<? extends Action> customActions = (List<? extends Action>) SettingsConversions.callFactory(
                prefs, mimePath, EditorPreferencesKeys.CUSTOM_ACTION_LIST, null); //NOI18N
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(EditorPreferencesKeys.CUSTOM_ACTION_LIST + " for '" + getContentType() + "' {"); //NOI18N
            if (customActions != null) {
                for(Action a : customActions) {
                    LOG.fine("    " + a); //NOI18N
                }
            }
            LOG.fine("} End of " + EditorPreferencesKeys.CUSTOM_ACTION_LIST + " for '" + getContentType() + "'"); //NOI18N
        }
        
        return customActions == null ? null : customActions.toArray(new Action[customActions.size()]);
    }
    
    /**
     * @deprecated Without any replacement. 
     */
    protected Action[] getMacroActions() {
        return new Action[0];
    }                               

    /** Get actions associated with this kit. createActions() is called
    * to get basic list and then customActions are added.
    */
    public @Override final Action[] getActions() {
        return (Action []) addActionsToMap()[0];
    }

    /* package */ Map<String, Action> getActionMap() {
        return (Map<String, Action>) addActionsToMap()[1];
    }

    private Object[] addActionsToMap() {
        synchronized (KEYMAPS_AND_ACTIONS_LOCK) {
            MimePath mimePath = MimePath.parse(getContentType());
            Action[] actions = kitActions.get(mimePath);
            Map<String, Action> actionMap = kitActionMaps.get(mimePath);
            
            if (actions == null || actionMap == null) {
                // Initialize actions - use the following actions:
                // 1. Declared "global" actions (declared in the xml layer under "Editors/Actions")
                // 2. Declared "mime-type" actions (declared in the xml layer under "Editors/mime-type/Actions")
                // 3. Result of createActions()
                // 4. Custom actions (EditorPreferencesKeys.CUSTOM_ACTION_LIST)
                // Higher levels override actions with same Action.NAME
                actions = getDeclaredActions(); // non-null
                actionMap = new HashMap<String, Action>(actions.length << 1);
                addActionsToMap(actionMap, actions, "Declared actions"); // NOI18N

                Action[] createActionsMethodResult = createActions();
                if (createActionsMethodResult != null) {
                    addActionsToMap(actionMap, createActionsMethodResult, "Actions from createActions()"); // NOI18N
                }
                
                // add custom actions
                Action[] customActions = getCustomActions();
                if (customActions != null) {
                    addActionsToMap(actionMap, customActions, "Custom actions"); // NOI18N
                }

                actions = actionMap.values().toArray(new Action[actionMap.values().size()]);

                kitActions.put(mimePath, actions);
                kitActionMaps.put(mimePath, actionMap);

                // At this moment the actions are constructed completely
                // The actions will be updated now if necessary
                updateActionSettings(actions);
                updateActions();
            }
            
            return new Object [] { actions, actionMap };
        }
    }

    /**
     * Get actions declared in the xml layer. They may be overriden by result
     * of <code>createActions()</code> and finally by result of <code>getCustomActions()</code>.
     *
     * @return non-null list of declared actions.
     */
    protected Action[] getDeclaredActions() {
        return new Action[0];
    }
    
    /** Update the actions right after their creation was finished.
     * The <code>getActions()</code> and <code>getActionByName()</code>
     * can be used safely in this method.
     * The implementation must call <code>super.updateActions()</code> so that
     * the updating in parent is performed too.
     */
    protected void updateActions() {
    }

    /** Get action from its name. */
    public Action getActionByName(String name) {
        return (name != null) ? (Action)getActionMap().get(name) : null;
    }

    public List<Action> translateActionNameList(List<String> actionNameList) {
        List<Action> ret = new ArrayList<Action>();
        if (actionNameList != null) {
            for(String actionName : actionNameList) {
                Action a = getActionByName(actionName);
                if (a != null) {
                    ret.add(a);
                }
            }
        }
        return ret;
    }



    /** Default typed action */
//    @EditorActionRegistration(name = defaultKeyTypedAction)
    public static class DefaultKeyTypedAction extends LocalBaseAction {

        static final long serialVersionUID =3069164318144463899L;

        public DefaultKeyTypedAction() {
            // Construct with defaultKeyTypedAction name to retain full compatibility for extending actions
            super(defaultKeyTypedAction, MAGIC_POSITION_RESET | CLEAR_STATUS_TEXT);
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
            LOG.fine("DefaultKeyTypedAction with enhanced logging, see issue #145306"); //NOI18N
        }

        private static final boolean isMac = System.getProperty("mrj.version") != null; //NOI18N

        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if ((target != null) && (evt != null)) {

                // Check whether the modifiers are OK
                int mod = evt.getModifiers();
                boolean ctrl = ((mod & ActionEvent.CTRL_MASK) != 0);
                // On the mac, norwegian and french keyboards use Alt to do bracket characters.
                // This replicates Apple's modification DefaultEditorKit.DefaultKeyTypedAction
                boolean alt = isMac ? ((mod & ActionEvent.META_MASK) != 0) : 
                    ((mod & ActionEvent.ALT_MASK) != 0);
                
                
                if (alt || ctrl) {
                    return;
                }
                
                // Check whether the target is enabled and editable
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                final EditorUI editorUI = Utilities.getEditorUI(target);
                // determine if typed char is valid
                final String cmd = evt.getActionCommand();
                if ((cmd != null) && (cmd.length() == 1)) {
                    //          Utilities.clearStatusText(target);

                    try {
                        NavigationHistory.getEdits().markWaypoint(target, caret.getDot(), false, true);
                    } catch (BadLocationException e) {
                        LOG.log(Level.WARNING, "Can't add position to the history of edits.", e); //NOI18N
                    }
                    
                    doc.runAtomicAsUser (new Runnable () {
                        public void run () {
                            DocumentUtilities.setTypingModification(doc, true);
                            try {
                                char ch = cmd.charAt(0);
                                if ((ch >= 0x20) && (ch != 0x7F)) { // valid character
                                    LOG.fine("Processing command char: " + Integer.toHexString(ch)); //NOI18N

                                    editorUI.getWordMatch().clear(); // reset word matching
                                    Boolean overwriteMode = (Boolean)editorUI.getProperty(
                                                                EditorUI.OVERWRITE_MODE_PROPERTY);
                                    try {
                                        boolean doInsert = true; // editorUI.getAbbrev().checkAndExpand(ch, evt);
                                        if (doInsert) {
                                            if (Utilities.isSelectionShowing(caret)) { // valid selection
                                                boolean ovr = (overwriteMode != null && overwriteMode.booleanValue());
                                                try {
                                                    doc.putProperty(DOC_REPLACE_SELECTION_PROPERTY, true);
                                                    replaceSelection(target, caret.getDot(), caret, cmd, ovr);
                                                } finally {
                                                    doc.putProperty(DOC_REPLACE_SELECTION_PROPERTY, null);
                                                }
                                            } else { // no selection
                                                int dotPos = caret.getDot();
                                                if (overwriteMode != null && overwriteMode.booleanValue()
                                                        && dotPos < doc.getLength() && doc.getChars(dotPos, 1)[0] != '\n'
                                                   ) { // overwrite current char
                                                    insertString(doc, dotPos, caret, cmd, true); 
                                                } else { // insert mode
                                                    insertString(doc, dotPos, caret, cmd, false);
                                                }
                                            }
                                        }
                                    } catch (BadLocationException e) {
                                        LOG.log(Level.FINE, null, e);
                                        target.getToolkit().beep();
                                    }
                                } else {
                                    LOG.fine("Invalid command char: " + Integer.toHexString(ch)); //NOI18N
                                }

                                checkIndent(target, cmd);
                            } finally {
                                DocumentUtilities.setTypingModification(doc, false);
                            }
                        }
                    });
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        StringBuilder sb = new StringBuilder();
                        for(int i = 0; i < cmd.length(); i++) {
                            String hex = Integer.toHexString(cmd.charAt(i));
                            sb.append(hex);
                            if (i + 1 < cmd.length()) {
                                sb.append(" ");
                            }
                        }
                        LOG.fine("Invalid command: '" + sb + "'"); //NOI18N
                    }                    
                }
            }
        }

      /** 
       * Hook to insert the given string at the given position into
       * the given document in insert-mode, no selection, writeable
       * document. Designed to be overridden by subclasses that want
       * to intercept inserted characters.
       */
      protected void insertString(BaseDocument doc,  
				  int dotPos, 
				  Caret caret,
				  String str, 
				  boolean overwrite) 
	throws BadLocationException 
      {
	if (overwrite) doc.remove(dotPos, 1);
	doc.insertString(dotPos, str, null);
      }

      /** 
       * Hook to insert the given string at the given position into
       * the given document in insert-mode with selection visible
       * Designed to be overridden by subclasses that want
       * to intercept inserted characters.
       */
      protected void replaceSelection(JTextComponent target,  
				  int dotPos, 
				  Caret caret,
				  String str, 
				  boolean overwrite) 
	throws BadLocationException 
      {
          target.replaceSelection(str);
      }


        /** Check whether there was any important character typed
        * so that the line should be possibly reformatted.
        */
        protected void checkIndent(JTextComponent target, String typedText) {
        }

    }

    public static class InsertBreakAction extends LocalBaseAction {

        static final long serialVersionUID =7966576342334158659L;

        public InsertBreakAction() {
            super(insertBreakAction, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final BaseDocument doc = (BaseDocument)target.getDocument();
                final Formatter formatter = doc.getFormatter();
                formatter.indentLock();
                try {
                    doc.runAtomicAsUser (new Runnable () {
                        public void run () {
                            DocumentUtilities.setTypingModification(doc, true);
                            try {
                                target.replaceSelection("");
                                Caret caret = target.getCaret();
                                Object cookie = beforeBreak(target, doc, caret);

                                int dotPos = caret.getDot();
                                int newDotPos = formatter.indentNewLine(doc, dotPos);
                                caret.setDot(newDotPos);

                                afterBreak(target, doc, caret, cookie);
                            } finally {
                                DocumentUtilities.setTypingModification(doc, false);
                            }
                        }
                    });
                } finally {
                    formatter.indentUnlock();
                }
            }
        }

      /**
       * Hook called before any changes to the document. The value
       * returned is passed intact to the other hook.
       */
      protected Object beforeBreak(JTextComponent target, BaseDocument doc, Caret caret) { 
	return null;
      }

      /**
       * Hook called after the enter was inserted and cursor
       * repositioned. *data* is the object returned previously by
       * *beforeBreak* hook. By default null.
       */
      protected void afterBreak(JTextComponent target, BaseDocument doc, Caret caret, Object data) {
      }
    }

    @EditorActionRegistration(name = splitLineAction)
    public static class SplitLineAction extends LocalBaseAction {

        static final long serialVersionUID =7966576342334158659L;

        public SplitLineAction() {
	  super(MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final BaseDocument doc = (BaseDocument)target.getDocument();
                final Caret caret = target.getCaret();

                final Formatter formatter = doc.getFormatter();
                formatter.indentLock();
                try {
                    doc.runAtomicAsUser (new Runnable () {
                        public void run () {
                            DocumentUtilities.setTypingModification(doc, true);
                            try{
                                target.replaceSelection("");
                                final int dotPos = caret.getDot();      // dot stays where it was
                                formatter.indentNewLine(doc, dotPos);   // newline
                                caret.setDot(dotPos);
                            } finally {
                                DocumentUtilities.setTypingModification(doc, false);
                            }
                        }
                    });
                } finally {
                    formatter.indentUnlock();
                }
            }
        }

    }


    public static class InsertTabAction extends LocalBaseAction {

        static final long serialVersionUID =-3379768531715989243L;

        public InsertTabAction() {
            super(insertTabAction, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        Formatter.pushFormattingContextDocument(doc);
                        try {
                        if (Utilities.isSelectionShowing(caret)) { // block selected
                            try {
                                doc.getFormatter().changeBlockIndent(doc,
                                        target.getSelectionStart(), target.getSelectionEnd(), +1);
                            } catch (GuardedException e) {
                                target.getToolkit().beep();
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        } else { // no selected text
                            int dotPos = caret.getDot();
                            int caretCol;
                            // find caret column
                            try {
                                caretCol = doc.getVisColFromPos(dotPos);
                            } catch (BadLocationException e) {
                                LOG.log(Level.WARNING, null, e);
                                caretCol = 0;
                            }

                            try {
                                // find indent of the first previous non-white row
                                int upperCol = Utilities.getRowIndent(doc, dotPos, false);
                                if (upperCol == -1) { // no prev line with  indent
                                    upperCol = 0;
                                }
                                // is there any char on this line before cursor?
                                int indent = Utilities.getRowIndent(doc, dotPos);
                                // test whether we should indent
                                if (indent == -1) {
                                    if (upperCol > caretCol) { // upper indent is greater
                                        indent = upperCol;
                                    } else { // simulate insert tab by changing indent
                                        indent = Utilities.getNextTabColumn(doc, dotPos);
                                    }

                                    // Fix of #32240 - #1 of 2
                                    int rowStart = Utilities.getRowStart(doc, dotPos);

                                    doc.getFormatter().changeRowIndent(doc, dotPos, indent);

                                    // Fix of #32240 - #2 of 2
                                    int newDotPos = doc.getOffsetFromVisCol(indent, rowStart);
                                    if (newDotPos >= 0) {
                                        caret.setDot(newDotPos);
                                    }

                                } else { // already chars on the line
                                    doc.getFormatter().insertTabString(doc, dotPos);

                                }
                            } catch (BadLocationException e) {
                                // use the same pos
                            }
                        }
                        } finally {
                            Formatter.popFormattingContextDocument(doc);
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }

    /**
     * Compound action that encapsulates several actions
     * @deprecated this action is no longer used.
     */
    @Deprecated
    public static class CompoundAction extends LocalBaseAction {

        Action[] actions;

        static final long serialVersionUID =1649688300969753758L;

        public CompoundAction(String nm, Action actions[]) {
            this(nm, 0, actions);
        }

        public CompoundAction(String nm, int resetMask, Action actions[]) {
            super(nm, resetMask);
            this.actions = actions;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                for (int i = 0; i < actions.length; i++) {
                    Action a = actions[i];
                    if (a instanceof BaseAction) {
                        ((BaseAction)a).actionPerformed(evt, target);
                    } else {
                        a.actionPerformed(evt);
                    }
                }
            }
        }
    }

    /**
     * Compound action that gets and executes its actions
     * depending on the kit of the component.
     * The other advantage is that it doesn't create additional
     * instances of compound actions.
     * @deprecated this action is no longer used. It is reimplemented in editor.actions module.
     */
    @Deprecated
    public static class KitCompoundAction extends LocalBaseAction {

        private String[] actionNames;

        static final long serialVersionUID =8415246475764264835L;

        public KitCompoundAction(String actionNames[]) {
            this(0, actionNames);
        }

        public KitCompoundAction(int resetMask, String actionNames[]) {
            super(resetMask);
            this.actionNames = actionNames;
        }

        public KitCompoundAction(String nm, String actionNames[]) {
            this(nm, 0, actionNames);
        }

        public KitCompoundAction(String nm, int resetMask, String actionNames[]) {
            super(nm, resetMask);
            this.actionNames = actionNames;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                BaseKit kit = Utilities.getKit(target);
                if (kit != null) {
                    for (int i = 0; i < actionNames.length; i++) {
                        Action a = kit.getActionByName(actionNames[i]);
                        if (a != null) {
                            if (a instanceof BaseAction) {
                                ((BaseAction)a).actionPerformed(evt, target);
                            } else {
                                a.actionPerformed(evt);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @deprecated this action is no longer used. It is reimplemented in editor.actions module.
     */
//    @EditorActionRegistration(name = insertContentAction)
    @Deprecated
    public static class InsertContentAction extends LocalBaseAction {

        static final long serialVersionUID =5647751370952797218L;

        public InsertContentAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if ((target != null) && (evt != null)) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                String content = evt.getActionCommand();
                if (content != null) {
                    target.replaceSelection(content);
                } else {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /** Insert text specified in constructor */
    public static class InsertStringAction extends LocalBaseAction {

        String text;

        static final long serialVersionUID =-2755852016584693328L;

        public InsertStringAction(String nm, String text) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
            this.text = text;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                target.replaceSelection(text);
            }
        }
    }

    /** Remove previous or next character */
    public static class DeleteCharAction extends LocalBaseAction {

        protected boolean nextChar;

        static final long serialVersionUID =-4321971925753148556L;

        public DeleteCharAction(String nm, boolean nextChar) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
            this.nextChar = nextChar;
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

		final BaseDocument doc = (BaseDocument)target.getDocument();
		final Caret caret = target.getCaret();
		final int dot = caret.getDot();
		final int mark = caret.getMark();

                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                    DocumentUtilities.setTypingModification(doc, true);

                    try {
                        if (dot != mark) { // remove selection
                            doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                        } else {
                            if (nextChar) { // remove next char
                                char ch = doc.getChars(dot, 1)[0];
                                doc.remove(dot, 1);
                                charDeleted(doc, dot, caret, ch);
                            } else { // remove previous char
                                char ch = doc.getChars(dot-1, 1)[0];
                                doc.remove(dot - 1, 1);
                                charBackspaced(doc, dot-1, caret, ch);
                            }
                        }
                    } catch (BadLocationException e) {
                        target.getToolkit().beep();
                    } finally {
                        DocumentUtilities.setTypingModification(doc, false);
                    }
                    }
                });
            }
        }

      protected void charBackspaced(BaseDocument doc, int dotPos, Caret caret, char ch) 
	throws BadLocationException
      {
      }
      
      protected void charDeleted(BaseDocument doc, int dotPos, Caret caret, char ch) 
	throws BadLocationException
      {
      }
    }

    /**
     * @deprecated this action is no longer used. It is reimplemented in editor.actions module.
     */
    @Deprecated
//    @EditorActionRegistration(name = readOnlyAction)
    public static class ReadOnlyAction extends LocalBaseAction {

        static final long serialVersionUID =9204335480208463193L;

        public ReadOnlyAction() {
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                target.setEditable(false);
            }
        }
    }

    /**
     * @deprecated this action is no longer used. It is reimplemented in editor.actions module.
     */
    @Deprecated
//    @EditorActionRegistration(name = writableAction)
    public static class WritableAction extends LocalBaseAction {

        static final long serialVersionUID =-5982547952800937954L;

        public WritableAction() {
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                target.setEditable(true);
            }
        }
    }

    public static class CutAction extends LocalBaseAction {

        static final long serialVersionUID =6377157040901778853L;

        public CutAction() {
            super(cutAction, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            setEnabled(false);
            //#54893 putValue ("helpID", CutAction.class.getName ()); // NOI18N
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                try {
                    NavigationHistory.getEdits().markWaypoint(target, target.getCaret().getDot(), false, true);
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, "Can't add position to the history of edits.", e); //NOI18N
                }
                
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                    DocumentUtilities.setTypingModification(doc, true);
                    try {
                        // If there is no selection then pre-select a current line including newline
                        // If on last line (without newline) then insert newline at very end temporarily
                        // then cut and remove previous newline.
                        int removeNewlineOffset = -1;
                        if (!Utilities.isSelectionShowing(target)) {
                            Element elem = ((AbstractDocument) target.getDocument()).getParagraphElement(
                                    target.getCaretPosition());
                            int lineStartOffset = elem.getStartOffset();
                            int lineEndOffset = elem.getEndOffset();
                            if (lineEndOffset == doc.getLength() + 1) { // Very end
                                // Temporarily insert extra newline
                                try {
                                    doc.insertString(lineEndOffset - 1, "\n", null);
                                    if (lineStartOffset > 0) { // Only when not on first line
                                        removeNewlineOffset = lineStartOffset - 1;
                                    }
                                } catch (BadLocationException e) {
                                    // could not insert extra newline
                                }
                            }
                            target.select(lineStartOffset, lineEndOffset);
                        }

                        target.cut();

                        if (removeNewlineOffset != -1) {
                            try {
                                doc.remove(removeNewlineOffset, 1);
                            } catch (BadLocationException e) {
                                // Could not remove ending newline
                            }
                        }
                    } finally {
                        DocumentUtilities.setTypingModification(doc, false);
                    }
                    }
                });
            }
        }
    }

    public static class CopyAction extends LocalBaseAction {

        static final long serialVersionUID =-5119779005431986964L;

        public CopyAction() {
            super(copyAction, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            setEnabled(false);
            //#54893 putValue ("helpID", CopyAction.class.getName ()); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    // If there is no selection then pre-select a current line including newline
                    if (!Utilities.isSelectionShowing(target)) {
                        Element elem = ((AbstractDocument) target.getDocument()).getParagraphElement(
                                target.getCaretPosition());
                        if (!Utilities.isRowWhite((BaseDocument) target.getDocument(), elem.getStartOffset())) {
                            target.select(elem.getStartOffset(), elem.getEndOffset());
                        }
                    }
                    target.copy();
                } catch (BadLocationException ble) {
                    LOG.log(Level.FINE, null, ble);
                }
            }
        }
    }

    public static class PasteAction extends LocalBaseAction {

        static final long serialVersionUID =5839791453996432149L;

        public PasteAction(boolean formatted) {
            super(formatted ? pasteFormatedAction : pasteAction,
                ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                
                
                final BaseDocument doc = Utilities.getDocument(target);
                if (doc==null) return;

                try {
                    NavigationHistory.getEdits().markWaypoint(target, target.getCaret().getDot(), false, true);
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, "Can't add position to the history of edits.", e); //NOI18N
                }

                final Formatter formatter = doc.getFormatter();
                final boolean formatted = pasteFormatedAction.equals(getValue(Action.NAME));
                if (formatted) {
                    formatter.reformatLock();
                }
                try {
                    doc.runAtomicAsUser (new Runnable () {
                        public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            Caret caret = target.getCaret();
                            int startOffset = target.getSelectionStart();
                            IN_PASTE.set(true);
                            if (target.getSelectedText() != null) {
                                doc.putProperty(DOC_REPLACE_SELECTION_PROPERTY, true);
                            }
                            try {
                                target.paste();
                            } finally {
                                IN_PASTE.set(false);
                                doc.putProperty(DOC_REPLACE_SELECTION_PROPERTY, null);
                            }
                            int endOffset = caret.getDot();
                            if (formatted) {
                                formatter.reformat(doc, startOffset, endOffset);
                            }
                        } catch (Exception e) {
                            target.getToolkit().beep();
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                        }
                    });
                } finally {
                    if (formatted) {
                        formatter.reformatUnlock();
                    }
                }
            }
        }

      public static void indentBlock(BaseDocument doc, int startOffset, int endOffset)
	throws BadLocationException
      {
	char [] text = doc.getChars(startOffset, endOffset-startOffset);
	String [] lines = toLines(new String(text));

	doc.remove(startOffset, endOffset - startOffset);
	//	System.out.println("Lines:\n"); // NOI18N
	//	for (int j = 0 ; j < lines.length; j++) System.out.println(lines[j] + "<"); // NOI18N

	int offset = startOffset;
	// handle the full lines
	for (int i = 0; i < lines.length - 1; i++) {
	  String indent = getIndentString(doc, offset, lines[i]);
	  String fragment = indent + lines[i].trim() + '\n';
	  //	  System.out.println(fragment + "|"); // NOI18N
	  doc.insertString(offset, fragment, null);
	  offset += fragment.length();
	}

	// the rest just paste without indenting
	doc.insertString(offset, lines[lines.length-1], null);

      }

      /** Break string to lines */
      private static String [] toLines(String str) {
	Vector v = new Vector();
	int p=0 , p0=0;
	for (; p < str.length() ; p++) {
	  if (str.charAt(p) == '\n') {
	    v.add(str.substring(p0, p+1));
	    p0 = p+1;
	  }
	}
	if (p0 < str.length()) v.add(str.substring(p0, str.length())); else v.add("");

	return (String [])v.toArray(new String [0]);
      }

      private static String getIndentString(BaseDocument doc, int startOffset, String str) {
	try {
	  Formatter f = doc.getFormatter();
	  CharArrayWriter cw = new CharArrayWriter();
	  Writer w = f.createWriter(doc, startOffset, cw);
	  w.write(str, 0, str.length());
	  w.close();
	  String out = new String(cw.toCharArray());
	  int i = 0;
	  for (; i < out.length(); i++) {
	    if (out.charAt(i) != ' ' && out.charAt(i) != '\t') break;
	  }
	  //	  System.out.println(out+"|"); // NOI18N
	  //	  System.out.println(out.substring(0,i)+"^"); // NOI18N

	  return out.substring(0, i);
	} catch (java.io.IOException e) {
	  return "";
	}
      }
    }


    @EditorActionRegistration(name = beepAction)
    public static class BeepAction extends LocalBaseAction {

        static final long serialVersionUID =-4474054576633223968L;

        public BeepAction() {
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                target.getToolkit().beep();
            }
        }
    }


    @EditorActionRegistrations({
        @EditorActionRegistration(name = upAction),
        @EditorActionRegistration(name = selectionUpAction)
    })
    public static class UpAction extends LocalBaseAction {

        static final long serialVersionUID =4621760742646981563L;

        public UpAction() {
            super(ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    Point p = caret.getMagicCaretPosition();
                    if (p == null) {
                        Rectangle r = target.modelToView(dot);
                        if (r!=null){
                            p = new Point(r.x, r.y);
                            caret.setMagicCaretPosition(p);
                        }else{
                            return; // model to view failed
                        }
                    }
                    try {
                        dot = Utilities.getPositionAbove(target, dot, p.x);
                        boolean select = selectionUpAction.equals(getValue(Action.NAME));
                        if (select) {
                            caret.moveDot(dot);
                        } else {
                            caret.setDot(dot);
                        }
                    } catch (BadLocationException e) {
                        // the position stays the same
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = downAction),
        @EditorActionRegistration(name = selectionDownAction)
    })
    public static class DownAction extends LocalBaseAction {

        static final long serialVersionUID =-5635702355125266822L;

        public DownAction() {
            super(ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    int dot = caret.getDot();
                    Point p = caret.getMagicCaretPosition();
                    if (p == null) {
                        Rectangle r = target.modelToView(dot);
                        if (r!=null){
                            p = new Point(r.x, r.y);
                            caret.setMagicCaretPosition(p);
                        }else{
                            return; // model to view failed
                        }
                    }
                    try {
                        dot = Utilities.getPositionBelow(target, dot, p.x);
                        boolean select = selectionDownAction.equals(getValue(Action.NAME));
                        if (select) {
                            caret.moveDot(dot);
                        } else {
                            caret.setDot(dot);
                        }
                    } catch (BadLocationException e) {
                        // position stays the same
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /** Go one page up */
    @EditorActionRegistrations({
        @EditorActionRegistration(name = pageUpAction),
        @EditorActionRegistration(name = selectionPageUpAction)
    })
    public static class PageUpAction extends LocalBaseAction {

        static final long serialVersionUID =-3107382148581661079L;

        public PageUpAction() {
            super(ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    int caretOffset = caret.getDot();
                    Rectangle caretBounds = ((BaseTextUI)target.getUI()).modelToView(target, caretOffset);
                    if (caretBounds == null) {
                        return; // Cannot continue reasonably
                    }

                    // Retrieve caret magic position and attempt to retain
                    // the x-coordinate information and use it
                    // for setting of the new caret position
                    Point magicCaretPosition = caret.getMagicCaretPosition();
                    if (magicCaretPosition == null) {
                        magicCaretPosition = new Point(caretBounds.x, caretBounds.y);
                    }
                    
                    Rectangle visibleBounds = target.getVisibleRect();
                    int newCaretOffset;
                    Rectangle newCaretBounds;

                    // Check whether caret was contained in the original visible window
                    if (visibleBounds.contains(caretBounds)) {
                        // Clone present view bounds
                        Rectangle newVisibleBounds = new Rectangle(visibleBounds);
                        // Do viewToModel() and modelToView() with the left top corner
                        // of the currently visible view. If that line is not fully visible
                        // then it should be the bottom line of the previous page
                        // (if it's fully visible then the line above it).
                        int topLeftOffset = target.viewToModel(new Point(
                                visibleBounds.x, visibleBounds.y));
                        Rectangle topLeftLineBounds = target.modelToView(topLeftOffset);

                        // newVisibleBounds.y will hold bottom of new view
                        if (topLeftLineBounds.y != visibleBounds.y) {
                            newVisibleBounds.y = topLeftLineBounds.y + topLeftLineBounds.height;
                        } // Component view starts right at the line boundary
                        // Go back by the view height
                        newVisibleBounds.y -= visibleBounds.height;

                        // Find the new caret bounds by using relative y position
                        // on the original caret bounds. If the caret's new relative bounds
                        // would be visually above the old bounds
                        // the view should be shifted so that the relative bounds
                        // are the same (user's eyes do not need to move).
                        int caretRelY = caretBounds.y - visibleBounds.y;
                        int caretNewY = newVisibleBounds.y + caretRelY;
                        newCaretOffset = target.viewToModel(new Point(magicCaretPosition.x, caretNewY));
                        newCaretBounds = target.modelToView(newCaretOffset);
                        if (newCaretBounds.y < caretNewY) {
                            // Need to go one line down to retain the top line
                            // of the present newVisibleBounds to be fully visible.
                            // Attempt to go forward by height of caret
                            newCaretOffset = target.viewToModel(new Point(magicCaretPosition.x,
                                    newCaretBounds.y + newCaretBounds.height));
                            newCaretBounds = target.modelToView(newCaretOffset);
                        }

                        // Shift the new visible bounds so that the caret
                        // does not visually move
                        newVisibleBounds.y = newCaretBounds.y - caretRelY;

                        // Scroll the window to the requested rectangle
                        target.scrollRectToVisible(newVisibleBounds);
                        
                    } else { // Caret outside of originally visible window
                        // Shift the dot by the visible bounds height
                        Point newCaretPoint = new Point(magicCaretPosition.x,
                                caretBounds.y - visibleBounds.height);
                        newCaretOffset = target.viewToModel(newCaretPoint);
                        newCaretBounds = target.modelToView(newCaretOffset);
                    }

                    boolean select = selectionPageUpAction.equals(getValue(Action.NAME));
                    if (select) {
                        caret.moveDot(newCaretOffset);
                    } else {
                        caret.setDot(newCaretOffset);
                    }
                    
                    // Update magic caret position
                    magicCaretPosition.y = newCaretBounds.y;
                    caret.setMagicCaretPosition(magicCaretPosition);
                    
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = forwardAction),
        @EditorActionRegistration(name = selectionForwardAction)
    })
    public static class ForwardAction extends LocalBaseAction {

        static final long serialVersionUID =8007293230193334414L;

        public ForwardAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int pos;
                    boolean select = selectionForwardAction.equals(getValue(Action.NAME));
                    if (!select && Utilities.isSelectionShowing(caret))
                    {
                        pos = target.getSelectionEnd(); 
                        if (pos != caret.getDot()) {
                            pos--;
                        } else {
                            // clear the selection, but do not move the cursor
                            caret.setDot(pos);
                            return;
                        }
                    }
                    else
                        pos = caret.getDot();
                    int dot = target.getUI().getNextVisualPositionFrom(target,
                              pos, Position.Bias.Forward, SwingConstants.EAST, null);
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /** Go one page down */
    @EditorActionRegistrations({
        @EditorActionRegistration(name = pageDownAction),
        @EditorActionRegistration(name = selectionPageDownAction)
    })
    public static class PageDownAction extends LocalBaseAction {

        static final long serialVersionUID =8942534850985048862L;

        public PageDownAction() {
            super(ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET
                | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    int caretOffset = caret.getDot();
                    Rectangle caretBounds = ((BaseTextUI)target.getUI()).modelToView(target, caretOffset);
                    if (caretBounds == null) {
                        return; // Cannot continue reasonably
                    }

                    // Retrieve caret magic position and attempt to retain
                    // the x-coordinate information and use it
                    // for setting of the new caret position
                    Point magicCaretPosition = caret.getMagicCaretPosition();
                    if (magicCaretPosition == null) {
                        magicCaretPosition = new Point(caretBounds.x, caretBounds.y);
                    }
                    
                    Rectangle visibleBounds = target.getVisibleRect();
                    int newCaretOffset;
                    Rectangle newCaretBounds;

                    // Check whether caret was contained in the original visible window
                    if (visibleBounds.contains(caretBounds)) {
                        // Clone present view bounds
                        Rectangle newVisibleBounds = new Rectangle(visibleBounds);
                        // Do viewToModel() and modelToView() with the left bottom corner
                        // of the currently visible view.
                        // That line should be the top line of the next page.
                        int bottomLeftOffset = target.viewToModel(new Point(
                                visibleBounds.x, visibleBounds.y + visibleBounds.height));
                        Rectangle bottomLeftLineBounds = target.modelToView(bottomLeftOffset);

                        // newVisibleBounds.y will hold bottom of new view
                        newVisibleBounds.y = bottomLeftLineBounds.y;

                        // Find the new caret bounds by using relative y position
                        // on the original caret bounds. If the caret's new relative bounds
                        // would be visually below the old bounds
                        // the view should be shifted so that the relative bounds
                        // are the same (user's eyes do not need to move).
                        int caretRelY = caretBounds.y - visibleBounds.y;
                        int caretNewY = newVisibleBounds.y + caretRelY;
                        newCaretOffset = target.viewToModel(new Point(magicCaretPosition.x, caretNewY));
                        newCaretBounds = target.modelToView(newCaretOffset);
                        if (newCaretBounds.y > caretNewY) {
                            // Need to go one line above to retain the top line
                            // of the present newVisibleBounds to be fully visible.
                            // Attempt to go up by height of caret.
                            newCaretOffset = target.viewToModel(new Point(magicCaretPosition.x,
                                    newCaretBounds.y - newCaretBounds.height));
                            newCaretBounds = target.modelToView(newCaretOffset);
                        }

                        // Shift the new visible bounds so that the caret
                        // does not visually move
                        newVisibleBounds.y = newCaretBounds.y - caretRelY;

                        // Scroll the window to the requested rectangle
                        target.scrollRectToVisible(newVisibleBounds);
                        
                    } else { // Caret outside of originally visible window
                        // Shift the dot by the visible bounds height
                        Point newCaretPoint = new Point(magicCaretPosition.x,
                                caretBounds.y + visibleBounds.height);
                        newCaretOffset = target.viewToModel(newCaretPoint);
                        newCaretBounds = target.modelToView(newCaretOffset);
                    }

                    boolean select = selectionPageDownAction.equals(getValue(Action.NAME));
                    if (select) {
                        caret.moveDot(newCaretOffset);
                    } else {
                        caret.setDot(newCaretOffset);
                    }
                    
                    // Update magic caret position
                    magicCaretPosition.y = newCaretBounds.y;
                    caret.setMagicCaretPosition(magicCaretPosition);
                    
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = backwardAction),
        @EditorActionRegistration(name = selectionBackwardAction)
    })
    public static class BackwardAction extends LocalBaseAction {

        static final long serialVersionUID =-3048379822817847356L;

        public BackwardAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int pos;
                    boolean select = selectionBackwardAction.equals(getValue(Action.NAME));
                    if (!select && Utilities.isSelectionShowing(caret))
                    {
                        pos = target.getSelectionStart(); 
                        if (pos != caret.getDot()) {
                            pos++;
                        } else {
                            // clear the selection, but do not move the cursor
                            caret.setDot(pos);
                            return;
                        }
                    }
                    else
                        pos = caret.getDot();
                    int dot = target.getUI().getNextVisualPositionFrom(target,
                              pos, Position.Bias.Backward, SwingConstants.WEST, null);
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class BeginLineAction extends LocalBaseAction {

        @EditorActionRegistrations({
            @EditorActionRegistration(name = beginLineAction),
            @EditorActionRegistration(name = selectionBeginLineAction)
        })
        public static BeginLineAction create() {
            return new BeginLineAction(false);
        }

        @EditorActionRegistrations({
            @EditorActionRegistration(name = lineFirstColumnAction),
            @EditorActionRegistration(name = selectionLineFirstColumnAction)
        })
        public static BeginLineAction createColumnOne() {
            return new BeginLineAction(true);
        }

        /** Whether the action should go to the begining of
         * the text on the line or to the first column on the line*/
        boolean homeKeyColumnOne;

        static final long serialVersionUID =3269462923524077779L;

        public BeginLineAction(boolean columnOne) {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
            homeKeyColumnOne = columnOne;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                try {
                    int dot = caret.getDot();
                    int lineStartPos = Utilities.getRowStart(target, dot);
                    if (homeKeyColumnOne) { // to first column
                        dot = lineStartPos;
                    } else { // either to line start or text start
                        int textStartPos = Utilities.getRowFirstNonWhite(doc, lineStartPos);
                        if (textStartPos < 0) { // no text on the line
                            textStartPos = Utilities.getRowEnd(target, lineStartPos);
                        }
                        if (dot == lineStartPos) { // go to the text start pos
                            dot = textStartPos;
                        } else if (dot <= textStartPos) {
                            dot = lineStartPos;
                        } else {
                            dot = textStartPos;
                        }
                    }
                    String actionName = (String) getValue(Action.NAME);
                    boolean select = selectionBeginLineAction.equals(actionName)
                            || selectionLineFirstColumnAction.equals(actionName);
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = endLineAction),
        @EditorActionRegistration(name = selectionEndLineAction)
    })
    public static class EndLineAction extends LocalBaseAction {

        static final long serialVersionUID =5216077634055190170L;

        public EndLineAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dot = Utilities.getRowEnd(target, caret.getDot());
                    boolean select = selectionEndLineAction.equals(getValue(Action.NAME));
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                    // now move the magic caret position far to the right
                    Rectangle r = target.modelToView(dot);
                    if (r!=null){
                        Point p = new Point(MAGIC_POSITION_MAX, r.y);
                        caret.setMagicCaretPosition(p);
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                    target.getToolkit().beep();
                }
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = beginAction),
        @EditorActionRegistration(name = selectionBeginAction)
    })
    public static class BeginAction extends LocalBaseAction {

        static final long serialVersionUID =3463563396210234361L;

        public BeginAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | SAVE_POSITION | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                int dot = 0; // begin of document
                boolean select = selectionBeginAction.equals(getValue(Action.NAME));
                if (select) {
                    caret.moveDot(dot);
                } else {
                    caret.setDot(dot);
                }
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = endAction),
        @EditorActionRegistration(name = selectionEndAction)
    })
    public static class EndAction extends LocalBaseAction {

        static final long serialVersionUID =8547506353130203657L;

        public EndAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | SAVE_POSITION | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                // Scroll the component down due to the extra visual space becomes visible.
                Rectangle bounds = target.getBounds();
                bounds.x = 0;
                bounds.y = bounds.height;
                bounds.width = 1;
                bounds.height = 1;
                target.scrollRectToVisible(bounds);

                Caret caret = target.getCaret();
                int dot = target.getDocument().getLength(); // end of document
                boolean select = selectionEndAction.equals(getValue(Action.NAME));
                if (select) {
                    caret.moveDot(dot);
                } else {
                    caret.setDot(dot);
                }
            }
        }
    }

    // Disabled annotations due to overriding by camel-case actions in GSF (no concrete mimetype)
//    @EditorActionRegistrations({
//        @EditorActionRegistration(name = nextWordAction),
//        @EditorActionRegistration(name = selectionNextWordAction)
//    })
    public static class NextWordAction extends LocalBaseAction {

        static final long serialVersionUID =-5909906947175434032L;

//        public NextWordAction() {
//            this(null);
//        }

        NextWordAction(String name) {
            super(name, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dotPos = caret.getDot();
                    dotPos = Utilities.getNextWord(target, dotPos);
                    boolean select = selectionNextWordAction.equals(getValue(Action.NAME));
                    if (caret instanceof BaseCaret){
                        BaseCaret bCaret = (BaseCaret) caret;
                        if (select) {
                            bCaret.moveDot(dotPos);
                        } else {
                            bCaret.setDot(dotPos, false);
                        }
                    }else {
                        if (select) {
                            caret.moveDot(dotPos);
                        } else {
                            caret.setDot(dotPos);
                        }
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    // Disabled annotations due to overriding by camel-case actions in GSF (no concrete mimetype)
//    @EditorActionRegistrations({
//        @EditorActionRegistration(name = previousWordAction),
//        @EditorActionRegistration(name = selectionPreviousWordAction)
//    })
    public static class PreviousWordAction extends LocalBaseAction {

        static final long serialVersionUID =-5465143382669785799L;

//        public PreviousWordAction() {
//            this(null);
//        }

        PreviousWordAction(String name) {
            super(name, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dot = Utilities.getPreviousWord(target, caret.getDot());
                    boolean select = selectionPreviousWordAction.equals(getValue(Action.NAME));
                    if (caret instanceof BaseCaret){
                        BaseCaret bCaret = (BaseCaret) caret;
                        if (select) {
                            bCaret.moveDot(dot);
                        } else {
                            bCaret.setDot(dot, false);
                        }
                    }else {
                        if (select) {
                            caret.moveDot(dot);
                        } else {
                            caret.setDot(dot);
                        }
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = beginWordAction),
        @EditorActionRegistration(name = selectionBeginWordAction)
    })
    public static class BeginWordAction extends LocalBaseAction {

        static final long serialVersionUID =3991338381212491110L;

        public BeginWordAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dot = Utilities.getWordStart(target, caret.getDot());
                    boolean select = selectionBeginWordAction.equals(getValue(Action.NAME));
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = endWordAction),
        @EditorActionRegistration(name = selectionEndWordAction)
    })
    public static class EndWordAction extends LocalBaseAction {

        static final long serialVersionUID =3812523676620144633L;

        public EndWordAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int dot = Utilities.getWordEnd(target, caret.getDot());
                    boolean select = selectionEndWordAction.equals(getValue(Action.NAME));
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException ex) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /** Select word around caret */
    @EditorActionRegistration(name = selectWordAction)
    public static class SelectWordAction extends KitCompoundAction {

        static final long serialVersionUID =7678848538073016357L;

        public SelectWordAction() {
            super(new String[] {
                      beginWordAction,
                      selectionEndWordAction
                  }
                 );
        }

    }

    /** Select line around caret */
    @EditorActionRegistration(name = selectLineAction)
    public static class SelectLineAction extends LocalBaseAction {

        static final long serialVersionUID =-7407681863035740281L;

        public SelectLineAction() {
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            int dotPos = caret.getDot();
                            int bolPos = Utilities.getRowStart(target, dotPos);
                            int eolPos = Utilities.getRowEnd(target, dotPos);
                            eolPos = Math.min(eolPos + 1, doc.getLength()); // include '\n'
                            caret.setDot(bolPos);
                            caret.moveDot(eolPos);
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }

    /** Select text of whole document */
    @EditorActionRegistration(name = selectAllAction)
    public static class SelectAllAction extends KitCompoundAction {

        static final long serialVersionUID =-3502499718130556524L;

        public SelectAllAction() {
            super(new String[] {
                      beginAction,
                      selectionEndAction
                  }
                 );
        }

    }

    @EditorActionRegistration(name = removeTrailingSpacesAction)
    public static class RemoveTrailingSpacesAction extends LocalBaseAction {
        
        public RemoveTrailingSpacesAction() {
        }

        protected boolean asynchonous() {
            return true;
        }
        
        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomic(new Runnable() {
                    public void run() {
                        try {
                            Element lineRootElem = doc.getDefaultRootElement();
                            int count = lineRootElem.getElementCount();
                            boolean removed = false;
                            for (int x = 0; x < count; x++) {
                                Element elem = lineRootElem.getElement(x);
                                int start = elem.getStartOffset();
                                int end = elem.getEndOffset();
                                CharSequence line = DocumentUtilities.getText(doc, start, end - start);
                                int endIndex = line.length() - 1;
                                if (endIndex >= 0 && line.charAt(endIndex) == '\n') {
                                    endIndex--;
                                    if (endIndex >= 0 && line.charAt(endIndex) == '\r') {
                                        endIndex--;
                                    }
                                }
                                int index = endIndex;
                                while (index >= 0 && Character.isWhitespace(line.charAt(index)) && line.charAt(index) != '\n') {
                                    index--;
                                }
                                if (index < endIndex) {
                                    doc.remove(start + index + 1, endIndex - index);
                                    removed = true;
                                }
                            } // for
                            if (removed) {
                                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(BaseKit.class, "TrailingSpacesWereRemoved_Lbl")); // NOI18N
                            } else {
                                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(BaseKit.class, "TrailingSpacesWereNotRemoved_Lbl")); // NOI18N
                            }
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                            target.getToolkit().beep();
                        }
                    }
                });
            }
        }
    }

    /* package */ static final class DefaultSyntax extends Syntax {

        private static final int ISI_TEXT = 0;

        public DefaultSyntax() {
            tokenContextPath = DefaultSyntaxTokenContext.CONTEXT.getContextPath();
        }

        protected TokenID parseToken() {
            // The main loop that reads characters one by one follows
            while (offset < stopOffset) {
                char ch = buffer[offset]; // get the current character

                switch (state) { // switch by the current internal state
                case INIT:
                    switch (ch) {
                    case '\n':
                        offset++;
                        return DefaultSyntaxTokenContext.EOL;
                    default:
                        state = ISI_TEXT;
                        break;
                    }
                    break;

                case ISI_TEXT:
                    switch (ch) {
                    case '\n':
                        state = INIT;
                        return DefaultSyntaxTokenContext.TEXT;
                    }
                    break;

                } // end of switch(state)

                offset++; // move to the next char
            }

            switch (state) {
            case ISI_TEXT:
                state = INIT;
                return DefaultSyntaxTokenContext.TEXT;
            }

            // need to continue on another buffer
            return null;
        }
    } // End of DefaultSyntax class

    /* package */ static final class DefaultSyntaxTokenContext extends TokenContext {

        // Numeric-ids for token-ids
        public static final int TEXT_ID =  1;
        public static final int EOL_ID =  2;

        public static final BaseTokenID TEXT = new BaseTokenID("text", TEXT_ID); // NOI18N
        public static final BaseImageTokenID EOL = new BaseImageTokenID("EOL", EOL_ID, "\n"); // NOI18N

        // Context declaration
        public static final DefaultSyntaxTokenContext CONTEXT = new DefaultSyntaxTokenContext();

        private DefaultSyntaxTokenContext() {
            super("defaultSyntax-token-"); // NOI18N

            try {
                addDeclaredTokenIDs();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Can't load token IDs", e); //NOI18N
            }
        }
    } // End of DefaultSyntaxTokenContext class
    
    private class KeybindingsAndPreferencesTracker implements LookupListener, PreferenceChangeListener {
        
        private final String mimeType;
        private final Lookup.Result<KeyBindingSettings> lookupResult;
        private final Preferences prefs;
        private final Set<JTextComponent> components = new WeakSet<JTextComponent>();
        
        public KeybindingsAndPreferencesTracker(String mimeType) {
            this.mimeType = mimeType;
            Lookup lookup = MimeLookup.getLookup(mimeType);
            
            this.lookupResult = lookup.lookupResult(KeyBindingSettings.class);
            this.lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, this, this.lookupResult));
            this.lookupResult.allInstances();
            
            this.prefs = lookup.lookup(Preferences.class);
            this.prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, this.prefs));
        }

        public void addComponent(JTextComponent c) {
            synchronized (KEYMAPS_AND_ACTIONS_LOCK) {
                components.add(c);
            }            
        }
        
        public void removeComponent(JTextComponent c) {
            synchronized (KEYMAPS_AND_ACTIONS_LOCK) {
                components.remove(c);
            }            
        }
        
        // -------------------------------------------------------------------
        // LookupListener implementation
        // -------------------------------------------------------------------
        
        public void resultChanged(LookupEvent ev) {
            refreshShortcutsAndActions(false);
        }
        
        // -------------------------------------------------------------------
        // PreferenceChangeListener implementation
        // -------------------------------------------------------------------
        
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || EditorPreferencesKeys.CUSTOM_ACTION_LIST.equals(settingName)) {
                refreshShortcutsAndActions(true);
            }
        }
        
        // -------------------------------------------------------------------
        // private implementation
        // -------------------------------------------------------------------
        
        private void refreshShortcutsAndActions(boolean refreshActions) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("BaseKit.KeymapTracker('" + mimeType + "') refreshing keymap " + (refreshActions ? "and actions" : "")); //NOI18N
            }
            
            MultiKeymap keymap;
            JTextComponent [] arr;
            
            synchronized (KEYMAPS_AND_ACTIONS_LOCK) {
                MimePath mimePath = MimePath.parse(mimeType);

                if (refreshActions) {
                    // reset the action maps
                    kitActions.remove(mimePath);
                    kitActionMaps.remove(mimePath);
                }
                
                // reset the keymap
                kitKeymaps.remove(mimePath);
                
                keymap = getKeymap();
                arr = components.toArray(new JTextComponent[components.size()]);
            }
            
            for(JTextComponent c : arr) {
                c.setKeymap(keymap);
            }

            searchableKit.fireActionsChange();
        }
        
    } // End of KeymapTracker class

    private static final class SearchableKit implements org.netbeans.modules.editor.lib2.actions.SearchableEditorKit {

        private final BaseKit baseKit;

        private final ListenerList<ChangeListener> actionsListenerList = new ListenerList<ChangeListener>();

        SearchableKit(BaseKit baseKit) {
            this.baseKit = baseKit;
        }

        public Action getAction(String actionName) {
            return baseKit.getActionByName(actionName);
        }

        public void addActionsChangeListener(ChangeListener listener) {
            actionsListenerList.add(listener);
        }

        public void removeActionsChangeListener(ChangeListener listener) {
            actionsListenerList.remove(listener);
        }

        void fireActionsChange() {
            ChangeEvent evt = new ChangeEvent(this);
            for (ChangeListener listener : actionsListenerList.getListeners()) {
                listener.stateChanged(evt);
            }
        }

    }

}
