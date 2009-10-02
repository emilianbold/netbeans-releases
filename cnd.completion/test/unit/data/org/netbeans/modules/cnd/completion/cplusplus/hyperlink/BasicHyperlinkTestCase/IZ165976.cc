typedef float iz165976_SNGL_VECT[3];
typedef struct iz165976_photon_struct iz165976_PHOTON;
struct iz165976_photon_struct {
  iz165976_SNGL_VECT Loc;          /* location */
};
typedef iz165976_PHOTON *iz165976_PHOTON_BLOCK;
struct iz165976_photon_map_struct {
  /* these 3 are render-thread safe - NOT pre-process thread safe */
  iz165976_PHOTON_BLOCK *head;   /* the photon map - array of blocks of photons */
};
int iz165976_main() {
    typedef struct iz165976_photon_map_struct PHOTON_MAP;
    PHOTON_MAP *map;
    int j = 0;
    (map->head [( j )>> 14 ][( j ) & ( (16384) -1) ]).Loc;
    return 0;
}