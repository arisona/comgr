#version 330

uniform float aspect;

in vec2 position;
in vec2 texCoord;

out vec2 vsTexCoord;

void main(){
	vsTexCoord = texCoord;
    gl_Position = vec4(position.x, position.y * aspect, 0.0, 1.0);
}
