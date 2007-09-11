package data;


public class Bool2FaceConverter extends org.jdesktop.beansbinding.Converter {
    private static String TRUE_FACE = ":)";
    private static String FALSE_FACE = ":(";
    
    public Object convertForward(Object arg) {
        return ((Boolean) arg) ? TRUE_FACE : FALSE_FACE; 
    }

    public Object convertReverse(Object arg) {
        return ((String) arg).equals(TRUE_FACE);
    }
}