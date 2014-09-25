package ch.fhnw.i4ds.cg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.media.opengl.GL3;

public class GLSLHelpers {
	public static int createShader(GL3 gl3, Class<?> cls, int shaderType,
			String filename) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				cls.getResourceAsStream(filename + ".glsl")));
		String content = "";
		String line;
		while ((line = in.readLine()) != null)
			content += line + "\n";
		in.close();

		int shader = gl3.glCreateShader(shaderType);

		gl3.glShaderSource(shader, 1, new String[] { content },
				new int[] { content.length() }, 0);
		gl3.glCompileShader(shader);

		checkStatus(gl3, shader, GL3.GL_COMPILE_STATUS);
		return shader;
	}

	public static int createProgram(GL3 gl3, List<Integer> shaders) {
		int program = gl3.glCreateProgram();

		for (int shader : shaders)
			gl3.glAttachShader(program, shader);

		gl3.glLinkProgram(program);
		checkStatus(gl3, program, GL3.GL_LINK_STATUS);

		gl3.glValidateProgram(program);
		checkStatus(gl3, program, GL3.GL_VALIDATE_STATUS);

		return program;
	}

	public static void checkStatus(GL3 gl3, int object, int statusType) {
		int[] status = { 0 };

		if (statusType == GL3.GL_COMPILE_STATUS)
			gl3.glGetShaderiv(object, statusType, status, 0);
		else if (statusType == GL3.GL_LINK_STATUS || statusType == GL3.GL_VALIDATE_STATUS)
			gl3.glGetProgramiv(object, statusType, status, 0);

		if (status[0] != 1) {
			System.err.println("status: " + status[0]);
			gl3.glGetShaderiv(object, GL3.GL_INFO_LOG_LENGTH, status, 0);
			byte[] infoLog = new byte[status[0]];
			if (statusType == GL3.GL_COMPILE_STATUS)
				gl3.glGetShaderInfoLog(object, status[0], status, 0, infoLog, 0);
			else if (statusType == GL3.GL_LINK_STATUS)
				gl3.glGetProgramInfoLog(object, status[0], status, 0, infoLog, 0);
			System.err.println(new String(infoLog));
			System.exit(-1);
		}
	}
}
