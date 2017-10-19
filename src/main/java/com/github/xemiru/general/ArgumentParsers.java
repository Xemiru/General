package com.github.xemiru.general;

import com.github.xemiru.general.exception.ParseException;
import com.github.xemiru.general.exception.SyntaxException;

import java.util.*;

/**
 * A collection of {@link ArgumentParsers} to use during command handling.
 *
 * <p>It is best to statically import this class for use in implementations of {@link CommandExecutor}.</p>
 */
public class ArgumentParsers {

    /**
     * Parses text input. The parser allows strings with whitespace if it receives unescaped single or double quotes.
     */
    public static final ArgumentParser<String> STRING = new ArgumentParser<String>() {
        @Override
        public String getTypename() {
            return "string";
        }

        @Override
        public String parse(RawArguments args) {
            String out = args.peek().get();
            if (out.isEmpty()) return args.next();

            char qc = out.charAt(0);
            if (qc == '"' || qc == '\'') {
                boolean in = false;
                StringBuilder sb = new StringBuilder();
                while (args.peek().isPresent()) {
                    String next = args.next();
                    int len = next.length();

                    sb.append(' ');
                    for (int i = 0; i < len; i++) {
                        char ch = next.charAt(i);
                        if (ch == '\\' && i + 1 < len && next.charAt(i + 1) == qc) {
                            sb.append(qc);
                            i++;
                        } else if (ch == qc) {
                            sb.append(qc);
                            if (!in) in = true;
                            else {
                                if (i != len - 1) throw new ParseException("invalid end of quoted string: " + next);
                                break;
                            }
                        } else {
                            sb.append(ch);
                        }
                    }
                }

                out = sb.toString().trim();
                if (out.endsWith("\\" + qc) || !out.endsWith(qc + ""))
                    throw new ParseException("unfinished quoted string: " + out);

                return out.substring(1, out.length() - 1);
            }

            return args.next();
        }

    };

    /**
     * Parses text input. The parser will consume all remaining tokens of the raw arguments passed to it. Quotes are not
     * considered special, unlike {@link #STRING}.
     */
    public static final ArgumentParser<String> REMAINING_STRING = new ArgumentParser<String>() {

        @Override
        public String getTypename() {
            return "text ..";
        }

        @Override
        public String parse(RawArguments args) {
            StringBuilder sb = new StringBuilder();
            while (args.peek().isPresent()) sb.append(' ').append(args.next());

            return sb.toString().trim();
        }

    };

    /**
     * Parses simple numerical input.
     *
     * <p>The returned value is a double, but is returned as a Number for convenience.</p>
     */
    public static final ArgumentParser<Number> NUMBER = new ArgumentParser<Number>() {

        @Override
        public String getTypename() {
            return "number";
        }

        @Override
        public Number parse(RawArguments args) {
            String parsed = args.next();

            try {
                return Double.parseDouble(parsed);
            } catch (NumberFormatException e) {
                throw new ParseException("not a number: " + parsed);
            }
        }

    };

    /**
     * Parses simple integer input.
     */
    public static final ArgumentParser<Integer> INTEGER = new ArgumentParser<Integer>() {

        @Override
        public String getTypename() {
            return "integer";
        }

        @Override
        public Integer parse(RawArguments args) {
            String parsed = args.next();

            try {
                return Integer.parseInt(parsed);
            } catch (NumberFormatException e) {
                throw new ParseException("not an integer: " + parsed);
            }
        }

    };

    /**
     * Generates an {@link ArgumentParser} accepting only the given choices.
     *
     * <p>Choice selection is defined by strings to allow the typename to be generated and allow tab-completion against
     * user input. Selection is case-insensitive, though the parser will always receive the exact string defined by
     * choices passed to this method; user input is simply matched. The choices you give are passed to the provided
     * parser to turn into usable tokens.</p>
     *
     * @param parser the ArgumentParser used to parse choices into a usable type
     * @param choice the first choice
     * @param moreChoices more choices
     * @param <T> the type of the choice objects
     * @return an ArgumentParser accepting only the choices given as valid tokens
     */
    public static <T> ArgumentParser<T> anyOf(ArgumentParser<T> parser, String choice, String... moreChoices) {
        return new ArgumentParser<T>() {

            private Set<String> selection;
            private String typename;

            {
                this.selection = new HashSet<>();
                this.selection.add(choice);
                this.selection.addAll(Arrays.asList(moreChoices));

                if (moreChoices.length == 0) {
                    this.typename = choice;
                } else {
                    StringBuilder sb = new StringBuilder(choice);
                    for (String moreChoice : moreChoices) sb.append('|').append(moreChoice);
                    this.typename = sb.toString().trim();
                }
            }

            @Override
            public String getTypename() {
                return this.typename;
            }

            @Override
            public T parse(RawArguments args) {
                String str = STRING.parse(args);
                for (String sel : this.selection) {
                    if (sel.equalsIgnoreCase(str)) return parser.parse(new RawArguments(new String[]{str}));
                }

                throw new ParseException("unknown choice: " + str);
            }

            @Override
            public Set<String> getSuggestions() {
                return new LinkedHashSet<>(this.selection);
            }
        };
    }

    /**
     * Generates an {@link ArgumentParser} that could resolve any parameter type with a corresponding parser.
     *
     * <p>The resolution type of the parser is Object. It is up to the consumer of the parameter to check the resulting
     * type in order to create a branching path.</p>
     *
     * <p>Note that the value is returned immediately by the first successful parser. The parsers are given input to
     * evaluate in the order they were given to this method. This means one should try not to pass a parser that pretty
     * much always succeeds as the first parser (e.g. {@link #STRING}).</p>
     *
     * @param parserA a parser
     * @param parserB another parser
     * @param moreParsers even more parsers
     * @return an ArgumentParser that may resolve various types
     */
    public static ArgumentParser<Object> or(ArgumentParser<?> parserA, ArgumentParser<?> parserB, ArgumentParser<?>... moreParsers) {
        return new ArgumentParser<Object>() {

            private String typename;
            private ArgumentParser<?>[] parsers;

            {
                this.parsers = new ArgumentParser<?>[2 + moreParsers.length];
                parsers[0] = parserA;
                parsers[1] = parserB;
                System.arraycopy(moreParsers, 0, parsers, 2, moreParsers.length);

                StringBuilder sb = new StringBuilder(this.parsers[0].getTypename());
                for (int i = 1; i < this.parsers.length; i++) sb.append('|').append(this.parsers[i].getTypename());
                this.typename = sb.toString().trim();
            }

            @Override
            public String getTypename() {
                return this.typename;
            }

            @Override
            public Object parse(RawArguments args) {
                StringBuilder errors = new StringBuilder();
                for (ArgumentParser<?> parser : parsers) {
                    try {
                        parser.parse(args.copy());
                        // if the copy succeeds, commit the changes with the original and return it
                        return parser.parse(args);
                    } catch (ParseException e) {
                        errors.append(e.getMessage()).append('\n');
                    }
                }

                throw new ParseException(errors.toString().trim());
            }

            @Override
            public Set<String> getSuggestions() {
                Set<String> completion = new LinkedHashSet<>();
                for (ArgumentParser<?> parser : this.parsers) completion.addAll(parser.getSuggestions());
                return completion;
            }

        };
    }

    /**
     * Generates an {@link ArgumentParser} that consumes all remaining tokens using the provided parser.
     *
     * @param parser the parser to consume tokens with
     * @param <T> the resolution type of the parser
     * @return an ArgumentParser returning a list of parameters resolved from remaining tokens passed to it
     */
    public static <T> ArgumentParser<List<T>> remain(ArgumentParser<T> parser) {
        return new ArgumentParser<List<T>>() {
            @Override
            public String getTypename() {
                return parser.getTypename() + "..";
            }

            @Override
            public List<T> parse(RawArguments args) {
                List<T> list = new ArrayList<>();
                while (args.peek().isPresent()) list.add(parser.parse(args));

                return list;
            }

            @Override
            public Set<String> getSuggestions() {
                return parser.getSuggestions();
            }
        };
    }

    /**
     * Generates an {@link ArgumentParser} that consumes the provided default token if it was not provided with any
     * tokens to begin with.
     *
     * @param parser the parser to consume tokens with
     * @param def the default token to pass to the parser if no tokens are available
     * @param <T> the resolution type of the parser
     * @return an ArgumentParser resolving an optional parameter
     */
    public static <T> ArgumentParser<T> opt(ArgumentParser<T> parser, String def) {
        return new ArgumentParser<T>() {
            @Override
            public Optional<String> getDefaultToken() {
                return Optional.ofNullable(def);
            }

            @Override
            public String getTypename() {
                return parser.getTypename();
            }

            @Override
            public T parse(RawArguments args) {
                return parser.parse(args);
            }

            @Override
            public Set<String> getSuggestions() {
                return parser.getSuggestions();
            }
        };
    }

    /**
     * Generates an {@link ArgumentParser} that returns the provided default parameter if it was not provided with any
     * tokens to begin with.
     *
     * @param parser the parser to consume tokens with
     * @param def the default token to pass to the parser if no tokens are available
     * @param <T> the resolution type of the parser
     * @return an ArgumentParser resolving an optional parameter
     */
    public static <T> ArgumentParser<T> alt(ArgumentParser<T> parser, T def) {
        return new ArgumentParser<T>() {
            @Override
            public Optional<T> getDefaultParameter() {
                return Optional.ofNullable(def);
            }

            @Override
            public String getTypename() {
                return parser.getTypename();
            }

            @Override
            public T parse(RawArguments args) {
                return parser.parse(args);
            }

            @Override
            public Set<String> getSuggestions() {
                return parser.getSuggestions();
            }
        };
    }

    /**
     * Generates an {@link ArgumentParser} that excuses {@link ParseException}s that result in {@link SyntaxException}s
     * by passing a fallback token for the parser to consume should the former occur.
     *
     * <p>Be warned of the side-effects of ignoring syntax exceptions. The {@link RawArguments} object is not rolled
     * back when one is encountered, meaning a token that's part of a larger parameter (e.g. a quoted string) may be
     * received by the next parser.</p>
     *
     * @param parser the parser to consume tokens with
     * @param def the default token to pass to the parser if a SyntaxException would otherwise occur
     * @param <T> the resolution type of the parser
     * @return a lenient ArgumentParser
     */
    public static <T> ArgumentParser<T> lenient(ArgumentParser<T> parser, String def) {
        return new ArgumentParser<T>() {
            @Override
            public Optional<String> getDefaultToken() {
                return Optional.ofNullable(def);
            }

            @Override
            public String getTypename() {
                return parser.getTypename();
            }

            @Override
            public T parse(RawArguments args) {
                try {
                    return parser.parse(args);
                } catch (ParseException e) {
                    return parser.parse(new RawArguments(new String[]{def}));
                }
            }

            @Override
            public Set<String> getSuggestions() {
                return parser.getSuggestions();
            }
        };
    }

    /**
     * Generates an {@link ArgumentParser} that excuses {@link ParseException}s that result in {@link SyntaxException}s
     * by instead returning a fallback parameter should the former occur.
     *
     * <p>Be warned of the side-effects of ignoring syntax exceptions. The {@link RawArguments} object is not rolled
     * back when one is encountered, meaning a token that's part of a larger parameter (e.g. a quoted string) may be
     * received by the next parser.</p>
     *
     * @param parser the parser to consume tokens with
     * @param def the default parameter to return if a SyntaxException would otherwise occur
     * @param <T> the resolution type of the parser
     * @return a lenient ArgumentParser
     */
    public static <T> ArgumentParser<T> fallback(ArgumentParser<T> parser, T def) {
        return new ArgumentParser<T>() {
            @Override
            public Optional<T> getDefaultParameter() {
                return Optional.ofNullable(def);
            }

            @Override
            public String getTypename() {
                return parser.getTypename();
            }

            @Override
            public T parse(RawArguments args) {
                try {
                    return parser.parse(args);
                } catch (ParseException e) {
                    return def;
                }
            }

            @Override
            public Set<String> getSuggestions() {
                return parser.getSuggestions();
            }
        };
    }

    /**
     * Generates an {@link ArgumentParser} that is functionally identical to the one provided, with the only difference
     * being the typename.
     *
     * @param parser the ArgumentParser to wrap around
     * @param typename the new typename to return
     * @param <T> the resolution type of the parser
     * @return a functionally identical ArgumentParser with a new typename
     */
    public static <T> ArgumentParser<T> rename(ArgumentParser<T> parser, String typename) {
        return new ArgumentParser<T>() {
            @Override
            public Optional<String> getDefaultToken() {
                return parser.getDefaultToken();
            }

            @Override
            public Optional<T> getDefaultParameter() {
                return parser.getDefaultParameter();
            }

            @Override
            public String getTypename() {
                return typename;
            }

            @Override
            public T parse(RawArguments args) {
                return parser.parse(args);
            }

            @Override
            public Set<String> getSuggestions() {
                return parser.getSuggestions();
            }
        };
    }

}
