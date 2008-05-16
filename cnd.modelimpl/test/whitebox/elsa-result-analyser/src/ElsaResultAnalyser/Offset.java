/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ElsaResultAnalyser;

/**
 *
 * @author nk220367
 */
public class Offset {

    public String file;
    public int line;
    public int row;
    
    public int elsaLine;

    public Offset(String loc, int elsaLine) {
        
        this.elsaLine = elsaLine;
        
        file = "";
        int i = 0;
        if(loc.charAt(i) != '/') {
            i = loc.indexOf(", at");
        }
        if(i < 0) {
            i = 0;
        }
        for (; i < loc.length() && loc.charAt(i) != '/'; i++) {
        }
        for (; i < loc.length() && loc.charAt(i)!= ':'; i++) {
            file += loc.charAt(i);
        }
        i++;
        line = 0;
        for (; i < loc.length() && Character.isDigit(loc.charAt(i)); i++) {
            line *= 10;
            line += loc.charAt(i) - '0';
        }
        i++;
        row = 0;
        for (; i < loc.length() && Character.isDigit(loc.charAt(i)); i++) {
            row *= 10;
            row += loc.charAt(i) - '0';
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        Offset o = (Offset) obj;
        return (file.equals(o.file) &&
                line == o.line &&
                row == o.row);
    }
}
