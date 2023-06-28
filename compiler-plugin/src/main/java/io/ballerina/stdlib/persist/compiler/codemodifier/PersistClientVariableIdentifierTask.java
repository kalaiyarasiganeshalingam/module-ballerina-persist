package io.ballerina.stdlib.persist.compiler.codemodifier;

import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.persist.compiler.model.Variable;
import io.ballerina.stdlib.persist.compiler.utils.Utils;

import java.util.List;

/**
 * Analysis task to identify all declared variables.
 */
public class PersistClientVariableIdentifierTask implements AnalysisTask<SyntaxNodeAnalysisContext> {
    private final List<Variable> variables;

    public PersistClientVariableIdentifierTask(List<Variable> variables) {
        this.variables = variables;
    }

    @Override
    public void perform(SyntaxNodeAnalysisContext ctx) {
        if (Utils.hasCompilationErrors(ctx)) {
            return;
        }
        Node variableDeclarationNode = ctx.node();
        if (variableDeclarationNode instanceof ModuleVariableDeclarationNode) {
            ModuleVariableDeclarationNode moduleVariableNode = (ModuleVariableDeclarationNode) variableDeclarationNode;
            String type = moduleVariableNode.typedBindingPattern().typeDescriptor().toString().trim();
            String variableName = moduleVariableNode.typedBindingPattern().bindingPattern().toString().trim();
            variables.add(new Variable(variableName, type));
        } else if (variableDeclarationNode instanceof VariableDeclarationNode) {
            VariableDeclarationNode moduleVariableNode = (VariableDeclarationNode) variableDeclarationNode;
            String type = moduleVariableNode.typedBindingPattern().typeDescriptor().toString().trim();
            String variableName = moduleVariableNode.typedBindingPattern().bindingPattern().toString().trim();
            variables.add(new Variable(variableName, type));
        }
    }
}
