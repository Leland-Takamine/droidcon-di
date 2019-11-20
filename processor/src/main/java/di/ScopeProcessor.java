package di;

import com.squareup.javapoet.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScopeProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeElement scopeAnnotation = processingEnv.getElementUtils().getTypeElement("di.Scope");
        ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(scopeAnnotation)).forEach(scope -> {
            JavaFile javaFile = createScopeImpl(scope);
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    private JavaFile createScopeImpl(TypeElement scope) {
        String packageName = processingEnv.getElementUtils().getPackageOf(scope).toString();
        String scopeImplClassName = scope.getSimpleName() + "Impl";
        return JavaFile.builder(packageName, TypeSpec.classBuilder(scopeImplClassName)
                .superclass(ClassName.get(scope))
                .addFields(createFields(scope))
                .addMethods(overrideAccessMethods(scope))
                .addMethods(createProviderMethods(scope))
                .build()).build();
    }

    private Iterable<FieldSpec> createFields(TypeElement scope) {
        return getFactoryMethods(scope)
                .map(factoryMethod -> {
                    TypeMirror returnType = factoryMethod.getReturnType();
                    return FieldSpec.builder(TypeName.get(returnType), name(returnType), Modifier.PRIVATE).build();
                })
                .collect(Collectors.toList());
    }

    private Iterable<MethodSpec> createProviderMethods(TypeElement scope) {
        return getFactoryMethods(scope)
                .map(factoryMethod -> {
                    String name = name(factoryMethod.getReturnType());
                    return MethodSpec.methodBuilder(name)
                            .returns(TypeName.get(factoryMethod.getReturnType()))
                            .beginControlFlow("if ($L == null)", name)
                            .addStatement("$L = $L($L)", name, factoryMethod.getSimpleName(),
                                    factoryMethod.getParameters().stream().map(parameter ->
                                            name(parameter.asType()) + "()").collect(Collectors.joining(", ")))
                            .endControlFlow()
                            .addStatement("return $L", name)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Iterable<MethodSpec> overrideAccessMethods(TypeElement scope) {
        return getScopeMethods(scope)
                .filter(scopeMethod -> scopeMethod.getModifiers().contains(Modifier.ABSTRACT))
                .map(accessMethod -> MethodSpec.overriding(accessMethod)
                        .addStatement("return $L()", name(accessMethod.getReturnType()))
                        .build())
                .collect(Collectors.toList());
    }

    private Stream<ExecutableElement> getFactoryMethods(TypeElement scope) {
        return getScopeMethods(scope)
                .filter(scopeMethod -> !scopeMethod.getModifiers().contains(Modifier.ABSTRACT));
    }

    private Stream<ExecutableElement> getScopeMethods(TypeElement scope) {
        return ElementFilter.methodsIn(scope.getEnclosedElements()).stream();
    }

    private String name(TypeMirror typeMirror) {
        String simpleName = typeMirror.toString().replaceAll(".*\\.", "");
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("di.Scope");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
