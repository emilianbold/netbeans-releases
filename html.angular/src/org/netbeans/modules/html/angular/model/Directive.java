/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.angular.model;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import static org.netbeans.modules.html.angular.model.DirectiveType.*;

/**
 *
 * @author marekfukala
 */
public enum Directive {
    //      requir.|attr |class |element
    app     (false, true, true,  false, angularModule), 
    bind    (true,  true, true,  false, expression), 
    bindHtmlUnsafe
            (true,  true, true,  false, expression), 
    bindTemplate
            (true,  true, true,  false, string), 
    change  (true,  true, false, true,  noValue), 
    checked (true,  true, false, false, expression),
    _class  (true,  true, true,  false, expression), //real name is "class"
    classEven
            (true,  true, true,  false, expression),
    classOdd(true,  true, true,  false, expression),
    click   (true,  true, true,  false, expression),
    cloak   (true,  true, true,  false, noValue),
    controller
            (true,  true, true,  false, expression), 
    csp     (false, true, true,  false, noValue),
    dblclick(true,  true, true,  false, expression),
    disabled(true,  true, false, false, expression),
    form    (true,  true, true,  true,  string),
    hide    (true,  true, true,  false, expression),
    href    (true,  true, false, false, template),
    include (true,  true, true,  true,  string),
    init    (true,  true, true,  false, expression),
    list    (true,  true, true,  false, string),
    model   (true,  true, true,  false, expression),
    mousedown
            (true,  true, true,  false, expression),
    mouseenter
            (true,  true, true,  false, expression),
    mouseleave
            (true,  true, true,  false, expression),
    mousemove
            (true,  true, true,  false, expression),
    mouseover
            (true,  true, true,  false, expression),
    mouseup (true,  true, true,  false, expression),
    multiple(true,  true, false, false, expression),
    nonBindable
            (true,  true, true,  false, noValue),
    //TODO add sub directives
    pluralize
            (true,  true, false, true,  noValue),    
    
    //TODO add sub directives
    readonly(false, true, false, false, noValue),
    repeat  (true,  true, true,  false, repeatExpression),
    selected(false, true, false, false, string),
    show    (true,  true, true,  false, expression),
    src     (true,  true, false, false, template),
    style   (true,  true, true,  false, expression),
    submit  (true,  true, true,  false, expression),
    
    //TODO add sub directives 
    _switch (true,  true, false, true,  noValue), //??? //real name is "switch"
    transclude
            (true,  true, true,  false, noValue),
    view    (false, true, true,  true,  noValue);
    
    //ngdoc parser is here: https://github.com/angular/angular.js/blob/master/docs/src/ngdoc.js
    //the directives documentation in .ngdoc format is here: https://github.com/angular/angular.js/blob/master/src/ng/directive/ngController.js
    
    private static final String DOC_URL_BASE = "http://docs.angularjs.org/api/ng.directive:"; //NOI18N
    
    public static final String NAME_PREFIX = "ng";
    
    public static boolean isAngularAttribute(Attribute attribute) {
        return DirectiveConvention.getConvention(attribute.unqualifiedName()) != null;
    }
    
    private static final Map<String, Directive> NAMES2DIRECTIVES = new HashMap<>();
    static {
        for(Directive d : values()) {
            for(DirectiveConvention dc : DirectiveConvention.values()) {
                NAMES2DIRECTIVES.put(d.getAttributeName(dc), d);
            }
        }
    }
    
    /**
     * Gets an instance of {@link Directive} for an angular attribute name.
     * 
     * Attribute names in all supported forms can be used.
     * 
     * @param attributeName
     */
    public static Directive getDirective(String attributeName) {
        return NAMES2DIRECTIVES.get(attributeName);
    }
    
    private boolean attributeValueTypicallyUsed;

    private boolean useAsAttribute, useAsClass, useAsElement;
    
    private DirectiveType type;
    
    private Directive(
            boolean attributeValueRequired, 
            boolean useAsAttribute, 
            boolean useAsClass, 
            boolean useAsElement,
            DirectiveType type) {
        this.attributeValueTypicallyUsed = attributeValueRequired;
        this.useAsAttribute = useAsAttribute;
        this.useAsClass = useAsClass;
        this.useAsElement = useAsElement;
        this.type = type;
    }

    public String getExternalDocumentationURL() {
        return new StringBuilder().append(DOC_URL_BASE)
                .append(NAME_PREFIX)
                .append(Character.toUpperCase(getCleanCoreName().charAt(0)))
                .append(getCleanCoreName().substring(1))
                .toString();
    }
    
    /**
     * Gets the directive name as html attribute using the given convention
     */
    @NonNull
    public String getAttributeName(DirectiveConvention convention) {
        return convention.createFQN(this);
    }

   /**
     * Resolves the attribute name to the word-by-dash-separated form.
     * "bind-html-unsafe", "class-even",...
     */
    String getAttributeCoreName(char delimiter) {
         StringBuilder sb = new StringBuilder();
        //class name workaround
        String name = getCleanCoreName();
        for(int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if(Character.isUpperCase(c)) {
                sb.append(delimiter);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

     /**
     * Name of the directive. Use this method instead of {@link #name()}.
     */
    private String getCleanCoreName() {
        return name().charAt(0) == '_' ? name().substring(1) : name();
    }
     
    public boolean isAttributeValueTypicallyUsed() {
        return attributeValueTypicallyUsed;
    }
    
    public boolean canUseAsAttribute() {
        return useAsAttribute;
    }
    
    public boolean canUseAsClass() {
        return useAsClass;
    }
    
    public boolean canUseAsElement() {
        return useAsElement;
    }

    public DirectiveType getType() {
        return type;
    }
    
}
