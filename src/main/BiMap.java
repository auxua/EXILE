package main;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * A simple Bidirectional HashMap Implementation
 * !not Threadsafe
 * 
 * Needs little more space than a simple hashMap but improves the time complexity for several operations.
 * 
 * @author arno
 *
 *
 */
public class BiMap<E,T> implements Map<E,T> {
	private HashMap<E,T> forward = new HashMap<E,T>();
	private HashMap<T,E> backward = new HashMap<T,E>();
	
	public BiMap() {
		super();
	}

	@Override
	public void clear() {
		forward.clear();
		backward.clear();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return forward.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		return backward.containsKey(arg0);
	}

	@Override
	public Set<Entry<E, T>> entrySet() {
		return forward.entrySet();
	}

	@Override
	public T get(Object arg0) {
		return forward.get(arg0);
	}

	@Override
	public boolean isEmpty() {
		return forward.isEmpty();
	}

	@Override
	public Set<E> keySet() {
		return forward.keySet();
	}

	@Override
	public T put(E arg0, T arg1) {
		backward.put(arg1, arg0);
		return forward.put(arg0, arg1);
	}

	@Override
	public void putAll(Map<? extends E, ? extends T> arg0) {
		for (Entry<? extends E, ? extends T> m : arg0.entrySet()) {
			forward.put(m.getKey(), m.getValue());
			backward.put(m.getValue(), m.getKey());
		}
	}

	@Override
	public T remove(Object arg0) {
		backward.remove(forward.get(arg0));
		return forward.remove(arg0);
	}

	@Override
	public int size() {
		return forward.size();
	}

	@Override
	public Collection<T> values() {
		return backward.keySet();
	}
	
	public E getKey(T key) {
		return backward.get(key);
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return forward.toString();
	}



	/**
	 * Very little and simple datastructure to store combined information about a map Position
	 *
	 * @author arno
	 *
	 */
	public static class MyEntry<K> {

		private K x;
		private K y;
		private K z;
		
		public MyEntry(K x, K y, K z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public K getValueX() { return this.x; }
		public K getValueY() { return this.y; }
		public K getValueZ() { return this.z; }

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MyEntry<?> ) {
				if (this.hashCode() == obj.hashCode())
				
				return true;
			}

			return false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			//return "["+this.x+","+this.y+","+this.z+"]"+System.getProperty("line.separator");
			return "["+this.x+","+this.y+","+this.z+"]";
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return ( (x.hashCode()*10000+y.hashCode()*100+z.hashCode()*2) % Integer.MAX_VALUE );
		}
	}
	
}