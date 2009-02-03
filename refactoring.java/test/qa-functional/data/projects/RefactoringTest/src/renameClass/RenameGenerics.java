package renameClass;

public class RenameGenerics<T> {

    public T getT(T a) {
        return a;
    }

    public <X extends T> X getX() {
        return null;
    }

}
