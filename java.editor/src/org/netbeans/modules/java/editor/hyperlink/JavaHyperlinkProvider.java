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

package org.netbeans.modules.java.editor.hyperlink;

import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.editor.java.GoToSupport;

/**
 * Implementation of the hyperlink provider for java language.
 * <br>
 * The hyperlinks are constructed for identifiers.
 * <br>
 * The click action corresponds to performing the goto-declaration action.
 *
 * @author Jan Lahoda
 */
public final class JavaHyperlinkProvider implements HyperlinkProviderExt {
 
    /** Creates a new instance of JavaHyperlinkProvider */
    public JavaHyperlinkProvider() {
    }

    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return GoToSupport.getIdentifierSpan(doc, offset, null);
    }

    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        GoToSupport.goTo(doc, offset, false);
    }

    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return GoToSupport.getGoToElementTooltip(doc, offset, false);
    }

}
