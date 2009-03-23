package org.netbeans.modules.xml.wsdl.model.extensions.soap12.validation;

import org.netbeans.modules.xml.wsdl.model.Message;

public class MessagePart {

    private final String messageName;
    private final String partName;
    private final int hashCode;

    public MessagePart(Message message, String partName) {
        if (message == null) {
            throw new NullPointerException("null messageName");
        }
        if (partName == null) {
            throw new NullPointerException("null partName");
        }
        if ("".equals(partName)) {
            throw new IllegalArgumentException("blank partName");
        }

        messageName = message.getName();
        this.partName = partName;
        hashCode = messageName.toString().concat(partName).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || !(other instanceof MessagePart)) {
            return false;
        }

        MessagePart that = (MessagePart) other;
        if (!messageName.equals(that.messageName)) {
            return false;
        }

        return partName.equals(that.partName);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
