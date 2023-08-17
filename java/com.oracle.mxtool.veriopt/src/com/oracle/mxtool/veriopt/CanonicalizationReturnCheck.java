package com.oracle.mxtool.veriopt;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;

public class CanonicalizationReturnCheck extends AbstractCheck {
    private String[] methods = new String[]{"canonicalize"};

    @Override
    public boolean isCommentNodesRequired() {
        return true;
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {
                TokenTypes.METHOD_DEF,
                TokenTypes.LITERAL_RETURN,
        };
    }

    public void setMethods(String[] methods) {
        this.methods = methods;
    }

    private boolean isChecking = false;

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.METHOD_DEF:
                visitMethod(ast);
                break;
            case TokenTypes.LITERAL_RETURN:
                visitReturn(ast);
                break;
        }
    }

    private void visitReturn(DetailAST ast) {
        if (!isChecking) {
            return;
        }

        int nextToken = ast.getFirstChild().getType();

        // ignore void returns
        if (nextToken == TokenTypes.SEMI) {
            return;
        }

        // ignore no-op canonicalizations
        if (nextToken == TokenTypes.LITERAL_NULL) {
            return;
        }

        DetailAST lastAST = ast.getPreviousSibling();
        if (lastAST == null
                || (lastAST.getType() != TokenTypes.SINGLE_LINE_COMMENT
                    && lastAST.getType() != TokenTypes.BLOCK_COMMENT_BEGIN)) {
            log(ast, "Canonicalization not documented");
            return;
        }

        DetailAST commentContent = lastAST.findFirstToken(TokenTypes.COMMENT_CONTENT);
        if (!commentContent.getText().trim().startsWith("veriopt")) {
            log(lastAST, "Canonicalization documentation not in correct form: " + commentContent.getText());
        }
    }

    public void visitMethod(DetailAST ast) {
        DetailAST methodName = ast.findFirstToken(TokenTypes.IDENT);
        if (Arrays.asList(methods).contains(methodName.getText())) {
            isChecking = true;
        }
    }

    @Override
    public void leaveToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.METHOD_DEF:
                isChecking = false;
                break;
        }
    }
}
