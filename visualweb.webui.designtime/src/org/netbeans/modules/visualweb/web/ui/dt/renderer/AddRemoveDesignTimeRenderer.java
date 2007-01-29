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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.web.ui.dt.renderer;

import com.sun.rave.web.ui.component.AddRemove;
import com.sun.rave.web.ui.renderer.AddRemoveRenderer;

import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;

/**
 * A delegating renderer for {@link org.netbeans.modules.visualweb.web.ui.component.AddRemove}.
 *
 * @author gjmurphy
 */
public class AddRemoveDesignTimeRenderer extends SelectorDesignTimeRenderer {

    /** Creates a new instance of ListboxDesignTimeRenderer */
    public AddRemoveDesignTimeRenderer() {
        super(new AddRemoveRenderer());
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if(component instanceof AddRemove) {
            AddRemove addRemove = (AddRemove) component;
            // Clear facets that are used to cache properties, so that any changes to
            // property will render
            if (addRemove.getFacet(AddRemove.AVAILABLE_LABEL_FACET) != null)
                addRemove.getFacets().remove(AddRemove.AVAILABLE_LABEL_FACET);
            if (addRemove.getFacet(AddRemove.SELECTED_LABEL_FACET) != null)
                addRemove.getFacets().remove(AddRemove.SELECTED_LABEL_FACET);
        }
        super.encodeBegin(context, component);
    }

}
