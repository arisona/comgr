#version 330

uniform float aspect;

in vec2 position;

void main(){
    gl_Position = vec4(position.x, position.y * aspect, 0.0, 1.0);
}