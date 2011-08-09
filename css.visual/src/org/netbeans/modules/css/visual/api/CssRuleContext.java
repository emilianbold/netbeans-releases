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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual.api;

import java.io.File;
import javax.swing.text.Document;
import org.netbeans.modules.css.lib.api.model.Stylesheet;
import org.netbeans.modules.css.visual.CssRuleContent;

/**
 * A context class representig a parsed css source. 
 * An instance is supposed to be set to StyleBuilderPanel.
 *
 * @author marek.fukala@sun.com
 * 
 */
public final class CssRuleContext {

    private File file;
    private Document doc;
    private CssRuleContent selectedRule;
    private Stylesheet model;

    /**
     * Creates an instance of CssRuleContext. 
     * 
     * @param selectedRule a selected rule from the list of rules held by the css model
     * @param model an instance of CssModel
     * @param doc source editor document for the model
     * @param file 
     */
    public CssRuleContext(CssRuleContent selectedRule, Stylesheet model, Document doc, File basePath) {
        this.selectedRule = selectedRule;
        this.model = model;
        this.file = basePath;
        this.doc = doc;
    }

    /** @param  a selected css rule from the list of rules held by the css model. */
    public CssRuleContent selectedRuleContent() {
        return selectedRule;
    }

    /** @param return an instance of CssModel which is this CssRuleContext based on. */
    public Stylesheet model() {
        return model;
    }

    /** @return a File representing a base of the css file. Used to resolve relative links.*/
    public File base() {
        return file;
    }

    /** @return source editor document for the css model or null if the model was created from a reader.*/
    public Document document() {
        return doc;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CssRuleContext)) {
            return false;
        } else {
            CssRuleContext c = (CssRuleContext) o;
            return c.document() == document() && c.base() == base() && c.selectedRuleContent() == selectedRuleContent();
        }
    }
}

