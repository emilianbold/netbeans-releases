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

package org.netbeans.modules.form;

import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.form.actions.*;

/**
 * A common superclass for nodes used in Form Editor.
 *
 * @author Tomas Pavek
 */

public class FormNode extends AbstractNode implements FormCookie {

    private FormModel formModel;

    protected FormNode(Children children, FormModel formModel) {
        super(children);
        this.formModel = formModel;
        getCookieSet().add(this);
    }

    // FormCookie implementation
    public final FormModel getFormModel() {
        return formModel;
    }

    public Node.Cookie getCookie(Class type) {
        Node.Cookie cookie = super.getCookie(type);
        if (cookie == null
            && (CompilerCookie.class.isAssignableFrom(type)
                || SaveCookie.class.isAssignableFrom(type)
                || ExecCookie.class.isAssignableFrom(type)
                || DebuggerCookie.class.isAssignableFrom(type)
                || CloseCookie.class.isAssignableFrom(type)
                || ArgumentsCookie.class.isAssignableFrom(type)
                || PrintCookie.class.isAssignableFrom(type)))
        {
            FormDataObject fdo = FormEditorSupport.getFormDataObject(formModel);
            if (fdo != null)
                cookie = fdo.getCookie(type);
        }
        return cookie;
    }

    protected SystemAction[] createActions() {
        return new SystemAction[] {
            SystemAction.get(GotoFormAction.class),
            SystemAction.get(GotoEditorAction.class),
            SystemAction.get(GotoInspectorAction.class),
            null,
//            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class) };
    }

    void updateChildren() {
        Children children = getChildren();
        if (children instanceof FormNodeChildren)
            ((FormNodeChildren)children).updateKeys();
    }

    // ----------

    protected abstract static class FormNodeChildren extends Children.Keys {
        protected void updateKeys() {}
    }
}
