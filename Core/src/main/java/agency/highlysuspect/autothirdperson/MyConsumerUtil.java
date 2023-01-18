package agency.highlysuspect.autothirdperson;

/**
 * Static methods in interfaces are also not supported yet :cry:
 */
public class MyConsumerUtil {
	private static final MyConsumer<Object> DOES_NOTHING = new MyConsumer<Object>() {
		@Override
		public void accept(Object thing) {
			
		}
	};
	
	@SuppressWarnings("unchecked")
	public static <T> MyConsumer<T> doNothing() {
		return (MyConsumer<T>) DOES_NOTHING;
	}
}
