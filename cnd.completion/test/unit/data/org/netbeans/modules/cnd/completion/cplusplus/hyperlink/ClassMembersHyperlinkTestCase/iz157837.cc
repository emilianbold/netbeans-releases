namespace iz157837 {
    typedef struct _object {
        int value;
    } PyObject;

    typedef struct {
        PyObject *me_value;
        PyFrameObject * (*me_lookup)(PyDictObject *mp, PyObject *key, long hash);
    } PyDictEntry;

    typedef struct {
        PyObject *mp_value;
    } PyFrameObject;

    typedef struct _dictobject PyDictObject;

    struct _dictobject {
        PyDictEntry * (*ma_lookup)(PyDictObject *mp, PyObject *key, long hash);
    };

    void deref_fun_type_field() {
        PyDictObject* d;
        d->ma_lookup(0, 0, 0)->me_value;
        d->ma_lookup(0, 0, 0)->me_value->value;
        d->ma_lookup(0, 0, 0)->me_lookup(0, 0, 0)->mp_value;
    }
}
