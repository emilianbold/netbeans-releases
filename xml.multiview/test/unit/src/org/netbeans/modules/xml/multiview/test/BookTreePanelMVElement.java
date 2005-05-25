package org.netbeans.modules.xml.multiview.test;

import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.nodes.*;
import org.netbeans.modules.xml.multiview.test.bookmodel.*;
import org.netbeans.modules.xml.multiview.Error;
import javax.swing.*;
/**
 *
 * @author mkuchtiak
 */
public class BookTreePanelMVElement extends TreePanelMultiViewElement {
    private TreePanelDesignEditor comp;
    private BookDataObject dObj;
    private PanelView view;
    //private PanelFactory factory;
    
    /** Creates a new instance of DesignMultiViewElement */
    public BookTreePanelMVElement(BookDataObject dObj) {
        super(dObj);
        this.dObj=dObj;
        view = new BookView(dObj);
        comp = new TreePanelDesignEditor(view);
        setVisualEditor(comp);
    }
    
    public void componentShowing() {
        super.componentShowing();
    }

    private class BookView extends PanelView {
        BookView(BookDataObject dObj) {
            super();           
            Children rootChildren = new Children.Array();
            Node root = new AbstractNode(rootChildren);
            try {
                Book book = dObj.getBook();
                Node bookNode = new BookNode(book);
                
                Chapter[] chapters = book.getChapter();
                Node[] chapterNode = new Node[chapters.length];
                Children ch = new Children.Array();
                for (int i=0;i<chapters.length;i++) {
                    chapterNode[i] = new ChapterNode(chapters[i]);
                }
                ch.add(chapterNode);
                Node chaptersNode = new SectionContainerNode(ch);
                chaptersNode.setDisplayName("Chapters");
                rootChildren.add(new Node[]{bookNode,chaptersNode});
                // add panels
            } catch (java.io.IOException ex) {
                System.out.println("ex="+ex);
                root.setDisplayName("Invalid Book");
            }
            setRoot(root);
        }
        
        public void initComponents() {
            setLayout(new java.awt.BorderLayout());
            JPanel scrollPanel= new JPanel();
            scrollPanel.add(new JButton("Hello"));
            JScrollPane scrollPane = new javax.swing.JScrollPane();
            scrollPane.setViewportView(scrollPanel);
            //scrollPane.getVerticalScrollBar().setUnitIncrement(15);
            //add (scrollPane, java.awt.BorderLayout.CENTER);
            add(scrollPanel, java.awt.BorderLayout.CENTER);
        }
        
        public void showSelection(Node[] node) {
            System.out.println("showSelection()");
        }
        
        public Error validateView() {
            try {
                Book book = dObj.getBook();
                String title = book.getTitle();
                if (title==null || title.length()==0) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(book,"title"); //NOI18N
                    return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                }
                Chapter[] chapters = book.getChapter();
                for (int i=0;i<chapters.length;i++) {
                    title = chapters[i].getTitle();
                    if (title==null || title.length()==0) {
                        Error.ErrorLocation loc = new Error.ErrorLocation(chapters[i],"title");
                        return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                    }
                    for (int j=0;j<chapters.length;j++) {
                        String tit = chapters[j].getTitle();
                        if (i!=j && title.equals(tit)) {
                            Error.ErrorLocation loc = new Error.ErrorLocation(chapters[i],"title");
                            return new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc);
                        }
                    }
                }
            } catch (java.io.IOException ex){}
            return null;
        }
    }
    
    private class BookNode extends org.openide.nodes.AbstractNode {
        BookNode(Book book) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(book.getTitle());
            //setIconBase("org/netbeans/modules/web/dd/multiview/resources/class"); //NOI18N
        }    
    }
    private class ChapterNode extends org.openide.nodes.AbstractNode {
        ChapterNode(Chapter chapter) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(chapter.getTitle());
            //setIconBase("org/netbeans/modules/web/dd/multiview/resources/class"); //NOI18N
        }    
    }
    
}
