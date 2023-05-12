package agency.highlysuspect.autothirdperson.consumer;

/**
 * Exactly the same as Java 8's Consumer, but Java 6 compatible.
 * @param <T> The type of thing to consume.
 */
public interface MyConsumer<T> {
	void accept(T thing);
}
