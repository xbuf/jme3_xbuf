#import "Common/ShaderLib/Skinning.glsllib"

uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat3 g_NormalMatrix;

attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec3 inNormal;
attribute vec3 inTangent;

varying vec3 vNormal;
varying vec2 texCoord;

varying vec3 vPositionM;
varying vec3 vViewDir;

void main() {

   vec4 modelSpacePos = vec4(inPosition, 1.0);
   vec3 modelSpaceNorm = inNormal;
   vec3 modelSpaceTan  = inTangent.xyz;

    #ifdef NUM_BONES
      Skinning_Compute(modelSpacePos, modelSpaceNorm, modelSpaceTan);
    #endif

    gl_Position = g_WorldViewProjectionMatrix * modelSpacePos;
    texCoord = inTexCoord;

    vec3 vPosition = (g_WorldViewMatrix * modelSpacePos).xyz;
    vNormal = normalize(g_NormalMatrix * modelSpaceNorm);
    vViewDir = normalize(-vPosition);
    vPositionM = inPosition;
}