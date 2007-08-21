/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.modules.gsf;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.gsf.BracketCompletion;
import org.netbeans.api.gsf.CancellableTask;
import org.netbeans.api.gsf.EditorAction;
import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.GsfLanguage;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.retouche.source.CompilationController;
import org.netbeans.api.retouche.source.Phase;
import org.netbeans.api.retouche.source.Source;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseKit.InsertBreakAction;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.editor.ext.ExtKit.ExtDeleteCharAction;
import org.netbeans.editor.ext.ExtKit.GotoDeclarationAction;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.NbEditorKit.NbGenerateGoToPopupAction;
import org.netbeans.modules.editor.retouche.InstantRenameAction;
import org.netbeans.modules.retouche.editor.fold.GsfFoldManager;
import org.netbeans.modules.retouche.editor.hyperlink.GoToSupport;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * This class represents a generic Editor Kit which is shared by a number of different
 * languages. The EditorKit is an innerclass, because its superclass (BaseKit) calls
 * getContentType() as part of its constructor. Our getContentType implementation
 * relies on accessing one of the fields passed in to our constructor; but when the
 * super constructor is calling getContentType(), our own constructor has not yet been
 * called. This is worked around by having an outer class which holds the field
 * we need at constructor time (langauge).
 *
 * @author Tor Norbye
 * @author Jan Jancura
 */
public class GsfEditorKitFactory {
    private final static boolean PRETTY_PRINT_AVAILABLE = Boolean.getBoolean("ruby.prettyprint");

    private static final String selectNextElementAction = "select-element-next"; //NOI18N
    private static final String selectPreviousElementAction = "select-element-previous"; //NOI18N
    private static final String previousCamelCasePosition = "previous-camel-case-position"; //NOI18N
    private static final String nextCamelCasePosition = "next-camel-case-position"; //NOI18N
    private static final String selectPreviousCamelCasePosition = "select-previous-camel-case-position"; //NOI18N
    private static final String selectNextCamelCasePosition = "select-next-camel-case-position"; //NOI18N
    private static final String deletePreviousCamelCasePosition = "delete-previous-camel-case-position"; //NOI18N
    private static final String deleteNextCamelCasePosition = "delete-next-camel-case-position"; //NOI18N
    private static final String expandAllCodeBlockFolds = "expand-all-code-block-folds"; //NOI18N
    private static final String collapseAllCodeBlockFolds = "collapse-all-code-block-folds"; //NOI18N

    Language language;
    String mimeType;

    public GsfEditorKitFactory(Language language) {
        assert language != null;
        this.language = language;
        this.mimeType = language.getMimeType();
    }

    /**
     * Return the actual kit. This is necessary because NbEditorKit's constructor
     * winds up calling getContentType(), before we've actually had a chance to
     * construct our own editor kit (where getContentType() depends on construction
     * of the object. The trick is to use an outer class, construct that first, and
     *
     */
    public GsfEditorKit kit() {
        LanguageRegistry registry = LanguageRegistry.getInstance();

        return new GsfEditorKit();
    }

    private static Language getLanguage(BaseDocument doc) {
        String mimeType = (String)doc.getProperty("mimeType");

        if (mimeType != null) {
            return LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        }

        return null;
    }

    private FileObject getFileObject(Document doc) {
        DataObject od = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);

        return (od != null) ? od.getPrimaryFile() : null;
    }
    
    /**
     * Returns true if bracket completion is enabled in options.
     */
    private static boolean completionSettingEnabled() {
        //return ((Boolean)Settings.getValue(GsfEditorKit.class, JavaSettingsNames.PAIR_CHARACTERS_COMPLETION)).booleanValue();
        return true;
    }

    public class GsfEditorKit extends NbEditorKit {
        String mimeType;

        public GsfEditorKit() {
            this.mimeType = language.getMimeType();
            Settings.addInitializer (new Settings.Initializer () {
                public String getName() {
                    return mimeType;
                }

                public void updateSettingsMap (Class kitClass, Map settingsMap) {
                    if (kitClass != null && kitClass.equals (GsfEditorKit.class)) {
                        settingsMap.put (SettingsNames.CODE_FOLDING_ENABLE, Boolean.TRUE);
                    }
                }
            });
        }

        @Override
        public String getContentType() {
            return language.getMimeType();
        }

        @Override
        public Document createDefaultDocument() {
            Document doc = new GsfDocument(this.getClass(), language);

            doc.putProperty("mimeType", mimeType); //NOI18N

            return doc;
        }
        
        @Override
        public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
            return new ExtSyntaxSupport(doc) {
            
                @Override
                public int[] findMatchingBlock(int offset, boolean simpleSearch)
                        throws BadLocationException {
                    // Do parenthesis matching, if applicable
                    BracketCompletion bracketCompletion = language.getBracketCompletion();
                    if (bracketCompletion != null) {
                        OffsetRange range = bracketCompletion.findMatching(getDocument(), offset/*, simpleSearch*/);
                        if (range == OffsetRange.NONE) {
                            return null;
                        } else {
                            return new int[] { range.getStart(), range.getEnd() };
                        }
                    }
                    
                    return null;
                }
            };
        }

        @Override
        protected void initDocument(BaseDocument doc) {
            // XXX This appears in JavaKit, not sure why, but doing it just in case.
            //do not ask why, fire bug in the IZ:
            CodeTemplateManager.get(doc);
        }

        @Override
        public Completion createCompletion(ExtEditorUI extEditorUI) {
            //return new GenericCompletion(extEditorUI);
            return null;
        }

        @Override
        public Object clone() {
            return new GsfEditorKit();
        }

        @Override
        protected Action[] createActions() {
            Action[] superActions = super.createActions();
            GsfLanguage gsfLanguage = language.getGsfLanguage();

            ArrayList<Action> actions = new ArrayList(10);

            actions.add(new GsfDefaultKeyTypedAction());
            actions.add(new GsfInsertBreakAction());
            actions.add(new GsfDeleteCharAction(deletePrevCharAction, false));

            String lineCommentPrefix = (gsfLanguage != null) ? gsfLanguage.getLineCommentPrefix() : null;
            if (lineCommentPrefix != null) {
                actions.add(new CommentAction(lineCommentPrefix));
                actions.add(new UncommentAction(lineCommentPrefix));
                actions.add(new ToggleCommentAction(lineCommentPrefix));
            }

            Collection<? extends EditorAction> extraActions = Lookup.getDefault().lookupAll(EditorAction.class);
            for (EditorAction action : extraActions) {
                actions.add(new EditorActionWrapper(action));
            }
            
            actions.add(new InstantRenameAction());
            actions.add(new PrettyPrintAction());
            actions.add(new GenericGoToDeclarationAction());
            actions.add(new GenericGenerateGoToPopupAction());
            actions.add(new SelectCodeElementAction(selectNextElementAction, true));
            actions.add(new SelectCodeElementAction(selectPreviousElementAction, false));
            //actions.add(new ExpandAllCodeBlockFolds());
            //actions.add(new CollapseAllCodeBlockFolds());
            actions.add(new NextCamelCasePosition(findAction(superActions, nextWordAction), language));
            actions.add(new PreviousCamelCasePosition(findAction(superActions, previousWordAction), language));
            actions.add(new SelectNextCamelCasePosition(findAction(superActions, selectionNextWordAction), language));
            actions.add(new SelectPreviousCamelCasePosition(findAction(superActions, selectionPreviousWordAction), language));
            actions.add(new DeleteToNextCamelCasePosition(findAction(superActions, removeNextWordAction), language));
            actions.add(new DeleteToPreviousCamelCasePosition(findAction(superActions, removePreviousWordAction), language));
            
            return TextAction.augmentList(superActions,
                actions.toArray(new Action[actions.size()]));
        }
        
        private Action findAction(Action [] actions, String name) {
            for(Action a : actions) {
                Object nameObj = a.getValue(Action.NAME);
                if (nameObj instanceof String && name.equals(nameObj)) {
                    return a;
                }
            }
            return null;
        }

        public class PrettyPrintAction extends BaseAction {

            static final long serialVersionUID =-1L;
            public PrettyPrintAction() {
                super("pretty-print",
                      ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
            }
            
            @Override
            public boolean isEnabled() {
                return PRETTY_PRINT_AVAILABLE;
            }

            public void actionPerformed(ActionEvent evt, final JTextComponent target) {
//                if (target != null) {
//                    if (!target.isEditable() || !target.isEnabled()) {
//                        target.getToolkit().beep();
//                        return;
//                    }
//
//                    final BaseDocument doc = (BaseDocument)target.getDocument();
//                    FileObject fo = getFileObject(doc);
//                    if (fo == null) {
//                        target.getToolkit().beep();
//                        return;
//                    }
//
//                    // Set hourglass cursor
//                    Cursor origCursor = target.getCursor();
//                    target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//
//                    try {
//                        Source js = Source.forFileObject(fo);
//                        final String[] result = new String[1];
//
//                        js.runUserActionTask(new CancellableTask<CompilationController>() {
//                            public void cancel() {
//                            }
//
//                            public void run(CompilationController controller)
//                                throws Exception {
//                                if (controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
//                                    return;
//                                }
//
//                                FormattingPreferences preferences = new GsfFormattingPreferences(language.getFormatter().indentSize(),
//                                        language.getFormatter().hangingIndentSize());
//                                ParserResult result = controller.getParserResult();
//                                Caret caret = target.getCaret();
//                                language.getFormatter().reformat(doc, result, preferences, caret);
//                                Language language = controller.getLanguage();
//                            }
//                        }, true);
//                    } catch (IOException ioe) {
//                        Exceptions.printStackTrace(ioe);
//                    } finally {
//                        target.setCursor(origCursor);
//                    }
//                }
            }
            
            @Override
            protected Class getShortDescriptionBundleClass() {
                return PrettyPrintAction.class;
            }
        }

        public class GsfDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
            private JTextComponent currentTarget;
            
            @Override
            public void actionPerformed(ActionEvent evt, JTextComponent target) {
                currentTarget = target;
                super.actionPerformed(evt, target);
                currentTarget = null;
            }

            @Override
            protected void insertString(BaseDocument doc, int dotPos, Caret caret, String str,
                boolean overwrite) throws BadLocationException {
                if (completionSettingEnabled()) {
                    Language language = getLanguage(doc);

                    if (language != null) {
                        BracketCompletion bracketCompletion = language.getBracketCompletion();

                        if (bracketCompletion != null) {
                            // TODO - check if we're in a comment etc. and if so, do nothing
                            boolean handled =
                                bracketCompletion.beforeCharInserted(doc, dotPos, currentTarget,
                                    str.charAt(0));

                            if (!handled) {
                                super.insertString(doc, dotPos, caret, str, overwrite);
                                handled = bracketCompletion.afterCharInserted(doc, dotPos, currentTarget,
                                        str.charAt(0));
                            }

                            return;
                        }
                    }
                }

                super.insertString(doc, dotPos, caret, str, overwrite);
            }

            @Override
            protected void replaceSelection(JTextComponent target, int dotPos, Caret caret,
                String str, boolean overwrite) throws BadLocationException {
                char insertedChar = str.charAt(0);
                Document document = target.getDocument();

                if (document instanceof BaseDocument) {
                    BaseDocument doc = (BaseDocument)document;

                    if (completionSettingEnabled()) {
                        Language language = getLanguage(doc);

                        if (language != null) {
                            BracketCompletion bracketCompletion = language.getBracketCompletion();

                            if (bracketCompletion != null) {
                                try {
                                    int caretPosition = caret.getDot();

                                    boolean handled =
                                        bracketCompletion.beforeCharInserted(doc, caretPosition,
                                            target, insertedChar);

                                    int p0 = Math.min(caret.getDot(), caret.getMark());
                                    int p1 = Math.max(caret.getDot(), caret.getMark());

                                    if (p0 != p1) {
                                        doc.remove(p0, p1 - p0);
                                    }

                                    if (!handled) {
                                        if ((str != null) && (str.length() > 0)) {
                                            doc.insertString(p0, str, null);
                                        }

                                        bracketCompletion.afterCharInserted(doc, caret.getDot() - 1,
                                            target, insertedChar);
                                    }
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }

                                return;
                            }
                        }
                    }
                }

                super.replaceSelection(target, dotPos, caret, str, overwrite);
            }
        }

        public class GsfInsertBreakAction extends InsertBreakAction {
            static final long serialVersionUID = -1506173310438326380L;

            @Override
            protected Object beforeBreak(JTextComponent target, BaseDocument doc, Caret caret) {
                if (completionSettingEnabled()) {
                    Language language = getLanguage(doc);

                    if (language != null) {
                        BracketCompletion bracketCompletion = language.getBracketCompletion();

                        if (bracketCompletion != null) {
                            try {
                                int newOffset = bracketCompletion.beforeBreak(doc, caret.getDot(), target);

                                if (newOffset >= 0) {
                                    return new Integer(newOffset);
                                }
                            } catch (BadLocationException ble) {
                                Exceptions.printStackTrace(ble);
                            }
                        }
                    }
                }

                // return Boolean.TRUE;
                return null;
            }

            @Override
            protected void afterBreak(JTextComponent target, BaseDocument doc, Caret caret,
                Object cookie) {
                if (completionSettingEnabled()) {
                    if (cookie != null) {
                        if (cookie instanceof Integer) {
                            // integer
                            int dotPos = ((Integer)cookie).intValue();
                            if (dotPos != -1) {
                                caret.setDot(dotPos);
                            } else {
                                int nowDotPos = caret.getDot();
                                caret.setDot(nowDotPos + 1);
                            }
                        }
                    }
                }
            }
        }

        public class GsfDeleteCharAction extends ExtDeleteCharAction {
            private JTextComponent currentTarget;
            
            public GsfDeleteCharAction(String nm, boolean nextChar) {
                super(nm, nextChar);
            }

            @Override
            public void actionPerformed(ActionEvent evt, JTextComponent target) {
                currentTarget = target;
                super.actionPerformed(evt, target);
                currentTarget = null;
            }

            @Override
            protected void charBackspaced(BaseDocument doc, int dotPos, Caret caret, char ch)
                throws BadLocationException {
                if (completionSettingEnabled()) {
                    Language language = getLanguage(doc);

                    if (language != null) {
                        BracketCompletion bracketCompletion = language.getBracketCompletion();

                        if (bracketCompletion != null) {
                            boolean success = bracketCompletion.charBackspaced(doc, dotPos, currentTarget, ch);
                        }
                    }
                }
            }
        }

        private class GenericGoToDeclarationAction extends GotoDeclarationAction {
            @Override
            public boolean gotoDeclaration(JTextComponent target) {
                GoToSupport.performGoTo((BaseDocument)target.getDocument(),
                    target.getCaretPosition());

                return true;
            }
        }

        private class GenericGenerateGoToPopupAction extends NbGenerateGoToPopupAction {
            @Override
            public void actionPerformed(ActionEvent evt, JTextComponent target) {
            }

            private void addAcceleretors(Action a, JMenuItem item, JTextComponent target) {
                // Try to get the accelerator
                Keymap km = target.getKeymap();

                if (km != null) {
                    KeyStroke[] keys = km.getKeyStrokesForAction(a);

                    if ((keys != null) && (keys.length > 0)) {
                        item.setAccelerator(keys[0]);
                    } else if (a != null) {
                        KeyStroke ks = (KeyStroke)a.getValue(Action.ACCELERATOR_KEY);

                        if (ks != null) {
                            item.setAccelerator(ks);
                        }
                    }
                }
            }

            private void addAction(JTextComponent target, JMenu menu, Action a) {
                if (a != null) {
                    String actionName = (String)a.getValue(Action.NAME);
                    JMenuItem item = null;

                    if (a instanceof BaseAction) {
                        item = ((BaseAction)a).getPopupMenuItem(target);
                    }

                    if (item == null) {
                        // gets trimmed text that doesn' contain "go to"
                        String itemText = (String)a.getValue(ExtKit.TRIMMED_TEXT);

                        if (itemText == null) {
                            itemText = getItemText(target, actionName, a);
                        }

                        if (itemText != null) {
                            item = new JMenuItem(itemText);
                            Mnemonics.setLocalizedText(item, itemText);
                            item.addActionListener(a);
                            addAcceleretors(a, item, target);
                            item.setEnabled(a.isEnabled());

                            Object helpID = a.getValue("helpID"); // NOI18N

                            if ((helpID != null) && (helpID instanceof String)) {
                                item.putClientProperty("HelpID", helpID); // NOI18N
                            }
                        } else {
                            if (ExtKit.gotoSourceAction.equals(actionName)) {
                                item = new JMenuItem(NbBundle.getBundle(GsfEditorKit.class)
                                                             .getString("goto_source_open_source_not_formatted")); //NOI18N
                                addAcceleretors(a, item, target);
                                item.setEnabled(false);
                            }
                        }
                    }

                    if (item != null) {
                        menu.add(item);
                    }
                }
            }

            private void addAction(JTextComponent target, JMenu menu, String actionName) {
                BaseKit kit = Utilities.getKit(target);

                if (kit == null) {
                    return;
                }

                Action a = kit.getActionByName(actionName);

                if (a != null) {
                    addAction(target, menu, a);
                } else { // action-name is null, add the separator
                    menu.addSeparator();
                }
            }

            private String getItemText(JTextComponent target, String actionName, Action a) {
                String itemText;

                if (a instanceof BaseAction) {
                    itemText = ((BaseAction)a).getPopupMenuText(target);
                } else {
                    itemText = actionName;
                }

                return itemText;
            }

            @Override
            public JMenuItem getPopupMenuItem(final JTextComponent target) {
                String menuText =
                    NbBundle.getBundle(GsfEditorKit.class).getString("generate-goto-popup"); //NOI18N
                JMenu jm = new JMenu(menuText);
                //addAction(target, jm, ExtKit.gotoSourceAction);
                addAction(target, jm, ExtKit.gotoDeclarationAction);
                //addAction(target, jm, gotoSuperImplementationAction);
                //addAction(target, jm, ExtKit.gotoAction);
                return jm;
            }
        }
    }

    /** Wrap a Swing Action implementing the EditorAction interface into a proper BaseAction action */
    private class EditorActionWrapper extends BaseAction {
        EditorAction gotoAction;
        
        /** Creates a new instance of InstantRenameAction */
        public EditorActionWrapper(EditorAction gotoAction) {
            super(gotoAction.getName(),
                  // Not sure about these flags?
                  ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | SAVE_POSITION);
            this.gotoAction = gotoAction;
        }

        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            gotoAction.actionPerformed(evt, target);
        }

        @Override
        protected Class getShortDescriptionBundleClass() {
            return gotoAction.getShortDescriptionBundleClass();
        }
    }

    public static class ExpandAllCodeBlockFolds extends BaseAction{
        public ExpandAllCodeBlockFolds(){
            super(expandAllCodeBlockFolds);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(GsfEditorKitFactory.class).getString("expand-all-code-block-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(GsfEditorKitFactory.class).getString("popup-expand-all-code-block-folds"));
        }
    
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            List types = new ArrayList();
            types.add(GsfFoldManager.CODE_BLOCK_FOLD_TYPE);
            //types.add(GsfFoldManager.IMPORTS_FOLD_TYPE);
            FoldUtilities.expand(hierarchy, types);
        }
    }
    
    public static class CollapseAllCodeBlockFolds extends BaseAction{
        public CollapseAllCodeBlockFolds(){
            super(collapseAllCodeBlockFolds);
            putValue(SHORT_DESCRIPTION, NbBundle.getBundle(GsfEditorKitFactory.class).getString("collapse-all-code-block-folds"));
            putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(GsfEditorKitFactory.class).getString("popup-collapse-all-code-block-folds"));
        }
    
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            // Hierarchy locking done in the utility method
            List types = new ArrayList();
            types.add(GsfFoldManager.CODE_BLOCK_FOLD_TYPE);
            ///types.add(GsfFoldManager.IMPORTS_FOLD_TYPE);
            FoldUtilities.collapse(hierarchy, types);
        }
    }
    
    /** @author Sandip V. Chitale (Sandip.Chitale@Sun.Com) */
    static abstract class AbstractCamelCasePosition extends BaseAction {

        private Action originalAction;
        protected Language language;

        public AbstractCamelCasePosition(String name, Action originalAction, Language language) {
            super(name);
            this.language = language;

            if (originalAction != null) {
                Object nameObj = originalAction.getValue(Action.NAME);
                if (nameObj instanceof String) {
                    // We will be wrapping around the original action, use its name
                    putValue(NAME, nameObj);
                    this.originalAction = originalAction;
                }
            }

            String desc = getShortDescription();
            if (desc != null) {
                putValue(SHORT_DESCRIPTION, desc);
            }
        }

        public final void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (originalAction != null && !isUsingCamelCase()) {
                    if (originalAction instanceof BaseAction) {
                        ((BaseAction) originalAction).actionPerformed(evt, target);
                    } else {
                        originalAction.actionPerformed(evt);
                    }
                } else {
                    int offset = newOffset(target);
                    if (offset != -1) {
                        moveToNewOffset(target, offset);
                    }
                }
            }
        }

        protected abstract int newOffset(JTextComponent textComponent);
        protected abstract void moveToNewOffset(JTextComponent textComponent, int offset);

        public String getShortDescription(){
            String name = (String)getValue(Action.NAME);
            if (name == null) {
                return null;
            }
            String shortDesc;
            try {
                shortDesc = NbBundle.getBundle(GsfEditorKitFactory.class).getString(name); // NOI18N
            }catch (MissingResourceException mre){
                shortDesc = name;
            }
            return shortDesc;
        }

        private boolean isUsingCamelCase() {
            return !Boolean.getBoolean("no-ruby-camel-case-style-navigation");
        }
    }
    
    /** @author Sandip V. Chitale (Sandip.Chitale@Sun.Com) */
    final static class DeleteToPreviousCamelCasePosition extends SelectPreviousCamelCasePosition {

        public DeleteToPreviousCamelCasePosition(Action originalAction, Language language) {
            // XXX Why does this get called so many times?        
            super(GsfEditorKitFactory.deletePreviousCamelCasePosition, originalAction, language);
        }
        protected void moveToNewOffset(JTextComponent textComponent, int offset) {
            super.moveToNewOffset(textComponent, offset);
            textComponent.replaceSelection("");
        }
    }
    
    /** @author Sandip V. Chitale (Sandip.Chitale@Sun.Com) */
    final static class DeleteToNextCamelCasePosition extends SelectNextCamelCasePosition {

        public DeleteToNextCamelCasePosition(Action originalAction, Language language) {
            super(GsfEditorKitFactory.deleteNextCamelCasePosition, originalAction, language);
        }

        protected void moveToNewOffset(JTextComponent textComponent, int offset) {
            super.moveToNewOffset(textComponent, offset);
            textComponent.replaceSelection("");
        }
    }
    
    /** @author Sandip V. Chitale (Sandip.Chitale@Sun.Com) */
    static class NextCamelCasePosition extends AbstractCamelCasePosition {

        public NextCamelCasePosition(Action originalAction, Language language) {
            this(GsfEditorKitFactory.nextCamelCasePosition, originalAction, language);
        }

        protected NextCamelCasePosition(String name, Action originalAction, Language language) {
            super(name, originalAction, language);
        }

        protected int newOffset(JTextComponent textComponent) {
            return CamelCaseOperations.nextCamelCasePosition(textComponent, language);
        }

        protected void moveToNewOffset(JTextComponent textComponent, int offset) {
            textComponent.setCaretPosition(offset);
        }
    }
    
    /** @author Sandip V. Chitale (Sandip.Chitale@Sun.Com) */
    static class PreviousCamelCasePosition extends AbstractCamelCasePosition {

        public PreviousCamelCasePosition(Action originalAction, Language language) {
            this(GsfEditorKitFactory.previousCamelCasePosition, originalAction, language);
        }

        protected PreviousCamelCasePosition(String name, Action originalAction, Language language) {
            super(name, originalAction, language);
        }

        protected int newOffset(JTextComponent textComponent) {
            return CamelCaseOperations.previousCamelCasePosition(textComponent, language);
        }

        protected void moveToNewOffset(JTextComponent textComponent, int offset) {
            textComponent.setCaretPosition(offset);
        }
    }
    
    /** @author Sandip V. Chitale (Sandip.Chitale@Sun.Com) */
    static class SelectNextCamelCasePosition extends NextCamelCasePosition {

        public SelectNextCamelCasePosition(Action originalAction, Language language) {
            this(GsfEditorKitFactory.selectNextCamelCasePosition, originalAction, language);
        }

        protected SelectNextCamelCasePosition(String name, Action originalAction, Language language) {
            super(name, originalAction, language);
        }

        protected void moveToNewOffset(JTextComponent textComponent, int offset) {
            textComponent.getCaret().moveDot(offset);
        }
    }
    
    /** @author Sandip V. Chitale (Sandip.Chitale@Sun.Com) */
    static class SelectPreviousCamelCasePosition extends PreviousCamelCasePosition {

        public SelectPreviousCamelCasePosition(Action originalAction, Language language) {
            this(GsfEditorKitFactory.selectPreviousCamelCasePosition, originalAction, language);
        }

        protected SelectPreviousCamelCasePosition(String name, Action originalAction, Language language) {
            super(name, originalAction, language);
        }

        protected void moveToNewOffset(JTextComponent textComponent, int offset) {
            textComponent.getCaret().moveDot(offset);
        }
    }
}
