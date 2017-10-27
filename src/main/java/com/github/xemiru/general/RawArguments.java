package com.github.xemiru.general;

import java.util.Optional;

public class RawArguments {

    private int current;
    private String[] args;

    public RawArguments(String[] args) {
        this.args = args;
        this.current = 0;
    }

    /**
     * Returns the underlying array of raw arguments that was given to this {@link RawArguments} upon instantiation.
     *
     * <p>The array returned is the same array belonging to this object, meaning modifications to it will reflect in the
     * methods of the object. Should usage of this be required, it is recommended to clone the array before usage or at
     * least ensure that it is only being read from and not written to.</p>
     *
     * @return the underlying array of raw arguments
     */
    public String[] getRaw() {
        return this.args;
    }

    /**
     * Drops the earliest {@code n} raw arguments held by this {@link RawArguments}.
     *
     * <p>Passing a number greater than the amount of raw arguments will throw an
     * {@link ArrayIndexOutOfBoundsException}.</p>
     *
     * @param n the amount of arguments to drop
     */
    public RawArguments drop(int n) {
        if (n > this.args.length) throw new ArrayIndexOutOfBoundsException();

        String[] neww = new String[this.args.length - n];
        System.arraycopy(this.args, n, neww, 0, this.args.length - n);
        if (this.current >= n) this.current -= n;
        this.args = neww;
        return this;
    }

    /**
     * @return the index of the value from the underlying array to return upon the next call to {@link #next()}
     */
    public int getNextIndex() {
        return this.current;
    }

    /**
     * Peeks at the next raw argument held by this {@link RawArguments} instance.
     *
     * <p>This method does not advance the internal counter, meaning this will always return the same value until
     * {@link #next()} is called.</p>
     *
     * <p>If there are no more remaining arguments, this returns an empty optional. This method should be checked with
     * in order to safely call {@link #next()}.</p>
     *
     * @return the next parameter, without advancing?
     */
    public Optional<String> peek() {
        return this.peek(1);
    }

    /**
     * Peeks at the next raw argument held by this {@link RawArguments} instance.
     *
     * <p>The {@code ahead} parameter specifies how far to look ahead. As an example, passing 1 will peek at the next
     * argument that one would receive if they called {@link #next()}. This also means passing 0 will retrieve the
     * current argument, -1 will retrieve the previous, etc.</p>
     *
     * <p>This method does not advance the internal counter, meaning this will always return the same value until
     * {@link #next()} is called.</p>
     *
     * <p>If there are no more remaining arguments, this returns an empty optional. This method should be checked with
     * in order to safely call {@link #next()}.</p>
     *
     * @param ahead the offset index to peek at
     * @return the next parameter, without advancing?
     */
    public Optional<String> peek(int ahead) {
        int target = this.current + ahead - 1;
        try {
            return Optional.of(this.args[target]);
        } catch (ArrayIndexOutOfBoundsException wat) {
            return Optional.empty();
        }
    }

    /**
     * Returns the next raw argument held by this {@link RawArguments} instance.
     *
     * <p>This method will advance the internal counter. Consecutive calls on this method will eventually pass all
     * arguments that were passed to this RawArguments instance upon instantiation..</p>
     *
     * <p>It is recommended to query {@link #peek()} before calling this method, as an
     * {@link ArrayIndexOutOfBoundsException} is thrown if the internal counter exceeds the maximum index.</p>
     *
     * @return the next parameter
     */
    public String next() {
        return this.args[this.current++];
    }

    /**
     * Returns the combined set of arguments ranging from {@code start} (inclusive) to {@link #getNextIndex()}
     * (exclusive) as a single String.
     *
     * @param start the start index
     * @return the combined String holding the arguments in range
     */
    public String getParsed(int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < this.current; i++) {
            try {
                sb.append(this.args[i]).append(' ');
            } catch (ArrayIndexOutOfBoundsException e) {
                break;
            }
        }

        return sb.toString().trim();
    }

    /**
     * @return a copy of this {@link RawArguments} object
     */
    public RawArguments copy() {
        RawArguments returned = new RawArguments(this.args);
        returned.current = this.current;
        return returned;
    }
}
