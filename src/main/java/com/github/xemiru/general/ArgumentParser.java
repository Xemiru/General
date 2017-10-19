package com.github.xemiru.general;

import com.github.xemiru.general.exception.ParseException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Parses raw tokens into usable parameters.
 *
 * @param <T> the parameter type resolved by the {@link ArgumentParser}
 */
public interface ArgumentParser<T> {

    /**
     * Returns the typename of the resulting kind of value returned by this {@link ArgumentParser}.
     *
     * <p>Essentially, a human-friendly name for {@link ArgumentParser <T>}. This typename appears in any syntax error
     * messages that result from this parser failing to correctly produce a result.</p>
     *
     * <p>Parsers in {@link ArgumentParsers} establish a common format. New implementations should try to follow.</p>
     * <ul>
     * <li>parsers with choices have choices separated using the '\u007C' character ({@code u007C})</li>
     * <li>parsers taking the remaining tokens have " .." appended to the end of their typename</li>
     * </ul>
     *
     * <p>When syntax is generated, angle brackets (<, >) surround non-optional parsers' typenames. Parsers who have a
     * default value, either a non-empty {@link #getDefaultToken()} or {@link #getDefaultParameter()}, have their
     * typename surrounded by square brackets ([, ]). If a default token is present (and a default parameter is not
     * present and therefore did not take priority), the typename is appended to with an equals sign '\u003D'
     * ({@code u003D}) followed by the default token.</p>
     *
     * @return the typename of this ArgumentParser's resulting value type
     */
    String getTypename();

    /**
     * @return a token to pass to this {@link ArgumentParser} should no tokens be available for consumption?
     */
    default Optional<String> getDefaultToken() {
        return Optional.empty();
    }

    /**
     * Returns a token to return should no tokens be available for consumption.
     *
     * <p>This takes priority over {@link #getDefaultToken()}.</p>
     *
     * @return a token to return should no tokens be available for consumption?
     */
    default Optional<T> getDefaultParameter() {
        return Optional.empty();
    }

    /**
     * Attempts to parse remaining arguments within the passed {@link RawArguments} instance into a usable value.
     *
     * <p>Should issues occur with parsing, one should throw a {@link ParseException} to signify that something went
     * wrong. It is considered an internal error should anything other than a ParseException be thrown from this
     * method.</p>
     *
     * <p>If only one argument is consumed, it is safe to not peek through the arguments object as the object is
     * guaranteed to always have at least one parameter available when passed to this method.</p>
     *
     * @param args the RawArguments to parse
     * @return a value
     */
    T parse(RawArguments args);

    /**
     * Returns suggestions for tab completion.
     *
     * <p>This can return null or an empty list to signify no suggestions.</p>
     *
     * @return a list of suggestions for tab completion
     */
    default Set<String> getSuggestions() { return null; }

}
