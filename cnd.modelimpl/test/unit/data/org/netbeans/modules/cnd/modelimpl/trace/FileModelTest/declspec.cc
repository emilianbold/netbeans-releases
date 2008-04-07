#define Q_DECL_IMPORT __declspec(dllimport)
#define Q_GUI_EXPORT Q_DECL_IMPORT

class Q_GUI_EXPORT A {
public:
   friend Q_GUI_EXPORT A* getA();
};
