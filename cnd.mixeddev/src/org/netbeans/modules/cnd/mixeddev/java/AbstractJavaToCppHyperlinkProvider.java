/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.mixeddev.java;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public abstract class AbstractJavaToCppHyperlinkProvider implements HyperlinkProviderExt {    
    
    private static final String JAVA_HYPERLINK_PROVIDER = "JavaHyperlinkProvider";
    
    private static HyperlinkProviderExt delegate;        
    
    
    protected abstract String getCppName(Document doc, int offset);
    
    protected abstract boolean navigate(Document doc, int offset);    
    
    
    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        HyperlinkProviderExt defaultProvider = getDelegate();
        if (defaultProvider != null) {
            return defaultProvider.isHyperlinkPoint(doc, offset, type);
        }
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        HyperlinkProviderExt defaultProvider = getDelegate();
        if (defaultProvider != null) {
            return defaultProvider.getHyperlinkSpan(doc, offset, type);
        }
        return JavaContextSupport.getIdentifierSpan(doc, offset, null);
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        if (!navigate(doc, offset)) {
            HyperlinkProviderExt defaultProvider = getDelegate();
            if (defaultProvider != null) {
                defaultProvider.performClickAction(doc, offset, type);
            }
        }
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        String cppName = getCppName(doc, offset);
        if (cppName != null) {
            return "<html><body>Search for <b>" + cppName + "</b></body></html>"; // NOI18N
        }
        HyperlinkProviderExt defaultProvider = getDelegate();
        if (defaultProvider != null) {
            return defaultProvider.getTooltipText(doc, offset, type);
        }
        return "Cannot navigate here!"; //NOI18N
    }    
    
    
    private synchronized HyperlinkProviderExt getDelegate() {
        if (delegate == null) {
            MimePath mimePath = MimePath.parse("text/x-java");
            Collection<? extends HyperlinkProviderExt> providers = MimeLookup.getLookup(mimePath).lookupAll(HyperlinkProviderExt.class);
            for(HyperlinkProviderExt provider : providers) {
                if (provider.getClass().getName().endsWith(JAVA_HYPERLINK_PROVIDER)) {
                    delegate = provider;
                    break;
                }
            }
        }
        return delegate;
    }        
}
