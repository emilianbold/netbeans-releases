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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.api.editor.settings.CodeTemplateSettings;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.spi.*;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Code template allows the client to paste itself into the given
 * text component.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateManagerOperation
    implements LookupListener, Runnable
{
    private static final Logger LOG = Logger.getLogger(CodeTemplateManagerOperation.class.getName());
    
    private static final Map<String, Reference<CodeTemplateManagerOperation>> mime2operation = 
            new HashMap<String, Reference<CodeTemplateManagerOperation>>(8);
    
    private static final KeyStroke DEFAULT_EXPANSION_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    
    public static synchronized CodeTemplateManager getManager(Document doc) {
        return get(doc).getManager();
    }

    public static synchronized CodeTemplateManagerOperation get(Document doc) {
        String mimeType = (String)doc.getProperty("mimeType"); //NOI18N
        CodeTemplateManagerOperation operation = (CodeTemplateManagerOperation)
                doc.getProperty(CodeTemplateManagerOperation.class);
        boolean mimesEqual = (operation != null) && mimeTypesEqual(mimeType,
                operation.getMimeType());

        if (!mimesEqual) {
            Reference<CodeTemplateManagerOperation> ref = mime2operation.get(mimeType);
            operation = ref == null ? null : ref.get();
            if (operation == null) {
                operation = new CodeTemplateManagerOperation(mimeType);
                mime2operation.put(mimeType, new WeakReference<CodeTemplateManagerOperation>(operation));
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
    private final Lookup.Result<CodeTemplateSettings> ctslr;
    private final EventListenerList listenerList = new EventListenerList();

    private boolean loaded = false;
    private Map<String, CodeTemplate> abbrev2template = Collections.<String, CodeTemplate>emptyMap();
    private List<CodeTemplate> sortedTemplatesByAbbrev = Collections.<CodeTemplate>emptyList();
    private List<CodeTemplate> sortedTemplatesByParametrizedText = Collections.<CodeTemplate>emptyList();
    private List<CodeTemplate> selectionTemplates = Collections.<CodeTemplate>emptyList();
    private KeyStroke expansionKey = DEFAULT_EXPANSION_KEY;
    private String expansionKeyText = getExpandKeyStrokeText(expansionKey);
    
    private CodeTemplateManagerOperation(String mimeType) {
        this.mimeType = mimeType;
        
        this.manager = CodeTemplateApiPackageAccessor.get().createCodeTemplateManager(this);
        assert manager != null : "Can't creat CodeTemplateManager"; //NOI18N
        
        this.ctslr = MimeLookup.getLookup(MimePath.parse(mimeType)).lookupResult(CodeTemplateSettings.class);
        this.ctslr.addLookupListener(WeakListeners.create(LookupListener.class, this, this.ctslr));
        
        // Compute descriptions asynchronously
        RequestProcessor.getDefault().post(this);
    }
    
    public CodeTemplateManager getManager() {
        return manager;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public Collection<? extends CodeTemplate> getCodeTemplates() {
        return sortedTemplatesByAbbrev;
    }
    
    public Collection<? extends CodeTemplate> findSelectionTemplates() {
        return selectionTemplates;
    }
    
    public CodeTemplate findByAbbreviation(String abbreviation) {
        return abbrev2template.get(abbreviation);
    }
    
    public Collection<? extends CodeTemplate> findByParametrizedText(String prefix, boolean ignoreCase) {
        List<CodeTemplate> result = new ArrayList<CodeTemplate>();
        
        int low = 0;
	int high = sortedTemplatesByParametrizedText.size() - 1;
	while (low <= high) {
	    int mid = (low + high) >> 1;
	    CodeTemplate t = sortedTemplatesByParametrizedText.get(mid);
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
            CodeTemplate t = sortedTemplatesByParametrizedText.get(i);
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
            CodeTemplate t = sortedTemplatesByParametrizedText.get(i);
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
    
    public static Collection<? extends CodeTemplateFilter> getTemplateFilters(JTextComponent component, int offset) {
        String mimeType = NbEditorUtilities.getMimeType(component);
        Collection<? extends CodeTemplateFilter.Factory> filterFactories = 
            MimeLookup.getLookup(MimePath.parse(mimeType)).lookupAll(CodeTemplateFilter.Factory.class);
        
        List<CodeTemplateFilter> result = new ArrayList<CodeTemplateFilter>(filterFactories.size());
        for (CodeTemplateFilter.Factory factory : filterFactories) {
            result.add(factory.createFilter(component, offset));
        }
        return result;
    }

    public static void insert(CodeTemplate codeTemplate, JTextComponent component) {
        String mimeType = NbEditorUtilities.getMimeType(component);
        Collection<? extends CodeTemplateProcessorFactory> processorFactories = 
            MimeLookup.getLookup(MimePath.parse(mimeType)).lookupAll(CodeTemplateProcessorFactory.class);
        
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
            return loaded;
        }
    }
    
    public void registerLoadedListener(ChangeListener listener) {
        synchronized (listenerList) {
            if (!isLoaded()) {
                // not yet loaded
                listenerList.add(ChangeListener.class, listener);
                return;
            }
        }

        // already loaded
        listener.stateChanged(new ChangeEvent(manager));
    }
    
    public void waitLoaded() {
        synchronized (listenerList) {
            while(!isLoaded()) {
                try {
                    listenerList.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted when waiting to load code templates"); //NOI18N
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
        rebuildCodeTemplates();
    }
    
    private static void processCodeTemplateDescriptions(
        CodeTemplateManagerOperation operation,
        Collection<? extends CodeTemplateDescription> ctds,
        Map<String, CodeTemplate> codeTemplatesMap,
        List<CodeTemplate> codeTemplatesWithSelection
    ) {
        for (CodeTemplateDescription ctd : ctds) {
            CodeTemplate ct = CodeTemplateApiPackageAccessor.get().createCodeTemplate(
                operation, 
                ctd.getAbbreviation(),
                ctd.getDescription(), 
                ctd.getParametrizedText(),
                ctd.getContexts()
            );
            
            codeTemplatesMap.put(ct.getAbbreviation(), ct);
            if (ct.getParametrizedText().toLowerCase().indexOf("${selection") > -1) { //NOI18N
                codeTemplatesWithSelection.add(ct);
            }
        }
    }
    
    private void rebuildCodeTemplates() {
        Collection<? extends CodeTemplateSettings> allCts = ctslr.allInstances();
        CodeTemplateSettings cts = allCts.isEmpty() ? null : allCts.iterator().next();

        Map<String, CodeTemplate> map = new HashMap<String, CodeTemplate>();
        List<CodeTemplate> templatesWithSelection = new ArrayList<CodeTemplate>();
        KeyStroke keyStroke = DEFAULT_EXPANSION_KEY;
        
        if (cts != null) {
            // Load templates
            Collection<? extends CodeTemplateDescription> ctds = cts.getCodeTemplateDescriptions();
            processCodeTemplateDescriptions(this, ctds, map, templatesWithSelection);
            
            // Load expansion key
            keyStroke = patchExpansionKey(cts.getExpandKey());
        } else {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.warning("Can't find CodeTemplateSettings for '" + mimeType + "'"); //NOI18N
            }
        }

        List<CodeTemplate> byAbbrev = new ArrayList<CodeTemplate>(map.values());
        Collections.sort(byAbbrev, CodeTemplateComparator.BY_ABBREVIATION_IGNORE_CASE);

        List<CodeTemplate> byText = new ArrayList<CodeTemplate>(map.values());
        Collections.sort(byText, CodeTemplateComparator.BY_PARAMETRIZED_TEXT_IGNORE_CASE);

        boolean fire = false;

        synchronized(listenerList) {
            fire = abbrev2template == null;

            abbrev2template = Collections.unmodifiableMap(map);
            sortedTemplatesByAbbrev = Collections.unmodifiableList(byAbbrev);
            sortedTemplatesByParametrizedText = Collections.unmodifiableList(byText);
            selectionTemplates = Collections.unmodifiableList(templatesWithSelection);
            expansionKey = keyStroke;
            expansionKeyText = getExpandKeyStrokeText(keyStroke);
            
            loaded = true;
            listenerList.notifyAll();
        }

        if (fire) {
            fireStateChanged(new ChangeEvent(manager));
        }
    }
    
    public KeyStroke getExpansionKey() {
        return expansionKey;
    }

    public String getExpandKeyStrokeText() {
        return expansionKeyText;
    }
    
    private static String getExpandKeyStrokeText(KeyStroke keyStroke) {
        String expandKeyStrokeText;
        if (keyStroke.equals(KeyStroke.getKeyStroke(' '))) { //NOI18N
            expandKeyStrokeText = "SPACE"; // NOI18N
        } else if (keyStroke.equals(KeyStroke.getKeyStroke(new Character(' '), InputEvent.SHIFT_MASK))) { //NOI18N
            expandKeyStrokeText = "Shift-SPACE"; // NOI18N
        } else if (keyStroke.equals(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))) {
            expandKeyStrokeText = "TAB"; // NOI18N
        } else if (keyStroke.equals(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0))) {
            expandKeyStrokeText = "ENTER"; // NOI18N
        } else {
            expandKeyStrokeText = keyStroke.toString();
        }
        return expandKeyStrokeText;
    }
    
    private static KeyStroke patchExpansionKey(KeyStroke eks) {
	// Patch the keyPressed => keyTyped to prevent insertion of expand chars into editor
        if (eks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0))) {
            eks = KeyStroke.getKeyStroke(' ');
        } else if (eks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK))) {
            eks = KeyStroke.getKeyStroke(new Character(' '), InputEvent.SHIFT_MASK);
        } else if (eks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))) {
        } else if (eks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0))) {
        }
	return eks;
    }
    
    public void resultChanged(LookupEvent ev) {
        rebuildCodeTemplates();
    }
}
