package com.github.kayjam.executor;

import com.github.kayjam.executor.exceptions.KayJamNotFoundException;
import com.github.kayjam.executor.exceptions.KayJamRuntimeException;
import com.github.kayjam.executor.executors.*;
import com.github.kayjamlang.core.Expression;
import com.github.kayjamlang.core.containers.ClassContainer;
import com.github.kayjamlang.core.containers.Container;
import com.github.kayjamlang.core.containers.Function;
import com.github.kayjamlang.core.containers.ObjectContainer;
import com.github.kayjamlang.core.expressions.*;
import com.github.kayjamlang.core.provider.MainContext;
import com.github.kayjamlang.core.provider.MainExpressionProvider;

import java.util.Map;

public class Executor extends MainExpressionProvider<Object> {

    public Executor() {
        super(null);

        //Containers
        addCompiler(Container.class, new ContainerExecutor());
        addCompiler(ObjectContainer.class, new ObjectExecutor());
        addCompiler(ClassContainer.class, new ClassContainerExecutor());

        //Variables
        addCompiler(Variable.class, new VariableExecutor());
        addCompiler(VariableLink.class, new VariableLinkExecutor());
        addCompiler(VariableSet.class, new VariableSetExecutor());

        //Other
        addCompiler(Access.class, new AccessExecutor());
        addCompiler(Const.class, new ConstExecutor());
        addCompiler(Return.class, new ReturnExecutor());
        addCompiler(If.class, new IfExecutor());
        addCompiler(CallCreate.class, new CallCreateExecutor());
        addCompiler(OperationExpression.class, new OperationExpressionExecutor());
    }

    public Object execute(Container container) throws Exception {
        MainContext context = new MainContext(container, null);
        for(Expression expression: container.children) {
            if (expression instanceof ClassContainer) {
                ClassContainer classContainer = (ClassContainer) expression;
                if (context.classes.containsKey(classContainer.name))
                    throw new KayJamRuntimeException(classContainer,
                            "A class with the same name has already been declared");

                context.classes.put(classContainer.name, classContainer);
            }
        }

        for(Map.Entry<String, ClassContainer> entry: context.classes.entrySet()) {
            ClassContainer classContainer = entry.getValue();

            for(String string: classContainer.implementsClass) {
                if (!context.classes.containsKey(string))
                    throw new KayJamNotFoundException(classContainer,
                            "class interface", string);

                ClassContainer implementsClass = context.classes.get(string);
                for(Function function: implementsClass.functions){
                    Function fun = findFunction(classContainer, function);
                    if(!fun.returnType.name.equals(function.returnType.name))
                        throw new KayJamRuntimeException(fun, "Invalid return type");
                }

                classContainer.children.addAll(implementsClass.children);
            }
        }


        return provide(container,
            context, context);
    }

    private Function findFunction(ClassContainer classContainer, Function function) throws KayJamRuntimeException {
        for(Function fun: classContainer.functions){
            if(fun.arguments.size()==function.arguments.size()&&
                fun.name.equals(function.name)){

                boolean args = true;
                for (int i = 0; i < fun.arguments.size(); i++) {
                    if (!fun.arguments.get(i)
                            .type.name.equals(function.arguments.get(i).type.name)) {
                        args = false;
                        break;
                    }
                }

                if(args) {
                    return fun;
                }
            }
        }

        throw new KayJamRuntimeException(classContainer,
                "Function "+function.name+" not found from implements class");
    }
}