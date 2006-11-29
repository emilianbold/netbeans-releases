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

package org.netbeans.lib.editor.codetemplates;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.spi.*;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

/**
 * Code template allows the client to paste itself into the given
 * text component.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateManagerOperation
implements LookupListener, Runnable, SettingsChangeListener {
    
    private static Map mime2operation = new HashMap(8);
    
    public static synchronized CodeTemplateManager getManager(Document doc) {
        return get(doc).getManager();
    }

    public static synchronized CodeTemplateManagerOperation get(Document doc) {
        String mimeType = (String)doc.getProperty("mimeType");
        CodeTemplateManagerOperation operation = (CodeTemplateManagerOperation)
                doc.getProperty(CodeTemplateManagerOperation.class);
        boolean mimesEqual = (operation != null) && mimeTypesEqual(mimeType,
                operation.getMimeType());

        if (!mimesEqual) {
            WeakReference ref = (WeakReference)mime2operation.get(mimeType);
            if (ref != null) {
                operation = (CodeTemplateManagerOperation)ref.get();
            } else {
                operation = null;
            }
            if (operation == null) {
                operation = new CodeTemplateManagerOperation(mimeType);
                CodeTemplateApiPackageAccessor.get().createCodeTemplateManager(operation);

                mime2operation.put(mimeType, new WeakReference(operation));
            }

            doc.putProperty(CodeTemplateManagerOperation.class, operation);
        }
        
        return operation;
    }
    
    private static boolean mimeTypesEqual(String mimeType1, String mimeType2) {
        return (mimeType1 == null && mimeType2 == null)
            || (mimeType1 != null && mimeType1.equals(mimeType2));
    }


    private final CodeTemplateManager manager;

    private final String mimeType;

    private Lookup.Result descriptions;
    
    private Collection/*<CodeTemplateProcessorFactory>*/ processorFactories;
    
    private Collection/*<CodeTemplateFilter.Factory>*/ filterFactories;
    
    private Map abbrev2template;
    
    private List sortedTemplatesByAbbrev;
    
    private List unmodSortedTemplatesByAbbrev;
    
    private List sortedTemplatesByParametrizedText;
    
    private List selectionTemplates;
    
    private EventListenerList listenerList = new EventListenerList();
    
    private boolean settingsListeningInitialized;
    
    private CodeTemplateManagerOperation(String mimeType) {
        this.mimeType = mimeType;
        
        // Ensure the API package accessor gets initialized
        CodeTemplateManager.class.getName();

        this.manager = CodeTemplateApiPackageAccessor.get().createCodeTemplateManager(this);
        
        // Compute descriptions asynchronously
        RequestProcessor.getDefault().post(this);
    }
    
    public CodeTemplateManager getManager() {
        assert (manager != null);
        return manager;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public Collection getCodeTemplates() {
        return unmodSortedTemplatesByAbbrev;
    }
    
    public Collection findSelectionTemplates() {
        return selectionTemplates;
    }
    
    public CodeTemplate findByAbbreviation(String abbreviation) {
        return (CodeTemplate)abbrev2template.get(abbreviation);
    }
    
    public Collection findByParametrizedText(String prefix, boolean ignoreCase) {
        List result = new ArrayList();
        
        int low = 0;
	int high = sortedTemplatesByParametrizedText.size() - 1;
	while (low <= high) {
	    int mid = (low + high) >> 1;
	    CodeTemplate t = (CodeTemplate)sortedTemplatesByParametrizedText.get(mid);
	    int cmp = compareTextIgnoreCase(t.getParametrizedText(), prefix);

	    if (cmp < 0) {
		low = mid + 1;
            } else if (cmp > 0) {
		high = mid - 1;
            } else {
                low = mid;
		break;
            }
	}
        
        // Go back whether prefix matches the name
        int i = low - 1;
        while (i >= 0) {
            CodeTemplate t = (CodeTemplate)sortedTemplatesByParametrizedText.get(i);
            int mp = matchPrefix(t.getParametrizedText(), prefix);
            if (mp == MATCH_NO) { // not matched
                break;
            } else if (mp == MATCH_IGNORE_CASE) { // matched when ignoring case
                if (ignoreCase) { // do not add if exact match required
                    result.add(t);
                }
            } else { // matched exactly
                result.add(t);
            }
            i--;
        }
        
        i = low;
        while (i < sortedTemplatesByParametrizedText.size()) {
            CodeTemplate t = (CodeTemplate)sortedTemplatesByParametrizedText.get(i);
            int mp = matchPrefix(t.getParametrizedText(), prefix);
            if (mp == MATCH_NO) { // not matched
                break;
            } else if (mp == MATCH_IGNORE_CASE) { // matched when ignoring case
                if (ignoreCase) { // do not add if exact match required
                    result.add(t);
                }
            } else { // matched exactly
                result.add(t);
            }
            i++;
        }
        
        return result;
    }
    
    public Collection/*<CodeTemplateFilter>*/ getTemplateFilters(JTextComponent component, int offset) {
        List/*<CodeTemplateFilter>*/ result = new ArrayList/*<CodeTemplateFilter>*/();
        for (Iterator it = filterFactories.iterator(); it.hasNext();) {
            CodeTemplateFilter.Factory factory = (CodeTemplateFilter.Factory)it.next();
            result.add(factory.createFilter(component, offset));
        }
        return result;
    }

    public void insert(CodeTemplate codeTemplate, JTextComponent component) {
        CodeTemplateInsertHandler handler = new CodeTemplateInsertHandler(
                codeTemplate, component, processorFactories);
        handler.processTemplate();
    }
    
    /**
     * Match text against the given prefix.
     *
     * @param text text to be compared with the prefix.
     * @param prefix text to be matched as a prefix of the text parameter.
     * @return one of <code>MATCH_NO</code>, <code>MATCH_IGNORE_CASE</code>
     *  or <code>MATCH</code>
     */
    private static final int MATCH_NO = 0;
    private static final int MATCH_IGNORE_CASE = 1;
    private static final int MATCH = 2;
    private static int matchPrefix(CharSequence text, CharSequence prefix) {
        boolean matchCase = true;
        int prefixLength = prefix.length();
        if (prefixLength > text.length()) { // prefix longer than text
            return MATCH_NO;
        }
        int i;
        for (i = 0; i < prefixLength; i++) {
            char ch1 = text.charAt(i);
            char ch2 = prefix.charAt(i);
            if (ch1 != ch2) {
                matchCase = false;
                if (Character.toLowerCase(ch1) != Character.toLowerCase(ch2)) {
                    break;
                }
            }
        }
        if (i == prefixLength) { // compared all
            return matchCase ? MATCH : MATCH_IGNORE_CASE;
        } else { // not compared all => not matched
            return MATCH_NO;
        }
    }
    
    private static int compareTextIgnoreCase(CharSequence text1, CharSequence text2) {
        int len = Math.min(text1.length(), text2.length());
        for (int i = 0; i < len; i++) {
            char ch1 = Character.toLowerCase(text1.charAt(i));
            char ch2 = Character.toLowerCase(text2.charAt(i));
            if (ch1 != ch2) {
                return ch1 - ch2;
            }
        }
        return text1.length() - text2.length();
    }
    
    public boolean isLoaded() {
        synchronized (listenerList) {
            return (descriptions != null);
        }
    }
    
    public void registerLoadedListener(ChangeListener listener) {
        synchronized (listenerList) {
            if (descriptions != null) { // already loaded
                listener.stateChanged(new ChangeEvent(this));
            } else { // not yet loaded
                listenerList.add(ChangeListener.class, listener);
            }
        }
    }
    
    public void waitLoaded() {
        synchronized (listenerList) {
            if (!isLoaded()) {
                try {
                    listenerList.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
    
    private void fireStateChanged(ChangeEvent evt) {
        Object[] listeners;
        synchronized (listenerList) {
            listeners = listenerList.getListenerList();
        }
        for (int i = 0; i < listeners.length; i += 2) {
            if (ChangeListener.class == listeners[i]) {
                ((ChangeListener)listeners[i + 1]).stateChanged(evt);
            }
        }
    }
    
    public void run() {
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(getMimeType()));
        Lookup.Result result = lookup.lookup(
                new Lookup.Template(CodeTemplateProcessorFactory.class));
        
        processorFactories = result.allInstances();
        // [TODO] listen for changes

        result = lookup.lookup(
                new Lookup.Template(CodeTemplateFilter.Factory.class));
        
        filterFactories = result.allInstances();
        // [TODO] listen for changes

        // [TODO] take from settings
        setDescriptions(Lookup.EMPTY.lookup(new Lookup.Template(CodeTemplateDescription.class)));
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        rebuildCodeTemplates();
    }
    
    void setDescriptions(Lookup.Result descriptions) {
        synchronized (listenerList) {
            this.descriptions = descriptions;
            rebuildCodeTemplates();
            fireStateChanged(new ChangeEvent(manager));
            // Notify loading finished
            listenerList.notifyAll();
        }
    }
    
    private Collection updateDescriptionInstances(Collection descriptionsInstances) {
        descriptionsInstances = new ArrayList();
        
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
        BaseOptions baseOptions = (BaseOptions) lookup.lookup(BaseOptions.class);
        if (baseOptions != null) {
            Map abbrevMap = baseOptions.getAbbrevMap();
            if (abbrevMap != null) {
                for (Iterator entryIt = abbrevMap.entrySet().iterator(); entryIt.hasNext();) {
                    Map.Entry entry = (Map.Entry)entryIt.next();
                    String abbreviation = (String)entry.getKey();
                    String abbrevText = (String)entry.getValue();

                    String parametrizedText = abbrevText.replaceAll(
                            "([^|]+)[|]([^|]+)", "$1\\${cursor}$2"); // NOI18N
                    parametrizedText.replaceAll("[|][|]", "[|]"); // NOI18N

                    String desc = abbrevText;
                    int nlInd = abbrevText.indexOf('\n');
                    if (nlInd != -1) {
                        desc = abbrevText.substring(0, nlInd) + "..."; // NOI18N
                    }
                    StringBuffer htmlText = new StringBuffer();
                    ParametrizedTextParser parser = new ParametrizedTextParser(null, desc);
                    parser.parse();
                    parser.appendHtmlText(htmlText);
                    desc = htmlText.toString();

                    CodeTemplateDescription ctd = new CodeTemplateDescription(
                            abbreviation, desc, parametrizedText, null);
                    descriptionsInstances.add(ctd);

                }
            }

            // Start listening on 'abbrevMap' changes
            if (!settingsListeningInitialized) {
                settingsListeningInitialized = true;
                Settings.addSettingsChangeListener(this);
            }
        }
        return descriptionsInstances;
    }
    
    private void rebuildCodeTemplates() {
        Collection descriptionsInstances = descriptions.allInstances();
        descriptionsInstances = updateDescriptionInstances(descriptionsInstances);
        List/*<CodeTemplate>*/ codeTemplates = new ArrayList(descriptionsInstances.size());
        selectionTemplates = new ArrayList(descriptionsInstances.size());
        CodeTemplateApiPackageAccessor api = CodeTemplateApiPackageAccessor.get();
        // Construct template instances
        for (Iterator it = descriptionsInstances.iterator(); it.hasNext();) {
            CodeTemplateDescription description = (CodeTemplateDescription)it.next();
            CodeTemplate ct = api.createCodeTemplate(this, description.getAbbreviation(),
                    description.getDescription(), description.getParametrizedText());
            codeTemplates.add(ct);
            if (description.getParametrizedText().toLowerCase().indexOf("${selection") > -1) { //NOI18N
                selectionTemplates.add(ct);
            }
        }
        
        refreshMaps(codeTemplates);
    }
    
    private void refreshMaps(List/*<CodeTemplate>*/ codeTemplates) {
        abbrev2template = new HashMap(codeTemplates.size());
        sortedTemplatesByAbbrev = new ArrayList(codeTemplates.size());
        unmodSortedTemplatesByAbbrev = Collections.unmodifiableList(sortedTemplatesByAbbrev);
        sortedTemplatesByParametrizedText = new ArrayList(codeTemplates.size());
        // Construct template instances and store them in map and sorted list
        for (Iterator it = codeTemplates.iterator(); it.hasNext();) {
            CodeTemplate template = (CodeTemplate)it.next();
            String abbreviation = template.getAbbreviation();
            abbrev2template.put(abbreviation, template);
            sortedTemplatesByAbbrev.add(template);
            sortedTemplatesByParametrizedText.add(template);
        }
        // Sort the templates in case insensitive order
        Collections.sort(sortedTemplatesByAbbrev,
                CodeTemplateComparator.BY_ABBREVIATION_IGNORE_CASE);

        Collections.sort(sortedTemplatesByParametrizedText,
                CodeTemplateComparator.BY_PARAMETRIZED_TEXT_IGNORE_CASE);
    }

    public void resultChanged(LookupEvent ev) {
        rebuildCodeTemplates();
    }
    
    public void testInstallProcessorFactory(CodeTemplateProcessorFactory factory) {
        processorFactories = Collections.singletonList(factory);
    }
    
}
