package org.netbeans.editor.example;

import java.util.Map;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.html.*;

/**
* Editor kit implementation for HTML content type
*
* @author Miloslav Metelka
* @version 0.01
*/

public class HTMLKit extends ExtKit {

    static final long serialVersionUID =-1381945567613910297L;

    public static final String HTML_MIME_TYPE = "text/html"; // NOI18N

    public static final String shiftInsertBreakAction = "shift-insert-break"; // NOI18N

    static {
        Settings.addInitializer( new HTMLSettingsInitializer( HTMLKit.class ) );
        Settings.addInitializer( new SaHTMLSettingsInitializer() );
        Settings.reset();
    }
    
    public HTMLKit() {
        super();
        DTD myDTD = new DTDcreator().createDTD( "My" );
    }

    public String getContentType() {
        return HTML_MIME_TYPE;
    }

    /** Create new instance of syntax coloring scanner
    * @param doc document to operate on. It can be null in the cases the syntax
    *   creation is not related to the particular document
    */
    public Syntax createSyntax(Document doc) {
        return new HTMLSyntax();
    }

    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new HTMLSyntaxSupport(doc);
    }

    public Completion createCompletion(ExtEditorUI extEditorUI) {
        return new HTMLCompletion(extEditorUI);
    }

    protected Action[] createActions() {
        Action[] HTMLActions = new Action[] {
                                   new HTMLShiftBreakAction()
                               };
        return TextAction.augmentList(super.createActions(), HTMLActions);
    }
    

    public static class HTMLShiftBreakAction extends BaseAction {

        static final long serialVersionUID =4004043376345356061L;

        public HTMLShiftBreakAction() {
            super( shiftInsertBreakAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Completion completion = ExtUtilities.getCompletion(target);
                if (completion != null && completion.isPaneVisible()) {
                    if (completion.substituteText( true )) {
//                        completion.setPaneVisible(false);
                    } else {
                        completion.refresh(false);
                    }
                }
            }
        }

    }

    private static class SaHTMLSettingsInitializer extends Settings.AbstractInitializer {
        public SaHTMLSettingsInitializer() {
            super( "sa-html-settings-initializer" );
        }
            
        public void updateSettingsMap(Class kitClass, Map settingsMap) {
            if (kitClass == HTMLKit.class) {
                SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST, getHTMLKeyBindings());
            }
        }

        public MultiKeyBinding[] getHTMLKeyBindings() {
            return new MultiKeyBinding[] {
                new MultiKeyBinding(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK),
                    HTMLKit.shiftInsertBreakAction
                )
            };
        }
    }

}
