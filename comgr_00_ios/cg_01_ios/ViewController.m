//
//  ViewController.m
//  cg_01_ios
//
//  Created by Simon Schubiger on 01/07/14.
//  Copyright (c) 2014 FHNW. All rights reserved.
//

#import "ViewController.h"

static const GLfloat TRIANGLE[] = {
    0.0, 0.5,
    0.5,  -0.5,
    -0.5, -0.5,
};

@interface ViewController ()

@property (strong, nonatomic) EAGLContext*       context;
@property (strong, nonatomic) NSMutableIndexSet* shaders;
@property (nonatomic)         GLint              material;
@property (nonatomic)         GLuint             VBO;
@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.context = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];
    
    GLKView* view                 = (GLKView *)self.view;
    view.context                  = self.context;
    view.drawableDepthFormat      = GLKViewDrawableDepthFormat24;
    
    [self initialize];
}

- (void)initialize {
    [EAGLContext setCurrentContext:self.context];
    
    self.shaders = [NSMutableIndexSet indexSet];
    [self.shaders addIndex:[self createShader:GL_VERTEX_SHADER   filename:@"simple_vs"]];
    [self.shaders addIndex:[self createShader:GL_FRAGMENT_SHADER filename:@"simple_fs"]];
    
    self.material = [self createProgram:self.shaders];
    
    glGenBuffers(1, &_VBO);
    glBindBuffer(GL_ARRAY_BUFFER, self.VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(GLfloat) * 6, TRIANGLE, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
}

- (void)dealloc {
    [EAGLContext setCurrentContext:self.context];
    
    glDeleteBuffers(1, &_VBO);
    
    for(GLuint shader = [self.shaders firstIndex]; shader != NSNotFound; shader = [self.shaders indexGreaterThanIndex:shader])
        glDeleteShader(shader);
    
    glDeleteProgram(self.material);
    
    if ([EAGLContext currentContext] == self.context)
        [EAGLContext setCurrentContext:nil];
}

- (void)glkView:(GLKView *)view drawInRect:(CGRect)rect {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    
    glUseProgram(self.material);
    
    glEnableVertexAttribArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, self.VBO);
    glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
    glDrawArrays(GL_TRIANGLES, 0, 3);
    
    glDisableVertexAttribArray(0);
    glUseProgram(0);
}

-(GLuint)createShader:(GLenum) type  filename:(NSString*)filename {
    const GLchar* content = (GLchar *)[[NSString stringWithContentsOfFile:[[NSBundle mainBundle] pathForResource:filename ofType:@"glsl"] encoding:NSUTF8StringEncoding error:nil] UTF8String];
    GLuint  result  = glCreateShader(type);
    GLint   length  = strlen(content);
    glShaderSource(result, 1, &content, &length);
    glCompileShader(result);
    
    [self checkStatus:result status:GL_COMPILE_STATUS];
    return result;
}

-(GLuint)createProgram:(NSIndexSet*) shaders {
    GLuint result = glCreateProgram();
    
    for(GLuint shader = [shaders firstIndex]; shader != NSNotFound; shader = [shaders indexGreaterThanIndex:shader])
        glAttachShader(result, shader);
    
    glLinkProgram(result);
    
    [self checkStatus:result status:GL_LINK_STATUS];
    glValidateProgram(result);
    
    return result;
}

-(void) checkStatus:(GLuint) object status:(GLenum)status {
    GLint param = 0;
    
    if(status == GL_COMPILE_STATUS)
        glGetShaderiv(object, status, &param);
    else if(status == GL_LINK_STATUS)
        glGetProgramiv(object, status, &param);
    
    if (param != 1) {
        NSLog(@"status: %d", param);
        glGetShaderiv(object, GL_INFO_LOG_LENGTH, &param);
        GLchar* infoLog = malloc(param);
        if(status == GL_COMPILE_STATUS)
            glGetShaderInfoLog(object, param, &param, infoLog);
        else if(status == GL_LINK_STATUS)
            glGetProgramInfoLog(object, param, &param, infoLog);
        NSLog(@"%s", infoLog);
        free(infoLog);
    }
}

@end
