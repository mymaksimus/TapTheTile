#version 150

in vec2 vIn;

uniform mat4 camera;
uniform mat4 model;

void main(){
	gl_Position = camera * model * vec4(vIn, 0, 1);
}

###

#version 150

uniform vec3 color;

out vec4 outColor;

void main(){
	outColor = vec4(color, 1);
}