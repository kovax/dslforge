package org.beedom.dslforge.ast.local

import org.beedom.dslforge.DSLEngine
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation


@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class UseRuntimeDSLTransformation implements ASTTransformation {

    /**
	 *
	 * @param astNodes
	 * @param sourceUnit
	 */
    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {

        if (!checkNode(astNodes, UseRuntimeDSL.class.name)) {
            addError("Internal Error: wrong arguments", astNodes[0], sourceUnit)
            return
        }

        MethodNode annotatedMethod = astNodes[1]

        ClosureExpression cl = new ClosureExpression(
			annotatedMethod.parameters,
			new BlockStatement(annotatedMethod.code.statements as Statement[], annotatedMethod.code.variableScope.copy())
		)
		cl.variableScope = cl.code.variableScope

        List existingStatements = annotatedMethod.code.statements

        existingStatements.clear()

        existingStatements.add(initDSLE())
        existingStatements.add(callDSLERun(cl))
    }


	/**
	 * 
	 * @param astNodes
	 * @param className
	 * @return
	 */
	private boolean checkNode(ASTNode[] astNodes, String className) {
		if (! astNodes) return false
		if (! astNodes[0]) return false
		if (! astNodes[1]) return false
		if (!(astNodes[0] instanceof AnnotationNode)) return false
		if (! astNodes[0].classNode?.name == className) return false
		if (!(astNodes[1] instanceof MethodNode)) return false

		return true
	}


	/**
	 * 
	 * @return
	 */
	public Statement initDSLE() {
		return new ExpressionStatement(
			new DeclarationExpression(
				new VariableExpression("dsle"),
				new Token(Types.ASSIGNMENT_OPERATOR, "=", -1, -1),
				new ConstructorCallExpression(ClassHelper.make(DSLEngine.class), new ArgumentListExpression())
			)
		)
	}
	
	
	/**
	 * 
	 * @param cl
	 * @return
	 */
    private Statement callDSLERun(Expression cl) {
        return new ExpressionStatement(
            new MethodCallExpression(
                new VariableExpression("dsle"), 
				"run", 
				new ArgumentListExpression( cl )
            )
        )
    }


    /**
     *
     * @param message
     * @return
     */
    private Statement createPrintlnCall(String message) {
        return new ExpressionStatement(
            new MethodCallExpression(
                new VariableExpression("this"),
                new ConstantExpression("println"),
                new ArgumentListExpression(
                    new ConstantExpression(message)
                )
            )
        )
    }


    /**
     *
     * @param msg
     * @param node
     * @param source
     */
    public void addError(String msg, ASTNode node, SourceUnit source) {
        int line = node.lineNumber
        int col = node.columnNumber

        SyntaxException se = new SyntaxException(msg + '\n', line, col)
        SyntaxErrorMessage sem = new SyntaxErrorMessage(se, source)
        source.errorCollector.addErrorAndContinue(sem)
    }
}
