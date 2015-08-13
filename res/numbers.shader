#version 150

in vec2 vIn;

uniform mat4 camera;
uniform mat4 model;

out vec2 fIn;

void main(){
	fIn = vIn;
	gl_Position = camera * model * vec4(vIn, 0, 1);
}

###

#version 150

in vec2 fIn;

uniform vec3 color;
uniform sampler2D texture;
uniform float number;

out vec4 outColor;

void main(){
	float singleNumberWidth = 1.0 / 10.0;
	vec2 texCoord = vec2(number * singleNumberWidth + fIn.x * singleNumberWidth, -fIn.y);
	vec4 color = texture2D(texture, texCoord);
	color += vec4(1, 0, 0, 0);
	outColor = color;
}