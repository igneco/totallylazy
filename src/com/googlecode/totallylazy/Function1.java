package com.googlecode.totallylazy;

import com.googlecode.totallylazy.callables.LazyCallable1;
import com.googlecode.totallylazy.callables.SleepyCallable1;

import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.LazyException.lazyException;
import static com.googlecode.totallylazy.Pair.pair;

public abstract class Function1<A, B> implements Callable1<A, B>, Mappable<B, Function1<A, ?>> {
    public static <A, B> Function1<A, B> function(final Callable1<? super A, ? extends B> callable) {
        return new Function1<A, B>() {
            @Override
            public B call(A a) throws Exception {
                return callable.call(a);
            }
        };
    }

    public B apply(final A a) {
        return Function1.call(this, a);
    }

    public static <A, B> B call(final Callable1<? super A, ? extends B> callable, final A a) {
        try {
            return callable.call(a);
        } catch (Exception e) {
            throw lazyException(e);
        }
    }

    public Function<B> deferApply(final A a) {
        return Callables.deferApply(this, a);
    }

    public Function1<A, B> lazy() {
        return LazyCallable1.lazy(this);
    }

    public Function1<A, B> sleep(int millis) {
        return SleepyCallable1.sleepy(this, millis);
    }

    public Function1<A, Option<B>> optional() {
        return Exceptions.optional(this);
    }

    public Function1<A, Either<Exception, B>> either() {
        return Exceptions.either(this);
    }

    @Override
    public <C> Function1<A, C> map(final Callable1<? super B, ? extends C> callable) {
        return Callables.compose(this, callable);
    }

    public <C> Function1<A, C> then(final Callable1<? super B, ? extends C> callable) {
        return map(callable);
    }

    public <C> Function1<A, C> then(final Callable<? extends C> callable) {
        return Callables.compose(this, callable);
    }

    public Function1<A,B> interruptable() {
        return Callables.interruptable(this);
    }

    public Function1<A, Function<B>> deferExecution() {
        return Callables.deferReturn(this);
    }

    public Function1<A, Pair<B, A>> asFirst() {
        return new Function1<A, Pair<B, A>>() {
            public Pair<B, A> call(A original) throws Exception {
                return pair(Function1.this.apply(original), original);
            }
        };
    }

    public static <A,B> Function1<A, B> returns1(final B result) {
        return new Function1<A, B>() {
            public B call(A ignore) throws Exception {
                return result;
            }
        };
    }
}
