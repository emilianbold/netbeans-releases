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
package org.netbeans.modules.web.inspect;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.modules.web.inspect.script.Script;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Default implementation of {@code PageModel}. It is based on
 * {@code ScriptExecutor} that allows to execute scripts in the target page.
 *
 * @author Jan Stola
 */
public class PageModelImpl extends PageModel {
    /** Logger used by this class. */
    private static final Logger LOG = Logger.getLogger(PageModelImpl.class.getName());
    /** Element attribute holding the position index of this node in the original DOM. */
    private static final String ELEMENT_INDEX_ATTR = "idx"; // NOI18N
    /** Message attribute holding information about a resource type. */
    private static final String RESOURCE_TYPE = "type"; // NOI18N
    /** Message attribute holding information about a resource URL. */
    private static final String RESOURCE_URL = "url"; // NOI18N
    /** Lock guarding access to {@code executor} field. */
    private final Object LOCK = new Object();
    /** Executor for the target page. */
    private ScriptExecutor executor;

    /**
     * Sets executor for the model.
     * 
     * @param executor 
     */
    void setExecutor(ScriptExecutor executor) {
        if (this.executor == executor) {
            return;
        }
        synchronized (LOCK) {
            this.executor = executor;
        }
        firePropertyChange(PROP_MODEL, null, null);
    }

    /**
     * Returns executor for the model.
     * 
     * @return executor for the model.
     */
    ScriptExecutor getExecutor() {
        synchronized (LOCK) {
            return executor;
        }
    }

    @Override
    public boolean isValid() {
        synchronized (LOCK) {
            return (executor != null);
        }
    }

    /**
     * Throws an exception if this thread is the event-dispatch thread.
     * 
     * @throws IllegalStateException when the current thread is
     * the event-dispatch thread.
     */
    private void checkThread() {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Do not access PageModel in event-dispatch thread!"); // NOI18N
        }
    }

    /**
     * Executes the specified script using the current executor.
     * It ensures that the target page is initialized correctly
     * with the helper objects/methods.
     * 
     * @param script script to execute.
     * @return result of the script.
     */
    private Object executeScript(String script) {
        checkThread();
        String isInitScript = Script.getScript("isInitialized"); // NOI18N
        ScriptExecutor currentExecutor = getExecutor();
        Object initialized = currentExecutor.execute(isInitScript);
        if (initialized == ScriptExecutor.ERROR_RESULT) {
            return ScriptExecutor.ERROR_RESULT;
        }
        if ((initialized != null) && "false".equals(initialized.toString())) { // NOI18N
            String initScript = Script.getScript("initialization"); // NOI18N
            initialized = currentExecutor.execute(initScript);
            if (initialized == ScriptExecutor.ERROR_RESULT) {
                return ScriptExecutor.ERROR_RESULT;
            }
        }
        return currentExecutor.execute(script);
    }

    @Override
    public Document getDocument() {
        Document document = null;
        if (isValid()) {
            try {
                Object xml = executeScript("NetBeans.getDOM()"); // NOI18N
                if (xml != ScriptExecutor.ERROR_RESULT) {
                    InputSource source = new InputSource(new StringReader(xml.toString()));
                    document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
                    // Set document URL
                    Object url = executeScript("document.URL"); // NOI18N
                    if (url != ScriptExecutor.ERROR_RESULT) {
                        document.setDocumentURI(url.toString());
                    }
                }
            } catch (SAXException ex) {
                LOG.log(Level.INFO, null, ex);
            } catch (IOException ex) {
                LOG.log(Level.INFO, null, ex);
            } catch (ParserConfigurationException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }
        return document;
    }

    @Override
    public void setSelectedElements(Collection<ElementHandle> elements) {
        if (isValid()) {
            List<JSONObject> jsonHandles = new ArrayList<JSONObject>(elements.size());
            for (ElementHandle handle : elements) {
                JSONObject jsonHandle = handle.toJSONObject();
                jsonHandles.add(jsonHandle);
            }
            JSONArray array = new JSONArray(jsonHandles);
            String code = array.toString();
            executeScript("NetBeans.selectElements("+code+")"); // NOI18N
        }
    }

    @Override
    public Map<String, String> getAtrributes(ElementHandle element) {
        Map<String,String> map;
        if (isValid()) {
            JSONObject jsonHandle = element.toJSONObject();
            String code = jsonHandle.toString();
            Object result = executeScript("NetBeans.getAttributes("+code+")"); // NOI18N
            if (result == ScriptExecutor.ERROR_RESULT) {
                map = Collections.EMPTY_MAP;
            } else if (result instanceof JSONObject) {
                JSONObject json = (JSONObject)result;
                map = toMap(json);
            } else {
                LOG.log(Level.INFO, "Unexpected attributes: {0}", result); // NOI18N
                map = Collections.EMPTY_MAP;
            }
        } else {
            map = Collections.EMPTY_MAP;
        }
        return map;
    }

    @Override
    public Map<String, String> getComputedStyle(ElementHandle element) {
        Map<String,String> map;
        if (isValid()) {
            JSONObject jsonHandle = element.toJSONObject();
            String code = jsonHandle.toString();
            Object result = executeScript("NetBeans.getComputedStyle("+code+")"); // NOI18N
            if (result == ScriptExecutor.ERROR_RESULT) {
                map = Collections.EMPTY_MAP;
            } else if (result instanceof JSONObject) {
                JSONObject json = (JSONObject)result;
                map = toMap(json);
            } else {
                LOG.log(Level.INFO, "Unexpected computed style: {0}", result); // NOI18N
                map = Collections.EMPTY_MAP;
            }
        } else {
            map = Collections.EMPTY_MAP;    
        }
        return map;
    }

    /**
     * Converts a JSON object into a map.
     * 
     * @param json JSON object to convert into a map.
     * @return map corresponding to the given JSON object, it maps attribute
     * names of the original object to their values.
     */
    private Map<String,String> toMap(JSONObject json) {
        Iterator iter = json.keys();
        Map<String,String> map = new HashMap<String,String>();
        while (iter.hasNext()) {
            String key = iter.next().toString();
            try {
                String value = json.getUnsafeString(key);
                map.put(key, value);
            } catch (JSONException ex) {
                LOG.log(Level.INFO, "Unexpected value of key {0} in {1}.", new Object[]{key, json}); // NOI18N
            }
        }
        return map;
    }

    @Override
    public Collection<ResourceInfo> getResources() {
        List<ResourceInfo> resources = null;
        if (isValid()) {
            Object result = executeScript("NetBeans.getResources()"); // NOI18N
            if (result instanceof JSONArray) {
                JSONArray array = (JSONArray)result;
                resources = new LinkedList<ResourceInfo>();
                for (int i=0; i<array.length(); i++) {
                    try {
                        JSONObject resource = array.getJSONObject(i);
                        String url = resource.getUnsafeString(RESOURCE_URL);
                        String typeCode = resource.getUnsafeString(RESOURCE_TYPE);
                        ResourceInfo.Type type = ResourceInfo.Type.fromCode(typeCode);
                        if (type == null) {
                            LOG.log(Level.INFO, "Unexpected resource type: {0}", typeCode); // NOI18N
                        } else {
                            resources.add(new ResourceInfo(type, url));
                        }
                    } catch (JSONException ex) {
                        LOG.log(Level.INFO, "Unexpected resource on index {0} in {1}", new Object[]{i, array}); // NOI18N
                    }
                }
            } else if (result != ScriptExecutor.ERROR_RESULT) {
                LOG.log(Level.INFO, "Unexpected resources: {0}", result); // NOI18N
            }
        }
        return resources;
    }

    @Override
    public void reloadResource(ResourceInfo resource) {
        if (isValid()) {
            String url = JSONObject.quote(resource.getURL());
            ResourceInfo.Type type = resource.getType();
            if (type == ResourceInfo.Type.STYLESHEET) {
                executeScript("NetBeans.reloadCSS("+url+")");
            } else if (type == ResourceInfo.Type.IMAGE) {
                executeScript("NetBeans.reloadImage("+url+")");
            } else if (type == ResourceInfo.Type.SCRIPT) {
                executeScript("NetBeans.reloadScript("+url+")");
            }
        }
    }

}
