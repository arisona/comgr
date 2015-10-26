#version 330

uniform sampler2D colorMap;

in vec2 vsTexCoord;

out vec4 fragColor;

void main() {
    fragColor = vec4(1, 1, 1, 1); //texture(colorMap, vsTexCoord);
}
