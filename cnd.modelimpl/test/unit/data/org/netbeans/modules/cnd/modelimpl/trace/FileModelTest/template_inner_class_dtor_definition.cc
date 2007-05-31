template<class T> class Outer {
	struct Inner {
		~Inner();
		void foo();
	};

};

template<class T> Outer<T>::Inner::~Inner() {
}

template<class T> void Outer<T>::Inner::foo() {
}
