package org.netbeans.modules.java.j2seproject.ui.customizer.vmo;

import org.antlr.runtime.Token;

import java.util.Map;

/**
 * @author Rastislav Komara
 */
public class UserPropertyNode extends JavaVMOption<OptionValue<Map.Entry<String, String>>> {
    
    public static final String NAME = "D";

    public UserPropertyNode(Token name, String value, int start) {
        super(name);
        setName(name.getText());
        setValue(new OptionValue.StringPair(name.getText(), value));
    }

    public UserPropertyNode() {
        super(NAME);
        setValue(new OptionValue.StringPair());        
    }

    @Override
    public StringBuilder print(StringBuilder builder) {
        StringBuilder sb = super.print(builder);
        OptionValue<Map.Entry<String, String>> val = getValue();
        if (val.isPresent()) {
            sb.append(SPACE).append(HYPHEN);
            Map.Entry<String, String> entry = val.getValue();
            sb.append(entry.getKey());
            if (entry.getValue() != null) {
                sb.append("=").append(entry.getValue());
            }
        }
        return sb;
    }    
}
