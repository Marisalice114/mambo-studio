package com.hachimi.mamboaiplatform.constant;

import java.util.regex.Pattern;

public interface CodePattern {

    // 匹配HTML代码块的正则表达式
    static final Pattern HTML_PATTERN = Pattern.compile(
            "```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE
    );

    // 匹配CSS代码块的正则表达式
    static final Pattern CSS_PATTERN = Pattern.compile(
            "```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE
    );

    // 匹配JavaScript代码块的正则表达式
    static final Pattern JS_PATTERN = Pattern.compile(
            "```(?:javascript|js)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE
    );

}
