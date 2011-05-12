struct ostream;
template <class T> class Dizionario;
template <class T> ostream& operator<<(ostream& os, const Dizionario<T>& d);

template <class T> class Dizionario {
    /*template <int i>*/ friend ostream& operator<< <T>(ostream& os, const Dizionario<T>& d);
public:
    Dizionario(): number(3) {}
private:
    int number;
};

template <class T>
ostream& operator<<(ostream& os, const Dizionario<T>& d) {
    return os << d.number;
}