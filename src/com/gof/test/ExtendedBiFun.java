package com.gof.test;

import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface

public interface ExtendedBiFun<T, U, R> extends BiFunction<T, U, R>{
	default Function<U, R> fixedX(T t){
		return u-> apply(t,u);
	}
	default Function<T, R> fixedY(U u){
		return t -> apply(t,u);
	}
	
	default <V> ExtendedBiFun<V, U, R> composeX( Function<? super V, ? extends T> before){
		return (v, u) -> apply(before.apply(v), u);
		
	}
	
	default <V> ExtendedBiFun<T, V, R> composeY( Function<? super V, ? extends U> before){
		return (t, v) -> apply(t, before.apply(v));
		
	}
}
