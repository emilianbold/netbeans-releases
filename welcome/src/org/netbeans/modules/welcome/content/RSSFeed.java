/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome.content;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RSSFeed extends JScrollPane implements Constants, PropertyChangeListener {
    
    private static final int NEWS_COUNT = 10;
    private static final String RSS_FEED = "RSSFeed"; // NOI18N
    private static final String HTML_VIEWER = "HTMLViewer"; // NOI18N
    
    private boolean firstTime = true;
    private String url;

    private RequestProcessor.Task reloadTimer;
    private long lastReload = 0;
    
    public RSSFeed( String url ) {
        this.url = url;
        setBorder(null);
        setOpaque(false);

        setBackground( DEFAULT_BACKGROUND_COLOR );
        getViewport().setBackground( DEFAULT_BACKGROUND_COLOR );
        setViewportView( buildContentLoadingLabel() );
        
        setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

        HttpProxySettings.getDefault().addPropertyChangeListener( this );
    }
    
    private void setContent( Component content ) {
        setViewportView( content );
        setCursor( Cursor.getDefaultCursor() );
    }
    
    public void reload() {
        new Reload().start();
    }

    protected ArrayList/*<Node>*/ buildHtmlNodeList() throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document domDocument = builder.parse( url );
        NodeList items = domDocument.getElementsByTagName("item"); // NOI18N
        ArrayList res = new ArrayList( items.getLength() );
        for( int i=0; i<items.getLength(); i++ )
            res.add( items.item( i ) );
        return res;
    }

    private class Reload extends Thread {
        public void run() {
            try {
                setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

                ArrayList nodeList = buildHtmlNodeList();
                final JPanel contentPanel = new NoHorizontalScrollPanel();
                contentPanel.setOpaque( false );
                int contentRow = 0;

                for( int i=0; i<Math.min(nodeList.size(), NEWS_COUNT); i++ ) {
                    Node node = (Node)nodeList.get(i);
                    NodeList children = node.getChildNodes();

                    String date = null;
                    String link = null;
                    String title = null;
                    String description = null;

                    for( int j=0; j<children.getLength(); j++ ) {
                        Node child = children.item(j);

                        String tag = child.getNodeName();

                        String content = getTextContent( child );

                        if ((content != null) && content.length() == 0) {
                            content = null;
                        }

                        if (tag.equals("title")) { // NOI18N
                            title = content;
                        } else if (tag.equals("description")) { // NOI18N
                            description = content;
                        } else if (tag.equals("link")) { // NOI18N
                            link = content;
                        } else if (tag.equals("date") || tag.equals("pubDate")) { // NOI18N // NOI18N
                            date = content;
                        }
                    }
                    
                    if( null != title && null != link ) {
                        JPanel panel = new JPanel( new GridBagLayout() );
                        panel.setOpaque( false );
                        int row = 0;
                        if (date != null) {
                            JLabel label = new JLabel( date );
                            label.setFont( REGULAR_FONT.deriveFont( REGULAR_FONT.getStyle(), REGULAR_FONT.getSize()-1 ) );
                            panel.add( label, new GridBagConstraints(0,row++,1,1,0.0,0.0,
                                    GridBagConstraints.WEST,GridBagConstraints.NONE,
                                    new Insets(0,TEXT_INSETS_LEFT+5,2,TEXT_INSETS_RIGHT),0,0 ) );
                        }

                        WebLink linkButton = new WebLink( title, link );
                        linkButton.setFont( HEADER_FONT );
                        linkButton.setForeground( HEADER_TEXT_COLOR );
                        linkButton.setIcon( BULLET_ICON );
                        panel.add( linkButton, new GridBagConstraints(0,row++,1,1,1.0,1.0,
                                GridBagConstraints.WEST,GridBagConstraints.BOTH,
                                new Insets(0,0,2,TEXT_INSETS_RIGHT),0,0 ) );


                        if (description != null) {
                            JLabel label = new JLabel( "<html>"+trimHtml( description ) );
                            label.setFont( REGULAR_FONT.deriveFont( REGULAR_FONT.getStyle(), REGULAR_FONT.getSize()-1 ) );
                            panel.add( label, new GridBagConstraints(0,row++,1,1,1.0,1.0,
                                    GridBagConstraints.WEST,GridBagConstraints.BOTH,
                                    new Insets(0,TEXT_INSETS_LEFT+5,0,TEXT_INSETS_RIGHT),0,0 ) );
                        }

                        contentPanel.add( panel, new GridBagConstraints(0,contentRow++,1,1,1.0,1.0,
                                GridBagConstraints.NORTHWEST,GridBagConstraints.BOTH,
                                new Insets(contentRow==1 ? UNDER_HEADER_MARGIN : 0,0,16,0),0,0 ) );
                    }
                }

                lastReload = System.currentTimeMillis();

                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        setContent( contentPanel );
                    }
                });

                //schedule feed reload
                reloadTimer = RequestProcessor.getDefault().post( this, RSS_FEED_TIMER_RELOAD_MILLIS );
            } catch( UnknownHostException uhE ) {
                setCursor( Cursor.getDefaultCursor() );
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        setContent( buildProxyPanel() );
                    }
                });
            } catch( Exception e ) {
                setCursor( Cursor.getDefaultCursor() );
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        setContent( buildErrorLabel() );
                    }
                });
                ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
            }
        }
    }
    
    protected String getTextContent(Node node) {
        Node child = node.getFirstChild();
        if( null == child )
            return null;
        
        return child.getNodeValue();
    }
    
    private static final long serialVersionUID = 1L; 

    public Dimension getPreferredSize() {
        Dimension retValue = super.getPreferredSize();
        retValue.width = 1;
        retValue.height = 1;
        return retValue;
    }
    
    public void removeNotify() {
        //cancel reload timer
        if( null != reloadTimer ) {
            reloadTimer.cancel();
            reloadTimer = null;
        }
        super.removeNotify();
    }

    public void addNotify() {
        super.addNotify();
        if( null == reloadTimer ) {
            if( System.currentTimeMillis() - lastReload >= RSS_FEED_TIMER_RELOAD_MILLIS ) {
                reload();
            } else {
                reloadTimer = RequestProcessor.getDefault().post( new Reload(),
                        (int)(RSS_FEED_TIMER_RELOAD_MILLIS - (System.currentTimeMillis() - lastReload)) );
            }
        }
    }

    private String trimHtml( String htmlSnippet ) {
        String res = htmlSnippet.replaceAll( "<[^>]*>", "" ); // NOI18N // NOI18N
        res = res.replaceAll( "&nbsp;", " " ); // NOI18N // NOI18N
        res = res.trim();
        if( res.length() > 100 ) {
            res = res.substring( 0, 100 ) + "..."; // NOI18N
        }
        return res;
    }

    private JComponent buildProxyPanel() {
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );
        panel.add( new JLabel(BundleSupport.getLabel("ErrCannotConnect")),  // NOI18N
                new GridBagConstraints(0,0,1,1,0.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
        JButton button = new JButton();
        Mnemonics.setLocalizedText( button, BundleSupport.getLabel( "ProxyConfig" ) );  // NOI18N
        button.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HttpProxySettings.getDefault().showConfigurationDialog();
            }
        });
        panel.add( button, new GridBagConstraints(0,1,1,1,0.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5,10,10,5),0,0 ) );
        return panel;
    }

    private JComponent buildContentLoadingLabel() {
        JLabel label = new JLabel( BundleSupport.getLabel( "ContentLoading" ) ); // NOI18N
        label.setHorizontalAlignment( JLabel.CENTER );
        label.setVerticalAlignment( JLabel.CENTER );
        label.setForeground( DEFAULT_TEXT_COLOR );
        label.setBackground( DEFAULT_BACKGROUND_COLOR );
        label.setOpaque( false );
        return label;
    }

    private JComponent buildErrorLabel() {
        JLabel label = new JLabel( BundleSupport.getLabel( "ErrLoadingFeed" ) ); // NOI18N
        label.setHorizontalAlignment( JLabel.CENTER );
        label.setVerticalAlignment( JLabel.CENTER );
        label.setForeground( DEFAULT_TEXT_COLOR );
        label.setBackground( DEFAULT_BACKGROUND_COLOR );
        label.setOpaque( false );
        return label;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if( HttpProxySettings.PROXY_SETTINGS.equals( evt.getPropertyName() ) ) {
            setViewportView( buildContentLoadingLabel() );
            reload();
        }
    }

    private static class NoHorizontalScrollPanel extends JPanel implements Scrollable {
        public NoHorizontalScrollPanel() {
            super( new GridBagLayout() );
        }

        public Dimension getPreferredScrollableViewportSize() {
            return super.getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Utils.getDefaultFontSize();
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10*Utils.getDefaultFontSize();
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
