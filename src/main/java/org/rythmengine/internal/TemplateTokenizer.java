/**
 * Copyright (C) 2013-2016 The Rythm Engine project
 * for LICENSE and other details see:
 * https://github.com/rythmengine/rythmengine
 */
package org.rythmengine.internal;

import org.rythmengine.RythmEngine;
import org.rythmengine.conf.RythmConfiguration;
import org.rythmengine.internal.parser.IRemoveLeadingLineBreakAndSpaces;
import org.rythmengine.internal.parser.IRemoveLeadingSpacesIfLineBreak;
import org.rythmengine.internal.parser.ParserBase;
import org.rythmengine.internal.parser.ParserDispatcher;
import org.rythmengine.internal.parser.build_in.*;
import org.rythmengine.logger.ILogger;
import org.rythmengine.logger.Logger;
import org.rythmengine.utils.F;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TemplateTokenizer implements Iterable<Token> {
    private final static ILogger logger = Logger.get(TemplateTokenizer.class);
    private IContext ctx;
    private List<IParser> parsers = new ArrayList<IParser>();
    private int lastCursor = 0;

    public TemplateTokenizer(IContext context) {
        ctx = context;
        RythmEngine engine = ctx.getEngine();
        RythmConfiguration conf = engine.conf();
        if ((conf.smartEscapeEnabled() || conf.naturalTemplateEnabled()) && engine.extensionManager().hasTemplateLangs()) {
            parsers.add(new CodeTypeBlockStartSensor(ctx));
            parsers.add(new CodeTypeBlockEndSensor(ctx));
        }
        if (conf.naturalTemplateEnabled() && engine.extensionManager().hasTemplateLangs()) {
            parsers.add(new DirectiveCommentStartSensor(ctx));
            parsers.add(new DirectiveCommentEndSensor(ctx));
        }
        parsers.add(new ParserDispatcher(ctx));
        parsers.add(new BlockCloseParser(ctx));
        parsers.add(new ScriptParser(ctx));
        parsers.add(new StringTokenParser(ctx));
        // add a fail through parser to prevent unlimited loop
        parsers.add(new ParserBase(ctx) {
            @Override
            public Token go() {
                TemplateParser p = (TemplateParser) ctx();
                if (lastCursor < p.cursor) return null;
                //logger.warn("fail-through parser reached. is there anything wrong in your template? line: %s", ctx.currentLine());
                String oneStep = p.getRemain().substring(0, 1);
                p.step(1);
                return new Token.StringToken(oneStep, p);
            }
        });
    }

    @Override
    public Iterator<Token> iterator() {
        return new Iterator<Token>() {

            @Override
            public boolean hasNext() {
                return ctx.hasRemain();
            }

            @Override
            public Token next() {
                for (IParser p : parsers) {
                    Token t;
                    F.T2<IParser, Token> t2 = null;
                    if (p instanceof ParserDispatcher) {
                        t2 = ((ParserDispatcher) p).go2();
                        t = null == t2 ? null : t2._2;
                    } else {
                        t = p.go();
                    }
                    
                    if (null != t) {
                        if (null != t2) {
                            p = t2._1;
                        }
                        IContext ctx = p.ctx();
                        CodeBuilder cb = ctx.getCodeBuilder();
                        if (p instanceof IRemoveLeadingLineBreakAndSpaces) {
                            cb.removeSpaceToLastLineBreak(ctx);
                        } else if (p instanceof IRemoveLeadingSpacesIfLineBreak) {
                            cb.removeSpaceTillLastLineBreak(ctx);
                        }
                        lastCursor = ((TemplateParser) ctx).cursor;
                        return t;
                    }
                }
                throw new RuntimeException("Internal error");
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }
}
