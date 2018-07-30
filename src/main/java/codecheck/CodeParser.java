package codecheck;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class CodeParser {

    private String module;
    private Class aClass;
    private String src = "src/main/java/";

    String code;
    CompilationUnit compilationUnit;

    public CodeParser(String module, Class aClass) {
        this.module = module;
        this.aClass = aClass;
    }

    public CodeParser src(String src) {
        this.src = src;
        return this;
    }

    public String loadCode() throws IOException {
        if (code != null) {
            return code;
        }
        byte[] encoded = Files.readAllBytes(Paths.get(src + "/" +
                aClass.getName().replace(".", "/") + ".java"));
        return code = new String(encoded, "UTF-8");
    }

    public CompilationUnit getCompilationUnit() throws IOException {
        if (compilationUnit != null) {
            return compilationUnit;
        }
        return compilationUnit = JavaParser.parse(loadCode());
    }

    public ClassOrInterfaceDeclaration getClassDeclaration() throws IOException {
        Optional<ClassOrInterfaceDeclaration> cd = getCompilationUnit().getClassByName(aClass.getSimpleName());
        return cd.orElseThrow(() -> new AssertionError("Class not found: " + aClass.getName()));
    }

    public MethodDeclaration findSingleMethod(String methodName) throws IOException {
        List<MethodDeclaration> methods = getClassDeclaration().getMethodsByName(methodName);
        if (methods.size() != 1) {
            throw new AssertionError("Wrong number of methods: " + methodName + " - " + methods.size());
        }
        return methods.get(0);
    }

    public FieldDeclaration findField(String fieldName) throws IOException {
        Optional<FieldDeclaration> fieldByName = getClassDeclaration().getFieldByName(fieldName);
        if (!fieldByName.isPresent()) {
            throw new AssertionError("Field not found: " + fieldName);
        }
        return fieldByName.get();
    }
}
