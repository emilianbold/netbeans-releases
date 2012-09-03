/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webkit.debugging.api.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;

/**
 * Java wrapper of the CSS domain of WebKit Remote Debugging Protocol.
 *
 * @author Jan Stola
 */
public class CSS {
    /** Transport used by this instance. */
    private TransportHelper transport;
    /** Callback for CSS event notifications. */
    private ResponseCallback callback;
    /** Registered listeners. */
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    /**
     * Creates a new wrapper for the CSS domain of WebKit Remote Debugging Protocol.
     *
     * @param transport transport to use.
     */
    public CSS(TransportHelper transport) {
        this.transport = transport;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }

    /**
     * Enables the CSS agent. Clients should not assume that the CSS agent
     * has been enabled until this method returns.
     */
    public void enable() {
        transport.sendBlockingCommand(new Command("CSS.enable")); // NOI18N
    }

    /**
     * Disables the CSS agent.
     */
    public void disable() {
        transport.sendCommand(new Command("CSS.disable")); // NOI18N
    }

    /**
     * Returns meta-information of all stylesheets.
     *
     * @return meta-information of all stylesheets.
     */
    public List<StyleSheetHeader> getAllStyleSheets() {
        List<StyleSheetHeader> sheets = new ArrayList<StyleSheetHeader>();
        Response response = transport.sendBlockingCommand(new Command("CSS.getAllStyleSheets")); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONArray headers = (JSONArray)result.get("headers"); // NOI18N
                for (Object o : headers) {
                    JSONObject header = (JSONObject)o;
                    sheets.add(new StyleSheetHeader(header));
                }
            }
        }
        return sheets;
    }

    /**
     * Returns (the content of) the specified stylesheet.
     *
     * @param styleSheetId identifier of the requested stylesheet.
     * @return specified stylesheet.
     */
    public StyleSheetBody getStyleSheet(String styleSheetId) {
        StyleSheetBody body = null;
        JSONObject params = new JSONObject();
        params.put("styleSheetId", styleSheetId); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.getStyleSheet", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONObject sheetInfo = (JSONObject)result.get("styleSheet"); // NOI18N
                body = new StyleSheetBody(sheetInfo);
            }
        }
        return body;
    }

    /**
     * Returns the content of the specified stylesheet.
     * 
     * @param styleSheetId identifier of a stylesheet.
     * @return content of the specified stylesheet.
     */
    public String getStyleSheetText(String styleSheetId) {
        String text = null;
        JSONObject params = new JSONObject();
        params.put("styleSheetId", styleSheetId); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.getStyleSheetText", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                text = (String)result.get("text"); // NOI18N
            }
        }
        return text;
    }

    /**
     * Sets the text of the specified stylesheet. Invocation of this method
     * invalidates all {@code StyleId}s and {@code RuleId}s attached
     * to the stylesheet before.
     *
     * @param styleSheetId identifier of the stylesheet.
     * @param styleSheetText new text of the stylesheet.
     */
    public void setStyleSheetText(String styleSheetId, String styleSheetText) {
        JSONObject params = new JSONObject();
        params.put("styleSheetId", styleSheetId); // NOI18N
        params.put("text", styleSheetText); // NOI18N
        transport.sendBlockingCommand(new Command("CSS.setStyleSheetText", params)); // NOI18N
        if (!styleSheetChanged.getAndSet(false)) {
            // Workaround for a bug - if a styleSheetChanged event is not fired
            // as a result of invocation of CSS.setStyleSheetText then we fire
            // this event manually.
            notifyStyleSheetChanged(styleSheetId);
        }
    }

    /** Determines whether styleSheetChanged event was fired. */
    private AtomicBoolean styleSheetChanged = new AtomicBoolean();

    /**
     * Returns names of supported CSS properties.
     *
     * @return names of supported CSS properties.
     */
    public List<String> getSupportedCSSProperties() {
        List<String> list = new ArrayList<String>();
        Response response = transport.sendBlockingCommand(new Command("CSS.getSupportedCSSProperties")); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONArray properties = (JSONArray)result.get("cssProperties"); // NOI18N
                list.addAll(properties);
            }
        }
        return list;
    }

    /**
     * Returns CSS rules matching the specified node.
     *
     * @param node node whose matching style should be returned.
     * @param forcedPseudoClasses element pseudo classes to force when
     * computing the applicable style rules.
     * @param includePseudo determines whether to include pseudo styles.
     * @param includeInherited determines whether to include inherited styles.
     * @return CSS rules matching the specified node.
     */
    public MatchedStyles getMatchedStyles(Node node,
            PseudoClass[] forcedPseudoClasses, boolean includePseudo, boolean includeInherited) {
        MatchedStyles matchedStyles = null;
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        if (forcedPseudoClasses != null && forcedPseudoClasses.length != 0) {
            JSONArray pseudoClasses = new JSONArray();
            for (PseudoClass pseudoClass : forcedPseudoClasses) {
                pseudoClasses.add(pseudoClass.getCode());
            }
            params.put("forcedPseudoClasses", pseudoClasses); // NOI18N
        }
        params.put("includePseudo", includePseudo); // NOI18N
        params.put("includeInherited", includeInherited); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.getMatchedStylesForNode", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                matchedStyles = new MatchedStyles(result);
            }
        }
        return matchedStyles;
    }

    /**
     * Returns styles defined by DOM attributes (like {@code style},
     * {@code witdth}, {@code height}, etc.)
     *
     * @param node node whose inline styles should be returned.
     * @return styles defined by DOM attributes.
     */
    public InlineStyles getInlineStyles(Node node) {
        InlineStyles inlineStyles = null;
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.getInlineStylesForNode", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                inlineStyles = new InlineStyles(result);
            }
        }
        return inlineStyles;
    }

    /**
     * Returns the computed style for the specified node.
     *
     * @param node node whose computed style should be returned.
     * @return computed style for the specified node.
     */
    public List<ComputedStyleProperty> getComputedStyle(Node node) {
        List<ComputedStyleProperty> list = Collections.EMPTY_LIST;
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.getComputedStyleForNode", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONArray properties = (JSONArray)result.get("computedStyle"); // NOI18N
                list = new ArrayList<ComputedStyleProperty>(properties.size());
                for (Object o : properties) {
                    list.add(new ComputedStyleProperty((JSONObject)o));
                }
            }
        }
        return list;
    }

    /**
     * Sets a new text of the specified property.
     *
     * @param styleId ID of the style to modify.
     * @param propertyIndex index of the property in the style.
     * @param propertyText text of the property in the form {@code name:value;}.
     * @param overwrite if {@code true} then the property at the given position
     * is overwritten, otherwise it is inserted.
     * @return the resulting style after the property text modification.
     */
    public Style setPropertyText(StyleId styleId, int propertyIndex, String propertyText, boolean overwrite) {
        Style resultingStyle = null;
        JSONObject params = new JSONObject();
        params.put("styleId", styleId.toJSONObject()); // NOI18N
        params.put("propertyIndex", propertyIndex); // NOI18N
        params.put("text", propertyText); // NOI18N
        params.put("overwrite", overwrite); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.setPropertyText", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONObject style = (JSONObject)result.get("style"); // NOI18N
                resultingStyle = new Style(style);
            }
        }
        return resultingStyle;
    }

    /**
     * Toggles a property in a style.
     *
     * @param styleId ID of the style to modify.
     * @param propertyIndex index of the property in the style.
     * @param disable detemines whether the property should be disabled
     * (i.e. removed from the style declaration). If {@code disable} is
     * {@code false} then the property is returned back into the style declaration.
     * @return the resulting style after the property toggling.
     */
    public Style toggleProperty(StyleId styleId, int propertyIndex, boolean disable) {
        Style resultingStyle = null;
        JSONObject params = new JSONObject();
        params.put("styleId", styleId.toJSONObject()); // NOI18N
        params.put("propertyIndex", propertyIndex); // NOI18N
        params.put("disable", disable); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.toggleProperty", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONObject style = (JSONObject)result.get("style"); // NOI18N
                resultingStyle = new Style(style);
            }
        }
        return resultingStyle;
    }

    /**
     * Sets the selector of a rule.
     *
     * @param ruleId ID of the rule to modify.
     * @param selector new selector of the rule.
     * @return the resulting rule after the selector modification.
     */
    public Rule setRuleSelector(RuleId ruleId, String selector) {
        Rule resultingRule = null;
        JSONObject params = new JSONObject();
        params.put("ruleId", ruleId.toJSONObject()); // NOI18N
        params.put("selector", selector); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("CSS.setRuleSelector", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONObject rule = (JSONObject)result.get("rule"); // NOI18N
                resultingRule = new Rule(rule);
            }
        }
        return resultingRule;
    }

    /**
     * Registers CSS domain listener.
     * 
     * @param listener listener to register.
     */
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters CSS domain listener.
     * 
     * @param listener listener to unregister.
     */
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify listeners about {@code mediaQueryResultChanged} event.
     */
    private void notifyMediaQuertResultChanged() {
        for (Listener listener : listeners) {
            listener.mediaQueryResultChanged();
        }
    }

    /**
     * Notify listeners about {@code styleSheetChanged} event.
     *
     * @param styleSheetId identifier of the modified stylesheet.
     */
    private void notifyStyleSheetChanged(String styleSheetId) {
        for (Listener listener : listeners) {
            listener.styleSheetChanged(styleSheetId);
        }
    }

    void handleMediaQuertResultChanged(JSONObject params) {
        notifyMediaQuertResultChanged();
    }

    void handleStyleSheetChanged(JSONObject params) {
        styleSheetChanged.set(true);
        String styleSheetId = (String)params.get("styleSheetId"); // NOI18N
        notifyStyleSheetChanged(styleSheetId);
    }

    /**
     * CSS domain listener.
     */
    public static interface Listener {

        /**
         * Fired whenever media query result changes (for example, when
         * a browser window is resized). The current implementation
         * considers viewport-dependent media features only.
         */
        void mediaQueryResultChanged();

        /**
         * Fired whenever a stylesheet is changed as a result
         * of the client operation.
         *
         * @param styleSheetId identifier od the modified stylesheet.
         */
        void styleSheetChanged(String styleSheetId);

    }

    /**
     * Callback for CSS domain events.
     */
    class Callback implements ResponseCallback {

        /**
         * Handles CSS domain events.
         *
         * @param response event description.
         */
        @Override
        public void handleResponse(Response response) {
            String method = response.getMethod();
            JSONObject params = response.getParams();
            if ("CSS.mediaQueryResultChanged".equals(method)) { // NOI18N
                handleMediaQuertResultChanged(params);
            } else if ("CSS.styleSheetChanged".equals(method)) { // NOI18N
                handleStyleSheetChanged(params);
            }
        }

    }

    /**
     * Pseudo class (used by {@code getMatchedStylesForNode()}).
     */
    public static enum PseudoClass {
        ACTIVE("active"), FOCUS("focus"), HOVER("hover"), VISITED("visited"); // NOI18N
        /** Code of the pseudo class. */
        private String code;

        /**
         * Creates a new {@code PseudoClass} with the specified code.
         *
         * @param code code of the pseudo class.
         */
        private PseudoClass(String code) {
            this.code = code;
        }

        /**
         * Returns the code of this pseudo class.
         *
         * @return code of this pseudo class.
         */
        String getCode() {
            return code;
        }
    }

}
