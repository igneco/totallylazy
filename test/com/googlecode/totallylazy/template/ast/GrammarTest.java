package com.googlecode.totallylazy.template.ast;

import org.junit.Test;

import static com.googlecode.totallylazy.Assert.assertThat;
import static com.googlecode.totallylazy.Lists.list;
import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.predicates.Predicates.instanceOf;
import static com.googlecode.totallylazy.predicates.Predicates.is;
import static com.googlecode.totallylazy.template.ast.Attribute.attribute;
import static com.googlecode.totallylazy.template.ast.FunctionCall.functionCall;
import static com.googlecode.totallylazy.template.ast.Grammar.Default;
import static com.googlecode.totallylazy.template.ast.ImplicitArguments.implicitArguments;
import static com.googlecode.totallylazy.template.ast.Indirection.indirection;
import static com.googlecode.totallylazy.template.ast.Name.name;
import static com.googlecode.totallylazy.template.ast.NamedArguments.namedArguments;
import static com.googlecode.totallylazy.template.ast.Text.text;

public class GrammarTest {
    @Test
    public void canParseSingleAttribute() throws Exception {
        Attribute attribute = Default.ATTRIBUTE.parse("foo").value();
        assertThat(attribute, is(attribute(name("foo"))));
    }

    @Test
    public void canParseAttributeWithSubAttribute() throws Exception {
        Attribute attribute = Default.ATTRIBUTE.parse("foo.bar").value();
        assertThat(attribute, is(attribute(name("foo"), name("bar"))));
    }

    @Test
    public void canParseText() throws Exception {
        Text text = Default.TEXT().parse("Some other text").value();
        assertThat(text, is(text("Some other text")));
    }

    @Test
    public void literalCanBeSingleOrDoubleQuoted() throws Exception {
        assertThat(Default.LITERAL.parse("\"foo\"").value(), is(text("foo")));
        assertThat(Default.LITERAL.parse("'foo'").value(), is(text("foo")));
    }

    @Test
    public void canParseAnExpression() throws Exception {
        assertThat(Default.EXPRESSION.parse("$template()$").value(), instanceOf(FunctionCall.class));
        assertThat(Default.EXPRESSION.parse("$template$").value(), instanceOf(Attribute.class));
    }

    @Test
    public void canParseFunctionCallWithNoParameters() throws Exception {
        FunctionCall unnamed = Default.FUNCTION_CALL.parse("template()").value();
        assertThat(unnamed, is(functionCall(name("template"), implicitArguments())));
    }

    @Test
    public void canParseFunctionCallWithNamedParameters() throws Exception {
        FunctionCall functionCall = Default.FUNCTION_CALL.parse("template(foo=bar, baz=dan)").value();
        assertThat(functionCall, is(functionCall(name("template"),
                namedArguments(map("foo", attribute(name("bar")), "baz", attribute(name("dan")))))));
    }

    @Test
    public void canParseFunctionCallImplicitParameters() throws Exception {
        FunctionCall functionCall = Default.FUNCTION_CALL.parse("template(foo, bar, baz)").value();
        assertThat(functionCall, is(functionCall(name("template"),
                implicitArguments(attribute(name("foo")), attribute(name("bar")), attribute(name("baz"))))));
    }

    @Test
    public void canParseFunctionCallLiteralParameters() throws Exception {
        FunctionCall functionCall = Default.FUNCTION_CALL.parse("template(\"foo\")").value();
        assertThat(functionCall, is(functionCall(name("template"), implicitArguments(text("foo")))));
    }

    @Test
    public void canParseImplicits() throws Exception {
        assertThat(Default.IMPLICIT_ARGUMENTS.parse("a").value(), is(implicitArguments(attribute(name("a")))));
        assertThat(Default.IMPLICIT_ARGUMENTS.parse("a,b").value(), is(implicitArguments(attribute(name("a")), attribute(name("b")))));
        assertThat(Default.IMPLICIT_ARGUMENTS.parse("\"a\"").value(), is(implicitArguments(text("a"))));
    }

    @Test
    public void canParseValue() throws Exception {
        assertThat(Default.VALUE.parse("a").value(), instanceOf(Attribute.class));
        assertThat(Default.VALUE.parse("\"a\"").value(), instanceOf(Text.class));
    }

    @Test
    public void canParseLiteral() throws Exception {
        Text text = Default.LITERAL.parse("\"Some other text\"").value();
        assertThat(text, is(text("Some other text")));
    }

    @Test
    public void canParseAnonymousTemplate() throws Exception {
        Anonymous template = Default.ANONYMOUS_TEMPLATE.parse("{ name | Hello $name$ }").value();
        assertThat(template.paramaeterNames(), is(list("name")));
    }

    @Test
    public void anonymousTemplateCanHaveNoArguments() throws Exception {
        Anonymous template = Default.ANONYMOUS_TEMPLATE.parse("{Hello $name$ }").value();
        assertThat(template.paramaeterNames(), is(list()));
    }

    @Test
    public void supportsMapping() throws Exception {
        Mapping mapping = Default.MAPPING.parse("users:{ user | Hello $user$ }").value();
        assertThat(mapping.attribute(), is(attribute(name("users"))));
        assertThat(mapping.expression(), is(instanceOf(Anonymous.class)));
    }

    @Test
    public void supportsIndirectionInFunctionCall() throws Exception {
        FunctionCall functionCall = Default.FUNCTION_CALL.parse("(template)()").value();
        assertThat(functionCall, is(functionCall(indirection(attribute(name("template"))), implicitArguments())));
    }

    @Test
    public void supportsIndirectionInAttribute() throws Exception {
        Attribute result = Default.ATTRIBUTE.parse("root.('parent').child").value();
        assertThat(result, is(attribute(name("root"), indirection(text("parent")), name("child"))));
    }
}