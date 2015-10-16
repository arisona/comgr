package ch.fhnw.i4ds.comgr;

import org.lwjgl.Sys;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class HelloLWJGL {
	private GLFWErrorCallback errorCallback;
	private GLFWKeyCallback keyCallback;

	private long window;

	public static void main(String[] args) {
		new HelloLWJGL().run();
	}

	private void run() {
		System.out.println("Hello LWJGL " + Sys.getVersion() + "!");

		try {
			GLFW.glfwSetErrorCallback(errorCallback = Callbacks.errorCallbackPrint(System.err));

			if (GLFW.glfwInit() != GL11.GL_TRUE)
				throw new IllegalStateException("Unable to initialize GLFW");

			init();
			loop();

			GLFW.glfwDestroyWindow(window);
			keyCallback.release();
		} finally {
			GLFW.glfwTerminate();
			errorCallback.release();
		}
	}

	private void init() {
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);

		int width = 300;
		int height = 300;

		window = GLFW.glfwCreateWindow(width, height, "Hello World!", MemoryUtil.NULL, MemoryUtil.NULL);
		if (window == MemoryUtil.NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		GLFW.glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
					GLFW.glfwSetWindowShouldClose(window, GL11.GL_TRUE);
			}
		});

		GLFWvidmode vidmode = new GLFWvidmode(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()));
		GLFW.glfwSetWindowPos(window, (vidmode.getWidth() - width) / 2, (vidmode.getHeight() - height) / 2);

		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);

		GLFW.glfwShowWindow(window);

		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
	}

	private void loop() {

		GL11.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

		while (GLFW.glfwWindowShouldClose(window) == GL11.GL_FALSE) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			GLFW.glfwSwapBuffers(window);

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			GLFW.glfwPollEvents();
		}
	}
}
