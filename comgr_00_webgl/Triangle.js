var TRIANGLE = [
                0.0, 0.5,
                0.5,  -0.5,
                -0.5, -0.5,
                ];
var VBO;
var shaders = [];
var program;

function init() {
	var canvas = document.getElementById('glcanvas');
	gl = canvas.getContext('webgl');
	
	
	shaders.push(createShader(gl, gl.VERTEX_SHADER,   "simple_vs"));
	shaders.push(createShader(gl, gl.FRAGMENT_SHADER, "simple_fs"));
	program = createProgram(gl, shaders);

	VBO = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, VBO);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(TRIANGLE), gl.STATIC_DRAW);
	gl.bindBuffer(gl.ARRAY_BUFFER, null);
	
	display(gl);
}

function display(gl) {
	gl.clearColor(0.0, 0.0, 0.0, 1.0);
	gl.clear(gl.COLOR_BUFFER_BIT);

	gl.useProgram(program);

	gl.enableVertexAttribArray(0);
	gl.bindBuffer(gl.ARRAY_BUFFER, VBO);
	gl.vertexAttribPointer(0, 2, gl.FLOAT, false, 0, 0);

	gl.drawArrays(gl.TRIANGLES, 0, 3);

	gl.disableVertexAttribArray(0);
	gl.useProgram(null);
}

function createShader(gl, type, shader) {
	var result = gl.createShader(type);
	gl.shaderSource(result, document.getElementById(shader).text);
	gl.compileShader(result);
	checkStatus(gl, result, gl.COMPILE_STATUS);
	return result;
}

function createProgram(gl, shaders) {
	var result = gl.createProgram();

	for(var i = 0; i < shaders.length; i++)
		gl.attachShader(result, shaders[i]);

	gl.linkProgram(result);
	checkStatus(gl, result, gl.LINK_STATUS);
	gl.validateProgram(result);

	return result;
}

function checkStatus(gl, object, status) {
	switch(status) {
	case gl.COMPILE_STAUTS:
		if (!gl.getShaderParameter(object, status))
			console.log(gl.getShaderInfoLog(vertShader));
		break;
	case gl.LINK_STATUS:
		if (!gl.getProgramParameter(object, status))
			console.log(gl.getProgramInfoLog(vertShader));
		break;
	}
}

