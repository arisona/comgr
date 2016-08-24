package ch.fhnw.i4ds.comgr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.lwjgl.opengl.GL20;

public class GLSLHelpers {
	public static int createShader(Class<?> cls, int shaderType, String filename) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(cls.getResourceAsStream(filename + ".glsl")));
		String content = "";
		String line;
		while ((line = in.readLine()) != null)
			content += line + "\n";
		in.close();

		int shader = GL20.glCreateShader(shaderType);

		
		
		GL20.glShaderSource(shader, content);
		GL20.glCompileShader(shader);

		checkStatus(shader, GL20.GL_COMPILE_STATUS);
		return shader;
	}

	public static int createProgram(List<Integer> shaders) {
		int program = GL20.glCreateProgram();

		for (int shader : shaders)
			GL20.glAttachShader(program, shader);

		GL20.glLinkProgram(program);
		checkStatus(program, GL20.GL_LINK_STATUS);

		GL20.glValidateProgram(program);
		checkStatus(program, GL20.GL_VALIDATE_STATUS);

		return program;
	}

	public static void checkStatus(int object, int statusType) {
		int status = 0;

		if (statusType == GL20.GL_COMPILE_STATUS)
			status = GL20.glGetShaderi(object, statusType);
		else if (statusType == GL20.GL_LINK_STATUS || statusType == GL20.GL_VALIDATE_STATUS)
			status = GL20.glGetProgrami(object, statusType);

		if (status != 1) {
			System.err.println("status: " + status);
			status = GL20.glGetShaderi(object, GL20.GL_INFO_LOG_LENGTH);
			String infoLog = "";
			if (statusType == GL20.GL_COMPILE_STATUS)
				infoLog = GL20.glGetShaderInfoLog(object);
			//else if (statusType == GL20.GL_LINK_STATUS)
				infoLog = GL20.glGetProgramInfoLog(object);
			System.err.println(infoLog);
			System.exit(-1);
		}
	}
}
