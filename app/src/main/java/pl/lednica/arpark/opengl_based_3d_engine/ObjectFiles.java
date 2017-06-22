package pl.lednica.arpark.opengl_based_3d_engine;

/**
 * Created by Maciej on 2016-10-18.
 */

public class ObjectFiles {
    public String file_v;
    public String file_n;
    public String file_t;
    public String file_i;
    public String file_c;

    public String file_texture_image;

    public ObjectFiles(String file_v, String file_n, String file_t, String file_i,String file_texture_image) {
        this.file_v = file_v;
        this.file_n = file_n;
        this.file_t = file_t;
        this.file_i = file_i;
        this.file_texture_image = file_texture_image;
    }

    public ObjectFiles(String file_v, String file_n, String file_i, String file_c) {
        this.file_v = file_v;
        this.file_n = file_n;
        this.file_i = file_i;
        this.file_c = file_c;
        this.file_t=null;
        file_texture_image=null;
    }
    public ObjectFiles(String file_c) {
        this.file_v = null;
        this.file_n = null;
        this.file_i = null;
        this.file_c = file_c;
        this.file_t=null;
        file_texture_image=null;
    }
}
