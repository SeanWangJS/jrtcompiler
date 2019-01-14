# jrtcompiler

Java Runtime Compiler

###useage

```java
String code =
            "package com.pkg;                           \n" +
            "public class A{                            \n" +
            "  public void run(){                       \n" +
            "    System.out.println(\"hello class A\"); \n" +
            "  }                                        \n" +
            "}                                          ";
Class<?> clazz = RuntimeCompiler.compile(code, "com.pkg.A");
Object o = clazz.newInstance();
clazz.getDeclaredMethod("run").invoke(o);
```

or if A is subclass of B

```java
package com.pkg2;
public abstract class B {
    public abstract void run();
}
```

then 

```java
String code =
            "package com.pkg;                                           \n" +
            "import com.pkg2.B;                                         \n" +
            "public class A extends B{                                  \n" +
            "  public void run(){                                       \n" +
            "    System.out.println(\"hello class A subclass of B\");   \n" +
            "  }                                                        \n" +
            "}                                                            ";

Class<?> clazz = RuntimeCompiler.compile(code, "com.pkg.A");
B a = (B)clazz.newInstance();
a.run();
```
