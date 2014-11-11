#include <OpenGL/gl3.h>
#define __gl_h_
#include <GLUT/glut.h>
#include <stdlib.h>
#include <vector>
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>


static const GLfloat TRIANGLE[] = {
    0.0,   0.5,
    0.5,  -0.5,
    -0.5, -0.5,
};

class Triangle {
private:
    std::vector<GLuint> shaders;
	GLuint              program;
	GLuint              VBO;
	GLuint              VAO;
    
public:
	Triangle() {
        shaders.push_back(createShader(GL_VERTEX_SHADER,   "simple_vs"));
        shaders.push_back(createShader(GL_FRAGMENT_SHADER, "simple_fs"));
        
        program = createProgram(shaders);

        glGenVertexArrays(1, &VAO);
        glBindVertexArray(VAO);
        
        glGenBuffers(1, &VBO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, 6 * sizeof(GLfloat), TRIANGLE, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

        glBindVertexArray(0);
	}
    
	~Triangle() {
        glDeleteVertexArrays(1, &VAO);
		glDeleteBuffers(1, &VBO);
        
        for (auto shader : shaders)
			glDeleteShader(shader);
        
		glDeleteProgram(program);
	}
    
	void display() const {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(program);
        glBindVertexArray(VAO);
		glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
        glUseProgram(0);
	}
    
	void reshape(int x, int y, int w, int h) const {
		glViewport(x, y, w, h);
	}
    
private:
	GLuint createShader(GLenum type, std::string filename) const {
        filename += ".glsl";
        std::ifstream file(filename);
        if (!file.is_open()) {
            std::cout << "Unable to open file " << filename << std::endl;
            exit(1);
        }
        std::stringstream fileData;
        fileData << file.rdbuf();
        file.close();
        
        const GLchar* content       = fileData.str().c_str();
        GLint         contentLength = (GLint)strlen(content);
        
		int result = glCreateShader(type);
        
		glShaderSource(result, 1, &content, &contentLength);
		glCompileShader(result);
        
        checkStatus(result, GL_COMPILE_STATUS);
		return result;
	}
    
	GLuint createProgram(const std::vector<GLuint>& shaders) const {
		GLuint result = glCreateProgram();
        
        for (auto shader : shaders)
			glAttachShader(result, shader);
        
		glLinkProgram(result);
		checkStatus(result, GL_LINK_STATUS);
		glValidateProgram(result);
        
		return result;
	}
    
	void checkStatus(GLuint object, GLenum status) const {
		GLint params;
        
		if (status == GL_COMPILE_STATUS)
			glGetShaderiv(object, status, &params);
		else if (status == GL_LINK_STATUS)
			glGetProgramiv(object, status, &params);
        
		if (params != 1) {
            std::cout << "status: " << params << std::endl;
			glGetShaderiv(object, GL_INFO_LOG_LENGTH, &params);
            GLchar* infoLog = new GLchar[params];
			if(status == GL_COMPILE_STATUS)
				glGetShaderInfoLog(object, params, &params, infoLog);
			else if(status == GL_LINK_STATUS)
				glGetProgramInfoLog(object, params, &params, infoLog);
            std::cout << infoLog << std::endl;
            delete[] infoLog;
			exit(1);
		}
	}
};

static Triangle* triangle = nullptr;

void handleKeypress(unsigned char key, int x, int y) {
    delete triangle;
    exit(0);
}

void handleResize(int w, int h) {
    triangle->reshape(0, 0, w, h);
}

void handleDraw() {
    triangle->display();
    glutSwapBuffers();
}

int main(int argc, char** argv) {
    glutInit(&argc, argv);
    glutInitWindowSize(800, 800);
    glutInitDisplayMode(GLUT_3_2_CORE_PROFILE | GLUT_RGB);
    glutCreateWindow("CG_01 Triangle");
    
    glutDisplayFunc(handleDraw);
    glutKeyboardFunc(handleKeypress);
    glutReshapeFunc(handleResize);
    
    triangle = new Triangle();
    
    glutMainLoop();
    return 0;
}
