package com.oracle.mxtool.veriopt;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.regex.Pattern;

public class VerioptCommentCheck extends AbstractCheck {
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
        return new int[] {TokenTypes.COMMENT_CONTENT};
    }

    /* We love regex */
    private Pattern commentPattern = Pattern.compile(" veriopt: (.+): (.+?(?= \\|->)) \\|-> (((.+?(?= when)) when (.+))|.+)");

    public void setCommentPattern(Pattern commentPattern) {
        this.commentPattern = commentPattern;
    }

    @Override
    public void visitToken(DetailAST ast) {
        String commentContents = ast.getText();
        if (!commentContents.stripLeading().startsWith("veriopt")) {
            return;
        }

        if (!commentPattern.matcher(commentContents).find()) {
            log(ast.getLineNo(), "Veriopt comment is not valid: " + commentContents);
        }
    }
}
