/*
 * HTMLListItem.java
 *
 * Created on October 17, 2002, 7:55 PM
 */

package org.netbeans.performance.spi.html;
/** Wrapper for list items 
 * @author Tim Boudreau
 */    
public class HTMLListItem extends HTMLTextItem {
    String topic;
    String topicLink = null;
    String target = null;
    public HTMLListItem(String topic, String description) {
        super(description);
        this.topic = topic;
    }

    public HTMLListItem(String topic, String description, String topicLink) {
        super(description);
        this.topic = topic;
        this.topicLink = topicLink;
    }

    public HTMLListItem(String topic, String description, String topicLink, String browserTarget) {
        super(description);
        this.topic = topic;
        this.topicLink = topicLink;
        this.target = browserTarget;
    }

    public void toHTML(StringBuffer sb) {
        sb.append("\n  <LI><B>");
        if (topicLink != null) {
            sb.append ("<A HREF=\"");
            sb.append (topicLink);
            sb.append ("\"");
            if (target != null) {
                sb.append (" TARGET=\"");
                sb.append (target);
                sb.append ("\"");
            }
            sb.append (">");
        }
        sb.append(topic);
        if (topicLink != null) {
            sb.append ("</A>");
        }
        sb.append("</B> - ");
        super.toHTML(sb);
        sb.append("</LI>");
    }
}
