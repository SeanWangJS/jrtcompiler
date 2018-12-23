package com.haswalk.jrtcompiler;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RuntimeCompiler {

    public static Class<?> compile(String source, String className) {

        HashMap<String, byte[]> bytes = new HashMap<>();

        // 自定义即将编译的 java 文件对象，默认情况下，该文件对象应该从硬盘中读取，但是在这里我们只需要直接返回源码字符串即可
        SimpleJavaFileObject simpleJavaFileObject = new SimpleJavaFileObject(
                URI.create("string:///" + className.replace(".", "/") + JavaFileObject.Kind.SOURCE.extension),
                JavaFileObject.Kind.SOURCE){
            public CharSequence getCharContent(boolean b) {
                return source;
            }
        };

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // 自定义 java 文件管理器，默认情况下，java 文件管理器会将编译后的字节码写入硬盘中的文件，但在这里我们将它拦截并输出到bytes 哈希表中
        JavaFileManager jfm = new ForwardingJavaFileManager(fileManager) {
            public JavaFileObject getJavaFileForOutput(Location location,
                                                       String className,
                                                       JavaFileObject.Kind kind,
                                                       FileObject sibling) throws IOException {
                if(kind == JavaFileObject.Kind.CLASS) {
                    return new SimpleJavaFileObject(URI.create("string:///" + className.replace(".", "/") + JavaFileObject.Kind.CLASS.extension), JavaFileObject.Kind.CLASS) {
                        public OutputStream openOutputStream() {
                            return new FilterOutputStream(new ByteArrayOutputStream()) {
                                public void close() throws IOException {
                                    out.close();
                                    ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                                    bytes.put(className, bos.toByteArray());
                                }
                            };
                        }
                    };
                }else{
                    return super.getJavaFileForOutput(location, className.replace(".", "/"), kind, sibling);
                }
            }
        };

        String path = RuntimeCompiler.class.getResource("").toString();
        String classpath = path.split("/target/")[0] + "/target/classes";
        List<String> options = Arrays.asList("-classpath", System.getProperty("java.class.path") + ";" + classpath.replace("file:/", ""));

        JavaCompiler.CompilationTask task = compiler.getTask(null, jfm, null, options, null, Collections.singletonList(simpleJavaFileObject));
        Boolean call = task.call();
        if(!call) {
            System.out.println("compile failed");
            System.exit(0);
        }

        // 自定义类加载器负责加载刚刚编译成功的字节码
        ClassLoader classLoader = new ClassLoader() {
            protected Class<?> findClass(String name) {
                byte[] byteCode = bytes.get(name);
                if(byteCode == null) {
                    try {
                        return RuntimeCompiler.class.getClassLoader().loadClass(name);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return defineClass(name, byteCode, 0, byteCode.length);
            }
        };

        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
