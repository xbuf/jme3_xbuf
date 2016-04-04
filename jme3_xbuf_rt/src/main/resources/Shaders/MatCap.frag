#import "Common/ShaderLib/GLSLCompat.glsllib"

uniform sampler2D m_DiffuseMap;

#ifdef NORMALMAP
    uniform sampler2D m_NormalMap;
#endif
#ifdef MULTIPLY_COLOR
    uniform vec4 m_Multiply_Color;
#endif
#ifdef CHESS_SIZE
    uniform float m_ChessSize;
#endif

varying vec3 vNormal;
varying vec2 texCoord;
varying vec3 vViewDir;
varying vec3 vPositionM;

vec3 diffuseColor;

vec2 matcap(vec3 eye, vec3 normal) {
  vec3 reflected = reflect(eye, normal);

  float m = 2.0 * sqrt(
    pow(reflected.x, 2.0) +
    pow(reflected.y, 2.0) +
    pow(reflected.z + 1.0, 2.0)
  );

  return reflected.xy / m + 0.5;
}

void main() {

    vec2 newTexCoord;
    newTexCoord = texCoord;

    #ifdef NORMALMAP
        vec3 normal = texture2D(m_NormalMap, newTexCoord).rgb;
        normal = normalize(normal);
    #else 
        vec3 normal = normalize(vNormal);
    #endif

    vec2 uv = matcap(normalize(-vViewDir), vNormal).xy;
    vec3 diffuseColor = texture2D(m_DiffuseMap, uv).rgb;

    #ifdef CHESS_SIZE
        //vec3 v = fract(vPositionM / vec3(m_ChessSize))+vec3(0.01);
        //vec3 p = smoothstep(vec3(0.495), vec3(0.505), v);
        vec3 v = cos((vPositionM * 2.0 * 3.14159) / vec3(m_ChessSize));
        vec3 p = smoothstep(vec3(-0.1), vec3(0.1), v);
        p = p * vec3(2.0) + vec3(-1.0);
        float coeff = 0.8 + 0.4 * smoothstep(-0.05, 0.05, p.x * p.y * p.z);
        diffuseColor.rgb *= coeff;
    #endif 
    #ifdef MULTIPLY_COLOR
        diffuseColor.rgb *= m_Multiply_Color.rgb;
    #endif

    gl_FragColor.rgb = diffuseColor;
    gl_FragColor.a = 1.0;
}
