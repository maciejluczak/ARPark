package pl.lednica.arpark.opengl_based_3d_engine;

/**
 * Created by Maciej on 2016-10-12.
 * Vertex i fragment shader stworzone dla światła Diffuse lighting z punktowym źródłem
 * z oświetleniem obliczanym per pixel wykorzystujace tekstury
 */

public abstract class SimpleLightTextureShaders {
    public static final String DEFFUSE_POINT_LIGHT_VERTEX_SHADER = " \n" + "\n"
            + "uniform mat4 u_MVPMatrix; \n" // A constant representing the combined model/view/projection matrix.
            + "uniform mat4 u_MVMatrix; \n"       // A constant representing the combined model/view matrix.
            + "attribute vec4 a_Position; \n"     // Per-vertex position information we will pass in.
            + "attribute vec3 a_Normal; \n"       // Per-vertex normal information we will pass in.
            + "attribute vec2 a_TexCoordinate; \n" // Per-vertex texture coordinate information we will pass in.
            + "varying vec3 v_Position; \n"       // This will be passed into the fragment shader.
            + "varying vec3 v_Normal; \n"         // This will be passed into the fragment shader.
            + "varying vec2 v_TexCoordinate; \n"   // This will be passed into the fragment shader.
            + "void main() \n" + "{ \n"
            // Transform the vertex into eye space.
            + "v_Position = vec3(u_MVMatrix * a_Position); \n"
            + "v_TexCoordinate = a_TexCoordinate; \n"
            // Transform the normal's orientation into eye space.
            + "v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0)); \n"
            // gl_Position is a special variable used to store the final position.
            // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
            + "gl_Position = u_MVPMatrix * a_Position; \n"
            + "} \n";

    public static final String DEFFUSE_POINT_LIGHT_FRAGMENT_SHADER = " \n"
            + "precision mediump float; \n"      // Set the default precision to medium. We don't need as high of a
            // precision in the fragment shader.
            + "uniform vec3 u_LightPos; \n"      // The position of the light in eye space.
            + "uniform sampler2D u_Texture; \n"
            + "varying vec3 v_Position; \n"      // Interpolated position for this fragment.
            // triangle per fragment.
            + "varying vec3 v_Normal; \n"         // Interpolated normal for this fragment.
            + "varying vec2 v_TexCoordinate; \n"
            // The entry point for our fragment shader.
            + "void main() \n"
            + "{ \n"
            // Will be used for attenuation.
            + "float distance = length(u_LightPos - v_Position); \n"
            // Get a lighting direction vector from the light to the vertex.
            + "vec3 lightVector = normalize(u_LightPos - v_Position); \n"
            // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
            // pointing in the same direction then it will get max illumination.
            + "float diffuse = max(dot(v_Normal, lightVector), 0.1); \n"
            // Add attenuation.
            + "diffuse = diffuse * (1.0 / (1.0 + (0.1 * distance))); \n"
            + "diffuse = diffuse + 0.3; \n"
            // Multiply the color by the diffuse illumination level to get final output color.
            + "gl_FragColor = diffuse * texture2D(u_Texture, v_TexCoordinate); \n"
            + "} \n";
}
