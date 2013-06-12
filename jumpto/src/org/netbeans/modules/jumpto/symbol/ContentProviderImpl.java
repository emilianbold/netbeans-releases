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
package org.netbeans.modules.jumpto.symbol;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.jumpto.EntitiesListCellRenderer;
import org.netbeans.modules.jumpto.common.HighlightingNameFormatter;
import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.awt.HtmlRenderer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
final class ContentProviderImpl implements GoToPanel.ContentProvider {
    
    private static final Logger LOG = Logger.getLogger(ContentProviderImpl.class.getName());
    private static final Pattern camelCasePattern = Pattern.compile("(?:\\p{javaUpperCase}(?:\\p{javaLowerCase}|\\p{Digit}|\\.|\\$)*){2,}"); // NOI18N
    private static final RequestProcessor rp = new RequestProcessor (ContentProviderImpl.class);
        
    private final JButton okButton;
    private final AtomicReference<Collection<? extends SymbolProvider>> typeProviders =
            new AtomicReference<Collection<? extends SymbolProvider>>();
    //@GuardedBy("this")
    private RequestProcessor.Task task;
    //@GuardedBy("this")
    private Worker running;
    //threading: accessed only in EDT
    private Dialog dialog;
    

    public ContentProviderImpl(final JButton okButton) {
        this.okButton = okButton;
    }
    
    
    void setDialog(final Dialog dialog) {
        this.dialog = dialog;
    }


    @Override
    public ListCellRenderer getListCellRenderer(
            @NonNull final JList list,
            @NonNull final ButtonModel caseSensitive) {
        Parameters.notNull("list", list);   //NOI18N
        Parameters.notNull("caseSensitive", caseSensitive); //NOI18N
        return new Renderer(list, caseSensitive);
    }

    @Override
    public void setListModel(GoToPanel panel, String text) {
        if (okButton != null) {
            okButton.setEnabled (false);
        }
        final Worker workToCancel;
        final RequestProcessor.Task  taskToCancel;
        synchronized (this) {
            workToCancel = running;
            taskToCancel = task;
            running = null;
            task = null;
        }
        if (workToCancel != null) {
                workToCancel.cancel();
        }
        if (taskToCancel != null) {
                taskToCancel.cancel();
        }
        
        if ( text == null ) {
            panel.setModel(new DefaultListModel(), -1);
            return;
        }
        final boolean exact = text.endsWith(" "); // NOI18N
        final boolean isCaseSensitive = panel.isCaseSensitive();        
        text = text.trim();        
        if ( text.length() == 0) {
            panel.setModel(new DefaultListModel(), -1);
            return;
        }
        // Compute in other thread
        
        synchronized( this ) {
            running = new Worker(text, exact, isCaseSensitive, panel);
            task = rp.post( running, 220);
            if ( panel.time != -1 ) {
                LOG.log(
                   Level.FINE,
                   "Worker posted after {0} ms.",   //NOI18N
                   System.currentTimeMillis() - panel.time);                
            }
        }
    }

    @Override
    public void closeDialog() {
        if (dialog != null) {
            dialog.setVisible( false );
            DialogFactory.storeDialogDimensions(
                    new Dimension(dialog.getWidth(), dialog.getHeight()));
            dialog.dispose();
            dialog = null;
            cleanUp();
        }
    }

    @Override
    public boolean hasValidContent() {
        return this.okButton.isEnabled();
    }
    
    private void cleanUp() {
        for (SymbolProvider provider : getTypeProviders()) {
            provider.cleanup();
        }
    }
            
    private Collection<? extends SymbolProvider> getTypeProviders() {
        Collection<? extends SymbolProvider> res = typeProviders.get();
        if (res == null) {                   
            res = Arrays.asList(Lookup.getDefault().lookupAll(SymbolProvider.class).toArray(new SymbolProvider[0]));
            if (!typeProviders.compareAndSet(null, res)) {
                res = typeProviders.get();
            }
        }
        return res;
    }
    
    private static class MyPanel extends JPanel {
	
	private SymbolDescriptor td;
	
	void setDescriptor(SymbolDescriptor td) {
	    this.td = td;
	    // since the same component is reused for dirrerent list itens, 
	    // null the tool tip
	    putClientProperty(TOOL_TIP_TEXT_KEY, null);
	}

	@Override
	public String getToolTipText() {
	    // the tool tip is gotten from the descriptor 
	    // and cached in the standard TOOL_TIP_TEXT_KEY property
	    String text = (String) getClientProperty(TOOL_TIP_TEXT_KEY);
	    if( text == null ) {
                if( td != null ) {                    
                    text = td.getFileDisplayPath();
                }
                putClientProperty(TOOL_TIP_TEXT_KEY, text);
	    }
	    return text;
	}
    }
    
    private static class Renderer extends EntitiesListCellRenderer implements ActionListener {

        private final HighlightingNameFormatter symbolNameFormatter;
         
        private MyPanel rendererComponent;
        private JLabel jlName = HtmlRenderer.createLabel();
        private JLabel jlOwner = new JLabel();
        private JLabel jlPrj = new JLabel();
        private int DARKER_COLOR_COMPONENT = 5;
        private int LIGHTER_COLOR_COMPONENT = 80;        
        private Color fgColor;
        private Color fgColorLighter;
        private Color bgColor;
        private Color bgColorDarker;
        private Color bgSelectionColor;
        private Color fgSelectionColor;
        
        private JList jList;
        private boolean caseSensitive;
        
        public Renderer(
                @NonNull final JList list,
                @NonNull final ButtonModel caseSensitive) {
            
            jList = list;
            this.caseSensitive = caseSensitive.isSelected();
            resetName();
            Container container = list.getParent();
            if ( container instanceof JViewport ) {
                ((JViewport)container).addChangeListener(this);
                stateChanged(new ChangeEvent(container));
            }
            
            rendererComponent = new MyPanel();
            rendererComponent.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;            
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets (0,0,0,7);
            rendererComponent.add( jlName, c);
            jlOwner.setOpaque(false);
            jlOwner.setFont(list.getFont());
            c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.1;            
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets (0,0,0,7);
            rendererComponent.add( jlOwner, c);
            
            c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;            
            c.anchor = GridBagConstraints.EAST;
            rendererComponent.add( jlPrj, c);
            
            
            jlPrj.setOpaque(false);            
            jlPrj.setFont(list.getFont());
            
            
            jlPrj.setHorizontalAlignment(RIGHT);
            jlPrj.setHorizontalTextPosition(LEFT);
            
            // setFont( list.getFont() );            
            fgColor = list.getForeground();
            fgColorLighter = new Color( 
                                   Math.min( 255, fgColor.getRed() + LIGHTER_COLOR_COMPONENT),
                                   Math.min( 255, fgColor.getGreen() + LIGHTER_COLOR_COMPONENT),
                                   Math.min( 255, fgColor.getBlue() + LIGHTER_COLOR_COMPONENT)
                                  );
                            
            bgColor = new Color( list.getBackground().getRGB() );
            bgColorDarker = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
            bgSelectionColor = list.getSelectionBackground();
            fgSelectionColor = list.getSelectionForeground();
            symbolNameFormatter = HighlightingNameFormatter.createBoldFormatter();
            caseSensitive.addActionListener(this);
        }
        
        public Component getListCellRendererComponent( JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean hasFocus) {
            
            // System.out.println("Renderer for index " + index );
            
            int height = list.getFixedCellHeight();
            int width = list.getFixedCellWidth() - 1;
            
            width = width < 200 ? 200 : width;
            
            // System.out.println("w, h " + width + ", " + height );
            
            Dimension size = new Dimension( width, height );
            rendererComponent.setMaximumSize(size);
            rendererComponent.setPreferredSize(size);                        
            resetName();
            if ( isSelected ) {
                jlName.setForeground(fgSelectionColor);
                jlOwner.setForeground(fgSelectionColor);
                jlPrj.setForeground(fgSelectionColor);
                rendererComponent.setBackground(bgSelectionColor);
            }
            else {
                jlName.setForeground(fgColor);
                jlOwner.setForeground(fgColorLighter);
                jlPrj.setForeground(fgColor);
                rendererComponent.setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }
            
            if ( value instanceof SymbolDescriptor ) {
                long time = System.currentTimeMillis();
                SymbolDescriptor td = (SymbolDescriptor)value;                
                jlName.setIcon(td.getIcon());
                final String formattedSymbolName = symbolNameFormatter.formatName(
                        td.getSymbolName(),
                        SymbolProviderAccessor.DEFAULT.getHighlightText(td),
                        caseSensitive,
                        isSelected? fgSelectionColor : fgColor);
                jlName.setText(formattedSymbolName);
                jlOwner.setText(NbBundle.getMessage(GoToSymbolAction.class, "MSG_DeclaredIn",td.getOwnerName()));
                setProjectName(jlPrj, td.getProjectName());
                jlPrj.setIcon(td.getProjectIcon());
		rendererComponent.setDescriptor(td);
                LOG.fine("  Time in paint " + (System.currentTimeMillis() - time) + " ms.");
            }
            else {
                jlName.setText( value.toString() );
            }
            
            return rendererComponent;
        }
        
        public void stateChanged(ChangeEvent event) {
            
            JViewport jv = (JViewport)event.getSource();
            
            jlName.setText( "Sample" ); // NOI18N
            //jlName.setIcon(UiUtils.getElementIcon(ElementKind.CLASS, null));
            jlName.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/type/sample.png", false));
            
            jList.setFixedCellHeight(jlName.getPreferredSize().height);
            jList.setFixedCellWidth(jv.getExtentSize().width);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            caseSensitive = ((ButtonModel)e.getSource()).isSelected();
        }

        private void resetName() {
            ((HtmlRenderer.Renderer)jlName).reset();
            jlName.setFont(jList.getFont());
            jlName.setOpaque(false);
            ((HtmlRenderer.Renderer)jlName).setHtml(true);
            ((HtmlRenderer.Renderer)jlName).setRenderStyle(HtmlRenderer.STYLE_TRUNCATE);
        }

     }
    
    private class Worker implements Runnable {
        
        private final String text;
        private final boolean exact;
        private final boolean isCaseSensitive;
        private final long createTime;
        private final GoToPanel panel;
        
        private volatile boolean isCanceled = false;
        private volatile SymbolProvider current;
        private final int textId;
        
        public Worker(
                @NonNull final String text,
                final boolean exact,
                final boolean isCaseSensitive,
                @NonNull final GoToPanel panel ) {
            this.text = text;
            this.exact = exact;
            this.isCaseSensitive = isCaseSensitive;
            this.panel = panel;
            this.createTime = System.currentTimeMillis();
            this.textId = panel.getTextId();
            LOG.log(
                Level.FINE,
                "Worker for {0} - created after {1} ms.", //NOI18N
                new Object[]{text, System.currentTimeMillis() - panel.time});                
       }
        
        @Override
        public void run() {
            LOG.log(
                Level.FINE,
                "Worker for {0} - started {1} ms.", //NOI18N
                new Object[]{text, System.currentTimeMillis() - createTime});                
            
            final List<? extends SymbolDescriptor> types = getSymbolNames( text );
            if ( isCanceled ) {
                LOG.log(
                    Level.FINE,
                    "Worker for {0} exited after cancel {1} ms.", //NOI18N
                    new Object[]{text, System.currentTimeMillis() - createTime});                                
                return;
            }
            final ListModel fmodel = Models.fromList(types);
            if ( isCanceled ) {            
                LOG.log(
                    Level.FINE,
                    "Worker for {0} exited after cancel {1} ms.", //NOI18N
                    new Object[]{text, System.currentTimeMillis() - createTime});                                
                return;
            }
            
            if ( !isCanceled && fmodel != null ) {                
                LOG.log(
                    Level.FINE,
                    "Worker for text {0} finished after {1} ms.", //NOI18N
                    new Object[]{text, System.currentTimeMillis() - createTime});                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        panel.setModel(fmodel, textId);
                        if (okButton != null && !types.isEmpty()) {
                            okButton.setEnabled (true);
                        }
                    }
                });
            }
            
            
        }
        
        public void cancel() {
            if ( panel.time != -1 ) {
                LOG.log(
                    Level.FINE,
                    "Worker for text {0} canceled after {1} ms.", //NOI18N
                    new Object[]{text, System.currentTimeMillis() - createTime});                
            }
            SymbolProvider _provider;
            synchronized (this) {
                isCanceled = true;
                _provider = current;
            }
            if (_provider != null) {
                _provider.cancel();
            }
        }

        @SuppressWarnings("unchecked")
        private List<? extends SymbolDescriptor> getSymbolNames(String text) {
            // TODO: Search twice, first for current project, then for all projects
            List<SymbolDescriptor> items;
            // Multiple providers: merge results
            items = new ArrayList<SymbolDescriptor>(128);
            String[] message = new String[1];                        
            for (SymbolProvider provider : getTypeProviders()) {
                current = provider;
                if (isCanceled) {
                    return null;
                }
                LOG.log(
                    Level.FINE,
                    "Calling SymbolProvider: {0}", //NOI18N
                    provider);                
                final SymbolProvider.Context context = SymbolProviderAccessor.DEFAULT.createContext(null, text, getSearchType(text, exact, isCaseSensitive));
                final SymbolProvider.Result result = SymbolProviderAccessor.DEFAULT.createResult(items, message, context);
                provider.computeSymbolNames(context, result);
                current = null;
            }
            if ( !isCanceled ) {   
                Collections.sort(items, new SymbolComparator());
                panel.setWarning(message[0]);
                return items;
            }
            else {
                return null;
            }
        }
    }

    @NonNull
    private static SearchType getSearchType(
            @NonNull final String text,
            final boolean exact,
            final boolean isCaseSensitive) {
        int wildcard = containsWildCard(text);
        if (exact) {
            //nameKind = isCaseSensitive ? SearchType.EXACT_NAME : SearchType.CASE_INSENSITIVE_EXACT_NAME;
            return SearchType.EXACT_NAME;
        } else if ((isAllUpper(text) && text.length() > 1) || isCamelCase(text)) {
            return SearchType.CAMEL_CASE;
        } else if (wildcard != -1) {
            return isCaseSensitive ? SearchType.REGEXP : SearchType.CASE_INSENSITIVE_REGEXP;
        } else {
            return isCaseSensitive ? SearchType.PREFIX : SearchType.CASE_INSENSITIVE_PREFIX;
        }
    }

    private static boolean isAllUpper( String text ) {
    for( int i = 0; i < text.length(); i++ ) {
        if ( !Character.isUpperCase( text.charAt( i ) ) ) {
            return false;
        }
    }

    return true;
    }

    private static int containsWildCard( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( text.charAt( i ) == '?' || text.charAt( i ) == '*' ) { // NOI18N
                return i;
            }
        }
        return -1;
    }

    private static boolean isCamelCase(String text) {
         return camelCasePattern.matcher(text).matches();
    }
}
