package population.util;

/**
 * provide public "clone" method
 * @param <T>
 */
public interface Cloneable<T extends Cloneable<T>> extends java.lang.Cloneable {
    T clone();
}
