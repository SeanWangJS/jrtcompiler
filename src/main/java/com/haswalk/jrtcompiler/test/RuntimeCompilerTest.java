package com.haswalk.jrtcompiler.test;

import com.haswalk.jrtcompiler.RuntimeCompiler;

public class RuntimeCompilerTest {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        String code =
                        "package com.haswalk.jrtcompiler.test;          \n" +
                        "import com.haswalk.jrtcompiler.test.B;      \n" +
                        "public class A extends B{                 \n" +
                        "  public void run(){                      \n" +
                        "    System.out.println(\"hello class A\");\n" +
                        "  }                                       \n" +
                        "}                                         ";

        Class<?> clazz = RuntimeCompiler.compile(code, "com.haswalk.jrtcompiler.test.A");
        B o = (B)clazz.newInstance();
        o.run();

    }
}
