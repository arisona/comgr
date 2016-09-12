#version 330

uniform float aspect;

in vec2 position;
in vec4 color;

out vec4 vsColor;

void main(){
	vsColor = color;
    gl_Position = vec4(position.x, position.y * aspect, 0.0, 1.0);
}
